package io.github.martinjelinek.sportactivitiesdemo.ui.add

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportType
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.github.martinjelinek.sportactivitiesdemo.ui.R
import io.github.martinjelinek.sportactivitiesdemo.ui.components.DateTimePickerDialog
import io.github.martinjelinek.sportactivitiesdemo.ui.components.ImageCard
import io.github.martinjelinek.sportactivitiesdemo.ui.components.PickerTarget
import io.github.martinjelinek.sportactivitiesdemo.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddScreen(
    onSaved: (StorageType) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }

    val currentOnSaved by rememberUpdatedState(onSaved)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        // flowWithLifecycle pauses collection below STARTED so onSaved (which triggers
        // navigation) can't fire while the host isn't RESUMED. Buffered effects in the
        // Channel are delivered when collection resumes.
        viewModel.effects.flowWithLifecycle(lifecycle).collect { effect ->
            when (effect) {
                is AddScreenEffect.Saved -> currentOnSaved(effect.storage)
            }
        }
    }

    val onEvent = remember(viewModel) { viewModel::onEvent }
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val onUseMyLocation = remember(locationPermission, onEvent) {
        {
            when {
                locationPermission.status.isGranted ->
                    onEvent(AddScreenEvent.RefreshLocation)
                locationPermission.status.shouldShowRationale ->
                    locationPermission.launchPermissionRequest()
                else -> locationPermission.launchPermissionRequest()
            }
        }
    }
    // Once the user grants the permission, immediately fetch — otherwise the
    // first tap only opens the system prompt and the user has to tap again.
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted && state.coordinates == null && !state.isResolvingLocation) {
            onEvent(AddScreenEvent.RefreshLocation)
        }
    }

    val onStartedClick = remember { { pickerTarget = PickerTarget.Started } }
    val onEndedClick = remember { { pickerTarget = PickerTarget.Ended } }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(state.sport?.titleRes() ?: R.string.add_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.add_back_description),
                        )
                    }
                },
            )
        },
    ) { padding ->
        AddScreenContent(
            state = state,
            onEvent = onEvent,
            onUseMyLocation = onUseMyLocation,
            onStartedClick = onStartedClick,
            onEndedClick = onEndedClick,
            modifier = Modifier.padding(padding),
        )
    }

    pickerTarget?.let { target ->
        val initial = when (target) {
            PickerTarget.Started -> state.startedAt
            PickerTarget.Ended -> state.endedAt
        }

        val onPickerDismiss = remember { { pickerTarget = null } }
        val onPickerConfirm = remember(target, onEvent) {
            { picked: Long ->
                onEvent(
                    when (target) {
                        PickerTarget.Started -> AddScreenEvent.StartedAtChanged(picked)
                        PickerTarget.Ended -> AddScreenEvent.EndedAtChanged(picked)
                    },
                )
                pickerTarget = null
            }
        }
        DateTimePickerDialog(
            initialMillis = initial,
            onConfirm = onPickerConfirm,
            onDismiss = onPickerDismiss,
        )
    }
}

@Composable
private fun AddScreenContent(
    state: AddScreenUiState,
    onEvent: (AddScreenEvent) -> Unit,
    onUseMyLocation: () -> Unit,
    onStartedClick: () -> Unit,
    onEndedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val storageTypes = remember { StorageType.entries }
    val onSportSelect = remember(onEvent) { { sport: SportType -> onEvent(AddScreenEvent.SportSelected(sport)) } }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SportSelector(
            selected = state.sport,
            onSelect = onSportSelect,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.location,
                readOnly = true,
                onValueChange = { onEvent(AddScreenEvent.LocationChanged(it)) },
                label = { Text(stringResource(R.string.add_field_location)) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag(AddScreenTestTags.LOCATION_FIELD),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onUseMyLocation,
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                    .testTag(AddScreenTestTags.USE_MY_LOCATION_BUTTON),
                enabled = !state.isResolvingLocation,
            ) {
                if (state.isResolvingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        trackColor = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = stringResource(R.string.add_use_my_location),
                    )
                }
            }
        }
        if (state.hasLocationError) {
            Text(
                text = stringResource(R.string.add_location_error_unavailable),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        val startedFormatted = remember(state.startedAt) { formatTimestamp(state.startedAt) }
        val endedFormatted = remember(state.endedAt) { formatTimestamp(state.endedAt) }

        OutlinedButton(
            onClick = onStartedClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.startedAt == 0L) stringResource(R.string.add_pick_start)
                else stringResource(R.string.add_started_at, startedFormatted),
            )
        }
        OutlinedButton(
            onClick = onEndedClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.endedAt == 0L) stringResource(R.string.add_pick_end)
                else stringResource(R.string.add_ended_at, endedFormatted),
            )
        }
        if (state.startedAt != 0L && state.endedAt != 0L && state.endedAt <= state.startedAt) {
            Text(
                text = stringResource(R.string.add_end_before_start_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Text(
            text = stringResource(R.string.add_storage_label),
            modifier = Modifier.padding(top = 8.dp),
        )
        storageTypes.forEach { type ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = state.storage == type,
                    onClick = { onEvent(AddScreenEvent.StorageChanged(type)) },
                )
                Text(stringResource(type.labelRes()))
            }
        }

        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = { onEvent(AddScreenEvent.Save) },
            enabled = state.isSavable,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AddScreenTestTags.SAVE_BUTTON),
        ) {
            Text(
                if (state.isSubmitting) stringResource(R.string.add_button_saving)
                else stringResource(R.string.add_button_save),
            )
        }
    }
}

@Composable
private fun SportSelector(
    selected: SportType?,
    onSelect: (SportType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    val onRun = remember(onSelect) { { onSelect(SportType.RUN) } }
    val onBike = remember(onSelect) { { onSelect(SportType.BIKE) } }
    val onSwim = remember(onSelect) { { onSelect(SportType.SWIM) } }
    Row(
        modifier = modifier
            .height(96.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ImageCard(
            selected = selected == SportType.RUN,
            painter = rememberVectorPainter(Icons.AutoMirrored.Filled.DirectionsRun),
            contentDescription = stringResource(R.string.activity_run),
            onClick = onRun,
            modifier = Modifier
                .weight(1f)
                .testTag(AddScreenTestTags.SPORT_CARD_RUN),
            tint = tint,
        )
        ImageCard(
            selected = selected == SportType.BIKE,
            painter = rememberVectorPainter(Icons.AutoMirrored.Filled.DirectionsBike),
            contentDescription = stringResource(R.string.activity_bike),
            onClick = onBike,
            modifier = Modifier
                .weight(1f)
                .testTag(AddScreenTestTags.SPORT_CARD_BIKE),
            tint = tint,
        )
        ImageCard(
            selected = selected == SportType.SWIM,
            painter = rememberVectorPainter(Icons.Filled.Pool),
            contentDescription = stringResource(R.string.activity_swim),
            onClick = onSwim,
            modifier = Modifier
                .weight(1f)
                .testTag(AddScreenTestTags.SPORT_CARD_SWIM),
            tint = tint,
        )
    }
}

@StringRes
private fun StorageType.labelRes(): Int = when (this) {
    StorageType.LOCAL -> R.string.storage_local
    StorageType.REMOTE -> R.string.storage_remote
}

@StringRes
private fun SportType.titleRes(): Int = when (this) {
    SportType.RUN -> R.string.add_title_run
    SportType.BIKE -> R.string.add_title_bike
    SportType.SWIM -> R.string.add_title_swim
}

internal object AddScreenTestTags {
    const val SPORT_CARD_RUN = "add_sport_card_run"
    const val SPORT_CARD_BIKE = "add_sport_card_bike"
    const val SPORT_CARD_SWIM = "add_sport_card_swim"
    const val LOCATION_FIELD = "add_location_field"
    const val USE_MY_LOCATION_BUTTON = "add_use_my_location_button"
    const val SAVE_BUTTON = "add_save_button"
}
