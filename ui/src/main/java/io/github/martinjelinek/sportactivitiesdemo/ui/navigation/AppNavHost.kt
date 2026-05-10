package io.github.martinjelinek.sportactivitiesdemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.martinjelinek.sportactivitiesdemo.ui.add.AddScreen
import io.github.martinjelinek.sportactivitiesdemo.ui.list.ListScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Route.List) {
        composable<Route.List> { entry ->
            val savedToFlow = entry.savedStateHandle.getStateFlow<String?>(KEY_SAVED_TO, null)
            ListScreen(
                onAddClick = { navController.navigate(Route.Add) },
                savedToSignal = savedToFlow,
                onSignalConsumed = { entry.savedStateHandle[KEY_SAVED_TO] = null },
            )
        }
        composable<Route.Add> {
            AddScreen(
                onSaved = { storage ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KEY_SAVED_TO, storage.name)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

private const val KEY_SAVED_TO = "saved_to"
