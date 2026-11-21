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

import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonPosition
import java.util.Date

/**
 * How the moon is turned in a place's sky (one calculation for the moon card and the Forecast
 * diagram, 2026-11-21): the bright limb's position angle, and the parallactic angle at the observer,
 * eased to upright near the horizon and averaged over the hours around the time so it never rocks.
 */
internal object MoonSky {
    private const val HOUR = 3_600_000L

    /** The bright limb's position angle at [at] (degrees, eastward from north; negative while waxing). */
    fun limbAngle(at: Date): Double = MoonIllumination.compute().on(at).execute().angle

    /** Upright for a place at [latitude]: the moon is "upside down" in the southern hemisphere. */
    fun rest(latitude: Double): Double = if (latitude < 0) 180.0 else 0.0

    /** The tilt at [time], relative to [rest]: the real parallactic angle while the moon is well up. */
    private fun tiltAt(time: Long, latitude: Double, longitude: Double): Double {
        val rest = rest(latitude)
        val p = MoonPosition.compute().on(Date(time)).at(latitude, longitude).execute()
        val up = ((p.altitude + 5.0) / 20.0).coerceIn(0.0, 1.0)
        var q = p.parallacticAngle
        if (q - rest > 180) q -= 360
        if (q - rest < -180) q += 360
        return (q - rest) * up * up * (3 - 2 * up)
    }

    /** The hour [time] falls in: the averaged tilt only changes once an hour has gone by. */
    fun hourOf(time: Long): Long = (time / HOUR) * HOUR

    /** The parallactic angle to draw for the hour starting at [hour], averaged over ±4 hours. */
    fun parallactic(hour: Long, latitude: Double, longitude: Double): Double =
        rest(latitude) + (-4..4).map { tiltAt(hour + it * HOUR, latitude, longitude) }.average()

    /**
     * How far to turn a phase icon drawn the standard way (lit on the right while waxing, on the left
     * while waning), in degrees clockwise on screen, so it shows the moon as it really looks: the
     * same orientation the moon card's shader draws ([MoonRenderer]: the bright limb at
     * limb − parallactic on screen).
     */
    fun iconRotation(phaseAngle: Int, limbAngle: Double, parallactic: Double): Float {
        val waxing = ((phaseAngle % 360) + 360) % 360 < 180
        val standard = if (waxing) -90.0 else 90.0
        return (standard - limbAngle + parallactic).toFloat()
    }
}
