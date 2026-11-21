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

package org.breezyweather.ui.expressive.sky

import org.breezyweather.ui.expressive.sky.SkyVisibility.Sample
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyVisibilityTest {

    private val hour = 3_600_000L

    /** A night of hourly samples: [up] per hour, and [cloud] per hour (0..1, or null). */
    private fun night(up: List<Boolean>, cloud: List<Float?>) = up.indices.map { Sample(it * hour, up[it], cloud[it]) }

    @Test
    fun `clear all night`() {
        val s = night(List(8) { true }, List(8) { 0.1f })
        assertEquals(Tonight.Clear(rises = null), SkyVisibility.decide(s))
    }

    @Test
    fun `clear, but it rises late`() {
        val s = night(List(3) { false } + List(5) { true }, List(8) { 0.1f })
        assertEquals(Tonight.Clear(rises = 3 * hour), SkyVisibility.decide(s))
    }

    @Test
    fun `clouds clear later`() {
        val s = night(List(8) { true }, listOf(0.9f, 0.9f, 0.8f, 0.2f, 0.1f, 0.1f, 0.2f, 0.1f))
        assertEquals(Tonight.AfterClouds(3 * hour), SkyVisibility.decide(s))
    }

    @Test
    fun `clouds come in later`() {
        val s = night(List(8) { true }, listOf(0.1f, 0.2f, 0.7f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f))
        assertEquals(Tonight.UntilClouds(2 * hour), SkyVisibility.decide(s))
    }

    @Test
    fun `clouds come and go`() {
        val s = night(List(6) { true }, listOf(0.9f, 0.1f, 0.9f, 0.1f, 0.9f, 0.1f))
        assertEquals(Tonight.Breaks(1 * hour), SkyVisibility.decide(s))
    }

    @Test
    fun `overcast the whole time it is up`() {
        val s = night(List(8) { it > 2 }, listOf(0.1f, 0.1f, 0.1f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f))
        assertEquals(Tonight.Hidden, SkyVisibility.decide(s))
    }

    @Test
    fun `not up while it is dark, or too faint to see`() {
        assertEquals(Tonight.NotUp, SkyVisibility.decide(night(List(4) { false }, List(4) { 0f })))
        assertEquals(Tonight.TooFaint, SkyVisibility.decide(night(List(4) { true }, List(4) { 0f }), faint = true))
    }

    @Test
    fun `no answer without dark hours or cloud data`() {
        assertNull(SkyVisibility.decide(emptyList()))
        assertNull(SkyVisibility.decide(night(List(4) { true }, listOf(null, null, null, 0.1f))))
    }

    @Test
    fun `a real night has dark hours, and the pole in summer has none`() {
        // 2026-11-21 18:00 UTC, Mexico City: dark from about 00:30 to 12:00 UTC.
        val mexico = SkyVisibility.samples(1_795_284_000_000L, 19.43, -99.13, emptyList()) { 10.0 }
        assertTrue(mexico.size in 55..80, "about ten and a half dark hours, got ${mexico.size} samples")
        // 2026-06-18 at the North Pole: the sun never sets.
        assertTrue(SkyVisibility.samples(1_781_784_000_000L, 89.9, 0.0, emptyList()) { 10.0 }.isEmpty())
    }

    @Test
    fun `each answer picks its own text`() {
        val texts = VisibilityTexts(1, 2, 3, 4, 5, 6, 7)
        val clock = { t: Long -> "t$t" }
        assertEquals(1 to emptyList<String>(), SkyVisibility.sentence(Tonight.Clear(null), texts, clock))
        assertEquals(2 to listOf("t9"), SkyVisibility.sentence(Tonight.Clear(9), texts, clock))
        assertEquals(2 to listOf("t9"), SkyVisibility.sentence(Tonight.AfterClouds(9), texts, clock))
        assertEquals(3 to listOf("t9"), SkyVisibility.sentence(Tonight.UntilClouds(9), texts, clock))
        assertEquals(4 to listOf("t9"), SkyVisibility.sentence(Tonight.Breaks(9), texts, clock))
        assertEquals(7 to emptyList<String>(), SkyVisibility.sentence(Tonight.TooFaint, texts, clock))
    }
}
