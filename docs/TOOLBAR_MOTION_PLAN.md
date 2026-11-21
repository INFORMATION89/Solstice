# Apple's principles for the toolbar (proposal, 2026-11-21, for the user to agree)

What `docs/MORPH_STUDY.md` learned, applied to the toolbar (`shell/Toolbar.kt`, its values in
`ToolbarMotion`). Nothing changes until it's agreed (MOTION.md).

## Where it stands

- Each animation is a separate `Animatable` on a token spring: the bud's neck, wobble and ripple; the
  pin's jab; slide to switch's pop; the tab press. Their values live in `ToolbarMotion`, and the Motion
  Lab can tune them.
- The tab highlight and labels follow the pager's position (or `ShellNavigation`'s glide). They don't
  have springs of their own.
- **Slide to switch:** a long press, then a drag that scrolls the pager at a fixed ratio
  (`slideTravel`). On release it snaps to the nearest page, with no velocity carried and no
  projection.

## Proposal

1. **Velocity everywhere** (WWDC18). Slide to switch tracks the finger's velocity. On release it
   projects where the fling would land and picks that tab (`MotionPhysics.chooseTarget`, the UIKit
   rule), and the highlight springs there starting at the finger's speed, not from rest. A quick
   flick across the pill jumps two tabs, as a fling does on iOS.
2. **The highlight is its own surface, on its own springs.** Today its position and width are maths
   on the pager position. Instead it becomes a `:motion` morph between tabs: position and width on
   separate springs, the width trailing a little so it stretches as it moves (iOS's "motion
   stretching", and the iOS 26 tab bar's liquid selection). Tapping a tab mid-glide retargets with the
   current velocity, never from rest.
3. **Interactive glass on press** (iOS 26: glass "scales and bounces" under the finger): the whole
   pill sinks a little and springs back with a bounce, using the same press as `SpringMorph` from one
   `:motion` piece. The tab squeeze stays for the tab itself.
4. **The bud as a morph:** the search and pin buds pop out of the pill as a `SpringMorph`-style
   surface: the bubble is the pill's end grown out, not a separate shape. The neck, wobble and
   ripple become the morph's stretch and overshoot, on its springs, with no hand-tuned amounts.
5. **Damping follows the gesture** (WWDC18): a tap moves the highlight with no bounce
   (`Motion.smooth`); a release with momentum gets a slight overshoot (`Motion.momentum`); Reduce
   Motion removes the elastic parts.
6. **Never blocks input:** any touch on the toolbar catches what's moving (highlight, bud, pop) where
   it is.
7. **Haptics** once F1's haptics system exists: a soft tick per tab passed while sliding, and a light
   impact when the bud pops free.

## Order

Lab first, as for the morph: a generic tab bar demo built on the new pieces next to today's toolbar,
then the app's toolbar once it's agreed.
