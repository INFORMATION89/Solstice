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

import androidx.compose.runtime.Immutable

/**
 * Effects drawn into the sky (F4, agreed 2026-11-21): built into the sky system, not beside it. The
 * animated sky draws them in its one full-screen shader pass; the static sky bakes them into its
 * cached image. Stars now; rain, snow, fog and lightning later plug in the same way (an amount here,
 * a function and uniforms in the shader, a baked fallback).
 */
@Immutable
data class SkyEffects(
    /** How many stars show, 0..1 (0: none). */
    val stars: Float = 0f,
    /** Stars twinkle (a slow, subtle brightness wave per star). Built, but off until agreed. */
    val twinkle: Boolean = false,
) {
    companion object {
        val None = SkyEffects()

        /**
         * Twinkling stars: built and ready, but **off until the user agrees** (F4; proposal: slow
         * and subtle, a few stars at a time).
         */
        const val TWINKLE_AGREED = false

        /**
         * The effects for [state] with [weather]: stars come out as the sun goes down (full once
         * it's a little below the horizon), only on a clear night, and fewer when it's partly
         * cloudy.
         */
        fun of(state: SkyState, weather: SkyWeather): SkyEffects {
            val night = ((-state.sunHeight - 0.02f) / 0.25f).coerceIn(0f, 1f)
            val clear = when (weather) {
                SkyWeather.CLEAR -> 1f
                SkyWeather.PARTLY_CLOUDY -> 0.45f
                else -> 0f
            }
            return SkyEffects(stars = night * clear, twinkle = TWINKLE_AGREED)
        }
    }
}
