# Weather sources: design proposal

Status: **proposal, not agreed** (2026-11-23). Nothing here gets built until Marco agrees to it
(RULES.md: ask first). Research notes behind it are in PLAN.md, research queue 6 to 11.

## Goal

The best support we can give everywhere (Marco, 2026-11-23): every useful source stays in the app,
and the local ones (Mexico City first) make the forecast better where they exist. Nothing is
downloaded separately; sources that need a key, cost money or read a website start off and are
offered once where they help.

## How Breezy does it today

- Each source declares, in code, where it works (`isFeatureSupportedForLocation`) and how good it
  is there (`getFeaturePriorityForLocation`: 100 for a national service, -1 for "not recommended").
- When a place is added, `applyDefaultPreset` picks **one source per feature** (forecast, current,
  air quality, pollen, next-hour rain, alerts, normals, address) and stores its id on the place.
- Refresh groups features by those stored sources and calls each once.

Problems for what we want:
1. **One source per feature.** Alerts can't come from WMO and the city at once; air quality can't
   be "local station when fresh, CAMS otherwise".
2. **Picked once.** The current location keeps the sources chosen when it was created. Travel out
   of a local source's area and refresh just reports "unsupported feature".
3. **Coverage lives in code**, one `when` per source. That's hardcoding by our rules.
4. Failure means an error, not a fallback to the next best source.

## What others do

- **Open-Meteo (best match):** picks the highest-resolution model for each location automatically,
  from 1–2 km regional models to 9–11 km global ones. One answer, no user choice needed; individual
  models stay available for comparison.
- **Apple Weather:** per feature and per country. Severe weather comes from national services
  (including Mexico's SMN and MeteoAlarm in Europe); next-hour rain only where data is good enough;
  one legal page lists every source.
- **Gradient:** several sources, including public weather stations, fed into an "accuracy engine"
  that applies small corrections per location.
- **Acme:** its own forecast plus grey "alternate prediction" lines; a tight cluster means trust it,
  a wide spread means it may change.
- **Hello Weather:** the user picks a provider, and can switch to compare.
- **DewLogic:** blends 25+ sources at your coordinates into one "virtual station"; sources that
  disagree wildly with the rest don't count, and models count for less the more stations are nearby.
- **FOSS Public Alert Server** (KDE, already in Solstice): 249 CAP feeds, including civil
  protection (Germany has 6 providers, the US 6), plus Taiwan's NCDR and Japan's typhoon centre.
  No Mexico feed yet. It accepts new feeds by merge request.

## Proposed design

### 1. Coverage as data
Each source declares, in a data file next to its code: the countries or regions it covers (or a
bounding area for city networks), and for each feature a **tier**: `official` (national or city
service), `regional` (high-resolution model, local network), `global`, or `fallback`. Priorities
come from the tier, not from typed numbers. Upstream sources keep their code; we add the
declaration beside it, so upstream syncs stay simple.

### 2. Automatic by default, chosen per refresh
Every feature on every place is "Automatic" unless you pick a source by hand. Automatic resolves
at each refresh from the current coordinates, so travelling just works: the current location in
Mexico City uses the city network, and in Madrid it uses AEMET through WMO. A source you picked by
hand stays put.

### 3. More than one source per feature, with rules per feature

| Feature | Rule |
|---|---|
| Forecast | One main forecast (best tier). Later: blend several models and show the spread (Acme), then correct it by local error (Gradient). |
| Next-hour rain | Open-Meteo's 15-minute data and the blend first; our own radar nowcast later where public radar exists. Never shown where data is too coarse (Apple's rule). Rainbow.ai dropped (2026-11-23). |
| Current conditions | Nearest real observation if it's close and fresh (MMMX airport report, city station), otherwise the model. The thresholds (distance, age) are tokens. |
| Alerts | **All** sources that cover the place (WMO, FOSS Public Alert Server, national, city), merged. Duplicates collapse: same identifier, or CAP `references`, or same sender, event and overlapping time. The most local copy wins. |
| Air quality and UV | Local station when close and fresh (SIMAT in CDMX), otherwise CAMS. |
| Pollen, normals | As today: one source. |

### 4. Off until relevant
- Official, free sources with an API: on automatically where they cover the place.
- Sources that need a key or a card, cost money, or read a website instead of an API
  (CDMX network): off, offered **once** when you're in their area ("Mexico City has its own
  station network. Use it?"). The answer is remembered for that area.

### 5. Health and fallback
Each source call reports how fresh its data is. If a source fails or its data is stale, the next
tier fills in quietly; the sources sheet notes "City network unavailable, using global data".
Sources that read websites are marked best-effort and never the only source for anything.

### 6. Attribution
Each card's sheet names the sources behind what it shows ("Alerts: SMN via WMO · Protección Civil
CDMX"), and one Sources page lists everything with its terms and links, like Apple's legal page.
Required credits (Open-Meteo CC BY 4.0, Wikipedia CC BY-SA) shown where the terms ask.

### 7. Settings
Per place: a Sources page showing, per feature, what Automatic picked and why, with a manual
override. App-wide: keys, and the "offered once" answers. The source list only shows sources
that cover the place (Breezy already filters this way).

## Gradient, looked at closely (2026-11-23)

What it is, from its store page, site and privacy policy: an "accuracy engine" that picks the best
model for your location and compares forecasts with real observations to apply small corrections,
plus a map with personal weather stations and emoji reports from people nearby.

What feels intrusive, and why:
- **Background location** when alerts are on (a geofence around your neighbourhood; checked on the
  device).
- **Your location on their server:** for the imminent-rain alert, their server asks Rainbow.ai about
  your coordinates every 10 to 30 minutes. Weather warnings register your push token with your
  countries or US zones. Pushes go through Firebase.
- **Social features:** emoji reports (rounded to ~1 km, deleted after 24 h) and Family & Friends
  (other people's places and weather).
- **Usage events** sent to their own Cloudflare backend (no IDs, but still sent).
- The policy doesn't describe the accuracy engine itself, so you can't see what it does with your
  data.

Is it needed? The idea, yes; the way it's done, no:
- **Correcting a forecast by local observations** is long-standing meteorology practice and is what
  helps Mexico City most (the global models miss the valley's afternoon storms and heat).
- **It can all happen on the phone:** keep a short local log per saved place (what each source
  forecast, what the nearest observation said), and compute the correction from it. No server, no
  account, no background location beyond normal refreshes, nothing sent anywhere except the usual
  weather requests.
- **No social features, no usage events.** Rain alerts use the phone's own scheduled refresh
  (WorkManager), not a server holding your location.
- **Shown, not hidden:** the correction appears in the sources sheet (below), and can be switched
  off.

## Showing how the forecast is made

Transparency is a design feature, not a settings page (Marco, 2026-11-23). Ideas, to pick from with
Marco:

1. **Recipe bar** in each card's sheet: one horizontal bar split by how much each source counts
   ("ECMWF 40 · ICON 35 · GFS 25"), then a chip for the local correction ("−1.2° from Tacubaya
   station, last 14 days"). Tap a segment for that source's own number.
2. **Spread band** on the hourly chart: the blended line with a soft band whose width is how much
   the models disagree; optional thin lines for each model (Acme's approach). A narrow band reads
   "confident" without a word.
3. **Flow** ("How this forecast was made"): sources on the left, the blend in the middle, what you
   see on the right, with the correction as a step. One sheet for the whole place.
4. **Track record:** last 7 days, forecast against what was observed at your place, per source. It
   explains why the weights are what they are.
5. **Nearby observations:** the stations and airport used, with distance and age ("MMMX · 9 km ·
   20 min ago").
6. **Confidence dots** under the hourly strip, one per hour (full, half, empty).

Proposal: 2 on the main chart (it's the only one visible without tapping), 1 and 5 in each card's
sheet, 3 and 4 on the place's Sources page.

## Engine guardrails

Agreed 2026-11-23; they live in RULES.md, section 4 (Data, sources and privacy) and section 2.6.

## Sources to add or bring back

- **Bring back upstream's cut national sources** (40 packages, 1.36 MB on the debug build): they
  cost little and upstream maintains them. Japan, Taiwan and Türkiye need them for alerts; the
  rest add official forecasts where WMO only gives alerts.
- **Mexico:** SMN municipal forecasts (official, per alcaldía, no key); airport reports from
  aviationweather.gov (public JSON, no key, 100 requests a minute per device); CDMX REDMET/SIMAT
  (best-effort). SGIRPC alerts once a feed exists; we can also propose SMN's
  CAP feed to the FOSS Public Alert Server.

## Build order (each step checked on the phone before the next)

1. Coverage as data, and Automatic resolved per refresh (no new sources yet). Check: travel with
   the debug location setting between the US, Canada and Mexico.
2. Merged alerts (WMO + FOSS Public Alert Server), with duplicates collapsed.
3. Bring back upstream's national sources.
4. Observations for current conditions (airport reports first).
5. Mexico City local sources, offered once.
6. Blend and spread, then local correction (research queue 8, steps 1 and 3).

## Questions for Marco

1. Does "Automatic per refresh, manual override per feature" match how you want it to feel?
2. Bring back all 40 cut sources, or only those that add something WMO doesn't?
3. For best-effort sources (reading a website), offer them once, or keep them in settings only?
4. How visible should the source be on the main screen: only in the sheets, or a small line on
   each card?
