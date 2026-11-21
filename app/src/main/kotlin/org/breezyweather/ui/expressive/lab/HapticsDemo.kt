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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.information89.motion.Haptic
import io.github.information89.motion.HapticPrimitive
import io.github.information89.motion.HapticToken
import io.github.information89.motion.haptics
import org.breezyweather.ui.expressive.theme.Space
import java.util.Locale

/**
 * The haptic tokens (F1): feel each one, change its primitive (tap the chip to cycle) and intensity,
 * and the overall intensity. Changes apply wherever the token is used until the app restarts.
 */
@Composable
internal fun HapticsDemo() {
    val haptics = haptics()
    Column(verticalArrangement = Arrangement.spacedBy(Space.S2)) {
        Text("Overall intensity: " + percent(Haptic.intensity), style = MaterialTheme.typography.bodyMedium)
        Slider(value = Haptic.intensity, onValueChange = { Haptic.intensity = it }, modifier = Modifier.fillMaxWidth())
        Haptic.all.forEach { token -> TokenRow(token) { haptics.perform(token) } }
        OutlinedButton(onClick = {
            Haptic.all.forEach { it.reset() }
            Haptic.intensity = 1f
        }) { Text("Back to the defaults") }
    }
}

@Composable
private fun TokenRow(token: HapticToken, play: () -> Unit) {
    val primitives = HapticPrimitive.names.keys.toList()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.S2)) {
            FilledTonalButton(onClick = play) { Text(token.name) }
            AssistChip(
                onClick = {
                    token.primitive = primitives[(primitives.indexOf(token.primitive) + 1).mod(primitives.size)]
                },
                label = { Text(HapticPrimitive.names[token.primitive] ?: "?") }
            )
            Text(percent(token.scale), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        }
        Slider(value = token.scale, onValueChange = { token.scale = it }, modifier = Modifier.fillMaxWidth())
    }
}

private fun percent(v: Float) = String.format(Locale.ROOT, "%.0f %%", v * 100f)
