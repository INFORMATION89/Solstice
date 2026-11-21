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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The springs of a [SpringMorph], one per property (docs/MORPH_STUDY.md): iOS lets position, size and
 * corners settle at their own pace instead of lerping everything with one value. Size trailing the
 * position on the way out (and leading it on the way back) is what reads as the surface stretching
 * out of its source and folding back into it.
 */
@Immutable
data class MorphSprings(
    val position: MotionSpring,
    val size: MotionSpring,
    val corner: MotionSpring,
) {
    companion object {
        /** Opened by a tap: a touch of bounce as it lands (brisk); the size a little behind the position. */
        val Open = MorphSprings(
            position = Motion.brisk,
            size = MotionSpring(response = Motion.brisk.response * 1.12f, damping = Motion.brisk.damping),
            corner = Motion.smooth
        )

        /** Closed by a tap or back: no bounce; the size a little ahead, so it folds as it travels. */
        val Close = MorphSprings(
            position = MotionSpring(response = Motion.smooth.response * 1.08f, damping = 1f),
            size = Motion.smooth,
            corner = Motion.smooth
        )

        /** Let go of a gesture with momentum, either way: the same, rewarded with a slight overshoot. */
        val Momentum = MorphSprings(
            position = Motion.momentum,
            size = MotionSpring(response = Motion.momentum.response * 1.1f, damping = Motion.momentum.damping),
            corner = Motion.smooth
        )
    }
}

/**
 * Where a [SpringMorph] is: five springs (centre x and y, width, height, corner radius) between its
 * source anchor and its target. Every move keeps each spring's velocity, so it can be redirected or
 * grabbed at any moment (iOS's "never cancelled"). A finger drives all five 1:1 along the path; let go,
 * the projected fling picks the end and each spring starts with the finger's velocity.
 */
@Stable
class MorphMotion internal constructor(private val scope: CoroutineScope) {
    internal var source: MorphAnchor? = null
    internal var target: MorphAnchor? = null
    private val cx = Animatable(0f)
    private val cy = Animatable(0f)
    private val w = Animatable(0f)
    private val h = Animatable(0f)
    private val r = Animatable(0f)
    private var placed = false

    // Under a finger the surface sinks a little and springs back when let go, like iOS 26's
    // interactive glass ("scales and bounces"; WWDC25 Meet Liquid Glass). 0 at rest, 1 pressed.
    private val press = Animatable(0f)

    /** How far a pressed surface sinks (scale), 0 for none. */
    var pressDepth: Float = MorphDefaults.PRESS_DEPTH

    /** Whether it's open or heading open. */
    var isOpen by mutableStateOf(false)
        private set

    /** Whether it's on screen: open, or still moving back into its source. */
    val isVisible: Boolean
        get() = isOpen || cx.isRunning || cy.isRunning || w.isRunning || h.isRunning || r.isRunning

    /** The surface's bounds now, in the host's coordinates. */
    val bounds: Rect
        get() {
            val k = 1f - pressDepth * press.value
            val width = (w.value * k).coerceAtLeast(0f)
            val height = (h.value * k).coerceAtLeast(0f)
            return Rect(Offset(cx.value - width / 2f, cy.value - height / 2f), Size(width, height))
        }

    /** Its corner radius now, in pixels. */
    val cornerRadius: Float get() = r.value.coerceAtLeast(0f)

    /**
     * How far it has grown from the source (0) to the target (1), by size (unclamped while it
     * overshoots). Content choreography follows this.
     */
    val progress: Float
        get() {
            val s = source ?: return 0f
            val t = target ?: return 0f
            val span = (t.bounds.height - s.bounds.height) + (t.bounds.width - s.bounds.width)
            if (abs(span) < 1f) return if (isOpen) 1f else 0f
            return ((h.value - s.bounds.height) + (w.value - s.bounds.width)) / span
        }

    // The corner as it actually looks: a pill's "100 dp" is really half its height. Springing from the
    // nominal value kept the corners pill-round too long, then they dropped (user 2026-11-21).
    private fun values(a: MorphAnchor) = listOf(
        a.bounds.center.x,
        a.bounds.center.y,
        a.bounds.width,
        a.bounds.height,
        a.cornerRadius.coerceAtMost(a.bounds.minDimension / 2f)
    )

    /** A tap on the surface: back the other way from wherever it is (never waits for it to finish). */
    fun toggle() = if (isOpen) close() else open()

    private suspend fun placeAtSource() {
        val s = source ?: return
        val v = values(s)
        cx.snapTo(v[0])
        cy.snapTo(v[1])
        w.snapTo(v[2])
        h.snapTo(v[3])
        r.snapTo(v[4])
        placed = true
    }

    /** Springs every property to [to] with [springs], each from its current velocity plus [kick]. */
    private fun animate(to: MorphAnchor, springs: MorphSprings, kick: List<Float> = List(5) { 0f }) {
        val v = values(to)
        val props = listOf(
            cx to springs.position,
            cy to springs.position,
            w to springs.size,
            h to springs.size,
            r to springs.corner
        )
        props.forEachIndexed { i, (a, spring) ->
            val velocity = a.velocity + kick[i]
            scope.launch { a.animateTo(v[i], spring.spec(), initialVelocity = velocity) }
        }
    }

    /** Opens, from wherever it is now (a tap: no bounce). */
    fun open(springs: MorphSprings = MorphSprings.Open) {
        val t = target ?: return
        isOpen = true
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            if (!placed) placeAtSource()
            animate(t, springs)
        }
    }

    /** Closes back into the source, from wherever it is now (a tap or back: no bounce). */
    fun close(springs: MorphSprings = MorphSprings.Close) {
        val s = source ?: return
        isOpen = false
        animate(s, springs)
    }

    /**
     * A finger moved by [delta] of the path (1 = from source to target): all five follow it at once,
     * along the straight path, so the surface stays under the finger.
     */
    fun drag(delta: Float) {
        val s = source ?: return
        val t = target ?: return
        val p = (progress + delta).coerceIn(-0.05f, 1.08f)
        val a = values(s)
        val b = values(t)
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            cx.snapTo(lerp(a[0], b[0], p))
            cy.snapTo(lerp(a[1], b[1], p))
            w.snapTo(lerp(a[2], b[2], p))
            h.snapTo(lerp(a[3], b[3], p))
            r.snapTo(lerp(a[4], b[4], p))
        }
    }

    /**
     * Let go at [velocity] (path per second, + towards the target). The projected fling picks open or
     * closed (WWDC18: the nearest end to where it would land); each spring starts with the finger's
     * velocity, and a flick with momentum gets the slight overshoot.
     */
    fun release(rawVelocity: Float, deceleration: Float = MotionPhysics.DECELERATION_NORMAL) {
        val s = source ?: return
        val t = target ?: return
        // A speed limit (user 2026-11-21: a very fast close broke the animation): the finger's speed is
        // handed to the springs only up to MAX_VELOCITY, so a hard flick still feels thrown but never
        // overshoots past the source or turns the surface inside out.
        val velocity = rawVelocity.coerceIn(-MAX_VELOCITY, MAX_VELOCITY)
        val landing = MotionPhysics.project(progress, velocity, deceleration)
        val opening = landing >= 0.5f
        val a = values(s)
        val b = values(t)
        val kick = List(5) { i -> velocity * (b[i] - a[i]) }
        val springs = when {
            abs(velocity) > MOMENTUM_VELOCITY -> MorphSprings.Momentum
            opening -> MorphSprings.Open
            else -> MorphSprings.Close
        }
        isOpen = opening
        // The springs carry their own velocity (zeroed by the drag's snaps), so the kick is the finger's.
        animate(if (opening) t else s, springs, kick)
    }

    private companion object {
        /** Path per second above which a release counts as a flick with momentum. */
        const val MOMENTUM_VELOCITY = 1.2f

        /** The fastest the springs are thrown, in path per second (~1/4 s for the whole way). */
        const val MAX_VELOCITY = 4f
    }

    /** The finger lifted: the surface springs back up with a little bounce. */
    internal fun unpress() {
        scope.launch { press.animateTo(0f, Motion.playful.spec()) }
    }

    /** Whether any of the springs is moving. */
    internal val isMoving: Boolean
        get() = cx.isRunning || cy.isRunning || w.isRunning || h.isRunning || r.isRunning

    /**
     * A finger came down on it: every spring stops where it is (iOS: caught mid-flight, never
     * cancelled); a drag carries on from there, a tap turns it back.
     */
    internal fun catch() {
        scope.launch { press.animateTo(1f, Motion.snappy.spec()) }
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            cx.stop()
            cy.stop()
            w.stop()
            h.stop()
            r.stop()
        }
    }
}

/** Remembers a [MorphMotion]. */
@Composable
fun rememberMorphMotion(): MorphMotion {
    val scope = rememberCoroutineScope()
    return remember(scope) { MorphMotion(scope) }
}

/**
 * A surface that grows out of anchor [from] into [target] and back, the iOS way (docs/MORPH_STUDY.md):
 * - five springs, not one progress value (see [MorphMotion], [MorphSprings]);
 * - drawn with transforms: the content is laid out **once** at the target's size and only scaled,
 *   moved and clipped while it grows, so nothing is measured per frame;
 * - the content is **carried**, growing uniformly with the surface (never squashed), not uncovered;
 * - the source's own content hands over with a **blur replace**: it blurs and fades out as the
 *   target's content sharpens in, so two readable things never overlap;
 * - the real source hides while this draws it (as [morphSource] does), and reappears once it's home.
 *
 * Its look comes from [fromStyle] / [toStyle] (fill, outline), following [MorphMotion.progress].
 */
@Composable
fun SpringMorph(
    from: Any,
    target: MorphAnchor,
    motion: MorphMotion,
    modifier: Modifier = Modifier,
    fromStyle: MorphStyle = MorphStyle.None,
    toStyle: MorphStyle = MorphStyle.None,
    handOff: ClosedFloatingPointRange<Float> = MorphHandOff.Window,
    handOffBlur: Dp = MorphHandOff.Blur,
    draggable: Boolean = true,
    // A tap on the surface; by default it turns back the other way ([MorphMotion.toggle]).
    onTap: (() -> Unit)? = null,
    sourceContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val registry = LocalMorphRegistry.current ?: return
    val source = registry[from] ?: return
    motion.source = source
    motion.target = target
    val density = LocalDensity.current
    val dragRange = with(density) { (target.bounds.height - source.bounds.height).coerceAtLeast(200.dp.toPx()) }

    // The real source hides while this is on screen, and comes back once it has landed.
    LaunchedEffect(motion, from) {
        snapshotFlow { motion.isVisible }.collect { visible ->
            if (visible) registry.covered[from] = 1 else registry.covered.remove(from)
        }
    }
    if (!motion.isVisible) return

    fun hand(): Float = MotionValue.window(motion.progress, handOff.start, handOff.endInclusive).let {
        it * it *
            (3f - 2f * it)
    }

    Box(modifier.fillMaxSize()) {
        // The surface's look at its current bounds: drawn, never re-laid out.
        Box(
            Modifier.fillMaxSize().drawBehind {
                val b = motion.bounds
                val st = MorphStyle.lerp(fromStyle, toStyle, hand())
                val rr = RoundRect(b, CornerRadius(motion.cornerRadius.coerceAtMost(b.minDimension / 2f)))
                val path = androidx.compose.ui.graphics.Path().apply { addRoundRect(rr) }
                if (st.fill.alpha > 0f) drawPath(path, st.fill)
                if (st.border.alpha > 0f && st.borderWidth.value > 0f) {
                    drawPath(path, st.border, style = Stroke(st.borderWidth.toPx()))
                }
            }
        )
        // The target's content: laid out once at the target's size, then scaled uniformly to the
        // surface's width, moved with it and clipped to it. Sharpens in as the source's blurs out.
        Box(
            Modifier
                .layout { measurable, constraints ->
                    val tw = target.bounds.width.roundToInt().coerceAtLeast(0)
                    val th = target.bounds.height.roundToInt().coerceAtLeast(0)
                    val placeable = measurable.measure(Constraints.fixed(tw, th))
                    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, 0) }
                }
                .graphicsLayer {
                    val b = motion.bounds
                    val k = (b.width / target.bounds.width.coerceAtLeast(1f)).coerceAtLeast(0.001f)
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = k
                    scaleY = k
                    translationX = b.left
                    translationY = b.top
                    // The clip in the layer's own (unscaled) coordinates: the surface's height and
                    // corner divided by the scale (a new value each change, so the layer notices).
                    val height = b.height / k
                    shape =
                        MorphClip(
                            height,
                            (motion.cornerRadius / k).coerceAtMost(minOf(target.bounds.width, height) / 2f)
                        )
                    clip = true
                    val t = hand()
                    alpha = t
                    val blur = handOffBlur.toPx() * (1f - t)
                    renderEffect = if (blur > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        BlurEffect(blur, blur, TileMode.Decal)
                    } else {
                        null
                    }
                }
        ) { content() }
        // Touch: only the surface as drawn this frame (laid out once at the target's size and transformed
        // onto the current bounds, so hit-testing follows it). Everywhere else, touches reach the page
        // at once; on it, a tap turns it back from wherever it is and a drag takes it over (MOTION.md:
        // animations never block input).
        Box(
            Modifier
                .layout { measurable, constraints ->
                    val tw = target.bounds.width.roundToInt().coerceAtLeast(0)
                    val th = target.bounds.height.roundToInt().coerceAtLeast(0)
                    val placeable = measurable.measure(Constraints.fixed(tw, th))
                    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, 0) }
                }
                .graphicsLayer {
                    val b = motion.bounds
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = (b.width / target.bounds.width.coerceAtLeast(1f)).coerceAtLeast(0.001f)
                    scaleY = (b.height / target.bounds.height.coerceAtLeast(1f)).coerceAtLeast(0.001f)
                    translationX = b.left
                    translationY = b.top
                }
                // One gesture, the iOS way (user 2026-11-21: it couldn't be reopened while closing). A
                // touch catches it where it is; moving past the touch slop makes it a drag that follows
                // the finger and is thrown on release; lifting without moving is a tap that turns it back.
                .pointerInput(motion, draggable, dragRange) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        motion.catch()
                        val tracker = VelocityTracker()
                        tracker.addPosition(down.uptimeMillis, down.position)
                        var dragging = false
                        var travelled = 0f
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                motion.unpress()
                                if (dragging) {
                                    val v = tracker.calculateVelocity().y
                                    motion.release(-v / dragRange)
                                } else {
                                    onTap?.invoke() ?: motion.toggle()
                                }
                                change.consume()
                                break
                            }
                            val dy = change.position.y - change.previousPosition.y
                            travelled += abs(change.position.y - change.previousPosition.y) +
                                abs(change.position.x - change.previousPosition.x)
                            tracker.addPosition(change.uptimeMillis, change.position)
                            if (!dragging && draggable && travelled > viewConfiguration.touchSlop) {
                                dragging = true
                                // Dragging, it follows the finger at full size.
                                motion.unpress()
                            }
                            if (dragging) motion.drag(-dy / dragRange)
                            change.consume()
                        }
                    }
                }
        )
        // The source's own content, at its own size, riding at the top of the surface and centred:
        // blurs and fades out as the target's sharpens in (and back on the way home).
        if (sourceContent != null) {
            Box(
                Modifier
                    .layout { measurable, constraints ->
                        val sw = source.bounds.width.roundToInt().coerceAtLeast(0)
                        val sh = source.bounds.height.roundToInt().coerceAtLeast(0)
                        val placeable = measurable.measure(Constraints.fixed(sw, sh))
                        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, 0) }
                    }
                    .graphicsLayer {
                        val b = motion.bounds
                        translationX = b.center.x - source.bounds.width / 2f
                        translationY = b.top + (minOf(b.height, source.bounds.height) - source.bounds.height) / 2f
                        val t = hand()
                        alpha = 1f - t
                        val blur = handOffBlur.toPx() * t
                        renderEffect = if (blur > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            BlurEffect(blur, blur, TileMode.Decal)
                        } else {
                            null
                        }
                    }
            ) { sourceContent() }
        }
    }
}

/** Defaults for [SpringMorph]'s hand-over from the source's content to the target's. */
object MorphHandOff {
    /** The part of the growth over which the source's content hands over (blur replace). */
    val Window: ClosedFloatingPointRange<Float> = 0.05f..0.45f

    /**
     * How blurred each side is at its faintest: strong enough to read as a blur, not a soft focus
     * (user 2026-11-21: 8 dp was too subtle).
     */
    val Blur: Dp = 20.dp
}

/** [SpringMorph]'s content clip: [height] and [radius] in the content layer's unscaled coordinates. */
private data class MorphClip(val height: Float, val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Rounded(RoundRect(0f, 0f, size.width, height.coerceAtLeast(0f), CornerRadius(radius.coerceAtLeast(0f))))
}

/** Waits until [motion] is on screen (for an opening that should start from the source's first frame). */
suspend fun MorphMotion.awaitVisible() {
    snapshotFlow { isVisible }.first { it }
}
