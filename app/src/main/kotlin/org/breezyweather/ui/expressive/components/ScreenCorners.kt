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

import android.os.Build
import android.view.RoundedCorner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * The screen's own corner radii in pixels (agreed 2026-11-21): a surface whose corners meet the screen
 * edges uses them, so it lines up with the glass, never clipped and never showing what's behind.
 * Read from Android's `RoundedCorner` API (Android 12+); 0 (square) where Android can't tell (agreed:
 * on Android 10–11, and on screens without rounded corners).
 */
@Immutable
internal data class ScreenCorners(val top: Float, val bottom: Float)

@Composable
internal fun rememberScreenCorners(): ScreenCorners {
    val view = LocalView.current
    return remember(view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@remember ScreenCorners(0f, 0f)
        val display = view.display ?: return@remember ScreenCorners(0f, 0f)
        fun radius(position: Int) = display.getRoundedCorner(position)?.radius?.toFloat() ?: 0f
        ScreenCorners(
            top = maxOf(radius(RoundedCorner.POSITION_TOP_LEFT), radius(RoundedCorner.POSITION_TOP_RIGHT)),
            bottom = maxOf(radius(RoundedCorner.POSITION_BOTTOM_LEFT), radius(RoundedCorner.POSITION_BOTTOM_RIGHT))
        )
    }
}
