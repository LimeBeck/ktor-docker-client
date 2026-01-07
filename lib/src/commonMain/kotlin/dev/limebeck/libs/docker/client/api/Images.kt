package dev.limebeck.libs.docker.client.api

import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.dslUtils.OciImageRefParser
import dev.limebeck.libs.docker.client.dslUtils.api
import dev.limebeck.libs.docker.client.model.ErrorResponse
import dev.limebeck.libs.docker.client.model.Result
import io.ktor.client.request.*

val DockerClient.images by ::Images.api()

class Images(private val dockerClient: DockerClient) {
    suspend fun create(
        fromImage: String,
        fromSrc: String? = null,
        repo: String? = null,
        tag: String? = null,
        message: String? = null,
        changes: List<String>? = null,
    ): Result<Unit, ErrorResponse> =
        with(dockerClient) {
            client.post("/images/create") {
                parameter("fromImage", fromImage)

                fromSrc?.let { parameter("fromSrc", it) }
                repo?.let { parameter("repo", it) }
                tag?.let { parameter("tag", it) }
                message?.let { parameter("message", it) }
                changes?.forEach { parameter("changes", it) }

                applyAuthForRegistry(
                    OciImageRefParser.normalize(
                        fromImage
                    ).registry
                )
            }.validateOnly()
        }
}