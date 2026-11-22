# Moving Motion into its own repo

Plan agreed with the user on 2026-11-21. The repo is
[INFORMATION89/Motion](https://github.com/INFORMATION89/Motion): public, empty, Apache-2.0 like
`:motion`. This file moves into that repo in phase 1 and is kept up to date there.

## Decisions (user, 2026-11-21)

- **Workflow: git submodule + Gradle composite build.** Solstice keeps Motion at `motion/` as a
  submodule pinned to an exact Motion commit and builds it from source (`includeBuild`). Nothing is
  published.
- **History: kept.** Motion starts with the commits that touched `motion/` and the lab (33 commits,
  all our own Apache-2.0 work, none of upstream Breezy's history).
- **Lab tabs:** Springs · Morphs · Pages · Controls · Effects (Material 3 bottom navigation bar).
  Slow motion and the frame counter sit in the top app bar, on every tab.
- **Lab app:** "Motion Lab", `io.github.information89.motion.lab`, its own app next to Solstice. The
  "Solstice Lab" launcher icon goes away from Solstice.
- **The lab is a plain Material You Expressive app** (the user): `MaterialExpressiveTheme`, dynamic
  colour, standard Material 3 components (Scaffold, top app bar, navigation bar, cards, chips,
  sliders, segmented buttons) used the standard way. No Solstice look (no glass, no sky), and no
  invented values: sizes and spacing come from Material 3's own defaults and specs. Every demo is
  laid out the same way.

## The Motion repo

```
Motion/
  motion/        the library (Gradle module :motion, package io.github.information89.motion)
  lab/           Motion Lab (Gradle module :lab, an Android app)
  docs/          this plan, MOTION.md (the motion rules), MORPH_STUDY.md, the lab plan
  gradle/        wrapper + its own version catalog
  .github/       CI
  LICENSE        Apache-2.0
  README.md      what Motion is, how to use it, how Solstice takes it in
```

- **Its own Gradle setup.** Today `:motion` uses Solstice's `breezy.library` convention plugin; the
  Motion repo gets plain `com.android.library` / `com.android.application` setups. Versions (AGP,
  Kotlin, Compose BOM, SDKs, minimum Android 10) are the same as Solstice's catalog, copied, so both
  build the same code the same way. Same Spotless/ktlint rules as Solstice.
- **CI:** `:motion` unit tests, ktlint, and a Motion Lab APK (debug, plus a test-signed build to
  install on the phone, like Solstice's `testing` pre-release).
- **Solstice-specific docs stay in Solstice.** Motion takes the motion docs (MOTION.md rules,
  MORPH_STUDY.md, MOTION_LAB_PLAN.md, the `:motion` README), and Solstice links to them.

## How changes flow (the workflow)

1. **Change Motion in the Motion repo** (my clone: `~/Developer/Motion`): code, lab demo, tests,
   commit, push. Motion's CI goes green.
2. **Solstice takes it:** in Solstice, `git -C motion fetch && git -C motion checkout <commit>` (or
   `git submodule update --remote motion`), build and check, then commit the new pointer
   ("Bump Motion to abc1234: what changed"). Solstice's CI checks out submodules
   (`actions/checkout` with `submodules: true`).
3. **Trying a Motion change in Solstice before pushing it:** work directly in Solstice's `motion/`
   submodule folder (it's a real Motion checkout), test in Solstice, then commit and push *from
   inside* `motion/` to Motion, and bump the pointer in Solstice.
4. **Rules:** Motion never imports Solstice code. A change Solstice needs from Motion lands in Motion
   first. Tags (`v0.1.0`, …) mark versions for the changelog, though Solstice pins commits.
5. **A new computer:** `git clone --recurse-submodules https://github.com/INFORMATION89/Solstice.git`
   (or `git submodule update --init` in an existing clone).

What changes about tuning: today the lab's Toolbar demo edits the app's `ToolbarMotion` live. As two
apps that link goes; values tuned in the lab are committed in Motion and reach Solstice with a bump.

## What moves from Solstice into `:motion`

Generic motion code that lives in Solstice today and has no Solstice imports (checked 2026-11-21):

| Solstice file | Becomes in `:motion` | Used by |
|---|---|---|
| `shell/ShellNavigation.kt` | page navigation: tab taps with a drawn slide, the settle, the toolbar's glide, the title hand-off | home, postcard window, lab |
| `shell/ToolbarMotion.kt` | the floating toolbar's motion (pill slide, bud, press, slide to switch), with `TOOLBAR_MOTION_PLAN.md` | `Toolbar.kt`, lab |
| `components/PressMorph.kt` | press morph shape for segmented rows | settings rows |
| `components/SolsticeSheetState.kt` | sheet motion (grow from a source, detents, drag away) | search |

Stays in Solstice: `Toolbar.kt`'s look (glass, sky, icons), `SolsticeMotion.kt` (Solstice's own
choreography constants, forwarding to `:motion`), all screens.

The lab's Toolbar demo needs a toolbar: once the toolbar's motion is in `:motion`, the lab shows it
with a plain Material 3 look, and Solstice keeps its glass look on the same motion.

## Phases

Each phase: plan the details, agree, build, check on the Pixel, then the next one.

1. **Bootstrap Motion with the library only.**
   - Install `git-filter-repo` (Homebrew; not installed yet). In a throwaway clone of Solstice, keep
     only `motion/`, the lab files and the motion docs, and lay them out as above. Check the result
     before anything is pushed: no Solstice-only files, no upstream history.
   - Add the standalone Gradle setup, CI and README; push to Motion. CI green.
   - In Solstice: remove `motion/`, add the submodule at `motion/` on that commit, wire
     `includeBuild`, CI with submodules. Solstice must build and behave exactly as before
     (same APK contents; tests pass).
2. **Motion Lab as its own app** (in Motion's `:lab`).
   - New Material You Expressive app shell: top app bar (slow motion, frame counter), the five tabs,
     one consistent card per demo.
   - Move the demos and swap Solstice's theme tokens (`Space`, `CornerShapes`, `Sizes`) for Material
     3's defaults. The Toolbar demo waits for phase 3.
   - Remove the lab from Solstice (activity, manifests, "Solstice Lab" icon, CI notes).
3. **Move the generic motion code** from the table above into `:motion`, one piece at a time, each
   with its lab demo and tests; Solstice switches to it with a bump. Page navigation and the toolbar
   go after the other session's settle work (swipe settle B in the app) has landed, since it's
   changing `ShellNavigation` right now.
4. **Workflow docs:** `CONTRIBUTING.md` in Motion and the "Motion" section in Solstice's
   `CONTINUE.md` (clone with submodules, bump, test a change in place).

## Open

- What each tab's screen holds exactly, and the demo card layout: planned in phase 2 with screenshots.
- The toolbar's motion API in `:motion`: comes with `TOOLBAR_MOTION_PLAN.md` (still to approve).
