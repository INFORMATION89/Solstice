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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import org.breezyweather.ui.expressive.theme.Sizes
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A clock icon showing a real time (agreed 2026-11-21): drawn to match Material Symbols Rounded
 * "schedule" (weight 400, optical size 20, as [SolsticeIcons.Small.Schedule]), only its hands point at
 * [hour]:[minute]. Every proportion is measured from that symbol's path on its 960-unit grid, so it
 * sits with the other small symbols.
 */
@Composable
internal fun ClockIcon(hour: Int, minute: Int, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(Sizes.IconSmall)) {
        val unit = size.minDimension / SymbolGrid.SIZE
        val stroke = SymbolGrid.STROKE * unit
        val centre = center
        drawCircle(tint, radius = SymbolGrid.RING * unit, center = centre, style = Stroke(stroke))
        fun hand(turns: Float, length: Float) {
            val angle = turns * 2 * PI - PI / 2
            val end = Offset(
                centre.x + cos(angle).toFloat() * length * unit,
                centre.y + sin(angle).toFloat() * length * unit
            )
            drawLine(tint, centre, end, strokeWidth = stroke, cap = StrokeCap.Round)
        }
        hand(((hour % 12) + minute / 60f) / 12f, SymbolGrid.HOUR_HAND)
        hand(minute / 60f, SymbolGrid.MINUTE_HAND)
    }
}

/** Material Symbols' "schedule" (Rounded, weight 400, opsz 20) measured on its 960-unit grid. */
private object SymbolGrid {
    const val SIZE = 960f

    /** The ring's centre line: outer radius 384, inner 312. */
    const val RING = 348f

    /** The ring's and the hands' thickness (384 − 312). */
    const val STROKE = 72f

    /** The long hand, centre to the middle of its rounded end. */
    const val MINUTE_HAND = 204f

    /** The short hand, centre to the middle of its rounded end. */
    const val HOUR_HAND = 178f
}
