# Solstice: plan

The decisions in force, the direction, and what's open. Short on purpose: if something here and
another doc disagree, this file wins, and the other doc gets fixed.

- **How we work:** [RULES.md](RULES.md) (read first, every session).
- **What Solstice is for and the design checks:** [DESIGN.md](DESIGN.md). Motion: [MOTION.md](MOTION.md).
  Writing: [WRITING.md](WRITING.md).
- **To-do list:** [ROADMAP.md](ROADMAP.md). **Where things stand right now:** [CONTINUE.md](CONTINUE.md).
- **History:** [LOG.md](LOG.md) (every dated entry, unchanged). The old, long PLAN.md is in
  [archive/PLAN_until_2026-11-22.md](archive/PLAN_until_2026-11-22.md) (reference only, not instructions).

Rule for this file: when a decision changes, edit it here and add a dated line to LOG.md. Don't append
logs, session notes or build reports here.

## Direction (discussed with Marco 2026-11-22, details to agree before any code)

1. **Motion becomes the UI framework, not only the animation library.** The pieces Solstice built
   (page frame and scaffold, card, sheet, toolbar, buttons and header actions, messages, text roles,
   glass, tokens) move into the Motion repo, so Solstice and future apps are mostly configuration.
   Work happens in Motion first (with its lab), then the app imports and configures it.
   Structure, names and order: to propose and agree (see "Open questions").
2. **Forecast keeps today's layout** (Marco, 2026-11-22: "it's more unique"). The layout wireframes
   (design canvas "Solstice layout directions") are not the new Forecast; what stays from them:
   - **A2 is the pattern for cards that aren't full pages:** a partial sheet that grows out of the
     card you tapped, over a dimmed page, with the detail, a chart and that card's own settings (e.g.
     the rain countdown). Full-page windows (postcards) stay for whole places.
   - Checked against Apple's sheet guidance: open at a medium height and expand to full (detents);
     a grabber you can drag, tap and resize with TalkBack; swipe down to close; one sheet at a time
     (a sheet never opens another on top); no Done button when settings apply at once; ✕ at the end
     stays our decided exception (Apple: leading edge).
   - **Every card opens partially first, then to a full page** (Marco, 2026-11-22): medium height,
     drag or scroll up for full height.
   - **Optional, reorderable cards** (Marco, 2026-11-22): you add or remove cards (e.g. wind,
     pressure) and reorder them. **The most important card moves to the top when it matters**
     (today only rain does this, hardcoded in `ForecastPage.kt`; it becomes a general priority rule).
     How your own order and the automatic "moves to the top" combine: to agree.
   - **Forecast keeps** the hero temperature with the sun/moon path and the time; it gets a **spacing
     pass** against Material's spacing guidance using the kit's tokens.
   - **The moon card gets reworked** with the move to `motion-ui`: it becomes a partial sheet built from
     the kit (it doesn't follow the Material work so far).
3. **Skeuomorphism moves into interaction, not looks** (Marco, 2026-11-22): physical feel (springs,
   drag, press, peel, haptics) stays; visuals follow a more standard design. The postcards go first.
4. **Proactive alerts the Gradient Weather way** (Marco, 2026-11-22: "should be used as well"):
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
5. **No coding until the next session (Saturday 1:00 AM)**; this week is research, rules and planning.
   Saturday's plan: [CONTINUE.md](CONTINUE.md), "Next session".

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
| Motion | Separate repo (`INFORMATION89/Motion`, Apache-2.0), a git submodule built with `includeBuild` |

### Look and feel
| Topic | Decision |
|---|---|
| Colour | Material You (wallpaper) colour; sky-blue seed when it's off |
| Type | Google Sans Flex, Material type scale, named hero styles |
| Icons | Material Symbols Rounded, one catalog (`SolsticeIcons`), one weight (500), fill only for on/selected; moon phases are the one exception |
| Glass | `surfaceContainerHigh` at 40 % for the toolbar, buttons and sheets; cards get a thicker card material (decided 2026-11-22, values to pick) |
| Toolbar | One toolbar at shell level: a pages pill (navigation only) plus one accessory (home: Search; window: Pin); contexts are data. **Its current animation stays** (Marco, 2026-11-22) |
| Close button | ✕ at the **end** of the header by default (Marco, 2026-11-22: easier to reach). **Motion option** to swap it with the leading button (✕ at the start, Apple's and Material's placement); mirrored for right-to-left. Back, predictive back and pull-down still close |
| Handles | On the moon card and search, without tap actions (accessibility check pending) |
| Look of cards | Standard, readable design; physical feel through interaction (Direction 3) |

### Framework (decided 2026-11-22)
- **Motion = two modules in one repo:** `motion` (core: springs, gestures, morphs, haptics; Compose
  only) and `motion-ui` (the UI kit: theme, page frame, card, sheet pieces, header buttons, glass;
  Material 3 and Haze). The kit never leaks into the core.
- **Tokens:** the kit defines the names with neutral Material-based defaults; each app passes its own
  values through the kit's theme. Solstice passes its current values.
- **Springs:** speed (fast, default, slow) × bounce (subtle, slight, playful) for movement: **movement
  always has some bounce** (Marco's opinion; a lab switch compares it with none). Effects springs
  (opacity, colour, blur) never bounce; Reduce Motion removes bounce; `wobble` stays for the toolbar bud.
- **Reduce Motion:** Apple's list, built into every Motion piece.
- **Materials:** glass for controls, navigation and sheets; a thicker card material for cards.
- **Motion Lab:** Motion's own app, built on the kit.
- **No typed-in values:** CI check with a reviewed allowlist that only shrinks.

### Motion (full rules: MOTION.md)
- Springs only, from tokens; things morph, never pop; one material per morph; the button becomes the card.
- Nothing blocks input, except page changes (the pager springs itself; a tap first stops it).
- Pages: one page spring (`Motion.momentum`) for swipes and tab taps, bouncing past the ends.
- Morphs: `SpringMorph` (iOS-style). A window grows from and closes into its postcard's live position.
  Opening owns the screen; everything that grows out of a page belongs to that page.
- Haptics: Motion's `Haptic` tokens only; no new haptics without agreeing them.
- Edge blur: Motion's `EdgeBlurDefaults`, bands derived from the chrome over each edge.
- Glass draws with Haze; the edge blur stays Motion's own (measured faster).

### Content
- **Summaries:** one question per summary, the top 1–2 facts from a priority list, nothing extra when
  nothing is useful. Forecast: "What do I need to know about today?" (alert; precipitation start/stop;
  high and when, compared with yesterday if the source has it; extremes only). From 6 PM (adjustable)
  it answers "tonight and tomorrow". This week: rain days, warmest day, coldest night, plus the trend
  and the weekend when useful. Engine built (`shell/Summary.kt`), not wired; every new sentence goes
  to Marco before it ships. Details: archive, "Summaries: what they say".
- **"Rain", "snow" or "precipitation":** rain-only says rain, snow-only says snow, either says
  precipitation; everywhere, This week included.
- **Last updated line** at the bottom of Forecast ("Updated 12 min ago · Open-Meteo", "Couldn't
  update · Try again" only on a real failure).
- **Refresh on a cold start:** the main place, whatever its age (API cost checked: no floor needed).
- **Moon card (F5):** tonight line, three tiles, Motion's `Dial`, close button from the framework.
- Writing: WRITING.md (future tense, natural, "The moon…", never "it"; 12-hour times follow the system).

### Settings and navigation
- Settings on Navigation 3 (N1 done; N2 one stack and N3 adaptive still to do).
- Reordering with the Reorderable library; M3 controls everywhere except the toolbar and postcards.

## Open questions (to settle with Marco)

1. **Card order:** how your own order and "the most important moves to the top" combine.
2. **The card material's values** (opacity, blur), picked in the lab on Saturday.
3. **Spring mapping:** confirm the table in CONTINUE.md "Next session".
4. **Handles without taps:** Apple's grabber is tappable (cycles heights) and works with VoiceOver;
   ours has no tap. Change for partial sheets?
5. **Proactive alerts (Direction 4):** which alerts at first, their defaults and wording, and where you
   switch them on.
6. Launcher icon drawing; distribution (GitHub only, or Obtainium/IzzyOnDroid too); twinkling stars.

## Research queue
1. Gradient Weather's rain countdown on screen: how it looks and how it disappears (the Threads post
   couldn't be opened; a screenshot from Marco would settle it).
2. The page receding behind a sheet, done cheaply (plan in LOG.md, 2026-11-22: dim + recede).
3. Long press alongside drag to reorder (iOS context menu as reference).
4. Proactive channels: Live Updates, daily brief, clothing.

## Far future
- Native Smartspacer Targets and Complications inside Solstice (SDK licence to check first).

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
- `settings.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`: Motion (`includeBuild`
  of the `motion/` submodule since 2026-11-21) and the Haze dependency (2026-11-19).
- `buildSrc/.../AndroidConfig.kt`: minimum Android 10 (2026-11-21).
- Added: `res/values/strings_expressive.xml` (new strings kept out of upstream's `strings.xml`;
  English only so far).
- Added (no upstream equivalent): `.gitattributes` (LF, so Spotless passes on Windows),
  `app/src/main/res/font/google_sans_flex.ttf`, `third_party/fonts/GoogleSansFlex-OFL.txt`,
  `tools/make_icon.py`, `tools/make_symbols.py`, `app/src/main/kotlin/org/breezyweather/ui/expressive/**`.
