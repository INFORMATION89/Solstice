# Solstice roadmap

[Home](Home)

This page is the product-facing roadmap. It describes what users can expect from the application, not internal implementation plans for Motion UI or Meridian.

Release gates, MVP detail, and version naming live in the [release plan](https://github.com/INFORMATION89/Solstice/blob/main/docs/RELEASE_PLAN.md).

## Now — define and complete the MVP

Solstice is building the first complete everyday-weather experience:

- Current weather, with actual and feels-like temperature clearly visible.
- Hourly and daily forecasts.
- Search, current location, and saved places.
- Reliable precipitation timing and useful weather summaries.
- Official warnings where supported by reliable sources.
- The Outlook page: Up next, Best time to be outside, and the forecast day list.
- Contextual What to wear guidance.
- Clear data freshness, provider attribution, offline/error handling, privacy, and accessibility.

The MVP is not considered complete until the main weather flows work reliably on supported Android versions.

## Alpha — `0.1.0-alpha.1`

**Purpose:** test the core experience with a small invited group.

The first Alpha starts when users can install the app, choose or search for a place, see current/hourly/daily weather, save places, and use the core experience without startup blockers or data loss.

Alpha can be incomplete. It is for validating the direction and finding problems early.

## Beta — `0.9.0-beta.1`

**Purpose:** stabilize a feature-complete MVP with a wider opt-in testing group.

Beta starts after all 1.0 features are implemented. The scope is frozen; work focuses on reliability, accuracy presentation, accessibility, privacy, battery behavior, device coverage, and upgrade testing.

## Release Candidate — `1.0.0-rc.1`

The final production candidate. No new features; only release blockers and regressions are fixed. It must pass the release checks and be installable/upgradable with the production signing identity.

## Stable — `1.0.0`

The first stable everyday-weather release. It includes the agreed MVP and has passed the production release gates.

## After 1.0

### 1.x candidates

- Optional weather forecasts for calendar events, using Android's read-only calendar API.
- Expanded configurable weather alerts and richer Live Updates.
- A Solstice-designed widget experience.
- Adaptive layouts as Solstice adopts a stable reusable capability from Motion UI.

### V2/V3 or later

- A focused Wear OS companion, beginning with a weather Tile and watch-face complications.
- Broader watch experiences only if the lightweight surfaces prove useful.

These are roadmap candidates, not promised dates or committed version contents. Each requires a separate scope and acceptance review.

## Release timing

No calendar dates are committed. Alpha begins when the core end-to-end flow is ready; Beta begins when the MVP is feature-complete; 1.0 ships when the release candidate passes the stable-release gates.

The `testing` pre-release on GitHub is a rolling development build and does not count as Alpha.
