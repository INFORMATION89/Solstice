# Solstice card material — historical visual options

**Status:** visual research proposal, 2026-11-24. This document concerns Solstice's own card
appearance. It does not define a reusable framework or require a separate component project. Any
option selected here must be expressed through Solstice's own design tokens and checked in the app.

## Existing glass, for reference

The earlier shell used `surfaceContainerHigh` at 40 %, over a 28 dp blur, with a thin white edge at
22 %; without blur, the tint was 92 %. Cards shared the same glass at that time.

## Why cards may need a thicker material

Cards hold the most text and can sit over busy backgrounds (a bright day sky, stars, or a postcard
photo). Text contrast should remain stable as the background changes. A thicker, more opaque material
may improve readability at the cost of showing less of the background.

## Options for an app-side comparison

| Option | Tint | Blur | Edge | Character |
|---|---|---|---|---|
| **A. Frosted** | `surfaceContainerHigh` at 60 % | 40 dp | white 18 % | Similar to the current glass, but thicker |
| **B. Milky** | `surfaceContainerHigh` at 75 % | 24 dp | none, soft shadow | Reads as a card; the sky only hints through |
| **C. Sky-tinted** | sky colour mixed with `surfaceContainerHigh` (50/50) at 65 % | 32 dp | white 14 % | Takes some colour from the current sky |
| **D. Solid** | `surfaceContainer` at 94 % | none | `outlineVariant` | Fastest to draw and consistent on older phones |

## How to review

- Check readable text on a clear day, night sky, and storm background.
- Target at least 4.5:1 for body text and 3:1 for large text; fail any option that is not readable on
  all required backgrounds.
- Measure frame time while scrolling Forecast with several cards on a representative Android device.
- Review the actual appearance on a physical phone before choosing.
- Store the selected values in Solstice's tokens and keep reduced-motion/accessibility behavior clear.

This proposal is not approval to change the card material. Bring the comparison to the product owner.
