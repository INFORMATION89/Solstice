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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.breezyweather.ui.expressive.sky.SkyEffects
import org.breezyweather.ui.expressive.sky.SkyState
import org.breezyweather.ui.expressive.sky.skyEffects
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Opacity
import org.breezyweather.ui.expressive.theme.Space

/**
 * Solstice's one card (agreed 2026-11-21: one card material everywhere, glass): full width, the large
 * corner, the one glass (theme colour at [Opacity.Glass], blurring the sky behind it where there is one,
 * otherwise just the tint, as on a plain page or inside a sheet). Its content gets the theme's ink for
 * text on glass. [tint] colours the glass for a reason (e.g. rain soon); [effects] adds the sky's stars
 * on top ([state] is the sky they belong to).
 */
@Composable
fun SolsticeCard(
    modifier: Modifier = Modifier,
    tint: Color? = null,
    state: SkyState? = null,
    effects: SkyEffects = SkyEffects.None,
    content: @Composable ColumnScope.(ink: Color) -> Unit,
) {
    val shape = CornerShapes.L
    val glass = Modifier.glass(LocalSkyGlass.current, shape, tint?.copy(alpha = Opacity.Glass) ?: glassTint())
    val stars = if (state != null) Modifier.clip(shape).skyEffects(state, effects) else Modifier
    val ink = MaterialTheme.colorScheme.onSurface
    Box(modifier.fillMaxWidth().then(glass).then(stars).padding(Space.S4)) {
        Column { content(ink) }
    }
}
