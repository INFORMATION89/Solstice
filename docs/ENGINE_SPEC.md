# Meridian, the weather engine: spec

Status: **draft for Marco** (2026-11-23). Clean room step 1 (RULES.md section 5): written in our own
words from public sources only (list at the end). The build session works from this file, never from
Breezy's code. Design background: SOURCES.md. Decisions: PLAN.md, "Weather engine".

## 1. What it is

**Meridian** (name decided 2026-11-23; repo `INFORMATION89/Meridian` when it's created) is a
weather library with no UI. Given a place, it returns a forecast, current conditions, next-hour
rain, alerts, air quality and UV, each with a record of where every number came from. Solstice uses it
first; other apps (a macOS menu bar app, a Windhawk mod, a clock app) can use it later.

- **Licence:** Apache 2.0.
- **Language and platforms:** Kotlin Multiplatform. The core lives in common code; Android and desktop
  JVM first, macOS next (as a framework Swift can call), Windows later (section 9).
- **Not in it:** UI, Android `Context` in the core, dependency injection frameworks, any app's code.

## 2. Platforms

Kotlin Multiplatform is stable for Android, iOS and desktop JVM. For Google's Jetpack libraries, macOS
is tier 2 (partly tested) and Windows tier 3 (untested). So:

| Target | Use | Notes |
|---|---|---|
| Android | Solstice | First target |
| JVM | desktop helper, tests, archive checks | Same code as Android; runs the offline tests |
| macOS (Kotlin/Native) | menu bar app | Built as a framework a Swift app imports |
| Windows | Windhawk mod | Windhawk mods are single C++ files loaded into Windows processes such as Explorer, so the engine shouldn't run inside them. A small helper process (JVM or Kotlin/Native) runs the engine; the mod reads its latest result. Decide when the time comes. |

Dependencies must work in common code: an HTTP client with a native engine per platform (Ktor offers
Android/OkHttp, Darwin for Apple and WinHttp for Windows), a JSON/XML serializer, a date-time library,
and storage behind an interface the app provides.

## 3. Core concepts

**Place:** latitude, longitude, optional elevation, time zone, optional country and region codes
(ISO 3166). The engine never stores place history; the app owns saved places.

**Feature:** `forecastHourly`, `forecastDaily`, `current`, `nowcast` (next 1–2 hours of rain),
`alerts`, `airQuality`, `uv`, `pollen`, `normals`, `observations`.

**Source descriptor** (data, one file per source, loaded at start):
- `id`, display name, operator, homepage, terms URL, required credit text and link.
- `access`: `api` or `website` (reads pages; always best-effort).
- `needsKey`, `costs` (free, paid), rate limits and the minimum interval between calls.
- `coverage`: list of country codes, bounding boxes or polygons (GeoJSON-style).
- Per feature: `tier` (`official`, `regional`, `global`, `fallback`), update cadence, resolution.

**Source adapter:** code that turns a source's response into the engine's model. One per source.
Clean adapters live in the engine; until they're rewritten, Breezy-based adapters live in Solstice
and plug in through the same interface.

## 4. Data model

All values in SI units and all times in UTC internally; units and time zones are applied by the app.

- **Time series:** per variable, a list of `(time, value, provenance)` at the source's native step
  (minutes, 15 minutes, hours, days).
- **Variables:** temperature, apparent temperature, dew point, humidity, pressure, wind speed, gust and
  direction, precipitation amount and probability, precipitation type, cloud cover, visibility, UV
  index, weather condition (a small fixed set), air quality pollutants and index, pollen.
- **Observation:** a measured value with station id, distance from the place, elevation difference,
  and measurement time.
- **Alert:** follows the Common Alerting Protocol 1.2 (OASIS): `identifier`, `sender`, `sent`,
  `status`, `msgType` (Alert, Update, Cancel), `references`, and per info block `language`, `event`,
  `urgency`, `severity`, `certainty`, `onset`, `expires`, `headline`, `description`, `instruction`,
  and areas (`areaDesc`, polygons, geocodes). Non-CAP sources are mapped into the same shape.

## 5. Provenance

Every value carries:
- the source ids that produced it and their weights;
- any correction applied (amount, the observations it came from, the window);
- when each input was issued and fetched;
- whether it's a fallback because a better source failed.

Results also carry a summary per feature: which sources were used, which were skipped and why
(out of coverage, off, no key, failed, stale). The transparency graphic and the sources sheets read
only this.

## 6. Choosing sources (on every refresh)

For each feature of each place:
1. Candidates: sources whose coverage contains the place, that the person hasn't turned off, and that
   have what they need (a key, consent for best-effort sources).
2. Order by tier, then by resolution, then by recent health (failures, staleness).
3. A source picked by hand for this place and feature wins while it covers the place.
4. Call the chosen sources in parallel, within each source's rate limit and minimum interval.
5. If a call fails or returns stale data, use the next candidate and record the fallback.

## 7. Combining sources

**Forecast (blend):** a weighted mean per variable and time step across the models used. Weights come
from configuration; later, from each model's recent error at this place (section 8). The spread per
step (lowest and highest model, and the standard deviation) is part of the result.
Precipitation probability, when models disagree, is the share of models above the rain threshold.

**Next-hour rain:** phase 1 uses 15-minute model data where it exists (Central Europe and North
America) and the hourly blend elsewhere, marked as coarse. Phase 2 adds a radar nowcast (section 10).

**Current conditions:** the nearest observation within a maximum distance, elevation difference and
age; otherwise the model's current hour, marked as a model value.

**Alerts:** the union of every alert source covering the place. Duplicates collapse when they share
an identifier, when one references the other (CAP `references`), or when sender, event and overlapping
validity match. Updates replace what they reference; cancels remove it. The most local copy wins
(city over national over global aggregator). Alerts are never blended or reworded.

**Air quality and UV:** a local station when close and fresh, otherwise the global model.

## 8. Learning (on the phone)

- A short rolling log per saved place: what each source forecast for each hour, and what the nearest
  observation later measured. Kept 30 days by default (configuration; decided 2026-11-23), then deleted.
- From it: each source's recent error, used for weights, and a bias correction for the next hours
  that fades out with lead time.
- Seeded from public data so it works from day one: archived past forecasts (Open-Meteo keeps them
  from 2022) checked against station observations.
- **Opt-in sharing** (RULES.md section 4): only aggregated forecast errors, area-rounded and
  hour-rounded, computed on the phone. Not built until the server and the legal check exist.

## 9. Storage, caching and scheduling

- Interfaces the app implements: key-value settings, a small database for logs and cache, secure
  storage for keys, and a scheduler.
- Cache each response for its source's update cadence; never call a source more often than its
  minimum interval, whatever the app asks.
- The engine never schedules itself; the app decides when to refresh (WorkManager on Android).

## 10. Phase 2: radar nowcast (not before phase 1 works)

Extrapolate the last radar frames forward with optical flow, then blend into the model over the first
hour, as open nowcasting systems do. Inputs: public radar composites where they exist (US, Canada,
Europe's OPERA, Japan, Taiwan and others); elsewhere, the satellite rain rate (NOAA's Enterprise Rain
Rate from GOES-18, GOES-19 and Himawari-9: 2 km, every 10 minutes, open data). Mexico City has SMN's
Cerro Catedral radar, published only as images on SMN's site; ask SMN about access first.

## 11. API for apps

- `refresh(place, features)` returns a result with values, spread and provenance; streams partial
  results as sources answer.
- `sources(place)` lists candidates and their state for the settings screen.
- `credits(result)` lists the credits that must be shown.
- Errors are values in the result, never exceptions across the API.
- Android: Solstice re-exposes results to other apps on the phone (the data sharing Breezy already
  offers to Gadgetbridge and launchers), with the person's consent.

## 12. First sources (decided 2026-11-23)

Open-Meteo (forecast, air quality; no key), aviationweather.gov airport reports (current conditions;
public, no key), WMO Severe Weather and the FOSS Public Alert Server (official alerts). Everything
else plugs in later.

## 13. Configuration and tokens

Every threshold, weight, distance, age, window, cell size and interval is configuration with a named
default, never typed into code (RULES.md section 3). The list grows with the code; each new one is
agreed with Marco.

## 14. Testing

- Each adapter is tested offline against recorded responses (fixtures), including error and empty
  responses.
- Blend and correction changes are checked against public archives (past forecasts against
  observations) before they ship, with the error before and after recorded in the pull request.

## Open questions for Marco

None right now.

## Consulted (public sources only)

Kotlin Multiplatform platform stability (kotlinlang.org); Jetpack KMP support tiers
(developer.android.com); Kotlin/Native frameworks for Swift; Ktor client engines (ktor.io); Windhawk
mod structure (Windhawk wiki); OASIS CAP 1.2; Open-Meteo API docs (best match, 15-minute data,
historical forecasts); aviationweather.gov Data API; WMO Severe Weather source list; FOSS Public Alert
Server source list; NOAA Enterprise Rain Rate; pysteps paper (Pulkkinen et al., 2019); LibreWXR README;
Apple WeatherKit attribution; Gradient Weather privacy policy and store page; Acme Weather coverage.
