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

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * A page change that never blocks input (MOTION.md, agreed 2026-11-21).
 *
 * The pages underneath (a pager, a set of screens) switch **at once**; this piece only draws the
 * change: a frozen image of the page being left slides out while the new page slides in from the
 * side it came from, both with a touch of blur. Because nothing underneath is ever "scrolling", a
 * tap during the change reaches the new page straight away (a pager that's mid-scroll spends the
 * first touch on stopping itself).
 *
 * Use: [rememberPageTransition], put [pageTransition] on the container of the pages, and change
 * pages with [go]. A new [go] (or [rest]) interrupts the one in progress.
 */
@Stable
class PageTransition internal constructor(
    internal val leaving: GraphicsLayer,
    internal val leavingDraw: GraphicsLayer,
    /** How far the outgoing page is blurred at the start (it clears as it leaves). */
    val leavingBlur: Dp,
    /** How blurred the arriving page starts (it clears quickly). */
    val arrivingBlur: Dp,
) {
    internal var capture by mutableStateOf(false)
    internal var image by mutableStateOf<ImageBitmap?>(null)
    internal var direction by mutableFloatStateOf(1f)

    /**
     * 0 for a full change ([go]); otherwise how much of a page a swipe still had to travel when it
     * was let go ([settle]): the frozen image then holds both pages as they were, and only the part
     * the new page hasn't covered yet is drawn.
     */
    internal var partialFrom by mutableFloatStateOf(0f)

    // Settle option A (Motion Lab, 2026-11-21): while a finger drags the pages, they're recorded into
    // [leaving] every frame, so a release only has to copy that recording into an image, without
    // [freeze]'s two frame waits. (The recording itself can't be drawn instead of the image: it points
    // at the pages rather than copying them, so after the jump it would show the new page.)
    internal var recording by mutableStateOf(false)
    internal var hasFrame = false

    /**
     * Option A: tell it while a finger is dragging the pages (e.g. from the pager's
     * `interactionSource`), so [settle] can start from the frame recorded last.
     */
    fun dragging(active: Boolean) {
        if (active) hasFrame = false
        recording = active
    }
    private var startSlide = 1f
    private var settleJob: Job? = null

    /** 1 at the start of a change, 0 at rest: the slide. */
    internal val slide = Animatable(0f)

    /** 1 at the start of a change, 0 at rest: the arriving page's blur. */
    internal val haze = Animatable(0f)

    /**
     * How far the change still has to go: 1 at its start, 0 at rest. Lets things drawn with the pages
     * (a backdrop that follows the page position) follow the drawn slide instead of the instant switch.
     */
    val remaining: Float get() = if (startSlide > 0f) slide.value / startSlide else 0f

    /** Whether a change is drawing right now. */
    val isRunning: Boolean get() = slide.value > 0f || haze.value > 0f

    /**
     * Changes page: freezes the current one, calls [switch] to change the pages underneath at once,
     * then slides the new page in. [forward] is true when the new page is to the right of the old.
     * [alongside] runs together with the slide (e.g. a toolbar following the change).
     */
    suspend fun go(forward: Boolean, switch: suspend () -> Unit, alongside: suspend () -> Unit = {}) {
        try {
            stopSettle()
            rest()
            freeze()
            direction = if (forward) 1f else -1f
            partialFrom = 0f
            startSlide = 1f
            slide.snapTo(1f)
            haze.snapTo(1f)
            switch()
            coroutineScope {
                launch { haze.animateTo(0f, Motion.snappy.spec()) }
                launch { alongside() }
                slide.animateTo(0f, Motion.content.spec())
            }
        } finally {
            // Finished or interrupted: always back at rest.
            withContext(NonCancellable) { rest() }
        }
    }

    /** Two frames: one records the pages as they are now, the next reads them back as an image. */
    private suspend fun freeze() {
        capture = true
        withFrameNanos { }
        withFrameNanos { }
        image = runCatching { leaving.toImageBitmap() }.getOrNull()
    }

    private suspend fun stopSettle() {
        settleJob?.cancel()
        settleJob?.join()
        settleJob = null
    }

    /**
     * A swipe let go part-way: called from a pager's fling (see [pageSettleFling]). Freezes what's on
     * screen, calls [jump] to put the pages at the [forward] neighbour at once ([from] is how much of
     * a page was still to go), then draws the rest of the way on [scope], starting at the finger's
     * [velocity] (pages per second), so the pages underneath are at rest and taps land.
     */
    suspend fun settle(
        scope: CoroutineScope,
        forward: Boolean,
        from: Float,
        velocity: Float,
        jump: suspend () -> Unit,
    ) {
        stopSettle()
        rest()
        if (hasFrame) {
            // Option A: the pages as they were when let go are already recorded; copy them now.
            recording = false
            hasFrame = false
            image = runCatching { leaving.toImageBitmap() }.getOrNull()
            if (image == null) freeze()
        } else {
            freeze()
        }
        direction = if (forward) 1f else -1f
        partialFrom = from
        startSlide = from
        slide.snapTo(from)
        jump()
        settleJob = scope.launch {
            try {
                slide.animateTo(0f, Motion.content.spec(), initialVelocity = -velocity)
            } finally {
                withContext(NonCancellable) { rest() }
            }
        }
    }

    /** Stops any change in progress and draws the pages as they are. */
    suspend fun rest() {
        slide.snapTo(0f)
        haze.snapTo(0f)
        image = null
        capture = false
        partialFrom = 0f
        startSlide = 1f
    }
}

/** A [PageTransition] for one set of pages. */
@Composable
fun rememberPageTransition(leavingBlur: Dp = 4.dp, arrivingBlur: Dp = 5.dp): PageTransition {
    val leaving = rememberGraphicsLayer()
    val leavingDraw = rememberGraphicsLayer()
    return remember(leaving, leavingDraw, leavingBlur, arrivingBlur) {
        PageTransition(leaving, leavingDraw, leavingBlur, arrivingBlur)
    }
}

/**
 * Draws [transition] over the pages inside: records them when asked, and while a change runs,
 * slides the arriving pages in and the frozen leaving page out. Reads the animation only in draw.
 */
fun Modifier.pageTransition(transition: PageTransition): Modifier = this
    .drawWithContent {
        if (transition.capture || transition.recording) {
            transition.leaving.record { this@drawWithContent.drawContent() }
            transition.capture = false
            if (transition.recording) transition.hasFrame = true
        }
        drawContent()
        // The page being left, on top: it stays opaque and slides out the other way, with a touch
        // of blur. Drawn outside the moving layer (that layer's blur would clip it).
        val f = transition.slide.value
        val image = transition.image
        val from = transition.partialFrom
        if (from > 0f && f > 0.001f && image != null) {
            // A swipe's settle: the image holds both pages as they were when let go. It moves with the
            // new page, and only the part the new page hasn't covered yet is drawn.
            val w = size.width
            val dir = transition.direction
            clipRect(
                left = if (dir > 0f) 0f else w * (1f - f),
                right = if (dir > 0f) w * f else w
            ) {
                translate(left = -dir * w * (from - f)) { drawImage(image) }
            }
        } else if (f > 0.01f && image != null) {
            val layer = transition.leavingDraw
            layer.alpha = 1f
            val b = transition.leavingBlur.toPx() * (1f - f)
            layer.renderEffect = if (b > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                BlurEffect(b, b, TileMode.Decal)
            } else {
                null
            }
            layer.record { drawImage(image) }
            translate(left = -transition.direction * size.width * (1f - f)) { drawLayer(layer) }
        }
    }
    .graphicsLayer {
        // The arriving page slides in from the side it came from, briefly blurred.
        translationX = transition.direction * size.width * transition.slide.value
        val b = transition.arrivingBlur.toPx() * transition.haze.value
        renderEffect = if (b > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BlurEffect(b, b, TileMode.Decal)
        } else {
            null
        }
    }

/**
 * A pager fling that never leaves the pager scrolling (MOTION.md: animations never block input):
 * when a swipe is let go, it picks the page (by momentum, then by position), jumps there at once,
 * and lets [transition] draw the rest of the way at the finger's speed. Use it as the pager's
 * `flingBehavior` together with `Modifier.pageTransition(transition)` on the pager.
 * [onSettle] hears where it was let go and which page it's going to (e.g. for a toolbar).
 */
fun pageSettleFling(
    pager: PagerState,
    transition: PageTransition,
    scope: CoroutineScope,
    onSettle: (from: Float, to: Int) -> Unit = { _, _ -> },
): TargetedFlingBehavior = object : TargetedFlingBehavior {
    override suspend fun ScrollScope.performFling(
        initialVelocity: Float,
        onRemainingDistanceUpdated: (Float) -> Unit,
    ): Float {
        val pageSize = (pager.layoutInfo.pageSize + pager.layoutInfo.pageSpacing).toFloat()
        if (pageSize <= 0f) return initialVelocity
        val position = pager.currentPage + pager.currentPageOffsetFraction
        val velocity = initialVelocity / pageSize
        val target = settleTarget(position, velocity, pager.pageCount)
        val distance = (target - position) * pageSize
        if (abs(distance) < 0.5f) return 0f
        onSettle(position, target)
        transition.settle(
            scope = scope,
            forward = target > position,
            from = abs(target - position),
            velocity = abs(velocity)
        ) { scrollBy(distance) }
        onRemainingDistanceUpdated(0f)
        return 0f
    }
}

/**
 * The page a swipe let go at [position] (fractional) with [velocity] (pages per second, + forward)
 * settles on: a flick past a third of a page a second picks the next page that way, otherwise the
 * nearest page. Shared by [pageSettleFling] and [pageSpringFling].
 */
internal fun settleTarget(position: Float, velocity: Float, pageCount: Int): Int = when {
    velocity > 0.35f -> floor(position).toInt() + 1
    velocity < -0.35f -> ceil(position).toInt() - 1
    else -> position.roundToInt()
}.coerceIn(0, (pageCount - 1).coerceAtLeast(0))

/**
 * A pager fling that's one spring the whole way (Motion Lab option B, 2026-11-21; the user: no linear
 * animations): it picks the page as [pageSettleFling] does, then springs the pages there with [spring],
 * starting at the finger's speed, so a flick carries into the spring and can overshoot and settle.
 * Unlike Compose's default pager fling there's no decay phase first. A touch while it moves stops the
 * pager first (it's really scrolling).
 */
fun pageSpringFling(pager: PagerState, spring: () -> MotionSpring): TargetedFlingBehavior =
    object : TargetedFlingBehavior {
        override suspend fun ScrollScope.performFling(
            initialVelocity: Float,
            onRemainingDistanceUpdated: (Float) -> Unit,
        ): Float {
            val pageSize = (pager.layoutInfo.pageSize + pager.layoutInfo.pageSpacing).toFloat()
            if (pageSize <= 0f) return initialVelocity
            val position = pager.currentPage + pager.currentPageOffsetFraction
            val target = settleTarget(position, initialVelocity / pageSize, pager.pageCount)
            val distance = (target - position) * pageSize
            var travelled = 0f
            Animatable(0f).animateTo(
                distance,
                spring().spec(Offset.VisibilityThreshold.x),
                initialVelocity = initialVelocity
            ) {
                travelled += scrollBy(value - travelled)
            }
            onRemainingDistanceUpdated(0f)
            return 0f
        }
    }
