# WAL (Write-Ahead Log)

## Current Focus
- Extend CI/CD contract with Dokka documentation publication.

## Completed in Last Session
- Extended `.github/workflows/main.yml` with Dokka docs deployment job:
  - added `push.branches: master` trigger for CI/docs flow
  - added `publish-dokka-docs` job (`:lib:dokkaGenerateHtml`)
  - publishes `./lib/build/dokka/html` to GitHub Pages via official pages actions
- Updated CI/CD spec `specs/ipc/PROP-002.md` with docs publication anchors:
  - `#triggers` includes docs branch trigger
  - added `#docs` section for GitHub Pages Dokka contract

## Next Steps
1. Add repository secrets required by publish stage (`GPG_SIGNING_KEY`, `SECRET_PASSPHRASE`, `GPG_PASSWORD`, `GPG_KEY_ID`, `OSSRH_USERNAME`, `OSSRH_PASSWORD`).
2. Enable GitHub Pages for repository and validate first docs deployment from `master`.
3. Validate workflow on dry-run tag and verify Maven Central publish gate still runs only on `v*` tags.

## Known Risks / Constraints
- `:lib:allTests` includes JS/Native/JVM test execution and can increase runtime on GitHub runners.
- Publish stage expects encrypted/base64 key material format identical to `reveal-kt` flow.
- Dokka deployment assumes output path `lib/build/dokka/html` from current Dokka task configuration.

## Decisions Pending
- Keep current trigger scope (`push.tags`, `push.branches: master`, `workflow_dispatch`) or extend with `pull_request` and update `PROP-002#triggers`.

## Resume Commands
- `git status --short`
- `cat .github/workflows/main.yml`
- `cat specs/ipc/PROP-002.md`
- `rg "publish-dokka-docs|dokkaGenerateHtml|upload-pages-artifact" .github/workflows/main.yml specs/ipc/PROP-002.md`
