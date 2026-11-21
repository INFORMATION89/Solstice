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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import breezyweather.domain.location.model.Location
import io.github.information89.motion.Haptic
import io.github.information89.motion.Motion
import io.github.information89.motion.haptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.sky.SkyBackground
import org.breezyweather.ui.expressive.sky.SkyWeather
import org.breezyweather.ui.expressive.theme.CornerShapes
import org.breezyweather.ui.expressive.theme.Corners
import org.breezyweather.ui.expressive.theme.Elevation
import org.breezyweather.ui.expressive.theme.HeroType
import org.breezyweather.ui.expressive.theme.Sizes
import org.breezyweather.ui.expressive.theme.SolsticePalette
import org.breezyweather.ui.expressive.theme.Space
import java.util.Date
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A place as the front of a vintage postcard (user's references, 2026-11-18): an aged cream card
 * with a deckled edge and fine paper grain, captions in small capitals above and below the picture
 * (place and weather on top, region and local time below), the place's sky as the picture with a
 * faint linen texture and the temperature on it, a postmark over the corner, and a stamp. The
 * main place wears a star sticker on its stamp; a visit has a dashed inner frame. Postcard
 * proportions (about 3:2). The sky is baked and static, so scrolling stays cheap. Sinks a little
 * while pressed.
 */
@Composable
internal fun PlaceCard(
    location: Location,
    selected: Boolean,
    starred: Boolean,
    visiting: Boolean,
    folded: Boolean,
    now: Long,
    context: Context,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sky = skyFor(location, now)
    val current = location.weather?.current
    val press = remember { MutableInteractionSource() }
    val pressed by press.collectIsPressedAsState()
    val sink = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        // Like the toolbar tabs' squeeze: in quickly, back out with a little bounce.
        sink.animateTo(if (pressed) 0.98f else 1f, if (pressed) Motion.snappy.spec() else Motion.playful.spec())
    }
    val card = SolsticePalette.PaperWarm
    val ink = SolsticePalette.Ink
    // Sticker peel (agreed 2026-11-18): when it gets the star, a gold star sticker drops onto the
    // stamp slightly too big and tilted, presses down (squash, haptic), settles, and a shine sweeps
    // across it as it flattens. Waits for the card's glide to the top.
    var hadStar by remember { mutableStateOf(starred) }
    val peel = remember { Animatable(1f) }
    val shine = remember { Animatable(1f) }
    val haptics = haptics()
    LaunchedEffect(starred) {
        if (starred && !hadStar) {
            peel.snapTo(0f)
            shine.snapTo(0f)
            delay(520)
            launch {
                delay(330)
                haptics.perform(Haptic.press)
            }
            // A timed effect, not a motion (agreed 2026-11-19 as one of the few exceptions): the
            // sticker's drop, press and shine are keyframes along this one clock.
            peel.animateTo(1f, tween(560, easing = LinearEasing))
        }
        hadStar = starred
    }
    val grain = remember { paperGrain(light = true) }
    val grainBrush = remember(grain) { ShaderBrush(ImageShader(grain, TileMode.Repeated, TileMode.Repeated)) }
    val linen = remember { ShaderBrush(ImageShader(linenTexture(), TileMode.Repeated, TileMode.Repeated)) }
    val caption = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
    // The paper border: one caption line with its margins, the same on every side.
    val bezel = postcardBezel()
    val (title, city) = placeName(context, location)
    val showTime = !location.isCurrentPosition && timeDifference(location.timeZone, now) != null
    val fold by animateFloatAsState(
        if (folded) 1f else 0f,
        Motion.content.spec(),
        label = "fold"
    )
    // Tall enough for the star stamp.
    val stripHeight = with(LocalDensity.current) { 64.dp.toPx() }
    // Rounded like the other cards (agreed 2026-11-19), with the deckle on the straight edges.
    val deckled = with(LocalDensity.current) { remember(this) { DeckledShape(Corners.L.toPx()) } }
    Box(
        modifier
            .fillMaxWidth()
            // Postcard proportions, folding down to a one-line strip.
            .layout { measurable, constraints ->
                val w = constraints.maxWidth
                val full = w / 1.5f
                val h = (full + (stripHeight - full) * fold).roundToInt()
                val placeable = measurable.measure(Constraints.fixed(w, h))
                layout(w, h) { placeable.place(0, 0) }
            }
            .graphicsLayer {
                scaleX = sink.value
                scaleY = sink.value
                // A visit is lighter until you keep it.
                alpha = if (visiting) 0.92f else 1f
            }
            // Taps are taken on the whole card (outside the deckled clip, which can swallow them).
            .clickable(interactionSource = press, indication = null, onClick = onClick)
            // A soft, low shadow, like card lying on paper.
            .shadow(
                elevation = if (selected) Elevation.CardSelected else Elevation.Card,
                shape = deckled,
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.22f)
            )
            .clip(deckled)
            .drawBehind {
                drawRect(card)
                drawRect(grainBrush)
                // Aged edges: a faint warm darkening towards the border.
                drawRect(
                    Brush.radialGradient(
                        0.6f to Color.Transparent,
                        1f to SolsticePalette.Sepia.copy(alpha = 0.16f),
                        center = center,
                        radius = size.maxDimension * 0.62f
                    )
                )
            }
            // An even paper border all round (agreed 2026-11-19): the captions sit on its top and
            // bottom, the sides are as wide.
            .padding(start = bezel, end = bezel, top = Space.S2, bottom = Space.S2)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Top caption, on the paper above the picture: the place, and its weather.
            val topCaption: @Composable (Modifier) -> Unit = { captionModifier ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = captionModifier) {
                    // The current location's name is red, like a "you are here" mark on a map.
                    val here = SolsticePalette.HerePin
                    val nameColor = if (location.isCurrentPosition) here else ink
                    if (location.isCurrentPosition) {
                        Icon(
                            SolsticeIcons.Small.NearMeFill,
                            contentDescription = stringResource(R.string.location_current),
                            tint = nameColor,
                            modifier = Modifier.size(Sizes.IconXSmall)
                        )
                        Spacer(Modifier.width(Space.S1))
                    }
                    Text(
                        title.uppercase(),
                        style = caption,
                        color = nameColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (location.isCurrentPosition && starred) {
                            stringResource(R.string.shell_you_are_here)
                        } else {
                            listOfNotNull(
                                if (visiting && selected) context.getString(R.string.shell_visiting) else null,
                                current?.weatherText
                            ).joinToString(" · ")
                        }.uppercase(),
                        style = caption,
                        color = if (location.isCurrentPosition && starred) here else ink,
                        maxLines = 1,
                        modifier = Modifier.padding(start = Space.S2)
                    )
                }
            }
            topCaption(Modifier.padding(bottom = Space.S1).graphicsLayer { alpha = 1f - fold })
            // The picture: the place's sky, linen-textured, with the temperature. Folding, it turns
            // up and away about its top edge like paper.
            SkyBackground(
                state = sky,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        rotationX = 80f * fold
                        cameraDistance = 12f * density
                        alpha = 1f - fold
                        // Concentric with the card would be its corner minus the border (24 - 28 dp),
                        // below the minimum, so the smallest corner.
                        shape = CornerShapes.XS
                        clip = true
                    }
                    .drawWithContent {
                        drawContent()
                        drawRect(linen, blendMode = BlendMode.Multiply)
                        if (visiting) {
                            drawRoundRect(
                                ink.copy(alpha = 0.5f),
                                cornerRadius = CornerRadius(Corners.XS.toPx()),
                                style = Stroke(
                                    width = 1.2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx()))
                                )
                            )
                        }
                    }
            ) {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        current?.temperature?.temperature?.let { degrees(context, it) } ?: "–",
                        color = sky.palette.text,
                        style = HeroType.CardTemperature,
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = Space.S3, bottom = Space.S2)
                    )
                }
            }
            // Bottom caption: the region, and the local time when it differs from yours.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Space.S1).graphicsLayer { alpha = 1f - fold }
            ) {
                Text(
                    location.country.uppercase(),
                    style = caption,
                    color = ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (showTime) {
                    Text(
                        listOfNotNull(
                            time(context, Date(now), location.timeZone),
                            timeDifference(location.timeZone, now)
                        ).joinToString(" · ").uppercase(),
                        style = caption,
                        color = ink,
                        maxLines = 1,
                        modifier = Modifier.padding(start = Space.S2)
                    )
                }
            }
        }
        // The stamp sits on the picture's top right corner, the postmark half over it.
        // The star stamp marks the main place only. Placed and pressed (agreed 2026-11-18): it
        // lowers onto the card from a little above (bigger, soft shadow), touches down tilted, gets
        // pressed flat (shadow gone, slight squash, haptic) and rests at a small natural angle.
        if (starred) {
            Stamp(
                time = null,
                starred = true,
                paper = SolsticePalette.PaperAged,
                ink = ink,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    // In the strip it sits centred at the end.
                    // Centred vertically on the strip.
                    .padding(
                        top = lerp(bezel + Space.S6, Space.S1, fold),
                        end = lerp(bezel + Space.S1, 10.dp, fold)
                    )
                    .graphicsLayer {
                        val t = peel.value
                        val lower = (t / 0.6f).coerceIn(0f, 1f)
                        val press = ((t - 0.6f) / 0.4f).coerceIn(0f, 1f)
                        // Lowering: 1.35 to 1, then a small squash that releases.
                        val squash = if (press <
                            0.5f
                        ) {
                            1f - 0.05f * (press / 0.5f)
                        } else {
                            0.95f + 0.05f * ((press - 0.5f) / 0.5f)
                        }
                        val k = (1.35f - 0.35f * (1f - (1f - lower) * (1f - lower))) * squash
                        scaleX = k
                        scaleY = k
                        rotationZ = -9f + 6f * lower
                        shadowElevation = Elevation.Lifted.toPx() * (1f - lower)
                        ambientShadowColor = Color.Black.copy(alpha = 0.12f)
                        spotShadowColor = Color.Black.copy(alpha = 0.18f)
                        alpha = (t * 6f).coerceIn(0f, 1f)
                    }
            )
        }
        // The postmark is inked over the picture, so it takes the picture's text colour.
        Postmark(
            country = location.countryCode,
            ink = sky.palette.text,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = bezel, end = bezel)
                .graphicsLayer { alpha = 1f - fold }
        )
        // Folded: one line on the paper (the starred place's name; its stamp sits at the end).
        if (fold > 0.01f) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = Space.S2)
                    .graphicsLayer { alpha = ((fold - 0.4f) / 0.6f).coerceIn(0f, 1f) }
            ) {
                if (location.isCurrentPosition) {
                    Icon(
                        SolsticeIcons.Small.NearMeFill,
                        contentDescription = stringResource(R.string.location_current),
                        tint = SolsticePalette.Stamp,
                        modifier = Modifier.padding(start = Space.S2).size(14.dp)
                    )
                }
                Text(
                    placeName(context, location).first,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (location.isCurrentPosition) SolsticePalette.Stamp else ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = Space.S2, end = Space.S2)
                )
                // The current location: "you are here" when it's the main place, its weather otherwise.
                if (location.isCurrentPosition) {
                    if (starred) {
                        Text(
                            stringResource(R.string.shell_you_are_here).uppercase(),
                            style = caption,
                            color = SolsticePalette.Stamp,
                            modifier = Modifier.padding(end = 56.dp)
                        )
                    } else {
                        Icon(
                            SolsticeIcons.forSize(
                                hourIcon(SkyWeather.of(current?.weatherCode), sky.sunHeight < 0f),
                                Sizes.IconMedium
                            ),
                            contentDescription = current?.weatherText,
                            tint = SolsticePalette.Stamp,
                            modifier = Modifier.size(Sizes.IconMedium)
                        )
                        Spacer(Modifier.width(Space.S2))
                        Text(
                            current?.temperature?.temperature?.let { degrees(context, it) } ?: "–",
                            style = MaterialTheme.typography.titleMedium,
                            color = SolsticePalette.Stamp,
                            modifier = Modifier.padding(end = Space.S2)
                        )
                    }
                } else {
                    Spacer(Modifier.width(Space.S12))
                }
            }
        }
    }
}

/**
 * The card's edge: a fine deckle (torn-paper zigzag) along the straight sides, and the cards' large
 * rounded corners (agreed 2026-11-19, rounder postcards). [radius] in pixels.
 */
private class DeckledShape(private val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val step = 5f
        val bite = 1.6f
        val w = size.width
        val h = size.height
        val r = radius.coerceAtMost(minOf(w, h) / 2f)
        val path = Path().apply {
            // Top edge, left to right.
            moveTo(r, 0f)
            var x = r
            while (x + step < w - r) {
                lineTo(x + step / 2, bite)
                lineTo(x + step, 0f)
                x += step
            }
            lineTo(w - r, 0f)
            arcTo(Rect(w - 2 * r, 0f, w, 2 * r), -90f, 90f, false)
            // Right edge, top to bottom.
            var y = r
            while (y + step < h - r) {
                lineTo(w - bite, y + step / 2)
                lineTo(w, y + step)
                y += step
            }
            lineTo(w, h - r)
            arcTo(Rect(w - 2 * r, h - 2 * r, w, h), 0f, 90f, false)
            // Bottom edge, right to left.
            x = w - r
            while (x - step > r) {
                lineTo(x - step / 2, h - bite)
                lineTo(x - step, h)
                x -= step
            }
            lineTo(r, h)
            arcTo(Rect(0f, h - 2 * r, 2 * r, h), 90f, 90f, false)
            // Left edge, bottom to top.
            y = h - r
            while (y - step > r) {
                lineTo(bite, y - step / 2)
                lineTo(0f, y - step)
                y -= step
            }
            lineTo(0f, r)
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

/** A small tile of fine horizontal and vertical threads, like linen-finish card stock. */
private fun linenTexture(): ImageBitmap {
    val size = 64
    val pixels = IntArray(size * size)
    val random = java.util.Random(3)
    for (yy in 0 until size) {
        val row = random.nextFloat()
        for (xx in 0 until size) {
            val thread = if (yy % 3 == 0) 0.10f else 0f
            val warp = if (xx % 4 == 0) 0.05f else 0f
            val dark = (thread + warp + row * 0.03f).coerceIn(0f, 0.2f)
            val v = (255 * (1f - dark)).toInt()
            pixels[yy * size + xx] = (255 shl 24) or (v shl 16) or (v shl 8) or v
        }
    }
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
    return bitmap.asImageBitmap()
}

/** A faint round postmark with the country code, partly over the stamp. */
@Composable
private fun Postmark(country: String?, ink: Color, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(34.dp)
            .graphicsLayer { rotationZ = -12f }
            .drawBehind {
                drawCircle(ink.copy(alpha = 0.32f), style = Stroke(1.2.dp.toPx()))
                drawCircle(ink.copy(alpha = 0.22f), radius = size.minDimension * 0.36f, style = Stroke(0.8.dp.toPx()))
            }
    ) {
        Text(
            country.orEmpty().uppercase(),
            color = ink.copy(alpha = 0.45f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

/** A small postage stamp with a perforated edge: the local time (when it differs), or a star sticker. */
@Composable
private fun Stamp(
    time: String?,
    starred: Boolean,
    paper: Color,
    ink: Color,
    modifier: Modifier = Modifier,
    peel: () -> Float = { 1f },
    shine: () -> Float = { 1f },
    starTint: Color? = null,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .drawBehind {
                drawRect(paper)
                val dot = 2.dp.toPx()
                drawRect(
                    color = ink.copy(alpha = 0.3f),
                    topLeft = Offset(dot, dot),
                    size = Size(size.width - 2 * dot, size.height - 2 * dot),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.1f, 3.dp.toPx())),
                        cap = StrokeCap.Round
                    )
                )
            }
            .padding(Space.S1)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (starred) {
                // The star sticker is red everywhere (2026-11-19).
                val gold = starTint ?: SolsticePalette.Stamp
                val starSize = if (time != null) 14.dp else 22.dp
                Icon(
                    SolsticeIcons.forSize(SolsticeIcons.StarFill, starSize),
                    contentDescription = stringResource(R.string.shell_starred),
                    tint = gold,
                    modifier = Modifier
                        .size(starSize)
                        .graphicsLayer {
                            val t = 1f
                            val sc = when {
                                t < 0.45f -> 1.55f - 0.69f * (t / 0.45f)
                                t < 0.7f -> 0.86f + 0.18f * ((t - 0.45f) / 0.25f)
                                else -> 1.04f - 0.04f * ((t - 0.7f) / 0.3f)
                            }
                            scaleX = sc
                            scaleY = sc * (if (t in 0.4f..0.55f) 0.94f else 1f)
                            rotationZ = -14f * (1f - (t / 0.45f).coerceIn(0f, 1f))
                            alpha = (t * 5f).coerceIn(0f, 1f)
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            // The shine sweeping across as the sticker flattens.
                            val k = shine()
                            if (k in 0.001f..0.999f) {
                                val x = -size.width + 3f * size.width * k
                                drawRect(
                                    Brush.linearGradient(
                                        0f to Color.Transparent,
                                        0.5f to Color.White.copy(alpha = 0.85f),
                                        1f to Color.Transparent,
                                        start = Offset(x, 0f),
                                        end = Offset(x + size.width * 0.7f, size.height)
                                    ),
                                    blendMode = BlendMode.SrcAtop
                                )
                            }
                        }
                )
            }
            if (time != null) {
                Text(
                    time,
                    color = ink,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

/** Grainy paper for the Places page: a warm off-white (or deep warm grey) with fine fibre grain. */
@Composable
private fun Modifier.grainyPaper(onPage: Color): Modifier {
    val light = onPage.luminance() < 0.5f
    val grain = remember(light) { paperGrain(light) }
    val brush = remember(grain) { ShaderBrush(ImageShader(grain, TileMode.Repeated, TileMode.Repeated)) }
    return drawBehind {
        drawRect(if (light) SolsticePalette.PaperSheet else SolsticePalette.PaperNight)
        drawRect(brush)
    }
}

/** A small tile of soft noise (seeded, so it's the same every time), drawn repeated. */
private fun paperGrain(light: Boolean): ImageBitmap {
    val size = 128
    val pixels = IntArray(size * size)
    val random = java.util.Random(7)
    for (i in pixels.indices) {
        val n = random.nextGaussian().toFloat()
        val alpha = (kotlin.math.abs(n) * if (light) 16f else 12f).toInt().coerceIn(0, 48)
        val shade = if (n > 0f) 255 else 0
        pixels[i] = (alpha shl 24) or (shade shl 16) or (shade shl 8) or shade
    }
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
    return bitmap.asImageBitmap()
}

/**
 * The current position's name: neighbourhood first, its city second (when the address has both),
 * e.g. "Providencia" over "Guadalajara". Other places keep their usual name.
 */
internal fun placeName(context: Context, location: Location): Pair<String, String?> {
    // For now the borough (e.g. "Miguel Hidalgo") rather than the neighbourhood: shorter.
    val borough = location.admin2?.takeIf { it.isNotEmpty() && it != location.city && it != location.admin1 }
    val district = location.district?.takeIf { it.isNotEmpty() && it != location.city }
    val local = borough ?: district
    return if (location.isCurrentPosition && location.customName.isNullOrEmpty() && local != null) {
        local to location.city.takeIf { it.isNotEmpty() }
    } else {
        location.getPlace(context) to null
    }
}

/**
 * Under the place name: its weather, plus its local time and time difference when it's in another
 * time zone (always for a visit), e.g. "Visiting · Overcast · 14:32, +1 h".
 */
private fun placeSubtitle(
    context: Context,
    location: Location,
    visiting: Boolean,
    now: Long,
    withTime: Boolean = true,
): String {
    val diff = timeDifference(location.timeZone, now)
    val local = if (diff != null || visiting) {
        listOfNotNull(if (withTime) time(context, Date(now), location.timeZone) else null, diff)
            .joinToString(", ")
            .ifEmpty { null }
    } else {
        null
    }
    return listOfNotNull(
        if (visiting) context.getString(R.string.shell_visiting) else null,
        location.weather?.current?.weatherText,
        local
    ).joinToString(" · ")
}

/** A dashed outline marking a place you're only visiting. */
private fun Modifier.dashedEdge(color: Color, corner: androidx.compose.ui.unit.Dp) = drawWithContent {
    drawContent()
    val r = corner.toPx()
    drawRoundRect(
        color = color.copy(alpha = 0.55f),
        cornerRadius = CornerRadius(r, r),
        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
    )
}

/** The postcard's paper border (top and bottom hold a caption line; the sides are as wide). */
@Composable
internal fun postcardBezel(): Dp {
    val line = with(LocalDensity.current) { MaterialTheme.typography.labelMedium.lineHeight.toDp() }
    return Space.S2 + line + Space.S1
}
