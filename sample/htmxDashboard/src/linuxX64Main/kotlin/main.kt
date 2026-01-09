import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.api.containers
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.body
import routes.handleLogsStream
import ui.renderContainerInspectFragment
import ui.renderDashboardPage

fun main() {
    val dockerClient = DockerClient()
    embeddedServer(CIO, configure = {
        reuseAddress = true
        connector {
            port = 8080
        }
    }) {
        routing {
            get("/") {
                val containers = dockerClient.containers.getList().getOrNull() ?: emptyList()
                call.respondHtml {
                    renderDashboardPage(containers)
                }
            }

            route("/containers/{id}") {
                get {
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val info = dockerClient.containers.getInfo(id).getOrNull()
                    call.respondHtml {
                        body { renderContainerInspectFragment(id, info) }
                    }
                }

                get("/logs") {
                    handleLogsStream(dockerClient)
                }
            }
        }
    }.start(wait = true)
}
