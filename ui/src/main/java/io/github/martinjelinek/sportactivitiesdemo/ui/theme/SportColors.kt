package io.github.martinjelinek.sportactivitiesdemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportType

@Composable
fun SportType.containerColor(): Color {
    val dark = isSystemInDarkTheme()
    return when (this) {
        SportType.RUN -> if (dark) RunDark else RunLight
        SportType.BIKE -> if (dark) BikeDark else BikeLight
        SportType.SWIM -> if (dark) SwimDark else SwimLight
    }
}

// Black reads AA against every variant in the sport palette; light-mode saturated
// tones support it strongly, dark-mode pastels even more so. Picking one content
// color avoids per-sport branching and stays consistent across the screen.
val SportType.onContainerColor: Color
    get() = Color.Black
