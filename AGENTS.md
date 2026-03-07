# AGENTS Protocol (kmp-docker-client)

Scope: entire repository.

## Session contract
- Start with `BOOT.md`.
- Then read `WAL.md` and `INSTRUCTIONS.md`.
- Treat these files as IPC state, not passive docs.

## Required behavior
1. Use explicit spec URI references: `spec://io.github.limebeck.kmp-docker-client/<DOC>#<anchor>`.
2. Keep edits atomic (one intent per commit).
3. If you disagree with spec, implement spec first and leave `REVIEW` marker.
4. Update `WAL.md` at session end for any non-trivial changes.
5. Do not silently rewrite normative values in spec files.

## Priority order
Human request > spec files > tests > implementation details.

## Safety/legal metadata rule
Do not add explicit AI-authorship indicators in commit metadata, code comments, or docs unless the human explicitly asks for it.
