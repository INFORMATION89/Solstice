# Rules

How every session works on Solstice and Motion. These rules come before speed and before any task.
They change only with Marco's OK. If a task seems to need breaking one, stop and ask.

## 1. Ask first, always

1. **Plans are made in chat with Marco.** Coding sessions only carry out what's agreed in
   [PLAN.md](PLAN.md), [ROADMAP.md](ROADMAP.md) and [CONTINUE.md](CONTINUE.md).
2. **Never start building anything new without asking**: no new feature, screen, card, button, text,
   setting, motion, haptic, token or system. Say exactly what would change, show options when there
   are choices, and wait for a yes.
3. **Check guidelines with Marco before relying on them.** When Apple's, Google's or our own rules
   disagree, or a rule doesn't cover the case, bring it to Marco; don't pick on your own.
4. When something agreed turns out not to work as planned, stop and report; don't improvise a
   different solution.

## 2. Motion and frameworks first, then the app

1. Anything reusable is built in **Motion** first, tried in the Motion Lab, then the app imports it and
   **configures** it. The app holds configuration and weather-specific content only.
2. Motion never imports app code. A change Motion needs lands in Motion, then Solstice bumps the
   submodule (CONTINUE.md, "Motion").
3. **Use what exists before writing anything**, in this order: Motion's pieces and tokens → Material 3
   components → a maintained library (check its licence) → new code, only after asking.
4. **One of each system.** One page frame, card, sheet, toolbar, message, button style, text role set,
   glass. If a system lacks something, extend the system; never build a one-off beside it.
5. New code in the app goes in `ui/expressive/`; edits to upstream files stay small and are listed in
   PLAN.md.

## 3. Never hardcode anything

1. **No typed-in values** in components or app code: no `dp`, `sp`, colours, alphas, corner radii,
   durations, `delay()`s, springs, thresholds, fractions or counts. Every value is either a **token** or
   **derived** (from layout, data, insets or another token).
2. **No typed-in text:** every string is a resource; English first, other languages marked as needing
   an update, never machine-translated.
3. A value with no token yet: **stop, propose a name and value, ask**, then add it to the tokens. Don't
   add it "for now".
4. Allowed without a token: 0, 1, mathematical constants, and the entries in the reviewed allowlist.
   CI enforces this; the allowlist only grows with Marco's OK.
5. No special cases by screen ("if in a window…"). Differences are configuration (data), passed in.

## 4. Platform guidelines

Android first in structure, iOS quality in feel. Check both before proposing anything.

**Material 3 (Android):** system back and predictive back everywhere; edge to edge; 48 dp touch
targets; Material Symbols; the type scale; dynamic colour roles; Material components where they exist;
the Material `MotionScheme` fed by our tokens.

**Apple HIG (feel and judgement):** fluid interfaces (respond at once, interrupt and redirect, hand over
velocity, project flings, rubber-band); a sheet keeps its source and closes with a swipe; glass is for
controls and navigation; motion is purposeful and brief on frequent actions; haptics consistent,
short and never the only feedback.

**Accessibility, not negotiable:** 48 dp targets with room between them; text up to 200 %; contrast
4.5:1 for body text, 3:1 for large text, on every sky; never colour alone; every gesture also has a
button or a TalkBack action; nothing that disappears on a timer alone; RTL mirrored.

**Decided exceptions** (Marco): the ✕ sits at the end of headers; the toolbar keeps its current
animation; glass on cards is under review. Exceptions are listed here and in PLAN.md, nowhere else.

## 5. Motion

Full rules in [MOTION.md](MOTION.md). In short:
- Springs only, from tokens. Movement and size use spatial springs; opacity, colour and blur use
  effects springs, which never bounce.
- Things morph, never pop; one material per morph; the button becomes the card; closing goes back
  into the source.
- Nothing blocks input; gestures drive a motion value and hand their velocity to the release.
- **Reduce Motion** ("Remove animations"): every Motion piece has a reduced form, agreed with Marco
  (Apple's list: fades instead of moves, no blur hand-offs, no depth, less bounce, follow the finger).
- Haptics from tokens only; they follow the system setting; none added without agreeing them.
- Every new or changed motion is proposed with options and agreed before it's built.

## 6. Design

- [DESIGN.md](DESIGN.md)'s checks apply to every proposal: one screen one job, someone needs it,
  essentials first, every state designed (loading, empty, error, offline), any size and orientation.
- **Skeuomorphism lives in interaction, not in looks** (physics, press, drag, haptics); visuals stay
  clean and standard.
- [WRITING.md](WRITING.md) for every sentence; every new sentence goes to Marco before it ships.

## 7. Done means checked

A change is done when:
1. CI passes (lint, the no-hardcoding check, Motion's and the app's unit tests);
2. logic has unit tests; each Motion piece has a Motion Lab demo;
3. it was checked on the Pixel (profile build for smoothness; frame stats when motion changed), or is
   clearly marked "not checked on the phone";
4. the docs are updated: PLAN.md (decisions), LOG.md (a dated line), ROADMAP.md (ticked), Motion's
   README for any Motion API change.

## 8. Docs

One source of truth per topic: PLAN.md decisions, LOG.md history, ROADMAP.md to-dos, CONTINUE.md
where things stand, DESIGN/MOTION/WRITING the rules, this file how we work. Fix contradictions
instead of adding to them. Never push upstream's history; never build with `-Pbreezy`.
