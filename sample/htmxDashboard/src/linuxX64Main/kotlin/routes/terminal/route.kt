package routes.terminal

import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.api.containers
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import logger
import routes.respondSmart

fun Routing.terminalRoute(dockerClient: DockerClient) {
    route("/containers/{id}/terminal") {
        get {
            val id = call.parameters["id"]!!
            val info = dockerClient.containers.getInfo(id).getOrNull()
            respondSmart("Terminal") {
                renderTerminal(id, info)
            }
        }

        webSocket("/ws") {
            val containerId = call.parameters["id"]!!

            val session = dockerClient.containers.attach(
                id = containerId,
                stdin = true,
                stdout = true,
                stderr = true,
                stream = true,
                logs = true,
            ).getOrThrow()

            // Start
            dockerClient.containers.start(containerId).getOrThrow()

            val input = incoming.consumeAsFlow().map {
                if (it is Frame.Text) {
                    it.readText()
                } else {
                    ""
                }
            }

            val job = launch {
                val buffer = ByteArray(8192)
                val channel = session.connection.read
                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer)
                    logger.debug { "Read $bytesRead bytes" }
                    send(
                        Frame.Binary(
                            true,
                            buffer.copyOfRange(0, bytesRead)
                        )
                    )
                }
                close(CloseReason(CloseReason.Codes.NORMAL, "Container finished"))
            }

            input.onCompletion {
                session.close()
            }.collect {
                session.send(it)
            }

            job.join()
        }
    }
}