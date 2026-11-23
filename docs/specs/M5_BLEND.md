# Meridian M5: blend and spread

Status: **spec, clean room step 1** (2026-11-23), from Open-Meteo's API documentation and the published
work listed in MERIDIAN_PLAN.md section 6. No other engine's code read.

## Scope

Ask several models for the same place, blend them into one forecast, and say how much they disagree.
Equal weights first; skill weights come once M7's statistics exist (the switch is configuration).

## Models

- `GET https://api.open-meteo.com/v1/forecast` with `models=` a comma list from configuration. Starting
  list for Mexico City, checked against the model test (Saturday step 3) before it's final: ECMWF IFS,
  GFS, ICON global, GEM global, and best match as the reference.
- Each variable comes back per model (suffixed by model id). A model without a variable, or past its
  range, is simply absent for those hours; the blend uses whoever is there.
- Call weight grows with models × variables × days (M2 counts it). One request for all models.
- Spread from Open-Meteo's ensemble API (`ensemble-api.open-meteo.com/v1/ensemble`) for temperature
  and precipitation, one ensemble from configuration: its members' standard deviation and the share
  of members with rain.

## Blending

Per variable, per hour:

| Variable kind | Blend |
|---|---|
| Temperature, dew point, pressure, humidity | weighted mean |
| Wind | weighted mean of u and v components, then back to speed and direction; gusts weighted mean |
| Precipitation amount | weighted mean |
| Rain chance | the weighted share of models at or above the rain threshold (configuration, default 0.1 mm in the hour), as ENGINE_SPEC.md section 7 says; each model's own chance is kept in provenance, not blended |
| Cloud cover | weighted mean |
| Weather code | from the blended values (rain amount, cloud, temperature for snow), not a vote between codes |

Weights:
- **Phase 1 (M5):** equal.
- **Phase 2 (after M7):** inverse RMSE over a rolling 30 days, per variable and lead-time bucket
  (0–24 h, 24–72 h, 3–7 days, beyond), normalised, with a floor (configuration, default 0.05) so no
  model drops out. Fewer than 14 days of data for a bucket: equal weights for that bucket.

Daily values are made from the blended hours (max, min, sums), never blended separately.

## Spread shown to people

- **Model spread:** the range (min to max) and standard deviation across models, per hour.
- **Ensemble spread:** as above, when available.
- `spread` in `MeridianResult` holds the larger of the two standard deviations per hour, and the
  range. Confidence in words (SOURCES.md, confidence dots) from thresholds in configuration, e.g.
  temperature spread under 1.5 °C high, over 4 °C low.

## Tests

- Fixture with four models: hand-worked blend for a few hours.
- Wind: two models at 350° and 10° blend to 0°, not 180°.
- A model missing hours 120+: the blend uses the rest, weights renormalise.
- Rain chance: 3 of 4 equal-weight models above the threshold gives 75 %.
- Weather code: blended light rain and 90 % cloud gives a rain code even if two models say cloudy.
- Spread: identical models give zero; one outlier widens the range.

## Done when

The blend beats or matches best match on the archive check (MERIDIAN_PLAN.md section 9): mean absolute
error for temperature and Brier score for rain, Mexico City, Previous Runs API, results in the PR.

## Consulted

Open-Meteo forecast and ensemble API pages; the weighting and bias-correction papers named in
MERIDIAN_PLAN.md section 6; WMO guidance on ensemble spread communication.
