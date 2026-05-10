package io.github.martinjelinek.sportactivitiesdemo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.github.martinjelinek.sportactivitiesdemo.ui.R

@Composable
fun FilterChips(
    selected: StorageType?,
    onSelect: (StorageType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf<StorageType?>(null, StorageType.LOCAL, StorageType.REMOTE).forEach { f ->
            FilterChip(
                selected = f == selected,
                onClick = { onSelect(f) },
                label = { Text(labelFor(f)) },
            )
        }
    }
}

@Composable
private fun labelFor(filter: StorageType?): String = when (filter) {
    null -> stringResource(R.string.filter_all)
    StorageType.LOCAL -> stringResource(R.string.storage_local)
    StorageType.REMOTE -> stringResource(R.string.storage_remote)
}
