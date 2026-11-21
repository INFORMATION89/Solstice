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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import breezyweather.domain.location.model.Location
import io.github.information89.motion.Haptic
import io.github.information89.motion.Motion
import io.github.information89.motion.haptics
import io.github.information89.motion.morphSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Elevation
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.Space
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin

// ---------------------------------------------------------------------------------------------
// Places

@Composable
internal fun PlacesPage(
    locations: List<Location>,
    currentId: String?,
    starred: Set<String>,
    visiting: Set<String>,
    now: Long,
    onPage: Color,
    onSelect: (Location, Rect?) -> Unit,
    onStar: (Location) -> Unit,
    onDelete: (Location) -> Unit,
    onAddCurrent: () -> Unit,
    onEdit: (Location) -> Unit,
    onMove: (id: String, beforeId: String?) -> Unit,
    onSettings: () -> Unit,
    listState: LazyListState,
    pageFade: () -> Float = { 1f },
) {
    val context = LocalContext.current
    val haptics = haptics()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val byId = locations.associateBy { it.formattedId }
    // The starred (main) place always first, then the current position; both stay where they are.
    val sortedIds = locations
        .sortedWith(
            // The current location always first, then the starred (main) place.
            compareByDescending<Location> { it.isCurrentPosition }.thenByDescending { it.formattedId in starred }
        )
        .map { it.formattedId }
    fun isPinned(id: String) = id in starred || byId[id]?.isCurrentPosition == true

    // Where each postcard is on screen, so a tapped one can grow into its window from there.
    val cardBounds = remember { HashMap<String, Rect>() }

    // Reorder state: while a card is carried we show our own copy of the order and save on drop.
    val order = remember { mutableStateListOf<String>() }
    var carried by remember { mutableStateOf<String?>(null) }
    var carryY by remember { mutableFloatStateOf(0f) }
    var tilt by remember { mutableFloatStateOf(0f) }
    val lift = remember { Animatable(1f) }
    var landed by remember { mutableStateOf<Pair<String, Long>?>(null) }
    val shown = if (carried == null) sortedIds else order.toList()
    val nudgeMax = with(density) { 7.dp.toPx() }
    val bumpMax = with(density) { 5.dp.toPx() }

    fun swapIfNeeded() {
        val info = listState.layoutInfo.visibleItemsInfo
        val cur = info.firstOrNull { it.key == carried } ?: return
        val centre = cur.offset + carryY + cur.size / 2f
        val target = info.firstOrNull {
            it.key != carried && it.key in order && (it.key as? String)?.let { k -> !isPinned(k) } == true &&
                centre > it.offset && centre < it.offset + it.size
        } ?: return
        val from = order.indexOf(carried)
        val to = order.indexOf(target.key)
        order.add(to, order.removeAt(from))
        carryY += cur.offset - target.offset
        haptics.perform(Haptic.detent)
    }

    // Neighbours lean out of the carried card's way as it approaches: the push rises and falls back
    // to zero by the time the cards swap, so there is never a jump. Read in the draw phase only.
    fun nudge(id: String): Float {
        val c = carried ?: return 0f
        if (c == id) return 0f
        val info = listState.layoutInfo.visibleItemsInfo
        val cur = info.firstOrNull { it.key == c } ?: return 0f
        val me = info.firstOrNull { it.key == id } ?: return 0f
        val dist = (me.offset + me.size / 2f) - (cur.offset + carryY + cur.size / 2f)
        val near = me.size / 2f
        val reach = me.size * 1.3f
        val d = abs(dist)
        if (d <= near || d >= reach) return 0f
        return sign(dist) * nudgeMax * sin(PI * (reach - d) / (reach - near)).toFloat()
    }

    // Edge to edge like This week, on the same plain page, title height and edge blur.
    PageScaffold(
        title = stringResource(R.string.shell_tab_places),
        onPage = onPage,
        page = if (onPage.luminance() < 0.5f) Color.White else Color.Black,
        listState = listState,
        pageFade = pageFade,
        spacing = Space.S4,
        action = {
            IconButton(onClick = onSettings) {
                Icon(
                    SolsticeIcons.Settings,
                    contentDescription = stringResource(R.string.action_settings),
                    tint = onPage.copy(alpha = 0.7f)
                )
            }
        }
    ) {
        items(shown, key = { it }) { id ->
            val location = byId[id] ?: return@items
            val isCarried = carried == id
            val pinned = isPinned(id)
            // Neighbours' landing bump when a carried card is dropped near them.
            val bump = remember { Animatable(0f) }
            LaunchedEffect(landed) {
                val l = landed ?: return@LaunchedEffect
                if (l.first == id) return@LaunchedEffect
                val diff = shown.indexOf(id) - shown.indexOf(l.first)
                if (abs(diff) in 1..2) {
                    bump.animateTo(sign(diff.toFloat()) * bumpMax / abs(diff), Motion.snappy.spec())
                    bump.animateTo(0f, Motion.playful.spec())
                }
            }
            // Starred just now: it lifts, glides to the top over the others, and sets down.
            val starredNow = id in starred
            var wasStarred by remember { mutableStateOf(starredNow) }
            val glide = remember { Animatable(0f) }
            LaunchedEffect(starredNow) {
                if (starredNow && !wasStarred) {
                    glide.animateTo(1f, Motion.container.spec())
                    delay(380)
                    glide.animateTo(0f, Motion.dismiss.spec())
                }
                wasStarred = starredNow
            }
            val lifted = glide.value > 0.01f
            Box(
                modifier = Modifier
                    .zIndex(if (isCarried || lifted) 1f else 0f)
                    .then(
                        if (isCarried) {
                            Modifier
                        } else {
                            Modifier.animateItem(
                                placementSpec = Motion.settle.spec(visibilityThreshold = IntOffset(1, 1))
                            )
                        }
                    )
                    .graphicsLayer {
                        if (!isCarried && glide.value > 0f) {
                            val k = 1f + 0.035f * glide.value
                            scaleX = k
                            scaleY = k
                            shadowElevation = glide.value * Elevation.Lifted.toPx()
                            ambientShadowColor = Color.Black.copy(alpha = 0.10f)
                            spotShadowColor = Color.Black.copy(alpha = 0.16f)
                            shape = CornerShapes.XXS
                        }
                        if (isCarried) {
                            translationY = carryY
                            scaleX = lift.value
                            scaleY = lift.value
                            rotationZ = tilt
                            shadowElevation = ((lift.value - 1f) / 0.03f).coerceIn(0f, 1f) * Elevation.Lifted.toPx()
                            ambientShadowColor = Color.Black.copy(alpha = 0.10f)
                            spotShadowColor = Color.Black.copy(alpha = 0.16f)
                            shape = CornerShapes.XXS
                        } else {
                            translationY = nudge(id) + bump.value
                        }
                    }
            ) {
                Column {
                    SwipeablePlace(
                        starred = id in starred,
                        enabled = carried == null,
                        onStar = { onStar(location) },
                        onDelete = { onDelete(location) }
                    ) {
                        PlaceCard(
                            location = location,
                            selected = id == currentId,
                            starred = id in starred,
                            visiting = id in visiting,
                            // The current location folds into a one-line strip when another place is
                            // the main place.
                            // The starred place folds into a strip under the current location (it's the
                            // main screen anyway); with no current location it stays a full card.
                            // The current location is always a strip on top; the starred place folds into a
                            // strip under it (with no current location it stays a full card).
                            folded = location.isCurrentPosition ||
                                (id in starred && locations.any { it.isCurrentPosition }),
                            now = now,
                            context = context,
                            // The starred place is the main screen itself: it doesn't open as a postcard.
                            onClick = { if (id !in starred) onSelect(location, cardBounds[id]) },
                            // Long-press lifts the card; drag to move it, or release without moving to edit.
                            modifier = Modifier.onGloballyPositioned {
                                cardBounds[id] = it.boundsInRoot()
                            }.morphSource(postcardKey(id), cornerRadius = Corners.L).pointerInput(id) {
                                var moved = 0f
                                // The finger's vertical speed at the drop, carried into the settle.
                                val tracker = VelocityTracker()
                                var travelled = 0f
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        moved = 0f
                                        tracker.resetTracking()
                                        travelled = 0f
                                        haptics.perform(Haptic.press)
                                        order.clear()
                                        order.addAll(sortedIds)
                                        carryY = 0f
                                        tilt = 0f
                                        carried = id
                                        scope.launch {
                                            lift.animateTo(1.03f, Motion.snappy.spec())
                                        }
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        moved += abs(amount.y) + abs(amount.x)
                                        travelled += amount.y
                                        tracker.addPosition(change.uptimeMillis, Offset(0f, travelled))
                                        if (!pinned) {
                                            carryY += amount.y
                                            val target = (amount.y * 0.35f).coerceIn(-3f, 3f)
                                            tilt += (target - tilt) * 0.3f
                                            swapIfNeeded()
                                        }
                                    },
                                    onDragEnd = {
                                        val wasMoved = moved > viewConfiguration.touchSlop && !pinned
                                        val index = order.indexOf(id)
                                        val before = order.getOrNull(index + 1)
                                        val vy = if (pinned) 0f else tracker.calculateVelocity().y
                                        scope.launch {
                                            // Settles into the slot without bounce, carrying the drop's speed.
                                            val soft = Motion.settle.spec<Float>()
                                            launch { lift.animateTo(1f, soft) }
                                            launch { animate(tilt, 0f, animationSpec = soft) { v, _ -> tilt = v } }
                                            if (wasMoved) landed = id to System.nanoTime()
                                            animate(carryY, 0f, initialVelocity = vy, animationSpec = soft) { v, _ ->
                                                carryY = v
                                            }
                                            carried = null
                                            // (Release without moving used to open the place's settings;
                                            // removed, it clashed with reordering. To revisit.)
                                            if (wasMoved) onMove(id, before)
                                        }
                                    },
                                    onDragCancel = {
                                        scope.launch {
                                            lift.animateTo(1f, Motion.settle.spec())
                                            carryY = 0f
                                            tilt = 0f
                                            carried = null
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
        if (locations.none { it.isCurrentPosition }) {
            item(key = "current") {
                OutlinedButton(onClick = onAddCurrent, modifier = Modifier.fillMaxWidth().padding(top = Space.S2)) {
                    Icon(SolsticeIcons.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(Space.S2))
                    Text(stringResource(R.string.shell_use_current_location))
                }
            }
        }
    }
}

/**
 * Swipe a place from right to left: past the first stage (about a quarter of the width) it stars
 * the place, past the second (60 %) it deletes it. The card follows the finger, a haptic tick marks
 * each stage, and on release it springs back (or slides out to delete). A new touch interrupts the
 * spring. TalkBack gets "star" and "delete" actions instead. Off while a card is being carried.
 */
@Composable
private fun SwipeablePlace(
    starred: Boolean,
    enabled: Boolean,
    onStar: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptics = haptics()
    // The drag moves the card directly (no queued coroutines); only the release animates, and a
    // new touch cancels that animation, so the card can never get stuck mid-way.
    var x by remember { mutableFloatStateOf(0f) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val starLabel = stringResource(if (starred) R.string.shell_unstar else R.string.shell_star)
    val deleteLabel = stringResource(R.string.shell_delete)
    // Springs back with a touch of give (the closest token to the old 0.8 damping).
    val settle = Motion.container.spec<Float>()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(starLabel) {
                        onStar()
                        true
                    },
                    CustomAccessibilityAction(deleteLabel) {
                        onDelete()
                        true
                    }
                )
            }
    ) {
        val width = constraints.maxWidth.toFloat()
        val starAt = width * 0.22f
        val deleteAt = width * 0.6f
        fun stageOf(pulled: Float) = when {
            pulled >= deleteAt -> 2
            pulled >= starAt -> 1
            else -> 0
        }
        val pulled = -x
        val stage = stageOf(pulled)
        LaunchedEffect(stage) {
            if (stage > 0) haptics.perform(Haptic.threshold)
        }
        // What the swipe reveals behind the card.
        val behind = if (stage ==
            2
        ) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        }
        val onBehind = if (stage == 2) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onTertiaryContainer
        }
        val icon = when {
            stage == 2 -> SolsticeIcons.Delete
            starred -> SolsticeIcons.StarFill
            else -> SolsticeIcons.Star
        }
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = (pulled / starAt).coerceIn(0f, 1f) }
                .clip(CornerShapes.L)
                .background(behind)
                .padding(end = Space.S7)
        ) {
            Icon(icon, contentDescription = null, tint = onBehind, modifier = Modifier.size(Sizes.IconXLarge))
        }
        Box(
            Modifier
                .offset { IntOffset(x.roundToInt(), 0) }
                // Only a right-to-left swipe moves the card; a left-to-right one is left to the pager
                // (it just goes to the page on the left).
                .pointerInput(enabled, width) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var leftward = false
                        val slop = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                            if (over < 0f || x < 0f) {
                                leftward = true
                                change.consume()
                            }
                        }
                        if (slop == null || !leftward) return@awaitEachGesture
                        settleJob?.cancel()
                        val tracker = VelocityTracker()
                        tracker.addPosition(slop.uptimeMillis, slop.position)
                        horizontalDrag(slop.id) { change ->
                            val delta = change.positionChange().x
                            x = (x + delta).coerceIn(-width, 0f)
                            tracker.addPosition(change.uptimeMillis, change.position)
                            change.consume()
                        }
                        val velocity = tracker.calculateVelocity().x
                        // Decide from where the card is right now, not from the last recomposition.
                        val released = stageOf(-x)
                        val out = Motion.dismiss.spec<Float>()
                        settleJob = scope.launch {
                            when (released) {
                                2 -> {
                                    animate(x, -width, velocity, out) { v, _ -> x = v }
                                    onDelete()
                                    x = 0f
                                }
                                1 -> {
                                    onStar()
                                    animate(x, 0f, velocity, settle) { v, _ -> x = v }
                                }
                                else -> animate(x, 0f, velocity, settle) { v, _ -> x = v }
                            }
                        }
                    }
                }
        ) { content() }
    }
}
