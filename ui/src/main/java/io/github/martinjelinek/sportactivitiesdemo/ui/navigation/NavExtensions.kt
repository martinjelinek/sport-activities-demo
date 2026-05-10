package io.github.martinjelinek.sportactivitiesdemo.ui.navigation

import androidx.navigation.NavController

/**
 * Write a result onto the previous back-stack entry's [androidx.lifecycle.SavedStateHandle]
 * and pop the current destination.
 *
 * The previous screen reads the value via `savedStateHandle.getStateFlow(key, null)`.
 */
fun <T : Any> NavController.deliverResultAndGoBack(key: String, value: T) {
    previousBackStackEntry?.savedStateHandle?.set(key, value)
    popBackStack()
}
