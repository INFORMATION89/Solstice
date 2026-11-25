# UX validation plan

Status: proposed research plan. No Solstice user study has been conducted as part of this audit.

## Goals

Validate that Solstice helps people make everyday weather decisions quickly and accurately, without overwhelming them, misrepresenting forecast certainty or creating unwanted interruptions.

The first round should test comprehension and task success before investing in long-term personalization. Routine learning and notification timing need a later longitudinal study.

## Research principles

- Recruit people who use weather apps for ordinary daily planning, not only weather enthusiasts.
- Include varied schedules: conventional daytime routines, night work, rotating shifts, remote work and irregular days.
- Include people who rely on weather for commuting, outdoor activity or caring responsibilities without collecting unnecessary sensitive details.
- Test Mexican Spanish wording and local warning conventions.
- Use realistic, internally consistent forecast scenarios. Label simulated data in prototypes.
- Do not collect precise location or routine data for research unless necessary, disclosed and separately consented.
- Record task outcomes and user explanations; do not treat preference votes as proof of comprehension.

## Study 1 — Forecast and Outlook comprehension

**Method:** moderated task-based usability sessions using clickable prototypes or a stable test build.

**Suggested sample:** 8–12 participants for an initial formative round. This is a practical discovery sample, not a statistically representative sample.

**Tasks:**
1. Find the current feels-like temperature and actual air temperature.
2. Explain why they differ using only the information Solstice provides.
3. Find when rain is expected to start and stop.
4. Identify the most important weather development over the next few days.
5. Explain how certain the forecast is for a near-term day versus a distant day.
6. Find a quiet-day summary when no significant event is forecast.
7. Find the source and last update time.

**Measures:**
- Task completion without help.
- Time to correct answer.
- Incorrect interpretation of timing, probability or certainty.
- Whether the user notices actual air temperature alongside feels-like.
- Whether users infer unsupported causes for the feels-like difference.
- Perceived clutter and confidence in the answer.

**Acceptance criteria for the formative round:**
- At least 80% complete each core task without moderator help.
- No recurring critical misunderstanding of which number is actual air temperature.
- No recurring interpretation that a long-range forecast is as certain as a near-term forecast.
- Any critical warning or timing misunderstanding blocks progression until corrected.

These are proposed project gates, not claims about industry benchmarks.

## Study 2 — Outlook structure and visual continuity

**Method:** compare at least two prototype treatments while keeping the forecast content constant:
- Connected vertical timeline.
- A conventional separated-card/list treatment.

**Tasks:** scan the next hours, locate a significant event, compare two days and find a recommendation.

**Measures:** time to locate information, correct recall of event timing, perceived continuity, scroll discoverability, visual hierarchy and preference with reasons.

**Decision rule:** keep the connected line only if it improves orientation or continuity without making unrelated cards appear causally connected or increasing scan time. Preference alone is insufficient.

## Study 3 — Adaptive preparation onboarding

**Method:** short prototype interviews and comprehension tasks.

Compare:
- A single departure time.
- A departure-time range.

Test optional start-of-day input separately. Include examples for overnight and rotating schedules.

**Tasks:**
- Set a preparation window.
- Skip onboarding and understand that the feature still works.
- Explain what the system may learn from app usage.
- Find how to correct, pause or disable learning.
- Distinguish a late-night check from the start of a new day.

**Measures:** completion, comprehension, perceived pressure to disclose a schedule, confidence in controls and ability to predict what the system will do.

**Gate:** do not finalize the onboarding question or learning rules until users understand the optional nature and can find controls without help.

## Study 4 — Quiet and Explainable modes

**Method:** scenario-based prototype evaluation followed by a short diary study if the concepts are understood.

Compare the two agreed modes:
- Quiet: learns and adapts without explaining every change; user can inspect what was learned.
- Explainable: learns automatically and occasionally explains patterns and changes.

**Measures:**
- Understanding that both modes learn.
- Ability to inspect and correct learned patterns.
- Perceived interruption and trust.
- Whether explanations are understandable and useful.
- Whether users mistake ignored recommendations for explicit negative feedback.

**Gate:** both modes must preserve the same underlying controls and privacy protections. The difference is explanation frequency and presentation, not access to information.

## Study 5 — Widgets and stale-data states

**Method:** prototype test on Android launchers and device sizes, followed by a technical reliability test on supported Android versions.

**Scenarios:**
- Important weather event available.
- Useful preparation recommendation available.
- No event or recommendation; current conditions are the fallback.
- Refresh fails but last-known data remains available.
- Last-known data exceeds the freshness threshold.
- Home changes while the new place is loading.
- Feels-like data is unavailable while actual air temperature is available.

**Measures:**
- Correct identification of the widget's place.
- Correct interpretation of freshness and stale-data warning.
- Whether old-place data is mistaken for new-place conditions.
- Whether the intelligent widget's content change is understood.
- Refresh reliability, battery impact and compliance with Android scheduling.

**Gate:** never show old-place data as current for the new Home. Never hide the fact that a forecast is stale when it could materially change a decision.

## Study 6 — Notification usefulness and interruption

**Method:** only after a stable notification prototype exists, run a small opt-in field pilot. Include non-urgent recommendations and separate safety-critical warning scenarios in review; do not experimentally delay official severe warnings.

**Measures:**
- User-rated usefulness.
- Dismissal and disable rates by category.
- Duplicate notifications.
- Notifications arriving after the relevant preparation window.
- Missed or late non-urgent recommendations.
- Perceived interruption and trust.

**Gate:** urgent warnings are never delayed by personalization. Users can control categories and disable non-essential notifications. Opens alone are not a success metric.

## Study 7 — Privacy and learning transparency

**Method:** comprehension tasks with a prototype of the privacy and learning controls.

**Tasks:**
- Identify what stays on device.
- Identify what data would be contributed if opting in.
- Identify recipient, destination and purpose.
- Find a sample payload and distinguish it from a real transmission.
- Withdraw consent.
- Inspect, correct, pause or reset learned routines.

**Measures:** correct understanding, ability to locate controls, mistaken assumptions about anonymity and perceived pressure to opt in.

**Gate:** do not enable contribution until the actual schema, recipients, destinations, retention and deletion behaviour are documented and reviewed.

## Cross-study issue log

Record each finding with:
- Scenario and prototype/build version.
- Participant context relevant to the task, without unnecessary personal data.
- Observed behaviour and participant explanation.
- Severity: blocker, major, minor or observation.
- Evidence: task result, quote or repeated pattern.
- Proposed change and whether it needs product approval.

## Release-readiness checks

Before a UX direction is considered validated:
- Core tasks have been tested in Mexican Spanish.
- Accessibility is checked with TalkBack, larger text and contrast.
- Loading, empty, offline, failure and stale-data states are tested.
- The actual source and index behind feels-like are verified.
- Android widget behaviour is measured on real devices and not inferred from emulator timing alone.
- Severe-warning behaviour is reviewed separately from ordinary recommendations.
- Privacy claims match the actual implementation and network payloads.

## What this plan does not claim

This plan does not assert that Solstice has passed usability testing, that the proposed sample sizes are statistically representative, or that the adaptive system has demonstrated improved outcomes. It defines the work required to establish those claims.
