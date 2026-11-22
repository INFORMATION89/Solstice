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

package org.breezyweather.ui.expressive.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Emphasis
import org.breezyweather.ui.expressive.theme.Space

/** Check / close icon shown inside a Switch thumb. */
@Composable
fun SwitchThumbIcon(checked: Boolean) {
    Icon(
        imageVector = if (checked) SolsticeIcons.Small.Check else SolsticeIcons.Small.Close,
        contentDescription = null,
        modifier = Modifier.size(SwitchDefaults.IconSize)
    )
}

/**
 * Button label for an option: the name without a trailing "(details)", e.g.
 * "Follow system (Follow day/night)" → "Follow system". The details stay in the row's summary.
 */
fun choiceLabel(name: String): String = name.substringBefore(" (").trim()

/**
 * Whether a list choice is small enough to show inline as a connected button group:
 * 2–3 enabled options with short labels and no group headers or extra buttons.
 */
fun isInlineChoice(
    valueArray: Array<String>,
    nameArray: Array<String>,
    enableArray: Array<Boolean>?,
    hasExtras: Boolean,
): Boolean {
    if (hasExtras || valueArray.size !in 2..3 || nameArray.size != valueArray.size) return false
    if (nameArray.any { it.isEmpty() } || enableArray?.all { it } == false) return false
    val labels = nameArray.map(::choiceLabel)
    return labels.maxOf { it.length } <= 14 && labels.sumOf { it.length } <= 30
}

/**
 * Connected button group: one tap selects; the selected button morphs into a pill
 * (spring-animated by the Expressive theme, so it can be interrupted).
 */
@Composable
fun InlineChoiceButtons(
    valueArray: Array<String>,
    nameArray: Array<String>,
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        valueArray.forEachIndexed { i, value ->
            ToggleButton(
                checked = value == selected,
                onCheckedChange = { if (it) onSelect(value) },
                enabled = enabled,
                // Width follows the label, so "Follow system" gets more room than "Dark".
                // + 6: every button needs room for its padding, whatever its label length.
                modifier = Modifier.weight(choiceLabel(nameArray[i]).length + 6f),
                contentPadding = PaddingValues(horizontal = Space.S2),
                colors = ToggleButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shapes = when (i) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    valueArray.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                Text(
                    text = choiceLabel(nameArray[i]),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Bottom sheet replacing upstream's list dialog. Drag down to dismiss. Options are a segmented
 * list; an empty name marks a group header (upstream's convention). The sheet is slightly
 * translucent (overlays show what's behind them; blur comes later).
 */
@Composable
fun ChoiceSheet(
    title: String,
    valueArray: Array<String>,
    nameArray: Array<String>,
    enableArray: Array<Boolean>?,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    extraButton: @Composable (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = valueArray.size <= 8)
    val scope = rememberCoroutineScope()
    fun close(then: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            then()
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f)
    ) {
        Column(modifier = Modifier.padding(horizontal = Space.S4).navigationBarsPadding()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = Space.S2, bottom = Space.S4)
            )
            // Segment boundaries: options between headers form one rounded group.
            val isHeader = nameArray.map { it.isEmpty() }
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(valueArray.toList()) { i, value ->
                    if (isHeader[i]) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                start = Space.S2,
                                top = if (i ==
                                    0
                                ) {
                                    0.dp
                                } else {
                                    Space.S4
                                },
                                bottom = Space.S2
                            )
                        )
                    } else {
                        val first = i == 0 || isHeader[i - 1]
                        val last = i == valueArray.lastIndex || isHeader[i + 1]
                        val big = Corners.M
                        val small = Corners.XXS
                        val itemEnabled = enableArray?.getOrNull(i) ?: true
                        val isSelected = value == selected
                        // Material 3 selectable surface (radio semantics), in the segmented shape.
                        Surface(
                            selected = isSelected,
                            onClick = { close { onSelect(value) } },
                            enabled = itemEnabled,
                            shape = RoundedCornerShape(
                                topStart = if (first) big else small,
                                topEnd = if (first) big else small,
                                bottomStart = if (last) big else small,
                                bottomEnd = if (last) big else small
                            ),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = if (last) 0.dp else Space.Half)
                                .semantics { role = Role.RadioButton }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Space.S4, vertical = Space.S4),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nameArray[i],
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (itemEnabled) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = Emphasis.Disabled)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = SolsticeIcons.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (extraButton != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = Space.S2),
                    horizontalArrangement = Arrangement.End
                ) { extraButton() }
            }
            Spacer(modifier = Modifier.height(Space.S4))
        }
    }
}
