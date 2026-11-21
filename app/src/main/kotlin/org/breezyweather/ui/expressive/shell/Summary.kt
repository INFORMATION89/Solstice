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

/**
 * The summary engine (agreed 2026-11-21, PLAN.md "Summaries: what they say"): every summary answers
 * one question with the top one or two facts from its priority list, and says nothing extra when
 * nothing is especially useful. Shared by every page and card; each one only brings its list.
 *
 * A [SummaryRule] looks at the page's facts ([T]) and gives a [SummaryLine] when it applies, or null.
 * Rules are tried top to bottom; the first [max] that apply win.
 */
internal fun interface SummaryRule<T> {
    fun line(facts: T): SummaryLine?
}

/**
 * One fact as a sentence: a template (`%1$s`…) and its values, ready for [sentences] (values drawn
 * bold). [topic] stops two lines about the same thing (e.g. two about rain) both showing.
 */
internal data class SummaryLine(val template: String, val values: List<String> = emptyList(), val topic: String? = null)

/** A summary: its rules in priority order, and how many lines it shows at most. */
internal class Summary<T>(private val rules: List<SummaryRule<T>>, private val max: Int = 2) {
    init {
        require(max >= 1)
    }

    /** The lines to show for [facts]: at most [max], one per topic; empty when nothing applies. */
    fun lines(facts: T): List<SummaryLine> {
        val out = mutableListOf<SummaryLine>()
        val topics = mutableSetOf<String>()
        for (rule in rules) {
            if (out.size >= max) break
            val line = rule.line(facts) ?: continue
            if (line.topic != null && !topics.add(line.topic)) continue
            out += line
        }
        return out
    }

    /** The lines as [sentences] parts. */
    fun parts(facts: T): List<Pair<String, List<String>>> = lines(facts).map { it.template to it.values }
}

/** When a page switches to its evening question (agreed: 6 PM by default, adjustable). */
internal object SummaryTime {
    const val DEFAULT_EVENING_HOUR = 18

    /** Whether [hour] (0..23, local to the place) is in the evening, from [eveningHour] until 4 AM. */
    fun isEvening(hour: Int, eveningHour: Int = DEFAULT_EVENING_HOUR): Boolean = hour >= eveningHour || hour < 4
}

/**
 * Which word a summary uses for what falls (agreed 2026-11-21, the same on every page, This week
 * included): "rain" when only rain is possible, "snow" when only snow is, "precipitation" (or "rain or
 * snow") when both could fall, or sleet or hail.
 */
internal enum class Precipitation {
    NONE,
    RAIN,
    SNOW,
    MIXED,
    ;

    companion object {
        private val RAIN_CODES = setOf(
            breezyweather.domain.weather.reference.WeatherCode.RAIN,
            breezyweather.domain.weather.reference.WeatherCode.THUNDERSTORM
        )
        private val SNOW_CODES = setOf(breezyweather.domain.weather.reference.WeatherCode.SNOW)
        private val MIXED_CODES = setOf(
            breezyweather.domain.weather.reference.WeatherCode.SLEET,
            breezyweather.domain.weather.reference.WeatherCode.HAIL
        )

        /** The kind over a span of [codes] (e.g. the next hours, or one day). */
        fun of(codes: Iterable<breezyweather.domain.weather.reference.WeatherCode?>): Precipitation {
            val set = codes.filterNotNull().toSet()
            return combine(
                buildList {
                    if (set.any { it in RAIN_CODES }) add(RAIN)
                    if (set.any { it in SNOW_CODES }) add(SNOW)
                    if (set.any { it in MIXED_CODES }) add(MIXED)
                }
            )
        }

        /**
         * The kind over several spans (e.g. the week from each day's own kind): one kind if they all
         * agree, [MIXED] if they differ.
         */
        fun combine(kinds: Iterable<Precipitation>): Precipitation {
            val wet = kinds.filter { it != NONE }.toSet()
            return when (wet.size) {
                0 -> NONE
                1 -> wet.first()
                else -> MIXED
            }
        }
    }
}
