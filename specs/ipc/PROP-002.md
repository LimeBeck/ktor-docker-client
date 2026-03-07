# PROP-002: CI/CD Release Pipeline Contract {#root}

Status: ACTIVE  
Module URI: `spec://io.github.limebeck.kmp-docker-client/specs/ipc/PROP-002.md`

## Goal {#goal}
Define mandatory behavior for GitHub Actions CI/CD pipeline in this repository.

## Workflow file contract {#workflow.file}
- CI/CD workflow must be declared in `.github/workflows/main.yml`.
- Any pipeline changes must preserve release-by-tag behavior unless explicitly revised in spec.

## Trigger contract {#triggers}
- Release pipeline trigger: `push.tags` with pattern `v*`.
- Manual trigger: `workflow_dispatch`.
- Publish steps must not run for non-tag refs.
- Docs publication trigger: `push.branches` for `master`.

## Build and test contract {#ci.jobs}
Pipeline must include these sequential jobs:
1. `build` running `./gradlew build`.
2. `test` running `./gradlew :lib:allTests` and depending on `build`.

Test artifacts:
- XML unit test reports from `lib/build/test-results/**/*.xml` must be uploaded even when tests fail (`if: always()`).

## Publish contract {#publish}
- Publishing is Maven Central-oriented and must depend on successful `test` job.
- Publish job must use `:lib:publishAndReleaseToMavenCentral`.
- Required publish inputs:
  - GPG material: `GPG_SIGNING_KEY`, `SECRET_PASSPHRASE`, `GPG_PASSWORD`, `GPG_KEY_ID`
  - Maven Central credentials: `OSSRH_USERNAME`, `OSSRH_PASSWORD`

## Test results publication contract {#test-results}
- Pipeline must include a dedicated post-test results publication job.
- It should download CI artifacts and publish JUnit-style reports to GitHub checks UI.

## Dokka docs publication contract {#docs}
- Pipeline must include a GitHub Pages deployment job for Dokka HTML docs.
- Docs job must run `:lib:dokkaGenerateHtml`.
- Published artifact path must match Dokka output directory (`lib/build/dokka/html`).
- Deployment must use GitHub Pages actions:
  - `actions/configure-pages`
  - `actions/upload-pages-artifact`
  - `actions/deploy-pages`
- Required job permissions:
  - `pages: write`
  - `id-token: write`

## Cache/runtime baseline {#runtime}
- Runner baseline: `ubuntu-latest`.
- Java baseline: Temurin JDK 21.
- Cache should include Gradle and Kotlin/Native directories used by project builds.

## Change control {#change-control}
- Any CI/CD modification must reference this spec:
  - `spec://io.github.limebeck.kmp-docker-client/specs/ipc/PROP-002.md#root`
- If release strategy changes (for example, adding PR trigger or changing publish target), update corresponding anchors first.

## Changelog {#changelog}
- 2026-03-07: initial CI/CD release pipeline contract added based on `.github/workflows/main.yml`.
- 2026-03-07: added Dokka GitHub Pages publication contract.
