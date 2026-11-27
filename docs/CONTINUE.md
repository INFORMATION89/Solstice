# Solstice — current handoff

**Snapshot:** 2026-11-27  
**Scope:** Solstice Android application only.

## Current repository state

- Work continues on `main`; current baseline is `e55a63863ccddd082ef76507cf03abc2b439a922`.
- PR #22, “maintenance: reconcile Solstice before feature work”, was squash-merged into `main`.
- Post-merge Build workflow run [46056082442](https://github.com/INFORMATION89/Solstice/actions/runs/46056082442) completed successfully on the exact `main` commit. The build, no-old-icons check, hardcoding guard, and debug APK build passed; profile signing and publication were skipped.
- No product runtime behavior or feature was introduced by PR #22.

## Completed audit work after the merge

1. Confirmed all seven SQLDelight tables dependent on saved locations use `ON DELETE CASCADE`: weather, source parameters, alerts, daily, hourly, minutely, and normals. The configured SQLite driver enables foreign keys. `LocationRepository.delete()`, `deleteAll()`, and the sync-style `addAll()` path delete location rows.
2. Confirmed that the current-location preference store is separate from the database. Its clear method resets latitude, longitude, and timestamp, but a complete call-site/reset trace remains open.
3. Inspected the HTTP cache: OkHttp stores up to 50 MiB in `cacheDir/http_cache`. HTTP response directives govern cacheability/freshness; cacheDir is excluded from Android Auto Backup and may be evicted by the OS. No location-specific cache cleanup is configured in the reviewed client setup.
4. Confirmed the app targets API 37 and has no explicit backup attributes or rules in the inspected source manifest. Android's default Auto Backup behavior makes private SharedPreferences and the database eligible for backup; actual merged-variant and device restore behavior still needs testing.
5. Inspected the license evidence: LGPL v3 text plus `LICENSE_ADDITIONAL`, fork-specific AboutLibraries configuration, custom metadata records, and the in-app Dependencies screen. Built-artifact inventory completeness and distribution obligations remain to be reconciled.

## Remaining work

1. Exhaustively trace `CurrentLocationStore.clearCurrentLocation()` call sites, all saved-place removal/reset flows, and any account/app reset behavior.
2. Verify merged manifests and Auto Backup/device-transfer behavior for every shipped build variant; decide whether location preferences and saved-place database should be excluded or selectively backed up. Do not change backup policy without resolving the product/privacy decision.
3. Inspect provider response cache headers and all cache eviction/clear call sites; establish whether location-derived responses are cacheable and when they expire.
4. Reconcile generated AboutLibraries output against dependencies in every shipped variant and inspect actual APK contents for license texts/notices and LGPL source/relink obligations.
5. Review the stale `READ_PROVIDER` label/description (“Breezy Weather”) and the provider's Android permission/access behavior. Any user-facing copy or access-policy change requires a separate review.
6. Continue the remaining provider terms/privacy/attribution, permissions, diagnostics, and build-variant audit in `docs/PRIVACY_DATA_MAP.md`.
7. Physical-device, TalkBack, visual, and performance checks remain **NOT TESTED**.

## Known boundaries

- Meridian remains independent; Solstice must not introduce an unapproved Meridian integration.
- Motion-UI is independently maintained. Solstice documents only its pinned consumer contract.
- Root `PRIVACY.md` is Solstice-specific but the evidence audit remains partial. Do not broaden public claims beyond verified behavior.
- No new provider, telemetry, analytics, monetization, or product behavior is authorized by this audit.
- Solstice is currently a non-commercial project: no advertising, subscriptions, paid features, or in-app purchases are planned.

## Working method

Continue on `main` for this owner-directed maintenance sequence. Keep related documentation changes atomic, avoid small PRs and unnecessary CI runs, and do not alter runtime privacy/security behavior or publish releases without explicit authorization.
