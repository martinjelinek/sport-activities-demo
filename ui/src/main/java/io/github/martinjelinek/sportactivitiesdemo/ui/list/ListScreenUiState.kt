package io.github.martinjelinek.sportactivitiesdemo.ui.list

import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType

data class ListScreenUiState(
    val filter: StorageType? = null,
    val items: List<SportActivity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface ListScreenEvent {
    data class FilterSelected(val filter: StorageType?) : ListScreenEvent
}
