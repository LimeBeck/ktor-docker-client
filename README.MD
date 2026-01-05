# ktor-docker-client (Work-In-Progress)

Kotlin Multiplatform Library for communicating with Docker Daemon via REST API

## Limitations

- Only Linux is supported
- Only unix sockets are supported
- No authentication at the moment

### Run Sample App

 - Linux/Macos/Windows native: `./gradlew :sample:terminalApp:runDebugExecutable[architecture]`

### Publish to MavenLocal

1) Run `./gradlew :shared:publishToMavenLocal`
2) Open `~/.m2/repository/dev/limebeck/docker/client/`
