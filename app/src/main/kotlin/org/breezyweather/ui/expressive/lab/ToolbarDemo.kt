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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.information89.motion.MotionSpring
import io.github.information89.motion.pageTransition
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.components.glassSource
import org.breezyweather.ui.expressive.components.rememberGlassSource
import org.breezyweather.ui.expressive.shell.ShellToolbar
import org.breezyweather.ui.expressive.shell.ToolbarMotion
import org.breezyweather.ui.expressive.shell.pageSwipeBlur
import org.breezyweather.ui.expressive.shell.rememberShellNavigation
import org.breezyweather.ui.expressive.sky.SkyWeather
import org.breezyweather.ui.expressive.sky.skyAt
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Space
import java.util.Locale

/** The lab's stand-in pages (the app's three tabs), shared by the pager demos. */
internal val LabPages = listOf(
    "This week" to Color(0xFF6F7FD8),
    "Forecast" to Color(0xFFE9A15B),
    "Places" to Color(0xFF5BA98A)
)

/** How tall the lab's pager demos are. */
internal val LabPagerHeight = 300.dp

/** Page [i] of [LabPages]: its colour fading down, its name in the middle. */
@Composable
internal fun LabPage(i: Int, modifier: Modifier = Modifier) {
    val (name, color) = LabPages[i]
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(color, color.copy(alpha = 0.5f))))
    ) {
        Text(name, style = MaterialTheme.typography.headlineSmall, color = Color.White)
    }
}

/**
 * The toolbar in the lab (PLAN.md "Morph refining", point 5): the app's own toolbar over three
 * stand-in pages, on the same navigation as home (tab taps, swipes, slide to switch, the search bud
 * on the last page), plus the postcard window's version for the pin's jab. The controls below edit
 * [ToolbarMotion] live, which is what the app reads: a value tuned here is the app's value until the
 * app restarts.
 */
@Composable
internal fun ToolbarDemo() {
    val pager = rememberPagerState(initialPage = 1) { 3 }
    val nav = rememberShellNavigation(pager)
    val glass = rememberGlassSource()
    val sky = remember { skyAt(SkyWeather.CLEAR, System.currentTimeMillis(), null, null) }
    var pinned by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(Space.S3)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(LabPagerHeight)
                .clip(CornerShapes.L)
                .glassSource(glass)
        ) {
            HorizontalPager(
                state = pager,
                flingBehavior = nav.fling,
                modifier = Modifier.fillMaxSize().pageTransition(nav.pageChange)
            ) { i ->
                LabPage(i, Modifier.pageSwipeBlur({ nav.visualPosition() }, i))
            }
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = Space.S3)) {
                ShellToolbar(
                    pager = pager,
                    sky = sky,
                    weather = SkyWeather.CLEAR,
                    glass = glass,
                    labelShown = listOf(1f, 1f, 1f),
                    onTab = { nav.navigate(it) },
                    onInterrupt = { nav.interrupt() },
                    position = { nav.toolbarPosition() },
                    passing = { nav.isPassing(it) },
                    searchEnabled = true,
                    onSearch = {},
                    onSearchBounds = {}
                )
            }
        }
        Text(
            "Tap or swipe the pages; hold the pill and slide to switch; go to Places for the search bud.",
            style = MaterialTheme.typography.bodySmall
        )
        // The postcard window's toolbar, for the pin's jab: tap the pin.
        WindowToolbar(pinned = pinned, onPin = { pinned = !pinned })

        SpringRow("Neck rejoining", ToolbarMotion.neckJoin) { ToolbarMotion.neckJoin = it }
        SpringRow("Neck letting go", ToolbarMotion.neckFree) { ToolbarMotion.neckFree = it }
        SpringRow("Wobble and ripple", ToolbarMotion.wobble) { ToolbarMotion.wobble = it }
        AmountRow("Wobble swell", ToolbarMotion.wobbleAmount, 0f..0.3f) { ToolbarMotion.wobbleAmount = it }
        AmountRow("Ripple stretch", ToolbarMotion.rippleStretch, 0f..0.1f) { ToolbarMotion.rippleStretch = it }
        AmountRow("Ripple squash", ToolbarMotion.rippleSquash, 0f..0.1f) { ToolbarMotion.rippleSquash = it }
        SpringRow("Pin jab", ToolbarMotion.jab) { ToolbarMotion.jab = it }
        AmountRow("Jab drop (dp)", ToolbarMotion.jabDrop, 0f..5f) { ToolbarMotion.jabDrop = it }
        AmountRow("Jab squash", ToolbarMotion.jabSquash, 0f..0.2f) { ToolbarMotion.jabSquash = it }
        AmountRow("Jab stretch", ToolbarMotion.jabStretch, 0f..0.1f) { ToolbarMotion.jabStretch = it }
        AmountRow("Jab tilt (°)", ToolbarMotion.jabTilt, 0f..20f) { ToolbarMotion.jabTilt = it }
        SpringRow("Slide: pop out", ToolbarMotion.popOut) { ToolbarMotion.popOut = it }
        SpringRow("Slide: sink back", ToolbarMotion.popIn) { ToolbarMotion.popIn = it }
        SpringRow("Slide: cancelled", ToolbarMotion.popCancel) { ToolbarMotion.popCancel = it }
        AmountRow("Slide: pop size", ToolbarMotion.popScale, 0f..0.3f) { ToolbarMotion.popScale = it }
        AmountRow("Slide: travel per page", ToolbarMotion.slideTravel, 0.8f..3f) { ToolbarMotion.slideTravel = it }
        SpringRow("Tab press: down", ToolbarMotion.pressDown) { ToolbarMotion.pressDown = it }
        SpringRow("Tab press: up", ToolbarMotion.pressUp) { ToolbarMotion.pressUp = it }
        AmountRow("Tab press: shrink to", ToolbarMotion.pressScale, 0.7f..1f) { ToolbarMotion.pressScale = it }
        SpringRow("Bud colour", ToolbarMotion.budTint) { ToolbarMotion.budTint = it }
        OutlinedButton(onClick = { ToolbarMotion.reset() }) { Text("Back to the agreed values") }
    }
}

/** The postcard window's toolbar (no Places tab, the pin as its bud), standing on the Forecast tab. */
@Composable
private fun WindowToolbar(pinned: Boolean, onPin: () -> Unit) {
    val pager = rememberPagerState(initialPage = 1) { 2 }
    val nav = rememberShellNavigation(pager)
    val glass = rememberGlassSource()
    val sky = remember { skyAt(SkyWeather.CLEAR, System.currentTimeMillis(), null, null) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(CornerShapes.L)
            .glassSource(glass)
            .background(Brush.horizontalGradient(listOf(Color(0xFFE9A15B), Color(0xFF6F7FD8))))
    ) {
        ShellToolbar(
            pager = pager,
            sky = sky,
            weather = SkyWeather.CLEAR,
            glass = glass,
            labelShown = listOf(1f, 1f),
            onTab = { nav.navigate(it) },
            onInterrupt = { nav.interrupt() },
            position = { nav.toolbarPosition() },
            passing = { nav.isPassing(it) },
            searchEnabled = true,
            onSearch = onPin,
            onSearchBounds = {},
            withPlaces = false,
            budIcon = if (pinned) SolsticeIcons.PushPinFill else SolsticeIcons.PushPin,
            budDescription = if (pinned) "Pinned" else "Pin",
            budChecked = pinned
        )
    }
}

/** A spring setting: tap to step through the tokens. */
@Composable
private fun SpringRow(label: String, value: MotionSpring, onChange: (MotionSpring) -> Unit) {
    val tokens = ToolbarMotion.tokens
    // By identity: two tokens can share values (settle and control).
    val index = tokens.indexOfFirst { it.second === value }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        AssistChip(
            onClick = { onChange(tokens[(index + 1).mod(tokens.size)].second) },
            label = { Text(if (index >= 0) tokens[index].first else "custom") }
        )
    }
}

/** An amount setting: a slider with its value. */
@Composable
private fun AmountRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(String.format(Locale.ROOT, "%.2f", value), style = MaterialTheme.typography.labelLarge)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range, modifier = Modifier.fillMaxWidth())
    }
}
