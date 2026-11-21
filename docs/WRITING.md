# Writing session: conditional texts

A worksheet for writing the sentences Solstice shows depending on the weather. For each text:
when it shows (the condition), what it says today, and room for your version. Fill in the
**Your version** column; placeholders like `{temp}` get filled in by the app.

How the app picks a text: it checks the conditions in each table from top to bottom and uses the
first one that matches.

## Ground rules (proposed, change them as you like; design rules in [DESIGN.md](DESIGN.md))

- **Say what's happening, not the numbers.** A title should make sense without reading the data
  ("Going down to 16° in the next 3 hours", not "Hourly").
- **Short and plain.** One idea per sentence; no "successfully", no exclamation marks.
- **Natural, not generated** (added by the user 2026-11-21). Forecasts are about the future, so say it
  with "will", in sentences a person would say, one fact each, instead of present-tense facts chained
  with "and". Not "Monday is the warmest at 35°, and the coldest night is Friday at 21°" but "Monday
  will be the warmest day, at 35°. The coldest night will be on Friday, at 21°." Same for "It's
  raining 3 days this week" → "It will rain on 3 days this week". Read every text aloud: if it sounds
  like a report generated from data, rewrite it.
- **Only what helps decide something.** Each sentence should answer a question the user has (do I
  need an umbrella, a jacket, when is it nicer); what that is for each summary is to be worked out
  with the user (DESIGN.md, check 2).
- **One question per summary** (agreed 2026-11-21): each summary answers one question ("What do I
  need to know about today?", "How will the week go?") with the top one or two facts from its
  priority list (PLAN.md "Summaries: what they say"). **If nothing is especially useful, say
  nothing extra.** Humidity, pressure and wind only when extreme.
- **Rain or snow → "precipitation"** (the user, 2026-11-21): when either could fall, write
  "Precipitation expected…"; only rain possible keeps "rain".
- **Same voice everywhere:** calm, a little warm, never alarmist (except real alerts).
- **Units and times** come from settings; write `{temp}`, `{time}`, `{minutes}`, `{hours}`, `{day}`.
- **Temperatures in the This week summary are bold and underlined** (future links to day cards).
- **Inline condition icons:** writing `[rain]`, `[snow]`, `[sun]` in a sentence puts the small icon
  there.

---

## 1. Forecast: sun path middle text

| When | Today | Your version |
|---|---|---|
| Daytime, within 1 hour of sunset | Sunset in {minutes} | |
| Night, within 1 hour of sunrise | Sunrise in {minutes} | |
| Daytime, UV < 3 | Weak sun, UV {uv} | |
| Daytime, UV 3–5 | Mild sun, UV {uv} | |
| Daytime, UV 6–7 | Strong sun, UV {uv} | |
| Daytime, UV 8+ | Very strong sun, UV {uv} | |
| Night | (the moon phase button) | |
| Ideas | e.g. "11 h 12 m of daylight" | |

## 2. Forecast: hourly card title

| When | Today | Your version |
|---|---|---|
| Rain, snow or storms within the next 6 h | {condition} from {time} | |
| Drops 2°+ within the next 6 h | Going down to {temp} in the next {hours} hours | |
| Rises 2°+ within the next 6 h | Going up to {temp} in the next {hours} hours | |
| Otherwise | Steady, {condition} for the next hours | |
| Ideas | windy hours, fog clearing, clearing up | |

## 3. Forecast: rain card (header is the sentence)

| When | Today | Your version |
|---|---|---|
| Raining now, stops within the hour | Rain is expected to stop in {minutes} min. | |
| Raining now, all hour | Rain is expected to continue for the next hour. | |
| Starts within the hour | Rain is expected to start in {minutes} min. | |
| Starts within 6 h (hourly data) | Rain is expected in about {hours} hours. | |
| No rain in the next 6 h (line below hourly) | No rain expected in the next {hours} hours | |
| Ideas | snow, drizzle, storms, on and off | |

## 4. Forecast: feels-like line

| When | Today | Your version |
|---|---|---|
| Feels different from actual | Feels like {temp} | |
| Feels-like is the main temperature | Actual {temp} | |
| Ideas | add wind: "Feels like 17°, wind 9 km/h from the sea" | |

## 5. This week: summary card

**Headline**

| When | Today | Your version |
|---|---|---|
| No wet days | A dry week ahead | |
| One wet day | Only {day} needs an umbrella | |
| Most days wet | A wet week, keep an umbrella close | |
| Some wet days | It's raining {count} days this week | |
| Ideas | heat wave, cold snap, getting warmer | |

**Sentence**

| When | Today | Your version |
|---|---|---|
| A wet day exists | Expect [rain] {condition} on {day}. | |
| Always | {day} is the warmest at **{temp}**, and the coldest night is {day} at **{temp}**. | |
| Ideas | weekend outlook, a big change ("Cooler from Thursday") | |

## 6. Messages

| When | Today | Your version |
|---|---|---|
| Opening a place from search | Visiting {place} | |
| A place removed | {place} removed · Undo | |
| A place starred | This is now your main place | |
| Ideas | pinned, refreshed, offline | |

## 7. Postcard window label

| When | Today | Your version |
|---|---|---|
| A visit in another time zone | Visiting · {time}, {difference} | |
| Loading state (planned) | Visiting Tokyo · 2:33 PM, 14 hours ahead | |

## 8. Moon card

| When | Today | Your version |
|---|---|---|
| Phase name | from the system (New moon, Waxing crescent…) | |
| Ideas | "Full moon in 5 days", "Moonrise at 14:18" sentences | |

---

## After the session

Your versions become the strings in `app/src/main/res/values/strings_expressive.xml`, and any new
conditions become rules in the code. The tables stay here as the reference for future texts.
