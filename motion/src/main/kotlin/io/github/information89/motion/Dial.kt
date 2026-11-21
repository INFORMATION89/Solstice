/*
 * Copyright 2026 INFORMATION89
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.information89.motion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * A dial's scale (F3, agreed 2026-11-21): values from [min] to [max], a minor tick every [step]
 * (where a release can rest, when [snaps]), a major one every [majorEvery] steps, and [visibleSpan]
 * values across the dial's width. E.g. ±14 days, a step of 0.25 (6 hours), a major tick every 4
 * steps (a day), 4 days across.
 */
@Immutable
data class DialScale(
    val min: Float,
    val max: Float,
    val step: Float,
    val majorEvery: Int = 4,
    val visibleSpan: Float = 4f,
    val snaps: Boolean = true,
) {
    init {
        require(max > min && step > 0f && majorEvery >= 1 && visibleSpan > 0f)
    }

    internal val stops: List<Float>
        get() = if (snaps) {
            val n = ((max - min) / step).roundToInt()
            List(n + 1) { min + it * step }
        } else {
            listOf(min, max)
        }
}

/** How a dial looks: colours, sizes, curvature, edge blur and labels. */
@Immutable
data class DialLook(
    val ink: Color,
    val marker: Color,
    val tickHeight: Dp = 10.dp,
    val majorHeight: Dp = 20.dp,
    val tickWidth: Dp = 1.2.dp,
    val majorWidth: Dp = 2.dp,
    /** Items laid out as on a turning drum: biggest and brightest at the centre, smaller at the sides. */
    val curvature: Boolean = true,
    /** The edges blur and fade ([edgeBlur]). */
    val edgeBlur: Dp = 10.dp,
    val labelStyle: TextStyle? = null,
)

/**
 * Where a dial is: a [MotionValue] whose rest points are the scale's stops, settling without bounce,
 * with UIKit's release rule, projection and rubber-banding past the ends.
 */
@Stable
class DialState internal constructor(val scale: DialScale, val value: MotionValue)

/** Remembers a [DialState] starting at [initial]. */
@Composable
fun rememberDialState(scale: DialScale, initial: Float = scale.min): DialState {
    val stops = scale.stops
    val over = scale.step * 2f
    val value = rememberMotionValue(
        initial = initial,
        restPoints = stops,
        overMin = over,
        overMax = over,
        deceleration = MotionPhysics.DECELERATION_NORMAL,
        springFor = { _, _ -> Motion.settle }
    )
    return remember(scale, value) { DialState(scale, value) }
}

/**
 * A dial (F3): a value scrubbed by dragging along a ruler, built once for any app.
 * - Drag 1:1, fling with projection, snap to steps, rubber-band past the ends, caught mid-flight,
 *   never blocking input (all from [MotionValue]).
 * - Haptics from [Haptic]: [Haptic.tick] per step (lighter when fast), [Haptic.detent] per major
 *   step, [Haptic.snap] on settling, [Haptic.edge] on hitting an end.
 * - Ticks, optional labels ([label] per major step) and decorations ([decorate], e.g. an icon), a
 *   centre marker, drum curvature, edge blur, and the ends drawn so the range is visibly finite.
 * - TalkBack: an adjustable control, [describe] reads the value out.
 * [onDoubleTap] is given the state (e.g. to go back to "today").
 */
@Composable
fun Dial(
    state: DialState,
    look: DialLook,
    modifier: Modifier = Modifier,
    label: ((major: Float) -> String?)? = null,
    decorate: (DrawScope.(major: Float, at: Offset, emphasis: Float) -> Unit)? = null,
    describe: (Float) -> String = { "%.2f".format(it) },
    onDoubleTap: ((DialState) -> Unit)? = null,
) {
    val scale = state.scale
    val value = state.value
    val haptics = haptics()
    val measurer = rememberTextMeasurer()

    // Haptics follow the value: a tick per step, a detent per major step, the edge when an end is hit.
    LaunchedEffect(state, haptics) {
        var last = value.value
        var atEdge = false
        snapshotFlow { value.value }.collect { v ->
            val stepBefore = floor((last - scale.min) / scale.step + 0.5f)
            val stepNow = floor((v - scale.min) / scale.step + 0.5f)
            if (stepBefore != stepNow) {
                val major = ((stepNow.toInt() % scale.majorEvery) + scale.majorEvery) % scale.majorEvery == 0
                haptics.perform(if (major) Haptic.detent else Haptic.tick, speed = abs(value.velocity) / scale.step)
            }
            val past = v < scale.min || v > scale.max
            if (past && !atEdge) haptics.perform(Haptic.edge)
            atEdge = past
            last = v
        }
    }
    // Settling on a step.
    LaunchedEffect(state, haptics) {
        var wasRunning = false
        snapshotFlow { value.isRunning }.collect { running ->
            if (wasRunning && !running) haptics.perform(Haptic.snap)
            wasRunning = running
        }
    }

    BoxWithConstraints(
        modifier
            .semantics {
                contentDescription = describe(value.target)
                stateDescription = describe(value.target)
                progressBarRangeInfo = ProgressBarRangeInfo(value.target, scale.min..scale.max, scale.stops.size)
                setProgress { to ->
                    value.animateTo(scale.stops.minBy { abs(it - to) })
                    true
                }
            }
            .then(
                if (onDoubleTap != null) {
                    Modifier.pointerInput(state) { detectTapGestures(onDoubleTap = { onDoubleTap(state) }) }
                } else {
                    Modifier
                }
            )
    ) {
        val perUnit = maxWidth / scale.visibleSpan
        Canvas(
            Modifier
                .fillMaxSize()
                .motionDrag(value, Orientation.Horizontal, perUnit, reverseDirection = true)
                .edgeBlur(
                    start = EdgeBand(maxWidth / 6, blur = look.edgeBlur, fadeOut = true),
                    end = EdgeBand(maxWidth / 6, blur = look.edgeBlur, fadeOut = true)
                )
        ) {
            val v = value.value
            val half = size.width / 2f
            val span = scale.visibleSpan / 2f

            // Where a value sits on screen, and how prominent it is (1 at the centre, less outward).
            fun place(x: Float): Pair<Float, Float>? {
                val u = (x - v) / span
                if (u < -1.05f || u > 1.05f) return null
                return if (look.curvature) {
                    val a = u.coerceIn(-1f, 1f) * (PI / 2).toFloat()
                    (half + sin(a) * half) to cos(a).coerceAtLeast(0f)
                } else {
                    (half + u * half) to (1f - 0.5f * abs(u)).coerceAtLeast(0f)
                }
            }
            val bottom = size.height
            val first = ceil((v - span * 1.1f - scale.min) / scale.step).toInt()
            val last = floor((v + span * 1.1f - scale.min) / scale.step).toInt()
            for (i in first..last) {
                val x = scale.min + i * scale.step
                if (x < scale.min - 1e-4f || x > scale.max + 1e-4f) continue
                val (px, emphasis) = place(x) ?: continue
                val major = ((i % scale.majorEvery) + scale.majorEvery) % scale.majorEvery == 0
                val h = (if (major) look.majorHeight else look.tickHeight).toPx() * (0.6f + 0.4f * emphasis)
                drawLine(
                    look.ink.copy(alpha = look.ink.alpha * (if (major) 0.9f else 0.45f) * (0.35f + 0.65f * emphasis)),
                    Offset(px, bottom - h),
                    Offset(px, bottom),
                    strokeWidth = (if (major) look.majorWidth else look.tickWidth).toPx()
                )
                if (major) {
                    val text = label?.invoke(x)
                    val style = look.labelStyle
                    if (text != null && style != null) {
                        val k = if (look.curvature) 0.7f + 0.3f * emphasis else 1f
                        val laid = measurer.measure(text, style.copy(color = look.ink, fontSize = style.fontSize * k))
                        drawText(
                            laid,
                            topLeft = Offset(
                                px - laid.size.width / 2f,
                                bottom - look.majorHeight.toPx() - laid.size.height - 4.dp.toPx()
                            ),
                            alpha = (0.4f + 0.6f * emphasis)
                        )
                    }
                    decorate?.invoke(this, x, Offset(px, bottom - look.majorHeight.toPx() - 8.dp.toPx()), emphasis)
                }
            }
            // The ends: a short end cap where the range stops, so it never looks endless.
            listOf(scale.min, scale.max).forEach { end ->
                place(end)?.let { (px, emphasis) ->
                    drawLine(
                        look.ink.copy(alpha = 0.9f * (0.35f + 0.65f * emphasis)),
                        Offset(px, bottom - look.majorHeight.toPx() * 1.3f),
                        Offset(px, bottom),
                        strokeWidth = look.majorWidth.toPx() * 1.5f
                    )
                }
            }
            // The centre marker.
            drawLine(
                look.marker,
                Offset(half, bottom - look.majorHeight.toPx() * 1.25f),
                Offset(half, bottom),
                strokeWidth = look.majorWidth.toPx() * 1.5f
            )
        }
    }
}
