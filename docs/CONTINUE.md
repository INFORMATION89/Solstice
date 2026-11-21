# Continuing on another computer

Where things stand and how to pick the work up anywhere. The plan, decisions and log are in
[PLAN.md](PLAN.md).

## Where we are (2026-11-19)

- **Repo:** this one (`INFORMATION89/Solstice`): upstream Breezy Weather's files at `5ec3b9b`
  **without upstream's git history** (see `UPSTREAM_BASE`). Never push upstream's history here.
- **F0 done (2026-11-18):** snapshot, app name "Solstice", `applicationId`
  `io.github.information89.solstice` (renamed 2026-11-18; was `…breezyexpressive`), CI (`.github/workflows/build.yml`), first run on the
  Pixel 7, own launcher icon with themed-icon layer (regenerate with `tools/make_icon.py`).
  `./gradlew assembleBasicDebug` builds (first build 6–20 min).
- **F1 done:** Expressive theme in `ui/expressive/theme/` (Google Sans Flex, expressive motion,
  larger corners, sky-blue/amber fallback). `Theme.kt` delegates to it.
- **F2 done** (except real blur): settings rows with colored icon shapes and press morph, inline
  button groups, bottom sheets, icon switches, collapsing large title, Compose reorder screens
  (`ui/expressive/reorder/`). New UI code lives in `ui/expressive/`; upstream files touched are
  listed in PLAN.md.
- **Built since F2 (2026-11-18/19, details in the PLAN.md log):** the Solstice shell
  (`ui/expressive/shell/`): Forecast, This week and Places pages in a pager, the floating glass
  toolbar with the button budding out of it, the redesigned search (`ui/expressive/search/`), the
  postcard window for visiting a place, messages, the moon card as a sheet that morphs out of its
  pill, frosted glass (`components/Glass.kt`) and progressive edge blur (`components/EdgeBlur.kt`).
  AccuWeather is disabled as a source.
- **Motion rules:** [MOTION.md](MOTION.md) (agreed 2026-11-19). Values come from the `:motion`
  module (`motion/`, Apache-2.0, 22 tests run in CI); `ui/expressive/components/SolsticeMotion.kt`
  forwards to it. Only the moon card runs on `:motion` so far.

## Done on 2026-11-19 (details in the PLAN.md log)

- **Design tokens** (`theme/Tokens.kt`), checked on the Pixel 7.
- **`:motion` M1 to M3:** tokens with response + damping and a sixth `snappy`, `MotionValue` with the
  UIKit release rules, `Morph` and anchors, the **Solstice `MotionScheme`**, and the **moon card on
  `:motion`** (janky frames ~10 % to 3.3 %). The review's bugs are fixed (catching an overshoot,
  catching mid-flight).
- **Glass on Haze 2.1.0** for good; **edge blur stays ours** (Haze measured slower there).
- **Component catalog, first pass:** pages split out of `ShellPages.kt`, `SolsticeCard`,
  `shell/Toolbar.kt`, `shell/PageFrame.kt` shared with the postcard window.
- **Library swaps:** Reorderable 3.1.0 runs `ReorderList.kt`; Material 3 controls everywhere except
  the toolbar and the postcard.
- **Icons on Material Symbols Rounded** (2026-11-19, PR #1), CI green, not yet seen on the phone.
- **Navigation 3: decided and started** (user, 2026-11-19). As of the last check the migration was
  not on `main` yet: pull, or ask where it lives, before touching Settings or pushed screens.

## Pick up here (next session)

State on 2026-11-19 evening: icons on Material Symbols Rounded (merged, selected tab filled) and
**M4 finished**: every animation in `ui/expressive/` is a motion token (1 `spring(…)` and 2 `tween(…)`
left: the three documented timed exceptions). The focus now: **fix the two reported bugs, check by
hand, build the motion lab (M5), then refine animations area by area.**

### 6 AM session (Saturday 2026-11-21, the user's list), work in `:motion`

**Done 2026-11-21 (built, not tested on the phone, which wasn't connected); see the PLAN.md log. Next:
test on the Pixel (profile build, Motion Lab → "Morph, iOS way (new)"), then the user agrees the two
plans (`MOTION_LAB_PLAN.md`, `TOOLBAR_MOTION_PLAN.md`).**

The user leaves the session to work alone: **work in the motion environment** (`:motion` and the Motion
Lab), maximize work, minimize testing, and **test by yourself at the end** (profile build, Pixel 7).
Research first where asked; the study is `docs/MORPH_STUDY.md`.

1. **Reopen while it's closing:** while the new morph (`SpringMorph`) is still closing, it can't be
   reopened. The card only opens again once the close animation has fully finished. A tap (or grab)
   on the shrinking card, and on where the pill is, must reopen it at once (MOTION.md: never block
   input).
2. **Text blur more obvious:** the label/heading blur replace (`MorphHandOff.Blur`, 8 dp) should read
   clearly as a blur.
3. **Research blur in iOS animations, especially iOS 26/27** (Liquid Glass, blur replace, morphing
   menus and sheets), and add the findings to `MORPH_STUDY.md`.
4. **Research iOS 26/27 animation specifically and refine** the morph against it.
5. **Closing too fast breaks the animation:** add a reasonable speed limit (cap the release velocity
   handed to the springs).
6. **A tiny bit more bounce on fully opening** (`Motion.brisk`, now 0.85 damping): going in the right
   direction.
7. **Plan the Motion Lab redesign** (plan only, part of the bigger `:motion` project): easy for a
   normal user to see and try; if possible, a comparison between `:motion` and Google's default
   libraries (Compose / Material motion) so the difference shows. Remove references to the weather
   app from the lab and `:motion`: it's a library of universal animations and interaction components.
8. **Plan applying the same Apple principles to the toolbar** (springs per property, velocity, never
   blocking input, momentum), as a proposal for the user.



Read DESIGN.md, MOTION.md (morph never pop, never block input, `:motion` covers every interaction) and
WRITING.md first. Each motion item: propose options, agree, then build.

1. ~~**Minimum Android 10**~~ (done 2026-11-21).
2. **(Done 2026-11-21 for home and the sheets; the window's pager moves with item 3.)** **Taps are never blocked by animations** (MOTION.md rule, high priority). Reported: a place card
   can't be opened while a tab transition plays. Likely cause: a tab tap to a neighbour runs
   `pager.animateScrollToPage`, and the pager spends the first touch on stopping its scroll, so the
   card never gets the tap. Fix it in `:motion` for every animation, not per screen.
3. **(Done 2026-11-21: `ShellNavigation`, `ToolbarMotion`, lab demo; MotionValue for the finger-driven parts still to propose.)** **One shell system in `:motion`:** the toolbar (section 0b.3, and section 0 point 6: its seven
   `Animatable`s into `:motion` with lab controls) and the **page transitions**: home's swipe blur (6 dp
   by distance) and tab-jump slide (frozen image, 4–5 dp blur) are written by hand in
   `SolsticeShell.kt`; the postcard window's pager has neither (reported: "visiting doesn't have the
   same blur between tabs"). One `:motion` page-transition piece, used by both.
4. **Morph, never pop: the button itself becomes the card** (section 0, point 2) on search, the moon
   pill and the postcard window.
5. **Summary wording** to WRITING.md's "natural, not generated" rule: `week_warmest` and
   `week_coldest_night` become "Monday will be the warmest day, at 35°. The coldest night will be on
   Friday, at 21°."; `week_some_wet` becomes "It will rain on 3 days this week"; read the rest aloud.
   Show the new texts to the user before committing.
6. **Moon card: direction A agreed (2026-11-21), built on foundations first.** Follow PLAN.md "Moon card
   redesign and its foundations" in order: **F1 haptics system in `:motion`** (this replaces the old
   item 7), **F2 generalized progressive edge blur in `:motion`**, **F3 a `:motion` dial** (with or
   without text, drum curvature, dates and icons, ends with rubber-band and haptic), **F4 sky effects**
   (stars in the AGSL sky, twinkle built but off until agreed, ambient clock into `:motion`), then **F5
   the moon card**. The user's rule, in capitals: **make it properly from the start, no hand-made things
   that should be systems.** Each piece gets a lab demo.
7. (Merged into 6 as F1.)
8. **Moon icon on the Forecast diagram follows the moon's real tilt in the sky** (user's choice): reuse
   the moon card's calculation (`MoonCard.kt`: bright limb angle from `MoonIllumination` and the
   averaged `parallacticAngle`), moved into a shared helper, and rotate the phase icon
   (`drawMoonPhaseIcon`, `ForecastPage.kt`) by it. Don't write a second calculation.

Then, if time is left: "last updated" text and refresh on open (section 0b), audits (DESIGN.md).
Research the chat environment does meanwhile (PLAN.md "Research queue"): the page recede done cheaply,
long press alongside drag to reorder, proactive channels, what the summaries should say.

### 00. ASAP, first thing next session: minimum Android 10 (decided by the user 2026-11-20)

Raise the minimum from Android 7 (API 24) to **Android 10 (API 29)**: `MIN_SDK = 29` in
`buildSrc/src/main/kotlin/breezy/buildlogic/AndroidConfig.kt` (an upstream file: one-line edit, note
it in the PLAN.md log as a fork difference to keep when syncing upstream). Then:
- Build and check on the Pixel 7 (profile build); CI must stay green (lint may flag now-useless
  version checks: fix them in `ui/expressive/` and `:motion`, **leave the ~30 in upstream files**
  (`VERSION_CODES.N` … `Q`) and the `-v26`/`-v28` resource folders as they are, so upstream merges stay
  easy).
- Our own code: drop fallbacks only needed below 10 (check `ui/expressive/`, `:motion`; Android 12+
  checks for `RenderEffect` blur stay).
- Docs: README, UI_SYSTEMS.md and PLAN.md wherever they say Android 7 / minSdk 24 (e.g. the icons'
  "works on minSdk 24", Haze's "fits our minSdk 24").
- It unblocks the far-future native Smartspacer work (its SDK needs Android 10).

### 0. Next session: Saturday 2026-11-21, 1:00 AM (planned by the user, top priority)

**Refine the morph animations, using the motion lab.** Motion rule still applies: start the session
by proposing options for each point (lab demos where possible), agree, then build. Plan in PLAN.md,
"Morph refining (planned 2026-11-20)".

1. **Rename the lab's launcher icon to "Motion Lab"** (now "Solstice Lab" in
   `app/src/profile/AndroidManifest.xml` and `app/src/debug/AndroidManifest.xml`; the Settings entry
   `motion_lab` is "Motion lab": make it "Motion Lab" too).
2. **The original button becomes the card.** Today `Morph` draws a new surface in an overlay and hides
   the source, so it reads as a swap. The button itself (its shape, glass, colour and icon or label)
   has to be what grows into the card, with no moment where two things or a replacement show.
3. **Blur to hide the text change**: while a label turns into a heading (or any text into other
   text), blur both briefly as they cross, so the change is never seen as two texts overlapping.
4. **Text transformations and transitions**: label into title (position, size, weight), text
   replaced in place (blur + fade), numbers changing (rolling or counting digits, like iOS's numeric
   text transition), text appearing and leaving with its container.
5. **More interactions in the lab, one per kind of animation**, so every pattern can be tried and
   compared with iOS: list in PLAN.md.
6. **The toolbar's animations into `:motion`**, so the system drives them and they can be tweaked
   and refined in the lab (PLAN.md "Morph refining", point 5).

### 0b. Also next session, after the morph work (added by the user 2026-11-20)

1. **"Last updated" text** on the Forecast page (the old main screen had one, `HeaderViewHolder`'s
   `refreshTime`; the Solstice shell has none). Propose where and how before building: relative and
   ticking ("Updated 12 min ago", then the time in 12-hour format after a few hours), "Updating…"
   while a refresh runs, "Couldn't update" with a retry when it fails.
2. **Refresh the weather when the app opens, without overdoing it.** First agree the compromise in
   PLAN.md "Refresh on open (proposed 2026-11-20)", then build it.
3. **One toolbar for home and the postcard window** (reported by the user 2026-11-20: in a visit, the
   This week tab's title doesn't fold away while the page shows its own title, so both show).
   `PlaceWindow.kt` builds its own `ShellToolbar` with different inputs from `SolsticeShell.kt`:

   | | Home (`SolsticeShell.kt`) | Postcard window (`PlaceWindow.kt`) |
   |---|---|---|
   | Tab title folding | `labelShown` from the lists' scroll (`titleVisible`) | **hard-coded `listOf(1f, 1f)`**: the bug |
   | Tapping a tab | `navigate()`: the agreed page jump | `pager.animateScrollToPage`: no page jump |
   | Interrupt / slide position | `onInterrupt`, `position` | missing |
   | Bud icon while its card is out | `budIconShown` | not set (pin) |
   | Messages | `ShellMessageHost` above it | none |
   | Fade in | `1 − max(search, window presence)` | `(progress − 0.25) / 0.5` |

   Fix: one shared piece (e.g. `rememberShellToolbarState(pager, lists…)` plus a `ShellToolbarHost`
   that both use) so the behaviour is the same by construction and only the **configuration** differs
   (which tabs, which bud: search or pin). Use the window's own This week list for its title folding.
   Then check both side by side on the Pixel 7: title folding, tab tap jump, slide to switch, bud.
   The fade-in difference may be intended (the window opening): propose, don't change it silently.

### 1. Bugs reported by the user on the Pixel 7 (2026-11-19), fix first

**A. Frames drop when opening the moon card and the search card.**
**Measured 2026-11-20** (PLAN.md log): the recede is the main cost for search (22 % → 8 % janky
without it); the moon card drops 28 % → 21 %, so it has another cost too. Next: options for the
recede, a Perfetto trace of the moon card.
**Decided by the user 2026-11-20: option A, drop the recede** for both search and the moon card (the
page behind no longer scales; the scrim and everything else stay). Then re-measure both the same way
(profile build, `dumpsys gfxinfo`, 6 opens and flick closes each): expect search near 8 %, moon card
near 21 %. Remove the lab's "Page recede" switch once the recede is gone. Then a **Perfetto trace of a
moon card opening** to find its remaining cost, and bring the findings (and any options) to the user.
- Likely cause (to confirm): both make the page behind recede, `scale = 1 − 0.04 × max(search
  backdrop, MoonCardState.progress)` in a `graphicsLayer` on the whole pager (`shell/SolsticeShell.kt`,
  "The page recedes a little…", added in M4). Scaling the page makes everything that samples it redraw
  every frame: the Haze glass (toolbar, cards) and the edge blur.
- How: profile build, `adb shell dumpsys gfxinfo io.github.information89.solstice.profile framestats`
  (or Perfetto) for 10 opens each, then the same with the recede off (temporary debug switch).
  Also note whether it's only the first open (first composition, shader compile) or every open.
- If the recede is the cause, options to propose (motion rule): keep the recede but freeze the glass
  sampling while it runs; recede only the content under the glass; or drop the recede.
- Before this, the moon card measured 3.3 % janky frames (2026-11-19, M3). Compare against that.

**B. Swiping search down shows two search icons.** **Fixed and checked on the phone 2026-11-19**
(`budIconShown` in `Toolbar.kt`, see PLAN.md log). (Clarified by the user.) Start with search
open and slide it down: the card shrinks back into the bubble and its own search icon travels to the
toolbar (correct), but the toolbar's real search button is already drawn at that spot, so two icons
show at once until the card lands.
- Cause: the card's travelling icon is in `search/SearchOverlay.kt` ("While still small it's the
  bubble's search icon", alpha `1 − inside()`). Meanwhile the whole toolbar fades back in with
  `ToolbarDock(alpha = { 1 − max(searchSheet.presence, windowPresence) })` (`shell/SolsticeShell.kt`),
  including its search button (`budIcon` in `shell/Toolbar.kt`).
- Fix (a bug in the agreed motion, "the card becomes the button", so no new options needed): keep the
  toolbar's **search button hidden while search is open or closing**, and show it in the same frame
  the card hides itself (`handedOff` in `SearchOverlay.kt`, `alpha = if (handedOff) 0f else 1f`), so
  it's a one-frame swap at the same spot. The rest of the toolbar can keep fading in with `presence`.
  Opening has the same hand-off in reverse: hide the button the frame the card appears over it.
- Check the same on the postcard window and the moon pill (a source drawn twice while its morph runs).

### 1C. Make Solstice and the motion lab installable by the user (requested 2026-11-20)

The user wants to install and test builds himself, on his phone, without a computer. Today the only
build CI makes is `basic-debug`: inside a zip, downloadable only when signed in to GitHub, expiring
after 90 days, slow (debug), and signed with CI's throwaway debug key, so a new build won't install
over the previous one. To do:

1. **A stable test key.** Create a dedicated keystore (not the release key, not anyone's
   `~/.android/debug.keystore`) and store it as GitHub Actions secrets (keystore as base64, passwords,
   alias). Sign the `profile` build with it in CI, so each new build installs over the last one and
   keeps the user's places and settings. Never commit the keystore.
2. **CI publishes the `profile` build** (smooth, R8, installs as `io.github.information89.solstice.profile`
   next to any other build) on every push to `main` to a rolling GitHub **pre-release** (for example
   tag `testing`, replaced each time), as a plain `.apk` asset with the commit in its name or notes.
   The repo is public, so the user can download it on the phone from the Releases page without signing
   in. Keep the `basic-debug` artifact as it is.
3. **The motion lab gets its own launcher icon in test builds only**: a `LAUNCHER` intent filter on
   `MotionLabActivity` with the label "Solstice Lab", in a `profile` (and `debug`) source-set manifest,
   so the release build is unchanged. It stays in Settings → Debug as well.
4. Optional: mention **Obtainium** in the README's testing section (it installs and updates from the
   `testing` pre-release automatically).
5. Write the steps for the user in `README.md` (Testing: where to download, "Install unknown apps"
   for the browser once, how the two icons look) and say in the PLAN.md log when the first build is up.

### 2. Check by hand on the Pixel 7 (profile build)

Not felt yet: toolbar and Places drag, catching the postcard window mid-close, messages (rise, sink,
fly off), page jumps, Places swipe actions. Icons on the screens not looked at yet: search, Settings,
details, Management (gauge → `speed`, UV → `wb_sunny`, wind → `air`, eye → `visibility`).

### 3. M5: the motion lab, before refining (**done 2026-11-19**: lab built and checked, README written)

A debug-only screen with each pattern (sheet, morph, window, toolbar bud, drag, swipe action,
message), a slow-motion switch (animator duration scale), a frame counter, and the tokens' values
shown. Then the `:motion` README for other projects. It makes refining "compare in the lab" instead
of "build, install, find the screen".

### 4. Refining animations, one area per round

Order: moon card, search, postcard window, toolbar, Places, messages. Each round: record the iOS
reference (Glasmio's notes for sheets and menus), compare in the lab, **propose changes, agree, then
tune**, check on the profile build. Rounds 1 (moon card), 2 (search) and 3 (postcard window) done. Carry into the rounds: `Motion.control` = `Motion.settle` (merge or tell apart).

### Also open, between rounds

- **Icons vs M3** (PLAN.md "Icons", gaps 1, 3, 4): grade −25 for light icons on dark backgrounds;
  audit small-icon labels, 48 dp touch targets, icon/text alignment; optical size 40 for big icons.
- **Tokens:** done 2026-11-19 (PLAN.md log); what stays local is on purpose (art, motion distances).
- **Navigation 3:** started on `main` 2026-11-20 (PLAN.md "Navigation 3"): N1 done (Settings on
  `NavDisplay`); next N2 (one Settings stack), N3 (adaptive Settings). New icons use `SolsticeIcons`.
- `PageScaffold` done (PLAN.md log); `Modifier.searchBlur` unused, `unresist` caps at 99.9 %.
- Old branches to delete on GitHub: `material-symbols`, `motion-framework-plan` (both merged).

Rules that still apply: propose and agree any new or changed motion before building it; keep the
PLAN.md log and [ROADMAP.md](ROADMAP.md) up to date; new code in `ui/expressive/`; check on the
Pixel 7, smoothness on the `profile` build (`./gradlew assembleBasicProfile`, installs as
`io.github.information89.solstice.profile`). Debug builds from different computers are signed with
different keys: copy `~/.android/debug.keystore` between them to avoid reinstalling.

How to add an icon: `tools/symbols.txt` + `python3 tools/make_symbols.py` (PLAN.md, "Icons").
Reference for iOS behaviour: `NEORUAA/Glasmio` (Apache-2.0), `docs/ios-sheet-motion.md` and
`docs/dropdown-motion.md`. Research only, not a dependency.

- **Performance:** judge smoothness on a release build; debug is much slower. Release builds take
  a long time the first time (R8).
- **Windows:** `.gitattributes` keeps LF so Spotless passes. In Git Bash, set
  `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"` (Android Studio's JDK 25 works).

## Set up a new computer

1. Install **Android Studio** (it bundles a JDK; the build needs **Java 21**), or install JDK 21 and
   the Android command-line tools yourself.
2. In the SDK manager, install **Android SDK Platform 37** and **Build-Tools 37**.
3. Clone and build:
   ```bash
   git clone https://github.com/INFORMATION89/Solstice.git
   cd Solstice
   ./gradlew assembleBasicDebug
   ```
   If not using Android Studio, create `local.properties` with `sdk.dir=<path to your Android SDK>`
   and set `JAVA_HOME` to a JDK 21. On Windows use `gradlew.bat`.
4. Install on a phone with USB debugging on:
   ```bash
   adb install -r app/build/outputs/apk/basic/debug/app-basic-arm64-v8a-debug.apk
   ```
   The debug app is `io.github.information89.solstice.debug`.

**Never** build with `-Pbreezy`: that config is reserved for the official Breezy Weather app.

## Updating from upstream (no shared history)

Keep a separate full clone of upstream, then apply the changes between our base and the new one:

```bash
git clone https://github.com/breezy-weather/breezy-weather.git breezy-weather-upstream
cd breezy-weather-upstream
git diff 5ec3b9bcb <new-commit> -- . ':!.github' ':!README.md' > ../upstream.patch
cd ../Solstice   # your clone of this repo (folder name may differ)
git apply -3 ../upstream.patch
```

Fix any conflicts, update `UPSTREAM_BASE` with the new commit and date, and note it in the PLAN.md log.

## Conventions

- Commit author: `INFORMATION89 <30944609+INFORMATION89@users.noreply.github.com>`.
- New code goes in `org.breezyweather.ui.expressive`; keep edits to upstream files small and list
  them under "Upstream files touched" in PLAN.md.
- Keep the PLAN.md log up to date.

## Related

- `INFORMATION89/Weather-App-Experiment` (private): the earlier standalone app. Its custom design
  (sky renderer, glass, sun path) was dropped for this fork; kept only as an archive.
