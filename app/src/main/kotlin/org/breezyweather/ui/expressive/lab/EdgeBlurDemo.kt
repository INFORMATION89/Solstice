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

package org.breezyweather.ui.expressive.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.information89.motion.EdgeBand
import io.github.information89.motion.edgeBlur
import org.breezyweather.ui.expressive.theme.Space

/**
 * :motion's edge blur (F2) on a carousel: the start and end edges blur and fade out. Change the blur
 * and the band, or turn the fade off to see the blur alone.
 */
@Composable
internal fun EdgeBlurDemo() {
    var blur by remember { mutableFloatStateOf(16f) }
    var band by remember { mutableFloatStateOf(72f) }
    var fade by remember { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(Space.S2)) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = Space.S4),
            horizontalArrangement = Arrangement.spacedBy(Space.S3),
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .edgeBlur(
                    start = EdgeBand(band.dp, blur = blur.dp, fadeOut = fade),
                    end = EdgeBand(band.dp, blur = blur.dp, fadeOut = fade)
                )
        ) {
            items(30) { i ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) { Text("${i + 1}", style = MaterialTheme.typography.titleLarge) }
            }
        }
        Text("Blur: ${blur.toInt()} dp", style = MaterialTheme.typography.bodyMedium)
        Slider(value = blur, onValueChange = { blur = it }, valueRange = 0f..32f)
        Text("Band: ${band.toInt()} dp", style = MaterialTheme.typography.bodyMedium)
        Slider(value = band, onValueChange = { band = it }, valueRange = 16f..160f)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Fade out", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = fade, onCheckedChange = { fade = it })
        }
    }
}
