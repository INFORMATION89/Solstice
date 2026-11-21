# :motion

A small motion framework for Jetpack Compose that makes interactions feel like iOS: springs written
as *response* and *damping*, gestures that hand their velocity to the animation, rubber-banding,
UIKit's release rule, and surfaces that morph out of the element that opened them. A universal library of
animations and interaction components for any app: it depends only on Compose (`animation`,
`foundation`).

Licensed under the Apache License 2.0 (see `LICENSE`), unlike the rest of Solstice (LGPL-3.0).

## Install

It's a Gradle module, not published yet. Copy `motion/` into your project, add `include(":motion")`
to `settings.gradle.kts` and `implementation(project(":motion"))` to your app. Package:
`io.github.information89.motion`.

## The pieces

| | What it is |
|---|---|
| `MotionSpring(response, damping)` | A spring the iOS way: `response` is the period in seconds (how quick it feels), `damping` the ratio (1 = no bounce). `spec()` gives a Compose `SpringSpec`; `fromCompose(dampingRatio, stiffness)` converts back. |
| `Motion` | The tokens. Use only these; anything new becomes a token first. |
| `MotionValue` | One number per interaction (0 = closed, 1 = open, or your own rest points). A finger moves it (`drag`), letting go hands the velocity to a spring (`release`), a new touch catches it (`stop`). Rubber-bands past its ends. |
| `rememberMotionValue(...)` | Remembers a `MotionValue`. Rest points can change later (e.g. a sheet's tall size following the keyboard). |
| `Modifier.motionDrag(value, orientation, range)` | A drag that drives a `MotionValue`: `range` of finger travel moves it by 1. Catches immediately while it moves. |
| `MotionPhysics` | The maths: `rubberBand`, `resist` / `unresist`, `project` (where a fling would land), `chooseTarget` (the release rule), and the `SlowFlick` / `FastFlick` thresholds. |
| `MorphHost`, `Modifier.morphAnchor`, `Morph` | A surface that grows from one element (`from` anchor) into another (`to` anchor, or a known `MorphAnchor`), driven by a `MotionValue`. Its content is laid out once at the final size, so nothing reflows. |
| `MorphStyle`, `Modifier.morphSource(key, cornerRadius, hidden)` | The button becomes the card: give `Morph` the source's style and the target's (fill, border, shadow, how much of your `material` shows) and the source's own `sourceContent`. The morph starts as exactly the button and every property interpolates to the card's (over `styleWindow`; nothing crossfades), while `morphSource` hides the real button in that same frame. |
| `rememberMorphDrawn(key)` | True once a `Morph` from `key` has drawn: start the opening spring then, so its first visible frame is the source itself. |
| `rememberAmbientClock(fps, running)`, `rememberAmbientMotionAllowed()` | The clock for ambient animation (drifts, twinkles, weather effects): capped fps, only while the screen is resumed and the thing is visible, wall-clock based; and whether ambient motion should run at all (not in battery saver or with animations off). |
| `Dial`, `DialScale`, `DialLook`, `rememberDialState` | A value scrubbed along a ruler: drag 1:1, fling with projection, snap to steps, rubber-band at the ends, caught mid-flight; haptics per step, major step, end and settle; labels and decorations per major step, a centre marker, drum curvature, edge blur, visible ends; adjustable in TalkBack. |
| `Modifier.edgeBlur(top, bottom, start, end)`, `EdgeBand` | A progressive blur and fade at any edge (lists, dials, carousels): each band blurs up to its `blur` at the edge (Android 12+) and fades into a colour (`fadeTo`) or to transparent (`fadeOut`). One blur effect per band, set once. |
| `Haptic` tokens, `MotionHaptics`, `LocalHaptics`, `haptics()` | Haptic feedback by purpose (`tick`, `detent`, `edge`, `snap`, `press`, `release`, `threshold`, `confirm`, `reject`): each a vibration primitive at an intensity, with a system fallback; rate-limited, lighter on fast gestures, following the touch-feedback setting and one global intensity. Call `haptics().perform(Haptic.detent)`; never the vibrator directly. |
| `PageTransition`, `Modifier.pageTransition`, `pageSettleFling` | Page changes that never block input: the pages switch at once and the slide is drawn on top (tab taps with `go`, swipes let go with `pageSettleFling`), so a tap during it always lands. |
| `Modifier.morphFrom(anchor)` | Inside a `Morph`: an element that travels from an anchor (a button's label) to its place in the target (a heading). |
| `Modifier.reveal(value, from, to)`, `Modifier.handOff(value, from, to)` | Choreography: content fades and scales in through a window of the value (and leaves first on close); a source's label fades out. |
| `rememberReducedMotion()` | True when the system's "Remove animations" is on. `Morph` then only fades. |
| `MotionTime.scale` | Slow motion for checking animations: every token runs this many times slower. Never left on. |

## Tokens

| Token | Response / damping | For |
|---|---|---|
| `container` | ≈0.32 s / 0.78 | Morphing containers opening: sheets, menus, pills. A touch of bounce. |
| `dismiss` | ≈0.39 s / 1 | A container going back into its source. No bounce. |
| `control` | ≈0.31 s / 1 | Controls: buttons, toggles, sliders. |
| `content` | ≈0.31 s / 0.85 | Inner content scaling and fading in. |
| `settle` | ≈0.31 s / 1 | Settling after a drag or fling. |
| `snappy` | 0.2 s / 0.9 | Small controls: switch thumbs, presses, icon changes. |
| `playful` | ≈0.31 s / 0.5 | Expressive moments only: a pop, a press springing back, a landing bump. |
| `wobble` | ≈0.31 s / 0.32 | Liquid effects only: a drop pulling free, a surface absorbing it. |
| `gentle` | ≈0.44 s / 1 | Unhurried settles, like a page swipe. |

## Rules

1. **Only springs, only tokens.** No `tween` or easing curves for motion. (Solstice keeps three
   timed effects, a sticker peel, a photo "develop" and a slow drift, as documented exceptions.)
2. **Velocity carries over.** A release starts its spring with the finger's speed; a flick that
   dismisses carries into the close.
3. **Everything can be caught.** A touch stops a running motion where it is and the finger takes
   over.
4. **The release rule (UIKit):** slower than 250 dp/s is no flick; otherwise the rest point nearest
   to where the fling would land; faster than 1000 dp/s, at least one step in its direction.
5. **Rubber-band past the ends** with iOS's curve, and release with the velocity scaled down by the
   resistance.
6. **Open on `container`, close on `dismiss`.** Opening can bounce a little; going back never does.
7. **Read motion values in layout and draw** (`graphicsLayer {}`, `Modifier.layout`, `drawBehind`),
   so a motion doesn't recompose every frame.
8. **One material for a morph.** A button and the card it morphs into use the same background
   material (the same glass or fill): give `SpringMorph` the same material at both ends and let it
   change only shape, size and content. Two different materials would read as a swap, not a growth.

## Examples

A sheet you can drag closed:

```kotlin
val open = rememberMotionValue(initial = 0f, overMin = 0.02f, overMax = 0.1f)
LaunchedEffect(Unit) { open.animateTo(1f) } // container spring
Box(
    Modifier
        .fillMaxSize()
        .motionDrag(open, Orientation.Vertical, range = 400.dp, reverseDirection = true)
        .graphicsLayer { translationY = (1f - open.value) * size.height }
)
```

A card that grows out of a pill:

```kotlin
val value = rememberMotionValue()
MorphHost {
    Button(onClick = { value.animateTo(1f) }, Modifier.morphAnchor("pill", cornerRadius = 100.dp)) {
        Text("Open")
    }
    Spacer(Modifier.fillMaxWidth().height(200.dp).morphAnchor("card", cornerRadius = 24.dp))
    Morph(from = "pill", to = "card", value = value, surface = Modifier.background(Color.White)) {
        Text("Grown from the pill", Modifier.reveal(value, 0.35f, 0.7f))
    }
}
```

## Seeing it

Solstice's motion lab (Settings → Debug → Motion lab) shows every token and pattern, with slow
motion and a frame meter. Design notes: `docs/MOTION.md`; history: "Motion framework" in
`docs/PLAN.md`.
