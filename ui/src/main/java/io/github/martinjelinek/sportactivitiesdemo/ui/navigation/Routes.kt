package io.github.martinjelinek.sportactivitiesdemo.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object List : Route
    @Serializable data object Add : Route
}
