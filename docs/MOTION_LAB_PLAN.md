# Motion Lab redesign (plan, 2026-11-21, for the user to agree)

The lab today is a developer screen: a long list of sections with token names, a frame meter and
notes that point at Solstice screens. `:motion` is now a **universal library of animations and
interaction components**, so the lab becomes its showroom and test bench, for anyone, with no
weather app in it.

## Goals

1. **Anyone can see the difference** in seconds: no jargon on the first screen, one thing per screen,
   big touch targets, and everything to try with a finger.
2. **A side-by-side comparison with Google's defaults** (Compose and Material motion) for every
   pattern, so the difference `:motion` makes is obvious.
3. **A test bench** for us: slow motion, a frame meter, live controls, but tucked away.
4. **No references to the weather app:** generic content (cards, a photo grid, a music player-style
   sheet, settings toggles), and its own name and icon ("Motion Lab").

## Structure

- **Home:** a gallery of patterns as large tiles, each a tiny live preview. Tapping a tile morphs it
  into its page with `SpringMorph`, so the lab itself shows the library off.
- **One page per pattern:**
  - Morph: button into card, pill into sheet.
  - Sheet: detents, pull to dismiss, catch mid-flight.
  - Menu out of a button.
  - Page change and tab bar.
  - Toolbar: generic tabs, no Forecast or This week.
  - Drag to reorder, swipe actions.
  - Press and controls: button press, toggle, switch, slider.
  - Text and numbers: blur replace, rolling digits.
  - Scroll effects: edge blur, collapsing title.
  - Haptics.
- **On each page:**
  - **The demo,** full width, with real content.
  - **A "Compare" switch:** the same demo split in two, *Motion* above and *Google default* below,
    driven by the same tap, so both run together. The Google side uses what a Compose developer gets
    out of the box (e.g. `AnimatedVisibility`, `SharedTransitionLayout`'s default bounds transform,
    `ModalBottomSheet`, `animateContentSize`, default `spring()`/`tween()`), with no tuning.
  - **One line** saying what to notice ("Grab it while it's moving", "Flick it", "Tap it twice
    quickly").
  - **"Tune" (collapsed):** the springs and amounts, with the tokens by name and a reset.
- **Tools, always reachable:** slow motion (2×, 5×, 10×), "show touches", the frame meter, and Reduce
  Motion preview.

## Steps

1. Generic content and naming; remove Solstice-specific demos (the toolbar demo becomes a generic
   tab bar built on the same `:motion` pieces).
2. The gallery home and page scaffold, with a morph from tile to page.
3. The compare switch, starting with Morph, Sheet and Page change.
4. Move the existing demos (tokens, release, morph, toolbar) onto pages.
5. Later: the lab as a separate sample app module (`:motion-lab`) next to `:motion`, so the library
   ships with its own showroom.
