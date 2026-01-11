package routes.exec

import dev.limebeck.libs.docker.client.model.ContainerInspectResponse
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.unsafe


fun FlowContent.renderExecTerminal(containerId: String, execId: String, info: ContainerInspectResponse?) {
    div("space-y-6") {
        div("flex justify-between items-center") {
            h1("text-3xl font-bold text-blue-400") {
                +"Exec: ${info?.name?.removePrefix("/") ?: containerId.take(12)}"
            }
            a(classes = "text-gray-400 hover:text-white cursor-pointer") {
                attributes["hx-get"] = "/containers/$containerId"
                attributes["hx-target"] = "#main-content"
                attributes["hx-push-url"] = "true"
                +"← Back to Container"
            }
        }
        div {
            div("bg-gray-800 rounded-lg p-4 font-mono text-sm h-96 h-full w-full overflow-hidden border border-gray-700 shadow-inner") {
                id = "terminal"
            }
            script {
                unsafe {
                    +"""
                    (function() {
                        var terminalContainer = document.getElementById('terminal');
                        var term = new Terminal({
                            cursorBlink: true,
                            convertEol: true,
                            theme: {
                                background: '#1f2937'
                            }
                        });
                        
                        var fitAddon = new FitAddon.FitAddon();
                        term.loadAddon(fitAddon);
                        term.open(terminalContainer);
                        fitAddon.fit();
            
                        var socket = new WebSocket('ws://' + window.location.host + '/exec/$execId/ws');
                        
                        socket.onopen = function() {
                            term.write('\r\n\x1b[32mConnection established.\x1b[0m\r\n');
                        }
            
                        socket.onmessage = function(event) {
                            console.log(event)
                            if (typeof event.data === 'string') {
                                term.write(event.data);
                            } else {
                                // Если Ktor шлет бинарные фреймы
                                const reader = new FileReader();
                                reader.onload = () => {
                                     // xterm умеет принимать Uint8Array
                                     term.write(new Uint8Array(reader.result));
                                };
                                reader.readAsArrayBuffer(event.data);
                            }
                        };
                        
                        socket.onclose = function() {
                            term.write('\r\n\x1b[31mConnection closed.\x1b[0m\r\n');
                        }
            
                        term.onData(function(data) {
                            console.log(data)
                            socket.send(data);
                        });
            
                        window.addEventListener('resize', function() {
                            fitAddon.fit();
                        });
                    })();
                    """.trimIndent()
                }
            }
        }
    }
}