# Model test for Mexico City: spec

Status: **spec for Saturday, step 3** (CONTINUE.md), written 2026-11-23. A research tool, not app or
Meridian code. It answers: how far off is each forecast model for Mexico City, at 1, 2 and 3 days
ahead, by time of day, during the 2026 rainy season?

## Data

**Forecasts: Open-Meteo's Previous Runs API**, not the Historical Forecast API.
- The Historical Forecast API stitches the first hours of every model run into one series, so its lead
  time is only a few hours and every model would look better than it is.
- The Previous Runs API keeps each variable at a fixed lead time of 1 to 7 days (`<variable>_previous_day1`
  and so on), from January 2024, for the same models as the Forecast API. That's a fair test.
- Variables: `temperature_2m`, `dew_point_2m`, `precipitation`, each with `_previous_day1`, `_day2`,
  `_day3`.
- Models (exact ids checked against the docs when building, kept in a config file): best match, GFS,
  ECMWF IFS, ECMWF AIFS, ICON, GEM, UKMO, Météo-France, JMA, and NCEP RRFS North America if its domain
  covers Mexico City.
- Place: the MMMX station's coordinates and elevation, read from the station list (not typed in), with
  `elevation` passed so Open-Meteo adjusts temperature to the same height. `timezone=GMT`, hourly.

**Observations: the Iowa Environmental Mesonet's METAR archive** (`/cgi-bin/request/asos.py`), since
aviationweather.gov keeps only 30 days.
- Station `MMMX`, fields temperature, dew point, present weather codes and the raw report; dates as
  `sts`/`ets` in UTC; routine and special reports.
- The service has an IP-based rate limit: one request per month of data is plenty.
- Rain from present weather codes (RA, SHRA, TSRA, DZ and so on), not from reported amounts: the IEM
  warns that METAR precipitation amounts have quirks.

**Period:** 2026-06-01 to 2026-10-15 (the rainy season). A second run on a dry-season month comes later
for comparison.

## Matching

- Each forecast hour gets the report closest to it within a set tolerance (a config value, starting at
  30 minutes). Hours without a report are skipped and counted.
- Temperatures converted from °F as reported by the archive, to °C, rounded only at the end.
- "Rain in the hour": the forecast says rain when `precipitation` ≥ 0.1 mm (the same threshold Open-Meteo
  uses for its rain chance); the observation says rain when any report in that hour has a rain or
  thunderstorm code.

## What it measures

Per model, per lead day (1, 2, 3), overall and by local time of day (night, morning, afternoon,
evening):
- **Temperature and dew point:** mean absolute error, RMSE and bias (warm or cold).
- **Rain in the hour:** hit rate (rain caught), false alarm ratio, and frequency bias (too often or too
  rarely).
- **Two blends**, as evidence for Meridian's milestone 5:
  - all models with equal weights;
  - weights from inverse RMSE, learned on June and July and tested on August to October, so the test
    never sees data it was trained on.

## Output

- `docs/research/model-test-2026-rainy.md`: the tables above, plus a few plain-language findings
  (which model runs warm in the afternoon, which catches storms best, whether a blend helps), and a
  list of what was checked and the gaps.
- The paired data as a CSV attached to the workflow run, not committed (it's large and re-creatable).
- Credits: forecast data by Open-Meteo.com (CC BY 4.0); observations from the Iowa Environmental
  Mesonet.

## How it runs

- `tools/model_test/`: a Python script using only the standard library plus pandas; models, dates,
  tolerance and thresholds in a config file (RULES.md section 3 applies to tools too).
- A GitHub Actions workflow started by hand (`workflow_dispatch`), because the coding sessions can't
  reach these sites. It runs the script and opens a pull request with the report. Marco can also run it
  on his computer.
- About one Open-Meteo request per model and month (well under the free limits) and one IEM request
  per month.

## Done when

The report exists, its numbers match a few hand-checked hours, and the findings are added to PLAN.md
research queue 15.
