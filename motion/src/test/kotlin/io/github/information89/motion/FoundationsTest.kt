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

import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/** The pure logic of the 2026-11-21 pieces: the dial's scale, haptic strength, the new tokens. */
class FoundationsTest {

    @Test
    fun `a dial scale has a stop per step, both ends included`() {
        val scale = DialScale(min = -14f, max = 14f, step = 0.25f)
        val stops = scale.stops
        stops.size shouldBe 113
        stops.first() shouldBe -14f
        stops.last() shouldBe (14f plusOrMinus 0.0001f)
    }

    @Test
    fun `a dial that doesn't snap only rests at its ends`() {
        DialScale(min = 0f, max = 10f, step = 1f, snaps = false).stops shouldBe listOf(0f, 10f)
    }

    @Test
    fun `a dial scale must make sense`() {
        (
            runCatching {
                DialScale(min = 5f, max = 1f, step = 1f)
            }.exceptionOrNull() is IllegalArgumentException
            ) shouldBe
            true
        (
            runCatching {
                DialScale(min = 0f, max = 1f, step = 0f)
            }.exceptionOrNull() is IllegalArgumentException
            ) shouldBe
            true
    }

    @Test
    fun `slow ticks play at full strength, fast ones up to 70 percent lighter`() {
        val tick = HapticToken("t", HapticPrimitive.TICK, 0.5f, 0, speedFade = true)
        hapticScale(tick, 1f, speed = 1f, slowSpeed = 2f, fastSpeed = 12f) shouldBe (0.5f plusOrMinus 0.0001f)
        hapticScale(tick, 1f, speed = 12f, slowSpeed = 2f, fastSpeed = 12f) shouldBe (0.15f plusOrMinus 0.0001f)
        hapticScale(tick, 1f, speed = -7f, slowSpeed = 2f, fastSpeed = 12f) shouldBe (0.325f plusOrMinus 0.0001f)
    }

    @Test
    fun `tokens without speed fade ignore speed, and intensity scales everything`() {
        val press = HapticToken("p", HapticPrimitive.CLICK, 0.8f, 0)
        hapticScale(press, 1f, speed = 50f, slowSpeed = 2f, fastSpeed = 12f) shouldBe (0.8f plusOrMinus 0.0001f)
        hapticScale(press, 0.5f, speed = 0f, slowSpeed = 2f, fastSpeed = 12f) shouldBe (0.4f plusOrMinus 0.0001f)
        hapticScale(press, 2f, speed = 0f, slowSpeed = 2f, fastSpeed = 12f) shouldBe 1f
    }

    @Test
    fun `apple's springs as tokens`() {
        Motion.smooth.damping shouldBe 1f
        Motion.brisk.damping shouldBe 0.78f
        Motion.momentum.damping shouldBe 0.8f
        Motion.smooth.response shouldBe 0.4f
    }
}
