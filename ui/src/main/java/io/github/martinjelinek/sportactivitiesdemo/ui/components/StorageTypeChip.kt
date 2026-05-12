package io.github.martinjelinek.sportactivitiesdemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.github.martinjelinek.sportactivitiesdemo.ui.R
import io.github.martinjelinek.sportactivitiesdemo.ui.theme.SportActivitiesDemoTheme

@Composable
internal fun StorageTypeChip(type: StorageType, modifier: Modifier = Modifier) {
    val (bg, fg, label) = when (type) {
        StorageType.LOCAL -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            stringResource(R.string.storage_local),
        )
        StorageType.REMOTE -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            stringResource(R.string.storage_remote),
        )
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@PreviewLightDark
@Composable
private fun StorageTypeChipPreviewLocal() {
    SportActivitiesDemoTheme {
        Surface(Modifier.padding(16.dp)) {
            StorageTypeChip(StorageType.LOCAL)
        }
    }
}

@PreviewLightDark
@Composable
private fun StorageTypeChipPreviewRemote() {
    SportActivitiesDemoTheme {
        Surface(Modifier.padding(16.dp)) {
            StorageTypeChip(StorageType.REMOTE)
        }
    }
}
