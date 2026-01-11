package routes.exec

import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.api.containers
import dev.limebeck.libs.docker.client.api.exec
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

fun Routing.execRoute(dockerClient: DockerClient) {
    route("/exec") {
        get("/{execId}") {
            val execId = call.parameters["execId"]!!
            val execInfo = dockerClient.exec.getInfo(execId).getOrThrow()
            val containerId = execInfo.containerID!!
            val info = dockerClient.containers.getInfo(containerId).getOrNull()
            respondSmart("Exec") {
                renderExecTerminal(containerId, execId, info)
            }
        }

        webSocket("/{execId}/ws") {
            val execId = call.parameters["execId"]!!

            val execConnection = dockerClient.exec.startInteractive(execId).getOrThrow()

            val input = incoming.consumeAsFlow().map {
                if (it is Frame.Text) {
                    it.readText()
                } else {
                    ""
                }
            }

            val job = launch {
                val buffer = ByteArray(8192)
                val channel = execConnection.connection.read
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
                close(CloseReason(CloseReason.Codes.NORMAL, "Exec finished"))
            }

            input.onCompletion {
                execConnection.close()
            }.collect {
                execConnection.send(it)
            }

            job.join()
        }
    }
}
