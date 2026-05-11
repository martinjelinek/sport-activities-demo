package io.github.martinjelinek.sportactivitiesdemo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.repository.SportActivityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddScreenViewModel @Inject constructor(
    private val repository: SportActivityRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddScreenUiState())
    val state: StateFlow<AddScreenUiState> = _state.asStateFlow()

    fun onEvent(event: AddScreenEvent) {
        when (event) {
            is AddScreenEvent.NameChanged -> _state.update { it.copy(name = event.value) }
            is AddScreenEvent.LocationChanged -> _state.update { it.copy(location = event.value) }
            is AddScreenEvent.StartedAtChanged -> _state.update { it.copy(startedAt = event.value) }
            is AddScreenEvent.EndedAtChanged -> _state.update { it.copy(endedAt = event.value) }
            is AddScreenEvent.StorageChanged -> _state.update { it.copy(storage = event.value) }
            AddScreenEvent.ConsumeSavedSignal -> _state.update { it.copy(savedTo = null, errorMessage = null) }
            AddScreenEvent.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value
        if (!s.isSavable) return
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val sportActivity = SportActivity(
                name = s.name.trim(),
                location = s.location.trim(),
                startedAt = s.startedAt,
                endedAt = s.endedAt,
                storage = s.storage,
            )
            repository.save(sportActivity)
                .onSuccess { _state.update { it.copy(isSubmitting = false, savedTo = s.storage) } }
                .onFailure { e -> _state.update { it.copy(isSubmitting = false, errorMessage = e.message) } }
        }
    }
}
