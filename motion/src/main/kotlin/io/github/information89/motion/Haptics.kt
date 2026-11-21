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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import kotlin.math.abs

/**
 * Android's haptic building blocks, by their documented values (some are only named on newer SDKs):
 * Composition primitives (Android 11+; [THUD] and [LOW_TICK] 12+) and the view feedback constants used
 * where primitives aren't supported.
 */
object HapticPrimitive {
    const val CLICK = 1
    const val THUD = 2
    const val SPIN = 3
    const val QUICK_RISE = 4
    const val SLOW_RISE = 5
    const val QUICK_FALL = 6
    const val TICK = 7
    const val LOW_TICK = 8

    /** Names for a lab. */
    val names: Map<Int, String> = mapOf(
        CLICK to "click",
        THUD to "thud",
        SPIN to "spin",
        QUICK_RISE to "quick rise",
        SLOW_RISE to "slow rise",
        QUICK_FALL to "quick fall",
        TICK to "tick",
        LOW_TICK to "low tick"
    )

    internal fun minSdk(primitive: Int): Int = if (primitive == THUD || primitive == SPIN ||
        primitive == LOW_TICK
    ) {
        31
    } else {
        30
    }
}

/**
 * One kind of haptic feedback, named for what it's for (F1, agreed 2026-11-21): a Composition
 * [primitive] at an intensity ([scale]), or the view feedback [fallback] where primitives aren't
 * supported. [minGapMillis] keeps a fast drag from buzzing; [speedFade] makes it lighter as the
 * gesture speeds up, as iOS does. [primitive] and [scale] can be changed live (e.g. from a lab).
 *
 * iOS's feedback for reference: selection (a fine step), impact light/medium/heavy/soft/rigid (a
 * physical contact), notification success/warning/error (an outcome). Ours map onto them by purpose.
 */
@Stable
class HapticToken(
    val name: String,
    primitive: Int,
    scale: Float,
    val fallback: Int,
    val minGapMillis: Long = 0L,
    val speedFade: Boolean = false,
) {
    var primitive by mutableIntStateOf(primitive)
    var scale by mutableFloatStateOf(scale)
    internal var last = 0L
    internal val defaults = primitive to scale

    fun reset() {
        primitive = defaults.first
        scale = defaults.second
    }
}

/**
 * The haptic tokens. Use only these, as with [Motion]'s springs: anything new becomes a token first
 * (and no new haptics without agreeing them, MOTION.md).
 */
object Haptic {
    /** A fine step: a value passing a small division (iOS selection). */
    val tick = HapticToken(
        "tick",
        HapticPrimitive.TICK,
        0.5f,
        HapticFeedbackConstants.CLOCK_TICK,
        minGapMillis = 30L,
        speedFade = true
    )

    /** A major step: a whole unit passed (a day on a dial). */
    val detent =
        HapticToken("detent", HapticPrimitive.CLICK, 0.6f, HapticFeedbackConstants.CLOCK_TICK, minGapMillis = 40L)

    /** Hitting a limit: the end of a range (iOS impact rigid). */
    val edge =
        HapticToken("edge", HapticPrimitive.THUD, 0.6f, HapticFeedbackConstants.CONTEXT_CLICK, minGapMillis = 120L)

    /** Settling on a target: a fling coming to rest on a stop (iOS impact soft). */
    val snap = HapticToken("snap", HapticPrimitive.CLICK, 0.4f, HapticFeedbackConstants.CLOCK_TICK, minGapMillis = 80L)

    /** Pressing something that lifts or is held: a long press picking a card up (iOS impact medium). */
    val press = HapticToken("press", HapticPrimitive.CLICK, 0.8f, HapticFeedbackConstants.LONG_PRESS)

    /** Letting go of something held: it lands (iOS impact light). */
    val release = HapticToken("release", HapticPrimitive.TICK, 0.6f, HapticFeedbackConstants.CLOCK_TICK)

    /** A drag crossing its point of no return: a swipe action arming, a sheet passing dismiss. */
    val threshold = HapticToken(
        "threshold",
        HapticPrimitive.QUICK_RISE,
        0.7f,
        HapticFeedbackConstants.CONTEXT_CLICK,
        minGapMillis = 150L
    )

    /** Something done (iOS notification success). */
    val confirm = HapticToken("confirm", HapticPrimitive.CLICK, 1f, FEEDBACK_CONFIRM)

    /** Something refused (iOS notification error). */
    val reject = HapticToken("reject", HapticPrimitive.THUD, 1f, FEEDBACK_REJECT)

    /** Every token, e.g. for a lab. */
    val all: List<HapticToken> get() = listOf(tick, detent, edge, snap, press, release, threshold, confirm, reject)

    /** One intensity for all of them (0..1), on top of each token's own. */
    var intensity by mutableFloatStateOf(1f)
}

// HapticFeedbackConstants.CONFIRM / REJECT (Android 11+), by value.
private const val FEEDBACK_CONFIRM = 16
private const val FEEDBACK_REJECT = 17

/**
 * Plays [Haptic] tokens. Respects the system's touch-feedback setting, rate-limits each token, and
 * lightens speed-faded ones on fast gestures. `:motion` pieces and apps reach it through [LocalHaptics]
 * and never call the vibrator themselves.
 */
@Stable
class MotionHaptics internal constructor(private val view: View) {
    private val vibrator: Vibrator? = view.context.vibrator()

    /**
     * Plays [token]. [speed] (units per second of whatever is moving, e.g. dial days per second) lets
     * speed-faded tokens get lighter on fast gestures: full at [slowSpeed] and below, faint by
     * [fastSpeed].
     */
    fun perform(token: HapticToken, speed: Float = 0f, slowSpeed: Float = 2f, fastSpeed: Float = 12f) {
        val now = SystemClock.uptimeMillis()
        if (now - token.last < token.minGapMillis) return
        token.last = now
        if (!feedbackEnabled()) return
        val scale = hapticScale(token, Haptic.intensity, speed, slowSpeed, fastSpeed)
        if (scale <= 0.01f) return
        if (!playPrimitive(token.primitive, scale)) playFallback(token.fallback)
    }

    @SuppressLint("NewApi", "MissingPermission")
    private fun playPrimitive(primitive: Int, scale: Float): Boolean {
        val v = vibrator ?: return false
        if (Build.VERSION.SDK_INT < HapticPrimitive.minSdk(primitive)) return false
        if (!v.areAllPrimitivesSupported(primitive)) return false
        // Without the VIBRATE permission (an app that removed it), fall back to view feedback.
        return runCatching {
            v.vibrate(VibrationEffect.startComposition().addPrimitive(primitive, scale).compose())
        }.isSuccess
    }

    private fun playFallback(constant: Int) {
        val c = if (Build.VERSION.SDK_INT < 30 && (constant == FEEDBACK_CONFIRM || constant == FEEDBACK_REJECT)) {
            HapticFeedbackConstants.CONTEXT_CLICK
        } else {
            constant
        }
        view.performHapticFeedback(c)
    }

    private fun feedbackEnabled(): Boolean = runCatching {
        Settings.System.getInt(view.context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0
    }.getOrDefault(true)
}

private fun Context.vibrator(): Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
    getSystemService(VibratorManager::class.java)?.defaultVibrator
} else {
    @Suppress("DEPRECATION")
    getSystemService(Vibrator::class.java)
}

/** The haptics for this part of the UI. */
val LocalHaptics = staticCompositionLocalOf<MotionHaptics?> { null }

/** Remembers a [MotionHaptics] for the current view. */
@Composable
fun rememberMotionHaptics(): MotionHaptics {
    val view = LocalView.current
    return remember(view) { MotionHaptics(view) }
}

/** Provides [LocalHaptics] to [content] (once, near the root). */
@Composable
fun ProvideHaptics(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalHaptics provides rememberMotionHaptics()) { content() }
}

/** The haptics here: the provided one, or one for this view. */
@Composable
fun haptics(): MotionHaptics = LocalHaptics.current ?: rememberMotionHaptics()

/**
 * How strongly [token] plays: its own scale times the global [intensity], and for speed-faded tokens
 * up to 70 % lighter between [slowSpeed] and [fastSpeed] (iOS's lighter ticks on fast gestures).
 */
internal fun hapticScale(
    token: HapticToken,
    intensity: Float,
    speed: Float,
    slowSpeed: Float,
    fastSpeed: Float,
): Float {
    var scale = (token.scale * intensity).coerceIn(0f, 1f)
    if (token.speedFade && fastSpeed > slowSpeed) {
        val t = ((abs(speed) - slowSpeed) / (fastSpeed - slowSpeed)).coerceIn(0f, 1f)
        scale *= 1f - 0.7f * t
    }
    return scale
}
