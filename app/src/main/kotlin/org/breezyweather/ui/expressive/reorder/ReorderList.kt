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

package org.breezyweather.ui.expressive.reorder

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.information89.motion.Haptic
import io.github.information89.motion.haptics
import org.breezyweather.R
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Elevation
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.Space
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * A segmented list whose rows are reordered by dragging the handle (or long-pressing the row).
 * Dragging, swapping, edge auto-scroll and the drop are the Reorderable library's (2026-11-19,
 * replacing our own); the look, haptics and TalkBack's "move up" / "move down" actions are ours.
 */
@Composable
fun <T : Any> ReorderList(
    items: SnapshotStateList<T>,
    key: (T) -> String,
    label: (T) -> String,
    onRemove: (T) -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable ((T) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
    footer: LazyListScope.() -> Unit = {},
) {
    val haptics = haptics()

    fun move(from: Int, to: Int) {
        if (from == to || from !in items.indices || to !in items.indices) return
        items.add(to, items.removeAt(from))
        haptics.perform(Haptic.detent)
    }

    // Moves by key, so the footer's items (hidden chips) are never drop targets.
    val reorder = rememberReorderableLazyListState(listState) { from, to ->
        move(items.indexOfFirst { key(it) == from.key }, items.indexOfFirst { key(it) == to.key })
    }

    val moveUp = stringResource(R.string.action_move_up_expressive)
    val moveDown = stringResource(R.string.action_move_down_expressive)
    val remove = stringResource(R.string.action_remove_expressive)
    val placement = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    LazyColumn(state = listState, contentPadding = contentPadding, modifier = modifier) {
        itemsIndexed(items, key = { _, it -> key(it) }) { index, item ->
            ReorderableItem(
                reorder,
                key = key(item),
                animateItemModifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                    placementSpec = placement
                )
            ) { isDragging ->
                val elevation by animateDpAsState(
                    if (isDragging) Elevation.Card else 0.dp,
                    MaterialTheme.motionScheme.fastSpatialSpec(),
                    label = "dragElevation"
                )
                val first = index == 0
                val last = index == items.lastIndex
                val big = Corners.L
                val small = if (isDragging) big else Corners.XXS
                Surface(
                    shape = RoundedCornerShape(
                        topStart = if (first) big else small,
                        topEnd = if (first) big else small,
                        bottomStart = if (last) big else small,
                        bottomEnd = if (last) big else small
                    ),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shadowElevation = elevation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Space.Half)
                        .longPressDraggableHandle(
                            onDragStarted = { haptics.perform(Haptic.press) }
                        )
                        .semantics {
                            customActions = listOf(
                                CustomAccessibilityAction(moveUp) {
                                    val i = items.indexOf(item)
                                    move(i, i - 1)
                                    true
                                },
                                CustomAccessibilityAction(moveDown) {
                                    val i = items.indexOf(item)
                                    move(i, i + 1)
                                    true
                                },
                                CustomAccessibilityAction(remove) {
                                    onRemove(item)
                                    true
                                }
                            )
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = Space.S1, end = Space.S1, top = Space.S1, bottom = Space.S1)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_drag),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(Sizes.TouchTarget)
                                .draggableHandle(
                                    onDragStarted = {
                                        haptics.perform(Haptic.threshold)
                                    }
                                )
                                .padding(Space.S3)
                        )
                        Text(
                            text = label(item),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        trailing?.invoke(item)
                        IconButton(onClick = { onRemove(item) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = remove,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        footer()
    }
}
