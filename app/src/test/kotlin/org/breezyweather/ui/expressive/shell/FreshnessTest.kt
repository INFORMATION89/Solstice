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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.TimeZone

class FreshnessTest {

    private val zone = TimeZone.getTimeZone("America/Mexico_City")
    private val minute = 60_000L
    private val hour = 60 * minute

    // 2026-11-21 18:00 in Mexico City (UTC-6) = 2026-11-22 00:00 UTC.
    private val now = 1_795_305_600_000L

    @Test
    fun `under a minute is just now, under an hour counts minutes`() {
        assertEquals(Freshness.JustNow, Freshness.of(now - 30_000L, now, zone))
        assertEquals(Freshness.MinutesAgo(12), Freshness.of(now - 12 * minute, now, zone))
        assertEquals(Freshness.MinutesAgo(59), Freshness.of(now - 59 * minute - 59_000L, now, zone))
    }

    @Test
    fun `then today, yesterday, or the date, by the local day`() {
        assertEquals(Freshness.Today(now - 3 * hour), Freshness.of(now - 3 * hour, now, zone))
        // 23:00 the day before, local time.
        assertEquals(Freshness.Yesterday(now - 19 * hour), Freshness.of(now - 19 * hour, now, zone))
        assertEquals(Freshness.On(now - 50 * hour), Freshness.of(now - 50 * hour, now, zone))
    }

    @Test
    fun `a time in the future counts as just now`() {
        assertEquals(Freshness.JustNow, Freshness.of(now + 5 * minute, now, zone))
    }
}
