# Solstice product and release plan

**Status:** proposed baseline for the first public release, recorded 2026-11-26. This document defines product scope and release gates; it does not claim that every listed feature is implemented today.

## 1. Product goal

Solstice is an Android weather application that helps people decide what to do and how to prepare, without overwhelming them with data. The app should make the most useful information obvious, explain relevant changes, and offer deeper detail only when requested.

The product principle is **actionable weather, not a dashboard of every available measurement**. See [DESIGN.md](DESIGN.md) for the product's design principles.

## 2. Platform and first-release boundary

- **Platform:** Android.
- **Minimum supported version:** Android 10 (API 29).
- **First release:** Android phone-first. The app must remain usable on different screen sizes and must not clip or lose essential controls, but dedicated foldable/tablet layouts are not a 1.0 blocker.
- **No account requirement:** core weather functionality must work without a Solstice account.
- **No mandatory background location:** approximate foreground location is sufficient for ordinary weather use. Manual place search remains available when location access is denied.
- **Weather backend:** Solstice continues using its current weather-provider path. A future Meridian integration must not block 1.0; it requires a production-ready, reviewed consumer API and a separate adoption decision.

## 3. MVP definition: what 1.0 must contain

The MVP is the smallest complete Solstice product that can be recommended as a stable everyday weather app. It is not a prototype and not a promise to include every idea in the long-term roadmap.

### 3.1 Location and places

Required:
- First-run choice to use the device's location or search for a place.
- Approximate location works; precise access is not required for the basic experience.
- Manual search works without location permission.
- Users can preview, add, remove, and reorder saved places.
- Show a place's local time when it differs from the device's time.
- Handle denied/revoked permissions, unavailable location services, travel, and unresolved searches without blocking weather for saved places.

Not required for 1.0:
- Continuous background GPS tracking as a general app requirement.
- Calendar-derived locations or event scheduling.

### 3.2 Current weather and forecasts

Required:
- Current conditions and actual air temperature.
- **Feels-like temperature presented prominently alongside actual temperature**, with an explanation of contributing factors when the available data supports one. It must not be presented as a personal measurement.
- Hourly forecast and daily forecast, with a planning horizon of up to 10 days when supported by the selected source.
- Daily high/low and meaningful changes through the day.
- Rain/snow timing, probability, and amount when reliable data is available. Do not imply minute-level precision where the source does not provide it.
- Sunrise/sunset and essential sun/UV information.
- Weather data freshness, source attribution, and clear stale/error/offline states.
- A useful fallback when one provider fails or does not cover a location.

Conditional information:
- UV, air quality, wind, pollen, pressure, visibility, and similar metrics appear only when their data is available and the result is useful. Missing source data must not be represented as a healthy or safe reading.

### 3.3 Outlook and practical guidance

Required:
- The first Outlook experience: **Up next**, **Best time to be outside**, and a forecast day list covering up to 10 days when data is available.
- A concise summary of notable changes ahead, rather than repeating every forecast number.
- A contextual **What to wear** recommendation that helps with clothing and footwear, rain protection, and sun protection. Keep advice grounded in available forecast facts; do not imply personal certainty.
- Relevant sun/moon information, including the existing moon-phase experience and the sun's daily timing.

The first release does not need every future Outlook idea. Advanced sky-event recommendations and more elaborate planning can follow after 1.0.

### 3.4 Alerts and proactive information

Required:
- Show official weather warnings when supported by the sources for the place. Include issuer/source, severity, and validity period.
- Provide user control over notifications and explain the relevant permission before asking Android.
- Implement only proactive precipitation notifications/countdowns that meet the agreed shared precipitation rule and have a reliable source; avoid duplicate or zero-probability alerts.
- Notifications must be dismissible and must not be required for core weather functionality.

Deferred:
- The full configurable alert catalogue (for example frost, rapid pressure changes, pollen thresholds, and multiple personalized watches).
- A fully customized daily briefing and every possible Live Update scenario. These can expand after the first stable release.

### 3.5 Settings, privacy, and quality

Required:
- Essential settings: units, appearance, language where translations are shipped, and notification controls.
- Accessibility: TalkBack semantics, usable touch targets, readable contrast, large text, and alternatives to gesture-only actions.
- Designed loading, empty, error, permission-denied, and offline states for core flows.
- No advertising, tracking, social features, or account requirement.
- No telemetry or data contribution by default. Do not show an opt-in prompt for a data-sharing service that is not actually available.
- Complete source attribution, applicable licence notices, and a reviewed public privacy notice before stable release.

## 4. Explicitly outside the 1.0 MVP

These ideas remain valid roadmap items, but they must not delay the first stable everyday weather app:

| Feature | Planned placement | Boundary |
|---|---|---|
| Weather for calendar events | Post-1.0, candidate 1.1 | Optional, read-only Android `CalendarContract` API. No Microsoft Graph, Outlook SDK, OAuth, or server-side calendar access. Outlook events are visible only when synchronized into Android's calendar provider. |
| Wear OS companion | V2/V3+ | Start with a focused Tile and a small set of complications; do not begin with a full watch app. |
| Dedicated foldable/tablet adaptive components | Motion UI future work; Solstice adoption after a stable API | Generic component/layout capability belongs in Motion UI. Solstice owns later product composition. |
| Full Solstice widget redesign | Post-1.0 | Existing inherited widgets may remain where functional; a Solstice-specific widget family is not a 1.0 gate. |
| Expanded personalized alert catalogue and advanced Live Updates | Post-1.0 | Add incrementally after thresholds, user controls, reliability, and battery impact are validated. |
| Production Meridian integration | Separate adoption milestone | Not a release prerequisite until Meridian has a stable, approved production artifact and the Solstice-side compatibility/privacy review is complete. |
| User accounts, social weather reports, mandatory cloud sync | Not planned for 1.0 | No account or server dependency for the core app. |

## 5. Release stages and exit criteria

Release timing is milestone-based, not calendar-based. No public date should be announced until the current implementation has been audited against the gates below.

### Stage 0 — MVP definition and foundation

**Status:** this document establishes the initial scope.

Work:
- Reconcile the current implementation and inherited Breezy behavior with the MVP list.
- Mark each item as implemented, incomplete, deferred, or needing device validation.
- Identify release blockers and test coverage.
- Keep Solstice-owned work separate from Motion UI and Meridian internal roadmaps.

Exit: MVP scope is agreed, remaining work has an owner and order, and the Alpha criteria can be tested.

### Stage 1 — Alpha

**First planned version:** `0.1.0-alpha.1`

Audience: a small, invited testing group.

Alpha begins when an end-to-end everyday-weather loop is usable:
- The app can be installed and launched on supported Android versions.
- A user can choose a location or search for a place.
- Current weather, actual and feels-like temperature, and core hourly/daily forecast can be viewed.
- A user can add and revisit saved places.
- Loading, permission-denied, no-network, and source-failure states do not crash or trap the user.
- Data and places survive ordinary app restarts and app updates.
- No known release-blocking security or privacy issue exists.

Alpha may be incomplete and visibly labelled as pre-release. It is for validating the direction, core flows, device compatibility, and early usability—not for promising feature completeness.

### Stage 2 — Beta

**First planned version:** `0.9.0-beta.1`

Audience: a wider opt-in testing group.

Beta begins when:
- All MVP features intended for 1.0 are implemented.
- The app is in feature freeze: no new 1.0 features are added without an explicit release-scope decision.
- Forecasts, Outlook, What to wear, places, warnings, settings, privacy, and accessibility have passed their acceptance checks.
- Known P0/P1 defects are resolved; remaining defects are documented with workarounds and owners.
- Upgrade, data migration, source fallback, offline, permission, battery, and notification behavior have been tested.
- Privacy policy, licence notices, source credits, and release notes are ready for review.

Beta is primarily stabilization and feedback. Feature requests normally move to the post-1.0 roadmap.

### Stage 3 — Release Candidate

**First planned version:** `1.0.0-rc.1`

A release candidate is the exact production build intended for stable release unless a blocker is found.

Exit criteria:
- No known P0/P1 defect.
- Required CI passes on the exact release commit.
- Release build, signing, package identity, installation, and update path are verified.
- Manual device checks cover the minimum Android version and representative current Android devices.
- TalkBack, large font, contrast, permissions, notifications, offline behavior, and core forecast flows have been checked.
- Privacy, source attribution, licence notices, and public release documentation are approved.
- Release notes and known limitations are published.

If a release blocker appears, fix it and issue a new `rc.N`. Do not add features to an RC.

### Stage 4 — Stable 1.0

**Stable version:** `1.0.0`

Ship when the RC passes the above criteria and there is no unresolved release blocker. A stable release means the MVP is complete and suitable for normal everyday use; it does not mean every post-1.0 roadmap idea is complete.

## 6. Version naming and Android version codes

Use Semantic Versioning for every formal Solstice release:

`MAJOR.MINOR.PATCH`

- **MAJOR:** incompatible product/data behavior or a major generation change (for example 1.x → 2.0).
- **MINOR:** backward-compatible user-facing features (for example 1.0 → 1.1).
- **PATCH:** backward-compatible fixes, reliability, and security updates (for example 1.0.0 → 1.0.1).

Before 1.0, the leading zero signals pre-stable development. Pre-release identifiers use these names:

| Stage | Example Git tag | Example Android `versionName` |
|---|---|---|
| Alpha | `v0.1.0-alpha.1` | `0.1.0-alpha.1` |
| Later Alpha | `v0.1.0-alpha.2` | `0.1.0-alpha.2` |
| Beta | `v0.9.0-beta.1` | `0.9.0-beta.1` |
| Later Beta | `v0.9.0-beta.2` | `0.9.0-beta.2` |
| Release candidate | `v1.0.0-rc.1` | `1.0.0-rc.1` |
| Stable | `v1.0.0` | `1.0.0` |
| First maintenance patch | `v1.0.1` | `1.0.1` |
| First feature release | `v1.1.0` | `1.1.0` |

Rules:
- Formal releases use an immutable Git tag prefixed with `v` and a matching GitHub Release.
- Alpha, Beta, and RC releases are marked as GitHub pre-releases. Stable releases are not.
- Increment the pre-release number (`alpha.1`, `alpha.2`, `beta.1`, and so on) for each replacement build in that stage.
- Do not reuse or move a formal release tag.
- Android `versionCode` is a separate, monotonically increasing integer. Every published APK/AAB must have a greater `versionCode` than the previous published artifact; never reset or reuse it.
- The current inherited baseline is `versionName 6.2.3` / `versionCode 60203`. The first formal SemVer artifact must use a `versionCode` greater than `60203`; subsequent artifacts continue increasing.
- Formal Alpha → Beta → RC → Stable builds should keep the same application ID (`io.github.information89.solstice`) and the same dedicated release signing certificate, so Android can update the app in place. Keep signing material outside GitHub source; CI may receive it only through protected secrets.
- Development `debug` and `profile` builds remain separate. The current rolling GitHub `testing` pre-release is an internal development channel, not a formal Alpha, Beta, or Stable release. It must not be used to imply that public Alpha has begun.
- Keep the tested commit SHA in release metadata/asset names so testers can identify exactly what they installed. Do not put a moving branch name in a formal version.

## 7. Channels and communication

| Channel | Purpose | Public status |
|---|---|---|
| CI / `testing` | Frequent profile builds for development and device checks | Development channel; not a formal release |
| Alpha | Validate core direction and early flows with invited testers | Pre-release |
| Beta | Validate feature-complete MVP with a wider opt-in group | Pre-release |
| Release Candidate | Final production candidate and regression pass | Pre-release |
| Stable | Normal everyday use | Stable release |

Every formal pre-release should state:
- What changed.
- What is incomplete or known to be unreliable.
- Android versions/devices tested.
- Whether upgrade from the previous formal release is supported.
- How testers can report a reproducible issue without sending private calendar or location data.

## 8. After 1.0

The roadmap can evolve after the MVP is stable. The current candidates are:
1. Calendar-aware weather through Android's read-only Calendar Provider API.
2. More personalized alert types and richer Live Updates.
3. A Solstice-designed widget experience.
4. Adoption of reusable adaptive window/foldable components when Motion UI exposes a stable capability.
5. Wear OS exploration in V2/V3 or later.

These are candidates, not release promises. Promote an item to a specific version only after its scope, owner, privacy impact, and acceptance criteria are reviewed.

## 9. Definition of done for a release

A release is complete only when:
- Its exact source commit and version identifiers are recorded.
- Required CI passes on that exact commit.
- The production artifact is built, signed, and installed using the intended release path.
- Core flows and release-specific features pass tests and manual checks.
- Known limitations are documented; no failed or skipped check is described as passing.
- Public release notes, source credits, licences, and privacy documentation are present.
- The preceding stable version can be upgraded without unintended data loss.

No release date is committed by this plan. We should publish dates only after the implementation audit has converted the remaining MVP work into a credible sequence.
