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
 * "schedule" (optical size 20, at the icons' [SolsticeIcons.WEIGHT]), only its hands point at
 * [hour]:[minute]. Its proportions ([ScheduleGeometry]) are measured from that symbol by the icon
 * generator, so it follows the icons' weight.
 */
@Composable
internal fun ClockIcon(hour: Int, minute: Int, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(Sizes.IconSmall)) {
        val unit = size.minDimension / ScheduleGeometry.GRID
        val stroke = ScheduleGeometry.STROKE * unit
        val centre = center
        drawCircle(tint, radius = ScheduleGeometry.RING * unit, center = centre, style = Stroke(stroke))
        fun hand(turns: Float, length: Float) {
            val angle = turns * 2 * PI - PI / 2
            val end = Offset(
                centre.x + cos(angle).toFloat() * length * unit,
                centre.y + sin(angle).toFloat() * length * unit
            )
            drawLine(tint, centre, end, strokeWidth = stroke, cap = StrokeCap.Round)
        }
        hand(((hour % 12) + minute / 60f) / 12f, ScheduleGeometry.HOUR_HAND)
        hand(minute / 60f, ScheduleGeometry.MINUTE_HAND)
    }
}
