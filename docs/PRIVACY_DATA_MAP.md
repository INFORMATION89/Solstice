# Solstice privacy and data-flow map

**Status:** Internal technical audit; partial code-backed findings  
**Reviewed:** 2026-11-26  
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
- The manifest declares coarse, fine, and `ACCESS_BACKGROUND_LOCATION`. The inspected `AndroidLocationService.permissions` list requests only coarse/fine location; this does not establish whether another worker or feature requests background access. The background permission's request flow, actual call sites, and user-visible rationale remain unverified. Do not infer continuous or background tracking from the manifest declaration alone.

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
- The precise frequency/trigger rules and current build-flavor availability still need confirmation from the worker/caller path. Do not copy the old Breezy statement "once every 24 hours" into the Solstice notice until verified.

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

## 6. Diagnostics and logs

The inspected `CrashLogUtils.dumpLogs()` path is user-initiated: it runs `logcat *:E -d -f` into a cache file, appends app version/flavor/commit, Android build, and device brand/manufacturer/name/model details, then exposes the file through a notification for the person to open/share. This is a local export path, not an automatic upload in the inspected implementation. Error-level logs may contain additional sensitive context if other code logs it; the person should be told to inspect the file before sharing. No automatic crash-report upload was identified in the limited code paths inspected. This is not yet proof that the entire app/dependency graph contains no diagnostics collection.

Before making an absolute statement, scan the complete dependency graph, build variants, manifests, network interceptors, logging wrappers, and CI/release tooling for crash-reporting, analytics, telemetry, attribution, and automatic upload SDKs.

## 7. Consent, telemetry, and data contribution

Solstice's project rules require analytics/tracking/ads/social features to remain absent unless explicitly approved. Any future weather-error contribution or remote data sharing must be off by default, described separately from core operation, opt-in, revocable, and disclose the exact payload, purpose, recipient, retention, and data path before activation.

The code inspected in this pass did not establish an active remote forecast-error telemetry pipeline. Do not imply that the future opt-in contribution design is already implemented. Equally, do not use that absence as proof of "no data leaves the device": weather/geocoding/provider requests and local app-to-app sharing are active functionality.

## 8. Remaining evidence to complete the privacy review

- [ ] Enumerate all permissions and link each to the exact user-visible feature and background worker that uses it.
- [ ] Verify default location, search, reverse-geocoding, and weather-source choices by build flavor and region.
- [ ] Capture representative sanitized requests for each active external provider and identify which fields (coordinates, place IDs, query text, locale, API key, headers) are transmitted.
- [ ] Review each provider's current privacy policy, terms, attribution, data retention, onward sharing, and regional limitations.
- [ ] Inspect the HTTP client/interceptor configuration and User-Agent construction; determine whether app/device/version details are included.
- [ ] Find the exact update-check scheduling/call path, trigger, frequency, and whether it differs by flavor.
- [ ] Audit crash/log export, logging, analytics/telemetry SDKs, notification services, update checks, dependency graph, and all shipped flavors.
- [ ] Verify local storage categories, backups, retention/deletion behavior, and what is removed when a saved place is deleted.
- [ ] Verify the Android permission grant flow and third-party client behavior for `READ_PROVIDER`; correct the stale Breezy label/description after copy approval.
- [ ] Document every configured app-to-app receiver and its payload; confirm the UI clearly explains the selected packages.
- [x] Publish a Solstice-specific notice distinguishing device location, IP-derived location, weather/geocoding providers, GitHub update checks, local app-to-app sharing, user-triggered logs, and future optional contributions.
- [ ] Review the public notice against all shipped build variants, current provider policies, Android backup/deletion behavior, dependency diagnostics, and applicable legal requirements.
- [ ] Review user-visible permission and consent wording before changing it.

## 9. Reviewed code entry points

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
