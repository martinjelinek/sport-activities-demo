package io.github.martinjelinek.sportactivitiesdemo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.repository.SportActivityRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class ListScreenViewModel @Inject constructor(
    private val repository: SportActivityRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ListScreenUiState())
    val state: StateFlow<ListScreenUiState> = _state.asStateFlow()

    init {
        // Single source of truth for isLoading / items / errorMessage
        _state.map { it.filter }
            .distinctUntilChanged()
            .flatMapLatest { filter ->
                repository.observe(filter)
                    .map<List<SportActivity>, LoadResult> { LoadResult.Items(it) }
                    .onStart { emit(LoadResult.Loading) }
                    .catch { e -> emit(LoadResult.Error(e.message)) }
            }
            .onEach { result ->
                _state.update {
                    when (result) {
                        LoadResult.Loading -> it.copy(isLoading = true, errorMessage = null)
                        is LoadResult.Items -> it.copy(items = result.items, isLoading = false, errorMessage = null)
                        is LoadResult.Error -> it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ListScreenEvent) {
        when (event) {
            // Same filter → equal state copy → StateFlow skips the emission →
            // upstream of flatMapLatest sees no new value → natural no-op.
            is ListScreenEvent.FilterSelected -> _state.update { it.copy(filter = event.filter) }
        }
    }

    private sealed interface LoadResult {
        data object Loading : LoadResult
        data class Items(val items: List<SportActivity>) : LoadResult
        data class Error(val message: String?) : LoadResult
    }
}
