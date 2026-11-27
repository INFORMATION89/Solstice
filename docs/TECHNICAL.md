# Solstice technical maintenance guide

This guide covers app-repository maintenance only. It replaces inherited Breezy release/build
instructions that do not apply to Solstice. When in doubt, inspect the current Gradle configuration,
workflow, and app source rather than relying on an old command or property name.

## Local development

- Read root [AGENTS.md](../AGENTS.md), [RULES.md](RULES.md), and the current
  [CONTINUE.md](CONTINUE.md) handoff first.
- Use the Gradle Wrapper committed to this repository. Keep JDK, Gradle, Android Gradle Plugin,
  Kotlin, compile SDK, and plugin versions consistent with the actual root configuration and CI.
- Never use the upstream Breezy `-Pbreezy` build parameter for Solstice.
- Keep developer credentials in local, untracked configuration or a secure secret store. Never put
  API keys, signing keys, or signing passwords in tracked files, logs, build artifacts, or CI caches.
- Do not commit `local.properties`, signing material, generated keystores, or local-only overrides.

## Weather providers and source changes

Weather providers are app integrations. A provider change must be reviewed against its current
implementation, endpoint contract, geographic coverage, terms, privacy, attribution requirements,
key handling, freshness, and failure behavior.

When adding or changing a provider:

1. Inspect the current adapter and configuration in app source. Do not rely on a historic property
   table or inherited source list.
2. Keep credentials outside source control. Follow the provider's documented public-client model;
   never embed private developer credentials in distributed artifacts.
3. Add deterministic adapter/mapping tests for success, malformed data, timeout, and unavailable
   service behavior where feasible.
4. Update [COVERAGE.md](COVERAGE.md) only with verified app coverage evidence, and update relevant
   app copy/attribution only after checking licence and provider terms.
5. Update the app's release notes and [ROADMAP.md](ROADMAP.md) as appropriate.
6. Keep provider-specific meteorological blending, correction, learning, and forecast-engine design
   out of Solstice; see the consumer boundary in [INTEGRATIONS.md](INTEGRATIONS.md).

## CI and test builds

- The active build workflow is `.github/workflows/build.yml`. Its exact configured jobs and
  permissions are the source of truth for CI behavior.
- Pull requests run the required Build workflow and may upload Debug artifacts. Protected-main-only
  signing/Profile and testing-release publication steps are not validated by a PR build.
- Verify a workflow result belongs to the exact commit SHA being reviewed.
- Re-run only failed jobs after inspecting the failure cause. Do not start repeated full CI runs for
  unchanged code.
- A successful CI build is not a physical-device, TalkBack, accessibility, or visual verification.
  Record device checks as NOT TESTED when they were not performed.
- Profile signing and testing-release publication require the protected-main workflow and configured
  secrets; never expose those secrets to untrusted PR code.

## Release maintenance

Before publishing a testing or production build:

1. Verify the exact source commit and clean working tree.
2. Verify version name/code, signing configuration, and the workflow's intended artifact path.
3. Confirm licences, third-party notices, attribution, and privacy wording match the build.
4. Verify the resulting APK/artifact and test it on a supported Android device.
5. Publish only through the approved workflow and with explicit owner authorization.

Do not tag or publish a release merely because a PR build is green.

## Adding or updating documentation

- Product decisions go in [PLAN.md](PLAN.md).
- App implementation tasks go in [ROADMAP.md](ROADMAP.md).
- Historical work goes in [LOG.md](LOG.md).
- The current app handoff goes in [CONTINUE.md](CONTINUE.md).
- Provider coverage evidence belongs in [COVERAGE.md](COVERAGE.md).
- Consumer-side dependency requirements belong in [INTEGRATIONS.md](INTEGRATIONS.md).
- Detailed internal privacy findings belong in [PRIVACY_DATA_MAP.md](PRIVACY_DATA_MAP.md), not in
  public privacy claims.

Keep this document focused on Solstice maintenance. Do not duplicate another repository's build,
release, licence, dependency, or implementation instructions here.
