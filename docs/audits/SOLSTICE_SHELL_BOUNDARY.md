# Solstice shell boundary — independent architecture specification

Status: **design specification, not implementation**  
Date: 2026-11-25  
Scope: `app/src/main/kotlin/org/breezyweather/ui/expressive/shell/SolsticeShell.kt` and its immediate orchestration boundary.

## 1. Decision

`SolsticeShell` remains a Solstice-owned Compose presentation component. It must not receive or import Breezy application/domain classes, Meridian engine internals, or provider implementation classes.

The target is:

- **Solstice UI:** renders product state and emits user intents.
- **Solstice application layer:** owns use cases, app state, saved places, visits/pins, search orchestration, refresh policy, and the Meridian adapter.
- **Meridian:** supplies normalized weather results, provenance, source status, and weather refresh operations through its public API.
- **Android/platform adapters:** location permission/current position, lifecycle, system bars, persistence implementation, background scheduling, and Android-specific UI integration.
- **Motion:** owns reusable motion/interaction primitives; Solstice owns product navigation policy and weather-specific visuals.

The UI consumes **Solstice presentation models**, not Meridian types. Meridian's own plan explicitly assigns this mapping to one small bridge in the app.

## 2. Current shell: confirmed responsibilities and coupling

The current file is 714 lines. It combines the Compose page composition with app orchestration and legacy service access.

### 2.1 Responsibilities already inside the shell

- Three-page navigation (Outlook, Forecast, Places), pager state and page transitions.
- Sky background selection and system-bar appearance.
- Legacy main/starred place selection, visit/pin behavior, and stale visit cleanup (migration-only behavior).
- Search overlay presentation and selection handoff to a postcard window.
- Postcard window lifecycle, refresh, pin/unpin, details navigation.
- Place list deletion, undo, reorder, add-current-location and edit actions.
- Weather-source selection dialog and duplicate-source/location handling.
- Recent-search persistence and legacy starred/visit persistence.

Some of these are legitimate product/presentation responsibilities; others are application use cases or persistence and should move out.

### 2.2 Direct Breezy dependencies to remove

Imports/calls currently include:

- `breezyweather.domain.location.model.Location`.
- `MainActivityViewModel` and `SearchViewModel`.
- `SourceManager`.
- `applyDefaultPreset`, `getPlace`, `isDaylight`.
- `SecondarySourcesPreference` and `SnackbarHelper`.
- `org.breezyweather.R` and the Breezy theme/resource namespace.
- ViewModel operations for add/delete/move/update/select/refresh and source-dialog state.

These are not merely naming issues. They expose the UI to Breezy's domain model, repository orchestration, provider selection, settings, and app lifecycle.

### 2.3 Existing Solstice state that should remain Solstice-owned

The following behavior is product policy and must not be delegated to Meridian or Motion:

- No star or favorites; Places is a plain reorderable saved list.
- Home follows device location by default; a fixed Home is selected in Settings → Locations.
- Travel swaps Forecast automatically between current location and Home; Home is marked in Places.
- Search opens an ephemeral preview with Cancel and Add. “Visiting” marks only an unsaved preview.
- A picked saved place opens as a place window/postcard according to the current navigation design.
- Search-to-postcard morph, page ownership during the transition, and current navigation semantics.
- Place ordering, deletion/undo UX, and user-facing messages.

Persisted place identities and visit/pin semantics belong to Solstice's app data layer, not the UI component and not Meridian's weather cache.

## 3. Target dependency graph

```text
SolsticeShell (Compose)
    |  ShellUiState + ShellActions (Solstice presentation contract)
    v
SolsticeShellViewModel / state holder
    |  Solstice use cases
    +--> ObservePlaces
    +--> SetHomeConfiguration
    +--> SearchPlaces
    +--> AddPlace / RemovePlace / RestorePlace / ReorderPlaces
    +--> AddPlace / PreviewPlace / DismissPreview
    +--> RefreshPlace / RefreshMainPlace
    +--> ObserveWeather / ObserveRefreshStatus
    +--> ObserveSourceStatus / ExplainProvenance
    |
    +--> Solstice PlaceRepository + SettingsRepository
    |
    +--> MeridianWeatherGateway (Solstice adapter)
             |
             v
         Meridian public API
         refresh(place, features)
         results(place): Flow<MeridianResult>
         sources(place)
         credits(result)
```

Platform adapters provide current location, permission checks, Android persistence, and scheduling. Motion is called by presentation components through its public API; it is not a domain dependency.

**Forbidden dependency direction:** UI -> Breezy `Location`, `MainActivityViewModel`, `SearchViewModel`, `SourceManager`, provider classes, or Meridian internal models.

## 4. Solstice-owned presentation contracts

These are conceptual Kotlin shapes to guide an independent implementation. They are not a claim that Meridian already has these exact types, and names may be refined in the implementation spec.

```kotlin
data class ShellUiState(
    val places: List<PlaceCardModel>,
    val mainPlaceId: PlaceId?,
    val selectedPlaceId: PlaceId?,
    val forecast: ForecastPageModel?,
    val sky: SkyPresentationModel,
    val search: SearchUiState,
    val refresh: RefreshUiState,
    val messages: List<ShellMessageModel>,
    val isInitialLoading: Boolean,
)

data class PlaceCardModel(
    val id: PlaceId,
    val title: String,
    val subtitle: String?,
    val localTime: Instant?,
    val isCurrentLocation: Boolean,
    val isMain: Boolean,
    val isVisited: Boolean,
    val isRefreshing: Boolean,
    val freshness: FreshnessModel?,
)

data class PlaceId(val value: String)
```

Rules:

1. Presentation models contain only values required to render the screen. They must not wrap or expose Breezy `Location` or Meridian `MeridianResult`.
2. Weather quantities should retain units/valid times/provenance at the app adapter boundary; formatting for the UI must be explicit and testable.
3. Missing, stale, partial, and failed data are separate states. Do not turn missing weather into a fake clear-sky forecast.
4. Stable place identity must be defined by Solstice and mapped to Meridian's `Place` at the gateway boundary.
5. UI-facing source labels/credits are derived from Meridian provenance and Solstice localization; no UI code looks up provider objects in a manager.

## 5. User intents / application actions

The shell should emit intents, not execute repository/provider operations.

Minimum action surface:

```kotlin
interface ShellActions {
    fun onPageSelected(page: ShellPage)
    fun onOpenSearch()
    fun onSearchQueryChanged(query: String)
    fun onSearchResultSelected(resultId: PlaceSearchResultId)
    fun onCloseSearch()
    fun onSelectMainPlace(placeId: PlaceId)
    fun onOpenPlace(placeId: PlaceId)
    fun onAddPlace(previewId: PlacePreviewId)
    fun onDismissPlacePreview()
    fun onRemovePlace(placeId: PlaceId)
    fun onUndoRemovePlace()
    fun onMovePlace(placeId: PlaceId, beforePlaceId: PlaceId?)
    fun onAddCurrentLocation()
    fun onRefresh(placeId: PlaceId)
    fun onOpenDetails(placeId: PlaceId)
    fun onOpenSettings()
    fun onOpenLocationSettings()
    fun onDismissMessage(messageId: String)
}
```

Implementation may consolidate related actions, but the contract must stay product-oriented. Do not pass `Context`, a Breezy `Location`, a source manager, repository, or provider object through these callbacks.

The shell's local-only state (pager scroll, morph progress, sheet detent, measured bounds, temporary animation handoff) can remain in Compose. Durable product state (saved places, Home configuration, refresh status, search results) comes from the state holder. Search preview/“Visiting” state is ephemeral.

## 6. Meridian boundary

The Solstice application layer owns a narrow adapter, for example `MeridianWeatherGateway`.

Responsibilities:

- Convert a Solstice `Place`/coordinate/time-zone identity to Meridian's public `Place`.
- Request only required Meridian `Feature` values for a given screen/use case.
- Collect partial `MeridianResult` updates and map them into Solstice presentation models.
- Preserve provenance, freshness, spread, source failures and fallback information.
- Expose source status and credits through Solstice-owned models.
- Translate Meridian problems into stable app-level refresh states without leaking engine exception types.

Meridian responsibilities (per `docs/MERIDIAN_PLAN.md`): weather providers, normalized SI values/UTC time series, source selection, model blending/spread, alerts, provenance, source status, and weather cache/statistics.

Solstice responsibilities: saved places, Home configuration, app preferences, screen composition, place search UX/history, app navigation, permission UX, notifications/widgets policy, attribution presentation, and the UI mapping layer.

**Do not call Meridian from composables.** No Ktor clients, provider IDs, API keys, source adapters, or engine storage objects enter the UI.

### Place search clarification

The current Meridian public API sketch lists weather refresh/results/source/provenance operations; it does not yet define a place-geocoding/search API. Therefore, do not assume `SearchPlaces` is already supplied by Meridian. Keep place discovery behind a Solstice `PlaceSearchGateway` interface and select its implementation separately (Android geocoder, a reviewed geocoding provider, or a future Meridian capability) after product/privacy/provider requirements are specified. Search result types must be Solstice-owned.

## 7. Source selection and source details

The current `ChooseSourcesDialog` passes Breezy `Location`, `SourceManager`, and `SecondarySourcesPreference` into the UI. This entire flow must be replaced.

Target behavior:

- Solstice presents source status, freshness, provenance, and credits from Meridian's public `sources(place)` and `credits(result)` contracts.
- Any user control over enabled sources or feature sets must use a Meridian-supported configuration contract once that API is specified.
- Do not invent per-place provider overrides by mutating Meridian internals or storing Breezy provider identifiers in Solstice.
- If Meridian does not yet expose a required configuration capability, record it as an API requirement for Meridian; do not recreate a second weather-source engine inside Solstice.

## 8. Persistence ownership

| Data | Owner | Notes |
|---|---|---|
| Saved places and stable app place IDs | Solstice | App database/repository; migrate existing user places safely |
| Home configuration | Solstice | Follow-device or fixed PlaceId; not a favorite/star flag |
| Search preview and recent search history | Solstice | Preview is ephemeral; history is separate and clearable |
| Place order and display preferences | Solstice | DataStore or app database according to consistency needs |
| Weather responses, source cache, forecast history, model statistics | Meridian | Through Meridian's storage abstraction |
| Current device location permission and acquisition | Android platform adapter | Consent and lifecycle policy remain Solstice |
| In-memory pager, animation, sheet, morph state | Compose UI | Save only the state that product behavior requires across recreation |

Do not let the shell call SharedPreferences directly. Migrate legacy starred/visit keys as compatibility inputs only; the target repositories expose saved places, Home configuration and recent search history, not stars or persisted visits.

## 9. Failure and lifecycle behavior

The new contract must explicitly cover:

- No saved place yet.
- Current location denied, disabled, unavailable, or stale.
- Place search offline, empty, ambiguous, or failed.
- Weather result absent, partial, stale, refreshing, or failed.
- One provider/source failing while other sources still return data.
- Meridian cache available but network unavailable.
- User removes a place while it is refreshing.
- Main place is deleted or loses current-location status.
- Process death during a postcard window, search handoff, or delete/undo.
- Permission changes while the app is backgrounded.
- Duplicate place identities and same city names in different regions.

A stale/partial result should remain displayable when valid, with truthful freshness/provenance. No-data states must be distinct from clear weather.

## 10. Incremental extraction plan

Do this in small, independently testable changes. Do not replace the entire shell in one PR.

1. **Freeze behavior:** add tests/spec scenarios for Home configuration, automatic travel swap, search preview/Cancel/Add, postcard handoff, delete/undo, reorder, refresh and no-place states.
2. **Introduce Solstice models:** add `PlaceId`, `PlaceSummary`, `PlaceCardModel`, `ShellUiState`, `SearchUiState`, and refresh/freshness presentation types in a Solstice-owned package. Add mapping tests.
3. **Extract repositories:** move saved-place, Home-configuration and recent-search persistence out of Compose behind app-owned interfaces. Read legacy starred/visit values only in the migration adapter; preserve ambiguous user data until its retention policy is approved.
4. **Create application use cases:** move add/delete/reorder/select/pin/visit/refresh/search orchestration out of `SolsticeShell`.
5. **Add Meridian gateway:** implement only against Meridian's approved public API; map result/provenance into Solstice models. Use fixtures/fakes until the engine API is ready.
6. **Replace the source chooser:** use Meridian source status/configuration capabilities; if unsupported, write a Meridian API proposal rather than retaining Breezy source management.
7. **Make shell state-driven:** `SolsticeShell(state, actions, controller)`; remove direct ViewModel/source manager arguments.
8. **Remove old integration:** only after all call sites and tests are migrated, remove Breezy imports and obsolete adapters from this path.
9. **Verify:** UI tests, use-case tests, migration tests, Meridian adapter tests, offline/partial-data tests, accessibility checks, and a clean app build.

Each step should have its own PR and should leave the app buildable.

## 11. Acceptance criteria

The shell boundary is considered independent only when:

- `SolsticeShell.kt` has no imports from `breezyweather.*`, `org.breezyweather.*`, Breezy ViewModels, `SourceManager`, or provider implementation packages.
- No composable receives `Context` for app data mutation or directly reads/writes SharedPreferences.
- No composable receives a Meridian engine object or engine domain model.
- All weather/place inputs are Solstice-owned presentation models.
- All user actions are expressed through Solstice action/use-case contracts.
- Place, visit, pin, ordering, and preference state is stored in Solstice-owned repositories.
- Meridian source status, provenance, freshness and failure states remain visible to the app.
- Existing user data and agreed UX behavior survive migration.
- Tests cover the failure/lifecycle scenarios in section 9.

## 12. Open decisions to resolve during implementation planning

These are not blockers for the boundary decision, but must be specified before their implementation:

- Exact stable place identity and duplicate/merge rules.
- Travel detection threshold/hysteresis and preview retention policy; see `PLACE_MODEL_CONTRACTS.md`.
- Place-search provider and privacy/attribution requirements.
- Whether Meridian will expose source configuration, or only status/selection policy.
- App database vs DataStore split for places, visit history and display preferences.
- Whether refresh status is streamed per place, per feature, or per request.
- Which shell state should survive process death versus being reconstructed from repositories.
- How the existing Breezy location database and preference keys will be migrated.

## 13. Audit references

- Solstice `SolsticeShell.kt` at audit branch revision `d7fb5719` (714 lines).
- Current calls include `MainActivityViewModel` operations: current/valid locations, loading, init, delete/add, set current, refresh, reorder, update, and source-dialog state.
- Current calls include `SearchViewModel` selected location, source selection and dialog state.
- Current calls include `SourceManager` weather-source and location-search-source lookups.
- Solstice `docs/MERIDIAN_PLAN.md`, especially sections 4, 7 and 8, define the current planned public engine boundary and app-side mapping.
- Upstream Breezy base: `5ec3b9bcb`; its `MainActivityViewModel`, `SearchViewModel`, and `Location` remain reference-only audit material. Do not use them as implementation templates.
