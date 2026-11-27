# PLAN.md as it was until 2026-11-22 (archive)

Kept for reference only. **Superseded:** the decisions in force are in [../PLAN.md](../PLAN.md);
the dated log is in [../LOG.md](../LOG.md). Nothing here is a current instruction.


**Solstice** (working name until 2026-11-18: Breezy Expressive) is a fork of [Breezy Weather](https://github.com/breezy-weather/breezy-weather) redesigned in full
**Material You Expressive**: wallpaper (dynamic) colors, the M3 Expressive components, shapes and
motion. Weather data, sources, widgets and everything else stay Breezy Weather's.

Status: **F0, F1 and F2 done** (2026-11-18); shell, search, postcard window, moon sheet, glass and edge blur built (2026-11-19). Next: design tokens and the motion framework (see "Design system foundations" and "Motion framework"). Upstream base: `5ec3b9b` (2026-11-17).

## What we start from

Upstream is already Compose with `material3` 1.5.0-rc01, so the Expressive APIs are available
without a migration. It uses plain `MaterialTheme` and no Expressive API yet.

| Area | Today | Size |
|---|---|---|
| Theme | `ui/theme/compose/Theme.kt`: `MaterialTheme`, dynamic color on Android 12+ | 71 lines |
| Settings screens | Compose: 11 screens in `ui/settings/compose/` | ~2.8k lines |
| Settings building blocks | Compose: `ui/settings/preference/composables/` (switch, list, multi-list, edit text, time picker, section) | ~1.9k lines |
| Reorder screens | Views + RecyclerView: card display, daily/hourly trend display | 3 activities + adapters |
| Location list | `ui/main/fragments/ManagementFragment.kt`: Compose shell + RecyclerView (`LocationAdapter`, `item_location_card.xml`, drag/swipe via `ItemTouchHelper`) | ~1k lines |
| Location search | `ui/search/SearchActivity.kt`: Compose | 391 lines |

## Decisions

| Topic | Decision (2026-11-17) |
|---|---|
| Name | **Solstice** (decided 2026-11-18; replaces the working name Breezy Expressive) |
| `applicationId` | `io.github.information89.breezyexpressive` (debug: `.debug` suffix, as upstream) |
| Flavor shipped | `basic` (all sources) |
| Code in this repo | Yes, as a snapshot of upstream **without its history** (29 MB; the full history is ~1 GB) |
| Weather-App-Experiment design (sky, glass, sun path) | Dropped: pure Material You Expressive |
| Fallback color (dynamic color off / Android < 12) | Sky blue seed (2026-11-18) |
| Typeface | Google Sans Flex, bundled; emphasized headlines via weight (2026-11-18; check license before bundling) |
| Shapes | Full Expressive: large corners, pill buttons, morph on press/selection (2026-11-18) |
| Launcher icon | Sun partly wrapped by a wind swirl, plus a monochrome layer (2026-11-18) |

## Rename to Solstice (decided 2026-11-18)
- **Why it's required, not just cosmetic:** upstream's license terms say a fork must not use the
  Breezy name. In release builds, `BreezyWeather.isImpersonatingBreezyWeather` is true when the app
  name **or** the applicationId contains "breezy"; the app then hides all locations and shows a
  blocking "License compliance" dialog that closes the app (`ui/main/dialogs/LicenseComplianceDialog`).
  Debug builds are exempt. Today both our name and ID contain "breezy", so **release builds are
  unusable until the rename.**
- **What changes:** `brand_name` in `res_fork/values/strings.xml` → "Solstice";
  `applicationId` → e.g. `io.github.information89.solstice` (a new app ID means a separate install:
  settings and locations on the phone don't carry over; fine at this stage); README, CLAUDE.md,
  CONTINUE.md, PRIVACY.md/INSTALL.md wording; `app.github.release_prefix`; optionally the GitHub
  repo name (GitHub redirects the old URL). The code package `org.breezyweather` stays (keeps
  upstream merges easy; the license check doesn't look at it).
- Check before release: no confusing clash with other apps called Solstice in the stores.

## Design direction

- **Color:** dynamic (wallpaper) color by default, everywhere; a tuned seed palette when it's off or
  on Android < 12. No custom sky/glass look in this fork (the Weather-App-Experiment design is dropped).
- **Theme:** `MaterialExpressiveTheme` with `MotionScheme.expressive()` (spring motion), Expressive
  shapes and a type scale with emphasized styles for headlines.
- **Components:** `LargeFlexibleTopAppBar`, segmented list groups (rounded group, small inner
  corners, like Android 16 Settings), connected `ButtonGroup`s for small choices, `Switch` with
  icons, `LoadingIndicator`, Expressive `SearchBar`, `FloatingToolbar`/FAB menu for actions,
  shape morphing on press and selection.
- **Days page (added 2026-11-18):** the multi-day forecast moves to **its own page to the left of
  Forecast**: swipe with the finger moving right from Forecast to reach it. Pages, left to right:
  **Days ← Forecast → Locations**; Forecast stays the screen the app opens on. Decided 2026-11-18: the pill
  toolbar has **three tabs: Days · Forecast · Places**. A small "next days" preview card appears on
  Forecast **only near the end of the day** (evening/night, when tomorrow matters more than the
  rest of today); tapping it opens the Days page. Exact switch-over time to tune (e.g. after
  sunset or after the last daytime hour).
- **Navigation (updated 2026-11-18):** two pages side by side. **Forecast is the first screen**;
  **Locations is to its right**: swipe with the finger moving left to reach it, right to come back.
  The page follows the finger and can reverse mid-swipe. A floating pill toolbar (M3 Expressive
  `HorizontalFloatingToolbar`) **stays at the bottom** with the two tabs for tapping. Replaces
  upstream's pushed Locations page (phones) and drawer (tablets).
  - Consequence: the sideways swipe on Forecast now moves between pages, so it no longer switches
    between locations (upstream behaviour). You change location by tapping a card in Locations,
    which slides back to Forecast showing it.
- **Forecast screen top (2026-11-18):** **nothing at the top but the location's title.** No toolbar
  buttons, no menu, **no "edit this location" button**. For the current (GPS) location the title is
  the place's real name with a small current-location icon next to it, not "Current location".
- **Editing a location (2026-11-18):** in the Locations list, swipe a location card sideways to
  reveal **Edit** (location details: name, weather sources…); swipe the other way to delete with
  undo. Edit opens upstream's location settings, restyled as in F2.
- **Motion: fluid and interruptible, iOS-like refinement.** Priority whenever we animate:
  - Every animation can be interrupted and retargeted mid-flight, keeping its current velocity; no
    animation blocks input or has to finish first.
  - Springs, not fixed-duration tweens, for anything the user can touch (`spring()` /
    `MotionScheme.expressive()`, `Animatable.animateTo`, `animate*AsState`); tweens only for things
    nobody can interrupt (e.g. a fade-in of loaded data).
  - Gesture-driven where possible: transitions track the finger (swipes, predictive back, drag to
    reorder) and settle from the release velocity.
  - Shared elements between screens instead of cross-fades where the same thing appears in both.
  - 60/120 Hz smooth: no layout work during animations, measure with frame timing on the Pixel 7.
- **Translucent overlays (added 2026-11-18):** surfaces that sit on top of other surfaces (bottom
  sheets, the floating toolbar, dialogs, menus, top bar when content scrolls under it) are
  translucent, letting what's behind show through, instead of opaque.
  - **Reference:** the settings page of the **Transit Go for Smartspacer** app, which is
    translucent. The user wants that implementation copied. To do before building: find its
    source and **check its license** (Smartspacer plugins are typically GPL-3.0; this repo is
    LGPL-3.0, so code may not be copyable as is; if not, re-implement the same look ourselves).
  - Likely technique: blur of the content behind (`RenderEffect`/`Modifier.blur`, Android 12+)
    plus a tinted, partly transparent surface color; plain translucent tint as fallback on
    Android < 12 and when the phone is in battery saver.
  - Must stay cheap: blur costs GPU time, so measure frame times on the Pixel 7 when adding it
    (see Performance).
- **Performance first (added 2026-11-18):** the app must feel fast before new features land. Test on
  a release (R8-minified, non-debuggable) build, not debug, when judging smoothness.
- **Accessibility first:** font scale up to 200 %, TalkBack order and labels, 48 dp targets, RTL.

## Release 1 scope: Settings, Locations and the tab toolbar

Out of scope for release 1: the main weather screen's content (it sits in the Forecast tab unchanged),
widgets, notifications, live wallpaper.

### F0: Fork setup
1. **Snapshot:** one commit with upstream's files at the base commit (now `5ec3b9b`), no history.
   Record the base in `UPSTREAM_BASE` (commit hash + date).
2. **Identity** (upstream's fork rules):
   - `res_fork/values/strings.xml`: `brand_name` = "Breezy Expressive".
   - New launcher icon in the `res_fork` mipmaps, with a monochrome layer for themed icons.
   - `applicationId` → `io.github.information89.breezyexpressive`; `namespace` stays
     `org.breezyweather` (keeps upstream code and diffs untouched).
   - Never build with the `breezy` flag; README says unofficial, not affiliated.
3. **Build here:** JDK 21 (as upstream) + Android SDK (already on this Mac), `./gradlew assembleBasicDebug`,
   run on the Pixel 7.
4. **Upstream updates without shared history:** keep a local full clone of upstream; to update,
   apply `git diff <old base>..<new base>` from upstream onto this repo (`git apply -3`), fix
   conflicts, bump `UPSTREAM_BASE`. Do it per upstream release.
5. **Keep diffs small:** new code in `org.breezyweather.ui.expressive`; edits in upstream files
   only to switch screens over. A list of touched upstream files lives in this doc (below).
6. **CI:** GitHub Actions builds `basicDebug` and runs lint on each push.

### F1: Expressive theme
- `Theme.kt` → `MaterialExpressiveTheme`, expressive motion, shapes, emphasized typography.
- Check the whole app still looks right (it is all themed through this file).
- Done: `ui/expressive/theme/` (`ExpressiveTheme`, `ExpressiveShapes`, `ExpressiveTypography`).
  Only Compose screens use it; the View screens (main Forecast, Locations list) keep the XML
  `Theme.Material3Expressive` theme. Upstream's settings rows hardcode their shapes, so the larger
  corners show up only once F2 restyles them.

### F2: Settings
- Restyle the shared building blocks first (`preference/composables/`): every settings screen
  changes at once with few edits upstream.
  - Sections → segmented list groups with a quiet section title (upstream already groups rows with
    `Material3ExpressiveCardListItem` first/last corners; adopt the theme's larger corners).
  - Switch rows with a check/close icon in the thumb.
  - List and multi-list choices open a **bottom sheet** (decided 2026-11-18): drag to dismiss,
    interruptible, options as a segmented list with a check on the selected one.
  - Small fixed choices (2–4 options: dark mode, units) shown **inline as connected button
    groups** (decided): one tap, the selected button morphs to a pill.
- `AppBar.kt` → `LargeFlexibleTopAppBar`: **large title that shrinks** into the bar as you scroll,
  tracking the finger (decided).
- Root settings: each category icon in its **own colored Expressive shape** (circle, rounded
  square, petal…), colors derived from the wallpaper palette, like Android 16 Settings (decided).
  Short summary line under each title.
- Mockup reviewed 2026-11-18: home with colored shapes, Appearance with button groups and an icon
  switch, Language as a bottom sheet.
- Reorder screens (cards, daily/hourly trends): rewrite the 3 View activities in Compose with
  drag handles and haptics (LazyColumn + a small reorder helper), same stored data.

### F3: Locations
- Location list (`ManagementFragment`) → pure Compose `LazyColumn`: location cards with current
  conditions, drag to reorder, swipe to delete with undo, current-location card pinned on top.
  Removes the Compose/RecyclerView mix (upstream notes it causes scroll stutter).
- Add / search: Expressive `SearchBar` expanding to full screen, results as segmented list,
  `LoadingIndicator` while searching, clear empty and error states.
- Location settings screen (`LocationSettingsScreen.kt`) follows F2.
- **Tab toolbar:** floating pill with Forecast and Locations at the bottom, tap or swipe to switch
  (Forecast first, Locations to the right; see Design direction). New shell in `ui.expressive`
  hosting both; `MainActivity` switches to it in one small edit. Tapping a location in the
  Locations tab slides to Forecast showing it.
- **Step 1 built 2026-11-18 (phones):** `ui/expressive/shell/` — `SolsticeShell` (pager Days ← Forecast →
  Places over the current location's animated sky; floating toolbar whose highlight follows the
  swipe; opens on Places when there are no locations), `ShellPages` (first simple Compose Forecast:
  title with current-location icon, big temperature, condition, feels-like line, sun path with
  sunrise/sunset, Today card with the next 6 hours, pull to refresh; Days list; Places with sky
  cards, tap to select and slide to Forecast, Add a place → upstream search, settings button).
  `MainActivity` hosts it in a new `solstice_shell` ComposeView (phone layout only); upstream's
  Forecast stays underneath, hidden, as **"More details"** (Back returns to the shell);
  `setManagementFragmentVisibility` routes to the Places page. Tablets keep upstream's drawer for now.
  **Left for F3:** swipe a card to edit/delete with undo, drag to reorder, current location pinned
  first + "add current location", pull-to-refresh/loading polish, transitions.
- **Location cards:** swipe one way to Edit (location details), the other way to delete with undo.
- **Telling places apart when their weather is the same (added 2026-11-18):** two overcast places
  get the same sky, so cards look alike. Idea from the user: a **generated map overlay** per place,
  e.g. a soft, low-contrast silhouette of the area around it (coastline, rivers, street grid or
  terrain contours drawn from OpenStreetMap / Natural Earth data, or a stylised outline of the
  city/region), blended into the card's gradient with the grain. Alternatives to weigh: a small
  local-time clock face, the country/region shape, a per-place accent hue from its coordinates.
  Must stay cheap (render once, cache per place) and keep text readable; data/license to check.
- **Swipe stages, animated (added 2026-11-18):** the change from the star stage to the delete stage
  should itself animate (the revealed colour sweeps from amber to red, the star morphs into the bin
  icon with a little scale/rotate, the card's corners round more as it nears delete), following the
  finger and reversible. On delete, a **fun discard animation** instead of a plain slide: e.g. the
  card shrinks and tumbles away with a slight spin and fade, or crumples like paper and drops,
  then the list closes the gap with a spring. Undo plays it in reverse.
- **Release ripple (added 2026-11-18, later refinement):** letting go of a swiped place sends a small
  spring wave through the other places, like Android's notification shade: neighbours nudge in the
  swipe direction and settle back, each a little later and softer the further away they are
  (staggered springs). Same when a card is deleted and the gap closes.
- **Location cards wear their own weather (added 2026-11-18):** each card's background is the
  same condition/time-of-day gradient that location's Forecast would show (gradient field + grain,
  see "Forecast background"), so the list reads at a glance: sunny afternoon here, night and rain
  there. Performance: render each card's gradient once per weather update (small cached bitmap or
  a cheap shader), never per frame; text contrast checked on every gradient.
- **Forecast top:** only the location title (current location: real place name + location icon);
  remove upstream's toolbar actions, including "edit this location", from the Forecast screen.

### F4: Polish and release
- Motion pass (shared transitions between list and search, press/selection morphs).
- Check: light/dark, dynamic color on/off, Android 12 and 17, font scale 200 %, RTL, TalkBack.
- Release build with our name/icon; changelog; screenshots.

## Planned next (added 2026-11-18, not yet scheduled into a phase)

### Edit the Forecast blocks in place, like Pixel Weather
- Instead of (or as well as) the separate Blocks list in Settings: long-press the Forecast screen
  to enter an edit mode where the real blocks shrink slightly and can be dragged into a new order
  right there, hidden with a remove button, and added back from a tray of hidden blocks. Done
  ends edit mode. Reference: how the Pixel Weather app reorders its home screen cards.
- Same motion rules: the dragged block follows the finger, others slide with springs, haptic
  ticks, interruptible; TalkBack move up/down actions.
- Depends on the main Forecast screen being Compose (upstream's is View/RecyclerView today), so it
  comes with or after a Compose Forecast screen. Reuses the stored `cardDisplayList` and the drag
  logic from `ui/expressive/reorder/ReorderList`.

### Forecast screen: no fade-ins, progressive blur at the edges
- **Don't fade in blocks.** Upstream animates the Forecast blocks in as they appear; remove that so
  blocks are simply there when the screen opens or scrolls (feels faster, less busy). Check the
  "animations" options in Main screen settings, which control this upstream.
- **Progressive blur at the top and bottom edges:** content scrolling out of view under the top
  bar / floating toolbar gets gradually more blurred (and dimmed) towards the edge instead of being
  cut off hard, like iOS. Android 12+ (`RenderEffect`), plain gradient fade as fallback.
- Performance first: blur is costly on the GPU, so measure frame times on the Pixel 7 (release
  build) and keep the blurred band small; same technique as the translucent overlays above.

### Forecast layout reference (added 2026-11-18)
The user likes how this reference lays out the information and wants it considered for the
Forecast screen (top to bottom):
1. **Location title**, centred (our rule: nothing else at the top; the reference's menu and bell
   buttons are dropped: Locations is a swipe/tab away, alerts appear as a card).
2. **Big temperature**, very large display numerals.
3. **Condition headline** in plain words ("Clear and calm"), then **one plain-language line**
   ("Feels like 17°, wind 9 km/h from the sea").
4. **Sun path** (liked most): a curve across the full width from sunrise to sunset with a dot at
   the sun's current position, the part already travelled highlighted; **sunrise time left,
   sunset time right**, and a short sun/UV note in the middle ("Mild sun, UV 3"). At night the
   same element can show the moon or night length.
5. **Today** card: next hours (temperature, icon, time; "Now" first) with a one-line summary in the
   header ("clear all day, cooler after 21").
6. **This week** card (moves to the Days page, see Navigation; Forecast may keep a short teaser): days as small bars at their temperature height, the day that needs
   attention highlighted, and a one-line summary ("only Thursday needs you").
7. **Alert card** when something severe is coming: when, what, and a short practical line.
- Cards are translucent over the gradient background (see translucent overlays), large corners.
- Builds on upstream's data (sun/moon times, UV, hourly, daily, alerts); the plain-language lines
  need a small summary generator (the old Weather-App-Experiment had a rules engine for this).
- Depends on a Compose Forecast screen (with in-place block editing); these elements would become
  blocks the user can reorder/hide.

### Forecast background: soft gradient (added 2026-11-18, style still open)
- The Forecast screen gets a soft, grainy gradient background reflecting the weather and time of
  day, like the references the user shared: blurry colour fields with film grain (lavender/blue/
  near-black at night; peach/orange/yellow for sun; blue sky into warm sunset). This updates the
  earlier "no custom sky look" decision for the Forecast background only; the rest of the app
  stays Material You.
- **Decided 2026-11-18 (from mockups A/B/C): gradient field (A).** Soft blurred colour fields with
  film grain, no sun/moon orb; the sun path stays a drawn curve with a dot for the sun, sunrise
  left and sunset right.
- **Palette approved 2026-11-18** (base colour + three soft blobs, film grain on top; text colour):
  | Condition | Base | Blob 1 | Blob 2 | Blob 3 | Text |
  |---|---|---|---|---|---|
  | Clear day | `#BFD3F2` | `#8FB0EE` | `#FFE1A8` | `#F3EBDD` | `#1E2340` |
  | Partly cloudy | `#F2D7C9` | `#9DAEE6` | `#F4A46B` | `#F7E3C8` | `#2A1A1A` |
  | Sunrise / sunset | `#F6B48A` | `#8C8FD6` | `#FF7B4A` | `#FFC870` | `#2B1408` |
  | Clear night | `#1D1A3D` | `#8E7CD0` (was `#C9B6F2`, darkened 2026-11-18 for title contrast) | `#5B5BD6` | `#0F0D22` | `#ECE9FF` |
  | Overcast | `#C9CBD3` | `#A7ABB8` | `#E2DED8` | `#B9B4C4` | `#22252E` |
  | Rain | `#7E8FA8` | `#4D6185` | `#9FB7C9` | `#5B6E7E` | `#F2F5FA` |
  | Thunderstorm | `#3A3550` | `#1E1B33` | `#7A5FB0` | `#4A5570` | `#F1EEFA` |
  | Snow | `#E7ECF4` | `#C3D2EC` | `#FFFFFF` | `#D8D2EA` | `#1F2633` |
  | Fog / haze | `#D9D6D1` | `#EDEAE4` | `#C4C0BA` | `#E4E0DA` | `#26241F` |
  Dawn/dusk blend between the day and night palettes over time; grain stronger on dark palettes.
- **Built 2026-11-18 (static version):** `ui/expressive/sky/` — `SkyCondition` (nine palettes above +
  mapping from upstream's `WeatherCode`, golden hour within 40 min of sunrise/sunset) and
  `SkyBackground` (base + three radial-gradient colour fields + tiled grain, baked once at half
  resolution and cached for the last 6 palette/size pairs). Debug-only `SkyPreviewActivity` swipes
  through all skies (`adb shell am start -n io.github.information89.solstice.debug/org.breezyweather.ui.expressive.sky.SkyPreviewActivity --ei page N`).
  Swiping between full-screen skies on the Pixel 7 (debug build): median 12 → 5 ms, GPU 8 → 1 ms
  after baking + cache.
- **Night skies (decided 2026-11-18): hand-tuned + blending.** Every condition gets its own designed
  night palette (rainy night, snowy night, foggy night…), and colours blend continuously with the
  sun's elevation: day → golden hour → short blue-violet twilight → night, and back at dawn.
- **Animation (decided 2026-11-18): calm drift + moving sun.** The colour fields drift very slowly
  (barely noticeable); one warm light field is the **sun**, placed where the real sun is (rises left,
  crosses the top, sets right, from the location's sunrise/sunset times) and moving with the time of
  day; after dark it becomes a cool **moon** glow. It is a soft light, not a hard disc (mockup B's orb
  was not chosen).
- **Technique (decided 2026-11-18):** one small AGSL shader on Android 13+ (GPU), frame rate throttled
  (e.g. 30 fps or less; the drift is slow), paused when off-screen, in battery saver and when the
  system "Remove animations" setting is on. Android < 13 (and the paused cases) use the baked static
  sky, with the sun field placed by time of day. Location cards stay static (baked) for scrolling.
- Colours: derived from conditions and time of day, harmonised with the wallpaper (dynamic) palette
  so cards and text stay readable; text contrast checked in both themes.
- Performance first: draw it cheaply, e.g. a few blurred shapes / a small pre-rendered bitmap scaled
  up, or an AGSL shader on Android 13+; grain as a tiled noise texture. No per-frame redraw unless
  something moves; measure frame times on the Pixel 7 (release build).

### Collapsing title: one title that moves and shrinks (added 2026-11-18)
- Today (settings and every screen using `FitStatusBarTopAppBar` / `LargeFlexibleTopAppBar`), the
  large title **cross-fades** into the small one when scrolling: both texts are visible at once
  for a moment. Replace it with a real transition: **a single title** whose position and size are
  driven by the scroll (`TopAppBarState.collapsedFraction`): it slides up and left from the large
  position into the bar and scales from the large to the small text size, never duplicated.
- Follows the finger exactly (no time-based animation), so it reverses mid-scroll; on release it
  settles to fully expanded or collapsed with the theme's spring (interruptible).
- How: our own top bar in `ui/expressive` (one `Text` with `graphicsLayer` scale/translation, measured
  once for both sizes; no re-layout per frame), swapped in at `FitStatusBarTopAppBar`, so all screens
  get it in one edit. Same component later for the Forecast/Days/Places titles.

### Screen transitions: no plain fade (added 2026-11-18)
- Tapping a settings entry (and opening any sub-screen) today **cross-fades** to the next screen:
  `SettingsActivity`'s `NavHost` sets no transitions, so Compose's default fade is used. Replace it
  with something smoother and more refined:
  - **Forward:** the new screen slides in from the right while the old one shifts slightly left and
    dims (parallax, Material shared-axis X / iOS-style push), spring-driven; **back** does the
    reverse.
  - **Predictive back:** the back gesture drags the screen with the finger (scales/peeks the
    previous one) and can be cancelled mid-way; release finishes with a spring.
  - Optional: the tapped row's **title flies into the new screen's large title** (shared element),
    so the user sees where they went.
- Interruptible, no fixed-duration fades; same transitions for the reorder and other expressive
  screens, and later for pages inside the app (Days/Forecast/Places use the pager swipe instead).
- How: `NavHost(enterTransition/exitTransition/popEnter/popExit)` with slide + slight scale using the
  theme's spatial springs, `SharedTransitionLayout` for the title; one shared definition in
  `ui/expressive`, one small edit in `SettingsActivity`.

### Days: a tactile (skeuomorphic) view, optional (added 2026-11-18)
- Besides the plain list of the next 10 days, an optional **skeuomorphic** presentation: a physical-
  feeling object for the week, e.g. a flip calendar / tear-off pages, a dial to turn through the days,
  or cards stacked like a deck that you flick through, with real depth, light and texture.
- A setting chooses between the clean list and the tactile view. Exact metaphor to decide with
  mockups; same motion rules (follows the finger, interruptible springs, haptics).

### Outlook as a real paper calendar (added 2026-11-18; extends the tactile view above)
Make Outlook look like an actual paper calendar. Proposals to mock up and compare:
1. **Tear-off day pad** ("page-a-day"): one page per day, big date numeral, weekday, the day's
   weather drawn like a stamp/illustration and the high/low; pull a page up/away to tear to the next
   day (page curl, torn perforation edge left behind, paper sound optional). Reference: classic
   page-a-day desk pads, Japanese *himekuri* daily calendars.
2. **Spiral desk flip calendar**: pages bound by metal rings at the top; flick a page over the rings
   to the next day; slight paper shadow and thickness of the remaining stack shows how many days are
   left. Reference: tent-style desk flip calendars.
3. **Wall month/week sheet**: a printed week or month grid on textured paper, each day's box with a
   small weather illustration and handwritten-style temperatures, today circled in pen. Reference:
   printed wall calendars, planner pages.
4. **Perpetual block calendar**: wooden/paper cubes or flip cards that roll to show day and weather.
   Reference: perpetual desk calendars (cubes, flip-card date displays).
- Shared details: paper texture (subtle grain, slight warmth), print-like typography (big numerals),
  real shadows/thickness, weather as ink illustrations in the day's palette colour; every gesture
  follows the finger and can be reversed mid-way (springs), haptic tick per page.
- References are for the look and feel only (no copying of any product's artwork). Choose one with
  mockups; the setting from "tactile view" switches between list and paper calendar.

### Places: visit vs pin, time difference (added 2026-11-18)
- Places behave like a map of places you **visit**: search somewhere else, look at its weather, and
  it stays only for the session (a "visiting" card) unless you **pin** it, which saves it
  permanently. Lets you consult other places without adding them for good.
- Pinned places are the saved list (the star from the swipe is the first form of this).
- Each place shows its **time difference** to you ("+1 h", "−6 h") and local time.
- Needs: a non-persisted "visiting" location in the view model (upstream saves every searched
  place), pin/unpin, and clearing visits after a while or on app restart.

### Alerts and Live Updates (added 2026-11-18)
Android's counterpart of iOS Live Activities is **Live Updates** (Android 16 promoted ongoing
notifications: lock screen, status bar chip, always-on display; plain ongoing notification on older
Android). Upstream already has weather-alert, precipitation and today/tomorrow notifications to
build on (the separate notification *widget* was removed).
- **Alerts for sudden changes and strong conditions:** a notification when the weather is about to
  change sharply (rain starting soon, temperature drop, wind jump, storm arriving) and warnings for
  strong conditions (official alerts from WMO/NWS/ECCC/MET Norway plus thresholds from the forecast:
  very high UV, heat, strong gusts, heavy rain). Plain language ("Heavy rain in 20 min, until
  about 16:00"), one notification per event, no repeats; per-type on/off and quiet hours.
- **Live Update for ongoing rain:** while rain is coming or falling at the current location, an
  ongoing Live Update shows the minute-by-minute picture (starts in / stops in, intensity bar from the
  nowcast). It appears ~30 min before rain, updates as it goes, and disappears when it stops. Uses
  the minutely (nowcast) data of Open-Meteo / MET Norway.
- **Live Update for the daily brief:** a short forecast brief for the day ("Clear and mild, 12–22°,
  rain after 18:00, take a jacket") as a Live Update at a **time the user chooses** (e.g. 7:00 on
  weekdays, 9:00 at weekends), staying until dismissed or until the evening; optionally a second
  brief in the evening for tomorrow. Shares the plain-language summary generator with the Forecast.
- Needs: notification permission flow, background refresh timed for the brief, battery-friendly
  scheduling (WorkManager, exact alarm only if needed), and a settings section for all three.

### Sky motion setting (added 2026-11-18)
- Appearance setting **"Sky motion"** as an inline button group: **Still · Calm · Gentle · Lively**.
  - Still: no drift (the baked static sky, no animation running at all; saves battery).
  - Calm: the first, very subtle drift (amplitude ~5–6 %, loops 79–151 s).
  - **Gentle (default):** current drift (amplitude 8.5–10 %, loops 47–97 s), user-approved 2026-11-18.
  - Lively: ~1.5× Gentle's amplitude and speed.
- How: one `motion` factor (0 / calm / 1 / 1.5) passed to `AnimatedSkyBackground` as a shader uniform
  scaling amplitude and speed; Still skips the shader. The weather effects (rain, snow, thunder) get
  their own on/off, but also stop when Sky motion is Still. System "Remove animations" and battery
  saver still override to Still.

### Weather effects: rain, thunder, snow (added 2026-11-18)
- Light, tasteful effects on top of the sky when it's actually happening at the location:
  - **Rain:** fine diagonal streaks (density from the precipitation intensity; drizzle sparse,
    downpour dense), slightly blurred, low contrast; optional droplets sliding on the "glass".
  - **Thunder:** occasional soft flash that brightens the sky for a moment (random intervals,
    rarer at low lightning probability), never strobing; no flashes if the user prefers reduced motion.
  - **Snow:** slow drifting flakes of a few sizes with gentle sway (parallax: big flakes faster).
  - Maybe later: fog banks drifting, wind making the streaks/flakes slant more.
- Same performance rules as the drift: GPU only (AGSL shader or a small particle shader), throttled
  frame rate, paused off-screen, in battery saver and with animations off (then: no effect, sky only).
  Effects stay behind the text and cards and are toned down under them for readability.
- Accessibility: flashes respect reduced-motion / "Remove animations"; option to turn effects off.

### App drawer name: "Solstice" or "Weather" (added 2026-11-18)
- A setting (Appearance) to choose the name shown under the icon in the app drawer/home screen:
  **Solstice** or **Weather**, so the app can sit where people expect "Weather".
- How: two `<activity-alias>` launcher entries for the main activity, each with its own label;
  the setting enables one and disables the other with
  `PackageManager.setComponentEnabledSetting(..., DONT_KILL_APP)`.
- Caveats to handle: some launchers take a moment to refresh or remove a home-screen shortcut
  when the alias switches (warn in the setting's summary); app shortcuts and widgets must point at
  the real activity, not the alias. The app's own name elsewhere (About, notifications) stays
  "Solstice".

### Icon search: softer and rounder (added 2026-11-18)
The user wants icons that feel **softer and rounder** than today's (the toolbar and new screens
currently use Compose's *Outlined* Material icons; upstream's own `ic_*` drawables are sharper).
Compare options side by side on the real screens (toolbar, Places, settings rows, weather states)
before choosing; check each license.
- **Material Symbols Rounded** (Apache 2.0): same family as now but rounded corners and terminals;
  variable fill (filled when selected), weight and grade; fits Material You. Cheapest switch: the
  Compose `Icons.Rounded` set already in the app, or the variable font for finer control.
- **Phosphor** (MIT): very soft, friendly, round line caps; six weights incl. Duotone and Fill;
  has a good weather set.
- **Lucide** (ISC): clean, rounded strokes, consistent 2 px line; wide coverage.
- **Fluent UI System Icons** (MIT): rounded, soft, filled + regular pairs.
- **Weather condition icons** to consider alongside: **Meteocons** (MIT, soft, also animated),
  Phosphor weather, Material Symbols weather.
- Decide with a comparison mockup; then apply everywhere (toolbar, shell, settings rows,
  upstream `ic_*` drawables regenerated under the same names, see "New Material You Expressive
  icons everywhere").

### New Material You Expressive icons everywhere
- Replace upstream's UI icons (settings, toolbar, actions, rows) with Material Symbols in the
  Expressive style: Rounded, filled when selected, consistent weight/grade, matching the type.
- Approach: map each upstream `ic_*` drawable to a Material Symbol and regenerate the vector
  drawables under the same names, so call sites don't change (small upstream diff). Check the
  Material Symbols license (Apache 2.0) and record it.
- Weather condition icons (icon packs) are separate: keep upstream's packs for now; an Expressive
  weather icon set is its own later decision.

### Simplify settings
- The Daily trends and Hourly trends options are too much. Proposal to discuss before building:
  fold them into the Forecast blocks (configure from the block itself / in-place edit mode), show
  only the few that matter by default, and move the rest under a single "More" or "Advanced"
  section.
- Then a pass over all settings screens: remove or merge rarely used options, shorter summaries,
  fewer levels of nesting. Nothing removed from upstream's stored settings, only from what's shown,
  so upstream merges stay easy.

### Simplify and lighten the app (decided 2026-11-18)
- **Approach: cut whole modules.** Delete entire features and sources we don't want as clean cuts
  (whole packages, their resources, settings and manifest entries), and keep everything else
  syncable with upstream. Not "hide only" (no size win), not "rewrite freely" (too costly to keep
  taking upstream fixes). Each cut gets listed under "Upstream files touched" and is applied again
  after each upstream sync (deleted files stay deleted; conflicts only where we cut references).
- **Remove:** live wallpaper; notification widget (persistent weather in the shade).
- **Keep:** data sharing with other apps (Gadgetbridge, watches, launchers: content provider and
  broadcasts).
- **Widgets: later.** Maybe keep one or two, decided much later; no widget work until then.
- **Weather sources (decided 2026-11-18): worldwide + North America.**
  - Keep: Open-Meteo (default), MET Norway, WMO Severe Weather, FOSS Public Alert Server,
    AccuWeather / OpenWeather / Pirate Weather (work only with the user's own key: the fork has
    none of upstream's keys), NWS and NCEI (US), ECCC (Canada).
  - Keep (infrastructure): Android geocoder, Nominatim, Natural Earth (offline fallback), GeoNames
    (with own key), Breezy Time Zone, IP.SB, Gadgetbridge, update notifier, debug, common.
  - Cut: all European (~25), Asian/Pacific (~17, incl. Baidu IP) and African (ClimWeb) national
    services. Mexico has no national source upstream; WMO alerts cover it.
  - Expectation: ~5 MB of source code goes, but the APK is mostly libraries, so the main win is
    simpler source lists in settings; measure the release APK before/after.
  - Upstream sync: deleted source packages stay deleted; `SourceManager` and the Hilt wiring are the
    only shared edits.
- **Use platform components** where they do the job: Android's own geocoder, system location,
  stock Material 3 components instead of upstream's custom ones as screens move to Compose.
- **Measure:** debug APK today is 43 MB (31 MB code). Compare release APK size, startup and frame
  times on the Pixel 7 before and after each cut.

### Current-location name: Android built-in geocoder (decided 2026-11-18)
- Use the phone's own geocoder (`nativegeocoder` source, available in the `basic` build) as the
  default address lookup for the current location: no key, no setup, good local names.
- Fallback when it isn't available (phones without Google services): Nominatim. It fails today only
  because the fork's contact link is empty (`app.report_issue` in `gradle.properties`, required by
  Nominatim's usage policy). Fill in the fork's links (`app.report_issue`, `app.source_code_link`,
  `app.privacy_policy_link`, …) pointing at this GitHub repo; that also fixes the build warnings
  and the About screen links.

### Search redesign: search bar at the bottom (added 2026-11-18)
- Redesign "Search somewhere else": the search field sits at the **bottom** of the screen (thumb
  reach, like a bottom address bar), results appear above it growing upwards, keyboard pushes the
  field up with it. Replaces upstream's top search screen (`SearchActivity`) with a Compose screen
  or sheet in `ui/expressive`; picking a result opens it as a visit (see Places: visit vs pin).
- **Transition (user's direction, 2026-11-18):** the "Search somewhere else" button **morphs into the
  search section** (container transform / shared element: the button grows into the bottom search
  bar and the results area, the list behind recedes), and closing morphs it back into the button,
  so everything feels fluid. Exact motion still to be proposed and agreed before building.

### Messages (snackbars) redesign (added 2026-11-18)
- Short, icon-led messages instead of sentences with buttons: e.g. "Guadalajara removed" becomes
  **[unpinned icon] Guadalajara unpinned**; "Visiting Puebla. Star it to keep it." becomes
  [compass icon] Visiting Puebla, etc.
- **No close (X) button:** dismiss by swiping the message away (sideways or down), following the
  finger; it still times out on its own. Undo stays as a compact action where it applies.
- Same glass look as the toolbar, sitting just above it. Motion (appear, swipe away) to propose to
  the user before building.

## Motion framework (proposed 2026-11-19, to agree before building)

Goal: get every animation to iOS-level refinement with **one small system** that the whole app (and
other projects) uses, instead of building each morph by hand. The rules stay the ones in
[MOTION.md](../MOTION.md); this is how to build them once.

### Why the current moon-card approach doesn't scale

- **It fights Compose's shared bounds.** The morph seeks a `SharedTransitionLayout` from an
  `Animatable`, so every shared part needs linear 1000 ms "fake" tweens (`MOON_SPAN`), the seek is
  clamped to 0.0001…0.9999 and wrapped in `runCatching`, and it must stay one endless seek towards
  "open" or the button and sheet get rebuilt mid-motion and drop frames. Each new morph would repeat
  all of that.
- **Only the moon card uses the tokens.** Elsewhere (shell, pages, search, reorder) there are 28
  hand-written springs, 12 tweens and 13 `animate*AsState`, with ~15 different damping/stiffness
  pairs. That's the main reason motion doesn't feel like one app.
- **Timings are written per part** (label 15 %, material 30 %, content 35–70 %, pill back at 90 %)
  as tween fractions, so choreography lives in several places.

### The idea: one number per interaction, everything else derived

iOS sheets and menus work in three layers (Glasmio's UIKit notes, `ios-sheet-motion.md`,
`dropdown-motion.md`): **layout** (where things rest), **interaction** (finger, projection,
resistance, picking a target), **presentation** (springs that keep their velocity when retargeted).
We copy that split:

1. **One `MotionValue` per interaction** (an `Animatable` wrapper). 0 = closed, 1 = open; it can go
   past both ends (overshoot, rubber-band). The finger moves it directly; letting go hands the
   finger's velocity to the spring; a new touch catches it mid-flight. Open, close, drag and back
   gesture all just move this one value.
2. **Geometry is computed, not shared.** Source and target register as **anchors** (their bounds,
   corner radii, colours). The morphing surface is drawn in an overlay with
   `lerp(source, target, p)` for position, size, corners and colour. No `SharedTransitionLayout`, no
   seek, no fake tweens. Overshoot comes for free (lerp past 1), and the source and target stay
   composed and hidden, so nothing is rebuilt during the motion.
3. **Choreography is a list of windows on that number.** For example `label = 0…0.15`,
   `material = 0…0.30`, `content = 0.35…0.70`. A part reads its window (`p.window(0.35f, 0.70f)`)
   and maps it to alpha / scale / offset with ready-made `reveal()` / `handOff()` modifiers.
   Closing plays the same windows backwards, so content leaves first without extra code;
   different closing windows are allowed when needed.
4. **Read the value only in layout and draw** (`graphicsLayer {}`, `Modifier.layout`, `drawBehind`),
   never in composition, so a morph doesn't recompose 120 times a second. Content is laid out once
   at its final size (already learned on the moon sheet).

Text morphs (button label into heading) are one more anchor pair: the heading is drawn at its final
size and scaled/translated from the label's bounds, with the label and heading handing over early,
as now.

### Springs written the iOS way

Tokens are written as **response (s) + damping ratio**, like UIKit and SwiftUI, and converted with
`stiffness = (2π / response)²`. Today's values are already close to iOS:

| Token | Now (damping / stiffness) | As response / damping | iOS reference |
|---|---|---|---|
| `container` | 0.78 / 380 | 0.32 s / 0.78 | menu morph 0.30 s / 0.80 |
| `dismiss` | 1 / 260 | 0.39 s / 1 | sheet 0.344 s / 1 |
| `control` | 1 / 400 | 0.31 s / 1 | |
| `content` | 0.85 / 400 | 0.31 s / 0.85 | |
| `settle` | 1 / 400 | 0.31 s / 1 | |

Possible additions, to agree: `snappy` for small toggles and icons, `gentle` for big page moves, and
`press` for press morphs. No other springs in the app; anything new becomes a token first.

### Release rules taken from UIKit (Glasmio's notes)

- **Projection:** where the value would rest after a fling, `deceleration 0.99` for sheets (as now).
- **Ignore slow flicks:** release speeds under ~250 dp/s count as zero, so a slow lift doesn't bounce.
- **Fast flick moves one step:** above ~1000 dp/s, if projection would land on the same resting
  point, go to the next one in the flick's direction.
- **Rubber-band** with coefficient 0.55 and a limit per edge from the layout (iOS: up to 100 pt past
  the top, 25 % of the height up to 200 pt past the bottom), not one global range.
- **Velocity past a limit is scaled down** by the same rubber-band fraction, so a release from an
  over-pull doesn't shoot off.
- **Interrupting keeps the current position and velocity.**

### What the framework is

A standalone Gradle module, `:motion`, that only depends on Compose (no Breezy or Solstice code), so
it can go into other projects as is. Rough API:

- `Motion` tokens (`Motion.container`, …) and `Motion.spring(response, damping)`.
- `rememberMotionValue()` with `animateTo`, `drag(delta)`, `release(velocity)`, rest points and
  per-edge rubber-band limits.
- `Modifier.motionDrag(value, orientation, range)` for a gesture that drives a value.
- `MorphHost` + `Modifier.morphAnchor(key)` + `Morph(key, value) { … }` for container transforms.
- `p.window(from, to)`, `Modifier.reveal(...)`, `Modifier.handOff(...)` for choreography.
- Reduced motion: with "Remove animations" on, morphs jump straight to the end (no travel or
  scaling). Compose finishes every animation instantly at that setting, so there is no short fade.
- `Modifier.pressMorph()` on the same tokens (replaces `PressMorph.kt`'s `animateDpAsState`).

Not in it: glass, blur, colours, layouts. Those stay in Solstice and only read the motion value.

Added after the library research (2026-11-19, [UI_SYSTEMS.md](../UI_SYSTEMS.md)):

- A **Solstice `MotionScheme`** that returns these tokens, set in `ExpressiveTheme` instead of
  `MotionScheme.expressive()`, so Material components (switches, buttons, Settings sheets) move like
  the rest of the app.
- **Snap swipes** (Places card star/delete, message swipe-away) use Compose's `AnchoredDraggable`
  with our springs, not `MotionValue`. `MotionValue` is for morphs that scrub and overshoot.
- **Back** is `PredictiveBackHandler` everywhere, feeding the same motion value as a pull, so the
  back gesture scrubs sheets and windows.
- **Pushed screens** (Settings, reorder, about) use Navigation 3 or Navigation 2 shared elements with
  predictive back instead of `ScreenPush`. Compose's own shared elements fit those; our `Morph` is
  only for overlays opened from a control.

### Solstice `MotionScheme`: decided 2026-11-19, built (`SolsticeMotionScheme`) with a sixth token `snappy` (0.2 s / 0.9) for Material's fast springs

Material's expressive scheme has three spatial springs (fast ≈ 0.17 s, default ≈ 0.31 s, slow
≈ 0.48 s, all bouncy) and three effects springs (no bounce). With only our five tokens, the closest
mapping is below. Our fastest token is 0.31 s, so **switches, ripples and small Material controls
would get slower** than today. Options: accept it, or allow one more token (e.g. `snappy`, 0.2 s /
0.9) only for the scheme's fast springs. Needs a decision and a phone check.

| `MotionScheme` | Material expressive now | Proposed token |
|---|---|---|
| `fastSpatialSpec` | ≈ 0.17 s / 0.6 | `control` (0.31 s / 1) |
| `defaultSpatialSpec` | ≈ 0.31 s / 0.8 | `container` (0.32 s / 0.78) |
| `slowSpatialSpec` | ≈ 0.48 s / 0.8 | `dismiss` (0.39 s / 1) |
| `fastEffectsSpec` | ≈ 0.11 s / 1 | `control` |
| `defaultEffectsSpec` | ≈ 0.16 s / 1 | `settle` |
| `slowEffectsSpec` | ≈ 0.26 s / 1 | `settle` |

### Steps

1. **M0, agreed 2026-11-19:** springs are the five current tokens only (`container`, `dismiss`,
   `control`, `content`, `settle`; no `snappy`/`gentle`/`press`); the module is `:motion`,
   licensed Apache-2.0 (our own code, for reuse). First task: design tokens, then Haze.
2. **M1, done 2026-11-19 (`ceae727`):** tokens, `MotionValue`, drag/release with the rules above, unit tests for the
   maths (spring conversion, projection, rubber-band, target choice).
3. **M2, done 2026-11-19 (`e0310e9`, not yet used in the app):** anchors, overlay, geometry and text morph, choreography windows, reduced motion.
4. **M3, done 2026-11-19 (checked on the Pixel 7, profile build):** remove `MoonSharedLayout`, the seek and the fake tweens; it must look the
   same or better, with the three open moon issues fixed (dark frame at the start, "· 71 %" drift,
   hitch after closing). Measure janky frames on a release build against today's ~10 %.
5. **M4, move everything over:** search sheet, postcard window, floating button budding from the
   toolbar, Places card drag, tab jumps, screen push, reorder, press morphs. Replace every hand-written
   spring and tween; `grep "spring(\|tween("` outside `:motion` should end up empty.
6. **M5, a motion lab:** a debug-only screen with each pattern, a slow-motion switch and a frame
   counter, to compare against iOS recordings side by side. Then write the module's README so it can
   be used in another project.

Each step goes through the motion test checklist below on the Pixel 7 (release build for
smoothness).

## Design system foundations (proposed 2026-11-19, to agree)

Audit of every UI system and the libraries checked: [UI_SYSTEMS.md](../UI_SYSTEMS.md). In short:

- **Tokens first:** spacing on a 4 dp grid, named sizes, shape tokens with continuous corners,
  one palette file for the non-dynamic colours, named hero type styles, motion tokens. Today there
  are 325 `.dp` literals (~30 values), 11 corner radii and 41 hex colours in `ui/expressive/`.
- **Haze prototype** for glass and edge blur (replaces `Glass.kt` and `EdgeBlur.kt` if it's as fast on
  the Pixel 7; supports every Android version).
- **Component catalog:** split `ShellPages.kt` (2,231 lines); one Card, Sheet, Toolbar, Message and
  PageScaffold; `PlaceWindow` reuses them instead of re-hosting the shell.
- **Library swaps:** Reorderable for `ReorderList.kt`, Navigation 3 for pushed screens, Material 3
  Expressive controls (buttons, toggles, loading).
- Order: tokens, Haze prototype, motion M1 to M3, catalog, swaps, motion M4 and M5.

## Icons (decided and built 2026-11-19)

Decided with the user: Google's **Material Symbols**, style **Rounded**, weight 400, grade 0;
**unfilled by default, filled only for on/selected** (starred, pinned, location in use); delivered as
static vectors (no icon font), so it works on every Android version; the old Material Icons
(`material-icons-extended`, frozen since 2022) are gone.

Rules, following Material 3's icon guidance (m3.material.io/styles/icons, "Applying icons", read
2026-11-19) and Google's Material Symbols guide:

- **One source:** Compose code uses `ui/expressive/components/SolsticeIcons.kt` only; XML uses the
  `ic_*.xml` drawables (upstream names, Rounded artwork). CI fails on `androidx.compose.material.icons`.
- **Optical size follows the drawn size:** each catalog icon exists at optical size 24
  (`SolsticeIcons.X`) and 20 (`SolsticeIcons.Small.X`, for icons drawn at 20 dp or smaller); a
  component that takes an icon parameter calls `SolsticeIcons.forSize(icon, size)`.
- **Fill** is a state, not decoration: `…Fill` variants exist for star, pin and near me, and for the
  toolbar tab icons (calendar, map and the condition glyphs, via `hourIcon(…, filled = true)`).
- **Sizes** from `Sizes.IconSmall` (16), `Sizes.Icon` (20), `Sizes.IconLarge` (24) where possible.
- **Adding an icon:** add its name to `tools/symbols.txt` (`fill` / `mirror` flags) and run
  `python3 tools/make_symbols.py`; for an XML drawable, copy the `_24px.svg` path into an `ic_*.xml`
  with `<group android:translateY="960">` (see any existing one). Source and license:
  `third_party/material-symbols/README.md` (Apache-2.0).
- **Weight:** 400 everywhere, never mixed; never below 200 at 24 dp (M3).
- **The one exception (decided 2026-11-20):** the eight moon-phase icons (`moon_phase_0..7.xml`,
  Weather Icons, `third_party/weather-icons`) on the sun path and the moon dial. Material Symbols has
  no full set of phases. Every other icon comes from the catalog.

### Gaps against M3 (found 2026-11-19, to do)

1. **Grade -25 for light icons on dark backgrounds.** M3: the default grade is 0 for a dark icon on
   a light background and **-25 for a light icon on a dark background** (it compensates for visual
   bleed). Solstice draws light icons on the night sky, dark glass and the dark theme, so those need
   -25. The SVGs exist (`<name>_gradN25_24px.svg`, `_gradN25_20px.svg`); add a grade variant to
   `make_symbols.py` and pick it by the icon's background (or by dark theme where the background
   isn't known). Positive grade is M3's option for an emphasized active state: only if agreed.
2. ~~**Selected toolbar tab filled.**~~ Done 2026-11-19 (see the log). M3's own example is navigation with filled icons for the
   selected tab; our rule says "current tab" but the tab icons (`CalendarMonth`, `Map`, the
   condition icons) have no `…Fill` variants yet. Add them and fill the selected tab (a swap, not an
   animation; animating fill is motion, agree first).
3. **Audit, then fix what fails:**
   - **Small icons:** below 20 dp, complex icons or icons for a key action need a text label (simple
     ones like the star are fine alone). Check the 12–16 dp icons on the postcard, Forecast ("near
     me", schedule), the place window (flight, schedule) and the rain card.
   - **Touch targets:** a 24 dp icon gets a **48 dp** target on a phone (40 dp for 20 dp icons is
     only for mouse and keyboard). Check every tappable icon, including the toolbar's bud and the
     Places swipe icons.
   - **Icons next to text:** same size and optical weight as the text, and the icon's baseline about
     **11.5 % of the text size** lower than the text's.
4. **Big icons:** icons drawn at 28 dp and up (search's "no results" at 32 dp, the Places swipe icon
   at 28 dp) should use optical size 40 (M3: 40–48 for highlighted primary actions). Add a `Large`
   set only for the icons that need it.

Later, to agree: animated fill for state changes (motion rule), weight matched to the text weight.

## Morph refining (planned 2026-11-20 by the user, for 2026-11-21 1:00 AM)

Top priority, in the motion lab first, then in the app. Each point: propose options, agree, build
(motion rule). The lab's launcher icon becomes **"Motion Lab"**.

**1. The original button becomes the card.** Today `Morph` (`:motion`) draws its own surface in an
overlay from the source's anchor to the target's and hides the source, so the eye can catch a swap.
Goal: what the user touched is what grows. The surface must start as exactly the button (shape,
corner, glass, colour, elevation, its icon or label in place) and end as exactly the card, and the
real button and card are never both visible. Options to weigh: the button's own container is the
morph surface (its layout animates from the button's bounds to the card's); or the overlay copies the
button pixel for pixel and swaps in the first frame. Check it on the moon pill, search's bubble and
the postcard window.

**2. Blur to hide text changes.** When text turns into other text (pill label into the sheet's heading,
a value into another), both texts blur as they cross (iOS-style "blur replace"): the old one blurs
out while the new one comes into focus, so no frame shows two readable texts on top of each other.
Values to set: peak blur radius, how long the cross overlaps, and whether size and position move at
the same time. `RenderEffect` blur needs Android 12+; below, fall back to a fade.

**3. Text transformations and transitions**, built as reusable pieces in `:motion` with lab demos:
- label into title: position, size and weight move together on the morph's progress, as one text;
- text replaced in place: blur + fade (point 2), width animating if the length changes;
- numbers: digits roll or count to the new value (like iOS's numeric text transition), for
  temperatures, percentages and times;
- text arriving and leaving with its container (staggered lines, the "content leaves first" rule).

**4. More interactions in the lab: one demo per kind of animation**, so every pattern can be felt,
slowed down and compared with iOS. Suggested list (agree which first):
- morphs: button into card, pill into sheet, card into full screen (and back, with the back gesture);
- sheets with detents, dismiss by pull, catching mid-flight;
- menus and popovers growing from their button (Glasmio's `dropdown-motion.md`), context menu on
  long press;
- screen push and pop with predictive back, tab switch, page swipe;
- swipe actions, drag to reorder, pull to refresh, rubber-band at list ends;
- controls: button press, toggle, switch, checkbox, slider, segmented control;
- text and numbers (point 3), loading and progress, toasts/messages in and out;
- scroll effects: collapsing title, edge blur, sticky headers;
- haptics on each (which ones, and when).

**5. The toolbar's animations into `:motion`** (added by the user 2026-11-20), so the system drives
them and they can be tweaked and refined. Today `shell/Toolbar.kt` animates with seven plain Compose
`Animatable`s on token springs, with the amounts typed inline, and none of it is in the lab:
- the **search bud** (agreed 2026-11-18, "gooey bud"): `neck` (`Motion.playful` / `settle`),
  `wobble` and `ripple` after it pops free or is taken back (`Motion.wobble`);
- the **pin's jab** when pinning (`jab`, `Motion.container`; 1.5 dp down, 7 % squash, 3 % stretch,
  8° tilt, haptic);
- **slide to switch pages**: the selection's `pop` (`Motion.playful` / `container` / `dismiss`) and the
  finger-to-page factor (`1.6`);
- each **tab's press** (`squeeze`), the **labels unfolding** (follows the pager), the bud's tint
  (`animateColorAsState`, `Motion.control`).
To do: name each of these as a `:motion` pattern or a parameter set (one place for its spring and
amounts, e.g. a `ToolbarMotion` object), drive the finger-driven ones (slide to switch, the bud's
swipe) with `MotionValue` and its release rules, and add a **toolbar demo to the lab** with live
controls for those parameters (and the tokens they use), shared with the app so a value tuned in the
lab is the value the app uses. No change to how it looks or moves until a change is proposed and
agreed.

## Last updated text and refresh on open (proposed 2026-11-20, to agree)

**Today:** `MainActivity.onStart()` calls `checkToUpdate()`, which refreshes the shown place only when
its data is older than the background-update interval (`UpdateInterval.validity`, 1.5 h by default
and when background updates are off). So most opens show old data, and the Solstice shell doesn't
show when it was last updated.

**Last updated text:** on the Forecast page, relative and ticking ("Updated 12 min ago"), the clock
time (12-hour) once it's older than a few hours, "Updating…" during a refresh, "Couldn't update" with
retry on failure. Place and look to propose first.

**Refresh on open, proposed compromise** (to agree):
- On open, and on coming back to the app, refresh the **shown place only** when its data is older
  than a **freshness threshold of 30 min** (suggested; 15 or 60 to discuss), separate from the
  background interval. Other places refresh the same way when they're shown (swiped to, postcard
  opened), never all at once.
- **Respect each source:** MET Norway's terms say never to repeat a request before its `Expires`
  header and to use `If-Modified-Since`; check what the sources code already does and keep it.
  Forecast models update hourly or every few hours, so 30 min rarely misses anything.
- **Guards:** one refresh at a time (already); the existing 10-second guard for manual refreshes; no
  network: skip and keep the last data with its "last updated" time; after a failure, wait (e.g.
  15 min, growing) before trying again on open, so it never loops.
- **Current location:** reuse the last position if it's recent (e.g. under 15 min) instead of asking
  for a new fix on every open.
- Widgets and the notification pick up the new data as they do today.
- Possible later: a shorter threshold only for the minutely rain nowcast, where a source has one.

## Far future (added by the user 2026-11-20)

**Native Smartspacer support, without a bridge app.** Smartspacer's plugin SDK
(`com.kieronquinn.smartspacer:sdk-plugin`) builds Targets and Complications as exported content
providers (`SmartspacerTargetProvider`, `SmartspacerComplicationProvider`) protected by Smartspacer's
permission; nothing says they must live in a separate app, so Solstice can ship them itself and push
updates with `notifyChange` after each refresh. Smartspacer needs Android 10+, which becomes Solstice's minimum
(decided 2026-11-20), so no version gate is needed. To check first: the SDK's license against Solstice's LGPL-3.0,
and what Targets (current conditions, rain soon, alerts) and Complications (temperature, condition
icon) to offer. Docs: github.com/KieronQuinn/Smartspacer/wiki ("SDK: Creating a Target",
"Creating a Complication").

## Moon card redesign and its foundations (agreed with the user 2026-11-21)

**Direction: A, "tonal tiles", with the user's changes.** Mockups (reference only):
https://claude.ai/artifact/K9fV9BHkuFY4EYxQesif8C (artboard A). The user's dial reference: Apple's
timer dial in the iOS 26 Control Centre (ticks with numbers above, a pointer, a soft progressive blur
at both edges); adapt it to Solstice, don't copy it.

**Rule for this work (the user, in capitals): build it properly from the start.** Nothing hand-made
that should be a system. Each piece below is a reusable system first (in `:motion` when it's an
interaction or something interactions need; in the sky system when it's a weather/sky effect), with a
lab demo, then the moon card uses it. If a piece seems to need a one-off, stop and extend the system.

### F1. Haptics in `:motion`

Today about 12 haptic calls in 4 files (reorder, postcard, moon dial, places), each picking its own
type. Make haptics a `:motion` system, controlled by tokens like the springs:
- **Semantic tokens**, not raw types: e.g. `Haptic.tick` (a fine step), `Haptic.detent` (a major
  step, like a day on the dial), `Haptic.edge` (hitting a limit), `Haptic.snap` (settling on a
  target), `Haptic.press` / `Haptic.release`, `Haptic.threshold` (a drag crossing its point of no
  return), `Haptic.confirm` / `Haptic.reject`. Study iOS's feedback types first (impact light/
  medium/heavy/soft/rigid, selection, success/warning/error) and name ours from what they're for.
- **Each token = a primitive + an intensity**, editable in one place: `VibrationEffect.Composition`
  primitives (`CLICK`, `TICK`, `LOW_TICK`, `THUD`, `QUICK_RISE`…) with a scale where the phone supports
  them (check `areAllPrimitivesSupported`), else the closest `HapticFeedbackConstants` (minimum is
  Android 10: primitives need 11+, `LOW_TICK`/`THUD` 12+).
- **Behaviour built in:** a minimum gap between ticks so fast drags don't buzz; ticks get lighter at
  high speed (as iOS does); respects the system's touch-feedback setting; one global intensity.
- **`LocalHaptics`** for Compose, used by every `:motion` piece (dials, sheets crossing thresholds,
  swipe actions, reorder, press) so the app never calls the vibrator itself.
- **Lab:** a haptics panel to feel each token and tune its primitive and intensity live.
- Then move the existing ~12 calls onto tokens (no new haptics without the user agreeing; the very
  subtle toolbar ticks are proposed after this exists).

### F2. Progressive edge blur, generalized, in `:motion`

`components/EdgeBlur.kt` blurs and fades the **top and bottom** edges of scrolling content (Android 12+
blur, colour fade below). Generalize it into `:motion` as one modifier for any edge or pair of edges
(top/bottom for lists, **left/right for dials and carousels**), band size, blur radius and fade, read
in the draw phase; the app's `edgeBlur` becomes a call to it. No second edge-blur implementation.

### F3. Dial in `:motion` (with and without text)

A dial is a value you scrub by dragging along a ruler. Build it once:
- **State on `MotionValue`:** drag 1:1, fling with projection, snap to steps (or not), the UIKit release
  rules, **rubber-band at the ends with `Haptic.edge`** (the end the user found missing), never blocks
  input, catches mid-flight.
- **Steps and haptics:** minor steps (e.g. 6 hours) give `Haptic.tick`, major steps (a day) give
  `Haptic.detent`, settling gives `Haptic.snap`, all from F1's tokens.
- **Look, configurable:** ticks only, ticks + labels, ticks + labels + icons (slots: the caller gives
  each major step's label and icon, e.g. date and moon phase). A centre marker. Optional **drum
  curvature**: items are placed as on a turning cylinder, so labels and icons are largest at the centre
  and get smaller (and fainter) towards the edges, like a real dial; tick spacing tightens the same
  way. **F2's edge blur** on both sides. Ends drawn so the range is visibly finite.
- **Accessibility:** an adjustable control for TalkBack (step back/forward, value read out).
- **Lab:** a dial demo with switches for text, icons, curvature, blur and haptics.

### F4. Sky effects: stars, ready for weather effects

The sky is already one AGSL shader (`sky/AnimatedSkyBackground.kt`), on Android 13+, run by a clock
that's capped in frames per second, pauses when not visible, and stops in battery saver or with
animations off. Build effects into that system, not beside it:
- **An effect layer** in the sky system: an effect is a few shader functions plus uniforms (density,
  brightness, seed, time), so stars now, and later rain, snow, fog, lightning (ROADMAP's weather effects)
  plug in the same way.
- **Stars:** generated in the shader from a hashed grid (no star list, no per-frame allocation), drawn
  in the same full-screen pass the sky already makes, so they cost almost nothing more. **Twinkle**
  (glitter) is a per-star brightness wave on the existing clock: built in but **off until the user
  agrees** (proposal: slow and subtle, a few stars at a time). Below Android 13: stars drawn once into a
  cached image, no twinkle.
- **The moon sheet uses the same effect** for its background, so the card's sky and the page's sky are
  one system.
- Move the clock and its "motion allowed" rule into `:motion` as an **ambient clock** (capped FPS,
  lifecycle-aware, reduced-motion and battery-saver aware), since any app's ambient animation needs it.

### F5. The moon card, A refined (after F1–F4)

- **The whole sheet is the night sky** (F4's stars), not a sky inside a box: the moon sits directly on
  it, and the stars show **behind the tiles** (tiles are translucent tonal containers over the sky).
- **Header:** phase as the heading, date under it, close button (as A).
- **Dial (F3):** drum curvature, **dates and moon-phase icons** on the major steps (one per day), ticks
  every 6 hours, a gold "now" marker, edge blur on both sides, visible ends with rubber-band and
  `Haptic.edge`, haptic per 6 hours and per day.
- **Tiles (as A):** how lit and growing or shrinking; rises and sets; days to full moon (and distance).
- Opens from the moon pill as a morph (MOTION.md: morph, never pop); text changes cross with blur
  (Morph refining, point 3).
- The moon itself keeps the realistic renderer and the real tilt.
- Not included unless the user agrees: "Will I see it tonight?" (moon + tonight's cloud cover).

Order: F1 → F2 → F3 → F4 → F5, each with its lab demo and a check on the Pixel 7, CI green.

## Summaries: what they say (agreed with the user 2026-11-21)

The question every summary answers: **what does this change about my day?** Research behind it (chat,
2026-11-21): people look at temperature and precipitation first; high temperature overtook "when will
it rain" as the top need between 2006 and 2022; most check in the early morning or the evening;
mornings are about what to wear; people want the numbers translated into plain words.

**Rule: one question per summary, the top 1–2 facts from a priority list** (agreed "for now"; revisit
after living with it). The app walks the list top to bottom and shows the first one or two items that
apply. **If nothing is especially useful, it says nothing extra**: no filler sentence just to fill
the space.

### Forecast: "What do I need to know about today?"
1. A weather alert, if there is one.
2. When precipitation will start or stop today ("Precipitation expected from 4 PM to 7 PM").
3. Today's high, when it comes, and **compared with yesterday** ("It will be warmer than yesterday, up
   to 28° by 3 PM"). The comparison is **"maybe do"**: first check that our sources give yesterday's
   data (Open-Meteo has past days; check the others). If a source can't, leave the comparison out
   rather than guessing.
4. Extras **only when they're extreme or matter for the day**: strong UV, strong wind gusts, poor air
   quality, a sharp drop tonight. **Humidity, pressure and ordinary wind never appear** unless extreme
   (thresholds to agree, starting from the app's existing alert/level scales).

**Evening switch:** from **6 PM** (default) the Forecast summary answers "What about tonight and
tomorrow?": tonight's low, then tomorrow's most useful fact from the same list ("Tomorrow will be
cooler, with rain in the morning"). **The time is adjustable** (a setting; agree where it lives and
how it looks with the user before building, per CLAUDE.md).

### This week: "How will the week go?"
- Keep: the rain days, the warmest day and the coldest night (wording already agreed).
- Add **both**, only when each is useful: the **trend** ("It will get warmer toward the weekend") and
  **the weekend** (e.g. which weekend day is better outside). Still at most two facts shown: the list
  order decides which.

### Rain, snow or both: "precipitation" (the user, 2026-11-21)
When the forecast could bring **either rain or snow** (snow, sleet, mixed, or the source can't say
which), texts say **"precipitation"** instead of "rain": "Precipitation expected in 20 minutes", "No
precipitation expected in the next 3 hours". **Only rain possible** keeps "rain"; **only snow possible**
says "snow" ("Snow expected in 20 minutes") (agreed 2026-11-21). **The same rule applies everywhere,
This week included** (agreed): "No precipitation expected this week"; the umbrella lines become, for
snow, "It will snow on %1$s" / "It will snow on %1$s days this week" / "It will snow most of the week",
and for mixed, "Expect rain or snow on %1$s" / "…on %1$s days this week" / "…most of the week" (draft
wording: show the user before shipping). Week rule: a day counts as snow or mixed by its own
precipitation type; the week's sentence uses "precipitation" or "rain or snow" if its days differ.

### What to wear
Not in these summaries. It belongs to the proactive channels (daily brief, notifications); see the
research queue.

### Onboarding helps set this up
The redesigned first-time setup (log 2026-11-18: where are you, where the weather comes from) should
also **help the user configure the app to their needs**: e.g. when their evening starts (the summary
switch), and other choices that change what Solstice tells them. Which steps, in what order, and how
few: to propose and agree with the user (keep it very short; everything changeable later in
settings).

### Build notes (for the code environment)
- One summary engine per page: an ordered list of rules, each `(applies?, text)`, first one or two
  that apply win. Shared, unit-tested, no phone needed. Not hand-written `if` chains per screen.
- Texts follow WRITING.md (future tense, natural, one fact per sentence, 12-hour times).
- Every new sentence's exact wording goes to the user before it ships.

## Research queue (from the user, 2026-11-21)

1. **The page receding behind a sheet, done cheaply, as a `:motion` piece.** We dropped it because
   scaling the live page made the glass and edge blur redraw every frame (search 22 % janky with it,
   8 % without). Look at existing implementations first (iOS's sheet presentation; Android and Compose
   libraries that scale the presenting screen) and at the cheap route we already use for the tab jump:
   draw a frozen image of the page and scale that while the sheet is up (the glass then samples a still
   picture). Bring options and measurements to the user.
2. **Long press, properly.** Study how iOS's context menu works (the item lifts, the rest blurs, the menu
   grows out of the item, and holding then moving turns it into a drag) and how Android and Compose
   handle the same, so a long-press menu on place cards can live alongside drag to reorder (hold still:
   menu; hold and move: drag). Then propose, as a `:motion` piece.
3. **Proactive channels** (DESIGN.md "What Solstice is for"): Android 16's Live Updates (promoted
   progress-centric notifications: status bar chip, lock screen, top of the shade), whether a weather
   case qualifies (Google describes them for user-initiated start-to-end journeys like rides, deliveries
   and navigation), and how they end on their own; plus a daily brief and clothing recommendations.
   Bring what's possible, and what each would say, to the user.

## Motion test checklist (added 2026-11-18)
Things to try on the phone before calling any motion done (debug and release builds):
- **Tapping toolbar tabs at the extremes:** from Outlook straight to Places (and back) the pager
  passes over Forecast: check the plain page fade, the sky, the highlight colour (sky colour on the
  way through Forecast?), the folding labels and the icon colours all stay smooth and don't flash;
  same when tapping during an ongoing swipe or animation (interrupt mid-way).
- **In-between states of the title hand-off:** half-scrolled titles (label half unfolded), a swipe
  between pages while a list is half-scrolled, scrolling while the pager is mid-swipe, fast flings
  and reversing direction; the label must never show twice or jump.
- Every gesture: hold still, release, reverse mid-way, interrupt with a second touch.

## Risks
- **Upstream merges:** F3 rewrites `ManagementFragment`; conflicts there are likely. Mitigation:
  new file next to it, switch over in one small edit; merge upstream often.
- **Reordering in Compose:** needs careful drag/scroll/haptics and TalkBack actions
  (move up/down) as an alternative.
- **Swipe conflict (settled 2026-11-18):** the sideways swipe on Forecast goes to Locations, so
  upstream's swipe between locations is dropped; locations are switched from the Locations list.
  Watch for conflicts with horizontally scrolling blocks on Forecast (hourly/daily charts): those
  scroll first, the page swipe takes over only at their ends or from the screen edge.
- **Current location title:** needs a reverse-geocoded place name. Upstream's Nominatim address
  lookup currently fails in this fork ("Required API key missing"), so fix that first or use
  another reverse-geocoding source; fall back to "Current location" + icon when no name exists.
- **Tab shell vs `MainActivity`:** upstream's activity (910 lines) manages the pushed page and drawer;
  replacing that touches more upstream code than other phases. Keep the edit to one switch-over.
- **Expressive APIs are experimental:** pin `material3`, expect API changes on updates.

## Open questions
1. Launcher icon: direction chosen (sun + wind swirl); exact drawing still to agree.
2. Contribute generic fixes back upstream (e.g. the location list rewrite)?
3. Distribution: GitHub releases only, or also an F-Droid-style repo (e.g. Obtainium/IzzyOnDroid)?

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
- `settings.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`: `:motion` module and
  the Haze dependency (2026-11-19). (`DebugSettingsScreen.kt` had a Haze test switch; removed again,
  so it matches upstream.)
- Added: `motion/` (Apache-2.0 module, 2026-11-19).
- Added: `res/values/strings_expressive.xml` (new strings kept out of upstream's `strings.xml`;
  English only so far).
- Added (no upstream equivalent): `.gitattributes` (LF, so Spotless passes on Windows),
  `app/src/main/res/font/google_sans_flex.ttf`, `third_party/fonts/GoogleSansFlex-OFL.txt`,
  `tools/make_icon.py`, `app/src/main/kotlin/org/breezyweather/ui/expressive/**`.


## Backlog (user notes, 2026-11-18)

### Foundations
- **Consolidate systems and components.** A change on the main screen today has to be repeated by
  hand in the postcard window (and other copies). Shared building blocks instead: one toolbar, one
  page scaffold, one card, one message, one sheet.
- **Blur:** look at how Android 17 blurs (live, not baked, and apparently cheap) and move glass and
  edge blur onto it where possible.
- **Frameworks for everything:** a spacing scale, a type scale, motion tokens and component rules,
  applied consistently and following UI/UX principles.
- **Review the design against Apple's Human Interface Guidelines** (developer.apple.com/design) and
  note what to improve.

### Visual polish
- Bolder text on titles.
- Square star stamp.
- A drop shadow on postcards.
- A unique grain for every picture (seeded per place, or per place and condition), so condition
  pictures don't repeat everywhere.
- Make the skies animated, or more obviously animated: they don't seem to move.
- Pinned icon: an outlined one that follows the Forecast colour.
- Search: the "Search somewhere else" field gets the same translucent material as the results.
- Search results: "Select" becomes **"Visit"**.
- Postcard window: no background behind the "Visiting" text.
- Sun path: no AM/PM on the sunrise and sunset times.

### Behaviour and fixes
- **Search naming:** searching "Puebla" adds "Heroica Puebla de Zaragoza, Centro…". Keep the name
  that was searched (look at what the geocoder refresh overwrites).
- **This week:** rename "Outlook" to "This week" everywhere.
- **This week:** reset the scroll position (when leaving the page, or when coming back to it).
- **Places:** add the same progressive edge blur that This week has.
- **Places:** a left-to-right swipe should just go to the page on the left. Probably drop the
  cards' own left-to-right swipe motion.
- **Fix:** the progressive blur shows when a swipe starts from Forecast.
- **Search card full width,** with no gap to the keyboard; test how it behaves.
- **Show the local time** when visiting or opening any place whose time differs from yours.
- **Long pages** (This week and others): the toolbar label and the page heading never show
  together. Decide the hand-off rules.
- **Sun and moon on the sun path:** the sun sometimes doesn't show; there's no moon. Design the
  night version.

### Motion (to propose and agree first)
- Toolbar tab taps between This week and Places (and back): a refined transition.
- The floating button budding out of the toolbar: refine it.
- Pin and unpin: an icon animation.
- Data loading for a place: a nice transition so values don't just pop in.
- **2026-11-18 evening (backlog items built, checked in the debug app on the Pixel):**
  - Renamed to **This week**.
  - Search results say **"Visit"**.
  - No AM/PM on the sun path.
  - Bolder titles (SemiBold).
  - A **square** star stamp.
  - A stronger, soft **drop shadow on postcards**.
  - **Search card full width**, sitting right on the keyboard: rounded as a bubble, and only the
    top corners stay round once docked. The field is more translucent.
  - **Keep the searched name:** cities from the bundled list no longer get renamed by the address
    lookup (not yet re-tested with Puebla).
  - Postcard window: the label has no background, and shows "Visiting" and/or the **local time and
    difference** when it differs from yours. The Pin icon is outlined, in the Forecast colour when
    pinned.
  - **This week scrolls back to the top** when you leave it.
  - **Edge blur** only comes in once the plain page is ~40 % in (it showed over the sky at the
    start of a swipe). **Places has the same edge blur** now.
  - **Places: a left-to-right swipe goes to the page on the left.** A card only moves when the
    swipe goes right to left (star / delete).
- **2026-11-18 evening (motion agreed with the user, built):**
  - **Tab jump This week ⇄ Places: cross-dissolve on paper.** The page fades out and shifts ~28 dp
    toward the tapped side (130 ms), the pager jumps, and the new page fades in from the other side
    on a no-bounce spring. No slide through Forecast's sky. Checked on the Pixel. The toolbar
    highlight jumps rather than gliding.
  - **Bud: liquid drop.** Once free of the pill, the bubble wobbles (overshoot, damping 0.32); taken
    back in, the pill ripples a little. Not yet watched on the phone.
  - **Pin: push in.** The pin tilts, jabs down (squash, haptic) and stands up in the Forecast colour;
    unpinning pops it out with a tilt; the colour eases. Not yet watched on the phone.
  - **Loading: develop like a photo.** With no data the Forecast is faint (35 %); when data arrives
    it comes up to full over 0.7 s while a 10 dp blur clears. Not yet watched on the phone.
- **2026-11-18 evening (user feedback, built):**
  - The window always shows "Visiting" (pinned or not), with the local time when it differs.
  - Calmer pin push: 1.5 dp travel, 7 % squash, 8° tilt, damping 0.75.
  - **Tab jump is a real crossfade** (checked on the Pixel). A frozen image of the page being left
    (`GraphicsLayer.toImageBitmap`; a recorded layer alone kept showing live content) fades out over
    the new page in 300 ms, with a brief blur that clears in about 220 ms. The **toolbar highlight
    glides** to the tapped tab on its own position.
- **2026-11-18 evening (user feedback, built):**
  - **Tab jump is a real slide now** (checked on the Pixel): the frozen old page slides out to one
    side while the new page slides in from the other (380 ms, fast-out-slow-in), with a touch of
    blur. The old page is drawn outside the moving layer: that layer's blur effect clipped it.
  - **The toolbar glides slower**, on a soft no-bounce spring, and doesn't unfold the label of a tab
    it passes (Forecast).
  - **This week and Places headings line up** (Places top padding 34 dp).
  - **Bud:** the neck lets go without bouncing; the spring overshoot brought a sliver of neck back
    (the "little thing that pops out"). Not yet watched on the phone.
  - **Progressive blur while swiping:** a page only partly in view is blurred up to 6 dp, clearing
    as it comes in. It follows the finger and reverses with it (draw phase). Not yet watched on the
    phone.
- **2026-11-18 evening (user notes):**
  - **The starred place folds into a more compact version** (design to propose).
  - **Very important now: a night counterpart to the sunrise/sunset sun path** (moonrise/moonset,
    or dusk to dawn with the moon), which is missing at night.
  - **Bug:** on This week, tap Places, then quickly tap Forecast (also in reverse): things break.
    **Rework the page navigation as one system** instead of layering it on old code: one source of
    truth for the current page and transitions (pager position, tab jump slide, toolbar glide,
    window, search) that any new input can interrupt cleanly. Today the jump's coroutine,
    `pager.scrollToPage`, the snapshot and the toolbar's own position can fight each other.
- **2026-11-18 evening (agreed + built, checked in the debug app on the Pixel):** **Night sun path.**
  At night the arc runs sunset to sunrise (yesterday's sunset before dawn; tomorrow's sunrise
  after dusk). A **moon in its real phase** (from the daily moon phase angle: a pale disc with the
  shadow offset by how lit it is) travels along it, and the travelled part is drawn in moonlight
  cream. **Tap the moon** to show a small label between the two times: phase name and illumination
  (e.g. "First quarter · 62 %"). Kept very simple for now.
- **2026-11-18 evening (built, checked in the debug app on the Pixel): navigation rework.** One
  navigator in the shell (`navigate(target)`) owns every page change: toolbar taps, host requests,
  "open Places", and the toolbar slide's settle. It keeps a single job: a new navigation (or a
  finger on the toolbar) cancels the one in progress, and a non-cancellable `finally` puts the
  jump state back at rest (snapshot, fade, blur, toolbar glide) before anything else runs. The jump
  slide only starts from a settled page, otherwise it scrolls normally. Checked: This week → Places →
  quick Forecast, the reverse, and four rapid taps in a row all end cleanly on the right page; the
  This week ⇄ Places slide still works. Next: the postcard window and search should use the same
  pattern for their opening and closing springs.

### Forecast cards: precipitation and sun path (user guidance, 2026-11-18)
- **Precipitation timeline, Apple-style.** A card with a headline ("Rain stopping", "Rain starting
  in 20 min"), one plain sentence ("Rain is expected to stop in 25 min."), and a bar timeline of
  intensity for the next hour (Now, 10m … 50m), from upstream's minutely data. Fallback to the
  hourly forecast (next ~6 h) when there's no minutely data.
- **Forecast items move dynamically.** When rain is happening or coming soon, the rain timeline
  moves to the top, just under the sun path (which stays where it is for now).
- **No rain in the next ~6 h:** hide the timeline and show a small line or banner instead, e.g. "No
  rain expected in the next 6 hours" (the window follows the setting).
- **Sun path, like the reference:** the arc fills with a soft vertical **gradient** under it (warm
  by day, blue at night), the horizon line splits day from night, and the **length of daylight
  sits in the middle** between the sunrise and sunset times ("11 h 12 m of daylight").
- Mockup first, then build. The hourly card's guidance comes next.
- **2026-11-18 (user feedback on the mockup):**
  - With no rain expected, the "No rain expected…" line sits **below the hourly card**.
  - The rain card has **no separate title**: the descriptive sentence is its header ("Rain is
    expected to stop in 25 min.").
  - **All card titles describe what's going on**, so you don't have to read the data. For example,
    Hourly becomes "Going down to 16° in the next 3 hours".
  - **Night backgrounds much darker** (e.g. overcast at night).
  - **Hourly:** like the Seoul mockup, with a small icon between the time and the temperature
    (icon design direction to search later), and the descriptive text as the title.
  - **Sun path text is dynamic:** by day the sun strength and UV ("Mild sun, UV 3"); within an
    hour of sunrise or sunset, the time left ("Sunset in 40 min").
  - **Sun/moon path shape like the mockup:** a smooth bell curve whose ends run flat into the
    horizon and fade out towards the screen edges.
  - **Option: show the feels-like temperature as the main temperature.**
- **2026-11-18 (user notes):**
  - **A smooth transition when a new background loads** (e.g. opening a place from search). Maybe
    start from a really dark sky that animates into the current condition, with the sun or moon
    visibly moving into place. To propose first.
  - **A nicer loading UI:** check with the user later.
- **2026-11-18 night (built, checked in the debug app on the Pixel):**
  - **Sun/moon path:** a smooth bell between the two times; the ends run flat along the horizon
    and fade out at the screen edges. A soft gradient fills under the curve (warm by day, cool blue
    at night). The middle text is dynamic: sun strength and UV by day ("Mild sun, UV 3"),
    "Sunset in 40 min" / "Sunrise in 40 min" within the last hour, and the moon phase at night
    when tapped.
  - **Hourly card:** a descriptive title ("Going down to 11° in the next 5 hours"; up; steady; or
    rain from a time) and a small condition icon between temperature and time.
  - **Rain:** with rain now or coming, a card under the sun path whose header is the sentence ("Rain
    is expected to stop in 25 min."), with bars for the next hour (minute data) or the next 6 hours
    (hourly chance). Otherwise "No rain expected in the next 6 hours" sits below the hourly card.
    The rain card wasn't seen (no rain right now).
  - **Night skies about 45 % darker** (checked: overcast night).
  - **Option: feels-like as the main temperature** (Settings → Main screen); the line under it then
    shows the actual temperature. Not yet toggled on the phone.
  - **Edge-to-edge tab jump:** speed matched to a neighbouring tab change (slide 280 ms, toolbar on
    the same spring as normal).
- **2026-11-18 night (user notes):**
  - The moon phase text is always present at night; tapping it will open a floating card with
    more moon info (the user will show the design later).
  - When starring a place, show a message: "This is now your main place".
  - **This week redesign (to mock up first):** temperature range bars ("blobs") per day with a
    gradient, warmer orange at the top down to bluer, whose colours follow how hot or cold each day
    is; as one card like the Seoul mockup; maybe the condition icon somewhere; and a **dynamic
    summary card at the top** with the week's general conditions.
- **2026-11-18 night (user picked): This week, design B (vertical pills).**
  - A summary card on top, more verbose, with a small condition icon inline when a condition is
    mentioned ("Expect [rain] on Saturday") and temperatures in **bold and underlined**, ready to
    become links.
  - Then one card with the days as vertical pills placed at their temperature range, warm at the
    top fading to cool, the colours following how hot or cold each day is, with the condition icon
    and day name under each.
  - Ready for a future feature: tapping a day (in the pills, or a day named in the summary) opens a
    floating card for that day.
- **Moon card (user's design references):** a floating card like Apple's Moon panel. A large moon in
  its phase, the phase name and the date and time, and a **dial** of days to scrub through phases
  (Tue · Today · Thu · Fri ticks). Then rows: illumination, moonrise, moonset, next full moon, and
  distance if we have it. Adapted to the data we have. Liked: the dark starry night of the second
  reference.
- **2026-11-19 (built, checked in the debug app on the Pixel):**
  - **This week B:** a summary card (headline like "2 wet days this week"; a sentence with an inline
    condition icon; temperatures bold and underlined), then one card with 7 day pills placed at
    their low-to-high range on a shared scale, gradient from the day's high colour to its low
    colour (cold blue to warm orange), with high and low values, a condition icon and the day
    (wet days in orange). Tapping a pill calls `onDay(index)`, ready for day cards (nothing opens
    yet).
  - **Moon button:** always visible under the sun path, outlined: a small moon in its phase, the
    phase name and illumination.
  - **Moon card** (`shell/MoonCard.kt`), a floating dialog: dark starry night, a big drawn moon in
    its phase, the phase name and date, a **dial** of days (drag to move through ±14 days, ticks
    every 6 h), and rows for illumination, moonrise, moonset, next full moon and distance, computed
    with suncalc for the place.
  - To refine later: a nicer moon texture (drawn "seas" for now), and the dial's day labels
    (Tue · Today · Thu like the reference).
- **2026-11-19 (built, checked in the debug app on the Pixel):** The moon phase button is back between
  the sunset and sunrise times (at night). A rain-cloud icon (`components/SolsticeIcons.RainCloud`,
  a cloud with three slanted drops) replaces the umbrella. The moon card is **black glass**: a
  rounded, translucent black window, with everything behind it blurred (`FLAG_BLUR_BEHIND`, radius
  70; the window background blur didn't apply).
- **Realistic moon research (2026-11-19):** there's no ready-made Android library. Options:
  - **Recommended: our own renderer.** Build it on NASA SVS's **CGI Moon Kit** colour map (free, LRO
    data, credit "NASA's Scientific Visualization Studio"; confirm NASA's usage guidelines),
    downscaled to ~1–2k and bundled. An AGSL shader (Android 13+; a baked bitmap otherwise) maps it
    onto a sphere, lights it by the phase angle with a soft terminator, adds faint earthshine on the
    dark side, and could add libration and tilt later. One component for the button, the moon card
    and the sun path.
  - **Reference only:** akrea-at/moonphase (Android, photo-realistic from NASA maps, terminator,
    libration, earthshine) is **GPL-3.0**, so we can't copy its code into this LGPL app; read it
    for the approach. Lunaria (web, astronomy-engine) is a reference for the lighting maths.
- **2026-11-19 (user notes):**
  - **Plan: an opening animation for the moon button → moon card.** Never a simple fade-in: things
    always **morph into other things** (the outlined pill grows into the card). Propose before
    building.
  - **Moon card background works like the search card:** only darken the page a bit, and blur only
    behind the card (the shell's glass), not the whole screen. Its size and position are to check
    with the user later.
  - **Keep things consistent across the app** (same materials, same morph language, same components).
  - Realistic moon: the user likes the NASA CGI Moon Kit and Lunaria directions.
- **2026-11-19 (user notes, moon dial):** a haptic tick on the dial as each day passes; double-tap
  the dial to go back to today; and a marker for today on the dial.
- **2026-11-19 (built, checked in the debug app on the Pixel):** The moon card is now an overlay in the
  shell on the **same glass as the search card**: the page is darkened 25 % and only the area behind
  the card is blurred (the dialog window and its full-screen blur are gone). Taps on the card stay
  on the card. Back closes it. **Dial:** a haptic tick each time a day passes the needle; dragging
  left goes forward in time with the ticks following the finger; **double-tap goes back to today**
  (spring); an orange dot marks today and moves with the dial. The opening morph and the card's
  size and position are still to agree.

### Next session (2 am, 2026-11-19): user's list
**Priorities, in order:**
1. **Rework what needs redoing and make everything consistent:** spacing, materials, components.
2. **The collapsing animation when starring a place** (the starred place folds into a compact
   version).
3. **All the small fixes below,** plus the realistic moon and how to implement it (NASA CGI Moon
   Kit 1k colour map; download approval still pending).
4. Commit and push everything.

**Foundations**
- **Very important: unify systems.** See what can be reused, and do better with existing
  open-source libraries, keeping the app light and performant from now on.
- **Check that all blur materials are consistent** and come from the same system.
- **Blur behind all card backgrounds,** including the moon button.
- **Pick an icon style:** the user likes Google's new Material icons (Material Symbols). Find a
  ready-made library we can properly use.
- **A reusable "move and transform into place" system** (search for libraries or build one), used
  everywhere, buttons included. First use: while a visited place loads, show "Visiting Tokyo ·
  2:33 PM, 14 hours ahead" under the title; when it's loaded, the buttons move and transform into
  their positions.

**Forecast and window**
- **Text colour rules:** in dark mode, opening a place where it's day, the "Visiting" text doesn't
  adapt to the background. All text on the Forecast page follows the sky's rules; only the This
  week page adapts to any background.
- **Pin in the window:** white outline; when pinned, filled (white fill in dark mode, black fill in
  light mode).
- **Toolbar buttons:** a slight gradient, lighter on top and darker at the bottom, derived from each
  button's own colour (not white/black).
- **Moon button:** remove its small moon icon (redundant). Use a proper moon-phase icon from the
  icon library on the Forecast page.

**Moon card**
- **Check its size:** compare a floating card with a full moon page (pros and cons).
- **Make it consistent with our design** (it looks straight out of iOS now). The user likes the
  direction of the This week page.

**This week**
- **Shorter pills,** with more spacing between the pills and the text above and below.
- **Wording:** "It's raining two days this week" (instead of "2 wet days this week").
- **Card background more obvious.**
- Maybe a **precipitation element** for the week.

**Places and search**
- **Remove the grain paper effect** from the Places background for now, and use the same
  background and fade-in system as This week.
- **A button to clear recent searches.**
- **Later fixes (user notes, 2026-11-19):**
  - **Rendering past the edges when the page recedes.** Opening search scales the page down a little
    (keep that animation), and you can see that the page background and the progressive edge blur
    don't render outside their original bounds; the postcards do. Extend the background and edge
    blur a bit beyond the visible area (e.g. draw them with overscan), so nothing looks cut while
    receding. The same receding animation will be used when opening floating cards, so it has to
    work everywhere.
  - **Tab tap speeds:** tapping a neighbouring tab still looks faster than the edge-to-edge jump.
    Use the average of the two speeds for both, only for icon taps (swipes are unaffected), and add
    a little spring to the edge-to-edge jump.
- **2026-11-19:** **AccuWeather disabled** (no longer a selectable or default source; places that still
  name it skip it quietly). The to-do list now lives in **`docs/ROADMAP.md`**, sorted by app area and
  by Functionality / Fix / Refinement; this file keeps decisions and the log.
- **2026-11-19 (2 am session, built, checked in the debug app on the Pixel):**
  - **Realistic moon:** NASA SVS CGI Moon Kit colour map (1k, WebP 82 KB, credit in
    `third_party/nasa-moon`) on a sphere via an AGSL shader (`shell/MoonRenderer.kt`), lit by the
    phase with a soft terminator, limb darkening and faint earthshine. Used in the moon card and
    on the night sun path; the drawn moon stays as the fallback below Android 13.
  - **Moon button:** no icon (it was redundant). **Moon card** opens over a postcard window too.
  - **Dial:** a firm tick per day and a light tick per 6-hour step; **inertia** (decay, then it
    settles on the nearest 6-hour tick); an **arrow** at the edge points to today when it's out of
    view.
  - **Places:** the plain page (no grain), the same as This week, with its edge blur.
  - **This week:** shorter pills (84 dp) with 8 dp space above and below; "It's raining two days
    this week"; a more visible card (9 % tint plus a hairline).
  - **Window:** the "Visiting" text follows the sky (checked: Paris by day in dark mode). The pin
    is an outline, filled once pinned, in the toolbar's text colour.
  - **Search:** a "Clear" button next to Recent. Hiding the keyboard expands the card to full
    height and runs it behind the navigation bar. Back already closes it after the keyboard.
  - **Fold like paper (agreed):** when another place is starred, the current location's picture
    folds up about its top edge and the card shrinks to a one-line strip ("➤ Miguel Hidalgo ☁ 12°");
    starring it again unfolds it.
- **2026-11-19 (built):** The current location always sorts first (then the starred place). Forecast's
  night path keeps the plain drawn moon (80 % shadow, no halo). The realistic moon in the moon card
  **turns like the real one**: suncalc's bright-limb position angle sets where the light comes
  from, and the parallactic angle at the place and time turns the whole disc (texture and light),
  so it rotates through the night and flips in the southern hemisphere. Its shadow is 80 % dark.
  **`docs/WRITING.md`** is the worksheet for tomorrow's conditional-text writing session.
- **2026-11-19 (built, checked in the debug app on the Pixel):**
  - **Moon rotation calmer.** The orientation is taken when the moon is highest in the sky within
    ±12 h of the shown time (not at that exact moment, which spun it and showed its tilt below the
    horizon), so it changes gently day to day.
  - **Phase icons on the dial:** a small moon icon above the tick where each principal phase
    begins (new, first quarter, full, last quarter), within ±15 days.
  - **Places:** the current location's name is red. When it's the starred place, its weather
    caption reads "YOU ARE HERE". A different starred place **folds into a 64 dp strip under the
    current location** with just its name and the star stamp, which slides into the strip; with no
    current location it stays a full card.
- **2026-11-19 (built, checked in the debug app on the Pixel):**
  - **Moon rotation, smooth and real:** the real parallactic angle at the shown time while the moon
    is up, easing to upright as it nears and drops below the horizon (smoothstep on altitude −5°…15°),
    then a no-bounce spring. Both inputs are continuous in time, so scrubbing turns it gently.
  - **Outline moon-phase icons** from Weather Icons (`wi-moon-alt-*`, SIL OFL 1.1, credited in
    `third_party/weather-icons`), as `moon_phase_0..7` vector drawables. Used on the dial where a
    phase begins (fading towards the edges like the ticks) and for the moon on the Forecast sun path.
  - **Sun path:** the curve and horizon fade out in a soft circle around the sun or moon, so the
    line never runs underneath it.
  - **Places:** the current location is always a strip on top (red arrow and name; "YOU ARE HERE"
    plus the star stamp when it's the main place; otherwise its weather and temperature). A different
    starred place folds into a strip under it with just its name and stamp. The stamp is centred
    on the strip.
  - **Places scrolls under a transparent status bar** like This week.
  - **Toolbar labels centred** in their tabs; a folding label only takes the width it has unfolded.
- **2026-11-19 (7:30 AM list, built; not checked on the Pixel, which wasn't connected):**
  - **Moon tilt calmer:** the tilt is averaged over the 9 hours around the shown time (snapped to
    the hour), then followed by a heavy critically damped spring (stiffness 12). Each 6-hour tick
    no longer rocks it, and a flick of the dial mostly passes the moon by; it turns into place once
    the dial settles.
  - **Main screen:** the starred place shows its local time under the name ("[clock] 2:33 PM, +1 h")
    when it's in another time zone (not in a postcard window, whose label already has it).
  - **Places:** on the current location's strip, the star and the weather icon and temperature
    are red like its name.
  - **Rain card redesigned:** sentence header ("Light rain expected in the next hour", "Heavy rain
    expected in 15 minutes"; light < 2.5 mm/h, moderate < 7.6, heavy above), pill-shaped bars with
    a light-to-deeper blue gradient (dots where dry, no marker lines), times under the bars (Now,
    10m… or the hours), the amount in the window, and an Amount | Probability switch (minute data
    takes each hour's probability). The card turns light blue when rain is within the hour; the sky
    already switches to rain once it's raining.
  - **Search:** with the keyboard hidden, swiping the card up until it hits the top brings the
    keyboard back.
- **2026-11-19 (8 AM, checked in the debug app on the Pixel with Paris):** the rain card is finer,
  Apple-style: one thin pill a minute for the next hour, or one every 5 minutes over 6 hours,
  eased between the source's steps (dots where dry). It opens on Probability when the amount rounds
  to nothing ("Barely any rain to measure" on the Amount side). Red star on the current-location
  strip confirmed.
- **2026-11-19 (checked on the Pixel with Paris):** rain card switch uses icons (agreed: Material
  WaterDrop for amount, Percent for probability). Probability is now hourly over 6 hours: a grey
  pill is 100 %, filled from the bottom with a softer violet-blue gradient, the percentage on top,
  hours centred under. The chart keeps one 80 dp height in both views and the caption is one line,
  so switching no longer resizes the card.
- **2026-11-19 (checked in the debug app on the Pixel):**
  - **Star sticker red everywhere** (not only on the current location).
  - **Moon card redesigned (design A, agreed):** works like the search card. It's on the same glass,
    grows out of the moon button and shrinks back into it (the button hides while it's open), and
    has a handle on top. It's docked at the bottom with one size only: open is the card's full
    height, measured from its content. To close it, pull it down anywhere on the card (past 110 dp,
    or a flick), tap the dimmed page or go back. The page recedes like it does for search. Inside:
    the phase as a big left heading with the date, the moon (stars only on dark glass), the dial,
    and the details as a sentence with bold underlined values ("The moon is 70 % lit and growing.
    It sets at 02:05 and rises again at 14:22. Full moon in 5 days, 386,643 km away.").
- **2026-11-19 (checked in the debug app on the Pixel):**
  - **Moon card material:** tinted with the forecast's sky (its base colour). It's darkened when
    it's night at the place, so the card goes dark mode with light text and stars, and lightened by
    day with the sky's text colour. It blurs the screen it was opened from, home or a postcard
    window, not always the home page.
  - **Real blur on the Forecast cards:** the sky alone is recorded in its own glass source (home
    sky and each postcard window's sky, provided as `LocalSkyGlass`). The Forecast's glass cards
    (hourly, rain) blur it under their tint, so the grain and glow soften behind them like
    frosted glass. Without a sky source (older Android) they keep the plain tint.
- **2026-11-19 (agreed and checked on the Pixel):**
  - **Sheets open to full screen height:** the moon card and search both go up to just under the
    status bar (search above the keyboard). There's no compact state on opening.
  - **Moon card morph refined:** the button's label morphs into the heading. The phase name slides
    and grows from the button's middle to the heading's place (labelLarge to headlineMedium), the
    "· 70 %" fades, and its colour turns from the forecast's text to the card's. The rest rises in
    (32 dp, from 65 % of the way). Closing plays it backwards.
  - **Progressive close:** pulling the sheet down drives the morph itself (420 dp of pull = fully
    closed), so it shrinks towards the pill under the finger. Let go past a quarter, or flick, and
    it finishes closing; otherwise it springs back open.
- **2026-11-19 (trial, checked on the Pixel):** the moon morph is now built on Compose's own shared
  bounds (container transform) instead of hand-written layout. `MoonSharedLayout` wraps the shell in
  a `SharedTransitionLayout` with one seekable transition (`MoonCardState.seek`). The button and the
  sheet share bounds (`moon-<place>`), and the button's phase name and the heading share bounds
  with scale-to-bounds (`moon-<place>-label`). Opening and closing animate the seekable state, and
  a pull seeks it (measured on the whole screen, since the sheet shrinks under the finger). A tap
  above the sheet closes it. Progress for the dim, the page recede and the rising content comes
  straight from the seekable state.
- **2026-11-19 (agreed: the user's motion reference, now `docs/MOTION.md`; checked on the Pixel):**
  - New motion system, `SolsticeMotion.kt`:
    - container spring 0.78 / 380;
    - control spring (no bounce, MediumLow);
    - content spring 0.85 / 400;
    - settle spring;
    - rubber-band (0.55);
    - fling projection.
  - Moon sheet rebuilt on it. A gesture `Animatable` drives the morph, and the shared transition
    is seeked to it, with linear bounds so the spring is the only curve. Release velocity feeds
    the spring. A drag catches it mid-flight. The target comes from the projected fling.
  - Pulling up past open rubber-bands: the sheet stretches up from the bottom, and the spring's
    overshoot shows the same way.
  - No container crossfade: the glass is the pill grown from the first frame. The pill's border
    and "· 71 %" fade in the first tenth (and come back last).
  - Content is choreographed: it fades and scales from 0.92 between 35 % and 70 % of the morph,
    and leaves first when closing.
  - Known: while scrubbing, the shrinking heading can overlap the date line for a moment.
- **2026-11-19 (performance and polish, measured on a release build on the Pixel):**
  - **Jank fixes:**
    - The home sky stops drifting while it's covered (Places, This week, a postcard window).
      Places went from 80 redraws in 3 s while idle to none.
    - The edge blur no longer creates a new blur effect every frame.
    - Glass blurs a quarter-size backdrop.
    - The moon map is decoded once, in the background.
    - The dial's phase marks are worked out off the main thread, and rise, set and full moon
      per 15 minutes of dial time.
    - The card's body is built a frame after the heading.
    - The morph stays one continuous seek, never settled at the bounce or turned round to close.
    - Result: moon open and close went from 51 % to about 10 % janky frames, with no spikes during
      the motion.
  - **Polish:**
    - The sheet's content is laid out once at its final width (no reflow while the glass grows).
    - The date line shows only once the heading has landed.
    - The sheet's glass fades in over the first 30 % while the pill's outline and "· 71 %" fade
      out, and the reverse on closing.
    - The label hands over to the heading in the first 15 %.
    - The pill's own content scales with the shape.
    - Closing uses a no-bounce dismiss spring (`SolsticeMotion.dismiss`).
  - **Still to check:**
    - A dark pill-sized frame at the very start of the open.
    - A slight drift of "· 71 %" at the end of the close.
    - A hitch after the close has finished, while the card is taken down.

- **2026-11-19 (proposed, not built):** **Motion framework** plan added (section "Motion framework"):
  one motion value per interaction, geometry computed from anchors instead of a seeked
  `SharedTransitionLayout`, choreography as windows on that value, springs written as response +
  damping like iOS, UIKit's release rules (from Glasmio's notes), packaged as a standalone `:motion`
  module. Steps M0 to M5; M0 (agree the plan, tokens, name and license) is next.
- **2026-11-19 (research, not built):** UI systems audit and library check, `docs/UI_SYSTEMS.md`.
  Found: Glass/EdgeBlur re-implement Haze; ReorderList re-implements Reorderable; 3 transition
  systems; 2 sheet systems; only 5 Material 3 component calls; no spacing/shape/colour tokens.
  Custom morph code stays justified for scrubbed overlay morphs. Added "Design system foundations"
  and four additions to "Motion framework" (Solstice `MotionScheme`, `AnchoredDraggable` for snap
  swipes, predictive back everywhere, Navigation 3 for pushed screens).
- **2026-11-19:** CONTINUE.md brought up to date (state as of 2026-11-19, "Pick up here" for the next
  session); status line updated. Plan branch merged into main.
- **2026-11-19 (review, not fixed):** code review of `98647ef`…`563f57c`. Two `MotionValue` bugs to fix
  before M3: catching an overshoot jumps back (rubber-band applied twice after `stop()`), and
  `motionDrag` doesn't catch a moving surface until the touch slop. Plus smaller notes (Morph content
  alignment, per-frame shape, reduced motion is a snap, `control` = `settle`) and what the token pass
  left. Details and next steps in CONTINUE.md, "Code review of those commits".
- **2026-11-19:** Safe review leftovers: CI runs `:motion:test`; reduced motion documented as a jump
  (Compose finishes animations instantly with "Remove animations"); `motionDrag` sets its flick
  thresholds in a `SideEffect`. Still for the user: `Morph` content alignment, `control` vs `settle`.
- **2026-11-19:** Navigation 3 decided by the user; migration started (not on `main` at the time of
  this entry). Next task: **consistent Material icons** (section "Icons"; inventory: 3 sources,
  34 distinct `Icons.*` in `ui/expressive/`, 40 Outlined vs 14 Filled, 54 upstream `ic_*.xml`).
  CONTINUE.md rewritten around it; finished ROADMAP items ticked.
- **2026-11-19: icons on Material Symbols Rounded** (decided with the user; PR #1). `SolsticeIcons.kt`
  is the one Compose catalog (39 symbols, optical sizes 24 and 20, 3 filled variants), generated by
  `tools/make_symbols.py` from `tools/symbols.txt`; all 78 `Icons.*` call sites (22 files, 12 of them
  upstream, import and name changes only) use it; icons drawn at 20 dp or smaller use optical size
  20. The 53 `ic_*.xml` drawables keep their names with Rounded artwork (they were Symbols Outlined
  and Pictogrammers icons). Weather conditions use the Symbols weather glyphs (`sunny`,
  `clear_night`, `partly_cloudy_day`/`_night`, `rainy`, `weather_snowy`, `foggy`) in one mapping
  (`hourIcon`, also used by the toolbar); the custom `RainCloud` is gone. `material-icons-extended`
  removed; CI step "No old Material Icons". CI green; **not yet checked on the Pixel 7**.
- **2026-11-19:** Read M3's icon guidance ("Applying icons") in the user's browser and checked the
  icon rules against it: style, weight, fill and optical size match. Added to PLAN.md "Icons" as
  gaps: grade -25 for light icons on dark backgrounds (an M3 default, not an extra), the selected
  toolbar tab filled, an audit of small icons' labels, touch targets (48 dp) and icon/text
  alignment, and optical size 40 for big icons.
- **2026-11-19: selected toolbar tab filled** (M3 gap 2). `calendar_month`, `map` and the 9 condition
  glyphs got `fill` in `tools/symbols.txt`; `hourIcon` takes `filled`; a toolbar `Tab` carries `icon`
  and `iconOn`, swapped once the tab's title is half unfolded (a swap, no animation). Checked on the
  Pixel 7: Days, Forecast and Places each show their icon filled when selected, the others outlined.
  Also: this PC had built the same icon task a second way (Symbols library); it was reverted on main
  in favour of the `material-symbols` branch, which is now merged into main.
- **2026-11-19: M4 started, search on `:motion`** (option B, agreed with the user). New
  `components/SolsticeSheetState.kt`: `presence` (0 folded into the button, 1 open; opens on
  `Motion.container`, closes on `dismiss`, so the close no longer overshoots) and `detent` (px: compact
  0, tall `room`, dismiss at -220 dp; UIKit release rule, rubber-band 80 dp past tall). A touch catches
  it mid-open or mid-close and drives `presence`; resting at the dismiss point closes and carries the
  fling's speed into `presence`; thrown back up while closing, it reopens. The page behind scales and
  the scrim fades with `backdrop()` (open, less as it's pulled). Search's tweens and loose springs are
  now tokens (`content`, `snappy`, `settle`, `control`). `:motion`: `MotionValue.restPoints` can change
  (and `rememberMotionValue` applies changed ones), `release` returns the chosen rest point. Checked
  on the Pixel 7 (profile build): opens tall, a slow short drag springs back, a fast flick from tall
  settles compact (one step, like iOS), a flick from compact dismisses into the button.
- **2026-11-19: search can be caught mid-close** (the user couldn't before). While the sheet moves, a
  layer over the whole card catches a touch at once (the handle and list alone were faded out);
  a dismiss holds the card where it was let go (no further slide to the dismiss point) and holds the
  keyboard's height, so it doesn't fall with the keyboard; thrown back up it reopens from where it is
  (`reopen()`). `MotionValue.targetFor` picks a rest point without moving. Checked by the user: it can
  be caught now. Open: the keyboard, still sliding away, takes touches at the bottom; idea (to agree):
  start hiding it once the card is pulled below compact.
- **2026-11-19: M4, the rest of the shell on `:motion`** (options agreed with the user).
  - **Tokens:** `Motion.playful` (0.5 damping, ≈0.31 s: pops, squeezes, the neck, landing bumps) and
    `Motion.wobble` (0.32: the liquid drop and ripple), for expressive moments only.
  - **Toolbar:** every spring is a token now (wobble and ripple on `wobble`; neck `playful` / `settle`;
    pin jab `container`; tab pop `playful` in, `container` out; press squeeze `snappy` in,
    `playful` out). Same liquid look.
  - **Places drag:** lift `snappy`; the drop settles on `settle` carrying the finger's vertical speed
    (VelocityTracker); neighbours' bump `snappy` then `playful`; star-to-top glide `container` up,
    `dismiss` down; list placement `settle`.
  - **Postcard window:** `presence` and `pull` MotionValues instead of one spring. Opens on
    `container`, closes on `dismiss` (no bounce). **Swipe down to close** (iOS zoom style): at the
    top of the page a pull follows the finger, shrinks the window up to 12 % and rounds its corners;
    a release past 260 dp × ½ (or a flick, UIKit rule) folds it into its postcard carrying the fling,
    otherwise it springs back. A touch catches it mid-open or mid-close; thrown back up it reopens.
    Its Forecast has no pull-to-refresh (`ForecastPage(pullToRefresh = false)`), so the pull reaches
    the window. Predictive back still works.
  - **Icon fix:** the window's pin was invisible: Google's `push_pin` SVGs are still in the old
    format (24 px grid, no viewBox) and the generator drew them on the 960 grid. `make_symbols.py`
    now reads each file's grid (`symbol(grid = …)`); only `push_pin` was affected.
  - Checked on the Pixel 7 (profile): window opens; a short slow pull springs back; a longer pull
    closes it into the postcard; mid-pull it follows the finger, shrinks and rounds; the pin shows.
    Not checked by hand yet: toolbar and Places feel, catching the window mid-close.
- **2026-11-19: M4 finished: every animation in `ui/expressive/` is a motion token** (agreed with
  the user), except three documented exceptions: the stamp's sticker peel (560 ms keyframes), the
  Forecast "develop" (700 ms) and the moon's slow parallactic drift (≈1.8 s). New token
  `Motion.gentle` (≈0.44 s / 1, the page swipe settle; same feel as before). Messages rise on
  `container`, sink and fly off on `dismiss` (keeping the flick's speed), spring back on `settle`; the
  page jump's haze `snappy`, fade `content`, toolbar `settle`; postcard press `snappy` in /
  `playful` out, fold `content`; Places swipe actions spring back on `container`, delete on `dismiss`;
  the moon dial settles on `settle`; screen push on `settle` (same values). Reorder elevation and the
  press-morph already go through the Material motion scheme (`snappy`).
  **Moon card:** stays on its own `MotionValue` (decided with the user): it already follows every
  motion rule; its pull plays the morph backwards and it has no compact size, unlike search's
  `SolsticeSheetState`. Not checked by hand yet: messages, page jumps, Places swipe actions.
- **2026-11-19 (reported by the user, not fixed):** two bugs from the phone check. (A) Frames drop
  when opening the moon card and the search card; likely the page recede (`graphicsLayer` scale on the
  whole pager) making the Haze glass and edge blur redraw every frame. (B) Swiping search down, the
  toolbar's search icon is back before the card reaches it (`ToolbarDock` alpha is `1 − presence`).
  Next steps, options and the order after them (check by hand, M5 motion lab, refining rounds) in
  CONTINUE.md "Pick up here".
- **2026-11-19 (clarified by the user):** bug (B) is two search icons at once: while search folds back
  into the bubble, the card's own icon travels to the toolbar, but the toolbar's search button is
  already drawn there (the toolbar fades in with `1 − presence`). Fix: hide that button until the
  card's hand-off, then swap in one frame (CONTINUE.md 1B).
- **2026-11-19: M4 done: `PageScaffold`** (`shell/PageFrame.kt`). This week and Places (and the
  postcard window's This week) build on it: the list edge to edge, the large title (with an optional
  action, Places' Settings) in a 48 dp row 34 dp below the status bar, `Space.S4` margins, 160 dp of
  toolbar room at the end, the top edge blur. Small shifts: This week's title 4 dp higher (now level
  with Places', checked on the Pixel 7), Places a little closer under its title and 28 dp more room at
  the end. Forecast stays its own layout (centred, on the sky).
- **2026-11-19: bug B fixed (not yet checked on the phone).** The toolbar's search icon is hidden
  (`ShellToolbar(budIconShown = …)`) whenever the search card is on screen (`presence > 0` or
  moving); the card carries the icon there and back, and the real one shows in the frame the card has
  folded away. Bug A (frames dropping) still to measure.
- **2026-11-19: tokens cleanup** (no visual change: every replacement has the same value). New
  tokens: `Sizes.IconXSmall` 12, `IconMedium` 18, `IconXLarge` 28, `IconHero` 32, `HandleWidth` 36 /
  `HandleThickness` 4 (the sheets' grab bar), `Elevation.Card` 6 / `CardSelected` 8 / `Lifted` 10,
  and in `:motion` `MotionPhysics.SlowFlick` 250 dp/s / `FastFlick` 1000 dp/s (were repeated in three
  places). The `Stroke` token object is now `StrokeWidth` (it clashed with Compose's drawing `Stroke`).
  Replaced: hairline/thin/regular borders and lines, touch-target rows, icon sizes, card shadows,
  segmented-row corners, the sheets' corner radius, top gap and handle, the collapsing top bar's
  margins. Colours and shapes were already done (no hex colours or fixed `RoundedCornerShape`s
  outside the theme). **Left as local values on purpose (~94):** illustration geometry (postmark,
  stamp, sun arc, rain bars, moon dial, blur radii), motion distances (pull ranges, dismiss points)
  and component-internal metrics (toolbar tab widths, search card sizes). Rule: a value becomes a
  token when it's a design decision shared by two or more places.
- **2026-11-19: M5, the motion lab** (`ui/expressive/lab/MotionLabActivity.kt`, Settings → Debug →
  Motion lab; built, **not yet checked on the phone**). One screen with: a **slow-motion switch** (5×,
  `:motion`'s new `MotionTime.scale`, which every token's `spec()` reads; reset when the lab closes),
  a **frame meter** (fps and the share of frames over 1.5 refresh intervals, last ~2 s), every
  **token** with its response and damping and a dot to play it (one by one or all together), the
  **UIKit release rule** on a three-stop track (value, velocity and chosen stop shown; rubber-band
  past the ends), and a **morph** (a pill growing into a card, catchable and scrubbable). The
  patterns that live in the app (search sheet, postcard window, moon card, toolbar, Places drag,
  messages) are listed with where to find them; slow motion applies to them too while the lab is
  open in the background. English only (a tool, `translatable="false"`). Next: the `:motion` README.
- **2026-11-19: checked on the Pixel 7 (profile):** the motion lab (tokens, Play all, 5× slow motion,
  release track, morph; frame meter 90 fps / 0–1 % slow frames while playing) with two fixes: the
  token dots use the middle 60 % of the track so overshoot shows (playful and wobble were clipped),
  and the morph's surface is only drawn while it's out (it hid the pill's label). Bug B: closing
  search now shows one icon (the card's) until it lands. Page jump and Places star swipe work.
- **2026-11-19: M5 done:** `motion/README.md` rewritten for other projects: what each piece is, the
  token table, the seven rules, two examples (a draggable sheet, a pill growing into a card), and the
  lab. Next: the refining rounds, moon card first.
- **2026-11-19: refining round 1, moon card** (agreed with the user, checked on the Pixel 7): its
  fling is projected with UIKit's **normal deceleration** (0.998; `rememberMotionValue(deceleration
  = …)` is new), so a medium flick down (~900 dp/s) closes it as on iOS while a slow drag springs
  back; the pull **tracks the finger 1:1** (the drag range is the real distance from the sheet's top
  to the moon pill, read with the new `morphAnchorOf(key)`; 420 dp until measured). Next round:
  search.
- **2026-11-19: refining round 2, search** (agreed with the user, checked on the Pixel 7): the card's
  sizes project flings with UIKit's **normal deceleration**, so a medium flick from tall steps to
  compact and a hard one dismisses straight away; **the keyboard hides on the pull** (once the card is
  pulled below compact it starts hiding and the card holds its height; let go and it springs back,
  the keyboard comes back with it). Next round: the postcard window.
- **2026-11-20: refining round 3, postcard window** (follows the principles agreed in rounds 1–2,
  checked on the Pixel 7): its presence and pull project flings with UIKit's normal deceleration, and
  the pull follows the finger 1:1 (was 0.6×). A held pull tracks, a short release springs back, a
  medium flick folds it into its postcard.
- **2026-11-20: bug A measured** (profile build, `dumpsys gfxinfo` janky frames, 6 opens and flick
  closes each; the lab's new **Page recede** switch turns the recede off for testing):
  moon card 28 % with the recede, 21 % without; search 22 % with, **8 %** without; page swipes 1 %.
  The recede is the main cost for search; the moon card has another one too (it was 3.3 % in M3, by a
  different measure). Options to propose next: drop the recede, or recede only the content under the
  glass; for the moon card, a Perfetto trace of an opening first.
- **2026-11-20: decided by the user: drop the page recede** (bug A, option A) for search and the moon
  card; re-measure both, then trace the moon card's remaining cost (21 % janky without the recede).

