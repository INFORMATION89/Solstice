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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.information89.motion.Dial
import io.github.information89.motion.DialLook
import io.github.information89.motion.DialScale
import io.github.information89.motion.rememberDialState
import org.breezyweather.ui.expressive.theme.Space
import java.util.Locale
import kotlin.math.roundToInt

/**
 * :motion's dial (F3): ±14 days in 6-hour steps, a major tick per day. Drag or flick it; it rubber-
 * bands at the ends with a haptic, and a double tap goes back to 0. Switches for labels, curvature
 * and snapping.
 */
@Composable
internal fun DialDemo() {
    var labels by remember { mutableStateOf(true) }
    var curvature by remember { mutableStateOf(true) }
    var snaps by remember { mutableStateOf(true) }
    val scale = DialScale(min = -14f, max = 14f, step = 0.25f, majorEvery = 4, visibleSpan = 4f, snaps = snaps)
    val state = rememberDialState(scale, initial = 0f)
    val ink = MaterialTheme.colorScheme.onSurface
    val shown by remember(state) { derivedStateOf { state.value.target } }
    Column(verticalArrangement = Arrangement.spacedBy(Space.S2)) {
        Text(describe(shown), style = MaterialTheme.typography.titleMedium)
        Dial(
            state = state,
            look = DialLook(
                ink = ink,
                marker = MaterialTheme.colorScheme.primary,
                curvature = curvature,
                labelStyle = if (labels) MaterialTheme.typography.labelMedium else null
            ),
            label = { day -> dayLabel(day) },
            describe = ::describe,
            onDoubleTap = { it.value.animateTo(0f) },
            modifier = Modifier.fillMaxWidth().height(72.dp)
        )
        Toggle("Labels", labels) { labels = it }
        Toggle("Drum curvature", curvature) { curvature = it }
        Toggle("Snap to 6-hour steps", snaps) { snaps = it }
    }
}

@Composable
private fun Toggle(name: String, on: Boolean, set: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = on, onCheckedChange = set)
    }
}

private fun dayLabel(day: Float): String {
    val d = day.roundToInt()
    return when {
        d == 0 -> "Today"
        d > 0 -> "+$d"
        else -> "$d"
    }
}

private fun describe(v: Float): String {
    val hours = (v * 24f).roundToInt()
    val days = hours / 24
    val rest = hours - days * 24
    return String.format(Locale.ROOT, "%+d days, %d h", days, rest)
}
