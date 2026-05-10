package io.github.martinjelinek.sportactivitiesdemo.ui.add

import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType

data class AddScreenUiState(
    val name: String = "",
    val location: String = "",
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val storage: StorageType = StorageType.LOCAL,
    val isSubmitting: Boolean = false, // prevents double submit
    /**
     * One-shot "save succeeded" signal expressed as state.
     *
     * `null` = no save completed yet, or the previous success has been consumed.
     * Non-null = the storage destination the activity was just persisted to.
     *
     * Typed as [StorageType] (not [Boolean]) because the value is passed back to
     * the List screen via savedStateHandle so it can auto-select the matching
     * filter chip — the user lands on a list pre-filtered to where their new
     * activity lives. The screen consumes the signal via
     * [AddScreenEvent.ConsumeSavedSignal] to reset to `null` and prevent the
     * navigation effect from re-firing on recomposition.
     */
    val savedTo: StorageType? = null,
    val errorMessage: String? = null,
) {
    val isSavable: Boolean
        get() = name.isNotBlank() && location.isNotBlank() && endedAt > startedAt && !isSubmitting
}

sealed interface AddScreenEvent {
    data class NameChanged(val value: String) : AddScreenEvent
    data class LocationChanged(val value: String) : AddScreenEvent
    data class StartedAtChanged(val value: Long) : AddScreenEvent
    data class EndedAtChanged(val value: Long) : AddScreenEvent
    data class StorageChanged(val value: StorageType) : AddScreenEvent
    data object Save : AddScreenEvent
    data object ConsumeSavedSignal : AddScreenEvent
}
