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

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * A spring written the iOS way: [response] is the period of the undamped oscillation in seconds
 * (how quick it feels), [damping] the damping ratio (1 = no bounce, below 1 = bounces).
 * Converted to Compose with `stiffness = (2π / response)²`.
 */
@Immutable
data class MotionSpring(val response: Float, val damping: Float) {
    init {
        require(response > 0f) { "response must be > 0" }
        require(damping > 0f) { "damping must be > 0" }
    }

    /** Compose stiffness for this spring (mass 1). */
    val stiffness: Float get() = stiffnessFor(response)

    fun <T> spec(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = damping,
        stiffness = stiffnessFor(response * MotionTime.scale),
        visibilityThreshold = visibilityThreshold
    )

    companion object {
        fun stiffnessFor(response: Float): Float {
            val omega = 2f * PI.toFloat() / response
            return omega * omega
        }

        fun responseFor(stiffness: Float): Float = 2f * PI.toFloat() / sqrt(stiffness)

        /** The same spring as Compose's `spring(dampingRatio, stiffness)`. */
        fun fromCompose(dampingRatio: Float, stiffness: Float) = MotionSpring(responseFor(stiffness), dampingRatio)
    }
}

/**
 * Slow motion for checking animations (a motion lab's switch): every token's spring runs [scale]
 * times slower. Applies to motions started after it changes (specs a screen already built keep their
 * speed until it's rebuilt). 1 = normal; never left on in normal use (it isn't saved).
 */
object MotionTime {
    var scale by mutableFloatStateOf(1f)
}

/**
 * The only springs in the app (agreed 2026-11-19; `snappy`, `playful` and `wobble` added the same day).
 * Anything new becomes a token first.
 * Values match the ones Solstice used before the framework, written as response + damping.
 */
object Motion {
    /** Morphing containers (sheets, menus, pills): start at once, a touch of bounce. ≈ 0.32 s / 0.78. */
    val container = MotionSpring.fromCompose(dampingRatio = 0.78f, stiffness = 380f)

    /** Dismissing a container back into its source: no bounce, a little slower. ≈ 0.39 s / 1. */
    val dismiss = MotionSpring.fromCompose(dampingRatio = 1f, stiffness = 260f)

    /** Controls (buttons, toggles, sliders): no bounce. ≈ 0.31 s / 1. */
    val control = MotionSpring.fromCompose(dampingRatio = 1f, stiffness = 400f)

    /** Inner content's scale-in. ≈ 0.31 s / 0.85. */
    val content = MotionSpring.fromCompose(dampingRatio = 0.85f, stiffness = 400f)

    /** Settling after a drag or fling: critically damped. ≈ 0.31 s / 1. */
    val settle = MotionSpring.fromCompose(dampingRatio = 1f, stiffness = 400f)

    /**
     * Small controls only (switch thumbs, toggles, icon changes, ripples): quick, barely any bounce.
     * 0.2 s / 0.9. Used for Material's fast springs, so Settings controls stay as quick as Material's.
     */
    val snappy = MotionSpring(response = 0.2f, damping = 0.9f)

    /**
     * Expressive, toolbar-like moments only (agreed 2026-11-19): a pop that overshoots and settles,
     * e.g. a tab lifting under the finger, a press squeeze, a neighbour's landing bump. ≈ 0.31 s / 0.5.
     */
    val playful = MotionSpring.fromCompose(dampingRatio = 0.5f, stiffness = 400f)

    /** Liquid wobble only (a drop pulling free, a surface absorbing it): several soft swings. ≈ 0.31 s / 0.32. */
    val wobble = MotionSpring.fromCompose(dampingRatio = 0.32f, stiffness = 400f)

    /** Unhurried, no bounce: a page swipe settling on its page (agreed 2026-11-19). ≈ 0.44 s / 1. */
    val gentle = MotionSpring.fromCompose(dampingRatio = 1f, stiffness = 200f)

    /**
     * Apple's general-purpose spring (WWDC23 "Animate with springs": bounce 0, the most versatile):
     * no overshoot. For anything closed by a tap or back (WWDC18: Now Playing presented by a tap uses
     * 100 % damping). docs/MORPH_STUDY.md. 0.4 s (Apple's 0.5 s felt slow on the phone, user 2026-11-21).
     */
    val smooth = MotionSpring(response = 0.4f, damping = 1f)

    /**
     * Opening a container by a tap (user 2026-11-21: "a little bit of bounce" when it finishes opening):
     * Apple's "a little more brisk", nudged a little further (user 2026-11-21: "a tiny bit more
     * bounce"): ~20 % bounce.
     */
    val brisk = MotionSpring(response = 0.4f, damping = 0.78f)

    /**
     * A gesture with momentum, let go (WWDC18: Now Playing dismissed by a swipe uses 80 % damping, "a
     * little bit of bounce and squish"): rewards the flick with a slight overshoot.
     */
    val momentum = MotionSpring(response = 0.4f, damping = 0.8f)
}
