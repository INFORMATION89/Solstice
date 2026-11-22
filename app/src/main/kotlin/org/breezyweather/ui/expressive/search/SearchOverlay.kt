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

package org.breezyweather.ui.expressive.search

import android.content.Context
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import io.github.information89.motion.EdgeBand
import io.github.information89.motion.Motion
import io.github.information89.motion.edgeBlur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.breezyweather.R
import org.breezyweather.ui.expressive.components.GlassSource
import org.breezyweather.ui.expressive.components.SheetHandle
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.components.SolsticeSheetState
import org.breezyweather.ui.expressive.components.glass
import org.breezyweather.ui.expressive.shell.timeDifference
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.Space
import org.breezyweather.ui.search.LoadableLocationStatus
import org.breezyweather.ui.search.SearchViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Search as one big card (agreed 2026-11-18). It grows out of the round search bubble into a card
 * docked at the bottom, above the keyboard: a handle on top, results (segmented rows) in the
 * middle, the field at the bottom. The card has a fixed height; drag the handle up to show more,
 * or down to dismiss (it follows the finger; releasing early springs it back). Results settle in
 * place when they change. Its motion is a [SolsticeSheetState] ([sheet]): grab it at any point, even
 * mid-open or mid-close, and a flick down carries its speed into the close. It's read in layout and
 * draw only, so opening never recomposes the screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchOverlay(
    open: Boolean,
    sheet: SolsticeSheetState,
    origin: Rect?,
    searchViewModel: SearchViewModel,
    near: Pair<Double, Double>?,
    countryCode: String?,
    onPickLocation: (Location) -> Unit,
    onPickCity: (City) -> Unit,
    onClose: () -> Unit,
    // Caught while closing and thrown back up: the caller opens it again.
    onReopen: () -> Unit = {},
    // A pick hands the card over to the postcard window: it reports where it is and then vanishes.
    onCardBounds: (Rect) -> Unit = {},
    glass: GlassSource? = null,
    handedOff: Boolean = false,
) {
    // Only this check reads the progress in composition, and it flips just twice per open/close.
    val visible by remember { derivedStateOf { sheet.presence.value > 0f } }
    if (!visible && !open) return
    val context = LocalContext.current
    val density = LocalDensity.current
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    val results by searchViewModel.listResource.collectAsState()
    val openNow = rememberUpdatedState(open)
    val onCloseNow = rememberUpdatedState(onClose)
    val onReopenNow = rememberUpdatedState(onReopen)
    // The keyboard's height while open (the card's bottom), held while closing.
    val heldInset = remember { IntArray(1) }
    // The keyboard and a pull (agreed 2026-11-19, like iOS): pulled below compact, the keyboard starts
    // hiding while the card holds its height; let go and it springs back, the keyboard comes back.
    // 0 = none, 1 = hidden by the pull, 2 = coming back (held until it's all the way up).
    val pullKeyboard = remember { IntArray(1) }
    // The query last sent, so "no results" only shows once that search has answered.
    var requested by remember { mutableStateOf("") }

    BackHandler(enabled = open, onBack = onClose)
    // Hiding the keyboard (its dismiss key) while search is open expands the card to full height.
    val imeShown = WindowInsets.isImeVisible
    val imeNow = rememberUpdatedState(imeShown)
    val keyboardController = LocalSoftwareKeyboardController.current
    var hadIme by remember { mutableStateOf(false) }
    LaunchedEffect(imeShown) {
        if (open && hadIme && !imeShown && pullKeyboard[0] == 0) sheet.expand()
        hadIme = imeShown
    }
    LaunchedEffect(open) {
        if (open) {
            // Opens tall (agreed 2026-11-19): the caller's sheet.showTall().
            pullKeyboard[0] = 0
            delay(120)
            runCatching { focus.requestFocus() }
        } else {
            focusManager.clearFocus()
            query = ""
        }
    }
    LaunchedEffect(query) {
        if (query.trim().length < 2) return@LaunchedEffect
        delay(300)
        searchViewModel.requestLocationList(query)
        requested = query
    }
    val suggestions by produceState(emptyList<City>(), near, countryCode) {
        value = withContext(Dispatchers.IO) { Cities.topPlaces(context, countryCode, near) }
        // Build the typing index now, so the first letters already find cities.
        withContext(Dispatchers.Default) { Cities.search(context, "a") }
    }
    var recentsVersion by remember { mutableStateOf(0) }
    val recents = remember(open, recentsVersion) { RecentSearches.all(context) }

    // Never looks like it's loading (agreed 2026-11-18): matches from the bundled city list show
    // instantly as you type; when the online answer for this query arrives, rows settle into it
    // (same city keeps its row). While waiting with no local match, the last rows stay.
    val local by produceState(emptyList<City>(), query) {
        val q = query.trim()
        value = if (q.length < 2) emptyList() else withContext(Dispatchers.Default) { Cities.search(context, q) }
    }
    val answered = requested == query && results.second != LoadableLocationStatus.LOADING
    val online = if (answered) results.first else emptyList()
    var lastHits by remember { mutableStateOf(emptyList<Hit>()) }
    val hits = remember(online, local, answered) {
        val fresh = when {
            online.isNotEmpty() -> online.map { it.toHit(onPickLocation) }
            local.isNotEmpty() -> local.map { it.toHit(onPickCity) }
            else -> emptyList()
        }
        uniqueKeys(fresh)
    }
    val shownHits = if (hits.isNotEmpty() || answered) hits else lastHits
    SideEffect { if (hits.isNotEmpty()) lastHits = hits }

    val view = LocalView.current
    val ime = WindowInsets.ime
    val navigation = WindowInsets.navigationBars
    val status = WindowInsets.statusBars
    // Round as the bubble; docked, only the top corners stay round (the bottom sits on the keyboard).
    val cardShape = remember {
        GenericShape { size, _ ->
            val top = minOf(Corners.XL.value * density.density, size.minDimension / 2)
            val bottom = top * (1f - sheet.presence.value.coerceIn(0f, 1f))
            addRoundRect(
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

    // How far the inside is shown, from the open progress (read only in draw/layout lambdas).
    fun inside() = ((sheet.presence.value.coerceIn(0f, 1f) - 0.45f) / 0.55f).coerceIn(0f, 1f)

    // The card's height and pull, from the handle or the list (deltas: + is the finger going down).
    fun drag(delta: Float) {
        if (sheet.presence.isRunning || sheet.detent.isRunning) sheet.catch()
        val wasTall = sheet.extra >= sheet.room - 0.5f
        sheet.drag(delta)
        if (sheet.pulled > 0f && imeNow.value && pullKeyboard[0] == 0) {
            pullKeyboard[0] = 1
            keyboardController?.hide()
        }
        // With the keyboard hidden, swiping the card up until it hits the top brings the keyboard back.
        if (delta < 0f && !imeNow.value && !wasTall && sheet.extra >= sheet.room - 0.5f) {
            runCatching { focus.requestFocus() }
            keyboardController?.show()
        }
    }

    // Let go: the sheet picks where to rest from the fling (UIKit's rule); resting pulled down far
    // enough dismisses, carrying the fling's speed into the close.
    fun release(velocity: Float) {
        val close = sheet.release(velocity)
        if (!close && pullKeyboard[0] == 1) {
            pullKeyboard[0] = 2
            runCatching { focus.requestFocus() }
            keyboardController?.show()
        }
        if (close && openNow.value) {
            onCloseNow.value()
        } else if (!close && !openNow.value) {
            onReopenNow.value()
        }
    }

    // The list drives the card like a bottom sheet: scrolling the results up grows the card first;
    // with the list at its edge, pulling down shrinks it and then dismisses it. Flings settle it.
    val listDrag = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy >= 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                val space = sheet.pulled + (sheet.room - sheet.extra).coerceAtLeast(0f)
                val take = minOf(-dy, space)
                if (take <= 0f) return Offset.Zero
                drag(-take)
                return Offset(0f, -take)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy <= 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                drag(dy)
                return Offset(0f, dy)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val between = sheet.unsettled || sheet.pulled > 0f ||
                    (sheet.extra > 0.5f && sheet.extra < sheet.room - 0.5f)
                if (!between) return Velocity.Zero
                release(available.y)
                return available
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // The page behind goes darker (the shell also blurs it). Pulling down lightens it, so
        // dismissing feels continuous.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = sheet.backdrop()
                }
                .background(Color.Black.copy(alpha = 0.25f))
                // Only while open: closing, taps reach the page underneath at once (a disabled click
                // handler still sat over the whole screen; MOTION.md: animations never block input).
                .then(
                    if (open) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClose
                        )
                    } else {
                        Modifier
                    }
                )
        )

        // The card's size and place follow the spring, keyboard and handle in the layout phase only:
        // the open animation re-lays out the card each frame but recomposes nothing.
        Box(
            Modifier
                .layout { measurable, constraints ->
                    val width = constraints.maxWidth.toFloat()
                    val height = constraints.maxHeight.toFloat()
                    val shrunk = (view.rootView.height - constraints.maxHeight).coerceAtLeast(0)
                    val keyboard = ime.getBottom(this)
                    // On the keyboard; with the keyboard hidden, down to the screen's edge (behind the
                    // navigation bar; the field keeps clear of it with padding).
                    val liveInset = if (keyboard > 0) (keyboard - shrunk).coerceAtLeast(0) else 0
                    // Closing hides the keyboard: keep the card where it was (not falling with the
                    // keyboard), so it folds into the button from there and can still be caught.
                    if (pullKeyboard[0] == 2 && liveInset >= heldInset[0]) pullKeyboard[0] = 0
                    val holding = !openNow.value || pullKeyboard[0] != 0
                    if (!holding) heldInset[0] = liveInset
                    val bottomInset = if (holding) heldInset[0] else liveInset
                    val top = status.getTop(this) + Space.S3.toPx()
                    // Full width, sitting right on the keyboard (no gaps).
                    val margin = 0f
                    val chrome = (Sizes.HandleStrip + 64.dp).toPx() // handle + field
                    val compact = 280.dp.toPx()
                    val tall = (height - bottomInset - top - chrome).coerceAtLeast(compact)
                    sheet.measure(room = tall - compact, dismissDistance = 220.dp.toPx())
                    val content = (compact + sheet.extra).coerceIn(compact, tall)
                    val bottom = height - bottomInset + sheet.pulled
                    val target = Rect(margin, bottom - content - chrome, width - margin, bottom)
                    val start = origin ?: target
                    sheet.travel = start.top - (height - bottomInset - content - chrome)
                    val p = sheet.presence.value.coerceIn(0f, 1f)
                    val left = start.left + (target.left - start.left) * p
                    val topEdge = start.top + (target.top - start.top) * p
                    val w = (start.width + (target.width - start.width) * p).roundToInt().coerceAtLeast(0)
                    val h = (start.height + (target.height - start.height) * p).roundToInt().coerceAtLeast(0)
                    val placeable = measurable.measure(Constraints.fixed(w, h))
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(left.roundToInt(), topEdge.roundToInt())
                    }
                }
                .onGloballyPositioned { onCardBounds(it.boundsInRoot()) }
                .graphicsLayer { alpha = if (handedOff) 0f else 1f }
                // The toolbar's glass: the page behind, blurred, under a light tint, with a fine edge.
                .glass(glass, cardShape, MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
                .clip(cardShape)
        ) {
            // While still small it's the bubble's search icon.
            Icon(
                SolsticeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center).graphicsLayer { alpha = 1f - inside() }
            )
            Column(Modifier.fillMaxSize().graphicsLayer { alpha = inside() }.nestedScroll(listDrag)) {
                Spacer(Modifier.height(Sizes.HandleStrip)) // room for the handle, drawn on top
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    // Built once the card is a third of the way open (it's invisible before that), so the
                    // first frame of the animation only has the card and field to build.
                    val ready by remember { derivedStateOf { sheet.presence.value > 0.3f } }
                    if (ready || !open) {
                        Results(
                            context = context,
                            query = query,
                            hits = shownHits,
                            answered = answered,
                            // A place already under Recent isn't repeated under Popular places.
                            suggestions = suggestions.filterNot { c ->
                                recents.any { it.name == c.name && it.countryCode == c.countryCode }
                            },
                            recents = recents,
                            onPickCity = onPickCity,
                            onClearRecents = {
                                RecentSearches.clear(context)
                                recentsVersion++
                            }
                        )
                    }
                }
                SearchField(
                    query = query,
                    onQuery = { query = it },
                    focus = focus,
                    onSelect = { shownHits.firstOrNull()?.pick?.invoke() }
                )
                // Behind the navigation bar when the keyboard is hidden.
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars.exclude(WindowInsets.ime)))
            }
            // The handle's touch area is taller than it looks (it overlaps the top of the results).
            Handle(
                modifier = Modifier.align(Alignment.TopCenter).graphicsLayer { alpha = inside() },
                onClose = onClose,
                moving = { sheet.presence.isRunning || sheet.detent.isRunning },
                onStart = { sheet.catch() },
                onDelta = { drag(it) },
                onRelease = { release(it) }
            )
            // While it's opening, closing or settling, the whole card catches a touch at once (not just
            // the handle and list, whose content is faded out then), like an iOS sheet. Kept for the
            // whole drag even if the card becomes fully open under the finger.
            var holding by remember { mutableStateOf(false) }
            val catchable by remember {
                derivedStateOf { sheet.unsettled || sheet.presence.isRunning || sheet.detent.isRunning }
            }
            if (catchable || holding) {
                Box(
                    Modifier
                        .matchParentSize()
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { drag(it) },
                            startDragImmediately = true,
                            onDragStarted = {
                                holding = true
                                sheet.catch()
                            },
                            onDragStopped = { velocity ->
                                holding = false
                                release(velocity)
                            }
                        )
                )
            }
        }
    }
}

/** The grab bar: drag up for more, down to dismiss. */
@Composable
private fun Handle(
    modifier: Modifier,
    onClose: () -> Unit,
    moving: () -> Boolean,
    onStart: () -> Unit,
    onDelta: (Float) -> Unit,
    onRelease: (Float) -> Unit,
) {
    // The shared handle (looks like the handle strip, grabs a 48 dp touch target), across the width.
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxWidth()
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState(onDelta),
                // While it's moving, a touch catches it at once (no touch slop).
                startDragImmediately = moving(),
                onDragStarted = { onStart() },
                onDragStopped = { velocity -> onRelease(velocity) }
            )
    ) {
        SheetHandle(MaterialTheme.colorScheme.onSurfaceVariant, onClose)
    }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit, focus: FocusRequester, onSelect: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Space.S2, end = Space.S2, bottom = Space.S2)
            .height(56.dp)
            .clip(CornerShapes.L)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f))
            .padding(horizontal = Space.S4)
    ) {
        Icon(SolsticeIcons.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(Space.S3))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    stringResource(R.string.shell_search_elsewhere),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                // Results already follow the typing, so the key picks the highlighted match.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSelect() }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus)
            )
        }
    }
}

private enum class ResultsMode { NONE, FOUND, SUGGEST }

/** One row of results, from the online lookup or the bundled list; the same city keeps its key. */
private class Hit(
    val key: String,
    val title: String,
    val subtitle: String,
    val zone: TimeZone,
    val pick: () -> Unit,
)

private fun cityKey(name: String, countryCode: String?) = (name + "|" + countryCode.orEmpty()).lowercase()

private fun Location.toHit(onPick: (Location) -> Unit): Hit {
    val name = city.ifEmpty { customName ?: country }
    return Hit(
        key = cityKey(name, countryCode),
        title = name,
        subtitle = listOfNotNull(admin1?.takeIf { it.isNotEmpty() }, country).distinct().joinToString(", "),
        zone = timeZone,
        pick = { onPick(this) }
    )
}

private fun City.toHit(onPick: (City) -> Unit) = Hit(
    key = cityKey(name, countryCode),
    title = name,
    subtitle = country,
    zone = TimeZone.getTimeZone(timeZone),
    pick = { onPick(this) }
)

/** Same name in the same country (several Springfields) gets "#2", "#3"… so list keys stay unique. */
private fun uniqueKeys(hits: List<Hit>): List<Hit> {
    val seen = HashMap<String, Int>()
    return hits.map { h ->
        val n = (seen[h.key] ?: 0) + 1
        seen[h.key] = n
        if (n == 1) h else Hit(h.key + "#" + n, h.title, h.subtitle, h.zone, h.pick)
    }
}

/**
 * The list inside the card (agreed 2026-11-18). It fades softly into the field and under the
 * handle instead of being cut by a hard line. Rows rise in one after another when they first
 * appear, starting next to the field. When results change, rows that stay slide to their new
 * place, new ones rise in and gone ones fade out. Switching between loading, no results and a
 * list crossfades.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Results(
    context: Context,
    query: String,
    hits: List<Hit>,
    answered: Boolean,
    suggestions: List<City>,
    recents: List<City>,
    onPickCity: (City) -> Unit,
    onClearRecents: () -> Unit,
) {
    val now = System.currentTimeMillis()
    val searching = query.trim().length >= 2
    val mode = when {
        searching && hits.isEmpty() && answered -> ResultsMode.NONE
        searching -> ResultsMode.FOUND
        else -> ResultsMode.SUGGEST
    }
    val rowSpring = Motion.settle.spec(visibilityThreshold = IntOffset(1, 1))
    AnimatedContent(
        targetState = mode,
        transitionSpec = { fadeIn(Motion.content.spec()) togetherWith fadeOut(Motion.snappy.spec()) },
        label = "results",
        modifier = Modifier.fillMaxSize().edgeBlur(
            // :motion's edge blur (F2): the list fades away at both ends, over the glass.
            top = EdgeBand(14.dp, fadeOut = true),
            bottom = EdgeBand(18.dp, fadeOut = true)
        )
    ) { shown ->
        when (shown) {
            ResultsMode.NONE -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(Space.S6)
            ) {
                Icon(
                    SolsticeIcons.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Sizes.IconHero)
                )
                Spacer(Modifier.height(Space.S2))
                Text(
                    stringResource(R.string.search_no_results, query.trim()),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.search_no_results_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            ResultsMode.FOUND -> {
                // Rows composed within a moment of a new set of hits rise in (new cities); rows that
                // stay keep their place and slide; rows scrolled into view later just appear.
                val changedAt = remember(hits.map { it.key }) { SystemClock.uptimeMillis() }
                LazyColumn(
                    reverseLayout = true, // the best match sits right above the field
                    contentPadding = PaddingValues(horizontal = Space.S2, vertical = Space.S3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(hits, key = { _, it -> it.key }) { i, hit ->
                        ResultRow(
                            title = hit.title,
                            subtitle = hit.subtitle,
                            zone = hit.zone,
                            now = now,
                            context = context,
                            highlighted = i == 0,
                            first = i == hits.lastIndex,
                            last = i == 0,
                            modifier = Modifier
                                .animateItem(
                                    fadeInSpec = null,
                                    placementSpec = rowSpring,
                                    fadeOutSpec = Motion.snappy.spec()
                                )
                                .riseIn(order = i, shownAt = changedAt),
                            onClick = hit.pick
                        )
                    }
                }
            }
            ResultsMode.SUGGEST -> {
                val shownAt = remember { SystemClock.uptimeMillis() }
                // Top-down list: the rows nearest the field (about the fifth on screen) go first.
                val rows = (if (recents.isEmpty()) 0 else recents.size + 1) + suggestions.size + 1
                fun order(index: Int) = (minOf(rows, 6) - 1 - index).coerceAtLeast(0)
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Space.S2, vertical = Space.S3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    var at = 0
                    if (recents.isNotEmpty()) {
                        val o = order(at++)
                        item(key = "recent") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SectionTitle(
                                    stringResource(R.string.search_recent),
                                    SolsticeIcons.History,
                                    Modifier.riseIn(o, shownAt).weight(1f)
                                )
                                TextButton(onClick = onClearRecents, modifier = Modifier.riseIn(o, shownAt)) {
                                    Text(stringResource(R.string.search_clear_recents))
                                }
                            }
                        }
                        val base = at
                        itemsIndexed(recents, key = { _, c -> "r" + c.name + c.countryCode }) { i, city ->
                            CityRow(
                                city,
                                now,
                                context,
                                first = i == 0,
                                last = i == recents.lastIndex,
                                Modifier.riseIn(order(base + i), shownAt)
                            ) { onPickCity(city) }
                        }
                        at += recents.size
                    }
                    if (suggestions.isNotEmpty()) {
                        val o = order(at++)
                        item(key = "top") {
                            SectionTitle(
                                stringResource(R.string.search_top_places),
                                SolsticeIcons.Public,
                                Modifier.riseIn(o, shownAt)
                            )
                        }
                        val base = at
                        itemsIndexed(suggestions, key = { _, c -> "t" + c.name + c.countryCode }) { i, city ->
                            CityRow(
                                city,
                                now,
                                context,
                                first = i == 0,
                                last = i == suggestions.lastIndex,
                                Modifier.riseIn(order(base + i), shownAt)
                            ) { onPickCity(city) }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A row's entrance: it rises a few dp and fades in, [order] x 25 ms after the others (capped), on
 * a soft spring. Only for rows composed within a moment of [shownAt]; a row scrolled into view
 * later (or one that stays when results change) is simply there.
 */
@Composable
private fun Modifier.riseIn(order: Int, shownAt: Long): Modifier {
    val fresh = remember { SystemClock.uptimeMillis() - shownAt < 350 }
    val enter = remember { Animatable(if (fresh) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (enter.value < 1f) {
            delay(order.coerceIn(0, 6) * 25L)
            enter.animateTo(1f, Motion.content.spec())
        }
    }
    return graphicsLayer {
        alpha = enter.value
        translationY = (1f - enter.value) * 10.dp.toPx()
    }
}

@Composable
private fun CityRow(
    city: City,
    now: Long,
    context: Context,
    first: Boolean,
    last: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) =
    ResultRow(
        title = city.name,
        subtitle = city.country,
        zone = TimeZone.getTimeZone(city.timeZone),
        now = now,
        context = context,
        highlighted = false,
        first = first,
        last = last,
        modifier = modifier,
        onClick = onClick
    )

@Composable
private fun SectionTitle(text: String, icon: ImageVector, modifier: Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(start = Space.S3, top = Space.S3, bottom = Space.S2)
    ) {
        Icon(
            SolsticeIcons.forSize(icon, Sizes.IconSmall),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Sizes.IconSmall)
        )
        Spacer(Modifier.width(Space.S2))
        Text(text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * One segment of a group: big outer corners on the group's first/last row, small ones inside.
 * Name, region/country, local time and time difference; the best result is highlighted.
 */
@Composable
private fun ResultRow(
    title: String,
    subtitle: String,
    zone: TimeZone,
    now: Long,
    context: Context,
    highlighted: Boolean,
    first: Boolean,
    last: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val is24 = android.text.format.DateFormat.is24HourFormat(context)
    val time = SimpleDateFormat(if (is24) "HH:mm" else "h:mm a", Locale.getDefault())
        .apply { timeZone = zone }
        .format(Date(now))
    val diff = timeDifference(zone, now)
    val big = Corners.M
    val small = Corners.XXS
    val colors = MaterialTheme.colorScheme
    // The best-match highlight eases on and off instead of jumping between rows.
    val fill by animateColorAsState(
        if (highlighted) colors.secondaryContainer else colors.surfaceContainerHighest.copy(alpha = 0.7f),
        animationSpec = Motion.control.spec(),
        label = "row"
    )
    // Material 3 surface button in the segmented list's shape.
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(
            topStart = if (first) big else small,
            topEnd = if (first) big else small,
            bottomStart = if (last) big else small,
            bottomEnd = if (last) big else small
        ),
        color = fill,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Space.S4, vertical = Space.S3)
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // The keyboard's key picks this one.
                    if (highlighted) {
                        Text(
                            " · " + stringResource(R.string.search_select),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                            maxLines = 1
                        )
                    }
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(time, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                if (diff != null) {
                    Text(diff, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}
