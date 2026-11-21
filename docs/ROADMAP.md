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
- [ ] 💬 **"Last updated" text** on Forecast (relative, "Updating…", "Couldn't update"). PLAN.md.
- [ ] 💬 **Refresh on open** without overdoing it: shown place only, 30 min freshness (to agree), sources'
  rules, back-off after failures, reuse a recent position. PLAN.md "Last updated text and refresh on open".
- [ ] **Fix:** one toolbar for home and the postcard window (the window's This week title doesn't fold;
  no page jump; missing hooks). Shared state + host, only configuration differs (CONTINUE.md 0b.3).
- [x] 💬 **Solstice's opinion** (DESIGN.md, written with the user 2026-11-21).
- [ ] 🔎 **Research:** page recede done cheaply as a `:motion` piece; long press alongside drag to reorder;
  proactive channels (Live Updates, daily brief, clothing). PLAN.md "Research queue".
- [ ] **Design audits** (DESIGN.md): empty/loading/error/offline states everywhere; affordances vs behaviour;
  dead ends and links between information; landscape/foldable/tablet layouts; settings that shouldn't exist.
- [ ] **Summary wording** to WRITING.md's "natural, not generated" rule (week summary first).
- [x] 💬 **Moon card redesign** designed in chat: direction A with changes (2026-11-21).
- [ ] ⭐ **Foundations for it** (PLAN.md): ~~F1 haptics system~~ (built 2026-11-21), ~~F2 edge blur generalized~~ (built 2026-11-21), ~~F3 dial~~ (built 2026-11-21), ~~F4 sky effects~~ (built 2026-11-21)
  (stars, twinkle ready but off, ready for weather effects), then F5 the moon card.
- [ ] 💬 **Twinkling stars** (built with F4, switched on only once agreed).
- [x] **Moon icon on the Forecast diagram follows the moon's real tilt** (done 2026-11-21, `MoonSky`; not yet seen on the phone).
- [ ] 💬 **Very subtle toolbar haptics**, once F1's haptics system exists.
- [x] **Fix:** taps blocked while a tab transition plays (done 2026-11-21: `:motion` PageTransition; closing sheets let taps through too).
- [x] **Fix:** the postcard window's pages lack home's blur between tabs (done 2026-11-21: `ShellNavigation`, shared with home).
- [ ] 🔭 **Far future: native Smartspacer** Targets and Complications in Solstice, no bridge app (needs Android 10,
  the new minimum; check the SDK's license). PLAN.md "Far future".
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
- [ ] 💬 **Swapping main and pinned when you travel:** e.g. Mexico City is main and Tijuana pinned;
  move to Tijuana and they swap (Tijuana main, Mexico City pinned). Research how Apple Maps and
  Apple Weather handle it before building.

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
