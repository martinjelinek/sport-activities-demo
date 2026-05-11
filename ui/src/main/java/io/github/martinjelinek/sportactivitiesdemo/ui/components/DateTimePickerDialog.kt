package io.github.martinjelinek.sportactivitiesdemo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.martinjelinek.sportactivitiesdemo.ui.R
import java.util.Calendar
import java.util.TimeZone

enum class PickerTarget { Started, Ended }

enum class PickerStep { Date, Time }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableStateOf(PickerStep.Date) }

    // Keeps the picker init value stable across recomposition
    val seed = remember(initialMillis) {
        if (initialMillis != 0L) initialMillis else System.currentTimeMillis()
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = seed,
        initialDisplayMode = androidx.compose.material3.DisplayMode.Input,
    )
    val seedCal = remember(seed) { Calendar.getInstance().apply { timeInMillis = seed } }
    val timePickerState = rememberTimePickerState(
        initialHour = seedCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = seedCal.get(Calendar.MINUTE),
        is24Hour = true,
    )

    when (step) {
        PickerStep.Date -> DatePickerDialog(
            onDismissRequest = onDismiss,
            modifier = modifier,
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
            DatePicker(
                state = datePickerState,
                showModeToggle = true,
            )
        }

        PickerStep.Time -> Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = modifier,
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
