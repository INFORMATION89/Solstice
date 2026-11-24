# motion-ui: the move (Saturday step 6)

Status: **plan** (2026-11-24), from reading Solstice's `ui/expressive/` at `main` and CONTINUE.md
"Decided in chat" 1–3 and 6. Pieces' behaviour: MOTION_UI.md. Card material: research/CARD_MATERIAL.md.

## The module

`motion/motion-ui/` in the Motion repo, Apache 2.0, depends on `:motion`, Material 3 and Haze; `:motion`
never depends on it. Package `io.github.information89.motion.ui`. Added to Motion's
`settings.gradle.kts` (today only `:motion` is included) and to Solstice's composite build.

## Tokens: names in the kit, values from the app

```kotlin
@Immutable class MotionUiTokens(val spacing: Spacing, val sizes: Sizes, val corners: Corners,
    val strokes: Strokes, val opacity: Opacity, val glass: GlassStyle, val card: CardStyle)
val LocalMotionUi = staticCompositionLocalOf { MotionUiTokens.Default }   // neutral, Material-based
@Composable fun MotionUiTheme(tokens: MotionUiTokens = MotionUiTokens.Default, content: @Composable () -> Unit)
```

Solstice passes today's values from `theme/Tokens.kt`, so nothing it shows changes. Values typed into
components today become tokens: `Glass.kt`'s blur 28 dp, edge white at 22 % and fallback tint at 92 %;
the handle's 32 × 4 dp; the sheet corners.

## What moves, in order (each builds and keeps the app looking the same)

| Order | From Solstice (`ui/expressive/…`) | To motion-ui | App links to cut |
|---|---|---|---|
| 1 | `theme/Tokens.kt` (names only), `SolsticeMotionScheme.kt` | `Tokens.kt`, `MotionUiTheme` | none; Solstice keeps its values in its own theme file |
| 2 | `components/Glass.kt` | `Glass.kt` | none (Haze moves with it) |
| 3 | `components/SolsticeCard.kt` | `MotionCard` | none; the card material is a token (picked in the Lab) |
| 4 | `components/ScreenCorners.kt`, `SheetShape.kt` | `ScreenCorners`, `SheetShape` | none |
| 5 | `components/SheetHandle.kt` | `SheetGrabber` | its content description string becomes a parameter |
| 6 | `components/SolsticeSheetState.kt`, `SheetDialog.kt` | `MotionSheetState`, `MotionSheet` (MOTION_UI.md section 1) | none |
| 7 | `shell/PageFrame.kt`: scaffold, header, `FrameAction`, `FrameButton`, top edge | `PageFrame` | "Close", "Settings" strings → parameters; `VisitingLine` and `Location` stay in Solstice |
| 8 | `components/PressMorph.kt`, `CollapsingTopBar.kt` | same names | none |

Stays in Solstice: `ExpressiveTheme.kt` and `ExpressiveType.kt` (they read Solstice's colour and font
resources; they now fill `MotionUiTokens`), `SolsticeIcons.kt`, `IconBadge.kt` (uses resources), `SolsticePalette` and `HeroType` (Solstice's own look,
in `Tokens.kt`), the
shell pages, sky, search, reorder.

Not in this move (later, each agreed first): `MotionMessage` (Messages.kt), settings rows
(`ExpressiveChoices.kt`), the toolbar.

## Checks after each step

- `./gradlew :app:assembleBasicDebug` and Motion's own build; no `org.breezyweather` import in
  motion-ui (a CI check).
- Screenshots of Forecast, Outlook, Places, a postcard window, search, the moon card and Settings,
  before step 1 and after step 8, in light and dark: no visible change except the card material if
  Marco picks a new one in the Lab.
- The CI no-hardcoding check (CONTINUE.md decision 7) runs on motion-ui from its first commit.

## Done when

Solstice imports every piece above from motion-ui and passes its values; the Lab uses the same pieces
(step 7).
