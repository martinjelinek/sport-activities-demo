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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportType
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.github.martinjelinek.sportactivitiesdemo.ui.R
import io.github.martinjelinek.sportactivitiesdemo.ui.components.FilterChips
import io.github.martinjelinek.sportactivitiesdemo.ui.components.StorageTypeChip
import io.github.martinjelinek.sportactivitiesdemo.ui.theme.SportActivitiesDemoTheme
import io.github.martinjelinek.sportactivitiesdemo.ui.theme.containerColor
import io.github.martinjelinek.sportactivitiesdemo.ui.theme.onContainerColor
import io.github.martinjelinek.sportactivitiesdemo.util.formatDuration
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onAddClick: () -> Unit,
    savedToSignal: StateFlow<String?>,
    modifier: Modifier = Modifier,
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
    val sportType = remember(item.name) {
        SportType.entries.firstOrNull { it.name == item.name }
    }
    val displayNameRes = remember(sportType) { sportType?.displayNameRes() }
    val displayName = displayNameRes?.let { stringResource(it) } ?: item.name
    val cardContainer = sportType?.containerColor() ?: MaterialTheme.colorScheme.surfaceVariant
    val cardContent = sportType?.onContainerColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardContainer, contentColor = cardContent),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(displayName, style = MaterialTheme.typography.titleMedium)
                StorageTypeChip(item.storage)
            }
            if (item.location.isNotBlank()) {
                Text(item.location, style = MaterialTheme.typography.bodySmall)
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

private fun previewActivity(
    id: String,
    name: String,
    location: String,
    durationMinutes: Int,
    storage: StorageType,
) = SportActivity(
    id = id,
    name = name,
    location = location,
    startedAt = 0L,
    endedAt = durationMinutes * 60L * 1000L,
    storage = storage,
    createdAt = 0L,
)

@PreviewLightDark
@Composable
private fun ActivityRowPreviewRun() {
    SportActivitiesDemoTheme {
        Surface(Modifier.padding(16.dp)) {
            ActivityRow(previewActivity("1", "RUN", "Stromovka", 32, StorageType.LOCAL))
        }
    }
}

@PreviewLightDark
@Composable
private fun ActivityRowPreviewBikeNoLocation() {
    SportActivitiesDemoTheme {
        Surface(Modifier.padding(16.dp)) {
            ActivityRow(previewActivity("2", "BIKE", "", 60, StorageType.REMOTE))
        }
    }
}

@PreviewLightDark
@Composable
private fun ActivityRowPreviewSwim() {
    SportActivitiesDemoTheme {
        Surface(Modifier.padding(16.dp)) {
            ActivityRow(previewActivity("3", "SWIM", "Hotel pool", 45, StorageType.LOCAL))
        }
    }
}

@PreviewLightDark
@Composable
private fun ActivityRowPreviewLegacyName() {
    // Legacy row written before commit 2074890: `name` holds the old display
    // string instead of the enum identifier, so SportType lookup falls back
    // to default Card color and the raw name renders as the title.
    SportActivitiesDemoTheme {
        Surface(Modifier.padding(16.dp)) {
            ActivityRow(previewActivity("4", "Run", "", 30, StorageType.LOCAL))
        }
    }
}
