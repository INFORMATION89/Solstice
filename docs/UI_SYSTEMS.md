# Solstice UI systems

This document describes the application-owned UI systems and the questions that keep their behavior
consistent. It is not a plan for an external component framework.

## App-owned systems

- **Design tokens:** colour, typography, spacing, dimensions, shapes, elevation/materials, and
  interaction values live in Solstice's theme and token definitions. UI call sites should use named
  values rather than typed-in numbers.
- **Shell and navigation:** Forecast, This week/Outlook, Places, and place windows follow one coherent
  app navigation model. The page toolbar and its animated bud remain part of Solstice.
- **Cards and surfaces:** cards, partial sheets, dialogs, empty states, and messages share app-owned
  layout and accessibility rules.
- **Weather presentation:** source labels, freshness, units, precipitation summaries, feels-like
  temperature, alerts, and app-specific recommendations are formatted consistently and remain
  distinguishable from the underlying weather evidence.
- **Interaction:** gesture ownership, interruption, predictive back, sheet detents, list reordering,
  and touch feedback follow [MOTION.md](MOTION.md).
- **Icons:** one app catalogue for interface symbols; weather-condition mapping is maintained in
  [research/ICONS.md](research/ICONS.md).
- **Accessibility:** text scaling, contrast over every sky/background, touch-target size, RTL,
  TalkBack actions, and reduced-motion behavior are checked for all screens.

## Review method

Before adding a new component or replacing an existing one:

1. Check whether Solstice already has a suitable app component or token.
2. Check current source and approved product decisions; historical research below is not proof that a
   system is still missing.
3. Prefer a maintained public API for generic low-level mechanics, but keep Solstice layout, styling,
   copy, navigation, accessibility decisions, and weather-specific behavior owned by the app.
4. Compare behavior and performance on a physical Android device where available.
5. Add app-side regression tests and update [ROADMAP.md](ROADMAP.md) only when the work is still
   needed and approved.

External dependency implementation, internal component roadmaps, labs, and API proposals belong in
their own repositories. Solstice records only its consumer requirements in [INTEGRATIONS.md](INTEGRATIONS.md).
