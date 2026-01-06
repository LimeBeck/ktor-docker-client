package dev.limebeck.docker.client.api

import dev.limebeck.docker.client.DockerClient
import dev.limebeck.docker.client.dslUtils.ApiCacheHolder
import dev.limebeck.docker.client.model.*
import dev.limebeck.docker.client.dslUtils.readLogLines
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private object ExecKey

val DockerClient.exec: DockerExecApi
    get() = (this as ApiCacheHolder).apiCache.getOrPut(ExecKey) {
        DockerExecApi(this)
    } as DockerExecApi

class DockerExecApi(val dockerClient: DockerClient) {
    suspend fun startAndForget(
        id: String,
        config: ExecStartConfig = ExecStartConfig()
    ): Result<Unit, ErrorResponse> = with(dockerClient) {
        return client.post("/exec/$id/start") {
            contentType(ContentType.Application.Json)
            setBody(config)
        }.validateOnly()
    }

    suspend fun startFlow(
        id: String,
        config: ExecStartConfig = ExecStartConfig()
    ): Result<Flow<LogLine>, ErrorResponse> = with(dockerClient) {
        coroutineScope {
            val execInfo = getInfo(id).onError {
                return@coroutineScope Result.error(it)
            }.getOrNull() ?: return@coroutineScope Result.error(ErrorResponse("Exec not found"))

            val container = dockerClient.containers.getInfo(execInfo.containerID!!).onError {
                return@coroutineScope Result.error(it)
            }.getOrNull() ?: return@coroutineScope Result.error(ErrorResponse("Container not found"))

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

    suspend fun getInfo(id: String): Result<ExecInspectResponse, ErrorResponse> = with(dockerClient) {
        return client.get("/exec/$id/json").parse()
    }

    suspend fun resize(
        id: String,
        h: Int,
        w: Int
    ): Result<Unit, ErrorResponse> = with(dockerClient) {
        return client.post("/exec/$id/resize") {
            parameter("h", h.toString())
            parameter("w", w.toString())
        }.validateOnly()
    }
}
