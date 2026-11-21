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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.information89.motion.Morph
import io.github.information89.motion.MorphHost
import io.github.information89.motion.Motion
import io.github.information89.motion.MotionSpring
import io.github.information89.motion.MotionTime
import io.github.information89.motion.morphAnchor
import io.github.information89.motion.motionDrag
import io.github.information89.motion.rememberMotionValue
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.Space
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import kotlin.math.roundToInt

/**
 * The motion lab (M5, 2026-11-19): every motion token and the framework's patterns on one screen,
 * with a slow-motion switch and a frame meter, so animations can be compared and tuned without
 * hunting for the screen they live on. Opened from Settings → Debug. A tool, not part of the app's
 * design: its labels are English only.
 */
class MotionLabActivity : BreezyActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BreezyWeatherTheme {
                MotionLab(onBack = { finish() })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Slow motion is for the lab only.
        if (isFinishing) MotionTime.scale = 1f
    }

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, MotionLabActivity::class.java))
    }
}

/** Every token, as named in `Motion`. */
private val tokens: List<Pair<String, MotionSpring>> = listOf(
    "container" to Motion.container,
    "dismiss" to Motion.dismiss,
    "control" to Motion.control,
    "content" to Motion.content,
    "settle" to Motion.settle,
    "snappy" to Motion.snappy,
    "playful" to Motion.playful,
    "wobble" to Motion.wobble,
    "gentle" to Motion.gentle
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MotionLab(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.motion_lab)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(SolsticeIcons.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = Space.S4,
                end = Space.S4,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + Space.S10
            ),
            verticalArrangement = Arrangement.spacedBy(Space.S4)
        ) {
            item { Controls() }
            item { Section("Tokens", "Tap a row to send its dot across; Play all to compare.") { TokenPlayer() } }
            item {
                Section(
                    "Release (UIKit rule)",
                    "Drag the dot and let go: slower than 250 dp/s doesn't throw, faster than 1000 dp/s " +
                        "moves at least one stop. Past the ends it rubber-bands."
                ) { ReleaseDemo() }
            }
            item {
                Section(
                    "Morph",
                    "Tap the pill: it grows into the card (Morph, container in, dismiss out). Drag the " +
                        "card to scrub it, and catch it mid-way."
                ) { MorphDemo() }
            }
            item {
                Section(
                    "Morph, iOS way (new)",
                    "The study's proposal (docs/MORPH_STUDY.md): five springs (position, size, corners), " +
                        "drawn with transforms, content carried, blur hand-over. Tap the pill; tap or drag " +
                        "the card. Compare with the Morph above."
                ) { SpringMorphDemo() }
            }
            item {
                Section(
                    "Haptics",
                    "Feel each token, named for what it's for (tick, detent, edge, snap, press, release, " +
                        "threshold, confirm, reject). Tap a chip to change its primitive; slide its intensity. " +
                        "Fine ticks get lighter on fast gestures and never buzz."
                ) { HapticsDemo() }
            }
            item {
                Section(
                    "Toolbar",
                    "The app's toolbar on the same navigation as home, and the postcard window's (tap the " +
                        "pin). Every spring and amount below is the app's own (ToolbarMotion): changes apply " +
                        "in the app too, until it restarts."
                ) { ToolbarDemo() }
            }
        }
    }
}

@Composable
private fun Section(title: String, note: String, content: @Composable () -> Unit) {
    Surface(shape = CornerShapes.L, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(Space.S4), verticalArrangement = Arrangement.spacedBy(Space.S3)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

/** Slow motion and the frame meter. */
@Composable
private fun Controls() {
    Surface(shape = CornerShapes.L, color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(Modifier.fillMaxWidth().padding(Space.S4), verticalArrangement = Arrangement.spacedBy(Space.S2)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Slow motion (5×)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Every motion token, app-wide, until you leave the lab.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = MotionTime.scale > 1f, onCheckedChange = { MotionTime.scale = if (it) 5f else 1f })
            }
            FrameMeter()
        }
    }
}

/**
 * Frames per second and the share of slow frames (longer than 1.5 refresh intervals) over the last
 * ~2 s. Only counts while something is animating (an idle screen draws no frames).
 */
@Composable
private fun FrameMeter() {
    val refresh = LocalView.current.display?.refreshRate?.takeIf { it > 0f } ?: 60f
    val budgetNs = (1_000_000_000L / refresh).toLong()
    var fps by remember { mutableFloatStateOf(0f) }
    var slow by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(budgetNs) {
        val intervals = ArrayDeque<Long>()
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    val d = now - last
                    // A gap over ~0.25 s is the screen idling, not a dropped frame.
                    if (d < 250_000_000L) intervals.addLast(d) else intervals.clear()
                    while (intervals.sum() > 2_000_000_000L) intervals.removeFirst()
                    if (intervals.isNotEmpty()) {
                        fps = 1_000_000_000f * intervals.size / intervals.sum()
                        slow = intervals.count { it > budgetNs * 3 / 2 }.toFloat() / intervals.size
                    }
                }
                last = now
            }
        }
    }
    Text(
        "${fps.roundToInt()} fps · ${(slow * 100).roundToInt()} % slow frames · display ${refresh.roundToInt()} Hz",
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
private fun TokenPlayer() {
    val scope = rememberCoroutineScope()
    val dots = remember { tokens.map { Animatable(0f) } }
    Column(verticalArrangement = Arrangement.spacedBy(Space.S2)) {
        tokens.forEachIndexed { i, (name, spring) ->
            fun play() = scope.launch {
                dots[i].animateTo(if (dots[i].targetValue < 0.5f) 1f else 0f, spring.spec())
            }
            Column(Modifier.fillMaxWidth().clip(CornerShapes.S).clickable { play() }.padding(vertical = Space.S1)) {
                Text(
                    "$name · ${"%.2f".format(spring.response)} s · damping ${"%.2f".format(spring.damping)}",
                    style = MaterialTheme.typography.labelLarge
                )
                Track(Sizes.IconLarge) { width ->
                    // The middle 60 % of the track, so overshoot and undershoot show (wobble's
                    // swings reach about a third past each end).
                    Dot(Modifier.graphicsLayer { translationX = (0.2f + 0.6f * dots[i].value) * width })
                }
            }
        }
        FilledTonalButton(onClick = {
            tokens.indices.forEach { i ->
                scope.launch { dots[i].animateTo(if (dots[i].targetValue < 0.5f) 1f else 0f, tokens[i].second.spec()) }
            }
        }) { Text("Play all") }
    }
}

/** A horizontal track; [content] gets the travel distance in px (the track minus the dot). */
@Composable
private fun Track(dot: Dp, content: @Composable (travel: Float) -> Unit) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(dot)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
    ) {
        val travel = with(LocalDensity.current) { (maxWidth - dot).toPx() }
        content(travel)
    }
}

@Composable
private fun Dot(modifier: Modifier = Modifier, size: Dp = Sizes.IconLarge) {
    Box(modifier.size(size).background(MaterialTheme.colorScheme.primary, CircleShape))
}

@Composable
private fun ReleaseDemo() {
    val value = rememberMotionValue(initial = 0f, restPoints = listOf(0f, 0.5f, 1f), overMin = 0.15f, overMax = 0.15f)
    val dot = Sizes.TouchTarget
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val range = maxWidth - dot
        val rangePx = with(LocalDensity.current) { range.toPx() }
        Column(verticalArrangement = Arrangement.spacedBy(Space.S2)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(dot)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                    .motionDrag(value, Orientation.Horizontal, range)
            ) {
                // The stops.
                listOf(0f, 0.5f, 1f).forEach { stop ->
                    Box(
                        Modifier
                            .offset(x = range * stop + dot / 2 - Space.Half)
                            .align(Alignment.CenterStart)
                            .size(Space.S1)
                            .background(MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
                Dot(Modifier.graphicsLayer { translationX = value.value * rangePx }, dot)
            }
            Text(
                "value ${"%.2f".format(value.value)} · velocity ${"%.2f".format(value.velocity)}/s · " +
                    "heading to ${"%.1f".format(value.target)}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun MorphDemo() {
    val value = rememberMotionValue(initial = 0f, overMin = 0.05f, overMax = 0.1f)
    MorphHost(Modifier.fillMaxWidth().height(260.dp)) {
        Box(Modifier.fillMaxSize()) {
            // Where the card ends up (measured, invisible).
            Spacer(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(200.dp)
                    .morphAnchor("lab-card", cornerRadius = Corners.L)
            )
            // The pill it grows from.
            Surface(
                onClick = { value.animateTo(if (value.target < 0.5f) 1f else 0f) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.morphAnchor("lab-pill", cornerRadius = 100.dp)
            ) {
                Text(
                    "Open",
                    modifier = Modifier.padding(horizontal = Space.S4, vertical = Space.S2),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            // Only while it's out: closed, the morph's surface would sit over the pill.
            val out by remember { derivedStateOf { value.value > 0.001f || value.isRunning } }
            if (out) {
                Morph(
                    from = "lab-pill",
                    to = "lab-card",
                    value = value,
                    modifier = Modifier.fillMaxSize().motionDrag(
                        value,
                        Orientation.Vertical,
                        200.dp,
                        reverseDirection = true
                    ),
                    surface = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(Space.S4)
                            .graphicsLayer { alpha = value.window(0.35f, 0.7f) }
                            .clickable { value.animateTo(0f) }
                    ) {
                        Text("Grown from the pill", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(Space.S2))
                        Text(
                            "Tap to close, or drag down to scrub it back.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
