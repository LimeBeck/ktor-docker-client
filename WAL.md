# WAL (Write-Ahead Log)

## Current Focus
- Fix unit test result publication permissions in release workflow.

## Completed in Last Session
- Fixed test result publication auth in release workflow:
  - added workflow baseline permission `contents: read`
  - added `publish-test-results` job permissions: `checks: write`, `contents: read`
  - addresses `403 Resource not accessible by integration` from check-runs API
- Updated `specs/ipc/PROP-002.md#test-results` with required `checks: write` permission.

## Next Steps
1. Add repository secrets required by publish stage (`GPG_SIGNING_KEY`, `SECRET_PASSPHRASE`, `GPG_PASSWORD`, `GPG_KEY_ID`, `OSSRH_USERNAME`, `OSSRH_PASSWORD`).
2. Enable GitHub Pages for repository and validate first docs deployment from `master`.
3. Re-run release workflow and verify `publish-test-results` creates Check Run successfully (no 403).

## Known Risks / Constraints
- `:lib:allTests` includes JS/Native/JVM test execution and can increase runtime on GitHub runners.
- Publish stage expects encrypted/base64 key material format identical to `reveal-kt` flow.
- Dokka deployment assumes output path `lib/build/dokka/html` from current Dokka task configuration.

## Decisions Pending
- Keep current trigger scope (`push.tags`, `push.branches: master`, `workflow_dispatch`) or extend with `pull_request` and update `PROP-002#triggers`.

## Resume Commands
- `git status --short`
- `cat .github/workflows/main.yml`
- `cat .github/workflows/docs.yml`
- `cat specs/ipc/PROP-002.md`
- `rg "checks: write|publish-test-results|GITHUB_REF_NAME#v|libVersion|dokkaGenerateHtml" .github/workflows/*.yml specs/ipc/PROP-002.md`
