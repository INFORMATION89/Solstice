# Solstice privacy and data-flow map

**Status:** Internal technical audit; partial code-backed findings  
**Reviewed:** 2026-11-27  
**Internal evidence map; not a public notice or legal opinion.** This document records implementation paths inspected so far. The public Solstice notice is in the repository root `PRIVACY.md`; this internal map does not establish that provider behavior, dependency instrumentation, retention, build variants, or legal obligations have been fully reviewed.

## 1. Relationship to the public notice

The root `PRIVACY.md` is now a Solstice-owned notice based on the findings below. This map remains the technical evidence record and is intentionally more cautious and detailed. It is not a legal opinion or proof that every dependency, build variant, device service, and retention path has been audited.

Do not broaden public claims beyond the code paths and provider behavior actually verified. In particular, do not claim that no personal data is processed or that all data stays on-device.

## 2. Location acquisition and geocoding

### Android native location

- Code: `app/src/main/kotlin/org/breezyweather/sources/android/AndroidLocationService.kt`.
- Uses Android `LocationManager`, preferring Fused Location Provider where available, then network/GPS/passive providers as appropriate.
- Requires coarse or fine Android location permission. Uses a balanced-accuracy location request and can fall back to the last known location.
- The app receives latitude/longitude from Android. The app code does not itself send a network request in this service; Android's chosen location provider or device services may have their own data handling, which varies by device/vendor and is not controlled by Solstice.
- The manifest declares coarse, fine, and `ACCESS_BACKGROUND_LOCATION`. `AndroidLocationService.permissions` exposes only coarse/fine for the location source. The separate background permission is user-controlled in **Settings → Location**: the screen shows a distinct background-access row after foreground location permission is granted and requests `ACCESS_BACKGROUND_LOCATION` only when the person selects it. Its rationale says this allows scheduled refresh to use the most recent location known by Android; without it, background refresh uses the most recent coordinates obtained in the foreground, which may be outdated.
- `WeatherUpdateJob` calls `RefreshHelper.updateCurrentCoordinates(context, true)` when its update set includes the current-position location. `RefreshHelper` refuses to acquire a new background location unless `ACCESS_BACKGROUND_LOCATION` is granted. The inspected `AndroidLocationService` requests balanced-accuracy updates and removes the listener after the first result; if no new fix arrives in its request window, it falls back to the last known location. This is a scheduled refresh path, not evidence of continuous location tracking.

### IP.SB IP-derived location

- Code: `app/src/src_nonfreenet/org/breezyweather/sources/ipsb/IpSbLocationService.kt` and `IpSbLocationApi.kt`.
- Endpoint: `https://api.ip.sb/geoip`; provider privacy link in code: [IP.SB Privacy Policy](https://ip.sb/privacy-policy/).
- If a person selects this location source, the HTTP request exposes the network's public IP address to IP.SB. The service returns an approximate IP-derived location, which Solstice converts to latitude/longitude.
- This is an alternative location source, not evidence that Solstice uploads GPS coordinates to IP.SB. Keep the distinction explicit in the public notice.
- Verify whether IP.SB appears in every distributed flavor and how it is presented in the location-source chooser before publishing final wording.

### Location search

- The current `SourceManager` registers GeoNames as the location-search source.
- Code: `app/src/src_nonfreenet/org/breezyweather/sources/geonames/GeoNamesService.kt`.
- A location-search query, language/locale-derived parameters, and the configured GeoNames API key are sent to GeoNames; results contain place names and coordinates. Requests also expose the normal network metadata (such as the caller IP) to the service.
- GeoNames usage limits are handled in the client. Confirm the precise API-key ownership/default and current provider terms/privacy text before making a user-facing statement.

### Reverse geocoding

- Candidate services registered by `SourceManager`: Natural Earth, Android Geocoder, and Nominatim.
- Solstice's current default selection prefers Android's native Geocoder where available in the relevant flavor, and uses Nominatim otherwise.
- Nominatim code: `app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimService.kt`. Reverse lookup sends latitude, longitude, locale/language, and a user-agent to the configured Nominatim instance. Privacy link in code: [OpenStreetMap Foundation Privacy Policy](https://osmfoundation.org/wiki/Privacy_Policy/).
- Android Geocoder is an OS API; the actual provider/network path can depend on the device and Android implementation. Do not promise that reverse geocoding is always on-device.
- Natural Earth is also registered, but its actual use and local-data behavior need a call-site-level check before describing it as an offline alternative.

## 3. Weather, air-quality, pollen, and alert providers

The current `SourceManager` source lists include:

- Worldwide/configurable feature sources: Open-Meteo, FOSS Public Alert Server (FPAS), MET Norway, NCEI, OpenWeather, Pirate Weather, and WMO Severe Weather.
- Regional/national sources: Environment and Climate Change Canada (ECCC) and the US National Weather Service (NWS).
- AccuWeather is injected in the source manager but explicitly excluded from the active worldwide and national chooser lists by a Solstice comment. Do not describe it as an active available source without rechecking the actual shipped build and settings.
- Defaults in the inspected Solstice code use Open-Meteo for ordinary weather features where supported and WMO Severe Weather for official alerts. Source selection can vary by feature, region, user configuration, and flavor.

For external weather requests, providers receive the request fields needed for the selected feature. This generally includes place coordinates or provider-specific location identifiers and requested forecast variables/time windows. Each HTTP request also exposes normal network metadata such as the request IP. Exact request fields, provider-side retention, logs, and onward sharing have not yet been audited endpoint by endpoint. The public notice must link to the relevant provider policies and must not imply Solstice controls provider retention.

Do not infer that all providers run for every refresh. The app uses per-feature source selection and availability/coverage checks.

## 4. Optional app update check

- User preference: `SettingsManager.isAppUpdateCheckEnabled` defaults to `false`.
- Code: `SettingsManager.kt`, `AppUpdateChecker.kt`, `GetApplicationRelease.kt`, and `ReleaseService.kt`.
- When enabled in a supported build, the app requests the latest release from the configured GitHub repository through `https://api.github.com/`. The request exposes ordinary HTTP metadata (including the network IP and request headers) to GitHub.
- The installed version name is passed into the local comparison logic after the latest release is fetched. The reviewed request endpoint is the latest-release endpoint; the exact User-Agent/headers need a separate network-client configuration check before claiming precisely what version/device information is or is not transmitted.
- The background `WeatherUpdateJob` invokes the checker after a weather-update attempt when the feature is supported and the user has enabled the setting. `GetApplicationRelease` throttles successful checks to at most once every 24 hours unless `forceCheck` is true; the timestamp is stored locally after the GitHub request succeeds. Other forced/manual caller paths and per-flavor feature availability still need a complete call-site review.
- The request is a GET to `https://api.github.com/repos/{org}/{repository}/releases/latest`. The reviewed request code does not put the installed app version, device model, or location in the URL or request body; ordinary network metadata and standard HTTP headers still reach GitHub. The update check uses the shared OkHttp client.

## 5. Local app-to-app data sharing

These features are distinct from remote telemetry and are enabled only for configured receiving applications.

### Gadgetbridge weather broadcast

- Code: `app/src/main/kotlin/org/breezyweather/sources/gadgetbridge/GadgetbridgeService.kt`.
- Action: `nodomain.freeyourgadget.gadgetbridge.ACTION_GENERIC_WEATHER`.
- The receiver packages selected in **Settings → Modules → Data sharing** can receive a weather payload. The inspected payload includes saved place names, latitude/longitude, current weather, forecast data, and related available weather fields such as air quality.
- Only choose a receiving app that the person trusts. The receiver controls its own subsequent processing, synchronization, and retention.

### Location update notifier

- Code: `app/src/main/kotlin/org/breezyweather/sources/breezyupdatenotifier/BreezyUpdateNotifierService.kt`.
- Action: `${applicationId}.ACTION_UPDATE_NOTIFIER`.
- Configured receiver packages can receive updated and all saved location IDs. This notifier payload is not the full weather payload.

### Read-only weather ContentProvider

- Code: `app/src/main/kotlin/org/breezyweather/background/provider/WeatherContentProvider.kt`.
- Manifest declares the provider `android:exported="true"`, protected by `android:readPermission="${applicationId}.READ_PROVIDER"`. The corresponding custom permission is `protectionLevel="dangerous"`.
- The provider can return saved places including precise latitude/longitude, current-place indicator, timezone and place labels, and weather data (including current/hourly/daily/minutely weather and other available features).
- The inspected provider's `isAllowedPackage()` method currently returns `true`; the access boundary is therefore the manifest's Android read permission, not a second in-code package allowlist. Keep the permission intact and make its Android permission prompt accurately disclose the data exposed.
- The permission label and description strings still say "Breezy Weather". They need a separate Solstice-specific copy/security review before users are asked to grant the permission.
- This mechanism is local Android app-to-app access, not a Solstice-hosted cloud API.
- `PrivacyPolicyActivity.kt` dynamically lists privacy-policy URLs for HTTP weather sources returned by `SourceManager.getHttpSources()` and can show the configured app privacy URL. This provider-link list does not by itself validate every statement in the Solstice-owned root `PRIVACY.md`; public wording must remain limited to reviewed behavior.

## 6. Local storage, cache, backup, and deletion

### Saved locations and dependent weather data

- `CurrentLocationStore` stores the last known latitude, longitude, and successful-refresh timestamp in a private `SharedPreferences` store named `current_location`. `clearCurrentLocation()` resets those values. The store is separate from SQLDelight, so deleting a location row does not itself clear these preferences; all call sites and reset flows that should clear them still need a complete trace.
- The SQLDelight database is opened as `breezyweather.db` through `AndroidSqliteDriver`. Its `locations` table contains coordinates, place labels, and source selections.
- All seven location-dependent tables inspected declare foreign keys to `locations(formatted_id)` with `ON DELETE CASCADE`: `weathers`, `location_parameters`, `alerts`, `dailys`, `hourlys`, `minutelys`, and `normals`. `DbModule` explicitly enables SQLite foreign-key enforcement on open. Therefore a location-row deletion cascades to these seven tables when the database is opened through the configured driver.
- `LocationRepository.delete()` deletes one location row; `deleteAll()` deletes all location rows; and `addAll()` removes rows absent from the supplied location list inside a transaction. These database operations cover the seven dependent tables through the cascades. They do not prove that the separate `current_location` preferences or HTTP cache are cleared.
- A full app reset path and every caller of `CurrentLocationStore.clearCurrentLocation()` have not yet been exhaustively traced. Do not describe user deletion/reset as complete until those paths are verified.

### HTTP cache

- The singleton OkHttp client stores its HTTP cache at `cacheDir/http_cache`, with a 50 MiB maximum. OkHttp's HTTP cache follows HTTP cacheability and freshness directives from responses; the configured size is an upper bound, not a fixed retention period.
- Android's app-specific cache directory is private to the app, excluded from Auto Backup, removed on uninstall, and may be evicted by Android when storage is low. These system behaviors do not replace app-managed cleanup.
- The reviewed client setup does not attach the cache to a location-deletion lifecycle or define a location-specific eviction policy. Deleting a saved place from SQLDelight therefore does not, by itself, prove that related cached responses are purged. Only responses eligible for HTTP caching are affected; actual provider response headers and cache contents still need endpoint-level verification.
- No fixed application-level retention duration has been established for this cache. Do not describe 50 MiB as a time-based retention limit.

### Android backup and restore

- The app targets Android API 37. Its merged source manifest does not set `android:allowBackup`, `android:fullBackupContent`, or `android:dataExtractionRules`, and no backup-rules XML was found in the app resource tree.
- For apps targeting API 23 or higher, Android Auto Backup is enabled by default when `android:allowBackup` is omitted. Its default included data classes cover private SharedPreferences and databases; cache directories are excluded. On the inspected configuration, `current_location` preferences and `breezyweather.db` are therefore eligible for Auto Backup unless overridden by a manifest overlay, device policy, or platform behavior.
- This is a source/configuration finding, not a successful backup-and-restore test. Cloud backup and device-to-device transfer behavior can differ by Android version and device manufacturer. Verify the merged manifests for every shipped variant and test backup/restore before making stronger claims.
- Android references: [Auto Backup](https://developer.android.com/identity/data/autobackup) and [app-specific storage](https://developer.android.com/training/data-storage/app-specific).

## 7. Diagnostics and logs

The inspected `CrashLogUtils.dumpLogs()` path is user-initiated: it runs `logcat *:E -d -f` into a cache file, appends app version/flavor/commit, Android build, and device brand/manufacturer/name/model details, then exposes the file through a notification for the person to open/share. This is a local export path, not an automatic upload in the inspected implementation. Error-level logs may contain additional sensitive context if other code logs it; the person should be told to inspect the file before sharing.

The shared OkHttp logging interceptor is configured to log request/response bodies only when `BreezyWeather.instance.debugMode` is true, and to `NONE` otherwise. The exact runtime definition of that flag and the full set of shipped build variants still need verification. A version-catalog scan found no obvious Firebase Analytics, Crashlytics, Sentry, Amplitude, Mixpanel, Segment, or Adjust dependency declarations; this is not a complete transitive dependency or runtime SDK audit.

Before making an absolute statement, scan the complete dependency graph, build variants, manifests, network interceptors, logging wrappers, and CI/release tooling for crash-reporting, analytics, telemetry, attribution, and automatic upload SDKs.

## 8. Consent, telemetry, and data contribution

Solstice's project rules require analytics/tracking/ads/social features to remain absent unless explicitly approved. Any future weather-error contribution or remote data sharing must be off by default, described separately from core operation, opt-in, revocable, and disclose the exact payload, purpose, recipient, retention, and data path before activation.

The code inspected in this pass did not establish an active remote forecast-error telemetry pipeline. Do not imply that the future opt-in contribution design is already implemented. Equally, do not use that absence as proof of "no data leaves the device": weather/geocoding/provider requests and local app-to-app sharing are active functionality.

## 9. Remaining evidence to complete the privacy review

- [x] Trace the background-location permission's user-facing request and scheduled-refresh call path: it is a separate, user-controlled permission in Settings → Location; the background worker requires it to acquire a fresh current-location fix.
- [x] Trace periodic refresh scheduling: WorkManager uses the configured interval (90 minutes by default in non-debug builds), requires network connectivity, and by default skips work when the battery is low. Android may defer execution.
- [x] Trace the optional update check's default, worker trigger, daily throttle, and GitHub endpoint. Complete enumeration of forced/manual callers and build-flavor support remains open.
- [x] Confirm that saved-location deletion cascades to all seven inspected dependent tables: weather, source parameters, alerts, daily, hourly, minutely, and normals.
- [x] Inspect source backup configuration: API 37 target, no explicit backup attributes/rules, and Android Auto Backup defaults make SharedPreferences and the SQLDelight database eligible while cacheDir is excluded.
- [ ] Verify merged manifests and backup/restore behavior for every shipped variant and supported device.
- [ ] Trace every call site that clears the separate current-location preference store and verify deletion/reset behavior.
- [x] Inspect the shared OkHttp cache location, 50 MiB cap, HTTP cache semantics, Android cache-directory lifecycle, and absence of a location-deletion hook in the reviewed client setup.
- [ ] Inspect provider response headers/cache entries and trace all cache eviction/clear call sites.
- [ ] Enumerate all permissions and link each to the exact user-visible feature and background worker that uses them.
- [ ] Verify default location, search, reverse-geocoding, and weather-source choices by build flavor and region.
- [ ] Capture representative sanitized requests for each active external provider and identify which fields (coordinates, place IDs, query text, locale, API key, headers) are transmitted.
- [ ] Review each provider's current privacy policy, terms, attribution, data retention, onward sharing, and regional limitations.
- [x] Inspect the HTTP client/interceptor configuration: update checks use the shared OkHttp client, and no app/device/version fields are added in the reviewed GitHub release request. Exact runtime headers/User-Agent still need capture.
- [x] Trace the background update-check trigger and 24-hour successful-check throttle; forced/manual callers and per-flavor support remain open.
- [ ] Audit crash/log export, logging, analytics/telemetry SDKs, notification services, update checks, dependency graph, and all shipped flavors.
- [ ] Complete the remaining local-storage audit: exhaustively trace current-location preference clearing, full reset paths, cache call sites/provider response headers, and real backup/restore behavior.
- [ ] Verify the Android permission grant flow and third-party client behavior for `READ_PROVIDER`; correct the stale Breezy label/description after copy approval.
- [ ] Document every configured app-to-app receiver and its payload; confirm the UI clearly explains the selected packages.
- [x] Publish a Solstice-specific notice distinguishing device location, IP-derived location, weather/geocoding providers, GitHub update checks, local app-to-app sharing, user-triggered logs, and future optional contributions.
- [ ] Review the public notice against all shipped build variants, current provider policies, Android backup/deletion behavior, dependency diagnostics, and applicable legal requirements.
- [ ] Review user-visible permission and consent wording before changing it.

## 10. Reviewed code entry points

- `app/src/main/AndroidManifest.xml` — permissions, custom `READ_PROVIDER`, provider export/read permission.
- `app/src/main/kotlin/org/breezyweather/sources/SourceManager.kt` — registered sources and default source selection.
- `app/src/main/kotlin/org/breezyweather/sources/android/AndroidLocationService.kt` — Android location request/fallback.
- `app/src/src_nonfreenet/org/breezyweather/sources/ipsb/IpSbLocationService.kt` and `IpSbLocationApi.kt` — IP.SB location.
- `app/src/src_nonfreenet/org/breezyweather/sources/geonames/GeoNamesService.kt` — location search.
- `app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimService.kt` — reverse geocoding.
- `app/src/main/kotlin/org/breezyweather/domain/settings/SettingsManager.kt` — app-update default and refresh preferences.
- `app/src/main/kotlin/org/breezyweather/background/updater/AppUpdateChecker.kt`, `GetApplicationRelease.kt`, and `ReleaseService.kt` — optional GitHub update check.
- `app/src/main/kotlin/org/breezyweather/sources/gadgetbridge/GadgetbridgeService.kt` and `breezyupdatenotifier/BreezyUpdateNotifierService.kt` — local app broadcasts.
- `app/src/main/kotlin/org/breezyweather/background/provider/WeatherContentProvider.kt` — local exported data provider.

## 11. Licensing and third-party attribution evidence

- The root `LICENSE` is an LGPL version 3 text with additional permissions/definitions; `LICENSE_ADDITIONAL` separately prohibits implying contributor trademark rights, misrepresenting material origin, and leaving modified versions unmarked. The README identifies Solstice as an unofficial Breezy Weather fork and links the repository source.
- The app uses the AboutLibraries Gradle plugin with `offlineMode = true` and the fork-specific `config-fork` metadata directory. `DependenciesActivity` renders the generated `R.raw.aboutlibraries` inventory in the in-app Dependencies screen.
- The fork-specific metadata currently includes custom records for Breezy Weather (LGPL-3.0-only), Geometric Weather (LGPL-3.0-only), KNMI iOS (EUPL-1.2), Mihon (Apache-2.0), Asap fonts (OFL-1.1), and Google Maps Android utility library (Apache-2.0). Gradle dependency collection supplements these custom records; this file list is not a complete dependency inventory.
- The code-backed existence of an in-app dependency inventory and custom license metadata is confirmed. Completeness of the generated inventory against each shipped variant, exact notices/source offers in distributed APKs, and compliance with the inherited LGPL additional permissions have not been verified against built artifacts. Do not treat the current inventory as a completed legal review.
