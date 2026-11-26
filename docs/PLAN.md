# Solstice: plan

The decisions in force, the direction, and what's open. Short on purpose: if something here and
another doc disagree, this file wins, and the other doc gets fixed.

- **How we work:** root [AGENTS.md](../AGENTS.md) (agent entry point), [RULES.md](RULES.md) (Solstice rules), and [AI_COST_POLICY.md](AI_COST_POLICY.md) (AI, tools, CI and verification costs). Read these before each coding session.
- **What Solstice is for and the design checks:** [DESIGN.md](DESIGN.md). Motion: [MOTION.md](MOTION.md).
  Writing: [WRITING.md](WRITING.md).
- **To-do list:** [ROADMAP.md](ROADMAP.md). **Where things stand right now:** [CONTINUE.md](CONTINUE.md).
- **Documentation/CI audit:** [MODERNIZATION_CHECKLIST.md](MODERNIZATION_CHECKLIST.md) (active checklist; tracking only, not a product-decision authority).
- **Privacy evidence:** [PRIVACY_DATA_MAP.md](PRIVACY_DATA_MAP.md) is an internal code/data-flow audit, not the public privacy policy; the public notice remains under review.
- **History:** [LOG.md](LOG.md) (every dated entry, unchanged). The old, long PLAN.md is in
  [archive/PLAN_until_2026-11-22.md](archive/PLAN_until_2026-11-22.md) (reference only, not instructions).

Rule for this file: when a decision changes, edit it here and add a dated line to LOG.md. Don't append
logs, session notes or build reports here.

## Direction (discussed with Marco 2026-11-22, details to agree before any code)

1. **Forecast keeps today's layout** (Marco, 2026-11-22: "it's more unique"). The layout wireframes
   (design canvas "Solstice layout directions") are not the new Forecast; what stays from them:
   - **A2 is the pattern for cards that aren't full pages:** a partial sheet that grows out of the
     card you tapped, over a dimmed page, with the detail, a chart and that card's own settings (e.g.
     the rain countdown). Full-page windows (postcards) stay for whole places.
   - Checked against Apple's sheet guidance: open at a medium height and expand to full (detents);
     a grabber you can drag, tap and resize with TalkBack; swipe down to close; one sheet at a time
     (a sheet never opens another on top); no Done button when settings apply at once; ✕ at the end
     stays our decided exception (Apple: leading edge).
   - **Every card opens one sheet with two heights** (Marco, 2026-11-22; refined 2026-11-23 after
     Apple's guidelines): medium first (the answer and its timeline), drag, scroll or tap the grabber
     for large (details, explainer, sources). No separate full page; the map stays full screen. The
     grabber is tappable (cycles heights) and has a TalkBack action. Per-card contents: PROPOSALS.md.
   - **Optional, reorderable cards** (Marco, 2026-11-22): you add or remove cards (e.g. wind,
     pressure) and reorder them. **The most important card moves to the top when it matters**
     (today only rain does this, hardcoded in `ForecastPage.kt`; it becomes a general priority rule).
     **By default the important, relevant card moves to the top** while it matters, then goes back to its
     place in your order (Marco, 2026-11-22). "By default" means it can be switched off.
   - **Forecast keeps** the hero temperature with the sun/moon path and the time; it gets a **spacing
     pass** against Material's spacing guidance using Solstice's design tokens.
   - **The moon card is a Solstice feature:** its content, layout, visual treatment, and sheet behavior
     remain owned and implemented by the app, using only reusable APIs already available to it.
2. **Skeuomorphism moves into interaction, not looks** (Marco, 2026-11-22): physical feel (springs,
   drag, press, peel, haptics) stays; visuals follow a more standard design. The postcards go first.
3. **Proactive alerts the Gradient Weather way** (Marco, 2026-11-22: "should be used as well"):
   - Alerts are things **you choose to watch**, each with its own threshold: imminent rain or snow
     (a heads-up 10, 15 or 20 minutes before, with a **live countdown**), frost, UV above your level,
     pollen per allergen, air quality dropping, rapid pressure changes, official warnings, an evening
     summary of tomorrow, and what to wear.
   - **Cooldowns** so the same alert never repeats; the countdown ends by itself when the rain starts
     or the chance passes.
   - Because you switch it on, the rain countdown counts as something you started: that's what lets it
     be an Android Live Update under Google's rules (with a way to stop it), not just a notification.
   - Official warnings open in full inside Solstice, coloured by severity.
   - Which alerts, their wording, defaults and where you set them up: to propose and agree. Source:
     gradientweather.app and its Play Store listing (2026-11-22).
4. **This week becomes "Outlook"** (Marco, 2026-11-23): a proactive planning page, like Acme Weather's
   approach, not just a list of days. Examples: the best weekend day to be outside, notable days ahead
   (heat, a cold front, a rainy stretch), good evenings for the sky. The day list stays. What it answers,
   and how, to propose to Marco.

## Decisions in force

### Project
| Topic | Decision |
|---|---|
| What | Fork of Breezy Weather (no shared git history; base in `UPSTREAM_BASE`), redesigned |
| Name and ID | Solstice, `io.github.information89.solstice` (required: upstream's licence forbids the Breezy name) |
| Minimum Android | 10 (API 29), since 2026-11-21 |
| Flavor shipped | `basic`; never build with `-Pbreezy` |
| Upstream sync | Patch-based (CONTINUE.md); edits to upstream files kept small and listed below |
| Weather sources | Worldwide + North America; AccuWeather disabled for now; current-location name from Android's geocoder |
| Widgets | Upstream's for now; Solstice widgets later |

### Look and feel
| Topic | Decision |
|---|---|
| Colour | Material You (wallpaper) colour; sky-blue seed when it's off |
| Type | Google Sans Flex, Material type scale, named hero styles |
| Icons | Interface: Material Symbols, one catalog (`SolsticeIcons`), one weight (500), fill only for on/selected; **a setting for Rounded, Outlined or Sharp** (decided 2026-11-22; Rounded default). Weather conditions: **Material Symbols for now** (decided 2026-11-23: no animated icons); later option: **our own set built from Material Symbols' weather shapes** with a Solstice colour layer (two or three tones; Apache 2.0 allows it). Contrast checked on every sky. Moon phases stay the one exception. Coverage and gaps: docs/research/ICONS.md |
| Glass | `surfaceContainerHigh` at 40 % for the toolbar, buttons and sheets; cards get a thicker card material (decided 2026-11-22, values to pick) |
| Toolbar | One toolbar at shell level: a pages pill (navigation only) plus one accessory (home: Search; window: Pin); contexts are data. **Its current animation stays** (Marco, 2026-11-22) |
| Close button | ✕ at the **end** of the header by default (Marco, 2026-11-22: easier to reach). An app setting may offer the leading position; mirror it for right-to-left. Back, predictive back and pull-down still close |
| Handles | On the moon card and search, without tap actions (accessibility check pending) |
| Look of cards | Standard, readable design; physical feel through interaction (Direction 3) |

### App interaction ownership
- Solstice owns its interaction design, toolbar, page transitions, cards, sheets, accessibility
  behavior, reduced-motion experience, haptics policy, and design tokens.
- Keep the current animated toolbar and its bud as Solstice product behavior; do not remove or redesign
  them without an explicit product decision.
- Keep page movement, sheet behavior, touch feedback, and content transitions consistent across the app.
- Detailed app motion principles are in [MOTION.md](MOTION.md).
### Places and travel (decided 2026-11-22)
- **Home is your current location** by default. Without location access, onboarding asks you to
  choose a place, which becomes home. Using a fixed place instead lives in Settings › Locations.
- **No star.** Places is a plain saved list (reorderable).
- **Travel swaps automatically:** away from home (e.g. home Mexico City, you're in Tijuana), Forecast
  shows where you are and your home place moves to the top of Places marked "Home" with its time;
  it swaps back when you return.
- **Search opens a preview** with Cancel and Add (Apple Weather's pattern). **"Visiting" only marks a
  preview** of a place you haven't added.
- **A place's local time** shows wherever its time differs from your phone's (saved or previewed),
  from one place-time component.

### Forecast card catalog (decided 2026-11-23; thresholds are tokens, tuned with real use)
Values follow the unit settings; a card is skipped when the source has no data for it.

| Card | Small view | Moves to the top when | Default |
|---|---|---|---|
| Alerts | Official warning title and severity | Always, while one is active | On, can't be turned off |
| Rain and snow | Start or stop time, chance, amount | It meets the rain rule within 2 hours | On, appears when it matters |
| Hourly | Next 6 hours: temperature and icon | Never (the anchor card) | On |
| UV | Index and "high until 4 PM" | Index 6 or above (WHO "high") | On, appears when it matters |
| Air quality | Index and level | **Anything worse than "good"** | On, appears when it matters |
| Wind | Speed, gusts, direction | Gusts of 50 km/h or more | On, appears when it matters |
| Feels like | Feels-like temperature and why | 4° or more from the real temperature, or 35° and above | Off |
| Humidity and dew point | Humidity, dew point, "muggy" or "dry" | Dew point 20° or above | Off |
| Pressure | Value and trend | 3 hPa or more change in 3 hours | Off |
| Visibility | Distance | Under 1 km | Off |
| Pollen | Main allergen and level | High for any allergen | Off |

- Default order: Alerts, Rain, Hourly, then your own order. "Appears when it matters" cards are hidden
  on normal days; any card can be set to always show.
- Several important at once: alerts, rain, wind, UV, air quality.
- **Sun and moon are not cards** (Marco, 2026-11-23): the sun gets its own button in the hero, like the
  moon's, opening a sun sheet (sunrise, sunset, daylight, UV through the day).
- **What to wear** (Marco, 2026-11-23): a Forecast card that **always shows**; what it covers depends on the
  time (today by day, tomorrow morning from the evening switch). Can also be one of the alerts you choose
  (Direction 4).
- **UV** (Marco, 2026-11-23): the UV card opens the sun sheet with UV first. The card (and the sun button)
  **turns red when UV is very high** (WHO 8 and above), with an icon or word too, never colour alone. The UV
  part of the sheet: today's index as a number, a **timeline of the next hours**, and **what to do** for
  the current level (e.g. sunscreen, shade at midday), following WHO's guidance per level.
- **What to wear suggests sunscreen** from the same UV rule (one shared rule, like rain).

### In-app messages (decided 2026-11-22)
- Several at once: Solstice owns the message queue and presentation policy. The app may choose
  queue-and-merge (priority order, repeats merge, one morphs into the next) or stacking above the toolbar.
- Up to **two lines** (the action moves below when it doesn't fit); always an icon, chosen by the
  message's kind (never matched from its text).
- **Accessibility:** follows Android's "Time to take action" timeout; a message with an action waits
  until dismissed when TalkBack is on.

### Solstice weather experience
Solstice owns the app experience built around weather information: saved places, permissions, refresh scheduling, settings, notifications, source presentation, user consent, and the practical recommendations shown to people. Clothing and footwear, umbrella and sunscreen advice, activity suggestions, outdoor planning, personal preferences, alert thresholds, cooldowns, wording, and delivery are Solstice features.


### Rain and alerts (decided 2026-11-22)
- **One "precipitation is likely" rule** for the rain card, notifications and Live Updates: hourly at
  least 40 % chance and 0.2 mm; minute by minute at least 0.1 mm/h for two steps in a row; snow counts;
  at most one alert per 3 hours for the same rain. Tokens, tuned with real use. Replaces upstream's
  "any radar trace" notification rule.

### Professional feel (decided 2026-11-22)
- In the app: Data sharing, Gadgetbridge and Debug move under "Advanced"; Settings shaped like Apple's
  and Google's weather apps.
- Docs and README written as a product's (no plugin side plans or hobby wording).
- Store-facing text (name, description, About) written like a real product.
- Solstice keeps its data/provider and upstream-derived implementation details behind app-facing
  boundaries, and progressively replaces inherited UI, settings, and widgets with its own product
  experience. Preserve all applicable upstream copyright, licence, and third-party notices.

### App interaction principles (full rules: MOTION.md)
- Interactions should respond promptly, remain interruptible, and never swallow unrelated input.
- Surfaces should retain a clear visual relationship to the control or content that opened them.
- The Solstice toolbar and its bud animation remain app-owned product behavior.
- Keep haptics, blur, glass, page movement, and reduced-motion behavior consistent with Solstice's
  design decisions.

### Content
- **Summaries:** one question per summary, the top 1–2 facts from a priority list, nothing extra when
  nothing is useful. Forecast: "What do I need to know about today?" (alert; precipitation start/stop;
  high and when, compared with yesterday if the source has it; extremes only). From 6 PM (adjustable)
  it answers "tonight and tomorrow". This week: rain days, warmest day, coldest night, plus the trend
  and the weekend when useful. The app-side summary logic exists in `shell/Summary.kt` but is not yet
  wired to all intended pages. Every new sentence goes through the app's writing approval process.
- **"Rain", "snow" or "precipitation":** rain-only says rain, snow-only says snow, either says
  precipitation; everywhere, This week included.
- **Last updated line** at the bottom of Forecast ("Updated 12 min ago · Open-Meteo", "Couldn't
  update · Try again" only on a real failure).
- **Refresh on a cold start:** the main place, whatever its age (API cost checked: no floor needed).
- **Moon card:** tonight line, three tiles, and a Solstice-owned interactive dial and close behavior.
- Writing: WRITING.md (future tense, natural, "The moon…", never "it"; 12-hour times follow the system).

### Settings and navigation
- Settings on Navigation 3 (N1 done; N2 one stack and N3 adaptive still to do).
- Reordering for app-managed lists; M3 controls everywhere except the toolbar and postcards.

## Open questions (to settle with Marco)

1. **Card catalog leftovers:** where the switch for "important cards move to the top" lives.
2. **Outlook's content:** which proactive answers it gives (Direction 6); proposal in PROPOSALS.md.
3. **The card material's values** (opacity, blur and contrast), reviewed as part of Solstice's own UI design.
4. **Proactive alerts (Direction 4):** which alerts at first, their defaults and wording, and where you
   switch them on. Proposal and wording: PROPOSALS.md section 4, WRITING.md section 9 (2026-11-23).
5. **Optional sources** (Marco, 2026-11-23: with very local sources, don't bundle everything). Facts:
   cutting 40 source packages saved 1.36 MB on the debug APK and less after R8 (release is 15.1 MB
   total, mostly libraries), so code size isn't the problem. Sources are never contacted unless
   picked, and settings already hide sources that don't cover a location. Ways to make them
   downloadable: separate plugin apps (each its own install, versioning and signing), or Play
   Feature Delivery (works only through Google Play, not GitHub or Obtainium). Proposal: keep them
   built in, but "off until relevant": a local source (SMN, CDMX network, SGIRPC) appears only for
   places it covers, starts off, and is offered once ("Mexico City has a local network, use it?").
   Revisit plugins only if the source list grows a lot. **Decided 2026-11-23:** all built in, off
   until relevant where needed, based on the app's verified location coverage.
6. Launcher icon drawing; distribution (GitHub only, or Obtainium/IzzyOnDroid too); twinkling stars.

## Solstice product research queue
1. Gradient Weather's rain countdown on screen: how it looks and how it disappears.
2. The page receding behind a sheet, done cheaply (plan in LOG.md, 2026-11-22: dim + recede).
3. Long press alongside drag to reorder (iOS context menu as reference).
4. Proactive channels: Live Updates, daily brief, clothing.
5. Card explainers (swipe up past the details): short Solstice texts, with "Read more" from Wikipedia
   (CC BY-SA: credit and link, never silently edited).
## Upstream files touched
- `app/src/main/kotlin/org/breezyweather/ui/theme/compose/Theme.kt`: `BreezyWeatherTheme` delegates
  to `ExpressiveTheme`; upstream's placeholder purple schemes removed (F1).
- `app/src/res_fork/**`: launcher icon (F0; fork-only resources).
- `ui/settings/preference/composables/SwitchPreference.kt`: check/close icon in the switch thumb (F2).
- `ui/settings/preference/composables/ListPreference.kt`: inline button group for small choices;
  bottom sheet replaces the list dialog (old dialog code removed) (F2).
- `ui/settings/preference/composables/Preference.kt`: leading icon → `IconBadge` (F2).
- `ui/common/widgets/insets/FitSystemBarComposeWrappers.kt`: `LargeTopAppBar` →
  `LargeFlexibleTopAppBar` (F2).
- `ui/main/MainActivity.kt` + `res/layout/activity_main.xml` (phone layout): host the Solstice shell in
  a `solstice_shell` ComposeView, hide/show upstream's HomeFragment as "More details", route
  `setManagementFragmentVisibility` to the Places page (F3).
- `ui/common/widgets/Material3Widgets.kt`: `Material3ExpressiveCardListItem` morphs its inner corners
  while pressed (F2).
- `ui/settings/preference/composables/MultiListPreference.kt`: `AlertDialogNoPadding` → `SheetDialog`;
  row icons → `IconBadge` (also in `ListPreference.kt`, `SwitchPreference.kt`) (F2).
- `common/utils/helpers/IntentHelper.kt`: the three reorder screens open `ReorderActivity` (F2).
- `AndroidManifest.xml`: `ReorderActivity` entry (F2).
- **Cut (2026-11-18):** deleted packages `wallpaper/` (live wallpaper) and
  `remoteviews/presenters/notification/` (notification widget), `res/xml/live_wallpaper.xml`.
  References removed in `AndroidManifest.xml`, `background/receiver/BootReceiver.kt`,
  `ui/settings/compose/ModulesSettingsScreen.kt`; `sources/RefreshHelper.kt` keeps
  `updateNotificationIfNecessary` / `updateWallpaperIfNecessary` as no-op stubs so their callers stay
  unchanged. Their strings and settings keys are left in place (harmless).
- **Cut (2026-11-18), weather sources:** deleted 40 source packages (European, Asian/Pacific and
  African national services, incl. Infoplaza and Baidu IP) from `main`, `src_nonfreenet` and
  `src_freenet`, plus `sources/FmiServiceStub.kt` and `sources/InfoplazaServiceStub.kt`. Only
  `sources/SourceManager.kt` referenced them (imports, constructor, lists; national list now typed
  `persistentListOf<Source>`). Their strings, API-key build fields and settings keys are left in
  place (harmless). Also removed the live wallpaper's leftover unit test
  (`test/.../wallpaper/FramePacingTest.kt`).
- `sources/SourceManager.kt`: default address lookup is Android's geocoder when present (Nominatim
  otherwise).
- `gradle.properties`: fork links filled in (`app.report_issue`, source, releases, install, privacy,
  GitHub org/repo/prefix).
- `app/build.gradle.kts`: `profile` build type (release code, debug-signed, `.profile`) (2026-11-19).
- `buildSrc/.../AndroidConfig.kt`: minimum Android 10 (2026-11-21).
- Added: `res/values/strings_expressive.xml` (new strings kept out of upstream's `strings.xml`;
  English only so far).
- Added (no upstream equivalent): `.gitattributes` (LF, so Spotless passes on Windows),
  `app/src/main/res/font/google_sans_flex.ttf`, `third_party/fonts/GoogleSansFlex-OFL.txt`,
  `tools/make_icon.py`, `tools/make_symbols.py`, `app/src/main/kotlin/org/breezyweather/ui/expressive/**`.
