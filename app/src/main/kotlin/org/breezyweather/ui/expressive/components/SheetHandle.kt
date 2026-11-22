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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import org.breezyweather.R
import org.breezyweather.ui.expressive.theme.Opacity
import org.breezyweather.ui.expressive.theme.Sizes

/**
 * The one drag handle for every sheet (search, moon card, postcard window; agreed 2026-11-21):
 * Material 3's — a 32 × 4 dp bar in [ink] at M3's 40 %, centred in the sheet's handle strip at the top
 * of at least a 48 dp touch target (which reaches below the strip, over the sheet's content).
 * Tapping it calls [onClose]; dragging is the sheet's (pass `Modifier.morphHandle(motion)` for a
 * morph, or the sheet's own drag).
 */
@Composable
internal fun SheetHandle(ink: Color, onClose: (() -> Unit)?, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.action_close)
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .sizeIn(minWidth = Sizes.TouchTarget, minHeight = Sizes.TouchTarget)
            .then(
                if (onClose != null) {
                    Modifier.clickable(onClickLabel = label, role = Role.Button, onClick = onClose)
                } else {
                    Modifier
                }
            )
    ) {
        Box(Modifier.height(Sizes.HandleStrip), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .width(Sizes.HandleWidth)
                    .height(Sizes.HandleThickness)
                    .background(ink.copy(alpha = Opacity.Handle), CircleShape)
            )
        }
    }
}
