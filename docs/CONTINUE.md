# Solstice — current handoff

**Snapshot:** 2026-11-27  
**Scope:** Solstice Android application only.

## Read first

Read root [AGENTS.md](../AGENTS.md), [RULES.md](RULES.md), [PLAN.md](PLAN.md),
[ROADMAP.md](ROADMAP.md), and [AI_COST_POLICY.md](AI_COST_POLICY.md). Use this file only for the
current Solstice handoff. Consumer-side dependency boundaries are in [INTEGRATIONS.md](INTEGRATIONS.md).

## Current cleanup branch

Work is in `maintenance/pre-feature-cleanup-2026-11-27`, based on `main` at `a7e7e8764f1440e1852b33d867de39c4591e6369`.

The cleanup restores the governance, CI, privacy-evidence and consumer-boundary work introduced by
PR #14. That work was merged historically, but later PRs were created from an older base and the
current `main` tree no longer contained those files. This branch reapplies the PR #14 changes on top
of the current `main`, while preserving the later Solstice decisions from PRs #15–#21.

No app feature, visual redesign, provider change, or runtime behavior change is intended.

## Completed in this cleanup pass

1. Checked relative Markdown links across all 52 repository Markdown files; no unresolved internal file targets remain. Checked the internal heading links in `docs/COVERAGE.md`.
2. Clarified `docs/PROPOSALS.md`: unapproved ideas are proposal-only; **Decided** markers are historical, with `PLAN.md` remaining authoritative.
3. Verified the dated log has 81 dated entries in chronological order, from 2026-11-17 through 2026-11-26.
4. Updated the Motion submodule URL to `INFORMATION89/Motion-UI`. Its exact gitlink, `25ffa862f503b482ed98671cd5eab66040f0d75f`, exists in that repository and was checked out by the successful PR build.
5. Reviewed the declared build toolchain: Gradle Wrapper 9.9.1 with distribution SHA-256, JDK 21, AGP 9.5.1, and Kotlin 2.4.21. Build workflow run [46050051016](https://github.com/INFORMATION89/Solstice/actions/runs/46050051016) passed on code commit `8dc4131234594c54897a42a7909f38cd8ba64d19`, including Spotless, the no-hardcoding self-test and diff check, selected unit tests, and the Basic Debug build.
6. Reviewed both GitHub Actions workflows. Pull-request builds receive read-only contents permission and no signing secrets. Profile signing and the release job are restricted to pushes to `main`; the release job has contents-write permission. The wiki workflow uses its separate secret only for wiki publication. Secret values/scopes and the main-only release path have not been inspected or exercised.
7. Checked GitHub Releases: no testing or other release is currently published. Updated README and installation instructions so they no longer imply an APK is available now.
8. Reviewed the complete changed-file inventory: no production Kotlin/Java, resources, manifest, provider, or app runtime implementation files changed. The only app source changes are two existing unit-test files that explicitly select deterministic formatting paths. Other changes are documentation, CI/build configuration, the Motion submodule URL, and Gradle build-cache configuration.

## Remaining work before integration

1. Reopen PR #22 as a draft, refresh its description against the current branch head, and let the required workflow run on that exact head. The successful run above predates the final documentation and comment-only follow-ups.
2. Complete the remaining privacy audit in [PRIVACY_DATA_MAP.md](PRIVACY_DATA_MAP.md): permissions/call paths, provider payloads and retention, refresh scheduling, diagnostics, local storage/backups/deletion, and build variants.
3. Reconcile current provider terms, privacy policies, attribution, third-party notices, and LGPL obligations against the shipped build.
4. Review the user-facing `READ_PROVIDER` permission wording. Any replacement copy remains a separate owner-approved change.
5. Verify the protected-main Profile signing and automatic testing-release path only if the required secrets are configured and publication is authorized. No release currently exists.
6. Physical-device, TalkBack, visual, and performance checks are **NOT TESTED**.
7. Do not merge or publish until the final PR review and explicit owner authorization.
 
## Known boundaries

- Meridian's platform/packaging probe is not a production weather engine or consumer API. Solstice
  must keep its current provider path until a stable, approved Meridian contract and artifact exist.
- Motion-UI is independently maintained. Solstice records only its pinned consumer API and
  app-side adoption requirements.
- Root PRIVACY.md is now Solstice's own notice, based on the current code-backed data map. The audit remains open; do not broaden its claims without evidence.
- No product feature, new provider, UI change, or new dependency is authorized by this cleanup task.

## Working method

Keep changes grouped into coherent commits, preserve current-main decisions, and open a PR for review.
Do not merge, publish releases, change protected repository settings, or incur unapproved paid usage.
