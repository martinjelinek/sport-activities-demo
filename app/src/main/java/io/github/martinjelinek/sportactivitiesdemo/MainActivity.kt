package io.github.martinjelinek.sportactivitiesdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.martinjelinek.sportactivitiesdemo.ui.navigation.AppNavHost
import io.github.martinjelinek.sportactivitiesdemo.ui.theme.SportActivitiesDemoTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportActivitiesDemoTheme {
                AppNavHost()
            }
        }
    }
}
