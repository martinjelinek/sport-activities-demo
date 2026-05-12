package io.github.martinjelinek.sportactivitiesdemo.ui.list

import androidx.compose.runtime.Stable
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType

/**
 * Compose can't infer stability for [items] (a plain `kotlin.collections.List`),
 * so it would conservatively mark the whole UiState — and any composable that
 * reads it — as unstable, defeating skippability. The ViewModel only ever
 * emits new immutable lists from the repository, never mutates one in place,
 * so claiming stability via [Stable] is safe.
 */
@Stable
internal data class ListScreenUiState(
    val filter: StorageType? = null,
    val items: List<SportActivity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

internal sealed interface ListScreenEvent {
    data class FilterSelected(val filter: StorageType?) : ListScreenEvent
}
