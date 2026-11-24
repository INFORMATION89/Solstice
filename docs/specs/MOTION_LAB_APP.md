# Motion Lab as Motion's own app (Saturday step 7)

Status: **plan** (2026-11-24). Builds on MOTION_LAB_PLAN.md (the redesign, **approved by Marco
2026-11-24**, with TOOLBAR_MOTION_PLAN.md); this file covers making it an app on the kit.

## Today

- Two copies of the Lab: Solstice's `ui/expressive/lab/` (launcher icon "Motion Lab" in test builds,
  and Settings › Debug) and Motion's `lab/` folder, which **isn't in Motion's `settings.gradle.kts`**
  (only `:motion` is included), so it isn't built.
- Demos: Spring morph, Haptics, Edge blur, Dial, Toolbar, Settle, plus `MorphDemo` (old `Morph`).

## Steps

1. Include `:lab` in Motion's `settings.gradle.kts` as an Android app module
   (`io.github.information89.motion.lab`), depending on `:motion` and `:motion-ui`. CI builds it.
2. Make Motion's `lab/` the only copy: bring over anything newer from Solstice's copy (compare file by
   file), then delete `ui/expressive/lab/`, its `MotionLabActivity` entries in `src/main` and
   `src/debug` `AndroidManifest.xml`, and the Debug link from Solstice. (Both copies have the same
   files today.)
3. Each demo uses motion-ui pieces (page frame, cards, sheet) instead of its own, so the Lab is the kit's
   first user.
4. A **subtle vs no bounce** switch on every demo (Marco, 2026-11-22), and a **Reduce Motion preview**
   switch (forces `LocalReducedMotion` on).
5. A **card material** page: the candidates in research/CARD_MATERIAL.md on three backgrounds, for
   Marco to pick on the phone.
6. A **far jumps** page: tapping a tab two pages away, slide (skipping the middle) vs fade through,
   for Marco to pick (B4).
7. Solstice's Toolbar demo stays until MOTION_LAB_PLAN.md step 1 makes it a generic tab bar.

## Install

Its own APK from Motion's CI (debug), installed next to Solstice. The testing pre-release flow
(README) gets a second file once the signing secrets exist.

## Done when

The Lab builds from the Motion repo, Solstice has no Lab code, and every demo runs on motion-ui
pieces with the two switches.
