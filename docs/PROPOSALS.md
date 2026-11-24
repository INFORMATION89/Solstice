# Proposals waiting for Marco

Mockups (sample data): Outlook, a rain card's sheet at both heights, onboarding and Settings, on the
canvas https://claude.ai/artifact/PQ6PAqLwXQ4CnAQnExJk6p (2026-11-23).

Status: **proposals** (2026-11-23). Nothing here is built until it's agreed (RULES.md section 1). Once
a part is agreed it moves to PLAN.md "Decisions in force" and leaves this file.

## 1. Outlook (replaces "This week")

Direction 6: a proactive planning page, like Acme Weather, not just a list of days. The day list stays.

**Shape of the page, top to bottom**
1. **Up next:** at most three notable things in the coming days, most important first. Examples:
   "Rain likely Thursday afternoon", "Coldest morning this week: Saturday, 6°", "Heat on Monday: 30°".
   Nothing notable: one line, "A steady week", and the list below.
2. **Best time to be outside:** for each of the next days, the best part of the day (morning,
   afternoon, evening), from a comfort score: temperature in a comfortable band, low rain chance, light
   wind, UV not very high, air quality good. Weekend days get a "better day" pick when one is clearly
   better.
3. **Changes ahead:** a cold front, a big temperature drop or rise, the start or end of a rainy stretch,
   the first rains of the season.
4. **Evenings for the sky:** clear evenings, with the moon phase (dark sky near the new moon).
5. **The day list:** 10 days, as today, each with the confidence from Meridian's spread.

**Rules that keep it honest**
- Every answer shows how sure it is: "models agree" or "could change" (from the spread, ENGINE_SPEC.md
  section 7). Past a set number of days ahead (a token, about 5), only trends, never "best day" picks.
- Each pick says why in a few words ("dry, light wind, 22°") and opens the day's detail.
- The comfort band, weights and thresholds are tokens; the person can nudge "comfortable" warmer or
  cooler later, in Settings.
- Outlook items can become proactive notifications (Direction 4), off unless switched on.

**Later, not first:** "Plan something": pick an activity (a run, drying laundry, a picnic, photos at
golden hour) and Outlook finds the best slot.

**Decided (Marco, 2026-11-23):** first version is Up next, Best time to be outside and the day list;
then Changes ahead; then Evenings for the sky. Outlook looks 10 days ahead (picks within about 5).

## 2. Partial sheets per card

Decided: every card opens one sheet with two heights, no separate page (Marco, 2026-11-22, refined 2026-11-23); Apple's sheets
(a medium and a large height, a grabber, one sheet at a time). Pattern for every card:

- **Partial (medium height):** the answer and its timeline. What the card says, bigger, plus the next
  hours, plus one line of "what to do" where it applies.
- **Full (large height, swipe up or tap the grabber):** the detail, then the explainer (a short Solstice
  text with "Read more" from Wikipedia, credited), then "Where this comes from" (sources and the
  transparency graphic, SOURCES.md).

| Card | Partial | Full adds |
|---|---|---|
| Alerts | Title, severity, until when, the first lines of the official text | Full official text, instructions, the issuing office, the area; several alerts as a list |
| Rain and snow | Start or stop time, next 2 hours by 5 or 15 minutes, chance and amount | Next 48 hours, totals, rain type, the rain rule explained |
| Hourly | Next 12 hours: temperature, icon, rain chance | 48 hours with wind, feels like, humidity; spread band |
| UV (opens the sun sheet) | Index, timeline of the next hours, what to do for the level | Sunrise, sunset, daylight length, golden hour, the sun path |
| Air quality | Index, level, main pollutant, what to do | Each pollutant, next days, the station or model used |
| Wind | Speed, gusts, direction, next hours | 48 hours, Beaufort-style description |
| Feels like | Feels-like value and why (wind, humidity, sun) | Next hours, how it's calculated |
| Humidity and dew point | Humidity, dew point, "muggy" or "dry" | Next hours |
| Pressure | Value, 3-hour trend, what the trend usually means | 48-hour graph |
| Visibility | Distance and cause (fog, haze, rain) | Next hours |
| Pollen | Main allergen and level | Each allergen, next days |
| What to wear | The suggestion for now or tomorrow morning, with the reasons | Through the day: layers, umbrella, sunscreen |
| Moon (hero button) | Phase, rise and set | Next phases, the moon card redesign (on hold) |

**Decided (Marco, 2026-11-23, after Apple's sheet guidelines and Apple Weather):** one sheet with two
heights, no separate page; the map stays full screen; the grabber is tappable. The table above is the
proposal for each card's contents, still to confirm card by card.

## 3. Settings structure

Rules: DESIGN.md rule 3 (a setting exists only for a real choice); Motion UI pieces (category icons in
coloured shapes, inline choice buttons for 2 or 3 options, choice sheets for lists, large title that
shrinks on scroll); every screen one job.

**Home, in this order**
1. **Places:** home (current location or a fixed place), travel swap, the saved list.
2. **Forecast:** card order, each card on, off or "when it matters", whether important cards move to the
   top (PLAN.md open question 1 lands here), the evening switch time.
3. **Outlook:** how far ahead, "comfortable" warmer or cooler.
4. **Notifications:** rain, official alerts, what to wear, Outlook items, Live Updates; each opens its
   own options.
5. **Units and language:** follow the system by default.
6. **Appearance:** theme, icons (Material Symbols now, our own set later), close button side.
7. **Data and sources:** per feature Automatic or a chosen source, keys, the "offered once" answers,
   background refresh, sharing with other apps on the phone.
8. **Privacy:** the data-sharing opt-in (same screen as onboarding), what's stored on the phone (the
   30-day forecast log), clear it.
9. **About:** version, credits and licences, required source credits, source code link.

Debug stays hidden (debug builds only).

**From today's screens:** Location → Places; Main screen → Forecast; Weather sources and Background
updates → Data and sources; Modules (sharing with other apps) → Data and sources; Notifications,
Units, Appearance stay; Debug hidden.

**Decided (Marco, 2026-11-23):** Outlook keeps its own section.

**Decided (Marco, 2026-11-24): search at the top of Settings.** Like Android's and iOS's: it finds
every setting by its title and a few keywords per row (English and Mexican Spanish), lists matches
with their section, and opening one scrolls to the row and highlights it briefly. Spec to write with
the settings rows (MOTION_UI.md section 3).

## 4. Proactive alerts (PLAN.md Direction 4, open question 4)

**Approved by Marco 2026-11-24**, with the rain countdown also offered in onboarding (ONBOARDING.md
step 2). Wording in WRITING.md section 9.

**First set** (the ones that answer "do I need to do something now?"):

| Alert | Default | You set | Ends or repeats |
|---|---|---|---|
| Precipitation soon, with a live countdown | off | heads-up 10, **15** or 20 min before; which places | ends when it starts or the chance passes; once per rain spell, then 3 h quiet |
| Official warnings | **on**, severe and above | minimum level (all, moderate, severe, extreme) | once per warning; again only when it's updated or upgraded |
| Tomorrow, in the evening | off | time (default 8:00 PM) | once a day; skipped when nothing is worth saying |
| Very high UV | off | level (default 8, WHO "very high") | once a day, in the morning before it gets there |
| Frost tonight | off | temperature (default 2°) | once a night |

**Later** (each needs its data first): pollen per allergen, air quality dropping, rapid pressure
changes, what to wear.

**Where you switch them on:** in the card's own sheet (the rain card's sheet has the countdown switch,
the UV card's the UV alert), and all together in Settings › Notifications. The rain countdown is also
offered in onboarding; for the others, the first time you open a card that has an alert, a one-line
hint under its title.

**How it runs** (from SOURCES.md, no server): the phone's scheduled refresh checks the minutely
forecast more often only while a rain alert is armed and rain is possible in the next 2 hours
(otherwise the normal schedule). Notification permission is asked when you first switch an alert on,
never at start. The countdown is an Android Live Update (you started it, it has a Stop button);
older Android versions get an ongoing notification with the same text.

**Rules:** the same thing never notifies twice (cooldowns above); quiet hours follow the phone's Do
Not Disturb, except extreme official warnings; every notification opens the matching card's sheet.

## 5. A sky that means something (Marco's earlier ideas, added 2026-11-24)

Marco's two notes: make the sun/moon light show the weather without numbers, and move only at
meaningful moments instead of drifting all the time. Checked against the sky code
(`ui/expressive/sky/`) and the 2026-11-18 decisions (archive: gradient field, calm drift, a soft
sun light at the real position, "not a hard disc"; AGSL on Android 13+ at 30 fps, paused when hidden,
in battery saver and with Remove animations).

| Idea | Today | Viable? How |
|---|---|---|
| **Position:** the real sun or moon position | **Built:** the glow follows an arc from sunrise to sunset (and the moon's at night) and sways a little | Yes, small: use the sun's real height (the moon dial already computes the moon's), so the light matches the sun path curve on Forecast |
| **Sharpness:** crisp when clear, hazy when cloudy, almost gone in fog | Partly: the glow's strength depends on the weather family (0.9 clear, 0.75 partly cloudy, 0.35 overcast) | Yes, small: size and softness from cloud cover (%) and visibility, continuous. **A crisp disc goes against "soft light, not a hard disc" (2026-11-18): question below** |
| **Warmth:** warmer or cooler with how the temperature feels | No | Yes, but subtle: shift the glow's colour a little with feels-like; the approved palettes stay. Risk: confused with golden hour. Try in the Lab first |
| **Drift speed follows the wind** | Always drifts (loops of 45–85 s) | Yes, small: drift speed from wind speed; still air is still. This also makes the drift a meaningful moment (idea 4) and saves battery when calm |
| **Rain:** a fine vertical streak texture | Planned slot ("rain, snow, fog, lightning later", `SkyEffects`) | Yes, medium: streaks in the grain, amount from rain now; Android 13+, static streaks on older phones |
| **Opening the app:** the light rises into place | No | Yes, small: once per cold start, a slow spring from the horizon; never delays content or taps; skipped with Remove animations |
| **Pull to refresh:** a pulse | No | Yes, small: the glow pulses once **when new data arrives** (not when you pull), next to the existing refresh indicator |
| **Changing conditions:** a slow colour change | **Gap:** new data with a new condition switches the colours at once | Yes, small, and it fixes a visible jump: blend to the new palette over a few seconds |
| **Swiping between pages:** the light shifts a little, like a panorama | No | Yes, small: the light moves a few % against the swipe (Forecast, Outlook, Places, and between places) |

**Fit with the plan:** the sky stays Solstice's own (ROADMAP: sky, sun path and moon renderer stay in
the app, built from kit parts), so this comes after Saturday's motion-ui move. It uses the motion
tokens (the rise and the panorama as springs, colour changes as effects springs) and the ambient clock
already in Motion. Data (cloud cover, visibility, wind, feels-like, rain now) already exists in
Breezy's current weather, so it doesn't wait for Meridian. Twinkling stars (still off) belong to the
same "meaningful" question.

**Suggested order:** condition change blend (fixes a jump) → drift follows wind → sharpness → rise on
opening → panorama → refresh pulse → rain streaks → warmth (Lab test).

**Decided (Marco, 2026-11-24):**
- The soft gradient light stays (no disc); it needs motion. When clear, **a bit tighter and brighter**.
- **Every small addition is Solstice work** (ROADMAP.md "Sky"): condition-change blend, drift follows
  wind, sharpness, rise on opening, panorama, refresh pulse. **Rain streaks: yes.** Warmth: a Lab test
  first (on hot days a warmer light could be confused with sunrise and sunset colours).
- **Twinkling stars: on.**
- **Pull to refresh needs a new interaction** (the floating icon is dull) and **transitions when new
  data arrives**: ideas below.

### Pull to refresh: ideas (for Marco to pick, 2026-11-24)

Rules from the platforms: the pull follows the finger with rubber-banding, a threshold you can feel
(a haptic tick), release to refresh, and it never blocks scrolling or taps; TalkBack gets a "Refresh"
action; Reduce Motion gets a plain fade.

- **A. Pull the sky:** pulling down lowers the sun light towards the horizon, as if pulling the sky
  down; past the threshold, a tick; on release it springs back up; while loading, the light breathes
  slowly; when data arrives, one pulse and the sky blends to the new weather. No icon at all.
- **B. Horizon line:** a thin arc at the top bends with the pull like a rubber band and fills as you
  reach the threshold; while loading it shimmers; when done it flattens with a bounce.
- **C. The temperature:** the big temperature stretches a little with the pull; while loading its
  digits soften; when data arrives they roll to the new value.
- Could combine A with C: the sky shows the pull, the numbers show the new data.

**Transitions when new data arrives** (whichever is picked): numbers roll to new values; card texts
cross-fade (effects springs); cards that appear or disappear (an alert, rain) grow or shrink in place;
the sky blends; nothing jumps.
