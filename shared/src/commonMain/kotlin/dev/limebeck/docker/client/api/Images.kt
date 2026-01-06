package dev.limebeck.docker.client.api

import dev.limebeck.docker.client.DockerClient
import dev.limebeck.docker.client.dslUtils.api

private object ImagesKey

val DockerClient.images by ::Images.api()

class Images(dockerClient: DockerClient) {

}