# Meridian M7: learning on the phone

Status: **spec, clean room step 1** (2026-11-24), from the published methods in MERIDIAN_PLAN.md
section 6 (NCEP's decaying-average bias correction, inverse-RMSE weights) and ENGINE_SPEC.md section 8.
No other engine's code read.

## Scope

For each saved place: log what each model forecast and what the nearest airport later measured, learn
each model's bias and error, correct the next hours with the bias, and feed the errors to M5's skill
weights. Everything stays on the phone (RULES.md section 4); kept 30 days.

## What gets logged

- **Forecasts:** on each refresh, for each model and variable, the values for lead times in the
  buckets that will be verified (configuration; default the hours at +1, +3, +6, +12, +24, +48, +72
  and then daily to +7 days), with issue time. Not every hour of every refresh: one row per model,
  variable, valid hour and lead bucket, the latest issue winning.
- **Observations:** from M4, for the place's chosen station.
- **Variables at first:** temperature and dew point (corrected); rain (verified only, Brier score,
  never corrected at first).

## Matching

A forecast for hour H is verified by the station's report closest to H within ±30 minutes
(configuration). No report: no error for that hour (never filled in). The station must still pass
M4's distance and elevation rule; if the place's station changes, its statistics start again.

## Statistics

Kept per place, model, variable, lead bucket (0–24 h, 24–72 h, 3–7 days) and hour-of-day bucket
(configuration, default 3-hour blocks: errors change through the day):

- `bias`: decaying average of error (forecast − observation):
  `bias ← (1 − w) · bias + w · error`, with `w = max(w_min, 1 / n)` so the first values are a plain
  running mean and later ones decay. `w_min` from configuration, NCEP's 0.02 as the starting value,
  tuned on the Mexico City archive.
- `rmse` over the rolling 30 days, for M5's weights.
- `count`: number of verified pairs. Fewer than the minimum (configuration, default 10) means no
  correction for that bucket.

## Correction

- Applied to each model's value before blending: `value − bias`, for corrected variables only.
- Fades out with lead time (ENGINE_SPEC.md section 8): full up to 48 h, then linearly to zero at 7
  days (both from configuration).
- Never pushes a value outside what the models span by more than the bias itself (a guard against a
  bad station).
- Shown in provenance (M2's `correction`) and in the sources sheet: "Adjusted −0.8° from 30 days of
  Mexico City airport reports". A switch in Settings › Data and sources turns it off.

## Seeding from public data (so it works from day one)

When a place is added and its station is known, one background job fetches 30 days of past forecasts
from Open-Meteo's **Previous Runs API** (not the Historical Forecast API, which stitches each run's
first hours; see MODEL_TEST_SPEC.md) and the station's reports (aviationweather.gov's 30-day history),
and computes the starting statistics. It counts against M2's limits and runs only on Wi-Fi or when
the person allows it (configuration).

## Skill weights

M5's phase 2 reads `rmse` per model, variable and lead bucket from these statistics. Weights and the
correction switch on separately, each only after its own archive check.

## Storage

M2's `StatRow` holds one row per statistic key. Forecast and observation logs older than 30 days are
deleted (`deleteOlderThan`); statistics keep only what the window needs. Settings › Privacy › Clear
removes all of it.

## Tests

- Decaying average: hand-worked sequence; `1/n` warm-up then `w_min`.
- Matching: a report 40 minutes off isn't used; a missing hour adds nothing.
- Fade: full at 24 h, half at the midpoint, zero at 7 days.
- Guard: a station 5° off for a day doesn't move the forecast past the models' range plus the bias.
- Archive check: on the Mexico City rainy-season archive, corrected temperature MAE is lower than
  uncorrected for 0–24 h and 24–72 h; Brier score for rain unchanged (not corrected).

## Done when

Error drops on the archive check; shown in the sources sheet (MERIDIAN_PLAN.md section 9).

## Consulted

MERIDIAN_PLAN.md section 6 and the papers it cites (NCEP decaying average, inverse-RMSE blending,
Kalman filter study for later); Open-Meteo's Previous Runs API page; aviationweather.gov's Data API
page (30-day history).
