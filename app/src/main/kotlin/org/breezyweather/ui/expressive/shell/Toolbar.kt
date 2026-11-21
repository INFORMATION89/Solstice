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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.information89.motion.Haptic
import io.github.information89.motion.Motion
import io.github.information89.motion.haptics
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.ui.expressive.components.GlassSource
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.components.glass
import org.breezyweather.ui.expressive.sky.SkyState
import org.breezyweather.ui.expressive.sky.SkyWeather
import org.breezyweather.ui.expressive.theme.Elevation
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.SolsticePalette
import org.breezyweather.ui.expressive.theme.Space
import kotlin.math.abs
import kotlin.math.roundToInt

/** A toolbar tab: [iconOn] is the filled icon shown while it is the selected one. */
internal class Tab(val label: Int, val icon: ImageVector, val iconOn: ImageVector)

/**
 * Floating toolbar. Each tab is an icon; the selected one unfolds to show its title. Widths, label
 * and highlight are all driven by the pager position, so they follow the finger mid-swipe and fold
 * back if the swipe reverses.
 */
@Composable
internal fun ShellToolbar(
    pager: PagerState,
    sky: SkyState,
    weather: SkyWeather,
    glass: GlassSource,
    labelShown: List<Float>,
    onTab: (Int) -> Unit,
    searchEnabled: Boolean,
    onSearch: () -> Unit,
    onSearchBounds: (Rect) -> Unit,
    // The same toolbar in a postcard window: no Places tab, and the bud (Pin) comes out of Forecast.
    withPlaces: Boolean = true,
    budIcon: ImageVector = SolsticeIcons.Search,
    budDescription: String = stringResource(R.string.shell_search_elsewhere),
    budChecked: Boolean = false,
    // 0 while something that grew out of the bud (search's card) is on screen: that card draws the
    // bud's icon itself on its way back, so the two never show at once (read in draw).
    budIconShown: () -> Float = { 1f },
    position: () -> Float? = { null },
    // Tabs only passed over by a change in progress keep their label folded (ShellNavigation.isPassing).
    passing: (Int) -> Boolean = { false },
    // A finger on the toolbar (the slide) stops any page transition in progress.
    onInterrupt: () -> Unit = {},
) {
    val tabs = listOfNotNull(
        Tab(R.string.shell_tab_days, SolsticeIcons.CalendarMonth, SolsticeIcons.CalendarMonthFill),
        Tab(
            R.string.shell_tab_forecast,
            hourIcon(weather, night = sky.sunHeight < 0f),
            hourIcon(weather, night = sky.sunHeight < 0f, filled = true)
        ),
        if (withPlaces) Tab(R.string.shell_tab_places, SolsticeIcons.Map, SolsticeIcons.MapFill) else null
    )
    val collapsed = 52.dp
    val expanded = 132.dp
    val position = position() ?: (pager.currentPage + pager.currentPageOffsetFraction)
    val open = tabs.indices.map { (1f - abs(position - it)).coerceIn(0f, 1f) }
    // How far each tab's title is unfolded: selected (follows the swipe) and not already shown as the
    // page's own big title. A tab only passed over by a change keeps its label folded.
    val shown = tabs.indices.map { i -> if (passing(i)) 0f else open[i] * labelShown[i] }
    val widths = shown.map { collapsed + (expanded - collapsed) * it }
    val starts = widths.runningFold(0.dp) { acc, w -> acc + w }
    val highlightX = tabs.indices.fold(0.dp) { acc, i -> acc + starts[i] * open[i] }
    val highlightW = tabs.indices.fold(0.dp) { acc, i -> acc + widths[i] * open[i] }
    // Frosted glass: the page behind, blurred, under a light tint of the surface colour.
    val tint = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.38f)
    // On Forecast the highlight wears the weather (a colour from the sky); swiping to Outlook or
    // Places morphs it into the Material You colour, in step with the swipe.
    val skyAccent = sky.palette.blobs[1]
    val onSkyAccent = if (skyAccent.luminance() > 0.45f) SolsticePalette.InkNearBlack else Color.White
    val primary = lerp(MaterialTheme.colorScheme.primary, skyAccent, open[PAGE_FORECAST])
    val onPrimary = lerp(MaterialTheme.colorScheme.onPrimary, onSkyAccent, open[PAGE_FORECAST])
    val onSurface = MaterialTheme.colorScheme.onSurface

    // The search bud (agreed 2026-11-18, "gooey bud"): on Places a round search button swells out of
    // the pill's right end, held by a stretchy glass neck, and snaps free into its slot beside the
    // toolbar. Pill, neck and bubble are one glass shape. The swipe drives it; only the neck's
    // snap (and its regrowth on the way back) is a spring.
    // The bud comes out of the last tab (Places at home, Forecast in a window).
    val bud = open[tabs.lastIndex]
    val height = 52.dp
    val gap = 10.dp
    val pillWidth = starts.last() + 8.dp
    val joined = bud < 0.97f
    val neck = remember { Animatable(1f) }
    // Liquid drop (agreed 2026-11-18): free of the pill, the bubble wobbles (overshoot); taken back in,
    // the pill ripples a little as it absorbs it.
    val wobble = remember { Animatable(0f) }
    val ripple = remember { Animatable(0f) }
    LaunchedEffect(joined) {
        if (!joined) {
            launch {
                wobble.snapTo(1f)
                wobble.animateTo(0f, ToolbarMotion.wobble.spec())
            }
        } else if (bud > 0.05f) {
            launch {
                ripple.snapTo(1f)
                ripple.animateTo(0f, ToolbarMotion.wobble.spec())
            }
        }
        neck.animateTo(
            if (joined) 1f else 0f,
            // Letting go: no bounce (overshooting below zero brought a sliver of neck back).
            if (joined) ToolbarMotion.neckJoin.spec() else ToolbarMotion.neckFree.spec()
        )
    }
    val density = LocalDensity.current
    val shape = with(density) {
        BudShape(budExtra = ((gap + height) * bud).toPx(), neck = neck.value, gap = gap.toPx())
    }
    Box(
        Modifier
            .width(pillWidth + (gap + height) * bud)
            .height(height)
            .graphicsLayer {
                // The ripple when it absorbs the bubble.
                scaleX = 1f + ToolbarMotion.rippleStretch * ripple.value
                scaleY = 1f - ToolbarMotion.rippleSquash * ripple.value
            }
            .glass(glass, shape, tint)
    ) {
        ToolbarTabs(
            pillWidth, open, shown, widths, starts, highlightX, highlightW, primary, onPrimary, onSurface, tabs,
            pager, onTab, onInterrupt
        )
        // It swells inside the pill first (a little bigger as it comes out), then shows its icon.
        val iconAlpha = ((bud - 0.6f) / 0.4f).coerceIn(0f, 1f)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(x = pillWidth - height / 2 + (gap + height) * bud - height / 2)
                .size(height)
                .onGloballyPositioned { onSearchBounds(it.boundsInRoot()) }
                .clip(CircleShape)
                // Only touchable once it's out: tucked in, it sits over the Places tab, and even a
                // disabled clickable would swallow that tab's taps.
                .then(if (searchEnabled && bud > 0.9f) Modifier.clickable(onClick = onSearch) else Modifier)
        ) {
            // Push in (agreed 2026-11-18): pinning tilts the pin, jabs it down (squash, haptic) and
            // stands it up in the Forecast colour; unpinning pops it back out with a little tilt.
            val jab = remember { Animatable(0f) }
            var wasChecked by remember { mutableStateOf(budChecked) }
            val haptics = haptics()
            LaunchedEffect(budChecked) {
                if (budChecked != wasChecked) {
                    jab.snapTo(if (budChecked) 1f else -1f)
                    if (budChecked) haptics.perform(Haptic.press)
                    jab.animateTo(0f, ToolbarMotion.jab.spec())
                }
                wasChecked = budChecked
            }
            // White in dark mode, black in light mode (the outline or the fill carries the state).
            val tintColor by animateColorAsState(onSurface, ToolbarMotion.budTint.spec(), label = "bud")
            Icon(
                budIcon,
                contentDescription = if (bud > 0.5f) budDescription else null,
                tint = tintColor,
                modifier = Modifier.graphicsLayer {
                    val j = jab.value
                    alpha = iconAlpha * budIconShown()
                    // In: down and squashed; out: up and stretched.
                    translationY = j * ToolbarMotion.jabDrop.dp.toPx()
                    scaleY = 1f - ToolbarMotion.jabSquash * j
                    scaleX = 1f + ToolbarMotion.jabStretch * j
                    rotationZ = -ToolbarMotion.jabTilt * kotlin.math.abs(j)
                    // The bubble's own wobble right after it pops free (scale only).
                    val w = wobble.value
                    scaleX *= 1f + ToolbarMotion.wobbleAmount * w
                    scaleY *= 1f + ToolbarMotion.wobbleAmount * w
                }
            )
        }
    }
}

@Composable
private fun ToolbarTabs(
    pillWidth: Dp,
    open: List<Float>,
    shown: List<Float>,
    widths: List<Dp>,
    starts: List<Dp>,
    highlightX: Dp,
    highlightW: Dp,
    primary: Color,
    onPrimary: Color,
    onSurface: Color,
    tabs: List<Tab>,
    pager: PagerState,
    onTab: (Int) -> Unit,
    onInterrupt: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    // Slide to switch pages (like iOS 26/27): hold the pill and the selection pops out (lifts and
    // grows a little); then it follows the finger at the finger's pace, tab by tab, with the page
    // following along. Let go and it settles on the nearest page and sinks back in.
    val pop = remember { Animatable(0f) }
    Box(
        Modifier
            .width(pillWidth)
            .pointerInput(pager, tabs.size) {
                val collapsedPx = 52.dp.toPx()
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        onInterrupt()
                        scope.launch {
                            pop.animateTo(1f, ToolbarMotion.popOut.spec())
                        }
                    },
                    onDragEnd = {
                        val position = pager.currentPage + pager.currentPageOffsetFraction
                        onTab(position.roundToInt().coerceIn(0, tabs.lastIndex))
                        scope.launch {
                            pop.animateTo(0f, ToolbarMotion.popIn.spec())
                        }
                    },
                    onDragCancel = {
                        onTab(pager.currentPage)
                        scope.launch { pop.animateTo(0f, ToolbarMotion.popCancel.spec()) }
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        val pagePx =
                            pager.layoutInfo.pageSize.toFloat().takeIf { it > 0f }
                                ?: return@detectDragGesturesAfterLongPress
                        // One page per tab's width of finger travel (the unselected tabs' width), so
                        // it moves at the finger's pace rather than racing ahead.
                        pager.dispatchRawDelta(amount.x * pagePx / (collapsedPx * ToolbarMotion.slideTravel))
                    }
                )
            }
            .padding(Space.S1)
    ) {
        Box(
            Modifier
                .offset(x = highlightX)
                .width(highlightW)
                .height(44.dp)
                .graphicsLayer {
                    // Popped out while held: a bit bigger, lifted with a soft shadow.
                    val k = 1f + ToolbarMotion.popScale * pop.value
                    scaleX = k
                    scaleY = k
                    shadowElevation = Elevation.Card.toPx() * pop.value
                    shape = CircleShape
                }
                .background(primary, CircleShape)
        )
        Row {
            tabs.forEachIndexed { i, tab ->
                val label = stringResource(tab.label)
                val color = lerp(onSurface, onPrimary, open[i])
                // No square ripple: the tab's content gives a little press (shrinks, springs back).
                val press = remember { MutableInteractionSource() }
                val pressed by press.collectIsPressedAsState()
                val squeeze = remember { Animatable(1f) }
                LaunchedEffect(pressed) {
                    // In quickly; back out with a little bounce.
                    squeeze.animateTo(
                        if (pressed) ToolbarMotion.pressScale else 1f,
                        if (pressed) ToolbarMotion.pressDown.spec() else ToolbarMotion.pressUp.spec()
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(widths[i])
                        .height(44.dp)
                        .clipToBounds()
                        .semantics(mergeDescendants = true) {
                            selected = pager.currentPage == i
                            contentDescription = label
                        }
                        .clickable(interactionSource = press, indication = null, role = Role.Tab) { onTab(i) }
                        .graphicsLayer {
                            scaleX = squeeze.value
                            scaleY = squeeze.value
                        }
                        .padding(horizontal = Space.S2),
                    // Icon and label centred in the tab, whatever the label's length.
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Filled while selected (M3: navigation shows the current destination filled); a swap, not an animation.
                    Icon(
                        // By selection (more than half way to this tab), not by the label: on This week and
                        // Places the label folds away while the page shows its own title (fixed 2026-11-20).
                        if (open[i] > 0.5f) tab.iconOn else tab.icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(Sizes.IconLarge)
                    )
                    Spacer(Modifier.width(Space.S2 * shown[i]))
                    Text(
                        text = label,
                        color = color,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            // Takes only as much width as it has unfolded, so the icon stays centred
                            // in a folded tab.
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(
                                    constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
                                )
                                layout((placeable.width * shown[i]).roundToInt(), placeable.height) {
                                    placeable.place(0, 0)
                                }
                            }
                            .graphicsLayer { alpha = shown[i] }
                    )
                }
            }
        }
    }
}

private operator fun Dp.times(f: Float): Dp = Dp(value * f)

/**
 * The toolbar pill plus the search bubble, as one outline. The bubble is a circle as tall as the
 * pill whose centre slides [budExtra] from the pill's right cap (hidden in it) to its
 * slot. Once they separate, a neck (two curves pinching towards the middle) joins them; it thins as
 * they part and vanishes with [neck] = 0.
 */
private class BudShape(
    private val budExtra: Float,
    private val neck: Float,
    private val gap: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = size.height / 2f
        // Measured from the size being drawn, so the outline always fits the box exactly.
        val pillWidth = size.width - budExtra
        val centre = pillWidth - r + budExtra
        // Tucked in (every page but Places): just the pill, no path operations.
        if (budExtra < 0.5f) {
            return Outline.Rounded(RoundRect(0f, 0f, size.width, size.height, CornerRadius(r, r)))
        }
        val pill = Path().apply { addRoundRect(RoundRect(0f, 0f, pillWidth, size.height, CornerRadius(r, r))) }
        val ball = Path().apply { addOval(Rect(centre - r, 0f, centre + r, size.height)) }
        var path = Path.combine(PathOperation.Union, pill, ball)
        val cap = pillWidth - r
        val apart = (centre - r) - pillWidth // space between pill and bubble
        if (neck > 0.02f && apart > 0f) {
            // Attach where the circles face each other, 50 degrees above and below the axis.
            val dx = r * 0.643f
            val dy = r * 0.766f
            val thinning = (apart / gap).coerceIn(0f, 1f)
            val waist = (neck.coerceIn(0f, 1.3f) * (1.5f - 1.2f * thinning) * r).coerceAtMost(2 * dy)
            val mid = (cap + centre) / 2f
            // Control points chosen so the curve's midpoint is exactly at the waist.
            val top = r + dy - waist
            val bottom = r - dy + waist
            val bridge = Path().apply {
                moveTo(cap + dx, r - dy)
                quadraticTo(mid, top, centre - dx, r - dy)
                lineTo(centre - dx, r + dy)
                quadraticTo(mid, bottom, cap + dx, r + dy)
                close()
            }
            path = Path.combine(PathOperation.Union, path, bridge)
        }
        return Outline.Generic(path)
    }
}
