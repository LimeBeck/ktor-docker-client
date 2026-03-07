# WAL (Write-Ahead Log)

## Current Focus
- Bootstrap primary project specs based on existing implementation and establish spec index under `specs/ipc/`.

## Completed in Last Session
- Added initial spec set:
  - `specs/ipc/README.md` (index and URI namespace)
  - `specs/ipc/PROP-000.md` (foundational invariants and architecture)
  - `specs/ipc/PROP-001.md` (implemented API surface contracts)
  - `specs/ipc/FEAT-001.md` (expansion roadmap for missing domains)
- Preserved repository protocol files (`BOOT.md`, `INSTRUCTIONS.md`, `AGENTS.md`, `CLAUDE.md`) as control plane.

## Next Steps
1. Link tests and future PRs to concrete URIs from `specs/ipc/PROP-001.md`.
2. Split `PROP-001` into per-domain specs when behavior depth increases (containers/images/networks/etc.).
3. Add lightweight spec-lint script to check URI references and anchor existence.

## Known Risks / Constraints
- Specs are foundational and may still be broader than exact per-method semantics.
- No automated URI/anchor validation in CI yet.

## Decisions Pending
- Whether to move top-level protocol docs into a dedicated `specs/` control-plane folder in next cleanup.
- Which missing API domain to prioritize first from `FEAT-001` (Service vs Secret/Config).

## Resume Commands
- `git status --short`
- `rg "spec://io.github.limebeck.kmp-docker-client" specs/ipc BOOT.md INSTRUCTIONS.md AGENTS.md CLAUDE.md WAL.md`
- `rg "\{#" specs/ipc/*.md INSTRUCTIONS.md`
