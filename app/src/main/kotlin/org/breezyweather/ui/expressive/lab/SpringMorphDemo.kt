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

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.information89.motion.MorphHost
import io.github.information89.motion.MorphStyle
import io.github.information89.motion.SpringMorph
import io.github.information89.motion.morphAnchor
import io.github.information89.motion.morphAnchorOf
import io.github.information89.motion.morphSource
import io.github.information89.motion.rememberMorphMotion
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Space
import org.breezyweather.ui.expressive.theme.StrokeWidth

/**
 * The new morph (docs/MORPH_STUDY.md), to compare with the old one above: an outlined pill that is
 * the button itself, growing into a filled card on five springs, its label handing over to the
 * card's heading with a blur replace. Tap the pill to open; tap the card to close; drag the card
 * (slowly, or flick) to scrub it and let go.
 */
@Composable
internal fun SpringMorphDemo() {
    val motion = rememberMorphMotion()
    val ink = MaterialTheme.colorScheme.onSurface
    val pillStyle = MorphStyle(border = ink.copy(alpha = 0.4f), borderWidth = StrokeWidth.Thin)
    val cardStyle = MorphStyle(fill = MaterialTheme.colorScheme.primaryContainer)
    MorphHost(Modifier.fillMaxWidth().height(320.dp)) {
        Box(Modifier.fillMaxSize()) {
            // Where the card rests: an invisible anchor.
            Spacer(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(240.dp)
                    .morphAnchor("lab-spring-card", cornerRadius = Corners.XL)
            )
            // The pill: the button itself, hidden while the morph draws it.
            Box(
                Modifier
                    .morphSource("lab-spring-pill", cornerRadius = 100.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        motion.open()
                    }
                    .border(pillStyle.borderWidth, pillStyle.border, CircleShape)
            ) { PillLabel(ink) }
            morphAnchorOf("lab-spring-card")?.let { t ->
                SpringMorph(
                    from = "lab-spring-pill",
                    target = t,
                    motion = motion,
                    fromStyle = pillStyle,
                    toStyle = cardStyle,
                    sourceContent = { PillLabel(ink) }
                ) {
                    Column(Modifier.fillMaxSize().padding(Space.S4)) {
                        Text("Details", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Grown on five springs. Tap to close, even mid-way; drag down to scrub, or flick.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PillLabel(ink: Color) {
    Text(
        "Open details",
        color = ink,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = Space.S3, vertical = Space.S2)
    )
}
