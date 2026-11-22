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

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * A sheet's shape as it grows from a button or card to the screen's edges: the top follows the morph's
 * corner [radius]; the bottom corners go from that to [bottomTo] (the screen's own bottom radius,
 * agreed 2026-11-21) as it docks, so they line up with the screen instead of being clipped by it or
 * showing what's behind. Built from the morph's values every frame.
 */
internal class SheetShape(
    private val radius: Float,
    private val progress: Float,
    private val bottomTo: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val top = minOf(radius, size.minDimension / 2)
        val p = progress.coerceIn(0f, 1f)
        val bottom = minOf(top + (bottomTo - top) * p, size.minDimension / 2)
        return Outline.Rounded(
            RoundRect(
                0f,
                0f,
                size.width,
                size.height,
                topLeftCornerRadius = CornerRadius(top),
                topRightCornerRadius = CornerRadius(top),
                bottomRightCornerRadius = CornerRadius(bottom),
                bottomLeftCornerRadius = CornerRadius(bottom)
            )
        )
    }
}
