package io.github.martinjelinek.sportactivitiesdemo.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.annotation.StringRes
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.github.martinjelinek.sportactivitiesdemo.ui.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    onSaved: (StorageType) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }
    val storageTypes = remember { StorageType.entries }

    LaunchedEffect(state.savedTo) {
        state.savedTo?.let {
            onSaved(it)
            viewModel.onEvent(AddScreenEvent.ConsumeSavedSignal)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_title)) },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(AddScreenEvent.NameChanged(it)) },
                label = { Text(stringResource(R.string.add_field_name)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AddScreenTestTags.NAME_FIELD),
            )
            OutlinedTextField(
                value = state.location,
                onValueChange = { viewModel.onEvent(AddScreenEvent.LocationChanged(it)) },
                label = { Text(stringResource(R.string.add_field_location)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AddScreenTestTags.LOCATION_FIELD),
            )

            // Format only when the underlying timestamp changes — avoids
            // re-running SimpleDateFormat.format on every recomposition
            val startedFormatted = remember(state.startedAt) { formatTimestamp(state.startedAt) }
            val endedFormatted = remember(state.endedAt) { formatTimestamp(state.endedAt) }

            OutlinedButton(
                onClick = { pickerTarget = PickerTarget.Started },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (state.startedAt == 0L) stringResource(R.string.add_pick_start)
                    else stringResource(R.string.add_started_at, startedFormatted),
                )
            }
            OutlinedButton(
                onClick = { pickerTarget = PickerTarget.Ended },
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
                        onClick = { viewModel.onEvent(AddScreenEvent.StorageChanged(type)) },
                    )
                    Text(stringResource(type.labelRes()))
                }
            }

            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = { viewModel.onEvent(AddScreenEvent.Save) },
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

    pickerTarget?.let { target ->
        val initial = when (target) {
            PickerTarget.Started -> state.startedAt
            PickerTarget.Ended -> state.endedAt
        }
        DateTimePickerDialog(
            initialMillis = initial,
            onConfirm = { picked ->
                when (target) {
                    PickerTarget.Started -> viewModel.onEvent(AddScreenEvent.StartedAtChanged(picked))
                    PickerTarget.Ended -> viewModel.onEvent(AddScreenEvent.EndedAtChanged(picked))
                }
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null },
        )
    }
}

@StringRes
private fun StorageType.labelRes(): Int = when (this) {
    StorageType.LOCAL -> R.string.storage_local
    StorageType.REMOTE -> R.string.storage_remote
}

private enum class PickerTarget { Started, Ended }

private enum class PickerStep { Date, Time }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerDialog(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var step by remember { mutableStateOf(PickerStep.Date) }
    // Capture seed once per initialMillis. Without remember, System.currentTimeMillis()
    // is evaluated bare on every recomposition and can drift under rememberDatePickerState.
    val seed = remember(initialMillis) {
        if (initialMillis != 0L) initialMillis else System.currentTimeMillis()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = seed)
    val seedCal = remember(seed) { Calendar.getInstance().apply { timeInMillis = seed } }
    val timePickerState = rememberTimePickerState(
        initialHour = seedCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = seedCal.get(Calendar.MINUTE),
        is24Hour = true,
    )

    when (step) {
        PickerStep.Date -> DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = { step = PickerStep.Time },
                    enabled = datePickerState.selectedDateMillis != null,
                ) { Text(stringResource(R.string.add_picker_next)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.add_picker_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }

        PickerStep.Time -> Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.add_picker_time_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.add_picker_cancel))
                        }
                        TextButton(onClick = {
                            val dateUtc = datePickerState.selectedDateMillis ?: return@TextButton
                            onConfirm(combineDateAndTime(dateUtc, timePickerState.hour, timePickerState.minute))
                        }) { Text(stringResource(R.string.add_picker_ok)) }
                    }
                }
            }
        }
    }
}

// DatePicker returns the selected day as UTC midnight; we extract the date
// fields in UTC, then rebuild the full timestamp in the device's local zone.
private fun combineDateAndTime(dateUtcMillis: Long, hour: Int, minute: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateUtcMillis }
    return Calendar.getInstance().apply {
        set(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
            0,
        )
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private val TIMESTAMP_FORMATTER: SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

private fun formatTimestamp(epochMillis: Long): String =
    TIMESTAMP_FORMATTER.format(Date(epochMillis))

internal object AddScreenTestTags {
    const val NAME_FIELD = "add_name_field"
    const val LOCATION_FIELD = "add_location_field"
    const val SAVE_BUTTON = "add_save_button"
}
