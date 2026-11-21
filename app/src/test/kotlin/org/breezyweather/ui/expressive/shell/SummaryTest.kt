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

import breezyweather.domain.weather.reference.WeatherCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SummaryTest {

    private data class Day(val alert: Boolean, val rain: Boolean, val high: Int, val uv: Int)

    private val summary = Summary<Day>(
        listOf(
            SummaryRule { d -> if (d.alert) SummaryLine("Alert.", topic = "alert") else null },
            SummaryRule { d -> if (d.rain) SummaryLine("Rain from %1\$s.", listOf("4 PM"), topic = "rain") else null },
            SummaryRule { d -> if (d.rain) SummaryLine("Rain again later.", topic = "rain") else null },
            SummaryRule { d -> SummaryLine("Up to %1\$s.", listOf("${d.high}°"), topic = "high") },
            SummaryRule { d -> if (d.uv >= 8) SummaryLine("Strong UV.", topic = "uv") else null }
        )
    )

    @Test
    fun `the first two rules that apply win, in order`() {
        val lines = summary.lines(Day(alert = true, rain = true, high = 28, uv = 9))
        assertEquals(listOf("Alert.", "Rain from %1\$s."), lines.map { it.template })
    }

    @Test
    fun `one line per topic`() {
        val lines = summary.lines(Day(alert = false, rain = true, high = 28, uv = 9))
        assertEquals(listOf("Rain from %1\$s.", "Up to %1\$s."), lines.map { it.template })
    }

    @Test
    fun `nothing extra when only one thing applies`() {
        val quiet = Summary<Day>(listOf(SummaryRule { d -> if (d.alert) SummaryLine("Alert.") else null }))
        assertTrue(quiet.lines(Day(false, false, 20, 1)).isEmpty())
    }

    @Test
    fun `evening starts at 6 PM by default and runs past midnight`() {
        assertFalse(SummaryTime.isEvening(17))
        assertTrue(SummaryTime.isEvening(18))
        assertTrue(SummaryTime.isEvening(1))
        assertFalse(SummaryTime.isEvening(5))
        assertTrue(SummaryTime.isEvening(20, eveningHour = 20))
    }

    @Test
    fun `rain alone says rain, snow alone says snow, both say precipitation`() {
        assertEquals(Precipitation.NONE, Precipitation.of(listOf(WeatherCode.CLEAR, null)))
        assertEquals(Precipitation.RAIN, Precipitation.of(listOf(WeatherCode.CLOUDY, WeatherCode.RAIN)))
        assertEquals(Precipitation.SNOW, Precipitation.of(listOf(WeatherCode.SNOW, WeatherCode.CLOUDY)))
        assertEquals(Precipitation.MIXED, Precipitation.of(listOf(WeatherCode.RAIN, WeatherCode.SNOW)))
        assertEquals(Precipitation.MIXED, Precipitation.of(listOf(WeatherCode.SLEET)))
    }

    @Test
    fun `a week of one kind keeps its word, days that differ make it mixed`() {
        assertEquals(
            Precipitation.SNOW,
            Precipitation.combine(listOf(Precipitation.NONE, Precipitation.SNOW, Precipitation.SNOW))
        )
        assertEquals(Precipitation.MIXED, Precipitation.combine(listOf(Precipitation.RAIN, Precipitation.SNOW)))
        assertEquals(Precipitation.NONE, Precipitation.combine(listOf(Precipitation.NONE)))
    }
}
