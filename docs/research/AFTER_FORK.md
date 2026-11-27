# Translation and widget research for Solstice

**Status:** historical app research, 2026-11-23. This note covers localization and Android widgets
owned by the Solstice application. It does not plan work for external frameworks or weather engines.
All user-visible changes remain subject to [RULES.md](../RULES.md).

## Translations

The upstream Breezy translations are useful context but must not be treated as Solstice-authored copy.
New Solstice strings are written and reviewed as part of the app's own UI and product terminology.

Proposed approach:

1. Write new app strings in English first; keep wording in [WRITING.md](../WRITING.md).
2. Mexican Spanish is the first human-reviewed translation.
3. Add more languages only when the app's terminology and string set are ready for translation.
4. If a translation platform is adopted, imported changes arrive through pull requests and are checked
   for placeholders, plural forms, and Android resource consistency.
5. Maintain a short glossary for weather terms, alert levels, and location language.

**Recorded decision (2026-11-23):** Mexican Spanish first. The decision to open translation to
community contributions remains a separate product/maintenance choice.

## Android widgets

Solstice owns its widget behavior, update policy, data presentation, permissions, and app lifecycle.
Android widgets are not the same rendering environment as the in-app Compose UI; each widget needs its
own responsive layout and accessible interaction targets.

Jetpack Glance was evaluated as one possible Android implementation approach. Re-check its stable
version, supported APIs, and platform behavior before adoption; this historical note does not approve
a dependency or version.

Quality requirements for any Solstice widget:

- Resize cleanly across supported launcher sizes and remain readable.
- Show the freshness of weather information and recover when data cannot be refreshed.
- Reflect relevant user actions in the app and refresh without creating an excessive background-work
  burden.
- Use accessible tap targets and provide a clear route to the relevant app screen.
- Use Solstice's own colour, type, and shape decisions so widgets feel consistent with the app.

Proposed first formats, subject to product approval:

1. A compact widget with temperature, condition, and the next notable precipitation change.
2. A wider widget with the compact summary plus the next several hours.
3. An alert-focused variant only when an active, relevant warning exists.

Refresh scheduling remains an app-owned policy and must respect Android background-work constraints,
provider freshness, and battery use.
