# Onboarding: proposal

Status: **proposal for Marco** (2026-11-23). Nothing is built until it's agreed (RULES.md section 1).

## What it has to respect

- **Ours:** DESIGN.md rule 4, "show, don't tell": no explainer slides; a first use shows the thing
  working. Rule 3: the app picks sensible defaults; a setting exists only for a real choice.
- **Decided:** home is your current location by default; without location access you choose a place,
  which becomes home (PLAN.md, Places). Sharing data is opt-in, asked during onboarding, with the real
  data and its path shown (RULES.md section 4).
- **Apple:** prefer context-specific tips to a separate flow; if a flow is needed, make it brief and
  enjoyable; ideally ask for a permission when a feature needs it, and a custom screen before the
  system prompt is fine when it explains why.
- **Android:** ask for location in context; ask for approximate and precise together, and keep working
  when only approximate is granted (Android 12 and later let people choose).

## The flow (two steps, no slides)

**Step 1: Where are you?** The Forecast page itself, already drawn with empty cards that shimmer, and
one sheet at the bottom:
- "Use my location" (primary). Opens Android's prompt, approximate and precise together. Approximate
  is enough for weather; we never ask again for precise.
- "Choose a place" (secondary). Opens search; the place picked becomes home.
- Granting or picking fills the page behind the sheet as the forecast loads: the first thing you see
  is the app working, not a description of it.

**Step 2: Help improve forecasts** (only once sharing exists, see below). A sheet, after the forecast
has loaded, so it never stands between you and the weather:
- One line: "Share how far off our forecasts are where you are. Off unless you turn it on."
- **The example card:** what this phone would send right now, in plain words: "Area about 4 km wide ·
  3 PM · forecast 24° · measured 22°". Built from the real values, not a mock-up.
- **The path** (a small diagram that animates once, Reduce Motion: shown still):
  your phone → removed before sending (exact place, exact time, any ID) → our server (counted only
  once enough phones share in your area) → combined into corrections → back to everyone's forecast.
- Two equal buttons: "Share" and "Not now". "Not now" changes nothing else, and the same screen lives
  in Settings › Privacy.

**Asked later, in context, never in onboarding:**
- Notifications: when you first turn on a rain or alert notification.
- Keys and best-effort sources: when you're in their area (SOURCES.md, "Off until relevant").
- Evening time, units, sources: sensible defaults from the system and the place; changeable in
  Settings.

## Until the sharing server exists

Step 2 doesn't appear at all, since asking for something that does nothing would be dishonest. When
sharing launches: new installs see step 2; existing users get it once as a message on Forecast.

## Every state

- Location denied: step 1 stays on "Choose a place"; a Settings link explains how to allow location
  later.
- Offline: the place is kept, the page shows the offline state, and it fills in when back online.
- No place found in search: the empty state suggests a nearby city or typing coordinates.
- TalkBack: each sheet's title is read first; the diagram has a text description; both buttons are
  48 dp and reachable.

## Questions for Marco

1. Does step 1 as a sheet over the forecast fit your idea, or do you want a separate first screen?
2. Should the opt-in also show a running count of what you've shared so far (in Settings)?
3. The wording above is a first draft; every sentence goes through WRITING.md and you before it ships.
