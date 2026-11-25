# UX audit

Status: product-direction audit based on the decisions agreed through 2026-11-25 and the documentary research in [UX-RESEARCH.md](UX-RESEARCH.md).

This is an evaluation of the proposed experience, not a claim that the interface has been usability-tested. “Keep” means the direction is supported enough to retain as a product decision; it does not mean implementation is validated.

## Summary

| Area | Assessment | Reason |
|---|---|---|
| Actionable, low-clutter experience | Keep | Consistent with weather-app usability evidence and the product's stated purpose |
| Feels-like temperature as the visual lead, air temperature always visible | Keep, with data-method safeguards | Useful distinction, but indices and source methods vary |
| Outlook as a timeline with summaries and event cards | Keep; test comprehension | Supports context and avoids a flat feed, but the connected-card visual itself is untested |
| More detail near-term, broader trends farther out | Keep | Extended-forecast research cautions against presenting distant detail with near-term certainty |
| Adaptive routine learning | Keep as a direction; stage rollout | Strong product rationale, but learning signals and confidence thresholds are not validated |
| Quiet and Explainable learning modes | Keep | Different explanation preferences can be supported without hiding learning or controls |
| Adaptive non-urgent notifications | Keep; safety boundary required | Notification timing evidence is promising but not weather-specific |
| Intelligent widgets | Keep; platform-aware implementation | Useful outside the app, but Android constrains refresh frequency and background work |
| Home-only widget content | Keep | Consistent with the agreed Home and Forecast model |
| Stale-data fallback C | Keep | Retains useful information without misrepresenting old data as current |
| Opt-in contribution and independent consent | Keep | Supports user control and clear product boundaries |

## 1. Core information hierarchy

**Decision: keep.**

Solstice should answer practical questions before exposing secondary meteorological detail. The user should be able to understand what matters, when it happens and what to do without reading a dense dashboard.

Acceptance direction:
- Every screen and card has one clear job.
- Important events outrank routine detail.
- Additional technical information remains available on demand.
- Repeated facts are consolidated rather than duplicated across Forecast and Outlook.

## 2. Thermal comfort and actual air temperature

**Decision: keep the agreed hierarchy; revise the existing Forecast specification to match it.**

The feels-like value is the primary displayed temperature. Actual air temperature remains visible beside or immediately with it as the objective reference. This applies consistently to Forecast, Outlook, recommendations and widgets when a reliable feels-like value is available.

Required safeguards:
- Preserve the provider's index name/method and provenance internally.
- Do not assume “feels like” means Heat Index, wind chill, apparent temperature or UTCI.
- Explain differences only using known inputs and the actual method.
- If the provider does not supply a reliable value, do not fabricate one; show air temperature clearly.
- Do not imply that the estimate captures each person's clothing, activity, health or individual thermal perception.

The previous Forecast catalog entry that treated “Feels like” as an optional card is inconsistent with the user's later decision. The hero temperature hierarchy must take precedence; any optional detailed card should not be the only place where feels-like appears.

## 3. Outlook

**Decision: keep the content direction; defer visual specifics until references and tests.**

The agreed structure is:
1. Immediate hours not already covered by Forecast, in compact form.
2. Daily summaries for the remaining forecast period.
3. Distinct event cards for relevant weather developments.
4. Brief explanations and practical recommendations when they help preparation.

Rules:
- Do not repeat the same information in multiple cards.
- Show daily summaries even on quiet days; never invent an event to fill space.
- Prioritize high-impact phenomena over ordinary changes, while retaining useful everyday changes.
- Put the action first in recommendations, then timing and reason.
- Group compatible recommendations and avoid persistent interruptions.
- Reduce specificity and communicate uncertainty as forecast lead time grows.
- The connected vertical line is a proposed visual device, not a validated usability pattern.

## 4. Adaptive preparation and routine learning

**Decision: keep the adaptive direction; validate before choosing algorithms or thresholds.**

The system should support different routines by weekday, overnight use, night shifts, rotating schedules, remote work and irregular days. It must not infer a stable routine from a few atypical interactions.

Required safeguards:
- Onboarding schedule questions remain optional.
- A user-selected start time must not be overridden by an overnight app open.
- Learning remains local by default and must not require telemetry.
- Adapt gradually, disclose uncertainty and avoid frequent prompts.
- Preserve manual preferences and provide inspect, correct, pause, reset and disable controls.
- “Ignored” recommendations are not automatically treated as rejected.
- Quiet mode reduces unsolicited explanations; it does not conceal the existence of learning.
- Explainable mode may surface patterns occasionally, not after every adjustment.

Do not specify minimum sample sizes, confidence scores or adaptation rates until tested with realistic usage patterns.

## 5. Notifications

**Decision: keep adaptive timing for non-urgent recommendations, with an explicit safety boundary.**

Weather alerts are not interchangeable with engagement notifications.

- Official severe warnings and time-critical hazards must not be silently delayed or suppressed by an interruptibility model.
- Routine clothing, planning and low-priority Outlook recommendations may adapt to a preparation window and user preferences.
- Respect notification permission and category controls.
- Apply deduplication and cooldowns without suppressing meaningful warning updates.
- Measure missed useful alerts and annoyance, not only opens or response speed.

## 6. Widgets

**Decision: keep both widget families and automatic prioritization; design around Android limits.**

The agreed families are:
- An intelligent widget that prioritizes significant weather events, then timely recommendations, then useful weather conditions.
- Simpler condition widgets for current temperature, high/low, hourly forecast, precipitation probability and saved-place information where separately supported.

Shared rules:
- Widgets show Home only.
- The intelligent widget chooses content automatically; it does not rotate merely to appear active.
- If nothing warrants priority, show current conditions.
- Feels-like leads when available; actual air temperature remains visible.
- When Home changes, retain old content only as clearly stale while new Home data loads; never label the previous location's data as current.
- Refresh and rotation must respect Android scheduling and battery constraints.

## 7. Failed updates and stale data

**Decision: C — retain the last valid data and communicate freshness honestly.**

- Keep the last successful forecast when a refresh fails.
- Display its update time or age.
- Use a discreet warning when data exceeds a freshness threshold appropriate to the data type and source.
- Make it unmistakable when data is too old to represent current conditions.
- Do not invent one universal age threshold; define and test thresholds by data type, source cadence and use.
- When the Home place changes, never present the previous place's forecast as the new place's current conditions.

This applies to the app and widgets. Error-only blank states are not the default when a useful last-known forecast exists.

## 8. Privacy and transparency

**Decision: keep.**

- Data contribution is off by default and optional.
- Onboarding may offer a clear, separate opt-in; skipping or declining does not reduce core functionality.
- Solstice and Meridian require independent consent.
- Learning routines locally must not depend on telemetry.
- Explain categories, purposes, recipients, destinations and retention before collection is enabled.
- Provide readable explanations and technical payload/transmission examples.
- Avoid unsupported anonymity claims.
- Provide a practical way to withdraw consent and explain the limits of deleting data already contributed.

The exact telemetry schema, destination, recipient, retention and deletion guarantees remain open. Do not implement collection based on this UX audit alone.

## 9. Benchmark assessment

The preliminary benchmark is useful for identifying patterns, not for copying screens. Apple Weather, The Weather Channel, AccuWeather, Windy, CARROT Weather and Google Weather cover different product priorities. Solstice should borrow neither their visual identity nor their complexity wholesale.

Before visual implementation, capture dated screenshots of the relevant current app versions and compare specific tasks:
- Find when rain starts.
- Understand the difference between actual and feels-like temperature.
- Identify the most important event in the next few days.
- Find the source and freshness of a forecast.
- Change or disable an adaptive recommendation.

## 10. Decisions still requiring validation

The following are not resolved by literature alone:
- The precise Forecast/Outlook boundary and the connected-card visual.
- Whether optional onboarding schedule input should ask for a time or a range.
- How much explanation users want in Quiet and Explainable modes.
- Routine-learning confidence, cold-start behaviour and adaptation speed.
- The freshness thresholds for different data types.
- Widget rotation usefulness and perceived stability.
- Notification usefulness versus interruption cost.
- Whether people understand the source-specific feels-like value and its limitations.

These are assigned to [UX-VALIDATION-PLAN.md](UX-VALIDATION-PLAN.md).
