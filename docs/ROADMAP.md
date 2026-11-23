# Solstice roadmap

Everything still to do, sorted by area of the app and by type. `PLAN.md` keeps the decisions and the
dated log; this file is the to-do list. Keep it updated: tick items off (or delete them) when they
ship, and add new notes under the right area.

- **Functionality:** something new the app can do.
- **Fix:** something broken or wrong.
- **Refinement:** polish to something that already works.
- 🗓 = scheduled · ⭐ = high priority · 💬 = needs your OK (a design or motion proposal) before building.

---

## 🗓 Scheduled for today, 7:30 AM (2026-11-19)

Built 2026-11-19 morning; not yet checked on the Pixel (it wasn't connected).


- [x] ⭐ **TOP PRIORITY · Fix · Moon:** the rotation still rocks back and forth. Smooth it with a
  heavy ease so the motion isn't jerky.
- [x] ⭐ **TOP PRIORITY · Fix · Moon:** when you flick the dial, the moon should ignore some of the
  movement so it doesn't turn too much.
- [x] **Refinement · Search:** with the keyboard collapsed, swiping up and hitting the top opens the
  keyboard.
- [x] **Functionality · Forecast:** show the local time on the main screen when the starred place
  is in a different time zone from you.
- [x] **Refinement · Places:** make the star (stamp) on the current location's strip red too.
- [x] **Refinement · Places:** make the current location's weather icon and temperature red too.
- [x] **Refinement · Precipitation:** refine the rain card (reference:
  `docs/references/rain-next-hour.png`, minute bars with Now · 10m · 20m … 50m under them). Take
  the layout as inspiration only: no marker lines for the rain, and rounder shapes like the rest
  of the app.
  - add the times under the bars, a gradient, and the amount of rain;
  - a switch between probability and amount;
  - wording: "Light rain expected in the next hour" / "Heavy rain expected in 15 minutes".
- [x] **Refinement · Forecast:** when precipitation is within the next hour, the rain card's
  background turns light blue. Once it's actually raining, the sky changes to its rain state.
- [x] Commit and push.

## 🗓 Scheduled for Friday

- [ ] **Fix · Places:** dragging a card that's partly out of view breaks the drag.
- [ ] **Fix · Places:** dragging a card to the edge of the screen doesn't scroll the list.
- [x] **Fix · Moon:** the moon card doesn't open when you're visiting a place.

## 🗓 6 AM session (2026-11-21): new morph refining, research, plans

See CONTINUE.md "6 AM session": reopen while it's still closing, a stronger text blur, iOS 26/27 blur and
animation research, a velocity cap, a bit more opening bounce, the Motion Lab redesign plan (with a
comparison to Google's libraries, no weather references), and Apple's principles for the toolbar.

## 🗓 Next week: interactive glass (backlog, added 2026-11-22)

Liquid-Glass-style touch response for every glass button (`FrameButton`, toolbar, pills), as one
`interactiveGlass` piece in Motion, Motion Lab first with a switch per behaviour, then the app. Ideas
(chat, 2026-11-22; none agreed yet): press swell and springy release; a touch light that spreads from the
finger and follows it; hold (brighter, a little more swell, a haptic tick, optionally growing into a small
glass menu via the morph kinds, e.g. ✕ → "Close" / "Pin and close"); elastic stretch when the finger slides
off, cancelling past the edge; a release ripple as a bend in the glass; appear and disappear through blur;
neighbouring glass buttons merging into one shape (most ambitious); haptics from the existing tokens.
Rules: never blocks input, values from Motion tokens only, Reduce Motion keeps light and haptics but drops
stretch and scale.

## 🐞 Review 2026-11-22: bugs, fixes and what to rebuild (from chat; details per item below)

Suggested order: R3 with B3 and B4 (Motion's core), then B2's rule and D1's text, then Saturday's kit
foundations, then the kit pieces in R2's order. Each item still gets agreed before it's built.

### Bugs
- [ ] **B1 · Place time on the "Visiting" line** (`PageFrame.kt` `VisitingLine`): says "Visiting" even for
  saved places (ignores `pinned`); four different looks for a place's time (Forecast header, window
  line with a typed-in " · ", postcard caption in capitals without the clock, search). Fix: one
  place-time component in the kit (variants), shown whenever the time differs; "Visiting" only for
  previews; separator as a string.
- [ ] **B2 · Rain card at 0 %, noisy rain notifications** (`RainCard.kt` `rainOutlook`: any trace over
  0.05 mm, or a "rain" code at 0 %; snow ignored; "10m"… typed in. Upstream `Weather.hasMinutelyPrecipitation`:
  any radar reading > 0). Fix: one shared rule (PLAN.md "Rain and alerts") for card, notification
  and Live Update, with a cooldown; unit tests from real AccuWeather data.
- [ ] **B3 · Tapping after a swipe stops the tabs** (`PageSpring.kt`): the pager counts as scrolling
  until the bounce settles, and Compose's pager swallows the tap that stops a scroll; `SpringMorph`
  also takes any tap while opening. Fix: travel by scroll without overshoot, draw the bounce as an
  offset; a "settled" threshold token; lab test of swipe-then-tap.
- [ ] **B4 · Jumping across the toolbar flashes Forecast** (`springToPage` scrolls through every page;
  `Toolbar.kt:157` blends the highlight colour from the scroll position). Fix: a Motion "jump"
  (snap next to the target, spring the last page), highlight driven by the target tab; a Motion UI
  option. Replaces "tab tap speeds" and "a little spring on the edge-to-edge jump" below.
- [ ] **B5 · Blur text change too fast and distracting** (`BlurCrossfade.kt`: both texts blur at once on a
  bouncy spring). Lab options, all three: A fade-through (out, then in, no bounce), B softer blur and
  slower spring, C only what changed moves (rolling digits). Marco picks on the phone.

### Design fixes
- [ ] **D1 · This week's text:** drop "through the week", "all week", "this week" from its summary lines
  (`strings_expressive.xml` `week_*`).
- [ ] **D2 · Top edge blur bigger** (`PageFrame.kt` `pageTopEdge`: ends exactly at the status bar): the
  larger of the status bar and the camera cutout, plus an eased fade below, from tokens.
- [ ] **D3 · The "no rain" line** becomes a small card that fits the card layout.

### Framework pieces (Motion UI)
- [ ] **F1 · Cards with several pages:** one sliding indicator (like iOS's segmented control),
  animated content, tap to switch (no sideways swipe inside a card). The rain card first.
- [ ] **F2 · Card heights:** medium shows the details, full shows an explainer (PLAN.md research 5).
- [ ] **F3 · Page-transition blur** as a Motion UI option (on or off).
- [ ] **F4 · Toolbar jump transition** (B4).
- [ ] **F5 · Settings pieces:** collapsing large title, section header, rows (plain, switch, value,
  connected-button choice), slider row, picker sheet, navigation stack with predictive back, About and
  licences pages.
- [ ] **Messages in Motion UI** (PLAN.md "Messages"): queue-and-merge or stacked as options, two lines,
  accessibility timing, kinds with icons; every typed-in value to tokens.
- [ ] **Header close side option** (✕ at the end or the start), already listed above.

### Rebuilds
- [ ] **R1 · This week's temperature card** on the kit (layout kept; spacing and sizes checked); days as
  kit rows instead of plain `Surface` buttons.
- [ ] **R2 · Everything onto Motion UI:**
  - Kit as is: `Glass`, `SolsticeCard`, `SheetHandle`, `SheetShape`, `ScreenCorners`, `SheetDialog`,
    `SolsticeSheetState`, `IconBadge`, `PressMorph`, `ExpressiveChoices`, `CollapsingTopBar`,
    `ScreenPush`, `PageFrame`, `Toolbar` + `ToolbarMotion`, `Messages`, theme files.
  - Kit pieces from today's code: paged card (`RainCard`), partial sheet (moon card), full-page window
    (`PlaceWindow`), search sheet (`SearchOverlay`), reorderable list (`ReorderList`, `PlacesPage`),
    footnote line (`UpdatedLine`), place time (`PlaceTime`).
  - Main pages off the framework today: Forecast's own frame (not `PageScaffold`), "More details"
    (upstream's screen) and the empty state's plain buttons, the hand-drawn sun path with typed-in
    sizes, the 700 ms develop timer; This week's day buttons; Places' own gestures and postcards (timed
    animations and `delay()`s); the window's own pager.
  - Stays in the app, built from kit parts: sky, sun path, postcard content, moon renderer,
    summaries, rain logic. Goes: `SolsticeMotion.kt`, the lab copy.
- [ ] **R3 · Motion's core, compared with Compose, Material 3, UIKit/SwiftUI, Framer Motion, React
  Native's gesture handler:** explicit states for every interactive piece (not "are springs still
  running"; `SpringMorph` runs five values in five coroutines); declared gesture rules (together,
  exclusive, wait-for) instead of on/off flags (`shareTouchesWhile`, `morphPassThrough`); build on
  Compose's `AnchoredDraggable` + nested scroll (sheet heights) and `SharedTransitionLayout` /
  `SeekableTransitionState` (morphs, predictive back) with Motion's springs; bounces drawn, never
  scrolled; touch slop, settle and blur sizes as tokens.

### Decisions and clean-up
- [ ] **Q1 · Icons** (PLAN.md "Icons"): build the Rounded / Outlined / Sharp setting; weather conditions
  in Material Symbols for now (no animated icons).
- [ ] 💬 Later: our own weather set from Material Symbols' shapes with a Solstice colour layer (mock six
  conditions on the design canvas first).
- [ ] **Q2 · Professional feel** (PLAN.md): Advanced section for Data sharing, Gadgetbridge, Debug;
  Settings reshaped; README and docs rewritten; store text, name and About.
- [ ] **Q3 · Breezy behind an interface:** a Solstice data interface over Breezy's sources and data;
  replace upstream's UI, settings and widgets.
- [ ] **Places model** (PLAN.md "Places and travel"): current location as home, no star, automatic
  travel swap with "Home" in Places, search preview with Cancel / Add, onboarding place choice.

### Earlier bugs checked against the rebuild
| Earlier item | Status |
|---|---|
| One toolbar for home and window | Fixed by the kit toolbar (R2) |
| Tab tap speeds; spring on the edge-to-edge jump | Replaced by B4 |
| Frames drop opening the moon card and search | Likely fixed by the moon card and search rebuilds; re-measure |
| Drawing past the edges while the page recedes | Gone (recede dropped) |
| Rain card with real rain; animate amount ↔ chance | Covered by B2 and F1 |
| Precipitation element for This week | Part of R1 |
| Toolbar label and page heading showing together | A page-frame rule in the kit |
| Places: dragging a card partly off screen; no scrolling at the edge | Not fixed by the move: fix in the kit's reorderable list (shared with Forecast cards) |
| Reset Places' scroll when you leave | Small fix, or a page-frame option |
| Search keeps "Puebla" | Separate re-test |
| "Last updated" and refresh on start | Already built; duplicate roadmap lines to remove |
| Swapping main and pinned when you travel | Decided: automatic swap (PLAN.md "Places and travel") |

## 🗓 Saturday 2026-11-28, 1:00 AM

Plan in CONTINUE.md "Next session": guardrails (no-hardcoding CI check, app tests in CI), one Motion
Lab, Motion README, first UI kit pieces in Motion (if the module split is agreed), docs, phone pass.

## 💬 Proposals to bring to Marco (from chat 2026-11-22)

- [x] 💬⭐ **App layout:** Forecast keeps today's layout (2026-11-22). A2 from the layout canvas is the
  pattern for partial sheets (PLAN.md Direction 2).
- [ ] ⭐ **Partial sheet in Motion's kit** (the A2 pattern): grows out of its card, dimmed page behind,
  medium height first, then large (no separate page) for every card, grabber (drag, tap cycles heights, TalkBack), swipe down to close, ✕ at the end, room for a
  chart and setting rows. Lab page first; then which Forecast cards open one (to agree).
- [ ] ⭐ **Header close side as a Motion option:** ✕ at the end (default) or swapped with the leading
  button; mirrored for right-to-left; a lab switch.
- [ ] ⭐ **Optional, reorderable Forecast cards** with a general "most important moves to the top" rule
  (replaces the rain-only special case in `ForecastPage.kt`): on by default, the card returns to your order
  once it no longer matters, a switch to turn it off. Catalog and thresholds: PLAN.md "Forecast card catalog".
- [ ] ⭐ **Forecast spacing pass** against Material's spacing guidance, kit tokens only (hero temperature,
  sun/moon path and time stay).
- [ ] ⭐ **Moon card rework** on `motion-ui` as a partial sheet (with the kit move).
- [ ] 💬⭐ **Outlook** (This week renamed, decided 2026-11-23): propose its proactive answers (best weekend day
  to go out, notable days ahead, good sky evenings); the day list stays. PLAN.md Direction 6.
- [ ] ⭐ **What to wear card** on Forecast, always shown, today by day and tomorrow from the evening switch.
- [ ] ⭐ **Sun button and sun sheet** (decided 2026-11-23): a button in the hero like the moon's, opening a
  partial sheet with sunrise, sunset, daylight and UV. **UV first:** index number, next-hours timeline,
  what to do per level (WHO); the UV card opens it; card and button turn red at 8+ (plus icon or word).
- [ ] ⭐ **One UV rule** shared by the UV card, the sun sheet and What to wear (sunscreen suggestion).
- [ ] 💬 **Postcards in a standard look**, the physical feel kept in the interaction (Direction 3).
- [ ] 💬⭐ **Proactive alerts the Gradient Weather way** (Direction 4): alerts you choose to watch, rain or
  snow countdown as a Live Update, cooldowns, official warnings in full, evening summary, what to wear.
- [ ] 💬 Spring tokens as speed × bounce; Reduce Motion behaviour; glass on cards (PLAN.md open questions).

## ⭐ Next session priorities

1. Rework what needs redoing and make everything consistent (spacing, materials, components).
2. ~~The starred place folding into a compact version~~ (done 2026-11-19, "fold like paper").
3. ~~The realistic moon~~ (done) and the small fixes below.

---

## Foundations: systems and consistency

### Functionality
- [ ] ⭐ **Unify systems and components:** one toolbar, page scaffold, card, message and sheet,
  reused everywhere. Use well-maintained open-source libraries where they make the app lighter and
  faster.
- [ ] **A reusable "move and transform into place" system** (morphs, for buttons too). Search for
  libraries first. First use: a visited place's loading state (see Postcard window).
- [x] Motion tokens: `SolsticeMotion.kt` and `docs/MOTION.md` (2026-11-19), first used by the moon sheet.
- [x] Apply the motion system to search, the postcard window, Places drag and the toolbar (M4, 2026-11-19).
- [x] **Fix:** `MotionValue` jumps back when an overshoot is caught; `motionDrag` doesn't catch a moving
  surface until the touch slop. Add `MotionValue` tests. (CONTINUE.md, code review.)
- [x] 💬⭐ **Design tokens:** spacing (4 dp grid), sizes, shapes with continuous corners, palette file,
  hero type styles, Solstice `MotionScheme`. See `docs/UI_SYSTEMS.md`.
- [x] 💬 **Haze prototype** for glass and edge blur, measured on the Pixel 7 against `Glass.kt`.
- [x] **Component catalog** (first pass; one Sheet and PageScaffold come with M4): split `ShellPages.kt`; one Card / Sheet / Toolbar / Message / PageScaffold;
  `PlaceWindow` reuses them.
- [x] Swap `ReorderList.kt` for the Reorderable library. Pushed screens to Navigation 3: decided, migration started (2026-11-19).
- [ ] 💬⭐ **Motion framework** (`:motion` module, reusable in other projects): one motion value per
  interaction, morphs from anchors instead of a seeked `SharedTransitionLayout`, iOS-style springs and
  release rules. Plan in PLAN.md, "Motion framework" (M0 to M5); M0 = agree it.
- [ ] **Motion in its own repo** (long term, the user 2026-11-21): `:motion` (Apache-2.0) and the
  Motion Lab move out into their own repository, where Motion work then happens; Solstice depends on
  it. Until then keep `:motion` free of app code (no Solstice imports) so the split stays a move.
- [ ] **Design frameworks:** spacing scale, type scale, motion tokens, component rules.
- [x] 💬⭐ **Icon style:** Material Symbols Rounded, one catalog (`SolsticeIcons`), old Material Icons
  removed (2026-11-19, PR #1). Check on the phone (CONTINUE.md).
- [ ] **Fix:** frames drop opening the moon card and search. Decided: drop the page recede, re-measure,
  then trace the moon card's remaining cost (CONTINUE.md 1A).
- [x] **Fix** (checked 2026-11-19): swiping search down shows two search icons (the toolbar's button is drawn while the
  card's icon travels to it); hide it until the hand-off (CONTINUE.md 1B).
- [x] **Test builds for the user** (built 2026-11-20; waiting on the two GitHub secrets): profile build on a rolling `testing` pre-release, stable test key,
  "Solstice Lab" launcher icon in test builds (CONTINUE.md 1C).
- [ ] **M5:** motion lab, then refining rounds (moon, search, window, toolbar, Places, messages).
- [x] **Minimum Android 10** (API 29), done 2026-11-21.
- [ ] 💬⭐ **Morph refining (Sat 2026-11-21, 1:00 AM, top priority):** lab renamed "Motion Lab"; the button
  itself becomes the card; blur to hide text changes; text transformations (label into title, replace,
  rolling numbers); a lab demo per kind of animation; the toolbar's animations into `:motion` and the lab.
  PLAN.md "Morph refining".
- [ ] **"Last updated" and the weather source** at the bottom of Forecast (agreed 2026-11-21; CONTINUE.md "Right now").
- [ ] **Refresh the main place on a cold start** (agreed 2026-11-21), then check the API cost with the user.
- [ ] **"Will I see it tonight?"** in the moon card (agreed 2026-11-21, with F5).
- [ ] **Fix:** one toolbar for home and the postcard window (the window's This week title doesn't fold;
  no page jump; missing hooks). Shared state + host, only configuration differs (CONTINUE.md 0b.3).
- [x] 💬 **Solstice's opinion** (DESIGN.md, written with the user 2026-11-21).
- [ ] 🔎 **Research:** page recede done cheaply as a `:motion` piece; long press alongside drag to reorder;
  proactive channels (Live Updates, daily brief, clothing). PLAN.md "Research queue".
- [ ] **Design audits** (DESIGN.md): empty/loading/error/offline states everywhere; affordances vs behaviour;
  dead ends and links between information; landscape/foldable/tablet layouts; settings that shouldn't exist.
- [ ] **Summary wording** (agreed table in CONTINUE.md "Right now").
- [ ] **What the summaries say** (agreed 2026-11-21, PLAN.md): priority-list engine (one question per
  summary, top 1–2 facts, nothing extra when nothing is useful); Forecast evening switch at 6 PM, adjustable;
  compare with yesterday if sources allow; This week trend and weekend; "precipitation" when snow is possible.
- [ ] 💬 **Onboarding helps configure the app** (where you are, source, when your evening starts, …): steps to
  propose to the user.
- [x] 💬 **Moon card redesign** designed in chat: direction A with changes (2026-11-21).
- [ ] ⭐ **Foundations for it** (PLAN.md): ~~F1 haptics system~~ (built 2026-11-21), ~~F2 edge blur generalized~~ (built 2026-11-21), ~~F3 dial~~ (built 2026-11-21), ~~F4 sky effects~~ (built 2026-11-21) — **F5 (the moon card itself) on hold until the user says go** (2026-11-21)
  (stars, twinkle ready but off, ready for weather effects), then F5 the moon card.
- [ ] 💬 **Twinkling stars** (built with F4, switched on only once agreed).
- [x] **Moon icon on the Forecast diagram follows the moon's real tilt** (done 2026-11-21, `MoonSky`; not yet seen on the phone).
- [ ] 💬 **Very subtle toolbar haptics**, once F1's haptics system exists.
- [x] **Fix:** taps blocked while a tab transition plays (done 2026-11-21: `:motion` PageTransition; closing sheets let taps through too).
- [x] **Fix:** the postcard window's pages lack home's blur between tabs (done 2026-11-21: `ShellNavigation`, shared with home).
- [ ] **Icons vs M3** (PLAN.md "Icons", gaps 1–4): grade -25 for light icons on dark backgrounds; selected
  toolbar tab filled; audit small-icon labels, 48 dp touch targets, icon/text alignment; optical size 40 for big icons.
- [ ] Icons later (agree first): animated fill.

### Refinement
- [x] Real blur behind the Forecast cards; moon card in the forecast's tint, dark at night (2026-11-19).
- [ ] ⭐ **One blur system,** with the same materials everywhere. Blur behind all card backgrounds,
  including the moon button.
- [ ] Look at how Android 17 blurs (live, cheap) and move glass and edge blur onto it.
- [ ] Review the design against Apple's Human Interface Guidelines.
- [ ] **Never a plain fade-in:** things morph into other things. Applies to every new motion.

---

## Toolbar and navigation

### Fix
- [ ] **Tab tap speeds:** tapping the next tab still looks faster than the edge-to-edge jump. Use the
  average of the two speeds for both (icon taps only; swipes stay as they are).

### Refinement
- [x] Toolbar labels centred in their tabs (2026-11-19).
- [ ] A little spring on the edge-to-edge jump.
- [ ] A slight gradient on toolbar buttons: lighter on top, darker at the bottom, derived from each
  button's own colour.
- [ ] Long pages: the toolbar label and the page heading never show at the same time (decide the
  hand-off rules).
- [ ] 💬 Refine the bud (button coming out of the toolbar).

---

## Forecast screen

### Functionality
- [ ] **Last updated time** for the weather data.
- [ ] **Refresh weather** when opening a place and when the app first opens.
- [ ] **Feels-like everywhere the data has it** (hourly, This week), not only the main temperature.
- [ ] Edit the Forecast blocks in place, like Pixel Weather.
- [ ] Weather effects (rain, thunder, snow) on the sky.
- [ ] Alerts as cards, and Live Updates.

### Fix
- [x] **Text colours follow the sky:** in dark mode, opening a place where it's day, the "Visiting"
  text doesn't adapt. All Forecast text follows the sky; only This week adapts to any background.
- [ ] **Rendering past the edges while the page recedes** (opening search, floating cards): draw the
  background and edge blur a bit beyond the visible area.

### Refinement
- [ ] **A small icon before the time.**
- [ ] A unique grain per sky picture (no repeating condition pictures).
- [ ] Skies visibly animated (they barely seem to move), plus a sky motion setting.
- [ ] 💬 A transition when a new background loads (e.g. from a dark sky, with the sun or moon moving
  into place), and a nicer loading UI.

## Precipitation (Forecast)

### Fix
- [ ] Check the rain card with real rain (redesigned 2026-11-19, not yet seen).
- [ ] 💬 **For later:** animate the change between amount and probability (propose first).
- [x] Switch icons (raindrop, percent), probability shown as hourly pills with percentages, no resizing when switching (2026-11-19).

### Functionality
- [ ] A precipitation element for This week.

## Moon

- [x] Smooth rotation (real parallactic angle eased by altitude, plus a spring); outline phase icons from Weather Icons on the dial (fading at the edges) and the sun path; the sun path line fades around the sun and moon (2026-11-19).

- [x] Calmer rotation (orientation at the moon's highest); phase icons on the dial where phases begin (2026-11-19).

- [x] Realistic moon turns with the sky (bright-limb angle + parallactic angle), shadow 80 %; Forecast keeps the plain moon icon, no halo (2026-11-19).

### Functionality
- [x] ⭐ **Realistic moon** from NASA's CGI Moon Kit colour map (done 2026-11-19: NASA map + AGSL shader),
  shader-lit by phase, with earthshine. One component for the button, the moon card and the sun
  path. Lunaria is a reference.
- [x] **Opening morph:** the moon button grows into the card (2026-11-19, like search).
- [x] Size and position: a sheet docked at the bottom, one size (the card's full height), 2026-11-19.

### Refinement
- [x] **Finer, lighter vibrations** on the dial's small steps (6-hour ticks), with the stronger
  tick per day.
- [x] **Inertia on the dial** (a flick keeps it turning, then it settles).
- [x] **An arrow towards today** when today is out of view on the dial.
- [x] Make the moon card match our design: big heading, details as a sentence (design A), the
  search card's glass and handle (2026-11-19).
- [x] Moon button: remove its small moon icon (redundant); use a proper moon-phase icon from the
  icon library on the Forecast page.
- [ ] Day labels on the dial (Tue · Today · Thu).

## This week

### Functionality
- [ ] Tapping a day (a pill, or a day named in the summary) opens that day's floating card.

### Refinement
- [x] Shorter pills, with more space between the pills and the text above and below.
- [x] Wording: "It's raining two days this week".
- [x] A more visible card background.

---

## Places

- [x] Starred place folds into a stamp-sized strip under the current location; current location in red; "YOU ARE HERE" when it's starred (2026-11-19).

- [x] The current location always stays at the top (2026-11-19).

- [x] The star sticker is red everywhere (2026-11-19).

- [x] The current location is always a strip; stamp centred on the strip; Places scrolls under a transparent status bar (2026-11-19).



### Functionality
- [x] ⭐💬 **The starred place folds into a compact version.**
- [x] **When another place is starred, the current location becomes a one-line card.**
- [x] 💬 **Swapping main and pinned when you travel:** decided 2026-11-22, automatic (PLAN.md "Places and
  travel"); build it with the Places model in the review section.

### Refinement
- [x] Remove the grain paper effect for now; use the same background and edge fade as This week.
- [ ] Reset the scroll position when you leave Places.

## Postcard window (visiting a place)

### Functionality
- [ ] 💬 **Loading state:** start with "Visiting Tokyo · 2:33 PM, 14 hours ahead" under the title;
  when the data arrives, the buttons move and transform into place (uses the morph system).

### Refinement
- [x] A small icon before the "Visiting" text (Material FlightLand) and a clock before the local time (Material Schedule), 2026-11-19.
- [x] Pin icon: white outline; when pinned, filled (white in dark mode, black in light mode).

## Search pane

### Functionality
- [x] **A button to clear recent searches.**

### Fix
- [x] **Back closes the search card.**
- [x] **The keyboard's dismiss button expands the search card to full height,** and the card is
  drawn behind the navigation bar.

### Refinement
- [ ] 💬 Opening search: the search icon should move towards the card as it opens.

---

## Data and sources

- [x] **AccuWeather disabled for now** (2026-11-19). It's no longer a selectable or default source
  (alerts fall back to WMO, pollen to Open-Meteo, no normals source), and places that still name it
  skip it quietly.
- [ ] Re-test that a searched city keeps its name (e.g. "Puebla").

## Writing

- [ ] 🗓 **Writing session for the conditional texts** (tomorrow): fill in `docs/WRITING.md`.

## Developer tools

- [ ] **Simulation tools** that are easy to reach (e.g. a hidden debug panel): fake the time of day
  and date, and the weather (rain now, rain in 15 minutes, heavy rain, snow, storms, clear night,
  different time zones), so every state can be checked on the phone without waiting for it.

## Settings

- [ ] Simplify settings (fewer trend options).
- [ ] App drawer name: choose "Solstice" or "Weather".

## Release

- [ ] Rewrite `PRIVACY.md` and `INSTALL.md`.
- [ ] Re-check a release build end to end.
- [ ] Tablets: rotation and layout.
- [ ] App icon: the softer, rounder icon search.
