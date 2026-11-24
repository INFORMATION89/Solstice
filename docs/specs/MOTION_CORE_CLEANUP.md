# Motion core clean-up (Saturday step 5)

Status: **plan** (2026-11-24), from reading Motion at `25ffa86` and the decisions in CONTINUE.md
("Decided in chat" 4 and 5, the confirmed spring table) and ROADMAP.md (R3, B3, B4). Section 5 is
only for Marco to add or not (CONTINUE.md: ask before adding R3/B3/B4 to Saturday).

## 1. Springs become speed × bounce

`MotionSpring(response, damping)` stays the type. New named levels, in `Motion.kt`:

```kotlin
object MotionSpeed  { val Fast = 0.2f; val Default = 0.31f; val Slow = 0.4f }          // response, s
object MotionBounce { val Subtle = 0.9f; val Slight = 0.8f; val Playful = 0.5f; val None = 1f } // damping
fun motionSpring(speed: Float, bounce: Float) = MotionSpring(speed, bounce)
```

`None` is only for effects and Reduce Motion (Marco, 2026-11-22: movement always has some bounce).

**Every token, before → after** (response s / damping; today's values worked out from Motion.kt):

| Token | Today | After | Change you'd feel |
|---|---|---|---|
| `snappy` | 0.20 / 0.9 | Fast × Subtle, 0.20 / 0.9 | none |
| `control` | 0.31 / 1.0 | Default × Subtle, 0.31 / 0.9 | a hint of bounce |
| `settle` | 0.31 / 1.0 | **merged into `control`** (Marco, 2026-11-24); `settle` stays as an alias for a while | a hint of bounce |
| `container` | 0.32 / 0.78 | Default, 0.31 / 0.78 | a touch quicker |
| `content` | 0.31 / 0.85 | Default, 0.31 / 0.85 | none |
| `playful` | 0.31 / 0.5 | Default × Playful, 0.31 / 0.5 | none |
| `dismiss` | 0.39 / 1.0 | Slow × Subtle, 0.40 / 0.9 | a hint of bounce |
| `smooth` | 0.40 / 1.0 | Slow × Subtle, 0.40 / 0.9 | a hint of bounce |
| `gentle` | 0.44 / 1.0 | Slow × Subtle, 0.40 / 0.9 | quicker, a hint of bounce |
| `brisk` | 0.40 / 0.78 | Slow, 0.40 / 0.78 | none |
| `momentum` | 0.40 / 0.8 | Slow × Slight, 0.40 / 0.8 | none |
| `wobble` | 0.31 / 0.32 | named special, unchanged | none |

`container` (0.78) and `content` (0.85) keep their own damping in the Slight column, as confirmed.
Old names stay as aliases until every use has moved; MOTION.md's "dismiss: no bounce" and "settle: no
bounce" lines change.

## 2. Effects springs (never bounce)

```kotlin
object MotionEffects { val Fast = MotionSpring(0.10f, 1f); val Default = MotionSpring(0.16f, 1f); val Slow = MotionSpring(0.22f, 1f) }
```

Material 3's effects springs (stiffness 3800, 1600, 800, no bounce) give these responses. Used for
opacity, colour and blur: `BlurCrossfade`, the glass fade in `SpringMorph`, the scrim, the toolbar
highlight's colour.

## 3. Reduce Motion, built in

Today `rememberReducedMotion()` lives in `Morph.kt`, reads `ANIMATOR_DURATION_SCALE == 0` once
(`remember`), and only 3 places use it.

- New `ReducedMotion.kt`: `LocalReducedMotion` (a CompositionLocal) provided by `MotionTheme`/the
  root, reading the setting and **following changes** (a content observer), so switching "Remove
  animations" applies without restarting.
- What changes when on (Apple's list, decision 5): every spring's damping → 1 (no bounce); morphs and
  sheets → a quick fade in place (`MotionEffects.Default`); blur hand-offs → plain crossfades; no
  page recede; drags still follow the finger; haptics stay.
- Each piece reads `LocalReducedMotion` itself; apps pass nothing.

## 4. The old `Morph` (decided 2026-11-24: remove it on Saturday)

Checked 2026-11-24: the moon card **already opens with `SpringMorph`** (`MoonCard.kt` line 423); its
`import io.github.information89.motion.Morph` is unused. The old `Morph(…)` composables are used only by
the two Labs' `MorphDemo`. Marco chose to port the moon card; there's nothing to port, so Saturday:

1. Split `Morph.kt` (404 lines). **Keep**, in a new `MorphAnchors.kt`: `MorphAnchor`, `MorphFrame`,
   `MorphGeometry`, `MorphRegistry`, `MorphHost`, `morphAnchor`, `morphAnchorOf` (`SpringMorph`, the moon
   card and the postcards use them).
2. **Delete:** both `Morph(…)` overloads, `MorphContentScope`, `morphFrom`, `reveal`, `handOff` (no users
   outside the file), and `rememberReducedMotion` (replaced by `LocalReducedMotion`, section 3).
3. Delete `MorphDemo` from the Lab (the "Morph, iOS way" demo stays), and the unused import in
   `MoonCard.kt`.
4. Check: the moon card and a postcard window open, close, reopen while closing and drag closed, the same
   as before (PHONE_PASS.md).

## 5. R3, B3, B4 (added to Saturday by Marco, 2026-11-24)

- **B3 (tap after a swipe stops the tabs):** `springPages` scrolls the pager through the whole bounce,
  so the pager reports "scrolling" until it settles and swallows the tap. Fix: scroll only to the target
  (no overshoot in the scroll), draw every bounce through `PageOvershoot` (today only at the ends), and
  count the pager as settled below a threshold token (`MotionThresholds.Settled`, px). Lab test:
  swipe, then tap within 100 ms, 20 times.
  **Platform rule (checked 2026-11-24):** on iOS and Android, a tap while content is still visibly
  moving stops it and is *not* a press (WebKit's tests check exactly this). So the fix only makes taps
  work once the pages look settled; a tap during a real fling still just stops it.
- **B4 (jumping across the toolbar flashes Forecast):** `springToPage` scrolls through every page in
  between. Fix: `PagerState.jumpToPage(target)`: snap to the page next to the target, then spring the
  last page; the toolbar's highlight follows the target tab, not the scroll position.
  **Platform rule:** Material recommends a fade-through between navigation-bar destinations and
  keeps sideways motion for neighbours. Marco (2026-11-24): build both for jumps of two or more
  pages (slide skipping the middle, and fade through) as a Lab switch, and pick on the phone.
- **R3 (explicit states):** `SpringMorph` runs five `Animatable`s (x, y, width, height, radius) in their
  own coroutines plus a press value; the fix drives them from one state (closed, opening, open,
  closing, dragging) and one progress value, with declared gesture rules instead of on/off flags.
  Added to Saturday by Marco; last in step 5, carried to the next session if it doesn't fit.
  **Platform rule:** Apple's "Designing Fluid Interfaces" (WWDC 2018): responsive, interruptible and
  redirectable at any moment; the explicit states must keep all three.

## 6. Tokens that are still typed in

Touch slop, settle threshold and blur sizes become tokens (R3). Search Motion for numbers in code; each
one gets a token or a line on the CI allowlist (CONTINUE.md decision 7).

## Checks

- Unit tests: each token's response and damping (table above); `stiffnessFor`/`responseFor` round
  trip; Reduce Motion gives damping 1 for every token.
- Lab: every demo with Reduce Motion on and off; the subtle vs no bounce switch.
- Phone pass: PHONE_PASS.md "Springs".

## Files

`Motion.kt` (tokens), new `ReducedMotion.kt`, `Morph.kt` (split into `MorphAnchors.kt`, the rest deleted), `BlurCrossfade.kt`,
`SpringMorph.kt` (effects springs, Reduce Motion), `PageSpring.kt` (only with B3/B4), tests in
`motion/src/test`, MOTION.md.
