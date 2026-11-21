@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import io.github.information89.motion.Haptic
import io.github.information89.motion.Morph
import io.github.information89.motion.MorphAnchor
import io.github.information89.motion.MorphGeometry
import io.github.information89.motion.MorphHost
import io.github.information89.motion.MorphStyle
import io.github.information89.motion.Motion
import io.github.information89.motion.MotionPhysics
import io.github.information89.motion.MotionValue
import io.github.information89.motion.haptics
import io.github.information89.motion.morphAnchor
import io.github.information89.motion.morphAnchorOf
import io.github.information89.motion.morphFrom
import io.github.information89.motion.morphSource
import io.github.information89.motion.motionDrag
import io.github.information89.motion.rememberMorphDrawn
import io.github.information89.motion.rememberMotionValue
import io.github.information89.motion.reveal
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.ui.expressive.components.GlassSource
import org.breezyweather.ui.expressive.components.SolsticeMotion
import org.breezyweather.ui.expressive.components.glass
import org.breezyweather.ui.expressive.sky.SkyVisibility
import org.breezyweather.ui.expressive.sky.Tonight
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.SolsticePalette
import org.breezyweather.ui.expressive.theme.Space
import org.breezyweather.ui.expressive.theme.StrokeWidth
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonPhase
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.MoonTimes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.random.Random

private const val DAY = 24L * 60 * 60 * 1000

/** The moon's phase angle (0/360 new, 180 full) and how lit it is, at [date]. */
private fun moonAt(date: Date): Pair<Int, Float> {
    val phase = MoonIllumination.compute().on(date).execute().phase
    val angle = (phase + 180).roundToInt().mod(360)
    val lit = ((1 - cos(Math.toRadians(angle.toDouble()))) / 2).toFloat()
    return angle to lit
}

/**
 * A moon in its phase: a pale disc with soft grey "seas", and the shadow sliding across it; lit on
 * the right while waxing (angle < 180).
 */
internal fun DrawScope.drawMoon(center: Offset, r: Float, angle: Int, lit: Float, shadow: Color, alpha: Float = 1f) {
    if (alpha < 0.999f) {
        // Faded as a whole (e.g. towards a dial's edges).
        val paint = androidx.compose.ui.graphics.Paint().apply { this.alpha = alpha }
        drawContext.canvas.saveLayer(Rect(center, r * 1.1f), paint)
        drawMoon(center, r, angle, lit, shadow, 1f)
        drawContext.canvas.restore()
        return
    }
    drawCircle(
        Brush.radialGradient(
            listOf(SolsticePalette.MoonLight, SolsticePalette.MoonShade),
            center = center - Offset(r / 3, r / 3),
            radius =
            r * 1.6f
        ),
        radius = r,
        center = center
    )
    // A few soft seas, always in the same places.
    val rnd = Random(4)
    repeat(6) {
        val o = Offset((rnd.nextFloat() - 0.5f) * r * 1.2f, (rnd.nextFloat() - 0.5f) * r * 1.2f)
        drawCircle(
            SolsticePalette.MoonCrater.copy(alpha = 0.28f),
            radius = r * (0.14f + rnd.nextFloat() * 0.18f),
            center =
            center + o
        )
    }
    val waxing = angle < 180
    val shift = 2f * r * lit * if (waxing) -1f else 1f
    clipPath(Path().apply { addOval(Rect(center, r)) }) {
        drawCircle(shadow, radius = r * 1.02f, center = center + Offset(shift, 0f))
    }
}

/** The dial's fling, slowing down. */
private val dialDecay = FloatExponentialDecaySpec(frictionMultiplier = 1.6f)

/** The dial's settle spring. */
private val moonSettle = Motion.settle.spec<Float>()

private fun hhmm(zone: java.util.TimeZone) = SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = zone }

/** Morph keys for a place's moon button and its label. */
private fun pillKey(location: Location) = "moon-pill-${location.formattedId}"
private fun labelKey(location: Location) = "moon-label-${location.formattedId}"

/**
 * Choreography on the morph's value (M3, 2026-11-19): the pill's own look (outline, "· 71 %") fades
 * over the first 30 % while the sheet's glass fades in over the same stretch; the label hands over to
 * the heading in the first 15 %. Closing plays the same windows backwards.
 */
private const val MATERIAL_SPAN = 0.3f
private const val LABEL_SPAN = 0.15f

/**
 * How far the sheet's top travels between the pill and open, for the drag, until the pill has been
 * measured (then the real distance is used, so the top edge stays under the finger, like iOS).
 */
private val MOON_RANGE = 420.dp

/** iOS: a sheet pulled past open travels up to about 100 pt. */
private val MOON_OVER_PULL = 100.dp

private fun smooth(t: Float) = t * t * (3 - 2 * t)

/**
 * Wraps the shell so the moon button (deep in a page) and the moon sheet (over everything) share one
 * morph space: the :motion module draws the morph from their anchors (M3, no shared transition).
 */
@Composable
internal fun MoonSharedLayout(content: @Composable () -> Unit) {
    MorphHost(Modifier.fillMaxSize()) { content() }
}

/** The moon phase button under the sun path: always visible, outlined so it reads as a button. */
@Composable
internal fun MoonButton(
    location: Location,
    now: Long,
    text: Color,
    palette: org.breezyweather.ui.expressive.sky.SkyPalette,
    night: Boolean,
) {
    val context = LocalContext.current
    val (angle, lit) = moonAt(Date(now))
    val phase = breezyweather.domain.weather.model.MoonPhase(angle).getDescription(context) ?: ""
    val suffix = " · ${(lit * 100).roundToInt()} %"
    // The screen it's on (home, or a postcard window): the card blurs that.
    val pageGlass = org.breezyweather.ui.expressive.components.LocalPageGlass.current
    // Get the moon's texture ready in the background, so opening the card doesn't wait for it.
    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { MoonRenderer.prewarm(context) }
    }
    // While its card is open, the morph draws the pill exactly as it is and grows it into the sheet
    // (the button becomes the card, 2026-11-21); morphSource hides the real pill only while it does.
    // Material 3 outlined button, in the sky's ink.
    // One material at both ends (MOTION.md, 2026-11-21): the pill is the sheet's own glass, in the
    // same tint, so opening grows it rather than swapping an outline for glass.
    val pill = androidx.compose.foundation.shape.CircleShape
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .morphSource(pillKey(location), cornerRadius = 100.dp)
            .glass(pageGlass, pill, moonGlassTint(palette, night))
            .clip(pill)
            .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                MoonCardState.glass = pageGlass
                MoonCardState.buttonInk = text
                MoonCardState.show(location, now, null, phase + suffix, palette, night)
            }
            .padding(horizontal = Space.S3, vertical = Space.S2)
    ) {
        MoonPillContent(phase, suffix, text, Modifier.morphAnchor(labelKey(location)))
    }
}

/**
 * The moon's glass tint, shared by the pill and the sheet it grows into: the sky's base colour,
 * darkened at night (dark mode when it's night at the place) and lightened by day.
 */
@Composable
internal fun moonGlassTint(sky: org.breezyweather.ui.expressive.sky.SkyPalette?, night: Boolean): Color = when {
    sky == null -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    night -> lerp(sky.base, SolsticePalette.NightDeep, 0.55f).copy(alpha = 0.62f)
    else -> lerp(sky.base, Color.White, 0.55f).copy(alpha = 0.55f)
}

/** The pill's label: the phase and its "· 85 %". The morph draws the same, so the swap is invisible. */
@Composable
private fun MoonPillContent(phase: String, suffix: String, text: Color, labelModifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(phase, color = text, style = MaterialTheme.typography.labelLarge, modifier = labelModifier)
        Text(suffix, color = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Which place's moon card is open (the shell draws it over everything, on its glass). */
internal object MoonCardState {
    var request by androidx.compose.runtime.mutableStateOf<Pair<Location, Long>?>(null)

    /** The open card's morph value (0 closed … 1 open), while it's on screen. */
    var motion by androidx.compose.runtime.mutableStateOf<MotionValue?>(null)

    /** The button's label. */
    var label: String = ""

    /** The forecast's sky the card is tinted with, and whether it's night at the place (dark card). */
    var palette: org.breezyweather.ui.expressive.sky.SkyPalette? = null
    var night: Boolean = true

    /** The button's text colour. */
    var buttonInk: Color = Color.White

    /** The glass of the screen the card was opened from (null: the shell's). */
    var glass: GlassSource? = null

    /** 0 closed … 1 open; the shell reads it to recede the page, like search. */
    var progress by mutableFloatStateOf(0f)

    fun show(
        location: Location,
        now: Long,
        @Suppress("UNUSED_PARAMETER") from: Rect?,
        text: String,
        sky: org.breezyweather.ui.expressive.sky.SkyPalette,
        isNight: Boolean,
    ) {
        label = text
        palette = sky
        night = isNight
        request = location to now
    }
}

/**
 * The sheet's shape: round on top (as the button), and the bottom corners square off as it docks.
 * Reads the morph's value when the outline is built (every size change, i.e. every frame of the
 * morph).
 */
private class MoonSheetShape(private val motion: MotionValue, private val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val top = minOf(radius, size.minDimension / 2)
        val bottom = top * (1f - motion.value.coerceIn(0f, 1f))
        return Outline.Rounded(
            RoundRect(
                0f,
                0f,
                size.width,
                size.height,
                topLeftCornerRadius = CornerRadius(top),
                topRightCornerRadius = CornerRadius(top),
                bottomRightCornerRadius = CornerRadius(bottom),
                bottomLeftCornerRadius = CornerRadius(bottom)
            )
        )
    }
}

/**
 * The moon card as a sheet, like the search card (agreed 2026-11-19): the same glass, docked at the
 * bottom at full screen height, a handle on top. It's the button grown: one morph value (M3,
 * `:motion`) drives the shape, the glass, the heading flying from the button's label and the
 * content. Pull it down and the morph runs backwards under the finger; let go and UIKit's rules pick
 * open or closed (agreed 2026-11-19); pulling up past open rubber-bands like iOS.
 */
@Composable
internal fun MoonCardOverlay(glass: GlassSource) {
    val request = MoonCardState.request ?: return
    // A fresh morph value per opening.
    androidx.compose.runtime.key(request) { MoonSheet(request.first, request.second, glass) }
}

@Composable
private fun MoonSheet(location: Location, now: Long, glass: GlassSource) {
    val density = LocalDensity.current
    val motion = rememberMotionValue(
        initial = 0f,
        overMin = 0.02f,
        overMax = MOON_OVER_PULL / MOON_RANGE,
        // UIKit's normal deceleration for the fling (agreed 2026-11-19): a medium flick down closes
        // it, as an iOS sheet would; a slow lift still springs back open.
        deceleration = MotionPhysics.DECELERATION_NORMAL
    )
    androidx.compose.runtime.DisposableEffect(motion) {
        MoonCardState.motion = motion
        onDispose { if (MoonCardState.motion === motion) MoonCardState.motion = null }
    }
    // Opening: the container's spring from the button, once the morph is on screen as the button
    // (rememberMorphDrawn), so it visibly starts from the pill however long the sheet takes to build.
    val drawn = rememberMorphDrawn(pillKey(location))
    LaunchedEffect(motion) {
        androidx.compose.runtime.snapshotFlow { drawn.value }.first { it }
        motion.animateTo(1f, velocity = 0f)
    }
    // Tell the shell how far it is (the page recedes), and finish once it has closed.
    LaunchedEffect(motion) {
        var opening = false
        androidx.compose.runtime.snapshotFlow { Triple(motion.value, motion.target, motion.isRunning) }
            .collect { (v, target, running) ->
                MoonCardState.progress = v.coerceIn(0f, 1f)
                if (target == 1f) opening = true
                if (opening && target == 0f && !running && v <= 0.001f) {
                    MoonCardState.progress = 0f
                    MoonCardState.request = null
                }
            }
    }
    fun close() = motion.animateTo(0f)
    BackHandler { close() }

    // The forecast's tint: the sky's base colour, darkened at night (the card goes dark mode when it's
    // night at the place) and lightened by day.
    val sky = MoonCardState.palette
    val night = MoonCardState.night
    val tint = moonGlassTint(sky, night)
    val ink = when {
        sky == null -> MaterialTheme.colorScheme.onSurface
        night -> SolsticePalette.MoonNightText
        else -> sky.text
    }
    val status = WindowInsets.statusBars
    // Where the sheet rests: full width, docked at the bottom, at full screen height below the status
    // bar (agreed 2026-11-19). Known up front, so the morph draws from its first frame.
    val window = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize
    val radius = with(density) { Corners.XL.toPx() }
    val top = status.getTop(density) + with(density) { Space.S3.toPx() }
    val target = remember(window, top, radius) {
        MorphAnchor(Rect(0f, top, window.width.toFloat(), window.height.toFloat()), radius)
    }
    val shape = remember(motion, radius) { MoonSheetShape(motion, radius) }
    // The drag tracks the finger 1:1 (agreed 2026-11-19): one unit of the morph is the distance the
    // sheet's top travels, from the moon pill to open.
    val pill = morphAnchorOf(pillKey(location))
    val range = pill?.let { with(density) { (it.bounds.top - top).coerceAtLeast(1f).toDp() } }
        ?.coerceAtLeast(200.dp) ?: MOON_RANGE

    // Closing (agreed 2026-11-21, MOTION.md: animations never block input): once it heads back to the
    // pill, only the sheet as drawn catches a touch (to grab it mid-close); everywhere else, taps reach
    // the page underneath at once.
    val closing by remember(motion) { androidx.compose.runtime.derivedStateOf { motion.target == 0f } }
    // Gestures on the whole screen while open (the sheet shrinks under the finger while a pull scrubs
    // it, so a drag measured on it would run ahead): vertical pulls drive the morph, a tap above it
    // closes.
    Box(
        Modifier
            .fillMaxSize()
            .then(
                if (closing) {
                    Modifier
                } else {
                    Modifier
                        .motionDrag(motion, Orientation.Vertical, range, reverseDirection = true)
                        .pointerInput(Unit) {
                            detectTapGestures { tap ->
                                if (tap.y < status.getTop(this) + Space.S3.toPx()) close()
                            }
                        }
                }
            )
    ) {
        // The page behind goes a little darker.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = motion.value.coerceIn(0f, 1f) }
                .background(Color.Black.copy(alpha = 0.25f))
        )
        Morph(
            from = pillKey(location),
            target = target,
            value = motion,
            modifier = Modifier.fillMaxSize(),
            // Laid out once at the sheet's final size and centred, so nothing reflows while the glass
            // grows around it.
            contentAlignment = Alignment.TopCenter,
            shape = { _, _ -> shape },
            // Starts as the pill itself (the same glass, its label) and grows into the sheet: one
            // material the whole way (MOTION.md), only the shape, size and content change.
            fromStyle = MorphStyle(material = 1f),
            toStyle = MorphStyle(material = 1f),
            styleWindow = 0f..MATERIAL_SPAN,
            material = { amount -> glass(MoonCardState.glass ?: glass, shape, tint, alpha = amount) },
            sourceWindow = 0f..LABEL_SPAN,
            sourceContent = {
                val label = MoonCardState.label
                val phase = label.substringBefore(" · ")
                MoonPillContent(phase, label.removePrefix(phase), MoonCardState.buttonInk)
            }
        ) {
            Box(Modifier.fillMaxSize()) {
                Column {
                    Spacer(Modifier.height(Space.S6)) // room for the handle, drawn on top
                    MoonCard(
                        location,
                        now,
                        ink,
                        // The button's label flies to the heading and hands over in the first 15 %.
                        headingModifier = Modifier
                            .morphFrom(labelKey(location))
                            .graphicsLayer { alpha = motion.window(0f, LABEL_SPAN) },
                        dateModifier = Modifier.graphicsLayer {
                            // Last in, first out: only once the heading has landed.
                            alpha = motion.window(0.85f, 1f)
                        },
                        deferRest = true,
                        interactive = !closing,
                        restModifier = Modifier.graphicsLayer {
                            // Choreographed (agreed): the content fades and scales in from 92 % as the
                            // glass reaches ~70 % of its size, and leaves first when it closes.
                            val e = smooth(motion.window(SolsticeMotion.CONTENT_FROM, SolsticeMotion.CONTENT_TO))
                            alpha = e
                            val k = SolsticeMotion.CONTENT_START_SCALE + (1f - SolsticeMotion.CONTENT_START_SCALE) * e
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                            scaleX = k
                            scaleY = k
                        }
                    )
                }
                // The handle, like search's: it comes and goes with the content (it sat on the pill at
                // the end of a close).
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .reveal(motion, SolsticeMotion.CONTENT_FROM, SolsticeMotion.CONTENT_TO, startScale = 1f)
                        .padding(top = Space.S3)
                        .width(Sizes.HandleWidth)
                        .height(Sizes.HandleThickness)
                        .background(ink.copy(alpha = 0.4f), CircleShape)
                )
            }
        }
        // While closing: a catcher exactly where the sheet is drawn this frame, so the shrinking sheet
        // can still be grabbed (and pulled back open) without blocking the rest of the screen.
        if (closing && pill != null) {
            Box(
                Modifier
                    .offset {
                        val r = MorphGeometry.frame(pill, target, motion.value).bounds
                        IntOffset(r.left.roundToInt(), r.top.roundToInt())
                    }
                    .layout { measurable, _ ->
                        val r = MorphGeometry.frame(pill, target, motion.value).bounds
                        val w = r.width.roundToInt().coerceAtLeast(0)
                        val h = r.height.roundToInt().coerceAtLeast(0)
                        val placeable = measurable.measure(Constraints.fixed(w, h))
                        layout(w, h) { placeable.place(0, 0) }
                    }
                    .motionDrag(motion, Orientation.Vertical, range, reverseDirection = true)
            )
        }
    }
}

/**
 * The moon card's content (redesigned 2026-11-19, design A): the phase as a big heading with the
 * date, the moon in its phase, the dial of days to scrub through phases (drag), and the details in
 * a sentence (values bold and underlined, like This week).
 */
@Composable
private fun MoonCard(
    location: Location,
    now: Long,
    ink: Color,
    headingModifier: Modifier = Modifier,
    dateModifier: Modifier = Modifier,
    restModifier: Modifier = Modifier,
    // Whether it takes touches (the dial): not while the card is closing.
    interactive: Boolean = true,
    // Built a frame after the heading when opening (it's invisible until a third of the way in), so
    // the morph's first frame only has the glass and the heading to build.
    deferRest: Boolean = false,
) {
    var restReady by remember { mutableStateOf(!deferRest) }
    if (!restReady) {
        LaunchedEffect(Unit) {
            androidx.compose.runtime.withFrameNanos { }
            restReady = true
        }
    }
    val context = LocalContext.current
    // Scrubbing offset in days (fractional while dragging).
    var offset by remember { mutableFloatStateOf(0f) }
    val haptics = haptics()
    val phaseIcons = rememberMoonPhaseIcons()
    // The principal phases within the dial's range, and their angles. Worked out in the background
    // (2026-11-19): on the main thread they held up the first frame of the card's opening, and the
    // dial's marks are only seen once the content has faded in.
    val phaseMarks by androidx.compose.runtime.produceState(emptyList<Pair<Long, Int>>(), now) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val start = Date(now - 15 * DAY)
            listOf(
                MoonPhase.Phase.NEW_MOON to 0,
                MoonPhase.Phase.FIRST_QUARTER to 90,
                MoonPhase.Phase.FULL_MOON to 180,
                MoonPhase.Phase.LAST_QUARTER to 270
            ).flatMap { (phase, a) ->
                buildList {
                    var from = start
                    repeat(2) {
                        val t = MoonPhase.compute().phase(phase).on(from).execute().time
                        add(t.time to a)
                        from = Date(t.time + DAY)
                    }
                }
            }.filter { abs(it.first - now) <= 15 * DAY }
        }
    }
    // "Will I see it tonight?": worked out in the background, like the phase marks.
    val hourly = location.weather?.nextHourlyForecast
    val tonight by androidx.compose.runtime.produceState<Tonight?>(null, now, hourly) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            MoonVisibility.tonight(
                now,
                location.latitude,
                location.longitude,
                hourly.orEmpty(),
                moonAt(Date(now)).second
            )
        }
    }
    var fling by remember { androidx.compose.runtime.mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Haptics: a firm tick per day, a lighter one per 6-hour step.
    fun tick(before: Float, after: Float) {
        when {
            kotlin.math.floor(before + 0.5f) != kotlin.math.floor(after + 0.5f) ->
                haptics.perform(Haptic.detent)
            kotlin.math.floor(before * 4f + 0.5f) != kotlin.math.floor(after * 4f + 0.5f) ->
                haptics.perform(Haptic.tick)
        }
    }
    val moon = MoonRenderer.remember()
    val scope = rememberCoroutineScope()
    val at = Date(now + (offset * DAY).toLong())
    val (angle, lit) = moonAt(at)
    val zone = location.timeZone
    // Rise and set, the next full moon and the distance only change by the minute: worked out per
    // 15 minutes of dial time, not on every frame of a scrub.
    val quarterHour = (at.time / 900_000L) * 900_000L
    val times = remember(quarterHour, zone, location.latitude, location.longitude) {
        MoonTimes.compute().on(Date(quarterHour)).timezone(zone).at(location.latitude, location.longitude)
            .fullCycle().execute()
    }
    val full = remember(quarterHour) {
        MoonPhase.compute().phase(MoonPhase.Phase.FULL_MOON).on(Date(quarterHour)).execute().time
    }
    val position = remember(quarterHour, location.latitude, location.longitude) {
        MoonPosition.compute().on(Date(quarterHour)).at(location.latitude, location.longitude).execute()
    }
    val distance = position.distance
    // How the moon is turned in this place's sky at the shown time: the real parallactic angle while
    // the moon is up, easing to upright as it nears and drops below the horizon (where its true
    // tilt would look wild). To keep it calm, the tilt is averaged over the hours around the shown
    // time (so each 6-hour tick doesn't rock it), and a heavy, slow spring follows that: a flick of
    // the dial mostly passes the moon by, and it turns gently into place once the dial settles.
    // One calculation with the Forecast diagram's icon (MoonSky).
    val limbAngle = MoonSky.limbAngle(at)
    val centre = MoonSky.hourOf(now + (offset * DAY).toLong())
    val targetParallactic = remember(centre, location.latitude, location.longitude) {
        MoonSky.parallactic(centre, location.latitude, location.longitude)
    }
    // A slow drift on purpose (≈1.8 s), not a gesture's motion: one of the few documented exceptions
    // to the motion tokens (agreed 2026-11-19).
    val parallactic by animateFloatAsState(
        targetParallactic.toFloat(),
        spring(dampingRatio = 1f, stiffness = 12f),
        label = "moonTilt"
    )
    run {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Space.S5, end = Space.S5, top = Space.S2, bottom = Space.S5)
        ) {
            // A big heading, like This week: the phase, and the date under it. It's the button's label
            // grown (shared bounds), so it morphs from and back into the button.
            val heading = breezyweather.domain.weather.model.MoonPhase(angle).getDescription(context) ?: ""
            Text(
                heading,
                color = ink,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = headingModifier
            )
            if (restReady) {
                Column(restModifier) {
                    Text(
                        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).apply { timeZone = zone }.format(at),
                        color = ink.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = dateModifier
                    )
                    // The moon (on a starry night when the glass is dark).
                    val dark = ink.luminance() > 0.5f
                    Canvas(Modifier.fillMaxWidth().height(200.dp)) {
                        if (dark) {
                            val rnd = Random(11)
                            repeat(70) {
                                drawCircle(
                                    Color.White.copy(alpha = 0.2f + rnd.nextFloat() * 0.5f),
                                    radius = 0.6f + rnd.nextFloat() * 1.4f,
                                    center = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
                                )
                            }
                        }
                        // A soft glow, then the real moon.
                        drawCircle(
                            Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                                center = center,
                                radius = size.minDimension * 0.6f
                            ),
                            radius = size.minDimension * 0.6f,
                            center = center
                        )
                        moon.draw(
                            this,
                            center,
                            size.minDimension * 0.36f,
                            angle,
                            SolsticePalette.NightDeepest,
                            limbAngle = limbAngle,
                            parallactic = parallactic.toDouble()
                        )
                    }
                    Spacer(Modifier.height(Space.S1))
                    // The dial: ticks every 6 hours, a longer one per day; drag to move through the days.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            // Only while the card is open: closing, it lets every touch through to the page (MOTION.md).
                            .then(
                                if (interactive) {
                                    Modifier.pointerInput(Unit) {
                                        val perDay = size.width / 4f
                                        val tracker = VelocityTracker()
                                        detectHorizontalDragGestures(
                                            onDragStart = {
                                                fling?.cancel()
                                                tracker.resetTracking()
                                            },
                                            onDragEnd = {
                                                // Inertia: a flick keeps the dial turning, slowing down, then it
                                                // settles on the nearest 6-hour tick.
                                                val v = -tracker.calculateVelocity().x / perDay
                                                fling = scope.launch {
                                                    var last = offset
                                                    animateDecay(offset, v, dialDecay) {
                                                            value,
                                                            _,
                                                        ->
                                                        offset = value.coerceIn(-14f, 14f)
                                                        tick(last, offset)
                                                        last = offset
                                                    }
                                                    val snapped = (
                                                        kotlin.math.round(
                                                            offset * 4f
                                                        ) / 4f
                                                        ).coerceIn(-14f, 14f)
                                                    animate(
                                                        offset,
                                                        snapped,
                                                        animationSpec = moonSettle
                                                    ) {
                                                            value,
                                                            _,
                                                        ->
                                                        offset = value
                                                    }
                                                }
                                            }
                                        ) { change, dx ->
                                            change.consume()
                                            tracker.addPosition(change.uptimeMillis, change.position)
                                            val before = offset
                                            // Drag left to go forward in time (the ticks follow the finger).
                                            offset = (offset - dx / perDay).coerceIn(-14f, 14f)
                                            tick(before, offset)
                                        }
                                    }
                                        // Double-tap: back to today.
                                        .pointerInput(Unit) {
                                            detectTapGestures(onDoubleTap = {
                                                scope.launch {
                                                    animate(
                                                        offset,
                                                        0f,
                                                        animationSpec = moonSettle
                                                    ) {
                                                            v,
                                                            _,
                                                        ->
                                                        offset = v
                                                    }
                                                    haptics.perform(Haptic.snap)
                                                }
                                            })
                                        }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Canvas(Modifier.fillMaxWidth().padding(top = Space.S4).height(28.dp)) {
                            val perDay = size.width / 4f
                            val mid = size.width / 2
                            for (q in -40..40) {
                                val d = q / 4f - (offset - offset.toInt())
                                val x = mid + d * perDay
                                if (x < 0 || x > size.width) continue
                                val major = (q + (offset.toInt() * 4)) % 4 == 0
                                val fade = 1f - (abs(x - mid) / mid).coerceIn(0f, 1f) * 0.8f
                                drawLine(
                                    ink.copy(alpha = (if (major) 0.9f else 0.35f) * fade),
                                    Offset(x, if (major) 2f else 8f),
                                    Offset(x, size.height - 2f),
                                    strokeWidth = if (major) 2f else 1.2f
                                )
                            }
                            // Where a new phase begins (new, first quarter, full, last quarter): its little moon
                            // icon above the tick.
                            for ((t, a) in phaseMarks) {
                                val x = mid + ((t - now).toFloat() / DAY - offset) * perDay
                                if (x < 0f || x > size.width) continue
                                val markFade = 1f - (abs(x - mid) / mid).coerceIn(0f, 1f) * 0.8f
                                drawMoonPhaseIcon(
                                    phaseIcons,
                                    Offset(x, -9f * density),
                                    7f * density,
                                    a,
                                    ink,
                                    markFade
                                )
                            }
                            // Today: a small dot under its tick (moves with the dial); out of view, an arrow
                            // at that edge points the way back.
                            val todayX = mid - offset * perDay
                            if (todayX !in 0f..size.width) {
                                val left = todayX < 0f
                                val ax = if (left) 6f * density else size.width - 6f * density
                                val ay = size.height + 6f * density
                                val dir = if (left) 1f else -1f
                                drawPath(
                                    Path().apply {
                                        moveTo(ax - dir * 4f * density, ay)
                                        lineTo(ax + dir * 3f * density, ay - 4f * density)
                                        lineTo(ax + dir * 3f * density, ay + 4f * density)
                                        close()
                                    },
                                    SolsticePalette.Sun
                                )
                            }
                            if (todayX in 0f..size.width) {
                                drawCircle(
                                    SolsticePalette.Sun,
                                    radius = 3.5f * density,
                                    center = Offset(
                                        todayX,
                                        size.height + 6f * density
                                    )
                                )
                            }
                            drawLine(
                                SolsticePalette.TideLine,
                                Offset(mid, 0f),
                                Offset(mid, size.height),
                                strokeWidth = 3f
                            )
                        }
                    }
                    Text(
                        if (abs(offset) <
                            0.5f
                        ) {
                            stringResource(R.string.shell_today)
                        } else {
                            SimpleDateFormat("EEE", Locale.getDefault()).apply {
                                timeZone =
                                    zone
                            }.format(at)
                        },
                        color = ink.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(Space.S4))
                    fun hm(d: Date?) =
                        d?.let { hhmm(zone).format(it) } ?: "–"
                    // The details in a sentence (design A), values bold and underlined like This week.
                    val days = ((full.time - at.time) / DAY.toDouble()).roundToInt().coerceAtLeast(0)
                    val km = "%,d km".format(distance.roundToInt())
                    val growing = angle < 180
                    // Tonight's visibility leads, only while the dial is on today.
                    val tonightSentence = tonight?.takeIf { abs(offset) < 0.5f }?.let { t ->
                        val (res, args) = SkyVisibility.sentence(t, MoonVisibility.texts) {
                            sentenceTime(context, it, zone)
                        }
                        stringResource(res) to args
                    }
                    val sentences = listOfNotNull(
                        tonightSentence,
                        stringResource(
                            if (growing) R.string.moon_lit_growing else R.string.moon_lit_shrinking
                        ) to listOf("${(lit * 100).roundToInt()} %"),
                        if (times.rise != null && times.set != null && times.set!!.before(times.rise)) {
                            stringResource(R.string.moon_sets_then_rises) to listOf(hm(times.set), hm(times.rise))
                        } else {
                            stringResource(R.string.moon_rises_then_sets) to listOf(hm(times.rise), hm(times.set))
                        },
                        if (days == 0) {
                            stringResource(R.string.moon_full_today) to listOf(km)
                        } else {
                            stringResource(R.string.moon_full_in) to
                                listOf(context.getString(R.string.moon_days, days), km)
                        }
                    )
                    val body = sentences(sentences)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(CornerShapes.L)
                            .background(ink.copy(alpha = 0.09f))
                            .border(StrokeWidth.Hairline, ink.copy(alpha = 0.10f), CornerShapes.L)
                            .padding(Space.S5)
                    ) {
                        Text(body, color = ink, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
