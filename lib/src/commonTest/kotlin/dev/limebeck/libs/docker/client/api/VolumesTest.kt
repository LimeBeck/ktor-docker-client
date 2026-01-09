package dev.limebeck.libs.docker.client.api

import dev.limebeck.libs.docker.client.DockerClient
import dev.limebeck.libs.docker.client.model.VolumeCreateOptions
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class VolumesTest {
    private val client = DockerClient()

    @Test
    fun testListVolumes() = runTest {
        client.volumes.getList().getOrThrow()
    }

    @Test
    fun testVolumeLifecycle() = runTest {
        val volumeName = "test-volume-${kotlin.random.Random.nextInt(1000)}"

        // Create
        val volume = client.volumes.create(
            config = VolumeCreateOptions(
                name = volumeName
            )
        ).getOrThrow()
        assertNotNull(volume.name)

        try {
            // Get Info
            val info = client.volumes.getInfo(volumeName).getOrThrow()
            assertNotNull(info.name)

            // List and check
            val list = client.volumes.getList().getOrThrow()
            assertNotNull(list.volumes?.find { it.name == volumeName })
        } finally {
            // Remove
            client.volumes.remove(volumeName, force = true).getOrThrow()
        }
    }
}
