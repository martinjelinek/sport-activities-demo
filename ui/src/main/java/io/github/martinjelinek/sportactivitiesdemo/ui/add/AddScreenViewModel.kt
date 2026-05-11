package io.github.martinjelinek.sportactivitiesdemo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.martinjelinek.sportactivitiesdemo.domain.IdGenerator
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.repository.SportActivityRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddScreenViewModel @Inject constructor(
    private val repository: SportActivityRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : ViewModel() {

    private val _state = MutableStateFlow(AddScreenUiState())
    val state: StateFlow<AddScreenUiState> = _state.asStateFlow()

    // One-shot effects. BUFFERED so send() doesn't suspend and pending items survive
    // a collector swap (config change, lifecycle stop).
    private val _effects = Channel<AddScreenEffect>(Channel.BUFFERED)
    val effects: Flow<AddScreenEffect> = _effects.receiveAsFlow()

    fun onEvent(event: AddScreenEvent) {
        when (event) {
            is AddScreenEvent.NameChanged -> _state.update { it.copy(name = event.value) }
            is AddScreenEvent.LocationChanged -> _state.update { it.copy(location = event.value) }
            is AddScreenEvent.StartedAtChanged -> _state.update { it.copy(startedAt = event.value) }
            is AddScreenEvent.EndedAtChanged -> _state.update { it.copy(endedAt = event.value) }
            is AddScreenEvent.StorageChanged -> _state.update { it.copy(storage = event.value) }
            AddScreenEvent.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value
        if (!s.isSavable) return
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val sportActivity = SportActivity(
                id = idGenerator.next(),
                name = s.name.trim(),
                location = s.location.trim(),
                startedAt = s.startedAt,
                endedAt = s.endedAt,
                storage = s.storage,
                createdAt = clock.millis(),
            )
            repository.save(sportActivity)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    _effects.send(AddScreenEffect.Saved(s.storage))
                }
                .onFailure { e -> _state.update { it.copy(isSubmitting = false, errorMessage = e.message) } }
        }
    }
}
