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

package org.breezyweather.ui.expressive.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Design tokens for ui/expressive. Use these instead of typing values by hand, so the same thing
 * looks the same everywhere. Anything new becomes a token first. Motion tokens live in
 * components/SolsticeMotion.kt (moving to the :motion module).
 */

/**
 * Spacing on a 4 dp grid: padding, gaps, offsets. `S1` = 4 dp, `S2` = 8 dp, … `Half` (2 dp) only
 * for optical nudges (a label sitting 2 dp lower), never for layout.
 */
object Space {
    val Half = 2.dp
    val S1 = 4.dp
    val S2 = 8.dp
    val S3 = 12.dp
    val S4 = 16.dp
    val S5 = 20.dp
    val S6 = 24.dp
    val S7 = 28.dp
    val S8 = 32.dp
    val S10 = 40.dp
    val S12 = 48.dp
}

/** Named sizes. Charts and one-off layout dimensions stay where they're used. */
object Sizes {
    /** Smallest touch target (Material and Android accessibility). */
    val TouchTarget = 48.dp
    val IconXSmall = 12.dp
    val IconSmall = 16.dp
    val IconMedium = 18.dp
    val Icon = 20.dp
    val IconLarge = 24.dp
    val IconXLarge = 28.dp
    val IconHero = 32.dp

    /** A sheet's drag handle (search, moon card, postcard window): Material 3's 32 × 4 dp. */
    val HandleWidth = 32.dp
    val HandleThickness = 4.dp

    /** The room a sheet leaves at its top for the handle, the bar centred in it (its touch target reaches below). */
    val HandleStrip = Space.S6
}

/**
 * How strongly ink (text and icons) shows over its background (agreed 2026-11-21): Material's
 * documented text emphasis levels, applied to whatever ink the page uses (the sky's text, the page's).
 */
object Emphasis {
    /** Main text. */
    val High = 0.87f

    /** Secondary text: labels, captions, supporting lines. */
    val Medium = 0.60f

    /** Disabled or placeholder. */
    val Disabled = 0.38f
}

/** Opacities for specific parts. */
object Opacity {
    /**
     * Solstice's one glass (agreed 2026-11-21: one level for every surface): the theme's surface colour
     * at 40 % over the blurred backdrop.
     */
    val Glass = 0.40f

    /** Material 3's drag handle: the ink at 40 %. */
    val Handle = 0.4f
}

/** Line widths. */
object StrokeWidth {
    val Hairline = 0.5.dp
    val Thin = 1.dp
    val Regular = 1.5.dp
}

/** Shadow heights: a card lying on the page, a selected one, one lifted while it moves. */
object Elevation {
    val Card = 6.dp
    val CardSelected = 8.dp
    val Lifted = 10.dp
}

/**
 * Corner radii on the Material 3 Expressive scale. Concentric rule: an element inside a rounded
 * container with padding `p` uses `outer - p` (never less than [Corners.XS]).
 */
object Corners {
    val XXS = 4.dp
    val XS = 8.dp
    val S = 12.dp
    val M = 20.dp
    val L = 24.dp
    val LIncreased = 28.dp
    val XL = 32.dp
    val XLIncreased = 36.dp
    val XXL = 48.dp
}

/** Shapes for the radii above (the same ones `MaterialTheme.shapes` gets). */
object CornerShapes {
    val XXS = RoundedCornerShape(Corners.XXS)
    val XS = RoundedCornerShape(Corners.XS)
    val S = RoundedCornerShape(Corners.S)
    val M = RoundedCornerShape(Corners.M)
    val L = RoundedCornerShape(Corners.L)
    val LIncreased = RoundedCornerShape(Corners.LIncreased)
    val XL = RoundedCornerShape(Corners.XL)
    val XLIncreased = RoundedCornerShape(Corners.XLIncreased)
    val XXL = RoundedCornerShape(Corners.XXL)
}

/**
 * Colours that don't come from Material You (paper, ink, data colours). Everything else uses the
 * dynamic colour scheme's roles.
 */
object SolsticePalette {
    // Paper and ink (postcard, outlook calendar, places)
    val Paper = Color(0xFFFAF7F0)
    val PaperDim = Color(0xFFE6E0D2)
    val PaperWarm = Color(0xFFF8F2E4)
    val PaperAged = Color(0xFFF6EBD6)
    val PaperSheet = Color(0xFFF7F3EA)
    val PaperNight = Color(0xFF1D1C19)
    val Ink = Color(0xFF2E2A24)
    val InkNearBlack = Color(0xFF1B1B1F)
    val Sepia = Color(0xFF8A6A3A)

    // Marks
    val Stamp = Color(0xFFE5483C)
    val HerePin = Color(0xFFFF5A4E)
    val Wet = Color(0xFFD85A30)

    // Sun and moon
    val Sun = Color(0xFFF6B04A)
    val SunWarm = Color(0xFFF2A33A)
    val MoonCool = Color(0xFF8FA3E8)
    val MoonLight = Color(0xFFF4F1E8)
    val MoonShade = Color(0xFFCFCBC0)
    val MoonCrater = Color(0xFF9E9A90)
    val MoonNightText = Color(0xFFEDEBF5)
    val NightDeep = Color(0xFF05050A)
    val NightDeepest = Color(0xFF050509)
    val TideLine = Color(0xFF6FB7F0)

    // Rain and snow
    val RainSoon = Color(0xFF8CCBFF)
    val RainLight = Color(0xFFB9E1FF)
    val Rain = Color(0xFF4F9EEA)
    val SnowLight = Color(0xFFCBC6FF)
    val Snow = Color(0xFF7C7FE6)

    // Temperature scale (°C stops)
    val TempFreezing = Color(0xFF6C8DE0)
    val TempCold = Color(0xFF85B7EB)
    val TempMild = Color(0xFFB9CFE6)
    val TempWarm = Color(0xFFF0B27B)
    val TempHot = Color(0xFFE4683A)
}

/** Display styles outside the Material type scale. */
object HeroType {
    /** The big temperature on the Forecast page. */
    val Temperature = TextStyle(fontSize = 112.sp, fontWeight = FontWeight.Light, letterSpacing = (-4).sp)

    /** The temperature on a postcard / place card. */
    val CardTemperature = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Light)
}
