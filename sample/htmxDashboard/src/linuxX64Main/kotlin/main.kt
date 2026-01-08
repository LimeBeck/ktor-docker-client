import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.api.containers
import dev.limebeck.libs.docker.client.model.ContainerInspectResponse
import dev.limebeck.libs.docker.client.model.ContainerLogsParameters
import dev.limebeck.libs.docker.client.model.ContainerSummary
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.cio.CIO
import kotlinx.coroutines.flow.collect
import kotlinx.html.BODY
import kotlinx.html.FlowContent
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe
import kotlinx.html.stream.createHTML

fun main() {
    val dockerClient = DockerClient()

    embeddedServer(CIO, host = "0.0.0.0", port = 8080) {
        routing {
            get("/") {
                val containersResult = dockerClient.containers.getList()
                val containers = containersResult.getOrNull().orEmpty()
                val errorMessage = containersResult.errorOrNull()?.toString()
                call.respondText(
                    contentType = ContentType.Text.Html,
                    text = renderDashboard(containers, errorMessage)
                )
            }

            get("/containers/{id}") {
                val id = call.parameters["id"] ?: return@get call.respondText(
                    contentType = ContentType.Text.Html,
                    text = renderErrorPanel("Container id is missing.")
                )

                val infoResult = dockerClient.containers.getInfo(id)
                val info = infoResult.getOrNull()
                if (info == null) {
                    call.respondText(
                        contentType = ContentType.Text.Html,
                        text = renderErrorPanel("Failed to load container $id: ${infoResult.errorOrNull()}")
                    )
                    return@get
                }

                call.respondText(
                    contentType = ContentType.Text.Html,
                    text = renderContainerDetails(info)
                )
            }

            get("/containers/{id}/logs/stream") {
                val id = call.parameters["id"] ?: return@get
                call.response.headers.append(HttpHeaders.CacheControl, "no-cache")
                call.response.headers.append(HttpHeaders.Connection, "keep-alive")

                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    val logsResult = dockerClient.containers.getLogs(
                        id = id,
                        parameters = ContainerLogsParameters(follow = true, tail = "200")
                    )

                    logsResult.fold(
                        onSuccess = { logs ->
                            logs.collect { line ->
                                val safeLine = escapeHtml("${line.type}: ${line.line}".trimEnd())
                                write("data: <div class=\"log-line\">$safeLine</div>\n\n")
                                flush()
                            }
                        },
                        onError = { error ->
                            val safeLine = escapeHtml("Failed to stream logs: $error")
                            write("data: <div class=\"log-line log-error\">$safeLine</div>\n\n")
                            flush()
                        }
                    )
                }
            }
        }
    }.start(wait = true)
}

private fun renderDashboard(containers: List<ContainerSummary>, errorMessage: String?): String =
    createHTML().html {
        head {
            title("Docker Containers Dashboard")
            meta { charset = "utf-8" }
            meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1"
            }
            script { src = "https://unpkg.com/htmx.org@1.9.12" }
            script { src = "https://unpkg.com/htmx.org@1.9.12/dist/ext/sse.js" }
            style {
                unsafe {
                    +"""
                    :root {
                        color-scheme: light dark;
                        --surface: #101827;
                        --surface-alt: #17202e;
                        --border: #2a3649;
                        --text: #e2e8f0;
                        --muted: #94a3b8;
                        --accent: #38bdf8;
                        font-family: "Inter", "Segoe UI", sans-serif;
                    }
                    body {
                        margin: 0;
                        background: var(--surface);
                        color: var(--text);
                    }
                    .page {
                        max-width: 1200px;
                        margin: 0 auto;
                        padding: 32px 24px 48px;
                    }
                    .grid {
                        display: grid;
                        grid-template-columns: minmax(240px, 1fr) minmax(360px, 2fr);
                        gap: 24px;
                    }
                    .panel {
                        background: var(--surface-alt);
                        border: 1px solid var(--border);
                        border-radius: 16px;
                        padding: 20px;
                    }
                    .panel h2 {
                        margin-top: 0;
                    }
                    .container-list {
                        list-style: none;
                        margin: 0;
                        padding: 0;
                        display: flex;
                        flex-direction: column;
                        gap: 12px;
                    }
                    .container-list button {
                        width: 100%;
                        text-align: left;
                        background: transparent;
                        border: 1px solid var(--border);
                        border-radius: 12px;
                        padding: 12px 14px;
                        color: var(--text);
                        cursor: pointer;
                        display: flex;
                        flex-direction: column;
                        gap: 4px;
                    }
                    .container-list button:hover {
                        border-color: var(--accent);
                    }
                    .container-name {
                        font-weight: 600;
                    }
                    .container-meta {
                        font-size: 0.85rem;
                        color: var(--muted);
                    }
                    .details-header {
                        display: flex;
                        align-items: baseline;
                        justify-content: space-between;
                        gap: 12px;
                    }
                    .pill {
                        padding: 4px 8px;
                        border-radius: 999px;
                        border: 1px solid var(--border);
                        font-size: 0.75rem;
                        color: var(--muted);
                    }
                    .details-list {
                        list-style: none;
                        padding: 0;
                        margin: 0;
                        display: grid;
                        gap: 8px;
                    }
                    .details-list span {
                        color: var(--muted);
                    }
                    .log-box {
                        background: #0b1220;
                        border: 1px solid var(--border);
                        border-radius: 12px;
                        padding: 12px;
                        max-height: 320px;
                        overflow-y: auto;
                        font-family: "JetBrains Mono", "Fira Code", monospace;
                        font-size: 0.85rem;
                    }
                    .log-line {
                        margin-bottom: 4px;
                        white-space: pre-wrap;
                    }
                    .log-error {
                        color: #f87171;
                    }
                    .muted {
                        color: var(--muted);
                    }
                    """.trimIndent()
                }
            }
            link(rel = "icon", href = "data:,")
        }
        body {
            div("page") {
                h1 { +"Docker Containers Dashboard" }
                p("muted") { +"Select a container to view details and live logs." }
                if (errorMessage != null) {
                    p("muted") { +errorMessage }
                }
                div("grid") {
                    div("panel") {
                        h2 { +"Containers" }
                        if (containers.isEmpty()) {
                            p("muted") { +"No containers found on the host." }
                        } else {
                            ul("container-list") {
                                containers.forEach { container ->
                                    li {
                                        button {
                                            attributes["hx-get"] = "/containers/${container.id}"
                                            attributes["hx-target"] = "#container-details"
                                            attributes["hx-swap"] = "innerHTML"
                                            span("container-name") { +containerDisplayName(container) }
                                            span("container-meta") { +containerSummaryMeta(container) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    div("panel") {
                        h2 { +"Details" }
                        div {
                            id = "container-details"
                            p("muted") { +"Choose a container from the list to inspect it." }
                        }
                    }
                }
            }
        }
    }

private fun renderContainerDetails(container: ContainerInspectResponse): String =
    createHTML().div {
        div("details-header") {
            div {
                h2 { +container.name?.removePrefix("/").orEmpty() }
                p("muted") { +container.id.orEmpty().take(12) }
            }
            span("pill") { +container.state?.status.orEmpty() }
        }
        ul("details-list") {
            detailRow("Image", container.config?.image.orEmpty())
            detailRow("Hostname", container.config?.hostname.orEmpty())
            detailRow("Entrypoint", container.config?.entrypoint?.joinToString(" ").orEmpty())
            detailRow("Cmd", container.config?.cmd?.joinToString(" ").orEmpty())
            detailRow("Network", container.hostConfig?.networkMode.orEmpty())
        }
        hr {}
        h2 { +"Live logs" }
        div("log-box") {
            attributes["hx-ext"] = "sse"
            attributes["sse-connect"] = "/containers/${container.id}/logs/stream"
            attributes["sse-swap"] = "message"
            attributes["hx-swap"] = "beforeend"
            p("muted") { +"Streaming logs..." }
        }
        p("muted") { +"Logs are streamed via Server-Sent Events (SSE)." }
    }

private fun renderErrorPanel(message: String): String =
    createHTML().div {
        p("muted") { +message }
    }

private fun FlowContent.detailRow(label: String, value: String) {
    if (value.isBlank()) return
    li {
        span { +"$label: " }
        +value
    }
}

private fun containerDisplayName(container: ContainerSummary): String {
    val name = container.names?.firstOrNull()?.removePrefix("/")
    return name ?: container.id.orEmpty().take(12)
}

private fun containerSummaryMeta(container: ContainerSummary): String {
    val state = container.state.orEmpty()
    val image = container.image.orEmpty()
    return listOfNotNull(image.ifBlank { null }, state.ifBlank { null }).joinToString(" • ")
}

private fun escapeHtml(input: String): String = buildString {
    input.forEach { char ->
        when (char) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&#39;")
            else -> append(char)
        }
    }
}
