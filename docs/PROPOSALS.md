# Proposals waiting for Marco

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

Decided: every card opens a partial sheet first, then a full page (Marco, 2026-11-22); Apple's sheets
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

**Question for Marco:** search inside Settings, needed with nine sections?
