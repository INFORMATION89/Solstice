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

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.github.information89.motion.Motion
import io.github.information89.motion.PageOvershoot
import io.github.information89.motion.pageOvershoot
import io.github.information89.motion.pageSpringFling
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Space

/** Springs to feel the settle with: the existing tokens with some bounce; the app uses momentum. */
private val SettleSprings = listOf(
    "momentum" to Motion.momentum,
    "content" to Motion.content,
    "container" to Motion.container,
    "brisk" to Motion.brisk,
    "playful" to Motion.playful
)

/**
 * Swipe settle (agreed 2026-11-21 after an A/B here: option B with `Motion.momentum`, now in the app):
 * the pages themselves spring to the page from the finger's speed, one spring the whole way, and
 * bounce past the first and last page. Pick another spring to feel it against the app's.
 */
@Composable
internal fun SettleDemo() {
    var spring by remember { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(Space.S3)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(LabPagerHeight)
                .clip(CornerShapes.L)
        ) { SettlePager { SettleSprings[spring].second } }
        // Chips that wrap, so every token's name shows in full.
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S2)) {
            SettleSprings.forEachIndexed { i, (name, _) ->
                FilterChip(selected = i == spring, onClick = { spring = i }, label = { Text(name) })
            }
        }
    }
}

@Composable
private fun SettlePager(spring: () -> io.github.information89.motion.MotionSpring) {
    val pager = rememberPagerState(initialPage = 1) { LabPages.size }
    val overshoot = remember { PageOvershoot() }
    HorizontalPager(
        state = pager,
        flingBehavior = remember(pager) { pageSpringFling(pager, overshoot, spring) },
        modifier = Modifier.fillMaxSize().pageOvershoot(overshoot)
    ) { i -> LabPage(i) }
}
