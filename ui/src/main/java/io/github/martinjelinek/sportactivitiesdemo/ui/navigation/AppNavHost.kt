package io.github.martinjelinek.sportactivitiesdemo.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Route.List) {
        composable<Route.List> {
            Text("List placeholder")
        }
        composable<Route.Add> {
            Text("Add placeholder")
        }
    }
}
