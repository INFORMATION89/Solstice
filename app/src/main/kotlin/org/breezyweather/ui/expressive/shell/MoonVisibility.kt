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

import breezyweather.domain.weather.model.Hourly
import org.breezyweather.R
import org.breezyweather.ui.expressive.sky.SkyVisibility
import org.breezyweather.ui.expressive.sky.Tonight
import org.breezyweather.ui.expressive.sky.VisibilityTexts
import org.shredzone.commons.suncalc.MoonPosition
import java.util.Date

/**
 * The moon's part of "Will I see it tonight?" (agreed 2026-11-21; the shared logic is [SkyVisibility]):
 * its altitude, when it's too thin to see, and its texts.
 */
internal object MoonVisibility {
    /** Under this much lit, the moon is too thin to see. */
    const val MIN_LIT = 0.03f

    val texts = VisibilityTexts(
        visible = R.string.moon_tonight_visible,
        from = R.string.moon_tonight_from,
        until = R.string.moon_tonight_until,
        atTimes = R.string.moon_tonight_at_times,
        hidden = R.string.moon_tonight_hidden,
        notUp = R.string.moon_tonight_not_up,
        tooFaint = R.string.moon_tonight_too_thin
    )

    /** Tonight's answer for the moon at a place, from [now], with [lit] the moon's lit fraction. */
    fun tonight(now: Long, latitude: Double, longitude: Double, hourly: List<Hourly>, lit: Float): Tonight? =
        SkyVisibility.decide(
            SkyVisibility.samples(now, latitude, longitude, hourly) { t ->
                MoonPosition.compute().on(Date(t)).at(latitude, longitude).execute().altitude
            },
            faint = lit < MIN_LIT
        )
}
