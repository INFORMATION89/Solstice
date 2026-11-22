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

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import org.breezyweather.R
import org.breezyweather.ui.expressive.components.ClockIcon
import org.breezyweather.ui.expressive.theme.Space
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs

/**
 * A place's local time (agreed 2026-11-21: one system for home, windows, postcards and search): the
 * time there, how far it is from yours, and the hands for its clock icon.
 */
internal data class PlaceTime(
    /** "2:33 PM" (or "14:33"), following the system's 12/24-hour setting. */
    val time: String,
    /** "+1 h", "−5:30 h"; null when it's the same as yours. */
    val offset: String?,
    val hour: Int,
    val minute: Int,
    /** "2:33 PM, +1 h": the time and offset as one label; null when it's the same as yours. */
    val label: String?,
)

/** [zone]'s time at [now], and how far it is from the phone's. */
internal fun placeTime(context: Context, zone: TimeZone, now: Long): PlaceTime {
    val calendar = Calendar.getInstance(zone).apply { timeInMillis = now }
    val time = time(context, Date(now), zone)
    val offset = timeDifference(context, zone, now)
    return PlaceTime(
        time = time,
        offset = offset,
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE),
        label = offset?.let { context.getString(R.string.place_time, time, it) }
    )
}

/** How far [zone] is from the phone's at [now] ("+1 h", "−5:30 h"); null if none. */
internal fun timeDifference(context: Context, zone: TimeZone, now: Long): String? {
    val minutes = (zone.getOffset(now) - TimeZone.getDefault().getOffset(now)) / 60_000
    if (minutes == 0) return null
    val sign = context.getString(if (minutes > 0) R.string.place_offset_ahead else R.string.place_offset_behind)
    val hours = abs(minutes) / 60
    val rest = abs(minutes) % 60
    return if (rest == 0) {
        context.getString(R.string.place_offset_hours, sign, hours)
    } else {
        context.getString(R.string.place_offset_hours_minutes, sign, hours, rest)
    }
}

/** A place's time as a line: its live clock icon and "2:33 PM, +1 h" (or just [text] when given). */
@Composable
internal fun PlaceTimeLabel(
    time: PlaceTime,
    ink: Color,
    style: TextStyle,
    modifier: Modifier = Modifier,
    text: String = time.label ?: time.time,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        ClockIcon(time.hour, time.minute, ink)
        Spacer(Modifier.width(Space.S1))
        Text(text, style = style, color = ink)
    }
}
