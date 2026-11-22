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
import androidx.core.content.edit
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Places the user is only visiting: picked from search, shown right away, kept only if starred.
 * Upstream saves every place it shows weather for, so a visit is a normal place plus a mark here;
 * unstarred visits are removed the next time the app starts ([takeStale]).
 */
object Visits {
    private const val PREFS = "solstice_places"
    private const val KEY_VISITING = "visiting"

    /** Stale visits are cleaned once per app process, before any new visit of this run. */
    @Volatile
    private var cleanedThisRun = false

    fun all(context: Context): Set<String> = prefs(context).getStringSet(KEY_VISITING, emptySet()).orEmpty()

    fun mark(context: Context, id: String) = prefs(context).edit { putStringSet(KEY_VISITING, all(context) + id) }

    /** Starred (kept) or deleted: no longer a visit. */
    fun unmark(context: Context, id: String) = prefs(context).edit { putStringSet(KEY_VISITING, all(context) - id) }

    /**
     * On the first call of an app run: returns the visits left from the previous run (to delete,
     * unless starred) and forgets them. Later calls in the same run return nothing.
     */
    fun takeStale(context: Context): Set<String> {
        if (cleanedThisRun) return emptySet()
        cleanedThisRun = true
        val stale = all(context)
        prefs(context).edit { putStringSet(KEY_VISITING, emptySet()) }
        return stale
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/** Whole hours of difference, for sorting or quick checks. */
internal fun hoursApart(zone: TimeZone, now: Long): Int =
    ((zone.getOffset(now) - TimeZone.getDefault().getOffset(now)) / 3_600_000f).roundToInt()
