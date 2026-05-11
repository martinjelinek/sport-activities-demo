package io.github.martinjelinek.sportactivitiesdemo.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportType
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.github.martinjelinek.sportactivitiesdemo.ui.R
import io.github.martinjelinek.sportactivitiesdemo.ui.components.FilterChips
import io.github.martinjelinek.sportactivitiesdemo.ui.components.StorageTypeChip
import io.github.martinjelinek.sportactivitiesdemo.util.formatDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Top-level (not `remember` in the default param) so the slot lives in ListScreen, not the caller
private val EmptySavedToSignal: StateFlow<String?> = MutableStateFlow(null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    savedToSignal: StateFlow<String?> = EmptySavedToSignal,
    onSignalConsumed: () -> Unit = {},
    viewModel: ListScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val signal by savedToSignal.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedLocalLabel = stringResource(R.string.storage_local)
    val savedRemoteLabel = stringResource(R.string.storage_remote)
    val snackbarTemplate = stringResource(R.string.list_snackbar_saved)

    // Hoist the filter callback so FilterChips sees a stable function reference
    // across recompositions and can be skipped when its inputs are unchanged.
    val onFilterSelected = remember(viewModel) {
        { filter: StorageType? -> viewModel.onEvent(ListScreenEvent.FilterSelected(filter)) }
    }

    LaunchedEffect(signal) {
        signal?.let { name ->
            val label = when (name) {
                StorageType.LOCAL.name -> savedLocalLabel
                StorageType.REMOTE.name -> savedRemoteLabel
                else -> name
            }
            snackbarHostState.showSnackbar(snackbarTemplate.format(label))
            onSignalConsumed()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.list_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.list_fab_add_description),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            FilterChips(
                selected = state.filter,
                onSelect = onFilterSelected,
                modifier = Modifier.padding(16.dp),
            )
            // Local capture so smart-cast applies in the `errorMessage != null`
            // arm — and so we don't need `!!` on the property read.
            val errorMessage = state.errorMessage
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                errorMessage != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
                state.items.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(stringResource(R.string.list_empty_state))
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.items, key = { it.id }) { item -> ActivityRow(item) }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(item: SportActivity) {
    val displayNameRes = remember(item.name) {
        runCatching { SportType.valueOf(item.name) }.getOrNull()?.displayNameRes()
    }
    val displayName = displayNameRes?.let { stringResource(it) } ?: item.name
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(displayName, style = MaterialTheme.typography.titleMedium)
                StorageTypeChip(item.storage)
            }
            Text(item.durationMillis.formatDuration(), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@StringRes
private fun SportType.displayNameRes(): Int = when (this) {
    SportType.RUN -> R.string.activity_run
    SportType.BIKE -> R.string.activity_bike
    SportType.SWIM -> R.string.activity_swim
}
