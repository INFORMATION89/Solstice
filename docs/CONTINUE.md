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

## Remaining cleanup and verification

1. Validate all internal Markdown links and remove stale references to deleted external-dependency
   plans and specifications.
2. Audit the active roadmap so it contains Solstice-owned app work only; Motion-UI and Meridian
   implementation work belongs in their respective repositories.
3. Continue the code-backed privacy audit in [PRIVACY_DATA_MAP.md](PRIVACY_DATA_MAP.md), including
   background-location call paths, provider request fields, refresh scheduling, diagnostics,
   retention/deletion, app-to-app disclosures, and build variants.
4. Keep the Solstice-owned [PRIVACY.md](../PRIVACY.md) synchronized with verified behavior. It
   replaces inherited Breezy wording but must not claim that unverified flows or retention practices
   have been fully audited.
5. Review Gradle, CI permissions, signing-secret scope, dependency pins, wrapper integrity, and
   release artifact handling. Verify only what the available CI and repository evidence supports.
6. Run the required checks once on the final coherent branch head. Record physical-device,
   accessibility, and protected-main release checks as NOT TESTED unless they are actually run.

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
