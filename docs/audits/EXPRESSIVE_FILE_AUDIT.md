# Expressive UI — file-by-file audit

Status: **Pass 1 — structural, history, and dependency audit**  
Baseline: Breezy Weather `5ec3b9bcb` (2026-11-17), recorded in `UPSTREAM_BASE`.  
Scope: all 59 Kotlin files under `app/src/main/kotlin/org/breezyweather/ui/expressive/`.

This is not a legal clearance and not yet a line-by-line similarity review against every differently named upstream file. No source files are deleted or moved by this audit.

## Classification key

- **KEEP / SOLSTICE** — preserve the product behavior/design; implementation still needs provenance/resource review.
- **REBUILD / DECOUPLE** — keep the feature but replace Breezy data, app, resource, or persistence dependencies.
- **MOTION CANDIDATE** — generic primitive/demo that may belong in Motion or Motion UI after that project's API and maintainers accept it.
- **PLATFORM / LIBRARY** — use a public platform or reviewed dependency API; do not reimplement it.
- **REVIEW** — provenance, assets, or similarity is not established; do not reuse/move until reviewed.

## File-by-file disposition

| File | Observed responsibility / dependency | Initial disposition |
|---|---|---|
| `components/ClockIcon.kt` | Custom Compose Canvas clock; uses Solstice size token | KEEP / SOLSTICE; confirm no borrowed vector/algorithm |
| `components/CollapsingTopBar.kt` | Generic Compose collapsing title/top bar; Material scroll behavior | MOTION CANDIDATE; compare with Material 3 app bar capabilities first |
| `components/ExpressiveChoices.kt` | Choice rows/sheets; Material 3 controls and Solstice tokens | KEEP / SOLSTICE for product choices; use Material components where they satisfy requirements |
| `components/Glass.kt` | Glass modifier built on Haze public API and Solstice opacity/stroke tokens | KEEP integration; Haze is a reviewed dependency, not code to copy |
| `components/IconBadge.kt` | Material Shapes badge; reads `org.breezyweather.R` drawables | REBUILD / DECOUPLE resource IDs; audit drawable origins |
| `components/PressMorph.kt` | Press gesture changes corner radius using Compose animation | MOTION CANDIDATE; check Motion API coverage before keeping a duplicate |
| `components/ScreenCorners.kt` | Reads Android display rounded-corner geometry | PLATFORM; keep behind an Android-specific adapter |
| `components/ScreenPush.kt` | Push/pop transitions using Motion springs | MOTION CANDIDATE; likely app navigation adapter around Motion |
| `components/SheetDialog.kt` | Material bottom sheet with slots explicitly documented as matching upstream `AlertDialogNoPadding` | REVIEW / REBUILD; do not preserve compatibility-shaped API solely to retain inherited call sites |
| `components/SheetHandle.kt` | Generic sheet handle; uses Breezy `R` string and Solstice tokens | MOTION CANDIDATE; replace resource namespace/string ownership |
| `components/SheetShape.kt` | Custom interpolated Compose shape | MOTION CANDIDATE; prove generic use and compare with platform shapes |
| `components/SolsticeCard.kt` | Product card combining glass, sky effects, and weather context | KEEP / SOLSTICE; split only generic card primitive if there is a real second consumer |
| `components/SolsticeIcons.kt` | Embedded ImageVector path catalogue; history says Material Symbols Rounded | REVIEW; verify each icon's official source, variant, licence, and whether maintained icon dependency is preferable |
| `components/SolsticeMotion.kt` | Solstice facade over Motion spring presets and physics | REBUILD / DECOUPLE; migrate call sites to approved Motion public API, then remove facade |
| `components/SolsticeSheetState.kt` | Gesture/detent state built on MotionValue and MotionPhysics | MOTION CANDIDATE; check overlap with Motion's own sheet/gesture primitives |
| `lab/DialDemo.kt` | Motion Dial demonstration | MOVE TO MOTION LAB; no production weather feature |
| `lab/EdgeBlurDemo.kt` | Motion edge-blur demonstration | MOVE TO MOTION LAB |
| `lab/HapticsDemo.kt` | Motion haptics demonstration | MOVE TO MOTION LAB |
| `lab/MotionLabActivity.kt` | Motion demos; extends `BreezyActivity`, uses Breezy theme and `R` | MOVE TO MOTION LAB after replacing activity/theme/resource host; remove from shipped Solstice app |
| `lab/SettleDemo.kt` | Motion pager/settling demonstration | MOVE TO MOTION LAB |
| `lab/SpringMorphDemo.kt` | Motion morph demonstration | MOVE TO MOTION LAB |
| `lab/ToolbarDemo.kt` | Solstice toolbar and Motion interaction demo | Split: generic Motion demo to Motion Lab; Solstice toolbar behavior remains documented/tested in Solstice |
| `reorder/ReorderActivity.kt` | Reorder settings; BreezyActivity, EventBus, SettingsManager, BaseEnum, Breezy theme and widget classes | REBUILD / DECOUPLE; app settings screen and persistence must be Solstice-owned |
| `reorder/ReorderList.kt` | Generic reorderable Compose list; Calvin Reorderable library, Motion haptics, Breezy resources | KEEP feature; use library API, replace resources, extract generic component only if Motion UI accepts it |
| `search/Cities.kt` | Local city model/search, bundled city dataset, conversion to Breezy Location, SharedPreferences recents | REBUILD / DECOUPLE; review dataset provenance; use Solstice Place model and app-owned persistence |
| `search/SearchOverlay.kt` | Custom search UI; Breezy Location, SearchViewModel, LoadableLocationStatus, R, Motion | KEEP search UX; rebuild state/repository/viewmodel contracts and resource references |
| `shell/ForecastPage.kt` | Main forecast composition; Breezy Location/weather model, R, Motion edge blur | KEEP product UI; replace weather contract with Solstice presentation model from Meridian |
| `shell/Format.kt` | Temperature/feels-like/date formatting; Breezy Location, SettingsManager, unit types, R | REBUILD / DECOUPLE; own formatting preferences and map Meridian quantities to app display models |
| `shell/Freshness.kt` | Last-updated freshness labels; Breezy R strings | KEEP behavior; replace string resources and feed from Meridian update metadata |
| `shell/Messages.kt` | Transient app messages/snackbar-like host; Motion, Breezy R strings | KEEP product behavior; replace resource ownership; generic host may be MOTION CANDIDATE |
| `shell/MoonCard.kt` | Moon sheet/card, astronomy presentation; Breezy Location/weather models, Motion, SunCalc | KEEP / SOLSTICE; keep SunCalc via reviewed API; replace weather/place input contracts and audit moon texture |
| `shell/MoonPhaseIcons.kt` | Moon phase painter selection and drawing; Breezy R drawables | KEEP / SOLSTICE; audit every drawable and replace Breezy resource IDs |
| `shell/MoonRenderer.kt` | Moon bitmap/shader renderer; Breezy R texture, Android RuntimeShader | KEEP / SOLSTICE + PLATFORM; audit texture source/licence and isolate Android shader implementation |
| `shell/MoonSky.kt` | Moon orientation/position calculations via SunCalc | KEEP / SOLSTICE; use SunCalc API, add independent astronomy tests |
| `shell/MoonVisibility.kt` | Moon visibility rules; Breezy Hourly model, SkyVisibility, SunCalc | REBUILD / DECOUPLE data input; keep independently specified visibility rules |
| `shell/PageFrame.kt` | Reusable page frame/header/scaffold; Breezy Location, R, Motion, glass | Split: generic frame primitives are MOTION CANDIDATE; app page title/visit semantics stay SOLSTICE |
| `shell/PlaceTime.kt` | Local time and time-offset labels; Android/Java time APIs, Breezy R strings | KEEP behavior; replace resources; test DST/locale edge cases |
| `shell/PlaceWindow.kt` | Full-screen place/postcard window; Breezy Location, R, BreezyWeatherTheme, Motion | KEEP product experience; replace place model/theme/resources and app navigation integration |
| `shell/PlacesPage.kt` | Saved places, star, reorder, swipe actions; Breezy Location, R, Motion | KEEP product behavior; rebuild against Solstice Place repository and models |
| `shell/Postcard.kt` | Place postcard visuals, transitions, custom paper/texture effects; Breezy Location and R | KEEP / SOLSTICE; audit texture/asset provenance and decouple place model |
| `shell/RainCard.kt` | Rain outlook computation and presentation; Breezy Location/weather/minutely/hourly models, R, unit classes | REBUILD / DECOUPLE; move meteorological interpretation into Meridian where appropriate, keep card/presentation in Solstice |
| `shell/ShellNavigation.kt` | Pager navigation, title handoff, page blur; Motion and Compose | KEEP app navigation policy; Motion owns reusable motion primitives |
| `shell/ShellPrefs.kt` | Feels-like display preference; SharedPreferences | REBUILD / DECOUPLE; move to Solstice settings/DataStore abstraction |
| `shell/SolsticeShell.kt` | App shell/controller; Breezy MainActivityViewModel, SearchViewModel, SourceManager, legacy Location methods, settings dialogs, R | REBUILD / DECOUPLE; this is the primary Breezy seam and must eventually depend only on Solstice use cases/repositories, Meridian and Motion public APIs |
| `shell/Summary.kt` | Weather summary rules; Breezy WeatherCode definitions | REBUILD / DECOUPLE domain codes; specify summary semantics independently over Solstice/Meridian weather types |
| `shell/Toolbar.kt` | Product toolbar, custom morphing tab/pill; Motion, R, sky theme | KEEP product behavior; extract only generic motion/UI mechanics after Motion UI review |
| `shell/ToolbarMotion.kt` | Solstice toolbar spring tuning and mutable tokens | KEEP product-specific tuning; use Motion's public spring API |
| `shell/Visits.kt` | Visited places and time-distance logic; SharedPreferences | REBUILD / DECOUPLE persistence and place IDs; product behavior stays Solstice |
| `shell/WeekPage.kt` | Seven-day outlook and weekly summary; Breezy Location/daily forecast/temperature/weather-code types, R | KEEP presentation; replace domain inputs with Solstice/Meridian models |
| `sky/AnimatedSkyBackground.kt` | Animated sky shader; Android RuntimeShader, lifecycle, power state, Motion ambient clock | KEEP / SOLSTICE + PLATFORM; isolate Android implementation, preserve low-power/lifecycle behavior |
| `sky/SkyBackground.kt` | Custom sky gradients, baked textures, stars and grain | KEEP / SOLSTICE; audit algorithm/inspiration and rendering performance |
| `sky/SkyEffects.kt` | Weather/sky visual effect selection | KEEP / SOLSTICE; generic drawing may be reusable but weather semantics remain app-specific |
| `sky/SkyPalette.kt` | WeatherCode-to-sky palette and sun-position state; Breezy WeatherCode | REBUILD / DECOUPLE weather code mapping; palette and visual design remain Solstice-owned |
| `sky/SkySystemBars.kt` | Android system-bar icon contrast and window configuration | PLATFORM; keep as an Android app adapter |
| `sky/SkyVisibility.kt` | Forecast cloud/sun visibility rules; Breezy Hourly model, SunPosition | REBUILD / DECOUPLE data input; keep product wording/rules after independent specification |
| `theme/ExpressiveTheme.kt` | Material Expressive/dynamic color theme; Breezy color resources and R | KEEP design direction; replace resource namespace and define Solstice-owned color source |
| `theme/ExpressiveType.kt` | Typography and Google Sans Flex font references; Breezy R | KEEP design direction; audit actual font binaries and redistribution licence, replace resource IDs |
| `theme/SolsticeMotionScheme.kt` | Material MotionScheme adapter over Motion | KEEP adapter if API remains valid; Motion owns underlying physics |
| `theme/Tokens.kt` | Solstice spacing, sizing, palette, emphasis, corners and hero type | KEEP / SOLSTICE; preserve design decisions, extract generic tokens only by explicit Motion UI agreement |

## Confirmed historical evidence

- `UPSTREAM_BASE` pins Breezy at `5ec3b9bcb`.
- The upstream recursive tree at that commit has no same-path or same-basename match for these 59 files.
- Solstice commit `98647ef` added `Tokens.kt` and replaced scattered literals in existing Solstice UI files.
- Solstice commit `d2c787a` added `SolsticeCard.kt`, `PageFrame.kt`, and `Toolbar.kt`; it split/changed the app shell and forecast pages.
- Solstice commit `bdd587c` added the Solstice Motion facade and rewrote the moon sheet interaction around Motion.
- Solstice commit `0b84c43` replaced the initial custom glass renderer with Haze integration.
- Solstice commit `d658ea4` imported/updated the Material Symbols Rounded icon catalogue in `SolsticeIcons.kt`; the exact source assets and licence still need to be recorded.
- Upstream `MainActivityViewModel.kt`, `SearchViewModel.kt`, and `Location.kt` remain the legacy integration points consumed by Solstice's new UI.

## Immediate blockers before any move/removal

1. Audit `SolsticeIcons.kt` path data against the official Material Symbols source and record exact licence/attribution.
2. Audit font binaries referenced by `ExpressiveType.kt`, the moon texture used by `MoonRenderer.kt`, city dataset, and all expressive string/drawable resources.
3. Write Solstice-owned `Place`, `WeatherPresentation`, forecast, alert, and search contracts before changing the UI.
4. Replace `SolsticeShell.kt` dependencies on Breezy ViewModels, SourceManager, and settings with app-owned use cases/repositories and Meridian public contracts.
5. Move `lab/` to Motion Lab only after its host no longer depends on Breezy.
6. Replace `ReorderActivity.kt` and app settings integration; do not delete reorder functionality.
7. Rebuild `SheetDialog.kt` from an independent behavior specification; its current documentation explicitly says it matches upstream's `AlertDialogNoPadding` slots.

**No code removal is authorized by this audit.** Each file remains in place until its replacement, tests, data migration, and provenance review are complete.
