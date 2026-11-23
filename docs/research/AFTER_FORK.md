# Translations and widgets after leaving the fork

Research 2026-11-23 for PLAN.md research queue 16 (leaving the Breezy fork). Proposals; nothing is
built without Marco's OK.

## Translations

**Today:** 1,162 strings in Breezy's `strings.xml` and 105 in ours (`strings_expressive.xml`), with 65
languages translated by Breezy's community. Those translations are LGPL, like the code: fine while we're
a fork, but they leave with Breezy's code.

**Hosting: Hosted Weblate's Libre plan.** Free for public libre projects (public source and an OSI or FSF
approved licence), with the same limits as the 160k plan: 160,000 strings counted across every language.
Solstice (LGPL-3.0), Motion and Meridian (Apache 2.0) all qualify. 160k is roughly 1,500 strings in
about 100 languages, or 3,000 strings in 50, plenty for a new string set. Weblate is itself open source and
can be self-hosted later if needed.

**Plan**
1. **New strings, written fresh** as screens move to Motion UI, following WRITING.md, not copied from
   Breezy's.
2. **English is the source, Spanish (Mexico) first**, written by Marco; then open the rest to the
   community through Weblate.
3. Weblate pushes translations as pull requests; CI checks them (placeholders match, no missing plurals).
4. Android per-app language (Android 13 and later lets people pick the app's language separately).
5. A short glossary per language (weather terms: "chubasco" versus "aguacero", alert levels) so
   translators stay consistent.

**Decided (Marco, 2026-11-23):** Mexican Spanish only at first. **Still open:** when to open Weblate
(with the first Motion UI screens, or once most strings are new).

## Widgets

**Today:** Breezy's widgets and notification views are about 8,500 lines (RemoteViews). They're replaced
when the fork is left.

**Tool: Jetpack Glance.** 1.2.0 is stable (August 2026), 1.3 is in alpha. It builds widgets with a
Compose-style API, but it renders to RemoteViews, so it has its own components, not Compose UI's. Glance's
responsive mode lets a widget define a few layouts bounded by sizes, chosen by the space the launcher
gives it.

**Google's widget quality tiers** (the bar to meet):
- Fill the launcher grid's bounds and resize to at least 2×2 or 4×2.
- Never be consistently stale; update after any action from the widget or a related action in the app.
- Use the canonical layouts (text, toolbar, list, grid) and tap targets of at least 48 dp.

**What it means for Motion:** Motion UI's components won't render in a widget, and widgets can't animate
with our springs. What carries over is the look: Motion's colour, type and shape tokens exported to a
Glance theme (a small `motion-glance` piece later), so widgets match the app.

**Proposed first widgets** (to agree with Marco):
1. **2×2:** temperature, condition, and the next rain change ("Rain at 4 PM").
2. **4×2:** the same plus the next hours.
3. Later: an alert widget only when one is active (or part of the 4×2).

**Updates:** pushed from each Meridian refresh (WorkManager), not Android's periodic widget timer, so
widgets are never staler than the app.

**Decided (Marco, 2026-11-23):** the 2×2 comes first, then the 4×2. Colours are a Settings option:
Solstice's sky palette by default, the phone's dynamic colour as a choice.
