package io.github.martinjelinek.sportactivitiesdemo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
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

// All filter options the user can pick (null = "All"). Lifted to file scope
// so the list isn't re-allocated on every recomposition of FilterChips.
private val FILTER_OPTIONS: List<StorageType?> = listOf(null, StorageType.LOCAL, StorageType.REMOTE)

@Composable
fun FilterChips(
    selected: StorageType?,
    onSelect: (StorageType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FILTER_OPTIONS.forEach { f ->
            FilterChip(
                selected = f == selected,
                onClick = { onSelect(f) },
                label = { Text(stringResource(f.labelRes())) },
            )
        }
    }
}

// Plain (non-`@Composable`) helper returning a `@StringRes Int` so the
// `stringResource(...)` call stays at the use site. A user-defined `@Composable`
// helper returning `String` would be non-restartable and re-execute for every
// chip on every recomposition of FilterChips.
@StringRes
private fun StorageType?.labelRes(): Int = when (this) {
    null -> R.string.filter_all
    StorageType.LOCAL -> R.string.storage_local
    StorageType.REMOTE -> R.string.storage_remote
}

@PreviewLightDark
@Composable
private fun FilterChipsPreviewAll() {
    SportActivitiesDemoTheme {
        Surface(Modifier.padding(16.dp)) {
            FilterChips(selected = null, onSelect = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun FilterChipsPreviewLocal() {
    SportActivitiesDemoTheme {
        Surface(Modifier.padding(16.dp)) {
            FilterChips(selected = StorageType.LOCAL, onSelect = {})
        }
    }
}
