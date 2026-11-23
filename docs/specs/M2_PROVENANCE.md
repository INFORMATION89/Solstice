# Meridian M2: provenance, source selection, caching, storage

Status: **spec, clean room step 1** (2026-11-23). Written from Meridian's own docs (ENGINE_SPEC.md,
MERIDIAN_PLAN.md, SOURCES.md) and the sources' published rate limits. No other engine's code read.

## Scope

Every result says where each value came from. Each refresh asks only the sources that need asking.
No source gets called more often than it allows. What's fetched is kept behind a storage interface
(Room comes in its own module).

## Provenance

Every value Meridian returns can be traced to:

| Field | Meaning |
|---|---|
| `source` | Source id (`open-meteo`, `aviationweather`, `wmo`, `fpas`) |
| `model` | Model id when the source has several (`best_match`, `ecmwf_ifs025`, …), else empty |
| `issued` | When the source made it (model run, observation time, alert `sent`) |
| `fetched` | When Meridian got it |
| `weight` | Share in the blend, 0–1 (1 when it's the only one) |
| `correction` | Applied correction id and amount, if any (M7) |
| `fallback` | `true` when the preferred source failed and this one stood in |

`Provenance` on a result holds one entry per source and model used, plus per variable the list of
contributors. The UI never reads values without being able to show this (SOURCES.md, transparency).

## Credits

Each source declares its credit as data: text, link, licence, and where it must appear (on screen,
on the about page, or both). `credits(result)` returns only the sources the result actually used.
Open-Meteo: CC BY 4.0, credit with link. WMO feeds: credit the issuing agency named in the alert.

## Source selection per refresh

1. Start from the features asked for (`forecast`, `current`, `alerts`, `airQuality`, …).
2. For each feature, take the sources that **cover the place** (coverage is data: a bounding box,
   country list or "worldwide", from configuration), are **turned on** (SOURCES.md, off until
   relevant) and have what they need (a key, consent for best-effort sources).
   Order them by tier, then resolution, then recent health; a source picked by hand for this place
   and feature wins while it covers the place (ENGINE_SPEC.md section 6).
3. Drop sources still **fresh** for that feature (below their wait, from configuration; defaults:
   forecast 15 min, current 10 min, alerts 5 min, air quality 1 h).
4. Drop sources **over their limit** (below).
5. Ask the rest at the same time; publish partial results through `results(place)` as each answers.
6. A source that fails: its last good data stays (marked stale, with its age), the next source for that
   feature fills in (`fallback`), and a `Problem` is added. Never an exception to the caller.

## Rate limits

One limiter per source, shared across places and callers (the CLI and the app count together):

| Source | Limits (from its docs) | Meridian's own ceiling |
|---|---|---|
| Open-Meteo | 600 / min, 5,000 / h, 10,000 / day, with call weights (more variables or days count as more calls) | half of each |
| aviationweather.gov | 100 / min | 20 / min |
| WMO feeds | none published | 1 per feed per 5 min |
| FPAS | congestion state in `/config/server_status` | 1 per 5 min, stop while congested |

All numbers live in configuration. When a limit is near, the limiter delays; when a source answers
429 or 503, it backs off (doubling from 1 min up to 1 h, honouring `Retry-After`). Call weights for
Open-Meteo are estimated from its documented rule and counted, not guessed later.

## Caching

- HTTP: the client's cache honours `Cache-Control`, `Expires`, `ETag` and `Last-Modified`.
- Meridian's own cache: last parsed result per source, place and feature, with its expiry, so a cold
  start shows something before any network call.
- Places that are close (configuration, default 1 km) and use the same source share cached forecasts.

## Storage interface

```kotlin
interface MeridianStorage {
    suspend fun putForecasts(rows: List<ForecastRow>)
    suspend fun forecasts(place: PlaceKey, from: Instant, to: Instant): List<ForecastRow>
    suspend fun putObservations(rows: List<ObservationRow>)
    suspend fun observations(station: String, from: Instant, to: Instant): List<ObservationRow>
    suspend fun putStats(rows: List<StatRow>)
    suspend fun stats(place: PlaceKey): List<StatRow>
    suspend fun putCache(entry: CacheEntry)
    suspend fun cache(key: String): CacheEntry?
    suspend fun deleteOlderThan(cutoff: Instant)     // 30 days, decided
    suspend fun clear()                              // Settings › Privacy
}
```

Settings and keys aren't part of it: the app passes key-value settings and secure key storage
(DataStore and Android's keystore in Solstice), as ENGINE_SPEC.md section 9 says.

`testing` ships an in-memory version; `storage-room` the Room one. Rows match MERIDIAN_PLAN.md section 7.
A place is stored by rounded coordinates (configuration, default 3 decimals, about 100 m), never by name.

## Tests

- Selection: a fresh source isn't asked; an uncovered one isn't asked; a failing one falls back.
- Limiter: 1,000 simulated refreshes never exceed any ceiling; 429 backs off.
- Provenance: every value in a result has a contributor; credits list only used sources.
- Storage: in-memory and Room pass the same test suite; `deleteOlderThan` removes exactly the old rows.

## Done when

Results show their sources; no source is called more often than allowed (MERIDIAN_PLAN.md section 9).

## Consulted

Meridian's docs (ENGINE_SPEC.md sections 6 and 9); Open-Meteo's terms and API pages (limits, call weights); aviationweather.gov's Data
API page (limits); the FPAS OpenAPI description (server status).
