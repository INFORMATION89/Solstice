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
import io.github.information89.motion.pageSettleFling
import io.github.information89.motion.pageSpringFling
import io.github.information89.motion.pageTransition
import io.github.information89.motion.rememberPageTransition
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Space

/** The three ways a swipe can settle, compared in the lab (2026-11-21, the user: "stiff" settle). */
private enum class Settle(val label: String, val note: String) {
    NOW(
        "Now",
        "The app today: let go, it waits two frames to take a picture of the pages, then slides the picture."
    ),
    A(
        "A",
        "The pages are recorded while you drag, so letting go only copies the last frame: no two-frame wait."
    ),
    B(
        "B",
        "No picture: the pager itself springs to the page from your finger's speed, one spring the whole " +
            "way (no slow-down phase first). Pick the spring below. A tap while it moves first stops it."
    ),
}

/** B's springs to feel: the existing tokens with some bounce (the user, 2026-11-21: "springy"). */
private val BSprings = listOf(
    "content" to Motion.content,
    "container" to Motion.container,
    "brisk" to Motion.brisk,
    "momentum" to Motion.momentum,
    "playful" to Motion.playful
)

/**
 * Swipe settle, A/B (agreed 2026-11-21): the same pages, the same spring (Motion.content, the app's
 * settle), three methods. Swipe and let go part-way or with a flick; watch the frame counter above.
 */
@Composable
internal fun SettleDemo() {
    var choice by remember { mutableIntStateOf(0) }
    var spring by remember { mutableIntStateOf(0) }
    val settle = Settle.entries[choice]
    Column(verticalArrangement = Arrangement.spacedBy(Space.S3)) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            Settle.entries.forEachIndexed { i, s ->
                SegmentedButton(
                    selected = i == choice,
                    onClick = { choice = i },
                    shape = SegmentedButtonDefaults.itemShape(i, Settle.entries.size)
                ) { Text(s.label) }
            }
        }
        // A fresh pager per method, so nothing carries over between them.
        key(settle) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(LabPagerHeight)
                    .clip(CornerShapes.L)
            ) { SettlePager(settle) { BSprings[spring].second } }
        }
        Text(settle.note, style = MaterialTheme.typography.bodySmall)
        if (settle == Settle.B) {
            // Chips that wrap, so every token's name shows in full.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S2)) {
                BSprings.forEachIndexed { i, (name, _) ->
                    FilterChip(selected = i == spring, onClick = { spring = i }, label = { Text(name) })
                }
            }
        }
    }
}

@Composable
private fun SettlePager(settle: Settle, spring: () -> io.github.information89.motion.MotionSpring) {
    val pager = rememberPagerState(initialPage = 1) { LabPages.size }
    if (settle == Settle.B) {
        val overshoot = remember { PageOvershoot() }
        HorizontalPager(
            state = pager,
            flingBehavior = remember(pager) { pageSpringFling(pager, overshoot, spring) },
            modifier = Modifier.fillMaxSize().pageOvershoot(overshoot)
        ) { i -> LabPage(i) }
        return
    }
    val transition = rememberPageTransition()
    val scope = rememberCoroutineScope()
    if (settle == Settle.A) {
        val dragged by pager.interactionSource.collectIsDraggedAsState()
        LaunchedEffect(dragged) { transition.dragging(dragged) }
    }
    HorizontalPager(
        state = pager,
        flingBehavior = remember(pager, transition, scope) { pageSettleFling(pager, transition, scope) },
        modifier = Modifier.fillMaxSize().pageTransition(transition)
    ) { i -> LabPage(i) }
}
