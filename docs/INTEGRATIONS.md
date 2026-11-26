# External dependencies: Solstice consumer boundaries

This document describes only what the Solstice app consumes and what the app must keep owning.
It is not a roadmap, architecture plan, API proposal, or acceptance plan for an external project.
Each dependency's implementation, release process, and technical decisions belong in its own repo.

## Motion-UI — UI and motion dependency

Repository: [INFORMATION89/Motion-UI](https://github.com/INFORMATION89/Motion-UI)

Solstice consumes Motion Core from its `motion/` Git submodule through the Gradle composite build.
The submodule must remain pinned to an exact reviewed commit. A dependency update is a separate
Solstice change: inspect the upstream diff, confirm the public API and licence, initialize a clean
recursive checkout, then run the app's relevant tests and build on the exact new pointer.

**Solstice owns:** its toolbar and bud, page and navigation policy, app screens, cards and sheet
content, weather-specific composition, labels, icons, visual identity, theme values, accessibility
choices, and all product behavior. The existing animated toolbar is part of Solstice's identity and
must not be removed or redesigned as incidental dependency work.

**Motion supplies:** generic, documented motion and interaction primitives, and only components
that are actually present in the pinned version. Solstice's consumer-side acceptance requires those
primitives to remain interruptible and not swallow unrelated taps or gestures. If a generic capability
or defect must change upstream, it is handled by that library's owner; Solstice records only its own
workaround or later adoption of a stable, reviewed API. Do not assume an unmerged API is available.

Do not assume a component, module, or API exists merely because it appears in an external proposal.
Use only what is actually present in Solstice's exact pinned revision, and verify availability again
when a dependency update is proposed.

## Meridian-Engine — meteorological engine dependency

Repository: [INFORMATION89/Meridian-Engine](https://github.com/INFORMATION89/Meridian-Engine)

Solstice does **not** currently depend on a production Meridian artifact or public weather API.
The app's current weather behavior remains in its existing Solstice/Breezy-derived provider path.
The MER-0.1 platform and packaging probe was merged into Meridian-Engine's `main` through PR #15
on 2026-11-26, but it remains a compatibility/test harness, not a production weather engine or
consumer-ready forecast API. Its successful platform and licence checks do not authorize Solstice
integration. Do not integrate the probe, an experimental package, or an unpublished API as a
production weather engine.

**Meridian owns:** reusable meteorological data and reasoning, including normalized observations and
forecasts, derived weather signals, event detection, source evidence, provenance, quality, freshness,
probabilities, confidence, uncertainty, and explanations where its approved contract supports them.

**Solstice owns:** the consumer experience and policy built on top of weather data. This includes
presentation, saved places, permissions, app refresh scheduling and lifecycle, app settings,
notifications and Live Updates, data-sharing consent, and practical/personalized recommendations:
what to wear, clothing and footwear, whether to carry an umbrella, sunscreen or shade, activity and
outdoor-planning advice, personal preferences, alert thresholds, cooldowns, wording, and delivery.

Keep the weather evidence distinguishable from the advice Solstice derives from it. Never make
Meridian depend on Solstice, its UI, product language, preferences, repository, or lifecycle. Never
place Solstice-specific recommendation models or policies in Meridian's core.

A future Solstice integration can be specified only against an approved, stable public API and
distribution artifact, after product-owner approval and a Solstice-side review of Android support,
licensing/notices, privacy, cancellation/errors, refresh lifecycle, offline behavior, and regression
parity. Until then, keep the current app path working. Do not choose Meridian's language, storage,
networking, provider list, algorithms, or release packaging here; those decisions belong to
Meridian-Engine.

## General rule

External dependencies are inputs to Solstice, not workstreams that Solstice manages. Solstice plans
contain app requirements and app-side adoption work only. A dependency's internal roadmap, milestones,
lab tasks, API implementation, or platform expansion must never be copied into PLAN.md, ROADMAP.md,
or CONTINUE.md.
