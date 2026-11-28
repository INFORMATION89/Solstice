# Canvas comments (2026-11-24)

What Marco added to the mockup canvas (https://claude.ai/artifact/PQ6PAqLwXQ4CnAQnExJk6p), read on
2026-11-28. The canvas holds one text note and four images; there are no comment threads. Images are
saved in `docs/references/canvas-2026-11-24/`.

This is a record, not a decision. Nothing here authorizes building (RULES.md section 1). Where the
canvas doesn't say what Marco wants from an image, that is marked **Unclear** and listed in the
questions at the end.

## 1. Text note (next to the Settings screen)

> Use colorful material icons like in pixel settings

- Placed right of the Settings screen, so it is read as a note on Settings rows (SETTINGS.md). It may
  also apply to the whole app. **Unclear.**
- Interface icons are Material Symbols through `tools/make_symbols.py` today (research/ICONS.md).
  I read "colorful" as each Settings row getting its own tinted icon badge, as in Pixel's Settings
  app. That is my reading, not something the note says.
- Colours must come from the palette, never hardcoded (RULES.md).

## 2. Images

| File | Where on canvas | What it shows |
|---|---|---|
| `outlook-reference-seoul.jpg` | below Outlook | Phone mockup, Seoul, light warm sky gradient. Large temperature, "Clear and calm", a feels-like line, a sun arc with sunrise and sunset, a "Today" hourly card, a "This week" card with one day marked ("only Thursday needs you") and a red alert card with the time and a one-ping line. |
| `forecast-sheet.png` | below the rain sheets | Dark sheet "How this forecast is made": next 12 hours with a band and "Storms likely" marker, a bar of what it is made of (ECMWF 40, ICON 35, GFS 25), a "−1.2° from local readings, last 14 days" chip, and "Checked against" rows (Airport MMMX, City station). |
| `rain-outlook-current.webp` | below Settings | Screenshot of the current rain screen: "Rain Outlook 85.8 mm", seven day pills, day header with arrows, Precipitation, hourly bars, "Rain, snow and ice". |
| `this-week-current.png` | left of Outlook | Screenshot (1:15) of the current "This week" page: rain summary text, seven day bars with tinted icons (rain in orange), and the bottom bar. |

What each could be used for (my reading from where they sit; Marco hasn't said):

- **Seoul mockup.** A reference for the Outlook screen: the proactive line under the temperature,
  the sun arc, a week card that says which day matters, and a single alert card. Many of these are
  already in PROPOSALS.md section 1 (Outlook). **Unclear** whether he wants the
  look copied, a few details taken, or the arc and card style compared.
- **Forecast sheet.** Looks like the "how this forecast is made" sheet from the mockups (sources and
  credits shown only in sheets, decided 2026-11-24). **Unclear** whether it is a layout to follow or
  something to check against our own mockup.
- **Current rain screen.** Visible in the shot: a day header ("Fri, Nov 27") drawn over the day pills
  while scrolling, so the two overlap and the text is cut in half. **Unclear** whether it was meant
  as a bug report. If so it is an app-side issue to track in ROADMAP.md and would need a repro
  before a fix.
- **Current This week.** Shows the starting point the Outlook replaces. The empty lower half and
  the plain card are probably the point. **Unclear.**

## 3. Questions for Marco

1. Does the icon note apply to Settings only, or everywhere?
2. For each image: reference to copy, detail to take, or a problem to fix?
3. Is the overlapping day header on the rain screen a bug to add to the app's task list?
