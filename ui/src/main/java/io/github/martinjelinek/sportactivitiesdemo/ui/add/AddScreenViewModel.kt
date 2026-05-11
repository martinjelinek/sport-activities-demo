package io.github.martinjelinek.sportactivitiesdemo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.martinjelinek.sportactivitiesdemo.domain.IdGenerator
import io.github.martinjelinek.sportactivitiesdemo.domain.location.LocationProvider
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
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddScreenUiState(
            startedAt = clock.millis(),
            endedAt = clock.millis() + DEFAULT_DURATION_MS,
        ),
    )
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
            AddScreenEvent.RefreshLocation -> refreshLocation()
            AddScreenEvent.Save -> save()
        }
    }

    private fun refreshLocation() {
        _state.update { it.copy(isResolvingLocation = true, hasLocationError = false) }
        viewModelScope.launch {
            val coords = locationProvider.currentCoordinates()
            if (coords == null) {
                _state.update {
                    it.copy(
                        isResolvingLocation = false,
                        hasLocationError = true,
                    )
                }
                return@launch
            }
            val resolved = locationProvider.getLocationDescription(coords)
            _state.update {
                // Don't clobber a manual edit — the coordinates still update so the
                val nextLocation = if (it.location.isBlank() && !resolved.isNullOrBlank()) resolved else it.location
                it.copy(
                    coordinates = coords,
                    location = nextLocation,
                    isResolvingLocation = false,
                    hasLocationError = false,
                )
            }
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

    private companion object {
        const val DEFAULT_DURATION_MS = 30L * 60L * 1000L
    }
}
