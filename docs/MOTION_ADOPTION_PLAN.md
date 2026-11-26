# Motion adoption plan

**Status (rechecked 2026-11-26):** planning only. No Solstice runtime code or Motion submodule pointer is changed by this document.

This plan turns the Motion direction recorded in [PLAN.md](PLAN.md) into a practical, evidence-based path for Solstice. The goal is to reuse the Motion framework and, later, its optional UI kit without moving Solstice's weather experience or visual identity into another repository.

## 1. Verified baseline (rechecked 2026-11-26)

| Item | Verified state |
|---|---|
| Solstice baseline | `main` at `e4200acab76f94acaaaec86b27e76eacf5c86560` when this modernization branch was created |
| Motion submodule | `motion/` remains pinned at `25ffa862f503b482ed98671cd5eab66040f0d75f`; this is the pre-MOT0.1 merge base, so Solstice does not yet consume the merged MOT0.1 changes |
| Motion integration | Already included through `includeBuild("motion")` and dependency substitution in `settings.gradle.kts` |
| Submodule URL | The old `INFORMATION89/Motion.git` URL is being changed to canonical `https://github.com/INFORMATION89/Motion-UI.git` in the current documentation/CI PR; the pinned commit is intentionally unchanged |
| Motion Core MOT0.1 | Motion PR #4 is **merged**. PR head `4b7d58ebef3a42a89596404930dfb22a502ed687`, merge commit `4025f320e48956dec573879b5f3e3abfa4508066`. Its exact PR-head workflow run #62 (`45764882203`) passed Spotless, unit tests, and `:motion:assembleRelease` |
| Motion current main | `04598184db3418f03a40eb4611bc0c5be853b949` at 2026-11-26 08:56 UTC. Verify again before any integration change |
| Motion Lab | PR #7 merged 2026-11-26 08:42 UTC; final PR head `570bfdd199669e51bbf5a2dade37831bb98321ae`, merge commit `6ea53caf31a17b70894487cd065707a800d1a35a`. It adds standalone `:motion-lab`, not `:motion-ui` |
| Motion Lab CI | Exact final PR-head run #106 (`45784947096`) passed. The earlier run #95 formatting failure was superseded by the corrected final head; the merged Lab is verified by that PR-head run |
| UI kit status | A separate `:motion-ui` Gradle module does not exist yet. It remains planned, not something Solstice can import today |
| Platform scope | Current Motion artifact is Android / Jetpack Compose only. iOS, Windows, macOS, and Linux desktop targets are not supported |
| Solstice migration status | No Motion submodule pointer change is included here. A future pointer bump must target a deliberately selected merged Motion commit and pass Solstice's clean recursive checkout/build/CI |

The MOT0.1 and Motion Lab merges in Motion do not update Solstice automatically: the submodule is still pinned to its earlier commit. The merged standalone Motion Lab is not the same as Motion UI and must not be treated as delivery of the planned `:motion-ui` component kit.
## 2. What belongs where

The intended architecture has two Motion layers:

### Motion Core — `:motion`

Reusable behavior with no Solstice screens, weather models, branding, or business rules:

- spring definitions and shared motion tokens;
- interruptible motion state, retargeting, settling, and velocity handoff;
- drag, catch, scrub, release projection, and elastic resistance;
- generic morph geometry and source/target mechanics;
- generic page-motion primitives;
- semantic haptic abstractions, ambient-motion scheduling, and reduced-motion behavior;
- low-level helpers that directly support those interactions.

Compose remains the animation and input foundation. Motion must not create a second animation engine.

### Motion UI — planned `:motion-ui`

A separate, optional Compose UI kit built on Motion Core. It may own reusable visual components and their interaction wiring, with neutral defaults and configurable tokens:

- a reusable page frame/scaffold and header-action slots;
- standard card and sheet building blocks;
- reusable toolbar/header action controls;
- shared typography, shape, spacing, and motion-token contracts;
- optional glass/material surface wrappers where dependencies and platform fallbacks are justified;
- reusable message/feedback presentation primitives.

The kit must depend on Motion Core, never the reverse. It must not contain Solstice's weather screens, sky rendering, weather icons, domain models, navigation policy, or fixed brand palette. Solstice supplies its own theme values and composes the kit's building blocks.

### Other libraries and Solstice

| Responsibility | Owner |
|---|---|
| Layout, drawing, pointer input, animation runtime | Jetpack Compose |
| Standard buttons, switches, dialogs, text fields, semantics and accessibility | Material 3 / Compose components |
| Specialized reorderable lists, charts, maps, image loading, and other domain widgets | A focused library where it already solves the problem |
| Navigation back stack, routes, deep links, and app lifecycle | Navigation 3 and Solstice app architecture |
| Glass blur implementation | Haze or another focused rendering library; Motion UI may provide a thin configurable wrapper only if useful |
| Weather data, forecasts, weather cards, sky, moon/sun visuals, and product behavior | Solstice |
| Solstice colors, glass opacity, typography choices, spacing values, and brand-specific styling | Solstice theme/configuration |

Do not clone Material components just to animate them. Prefer the component library for structure and semantics, then connect it to Motion where a reusable motion behavior is needed.

## 3. Inventory and recommendation

The following inventory is based on the current Solstice source tree and inspected call sites. It is a migration proposal, not an instruction to move every listed file.

| Solstice area | Current implementation / evidence | Recommendation | Notes |
|---|---|---|---|
| Motion tokens | `components/SolsticeMotion.kt`, `theme/SolsticeMotionScheme.kt` | **Direct reuse + app-side configuration** | Motion Core already supplies `Motion` tokens and physics. Keep Solstice's mapping/theme adapter until the final token contract is agreed. Avoid maintaining duplicate spring values. |
| Sheet drag and physics | `components/SolsticeSheetState.kt` uses `MotionValue`, `MotionPhysics`, `rememberMotionValue` | **App-side composition** | Keep sheet intent, detents, keyboard sizing, dismissal policy, and Solstice state here. Reuse Motion for generic drag/release mechanics. |
| Screen push transition | `components/ScreenPush.kt` uses `Motion.settle` with Compose Navigation transitions | **Direct reuse** | Keep route and predictive-back ownership in Navigation 3/Solstice. Motion supplies the spring. |
| Pressed segmented-row shape | `components/PressMorph.kt` uses Compose pointer observation and Material motion scheme | **App-side composition / possible Motion UI candidate** | Keep semantics and row structure with the component. Consider a reusable press-interaction helper only if multiple consumers need the same behavior and its contract can be separated from this row. |
| Sheet dialog | `components/SheetDialog.kt` wraps Material 3 `ModalBottomSheet` | **Use another library + app-side composition** | Material owns sheet structure, accessibility, and semantics. Solstice owns its slots, wording, and appearance. Do not rebuild a bottom sheet in Motion Core. |
| Glass | `components/Glass.kt` uses Haze | **Use another library + possible Motion UI wrapper** | Haze owns blur. Solstice owns tint, opacity, and where glass appears. A future kit wrapper must accept material/tint configuration and have explicit fallbacks. |
| Cards | `components/SolsticeCard.kt` and weather-specific card call sites | **Split: Motion UI building block + Solstice content** | A neutral card container may move to Motion UI; weather data, card ordering, priority, and Solstice material remain in Solstice. |
| Page frame and toolbar | `shell/PageFrame.kt`, `shell/Toolbar.kt`, `shell/ToolbarMotion.kt` | **Motion UI candidate** | Extract only generic layout slots, reusable header actions, and interaction wiring. Keep page identity, page-specific actions, toolbar state, and navigation decisions in Solstice. |
| Page navigation and pager | `shell/ShellNavigation.kt` and shell call sites | **Split: Motion Core + Solstice** | Reuse generic page spring/overshoot behavior where API fit is verified. Keep destinations, selected-page state, navigation policy, and predictive back in Solstice/Navigation 3. |
| Moon card and sky | `shell/MoonCard.kt`, `MoonRenderer.kt`, `MoonSky.kt`, `sky/*` | **Keep in Solstice** | Weather/astronomy content, shaders, sky palette, and the moon experience are product-specific. Motion can animate their generic containers. |
| Search, places, forecast and outlook | `search/*`, `shell/ForecastPage.kt`, `PlacesPage.kt`, `WeekPage.kt`, `RainCard.kt` | **Keep in Solstice** | These own weather/domain content and app workflows. Use Motion or Motion UI only at their reusable interaction boundaries. |
| Motion demos | `lab/*` under Solstice's expressive package | **Move to Motion's own lab in a separate phase** | First audit every file for Solstice/Breezy-derived code and licensing. Do not copy blindly or combine this with the first app migration. |
| Reorder interaction | `expressive/reorder/*` | **Use existing specialized library / keep app wiring** | Keep reorderable list semantics and app ordering rules with the chosen reorder library and Solstice. Motion should not become a general list library. |

## 4. Migration phases

Each phase should be a small, independently reviewable change. Do not start the next phase until the previous one builds and its behavior is reviewed.

### Phase 0 — unblock and align the baseline

1. MOT0.1 reliability work is merged in Motion PR #4 and its recorded exact-head CI passed. Do not repeat the spike.
2. Motion PR #7 (standalone Motion Lab) is merged; its exact final PR-head run #106 passed. This does not create or deliver `:motion-ui`.
3. The canonical submodule URL is updated in this Solstice PR while the existing submodule SHA is deliberately preserved.
4. Verify clean recursive submodule checkout and Gradle composite build in Solstice CI after the URL change.
5. Treat adopting the merged MOT0.1 commit as a separate implementation change: select the exact merged commit, review its diff against the currently pinned SHA, bump the pointer deliberately, and run focused Motion/Solstice checks before any further migration.

**Acceptance:** a clean clone initializes the submodule; Gradle resolves `:motion`; Solstice CI passes. No visual or runtime behavior changes.

### Phase 1 — use Motion Core consistently

1. Compare the checked-out Motion API with the current Solstice adapters.
2. Remove only true duplicate spring/physics implementations; retain app-specific state and behavior.
3. Route generic sheet drag/release and page movement through stable Motion APIs where the behavior matches.
4. Keep Navigation 3, Material 3, Haze, and other focused libraries responsible for their own structures and capabilities.
5. Add focused regression tests around sheet interruption, dismissal, detents, page reversal, and gesture ownership.

**Acceptance:** no duplicated generic physics; no change to the agreed interaction feel; input remains responsive; CI passes.

### Phase 2 — design and build Motion UI foundations

This phase starts only after the module boundary and API proposal are reviewed.

1. Create the separate `:motion-ui` module depending on `:motion`.
2. Define neutral component contracts and theme/token inputs before moving components.
3. Start with the page frame and header-action slots, then neutral card and sheet building blocks.
4. Keep Material 3 responsible for standard controls and semantics. Use Haze for blur rather than implementing a blur engine.
5. Add previews, focused tests, accessibility checks, and platform capability fallbacks for each reusable component.

**Acceptance:** Motion UI can be used by a sample app with a different theme; it has no Solstice package/domain dependency; Solstice-specific values are injected rather than hardcoded.

### Phase 3 — adopt Motion UI in Solstice

1. Replace only the generic pieces selected in Phase 2.
2. Pass Solstice's existing colors, typography, shapes, spacing, glass values, and motion choices through the kit's theme/configuration.
3. Keep weather-specific screens and content in Solstice.
4. Compare before/after screenshots and behavior for every migrated surface.
5. Remove old app-side implementations only after the replacement is verified and no call sites remain.

**Acceptance:** Solstice keeps its agreed appearance and behavior; shared components have one implementation; no duplicate theme or component layer remains.

### Phase 4 — move the lab and consolidate

1. Audit the existing Solstice lab files and headers for provenance and licence compatibility.
2. Move only clean, generic demos into Motion's own lab.
3. Keep the lab independent of Solstice data and styling.
4. Remove Solstice's duplicate demos only after Motion's lab covers the migrated components.

**Acceptance:** Motion's lab demonstrates Core and UI components independently; Solstice no longer carries duplicate generic Motion demos.

## 5. Rules for AI-assisted work

Every AI agent working on this migration must:

1. Read Solstice `RULES.md`, `PLAN.md`, `MOTION.md`, `UI_SYSTEMS.md`, and this document; then read Motion's `AGENTS.md`, framework overview, platform support, motion rules, and module API.
2. Verify current branch, working tree, submodule SHA, open PRs, and latest CI before editing.
3. Inspect actual source and call sites; old plans and this inventory are navigation aids, not proof that code is unchanged.
4. Classify each proposed change as direct reuse, app-side composition, Motion Core gap, Motion UI candidate, keep in Solstice, or use another library.
5. Never move a Solstice component into Motion just because it animates. Separate generic mechanics from app content, visual identity, and product behavior.
6. Prefer existing Compose, Material 3, Navigation 3, Haze, and specialized libraries over reimplementing them.
7. Propose missing Motion APIs as a separate design with contracts and focused tests before implementing them.
8. Preserve agreed motion behavior: interruption, velocity continuity, gesture ownership, morph continuity, and responsive input.
9. Keep CI cost-aware: run the smallest relevant checks while iterating, then one full CI workflow for a coherent code batch. Do not run device tests when hardware is unavailable; mark them `NOT TESTED` / `DEFERRED`.
10. Do not change visible behavior, migrate multiple unrelated components, or bump the Motion submodule as incidental cleanup. Ask for approval where Solstice's existing rules require it.

## 6. Verification checklist

- [x] Motion PR #4 MOT0.1 merge and exact-head CI evidence verified; see baseline table. Remaining platform/API adoption work is tracked separately.
- [ ] Solstice points to the reviewed Motion commit and uses the canonical submodule URL.
- [ ] A clean recursive clone resolves the composite build.
- [ ] No Motion source imports Solstice packages or weather-domain types.
- [ ] No duplicate spring/gesture physics remains in migrated areas.
- [ ] Material 3 and specialized libraries still own component structure and semantics.
- [ ] Solstice owns weather content, navigation policy, and visual identity.
- [ ] Interactions remain interruptible and gestures have one clear owner.
- [ ] Reduced-motion and accessibility behavior are preserved.
- [ ] Focused tests and full CI pass for each coherent phase.
- [ ] Physical-device checks are recorded separately and remain `NOT TESTED` / `DEFERRED` until hardware is available.

## 7. Explicit non-goals

- No iOS, Windows, macOS, or Linux support is implied by this Android migration.
- No conversion of Motion to Kotlin Multiplatform as part of Solstice adoption.
- No migration of weather screens, weather data, sky rendering, moon/sun visuals, or Solstice branding into Motion.
- No replacement of Material 3, Navigation 3, Haze, or specialized component libraries.
- No broad UI redesign or visual changes as part of extracting reusable components.
- No submodule bump to an unmerged or unverified Motion branch.
