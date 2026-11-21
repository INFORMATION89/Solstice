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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import io.github.information89.motion.MorphAnchor
import io.github.information89.motion.MorphMotion
import io.github.information89.motion.MotionPhysics
import io.github.information89.motion.MotionValue
import io.github.information89.motion.SpringMorph
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

/** How far below the sheet's top a pull closes the window (the handle and the "Visiting" line). */
private val CLOSE_STRIP = 64.dp

/** The morph key of a place's postcard (its window grows out of it). */
internal fun postcardKey(id: String) = "postcard-$id"

/**
 * A place opened from its postcard (agreed 2026-11-18, "card becomes the sky"): the postcard grows
 * into a full-screen window with that place's Forecast and, a swipe to the left, its This week.
 *
 * On `SpringMorph` (the user, 2026-11-21: the new morph applies to postcards too): five springs grow it
 * from the postcard; the postcard blurs out as the window sharpens in, and the window is carried at
 * its full layout, growing uniformly. A touch catches it while it moves (a tap turns it back, a drag
 * takes it over). Open, a pull down from its top strip (status bar and "Visiting" line) drags it back
 * towards its postcard and a release past halfway (or a flick) folds it home; a pull on the page
 * refreshes, like home. Back follows the finger (predictive back). The window only shows the place:
 * it doesn't change the selected or main place.
 *
 * [fromBounds] is where it grows from (the postcard, or the search card it was picked in); [from] the
 * postcard's key, hidden while the window draws it. A visit you didn't keep ([fadeOut]) fades away as
 * it folds back.
 */
@Composable
internal fun PlaceWindow(
    location: Location,
    motion: MorphMotion,
    from: Any,
    fromBounds: MorphAnchor,
    now: Long,
    onClose: () -> Unit,
    pinned: Boolean,
    fadeOut: Boolean,
    onPin: () -> Unit,
    onDetails: () -> Unit,
    weather: SkyWeather,
    // The forecast source's name, for the "last updated" line.
    source: String? = null,
    // Pull to refresh on its Forecast, like home (user 2026-11-21).
    refreshing: Boolean = false,
    onRefresh: () -> Unit = {},
) {
    PredictiveBackHandler(enabled = motion.isOpen) { events ->
        try {
            events.collect { motion.scrub(1f - 0.5f * it.progress) }
            onClose()
        } catch (e: CancellationException) {
            motion.open()
            throw e
        }
    }
    fun progress() = motion.progress
    val window = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize
    // A sheet like the moon card's (agreed 2026-11-21): docked at the bottom, its top just below the
    // status bar, rounded on top with a handle; the bottom corners square off as it docks.
    val density = LocalDensity.current
    val statusTop = WindowInsets.statusBars.getTop(density)
    val sheetTop = statusTop + with(density) { Space.S3.toPx() }
    val radius = with(density) { Corners.XL.toPx() }
    val target = remember(window, sheetTop, radius) {
        MorphAnchor(Rect(0f, sheetTop, window.width.toFloat(), window.height.toFloat()), radius)
    }
    val sheetShape = remember { { r: Float, p: Float -> MoonSheetShape(r, p) } }
    val sky = skyFor(location, now)
    // Night there: the window's own interface is dark (the main screen keeps your setting).
    val night = sky.sunHeight < 0f
    BreezyWeatherTheme(darkTheme = night || isSystemInDarkTheme()) {
        val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val page = if (dark) Color.Black else Color.White
        val onPage = if (dark) Color.White else Color.Black
        val pager = rememberPagerState(initialPage = 1) { 2 }
        val days = rememberLazyListState()
        val forecastScroll = rememberScrollState()
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
        val context = LocalContext.current
        val closeStrip = sheetTop + with(density) { CLOSE_STRIP.toPx() }
        // While open, the page behind dims a little, and a tap above the sheet closes it.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress().coerceIn(0f, 1f) }
                .background(Color.Black.copy(alpha = 0.25f))
        )
        if (motion.isOpen) {
            Box(
                Modifier.fillMaxWidth().height(with(density) { sheetTop.toDp() }).pointerInput(Unit) {
                    detectTapGestures { onClose() }
                }
            )
        }
        SpringMorph(
            from = from,
            target = target,
            motion = motion,
            fromBounds = fromBounds,
            // A visit that isn't kept fades away as it folds back (it has no postcard to land on).
            modifier = Modifier.graphicsLayer {
                alpha = if (fadeOut && !motion.isOpen) progress().coerceIn(0f, 1f) else 1f
            },
            shape = sheetShape,
            // A pull on the page refreshes, like home (user 2026-11-21); a pull from the sheet's top (its
            // handle and the "Visiting" line) folds it back into its postcard.
            canPull = { start -> start.y < closeStrip },
            // A tap never closes a window (user 2026-11-21: a tap just after opening a postcard dismissed
            // it): caught while opening, it carries on; caught while closing, it opens again.
            onTap = { motion.open() },
            // The postcard itself blurs out as the window sharpens in (and back on the way home).
            sourceContent = {
                PlaceCard(
                    location = location,
                    selected = false,
                    starred = false,
                    visiting = !pinned,
                    folded = false,
                    now = now,
                    context = context,
                    onClick = {}
                )
            }
        ) {
            Box(Modifier.fillMaxSize()) {
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
                        modifier = Modifier.fillMaxSize().glassSource(windowSky)
                    ) {}
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = ((progress() - 0.25f) / 0.5f).coerceIn(0f, 1f) }
                            // This week sits on the plain page, like Outlook.
                            .plainPageBackdrop({ nav.visualPosition() }, skyPage = 1, page = page)
                    ) {
                        CompositionLocalProvider(LocalVisit provides Visit(location, now, pinned)) {
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
                                                refreshing = refreshing,
                                                onRefresh = onRefresh,
                                                onDetails = onDetails,
                                                onOpenPlaces = { onClose() },
                                                showLocalTime = false,
                                                source = source,
                                                scroll = forecastScroll
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // The handle, like the moon card's.
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .graphicsLayer { alpha = ((progress() - 0.25f) / 0.5f).coerceIn(0f, 1f) }
                        .padding(top = Space.S3)
                        .width(Sizes.HandleWidth)
                        .height(Sizes.HandleThickness)
                        .background(sky.palette.text.copy(alpha = 0.4f), CircleShape)
                )
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
            }
        }
    }
}
