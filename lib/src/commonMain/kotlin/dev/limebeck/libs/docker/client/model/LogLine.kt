package dev.limebeck.libs.docker.client.model

data class LogLine(
    val type: Type,
    val line: String
) {
    enum class Type {
        STDOUT,
        STDERR,
        UNKNOWN,
    }
}