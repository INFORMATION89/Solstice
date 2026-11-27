# Solstice weather icons: Material Symbols coverage

Checked 2026-11-23 directly against Google's `material-design-icons` repository in Rounded style.
The current product decision is to use Material Symbols for now and consider a Solstice-owned weather
icon set later. Interface icons already use Material Symbols through `tools/make_symbols.py`.

## Solstice weather conditions → Material Symbols

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

## Gaps a Solstice-owned icon set could address

1. Showers: sun with rain, moon with rain, and moon with snow.
2. Distinguish mostly clear from partly cloudy.
3. Drizzle, freezing rain, and ice.
4. Thunderstorm with rain, heavy thunderstorm, and thunderstorm with hail.
5. Haze, dust, and smoke.
6. UV index, sunrise, and sunset as distinct symbols.
7. Eight moon phases.
8. Pressure gauge and pollen.

Everything else can use Material Symbols. Keep the condition-to-icon mapping in app data so a future
icon set changes the mapping rather than the weather logic.
