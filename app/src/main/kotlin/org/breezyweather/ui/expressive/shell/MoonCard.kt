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
import androidx.compose.runtime.Stable
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
import io.github.information89.motion.BlurCrossfade
import io.github.information89.motion.Dial
import io.github.information89.motion.DialLook
import io.github.information89.motion.DialScale
import io.github.information89.motion.Haptic
import io.github.information89.motion.Morph
import io.github.information89.motion.MorphAnchor
import io.github.information89.motion.MorphHost
import io.github.information89.motion.MorphPage
import io.github.information89.motion.MorphSettings
import io.github.information89.motion.MorphStyle
import io.github.information89.motion.Motion
import io.github.information89.motion.MotionValue
import io.github.information89.motion.SpringMorph
import io.github.information89.motion.haptics
import io.github.information89.motion.morphAnchor
import io.github.information89.motion.morphPassThrough
import io.github.information89.motion.morphSource
import io.github.information89.motion.rememberDialState
import io.github.information89.motion.rememberMorphMotion
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.ui.expressive.components.GlassSource
import org.breezyweather.ui.expressive.components.SheetShape
import org.breezyweather.ui.expressive.components.SolsticeCard
import org.breezyweather.ui.expressive.components.SolsticeMotion
import org.breezyweather.ui.expressive.components.glass
import org.breezyweather.ui.expressive.sky.SkyEffects
import org.breezyweather.ui.expressive.sky.SkyVisibility
import org.breezyweather.ui.expressive.sky.SkyWeather
import org.breezyweather.ui.expressive.sky.Tonight
import org.breezyweather.ui.expressive.sky.skyEffects
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Emphasis
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

/**
 * The moon card's dial (F5, agreed 2026-11-21): ±14 days, a tick every 6 hours, a labelled major tick
 * per day, four days across.
 */
private val MoonDialScale = DialScale(min = -14f, max = 14f, step = 0.25f, majorEvery = 4, visibleSpan = 4f)

/** Morph keys for a place's moon button and its label. */
private fun pillKey(location: Location) = "moon-pill-${location.formattedId}"
private fun labelKey(location: Location) = "moon-label-${location.formattedId}"

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
internal fun MoonButton(location: Location, now: Long) {
    val context = LocalContext.current
    val moonCards = LocalMoonCards.current
    val morphPage = LocalMorphPage.current
    val (angle, lit) = moonAt(Date(now))
    val phase = breezyweather.domain.weather.model.MoonPhase(angle).getDescription(context) ?: ""
    val suffix = " · ${(lit * 100).roundToInt()} %"
    // The screen it's on (home, or a postcard window): the card blurs that.
    val pageGlass = org.breezyweather.ui.expressive.components.LocalPageGlass.current
    // The pill's own glass blurs the sky behind it, like the cards on the page: it sits inside the page
    // glass, which can't blur itself (2026-11-21: on the page glass the pill had no background at all).
    val skyGlass = org.breezyweather.ui.expressive.components.LocalSkyGlass.current
    // Get the moon's texture ready in the background, so opening the card doesn't wait for it.
    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { MoonRenderer.prewarm(context) }
    }
    // While its card is open, the morph draws the pill exactly as it is and grows it into the sheet
    // (the button becomes the card, 2026-11-21); morphSource hides the real pill only while it does.
    // One material at both ends (MOTION.md, 2026-11-21): the pill is the sheet's own glass, Solstice's one
    // glass, so opening grows it rather than swapping materials; its label in the theme's ink on glass.
    val pill = androidx.compose.foundation.shape.CircleShape
    val glassInk = MaterialTheme.colorScheme.onSurface
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .morphSource(pillKey(location), cornerRadius = 100.dp)
            .glass(skyGlass, pill)
            .clip(pill)
            .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                moonCards.glass = pageGlass
                moonCards.page = morphPage
                moonCards.buttonInk = glassInk
                moonCards.show(location, now, phase + suffix)
            }
            .padding(horizontal = Space.S3, vertical = Space.S2)
    ) {
        MoonPillContent(phase, suffix, glassInk, Modifier.morphAnchor(labelKey(location)))
    }
}

/** The pill's label: the phase and its "· 85 %". The morph draws the same, so the swap is invisible. */
@Composable
private fun MoonPillContent(phase: String, suffix: String, text: Color, labelModifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(phase, color = text, style = MaterialTheme.typography.labelLarge, modifier = labelModifier)
        Text(suffix, color = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * The moon card of one screen (agreed 2026-11-21: a moon card belongs to the screen it opened from, so
 * it closes and goes with that screen): home and each postcard window have their own, provided as
 * [LocalMoonCards] and drawn by that screen's [MoonCardOverlay].
 */
@Stable
internal class MoonCards {
    var request by androidx.compose.runtime.mutableStateOf<Pair<Location, Long>?>(null)

    /** The button's label. */
    var label: String = ""

    /** The button's text colour. */
    var buttonInk: Color = Color.White

    /** The glass of the screen the card was opened from. */
    var glass: GlassSource? = null

    /** The page the button was on: the card belongs to it (moves with it, clipped to it, while closing). */
    var page: MorphPage? = null

    /** 0 closed … 1 open. */
    var progress by mutableFloatStateOf(0f)

    fun show(location: Location, now: Long, text: String) {
        label = text
        request = location to now
    }
}

/** The moon cards of the screen this is on (each screen must provide its own). */
internal val LocalMoonCards = androidx.compose.runtime.staticCompositionLocalOf<MoonCards> {
    error("No MoonCards here: each screen (home, a postcard window) provides its own")
}

/**
 * The moon card as a sheet, like the search card (agreed 2026-11-19): the same glass, docked at the
 * bottom at full screen height, a handle on top. It's the button grown: one morph value (M3,
 * `:motion`) drives the shape, the glass, the heading flying from the button's label and the
 * content. Pull it down and the morph runs backwards under the finger; let go and UIKit's rules pick
 * open or closed (agreed 2026-11-19); pulling up past open rubber-bands like iOS.
 */
@Composable
internal fun MoonCardOverlay(cards: MoonCards, glass: GlassSource) {
    val request = cards.request ?: return
    // A fresh morph value per opening.
    androidx.compose.runtime.key(request) { MoonSheet(cards, request.first, request.second, glass) }
}

@Composable
private fun MoonSheet(cards: MoonCards, location: Location, now: Long, glass: GlassSource) {
    val density = LocalDensity.current
    // The new morph (SpringMorph, agreed in the lab 2026-11-21, applied to the moon card at the user's
    // request): five springs grow the pill into the sheet; a touch catches it mid-flight, a tap turns it
    // back, a drag takes it over and a flick throws it.
    val motion = rememberMorphMotion()
    LaunchedEffect(motion) { motion.open() }
    // Tell the shell how far it is, and finish once it's home.
    LaunchedEffect(motion) {
        var opened = false
        androidx.compose.runtime.snapshotFlow { Triple(motion.progress, motion.isOpen, motion.isVisible) }
            .collect { (p, open, visible) ->
                cards.progress = p.coerceIn(0f, 1f)
                if (open) opened = true
                if (opened && !open && !visible) {
                    cards.progress = 0f
                    cards.request = null
                }
            }
    }
    fun close() = motion.close()
    BackHandler(enabled = motion.isOpen) { close() }

    // Solstice's one glass and the theme's ink on it (agreed 2026-11-21); stars on top at night at the
    // place, from the sky's own star system (the moon card is the one card with them).
    val ink = MaterialTheme.colorScheme.onSurface
    val placeSky = remember(location, now) { skyFor(location, now) }
    val stars = SkyEffects.of(placeSky, SkyWeather.of(location.weather?.current?.weatherCode))
    val status = WindowInsets.statusBars
    // Where the sheet rests: full width, docked at the bottom, at full screen height below the status
    // bar (agreed 2026-11-19).
    val window = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize
    val radius = with(density) { Corners.XL.toPx() }
    val top = status.getTop(density) + with(density) { Space.S3.toPx() }
    val target = remember(window, top, radius) {
        MorphAnchor(Rect(0f, top, window.width.toFloat(), window.height.toFloat()), radius)
    }
    // Round on top like the button; the bottom corners become the screen's as it docks.
    val screenCorners = org.breezyweather.ui.expressive.components.rememberScreenCorners()
    val sheetShape = remember(screenCorners) { { r: Float, p: Float -> SheetShape(r, p, screenCorners.bottom) } }
    fun shown() = motion.progress.coerceIn(0f, 1f)

    // While open, a tap above the sheet closes it. Closing, nothing here takes touches: the page
    // underneath gets them at once, and only the sheet as drawn catches one (SpringMorph).
    Box(
        Modifier
            .fillMaxSize()
            // Beside the page: moving, a touch also reaches the page below (SpringMorph's rule).
            .morphPassThrough(motion)
            .then(
                if (motion.isOpen) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { tap -> if (tap.y < status.getTop(this) + Space.S3.toPx()) close() }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        // The page behind goes a little darker.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = shown() }
                .background(Color.Black.copy(alpha = 0.25f))
        )
        SpringMorph(
            from = pillKey(location),
            target = target,
            motion = motion,
            // One material the whole way (MOTION.md): the pill's own glass grows into the sheet's.
            material = { shape -> glass(cards.glass ?: glass, shape) },
            shape = sheetShape,
            // It belongs to its button's page: closing while you swipe, it stays inside that page.
            settings = MorphSettings(page = cards.page),
            // The pill's label blurs out as the sheet's content sharpens in (and back on the way home).
            sourceContent = {
                val label = cards.label
                val phase = label.substringBefore(" · ")
                MoonPillContent(phase, label.removePrefix(phase), cards.buttonInk)
            }
        ) {
            Box(Modifier.fillMaxSize().skyEffects(placeSky, stars)) {
                Column {
                    Spacer(Modifier.height(Space.S4)) // the same top margin as the close button
                    MoonCard(
                        location,
                        now,
                        ink,
                        dateModifier = Modifier.graphicsLayer {
                            // Last in, first out: only once the heading has landed.
                            alpha = MotionValue.window(motion.progress, 0.85f, 1f)
                        },
                        deferRest = true,
                        interactive = motion.isOpen,
                        restModifier = Modifier.graphicsLayer {
                            // Choreographed (agreed): the content fades and scales in from 92 % as the
                            // glass reaches ~70 % of its size, and leaves first when it closes.
                            val e = smooth(
                                MotionValue.window(
                                    motion.progress,
                                    SolsticeMotion.CONTENT_FROM,
                                    SolsticeMotion.CONTENT_TO
                                )
                            )
                            alpha = e
                            val k = SolsticeMotion.CONTENT_START_SCALE + (1f - SolsticeMotion.CONTENT_START_SCALE) * e
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                            scaleX = k
                            scaleY = k
                        }
                    )
                }
                // The framework's close button (agreed 2026-11-22: a one-size sheet has a close button, not a
                // handle, like the postcard window); pulling the card down and back still close it.
                FrameButton(
                    closeAction { close() },
                    glass = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Space.S4)
                        .graphicsLayer {
                            alpha = MotionValue.window(
                                motion.progress,
                                SolsticeMotion.CONTENT_FROM,
                                SolsticeMotion.CONTENT_TO
                            )
                        }
                )
            }
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
    // The dial (Motion's): where it is, in days from now (fractional while it moves).
    val dial = rememberDialState(MoonDialScale, initial = 0f)
    val offset = dial.value.value
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
    val moon = MoonRenderer.remember()
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
            // Changing phase while scrubbing, the heading crosses with a blur (Motion's BlurCrossfade).
            BlurCrossfade(heading, headingModifier) { text ->
                Text(
                    text,
                    color = ink,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            if (restReady) {
                Column(restModifier) {
                    // The date crosses with a blur as the dial passes into another day.
                    BlurCrossfade(
                        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).apply { timeZone = zone }.format(at),
                        dateModifier
                    ) { date ->
                        Text(
                            date,
                            color = ink.copy(alpha = Emphasis.Medium),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    // "Will I see it tonight?" as a line under the date (agreed 2026-11-22), only on today. Away
                    // from today it keeps its space and just fades, so nothing moves under a finger on the dial.
                    tonight?.let { t ->
                        val (res, args) = SkyVisibility.sentence(t, MoonVisibility.texts) {
                            sentenceTime(context, it, zone)
                        }
                        val line = sentences(listOf(stringResource(res) to args))
                        // Leaving today it blurs away and coming back it sharpens in, keeping its space.
                        BlurCrossfade(abs(offset) < 0.5f, Modifier.padding(top = Space.S2)) { onToday ->
                            Text(
                                line,
                                color = ink.copy(alpha = Emphasis.High),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.graphicsLayer { alpha = if (onToday) 1f else 0f }
                            )
                        }
                    }
                    // The moon (the sheet's stars, from the sky's star system, are behind it).
                    Canvas(Modifier.fillMaxWidth().height(200.dp)) {
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
                    // The dial (Motion's Dial, F5): days labelled, today as home (its arrow points back when
                    // it's out of view), the moon-phase icon above the days a principal phase begins; drag or
                    // fling through ±14 days, double-tap to come back to today. Haptics are the dial's own.
                    val todayLabel = stringResource(R.string.shell_today)
                    val dayFormat = remember(zone) {
                        SimpleDateFormat("EEE", Locale.getDefault()).apply { timeZone = zone }
                    }
                    val dateFormat = remember(zone) {
                        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).apply { timeZone = zone }
                    }
                    Dial(
                        state = dial,
                        look = DialLook(
                            ink = ink,
                            marker = SolsticePalette.TideLine,
                            labelStyle = MaterialTheme.typography.labelMedium,
                            home = SolsticePalette.Sun
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        label = { d -> if (d == 0f) todayLabel else dayFormat.format(Date(now + (d * DAY).toLong())) },
                        decorate = { d, at, emphasis ->
                            phaseMarks.firstOrNull { (t, _) -> abs((t - now).toFloat() / DAY - d) < 0.5f }
                                ?.let { (_, phaseAngle) ->
                                    drawMoonPhaseIcon(
                                        phaseIcons,
                                        at,
                                        Sizes.IconSmall.toPx() / 2f,
                                        phaseAngle,
                                        ink,
                                        emphasis
                                    )
                                }
                        },
                        describe = { d -> dateFormat.format(Date(now + (d * DAY).toLong())) },
                        onDoubleTap = { it.value.animateTo(0f) },
                        home = 0f,
                        // Room for the phase icons, one small icon tall.
                        decorationSize = Sizes.IconSmall
                    )
                    Spacer(Modifier.height(Space.S4))
                    fun hm(d: Date?) = d?.let { sentenceTime(context, it.time, zone) } ?: "–"
                    // The details in a sentence (design A), values bold and underlined like This week.
                    val days = ((full.time - at.time) / DAY.toDouble()).roundToInt().coerceAtLeast(0)
                    val km = "%,d km".format(distance.roundToInt())
                    val growing = angle < 180
                    // The details as tiles (design A, agreed 2026-11-22): how lit and growing or shrinking; rises
                    // and sets; the next full moon and the distance. Each is the one card (inside the sheet
                    // there's nothing to blur: just the glass tint).
                    val tiles = listOf(
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
                    CompositionLocalProvider(org.breezyweather.ui.expressive.components.LocalSkyGlass provides null) {
                        Column(verticalArrangement = Arrangement.spacedBy(Space.S3)) {
                            tiles.forEach { tile ->
                                SolsticeCard { cardInk ->
                                    Text(
                                        sentences(listOf(tile)),
                                        color = cardInk,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
