# Personal Systems Coordinator: future Solstice integration

**Status:** planning note · research pending · no implementation approved  
**Date:** 2026-11-26

Shared foundation: [INFORMATION89/Personal-Systems-Coordinator](https://github.com/INFORMATION89/Personal-Systems-Coordinator).

## Why this matters to Solstice

Solstice already has plans for proactive weather recommendations and locally derived personalization. The user wants eligible settings and personalization data to remain consistent across their own devices without requiring a Solstice-operated cloud.

Personal Systems Coordinator (PSC) is the proposed shared foundation for provider-neutral, user-controlled cross-device synchronization and future interoperability between the application family.

This is a future integration point, not a new runtime dependency today.

## Relevant Solstice work

- **Outlook:** recommendation content such as "Best time to be outside" and notable weather ahead (see PLAN.md and PROPOSALS.md).
- **What to wear:** recommendations shaped by weather conditions and the user's comfort.
- **User-adjustable comfort preference:** the Outlook proposal already anticipates that a person may nudge their comfort preference warmer or cooler.
- **Meridian learning:** forecast-error history and local corrections are currently designed to run on the phone (ENGINE_SPEC.md section 8). This is a separate data category from recommendation preferences.

## Candidate data categories

These should be evaluated when the recommendation model is designed. They are not an approved sync schema.

1. **Explicit recommendation preferences** — user-adjusted comfort settings, activity or planning preferences if later introduced, and recommendation-specific choices.
2. **Recommendation configuration** — stable thresholds or settings that the user expects to follow them between devices, with units and semantics made explicit.
3. **Derived personalization state** — a compact, versioned profile only if it demonstrably improves consistency and can be explained and inspected.
4. **Meridian learning state** — per-place forecast errors and local corrections require a separate review. Determine whether devices should sync, recompute or keep this data local. Do not assume it belongs in the first Solstice integration.

Do not synchronize all of Solstice's local database as one opaque package.

## Keep these concerns separate

- Syncing a person's own preferences between their devices is not the same as contributing anonymized forecast errors to improve Meridian for everyone.
- Neither purpose enables telemetry or analytics.
- Raw forecast logs, station observations, exact coordinates, location history, device identifiers and provider credentials must not be silently included.
- Algorithm and application code updates are distributed through normal app releases; they are not sync payloads.
- Calendar service connections are separate from PSC storage providers.

## Ownership boundaries

- **Solstice** defines weather recommendation semantics, user preferences, local personalization behavior and which data categories may be exported.
- **Meridian** owns weather-engine and forecast-learning semantics.
- **PSC** supplies generic provider adapters, sync coordination, portable metadata/contracts and synchronization status.
- **Storage providers** retain their own authorization, quotas and service behavior.

Solstice should depend on a stable PSC boundary, not directly on Google Drive, iCloud Drive, GitHub or WebDAV APIs.

## Required checkpoint when recommendations are implemented

Before building recommendation persistence:

1. Inventory each preference and personalization field.
2. Classify it as device-local, app-local, portable user preference, derived personalization or raw learning history.
3. Decide which categories can be exported and synchronized, and which must stay local.
4. Define stable identifiers, units, schema versions and migrations for portable records.
5. Design explicit provider selection, consent, inspection, pause, disconnect and recovery.
6. Keep the recommendation engine functional when no provider is configured or synchronization is unavailable.
7. Add an adapter boundary for PSC only after its own research and contracts have been reviewed.

Do not delay the first recommendation design waiting for PSC to be implemented. Design the data boundary so future integration is possible, then connect it when PSC is ready.

## References

- [PSC product overview](https://github.com/INFORMATION89/Personal-Systems-Coordinator/blob/main/docs/PRODUCT_OVERVIEW.md)
- [PSC principles](https://github.com/INFORMATION89/Personal-Systems-Coordinator/blob/main/docs/PRINCIPLES.md)
- [PSC research plan](https://github.com/INFORMATION89/Personal-Systems-Coordinator/blob/main/docs/RESEARCH_PLAN.md)
- [PSC Solstice integration brief](https://github.com/INFORMATION89/Personal-Systems-Coordinator/blob/main/docs/INTEGRATION_SOLSTICE.md)
- Solstice: PLAN.md, PROPOSALS.md, RULES.md and Meridian ENGINE_SPEC.md.
