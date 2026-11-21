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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

/**
 * One number per interaction: 0 = closed, 1 = open (or any rest points you give). A finger moves
 * it directly ([drag]), letting go hands the finger's velocity to a spring ([release]), and a new
 * touch catches it mid-flight. It can go past the ends with rubber-banding.
 *
 * Read [value] only in layout and draw (`graphicsLayer {}`, `Modifier.layout`, `drawBehind`), so a
 * motion doesn't recompose every frame.
 */
@Stable
class MotionValue internal constructor(
    initial: Float,
    private val scope: CoroutineScope,
    restPoints: List<Float> = listOf(0f, 1f),
    /** How far past the lowest / highest rest point a pull may travel (in value units). */
    val overMin: Float = 0.1f,
    val overMax: Float = 0.1f,
    /** Releases slower / faster than these (value units per second): see [MotionPhysics.chooseTarget]. */
    var slowFlick: Float = 0.5f,
    var fastFlick: Float = 2f,
    val deceleration: Float = MotionPhysics.DECELERATION_FAST,
    /** The spring for a move towards [target]; by default the container opens, dismiss closes. */
    val springFor: (from: Float, target: Float) -> MotionSpring = { from, target ->
        if (target >= from) Motion.container else Motion.dismiss
    },
) {
    private val animatable = Animatable(initial)

    /**
     * Where a release may come to rest. Can change (e.g. a sheet whose tall size follows the
     * keyboard); a motion already running keeps its target.
     */
    var restPoints: List<Float> = restPoints
        set(value) {
            require(value.isNotEmpty()) { "restPoints must not be empty" }
            field = value
        }
    private val min get() = restPoints.min()
    private val max get() = restPoints.max()

    /** The finger's position before resistance (only meaningful while dragging). */
    private var raw = initial

    /** Current position. Can be past the ends while pulled or overshooting. */
    val value: Float get() = animatable.value

    /** Current velocity in value units per second. */
    val velocity: Float get() = animatable.velocity

    /** The rest point the value is heading to (or sitting at). */
    val target: Float get() = animatable.targetValue

    val isRunning: Boolean get() = animatable.isRunning

    /** Stops any motion where it is (a touch catching it mid-flight). */
    fun stop() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) { animatable.stop() }
        // Past an end the value is already rubber-banded: go back to the finger position that shows
        // it, or the next drag would resist it twice and the surface would jump back.
        raw = MotionPhysics.unresist(animatable.value, min, max, overMin, overMax)
    }

    /** Moves by [delta] value units with rubber-banding past the ends. */
    fun drag(delta: Float) {
        raw += delta
        val shown = MotionPhysics.resist(raw, min, max, overMin, overMax)
        scope.launch(start = CoroutineStart.UNDISPATCHED) { animatable.snapTo(shown) }
    }

    /**
     * Lets go with [velocity] (value units per second): picks a rest point from the fling and
     * springs there, keeping the velocity. Past an edge the velocity is scaled down by the
     * resistance, so a release from an over-pull doesn't shoot off. Returns the chosen rest point.
     */
    fun release(velocity: Float): Float {
        val v = velocity * MotionPhysics.resistance(raw, min, max, overMin, overMax)
        val to = targetFor(velocity)
        animateTo(to, v)
        return to
    }

    /** The rest point [release] would pick for [velocity], without moving. */
    fun targetFor(velocity: Float): Float {
        val v = velocity * MotionPhysics.resistance(raw, min, max, overMin, overMax)
        return MotionPhysics.chooseTarget(animatable.value, v, restPoints, slowFlick, fastFlick, deceleration)
    }

    /** Springs to [target], starting with [velocity] (defaults to the current one, so it never jumps). */
    fun animateTo(target: Float, velocity: Float = animatable.velocity, spring: MotionSpring? = null) {
        val spec = (spring ?: springFor(animatable.value, target)).spec<Float>()
        raw = target
        scope.launch { animatable.animateTo(target, spec, initialVelocity = velocity) }
    }

    /** Jumps to [target] with no motion (e.g. "Remove animations" is on). */
    fun snapTo(target: Float) {
        raw = target
        scope.launch(start = CoroutineStart.UNDISPATCHED) { animatable.snapTo(target) }
    }

    /** 0..1 progress of [value] through the window [from]..[to] (choreography). */
    fun window(from: Float, to: Float): Float = window(value, from, to)

    companion object {
        /** 0..1 progress of [p] through [from]..[to], clamped. */
        fun window(p: Float, from: Float, to: Float): Float {
            if (to <= from) return if (p >= to) 1f else 0f
            return ((p - from) / (to - from)).coerceIn(0f, 1f)
        }
    }
}

/** Remembers a [MotionValue]; see its parameters. */
@Composable
fun rememberMotionValue(
    initial: Float = 0f,
    restPoints: List<Float> = listOf(0f, 1f),
    overMin: Float = 0.1f,
    overMax: Float = 0.1f,
    // How a fling is projected: DECELERATION_FAST (short throws, the default) or
    // DECELERATION_NORMAL (UIKit's scroll and sheet-dismiss feel: a medium flick goes far).
    deceleration: Float = MotionPhysics.DECELERATION_FAST,
    // The spring for each move; by default containers open and dismiss closes. A dial settles
    // without bouncing: `{ _, _ -> Motion.settle }`.
    springFor: ((from: Float, target: Float) -> MotionSpring)? = null,
): MotionValue {
    val scope = rememberCoroutineScope()
    val value = remember(scope) {
        if (springFor != null) {
            MotionValue(
                initial,
                scope,
                restPoints,
                overMin,
                overMax,
                deceleration = deceleration,
                springFor = springFor
            )
        } else {
            MotionValue(initial, scope, restPoints, overMin, overMax, deceleration = deceleration)
        }
    }
    // Later changes to the rest points passed here apply too (the other parameters are fixed at
    // creation). Only when they change, so rest points set directly on the value aren't undone.
    val passed = remember(value) { arrayOf(restPoints) }
    SideEffect {
        if (passed[0] != restPoints) {
            passed[0] = restPoints
            value.restPoints = restPoints
        }
    }
    return value
}

/**
 * A drag that drives [value]: dragging [range] (the distance from rest point 0 to 1) moves it by
 * 1; letting go releases with the finger's velocity. Flicks slower than 250 dp/s count as none,
 * faster than 1000 dp/s move one step (UIKit's rules).
 */
@Composable
fun Modifier.motionDrag(
    value: MotionValue,
    orientation: Orientation,
    range: Dp,
    reverseDirection: Boolean = false,
    enabled: Boolean = true,
): Modifier {
    val density = LocalDensity.current
    val rangePx = with(density) { range.toPx() }.coerceAtLeast(1f)
    val slowPx = with(density) { MotionPhysics.SlowFlick.toPx() }
    val fastPx = with(density) { MotionPhysics.FastFlick.toPx() }
    // Written after composition, not during it.
    SideEffect {
        value.slowFlick = slowPx / rangePx
        value.fastFlick = fastPx / rangePx
    }
    val state = rememberDraggableState { delta -> value.drag(delta / rangePx) }
    return draggable(
        state = state,
        orientation = orientation,
        enabled = enabled,
        reverseDirection = reverseDirection,
        // While it's moving, a touch catches it at once (no touch slop, so it doesn't keep going
        // under the finger and then jump).
        startDragImmediately = value.isRunning,
        onDragStarted = { value.stop() },
        onDragStopped = { velocity -> value.release(velocity / rangePx) }
    )
}
