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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.ui.expressive.components.CardMaterial
import org.breezyweather.ui.expressive.components.SolsticeCard
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.sky.SkyWeather
import org.breezyweather.ui.expressive.theme.Emphasis
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.SolsticePalette
import org.breezyweather.ui.expressive.theme.Space
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import kotlin.math.roundToInt
import kotlin.math.sign

// ---- Rain ---------------------------------------------------------------------------------------

internal sealed interface RainOutlook {
    /**
     * Rain now or within the window. [kind] and [value] (minutes or hours) say what happens; [heavy]
     * is 0 light, 1 moderate, 2 heavy. [amounts] are millimetres per step, [chances] 0..1 per step
     * (null when the source has none), [labels] go under the bars, [total] is the window's sum in
     * millimetres, and [soon] (rain within the hour) tints the card light blue.
     */
    class Coming(
        val kind: RainKind,
        val value: Int,
        val heavy: Int,
        val amounts: List<Float>,
        val chances: List<Float>?,
        val labels: List<String>,
        val hourLabels: List<String>,
        val minutely: Boolean,
        val total: Double,
        val soon: Boolean,
    ) : RainOutlook

    /** No rain expected in the next [hours]. */
    class None(val hours: Int) : RainOutlook
}

internal enum class RainKind { ALL_HOUR, STOPS, STARTS, STARTS_HOURS }

/** Light under 2.5 mm/h, moderate up to 7.6 mm/h, heavy above (the usual rainfall-rate bands). */
private fun rainStrength(mmPerHour: Float): Int = when {
    mmPerHour < 2.5f -> 0
    mmPerHour < 7.6f -> 1
    else -> 2
}

internal fun rainOutlook(context: Context, location: Location, now: Long): RainOutlook? {
    val weather = location.weather ?: return null
    val hourly = weather.nextHourlyForecast
    // Probability is hourly: the next 6 hours, whichever view the amount uses.
    val hours = hourly.take(6)
    val hourChances = hours.map { it.precipitationProbability?.total?.inPercent?.let { p -> (p / 100.0).toFloat() } }
        .takeIf { it.isNotEmpty() && it.all { c -> c != null } }?.map { it!!.coerceIn(0f, 1f) }
    val hourLabels = hours.mapIndexed { i, h ->
        if (i == 0) context.getString(R.string.shell_now) else time(context, h.date, location.timeZone, hourOnly = true)
    }
    // Next hour from minute data (5-minute steps), when the source has it.
    val minutes = weather.minutelyForecastBy5Minutes.filter {
        it.date.time + it.minuteInterval * 60_000L > now
    }.take(12)
    if (minutes.size >= 6) {
        val rate = minutes.map { (it.precipitationIntensity?.inMillimeters ?: 0.0).toFloat() }
        val wet = rate.map { it > 0.05f }
        if (wet.any { it }) {
            val step = minutes.first().minuteInterval.coerceAtLeast(5)
            val (kind, value) = if (wet.first()) {
                val stop = wet.indexOfFirst { !it }
                if (stop > 0) RainKind.STOPS to stop * step else RainKind.ALL_HOUR to 0
            } else {
                RainKind.STARTS to wet.indexOfFirst { it } * step
            }
            val amounts = rate.map { it * step / 60f }
            return RainOutlook.Coming(
                kind = kind,
                value = value,
                heavy = rainStrength(rate.max()),
                amounts = amounts,
                chances = hourChances,
                hourLabels = hourLabels,
                labels = listOf(context.getString(R.string.shell_now), "10m", "20m", "30m", "40m", "50m"),
                minutely = true,
                total = amounts.sum().toDouble(),
                soon = true
            )
        }
    }
    // Otherwise the next 6 hours from the hourly forecast.
    val wetHour = hours.indexOfFirst {
        val w = SkyWeather.of(it.weatherCode)
        w == SkyWeather.RAIN || w == SkyWeather.THUNDERSTORM ||
            (it.precipitationProbability?.total?.inPercent ?: 0.0) >= 50.0
    }
    if (wetHour < 0) return RainOutlook.None(6)
    val amounts = hours.map { (it.precipitation?.total?.inMillimeters ?: 0.0).toFloat() }
    return RainOutlook.Coming(
        kind = if (wetHour == 0) RainKind.ALL_HOUR else RainKind.STARTS_HOURS,
        value = wetHour,
        heavy = rainStrength(amounts.max()),
        amounts = amounts,
        chances = hourChances,
        hourLabels = hourLabels,
        labels = hourLabels,
        minutely = false,
        total = amounts.sum().toDouble(),
        soon = wetHour <= 1
    )
}

/**
 * The rain card (refined 2026-11-19): the sentence as the header, rounded bars with a soft
 * gradient, times under them, the amount in the window, and a switch between amount and
 * probability. Light blue when the rain is within the hour.
 */
@Composable
internal fun RainCard(rain: RainOutlook.Coming, location: Location, text: Color) {
    val context = LocalContext.current
    val strength = stringArrayResource(R.array.shell_rain_strength)[rain.heavy]
    val sentence = when (rain.kind) {
        RainKind.ALL_HOUR -> stringResource(R.string.shell_rain_next_hour, strength)
        RainKind.STOPS -> stringResource(R.string.shell_rain_stops_in, rain.value)
        RainKind.STARTS -> stringResource(R.string.shell_rain_starts_in, strength, rain.value)
        RainKind.STARTS_HOURS -> pluralStringResource(R.plurals.shell_rain_in_hours, rain.value, strength, rain.value)
    }
    // Probability is hourly (the forecasts give it per hour) over the next 6 hours.
    val chances = rain.chances
    // When the amount rounds to nothing (a likely drizzle), probability says more: start there.
    val dryAmount = rain.total < 0.1
    var showChance by remember(rain.minutely) { mutableStateOf(dryAmount && chances != null) }
    SolsticeCard(CardMaterial.Glass, text, tint = if (rain.soon) SolsticePalette.RainSoon else null) {
        // No separate title: the sentence is the header.
        Text(sentence, color = text, style = MaterialTheme.typography.titleMediumEmphasized)
        Spacer(Modifier.height(Space.S1))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // The amount in the window, or the highest chance. One line, so switching never
            // resizes the card.
            val caption = if (showChance && chances != null) {
                stringResource(R.string.shell_rain_chance_up_to, (chances.max() * 100).roundToInt())
            } else if (dryAmount) {
                stringResource(R.string.shell_rain_barely)
            } else {
                stringResource(
                    if (rain.minutely) R.string.shell_rain_total_hour else R.string.shell_rain_total_hours,
                    rain.total.millimeters.formatMeasure(context)
                )
            }
            Text(
                caption,
                color = text.copy(alpha = Emphasis.Medium),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = Space.S2)
            )
            if (chances != null) {
                RainSwitch(showChance, text) { showChance = it }
            }
        }
        Spacer(Modifier.height(Space.S3))
        // The chart keeps one height in both views (room for the percentages on top).
        Box(Modifier.fillMaxWidth().height(80.dp)) {
            if (showChance && chances != null) {
                ChanceBars(chances, text)
            } else {
                AmountBars(rain, text)
            }
        }
        // Times under the bars, each at the start of its stretch (Now, 10m, … or the hours).
        val labels = if (showChance && chances != null) rain.hourLabels else rain.labels
        Row(Modifier.fillMaxWidth().padding(top = Space.S2)) {
            labels.forEach {
                Text(
                    it,
                    color = text.copy(alpha = Emphasis.Medium),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = if (showChance && chances != null) TextAlign.Center else TextAlign.Start,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Amount: thin pills, finer than the data (Apple-style): one a minute for the next hour, one every
 * 5 minutes over 6 hours, eased between the source's steps. A dot where it's dry, no marker lines.
 */
@Composable
private fun BoxScope.AmountBars(rain: RainOutlook.Coming, text: Color) {
    val max = rain.amounts.max().coerceAtLeast(if (rain.minutely) 0.2f else 1f)
    val values = finer(rain.amounts.map { (it / max).coerceIn(0f, 1f) }, if (rain.minutely) 5 else 12)
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(56.dp)
    ) {
        values.forEach { v ->
            BoxWithConstraints(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                val h = (maxHeight * v).coerceAtLeast(maxWidth.coerceAtMost(4.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(h)
                        .clip(CircleShape)
                        .background(
                            if (v > 0.02f) {
                                Brush.verticalGradient(listOf(SolsticePalette.RainLight, SolsticePalette.Rain))
                            } else {
                                SolidColor(text.copy(alpha = 0.22f))
                            }
                        )
                )
            }
        }
    }
}

/**
 * Probability: one bar an hour. The grey pill is 100 %, filled from the bottom with a softer
 * violet-blue gradient up to the chance, with the percentage on top.
 */
@Composable
private fun BoxScope.ChanceBars(chances: List<Float>, text: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Space.S3),
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxSize()
    ) {
        chances.forEach { c ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Text(
                    "${(c * 100).roundToInt()}%",
                    color = text.copy(alpha = Emphasis.High),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
                Spacer(Modifier.height(Space.S1))
                BoxWithConstraints(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .weight(1f)
                        .width(22.dp)
                        .clip(CircleShape)
                        .background(text.copy(alpha = 0.14f))
                ) {
                    if (c > 0.005f) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((maxHeight * c).coerceAtLeast(maxWidth))
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(listOf(SolsticePalette.SnowLight, SolsticePalette.Snow))
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * [per] bars for each step of [steps], eased (smoothstep) between the steps' middles, so the chart
 * reads as a continuous curve rather than blocks. Dry stays dry between two dry steps.
 */
private fun finer(steps: List<Float>, per: Int): List<Float> {
    if (steps.isEmpty()) return steps
    return List(steps.size * per) { i ->
        val x = (i + 0.5f) / per - 0.5f
        val a = kotlin.math.floor(x).toInt().coerceIn(0, steps.size - 1)
        val b = (a + 1).coerceAtMost(steps.size - 1)
        val t = (x - a).coerceIn(0f, 1f)
        val e = t * t * (3 - 2 * t)
        steps[a] + (steps[b] - steps[a]) * e
    }
}

/**
 * Amount | Probability: Material 3's connected toggle buttons (a raindrop and a percent sign),
 * coloured for the sky they sit on.
 */
@Composable
private fun RainSwitch(chance: Boolean, text: Color, onChange: (Boolean) -> Unit) {
    val options = listOf(
        Triple(false, SolsticeIcons.WaterDrop, R.string.shell_rain_amount),
        Triple(true, SolsticeIcons.Percent, R.string.shell_rain_probability)
    )
    val colors = ToggleButtonDefaults.colors(
        containerColor = text.copy(alpha = 0.10f),
        contentColor = text.copy(alpha = Emphasis.Medium),
        checkedContainerColor = text.copy(alpha = 0.22f),
        checkedContentColor = text
    )
    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        options.forEachIndexed { i, (value, icon, label) ->
            ToggleButton(
                checked = chance == value,
                onCheckedChange = { onChange(value) },
                shapes = if (i == 0) {
                    ButtonGroupDefaults.connectedLeadingButtonShapes()
                } else {
                    ButtonGroupDefaults.connectedTrailingButtonShapes()
                },
                colors = colors,
                contentPadding = PaddingValues(horizontal = Space.S3),
                modifier = Modifier.height(Space.S8).semantics { role = Role.RadioButton }
            ) {
                Icon(
                    SolsticeIcons.forSize(icon, Sizes.IconSmall),
                    contentDescription = stringResource(label),
                    modifier = Modifier.size(Sizes.IconSmall)
                )
            }
        }
    }
}

/** "No rain expected in the next 6 hours", after the hourly card. */
@Composable
internal fun NoRainLine(hours: Int, text: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(text.copy(alpha = 0.08f))
            .padding(horizontal = Space.S4, vertical = Space.S2)
    ) {
        Icon(
            SolsticeIcons.Small.Rainy,
            contentDescription = null,
            tint = text.copy(alpha = Emphasis.Medium),
            modifier = Modifier.size(Sizes.IconSmall)
        )
        Spacer(Modifier.width(Space.S2))
        Text(
            stringResource(R.string.shell_no_rain, hours),
            color = text.copy(alpha = Emphasis.High),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
