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
6. **This week becomes "Outlook"** (Marco, 2026-11-23): a proactive planning page, like Acme Weather's
   approach, not just a list of days. Examples: the best weekend day to be outside, notable days ahead
   (heat, a cold front, a rainy stretch), good evenings for the sky. The day list stays. What it answers,
   and how, to propose to Marco.
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
| Icons | Interface: Material Symbols, one catalog (`SolsticeIcons`), one weight (500), fill only for on/selected; **a setting for Rounded, Outlined or Sharp** (decided 2026-11-22; Rounded default). Weather conditions: **Material Symbols for now** (decided 2026-11-23: no animated icons); later option: **our own set built from Material Symbols' weather shapes** with a Solstice colour layer (two or three tones; Apache 2.0 allows it). Contrast checked on every sky. Moon phases stay the one exception. Coverage and gaps: docs/research/ICONS.md |
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

### Messages (decided 2026-11-22; a Motion UI component)
- Several at once: **two options in Motion UI**, queue-and-merge (priority order, repeats merge, one
  morphs into the next) or stacked above the toolbar.
- Up to **two lines** (the action moves below when it doesn't fit); always an icon, chosen by the
  message's kind (never matched from its text).
- **Accessibility:** follows Android's "Time to take action" timeout; a message with an action waits
  until dismissed when TalkBack is on.

### Weather engine (decided 2026-11-23; design in SOURCES.md, spec in ENGINE_SPEC.md, plan in MERIDIAN_PLAN.md)
- **Name: Meridian** (the sun's highest point at noon; an iOS app "Meridian: Daylight" shares the
  name, acceptable for a library).
- **Its own library repo**, like Motion: works on the phone, has no UI, any Android app can include
  it. Solstice imports and configures it, and shares its results with other apps on the phone
  (Breezy's data sharing). Written clean room (RULES.md section 5), no Breezy code in it.
- **No Rainbow.ai** (Marco: signing up asks for a card even for free use). Next-hour rain is phased:
  Open-Meteo's 15-minute data and the blend first; our own radar nowcast later, where public radar
  exists (optical flow, as in pysteps and LibreWXR).
- **Learns from public data first** (past forecast archives and station observations); the local
  correction runs on the phone.
- **Sharing data is opt-in, always.** Asked during onboarding, never on by default, and the screen
  shows exactly what would be sent and visualizes how it travels to us. Later: anonymized forecast
  errors per area, which needs a server. Rules in RULES.md section 4.
- **Licence: Apache 2.0**, like Motion (patent grant, any app can use it, works inside LGPL Solstice).
- **Solstice leaves the Breezy fork gradually** (decided 2026-11-23): the clean engine replaces
  Breezy's data layer and Motion UI its screens, part by part, so the app keeps working throughout;
  ties are cut once no Breezy code is left. Until then, Breezy's source code stays in the app as
  adapters plugged into the engine, and is rewritten clean over time, main sources first.
- **Room** for its database; Solstice **switches directly**, feature by feature (alerts first), no
  side-by-side step; **M0** (empty repo) at the end of Saturday if time is left (2026-11-23).
- Engine code starts after Saturday's session; Saturday stays as planned (guardrails, then Motion).

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
- Solstice keeps Breezy's weather sources and data behind a Solstice interface the UI never reaches
  past, and replaces all of upstream's UI, settings and widgets (so it doesn't look or behave like a
  clone). Breezy-derived code stays LGPL; Motion stays Apache.

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

1. **Card catalog leftovers:** where the switch for "important cards move to the top" lives.
2. **Outlook's content:** which proactive answers it gives (Direction 6); proposal in PROPOSALS.md.
3. **The card material's values** (opacity, blur), picked in the lab on Saturday.
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
   until relevant where needed (SOURCES.md).
6. Launcher icon drawing; distribution (GitHub only, or Obtainium/IzzyOnDroid too); twinkling stars.

7. **Clean room leftovers:** Motion Lab's 7 demo files still carry Breezy's LGPL header; first job on
   Saturday (CONTINUE.md, step 2).

## Research queue
Weather sources design (coverage, Automatic per refresh, merged alerts, transparency graphic, Gradient
review): `docs/SOURCES.md`, proposal awaiting Marco.
1. Gradient Weather's rain countdown on screen: how it looks and how it disappears.
2. The page receding behind a sheet, done cheaply (plan in LOG.md, 2026-11-22: dim + recede).
3. Long press alongside drag to reorder (iOS context menu as reference).
4. Proactive channels: Live Updates, daily brief, clothing.
5. Card explainers (swipe up past the details): short Solstice texts, with "Read more" from Wikipedia
   (CC BY-SA: credit and link, never silently edited).
6. **Rainbow.ai nowcast** (checked 2026-11-23; **dropped** the same day, see Weather engine): fits Breezy's keyed-source pattern (like AccuWeather: own
   key in the source's settings, per-feature choice, attribution). Gives minute-by-minute precipitation
   for 4 hours at 1 km (updated every 10 min), an hourly/daily Weather API and precipitation/cloud map
   tiles. Free tier is per account (5,000 requests a month per API on the pricing table), so each user
   brings a free key; a shared key would run out. Terms: key kept private, "Powered by Rainbow.ai" with a
   link wherever its data shows, not for safety-critical use (alerts stay official), may end free use
   without notice. Call it only when rain is plausible or the countdown runs.
   Terms read in full (2026-11-23, effective 2025-04-16): signing up asks for payment details even for
   free use (pay-as-you-go), and users must be 18 or older, so "bring your own key" is a setup step for
   adults with a card; say so in the source's settings. Attribution wherever its data or anything
   derived from it shows (so the blend too), reasonably prominent, following their branding guidelines;
   logo and name not altered. Apps may be distributed to anyone, as long as nobody gets direct API
   access; reselling or sharing a key is out. The key stays on the device, never in the repo or the
   build. Caching isn't addressed; keep it short (the data is stale in 10 minutes anyway). Rainbow can
   end access at any time without reason, so Solstice must work fully without it.
7. **Alerts coverage after the 2026-11-18 source cut:** Gradient gets official warnings from MeteoAlarm,
   NWS, ECCC, SACHET, Hong Kong Observatory, CWA and WMO's national services (CAP); Solstice keeps NWS,
   ECCC and WMO severe weather. Check what Europe and Asia lost.
   **Checked 2026-11-23** against upstream's SOURCES.md and WMO's source list (sources.json): the cut
   removed national alert sources for 13 European places (Météo-France, GeoSphere, DMI, Bright Sky,
   Veðurstofa, MET Éireann, LHMT, MeteoLux, IPMA…), 8 in Asia and the Pacific (China, HKO, BMKG,
   IMS, JMA, SMG, CWA/NCDR, MGM) and 17 African countries (ClimWeb). WMO lists an operating feed for
   all of them **except Japan, Taiwan and Türkiye**, so those three lost official alerts; everywhere
   else WMO stands in. (MET Norway stayed.) WMO also covers places upstream never had: Spain, Italy,
   UK, Netherlands, Belgium, Switzerland, India, Korea, Philippines, Australia, Brazil, Argentina,
   Colombia, Chile. Not checked: whether WMO's copy is as detailed or as quick as the national source
   (Germany's DWD directly is per municipality); FOSS Public Alert Server is still "early testing"
   upstream. Proposal: bring back JMA, CWA/NCDR and MGM for alerts only, or accept the gap.
8. **Localized accuracy and combined sources** (Marco, 2026-11-23; Mexico City is hard to forecast:
   afternoon storms shaped by the mountains and the city are poorly captured by models). Proposed in
   steps, each verified before the next:
   1. **Consensus:** several models from Open-Meteo (ECMWF, GFS, ICON, GEM…) blended, with their spread
      shown ("the models disagree today"), like Acme and meteoblue's multi-model view.
   2. **Nowcast first:** our own next-hour rain (Open-Meteo 15-minute data, then radar nowcast; Rainbow.ai dropped).
   3. **Learn locally:** keep a log of each source's forecast against what was observed at your place,
      and weight sources by their recent error there (what Gradient's "localized accuracy" implies);
      nudge the next hours by the current error.
   4. **Observations for Mexico City:** the airport METAR (MMMX, public), SMN's automatic stations
      (10-minute data, ~5 km representative radius), the Tacubaya observatory, CDMX's own network on
      aire.cdmx.gob.mx (hourly weather, air quality and UV per station; no official API found, open data
      portal has some sets). UNAM's 55-station rain network (Observatorio Hidrológico) is reported out
      of operation.
      Access (checked 2026-11-23):
      - **METAR MMMX:** the only one ready for "now": public, every 30–60 minutes, standard format.
      - **SMN stations:** SMN publishes the station list and a KMZ map; I found no download or API for
        the readings. The 10-minute averages reach SMN by satellite every 1 or 3 hours, so they can't
        drive "now" anyway; they're useful for step 3 (checking forecasts after the fact). Ask SMN, or
        check Conagua's hydrological system (SIH), before relying on them.
      - **CDMX network (REDMET for weather, SIMAT for air):** yearly CSV/Excel files plus the latest
        hours on the site; a BSD-licensed R package (aire.zmvm) and research scripts read the current
        hour from the site's pages, so it works but by reading pages, which breaks when the site
        changes and the site goes down at times. Treat it as best-effort with a fallback.
   A Solstice "blend" source behind the data interface (Q3).
9. **Conagua / SMN** (checked 2026-11-23): free web service, no key, forecasts per municipality (CDMX by
   alcaldía): hourly for 48 hours and daily for 3 days, updated hourly at :15, as one national gzip
   file of several MB. Its forecasts come from the FV3-GFS model, so it's local GFS, not a separate
   local model. Terms of reuse, the endpoint details and SMN's official warnings (CAP) to check.
10. **Local alerts for Mexico City:** CDMX's civil protection (SGIRPC) issues a colour alert per alcaldía
    for rain (green under 15 mm, yellow 15–29, orange 30–49, red 50–70, purple above), plus wind, hail,
    heat and cold, published on official channels; no machine-readable feed found yet. SMN adopted CAP
    in 2014 and Mexico is building a CAP cell-alert system.
    **SMN warnings already reach Solstice** (checked 2026-11-23): Solstice builds the `basic` variant,
    which includes WMO severe weather, and `SourceManager.getDefaultSourceForFeature` already makes WMO
    the default alert source. WMO's source list has "CONAGUA - Servicio Meteorológico Nacional de
    México" as registered, official and operating, with its own CAP feed. To confirm on the phone
    during the next storm warning: how fine its areas are (state, region or municipality). SGIRPC's
    per-alcaldía colours stay the gap; ask SGIRPC whether a feed exists.
11. **Mexico City air quality and UV:** SIMAT measures both per station; a local source would beat the
    global CAMS model there. Access is the same as REDMET above (site pages, best-effort).
12. **The engine on macOS and Windows** (checked 2026-11-23; spec in ENGINE_SPEC.md section 2):
    Kotlin Multiplatform is stable for Android, iOS and desktop JVM; Kotlin/Native builds a framework
    a Swift menu bar app can import. Jetpack's KMP support rates macOS tier 2 and Windows tier 3. Ktor
    has native HTTP engines for Apple (Darwin) and Windows (WinHttp). Windhawk mods are single C++
    files loaded into Windows processes, so a Windows helper runs the engine and the mod reads its
    result. Consequence now: the engine's core avoids Android-only code from day one.
13. **Next-hour rain without Rainbow.ai** (checked 2026-11-23): RainViewer's free radar forecast ended
    on 2026-01-01. Open-Meteo has real 15-minute data only for Central Europe and North America.
    LibreWXR (AGPL, self-hostable) blends optical-flow radar nowcasts with models for 60 minutes, with
    real radar for the US, Canada, Europe, Japan, Taiwan and a few more, but not Mexico. NOAA's
    Enterprise Rain Rate (GOES-18/19, Himawari-9; 2 km, every 10 minutes, open data on AWS) covers
    Mexico City from satellite; it replaced the GOES rain-rate product retired on 2026-09-01. SMN's
    Cerro Catedral radar near CDMX publishes images only; ask SMN.
14. **Privacy law and a server for opt-in sharing** (checked 2026-11-23; not legal advice):
    - **Mexico:** a new LFPDPPP took effect 2025-03-21; the authority is now the Secretaría
      Anticorrupción y Buen Gobierno (INAI is gone); a privacy notice (aviso de privacidad) is due when
      data is collected.
    - **EU:** truly anonymous data is outside the GDPR, but the EDPB's draft guidelines (2026-07)
      treat the anonymizing itself as processing, and location data is hard to anonymize. So what
      reaches the server (and its IP address) counts as personal data until it's aggregated: consent,
      a privacy notice, no IP logging, raw contributions deleted once aggregated.
    - **Hosting:** Cloudflare's free Workers plan allows 100,000 requests a day and its D1 database
      100,000 row writes a day, enough for a start; paid Workers begin at $0.30 per million requests.
15. **How much models disagree in Mexico City** (2026-11-23): meteoblue compares 12 models for CDMX
    (IFS, ICON, GFS, NAM, UKMO, GEM, ARPEGE, AIFS and its own NEMS), but its numbers are only in
    images. The hard season is the rainy one (June to early October, about 85 % of the year's rain);
    in November meteoblue rates predictability "very high". Measuring it properly needs a small
    script (Open-Meteo's Previous Runs API against the MMMX airport reports, rainy season 2026;
    spec in docs/research/MODEL_TEST_SPEC.md): a
    research job, Saturday step 3 (CONTINUE.md).
16. **Leaving the Breezy fork, inventory** (2026-11-23; Kotlin lines today): screens 39,000 (main 12.2k,
    common 8.8k, settings 7.3k, details 7.1k, theme, search, about, alert) → Motion UI; sources 8,700,
    common utilities 8,100, domain models 5,200, units 5,500, storage 2,300, map geometry 1,800 → the
    engine; widgets and the notification views 8,500 and background refresh and notifications 3,900
    → rebuilt on the engine; the old animated weather view 3,000 → dropped or replaced (check what still uses it); 65 translations →
    new strings, translated again. Proposed order: units and models (the engine's base), sources
    and refresh (adapters first), screens (ongoing with Motion UI), background and notifications,
    widgets and sharing, settings and the rest, then translations, then the package name
    (`org.breezyweather`) and licence change last. Translations and widgets: docs/research/AFTER_FORK.md.

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

## Breezy exit (decision 2026-11-25)

Solstice is to become an independent application and must progressively remove its dependency on
Breezy Weather. Breezy was a temporary experimental base, not the intended long-term architecture.

**Migration strategy: gradual replacement (Marco, 2026-11-25).** Keep the application usable while
each inherited subsystem is specified independently, rebuilt, tested, migrated, and only then removed.
Do not attempt a big-bang rewrite or delete inherited functionality before its replacement is verified.

The subsystem inventory, platform capabilities, Solstice-owned responsibilities, clean-room protocol,
and migration sequence live in [RECONSTRUCTION.md](RECONSTRUCTION.md). That document is the working
migration map; update it as file-level provenance and dependency audits are completed.

The existing LGPL-3.0 licence, additional terms, third-party notices, and copyright headers are not
changed by this architectural decision. Their scope and any future licensing transition require a
separate provenance and legal review.
