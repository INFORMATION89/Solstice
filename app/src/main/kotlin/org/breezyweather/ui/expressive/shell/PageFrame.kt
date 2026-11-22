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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import io.github.information89.motion.EdgeBlurDefaults
import io.github.information89.motion.Motion
import io.github.information89.motion.scrollEdgeBlur
import org.breezyweather.R
import org.breezyweather.ui.expressive.components.GlassSource
import org.breezyweather.ui.expressive.components.LocalSkyGlass
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.components.glass
import org.breezyweather.ui.expressive.theme.Emphasis
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.Space
import kotlin.math.abs

/*
 * The frame both the home shell and a postcard window build their pages in (component catalog,
 * 2026-11-19): the pager's position, the plain page that fades in away from the sky, the pager's
 * fling, and the dock the toolbar sits in.
 */

/** Where the pager is, fractionally (1.5 = half way between pages 1 and 2). Read it in draw/layout. */
internal val PagerState.position: Float get() = currentPage + currentPageOffsetFraction

/**
 * The plain page under the pages: invisible on the sky's page ([skyPage]), fully there one page
 * away, so pages other than Forecast sit on [page] and the sky only shows behind Forecast. Read in
 * draw, so it follows the finger without recomposing.
 */
internal fun Modifier.plainPageBackdrop(pager: PagerState, skyPage: Int, page: Color): Modifier =
    plainPageBackdrop({ pager.position }, skyPage, page)

/** The same, following a [position] read in draw (e.g. a page change's drawn slide). */
internal fun Modifier.plainPageBackdrop(position: () -> Float, skyPage: Int, page: Color): Modifier =
    drawBehind { drawRect(page, alpha = abs(position() - skyPage).coerceIn(0f, 1f)) }

/** The pages' fling: an unhurried, unbouncy snap to the nearest page (Motion.gentle). */
@Composable
internal fun pageFling(pager: PagerState): TargetedFlingBehavior = PagerDefaults.flingBehavior(
    state = pager,
    snapAnimationSpec = Motion.gentle.spec()
)

/**
 * Where the toolbar (and anything stacked above it, like messages) sits: centred at the bottom,
 * above the navigation bar. [alpha] (read in draw) lets it step aside while something covers it.
 */
@Composable
internal fun ToolbarDock(alpha: () -> Float = { 1f }, content: @Composable ColumnScope.() -> Unit) {
    val height = LocalToolbarHeight.current
    val density = LocalDensity.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                // How much of the screen's bottom it covers (toolbar, its margin, the navigation bar), for
                // the pages' bottom edge blur.
                .onSizeChanged { height.value = with(density) { it.height.toDp() } }
                .navigationBarsPadding()
                .padding(bottom = Space.S3)
                .graphicsLayer { this.alpha = alpha() },
            content = content
        )
    }
}

/**
 * How much of the screen's bottom the toolbar dock covers, measured by [ToolbarDock] (one shared state:
 * home and windows use the same dock). Provided by the shell.
 */
internal val LocalToolbarHeight = staticCompositionLocalOf<MutableState<Dp>> {
    error("No toolbar height here: the shell provides it")
}

/**
 * A place being visited in a postcard window (null on home): every page frame shows its "Visiting"
 * line at the top of the page, scrolling with it (user 2026-11-21: the line stayed fixed on top while
 * the page scrolled).
 */
internal data class Visit(val location: Location, val now: Long, val pinned: Boolean)

internal val LocalVisit = compositionLocalOf<Visit?> { null }

/**
 * Where a page's content starts: under the status bar, and in a postcard window also under its pinned
 * header ([VisitHeader], one touch target tall).
 */
@Composable
internal fun pageTopInset(): Dp {
    val status = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return if (LocalVisit.current != null) status + Sizes.TouchTarget else status
}

/**
 * A postcard window's header, pinned at the top of each of its pages (agreed 2026-11-21): it slides with
 * its page but doesn't scroll. The visit's line in the middle in [ink] (the page's), and the close button
 * at the start (Material's full-screen convention; back and swipe still close too), on the one glass
 * over [glass] (the page's sky, or none on a plain page), its icon in the theme's ink on glass.
 */
@Composable
internal fun VisitHeader(ink: Color, glass: GlassSource?, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(Sizes.TouchTarget)
            .padding(horizontal = Space.S4)
    ) {
        val label = stringResource(R.string.action_close)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(Sizes.TouchTarget)
                .glass(glass, CircleShape)
                .clickable(onClickLabel = label, role = Role.Button, onClick = onClose)
        ) {
            Icon(SolsticeIcons.Close, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
        }
        VisitingLine(ink, Modifier.align(Alignment.Center))
    }
}

/**
 * A visit's line: "[landing plane] Visiting" in any window (agreed 2026-11-21), and its local time from
 * the place time system when that differs from yours ("[clock] 2:33 PM, +14 h"). Nothing on home.
 */
@Composable
internal fun VisitingLine(ink: Color, modifier: Modifier = Modifier) {
    val visit = LocalVisit.current ?: return
    val local = placeTime(LocalContext.current, visit.location.timeZone, visit.now)
    val style = MaterialTheme.typography.labelLarge
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(SolsticeIcons.Small.FlightLand, null, tint = ink, modifier = Modifier.size(Sizes.IconSmall))
        Spacer(Modifier.width(Space.S1))
        Text(stringResource(R.string.shell_visiting), style = style, color = ink)
        if (local.label != null) {
            Text(" · ", style = style, color = ink)
            PlaceTimeLabel(local, ink, style)
        }
    }
}

/** The large title's distance below the status bar on plain pages. */
private val PageTitleTop = 34.dp

/** Room at the end of a plain page's list so its last item can scroll clear of the toolbar. */
private val ToolbarClearance = 160.dp

/**
 * A plain page (This week, Places): a list edge to edge under a transparent status bar and the
 * toolbar, the page's large title (with an optional [action], e.g. Settings) at the same height on
 * every page, the standard side margins, room for the toolbar at the end, and the progressive edge
 * blur at the top. One place to change them all (component catalog, M4).
 */
@Composable
internal fun PageScaffold(
    title: String,
    onPage: Color,
    page: Color,
    listState: LazyListState,
    pageFade: () -> Float,
    spacing: Dp,
    action: (@Composable () -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val top = pageTopInset()
    // A plain page has no sky behind its cards: their glass is just the tint (agreed 2026-11-21).
    CompositionLocalProvider(LocalSkyGlass provides null) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = Space.S4,
                end = Space.S4,
                top = top + PageTitleTop,
                bottom = ToolbarClearance
            ),
            verticalArrangement = Arrangement.spacedBy(spacing),
            // Motion's edge blur (agreed 2026-11-21): each edge, as tall as the chrome over it, shows only
            // while the list is scrolled under it, fading into the page.
            modifier = Modifier.fillMaxSize().scrollEdgeBlur(
                listState,
                top = EdgeBlurDefaults.top(top, page),
                bottom = EdgeBlurDefaults.bottom(LocalToolbarHeight.current.value, page),
                strength = pageFade
            )
        ) {
            item(key = "header") {
                // As tall as an icon button, with or without one, so the title sits at the same height.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.heightIn(min = Sizes.TouchTarget).padding(bottom = Space.S1)
                ) {
                    Text(
                        title,
                        color = onPage,
                        style = MaterialTheme.typography.displaySmallEmphasized.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f).padding(start = Space.S1)
                    )
                    action?.invoke()
                }
            }
            content()
        }
    }
}
