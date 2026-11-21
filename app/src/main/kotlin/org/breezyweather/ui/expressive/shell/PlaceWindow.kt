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

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import io.github.information89.motion.MotionPhysics
import io.github.information89.motion.MotionValue
import io.github.information89.motion.pageTransition
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.ui.expressive.components.LocalPageGlass
import org.breezyweather.ui.expressive.components.LocalSkyGlass
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.components.glassSource
import org.breezyweather.ui.expressive.components.rememberGlassSource
import org.breezyweather.ui.expressive.sky.SkyBackground
import org.breezyweather.ui.expressive.sky.SkyEffects
import org.breezyweather.ui.expressive.sky.SkySystemBars
import org.breezyweather.ui.expressive.sky.SkyWeather
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.SolsticePalette
import org.breezyweather.ui.expressive.theme.Space
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import kotlin.math.abs

/**
 * A place opened from its postcard (agreed 2026-11-18, "card becomes the sky"): the postcard grows
 * into a full-screen window with that place's Forecast and, a swipe
 * to the left, its This week. The paper frame and stamp fade away while the sky grows into the
 * window's sky, and the forecast fades in as it grows. Back plays it in reverse and follows the
 * finger (predictive back; cancel midway to stay). Swiping down (at the top of the page) also closes
 * it, like iOS's zoom transition: the window follows the finger, shrinking a little, and a release
 * past the dismiss point (or a flick) folds it back into its postcard, carrying the fling. While it
 * opens or closes, a touch catches it (agreed 2026-11-19). The window only shows the place: it
 * doesn't change the selected or main place.
 *
 * [presence] (0 = the postcard, 1 = full screen) and [pull] (0 = in place, 1 = at the dismiss
 * point) are read in draw only. [origin] is the postcard's bounds on screen.
 */
@Composable
internal fun PlaceWindow(
    location: Location,
    origin: Rect?,
    presence: MotionValue,
    pull: MotionValue,
    closing: Boolean,
    now: Long,
    onBackProgress: (Float) -> Unit,
    onBackCancelled: () -> Unit,
    // velocity: the finger's speed down (px/s) carried into the close, NaN for none.
    onClose: (velocity: Float) -> Unit,
    onReopen: () -> Unit,
    pinned: Boolean,
    fadeOut: Boolean,
    onPin: () -> Unit,
    onDetails: () -> Unit,
    weather: SkyWeather,
) {
    PredictiveBackHandler { events ->
        try {
            events.collect { onBackProgress(it.progress) }
            onClose(Float.NaN)
        } catch (e: CancellationException) {
            onBackCancelled()
            throw e
        }
    }
    fun progress() = presence.value
    val density = LocalDensity.current
    // How far down a pull reaches the dismiss point.
    val rangePx = with(density) { 260.dp.toPx() }
    val slowPx = with(density) { MotionPhysics.SlowFlick.toPx() }
    val fastPx = with(density) { MotionPhysics.FastFlick.toPx() }
    val screenPx = remember { FloatArray(1) { 1f } }
    val closingNow = rememberUpdatedState(closing)
    SideEffect {
        pull.slowFlick = slowPx / rangePx
        pull.fastFlick = fastPx / rangePx
    }

    // Swipe down to close: a pull follows the finger; let go past the dismiss point (or flick) and it
    // folds back into its postcard. While opening or closing, the finger drives the morph itself.
    fun unsettled() = presence.value < 0.999f || (presence.isRunning && presence.target < 0.5f)
    fun dragBy(dy: Float) {
        if (unsettled()) {
            presence.drag(-dy / screenPx[0])
        } else {
            pull.drag(dy / rangePx)
        }
    }
    fun releaseWith(vy: Float) {
        if (unsettled()) {
            presence.slowFlick = slowPx / screenPx[0]
            presence.fastFlick = fastPx / screenPx[0]
            val open = presence.release(-vy / screenPx[0]) > 0.5f
            if (open && closingNow.value) {
                onReopen()
            } else if (!open && !closingNow.value) {
                onClose(vy)
            }
        } else if (pull.targetFor(vy / rangePx) >= 1f) {
            pull.stop()
            onClose(vy)
        } else {
            pull.release(vy / rangePx)
        }
    }
    val pullScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Pulled: going back up undoes the pull before the page scrolls.
                if (source != NestedScrollSource.UserInput || available.y >= 0f || pull.value <= 0f) {
                    return Offset.Zero
                }
                dragBy(available.y)
                return Offset(0f, available.y)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // At the top of the page, pulling down pulls the window.
                if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero
                if (pull.isRunning) pull.stop()
                dragBy(available.y)
                return Offset(0f, available.y)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pull.value <= 0f && !unsettled()) return Velocity.Zero
                releaseWith(available.y)
                return available
            }
        }
    }
    val sky = skyFor(location, now)
    // Night there: the window's own interface is dark (the main screen keeps your setting).
    val night = sky.sunHeight < 0f
    BreezyWeatherTheme(darkTheme = night || isSystemInDarkTheme()) {
        val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val paper = if (dark) SolsticePalette.PaperDim else SolsticePalette.Paper
        val page = if (dark) Color.Black else Color.White
        val onPage = if (dark) Color.White else Color.Black
        // The postcard's paper border, which stretches out and thins away as the window grows.
        val bezel = postcardBezel()
        val pager = rememberPagerState(initialPage = 1) { 2 }
        val days = rememberLazyListState()
        // The same page changes and toolbar behaviour as home (ShellNavigation, 2026-11-21): tab taps
        // and swipes switch at once with a drawn slide, swipe blur, and This week's title hand-off.
        val nav = rememberShellNavigation(pager)
        val labelShown by rememberTitleHandOff(listOf(days, null))
        // The status and navigation bar icons follow the window once it's mostly open: the sky's
        // text colour on Forecast, the page's on This week (light icons on a dark night window).
        val covers by remember { derivedStateOf { progress() > 0.5f } }
        if (covers) {
            SkySystemBars(if (pager.currentPage == 1) sky.palette else plainPalette(page, onPage))
        }

        Box(
            Modifier
                .fillMaxSize()
                .onSizeChanged { screenPx[0] = it.height.toFloat().coerceAtLeast(1f) }
                // The pull: follows the finger down and shrinks a little, less as it folds into the
                // postcard (so it lands exactly on it).
                .graphicsLayer {
                    val p = progress().coerceIn(0f, 1f)
                    val f = pull.value
                    val k = 1f - 0.12f * f.coerceIn(0f, 1.5f) * p
                    scaleX = k
                    scaleY = k
                    // Follows the finger 1:1, like the moon card (round 3, 2026-11-19).
                    translationY = f * rangePx * p
                }
                .graphicsLayer {
                    val p = progress().coerceIn(0f, 1f)
                    val from = origin ?: Rect(0f, 0f, size.width, size.height)
                    val rect = Rect(
                        from.left * (1f - p),
                        from.top * (1f - p),
                        from.right + (size.width - from.right) * p,
                        from.bottom + (size.height - from.bottom) * p
                    )
                    // Clipped to the growing card; corners from the postcard's to square.
                    // Pulled, the window rounds its corners like a card.
                    val pulled = Corners.XL.toPx() * (pull.value * 4f).coerceIn(0f, 1f)
                    shape = RectShape(rect, Corners.L.toPx() * (1f - p) + pulled * p)
                    clip = true
                    // Fades in over the postcard during the first moments (their skies are laid out
                    // differently), and out again at the very end of a close.
                    // A visit you didn't keep fades away all the way down instead.
                    alpha = if (fadeOut) p else (p / 0.12f).coerceIn(0f, 1f)
                }
                .drawWithContent {
                    drawContent()
                    val p = progress().coerceIn(0f, 1f)
                    val from = origin ?: return@drawWithContent
                    val rect = Rect(
                        from.left * (1f - p),
                        from.top * (1f - p),
                        from.right + (size.width - from.right) * p,
                        from.bottom + (size.height - from.bottom) * p
                    )
                    // The paper frame stretches out with it and thins away...
                    val edge = bezel.toPx() * (1f - p)
                    if (edge > 0.3f) {
                        drawRoundRect(
                            paper,
                            topLeft = rect.topLeft + Offset(edge / 2, edge / 2),
                            size = Size(rect.width - edge, rect.height - edge),
                            cornerRadius = CornerRadius(Corners.L.toPx()),
                            style = Stroke(edge)
                        )
                    }
                    // ...and the stamp fades.
                    val stampAlpha = (1f - p / 0.35f).coerceIn(0f, 1f)
                    if (stampAlpha > 0f) {
                        val w = 46.dp.toPx()
                        val h = 52.dp.toPx()
                        drawRect(
                            paper.copy(alpha = stampAlpha),
                            topLeft = Offset(rect.right - w - bezel.toPx(), rect.top + bezel.toPx() + Space.S5.toPx()),
                            size = Size(w, h)
                        )
                    }
                }
        ) {
            // The window's own glass: its toolbar blurs the window's sky and pages, like the home toolbar.
            val windowGlass = rememberGlassSource()
            // The window's sky alone, for its Forecast cards' blur.
            val windowSky = rememberGlassSource()
            Box(Modifier.fillMaxSize().glassSource(windowGlass)) {
                // The postcard's sky is what grows: laid out full screen and scaled down onto the card.
                SkyBackground(
                    state = sky,
                    // Stars on a clear night (F4), baked into the window's sky.
                    effects = SkyEffects.of(sky, SkyWeather.of(location.weather?.current?.weatherCode)),
                    modifier = Modifier.fillMaxSize().glassSource(windowSky).graphicsLayer {
                        val p = progress().coerceIn(0f, 1f)
                        val from = origin ?: Rect(0f, 0f, size.width, size.height)
                        val left = from.left * (1f - p)
                        val top = from.top * (1f - p)
                        val right = from.right + (size.width - from.right) * p
                        val bottom = from.bottom + (size.height - from.bottom) * p
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = (right - left) / size.width
                        scaleY = (bottom - top) / size.height
                        translationX = left
                        translationY = top
                    }
                ) {}
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = ((progress() - 0.25f) / 0.5f).coerceIn(0f, 1f) }
                        // This week sits on the plain page, like Outlook.
                        .plainPageBackdrop({ nav.visualPosition() }, skyPage = 1, page = page)
                        .nestedScroll(pullScroll)
                ) {
                    HorizontalPager(
                        state = pager,
                        beyondViewportPageCount = 1,
                        flingBehavior = nav.fling,
                        modifier = Modifier.fillMaxSize().pageTransition(nav.pageChange)
                    ) { index ->
                        Box(Modifier.fillMaxSize().pageSwipeBlur({ nav.visualPosition() }, index)) {
                            if (index == 0) {
                                DaysPage(location, onPage, page, days) {
                                    abs(nav.visualPosition() - 1f).coerceIn(0f, 1f)
                                }
                            } else {
                                CompositionLocalProvider(
                                    LocalSkyGlass provides windowSky,
                                    LocalPageGlass provides windowGlass
                                ) {
                                    ForecastPage(
                                        location = location,
                                        sky = sky,
                                        now = now,
                                        refreshing = false,
                                        onRefresh = {},
                                        onDetails = onDetails,
                                        onOpenPlaces = { onClose(Float.NaN) },
                                        showLocalTime = false,
                                        pullToRefresh = false
                                    )
                                }
                            }
                        }
                    }
                    // "Visiting" until you pin it, and its local time when it differs from yours.
                    val context = LocalContext.current
                    // "[landing plane] Visiting · [clock] 2:33 PM, 14 hours ahead".
                    val localTime = timeDifference(location.timeZone, now)?.let { diff ->
                        time(context, java.util.Date(now), location.timeZone) + ", " + diff
                    }
                    val ink = sky.palette.text.copy(alpha = 0.85f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = Space.S2)
                    ) {
                        Icon(
                            SolsticeIcons.Small.FlightLand,
                            contentDescription = null,
                            tint = ink,
                            modifier = Modifier.size(Sizes.IconSmall)
                        )
                        Spacer(Modifier.width(Space.S1))
                        Text(
                            stringResource(R.string.shell_visiting),
                            style = MaterialTheme.typography.labelLarge,
                            color = ink
                        )
                        if (localTime != null) {
                            Text(" · ", style = MaterialTheme.typography.labelLarge, color = ink)
                            Icon(
                                SolsticeIcons.Small.Schedule,
                                contentDescription = null,
                                tint = ink,
                                modifier = Modifier.size(Sizes.IconSmall)
                            )
                            Spacer(Modifier.width(Space.S1))
                            Text(localTime, style = MaterialTheme.typography.labelLarge, color = ink)
                        }
                    }
                }
            }
            // The home toolbar, as is: This week and Forecast, and Pin budding out of Forecast like search
            // does out of Places (pin keeps the place; otherwise it's gone when you go back).
            val scope = rememberCoroutineScope()
            ToolbarDock(alpha = { ((progress() - 0.25f) / 0.5f).coerceIn(0f, 1f) }) {
                ShellToolbar(
                    pager = pager,
                    sky = sky,
                    weather = weather,
                    glass = windowGlass,
                    labelShown = labelShown,
                    onTab = { nav.navigate(it) },
                    onInterrupt = { nav.interrupt() },
                    position = { nav.toolbarPosition() },
                    passing = { nav.isPassing(it) },
                    searchEnabled = true,
                    onSearch = onPin,
                    onSearchBounds = {},
                    withPlaces = false,
                    // Outline when it's a visit; filled once pinned.
                    budIcon = if (pinned) SolsticeIcons.PushPinFill else SolsticeIcons.PushPin,
                    budDescription = stringResource(if (pinned) R.string.shell_pinned else R.string.shell_pin),
                    budChecked = pinned
                )
            }
            // While it opens, closes or springs back, a touch anywhere catches it (like search).
            var holding by remember { mutableStateOf(false) }
            val catchable by remember { derivedStateOf { unsettled() || pull.isRunning } }
            if (catchable || holding) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { dragBy(it) },
                            startDragImmediately = true,
                            onDragStarted = {
                                holding = true
                                if (unsettled()) presence.stop()
                                pull.stop()
                            },
                            onDragStopped = { v ->
                                holding = false
                                releaseWith(v)
                            }
                        )
                )
            }
        }
    }
}

/** A rounded rectangle at [rect] (screen space) with corner radius [radius] px. */
private class RectShape(private val rect: Rect, private val radius: Float) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density,
    ) = androidx.compose.ui.graphics.Outline.Rounded(
        androidx.compose.ui.geometry.RoundRect(rect, CornerRadius(radius, radius))
    )
}
