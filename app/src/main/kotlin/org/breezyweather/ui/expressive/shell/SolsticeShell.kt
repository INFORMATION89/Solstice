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
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import breezyweather.domain.location.model.Location
import io.github.information89.motion.MorphAnchor
import io.github.information89.motion.Motion
import io.github.information89.motion.MotionPhysics
import io.github.information89.motion.pageSettleFling
import io.github.information89.motion.pageTransition
import io.github.information89.motion.rememberMorphMotion
import io.github.information89.motion.rememberMotionValue
import io.github.information89.motion.rememberPageTransition
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.domain.location.model.applyDefaultPreset
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.sources.SourceManager
import org.breezyweather.ui.common.composables.SecondarySourcesPreference
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.components.glass
import org.breezyweather.ui.expressive.components.glassSource
import org.breezyweather.ui.expressive.components.rememberGlassSource
import org.breezyweather.ui.expressive.components.rememberSolsticeSheetState
import org.breezyweather.ui.expressive.search.RecentSearches
import org.breezyweather.ui.expressive.search.SearchOverlay
import org.breezyweather.ui.expressive.sky.AnimatedSkyBackground
import org.breezyweather.ui.expressive.sky.SkyEffects
import org.breezyweather.ui.expressive.sky.SkyPalette
import org.breezyweather.ui.expressive.sky.SkyState
import org.breezyweather.ui.expressive.sky.SkySystemBars
import org.breezyweather.ui.expressive.sky.SkyWeather
import org.breezyweather.ui.expressive.sky.skyAt
import org.breezyweather.ui.expressive.theme.Space
import org.breezyweather.ui.main.MainActivityViewModel
import org.breezyweather.ui.search.SearchViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

/** What the shell asks its host (MainActivity) to do. */
interface ShellActions {
    fun openSearch()
    fun openDetails()
    fun openSettings()
    fun openLocationSettings()

    /** Show [page] and [message] now, and again on the next resume if the activity is recreated. */
    fun afterPick(page: Int?, message: String?)
}

/** Lets the host move the shell between pages (e.g. show Places when there are no locations). */
class ShellController {
    companion object {
        const val FORECAST = PAGE_FORECAST
    }

    internal var request by mutableStateOf<Int?>(null)
    internal var visitsChanged by mutableStateOf(0)

    internal var message by mutableStateOf<String?>(null)
    internal var pending by mutableStateOf<ShellMessage?>(null)

    /** Show [message] in the shell's message pill. */
    fun say(message: ShellMessage) {
        pending = message
    }

    internal var searchOpen by mutableStateOf(false)

    /** Set by the host while upstream's details screen covers the shell (its back closes that). */
    var detailsOpen by mutableStateOf(false)

    /** A place picked from search: mark it as a visit; the host then shows its Forecast. */
    fun visit(context: Context, id: String) {
        Visits.mark(context, id)
        visitsChanged++
    }

    /** Called by the host when it's resumed: go to [page] and show [text] in the shell's message bar. */
    fun show(page: Int?, text: String?) {
        if (page != null) request = page
        if (text != null) message = text
    }
    fun showPlaces(visible: Boolean) {
        request = if (visible) PAGE_PLACES else PAGE_FORECAST
    }
}

internal const val PAGE_DAYS = 0
internal const val PAGE_FORECAST = 1
internal const val PAGE_PLACES = 2

/**
 * Solstice's main screen: three pages side by side, Outlook ← Forecast → Places, over the current
 * location's sky. Outlook and Places sit on a plain page colour (white in light mode, black in dark)
 * that fades in exactly with the swipe away from Forecast, so it follows the finger and reverses.
 */
@Composable
fun SolsticeShell(
    viewModel: MainActivityViewModel,
    actions: ShellActions,
    controller: ShellController,
    sourceManager: SourceManager,
    searchViewModel: SearchViewModel,
) {
    val context = LocalContext.current
    val current by viewModel.currentLocation.collectAsState()
    val locations by viewModel.validLocationList.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val location = current?.location
    val now by rememberMinuteClock()
    val sky = location?.let { skyFor(it, now) } ?: skyAt(SkyWeather.CLEAR, now, null, null, isDaylight = true)

    val pager = rememberPagerState(initialPage = if (locations.isEmpty()) PAGE_PLACES else PAGE_FORECAST) { 3 }
    val scope = rememberCoroutineScope()
    val messages = remember { ShellMessages() }
    // One star: the main place (agreed 2026-11-18). The app opens on it and "back" returns to it;
    // any other place you look at is a visit.
    var starred by remember { mutableStateOf(loadStarred(context).take(1).toSet()) }
    val main = starred.firstOrNull()?.takeIf { id -> locations.any { it.formattedId == id } }
    val visiting = remember(controller.visitsChanged, locations) { Visits.all(context) }
    // Visits left from the previous run go away unless they were starred; then open on the main place.
    val initCompleted by viewModel.initCompleted.collectAsState()
    LaunchedEffect(initCompleted) {
        if (!initCompleted) return@LaunchedEffect
        Visits.takeStale(context).filter { it !in starred }.forEach { id ->
            val index = viewModel.validLocationList.value.indexOfFirst { it.formattedId == id }
            if (index >= 0) viewModel.deleteLocation(index)
        }
        controller.visitsChanged++
    }
    // There's always one starred place (the current position, else the first), and the main screen
    // shows it.
    LaunchedEffect(locations, starred) {
        if (locations.isEmpty()) return@LaunchedEffect
        if (starred.none { id -> locations.any { it.formattedId == id } }) {
            val pick = (locations.firstOrNull { it.isCurrentPosition } ?: locations.first()).formattedId
            starred = setOf(pick)
            saveStarred(context, starred)
            Visits.unmark(context, pick)
            controller.visitsChanged++
        }
    }
    LaunchedEffect(main, location?.formattedId) {
        if (main != null && location?.formattedId != main) viewModel.setLocation(main)
    }
    LaunchedEffect(controller.message) {
        controller.message?.let {
            controller.message = null
            messages.show(ShellMessage(SolsticeIcons.Explore, it))
        }
    }
    LaunchedEffect(controller.pending) {
        controller.pending?.let {
            controller.pending = null
            messages.show(it)
        }
    }

    val page = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.Black else Color.White
    val onPage = if (page == Color.Black) Color.White else Color.Black
    // System bar icons follow whatever is behind them.

    val glass = rememberGlassSource()
    // The sky alone, for the Forecast cards' blur.
    val skyGlass = rememberGlassSource()
    // How the pages change (ShellNavigation, shared with the postcard window, 2026-11-21): tab taps
    // and swipes switch the pager at once and :motion draws the slide, so taps are never lost.
    val nav = rememberShellNavigation(pager)
    fun visualPosition() = nav.visualPosition()
    fun navigate(target: Int) = nav.navigate(target)
    LaunchedEffect(controller.request) {
        controller.request?.let {
            controller.request = null
            navigate(it)
        }
    }
    val searchBlur = rememberGraphicsLayer()
    val searchSharp = rememberGraphicsLayer()
    // Search grows out of the Places button (agreed motion); one spring drives open and close.
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    SideEffect { controller.searchOpen = searchOpen }
    var searchOrigin by remember { mutableStateOf<Rect?>(null) }
    // Where the search card is, and whether it has just handed itself over to a postcard window.
    var searchCard by remember { mutableStateOf<Rect?>(null) }
    var searchHandoff by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    // A postcard opened as a window (agreed 2026-11-18): one spring grows it from its card and back.
    var windowId by rememberSaveable { mutableStateOf<String?>(null) }
    var windowOrigin by remember { mutableStateOf<Rect?>(null) }
    // The window's motion: SpringMorph's five springs (the user, 2026-11-21: the new morph applies to
    // postcards too). A touch catches it while it moves; a pull, back or a flick folds it home.
    val windowMotion = rememberMorphMotion()
    // System bar icons follow whatever is behind them; a postcard window sets its own once it's
    // mostly open.
    val windowCovers by remember { derivedStateOf { windowId != null && windowMotion.progress > 0.5f } }
    if (!windowCovers) {
        SkySystemBars(if (pager.currentPage == PAGE_FORECAST) sky.palette else plainPalette(page, onPage))
    }
    // Grows once the place is in the list (a place just picked in search is added a moment later),
    // so the window never pops in half-way.
    val windowReady = windowId != null && locations.any { it.formattedId == windowId }
    LaunchedEffect(windowId, windowReady) {
        if (windowReady) windowMotion.open()
    }

    fun closeWindow() = windowMotion.close()

    // Once folded all the way back (not caught), the window goes.
    LaunchedEffect(windowMotion) {
        var opened = false
        snapshotFlow { windowMotion.isOpen to windowMotion.isVisible }.collect { (open, visible) ->
            if (open) opened = true
            if (opened && !open && !visible) {
                opened = false
                val id = windowId
                windowId = null
                // A visit you didn't pin (or star) is gone once you go back.
                if (id != null && id in Visits.all(context) && id !in starred) {
                    val index = viewModel.validLocationList.value.indexOfFirst { it.formattedId == id }
                    if (index >= 0) viewModel.deleteLocation(index)
                    Visits.unmark(context, id)
                    controller.visitsChanged++
                }
            }
        }
    }

    // Search's motion (a SolsticeSheet): opens on Motion.container, closes on dismiss; a drag can
    // catch it at any point and a flick down carries its speed into the close.
    val searchSheet = rememberSolsticeSheetState()
    LaunchedEffect(searchOpen) {
        if (searchOpen) {
            // From the button: tall. Caught while closing and thrown back up: from where it is.
            if (searchSheet.presence.value <= 0f && !searchSheet.presence.isRunning) {
                searchSheet.showTall()
            } else {
                searchSheet.reopen()
            }
        } else {
            searchSheet.dismiss()
        }
    }
    // A picked place becomes a visit straight away: no weather-sources panel (that's in the place's
    // settings as an advanced option).
    val pickReady by searchViewModel.dialogLocationSourcesOpenState.collectAsState()
    val picked by searchViewModel.selectedLocation.collectAsState()
    LaunchedEffect(pickReady, picked) {
        val place = picked ?: return@LaunchedEffect
        if (!pickReady) return@LaunchedEffect
        searchViewModel.closeDialogLocationSources()
        val existing = viewModel.validLocationList.value.firstOrNull {
            it.formattedId == place.formattedId ||
                (it.city == place.city && it.countryCode == place.countryCode && it.city.isNotEmpty())
        }
        val id = existing?.formattedId ?: place.formattedId
        if (existing == null) {
            // Added as a visit (gone when you go back, unless you pin it) and fetched in the
            // background; your selected and main place stay as they are.
            viewModel.addLocation(place, null, context, select = false)
            Visits.mark(context, id)
            controller.visitsChanged++
            viewModel.refreshPlace(context, place)
        }
        RecentSearches.add(context, place)
        searchOpen = false
        if (id in starred) {
            // The main place is the Forecast itself.
            controller.request = PAGE_FORECAST
        } else {
            // The search card itself turns into the postcard and opens into the window.
            keyboard?.hide()
            windowOrigin = searchCard ?: searchOrigin
            searchHandoff = true
            windowId = id
        }
    }
    fun pick(place: Location) = searchViewModel.setSelectedLocation(
        place,
        sourceManager.getLocationSearchSourceOrDefault(searchViewModel.locationSearchSource.value)
    )
    // Title hand-off: while a page's big title is on screen its toolbar tab shows only the icon; as
    // the title scrolls away the label unfolds in the toolbar (never the same word twice).
    val daysList = rememberLazyListState()
    // This week starts at the top again whenever you come back to it.
    LaunchedEffect(pager.settledPage) {
        if (pager.settledPage != PAGE_DAYS &&
            daysList.firstVisibleItemIndex + daysList.firstVisibleItemScrollOffset > 0
        ) {
            daysList.scrollToItem(0)
        }
    }
    val placesList = rememberLazyListState()
    val labelShown by rememberTitleHandOff(listOf(daysList, null, placesList))
    // The moon button and sheet share one transition (Compose's container transform).
    MoonSharedLayout {
        Box(Modifier.fillMaxSize()) {
            // Everything the toolbar's glass shows, blurred, behind it.
            AnimatedSkyBackground(
                sky,
                Modifier.fillMaxSize().glassSource(glass),
                skyGlass = skyGlass,
                pageGlass = glass,
                // Only while the Forecast page shows some of it and no postcard window covers it.
                visible = {
                    abs(visualPosition() - PAGE_FORECAST) < 0.999f &&
                        windowMotion.progress < 0.999f
                },
                // Stars on a clear night (F4).
                effects = SkyEffects.of(sky, SkyWeather.of(location?.weather?.current?.weatherCode))
            ) {
                Box(Modifier.fillMaxSize().plainPageBackdrop({ visualPosition() }, PAGE_FORECAST, page))
                HorizontalPager(
                    state = pager,
                    // All three pages stay built: going Places -> Forecast would otherwise build Outlook (ten
                    // sky cards) in the middle of the swipe.
                    beyondViewportPageCount = 2,
                    // Letting go of a swipe: the pager jumps to the page at once and the rest of the way is
                    // drawn (2026-11-21), so a tap during it lands (MOTION.md: never block input).
                    flingBehavior = nav.fling,
                    modifier = Modifier
                        .fillMaxSize()
                        .pageTransition(nav.pageChange)
                ) { index ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .pageSwipeBlur({ visualPosition() }, index)
                    ) {
                        when (index) {
                            PAGE_DAYS -> DaysPage(location, onPage, page, daysList) {
                                // The edge blur only comes in once the plain page is mostly there (it showed
                                // over the sky as soon as a swipe started).
                                edgeFade(visualPosition())
                            }
                            PAGE_FORECAST -> ForecastPage(
                                location = location,
                                sky = sky,
                                now = now,
                                refreshing = loading,
                                onRefresh = {
                                    viewModel.updateWithUpdatingChecking(
                                        triggeredByUser = true,
                                        checkPermissions = true
                                    )
                                },
                                onDetails = actions::openDetails,
                                onOpenPlaces = { navigate(PAGE_PLACES) },
                                source = location?.let { sourceManager.getWeatherSource(it.forecastSource)?.name }
                            )
                            else -> PlacesPage(
                                // The visit open in a window isn't listed until you pin it.
                                locations = locations.filterNot {
                                    it.formattedId == windowId && it.formattedId in visiting
                                },
                                currentId = location?.formattedId,
                                starred = starred,
                                visiting = visiting,
                                now = now,
                                onPage = onPage,
                                // Tapping a postcard opens it as a window (no jump, no change of place).
                                onSelect = { place, bounds ->
                                    windowOrigin = bounds
                                    windowId = place.formattedId
                                },
                                onStar = {
                                    val id = it.formattedId
                                    // One star: starring moves it here; the starred place stays starred.
                                    if (id in starred) return@PlacesPage
                                    starred = setOf(id)
                                    saveStarred(context, starred)
                                    messages.show(
                                        ShellMessage(
                                            SolsticeIcons.StarFill,
                                            context.getString(R.string.shell_now_main_place)
                                        )
                                    )
                                    // The main place is kept, never a visit.
                                    if (id in starred) Visits.unmark(context, id)
                                    controller.visitsChanged++
                                },
                                onDelete = { place ->
                                    val index = locations.indexOfFirst { it.formattedId == place.formattedId }
                                    if (index >= 0) {
                                        viewModel.deleteLocation(index)
                                        messages.show(
                                            ShellMessage(
                                                icon = SolsticeIcons.Delete,
                                                text = context.getString(
                                                    R.string.shell_place_removed,
                                                    place.getPlace(context)
                                                ),
                                                action = context.getString(R.string.shell_undo),
                                                onAction = { viewModel.addLocation(place, index, context) }
                                            )
                                        )
                                    }
                                },
                                onAddCurrent = {
                                    viewModel.openChooseWeatherSourcesDialog(
                                        Location(isCurrentPosition = true).applyDefaultPreset(sourceManager)
                                    )
                                },
                                onEdit = {
                                    viewModel.setLocation(it.formattedId)
                                    actions.openLocationSettings()
                                },
                                onMove = { id, beforeId ->
                                    // Same move in the saved list (where the current position may sit anywhere).
                                    val from = locations.indexOfFirst { it.formattedId == id }
                                    val rest = locations.filter { it.formattedId != id }
                                    val to = beforeId?.let { b -> rest.indexOfFirst { it.formattedId == b } }
                                        ?.takeIf { it >= 0 } ?: rest.size
                                    if (from >= 0) viewModel.swapLocations(from, to)
                                },
                                onSettings = actions::openSettings,
                                listState = placesList,
                                pageFade = { edgeFade(visualPosition()) }
                            )
                        }
                    }
                }
            }
            // The toolbar steps aside while search or a place window is open.
            ToolbarDock(alpha = {
                1f -
                    maxOf(searchSheet.presence.value.coerceIn(0f, 1f), windowMotion.progress.coerceIn(0f, 1f))
            }) {
                ShellMessageHost(messages, glass)
                ShellToolbar(
                    pager = pager,
                    sky = sky,
                    weather = SkyWeather.of(location?.weather?.current?.weatherCode),
                    glass = glass,
                    labelShown = labelShown,
                    onTab = { navigate(it) },
                    onInterrupt = { nav.interrupt() },
                    position = { nav.toolbarPosition() },
                    passing = { nav.isPassing(it) },
                    searchEnabled = !searchOpen,
                    onSearch = {
                        searchHandoff = false
                        searchOpen = true
                    },
                    onSearchBounds = { searchOrigin = it },
                    // The search card is the button while it's out: it carries the icon there and back,
                    // and the real one shows again in the frame the card has folded away (one swap).
                    budIconShown = {
                        if (searchSheet.presence.value > 0f || searchSheet.presence.isRunning) 0f else 1f
                    }
                )
            }
            ChooseSourcesDialog(viewModel, sourceManager)
            val nearPoint = (
                locations.firstOrNull {
                    it.isCurrentPosition
                } ?: location
                )?.let { it.latitude to it.longitude }
            SearchOverlay(
                open = searchOpen,
                sheet = searchSheet,
                origin = searchOrigin,
                searchViewModel = searchViewModel,
                near = nearPoint,
                countryCode = (locations.firstOrNull { it.isCurrentPosition } ?: location)?.countryCode,
                onPickLocation = { pick(it) },
                onPickCity = { pick(it.toLocation()) },
                onClose = { searchOpen = false },
                onReopen = { searchOpen = true },
                onCardBounds = { searchCard = it },
                glass = glass,
                handedOff = searchHandoff && windowReady
            )
            // Above search: a city picked there grows into its window right where the search card was.
            val windowPlace = locations.firstOrNull { it.formattedId == windowId }
            val cornerPx = with(LocalDensity.current) { org.breezyweather.ui.expressive.theme.Corners.L.toPx() }
            val screen = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize
            val screenW = screen.width.toFloat()
            val screenH = screen.height.toFloat()
            if (windowPlace != null) {
                val id = windowPlace.formattedId
                // Where it grows from: the postcard (or the search card), captured when it opened, since a
                // visit's postcard leaves the list while its window is open.
                val fromBounds = remember(windowOrigin) {
                    val r = windowOrigin
                    if (r != null) {
                        MorphAnchor(r, cornerPx)
                    } else {
                        MorphAnchor(Rect(Offset(screenW / 2f - 1f, screenH / 2f - 1f), Size(2f, 2f)), cornerPx)
                    }
                }
                PlaceWindow(
                    location = windowPlace,
                    motion = windowMotion,
                    from = postcardKey(id),
                    fromBounds = fromBounds,
                    now = now,
                    onClose = { closeWindow() },
                    pinned = id !in visiting,
                    fadeOut = id in visiting && id !in starred,
                    weather = SkyWeather.of(windowPlace.weather?.current?.weatherCode),
                    source = sourceManager.getWeatherSource(windowPlace.forecastSource)?.name,
                    onPin = {
                        if (id in visiting) Visits.unmark(context, id) else Visits.mark(context, id)
                        controller.visitsChanged++
                    },
                    onDetails = {
                        viewModel.setLocation(windowPlace.formattedId)
                        actions.openDetails()
                    }
                )
            }
            // The moon card, over everything (also over a postcard window), on the search card's glass.
            MoonCardOverlay(glass)
        }
    }
}

/** Upstream's weather-source chooser shown when adding a place (it used to live in its Locations page). */
@Composable
private fun ChooseSourcesDialog(viewModel: MainActivityViewModel, sourceManager: SourceManager) {
    val context = LocalContext.current
    val open by viewModel.dialogChooseWeatherSourcesOpen.collectAsState()
    val selected by viewModel.selectedLocation.collectAsState()
    if (!open) return
    val location = selected ?: return
    SecondarySourcesPreference(
        sourceManager = sourceManager,
        location = location,
        onClose = { newLocation: Location? ->
            viewModel.closeChooseWeatherSourcesDialog()
            if (newLocation != null) {
                if (viewModel.locationExists(location)) {
                    if (location.forecastSource != newLocation.forecastSource &&
                        viewModel.locationExists(newLocation)
                    ) {
                        SnackbarHelper.showSnackbar(context.getString(R.string.location_message_already_exists))
                    } else {
                        viewModel.updateLocation(newLocation, location)
                        SnackbarHelper.showSnackbar(context.getString(R.string.location_message_updated))
                    }
                } else if (viewModel.locationExists(newLocation)) {
                    SnackbarHelper.showSnackbar(context.getString(R.string.location_message_already_exists))
                } else {
                    viewModel.addLocation(newLocation, null, context)
                    SnackbarHelper.showSnackbar(context.getString(R.string.location_message_added))
                }
            }
        },
        locationExists = { viewModel.locationExists(it) }
    )
}

/** The sky for a location at [now], from its current weather and today's sunrise/sunset. */
internal fun skyFor(location: Location, now: Long): SkyState {
    val weather = location.weather
    val sun = weather?.today?.sun
    return skyAt(
        weather = SkyWeather.of(weather?.current?.weatherCode),
        now = now,
        sunrise = sun?.riseDate?.time,
        sunset = sun?.setDate?.time,
        isDaylight = location.isDaylight
    )
}

/** Current time, ticking once a minute (enough for the sun's position and the hour list). */
@Composable
internal fun rememberMinuteClock() = produceState(System.currentTimeMillis()) {
    while (true) {
        value = System.currentTimeMillis()
        delay(60_000L - value % 60_000L)
    }
}

/** A palette standing for a plain page colour, so system bar icons can follow it. */
internal fun plainPalette(page: Color, text: Color) = SkyPalette(
    base = page,
    blobs = listOf(page, page, page),
    text = text,
    grain = 0f,
    sun = page,
    sunStrength = 0f
)

// Starred places (first form of "pin"; see docs/PLAN.md, Places: visit vs pin).
private const val PREFS = "solstice_places"
private const val KEY_STARRED = "starred"

private fun loadStarred(context: Context): Set<String> =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_STARRED, emptySet()).orEmpty()

private fun saveStarred(context: Context, starred: Set<String>) =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putStringSet(KEY_STARRED, starred) }

/**
 * Blurs the page behind the search card. The blur runs on a quarter-size copy of the page (16x
 * fewer pixels, and a strong blur hides the lower resolution), drawn scaled up over the sharp
 * page and faded in with [progress]. The radius stays fixed, so the GPU's work doesn't grow as
 * the card opens.
 */
private fun Modifier.searchBlur(layer: GraphicsLayer, sharp: GraphicsLayer, progress: () -> Float): Modifier =
    drawWithContent {
        val p = progress().coerceIn(0f, 1f)
        if (p <= 0.01f || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            drawContent()
            return@drawWithContent
        }
        // The page's background is behind the pager, so the sharp page has to fade out as the blurred
        // copy fades in (otherwise the sharp text shows through the blur).
        sharp.record { this@drawWithContent.drawContent() }
        sharp.alpha = 1f - p
        drawLayer(sharp)
        val k = 4f
        val radius = 28.dp.toPx() / k
        if (layer.renderEffect == null) layer.renderEffect = BlurEffect(radius, radius, TileMode.Clamp)
        layer.record(IntSize((size.width / k).toInt().coerceAtLeast(1), (size.height / k).toInt().coerceAtLeast(1))) {
            scale(1f / k, 1f / k, pivot = Offset.Zero) { this@drawWithContent.drawContent() }
        }
        layer.alpha = p
        scale(k, k, pivot = Offset.Zero) { drawLayer(layer) }
    }

/** How strongly a plain page's edge blur shows: none until the page is ~40 % in, full when it's there. */
internal fun edgeFade(position: Float): Float =
    ((abs(position - PAGE_FORECAST) - 0.4f) / 0.6f).coerceIn(0f, 1f)
