# Meridian M6: Solstice reads alerts from Meridian

Status: **spec** (2026-11-24). The first feature switched (MERIDIAN_PLAN.md section 8: switch directly,
alerts first). This is app-side work: Solstice's own code (LGPL, from Breezy Weather) talks to
Meridian's public API. Reading Solstice's source classes is fine here; Meridian's code stays clean room.

## How Meridian gets into the app

- `meridian/` as a git submodule plus a Gradle composite build (`includeBuild`), the same way as
  `motion/` (CONTINUE.md, Motion submodule workflow). Added only once M3 works (CONTINUE.md step 10).
- Solstice depends on `meridian:core`, `meridian:sources-wmo`, `meridian:sources-fpas` and
  `meridian:storage-room`. Nothing else in the app imports Meridian except the bridge.

## The bridge: Meridian as one source

A new source in `sources/meridian/`, implementing the app's existing `WeatherSource` (and `Source`)
interfaces, so everything downstream (alert list, details, notifications, widgets) keeps working:

| App contract | Meridian |
|---|---|
| `id` | `meridian` |
| `name` | "Meridian" (string resource) |
| `supportedFeatures` | `ALERT` only, for now |
| `isFeatureSupportedForLocation` | true where Meridian's alert sources cover the place (`sources(place)`) |
| `requestWeather(context, location, features)` returns `Observable<WeatherWrapper>` | calls `refresh(place, setOf(Feature.Alerts))` (a suspend function) through `rxObservable`/`rxSingle` (kotlinx-coroutines-rx3), returns `WeatherWrapper(alertList = …)` |
| `failedFeatures` | Meridian's `problems` for alerts, when no alert source answered |

One Meridian instance for the app (built in the app's dependency setup) with Room storage and the
app's HTTP client.

## Mapping a Meridian alert to the app's `Alert`

| App field | From Meridian (CAP 1.2 shaped) |
|---|---|
| `alertId` | Meridian's stable id (the CAP identifier at the root of its update chain, so updates replace, not add) |
| `startDate` | `onset`, else `effective`, else `sent` |
| `endDate` | `expires` (null when absent) |
| `headline` | `headline`, else `event` |
| `description` | `description`, HTML-escaped (`&`, `<`, `>`); line breaks become `<br />` in the app already |
| `instruction` | `instruction` |
| `source` | the issuing agency (`senderName`, else sender), the credit RULES.md and the WMO terms ask for |
| `severity` | Extreme → `EXTREME`, Severe → `SEVERE`, Moderate → `MODERATE`, Minor → `MINOR`, Unknown → `UNKNOWN` |
| `color` | `Alert.colorFromSeverity(severity)` for now; moves to the kit's tokens with motion-ui |

**Language:** the `info` block in the app's language, else the same language without region (`es` for
`es-MX`), else English, else the first. Alerts are never translated or reworded (ENGINE_SPEC.md).

**Imprecise area:** a geocode-only alert (M3) gets "Area approximate" before its description (string
resource, English and Mexican Spanish).

## The switch

- **New places:** Meridian is the default alert source wherever it covers the place.
- **Existing places:** a one-time migration moves places whose alert source is `wmo` or `fpas` (the two
  Meridian replaces) to `meridian`. A place where the person picked another alert source (a national
  one) keeps it. **To confirm with Marco before building.**
- Breezy's `wmo` and `fpas` sources stay in the app as choices until M6 has been checked on the phone
  for a while; removing them is a separate decision.
- Settings › Data and sources shows Meridian with its own sources and credits (`credits(result)`).

## Checks

- Unit tests: the mapping table above, language choice, HTML escaping, severity mapping, update
  replacing the earlier alert.
- Recorded real alerts (Mexico, US, Germany, from M3's fixtures) fed through the bridge: same count,
  titles, times and severities as Meridian's own result.
- On the phone: the alert card, its sheet, the alert notification and the widget show a Meridian alert;
  an update replaces it; a cancel removes it.

## Done when

Alerts on the phone come from Meridian, checked against recorded real alerts (MERIDIAN_PLAN.md
section 9).

## Consulted

Solstice's `Alert`, `AlertSeverity`, `WeatherSource`, `WeatherWrapper` and `SourceManager` (app code);
Meridian's docs. Not Breezy's `wmosevereweather` or `fpas` source code.
