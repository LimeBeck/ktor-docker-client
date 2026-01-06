package dev.limebeck.docker.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

data class DockerClientConfig(
    val json: Json = Json {
        ignoreUnknownKeys = true
    },
    val connectionConfig: ConnectionConfig = ConnectionConfig.SocketConnection("/var/run/docker.sock")
) {
    sealed interface ConnectionConfig {
        data class SocketConnection(val socketPath: String) : ConnectionConfig
    }
}