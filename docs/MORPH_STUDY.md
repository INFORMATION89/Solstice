# Morph study: how iOS does it, and why ours doesn't feel like it

Written 2026-11-21 at the user's request ("understand how it's made before implementing"). This is
research and diagnosis only. Nothing here is built yet; the proposal at the end needs the user's OK
(MOTION.md: motion is agreed before it's built).

## 1. How Apple builds fluid morphs

From Apple's own talks: WWDC18 "Designing Fluid Interfaces", WWDC23 "Animate with springs" and
"Wind your way through advanced animations", and WWDC24 "Enhance your UI animations and
transitions".

**Motion is a behaviour that's always running, not a timed animation.** WWDC18 contrasts timed
"call and response" animations with continuous behaviours that are always running and ready to go
somewhere else. Like a braking car, the curve has no breaks: you can't tell exactly when it stopped.

**Springs, never curves, because of velocity.** An ease curve can't start with an initial velocity; a
spring can, and it's the only animation that stays continuous both from rest and from a moving
start. When retargeted, a spring uses its current velocity towards the new destination. That is why
interrupting feels smooth.

**Defaults.** Apple's general-purpose spring is **bounce 0** (critically damped, "smooth"). About 15 %
bounce is "a little more brisk", about 30 % "noticeably bouncy", and anything above about 0.4 is too
much for UI. The parameter is a *perceptual* duration (about 0.5 s for SwiftUI's presets), not the
time it takes to settle.

**Damping follows the gesture.** In WWDC18, Now Playing *presented by a tap* uses **100 % damping**
(no overshoot). *Dismissed by a swipe*, it uses **80 %** "to have a little bit of bounce and squish".
The rule: if the gesture driving the motion has momentum, reward it with a little overshoot.
Otherwise use no bounce.

**Gestures hand off to springs.** Content tracks the finger one to one, keeping the grab offset.
Letting go **projects** where the motion would land (velocity and deceleration rate) and picks the
nearest end to that projected point, not to the current position. The spring then starts with the
finger's velocity. UIKit retargets an interactive spring every frame during the drag and ends with a
normal spring that inherits that velocity, with no manual velocity maths.

**Interruptible at any time, and never "cancelled".** The WWDC24 zoom transition is "continuously
interactive": you can grab it at the start or mid-way. An interrupted push is never cancelled; it
completes immediately and becomes a pop. Exits retrace their entrances (spatial consistency).

**Morphing from a source.** The tapped cell morphs into the incoming view (zoom transition); the app
icon "elastically stretches" into the app as it opens and back as it closes (motion stretching);
Control Center modules grow towards the finger, then pop open.

**Different properties move on different springs.** App launch is described as "different springs,
different start times, and different end times" coming together. Properties driven by springs
finishing at different times is intended. It is *not* one progress value lerping everything.

**It's what's in the frames, not just the frame rate.** WWDC18 says smoothness comes from the content
of each frame too (motion blur and stretching against strobing). And on iOS these animations run in
the render server as layer transforms: the view isn't re-laid out each frame.

## 2. Why ours feels linear, stiff and laggy

This comes from reading the code (`:motion` `Morph.kt`, `MotionValue.kt`, the moon sheet in
`MoonCard.kt`). Nothing was measured, as the user asked.

1. **One number drives everything, linearly.** `MorphGeometry.frame` lerps left, top, right, bottom
   and the corner radius by the same `p`. The only spring is on `p`, so every edge moves in lockstep
   along straight lines. The result is a uniform rectangle zoom with no stretch, lead or follow. That
   is the "linear" look, even though `p` itself is a spring. On iOS, position, size and corners
   settle at different times.
2. **The close never uses momentum, and starts from rest.** Back or a tap closes with
   `animateTo(0f)`, which defaults to `Motion.dismiss` (critically damped) from velocity 0. Over
   ~1700 px that's a long, even slide with no character. Apple's tap close is also bounce 0, but
   with its stretch and per-property timing it doesn't read as a plain ramp.
3. **The choreography windows are linear ramps.**
   - `MotionValue.window()` is a straight line from 0 to 1.
   - The label hand-off (0–15 %), and the style (0–30 %) apart from its smoothstep, are linear in
     `p`.
   - The label and the heading cross-fade, so mid-way two readable labels overlap. That's visible in
     the recordings.
4. **Every frame re-lays out a full-screen surface.** `Morph` measures the surface at a new size each
   frame (`Constraints.fixed(w, h)`). The glass (Haze) then re-blurs a backdrop region that changes
   size every frame, and the scrim, the moon shader and the stars are drawn inside it. That's heavy
   per-frame work (the lag), where iOS animates a layer transform.
5. **The content is clipped, not carried.** The sheet's content is laid out at full size and
   uncovered by the growing clip, so it reads as a window being dragged open, not an object growing.
   iOS's zoom scales the destination with the container.
6. **The ends are abrupt.** The style and label windows end exactly at 0 or 1. Closing, the pill's
   outline and label pop back in the last 15–30 % of the way, and the swap happens right as the
   motion lands.

## 3. Proposal (to agree before building)

A rework of `:motion`'s `Morph` and the morph tokens, generic for any app:

1. **Separate springs per property, all retargetable with velocity.** Instead of one `p`, the morph
   has springs for **position (centre)**, **size** and **corner radius**:
   - Size trails position slightly on open and leads on close, for the stretch.
   - Each keeps its velocity when interrupted or retargeted.
   - A gesture still drives it: during a drag all of them follow the finger (as now). On release,
     each springs on with the projected target and the finger's velocity.
2. **Apple's damping rule as tokens:**
   - opened or closed by a tap or back: bounce 0;
   - released from a gesture with momentum: about 15–20 % bounce (Apple's 80 % damping);
   - perceptual durations around 0.45–0.5 s;
   - `Motion` tokens for all of these, not values in screens.
3. **Transform-based rendering:**
   - The surface is laid out once at the target size and moved and scaled with `graphicsLayer`.
   - The clip shape carries the corner radius, corrected for the scale.
   - No relayout per frame.
   - The glass samples once per frame at the drawn bounds, or samples a frozen backdrop while the
     morph runs (to measure later).
4. **Carry the content.** The content scales with the container from the source's size, with no
   window reveal, and fades in under the content spring.
5. **Text never overlaps readably.** The label and the heading hand over with a **blur replace**: the
   old one blurs out as the new one sharpens, as one travelling text. This is point 2 of the morph
   refining plan, done here because it's part of the same motion.
6. **Exits retrace entrances,** the same springs in reverse, with the source's look returning *as* it
   lands rather than in a separate window.
7. **The lab first:** a "Morph" demo with the per-property springs, their tokens, slow motion and
   tap, drag and flick, compared side by side with today's morph, before the moon pill uses it.

Sources:
[WWDC18 Designing Fluid Interfaces](https://developer.apple.com/videos/play/wwdc2018/803/) ·
[transcript](https://asciiwwdc.com/2018/sessions/803) ·
[WWDC23 Animate with springs](https://developer.apple.com/videos/play/wwdc2023/10158/) ·
[WWDC23 Wind your way through advanced animations](https://developer.apple.com/videos/play/wwdc2023/10157/) ·
[WWDC24 Enhance your UI animations and transitions](https://developer.apple.com/videos/play/wwdc2024/10145/)

## 4. iOS 26/27: blur and morphing (research, 2026-11-21)

From Apple's WWDC25 sessions "Meet Liquid Glass" (219), "Get to know the new design system" (356),
"Build a SwiftUI app with the new design" (323) and "Build a UIKit app with the new design" (284);
WWDC24 10145; and WWDC26 269, 278, 322 and 250. Apple gives **no numbers** for glass morphs, menus,
sheets or blur. Everything below is qualitative, and our values are our own tuning.

**The control becomes what it opens.**
- "the button morphs into the overlay" (284).
- Sheets "directly morph out of buttons that present them", and dialogs too (323).
- A menu's "bubble simply pops open to reveal the content" (219).
- The search button "expands into a search field" while the others collapse (284).
- Dismissing retraces to the source; collapsing badges are "re-absorbed" into their button (323).

**Glass materializes, it doesn't fade.**
- "Instead of fading, Liquid Glass objects materialize in and out" (219), by modulating the light
  bending and lensing. Larger glass reads as thicker, with deeper shadows and stronger lensing.
- On Android we fake lensing with blur and tint, so the closest match ramps the glass in with the
  shape (the style window). Avoid plain alpha.

**Touch.**
- Interactive glass "scales and bounces" when tapped (284), reacts by "scaling, bouncing, and
  shimmering" (323), and "illuminates from within" under the finger (219).
- Elements can "lift up into Liquid Glass temporarily".
- Reduce Motion "disables any elastic properties".

**Interruption.**
- Transitions are "fluid and interruptible" (243).
- An interrupted push "is always converted into a pop" (10145): an interrupted opening turns into a
  closing, it's never cancelled.

**Blur.**
- During motion, blur mostly shows up as **scroll edge effects**: content under bars gets "a subtle
  blur and fade" (323) rather than a hard divider.
- The text "blur replace" transitions exist in SwiftUI, but none of the sessions we could read give
  their values (the documentation pages didn't load).

**iOS 27 (WWDC26):** no change to the morph mechanics was found. The new APIs are about toolbars and
tab bars minimizing on scroll, and tinting the glass.

**What this changed in `SpringMorph` (2026-11-21):**
- **One gesture:** a touch catches it (each spring stops where it is). Moving past the touch slop
  drags it; lifting without moving taps it back the other way. Before, the drag swallowed taps, so
  it couldn't be reopened mid-close.
- **Press:** under the finger the surface sinks 3 % (`MorphDefaults.PRESS_DEPTH`, snappy), and
  springs back with a little bounce (playful). iOS 26's interactive glass.
- **Speed limit:** the release velocity handed to the springs is capped at 4 path/s, so a hard flick
  never breaks the morph.
- **Stronger blur replace:** 20 dp, was 8. **More bounce opening:** `Motion.brisk` 0.78, was 0.85.

Sources: [WWDC25 219](https://developer.apple.com/videos/play/wwdc2025/219/) ·
[WWDC25 356](https://developer.apple.com/videos/play/wwdc2025/356/) ·
[WWDC25 323](https://developer.apple.com/videos/play/wwdc2025/323/) ·
[WWDC25 284](https://developer.apple.com/videos/play/wwdc2025/284/) ·
[WWDC25 243](https://developer.apple.com/videos/play/wwdc2025/243/) ·
[WWDC24 10145](https://developer.apple.com/videos/play/wwdc2024/10145/) ·
[WWDC26 269](https://developer.apple.com/videos/play/wwdc2026/269/) ·
[WWDC26 278](https://developer.apple.com/videos/play/wwdc2026/278/)
