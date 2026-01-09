package routes

import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.api.containers
import dev.limebeck.libs.docker.client.model.ContainerLogsParameters
import dev.limebeck.libs.docker.client.model.LogLine
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*

suspend fun RoutingContext.handleLogsStream(dockerClient: DockerClient) {
    val id = call.parameters["id"] ?: return call.respond(HttpStatusCode.BadRequest)
    call.response.cacheControl(CacheControl.NoCache(null))
    call.respondBytesWriter(contentType = ContentType.Text.EventStream) {
        try {
            val logsFlow = dockerClient.containers.getLogs(
                id = id,
                parameters = ContainerLogsParameters(follow = true, stdout = true, stderr = true, tail = "20")
            ).getOrThrow()

            logsFlow.collect { log ->
                val color = if (log.type == LogLine.Type.STDERR) "text-red-400" else "text-gray-400"
                val html = "<div class='leading-relaxed'><span class='$color'>${log.line.escapeHtml()}</span></div>"
                writeStringUtf8("data: $html\n\n")
                flush()
            }
        } catch (e: Exception) {
            writeStringUtf8("data: <div class='text-orange-500 italic'>Stream disconnected</div>\n\n")
            flush()
        }
    }
}

fun String.escapeHtml() = replace("<", "&lt;").replace(">", "&gt;")
