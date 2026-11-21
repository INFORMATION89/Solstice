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

import android.animation.ValueAnimator
import android.os.PowerManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/**
 * The clock for ambient animation (F4, agreed 2026-11-21): a slow drift, twinkling, weather effects,
 * anything that just keeps moving rather than responding to a gesture. Any app's ambient animation
 * needs the same rules, so they live here:
 * - **capped frames per second** ([fps]): ambient motion doesn't need the display's full rate, and
 *   every frame of a full-screen shader costs battery;
 * - **lifecycle-aware:** it only ticks while the screen is resumed;
 * - **paused while unseen:** [running] (read every frame) stops it when whatever it drives is covered;
 * - its time is **wall-clock based**, so the drift carries on where it was after a pause.
 *
 * Use it with [rememberAmbientMotionAllowed]: when that's false (battery saver, animations off),
 * draw a still frame instead.
 */
@Composable
fun rememberAmbientClock(fps: Int = 30, running: () -> Boolean = { true }): State<Float> {
    val seconds = remember { mutableFloatStateOf(ambientSeconds()) }
    val isRunning = rememberUpdatedState(running)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val frameNanos = 1_000_000_000L / fps.coerceIn(1, 120)
    LaunchedEffect(lifecycle, frameNanos) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var last = 0L
            while (true) {
                withFrameNanos { now ->
                    if (now - last >= frameNanos && isRunning.value()) {
                        last = now
                        seconds.floatValue = ambientSeconds()
                    }
                }
            }
        }
    }
    return seconds
}

/** Whether ambient motion should run: not in battery saver, not with the system's animations off. */
@Composable
fun rememberAmbientMotionAllowed(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val powerSave = context.getSystemService(PowerManager::class.java)?.isPowerSaveMode == true
        ValueAnimator.areAnimatorsEnabled() && !powerSave
    }
}

/** Seconds within the current hour, from the wall clock (small enough for a shader's float). */
private fun ambientSeconds() = (System.currentTimeMillis() % 3_600_000L) / 1000f
