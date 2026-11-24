# Card material: options to compare in the Lab

Status: **options for Marco** (2026-11-24). Decision 6 (2026-11-22): glass for the toolbar, buttons and
sheets; a **thicker card material** for cards, picked with Marco in the Lab.

## Today's glass (for reference)

`surfaceContainerHigh` at 40 %, over a 28 dp blur, with a hairline white edge at 22 %; without blur
(older Android), the tint at 92 %. Cards use the same glass now.

## Why cards need a thicker one

Cards hold the most text, over the busiest backgrounds (a bright day sky, stars, a postcard photo).
At 40 % the text's contrast changes with the sky behind it. Apple's materials go from ultra thin to
ultra thick for the same reason: thicker where text must stay readable. Material 3 uses tonal surfaces
without blur.

## Options (all values become tokens; the Lab shows each on a clear day, a night sky and a storm)

| | Tint | Blur | Edge | What it's like |
|---|---|---|---|---|
| **A. Frosted** | `surfaceContainerHigh` at 60 % | 40 dp | white 18 % | today's glass, thicker; still sky-coloured |
| **B. Milky** | `surfaceContainerHigh` at 75 % | 24 dp | none, soft shadow (`Elevation.Card`) | reads as a card on the sky, the sky only hints through |
| **C. Sky-tinted** | the sky's main colour mixed with `surfaceContainerHigh` (50/50) at 65 % | 32 dp | white 14 % | each card takes the weather's colour, like Apple's thick material over wallpaper |
| **D. Solid** | `surfaceContainer` at 94 % | none | hairline `outlineVariant` | fastest to draw, Material-like; the same look on old phones |

## How to pick

- Text on each option, on the three backgrounds, measured in the Lab: body text at least 4.5:1,
  large text at least 3:1 (WCAG). An option that fails on any background is out.
- Frame time while scrolling Forecast with 8 cards (the Lab's frame meter): blur costs most.
- Then Marco's eye on the phone. The pick fills `CardStyle` in Solstice's tokens (MOTION_UI_MOVE.md).
