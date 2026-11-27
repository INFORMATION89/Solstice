# Solstice toolbar motion — design proposal

**Status:** proposal for product review; no implementation change is authorized by this document alone.

The toolbar in `shell/Toolbar.kt` and its app-side interaction state in `ToolbarMotion` are part of
Solstice's product identity. The pages pill is navigation; the accessory action changes with context
(for example, Search on the home screen and Pin in a place window). Preserve the existing bud,
wobble, and toolbar animation unless the product owner explicitly approves a change.

## Current interaction questions

- The tab highlight and labels follow the current page/pager position.
- A long-press-and-drag gesture may be used to move between tabs; it should follow the finger and
  settle on the intended page on release.
- A tap during movement must remain responsive and should not be swallowed by an animation.
- The accessory bud should remain visually attached to the toolbar pill as it appears or changes.

## Design principles to evaluate

1. **Velocity continuity:** a drag should carry a natural sense of momentum into the selected tab.
   A quick gesture may travel farther than a slow drag, but should not skip pages unexpectedly.
2. **Continuous selection geometry:** the selection indicator should move and resize as one coherent
   surface. A retargeted gesture should continue from its current position rather than restarting.
3. **Press feedback:** pressing the toolbar may produce a restrained scale/shape response that
   remains legible and does not interfere with the tab selection.
4. **Bud continuity:** Search and Pin should feel like accessories that grow from the toolbar's end,
   not unrelated floating shapes.
5. **Reduced motion:** remove unnecessary travel, elastic stretch, and decorative blur while keeping
   the active page and action clear.
6. **Haptics:** any additional tactile feedback is optional and requires explicit product approval.
   It must not be the only indication that an action occurred.

## Review checklist

- Does the current toolbar behavior remain recognizable?
- Can the user interrupt movement, tap another page, or use back without waiting?
- Does the indicator always identify the actual selected page?
- Do the bud and accessory actions remain visually and semantically clear?
- Are touch targets, TalkBack actions, focus, RTL, and reduced motion supported?
- Does the result remain smooth on a physical Android device?

This document defines only Solstice's desired toolbar experience. It does not specify how an external
library implements animation primitives, and it does not assign tasks to another repository.
