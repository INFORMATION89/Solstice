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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
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

    // Held still under a finger (caught): stopped, but still on screen until the finger decides.
    private var caught by mutableStateOf(false)

    /**
     * Whether it's on screen: open, still moving back into its source, or caught on the way (2026-11-21:
     * a card caught while closing read as "closed" the moment its springs stopped, so the app removed it
     * and a tap couldn't reopen it).
     */
    val isVisible: Boolean
        get() = isOpen || caught || cx.isRunning || cy.isRunning || w.isRunning || h.isRunning || r.isRunning

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
        caught = false
        val props = listOf(
            cx to springs.position,
            cy to springs.position,
            w to springs.size,
            h to springs.size,
            r to springs.corner
        )
        props.forEachIndexed { i, (a, spring) ->
            val velocity = a.velocity + kick[i]
            // Settled within Compose's own pixel threshold (the default 0.01 for a bare Float kept a resting
            // surface "moving" for a long tail of invisible motion, so a touch on an open card was caught as
            // a tap that closed it).
            scope.launch { a.animateTo(v[i], spring.spec(PIXEL_THRESHOLD), initialVelocity = velocity) }
        }
    }

    /** Carries on to where it was heading (open or closed), from wherever it is now. */
    fun resume() = if (isOpen) open() else close()

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
     * Puts it at [progress] along the path (0 = the source, 1 = the target) at once, e.g. following a
     * predictive back gesture. Then [open] (cancelled) or [close] (committed) carries on from there.
     */
    fun scrub(progress: Float) {
        val s = source ?: return
        val t = target ?: return
        val p = progress.coerceIn(-0.05f, 1.08f)
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

        /** How close (px) a spring must be to count as settled: Compose's pixel visibility threshold. */
        val PIXEL_THRESHOLD = Offset.VisibilityThreshold.x
    }

    /** The finger lifted: the surface springs back up with a little bounce. */
    internal fun unpress() {
        scope.launch { press.animateTo(0f, Motion.playful.spec()) }
    }

    /** Whether any of the springs is moving. */
    val isMoving: Boolean
        get() = cx.isRunning || cy.isRunning || w.isRunning || h.isRunning || r.isRunning

    /** Open and still: its content owns the touches. Otherwise it's moving and catches them itself. */
    val isRestingOpen: Boolean get() = isOpen && !isMoving

    /** Where its handle is on screen, if it has one ([morphHandle]); a pull starting there is the surface's. */
    internal var handle: Rect = Rect.Zero

    /**
     * A finger came down on it: every spring stops where it is (iOS: caught mid-flight, never
     * cancelled); a drag carries on from there, a tap turns it back.
     */
    internal fun catch() {
        caught = true
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
 * Its look comes from [fromStyle] / [toStyle] (fill, outline), following [MorphMotion.progress], over
 * an optional app [material] (e.g. glass) drawn on its own layer at the surface's bounds; [shape] gives
 * the surface's outline (e.g. a sheet that squares its docked corners as it opens).
 *
 * Touch: while it moves, a touch on the surface catches it (a tap turns it back, a drag takes it over).
 * Once it's open and at rest, its content owns taps and sideways drags (e.g. a dial inside); a downward
 * drag pulls the surface when [canPull] (e.g. its page is scrolled to the top), measured on screen, so
 * the surface follows the finger however it scales.
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
    // A tap on the moving surface; by default it turns back the other way ([MorphMotion.toggle]).
    onTap: (() -> Unit)? = null,
    // The surface's outline for its corner radius and progress (default: evenly rounded).
    shape: ((radius: Float, progress: Float) -> Shape)? = null,
    // The app's material (e.g. glass) in the given outline, drawn behind the fill and the content.
    material: (@Composable Modifier.(Shape) -> Modifier)? = null,
    // Where it grows from when that isn't a registered [morphSource] (e.g. a card that has just closed
    // into this); [from] is then only the key to hide.
    fromBounds: MorphAnchor? = null,
    // Whether a downward drag on the open surface, starting at this point on screen, pulls it (e.g.
    // only from the top of its page, or only from a strip, leaving the page's own pull to refresh).
    canPull: (start: Offset) -> Boolean = { true },
    sourceContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val registry = LocalMorphRegistry.current
    val source = fromBounds ?: registry?.get(from) ?: return
    motion.source = source
    motion.target = target
    val density = LocalDensity.current
    // How far a finger travels for the whole path: the farthest an edge moves between source and target,
    // so the edge under the finger keeps up with it (2026-11-21: by the change in height, the moon sheet
    // moved at half the finger's speed).
    val dragRange = with(density) {
        maxOf(
            abs(target.bounds.top - source.bounds.top),
            abs(target.bounds.bottom - source.bounds.bottom)
        ).coerceAtLeast(200.dp.toPx())
    }

    // The real source hides while this is on screen, and comes back once it has landed.
    LaunchedEffect(motion, from) {
        snapshotFlow { motion.isVisible }.collect { visible ->
            if (registry == null) return@collect
            if (visible) registry.covered[from] = 1 else registry.covered.remove(from)
        }
    }
    LaunchedEffect(motion) {
        var last = 0L
        while (true) {
            androidx.compose.runtime.withFrameNanos { t ->
                if (motion.isVisible) {
                    val b = motion.bounds
                    android.util.Log.println(
                        android.util.Log.WARN,
                        "SolDbg",
                        "f dt=${(t - last) / 100_000 / 10f} l=${b.left} t=${b.top} w=${b.width} h=${b.height} r=${motion.cornerRadius} p=${motion.progress} moving=${motion.isMoving} resting=${motion.isRestingOpen}"
                    )
                }
                last = t
            }
        }
    }
    if (!motion.isVisible) return
    val outline = remember(motion, shape) { LiveMorphShape(motion, shape) }
    // Each touch layer's transform as last drawn: scale x, scale y, left, top. A finger's position in a
    // layer that moves and scales under it is turned back into screen space with these, so a drag
    // follows the finger (2026-11-21: measured in the layer, a pull ran away to the bottom on its own).
    val contentDrawn = remember { floatArrayOf(1f, 1f, 0f, 0f) }
    val catcherDrawn = remember { floatArrayOf(1f, 1f, 0f, 0f) }
    // At rest and open, the content owns its touches; while it moves, the surface catches them.
    // A finger on the catcher keeps it there until it lifts (2026-11-21: caught, the springs stopped,
    // the catcher went away under the finger and the surface stayed pressed in).
    var held by remember { mutableStateOf(false) }
    val restingOpen by remember(motion) { derivedStateOf { motion.isRestingOpen && !held } }

    fun hand(): Float = MotionValue.window(motion.progress, handOff.start, handOff.endInclusive).let {
        it * it *
            (3f - 2f * it)
    }

    // Moving (opening or closing), a touch reaches what's underneath as well as the surface: the
    // surface takes a tap or a vertical drag, a sideways swipe is left to the page below (user
    // 2026-11-21: a closing window held up swipes). At rest and open, it's the only thing touched.
    Box(modifier.morphPassThrough(motion).fillMaxSize()) {
        // The app's material at the surface's bounds (laid out each frame: a material like glass
        // needs a real node where the surface is, to blur what's behind it).
        if (material != null) {
            Box(
                Modifier
                    .layout { measurable, constraints ->
                        val b = motion.bounds
                        val placeable = measurable.measure(
                            Constraints.fixed(
                                b.width.roundToInt().coerceAtLeast(0),
                                b.height.roundToInt().coerceAtLeast(0)
                            )
                        )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(b.left.roundToInt(), b.top.roundToInt())
                        }
                    }
                    .material(outline)
            )
        }
        // The surface's look at its current bounds: drawn, never re-laid out.
        Box(
            Modifier.fillMaxSize().drawBehind {
                val b = motion.bounds
                val st = MorphStyle.lerp(fromStyle, toStyle, hand())
                val hasBorder = st.border.alpha > 0f && st.borderWidth.value > 0f
                if (st.fill.alpha <= 0f && !hasBorder) return@drawBehind
                val o = outline.createOutline(b.size, layoutDirection, this)
                translate(b.left, b.top) {
                    if (st.fill.alpha > 0f) drawOutline(o, st.fill)
                    if (hasBorder) drawOutline(o, st.border, style = Stroke(st.borderWidth.toPx()))
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
                    contentDrawn[0] = k
                    contentDrawn[1] = k
                    contentDrawn[2] = b.left
                    contentDrawn[3] = b.top
                    // The clip in the layer's own (unscaled) coordinates: the surface's height and
                    // corner divided by the scale (a new value each change, so the layer notices).
                    val height = b.height / k
                    this.shape =
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
        ) {
            Box(
                Modifier.pointerInput(motion, draggable, dragRange, restingOpen) {
                    if (!restingOpen || !draggable) return@pointerInput
                    // Seen before the content (Initial pass), so a pull can be taken before a page
                    // scrolls; otherwise left to the content, and dropped once a child takes it.
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        val tracker = VelocityTracker()
                        var last = onScreen(down.position, contentDrawn)
                        val start = last
                        tracker.addPosition(down.uptimeMillis, last)
                        var dragging = false
                        var dx = 0f
                        var dy = 0f
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            val now = onScreen(change.position, contentDrawn)
                            if (!change.pressed) {
                                if (dragging) {
                                    motion.release(-tracker.calculateVelocity().y / dragRange)
                                    change.consume()
                                }
                                break
                            }
                            tracker.addPosition(change.uptimeMillis, now)
                            val step = now - last
                            last = now
                            if (!dragging) {
                                dx += abs(step.x)
                                dy += step.y
                                if (dy > viewConfiguration.touchSlop && dy > dx &&
                                    (motion.handle.contains(start) || canPull(start))
                                ) {
                                    dragging = true
                                }
                            }
                            if (dragging) {
                                motion.drag(-step.y / dragRange)
                                change.consume()
                            } else {
                                // A child took it (a dial, a scroll): it's theirs.
                                val after = awaitPointerEvent(PointerEventPass.Final)
                                if (after.changes.any { it.id == down.id && it.isConsumed }) break
                            }
                        }
                    }
                }
            ) { content() }
        }
        // Touch while it moves: only the surface as drawn this frame (laid out once at the target's size and transformed
        // onto the current bounds, so hit-testing follows it). Everywhere else, touches reach the page
        // at once; on it, a tap turns it back from wherever it is and a drag takes it over (MOTION.md:
        // animations never block input).
        if (!restingOpen) {
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
                        catcherDrawn[0] = scaleX
                        catcherDrawn[1] = scaleY
                        catcherDrawn[2] = b.left
                        catcherDrawn[3] = b.top
                    }
                    // One gesture, the iOS way (user 2026-11-21: it couldn't be reopened while closing). A
                    // touch catches it where it is; moving vertically past the touch slop makes it a drag that
                    // follows the finger and is thrown on release; sideways, it lets go for the page below;
                    // lifting without moving is a tap that turns it back.
                    .pointerInput(motion, draggable, dragRange) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            held = true
                            motion.catch()
                            val tracker = VelocityTracker()
                            var last = onScreen(down.position, catcherDrawn)
                            tracker.addPosition(down.uptimeMillis, last)
                            var dragging = false
                            var moved = Offset.Zero
                            var released = false
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    val now = onScreen(change.position, catcherDrawn)
                                    if (!change.pressed) {
                                        released = true
                                        motion.unpress()
                                        val long = change.uptimeMillis - down.uptimeMillis >
                                            viewConfiguration.longPressTimeoutMillis
                                        when {
                                            dragging -> motion.release(-tracker.calculateVelocity().y / dragRange)
                                            // A hold isn't a tap (user 2026-11-21: holding the card closed it).
                                            long -> motion.resume()
                                            else -> onTap?.invoke() ?: motion.toggle()
                                        }
                                        change.consume()
                                        break
                                    }
                                    val step = now - last
                                    last = now
                                    moved += step
                                    tracker.addPosition(change.uptimeMillis, now)
                                    if (!dragging && moved.getDistance() > viewConfiguration.touchSlop) {
                                        if (draggable && abs(moved.y) >= abs(moved.x)) {
                                            dragging = true
                                            // Dragging, it follows the finger at full size.
                                            motion.unpress()
                                        } else {
                                            // A sideways swipe isn't for the surface: it carries on where it
                                            // was heading and the page underneath takes the swipe.
                                            released = true
                                            motion.unpress()
                                            motion.resume()
                                            break
                                        }
                                    }
                                    // Moves are only taken once it's a drag, so a sideways swipe stays
                                    // whole for the page underneath.
                                    if (dragging) {
                                        motion.drag(-step.y / dragRange)
                                        change.consume()
                                    }
                                }
                            } finally {
                                held = false
                                // Cancelled (the touch was taken away): never left caught and pressed in.
                                if (!released) {
                                    motion.unpress()
                                    motion.resume()
                                }
                            }
                        }
                    }
            )
        }
        // The source's own content, at its own size, riding at the top of the surface and centred:
        // blurs and fades out as the target's sharpens in (and back on the way home).
        if (sourceContent != null) {
            // Centred in the source's size, as a button centres its label (2026-11-21: pinned to the
            // corner, the moon pill's label sat up and to the left during the hand-off).
            // Drawn only: never in the accessibility tree. It must not take touches either (no click
            // handlers in it): above everything here, an invisible clickable copy ate the touches meant
            // for the surface and its content (2026-11-21: the window's handle and reopening).
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clearAndSetSemantics { }
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

/**
 * A morph's touch rule for whatever holds it on screen (agreed 2026-11-21, MOTION.md "animations never
 * block input"): while it moves, a touch reaches the page below as well as the surface (the surface
 * takes a tap or a vertical drag; a sideways swipe is the page's). Resting open, it's the only thing
 * touched. [SpringMorph] applies it to itself; a wrapper around it that sits beside the page needs it
 * too, as touches are only shared between siblings.
 */
fun Modifier.morphPassThrough(motion: MorphMotion): Modifier = shareTouchesWhile { !motion.isRestingOpen }

/**
 * Marks a handle that pulls [motion] (agreed 2026-11-21: Material 3's drag handle): a vertical drag that
 * starts on it drags the surface 1:1 and a release throws it (open or home, by the projected fling),
 * whatever [SpringMorph]'s `canPull` says. The drag itself is [SpringMorph]'s, measured on screen, so it
 * follows the finger as the surface scales. Put it on the handle's touch target inside the morph's
 * content; taps are left to the handle (e.g. a click that closes).
 */
fun Modifier.morphHandle(motion: MorphMotion): Modifier = onGloballyPositioned {
    motion.handle = it.boundsInRoot()
}

/**
 * While [share] is true, a touch that lands here also reaches the siblings underneath (Compose's
 * `sharePointerInputWithSiblings`), so something on top doesn't hide the page below from it. Put it on
 * the node that is the sibling of what's underneath.
 */
fun Modifier.shareTouchesWhile(share: () -> Boolean): Modifier = this then ShareTouchesElement(share)

private data class ShareTouchesElement(val share: () -> Boolean) : ModifierNodeElement<ShareTouchesNode>() {
    override fun create() = ShareTouchesNode(share)

    override fun update(node: ShareTouchesNode) {
        node.share = share
    }
}

private class ShareTouchesNode(var share: () -> Boolean) :
    Modifier.Node(),
    PointerInputModifierNode {
    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) = Unit

    override fun onCancelPointerInput() = Unit

    override fun sharePointerInputWithSiblings(): Boolean = share()
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

/** A position in a layer drawn with [drawn] (scale x, scale y, left, top) as a position on screen. */
private fun onScreen(local: Offset, drawn: FloatArray): Offset =
    Offset(local.x * drawn[0] + drawn[2], local.y * drawn[1] + drawn[3])

/**
 * The surface's outline as it is now: [custom] for the current radius and progress, or evenly rounded.
 * Reads the motion when asked, so one instance serves every frame.
 */
private class LiveMorphShape(
    private val motion: MorphMotion,
    private val custom: ((radius: Float, progress: Float) -> Shape)?,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = motion.cornerRadius.coerceAtMost(size.minDimension / 2f)
        custom?.let { return it(radius, motion.progress).createOutline(size, layoutDirection, density) }
        return Outline.Rounded(RoundRect(0f, 0f, size.width, size.height, CornerRadius(radius)))
    }
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
