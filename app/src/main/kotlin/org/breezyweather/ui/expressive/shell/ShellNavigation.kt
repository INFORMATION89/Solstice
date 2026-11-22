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

import android.os.Build
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.information89.motion.Motion
import io.github.information89.motion.PageOvershoot
import io.github.information89.motion.pageSpringFling
import io.github.information89.motion.springToPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * How the pages change, shared by home and the postcard window (2026-11-21), so both behave the same by
 * construction and only their configuration differs. One page motion for everything (agreed
 * 2026-11-21: tab taps mirror swipes): the pages themselves spring to their page on `Motion.momentum`,
 * whether a swipe was let go ([fling]) or a tab was tapped ([navigate]), and bounce past the ends
 * ([overshoot]). A touch while they move stops the pager first (the accepted trade-off).
 */
@Stable
internal class ShellNavigation(
    val pager: PagerState,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    // The tabs a tap's change passes over (a jump from This week to Places passes Forecast), while it runs.
    private var passingFrom by mutableIntStateOf(-1)
    private var passingTo by mutableIntStateOf(-1)

    /** Where the pages are (fractional), for anything that follows them (sky fade, blurs, backdrop). */
    fun visualPosition(): Float = pager.currentPage + pager.currentPageOffsetFraction

    /**
     * Whether tab [i] is only passed over by a tap's change in progress: its label stays folded. The
     * tabs a change starts and ends on unfold normally.
     */
    fun isPassing(i: Int): Boolean =
        passingFrom >= 0 && i > minOf(passingFrom, passingTo) && i < maxOf(passingFrom, passingTo)

    /** Where the toolbar shows the selection: null, it follows the pager as for a swipe. */
    fun toolbarPosition(): Float? = null

    /** How far a settle bounces past the first or last page (drawn with `pageOvershoot`). */
    val overshoot = PageOvershoot()

    /** The pager's fling: let go, the pages spring to the page from the finger's speed. */
    val fling: TargetedFlingBehavior = pageSpringFling(pager, overshoot) { Motion.momentum }

    /** A finger on the toolbar (its slide) stops any tap's change in progress. */
    fun interrupt() {
        job?.cancel()
        job = null
    }

    /** Goes to page [target] the way a swipe settles. A new tap, or a finger, interrupts it where it is. */
    fun navigate(target: Int) {
        job?.cancel()
        job = scope.launch {
            passingFrom = (pager.currentPage + pager.currentPageOffsetFraction).roundToInt()
            passingTo = target
            try {
                pager.springToPage(target, overshoot, Motion.momentum)
            } finally {
                passingFrom = -1
                passingTo = -1
            }
        }
    }
}

@Composable
internal fun rememberShellNavigation(pager: PagerState): ShellNavigation {
    val scope = rememberCoroutineScope()
    return remember(pager, scope) { ShellNavigation(pager, scope) }
}

/**
 * Title hand-off, for each tab: while its page's big title is on screen the tab shows only the icon;
 * as the title scrolls away the label unfolds in the toolbar (never the same word twice). One list
 * per tab, or null for a page with no big title (its label always shows).
 */
@Composable
internal fun rememberTitleHandOff(lists: List<LazyListState?>): State<List<Float>> {
    val titleHeight = with(LocalDensity.current) { 72.dp.toPx() }
    return remember(lists, titleHeight) {
        derivedStateOf { lists.map { it?.let { list -> 1f - titleVisible(list, titleHeight) } ?: 1f } }
    }
}

/** 1 while the list's big title (its first item) is fully on screen, 0 once it has scrolled away. */
private fun titleVisible(state: LazyListState, titleHeight: Float): Float =
    if (state.firstVisibleItemIndex > 0) {
        0f
    } else {
        (1f - state.firstVisibleItemScrollOffset / titleHeight).coerceIn(0f, 1f)
    }

/**
 * A page partly in view gets a little blur that clears as it comes fully in. Read in the draw phase
 * from [position] (the pages' visual position), so it follows the finger and reverses with it.
 */
internal fun Modifier.pageSwipeBlur(position: () -> Float, index: Int): Modifier = graphicsLayer {
    val d = abs(position() - index).coerceIn(0f, 1f)
    val b = 6.dp.toPx() * d
    renderEffect = if (b > 0.5f && d < 1f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BlurEffect(b, b, TileMode.Decal)
    } else {
        null
    }
}
