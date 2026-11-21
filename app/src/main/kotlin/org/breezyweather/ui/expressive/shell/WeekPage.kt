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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.ui.expressive.components.CardMaterial
import org.breezyweather.ui.expressive.components.SolsticeCard
import org.breezyweather.ui.expressive.sky.SkyState
import org.breezyweather.ui.expressive.sky.SkyWeather
import org.breezyweather.ui.expressive.sky.paletteAt
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.SolsticePalette
import org.breezyweather.ui.expressive.theme.Space
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ---------------------------------------------------------------------------------------------
// Outlook (next days)

/** Plain page (white/black); each day's card wears its own weather, as a daytime sky. */
@Composable
internal fun DaysPage(
    location: Location?,
    onPage: Color,
    page: Color,
    listState: LazyListState,
    // Ready for per-day floating cards (a tapped pill, or a day named in the summary).
    onDay: (Int) -> Unit = {},
    pageFade: () -> Float,
) {
    val context = LocalContext.current
    val days = location?.weather?.dailyForecastStartingToday.orEmpty().take(7)
    // Edge to edge: the list scrolls under the status bar and the toolbar, with a progressive blur
    // at both edges (also a hint that there's more to scroll).
    PageScaffold(
        title = stringResource(R.string.shell_tab_days),
        onPage = onPage,
        page = page,
        listState = listState,
        pageFade = pageFade,
        spacing = Space.S3
    ) {
        if (location != null && days.isNotEmpty()) {
            item { WeekSummary(location, days, onPage, onDay) }
            item { WeekPills(location, days, onPage, onDay) }
        }
    }
}

private fun isWet(w: SkyWeather) = w == SkyWeather.RAIN || w == SkyWeather.THUNDERSTORM || w == SkyWeather.SNOW

/**
 * A dry week's headline (agreed 2026-11-21): the trend of the highs, the first three days against the
 * last three; a change of [TREND_DEGREES] °C or more is warming up or getting colder, otherwise steady.
 */
internal fun weekTrend(highs: List<Double?>): Int {
    val known = highs.filterNotNull()
    if (known.size < 4) return R.string.week_steady
    val n = minOf(3, known.size / 2)
    val change = known.takeLast(n).average() - known.take(n).average()
    return when {
        change >= TREND_DEGREES -> R.string.week_warming
        change <= -TREND_DEGREES -> R.string.week_cooling
        else -> R.string.week_steady
    }
}

private const val TREND_DEGREES = 3.0

/**
 * The week in words (agreed 2026-11-18, design B): a headline and a sentence. A condition named in
 * the text has its small icon inline ("Expect [rain] on Saturday"); temperatures are bold and
 * underlined, ready to become links to that day's card.
 */
@Composable
private fun WeekSummary(
    location: Location,
    days: List<breezyweather.domain.weather.model.Daily>,
    onPage: Color,
    onDay: (Int) -> Unit,
) {
    val context = LocalContext.current
    val highs = days.map { it.day?.temperature?.temperature }
    val lows = days.map { it.night?.temperature?.temperature }
    val hottest = highs.indices.filter { highs[it] != null }.maxByOrNull { highs[it]!!.inCelsius }
    val coldest = lows.indices.filter { lows[it] != null }.minByOrNull { lows[it]!!.inCelsius }
    val wetDays = days.indices.filter { isWet(SkyWeather.of(days[it].day?.weatherCode)) }
    fun name(i: Int) = if (i == 0) context.getString(R.string.shell_today) else dayLong(days[i].date, location.timeZone)
    val headline = when {
        // A dry week says how it feels instead (user 2026-11-21: "no rain expected" isn't useful).
        wetDays.isEmpty() -> context.getString(weekTrend(highs.map { it?.inCelsius }))
        wetDays.size == 1 && wetDays.first() == 0 -> context.getString(R.string.week_one_wet_today)
        wetDays.size == 1 -> context.getString(R.string.week_one_wet, name(wetDays.first()))
        wetDays.size >= days.size - 1 -> context.getString(R.string.week_mostly_wet)
        else -> context.getString(R.string.week_some_wet, countWord(context, wetDays.size))
    }
    val wetIcon = wetDays.firstOrNull()?.let { SkyWeather.of(days[it].day?.weatherCode) }
    val body = buildAnnotatedString {
        if (wetIcon != null) {
            append(context.getString(R.string.week_expect) + " ")
            appendInlineContent("cond", "[]")
            append(" " + (days[wetDays.first()].day?.weatherText?.lowercase() ?: "") + " ")
            append(context.getString(R.string.week_on, name(wetDays.first())))
            append(". ")
        }
        if (hottest != null) {
            append(context.getString(R.string.week_warmest, name(hottest)) + " ")
            withStyle(ValueStyle) { append(degrees(context, highs[hottest]!!)) }
            append(".")
        }
        if (coldest != null) {
            if (hottest != null) append(" ")
            append(
                if (coldest == 0) {
                    context.getString(R.string.week_coldest_tonight)
                } else {
                    context.getString(R.string.week_coldest_night, name(coldest))
                } + " "
            )
            withStyle(ValueStyle) { append(degrees(context, lows[coldest]!!)) }
            append(".")
        }
    }
    val inline = mapOf(
        "cond" to InlineTextContent(
            Placeholder(18.sp, 18.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            Icon(
                hourIcon(wetIcon ?: SkyWeather.RAIN, false),
                contentDescription = null,
                tint = onPage.copy(alpha = 0.85f)
            )
        }
    )
    SolsticeCard(CardMaterial.Paper, onPage) {
        Text(headline, color = onPage, style = MaterialTheme.typography.titleLargeEmphasized)
        Spacer(Modifier.height(Space.S2))
        Text(
            body,
            inlineContent = inline,
            color = onPage.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * The days as vertical pills (design B): each placed at its low-to-high range on a shared scale,
 * warm at the top fading to cool, the colours following how hot or cold the day is. The condition
 * icon and day name sit below. Wet days are tinted in the label. Tapping a day is wired for its
 * future card.
 */
@Composable
private fun WeekPills(
    location: Location,
    days: List<breezyweather.domain.weather.model.Daily>,
    onPage: Color,
    onDay: (Int) -> Unit,
) {
    val context = LocalContext.current
    val highs = days.map { it.day?.temperature?.temperature?.inCelsius }
    val lows = days.map { it.night?.temperature?.temperature?.inCelsius }
    val top = highs.filterNotNull().maxOrNull() ?: return
    val bottom = lows.filterNotNull().minOrNull() ?: return
    val span = (top - bottom).coerceAtLeast(1.0)
    val pillArea = 84.dp
    SolsticeCard(CardMaterial.Paper, onPage) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEachIndexed { i, day ->
                val hi = highs[i] ?: return@forEachIndexed
                val lo = lows[i] ?: hi
                val weather = SkyWeather.of(day.day?.weatherCode)
                val wet = isWet(weather)
                // Material 3 surface button: ripple, press and button semantics.
                Surface(onClick = { onDay(i) }, shape = CornerShapes.S, color = Color.Transparent) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = Space.Half, vertical = Space.S1)
                    ) {
                        Text(
                            day.day?.temperature?.temperature?.let { degrees(context, it) } ?: "",
                            color = onPage,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = Space.S2)
                        )
                        Box(Modifier.height(pillArea).width(22.dp)) {
                            val topFrac = ((top - hi) / span).toFloat()
                            val heightFrac = ((hi - lo) / span).toFloat().coerceAtLeast(0.12f)
                            Box(
                                Modifier
                                    .padding(top = pillArea * topFrac)
                                    .fillMaxWidth()
                                    .height(pillArea * heightFrac)
                                    .clip(CornerShapes.S)
                                    .background(Brush.verticalGradient(listOf(tempColor(hi), tempColor(lo))))
                            )
                        }
                        Text(
                            day.night?.temperature?.temperature?.let { degrees(context, it) } ?: "",
                            color = onPage.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = Space.S2)
                        )
                        Icon(
                            hourIcon(weather, false),
                            contentDescription = day.day?.weatherText,
                            tint = if (wet) SolsticePalette.Wet else onPage.copy(alpha = 0.75f),
                            modifier = Modifier.padding(top = Space.S2, bottom = Space.Half).size(Sizes.IconMedium)
                        )
                        Text(
                            if (i == 0) stringResource(R.string.shell_today) else dayShort(day.date, location.timeZone),
                            color = if (wet) SolsticePalette.Wet else onPage.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

/** Cold blue to warm orange by temperature (°C). */
private fun tempColor(c: Double): Color {
    val stops = listOf(
        -5.0 to SolsticePalette.TempFreezing,
        8.0 to SolsticePalette.TempCold,
        16.0 to SolsticePalette.TempMild,
        22.0 to SolsticePalette.TempWarm,
        30.0 to SolsticePalette.TempHot
    )
    if (c <= stops.first().first) return stops.first().second
    if (c >= stops.last().first) return stops.last().second
    val k = stops.indexOfFirst { it.first >= c }
    val (a, ca) = stops[k - 1]
    val (b, cb) = stops[k]
    return lerp(ca, cb, ((c - a) / (b - a)).toFloat())
}

private fun dayShort(date: java.util.Date, zone: java.util.TimeZone): String =
    SimpleDateFormat("EEE", Locale.getDefault()).apply { timeZone = zone }.format(date)

private fun dayLong(date: java.util.Date, zone: java.util.TimeZone): String =
    SimpleDateFormat("EEEE", Locale.getDefault()).apply { timeZone = zone }.format(date)

/** A day's weather as a mid-morning sky (sun high on the right), for cards. */
private fun daySky(weather: SkyWeather) =
    SkyState(paletteAt(weather, 0.8f), sunX = 0.82f, sunY = 0.3f, sunHeight = 0.8f)
