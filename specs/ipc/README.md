# kmp-docker-client IPC Specs Index {#root}

Base URI namespace:
`spec://io.github.limebeck.kmp-docker-client/specs/ipc/<DOC>#<anchor>`

## Core specs {#core}
- [`PROP-000.md`](./PROP-000.md) — foundational architecture and invariants.
- [`PROP-001.md`](./PROP-001.md) — current API surface and behavioral contracts.
- [`FEAT-001.md`](./FEAT-001.md) — initial expansion roadmap for missing Docker domains.

## Usage rule {#usage}
For new implementation work:
1. Link task to one or more explicit spec URIs.
2. Implement atomically with tests.
3. Update `WAL.md` and (if needed) spec changelog sections.
