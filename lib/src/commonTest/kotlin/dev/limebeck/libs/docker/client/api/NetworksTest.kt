package dev.limebeck.libs.docker.client.api

import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.model.NetworkCreateRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class NetworksTest {
    private val client = DockerClient()

    @Test
    fun testListNetworks() = runTest {
        client.networks.list().getOrThrow()
    }

    @Test
    fun testNetworkLifecycle() = runTest {
        val networkName = "test-network-${kotlin.random.Random.nextInt(1000)}"

        // Create
        val createResponse = client.networks.create(
            networkConfig = NetworkCreateRequest(
                name = networkName
            )
        ).getOrThrow()
        val networkId = createResponse.id
        assertNotNull(networkId)

        try {
            // Inspect
            val network = client.networks.inspect(networkId).getOrThrow()
            assertNotNull(network.name)

            // List and check
            val list = client.networks.list().getOrThrow()
            assertNotNull(list.find { it.id == networkId })
        } finally {
            // Remove
            client.networks.remove(networkId).getOrThrow()
        }
    }
}
