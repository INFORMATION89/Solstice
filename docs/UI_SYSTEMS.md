# Solstice UI systems: audit and libraries

Historical audit researched 2026-11-19. Its inventory counts and implementation descriptions are a snapshot of that date, not a verified description of the current tree. It records candidates for standardization and library evaluation; re-check the source and current Motion-UI status before acting on any row. It informed the "Motion framework" and "Design system foundations" sections of [PLAN.md](PLAN.md). Some recommendations may since have been implemented, superseded, or gated. Any user-visible motion change still requires agreement first.

## Short answer

The suspicion is partly right.

- **Hand-written where a library already exists:**
  - glass and edge blur (Haze does both);
  - list reordering (Reorderable);
  - screen push and back transitions (Navigation 3 with shared elements and predictive back);
  - snap-to-position swipes (Compose's `AnchoredDraggable`);
  - Material 3 components, which are almost unused (5 calls in all of `ui/expressive/`).
- **Custom code is justified for the gesture-scrubbed morphs** (pill into sheet, pull to close,
  overshoot past open). Compose's shared elements are built for navigation and visibility
  transitions. They can now take a gesture's velocity, but they aren't meant to be scrubbed by a
  spring that overshoots. That is exactly what the moon card fights today. That part stays our own,
  but smaller (see "Motion framework" in PLAN.md).
- **The biggest problem isn't a missing library, it's missing tokens.** Spacing, corners, colours
  and springs are typed by hand in each file, so the same thing looks or moves slightly differently
  everywhere.

## Inventory (counted in `app/src/main/kotlin/org/breezyweather/ui/expressive/`)

| System | What's there now | Problem | Standardize to |
|---|---|---|---|
| **Motion** | `SolsticeMotion.kt` (5 springs, rubber-band, projection), used only by `MoonCard.kt`. Elsewhere: 28 `spring(...)`, 12 `tween(...)`, 13 `animate*AsState`, ~15 different damping/stiffness pairs | Every screen moves a bit differently; tweens break the springs-only rule | `:motion` tokens only, plus a Solstice `MotionScheme` (below) so Material components move the same way |
| **Spacing and sizes** | 325 `.dp` literals, about 30 distinct values, including off-grid ones (5, 7, 10, 11, 14, 18, 22 dp) | No scale; padding drifts between cards and pages | A spacing scale on a 4 dp grid (e.g. 4, 8, 12, 16, 24, 32, 48) and named sizes (toolbar height, touch target, card padding) |
| **Corners and shapes** | 23 `RoundedCornerShape(...)` with 11 radii (4 to 48 dp); 9 uses of `MaterialTheme.shapes` / `MaterialShapes` | Radii picked per component; plain circular corners (iOS and M3 Expressive both read smoother with continuous corners) | Shape tokens on the M3 scale (incl. `largeIncreased`, `extraLargeIncreased`), one continuous-corner shape, and a rule for concentric corners (inner radius = outer minus padding) |
| **Type** | `ExpressiveType.kt` + 59 `MaterialTheme.typography` uses; a few literal sizes (112 sp hero temperature, 40 sp) | Mostly fine | Name the hero styles (e.g. `heroTemperature`) in the type scale |
| **Colour** | 41 `Color(0x…)` literals, 25 in `ShellPages.kt` (paper, postcard, stamp), 8 in `MoonCard.kt`; 31 `Color.White/Black` | Material You colour roles bypassed in places; paper colours repeated | One palette file for non-dynamic colours (paper, ink, stamp, night card) and dynamic roles for everything else |
| **Materials (glass, blur)** | `Glass.kt` (own backdrop recorder: records a `GraphicsLayer` per frame, blurs each element's rectangle at ¼ size), `EdgeBlur.kt`, 34 glass call sites, 3 sources (`LocalSkyGlass`, `LocalPageGlass`, shell) | This is a re-implementation of what Haze does, and we maintain the performance work ourselves | Haze (below), wrapped in one `SolsticeMaterial` API: `glass`, `edge`, `scrim` |
| **Sheets** | Two systems: Material `ModalBottomSheet` (`SheetDialog`, `ExpressiveChoices`) and hand-made overlays (moon card, search) | Different motion, handle and dismiss rules | One `SolsticeSheet` on the motion framework; settings can keep M3's sheet if it gets the Solstice `MotionScheme` |
| **Gestures** | 19 `detect*Gestures` in 6 files (shell, pages, moon, messages, reorder), 2 `draggable` | Each one has its own thresholds, velocity handling and snap logic | `Modifier.motionDrag` (morphs) and `AnchoredDraggable` (snap swipes) |
| **Back** | 2 `BackHandler`, 2 `PredictiveBackHandler` | The moon card and search close instantly on back instead of following the gesture | `PredictiveBackHandler` everywhere, feeding the same motion value as a pull |
| **Navigation** | Shell: `HorizontalPager` (3 pages). Screens: `ScreenPush` (`AnimatedContent`). Settings: upstream `NavHost` with the default fade | Three transition systems; the plan already notes the fade | Keep the pager. Move pushed screens to Navigation 3 (or Navigation 2 with shared elements) for predictive back and shared titles |
| **Components** | `ShellPages.kt` is 2,231 lines holding the pages *and* their cards (`TodayCard`, `RainCard`, `PaperCard`, `PlaceCard`, `GlassCard`, `Stamp`…); `PlaceWindow.kt` re-hosts the shell's toolbar and pages | The backlog's "change it once, repeat it in the postcard window" problem | A small catalog in `ui/expressive/components/`: Card, Sheet, Toolbar, Message, PageScaffold, Button/Pill |
| **Reorder** | Own `ReorderList.kt` (263 lines) | Long-press, auto-scroll and drop animation all hand-written | Reorderable library |
| **Icons** (added 2026-11-19) | `material-icons-extended` (frozen since 2022; 34 distinct icons in `ui/expressive/`, 40 Outlined / 14 Filled), 54 upstream `ic_*.xml`, our own `SolsticeIcons.kt` | Three sources, two styles, filled used without meaning; 20 of 29 icon sizes typed by hand | **Done 2026-11-19:** Material Symbols Rounded, one catalog (`SolsticeIcons`, optical sizes 24/20), fill only for on/selected; old library removed |
| **Messages** | Own snackbar host with its own drag | Another one-off gesture | Same `SolsticeSheet`/motion value for swipe-away |

## Libraries checked

| Library | Use for | Fit for Solstice | Verdict |
|---|---|---|---|
| **Haze** (`dev.chrisbanes.haze`) | Backdrop blur, tint, noise, progressive (gradient) blur, glass | Stable 2.1.0 (2026-11-02). Works on all Android versions (RenderScript below Android 12), so it fits our minimum (Android 10, API 29, since 2026-11-21). Has performance modes and a stable `haze-glass` module | **Prototype first.** Replace `Glass.kt` + `EdgeBlur.kt`, measure on the Pixel 7 release build against today's numbers. Keep ours only if Haze is slower |
| **Backdrop / Liquid Glass** (Kyant0) | iOS Liquid Glass: refraction, lens, highlights | 2.0.0 (May 2026), Apache-2.0, multiplatform. No ready-made components | Only if we want refraction. Solstice's direction is frosted Material glass, so Haze is the better fit |
| **Glasmio** (NEORUAA) | iOS 27 components and motion research | Needs Android 12+ (13+ for refraction); bundles Apple artwork | **Reference only**: its UIKit motion notes are what our release rules come from |
| **Compose shared elements** | Container transforms between screens | 1.10 added `prepareTransitionWithInitialVelocity` (gesture velocity in), per-element enable/disable and `skipToLookaheadPosition`; 1.11 added `LookaheadAnimationVisualDebugging` | Use for **navigation** (pushed screens, shared titles). Not for the scrubbed overlay morphs |
| **Navigation 3** | Back stack, predictive back, shared elements | All versions support predictive back; works with shared elements | Use for pushed screens (Settings, Reorder, About) instead of `ScreenPush` and the default fade |
| **`AnchoredDraggable`** (Compose foundation) | Drags that snap between fixed positions (swipe card to star or delete, swipe a message away) | Built in. Snaps with velocity and positional thresholds; dragging stays between the anchors (overscroll via `overscrollEffect`) | Use for snap swipes. The pill-into-sheet morph needs overshoot, so it stays on `MotionValue` |
| **Material 3 `MotionScheme`** | One place where Material components get their springs | Already set to `MotionScheme.expressive()` in `ExpressiveTheme.kt` | Write a **Solstice `MotionScheme`** that returns our tokens, so switches, sheets and buttons in Settings move exactly like the rest |
| **Material 3 Expressive components** | Buttons, button groups, toggle buttons, loading indicator, floating toolbar, flexible top bars | In material3 1.5.0-rc01 (what we use) | Use for controls (buttons, toggles, loading). Keep our toolbar (the bud and the glass are ours) |
| **graphics-shapes / `MaterialShapes`** | Shape morphs (pin icon, press morphs, loading) | Part of Material 3 already | Use for icon and button shape morphs instead of hand-drawn paths |
| **Reorderable** (Calvin-LL) | Drag to reorder in lazy lists and grids | 3.1.0 (Apr 2026), Apache-2.0, actively maintained | Replace `ReorderList.kt` |
| **Continuous corners** (Kyant0 Capsule / Shapes) | iOS-style squircle corners | Apache-2.0; used by Glasmio | Try for cards and sheets as part of the shape tokens |

**Haze, decided 2026-11-19:** Haze draws the glass (same speed and look as ours on the Pixel 7);
the edge blur stays ours (Haze's progressive content blur measured slower). Numbers in the PLAN.md log.

**Catalog, first pass 2026-11-19:** `SolsticeCard` (glass/paper), `Toolbar.kt`, `PageFrame.kt`
(shared by the shell and the postcard window); `ShellPages.kt` split into one file per page. Sheet
and page scaffold follow in M4.

## Proposed order (to agree)

1. **Tokens first (cheap, no visual risk):** spacing, sizes, shapes, palette, type names, and the
   motion tokens with the Solstice `MotionScheme`. Replace literals file by file.
2. **Haze prototype:** toolbar and edge blur on Haze behind a debug switch. Compare frame times
   and looks, then decide.
3. **Motion framework M1 to M3** (PLAN.md), using `AnchoredDraggable` for snap swipes and
   `PredictiveBackHandler` everywhere.
4. **Component catalog:** split `ShellPages.kt`, one Card / Sheet / Toolbar / Message /
   PageScaffold, and make `PlaceWindow` reuse them instead of re-hosting the shell.
5. **Library swaps:** Reorderable, Navigation 3 for pushed screens, M3 Expressive controls.
6. **Motion framework M4 and M5:** move everything over, then the motion lab.

## Sources

- Haze docs and changelog: https://chrisbanes.github.io/haze/latest/
- Backdrop (Kyant0): https://github.com/Kyant0/AndroidLiquidGlass
- Compose December '25 (1.10) release notes: https://android-developers.googleblog.com/2025/12/whats-new-in-jetpack-compose-december.html
- Compose April '26 (1.11) release notes: https://developer.android.com/blog/posts/whats-new-in-the-jetpack-compose-april-26-release
- Shared elements with navigation and predictive back: https://developer.android.com/develop/ui/compose/animation/shared-elements/navigation
- `AnchoredDraggable`: https://developer.android.com/jetpack/compose/touch-input/pointer-input/migrate-swipeable
- `MotionScheme`: https://composables.com/docs/androidx.compose.material3/material3/interfaces/MotionScheme
- Shapes and `Morph`: https://developer.android.com/develop/ui/compose/graphics/draw/shapes
- Reorderable: https://github.com/Calvin-LL/Reorderable
- Glasmio: https://github.com/NEORUAA/Glasmio (`docs/ios-sheet-motion.md`, `docs/dropdown-motion.md`, `THIRD_PARTY_NOTICES.md`)
