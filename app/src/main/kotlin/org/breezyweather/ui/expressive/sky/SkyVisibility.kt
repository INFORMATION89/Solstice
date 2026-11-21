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

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import breezyweather.domain.weather.model.Hourly
import org.shredzone.commons.suncalc.SunPosition
import java.util.Date

/**
 * "Will I see it tonight?" for anything in the night sky (the moon first; planets, meteor showers,
 * the ISS, auroras later): whether it will be up during tonight's dark hours with clouds thin enough
 * to see it. A card gives it the object's altitude over time and its texts; the rest is shared.
 */
sealed interface Tonight {
    /** Up and clear the whole time it's up tonight. [rises]: when it comes up, if after dark. */
    data class Clear(val rises: Long?) : Tonight

    /** Hidden by clouds at first, then clear from [clearsAt] for the rest of the time it's up. */
    data class AfterClouds(val clearsAt: Long) : Tonight

    /** Clear at first, then hidden by clouds from [cloudsAt]. */
    data class UntilClouds(val cloudsAt: Long) : Tonight

    /** Clouds come and go; first seen at [firstSeen]. */
    data class Breaks(val firstSeen: Long) : Tonight

    /** Up, but clouds hide it the whole time. */
    data object Hidden : Tonight

    /** Not up at all while it's dark. */
    data object NotUp : Tonight

    /** Too faint to see tonight (e.g. the moon near new); the card decides when. */
    data object TooFaint : Tonight
}

/** What counts as dark, up and clear. Defaults suit the moon; each object can tune its own. */
@Immutable
data class VisibilityRules(
    /** Cloud cover (0..1) under which the sky counts as clear enough. */
    val clearBelow: Float = 0.4f,
    /** The sun counts as down below this altitude, in degrees (-6: civil twilight's end). */
    val darkSunAltitude: Double = -6.0,
    /** The object counts as up above this altitude, in degrees (clear of haze and buildings). */
    val upAltitude: Double = 3.0,
    /** How often the night is checked. */
    val stepMillis: Long = 10 * 60_000L,
)

/** One card's texts for each answer; [from], [until] and [atTimes] take a time as `%1$s`. */
@Immutable
data class VisibilityTexts(
    @StringRes val visible: Int,
    @StringRes val from: Int,
    @StringRes val until: Int,
    @StringRes val atTimes: Int,
    @StringRes val hidden: Int,
    @StringRes val notUp: Int,
    @StringRes val tooFaint: Int,
)

object SkyVisibility {
    private const val HOUR = 3_600_000L

    /** One moment of tonight: whether the object is up, and the cloud cover then (null: unknown). */
    data class Sample(val time: Long, val up: Boolean, val cloud: Float?)

    /**
     * The answer from tonight's dark-hour [samples] (in time order). [faint]: the object is too faint
     * tonight whatever the weather. Null when it can't say (no dark hours, or too little cloud data
     * while it's up: better nothing than a guess).
     */
    fun decide(samples: List<Sample>, faint: Boolean = false, rules: VisibilityRules = VisibilityRules()): Tonight? {
        if (samples.isEmpty()) return null
        if (faint) return Tonight.TooFaint
        val up = samples.filter { it.up }
        if (up.isEmpty()) return Tonight.NotUp
        val known = up.filter { it.cloud != null }
        if (known.size * 2 < up.size) return null
        val seen = known.map { it.cloud!! < rules.clearBelow }
        if (seen.none { it }) return Tonight.Hidden
        val changes = seen.zipWithNext().count { (a, b) -> a != b }
        return when {
            changes == 0 -> Tonight.Clear(rises = up.first().time.takeIf { it > samples.first().time })
            changes == 1 && !seen.first() -> Tonight.AfterClouds(known[seen.indexOf(true)].time)
            changes == 1 -> Tonight.UntilClouds(known[seen.indexOf(false)].time)
            else -> Tonight.Breaks(known[seen.indexOf(true)].time)
        }
    }

    /**
     * Tonight's dark-hour samples at a place, from [now]: the night we're in, or the next one starting
     * within a day. [altitude] is the object's altitude in degrees at a time; cloud cover comes from
     * [hourly], by the hour.
     */
    fun samples(
        now: Long,
        latitude: Double,
        longitude: Double,
        hourly: List<Hourly>,
        rules: VisibilityRules = VisibilityRules(),
        altitude: (Long) -> Double,
    ): List<Sample> {
        val clouds = hourly.mapNotNull { h ->
            h.cloudCover?.let { (h.date.time / HOUR) * HOUR to (it.inPercent / 100.0).toFloat() }
        }.toMap()
        fun dark(t: Long) =
            SunPosition.compute().on(Date(t)).at(latitude, longitude).execute().altitude < rules.darkSunAltitude
        var t = now
        val limit = now + 24 * HOUR
        while (t < limit && !dark(t)) t += rules.stepMillis
        if (t >= limit) return emptyList()
        val end = t + 24 * HOUR
        return buildList {
            while (t < end && dark(t)) {
                add(Sample(t, altitude(t) > rules.upAltitude, clouds[(t / HOUR) * HOUR]))
                t += rules.stepMillis
            }
        }
    }

    /** The sentence for [answer] in a card's [texts]: a string resource and its values ([clock] formats a time). */
    fun sentence(answer: Tonight, texts: VisibilityTexts, clock: (Long) -> String): Pair<Int, List<String>> =
        when (answer) {
            is Tonight.Clear -> answer.rises?.let { texts.from to listOf(clock(it)) } ?: (texts.visible to emptyList())
            is Tonight.AfterClouds -> texts.from to listOf(clock(answer.clearsAt))
            is Tonight.UntilClouds -> texts.until to listOf(clock(answer.cloudsAt))
            is Tonight.Breaks -> texts.atTimes to listOf(clock(answer.firstSeen))
            Tonight.Hidden -> texts.hidden to emptyList()
            Tonight.NotUp -> texts.notUp to emptyList()
            Tonight.TooFaint -> texts.tooFaint to emptyList()
        }
}
