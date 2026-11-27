# Solstice morph interaction study

**Research date:** 2026-11-21  
**Scope:** Solstice's desired Android app experience only.  
**Status:** design research, not an implementation specification.

This study records interaction lessons from Apple's public design sessions and translates them into
Solstice-specific UX requirements. It does not define a generic animation framework, public API,
module, reusable component library, lab, or implementation plan for an external dependency. Solstice
owns its desired appearance and behavior; generic mechanics may be consumed only through stable,
documented APIs reviewed under [INTEGRATIONS.md](INTEGRATIONS.md).

## 1. Interaction lessons

Sources reviewed include WWDC18 "Designing Fluid Interfaces", WWDC23 "Animate with springs" and
"Wind your way through advanced animations", WWDC24 "Enhance your UI animations and transitions",
and the WWDC25/26 sessions listed below.

**Continuous behavior:** an interaction should remain ready to respond to the next input. It should
not feel like a sequence of unrelated timed animations.

**Velocity continuity:** a drag follows the finger directly. Releasing it should preserve the
gesture's direction and momentum, rather than starting an unrelated movement from rest.

**Interruptibility:** the user should be able to catch, reverse, or redirect an opening or closing
transition. A second action should not be swallowed while the first animation is settling.

**Source continuity:** a sheet, menu, or expanded card should remain visually connected to the
control that opened it. Closing should retrace that relationship instead of dismissing to an
unrelated location.

**Choreography:** container shape, position, content, and text do not all need to change at exactly the
same rate. Content should remain readable, and the label-to-heading handoff should not show two
competing pieces of text at once.

**Material continuity:** a control and the surface it becomes should read as one object. Material,
shape, and elevation changes should feel intentional rather than like two unrelated layers crossfading.

**Reduced motion:** reduce unnecessary travel, stretch, overshoot, blur, and decorative effects while
keeping the state change and direct manipulation understandable.

## 2. Solstice experience to preserve

- **Moon card:** the moon control and its expanded card remain connected throughout opening,
  dragging, interruption, and closing.
- **Search:** the search control and its expanded surface remain visually related; the input remains
  responsive while the surface settles.
- **Place windows:** opening a place from its postcard should preserve the postcard as the visual
  source. Pull-to-close and predictive back should remain reversible.
- **Toolbar:** the current animated toolbar and bud remain recognizable and app-owned. Their
  selection state must always match the selected page.
- **Page movement:** swipes and tab taps must not flash unrelated intermediate pages, trap input, or
  obscure which page is active.
- **Text replacement:** labels and headings must hand over clearly. A blur-replace or another approved
  treatment may be used, but old and new text must not overlap as two readable labels.
- **Cards and sheets:** content should feel carried by the surface rather than simply revealed through
  a growing rectangular hole.

## 3. Solstice acceptance questions

Before changing an app interaction, check:

1. Does it follow the finger and preserve a natural release?
2. Can the user interrupt, reverse, or redirect it?
3. Does the opening element remain visually connected to its destination?
4. Is the selected page/action clear throughout the movement?
5. Do labels, icons, and body content remain readable?
6. Does reduced motion preserve the meaning and responsiveness of the interaction?
7. Are touch targets, TalkBack actions, focus, RTL, and text scaling still correct?
8. Does it remain smooth on a physical Android device?

This research does not authorize a visible change. App-side refinements require agreement under
[RULES.md](RULES.md), should be recorded in [PLAN.md](PLAN.md), and belong in [ROADMAP.md](ROADMAP.md)
only when approved and still needed.

## Sources

- [WWDC18 — Designing Fluid Interfaces](https://developer.apple.com/videos/play/wwdc2018/803/)
- [WWDC18 transcript](https://asciiwwdc.com/2018/sessions/803)
- [WWDC23 — Animate with springs](https://developer.apple.com/videos/play/wwdc2023/10158/)
- [WWDC23 — Wind your way through advanced animations](https://developer.apple.com/videos/play/wwdc2023/10157/)
- [WWDC24 — Enhance your UI animations and transitions](https://developer.apple.com/videos/play/wwdc2024/10145/)
- [WWDC25 — Meet Liquid Glass](https://developer.apple.com/videos/play/wwdc2025/219/)
- [WWDC25 — Get to know the new design system](https://developer.apple.com/videos/play/wwdc2025/356/)
- [WWDC25 — Build a SwiftUI app with the new design](https://developer.apple.com/videos/play/wwdc2025/323/)
- [WWDC25 — Build a UIKit app with the new design](https://developer.apple.com/videos/play/wwdc2025/284/)
