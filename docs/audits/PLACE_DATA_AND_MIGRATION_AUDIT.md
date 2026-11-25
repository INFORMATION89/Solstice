# Place data and migration audit

Status: **source-level storage audit and target specification; no implementation or data migration**
Date: 2026-11-25
Scope: saved places, main/starred place, temporary visits, recent searches, shell preferences, and the existing location database.

## 1. Decision

Solstice must own all user-facing place identity and place lifecycle state. Meridian owns weather data/cache/statistics only; it must not become the owner of a user's saved places, favorites, visit history, or search history.

Keep the current database and preference stores intact until a Solstice-owned replacement has been implemented, migrated, and verified on upgrade installs. Do not reset the database or collapse all data into Meridian storage.

Recommended target:
- A Solstice-owned place repository with a stable, app-generated `PlaceId` independent of coordinates, display name, and weather source.
- A Solstice-owned database for saved place records and ordering.
- A transactional app-state store for the single main place and temporary visit lifecycle.
- A small preference store for display settings; use the database only where atomic consistency with place state is required.
- Meridian receives a weather query location through an adapter; it never receives Solstice persistence IDs as its own identity.

## 2. Current sources of truth (confirmed in code)

| User data | Current source | Confirmed behavior |
|---|---|---|
| Saved places | SQLDelight `:data` database, `locations` table | Rows contain location/address fields, source IDs, current-position flag and list order. |
| Per-place provider parameters | SQLDelight `location_parameters` table | Child rows keyed by `location_formatted_id`; foreign key cascades on location deletion. |
| Place order | `locations.list_order` | Repository rewrites order from list indices in `addAll`. |
| Main/starred place | SharedPreferences file `solstice_places`, key `starred` | Stored as a StringSet. Shell reads `take(1)`; when no valid starred ID exists it chooses current-position place or first place. |
| Temporary visits | Same preferences file, key `visiting` | StringSet of place IDs; stale visits are cleared at startup and unstarred rows deleted. |
| Recent searches | Same preferences file, key `recent_searches` | Newline-separated tab-encoded `City` records; newest first, maximum 8. |
| Solstice feels-like display preference | Same preferences file, key `feels_like_main` | Boolean, default false, cached in a Compose mutable state. |
| Current device location cache | Preferences file `current_location` | Last-known latitude/longitude and last successful refresh timestamp. |
| General application settings | `ConfigStore` backed by SharedPreferences named `<packageName>_preferences` | `SettingsManager` stores version marker, units, refresh, appearance, provider configuration and many other settings. |
| Source-specific settings | `source_<sourceId>_preferences` | Separate SharedPreferences namespace per legacy source. |

Important file paths:
- `data/src/main/sqldelight/breezyweather/data/locations.sq`
- `data/src/main/sqldelight/breezyweather/data/location_parameters.sq`
- `data/src/main/kotlin/breezyweather/data/location/LocationRepository.kt`
- `data/src/main/kotlin/breezyweather/data/location/LocationMapper.kt`
- `app/src/main/kotlin/org/breezyweather/ui/expressive/shell/SolsticeShell.kt`
- `app/src/main/kotlin/org/breezyweather/ui/expressive/shell/Visits.kt`
- `app/src/main/kotlin/org/breezyweather/ui/expressive/shell/ShellPrefs.kt`
- `app/src/main/kotlin/org/breezyweather/ui/expressive/search/Cities.kt`
- `app/src/main/kotlin/org/breezyweather/domain/settings/CurrentLocationStore.kt`
- `app/src/main/kotlin/org/breezyweather/domain/settings/SettingsManager.kt`
- `app/src/main/kotlin/org/breezyweather/Migrations.kt`

## 3. Existing schema and identity

### 3.1 Saved-place database

The current `locations` table stores:
- `formatted_id` (primary key), `list_order`;
- city/geographic identity and display data (coordinates, timezone, country, administrative levels, city, district, custom name);
- legacy source IDs for forecast/current/air quality/pollen/minutely/alerts/normals/reverse geocoding;
- current-position and geocode-refresh flags.

`location_parameters` stores source-specific key/value parameters and cascades when a location row is deleted.

The database uses SQLDelight. The repository currently contains numbered migrations `1.sqm` through `25.sqm`. This history is part of the user's upgrade path and must be preserved or explicitly translated into a tested migration path. The current app build version is 6.2.3 / versionCode 60203; do not equate app versionCode with SQL schema version.

### 3.2 Current place ID is not a durable product identity

The legacy `Location.formattedId` is:
- the constant `CURRENT_POSITION` for current-position locations; otherwise
- latitude + longitude formatted to six decimal places, joined with the forecast source ID.

Consequences:
- Changing the forecast source changes the ID even when the physical place is unchanged.
- Coordinates rounded to six decimal places become part of identity.
- The current-position sentinel represents a role, not a stable physical place.
- Starred and visit preference sets store this legacy ID, so they must be migrated alongside database rows.

Do not use `formattedId` as the new Solstice `PlaceId`.

## 4. Legacy behavior vs current product decisions

The legacy implementation has one starred/main place, temporary persisted visits, pin/unpin behavior, and a postcard opened for a picked place. Those are **not** the target product rules.

The authoritative decisions are in `docs/PLAN.md` → “Places and travel”:
- No star or favorites; Places is a plain reorderable saved list.
- Home follows device location by default; onboarding can choose a fixed place when location is unavailable.
- Travel swaps Forecast automatically between current location and Home; Home is marked in Places.
- Search opens an ephemeral preview with Cancel and Add. “Visiting” marks only an unsaved preview.

Do not preserve star/pin/temporary-visit semantics as product requirements. Preserve legacy data safely, then map it to the new model according to `PLACE_MODEL_CONTRACTS.md`. Old `starred` values are migration hints, not a new favorite feature. Old `visiting` rows need a retention decision rather than automatic conversion into persistent visits.

Saved-place ordering, safe removal/undo, recent search history being separate from saved places, and separation of device-location permission/cache from saved-place identity remain valid requirements.

## 5. Risks and defects to address before migration

### High priority

- **Source-dependent IDs:** source changes can alter `formattedId`, which affects current selection, starred IDs, visit IDs, and references in weather/parameter tables. Stable identity must be decoupled from source configuration.
- **Unordered starred set:** SharedPreferences StringSet has no guaranteed ordering, but the shell reads `take(1)`. If corrupt/old state contains more than one entry, the selected main place is not deterministic. Migration must resolve this explicitly.
- **Visit cleanup is not atomic:** `Visits.takeStale` clears the visit set before the shell asynchronously deletes unstarred place rows. A process death between those operations can leave formerly temporary rows behind without their visit marker. Replace this with a durable cleanup state/transaction.
- **Async full-list writes:** `MainActivityViewModel.writeLocationList` launches a background write, while deletion is also asynchronous. A replacement must serialize mutations and prevent an older list snapshot from overwriting a newer user action.
- **Delete/undo:** the shell's undo path re-adds a place through the ViewModel. Verify that its place parameters, custom name, ordering, main-place state, and any associated data are restored as intended; do not assume a deleted row can be reconstructed from a bare coordinate.

### Additional risks

- Recent search records are hand-encoded with tabs/newlines and parsed permissively. Names containing delimiters or malformed numeric fields can be lost or misparsed. A new format must be versioned and safely encoded.
- Recent searches deduplicate by exact city name + country code, not by a stable geocoding result identity or coordinates. Same-named settlements and changed display names need defined behavior.
- `City.toLocation()` creates a new weather-domain `Location` from the bundled city dataset; it is a search result conversion, not a durable identity model.
- The `current_location` store keeps last-known coordinates separately from the saved current-position place. Keep the distinction between permission/device acquisition and user-owned saved places.
- `ShellPrefs` uses a process-local mutable-state cache for one preference. Replace it with a repository/observable state source rather than letting composables own persistence.
- Existing database code contains inherited Breezy models, source identifiers, and storage implementation. The data is valuable and must be migrated, but the implementation must be rebuilt from this specification and public platform/database documentation, not copied.

## 6. Recommended target data model

These are design recommendations, not existing APIs.

### Place record (Solstice-owned)

Minimum conceptual fields:
- `placeId: PlaceId` — generated once by Solstice (UUID or equivalent opaque ID), persisted permanently.
- `latitude`, `longitude` — validated coordinates.
- `timeZoneId` — IANA time-zone identifier.
- `countryCode`, localized/display names and administrative labels.
- `customName` — optional user override, distinct from provider/geocoder name.
- `isCurrentLocation` — whether this row represents the device-current-place role; device permission state is not stored here.
- `createdAt`, `updatedAt` — for deterministic migration/debugging, not user-visible identity.
- `sortOrder` — stable ordered-list position, maintained transactionally.
- Any future geocoder/provider reference should be optional metadata, never the primary key.

Do not persist Meridian `MeridianResult`, provider clients, or weather-source configuration inside the Place entity. Meridian's public `Place` input is constructed at the gateway boundary from coordinates and time zone.

### Main place and visit state

- Store `mainPlaceId` as one nullable scalar, not a set.
- Enforce: if there are saved places, mainPlaceId references exactly one existing place; if there are none, it is null.
- Store visit state as explicit rows/records with `placeId`, `createdAt`, and a lifecycle status (temporary/kept) or equivalent transactionally consistent representation.
- Pinning should atomically set the main place and convert the visit to a kept/saved place.
- Removing the main place must atomically choose a deterministic replacement (current-location place if valid, otherwise first remaining place), or null if empty.
- Undo must restore the removed place and the relevant main/visit/order metadata as one app-level operation.

### Search history and preferences

- Recent searches remain a bounded, separate history (initial limit 8 can be preserved), with a versioned structured encoding and stable result identity where available.
- Search history must be clearable independently from saved places.
- Display preferences remain in Solstice. Use DataStore for simple independent preferences; use the app database for state that must change atomically with place records.
- Keep the current device-location cache behind an Android platform adapter. Never confuse it with the user's selected main place.

## 7. Storage technology boundary

The existing SQLDelight database is not a reason to make Meridian use SQLDelight. Conversely, Meridian's planned Room KMP storage for weather cache is not a reason to put Solstice user-owned place records into Meridian.

For the Android app reconstruction:
- Choose one Solstice-owned persistence implementation after a short evaluation (Room is a reasonable default for Android-only app data; DataStore for simple preferences).
- Keep repository interfaces independent of the database implementation.
- Do not introduce two databases for the same source of truth.
- Keep Meridian weather storage separate behind Meridian's own storage abstraction.
- If Solstice later targets desktop/iOS, revisit the app persistence implementation based on actual product targets; do not prematurely force Android app UI persistence into Meridian.

## 8. Safe migration plan

Migration must be additive, restart-safe, observable, and idempotent.

1. **Inventory and backup:** before writing, record the exact legacy database schema/version, all relevant preference files/keys, and representative fixture snapshots from real upgrade installs (including empty, one-place, multi-place, current-position, custom-name, visit, and malformed-preference cases).
2. **Introduce new Solstice models/repositories:** add stable IDs and new storage alongside legacy storage. Do not remove or rename legacy tables/keys yet.
3. **Deterministic ID mapping:** assign one new PlaceId per legacy saved place. Preserve a mapping from legacy `formattedId` to new PlaceId for all migrated references. Never derive PlaceId from display name or source ID.
4. **Migrate saved places and order:** copy place metadata and user custom names; preserve list order and current-position semantics. Preserve source parameters only as legacy migration data if still needed for an app feature; do not carry provider-specific configuration into the new Place domain model.
5. **Migrate main place:** resolve the legacy starred set deterministically: keep its single valid ID if exactly one exists; otherwise choose the valid current-position place, else first saved place. Record a diagnostic for malformed/multiple/missing values.
6. **Migrate visits:** map visit IDs through the same legacy-to-new ID map. Expire old unpinned visits only after the new place records and main-place invariant are committed. Make cleanup restart-safe; never clear the only visit marker before the deletion transaction succeeds.
7. **Migrate recent searches:** parse old records defensively, drop only malformed entries, preserve newest-first order and limit, then write a versioned new representation. Keep the old key until verification succeeds.
8. **Migrate preferences:** preserve `feels_like_main` and all other Solstice-specific preferences with their exact defaults. Do not indiscriminately migrate legacy Breezy settings into the new product without classifying each setting.
9. **Verify:** compare old/new record counts and semantic fields; test duplicate IDs, source changes, app process death at each migration step, failed writes, rollback/retry, and upgrade from every supported legacy database version.
10. **Cut over:** read from new repositories only after validation. Keep legacy data for at least one release/rollback window. Remove old tables/preferences only in a later explicitly approved cleanup migration.

A migration marker must be written only after all required data has been committed and verified. Re-running a partially completed migration must not duplicate places, reorder them, or lose visits.

## 9. Required tests and release gates

- Unit tests: PlaceId generation/persistence, main-place invariant, deterministic fallback, deduplication, ordering, custom names, visit-to-pin conversion, recent-search encoding.
- Repository tests: add/update/delete/reorder, atomic main-place replacement, undo, concurrent mutations, transaction rollback.
- Migration tests: each supported old schema version, empty DB, one/many places, current-position place, multiple/corrupt starred IDs, stale visits, custom names, provider parameters, malformed recent search rows, interrupted migration and retry.
- Instrumented upgrade test: install an existing build with real fixture data, upgrade without uninstalling, and verify user-visible place list/main place/history.
- Manual device test: deny/revoke location permission, disable location services, go offline, change weather source in legacy app before migration, and force-stop during migration.
- No release may delete legacy data before migration metrics/logging show success and rollback strategy is available.

## 10. Open decisions (only if product behavior requires changing)

The audit recommends keeping the current product rules. The following implementation choices can be decided by the engineering plan without asking the user:
- UUID format/library and exact schema names.
- Room vs another app-owned database after dependency/licence/platform review.
- DataStore vs database for each independent preference.
- How long recent searches are retained (keep current max 8 initially).
- How diagnostics are surfaced in debug builds without exposing private location data.

## 11. Source references

Inspected at branch `docs/solstice-reconstruction-audit`:
- `locations.sq`, `location_parameters.sq`, SQLDelight migrations 1–25.
- `LocationRepository.kt`, `LocationMapper.kt`, and domain `Location.kt`.
- `SolsticeShell.kt`, `Visits.kt`, `ShellPrefs.kt`, `Cities.kt`.
- `MainActivityViewModel.kt`, `SettingsManager.kt`, `ConfigStore.kt`, `CurrentLocationStore.kt`, and `Migrations.kt`.
- `app/build.gradle.kts` (versionCode 60203 / versionName 6.2.3).
- `docs/MERIDIAN_PLAN.md`: Meridian stores weather responses, observations, statistics and cache; it does not define ownership of Solstice's saved places or search history.

This audit is structural and behavioral, not legal clearance. The legacy implementation and schema are retained as migration inputs only; new code must be written from this specification and approved public documentation.
