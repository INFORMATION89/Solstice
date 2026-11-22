/*
 * This file is part of Solstice, a fork of Breezy Weather.
 *
 * Solstice is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Solstice is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Solstice. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.expressive.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.breezyweather.ui.expressive.theme.Opacity
import org.breezyweather.ui.expressive.theme.StrokeWidth

/**
 * Frosted glass for overlays and cards: the content behind is blurred, tinted and edged with a fine
 * light line. Drawn by Haze (decided 2026-11-19 after measuring it against our own backdrop
 * recorder on the Pixel 7: the same speed and look, less code of our own). Haze also handles
 * Android versions without RenderEffect blur (a more opaque tint).
 */
@Stable
class GlassSource internal constructor(internal val haze: HazeState)

@Composable
fun rememberGlassSource(): GlassSource {
    val haze = rememberHazeState()
    return remember(haze) { GlassSource(haze) }
}

/**
 * The sky alone (no pages), for cards that sit on it: they blur the sky behind them, like real
 * frosted glass. Provided by the sky that draws it (the home sky, or a postcard window's own).
 */
val LocalSkyGlass = compositionLocalOf<GlassSource?> { null }

/** The whole screen a page is on (sky and pages), for sheets opened from it, like the moon card. */
val LocalPageGlass = compositionLocalOf<GlassSource?> { null }

/** Marks the content that glass elements should show, blurred, behind them. */
fun Modifier.glassSource(source: GlassSource): Modifier = this.hazeSource(source.haze)

/**
 * Solstice's one glass tint (agreed 2026-11-21): the theme's surface colour at [Opacity.Glass], so every
 * glass surface (toolbar, cards, pills, sheets, messages, search) is the same and goes dark with the
 * theme (dark mode, or a postcard window at night).
 */
@Composable
fun glassTint(): Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = Opacity.Glass)

/**
 * A frosted-glass background in [shape]: blurred backdrop + [tint] + a hairline light edge. Without a
 * [source] (nothing to blur, e.g. a plain page or inside a sheet), just the tint.
 *
 * [alpha] (read every frame) fades the glass in or out, e.g. while a button's look becomes a
 * sheet's. It fades the whole element, so content inside that must stay visible shouldn't rely on
 * it (the moon card's content only appears after its glass has fully faded in).
 */
@Composable
fun Modifier.glass(
    source: GlassSource?,
    shape: Shape,
    tint: Color = glassTint(),
    blur: Dp = 28.dp,
    edge: Color = Color.White.copy(alpha = 0.22f),
    alpha: (() -> Float)? = null,
): Modifier {
    val faded = if (alpha != null) this.graphicsLayer { this.alpha = alpha().coerceIn(0f, 1f) } else this
    if (source == null) {
        return faded.clip(shape).background(tint).border(StrokeWidth.Hairline, edge, shape)
    }
    val style = remember(tint, blur) {
        HazeBlurStyle {
            blurRadius(blur)
            colorEffects(listOf(HazeColorEffect.tint(tint)))
            // No blur (older Android versions): a more opaque tint instead.
            fallbackColorEffect(HazeColorEffect.tint(tint.copy(alpha = maxOf(tint.alpha, 0.92f))))
        }
    }
    return faded
        .clip(shape)
        .hazeBlur(HazeInput.Sources(source.haze), style, performanceMode = HazePerformanceMode.Balanced)
        .border(StrokeWidth.Hairline, edge, shape)
}
