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

package org.breezyweather.ui.expressive.sky

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.random.Random

/**
 * The soft, grainy sky gradient behind the Forecast (and on location cards).
 *
 * Cheap by design: the "blurred" colour fields are radial gradients (no real blur), the grain is
 * one small noise tile repeated, and everything is built once per size/palette in
 * [drawWithCache], so a static sky costs nothing per frame beyond drawing a few cached brushes.
 */
@Composable
fun SkyBackground(
    state: SkyState,
    modifier: Modifier = Modifier,
    effects: SkyEffects = SkyEffects.None,
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier.drawWithCache {
            // Bake the whole sky once into a half-resolution image; each frame then only draws
            // that image (one cheap blit), even while it moves during a swipe.
            val bw = (size.width / BAKE_SCALE).toInt().coerceAtLeast(1)
            val bh = (size.height / BAKE_SCALE).toInt().coerceAtLeast(1)
            val key = BakeKey(
                state.palette,
                (state.sunX * 100).toInt(),
                (state.sunY * 100).toInt(),
                bw,
                bh,
                (effects.stars * 20).toInt()
            )
            val baked = bakedSkies.getOrPut(key) {
                ImageBitmap(bw, bh).also { image ->
                    CanvasDrawScope().draw(this, layoutDirection, Canvas(image), Size(bw.toFloat(), bh.toFloat())) {
                        drawSky(state)
                        drawStars(state, effects.stars)
                    }
                }
            }
            onDrawBehind {
                drawImage(
                    image = baked,
                    dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                    filterQuality = FilterQuality.Low
                )
            }
        }
    ) { content() }
}

/** The sky is baked at 1/[BAKE_SCALE] resolution: smooth gradients lose nothing, grain gets softer. */
private const val BAKE_SCALE = 2f

/**
 * The last few baked skies (by palette and size), so swiping back and forth between pages or
 * scrolling location cards doesn't bake again. A full-screen sky at half resolution is ~2.6 MB,
 * so keep only a handful. Only touched from the UI thread (drawWithCache).
 */
private val bakedSkies = object : LinkedHashMap<BakeKey, ImageBitmap>(8, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<BakeKey, ImageBitmap>) = size > 6
}

/** Sun position is rounded to 1 % so a slowly moving sun doesn't re-bake on every tiny step. */
private data class BakeKey(
    val palette: SkyPalette,
    val sunX: Int,
    val sunY: Int,
    val w: Int,
    val h: Int,
    val stars: Int,
)

/**
 * Stars baked into the static sky (F4, below Android 13 or with motion off): the same idea as the
 * shader's (a fixed random field, brighter towards the top, hidden under the sun/moon glow), drawn
 * once into the cached image. No twinkle.
 */
private fun DrawScope.drawStars(state: SkyState, amount: Float) {
    if (amount <= 0.01f) return
    val w = size.width
    val h = size.height
    val unit = max(w, h)
    val random = java.util.Random(7)
    val count = (220 * amount).toInt()
    val glow = Offset(state.sunX * w, state.sunY * h)
    repeat(count) {
        val p = Offset(random.nextFloat() * w, random.nextFloat() * h * 0.85f)
        val fadeDown = (1f - p.y / (h * 0.85f)).coerceIn(0f, 1f)
        val nearGlow = ((p - glow).getDistance() / (0.25f * unit)).coerceIn(0f, 1f)
        val b = (0.35f + 0.65f * random.nextFloat()) * fadeDown * nearGlow * amount
        val r = (0.6f + 1.2f * random.nextFloat()) * unit / 2160f
        drawCircle(androidx.compose.ui.graphics.Color.White.copy(alpha = b.coerceIn(0f, 1f)), radius = r, center = p)
    }
}

private fun DrawScope.drawSky(state: SkyState) {
    val palette = state.palette
    val w = size.width
    val h = size.height
    val unit = max(w, h)
    drawRect(palette.base)
    blobLayout.forEachIndexed { i, b ->
        val color = palette.blobs[i]
        drawRect(
            Brush.radialGradient(
                0f to color,
                0.45f to color.copy(alpha = 0.75f),
                1f to color.copy(alpha = 0f),
                center = Offset(b.x * w, b.y * h),
                radius = b.r * unit
            )
        )
    }
    // The sun (or moon after dark): a soft light where the real sun is.
    val sun = palette.sun.copy(alpha = palette.sunStrength)
    drawRect(
        Brush.radialGradient(
            0f to sun,
            0.35f to sun.copy(alpha = palette.sunStrength * 0.55f),
            1f to sun.copy(alpha = 0f),
            center = Offset(state.sunX * w, state.sunY * h),
            radius = 0.42f * unit
        )
    )
    drawRect(
        ShaderBrush(ImageShader(grainTile, TileMode.Repeated, TileMode.Repeated)),
        alpha = palette.grain,
        blendMode = BlendMode.Overlay
    )
}

/** Where the three colour fields sit, as fractions of the width/height; radius of the longer side. */
private class Blob(val x: Float, val y: Float, val r: Float)

private val blobLayout = listOf(
    Blob(0.08f, 0.06f, 0.62f), // top left
    Blob(0.98f, 0.46f, 0.55f), // right, middle
    Blob(0.18f, 0.98f, 0.60f) // bottom left
)

/** A 160×160 tile of grey film grain, made once per process. */
private val grainTile: ImageBitmap by lazy {
    val size = 160
    val random = Random(7)
    val pixels = IntArray(size * size) {
        val v = random.nextInt(256)
        (0xFF shl 24) or (v shl 16) or (v shl 8) or v
    }
    android.graphics.Bitmap.createBitmap(pixels, size, size, android.graphics.Bitmap.Config.ARGB_8888)
        .asImageBitmap()
}
