# Solstice motion

This document defines the interaction feel and motion behavior of the Solstice Android app. It does
not define or plan the internals, APIs, modules, lab, or roadmap of an external motion library.
Solstice owns its interaction design and decides which stable dependency APIs it consumes.

Any new or changed user-visible motion is proposed and agreed before implementation.

## Product principles

### 1. Fluid, interruptible movement

- Interactions should respond immediately and remain interruptible.
- Dragging follows the finger; release uses the gesture's direction and velocity where appropriate.
- A new gesture can catch an animation in progress. No animation should swallow an unrelated tap.
- Avoid linear/eased movement for physical interactions when spring behavior better expresses the
  gesture. Short, simple opacity or colour changes may use direct transitions.

### 2. Things morph; they do not pop

A sheet, card, menu, or window should retain a clear visual relationship to the control or content that
opened it. The opening element should feel like it grows into the destination, and closing should
restore that relationship. Do not introduce a surface from nowhere or replace connected content with
an unrelated fade.

### 3. One material through a morph

The source control and the surface it becomes should read as one continuous object. The transition may
change shape, size, elevation, and content; it should not create an unnecessary material crossfade.

### 4. Choreographed content

Content should appear when the container can support it. Text and icons should not be distorted while
a surface changes size. Closing content should leave before the container collapses where this makes
the transition clearer.

### 5. Responsive input and gesture ownership

- Input remains responsive during page changes, sheets, and morphs.
- Every gesture has one clear owner; nested scrolling and child interactions must continue to work.
- A touch may interrupt, reverse, or redirect an animation.
- The app must not rely on an animation completing before an action can be taken.

### 6. Reduce Motion and accessibility

Respect Android's system animation and reduced-motion settings. A reduced form should preserve the
meaning and continuity of the interaction while reducing spatial travel, overshoot, blur, and
decorative effects. Direct manipulation should still follow the finger. Haptics are supplementary;
they are never the only feedback.

## Solstice-specific behavior

- **Toolbar:** the current animated toolbar and its bud are part of Solstice's product identity.
  Keep their behavior and recognizable appearance unless a product decision explicitly changes them.
- **Moon card:** the moon button and card remain visually connected through opening and closing.
- **Search and place windows:** retain their source relationship and responsive pull/back behavior.
- **Places:** reorder gestures must remain predictable, interruptible, and accessible.
- **Messages:** actions and timing must remain accessible and must not disappear before assistive
  technology users can act on them.

## Implementation

Solstice uses its own design decisions and tokens and may consume generic interaction primitives
from pinned dependencies. This document does not prescribe a dependency's implementation or public
API. Check [INTEGRATIONS.md](INTEGRATIONS.md) before changing a dependency pin, and consult the
dependency's own documentation for APIs actually present at that exact revision.

When an app-side interaction changes, add focused regression coverage and update the Solstice
roadmap. Check the result on a physical Android device when available; otherwise record NOT TESTED.
