# Solstice phone pass checklist

Use a current Solstice testing build on the Pixel 7 when available. Confirm the exact commit/build
first. Each item records app behavior to inspect; note failures in [CONTINUE.md](../CONTINUE.md) with
what happened. Do not silently change product behavior during a verification pass.

## Solstice interaction and motion

- [ ] Close a postcard window, the moon card, and search: each settles cleanly without flicker or
  content popping.
- [ ] Settle after a drag between places or pages: the motion lands on the intended item.
- [ ] Controls in Settings: press feedback is clear and never slower than before.
- [ ] Fades and blurs: no flicker or distracting overshoot.
- [ ] Enable Android's Remove animations/reduced-motion setting: reduce unnecessary movement while
  keeping content and state clear.
- [ ] Toolbar bud: recognizable and behaves as before.
- [ ] Toolbar selection: tapping during movement remains responsive and always reflects the selected
  page.
- [ ] Check RTL, TalkBack focus and actions, text scaling, touch targets, and contrast.

## Previously built app behavior to re-check

- [ ] Cold start refreshes the main place once, without a false "too frequent" message.
- [ ] Summary wording follows the approved copy; moon lines say "The moon will…".
- [ ] Swipe then tap a postcard: the tap works even immediately after a swipe.
- [ ] Reopen the moon card or a postcard while it closes: it reverses and reopens; taps do not close a
  window unexpectedly.
- [ ] Postcard window as a sheet: pull from the top to close, pull on the page to refresh, tap above to
  close.
- [ ] "Visiting" line scrolls with the page and is used only for previews.
- [ ] Dry-week headline reflects the temperature trend.
- [ ] Holding the open moon card does not close it; the moon pill keeps its intended surface.
- [ ] Moon icon tilt matches the modeled moon orientation on Forecast.
- [ ] Clear-night stars remain subtle and do not reduce text contrast.
- [ ] The window toolbar title folds, slides, and blurs without overlapping the page heading.
- [ ] Haptics remain supplementary and occur only at approved moments.
- [ ] Settings, details, and management icons follow the chosen Material Symbols style.

## Performance re-check

- [ ] Measure janky frames opening Search and the moon card on a release-like build. Record the
  method, device, build SHA, and before/after numbers in CONTINUE.md.
- [ ] If frame drops remain material, capture one focused Perfetto trace and attach the findings to
  the relevant app bug.

## Known app bugs to confirm

From ROADMAP.md's app interaction review: tapping after a swipe (B3), jumping across toolbar tabs
(B4), and distracting text blur during content changes (B5). Record whether each still occurs.
