# Continuing on another computer

Where things stand and how to pick the work up anywhere. The plan, decisions and log are in
[PLAN.md](PLAN.md).

## Where we are (2026-11-21, midday)

- **Repo:** `INFORMATION89/Solstice`, upstream Breezy Weather's files at `5ec3b9b` **without
  upstream's git history** (see `UPSTREAM_BASE`). Never push upstream's history here. Fork differences
  to keep when syncing upstream: minimum Android 10 (`buildSrc/.../AndroidConfig.kt`), and the files
  listed in PLAN.md.
- **Read first:** [DESIGN.md](DESIGN.md) (Solstice's opinion and the 15 checks), [MOTION.md](MOTION.md)
  (springs only, morph never pop, never block input, one material per morph, `:motion` covers every
  interaction), [WRITING.md](WRITING.md), [UI_SYSTEMS.md](UI_SYSTEMS.md), and CLAUDE.md's rules:
  plans come from the chat environment, consult the user before adding anything they'll see, use the
  systems that exist.
- **The app:** the Solstice shell (`ui/expressive/shell/`: Forecast, This week, Places, the glass
  toolbar with its bud, search, the postcard window, messages, the moon card), Material Symbols Rounded
  icons (`SolsticeIcons`), design tokens (`theme/Tokens.kt`), Settings on Navigation 3 (step N1).
- **`:motion`** (`motion/`, Apache-2.0, unit tests in CI): tokens (response + damping, incl. `smooth`,
  `momentum`, `brisk`), `MotionValue` with UIKit's release rules, `Morph` and `SpringMorph` (iOS-style,
  in the lab), `PageTransition` and `pageSettleFling` (taps never blocked), `MorphStyle`/`morphSource`
  (the button becomes the card), **haptics** (`Haptic` tokens, `MotionHaptics`), **edge blur**
  (`Modifier.edgeBlur`, any edge), **dial** (`Dial`), **ambient clock**. The Motion Lab (its own
  launcher icon in test builds) has a demo and live controls for each.
- **Sky:** one AGSL shader with `SkyEffects` (stars; twinkle built, off until agreed).

## Right now (2026-11-21 afternoon, agreed with the user in chat), in this order

1. **Summary texts** (agreed wording; WRITING.md "natural, not generated"). No phone needed to build.
   | String | Now | New |
   |---|---|---|
   | `week_dry` | A dry week ahead | No rain expected this week |
   | `week_one_wet` | Only %1$s needs an umbrella | You'll need an umbrella on %1$s |
   | `week_some_wet` | It's raining %1$s days this week | It will rain on %1$s days this week |
   | `week_mostly_wet` | A wet week, keep an umbrella close | It will rain most of the week. Keep an umbrella close. |
   | `week_warmest` + `week_coldest_night` | Monday is the warmest at **35°**, and the coldest night is Friday at **21°**. | Monday will be the warmest day, at **35°**. The coldest night will be on Friday, at **21°**. |
   | `shell_hourly_down` | Going down to %1$s in the next %2$d hours | It will drop to %1$s over the next %2$d hours |
   | `shell_hourly_up` | Going up to %1$s in the next %2$d hours | It will climb to %1$s over the next %2$d hours |
   | `shell_hourly_steady` | Steady, %1$s for the next hours | It will stay around %1$s for the next few hours |
   | `shell_rain_stops_in` | Rain expected to stop in %1$d minutes | Rain should stop in about %1$d minutes |
   | `moon_lit_growing` / `_shrinking` | The moon is %1$s lit and growing. | Tonight the moon will be %1$s lit and still growing. (shrinking: "…and already shrinking.") |
   | `moon_rises_then_sets` / `moon_sets_then_rises` | It rises at %1$s and sets at %2$s. | It will rise at %1$s and set at %2$s. (and "It will set at %1$s and rise again at %2$s.") |
   - **Unchanged on purpose:** every "…rain expected…" text (`shell_rain_starts_in`, the plural, `shell_rain_next_hour`, `shell_no_rain`), "Expect [icon] … on %1$s", `moon_full_in` / `moon_full_today`.
   - The warmest/coldest pair becomes **two sentences**: `week_warmest` = "%1$s will be the warmest day, at"
     + temperature + ". "; `week_coldest_night` = "The coldest night will be on %1$s, at" + temperature + "."
     (`WeekPage.WeekSummary` adds the full stop after the first). "Today will be the warmest day" reads right.
   - Translations: English only for now; mark the old translations as needing an update (don't machine-translate).
2. **"Last updated" and the weather source at the bottom of Forecast** (asked for by the user, so no new
   proposal needed for the feature; check the look on the phone with the user). One small, quiet line at the
   end of the Forecast page, `labelMedium`, secondary colour:
   - "Updated 12 min ago · Open-Meteo" (the forecast source's name; several sources: the forecast one first,
     then "+2 more" only if attribution requires naming them all: check each source's licence, e.g. MET Norway
     asks for credit).
   - Time: "Updated just now" (under a minute), "Updated 12 min ago" (under an hour), "Updated at 3:40 PM"
     (today), "Updated yesterday at 9:10 PM", then the date. 12-hour clock, ticking each minute while shown.
   - States: "Updating…" while a refresh runs; "Couldn't update · Try again" when it fails (the only
     interactive part: a real text button, 48 dp target).
   - Use what exists: the location's `refreshTime` and the sources' names/attribution already in the app
     (upstream showed both); no new data.
3. **Refresh on a cold start: the main place** (the user's decision, replaces the 30-min proposal). When the app
   starts from a cold start (`MainActivity`'s `isLaunch`, `savedInstanceState == null`), refresh the **main
   place** (the first one on home) **whatever its age**; resuming from the background keeps today's rule. Then
   **check the API cost before calling it done**: count the requests one cold start makes per source (forecast
   plus secondary sources), confirm we honour MET Norway's `Expires` / `If-Modified-Since` and any source's rate
   limits, and how often Android cold-starts the app in normal use. Bring the numbers to the user; if needed,
   propose a small floor (e.g. skip when updated under 5–10 min ago).
4. **"Will I see it tonight?" in the moon card** (approved idea; part of F5). A tile or one line, written by
   WRITING.md's rules (future tense, natural, one fact per sentence, only what helps decide):
   - Data: the moon's time above the horizon tonight (rise/set from `MoonSky`), the night hours, and hourly
     `cloudCover` for those hours. "Visible" = moon up during the night and cloud cover under a threshold (to
     tune, e.g. 40 %).
   - Texts (to show the user before committing):
     - "You'll see it tonight." / "Clear skies the whole time it's up."
     - "You'll see it after 11 PM, once the clouds clear."
     - "Clouds will hide it until about 2 AM."
     - "Clouds will hide it tonight."
     - "It won't be up tonight." (sets before dark and rises after sunrise)
     - "Too close to new moon to see." (lit under ~3 %)
   - Times in 12-hour format; never more than two short sentences.
5. Then continue with **F5, the moon card** (below), which includes 4.

**Phone pass still due** for everything built today (list below). The user tests when the phone is connected;
items 1 and 3 can be built and unit-tested without it.

## Pick up here

**Built 2026-11-21 but not yet tested on the phone** (one pass, profile build, Pixel 7; details in the
PLAN.md log):
- Motion Lab: "Morph, iOS way (new)" (`SpringMorph`, with the 6 AM changes: reopen while closing,
  stronger text blur, press, speed limit, more bounce), Haptics panel (feel every token), Edge blur,
  Dial (drag, fling, ends, haptics, curvature), Toolbar.
- App: swipe then tap a postcard (`pageSettleFling`), the moon pill as the sheet's own glass, the moon
  icon's real tilt on Forecast, stars on a clear night (home and window), the window's toolbar (title
  folding, slide, blur), the moved haptics (same moments as before), icons on Settings, details and
  Management.
- Not re-measured since dropping the page recede: janky frames opening search and the moon card
  (`dumpsys gfxinfo`, 6 opens and flick closes each; before: search 8 % without the recede, moon 21 %).
  Then a Perfetto trace of a moon card opening for its remaining cost.

**Waiting on the user:**
1. **Two GitHub secrets** (`SOLSTICE_TEST_KEYSTORE_BASE64`, `SOLSTICE_TEST_KEY_PASSWORD`): until they're
   added, CI skips the rolling `testing` pre-release, so the user can't install builds without a
   computer. README has the steps.
2. **Two plans to approve:** `MOTION_LAB_PLAN.md` (lab redesign, compare with Google's defaults) and
   `TOOLBAR_MOTION_PLAN.md` (the toolbar on Apple's principles).
3. **What the summaries should say at all** (the wording is agreed, see "Right now"): next talk in chat.
4. Open questions: twinkling stars on or off, `Motion.control` vs `Motion.settle` (merge or tell apart).

**Next to build, in order** (each after the user agrees where it changes what they see or feel):
1. **F5, the moon card, direction A** (PLAN.md "Moon card redesign and its foundations"): the whole sheet
   is the night sky with `SkyEffects` stars behind translucent tiles; the `:motion` `Dial` with dates and
   phase icons, drum curvature, edge blur, ends and haptics; tiles for lit, rise/set and full moon;
   opened by `SpringMorph` once approved.
2. **Search's bubble and the postcard window on the morph pieces** (the button becomes the card), then
   moving the moon pill from `Morph` to `SpringMorph` once the lab version is approved.
3. **Unit tests** for the new `:motion` pieces: dial scale, haptic strength and the new spring tokens done
   (`FoundationsTest`, 2026-11-21); still to do: haptics rate limit, page settle, `SpringMorph` release.
5. Refining rounds 4–6: toolbar (per `TOOLBAR_MOTION_PLAN.md`), Places, messages.

**Open, between the above:** icons vs M3 (grade −25 on dark backgrounds, the small-icon/touch-target
audit, optical size 40 for big icons); the DESIGN.md audits (states, affordances, dead ends, screen
sizes, settings); Navigation 3 N2 (one Settings stack) and N3 (adaptive Settings); `Modifier.searchBlur`
unused; `unresist` caps at 99.9 %.

**Research the chat environment is doing** (PLAN.md "Research queue"): the page recede done cheaply as a
`:motion` piece, long press alongside drag to reorder, proactive channels (Live Updates, daily brief,
clothing), what the summaries should say, Smartspacer's SDK license. Far future: native Smartspacer.

Rules that still apply: propose and agree any new or changed motion before building it; keep the
PLAN.md log and [ROADMAP.md](ROADMAP.md) up to date; new code in `ui/expressive/` or `:motion`; check on
the Pixel 7, smoothness on the `profile` build (`./gradlew assembleBasicProfile`, installs as
`io.github.information89.solstice.profile`, signed with the shared test key in
`~/.android/solstice-test.keystore`).

How to add an icon: `tools/symbols.txt` + `python3 tools/make_symbols.py` (PLAN.md, "Icons").
Reference for iOS behaviour: `docs/MORPH_STUDY.md`, and `NEORUAA/Glasmio` (Apache-2.0)
`docs/ios-sheet-motion.md` and `docs/dropdown-motion.md` (research only, not a dependency).

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
