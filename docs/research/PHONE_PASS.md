# Phone pass checklist (Saturday step 9)

On the Pixel 7, profile build (`./gradlew :app:assembleBasicProfile`, never `-Pbreezy`), after the
Motion changes. Each line: what to do, and what "passes" looks like. Note anything that fails in
CONTINUE.md with what you saw; don't fix during the pass.

## Springs (Saturday's change)

- [ ] **Close a postcard window, the moon card and search:** each settles with a hint of bounce, no
  flicker, nothing popping.
- [ ] **Settle after a drag** (swipe between places, let go halfway): a hint of bounce, lands on a page.
- [ ] **Controls** (switches, choice buttons in Settings): a small bounce, never slower than before.
- [ ] **Fades and blurs** (text cross-fades, the toolbar's colour): no bounce at all, no flicker.
- [ ] **Reduce Motion on** (Settings › Accessibility › Remove animations): no bounce anywhere; fades
  stay; nothing jumps.
- [ ] **Toolbar bud:** still wobbles as before.
- [ ] **Motion Lab, subtle vs none switch:** both feel right on every demo; note which demos feel off.

## Built 2026-11-21, never checked

**App**
- [ ] **Cold start refreshes the main place:** force-stop, open; it updates once. Pass: one refresh, no
  "too frequent" snackbar.
- [ ] **Summary texts:** "No rain expected this week", "You'll need an umbrella on …", "It will rain on
  … days this week", "The coldest night will be tonight, at …"; moon lines say "The moon will…".
- [ ] **Swipe then tap a postcard:** the tap opens it even right after a swipe (bug B3 if not).
- [ ] **Reopen the moon card or a postcard while it closes:** it reverses and reopens; taps never close a
  window.
- [ ] **Postcard window as a sheet:** pull from the top to close, pull on the page to refresh, tap above
  to close.
- [ ] **"Visiting" line** scrolls with the page.
- [ ] **A dry week's headline** is the temperature trend.
- [ ] **Holding the open moon card** doesn't close it; the moon pill has its glass background.
- [ ] **Moon icon tilt** matches the real moon on Forecast.
- [ ] **Stars on a clear night** (home and window).
- [ ] **The window's toolbar:** title folds, slides and blurs.
- [ ] **Haptics** at the same moments as before.
- [ ] **Icons** in Settings, details and Management are Material Symbols, one weight.

**Motion Lab**
- [ ] "Morph, iOS way (new)": reopen while closing, text blur, press, speed limit, bounce.
- [ ] Haptics panel: feel every token.
- [ ] Edge blur, Dial (drag, fling, ends, haptics, curvature), Toolbar.

## Performance (re-measure)

- [ ] **Janky frames opening search and the moon card:** `adb shell dumpsys gfxinfo
  io.github.information89.solstice.profile reset`, then 6 opens and flick closes each, then `dumpsys gfxinfo`. Before:
  search 8 %, moon card 21 %. Pass: lower than before; write the numbers in CONTINUE.md.
- [ ] If the moon card is still above 10 %: one Perfetto trace of an opening, for the next session.

## Known bugs (don't fix in the pass, just confirm)

From ROADMAP.md "Review 2026-11-22": B3 (tapping after a swipe stops the tabs), B4 (jumping across the
toolbar flashes Forecast), B5 (both texts blur at once on a text change). Note whether each still happens after the spring change.
