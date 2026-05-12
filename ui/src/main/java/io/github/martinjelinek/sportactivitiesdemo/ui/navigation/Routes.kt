package io.github.martinjelinek.sportactivitiesdemo.ui.navigation

import kotlinx.serialization.Serializable

internal sealed interface Route {
    @Serializable data object List : Route
    @Serializable data object Add : Route
}
