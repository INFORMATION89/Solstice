# Solstice — current handoff

**Snapshot:** 2026-11-26  
**Scope:** Solstice Android application only.

## Read first

Read root [AGENTS.md](../AGENTS.md), [RULES.md](RULES.md), [PLAN.md](PLAN.md),
[ROADMAP.md](ROADMAP.md), and [AI_COST_POLICY.md](AI_COST_POLICY.md). Use this file only for the
current app handoff. Dependency consumer boundaries are in [INTEGRATIONS.md](INTEGRATIONS.md);
do not use Solstice plans to continue development of external dependencies.

## Current modernization PR

[PR #14 — Solstice documentation, AI governance, and Gradle CI](https://github.com/INFORMATION89/Solstice/pull/14)
is the active Draft containing the documentation and CI modernization.

- The previous exact head `31cace1d3166896871bb0e9ede8182f3b4c29aad` passed Build run #285
  (`45905434026`). Spotless, the no-hardcoding guard and its self-tests, the configured app/dependency unit-test task
graph, Debug assembly, and Debug APK upload passed. The main-only signing/Profile and publication
  steps were correctly skipped on a PR.
- This handoff and documentation cleanup advance the branch beyond that verified SHA. Run and inspect
  CI on the new exact head before treating the updated branch as verified.
- No app runtime or visible UI change is included in this documentation cleanup.
- The no-hardcoding guard is heuristic; manual diff review remains required. Protected-main
  Profile/signing/publication and physical-device behavior have not been verified by a PR run.

## Solstice work still open

1. **Privacy and data-flow review.** Continue from [PRIVACY_DATA_MAP.md](PRIVACY_DATA_MAP.md).
   Trace the background-location call path, current provider/endpoint fields, local app-to-app
   disclosures, update checks, logging/diagnostics, retention/deletion, and build variants before
   replacing the inherited public [PRIVACY.md](../PRIVACY.md). The data map is internal evidence,
   not the public notice or legal approval.
2. **Permission and user-facing copy.** The custom `READ_PROVIDER` permission still uses Breezy
   wording. Any replacement is a visible app change and must follow RULES.md approval requirements.
3. **CI/release path.** A protected-main run with configured signing secrets still needs to verify
   the Profile artifact and testing-release publication end to end. Do not infer this from PR CI.
4. **Android device pass.** UI, interaction, animation, TalkBack, and Android ICU behavior still need
   physical-device/instrumentation checks where relevant. Record unavailable checks as NOT TESTED.
5. **App roadmap.** Revalidate the remaining app bugs, settings, forecast, Outlook, alert, and
   accessibility tasks in [ROADMAP.md](ROADMAP.md) against current source before starting them.
   Preserve feels-like as a prominent practical metric while keeping actual air temperature visible.

## Dependency use

Solstice consumes external libraries only through reviewed, pinned public APIs. Their own internal
implementation work is outside this repository. Check [INTEGRATIONS.md](INTEGRATIONS.md) before any
app-side adoption change; keep this handoff and all other app plans limited to Solstice tasks.

## Working sequence

1. Finish the documentation cleanup on PR #14; validate links, references, and active plan scope.
2. Re-run required CI on the exact final PR head and review the complete diff.
3. Continue the app privacy evidence audit before drafting public privacy wording.
4. Take the next approved Solstice-owned task from ROADMAP, with focused tests and a phone check
   when applicable.

Do not merge PRs, publish releases, change protected repository settings, or incur unapproved paid
usage without the required explicit authorization.
