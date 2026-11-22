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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.ui.expressive.components.CardMaterial
import org.breezyweather.ui.expressive.components.SolsticeCard
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.sky.SkyState
import org.breezyweather.ui.expressive.sky.SkyWeather
import org.breezyweather.ui.expressive.theme.Emphasis
import org.breezyweather.ui.expressive.theme.HeroType
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.SolsticePalette
import org.breezyweather.ui.expressive.theme.Space
import org.breezyweather.ui.expressive.theme.StrokeWidth
import java.util.Date
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------------------------
// Forecast

@Composable
internal fun ForecastPage(
    location: Location?,
    sky: SkyState,
    now: Long,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onDetails: () -> Unit,
    onOpenPlaces: () -> Unit,
    // Off in a postcard window, whose label already gives the local time.
    showLocalTime: Boolean = true,
    pullToRefresh: Boolean = true,
    // The forecast source's name, for the "last updated" line.
    source: String? = null,
    // Its scroll (hoisted by a window that needs to know when it's at the top).
    scroll: androidx.compose.foundation.ScrollState? = null,
) {
    val scrollState = scroll ?: rememberScrollState()
    val text = sky.palette.text
    if (location == null) {
        EmptyForecast(text, onOpenPlaces)
        return
    }
    val context = LocalContext.current
    val weather = location.weather
    val current = weather?.current
    // Develop like a photo (agreed 2026-11-18): while there's no data the values are faint; when it
    // arrives they come up from soft to sharp (a slight blur clearing) instead of popping in.
    val hasData = current != null
    val develop = remember { Animatable(if (hasData) 1f else 0f) }
    LaunchedEffect(hasData) {
        // A timed effect (agreed 2026-11-19 as one of the few exceptions): a photo developing.
        if (hasData) develop.animateTo(1f, tween(700)) else develop.snapTo(0f)
    }
    val content: @Composable BoxScope.() -> Unit = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val d = develop.value
                    alpha = 0.35f + 0.65f * d
                    val blur = 10.dp.toPx() * (1f - d)
                    renderEffect = if (blur > 0.5f && android.os.Build.VERSION.SDK_INT >= 31) {
                        BlurEffect(blur, blur, TileMode.Decal)
                    } else {
                        null
                    }
                }
                .verticalScroll(scrollState)
                .padding(top = pageTopInset())
                .padding(horizontal = Space.S5)
                .padding(top = Space.S10, bottom = 120.dp)
        ) {
            // A visit's line above the title, scrolling with the page (postcard windows only).
            VisitingLine(text.copy(alpha = Emphasis.High), Modifier.padding(bottom = Space.S2))
            // Only the location's title at the top (plus an icon for the current position).
            val (title, city) = placeName(context, location)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (location.isCurrentPosition) {
                    Icon(
                        SolsticeIcons.Small.NearMeFill,
                        contentDescription = stringResource(R.string.location_current),
                        tint = text,
                        modifier = Modifier.size(Sizes.IconMedium)
                    )
                    Spacer(Modifier.width(Space.S2))
                }
                // Long names shrink to fit, then ellipsize.
                Text(
                    title,
                    color = text,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    autoSize = TextAutoSize.StepBased(16.sp, MaterialTheme.typography.titleLarge.fontSize),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            city?.let {
                Text(
                    it,
                    color = text.copy(alpha = Emphasis.Medium),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // The place's local time when it's in another time zone from yours, e.g. "[clock] 2:33 PM, +1 h"
            // (the same clock as a visit's label).
            if (showLocalTime) {
                timeDifference(location.timeZone, now)?.let { diff ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Space.Half)) {
                        Icon(
                            SolsticeIcons.Small.Schedule,
                            contentDescription = null,
                            tint = text.copy(alpha = Emphasis.Medium),
                            modifier = Modifier.size(Sizes.IconSmall)
                        )
                        Spacer(Modifier.width(Space.S1))
                        Text(
                            time(context, Date(now), location.timeZone) + ", " + diff,
                            color = text.copy(alpha = Emphasis.Medium),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            // The big number is the actual temperature, or the feels-like one when that option is on.
            val feelsMain = ShellPrefs.feelsLikeMain(context)
            val main = if (feelsMain) {
                current?.temperature?.feelsLikeTemperature ?: current?.temperature?.temperature
            } else {
                current?.temperature?.temperature
            }
            Text(
                main?.let { degrees(context, it) } ?: "–",
                color = text,
                style = HeroType.Temperature
            )
            current?.weatherText?.let {
                Text(it, color = text, style = MaterialTheme.typography.headlineSmallEmphasized)
            }
            feelsLikeLine(context, location)?.let {
                Text(it, color = text.copy(alpha = Emphasis.Medium), style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(Space.S7))
            SunPath(location, now, text, sky.palette)
            Spacer(Modifier.height(Space.S5))
            // The order follows what matters now: rain near moves its timeline right under the sun
            // path; otherwise a short "no rain" line sits after the hourly card.
            val rain = remember(location.weather, now / 60_000L) { rainOutlook(context, location, now) }
            if (rain is RainOutlook.Coming) {
                RainCard(rain, location, text)
                Spacer(Modifier.height(Space.S3))
            }
            TodayCard(location, now, text)
            if (rain is RainOutlook.None) {
                Spacer(Modifier.height(Space.S3))
                NoRainLine(rain.hours, text)
            }
            Spacer(Modifier.height(Space.S4))
            TextButton(onClick = onDetails) {
                Text(stringResource(R.string.shell_more_details), color = text)
            }
            // When this was updated and where it's from (agreed 2026-11-21), quiet, at the very end.
            Spacer(Modifier.height(Space.S2))
            UpdatedLine(
                updated = weather?.base?.refreshTime?.time,
                source = source,
                now = now,
                zone = TimeZone.getDefault(),
                refreshing = refreshing,
                text = text,
                onRetry = if (pullToRefresh) onRefresh else null
            )
        }
    }
    if (pullToRefresh) {
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    } else {
        Box(Modifier.fillMaxSize(), content = content)
    }
}

@Composable
private fun EmptyForecast(text: Color, onOpenPlaces: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(Space.S8)
    ) {
        Text(
            stringResource(R.string.shell_no_location),
            color = text,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(Space.S4))
        FilledTonalButton(onClick = onOpenPlaces) { Text(stringResource(R.string.shell_tab_places)) }
    }
}

/** Sunrise left, sunset right, the sun's arc between with a dot where it is now. */
@Composable
private fun SunPath(
    location: Location,
    now: Long,
    text: Color,
    palette: org.breezyweather.ui.expressive.sky.SkyPalette,
) {
    val context = LocalContext.current
    val weather = location.weather ?: return
    val sun = weather.today?.sun ?: return
    val rise = sun.riseDate ?: return
    val set = sun.setDate ?: return
    val day = 24L * 60 * 60 * 1000
    // Day: sunrise to sunset with the sun. Night: sunset to sunrise with the moon in its phase.
    val night = now !in rise.time..set.time
    val (from, to) = when {
        !night -> rise.time to set.time
        now > set.time -> set.time to (weather.tomorrow?.sun?.riseDate?.time ?: (rise.time + day))
        else -> (set.time - day) to rise.time
    }
    val progress = ((now - from).toFloat() / (to - from)).coerceIn(0f, 1f)
    val phase = weather.today?.moonPhase?.takeIf { it.isValid }
    val angle = phase?.angle ?: 180
    // 0/360 = new, 180 = full; waxing below 180 (lit on the right).
    val lit = ((1 - kotlin.math.cos(Math.toRadians(angle.toDouble()))) / 2).toFloat()
    var showPhase by remember { mutableStateOf(false) }
    val phaseIcons = rememberMoonPhaseIcons()
    // How the moon is turned in this place's sky now, from the same calculation as the moon card
    // (MoonSky), worked out per hour.
    val moonHour = MoonSky.hourOf(now)
    val moonTilt = remember(moonHour, angle, location.latitude, location.longitude) {
        MoonSky.iconRotation(
            angle,
            MoonSky.limbAngle(java.util.Date(now)),
            MoonSky.parallactic(moonHour, location.latitude, location.longitude)
        )
    }
    val warm = SolsticePalette.SunWarm
    val cool = SolsticePalette.MoonCool
    val accent = if (night) cool else warm
    // The shape (agreed 2026-11-18, like the reference): a smooth bell between the two times whose
    // ends run flat along the horizon and fade out towards the screen edges.
    val edge = 0.14f
    fun bell(x: Float): Float {
        val t = ((x - edge) / (1f - 2 * edge))
        return if (t <= 0f || t >= 1f) 0f else ((1 - kotlin.math.cos(2 * PI * t)) / 2).toFloat()
    }
    Column(Modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(84.dp)
                .pointerInput(night, progress) {
                    if (!night) return@pointerInput
                    detectTapGestures { tap ->
                        val x = (edge + progress * (1f - 2 * edge)) * size.width
                        val base = size.height - 10.dp.toPx()
                        val y = base - bell(x / size.width) * (base - 8.dp.toPx())
                        if (kotlin.math.hypot(tap.x - x, tap.y - y) < 36.dp.toPx()) showPhase = !showPhase
                    }
                }
        ) {
            val w = size.width
            val base = size.height - 10.dp.toPx()
            val peak = base - 8.dp.toPx()
            fun point(x: Float) = Offset(x * w, base - bell(x) * peak)
            val curve = Path().apply {
                moveTo(0f, base)
                for (i in 1..96) point(i / 96f).let { lineTo(it.x, it.y) }
            }
            // Soft gradient under the curve.
            val fill = Path().apply {
                addPath(curve)
                lineTo(w, base)
                close()
            }
            drawPath(
                fill,
                Brush.verticalGradient(
                    0f to accent.copy(alpha = 0.35f),
                    1f to accent.copy(alpha = 0f),
                    startY = 8.dp.toPx(),
                    endY = base
                )
            )
            // The curve and horizon fade out towards the screen edges.
            val fade = Brush.horizontalGradient(
                0f to Color.Transparent,
                0.1f to Color.Black,
                0.9f to Color.Black,
                1f to Color.Transparent
            )
            drawContext.canvas.saveLayer(
                androidx.compose.ui.geometry.Rect(0f, 0f, w, size.height),
                androidx.compose.ui.graphics.Paint()
            )
            drawLine(text.copy(alpha = 0.3f), Offset(0f, base), Offset(w, base), strokeWidth = StrokeWidth.Thin.toPx())
            drawPath(curve, text.copy(alpha = 0.55f), style = Stroke(StrokeWidth.Regular.toPx()))
            val sunX = edge + progress * (1f - 2 * edge)
            val done = Path().apply {
                moveTo(0f, base)
                for (i in 1..96) {
                    val x = sunX * i / 96f
                    point(x).let { lineTo(it.x, it.y) }
                }
            }
            drawPath(done, accent, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            drawRect(fade, blendMode = BlendMode.DstIn)
            val c = point(sunX)
            // The lines fade out around the sun or moon instead of running underneath it.
            val clear = (if (night) 10.dp else 7.dp).toPx() + 8.dp.toPx()
            drawCircle(
                Brush.radialGradient(
                    0f to Color.Black,
                    0.6f to Color.Black,
                    1f to Color.Transparent,
                    center = c,
                    radius = clear
                ),
                radius = clear,
                center = c,
                blendMode = BlendMode.DstOut
            )
            drawContext.canvas.restore()
            if (!night) {
                drawCircle(SolsticePalette.Sun, radius = 7.dp.toPx(), center = c)
            } else {
                // The plain outline phase icon (the realistic moon is for the moon card), turned the way
                // the moon really looks in this place's sky now (agreed: the same tilt as the card).
                drawMoonPhaseIcon(phaseIcons, c, 10.dp.toPx(), angle, text, rotation = moonTilt)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TimeLabel(
                time(context, Date(from), location.timeZone, noPeriod = true),
                stringResource(if (night) R.string.shell_sunset else R.string.shell_sunrise),
                text,
                Alignment.Start
            )
            Spacer(Modifier.weight(1f))
            // Dynamic: sun strength and UV by day; time left within an hour of sunrise or sunset;
            // the moon's phase at night (tap the moon).
            val middle = when {
                !night && set.time - now in 0..3_600_000 ->
                    context.getString(R.string.shell_sunset_in, minutesText(context, set.time - now))
                night && to - now in 0..3_600_000 ->
                    context.getString(R.string.shell_sunrise_in, minutesText(context, to - now))
                !night -> sunStrength(context, weather.current?.uV?.index)
                else -> null
            }
            if (night) {
                // At night: the moon's phase, outlined so it reads as a button (opens the moon card).
                MoonButton(location, now, text, palette, night)
                Spacer(Modifier.weight(1f))
            } else if (middle != null) {
                Text(
                    middle,
                    color = text.copy(alpha = Emphasis.High),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
            }
            TimeLabel(
                time(context, Date(to), location.timeZone, noPeriod = true),
                stringResource(if (night) R.string.shell_sunrise else R.string.shell_sunset),
                text,
                Alignment.End
            )
        }
    }
}

private fun minutesText(context: Context, millis: Long): String =
    context.getString(R.string.shell_minutes, (millis / 60_000L).coerceAtLeast(1L).toInt())

/** "Mild sun, UV 3" from the UV index. */
private fun sunStrength(context: Context, uv: Double?): String? {
    uv ?: return null
    val word = when {
        uv < 3 -> R.string.shell_sun_weak
        uv < 6 -> R.string.shell_sun_mild
        uv < 8 -> R.string.shell_sun_strong
        else -> R.string.shell_sun_very_strong
    }
    return context.getString(word) + ", UV " + uv.roundToInt()
}

@Composable
private fun TimeLabel(time: String, label: String, text: Color, align: Alignment.Horizontal) {
    Column(horizontalAlignment = align) {
        Text(time, color = text, style = MaterialTheme.typography.titleLargeEmphasized)
        Text(label, color = text.copy(alpha = Emphasis.Medium), style = MaterialTheme.typography.bodyMedium)
    }
}

/** The next hours, "Now" first, on a translucent card. */
@Composable
private fun TodayCard(location: Location, now: Long, text: Color) {
    val context = LocalContext.current
    val hours = location.weather?.nextHourlyForecast?.take(6).orEmpty()
    if (hours.isEmpty()) return
    SolsticeCard(CardMaterial.Glass, text) {
        // The title says what's happening, so you don't have to read the numbers.
        Text(
            hourlyHeadline(context, hours) ?: stringResource(R.string.shell_today),
            color = text,
            style = MaterialTheme.typography.titleMediumEmphasized
        )
        Spacer(Modifier.height(Space.S3))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            hours.forEachIndexed { i, hour ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        hour.temperature?.temperature?.let { degrees(context, it) } ?: "–",
                        color = text,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        hourIcon(SkyWeather.of(hour.weatherCode), !hour.isDaylight),
                        contentDescription = hour.weatherText,
                        tint = text.copy(alpha = Emphasis.High),
                        modifier = Modifier.padding(vertical = Space.S2).size(Sizes.Icon)
                    )
                    Text(
                        if (i == 0 && hour.date.time <= now) {
                            stringResource(R.string.shell_now)
                        } else {
                            time(context, hour.date, location.timeZone, hourOnly = true)
                        },
                        color = text.copy(alpha = Emphasis.Medium),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/** What the next hours do, in words: "Going down to 16° in the next 3 hours", "Rain from 15:00"… */
private fun hourlyHeadline(context: Context, hours: List<breezyweather.domain.weather.model.Hourly>): String? {
    val temps = hours.mapNotNull { it.temperature?.temperature?.inCelsius }
    if (temps.size < 2) return null
    val wet = hours.indexOfFirst {
        val w = SkyWeather.of(it.weatherCode)
        w == SkyWeather.RAIN ||
            w == SkyWeather.THUNDERSTORM ||
            w == SkyWeather.SNOW
    }
    if (wet > 0) {
        return context.getString(
            R.string.shell_hourly_rain_at,
            hours[wet].weatherText ?: "",
            time(context, hours[wet].date, TimeZone.getDefault(), hourOnly = true)
        )
    }
    val first = temps.first()
    val minIdx = temps.indices.minBy { temps[it] }
    val maxIdx = temps.indices.maxBy { temps[it] }
    return when {
        temps[minIdx] <= first - 2 -> context.getString(
            R.string.shell_hourly_down,
            hours[minIdx].temperature?.temperature?.let { degrees(context, it) } ?: "",
            minIdx.coerceAtLeast(1)
        )
        temps[maxIdx] >= first + 2 -> context.getString(
            R.string.shell_hourly_up,
            hours[maxIdx].temperature?.temperature?.let { degrees(context, it) } ?: "",
            maxIdx.coerceAtLeast(1)
        )
        else -> context.getString(
            R.string.shell_hourly_steady,
            hours.first().temperature?.temperature?.let { degrees(context, it) } ?: return null
        )
    }
}

/**
 * The icon for a weather condition, day or night. Used by the hours, the week, the postcard and the
 * toolbar; [filled] only for a selected state (the toolbar's current tab).
 */
internal fun hourIcon(weather: SkyWeather, night: Boolean, filled: Boolean = false): ImageVector = with(SolsticeIcons) {
    when (weather) {
        SkyWeather.CLEAR -> if (night) pick(filled, ClearNight, ClearNightFill) else pick(filled, Sunny, SunnyFill)
        SkyWeather.PARTLY_CLOUDY -> if (night) {
            pick(filled, PartlyCloudyNight, PartlyCloudyNightFill)
        } else {
            pick(filled, PartlyCloudyDay, PartlyCloudyDayFill)
        }
        SkyWeather.OVERCAST -> pick(filled, Cloud, CloudFill)
        SkyWeather.RAIN -> pick(filled, Rainy, RainyFill)
        SkyWeather.THUNDERSTORM -> pick(filled, Thunderstorm, ThunderstormFill)
        SkyWeather.SNOW -> pick(filled, WeatherSnowy, WeatherSnowyFill)
        SkyWeather.FOG -> pick(filled, Foggy, FoggyFill)
    }
}

private fun pick(filled: Boolean, icon: ImageVector, fill: ImageVector) = if (filled) fill else icon
