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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.expressive.components.LocalSkyGlass
import org.breezyweather.ui.expressive.components.glass
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Space
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------------------------
// Shared bits

internal fun degrees(context: Context, t: org.breezyweather.unit.temperature.Temperature): String {
    val unit = SettingsManager.getInstance(context).getTemperatureUnit(context)
    return "${t.toDouble(unit).roundToInt()}°"
}

internal fun feelsLikeLine(context: Context, location: Location): String? {
    val current = location.weather?.current ?: return null
    val feels = current.temperature?.feelsLikeTemperature ?: return null
    val actual = current.temperature?.temperature ?: return null
    // With feels-like as the main number, this line gives the actual temperature instead.
    if (ShellPrefs.feelsLikeMain(context)) {
        if (degrees(context, feels) == degrees(context, actual)) return null
        return context.getString(R.string.shell_actual_temperature, degrees(context, actual))
    }
    if (degrees(context, feels) == degrees(context, actual)) return null
    return context.getString(R.string.shell_feels_like, degrees(context, feels))
}

internal fun time(
    context: Context,
    date: Date,
    zone: TimeZone,
    hourOnly: Boolean = false,
    noPeriod: Boolean = false,
): String {
    val is24 = android.text.format.DateFormat.is24HourFormat(context)
    val pattern = when {
        hourOnly && is24 -> "HH"
        hourOnly -> "h a"
        is24 -> "HH:mm"
        noPeriod -> "h:mm"
        else -> "h:mm a"
    }
    return SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = zone }.format(date)
}

/**
 * A time inside a sentence: "11 PM" on the hour, "11:40 PM" off it (or "23" / "23:40" on the 24-hour
 * clock, following the system setting like [time]).
 */
internal fun sentenceTime(context: Context, time: Long, zone: TimeZone): String {
    val onHour = java.util.Calendar.getInstance(zone).apply { timeInMillis = time }.get(java.util.Calendar.MINUTE) == 0
    return time(context, Date(time), zone, hourOnly = onHour)
}

/** How a value stands out in a summary sentence: bold and underlined (future links to its detail). */
internal val ValueStyle = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)

/**
 * Sentences joined into one paragraph, each a template (`%1$s`, `%2$s`…) and its values, the values
 * drawn in [ValueStyle]. Shared by every card that writes its details as sentences.
 */
internal fun sentences(parts: List<Pair<String, List<String>>>): AnnotatedString = buildAnnotatedString {
    parts.forEachIndexed { i, (template, args) ->
        if (i > 0) append(" ")
        var last = 0
        Regex("%(\\d)\\\$s").findAll(template).forEach { m ->
            append(template.substring(last, m.range.first))
            withStyle(ValueStyle) { append(args[m.groupValues[1].toInt() - 1]) }
            last = m.range.last + 1
        }
        append(template.substring(last))
    }
}

private fun dayName(context: Context, date: Date, zone: TimeZone): String =
    SimpleDateFormat("EEE d", context.resources.configuration.locales[0]).apply { timeZone = zone }.format(date)

/** "two", "three"… for small counts in sentences; digits otherwise. */
internal fun countWord(context: Context, n: Int): String =
    context.resources.getStringArray(R.array.count_words).getOrNull(n) ?: n.toString()
