# Solstice reconstruction and Breezy exit audit

Status: **initial repository audit; not a file-by-file provenance or legal clearance**  
Decision: **gradual reconstruction (option A)**  
Scope: Solstice repository on `main`, inspected 2026-11-25.

This document records what should remain, what must be replaced, what can be supplied by existing platform/framework capabilities, and what Solstice must implement itself. It is a migration map, not authorization to delete files. Every removal requires a replacement, tests, data-migration plan where applicable, and a provenance/licence check.

## 1. Target state

Solstice is an independent weather application. Breezy Weather was a temporary experimental starting point, not a permanent architectural dependency.

- **Solstice** owns product behavior, user experience, navigation, places, preferences, app lifecycle, presentation, and app-specific persistence.
- **Meridian-Engine** owns weather-provider access, meteorological normalization/processing, source selection/blending, forecast data, provenance, and weather-domain results through its public API.
- **Motion / Motion UI** own reusable interaction, animation, design tokens, and reusable UI primitives within their documented boundaries.
- **Android and maintained libraries** provide platform mechanisms such as permissions, background scheduling, notifications, widgets, storage engines, maps, and UI primitives. Solstice integrates and configures these; it should not reimplement platform infrastructure without a product-specific reason.

The target dependency direction is:

`Android / libraries` → `Solstice app` → public contracts of `Meridian` and `Motion`

Meridian and Motion must not import Solstice code.

## 2. What was inspected

The Git tree contained 2,331 entries (not truncated), including:

- 627 entries under `app/src/main/kotlin/org/breezyweather/` (502 Kotlin files plus directory entries).
- 1,608 entries under `app/` overall.
- 672 entries under `app/src/main/res/`.
- Gradle modules: `:app`, `:data`, `:domain`, `:maps-utils`, `:ui-weather-view`, and `:weather-unit`.
- A Git submodule at `motion/`, pointing to `INFORMATION89/Motion.git`.
- Existing Solstice planning and research documents, including `docs/research/AFTER_FORK.md`, `docs/ENGINE_SPEC.md`, and `docs/MERIDIAN_PLAN.md`.

The repository still has strong Breezy fingerprints:

- Kotlin namespace and source package: `org.breezyweather`.
- Root Gradle project name: `BreezyWeather`.
- Build logic imports and plugins under `breezy.buildlogic` and `breezy.*`.
- Build flag `IS_BREEZY`, Breezy-specific Gradle properties, and source configuration.
- Modules and namespaces such as `breezyweather.data`, `breezyweather.domain`, and `org.breezyweather.unit`.
- Legacy source adapters, domain models, persistence, UI, RemoteViews widgets, resources, and translations.

**Important limitation:** a path or package name is evidence of inherited structure, not proof that every line in that file is unchanged. Solstice-specific modifications are mixed into inherited files. The audit must not label an entire file as safe to delete or safe to keep without inspecting its history, headers, implementation, assets, and dependencies.

## 3. Classification

Use these labels in future inventory work:

- **KEEP** — demonstrably Solstice-authored or an approved independent project; retain, while checking dependencies.
- **REBUILD** — inherited Breezy implementation whose responsibility Solstice still needs; specify independently, implement anew, migrate, then remove.
- **PLATFORM** — use Android, Kotlin/Jetpack, or a maintained dependency through its supported API.
- **MERIDIAN** — responsibility belongs in Meridian, not in Solstice.
- **MOTION** — reusable interaction/UI responsibility belongs in Motion or Motion UI.
- **REVIEW** — mixed provenance, third-party material, legal uncertainty, or insufficient evidence. Do not copy, delete, or relabel until reviewed.

## 4. Initial subsystem inventory

| Area / current location | Initial classification | Required destination / action |
|---|---|---|
| `app/src/main/kotlin/org/breezyweather/ui/expressive/` | KEEP + REVIEW | Preserve the Solstice-specific expressive work. Inspect each file for inherited imports, assets, models, and UI dependencies. Move genuinely reusable primitives to Motion UI; keep weather/product-specific composition in Solstice. |
| `motion/` submodule | KEEP / MOTION | Retain as the existing independent Motion integration. Verify the submodule API and reconcile naming/documentation with the separate Motion-UI project before changing dependencies. |
| `app/src/main/kotlin/org/breezyweather/sources/` | REBUILD + MERIDIAN + REVIEW | Provider adapters and weather-source logic must move to Meridian or be replaced by Meridian adapters. Preserve only app/platform adapters that are explicitly outside meteorological provider logic, after review. |
| `app/src/main/kotlin/org/breezyweather/domain/` and Gradle `:domain` | REBUILD + REVIEW | Replace inherited Breezy domain types with Solstice-owned app models and Meridian public result contracts. Do not make UI depend directly on Meridian internal types. |
| `app/src/main/kotlin/org/breezyweather/data/`, `:data`, SQLDelight schemas and migrations | REBUILD + PLATFORM + REVIEW | Design Solstice-owned persistence for places, user preferences, widget configuration, and app state. Keep weather cache/storage owned by Meridian behind its storage contract. Plan schema/data migration before removing SQLDelight. |
| `app/src/main/kotlin/org/breezyweather/background/` | REBUILD + PLATFORM + REVIEW | Rebuild app jobs, refresh orchestration, boot handling, notifications, and update scheduling around Meridian and Android APIs. Use WorkManager/AlarmManager only where their documented behavior fits; do not port Breezy job implementations. |
| `app/src/main/kotlin/org/breezyweather/remoteviews/` and `background/receiver/widget/` | REBUILD + PLATFORM | Replace legacy widget implementation with Jetpack Glance where suitable. Glance has its own widget UI model; Motion Compose components cannot simply be reused inside widgets. Keep widget data access behind Solstice/Meridian contracts. |
| `app/src/main/kotlin/org/breezyweather/ui/main/`, `ui/details/`, `ui/search/`, `ui/settings/`, `ui/about/`, `ui/alert/` | REBUILD + REVIEW | These are mixed product/UI surfaces. Rebuild each screen and flow from Solstice requirements and approved design specs. Reuse only individually verified Solstice-authored components and approved libraries. |
| `app/src/main/kotlin/org/breezyweather/common/` | REBUILD + PLATFORM + REVIEW | Split generic Android/platform utilities from Breezy-specific abstractions. Prefer AndroidX/Kotlin standard APIs or maintained libraries; implement only Solstice-specific glue. |
| `:weather-unit` and `org.breezyweather.unit` | REBUILD or PLATFORM + REVIEW | Audit unit conversion, formatting, CLDR data, and translations. Decide which functionality is supplied by standard locale/formatting APIs or a compatible library and which small domain-specific conversions Solstice must own. Do not copy inherited implementation or translation files into a new module. |
| `:ui-weather-view` | REVIEW → likely REBUILD | Audit custom weather backgrounds, animations, painters, and drawable assets individually. Replace with Solstice's own sky/condition presentation and Motion capabilities where appropriate. Assets and animation sequences need provenance/licence review. |
| `:maps-utils` | REVIEW + PLATFORM | This is a separate Google Maps Android utility namespace, not automatically Breezy-owned. Identify its upstream origin, licence, modifications, and actual consumers. Prefer a maintained compatible dependency if possible; do not rewrite or delete blindly. |
| `app/src/main/res/` | KEEP + REBUILD + REVIEW | Retain only verified Solstice-created assets/resources. Recreate inherited screens, icons, drawables, animations, layouts, and strings from independent specs. Audit fonts, images, raw JSON/data, and other assets separately. |
| `weather-unit/src/main/res/values-*/` and app translations | REBUILD + REVIEW | Existing research records 1,162 inherited Breezy strings and 105 Solstice expressive strings across 65 languages. Start with new English source strings and Mexican Spanish as already decided; do not carry Breezy community translations forward without a rights/provenance decision. |
| `config/`, `config-fork/`, `buildSrc/`, root Gradle files, `app/build.gradle.kts` | REBUILD + REVIEW | Remove Breezy-specific build conventions, plugin IDs, configuration flags, namespaces, properties, and project identity gradually. Preserve Solstice CI, profile build, signing, and release behavior by specifying and testing them independently. |
| `fastlane/`, `.github/`, release metadata and workflows | REVIEW + KEEP/REBUILD | Preserve Solstice-owned CI/release automation where provenance is clear. Replace inherited store text, screenshots, release metadata, and workflows that assume Breezy flavors or infrastructure. |
| `third_party/`, `3RD_PARTY.md`, `LICENSE`, `LICENSE_ADDITIONAL` | REVIEW | Inventory every vendored component, attribution, notice, and licence. Keep required notices until legal review confirms they can be changed. Do not assume the app's overall licence can change merely because code is rewritten. |
| `docs/` Solstice planning, research, and specifications | KEEP + REVIEW | Retain independently authored Solstice documentation. Correct outdated statements that Solstice will remain a Breezy fork. Keep archived upstream documentation clearly marked as historical/reference-only. |

## 5. What the platform or existing projects already provide

Solstice should integrate these capabilities rather than write replacements without a clear need:

| Need | Existing capability | Solstice responsibility |
|---|---|---|
| Android UI primitives and accessibility | Jetpack Compose, Material 3, Android accessibility APIs | Compose Solstice's product UI and meet its design/accessibility requirements. |
| Reusable animation and interaction | Motion / Motion UI | Use its public APIs; contribute reusable primitives to the owning project after approval. |
| Weather data, provider adapters, normalization, provenance and blending | Meridian-Engine | Integrate its public contracts and map results to app presentation models. |
| Background work | AndroidX WorkManager and platform scheduling APIs | Define refresh policy, constraints, user controls, and orchestration. |
| Notifications | Android notification APIs and AndroidX support | Define Solstice alert semantics, content, channels, actions, and permission UX. |
| Home-screen widgets | Jetpack Glance / Android widget APIs | Design Solstice widget layouts, refresh behavior, configuration, and data flow. |
| Local persistence | Room / AndroidX SQLite options, DataStore for preferences | Own the app's schema and migration strategy; keep engine cache separate from user-owned app data. |
| Networking and serialization for engine sources | Meridian's chosen Ktor and serialization stack | Do not duplicate provider HTTP clients in Solstice. |
| Maps and geographic rendering | Android/Google Maps APIs or a reviewed maintained library | Decide product map behavior and integrate a suitable provider; do not maintain a forked utility library without need. |
| Location and permissions | Android location APIs / a reviewed location provider | Own consent, foreground/background behavior, privacy, and place selection. |
| Localization formatting | Android resource and locale APIs, ICU/CLDR facilities | Write Solstice strings and weather terminology; do not inherit Breezy translations by default. |
| Charts | Compose-compatible maintained chart library, if one meets requirements | Own chart semantics, accessibility, visual design, and tests. Select a library only after licence and capability review. |

A platform API is not a complete product feature. For example, WorkManager schedules work but does not decide when Solstice should refresh weather; Glance renders widgets but does not define their content or data model; Meridian supplies weather results but does not own Solstice's saved places or screens.

## 6. What only Solstice should implement

These are product responsibilities and must not be delegated to Breezy or duplicated in Meridian/Motion:

1. Product identity, information architecture, navigation, onboarding, and app settings.
2. Solstice's forecast presentation, cards, page composition, place switching, and unique interactions.
3. User-owned place list, favorites, ordering, preferences, and widget configuration.
4. App-specific persistence and safe migration of existing user data.
5. The bridge between Meridian public results and Solstice presentation models.
6. App refresh orchestration, notification policy, widget refresh policy, and lifecycle integration.
7. Permission/consent UX and privacy decisions for location and notifications.
8. Attribution presentation driven by Meridian provenance and provider terms.
9. Solstice-specific error, loading, empty, offline, and degraded-data experiences.
10. App-specific tests, release configuration, and accessibility verification.

Do not put these responsibilities in Meridian or Motion just because it is convenient.

## 7. Migration order (gradual, no big-bang rewrite)

Each step is a separate approved increment. Keep the current app usable until its replacement is verified.

1. **Baseline and provenance ledger**
   - Record current build/test status and a working device baseline.
   - Inventory modules, source files, resource assets, licences, dependencies, and Breezy-specific build logic.
   - Mark each item KEEP, REBUILD, PLATFORM, MERIDIAN, MOTION, or REVIEW.
   - Identify Solstice modifications mixed into inherited files.

2. **Architecture and contracts**
   - Approve Solstice app architecture and module boundaries.
   - Approve Meridian integration boundary and the app-side adapter.
   - Define ownership of saved places, preferences, weather cache, and widget state.
   - Define data migration and backward-compatibility policy.

3. **Build identity and foundations**
   - Replace Breezy project identity, namespace, plugin conventions, and build flags in controlled steps.
   - Keep CI, signing, profile builds, and test distribution working.
   - Establish clean Solstice-owned modules and tests.

4. **Meridian integration**
   - Integrate a minimal stable Meridian contract.
   - Replace one weather capability at a time.
   - Compare output and failure behavior against recorded fixtures and independent reference data.
   - Remove the corresponding legacy source path only after parity and regression checks.

5. **Core app data and lifecycle**
   - Migrate place/settings storage.
   - Replace refresh scheduling and app orchestration.
   - Preserve user data through tested migrations and rollback-safe releases.

6. **Screens and interactions**
   - Rebuild the forecast and navigation flows from Solstice specifications.
   - Rebuild search, places, details, alerts, and settings incrementally.
   - Extract reusable components to Motion UI where approved.

7. **Widgets, notifications, and remaining platform surfaces**
   - Replace RemoteViews widgets with Glance/platform implementations.
   - Rebuild notifications, tiles, shortcuts, and background integrations as applicable.

8. **Resources, translations, and legal cleanup**
   - Replace inherited strings and assets with independently created material.
   - Audit fonts, icons, maps data, attribution, licences, and third-party notices.
   - Update store metadata and privacy documentation.

9. **Breezy exit verification**
   - No runtime or build dependency on Breezy modules, plugin conventions, package namespaces, source adapters, or internal APIs.
   - No inherited source/assets/translations remain unless their licence and provenance have been explicitly reviewed and their continued use approved.
   - A clean checkout builds and tests using only documented Solstice dependencies and submodules.
   - Data migration, notifications, widgets, offline behavior, and release workflows are verified.

## 8. Clean-room and AI-agent protocol

The existing repository is a mixed-origin artifact. Reading it to identify migration responsibilities does not make its implementation safe to copy.

For each REBUILD subsystem:

1. A specification phase records required user-visible behavior, inputs/outputs, edge cases, and platform constraints using Solstice requirements, public standards, official API documentation, and observable behavior.
2. The specification must not contain copied source, comments, identifiers, code structure, pseudocode, or unexplained implementation choices from Breezy.
3. Record all consulted sources and the author/date/version of the specification.
4. Review the specification for inherited expression and licensing concerns before implementation.
5. Implement from the approved specification and permitted public documentation; do not use Breezy source as a coding reference.
6. Record implementation provenance, dependencies, tests, and review results.
7. If a developer or AI agent has already seen inherited code, do not describe that participant as an unexposed clean-room implementer. Assign a genuinely separate implementation process where practical, and record limitations honestly.

**AI limitation:** separate chat sessions or different prompts do not prove model isolation. An AI model may have encountered public source code during training, and prior repository context may contaminate a claimed clean implementation. AI assistance can follow source restrictions and produce provenance records, but cannot certify legal clean-room status. Obtain qualified legal review for licensing, copyright, trademarks, patents, and applicable jurisdictional questions.

## 9. Explicit do-not-delete list

Do not remove any of the following until its replacement and migration are verified:

- User database, SQLDelight schemas, or migration history.
- Place models, saved places, preferences, and configuration data.
- Manifest declarations, permissions, services, receivers, providers, or deep links.
- Background refresh, boot handling, notification, widget, tile, or shortcut behavior.
- Solstice-authored expressive UI, tokens, motion work, and custom release/CI changes.
- Third-party notices, licence files, copyright headers, or attribution data.
- Assets, fonts, raw datasets, map/geographic data, and translations whose provenance is unresolved.
- Existing signing and test-distribution behavior.

## 10. Open audit items

These require source-level review before the inventory can be considered complete:

- File-by-file origin classification, including copyright headers and diffs from the upstream base commit.
- Full Gradle dependency graph and transitive dependency/licence report.
- All manifest components and their runtime responsibilities.
- Database schema, migrations, and data ownership.
- Asset/font/icon/raw-data provenance.
- Actual use and upstream origin of `:maps-utils`, `:ui-weather-view`, and `:weather-unit`.
- Solstice-specific changes mixed into Breezy classes and resources.
- Compatibility of the current Motion submodule with the planned Motion UI extraction.
- Legal review of the current LGPL-3.0 text, additional terms, notices, and any future licence transition.

## 11. Source references

Repository evidence inspected for this initial audit:

- `settings.gradle.kts`, `app/build.gradle.kts`, root `build.gradle.kts`.
- `.gitmodules`.
- Module build files for `:data`, `:domain`, `:weather-unit`, `:ui-weather-view`, and `:maps-utils`.
- `docs/RULES.md`, `docs/PLAN.md`, `docs/ENGINE_SPEC.md`, `docs/MERIDIAN_PLAN.md`.
- `docs/SOURCES.md`, `docs/research/AFTER_FORK.md`, `docs/CONTINUE.md`.
- `README.md`, `LICENSE`, `LICENSE_ADDITIONAL`, `3RD_PARTY.md`, and `UPSTREAM_BASE`.

This audit must be updated as source-level reviews are completed. Do not treat the initial classifications as a completed legal or provenance determination.

## 12. Focused audit: `ui/expressive/` (2026-11-25)

This package contains a substantial amount of Solstice-specific product/UI work, but it is **not yet independent of Breezy**. It is the strongest preservation candidate in the current application, not a clean standalone module. Its source files still carry the fork's LGPL header and Breezy package path; those facts do not establish the origin of each implementation.

### 12.1 Solstice work to preserve

The following areas show clear Solstice product intent and should be preserved as behavior/design requirements while their implementations are reviewed:

- **App shell and page composition:** `shell/SolsticeShell.kt`, `PageFrame.kt`, `Toolbar.kt`, `ToolbarMotion.kt`, `ShellNavigation.kt`, `PlaceWindow.kt`, `Postcard.kt`.
- **Forecast experience:** `ForecastPage.kt`, `RainCard.kt`, `Summary.kt`, `WeekPage.kt`, `MoonCard.kt`, `MoonRenderer.kt`, `MoonSky.kt`, `MoonVisibility.kt`, `MoonPhaseIcons.kt`.
- **Places and search experience:** `PlacesPage.kt`, `Visits.kt`, `SearchOverlay.kt`, `Cities.kt`, and the reorder UI.
- **Solstice visual language:** `theme/Tokens.kt`, `ExpressiveTheme.kt`, `ExpressiveType.kt`, `SolsticeMotionScheme.kt`, `sky/SkyPalette.kt`, `SkyBackground.kt`, `SkyEffects.kt`, and `SolsticeIcons.kt`.
- **Product-specific components:** weather cards, sky rendering/effects, moon/sun visualization, place time, weather summaries, and forecast presentation.

Preserve the requirements, interaction decisions, and visual intent even when the implementation must be rewritten. Do not assume every line or asset in these files is independently authored; inspect Git history/diffs and resource provenance.

### 12.2 Breezy coupling that must be removed

The focused source review found direct dependencies that prevent this UI from being considered independent:

- `ForecastPage.kt`, `PlacesPage.kt`, `WeekPage.kt`, `SearchOverlay.kt`, and `Cities.kt` import `breezyweather.domain.location.model.Location`.
- Several screens read the legacy weather graph directly (for example `location.weather`, daily forecasts, sun/moon values, and weather codes). These must be mapped from Meridian's public contracts into Solstice-owned presentation models.
- `SearchOverlay.kt` imports legacy `ui.search.SearchViewModel` and `LoadableLocationStatus`; search orchestration must move behind Solstice-owned search/place interfaces.
- Many files reference `org.breezyweather.R`, so resource ownership and namespace migration must be handled as a coordinated build/resource task.
- `Cities.kt` converts its own `City` model into Breezy's `Location`, and stores recents using Android SharedPreferences. Replace the conversion with a Solstice-owned place model; decide on DataStore or another supported persistence abstraction for search recents.
- `SheetDialog.kt` explicitly documents matching upstream `AlertDialogNoPadding` call-site slots to make a dialog-to-sheet substitution. This compatibility-shaped API must be reviewed and specified independently; do not preserve it solely to keep inherited call sites working.
- The theme imports app resources and Android runtime APIs. It can remain app-owned until its reusable contract is extracted.

### 12.3 Destination by responsibility

| Files / responsibility | Destination | Notes |
|---|---|---|
| `theme/Tokens.kt`, generic type/shape/spacing tokens | MOTION (kit contract) + REVIEW | Extract only reusable tokens. Solstice-specific weather palettes and product semantic tokens remain in Solstice. |
| `components/Glass.kt`, `IconBadge.kt`, `PressMorph.kt`, `ScreenCorners.kt`, `ScreenPush.kt`, `SheetHandle.kt`, `SheetShape.kt`, `SheetDialog.kt`, `SolsticeSheetState.kt` | MOTION / Motion UI + REVIEW | Candidate reusable interaction/UI primitives. Re-specify against Motion's API and check licensing/provenance before moving. |
| `components/SolsticeCard.kt` | MOTION UI primitive + Solstice weather-card composition | Separate generic card/material behavior from its sky/weather-specific parameters. |
| `components/SolsticeIcons.kt` | REVIEW; likely split | Identify source icon set and licence. Generic approved icons can use a maintained icon library; Solstice-specific weather glyphs remain product assets. |
| `components/SolsticeMotion.kt` | MOTION | Compatibility facade only; remove after call sites use Motion's public API. |
| `shell/*` weather pages, cards, summaries, moon/sun and sky | SOLSTICE | Product UI and domain-specific visualizations; rebuild against Solstice models and Meridian data. |
| `shell/PageFrame.kt`, generic toolbar, messages, place/window shell mechanics | MOTION UI + Solstice adapters | Extract only generic frame and interaction primitives; app navigation and product behavior remain in Solstice. |
| `search/SearchOverlay.kt`, `Cities.kt` | SOLSTICE | Keep search UX and city selection as product features; replace Breezy types/ViewModel and move to app-owned interfaces. |
| `reorder/*` | MOTION UI or maintained library + SOLSTICE policy | Generic drag/reorder mechanics may be reusable; card ordering rules and persistence belong to Solstice. |
| `lab/*` | MOTION Lab or remove after migration | These demos belong to the Motion development tool, not the shipped weather product. Audit each file's origin first. |
| `sky/*` | SOLSTICE initially | Weather-aware sky rendering is product-specific. Extract only generic graphics primitives if another consumer is demonstrated. |

### 12.4 Platform/library capabilities already available

- Jetpack Compose supplies layout, drawing, gestures, text, animation primitives, accessibility semantics, and state handling.
- Material 3 supplies theme, typography, shapes, common controls, and bottom-sheet/dialog primitives.
- AndroidX provides navigation, lifecycle, saved state, and supported persistence options.
- Motion supplies the independent motion/gesture/haptic APIs already used by this code.
- Haze is already integrated for backdrop blur; retain it through its public API if its licence, performance, and supported Android versions remain acceptable.
- Android/ICU locale APIs can handle many date, time, and text normalization needs; keep custom logic only where Solstice's product behavior requires it.

Solstice should implement its own weather presentation, place-management behavior, forecast summaries, sky/moon/sun visualization, search product experience, and app-specific state. It should not reimplement Compose, Material controls, platform lifecycle, or generic animation/gesture primitives that Motion already provides.

### 12.5 Required next audit actions

1. Compare the expressive package against the upstream base commit and repository history, file by file. Record which files were newly added, copied then modified, or substantially derived.
2. Audit every drawable, font, vector path, raw data file, and string resource used by this package. The package alone does not prove asset provenance.
3. Build a dependency map from each expressive file to Breezy models, ViewModels, resources, and services.
4. Define Solstice-owned presentation models and repository/use-case interfaces before changing the UI imports.
5. Agree with the Motion and Motion UI repositories on which generic primitives are accepted, then move them via their own specifications and tests.
6. Migrate one vertical slice at a time (for example Forecast data → Solstice model → Forecast UI) and keep the app buildable.
7. Only after the replacement compiles and is verified, remove the corresponding Breezy imports and compatibility adapters.

**Initial conclusion:** preserve the Solstice experience and design decisions; replace the Breezy data/search contracts beneath them; extract reusable interaction primitives to Motion; keep weather-specific visualizations and product behavior in Solstice. This is a responsibility-level audit, not a legal provenance clearance.
