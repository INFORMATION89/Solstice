# Weather icons: what Material Symbols covers

Checked 2026-11-23 directly against Google's `material-design-icons` repo (the same place
`tools/make_symbols.py` downloads from), Rounded style. Decided: Material Symbols now, our own set
based on it later (PLAN.md). Weather icons today are still Breezy's PNGs; the app's UI icons already
come from Material Symbols through `tools/make_symbols.py`.

## Meridian's conditions → Material Symbols

| Condition | Day | Night | Gap |
|---|---|---|---|
| Clear | `clear_day` (or `sunny`) | `clear_night` (or `bedtime`) | |
| Mostly clear | `partly_cloudy_day` | `partly_cloudy_night` | Same glyph as partly cloudy |
| Partly cloudy | `partly_cloudy_day` | `partly_cloudy_night` | |
| Overcast | `cloud` (or `cloudy`) | same | |
| Fog, rime fog | `foggy` | same | No rime variant |
| Mist, haze | `mist` | same | No haze, dust or smoke |
| Drizzle | `rainy_light` | same | No distinct drizzle |
| Rain light, moderate, heavy | `rainy_light`, `rainy`, `rainy_heavy` | same | |
| Rain showers | `rainy` | same | No sun or moon with rain |
| Freezing drizzle, freezing rain | `weather_mix` | same | No ice-specific glyph |
| Rain and snow | `rainy_snow` | same | |
| Snow light, moderate, heavy | `weather_snowy`, `snowing`, `snowing_heavy` | same | |
| Snow grains | `weather_snowy` | same | |
| Snow showers | `sunny_snowing` | — | No night version |
| Thunderstorm, heavy thunderstorm | `thunderstorm` | same | No heavy version |
| Thunderstorm with hail | `weather_hail` | same | No storm and hail together |
| Wind | `air` | same | |
| Tropical cyclone, tornado | `cyclone`, `tornado` | | |
| Heat, severe cold | `heat`, `severe_cold` | | |
| Flood | `flood` | | |

**Card and detail icons:** humidity `humidity_percentage` (also `humidity_high`, `humidity_low`), dew
point `dew_point`, visibility `visibility`, sunrise and sunset `wb_twilight` (one glyph for both),
temperature `thermostat`, rain chance `water_drop`, umbrella `umbrella`, moon `bedtime` or
`nightlight` (no phases).

**Not in Material Symbols** (404 when checked): `uv_index`, `sunrise`, `sunset`, `haze`, `wildfire`.

## What our own set would add (drawn on the same 960 grid, weight 500, Rounded)

1. Showers: sun with rain, moon with rain; moon with snow.
2. Mostly clear distinct from partly cloudy (smaller cloud).
3. Drizzle (fine dots), freezing rain and ice.
4. Thunderstorm with rain, heavy thunderstorm, thunderstorm with hail.
5. Haze, dust, smoke.
6. UV index (for the sun button and the UV sheet).
7. Sunrise and sunset as two glyphs.
8. Eight moon phases (for the moon card rework).
9. Pressure gauge and pollen.

Everything else uses Material Symbols as is. The mapping table above becomes data (a condition-to-icon
table), so swapping in our own icons later changes the table, not code.
