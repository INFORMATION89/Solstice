# Meridian M4: observations (nearest airport)

Status: **spec, clean room step 1** (2026-11-23), from aviationweather.gov's public Data API page and
the METAR format as published by WMO and ICAO. No other engine's code read.

## Scope

Find the airports near a place, fetch their latest reports, and use them for current conditions when
they're fresh and close enough. Keep observations for M7 (correcting forecasts) and verification.

## Stations

- Stations from aviationweather.gov's `GET /api/data/stationinfo?bbox=lat0,lon0,lat1,lon1&format=json`
  (OpenAPI version 3.12): one call when a place is added, for a box around it, cached on the phone
  and refreshed weekly. No worldwide list needed (the bulk `stations.cache.json.gz` updates once a
  day and stays as a fallback). Kept: id, name, latitude, longitude, elevation, country.
- For a place: the stations within the search radius (configuration, default 30 km), nearest first,
  at most 3. Distance is great-circle; an elevation difference over a limit (configuration, default
  300 m) counts against a station (temperature changes about 6.5 °C per km of height).

## Request

`GET https://aviationweather.gov/api/data/metar?ids=MMMX,MMSM&format=json&hours=3`

- Only the chosen ids; a custom User-Agent with Meridian's name and a contact link (the API page asks
  for one so valid traffic isn't blocked).
- Parameters are only `ids`, `format`, `hours`, `bbox`, `date`, `taf` (documented in the OpenAPI
  file). **Undocumented parameters cause an error**, and going over the limits blocks access, so the
  adapter sends nothing else.
- `hours` from configuration (3 gives the last few reports, for trend and gaps). Most endpoints return
  at most 400 entries.
- History up to 30 days is available; M7 uses `hours` larger once a day, not every refresh.

## Mapping

From the official JSON schema (`aviationweather.gov/data/schema/metar1_0.json`, read 2026-11-24). The
response is an array of reports. Record a real MMMX response on Saturday as the fixture (it couldn't
be fetched from here) and check it against this table.

| Field (type) | Meridian | Unit change |
|---|---|---|
| `icaoId` (string) | `station` | |
| `obsTime` (integer, Unix seconds) | `time` (UTC) | |
| `metarType` (METAR, SPECI, …) | `special` when SPECI | |
| `temp`, `dewp` (number, °C; `dewp` nullable) | `temperature`, `dewPoint` | kept |
| `wdir` (integer degrees, or the string `"VRB"`) | `windDirection` | `VRB` → direction unknown |
| `wspd`, `wgst` (integer knots, nullable) | `windSpeed`, `windGust` | × 0.514444 → m/s |
| `visib` (number in statute miles, the string `"10+"`, or null) | `visibility` | × 1609.344 → m; `"10+"` → at least 16,093 m |
| `slp` (hPa, nullable), else `altim` (hPa) | `pressureSeaLevel` | kept (both already hPa) |
| `presTend` (hPa over 3 h, nullable) | `pressureTrend` | kept |
| `clouds` (list of `cover`: CLR, CAVOK, FEW, SCT, BKN, OVC, OVX; `base` in feet) | `cloudCover` (oktas), `cloudBase` | FEW 2, SCT 4, BKN 6, OVC/OVX 8; feet × 0.3048 → m |
| `wxString` (string, nullable) | `condition` | table below |
| `precip` (inches, last hour, nullable) | `precipitationLastHour` | × 25.4 → mm |
| `qcField` (bit flags) | `quality` | kept; a report flagged as sensor-off for a value drops that value |
| `rawOb` (string) | `raw` | kept for "nearby observations" (SOURCES.md) |
| `lat`, `lon`, `elev` (m) | station position | kept |

The schema lists every field as required but many as nullable: a missing value is `null`, never zero.

Present weather to condition (first match wins, intensity kept): `TS` → thunderstorm; `SN`, `SG` →
snow; `PL`, `GR`, `GS` → hail/ice; `FZ` → freezing; `RA`, `SH` → rain or showers; `DZ` → drizzle;
`FG` → fog; `BR` → mist; `HZ`, `FU` → haze/smoke; `DU`, `SA` → dust. No weather: from cloud cover
(clear, partly, mostly, overcast). The table is data, not code.

Missing values stay missing; never filled from the forecast.

## The current conditions rule

Current conditions use the observation when **all** are true (thresholds in configuration):
- the report is younger than 75 minutes (reports are normally hourly, plus specials);
- the station is within 15 km and within 300 m of the place's elevation;
- the value isn't missing and passes a sanity check (temperature −90 to 60 °C, a jump of more than
  10 °C in an hour flagged).

Otherwise the forecast's current values. Per variable: temperature can come from the airport while
the sky condition comes from the forecast. Provenance says which (M2). Night-time conditions (clear
night, not sunny) follow the place's sun times.

## Tests

- Fixture for MMMX (and one with `VRB`, gusts, missing temperature, a special report, `CAVOK`).
- Unit changes against hand-worked values.
- Rule: a 2-hour-old report isn't used; a station 40 km away isn't; mixed per variable works.
- Station picker: a place between two airports picks the nearer; one at 2,240 m skips a station at
  sea level.

## Done when

Current conditions in CDMX come from MMMX when fresh (MERIDIAN_PLAN.md section 9).

## Consulted

aviationweather.gov Data API page (endpoints, limits, history, cache files), its OpenAPI file (version
3.12) and its METAR JSON schema (`metar1_0.json`); WMO Manual on Codes
(FM 15 METAR) and ICAO Annex 3 for the report fields; the standard atmosphere lapse rate.
