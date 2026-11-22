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

package org.breezyweather.ui.expressive.shell

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.information89.motion.Motion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.ui.expressive.components.GlassSource
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.components.glass
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.Space
import kotlin.math.abs

/** A short, icon-led message: "[compass] Visiting Tokyo", "[bin] Guadalajara removed · Undo". */
class ShellMessage(
    val icon: ImageVector,
    val text: String,
    val action: String? = null,
    val onAction: () -> Unit = {},
) {
    /** How long it stays: a bit longer when there's something to undo. */
    internal val millis: Long get() = if (action != null) 5_000L else 3_000L
}

/** Holds the message on screen. A new message replaces the current one. */
@Stable
class ShellMessages {
    internal var current by mutableStateOf<ShellMessage?>(null)

    fun show(message: ShellMessage) {
        current = message
    }
}

/**
 * The message, agreed 2026-11-18 ("rise and swipe"): it rises from behind the toolbar on a soft
 * spring and, after a few seconds, sinks back by itself. Swipe it sideways to dismiss: it follows
 * the finger with a slight tilt and flies off past a point, or springs back. It also follows the
 * finger a little up and down; pulled down, it tucks behind the toolbar. No close button; Undo
 * is a compact action where it applies. Every motion can be caught mid-way.
 */
@Composable
fun ShellMessageHost(messages: ShellMessages, glass: GlassSource, modifier: Modifier = Modifier) {
    val message = messages.current ?: return
    val scope = rememberCoroutineScope()
    val rise = remember(message) { Animatable(0f) } // 0 = tucked behind the toolbar, 1 = shown
    // Where the finger has moved it (px). Plain state, written straight from the gesture; one
    // cancellable settle animates it on release (no queued jumps that could cancel the settle).
    var dx by remember(message) { mutableFloatStateOf(0f) }
    var dy by remember(message) { mutableFloatStateOf(0f) }
    var settle by remember(message) { mutableStateOf<Job?>(null) }
    val tracker = remember(message) { VelocityTracker() }
    var width by remember { mutableStateOf(1) }
    var holding by remember(message) { mutableStateOf(false) }
    // Rises like a container; settles back after a drag without bounce.
    val rising = Motion.container.spec<Float>()
    val soft = Motion.settle.spec<Float>()

    suspend fun sinkAndClear() {
        rise.animateTo(0f, Motion.dismiss.spec())
        if (messages.current === message) messages.current = null
    }

    // Let go: past about a third sideways (or a fling) it flies off; pulled down past a little (or
    // flung down) it tucks behind the toolbar; otherwise it springs back. Velocity carries over.
    fun release(vx: Float, vy: Float, density: Float) {
        val sideways = abs(dx) > width * 0.35f || abs(vx) > 1200f
        val down = dy > 24f * density || vy > 900f
        settle = scope.launch {
            when {
                sideways -> {
                    val dir = if (dx + vx * 0.1f >= 0f) 1f else -1f
                    launch { animate(dy, 0f, vy, soft) { v, _ -> dy = v } }
                    animate(dx, dir * width * 1.3f, vx, Motion.dismiss.spec()) { v, _ ->
                        dx = v
                    }
                    if (messages.current === message) messages.current = null
                }
                down -> {
                    launch { animate(dx, 0f, vx, soft) { v, _ -> dx = v } }
                    launch { animate(dy, 0f, vy, soft) { v, _ -> dy = v } }
                    sinkAndClear()
                }
                else -> {
                    launch { animate(dx, 0f, vx, soft) { v, _ -> dx = v } }
                    animate(dy, 0f, vy, soft) { v, _ -> dy = v }
                }
            }
        }
    }

    LaunchedEffect(message) { rise.animateTo(1f, rising) }
    // Holding it (mid-swipe) pauses the timer; letting go restarts it.
    LaunchedEffect(message, holding) {
        if (holding) return@LaunchedEffect
        delay(message.millis)
        sinkAndClear()
    }

    // Cut along the toolbar's top edge (this box's bottom), so a sinking message slips behind the
    // toolbar instead of showing beside or under it. Only the bottom is cut: a sideways swipe can
    // still fly the message off past the sides.
    Box(
        modifier.drawWithContent {
            val big = size.width * 4f
            clipRect(left = -big, top = -big, right = size.width + big, bottom = size.height) {
                this@drawWithContent.drawContent()
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = Space.S6)
                .padding(bottom = Space.S3)
                .widthIn(max = 420.dp)
                .heightIn(min = Sizes.TouchTarget)
                .onSizeChanged { width = it.width.coerceAtLeast(1) }
                // Before the layer, so the finger is tracked in screen space while the pill moves.
                .pointerInput(message) {
                    var rawY = 0f
                    detectDragGestures(
                        onDragStart = {
                            settle?.cancel()
                            holding = true
                            rawY = dy
                            tracker.resetTracking()
                        },
                        onDragEnd = {
                            holding = false
                            val v = tracker.calculateVelocity()
                            release(v.x, v.y, density)
                        },
                        onDragCancel = {
                            holding = false
                            release(0f, 0f, density)
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            tracker.addPosition(change.uptimeMillis, change.position)
                            dx += amount.x
                            rawY += amount.y
                            // Follows the finger down; resists going up (a rubber band).
                            dy = if (rawY > 0f) rawY * 0.7f else rawY * 0.25f
                        }
                    )
                }
                .graphicsLayer {
                    val r = rise.value
                    // Rises out from behind the toolbar (it sits below; the toolbar draws on top).
                    translationY = (1f - r) * 56.dp.toPx() + dy
                    val x = dx
                    translationX = x
                    rotationZ = x / width * 8f
                    alpha = (r * (1f - abs(x) / width)).coerceIn(0f, 1f)
                    scaleX = 0.92f + 0.08f * r
                    scaleY = 0.92f + 0.08f * r
                }
                .glass(glass, CircleShape)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .padding(start = Space.S4, end = if (message.action != null) Space.S2 else Space.S5)
        ) {
            Icon(
                SolsticeIcons.forSize(message.icon, Sizes.Icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(Sizes.Icon)
            )
            Spacer(Modifier.width(Space.S3))
            Text(
                message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false).padding(vertical = Space.S3)
            )
            message.action?.let { label ->
                Spacer(Modifier.width(Space.S1))
                TextButton(onClick = {
                    message.onAction()
                    scope.launch { sinkAndClear() }
                }) { Text(label) }
            }
        }
    }
}

/** An icon for upstream's plain-text messages, picked from what they say. */
fun messageIcon(context: Context, text: String): ImageVector = when (text) {
    context.getString(R.string.location_message_added) -> SolsticeIcons.AddLocationAlt
    context.getString(R.string.location_message_updated) -> SolsticeIcons.Check
    context.getString(R.string.location_message_deleted) -> SolsticeIcons.Delete
    context.getString(R.string.message_please_wait_refresh) -> SolsticeIcons.HourglassEmpty
    context.getString(R.string.message_multiple_refresh_errors) -> SolsticeIcons.CloudOff
    else -> SolsticeIcons.Info
}
