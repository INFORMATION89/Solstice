/*
 * Copyright 2026 INFORMATION89
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.information89.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.util.lerp as lerpFloat

/**
 * What a morphing surface looks like at one end (agreed 2026-11-21: "the button itself becomes the
 * card"). A [Morph] given the source's style and the target's draws the button **exactly as it is**
 * at the start (so the real button can hide in the same frame, at identical pixels) and moves every
 * property towards the card's as it grows. Nothing crossfades: fill, border, shadow and the amount
 * of the app's [material] (e.g. its glass) each interpolate.
 *
 * Corner radius isn't here: it comes from the morph's anchors.
 */
@Immutable
data class MorphStyle(
    /** A solid fill, drawn over the material. */
    val fill: Color = Color.Transparent,
    /** An outline along the surface's shape. */
    val border: Color = Color.Transparent,
    val borderWidth: Dp = 0.dp,
    /** Elevation shadow. */
    val shadow: Dp = 0.dp,
    /** How much of the app-supplied material shows (0 none … 1 full), e.g. a frosted glass. */
    val material: Float = 0f,
) {
    companion object {
        /** No fill, outline, shadow or material. */
        val None = MorphStyle()

        /** The style [t] of the way (0 = [a], 1 = [b]). */
        fun lerp(a: MorphStyle, b: MorphStyle, t: Float): MorphStyle = MorphStyle(
            fill = lerpColor(a.fill, b.fill, t),
            border = lerpColor(a.border, b.border, t),
            borderWidth = lerpDp(a.borderWidth, b.borderWidth, t),
            shadow = lerpDp(a.shadow, b.shadow, t),
            material = lerpFloat(a.material, b.material, t)
        )
    }
}

/** Defaults for [Morph]'s choreography, in fractions of the morph's value. */
object MorphDefaults {
    /** The surface's style changes from the source's to the target's over this part of the way. */
    val StyleWindow: ClosedFloatingPointRange<Float> = 0f..0.3f

    /** The source's own content (a button's label) hands over to the target's over this part. */
    val SourceWindow: ClosedFloatingPointRange<Float> = 0f..0.15f

    /** How far a pressed morph surface sinks (iOS 26 interactive glass scales under the finger). */
    const val PRESS_DEPTH: Float = 0.03f
}

/**
 * The element a [Morph] grows out of: registers it as anchor [key] (like [morphAnchor]) and hides it
 * exactly while a [Morph] from [key] is drawing it, so the morph (identical in its first frame) takes
 * over without a gap or a swap, and it reappears when the morph is gone. [hidden] can hide it for
 * other reasons too. Read in draw.
 */
@Composable
fun Modifier.morphSource(key: Any, cornerRadius: Dp = 0.dp, hidden: () -> Boolean = { false }): Modifier {
    val registry = LocalMorphRegistry.current
    return this
        .morphAnchor(key, cornerRadius)
        .graphicsLayer { alpha = if (hidden() || registry?.covered?.containsKey(key) == true) 0f else 1f }
}

/**
 * Whether a [Morph] from [key] is on screen (it has drawn at least one frame). Start the morph's
 * motion once this is true, so its first visible frame is the source itself: a sheet that takes a
 * few frames to build would otherwise appear already part-grown.
 */
@Composable
fun rememberMorphDrawn(key: Any): androidx.compose.runtime.State<Boolean> {
    val registry = LocalMorphRegistry.current
    return androidx.compose.runtime.remember(registry, key) {
        androidx.compose.runtime.derivedStateOf { registry?.covered?.containsKey(key) == true }
    }
}
