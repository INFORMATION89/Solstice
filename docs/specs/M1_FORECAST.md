# Meridian M1: first forecast (Open-Meteo)

Status: **spec, clean room step 1** (2026-11-23), written only from Open-Meteo's public documentation.
The build session works from this file alone. Context: ENGINE_SPEC.md, MERIDIAN_PLAN.md section 9.

## Scope

Given a place, return hourly, daily and current weather from Open-Meteo's best-match forecast, as
Meridian's own types, with provenance. One source, one model, no blending (that's M5), no storage
beyond an in-memory cache (M2).

## Request

`GET https://api.open-meteo.com/v1/forecast` with:

| Parameter | Value | Why |
|---|---|---|
| `latitude`, `longitude` | the place | WGS84 |
| `elevation` | the place's, when known | Otherwise Open-Meteo uses a 90 m elevation model; passing ours keeps temperature right in mountains |
| `hourly` | list below | |
| `daily` | list below | Needs `timezone` |
| `current` | list below | |
| `timezone` | the place's IANA zone | Daily values follow local days |
| `timeformat` | `unixtime` | `utc_offset_seconds` is fixed for the whole response and ignores daylight-saving changes, so local times are computed from the zone, not the offset |
| `forecast_days` | configuration (default 10, Outlook's span) | Allowed 0 to 16 |
| `past_days` | configuration (default 0) | Allowed 0 to 92 |
| `wind_speed_unit` | `ms` | Already SI |
| `temperature_unit`, `precipitation_unit` | defaults (`celsius`, `mm`) | |
| `models` | not sent (best match) | M5 adds explicit models |
| `cell_selection` | default (`land`) | |

No key. A custom User-Agent naming Meridian and its repo.

**Hourly:** `temperature_2m`, `apparent_temperature`, `relative_humidity_2m`, `dew_point_2m`,
`pressure_msl`, `surface_pressure`, `cloud_cover`, `visibility`, `wind_speed_10m`, `wind_direction_10m`,
`wind_gusts_10m`, `precipitation`, `rain`, `showers`, `snowfall`, `precipitation_probability`,
`weather_code`, `uv_index`, `is_day`.

**Daily:** `weather_code`, `temperature_2m_max`, `temperature_2m_min`, `apparent_temperature_max`,
`apparent_temperature_min`, `sunrise`, `sunset`, `daylight_duration`, `uv_index_max`,
`precipitation_sum`, `precipitation_probability_max`, `wind_speed_10m_max`, `wind_gusts_10m_max`,
`wind_direction_10m_dominant`. (Names to confirm against the docs' daily table while building.)

**Current:** `temperature_2m`, `apparent_temperature`, `relative_humidity_2m`, `weather_code`,
`wind_speed_10m`, `wind_direction_10m`, `wind_gusts_10m`, `precipitation`, `is_day`.

Variable lists live in configuration, not code.

## Response

```
latitude, longitude        the grid cell used (can be a few km from the request)
elevation                  of that cell
utc_offset_seconds, timezone, timezone_abbreviation
current  { time, interval, <variable>: value }       interval in seconds (900 = 15 minutes)
hourly   { time: [...], <variable>: [...] }           arrays aligned to time
hourly_units, daily, daily_units, current_units
```

- Values can be `null` (not available for that hour or model): an absent value, never zero.
- Arrays may be shorter or longer than expected: align by `time`, never by index assumptions.

## Mapping to Meridian

**Units** (all checked against `*_units`; a unit we don't expect is a problem, not a guess):

| Open-Meteo | Unit | Meridian |
|---|---|---|
| temperatures, dew point | °C | °C (kept; the app converts) |
| relative humidity, cloud cover, precipitation probability | % | fraction 0 to 1 |
| pressure | hPa | Pa |
| wind speed, gusts | m/s | m/s |
| wind direction | ° | degrees, meteorological (from) |
| precipitation, rain, showers | mm (preceding hour) | mm over the hour **ending** at the timestamp |
| snowfall | cm (preceding hour) | m of snow over the hour ending at the timestamp |
| visibility | m | m |
| UV index | index | index |
| daylight duration | s | duration |
| sunrise, sunset | unixtime | instant; absent during polar day or night |

Note the sums and maxima (precipitation, gusts, radiation) describe the **preceding** hour; instant
values (temperature) describe the moment. Each Meridian variable records which kind it is.

**Weather codes** (WMO, as Open-Meteo documents them) to Meridian's condition set, with day or night
from `is_day`:

| Codes | Meridian condition |
|---|---|
| 0 | clear |
| 1 | mostly clear |
| 2 | partly cloudy |
| 3 | overcast |
| 45, 48 | fog (48: rime fog) |
| 51, 53, 55 | drizzle (light, moderate, dense) |
| 56, 57 | freezing drizzle (light, dense) |
| 61, 63, 65 | rain (light, moderate, heavy) |
| 66, 67 | freezing rain (light, heavy) |
| 71, 73, 75 | snow (light, moderate, heavy) |
| 77 | snow grains |
| 80, 81, 82 | rain showers (light, moderate, violent) |
| 85, 86 | snow showers (light, heavy) |
| 95 | thunderstorm |
| 96, 99 | thunderstorm with hail (light, heavy); only models with explicit hail produce these |
| 97 | heavy thunderstorm |
| anything else | unknown, recorded as a problem |

The table is data in the source's descriptor, not code.

**Time:** timestamps become instants (UTC); daily entries become local dates in the place's zone.

**Provenance:** source `open-meteo`, model `best_match`, the grid cell's coordinates and elevation, the
fetch time, and the request's call weight.

## Errors

- HTTP 400 returns `{ "error": true, "reason": "..." }`: a problem with the reason, no values.
- HTTP 429 or other limits: a problem, and Meridian backs off (time from configuration).
- Timeouts and network failures: a problem; cached values are returned if still fresh, marked stale.
- Never an exception across the API.

## Limits and caching

- Free tier: under 10,000 calls a day, 5,000 an hour, 600 a minute; long ranges, many variables or many
  models count as more than one call. Meridian keeps a running call-weight estimate and refuses to exceed
  a configured share of each limit.
- Cache by rounded coordinates (precision from configuration) and the request's parameters; time to
  live from configuration (a model updates every few hours, so a short TTL buys nothing).

## Credit

"Weather data by Open-Meteo.com", linked, plus the CC BY 4.0 notice, wherever these values show.

## Fixtures (recorded once, then tests run offline)

Mexico City (high altitude, rainy season), Berlin (daylight-saving switch inside the range), New York,
Sydney (southern summer), Tromsø in June (no sunset), a response with `null`s, an HTTP 400 error.

## Tests

- Every unit conversion and every weather code row.
- Hours align by time across a daylight-saving change; daily dates follow the place's zone.
- Nulls stay absent; unexpected units become problems.
- Errors and timeouts return problems, never exceptions; stale cache is marked stale.
- The call-weight guard stops requests at its limit.

## Done when

The CLI prints Mexico City's forecast and it matches Open-Meteo's own page for the same point; all
tests pass on CI.

## Consulted

Open-Meteo Forecast API documentation (parameters, response, variables and units, WMO weather codes,
errors); Open-Meteo terms; Open-Meteo ensemble API page (call weights).
