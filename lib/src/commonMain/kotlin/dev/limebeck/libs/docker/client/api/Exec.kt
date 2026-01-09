package dev.limebeck.libs.docker.client.api

import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.dslUtils.api
import dev.limebeck.libs.docker.client.dslUtils.readLogLines
import dev.limebeck.libs.docker.client.model.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

val DockerClient.exec by ::Exec.api()

class Exec(private val dockerClient: DockerClient) {
    /**
     * Start an exec instance
     *
     * Starts a previously set up exec instance. If detach is true, this endpoint returns immediately after starting
     * the command. Otherwise, it sets up an interactive session with the command.
     */
    suspend fun startAndForget(
        id: String,
        config: ExecStartConfig = ExecStartConfig()
    ): Result<Unit, ErrorResponse> = with(dockerClient) {
        client.post("/exec/$id/start") {
            contentType(ContentType.Application.Json)
            setBody(config)
        }.validateOnly()
    }

    /**
     * Start an exec instance and receive output
     */
    suspend fun startFlow(
        id: String,
        config: ExecStartConfig = ExecStartConfig()
    ): Result<Flow<LogLine>, ErrorResponse> =
        with(dockerClient) {
            coroutineScope {
                val execInfo = getInfo(id).onError {
                    return@coroutineScope Result.error(it)
                }.getOrNull()
                    ?: return@coroutineScope Result.error(
                        ErrorResponse("Exec not found")
                    )

                val container = dockerClient.containers.getInfo(execInfo.containerID!!).onError {
                    return@coroutineScope Result.error(it)
                }.getOrNull()
                    ?: return@coroutineScope Result.error(ErrorResponse("Container not found by ID"))

                val logs = flow {
                    client.preparePost("/exec/$id/start") {
                        contentType(ContentType.Application.Json)
                        setBody(config)
                        applyConnectionConfig()
                    }.execute {
                        val channel = it.bodyAsChannel()
                        channel.readLogLines(container.config?.tty == true, this@flow)
                    }
                }
                return@coroutineScope Result.success(logs)
            }
        }

    /**
     * Inspect an exec instance
     *
     * Return low-level information about an exec instance.
     */
    suspend fun getInfo(id: String): Result<ExecInspectResponse, ErrorResponse> =
        with(dockerClient) {
            return client.get("/exec/$id/json").parse()
        }

    /**
     * Resize an exec instance
     *
     * Resize the TTY session used by an exec instance. This endpoint only works if `tty` was specified as `true`
     * when creating the exec instance.
     */
    suspend fun resize(
        id: String,
        h: Int,
        w: Int
    ): Result<Unit, ErrorResponse> =
        with(dockerClient) {
            return client.post("/exec/$id/resize") {
                parameter("h", h.toString())
                parameter("w", w.toString())
            }.validateOnly()
        }
}
