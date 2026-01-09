package routes.networks

import dev.limebeck.libs.docker.client.model.Network
import kotlinx.html.*
import ui.card


fun FlowContent.renderNetworksPage(networks: List<Network>) {
    h1("text-3xl font-bold mb-6 text-yellow-400") { +"🌐 Networks" }

    card("bg-gray-800 rounded-lg overflow-hidden border border-gray-700") {
        table("w-full text-left") {
            thead("bg-gray-700 text-gray-400 uppercase text-xs") {
                tr {
                    listOf("Name", "Id", "Driver", "Scope").forEach { th(classes = "px-6 py-3") { +it } }
                }
            }
            tbody("divide-y divide-gray-700") {
                networks.forEach { network ->
                    tr("hover:bg-gray-700/50") {
                        td("px-6 py-4 font-bold") { +(network.name ?: "-") }
                        td("px-6 py-4 text-xs font-mono text-gray-400") { +(network.id ?: "-") }
                        td("px-6 py-4") { +(network.driver ?: "-") }
                        td("px-6 py-4") { +(network.scope ?: "-") }
                    }
                }
            }
        }
    }
}
