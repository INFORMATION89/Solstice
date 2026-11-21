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

package org.breezyweather.ui.expressive.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import org.breezyweather.R

/** Plain outline moon-phase icons (Weather Icons, SIL OFL; see third_party/weather-icons), 8 steps. */
@Composable
internal fun rememberMoonPhaseIcons(): List<Painter> = listOf(
    painterResource(R.drawable.moon_phase_0),
    painterResource(R.drawable.moon_phase_1),
    painterResource(R.drawable.moon_phase_2),
    painterResource(R.drawable.moon_phase_3),
    painterResource(R.drawable.moon_phase_4),
    painterResource(R.drawable.moon_phase_5),
    painterResource(R.drawable.moon_phase_6),
    painterResource(R.drawable.moon_phase_7)
)

/** Draws the phase icon closest to [angle] (0/360 new, 180 full) centred at [center], [r] wide. */
internal fun DrawScope.drawMoonPhaseIcon(
    icons: List<Painter>,
    center: Offset,
    r: Float,
    angle: Int,
    tint: Color,
    alpha: Float = 1f,
    // Degrees clockwise: the moon as it really looks in the sky (MoonSky.iconRotation).
    rotation: Float = 0f,
) {
    val i = (((angle % 360) + 360) % 360 + 22) / 45 % 8
    val side = r * 2f
    rotate(rotation, pivot = center) {
        translate(center.x - r, center.y - r) {
            with(icons[i]) { draw(Size(side, side), alpha = alpha, colorFilter = ColorFilter.tint(tint)) }
        }
    }
}
