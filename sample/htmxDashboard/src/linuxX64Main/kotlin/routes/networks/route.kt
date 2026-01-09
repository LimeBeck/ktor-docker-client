package routes.networks

import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.api.networks
import io.ktor.server.routing.*
import logger
import routes.respondSmart

fun Route.networksRoute(dockerClient: DockerClient) {
    route("/networks") {
        get {
            logger.debug { "Fetching networks list" }
            val networks = dockerClient.networks.list().getOrNull() ?: emptyList()
            respondSmart("Networks") { renderNetworksPage(networks) }
        }
    }
}
