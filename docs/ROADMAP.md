# Solstice roadmap

**Status (2026-11-26):** This is Solstice's application backlog, not a session schedule. Dated session notes are historical, not current commitments. Revalidate unchecked items against current app source and approved product decisions before implementation. Documentation/CI work is tracked in [MODERNIZATION_CHECKLIST.md](MODERNIZATION_CHECKLIST.md); product decisions remain in [PLAN.md](PLAN.md).

Everything is grouped by area and type. Keep the roadmap focused on work still relevant to the app; remove or archive obsolete items only after checking the current source and approved decisions.

- **Functionality:** something new the app can do.
- **Fix:** something broken or wrong.
- **Refinement:** polish to something that already works.
- 🗓 = scheduled · ⭐ = high priority · 💬 = needs your OK (a design or motion proposal) before building.

---

## 🐞 Review 2026-11-22: bugs, fixes and what to rebuild (from chat; details per item below)

Suggested order: app interaction bugs B3/B4, then B2's precipitation rule and D1's text, followed by
approved Solstice UI refinements. Each visible change still follows RULES.md.

### Bugs
- [ ] **B1 · Place time on the "Visiting" line** (`PageFrame.kt` `VisitingLine`): says "Visiting" even for
  saved places (ignores `pinned`); four different looks for a place's time (Forecast header, window
  line with a typed-in " · ", postcard caption in capitals without the clock, search). Fix: one
  Solstice-owned place-time component (variants), shown whenever the time differs; "Visiting" only for
  previews; separator as a string.
- [ ] **B2 · Rain card at 0 %, noisy rain notifications** (`RainCard.kt` `rainOutlook`: trace values and
  rain codes can produce a card at 0 %; snow is not handled consistently; time labels are typed in).
  Fix: one shared rule (PLAN.md "Rain and alerts") for card, notification and Live Update, with a
  cooldown; use deterministic fixtures from currently supported providers.
- [ ] **B3 · Tapping after a swipe stops the tabs** (`PageSpring.kt`): the pager counts as scrolling
  until the bounce settles, and the tap intended to select a tab is swallowed. Keep tab selection
  responsive while the pager settles; draw the bounce as an offset rather than extra scroll distance,
  and add an app regression test for swipe-then-tap.
- [ ] **B4 · Jumping across the toolbar flashes Forecast** (`springToPage` scrolls through every page;
  `Toolbar.kt:157` blends the highlight colour from the scroll position). Fix the Solstice page-selection transition so direct tab jumps do not flash intermediate pages and
  the highlight follows the target. Replaces "tab tap speeds" and "a little spring on the edge-to-edge jump" below.
- [ ] **B5 · Text changes feel too blurred and distracting**: the old and new values become hard to
  read during a content update. App-side alternatives: A fade-through, B softer/slower blur, or C animate
  only changed digits. Review the alternatives in Solstice before choosing.

- [ ] **B6 · Place drag near the viewport edge**: dragging a card that is partly out of view must continue to track the finger and settle on the intended position.
- [ ] **B7 · Places edge auto-scroll**: dragging a card to the top or bottom edge must scroll the list predictably without losing the dragged item.
### Design fixes
- [ ] **D1 · This week's text:** drop "through the week", "all week", "this week" from its summary lines
  (`strings_expressive.xml` `week_*`).
- [ ] **D2 · Top edge blur bigger** (`PageFrame.kt` `pageTopEdge`: ends exactly at the status bar): the
  larger of the status bar and the camera cutout, plus an eased fade below, from tokens.
- [ ] **D3 · The "no rain" line** becomes a small card that fits the card layout.

### Solstice-owned refinements

- [ ] **R1 · This week's temperature card:** preserve the approved layout while refining spacing,
  sizes, and day-row interaction in the app.

### Decisions and clean-up
- [ ] **Q1 · Icons** (PLAN.md "Icons"): build the Rounded / Outlined / Sharp setting; weather conditions
  in Material Symbols for now (no animated icons).
- [ ] 💬 Later: our own weather set from Material Symbols' shapes with a Solstice colour layer (mock six
  conditions on the design canvas first).
- [ ] **Q2 · Professional feel** (PLAN.md): Advanced section for Data sharing, Gadgetbridge, Debug;
  Settings reshaped; README and docs rewritten; store text, name and About.
- [ ] **Q3 · Replace inherited app behavior within Solstice:** keep weather-source access behind app-facing
  interfaces, and progressively replace inherited screens, settings, and widgets with Solstice's own UX.
- [ ] **Places model** (PLAN.md "Places and travel"): current location as home, no star, automatic
  travel swap with "Home" in Places, search preview with Cancel / Add, onboarding place choice.

### Earlier bugs checked against the rebuild
| Earlier item | Status |
|---|---|
| One toolbar for home and window | Fixed by the Solstice shell toolbar |
| Tab tap speeds; spring on the edge-to-edge jump | Replaced by B4 |
| Frames drop opening the moon card and search | Likely fixed by the moon card and search rebuilds; re-measure |
| Drawing past the edges while the page recedes | Gone (recede dropped) |
| Rain card with real rain; animate amount ↔ chance | Covered by B2 and the precipitation card backlog |
| Precipitation element for This week | Part of R1 |
| Toolbar label and page heading showing together | An app page-layout rule |
| Places: dragging a card partly off screen; no scrolling at the edge | Tracked by B6 and B7 |
| Reset Places' scroll when you leave | Small fix, or a page-frame option |
| Search keeps "Puebla" | Separate re-test |
| "Last updated" and refresh on start | App implementation built; device verification remains listed above |
| Swapping main and pinned when you travel | Decided: automatic swap (PLAN.md "Places and travel") |

## 💬 Proposals to bring to Marco (from chat 2026-11-22)

- [x] 💬⭐ **App layout:** Forecast keeps today's layout (2026-11-22). A2 from the layout canvas is the
  pattern for partial sheets (PLAN.md Direction 2).
- [ ] ⭐ **Partial sheet experience in Solstice** (the A2 pattern): grows out of its card, with a dimmed
  page behind, medium height first and then large, a grabber that supports drag/tap/TalkBack, swipe down
  to close, and room for a chart and setting rows. Confirm which Forecast cards open one.
- [ ] ⭐ **Header close position in Solstice:** ✕ at the end (default) or swapped with the leading button; mirrored for right-to-left.
- [ ] ⭐ **Optional, reorderable Forecast cards** with a general "most important moves to the top" rule
  (replaces the rain-only special case in `ForecastPage.kt`): on by default, the card returns to your order
  once it no longer matters, a switch to turn it off. Catalog and thresholds: PLAN.md "Forecast card catalog".
- [ ] ⭐ **Forecast spacing pass** against Material's spacing guidance, Solstice design tokens only (hero temperature,
  sun/moon path and time stay).
- [ ] ⭐ **Moon card rework** as a Solstice-owned partial sheet.
- [ ] 💬⭐ **Outlook** (This week renamed, decided 2026-11-23): propose its proactive answers (best weekend day
  to go out, notable days ahead, good sky evenings); the day list stays. PLAN.md Direction 4.
- [ ] ⭐ **What to wear card** on Forecast, always shown, today by day and tomorrow from the evening switch.
- [ ] ⭐ **Sun button and sun sheet** (decided 2026-11-23): a button in the hero like the moon's, opening a
  partial sheet with sunrise, sunset, daylight and UV. **UV first:** index number, next-hours timeline,
  what to do per level (WHO); the UV card opens it; card and button turn red at 8+ (plus icon or word).
- [ ] ⭐ **One UV rule** shared by the UV card, the sun sheet and What to wear (sunscreen suggestion).
- [ ] 💬 **Postcards in a standard look**, the physical feel kept in the interaction (Direction 2).
- [ ] 💬⭐ **Proactive alerts the Gradient Weather way** (Direction 3): alerts you choose to watch, rain or
  snow countdown as a Live Update, cooldowns, official warnings in full, evening summary, what to wear.
- [ ] 💬 Refine Solstice's interaction feel, reduced-motion behavior, and card materials (PLAN.md open questions).
- [ ] 💬 Optional interactive-glass response: restrained press swell, a finger-following highlight, a
  subtle hold response, edge stretch, release ripple, or neighboring controls sharing one surface.
  This is an unapproved idea; review the behavior and accessibility on Solstice before building it.

---

## Solstice app foundations and consistency

### Functionality
- [ ] ⭐ **Unify systems and components:** one toolbar, page scaffold, card, message and sheet,
  reused everywhere. Use well-maintained open-source libraries where they make the app lighter and
  faster.
- [ ] **Connected transitions for Solstice surfaces and controls:** define how a control expands into
  its destination and how the visited-place loading state transforms into content. Keep the acceptance
  criteria app-focused; implementation is chosen only when the app task is approved.
- [x] Solstice motion tokens: `SolsticeMotion.kt` and `docs/MOTION.md` (2026-11-19), first used by the moon sheet.
- [x] Apply the motion system to search, the postcard window, Places drag and the toolbar (2026-11-19).
- [x] 💬⭐ **Design tokens:** spacing (4 dp grid), sizes, shapes with continuous corners, palette file,
  hero type styles, Solstice `MotionScheme`. See `docs/UI_SYSTEMS.md`.
- [x] 💬 **Haze prototype** for glass and edge blur, measured on the Pixel 7 against `Glass.kt`.
- [x] **Component catalog** (first pass; one Sheet and PageScaffold come with in the initial pass): split `ShellPages.kt`; one Card / Sheet / Toolbar / Message / PageScaffold;
  `PlaceWindow` reuses them.
- [x] Replace the initial custom list-reordering behavior with a reusable app implementation; move the relevant screens to Navigation 3 (migration started 2026-11-19).

- [ ] **Solstice design system:** spacing scale, type scale, motion tokens, and app component rules.
- [x] 💬⭐ **Icon style:** Material Symbols Rounded, one catalog (`SolsticeIcons`), old Material Icons
  removed (2026-11-19, PR #1). Check on the phone (CONTINUE.md).
- [ ] **Fix:** frames drop opening the moon card and search. Decided: drop the page recede, re-measure,
  then trace the moon card's remaining cost (CONTINUE.md 1A).
- [x] **Fix** (checked 2026-11-19): swiping search down shows two search icons (the toolbar's button is drawn while the
  card's icon travels to it); hide it until the hand-off (CONTINUE.md 1B).
- [x] **Test builds for the user** (built 2026-11-20; waiting on the two GitHub secrets): profile build on a rolling `testing` pre-release, stable test key,
  "Solstice" launcher icon in test builds (CONTINUE.md 1C).

- [x] **Minimum Android 10** (API 29), done 2026-11-21.
- [ ] 💬⭐ **Morph and content refinements:** improve how Solstice controls become app surfaces, how text
  changes are introduced, and how numeric values update. Review alternatives in the app before implementation.

- [ ] **Re-check the "Last updated" line and attribution** on a physical Android device.
- [x] **Refresh the main place on a cold start** (built 2026-11-21; API-cost review recorded in CONTINUE.md).
- [ ] **"Will I see it tonight?"** in the moon card (agreed 2026-11-21, with F5).
- [x] **One toolbar for home and the postcard window** (shared app toolbar state; verified in the dated app history).
- [x] 💬 **Solstice's opinion** (DESIGN.md, written with the user 2026-11-21).
- [ ] 🔎 **Research:** long press alongside drag to reorder; app-owned proactive channels (Live Updates,
  daily brief, and clothing guidance).
- [ ] **Design audits** (DESIGN.md): empty/loading/error/offline states everywhere; affordances vs behaviour;
  dead ends and links between information; landscape/foldable/tablet layouts; settings that shouldn't exist.
- [ ] **Summary wording** (agreed table in CONTINUE.md "Right now").
- [ ] **What the summaries say** (agreed 2026-11-21, PLAN.md): priority-based selection (one question per
  summary, top 1–2 facts, nothing extra when nothing is useful); Forecast evening switch at 6 PM, adjustable;
  compare with yesterday if sources allow; Outlook trend and weekend; "precipitation" when snow is possible.
- [ ] 💬 **Onboarding helps configure the app** (where you are, source, when your evening starts, …): steps to
  propose to the user.
- [x] 💬 **Moon card redesign** designed in chat: direction A with changes (2026-11-21).
- [x] **Moon-card foundations:** haptics, generalized edge blur, dial, sky effects, and the initial
  moon-card experience were built during 2026-11-21. Any new moon-card design change is tracked as a
  separate app-side proposal and requires approval.
- [ ] 💬 **Twinkling stars** (built with F4, switched on only once agreed).
- [x] **Moon icon on the Forecast diagram follows the moon's real tilt** (done 2026-11-21, `MoonSky`; not yet seen on the phone).
- [ ] 💬 **Very subtle toolbar haptics**, only if approved after the app's existing haptics are checked on device.
- [x] **Fix:** taps blocked while a tab transition plays (done 2026-11-21: page changes no longer block taps; closing sheets also let taps through).
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
- [ ] **Prefer connected surface transitions:** use a morph when a control grows into a related surface;
  use a clear fade/crossfade when it improves content clarity or reduced-motion behavior.

---

## Toolbar and navigation

### Refinement
- [x] Toolbar labels centred in their tabs (2026-11-19).
- [ ] A consistent, app-approved transition for direct tab jumps (tracked by B4).
- [ ] A slight gradient on toolbar buttons: lighter on top, darker at the bottom, derived from each
  button's own colour.
- [ ] Long pages: the toolbar label and the page heading never show at the same time (decide the
  hand-off rules).
- [ ] 💬 Refine the bud (button coming out of the toolbar).

---

## Forecast screen

### Functionality
- [x] **Last updated time** for the weather data (built; verify presentation on device).
- [ ] **Refresh behavior when opening a place**: confirm freshness and avoid unnecessary requests; the main-place cold-start path is tracked above.
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
- [ ] Check the rain card with real precipitation using a currently supported source (redesigned 2026-11-19, not yet seen).
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

- [ ] Review conditional weather-summary wording against the current Solstice writing principles in `docs/WRITING.md`.

## Developer tools

- [ ] **Simulation tools** that are easy to reach (e.g. a hidden debug panel): fake the time of day
  and date, and the weather (rain now, rain in 15 minutes, heavy rain, snow, storms, clear night,
  different time zones), so every state can be checked on the phone without waiting for it.

## Settings

- [ ] Simplify settings (fewer trend options).
- [ ] App drawer name: choose "Solstice" or "Weather".

## Calendar-aware weather

- [ ] **Weather for calendar events:** add an optional, read-only integration through Android's `CalendarContract` API and `READ_CALENDAR`. Use an event's own physical location and event time; show no event weather when the location is missing, ambiguous, or not a real place. Outlook works when its calendar is synced into Android's calendar provider. No Microsoft Graph, Outlook SDK, OAuth, or account sign-in. Scope and privacy rules: [CALENDAR_INTEGRATION.md](CALENDAR_INTEGRATION.md).

## Post-MVP — future versions

- [ ] **Wear OS companion (V2/V3+ candidate; explicitly outside MVP):** explore a focused weather Tile and a small set of watch-face complications for glanceable current conditions and relevant short-term weather. Validate usefulness and update/caching constraints before considering a full watch app.

## Release

- [ ] Replace inherited `PRIVACY.md` only after the technical evidence in `docs/PRIVACY_DATA_MAP.md` is complete and the final wording is reviewed. Detailed audit and release gates are tracked in `MODERNIZATION_CHECKLIST.md`.
- [ ] **Adaptive-layout adoption:** once Motion UI provides a stable window-adaptive capability, compose Solstice's product layouts with it and validate phone/tablet/foldable, rotation, resizing, and split-screen behavior. Generic implementation remains owned by Motion UI.
- [ ] App icon: the softer, rounder icon search.
