package dev.limebeck.libs.docker.client.api

import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.dslUtils.OciImageRefParser
import dev.limebeck.libs.docker.client.dslUtils.api
import dev.limebeck.libs.docker.client.model.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*

val DockerClient.images by ::Images.api()

class Images(private val dockerClient: DockerClient) {
    suspend fun list(
        all: Boolean = false,
        filters: Map<String, List<String>>? = null,
        sharedSize: Boolean = false,
        digests: Boolean = false,
        manifests: Boolean = false
    ): Result<List<ImageSummary>, ErrorResponse> =
        with(dockerClient) {
            client.get("/images/json") {
                parameter("all", all)
                filters?.let { parameter("filters", json.encodeToString(it)) }
                parameter("shared-size", sharedSize)
                parameter("digests", digests)
                parameter("manifests", manifests)
            }.parse()
        }

    suspend fun create(
        fromImage: String,
        fromSrc: String? = null,
        repo: String? = null,
        tag: String? = null,
        message: String? = null,
        changes: List<String>? = null,
        platform: String? = null
    ): Result<Unit, ErrorResponse> =
        with(dockerClient) {
            client.post("/images/create") {
                parameter("fromImage", fromImage)

                fromSrc?.let { parameter("fromSrc", it) }
                repo?.let { parameter("repo", it) }
                tag?.let { parameter("tag", it) }
                message?.let { parameter("message", it) }
                changes?.forEach { parameter("changes", it) }
                platform?.let { parameter("platform", it) }

                applyAuthForRegistry(
                    OciImageRefParser.normalize(
                        fromImage
                    ).registry
                )
            }.validateOnly()
        }

    suspend fun inspect(
        name: String,
        manifests: Boolean = false
    ): Result<ImageInspect, ErrorResponse> =
        with(dockerClient) {
            client.get("/images/$name/json") {
                parameter("manifests", manifests)
            }.parse()
        }

    suspend fun history(name: String): Result<List<HistoryResponseItem>, ErrorResponse> =
        with(dockerClient) {
            client.get("/images/$name/history").parse()
        }

    suspend fun push(
        name: String,
        tag: String? = null,
        platform: String? = null
    ): Result<Unit, ErrorResponse> =
        with(dockerClient) {
            client.post("/images/$name/push") {
                tag?.let { parameter("tag", it) }
                platform?.let { parameter("platform", it) }

                applyAuthForRegistry(OciImageRefParser.normalize(name).registry)
            }.validateOnly()
        }

    suspend fun tag(
        name: String,
        repo: String? = null,
        tag: String? = null
    ): Result<Unit, ErrorResponse> =
        with(dockerClient) {
            client.post("/images/$name/tag") {
                repo?.let { parameter("repo", it) }
                tag?.let { parameter("tag", it) }
            }.validateOnly()
        }

    suspend fun remove(
        name: String,
        force: Boolean = false,
        noPrune: Boolean = false
    ): Result<List<ImageDeleteResponseItem>, ErrorResponse> =
        with(dockerClient) {
            client.delete("/images/$name") {
                parameter("force", force)
                parameter("noprune", noPrune)
            }.parse()
        }

    suspend fun search(
        term: String,
        limit: Int? = null,
        filters: Map<String, List<String>>? = null
    ): Result<List<ImageSearchResponseItem>, ErrorResponse> =
        with(dockerClient) {
            client.get("/images/search") {
                parameter("term", term)
                limit?.let { parameter("limit", it) }
                filters?.let { parameter("filters", json.encodeToString(it)) }
            }.parse()
        }

    suspend fun prune(
        filters: Map<String, List<String>>? = null
    ): Result<ImagePruneResponse, ErrorResponse> =
        with(dockerClient) {
            client.post("/images/prune") {
                filters?.let { parameter("filters", json.encodeToString(it)) }
            }.parse()
        }

    suspend fun export(
        name: String,
        platform: String? = null
    ): Result<ByteReadChannel, ErrorResponse> =
        with(dockerClient) {
            val response = client.get("/images/$name/get") {
                platform?.let { parameter("platform", it) }
            }
            if (response.status.isSuccess()) {
                response.bodyAsChannel().asSuccess()
            } else {
                json.decodeFromString<ErrorResponse>(response.bodyAsText()).asError()
            }
        }

    suspend fun exportAll(
        names: List<String>? = null,
        platform: String? = null
    ): Result<ByteReadChannel, ErrorResponse> =
        with(dockerClient) {
            val response = client.get("/images/get") {
                names?.forEach { parameter("names", it) }
                platform?.let { parameter("platform", it) }
            }
            if (response.status.isSuccess()) {
                response.bodyAsChannel().asSuccess()
            } else {
                json.decodeFromString<ErrorResponse>(response.bodyAsText()).asError()
            }
        }

    suspend fun load(
        quiet: Boolean = false,
        body: ByteReadChannel
    ): Result<Unit, ErrorResponse> =
        with(dockerClient) {
            client.post("/images/load") {
                parameter("quiet", quiet)
                setBody(body)
            }.validateOnly()
        }
}