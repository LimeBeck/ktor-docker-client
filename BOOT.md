# BOOT: kmp-docker-client AI session bootstrap

Purpose: treat specification files as IPC (shared mutable state), not passive docs.

## Mandatory boot sequence (every session)
1. Read `WAL.md` first (cache invalidation + continuation state).
2. Read `INSTRUCTIONS.md` (protocol rules, URI addressing, conflict policy).
3. Read project task/request.
4. Resolve relevant spec URIs and open only required files.

## Hard invariants
- Human instruction priority: **Human > Specs > Tests > Code**.
- Never silently change spec values; if an improvement is proposed, keep spec behavior and add `REVIEW` marker.
- One atomic change-set = one intent (spec item + matching code/tests/WAL updates).
- Session must end with WAL update if any meaningful work happened.

## URI base for this repository
Use absolute spec URIs:
- `spec://io.github.limebeck.kmp-docker-client/<DOC>#<anchor>`

## Before finalizing
- Re-read touched spec anchors.
- Run relevant checks/tests.
- Ensure WAL contains: next step, touched files, commands to resume, known risks.
