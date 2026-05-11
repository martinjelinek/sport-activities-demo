package io.github.martinjelinek.sportactivitiesdemo.ui.add

import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportType
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType

data class AddScreenUiState(
    val sport: SportType? = null,
    val location: String = "",
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val storage: StorageType = StorageType.LOCAL,
    val isSubmitting: Boolean = false, // prevents double submit
    val errorMessage: String? = null,
) {

    // Just a derivation, not business logic. Keep in the state for easy access.
    val isSavable: Boolean
        get() = sport != null && endedAt > startedAt && !isSubmitting
}

sealed interface AddScreenEvent {
    data class SportSelected(val value: SportType) : AddScreenEvent
    data class LocationChanged(val value: String) : AddScreenEvent
    data class StartedAtChanged(val value: Long) : AddScreenEvent
    data class EndedAtChanged(val value: Long) : AddScreenEvent
    data class StorageChanged(val value: StorageType) : AddScreenEvent
    data object Save : AddScreenEvent
}

/**
 * One-shot side effects the screen reacts to but that don't belong in [AddScreenUiState].
 * Delivered through a [Channel] so each event is consumed
 * exactly once and survives configuration changes.
 */
sealed interface AddScreenEffect {
    data class Saved(val storage: StorageType) : AddScreenEffect
}
