package io.github.martinjelinek.sportactivitiesdemo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class ListScreenViewModel @Inject constructor(
    private val repository: SportActivityRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ListScreenUiState())
    val state: StateFlow<ListScreenUiState> = _state.asStateFlow()

    init {
        _state.map { it.filter }
            .distinctUntilChanged()
            .flatMapLatest { repository.observe(it) }
            .onEach { items ->
                _state.update { it.copy(items = items, isLoading = false, errorMessage = null) }
            }
            .catch { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ListScreenEvent) {
        when (event) {
            is ListScreenEvent.FilterSelected -> _state.update { it.copy(filter = event.filter, isLoading = true) }
        }
    }
}
