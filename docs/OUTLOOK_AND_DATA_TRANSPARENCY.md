# Outlook and adaptive preparation

Status: **product direction agreed; visual design and implementation details remain open**  
Authority: decisions discussed on 2026-11-25. Update `PLAN.md` when these decisions are refined.

## 1. Purpose

Outlook is a readable, forward-looking weather timeline. It complements Forecast rather than duplicating it. Its job is to help people understand what is coming and prepare at useful moments, without turning the page into a dense data feed.

The visual direction is a vertical sequence of cards connected by a line. The line communicates continuity and indicates that more information follows below. Exact card styling, line treatment, typography, spacing and visual references are intentionally deferred until the user supplies and reviews the design references.

## 2. Information order

1. **Immediate hours not covered by Forecast.** Show the upcoming hours that Forecast does not already cover, using a compact presentation with less detail.
2. **The rest of the week.** Combine a concise summary for each day with additional cards for relevant weather developments.
3. **Relevant developments.** Include both significant phenomena and ordinary changes, prioritised by importance. Examples include storms, heavy precipitation, extreme heat, snow, temperature changes, clearing skies and wind changes.

Daily summaries and event cards must add distinct information. Do not repeat the same fact in multiple cards merely to fill the timeline. When there is nothing notable, retain the useful daily summaries and do not invent events.

## 3. Context and recommendations

Outlook may explain what a weather change means when the context helps the user make a decision. Practical recommendations are also part of the experience, but should appear at useful preparation moments rather than continuously.

- Simple recommendations may be integrated into the relevant weather card.
- Recommendations that deserve more attention may use a dedicated card.
- Important weather events can be highlighted whenever the forecast warrants it; they do not have to wait for a preparation window.
- When several recommendations apply, show the most relevant ones and combine compatible actions to avoid clutter.
- Wording leads with the action and follows with the reason and timing. Example: “Lleva paraguas: se espera lluvia entre las 16:00 y las 18:00”.

## 4. Adaptive Daily Preparation

The preparation system should adapt to the person's actual routine instead of assuming a conventional daytime work schedule.

### User preferences

Onboarding may optionally ask questions such as:

- “At what time does your day start?”
- “When do you usually leave home?”

The departure answer may be a single time or a time range. The preferred input should be selected through user research, not assumed in advance. These questions are optional; skipping them must not reduce core functionality.

Users may choose a preferred preparation time range. This preference is a starting point and remains under the user's control.

### Learning model

The system may learn separate routines for each day of the week, including different weekday and weekend patterns, rotating schedules and night shifts. Signals may include meaningful app opens and weather checks, the time of day, whether the user is checking the next day, and the optional schedule supplied by the user.

Do not treat every app launch as the start of the day. For example, a check at 3:00 AM should not automatically move a user-selected 6:00 AM preparation start earlier. The system must distinguish overnight use from the beginning of the user's active day.

The system may anticipate tomorrow's clothing or preparation recommendation before the usual departure time. An eight-hour lead is an example to evaluate, not a fixed product rule. Recommendations appear at their corresponding chronological position in the timeline, even when the card is already available to inspect earlier.

### Adaptation and user control

The chosen direction is full adaptive learning:

- Learn distinct routines for morning, evening/next-day preparation and individual weekdays.
- Adjust recommendation windows gradually based on observed behaviour and forecast relevance.
- Support night workers, rotating schedules, remote workers, irregular routines and days without a stable pattern.
- Do not invent a routine when there is insufficient evidence.
- Occasionally explain what has been learned and how it affects recommendations.
- Let users inspect and correct learned routines, retain their original preferences, disable adaptation and return to manual scheduling.

Learning should be local by default. Routine inference must not require telemetry or a network service.

## 5. Data transparency and voluntary contribution

The same privacy principles apply to Solstice and Meridian, while consent remains independent for each product.

- Contribution is **off by default**.
- During onboarding, the user may be invited to opt in; declining or skipping has no effect on core functionality.
- The user can review and change the choice later.
- Solstice consent does not imply Meridian consent, and a host application's consent must not silently activate Meridian contribution.
- Core product operation must not depend on contribution being enabled.

A shared transparency model may provide common concepts and presentation patterns, but each product and integration must retain a distinct consent boundary and disclose its own data flows.

### What the user can inspect

The transparency area should provide both:

1. A readable summary of data categories, purposes, recipients and destinations.
2. A technical history of transmissions, including date, destination and the content sent, with understandable examples and an expandable detailed view.

The user should be able to understand what leaves the device, where it goes, who receives it, why it is used and what happens afterward. Examples should reflect the actual payload schema and clearly distinguish sample data from a real transmission.

### Privacy requirements

- Prefer on-device processing and local learning.
- Minimise data before transmission and avoid collecting identifiers or precise location unless a separately justified, consented feature requires them.
- Provide a way to withdraw consent and document what deletion can and cannot be guaranteed for previously contributed data.
- Do not describe data as anonymous solely because direct identifiers were removed. Distinguish anonymous, pseudonymous and aggregated data, and substantiate any anonymity claim technically.
- Document retention, access, processing, recipients and server-side handling before enabling a collection service.
- Do not add analytics, tracking, advertising or unrelated collection without separate product approval.

For Meridian, these requirements must be expressible through a public integration contract. An embedding application must be able to present and enforce consent without Meridian assuming that the host has already obtained it. Meridian's normal weather-engine operation remains independent of telemetry.

## 6. Open specification work

The following remain open and must be researched or agreed before implementation:

- Visual design based on the user's references.
- Exact boundary between information shown in Forecast and Outlook.
- The user research needed to choose single-time versus time-range onboarding input.
- Learning signals, confidence thresholds, minimum sample sizes, adaptation speed and safeguards against accidental or atypical app opens.
- How to model overnight routines and shifts that cross midnight.
- The lead time for clothing recommendations and how it changes by forecast confidence and routine.
- How users inspect, edit, reset or pause learned routines.
- The exact telemetry schema, destinations, recipients, retention and deletion guarantees for each product.
- Technical design for a shared transparency experience without shared consent.
- Legal and privacy review before any contribution service is deployed.

No fixed eight-hour rule, learning threshold, collection destination or anonymity guarantee is implied by this document.
