# motion-ui: sheet, message and settings rows

Status: **spec, clean room step 1** (2026-11-23). Written from Material 3's public guidelines (bottom
sheets, snackbars, lists, spacing), Apple's Human Interface Guidelines (sheets, alerts, toggles,
accessibility) and Solstice's decisions (PLAN.md, MOTION.md, CONTINUE.md "Decided in chat"). The
build session works from this file and today's Solstice pieces it replaces.

Rules for all three: names and behaviour in the kit, values from the app's theme (no typed-in numbers
in components); Reduce Motion built in; every gesture has a TalkBack equivalent; taps are never
blocked by an animation.

## 1. Sheet (`MotionSheet`)

**What it is.** One sheet with two heights, growing out of what was tapped (morph, never pop).

| | Behaviour |
|---|---|
| Heights | `medium` (theme, default 50 % of the screen) and `large` (theme, default the screen minus the status bar and a top gap). The app can turn `medium` off for one sheet (opens large) |
| Opening | morphs from the source's bounds (`morphSource`) with `container`; the page behind dims (theme scrim) and doesn't recede (dropped 2026-11-21) |
| Grabber | always shown. Drag resizes; **tap cycles** medium ↔ large; TalkBack actions "Expand", "Collapse", "Close" |
| Drag | follows the finger 1:1; release picks the height from position and speed (`settle`); past the bottom height with downward speed, closes (`dismiss`) |
| Scroll | content scrolls only at large; at medium, an upward scroll expands first (Apple's behaviour); at the top of the content, a pull down shrinks then closes |
| Close | swipe down, tap the scrim, back gesture, or ✕ at the end of the header (decided exception) |
| One at a time | a sheet never opens another sheet; deeper content replaces the sheet's own content (with a back arrow) |
| No Done button | settings inside apply at once |
| Reopen while closing | a tap on the source reverses the close from where it is |
| Material | glass (theme `glass`), screen-matched top corners (theme `sheetCorner`, follows the device's display corner when known) |
| Reduce Motion | opens and closes as a quick fade in place (`effects.default`), height changes jump with a fade, dragging still follows the finger |
| Keyboard | a text field inside moves the sheet to large |

**API sketch**

```kotlin
@Composable fun MotionSheet(
    state: MotionSheetState,               // presence + height, rememberMotionSheetState()
    source: MorphSource?,                  // what it grows from; null = from the bottom edge
    heights: SheetHeights = SheetHeights.Default,
    onClose: () -> Unit,
    header: @Composable SheetHeaderScope.() -> Unit = {},
    content: @Composable SheetScope.() -> Unit,
)
```

Replaces: `SolsticeSheet`/`SolsticeSheetState`, `SheetDialog`, the postcard and moon sheet handles.

## 2. Message (`MotionMessage`)

**What it is.** A short, temporary message at the bottom (Material's snackbar), on glass, above the
toolbar. Replaces Solstice's own snackbar host and its one-off drag (UI_SYSTEMS.md).

| | Behaviour |
|---|---|
| Content | one line of text, up to two if needed; one optional action (text button); no icon unless it carries meaning |
| Appears | grows from the toolbar (`container`), never covers it |
| Time | theme: 4 s without action, 8 s with one (Material: about 4–10 s); **stays** while touched or while TalkBack is on (announced politely, with its action reachable) |
| Dismiss | swipe down or sideways (same motion value as the sheet), or the action |
| One at a time | a new message replaces the current one with a crossfade; messages queue only when they carry an action |
| Not for | errors that need a decision (those are a sheet or a dialog) or anything the person must not miss |
| Reduce Motion | fades in and out in place |
| Haptic | none by default; the app may pass `Haptic.confirm` for an action's result |

```kotlin
class MotionMessageHost { suspend fun show(text: String, action: MessageAction? = null): MessageResult }
@Composable fun MotionMessages(host: MotionMessageHost, modifier: Modifier = Modifier)
```

## 3. Settings rows

**What they are.** The rows every settings page is made of (Settings has nine sections, PROPOSALS.md).

| Row | Use | Behaviour |
|---|---|---|
| `SwitchRow` | on/off that applies at once | whole row toggles; switch on the end; the row reads "Label, on/off, switch" |
| `ChoiceRow` | one of a few (≤ 5) | shows the current choice under the label; tap opens a sheet (medium) with radio rows; picking applies and closes |
| `SegmentRow` | one of 2–3 short choices | Material segmented button in the row (units: °C / °F) |
| `SliderRow` | a value in a range | label, value text on the end, slider below; steps from the app; haptic tick on each step (`Haptic.tick`) |
| `LinkRow` | goes to a page | chevron on the end; trailing current value when helpful |
| `InfoRow` | read only | label and value, selectable text |
| `ActionRow` | does something (clear data) | text in the theme's action colour; destructive ones ask first in a sheet |
| `SectionHeader` | groups rows | small label above a card of rows |

Common: minimum height 56 dp (theme; Material list item), touch target ≥ 48 dp, optional leading icon
(Material Symbols, one weight), supporting text under the label in the secondary text colour; rows of a
section sit on one card (theme `card` material) with dividers inset; a disabled row says why in its
supporting text. Press feedback: the `press` spring, no ripple bloom across the card.

Search in Settings: not part of this spec (open question with Marco).

## Tests (in the Motion Lab and unit tests)

- Sheet: tap the grabber cycles heights; TalkBack actions work; fast drag down closes from large;
  reopen while closing reverses; Reduce Motion fades.
- Message: stays while touched; replaced by the next; swipe dismisses; TalkBack reads it once.
- Rows: each row type in light, dark and largest font size; touch targets measured.

## Consulted

Material 3: bottom sheets, snackbar, lists, switch, segmented buttons, sliders, spacing. Apple HIG:
sheets (detents, grabber), alerts, toggles, accessibility (Reduce Motion). Solstice's own docs.
