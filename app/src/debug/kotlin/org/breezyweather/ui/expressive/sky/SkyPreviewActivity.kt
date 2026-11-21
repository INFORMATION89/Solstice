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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme

/**
 * Debug-only: swipe through the weathers and drag the slider through a whole day (sunrise 7:00,
 * sunset 18:30) to judge colours, golden hour, twilight, night and the sun/moon position.
 * adb shell am start -n io.github.information89.solstice.debug/org.breezyweather.ui.expressive.sky.SkyPreviewActivity
 * Optional extras: --ei page N (weather) --ef hour H (0–24) --ez static true (no drift).
 */
class SkyPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val weathers = SkyWeather.entries
        val startPage = intent.getIntExtra("page", 0).coerceIn(0, weathers.lastIndex)
        val startHour = intent.getFloatExtra("hour", 12f).coerceIn(0f, 24f)
        val animated = !intent.getBooleanExtra("static", false)
        setContent {
            BreezyWeatherTheme {
                var hour by remember { mutableFloatStateOf(startHour) }
                val pager = rememberPagerState(initialPage = startPage) { weathers.size }
                val sunrise = (7 * HOUR).toLong()
                val sunset = (18.5f * HOUR).toLong()
                val now = (hour * HOUR).toLong()
                HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                    val weather = weathers[page]
                    val sky = skyAt(weather, now, sunrise, sunset)
                    val skyModifier = Modifier.fillMaxSize()
                    val body: @androidx.compose.runtime.Composable () -> Unit = {
                        Column(
                            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Mexico City", color = sky.palette.text, style = MaterialTheme.typography.titleLarge)
                            Text("21°", color = sky.palette.text, fontSize = 120.sp, fontWeight = FontWeight.Light)
                            Text(
                                weather.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                                color = sky.palette.text,
                                style = MaterialTheme.typography.titleLargeEmphasized
                            )
                            Text(
                                "%02d:%02d".format(hour.toInt() % 24, ((hour % 1) * 60).toInt()),
                                color = sky.palette.text.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    if (animated) {
                        AnimatedSkyBackground(sky, skyModifier, content = body)
                    } else {
                        SkyBackground(sky, skyModifier, content = body)
                    }
                }
                val current = skyAt(weathers[pager.currentPage], now, sunrise, sunset).palette
                SkySystemBars(current)
                val text = current.text
                Box(
                    Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Slider(
                        value = hour,
                        onValueChange = { hour = it },
                        valueRange = 0f..24f,
                        colors = SliderDefaults.colors(
                            thumbColor = text,
                            activeTrackColor = text.copy(alpha = 0.6f),
                            inactiveTrackColor = text.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    private companion object {
        const val HOUR = 60f * 60 * 1000
    }
}
