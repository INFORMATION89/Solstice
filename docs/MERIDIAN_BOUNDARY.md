# Meridian and Solstice: responsibility boundary

**Decision date:** 2026-11-26  
**Status:** Accepted  
**Related decision:** [Meridian ADR-0002](https://github.com/INFORMATION89/Meridian-Engine/blob/main/docs/decisions/ADR-0002-consumer-responsibility-boundary.md)

## The short version

**Meridian understands and explains the weather. Solstice decides what the person should do with that information.**

Meridian is an independent meteorological engine. It must remain useful to applications other than Solstice, so its public contracts cannot be shaped around Solstice's screens, wording, preferences, or lifestyle advice.

Solstice is a weather application. It can build a distinct, helpful, personalized experience on top of Meridian without making those product choices part of the engine.

## Responsibility split

| Meridian — meteorological engine | Solstice — application experience |
|---|---|
| Current conditions and forecasts | How conditions are presented in the UI |
| Source/model selection, blending, and correction | Which information is emphasized on each screen |
| Temperature, feels-like temperature, wind, humidity, precipitation, UV, and other weather variables | Clothing and footwear suggestions |
| Meteorological probabilities, uncertainty, confidence, and provenance | Whether to suggest carrying an umbrella, sunscreen, or another item |
| Weather-event detection and meteorological risk indicators | Activity, exercise, travel, and outdoor-planning advice |
| Rain/storm timing windows and explanations | Personalized routines, alert thresholds, notification wording, and delivery |
| Data quality, freshness, and limitations | User preferences, accessibility choices, and app-specific policy |

The table is a boundary of responsibility, not a limitation on Meridian's intelligence. Meridian should do substantive meteorological reasoning; Solstice should do product-specific recommendation reasoning.

## Examples

- Meridian reports a 70% precipitation probability between 16:00 and 18:00, with the source coverage and confidence attached. Solstice may recommend carrying an umbrella.
- Meridian reports high UV conditions for a time window. Solstice may suggest sunscreen or shade, using the person's settings and the app's wording.
- Meridian estimates a feels-like temperature and explains the meteorological factors contributing to it. Solstice decides how prominently to show it and whether it changes a clothing suggestion.
- Meridian detects a likely storm and describes timing, severity evidence, and uncertainty. Solstice decides whether and how to notify the person.

## Rules for implementation

1. Do not add clothing, umbrella, sunscreen, activity, or personal-routine recommendation policies to Meridian's core.
2. Keep Solstice recommendation logic in Solstice's own domain/application layer (or another explicitly consumer-specific layer).
3. Do not make Meridian depend on Solstice, Solstice's data models, preferences, UI, lifecycle, or repository.
4. Do not move personal preferences into Meridian merely to make recommendations easier.
5. Give Meridian enough neutral meteorological outputs for consumers to implement their own decisions: typed values, units, valid times, provenance, freshness, confidence, uncertainty, and explanations where appropriate.
6. Do not treat a consumer's recommendation as a meteorological fact. Keep the input weather evidence and the resulting advice distinguishable.
7. Any future generic recommendation capability proposed for Meridian requires a separate product review and must not silently expand the engine's data collection or privacy scope.

## Why this matters

This separation protects Meridian's independence and makes its API reusable across mobile, desktop, and unrelated applications. It also lets Solstice have a richer identity and evolve its recommendations without coupling every consumer to Solstice's assumptions.
