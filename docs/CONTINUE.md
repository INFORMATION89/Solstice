# Continuing on another computer

Where things stand and how to pick the work up anywhere. Read [RULES.md](RULES.md) first; decisions
are in [PLAN.md](PLAN.md), history in [LOG.md](LOG.md).

## Next session: Saturday 2026-11-28, 1:00 AM (planned in chat 2026-11-22)

Theme: **guardrails and the start of Motion as the UI framework.** Nothing on screen should change
this session except where Marco agreed to a change beforehand. RULES.md applies throughout: anything
not listed here, or any surprise, goes back to Marco before it's built.

**Decided in chat (2026-11-22, Marco):**
1. **Motion splits into two modules** in the same repo: `motion` (springs, gestures, morphs, haptics;
   Compose only) and `motion-ui`, the UI kit (theme, page frame, card, sheet pieces, header buttons,
   glass; adds Material 3 and Haze). `motion-ui` depends on `motion`, never the other way.
2. **Tokens: names in the kit, values from the app.** The kit defines the token names with neutral,
   Material-based defaults and a theme (like `MaterialTheme`) that apps fill in; Solstice passes
   today's values, so it looks the same.
3. **First pieces to move:** theme and tokens; glass and card; the page frame (scaffold, header,
   `FrameAction`, `FrameButton`, top edge); sheet pieces (handle, shape, screen corners, `SheetDialog`).
   These have no weather code today (except a few string resources, which become parameters).
4. **Springs become speed × bounce**, plus effects springs that never bounce (proposal below; confirm
   the table before Saturday).
5. **Reduce Motion ("Remove animations") follows Apple's list:** morphs and sheets become quick fades
   in place, blur hand-offs become plain crossfades, no bounce, no recede; drags still follow the finger.
   Built into every Motion piece.
6. **Two materials:** glass for the toolbar, buttons and sheets; a thicker card material for cards. Both
   are kit tokens. The card material's values get picked with Marco in the lab (that is the one visible
   change this Saturday).
7. **CI check: baseline, then shrink.** Today's typed-in values go on an allowlist Marco reviews; anything
   new fails the build; the list only shrinks.
8. **The Motion Lab becomes Motion's own app, built on the kit** (its first real user); Solstice's copy
   and the "Solstice Lab" launcher icon go.

**Spring tokens, mapping (response in seconds / damping; confirmed by Marco 2026-11-23):**

**Opinion (Marco, 2026-11-22): movement always has some bounce.** The lowest level is "subtle", not
none. The Motion Lab gets a switch to compare subtle against no bounce on every demo before it's final.

| | Subtle (0.9) | Slight (0.8) | Playful (0.5) |
|---|---|---|---|
| **Fast** (0.2) | `snappy` | | |
| **Default** (0.31) | `control`, `settle` (were 1.0) | `container` (0.78), `content` (0.85) | `playful` |
| **Slow** (0.4) | `dismiss` (0.39, was 1.0), `smooth`, `gentle` (0.44, were 1.0) | `brisk` (0.78), `momentum` | |

- **Effects** (opacity, colour, blur), Material's values: fast 0.10 s, default 0.16 s, slow 0.22 s,
  no bounce: opacity and blur can't go past "fully shown", so a bounce there reads as a flicker.
- **Reduce Motion** removes the bounce everywhere (Apple: tighten springs).
- `wobble` (0.31 / 0.32) stays as a named special for the toolbar bud (its animation stays).
- What changes in feel: closing, settling after a drag and controls gain a hint of bounce (MOTION.md's
  "dismiss: no bounce" and "settle: no bounce" rules change with it); `gentle` gets a little quicker.
  Old names stay as aliases until every use moves.

**Order for Saturday** (each step: build, check, commit; stop and ask if a step doesn't go as planned):
1. **Pull and read** RULES.md, PLAN.md, this file; `git submodule update --init`.
2. **Motion Lab licence headers (first job, Marco 2026-11-23):** 7 files in Motion's `lab/` still carry
   Breezy's LGPL header (`EdgeBlurDemo`, `MotionLabActivity`, `SettleDemo`, `ToolbarDemo`, `DialDemo`,
   `SpringMorphDemo`, `HapticsDemo`). Check each holds no Breezy code (compare against upstream; this
   is an audit, not building, so reading Breezy is fine here). Clean files get Motion's Apache 2.0
   header (`Copyright 2026 INFORMATION89`); anything Breezy-derived is reported to Marco, not fixed on
   the spot (RULES.md section 5). Commit in Motion, then bump the submodule in Solstice.
3. **Model spread test for Mexico City (Marco: asap, 2026-11-23):** a standalone research script, not
   app code, in `tools/model_test/`, built from `docs/research/MODEL_TEST_SPEC.md`. It uses Open-Meteo's
   **Previous Runs API** (fixed 1 to 3 day lead times; the Historical Forecast API only keeps the first
   hours of each run, so it would flatter every model) against the MMMX airport reports from the Iowa
   Environmental Mesonet's archive, June to mid-October 2026. These sessions can't reach those sites,
   so it runs as a manual GitHub Actions workflow (or on Marco's computer) and opens a pull request
   with the report.
4. **Guardrails:** the CI check (decision 7) on `ui/expressive/` and Motion's UI code (`dp`/`sp`
   literals, `Color(0x…)`, `spring(`, `tween(`, `delay(`); **show the allowlist to Marco** before
   merging. Run the app's unit tests in CI too.
   Then branch protection on `main` (Marco, 2026-11-23): block force pushes and deletion; once the
   checks exist, require them to pass before merging (sessions work on a branch, merge by pull request).
5. **Motion core clean-up** (plan: `docs/specs/MOTION_CORE_CLEANUP.md`; old Morph: removed, the moon card already uses `SpringMorph`): spring tokens (decision 4, with aliases); effects springs; Reduce Motion in
   `SpringMorph`, `BlurCrossfade`, `Dial`, page springs (decision 5); remove the old `Morph` composable
   if only the lab uses it; tests for each.
6. **`motion-ui` module** (order of the move: `docs/specs/MOTION_UI_MOVE.md`; sheet, message and settings
   rows: `docs/specs/MOTION_UI.md`; card material options: `docs/research/CARD_MATERIAL.md`): theme and tokens → glass and card (two materials, decision 6) → page frame →
   sheet pieces. Solstice imports each and passes its values. Check: screenshots of every page before
   and after match, except the card material.
7. **Motion Lab as its own app** (`docs/specs/MOTION_LAB_APP.md`) on `motion-ui`, one page per piece (core and kit); remove Solstice's
   copy.
8. **Docs:** Motion README (all pieces, the new springs, the kit), UI_SYSTEMS.md, MOTION.md's token
   table, LOG.md, ROADMAP.md.
9. **Phone pass**, if the Pixel is connected: the checklist in `docs/research/PHONE_PASS.md` (the
   "built, not checked" items further down this file, and the spring changes above).
10. **Meridian M0, only if time is left (Marco, 2026-11-23; checklist: `docs/specs/M0_REPO.md`):** create `INFORMATION89/Meridian`
    (Apache 2.0): an empty Kotlin Multiplatform project with the modules in MERIDIAN_PLAN.md section 3,
    the licence, a README, a copy of the clean-room rule, and CI building on Linux. No engine code yet:
    M1 starts from its own spec section in a later session. Add it to Solstice as a submodule only
    when M1 has something to use.

If time runs short, stop after a finished step; the rest moves to the next session in this order.

**Bug review (2026-11-22):** ROADMAP.md "Review 2026-11-22" (B1–B5, D1–D3, F1–F5, R1–R3, Q1–Q3).
Its R3 findings shape step 5 (Motion core); B3 and B4 come from the same code. Ask Marco before adding any
of them to this Saturday.

**Not this Saturday** (proposals to Marco first, in chat): the new app layout (not an endless list),
the postcards' standard look, proactive alerts the Gradient way (PLAN.md Direction 4), the spring
token grid and Reduce Motion unless decided, interactive glass.

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
   - **Built 2026-11-21** (not seen on the phone). Two additions so "Today" reads right: `week_one_wet_today`
     ("You'll need an umbrella today") and `week_coldest_tonight` ("The coldest night will be tonight, at").
     The moon's rise/set lines say "The moon will…" (the user's later rule: never "it"). `shell_hourly_steady`
     now takes the temperature. No translations of these strings existed, so none to mark.
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
   - **Changed after (2026-11-21):** the "…rain expected…" texts say **"precipitation"** when snow is
     possible too, "snow" when only snow is (PLAN.md "Summaries: what they say"); rain-only stays "rain".
     Same rule for the This week texts (draft wording in PLAN.md, show the user first).
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
   - **Built 2026-11-21, look not yet checked on the phone with the user.** Shared pieces in
     `shell/Freshness.kt`: `Freshness` (pure, 3 tests in `FreshnessTest`), `freshnessText`, and `UpdatedLine`
     (any page or card can use it). On Forecast and in the postcard window (no "Try again" there). A refresh
     counts as failed when it ends without moving the place's update time forward. Times follow the
     system's 12/24-hour setting; older than yesterday shows the date. **Still open:** only the forecast
     source is named; "+N more" waits on checking which sources' licences ask to be named.
3. **Refresh on a cold start: the main place** (the user's decision, replaces the 30-min proposal). When the app
   starts from a cold start (`MainActivity`'s `isLaunch`, `savedInstanceState == null`), refresh the **main
   place** (the first one on home) **whatever its age**; resuming from the background keeps today's rule. Then
   **check the API cost before calling it done**: count the requests one cold start makes per source (forecast
   plus secondary sources), confirm we honour MET Norway's `Expires` / `If-Modified-Since` and any source's rate
   limits, and how often Android cold-starts the app in normal use. Bring the numbers to the user; if needed,
   propose a small floor (e.g. skip when updated under 5–10 min ago).
   - **Built 2026-11-21** (not checked on the phone): `MainActivity` (cold start) → `requestColdStartRefresh()`;
     the next update check refreshes the main place once if it's the place shown. The "too frequent"
     snackbar now only shows when the user asked for the refresh.
   - **API cost (measured from the code):** a refresh asks each source only for features past their own
     wait (`RefreshHelper`: forecast 5 min, 15 min for restricted sources; air quality, pollen, alerts 5 min
     to 1 h). So a cold start within 5 min of the last update makes **no weather requests**. Otherwise,
     with Open-Meteo for everything: **2 requests** (forecast with current and minutely; air quality with
     pollen). "auto" may mix in a national source for alerts or minutely: **+1–2**. Reverse geocoding is
     offline (Natural Earth). OkHttp has a 50 MB HTTP cache, so `Expires` / `Last-Modified` (MET Norway)
     are honoured with conditional requests. **No extra floor needed**; how often Android cold-starts the
     app is still to watch on the phone.
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
   - **Shared, not moon-only** (the user: "don't hard code anything, the moon card is one of many future
     cards"): `sky/SkyVisibility.kt` answers "will I see it tonight?" for any sky object (`Tonight`,
     `VisibilityRules`, `VisibilityTexts`, 10 tests in `SkyVisibilityTest`); a card gives only its altitude,
     its "too faint" rule and its texts (`shell/MoonVisibility.kt`). Cards' sentences share `sentences()`,
     `ValueStyle` and `sentenceTime()` in `shell/Format.kt` (follows the system's 12/24-hour setting).
   - **Logic built 2026-11-21**: dark =
     sun below -6°, up = moon above 3°, checked every 10 min, clear = cloud cover under 40 %, too new under
     3 % lit, no answer when cloud data is missing. Answers: Clear (maybe with a late rise), AfterClouds,
     UntilClouds, Breaks, Hidden, NotUp, TooNew. Shown as one line in the moon card's details (tonight only), until F5 gives it its tile.
   - **Texts, agreed 2026-11-21** ("more human, more neutral", always "The moon…", never "it", "from" for a start time; these replace the list above):
     Clear "The moon will be visible tonight." · late rise / clouds clear "The moon will be visible from
     11 PM." · clouds come in "The moon will be visible until 1 AM." · breaks "The moon will be visible at
     times from 10 PM." · hidden "The moon won't be visible tonight." · not up "The moon won't be up
     tonight." · too new "The moon will be too thin to see tonight."
5. **What the summaries say** (agreed 2026-11-21, PLAN.md "Summaries: what they say"): build the
   priority-list summary engine (shared, unit-tested; no phone needed), the 6 PM evening switch, the
   "precipitation" rule, and check whether our sources give yesterday's data. **Show the user every new
   sentence and the evening-time setting before shipping.** Onboarding steps: propose to the user first.
   - **Engine built 2026-11-21** (`shell/Summary.kt`, 5 tests in `SummaryTest`): `Summary(rules, max = 2)`,
     `SummaryRule`, `SummaryLine` (with a topic, so two lines never say the same thing), `SummaryTime`
     (evening from 6 PM, adjustable), `Precipitation` (rain, snow, or mixed; `combine` for the week from
     each day's kind). **Not wired to any page yet:**
     next, write Forecast's and This week's rule lists and show the user every sentence first.
6. Then continue with **F5, the moon card** (below), which includes 4.

**Phone pass still due** for everything built today (list below). The user tests when the phone is connected;
items 1 and 3 can be built and unit-tested without it.

## Pick up here

**Moon card on `SpringMorph` (2026-11-21, the user asked; tested on the Pixel):** opens from the pill with
the five springs and blur replace, the dial scrubs while open, a flick down folds it home. `SpringMorph`
gained (generic, `:motion`): a `material` slot (glass at the surface's bounds), a `shape` hook (the sheet
squares its docked corners), and touches for the content once open and at rest (taps and sideways
drags are the content's; a vertical drag no child took pulls the surface). Drags now track 1:1 on
screen as the surface scales. The label no longer flies to the heading (blur replace instead, as in the
lab). Fixed after the user's phone check: the pill's label is centred during the hand-off, and drags are
measured on screen (measured inside the shrinking surface, a pull ran away to the bottom on its own);
a finger's drag range is how far an edge travels.

**Postcard windows on `SpringMorph` (2026-11-21, built, NOT yet tested on the phone; the user said to
keep testing for later):** `PlaceWindow` is now a `SpringMorph` from the postcard (or the search card):
the postcard (`PlaceCard`) blurs out as the window sharpens in; the window is carried at full layout.
The old paper frame thinning, stamp fade and non-uniform sky stretch are gone (replaced by the blur
replace). `SpringMorph` gained `fromBounds` (grow from a rectangle; the key only hides the source),
`canPull` (a pull at the top of the page closes; otherwise the page scrolls), and `MorphMotion.scrub`
(predictive back). Postcards are `morphSource`s (`postcardKey`). The shell has one `windowMotion`
(no more presence/pull/closing). **To test on the phone:** open from a postcard and from search; pull
down at the top of Forecast and of This week (and scrolled down: it should scroll first); back gesture
(follow, cancel, commit); tap while closing reopens; an unkept visit fades away; pin.

**Second round 2026-11-21, to check on the phone** (PLAN.md log): reopening the moon card or a postcard
window while it closes; taps never close a window; **the postcard window is now a sheet** with a handle
(pull from the top to close, pull on the page to refresh, tap above to close); the "Visiting" line
scrolls with the page (`VisitingLine` in the page frame); a dry week's headline is the temperature trend.
Planned, not built: an analog clock icon showing the real time.

**Fixed 2026-11-21 evening, to check on the phone** (PLAN.md log): holding the open moon card no longer
closes it; the moon pill has its glass background (sky glass); a postcard window refreshes on a pull,
and closes on a pull from its top strip ("Visiting" line) or back. **Next after this: the moon card
(F5).**

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
clothing), what the summaries should say.

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

## Motion (submodule)

Since 2026-11-21 `motion/` is a git submodule: [INFORMATION89/Motion](https://github.com/INFORMATION89/Motion)
(Apache-2.0), built from source with `includeBuild` (`settings.gradle.kts`); the app depends on
`io.github.information89:motion`. Plan and phases: Motion's `docs/MOTION_REPO_PLAN.md`.

- **A Motion change lands in Motion first.** Edit in `motion/` (a real Motion checkout) or in a
  separate clone (`~/Developer/Motion`), commit and push to Motion, then in Solstice commit the new
  pointer: `git -C motion pull` (or `git submodule update --remote motion`), build and check, then
  `git add motion` and commit "Bump Motion to abc1234: what changed". Never commit `motion/` files
  in Solstice's own history.
- **After pulling Solstice**, run `git submodule update --init` so `motion/` matches the pointer.
- **Gradle paths:** Motion's tasks run as `:motion:motion:<task>` from Solstice (e.g.
  `./gradlew :motion:motion:testDebugUnitTest`), or as `:motion:<task>` inside `motion/`.
- The Motion Lab still lives in Solstice (`ui/expressive/lab/`) until phase 2 moves it to Motion.

## Set up a new computer

1. Install **Android Studio** (it bundles a JDK; the build needs **Java 21**), or install JDK 21 and
   the Android command-line tools yourself.
2. In the SDK manager, install **Android SDK Platform 37** and **Build-Tools 37**.
3. Clone and build:
   ```bash
   git clone --recurse-submodules https://github.com/INFORMATION89/Solstice.git
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
