# Meridian M4: observations (nearest airport)

Status: **spec, clean room step 1** (2026-11-23), from aviationweather.gov's public Data API page and
the METAR format as published by WMO and ICAO. No other engine's code read.

## Scope

Find the airports near a place, fetch their latest reports, and use them for current conditions when
they're fresh and close enough. Keep observations for M6 (correcting forecasts) and verification.

## Stations

- Station list from aviationweather.gov's station info (worldwide; the bulk file updates once a day).
  Meridian keeps a copy on the phone, refreshed weekly: id, name, latitude, longitude, elevation,
  country.
- For a place: the stations within the search radius (configuration, default 30 km), nearest first,
  at most 3. Distance is great-circle; an elevation difference over a limit (configuration, default
  300 m) counts against a station (temperature changes about 6.5 °C per km of height).

## Request

`GET https://aviationweather.gov/api/data/metar?ids=MMMX,MMSM&format=json&hours=3`

- Only the chosen ids; a custom User-Agent with Meridian's name and a contact link.
- `hours` from configuration (3 gives the last few reports, for trend and gaps).
- History up to 30 days is available; M6 uses `hours` larger once a day, not every refresh.

## Mapping

Record a real JSON response for MMMX before building and map from that (fixture). Expected fields and
our units:

| METAR | Meridian | Unit change |
|---|---|---|
| observation time | `time` (UTC) | |
| temperature, dew point | `temperature`, `dewPoint` | °C, kept |
| wind direction, speed, gust | `windDirection`, `windSpeed`, `windGust` | knots → m/s (× 0.514444); `VRB` → direction unknown |
| visibility | `visibility` | statute miles → m; "10+" → at least 16 km |
| altimeter | `pressureSeaLevel` | hPa (or inHg × 33.8639) |
| clouds (cover, base) | `cloudCover` (0–8 oktas), `cloudBase` | feet → m |
| present weather (`-RA`, `TSRA`, `BR`, `HZ`, …) | `condition` | table below |
| raw report | `raw` | kept for "nearby observations" (SOURCES.md) |

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

aviationweather.gov Data API page (endpoints, limits, history, cache files); WMO Manual on Codes
(FM 15 METAR) and ICAO Annex 3 for the report fields; the standard atmosphere lapse rate.
