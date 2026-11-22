/*
 * Copyright 2026 INFORMATION89
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.information89.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/*
 * One page motion for a pager (agreed 2026-11-21, Motion Lab option B; the user: tab taps should mirror
 * swipes, no linear animations): the pages themselves spring to their page, one spring the whole way,
 * from a swipe's release ([pageSpringFling]) or a tab tap ([springToPage]), and bounce past the first and
 * last page ([PageOvershoot]). A touch while they move stops the pager first (it's really scrolling).
 */

/**
 * The page a swipe let go at [position] (fractional) with [velocity] (pages per second, + forward)
 * settles on: a flick past a third of a page a second picks the next page that way, otherwise the
 * nearest page.
 */
internal fun settleTarget(position: Float, velocity: Float, pageCount: Int): Int = when {
    velocity > 0.35f -> floor(position).toInt() + 1
    velocity < -0.35f -> ceil(position).toInt() - 1
    else -> position.roundToInt()
}.coerceIn(0, (pageCount - 1).coerceAtLeast(0))

/**
 * How far a page spring has gone past the first or last page, in pixels (+ forward). The pager itself
 * can't scroll past its ends, so this part of the bounce is drawn instead: put [pageOvershoot] on the
 * pager (2026-11-21, the user: the bounce only played between pages).
 */
@Stable
class PageOvershoot {
    var pixels by mutableFloatStateOf(0f)
        internal set
}

/** Shifts the pages by [overshoot]'s pixels, so a settle's bounce shows at the first and last page too. */
fun Modifier.pageOvershoot(overshoot: PageOvershoot): Modifier = offset {
    // `offset` (not `absoluteOffset`) mirrors in right-to-left layouts, as the pager does.
    IntOffset(-overshoot.pixels.roundToInt(), 0)
}

/**
 * Springs the pages to [target] with [spring], starting at [velocity] (pixels per second), inside a
 * scroll. What the pager can't take (past an end) goes to [overshoot]; it's always reset when done or
 * caught.
 */
private suspend fun ScrollScope.springPages(
    pager: PagerState,
    target: Int,
    velocity: Float,
    overshoot: PageOvershoot?,
    spring: MotionSpring,
) {
    val pageSize = (pager.layoutInfo.pageSize + pager.layoutInfo.pageSpacing).toFloat()
    if (pageSize <= 0f) return
    val position = pager.currentPage + pager.currentPageOffsetFraction
    val distance = (target - position) * pageSize
    var travelled = 0f
    try {
        Animatable(0f).animateTo(
            distance,
            spring.spec(Offset.VisibilityThreshold.x),
            initialVelocity = velocity
        ) {
            travelled += scrollBy(value - travelled)
            overshoot?.pixels = value - travelled
        }
    } finally {
        // Settled or caught by a finger: never left shifted.
        overshoot?.pixels = 0f
    }
}

/**
 * A pager fling that's one spring the whole way: it picks the page by [settleTarget], then springs the
 * pages there with [spring] from the finger's speed, so a flick carries into the spring and can
 * overshoot and settle. No decay phase first, unlike Compose's default pager fling.
 */
fun pageSpringFling(
    pager: PagerState,
    overshoot: PageOvershoot? = null,
    spring: () -> MotionSpring,
): TargetedFlingBehavior =
    object : TargetedFlingBehavior {
        override suspend fun ScrollScope.performFling(
            initialVelocity: Float,
            onRemainingDistanceUpdated: (Float) -> Unit,
        ): Float {
            val pageSize = (pager.layoutInfo.pageSize + pager.layoutInfo.pageSpacing).toFloat()
            if (pageSize <= 0f) return initialVelocity
            val position = pager.currentPage + pager.currentPageOffsetFraction
            val target = settleTarget(position, initialVelocity / pageSize, pager.pageCount)
            springPages(pager, target, initialVelocity, overshoot, spring())
            onRemainingDistanceUpdated(0f)
            return 0f
        }
    }

/**
 * Springs the pages to [target] the way a swipe settles (a tab tap, agreed 2026-11-21: taps mirror
 * swipes). It starts from rest; a new tap or a finger interrupts it where it is.
 */
suspend fun PagerState.springToPage(target: Int, overshoot: PageOvershoot? = null, spring: MotionSpring) {
    scroll {
        springPages(this@springToPage, target.coerceIn(0, (pageCount - 1).coerceAtLeast(0)), 0f, overshoot, spring)
    }
}
