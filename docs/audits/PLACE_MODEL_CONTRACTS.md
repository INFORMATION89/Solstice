# Solstice place model and repository contracts

Status: **target contract specification; no implementation**
Authority: `docs/PLAN.md` decisions under "Places and travel" (2026-11-22)
Related audits: `PLACE_DATA_AND_MIGRATION_AUDIT.md`, `SOLSTICE_SHELL_BOUNDARY.md`

## 1. Product rules (authoritative)

The existing app behavior is not the target product behavior. Follow the current product decisions:

1. **No star and no favorites.** Places is a plain, reorderable saved list.
2. **Home follows the device location by default.** If location permission is unavailable during onboarding, the user chooses a place and that becomes Home.
3. **Fixed Home is a user setting** under Settings → Locations.
4. **Travel changes Forecast automatically.** When away from Home, Forecast shows the current location; the Home place is moved to the top of Places and marked "Home", with its local time. When the user returns, the display swaps back.
5. **Search opens a preview** with Cancel and Add. A preview can be marked "Visiting", but this does not save it. Add makes it a saved place.
6. Saved places can be reordered. Home is a role/configuration, not a favorite flag or a permanent list pin.

These rules supersede the legacy star/pin/temporary-visit behavior documented in source audits. The old behavior is relevant only for understanding and migrating existing data.

## 2. Domain model

All types below are Solstice-owned conceptual contracts. Names can be refined during implementation, but ownership and invariants must remain.

### 2.1 Identity and coordinates

`PlaceId` is an opaque, stable identifier generated once by Solstice and persisted (UUID or equivalent). It must not be derived from:
- latitude/longitude;
- city/display name;
- timezone;
- geocoder/provider identifiers;
- weather source IDs;
- Meridian internal IDs.

Coordinates and identity are different concepts. A saved place keeps its ID if its display name, coordinates are corrected slightly, geocoder metadata changes, or Meridian changes its internal query representation. A materially different place selected by the user receives a different ID.

Conceptual types:

```kotlin
@JvmInline
value class PlaceId(val value: String)

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class PlaceDescriptor(
    val point: GeoPoint,
    val timeZoneId: String,
    val countryCode: String?,
    val name: String,
    val administrativeArea: String?,
    val locality: String?,
    val district: String?,
    val geocoderReference: String?,
)

data class SavedPlace(
    val id: PlaceId,
    val descriptor: PlaceDescriptor,
    val customName: String?,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
```

Validation:
- Latitude must be finite and within -90..90; longitude finite and within -180..180.
- Time zone must be a valid IANA zone ID or represented as unresolved until corrected; do not silently substitute the device zone.
- `customName` is a user override; it never overwrites the provider/geocoder name.
- Geocoder references are optional metadata, not identity.
- No weather/provider configuration belongs in `SavedPlace`.

### 2.2 Home configuration and travel

Home is not a boolean star on a place. Model it separately:

```kotlin
sealed interface HomeConfiguration {
    data object FollowDeviceLocation : HomeConfiguration
    data class FixedPlace(val placeId: PlaceId) : HomeConfiguration
}

data class ActiveForecastPlace(
    val descriptor: PlaceDescriptor,
    val savedPlaceId: PlaceId?,
    val role: ForecastPlaceRole,
)

enum class ForecastPlaceRole {
    HOME,
    CURRENT_TRAVEL_LOCATION,
    PREVIEW,
}
```

Rules:
- Default configuration is `FollowDeviceLocation`.
- If onboarding cannot obtain usable location, user selection creates a saved place and configures `FixedPlace(id)`.
- Fixed Home must reference an existing saved place. Removing that place requires a deterministic user-facing fallback/confirmation; never leave a dangling ID.
- In follow-device mode, current location is an observation from the platform location adapter, not automatically a saved place.
- The active Forecast target is derived from HomeConfiguration + current device location + the approved travel-detection policy. It is not persisted as a second competing "main place".
- When away, Home remains a saved place and is visually marked in Places; current travel location is displayed as the active Forecast target. Returning restores Home.
- The exact distance/time threshold and hysteresis for "away" vs "returned" are not specified in PLAN.md. Do not invent them; write a short behavior proposal and obtain approval before implementing automatic swap logic.

### 2.3 Search preview

```kotlin
@JvmInline
value class PlacePreviewId(val value: String)

data class PlacePreview(
    val id: PlacePreviewId,
    val descriptor: PlaceDescriptor,
    val isVisiting: Boolean,
)
```

- Preview state is ephemeral and may be held in memory/saved UI state only if needed for recreation.
- Cancel discards the preview; it does not mutate saved places.
- "Visiting" is a presentation marker for an unsaved preview only. It does not create a persisted visit record.
- Add converts the selected descriptor into a new SavedPlace with a new PlaceId.
- Adding a preview that represents an existing saved place should select/open the existing saved place, not create a duplicate. Duplicate matching rules need a separate explicit policy; do not compare display names alone.

## 3. Repository and use-case contracts

Repositories expose domain operations, not database entities or Breezy types.

```kotlin
interface PlaceRepository {
    fun observeSavedPlaces(): Flow<List<SavedPlace>>
    suspend fun getPlace(id: PlaceId): SavedPlace?
    suspend fun addPlace(descriptor: PlaceDescriptor): PlaceId
    suspend fun updateCustomName(id: PlaceId, name: String?)
    suspend fun removePlace(id: PlaceId): RemovedPlaceSnapshot
    suspend fun restorePlace(snapshot: RemovedPlaceSnapshot)
    suspend fun reorderPlaces(orderedIds: List<PlaceId>)
}

data class RemovedPlaceSnapshot(
    val place: SavedPlace,
    val previousHomeConfiguration: HomeConfiguration,
    val previousIndex: Int,
)

interface HomeConfigurationRepository {
    fun observeConfiguration(): Flow<HomeConfiguration>
    suspend fun setFollowDeviceLocation()
    suspend fun setFixedHome(placeId: PlaceId)
}

interface PlaceSearchGateway {
    fun search(query: String): Flow<List<PlaceSearchResult>>
}

interface RecentSearchRepository {
    fun observeRecentSearches(): Flow<List<PlaceSearchResult>>
    suspend fun record(result: PlaceSearchResult)
    suspend fun clear()
}
```

Contract notes:
- `reorderPlaces` accepts the complete desired order and validates that IDs are unique and refer to current saved places; it commits atomically.
- `removePlace` + `restorePlace` preserve order and Home configuration, but only the application layer decides whether confirmation is required and what fallback to offer.
- The repository must serialize concurrent mutations or use database transactions/version checks so stale list snapshots cannot overwrite newer changes.
- `PlaceSearchGateway` is an app-side abstraction. The current Meridian plan does not define geocoding/search; do not assume Meridian provides it.
- Recent searches are search history, not saved places. Bound it to the existing maximum of 8 initially; keep a versioned structured format.
- Do not retain a legacy `visiting` persistence repository. Preview/visiting state is ephemeral under the current product decision.

## 4. Meridian boundary

The Solstice adapter maps `PlaceDescriptor.point` and `timeZoneId` into Meridian's public weather query place. `PlaceId` remains Solstice-local and is used only to correlate app UI/state with results.

Never:
- store Meridian result objects inside `SavedPlace`;
- pass Solstice persistence IDs as Meridian's place identity unless a documented public API explicitly defines such a field;
- store legacy Breezy source IDs or provider parameters in the new place domain model;
- let composables or repositories expose Meridian internals to the UI.

Meridian owns weather observations, forecasts, source provenance, weather cache and model statistics. Solstice owns saved place records, Home configuration, search UX/history, and the mapping from weather results to presentation state.

## 5. Persistence recommendations

For the Android implementation:
- Place records and order: one Solstice-owned database, with stable primary key `place_id`.
- Home configuration: same database if fixed-place referential integrity and atomic deletion are required; otherwise a typed preference with explicit validation. Prefer same database to enforce the invariant.
- Recent searches: structured app-owned persistence, separate from saved places; DataStore is sufficient if bounded history does not require relational queries.
- Search preview/"Visiting": in-memory UI/application state; not a durable saved-place row.
- Current device coordinates/permission/freshness: Android platform adapter, separate from saved-place identity.
- Weather data: Meridian storage only.

The current Meridian plan already chose Room for Meridian's own database. That does not make Meridian the owner of Solstice places. A short implementation decision must select the Solstice app database and its KMP implications before coding.

## 6. Legacy migration mapping

| Legacy data | Target treatment |
|---|---|
| `locations` rows | Convert to Solstice `SavedPlace` records with newly generated stable PlaceIds; preserve user-visible names, coordinates, timezone and order. |
| `formatted_id` | Keep only in a temporary migration mapping table to associate legacy references; never reuse as PlaceId. |
| `starred` StringSet | Do not recreate stars. Use valid legacy selection only as a migration hint for initial Home configuration if product behavior requires it; default to FollowDeviceLocation when valid device location is available, otherwise preserve a valid selected place as FixedPlace only where consistent with onboarding/product rules. Resolve malformed/multiple values deterministically and record diagnostics. |
| `visiting` StringSet | Do not migrate as durable visits. Determine whether each row is a user-added saved place or an old temporary preview artifact; retain only records that meet the approved saved-place retention policy. Do not silently delete ambiguous user data. |
| Legacy current-position row | Use only as a migration hint for current device location; do not make a sentinel string the new PlaceId. |
| `recent_searches` | Parse defensively into versioned search history, preserving newest-first order and max 8; malformed entries are dropped with diagnostics. |
| `feels_like_main` | Preserve as a Solstice display preference if the feature remains in the new product; otherwise retain until preference migration policy is approved. |
| Legacy provider IDs/parameters | Do not move into SavedPlace. Preserve only in a temporary migration/compatibility layer if a still-shipped feature demonstrably requires them; map weather configuration through Meridian's public API. |

Migration must be additive, idempotent, restart-safe, and validated on upgrade installs. No legacy key/table is deleted until the new state is verified and the rollback window has passed.

## 7. Required tests

- PlaceId remains stable across name/metadata correction and weather-source changes.
- Coordinate and time-zone validation; unresolved zone behavior.
- Home FollowDeviceLocation and FixedPlace invariants.
- Fixed Home deletion, fallback, and restoration.
- Travel-state derivation with boundary/hysteresis fixtures once approved.
- Search preview Cancel leaves saved places untouched.
- Visiting marker does not persist a place.
- Add creates one stable SavedPlace; adding an existing place does not duplicate it.
- Reorder is atomic and rejects duplicate/unknown IDs.
- Concurrent add/remove/reorder operations do not lose updates.
- Recent search encoding, truncation, malformed legacy records and clear.
- Migration fixtures for zero/one/many places, starred corruption, legacy visits, current-position row, interrupted migration and retry.

## 8. Not yet decided

Do not silently decide these in implementation:
1. Travel detection distance/time threshold and hysteresis.
2. Duplicate-place matching radius and same-city/different-coordinate behavior.
3. What to do when fixed Home is deleted (confirm new Home, choose another, or revert to FollowDeviceLocation).
4. Whether a legacy unstarred saved row that was also in `visiting` was intentionally saved or only a temporary preview; define retention policy.
5. Exact app database choice and KMP support target.

These are the only product-level decisions likely to change visible behavior. The rest are implementation details that can follow these contracts.
