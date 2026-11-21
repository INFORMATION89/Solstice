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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.breezyweather.R
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * How fresh some data is (agreed 2026-11-21 for Forecast's "last updated" line; shared so any page or
 * card can say when its data is from). Pure, so it's unit-tested.
 */
@Immutable
internal sealed interface Freshness {
    /** Under a minute ago. */
    data object JustNow : Freshness

    /** Under an hour ago. */
    data class MinutesAgo(val minutes: Int) : Freshness

    /** Earlier today, at [time]. */
    data class Today(val time: Long) : Freshness

    /** Yesterday, at [time]. */
    data class Yesterday(val time: Long) : Freshness

    /** Before yesterday: on [time]'s date. */
    data class On(val time: Long) : Freshness

    companion object {
        fun of(updated: Long, now: Long, zone: TimeZone): Freshness {
            val ago = (now - updated).coerceAtLeast(0L)
            if (ago < 60_000L) return JustNow
            if (ago < 3_600_000L) return MinutesAgo((ago / 60_000L).toInt())
            val days = dayNumber(now, zone) - dayNumber(updated, zone)
            return when (days) {
                0L -> Today(updated)
                1L -> Yesterday(updated)
                else -> On(updated)
            }
        }

        private fun dayNumber(time: Long, zone: TimeZone): Long {
            val c = Calendar.getInstance(zone).apply { timeInMillis = time }
            return (time + c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 86_400_000L
        }
    }
}

/** [freshness] as text: "Updated 12 min ago", "Updated at 3:40 PM", "Updated yesterday at 9:10 PM"… */
internal fun freshnessText(context: Context, freshness: Freshness, zone: TimeZone): String = when (freshness) {
    Freshness.JustNow -> context.getString(R.string.shell_updated_just_now)
    is Freshness.MinutesAgo -> context.getString(R.string.shell_updated_minutes, freshness.minutes)
    is Freshness.Today -> context.getString(R.string.shell_updated_at, time(context, Date(freshness.time), zone))
    is Freshness.Yesterday ->
        context.getString(R.string.shell_updated_yesterday, time(context, Date(freshness.time), zone))
    is Freshness.On -> context.getString(
        R.string.shell_updated_on,
        DateFormat.getDateInstance(DateFormat.MEDIUM).apply { timeZone = zone }.format(Date(freshness.time))
    )
}

/**
 * The quiet "last updated" line (agreed 2026-11-21): "Updated 12 min ago · Open-Meteo", "Updating…"
 * while a refresh runs, or "Couldn't update · Try again" when the last one failed (the only
 * interactive part, a real text button). [now] should tick at least each minute. With no [onRetry]
 * (nothing to refresh, e.g. a postcard window) it only says how fresh the data is.
 */
@Composable
internal fun UpdatedLine(
    updated: Long?,
    source: String?,
    now: Long,
    zone: TimeZone,
    refreshing: Boolean,
    text: Color,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    // A refresh failed when it ends without moving the update time forward.
    var startedAt by remember { mutableLongStateOf(0L) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            startedAt = System.currentTimeMillis()
            failed = false
        } else if (startedAt != 0L) {
            failed = (updated ?: 0L) < startedAt
        }
    }
    LaunchedEffect(updated) { if (updated != null && updated >= startedAt) failed = false }
    val quiet = text.copy(alpha = 0.6f)
    val style = MaterialTheme.typography.labelMedium
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        when {
            refreshing && onRetry != null -> Text(stringResource(R.string.shell_updating), color = quiet, style = style)
            failed && onRetry != null -> {
                Text(stringResource(R.string.shell_update_failed) + SEPARATOR, color = quiet, style = style)
                TextButton(onClick = onRetry, modifier = Modifier.defaultMinSize(minHeight = 48.dp)) {
                    Text(stringResource(R.string.shell_try_again), color = text, style = style)
                }
            }
            updated != null -> Text(
                freshnessText(context, Freshness.of(updated, now, zone), zone) +
                    (source?.let { SEPARATOR + it } ?: ""),
                color = quiet,
                style = style
            )
        }
    }
}

private const val SEPARATOR = " · "
