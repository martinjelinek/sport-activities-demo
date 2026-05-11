package io.github.martinjelinek.sportactivitiesdemo.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import io.github.martinjelinek.sportactivitiesdemo.domain.IdGenerator
import io.github.martinjelinek.sportactivitiesdemo.domain.location.LocationProvider
import io.github.martinjelinek.sportactivitiesdemo.domain.repository.SportActivityRepository
import io.github.martinjelinek.sportactivitiesdemo.ui.add.AddScreen
import io.github.martinjelinek.sportactivitiesdemo.ui.add.AddScreenTestTags
import io.github.martinjelinek.sportactivitiesdemo.ui.add.AddScreenViewModel
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Clock
import org.junit.Rule
import org.junit.Test

// The Material3 DatePicker + TimePicker dialogs are window-level and don't
// expose stable test tags, so we exercise the form-validation contract by
// driving only the text fields here. With Task 7's defaults the start / end
// timestamps are already valid on first composition, so no VM bypass is
// needed any more.
class AddScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun save_button_disabled_until_name_and_location_filled() {
        val repo: SportActivityRepository = mockk(relaxed = true)
        coEvery { repo.save(any()) } returns Result.success(Unit)
        val locationProvider: LocationProvider = mockk(relaxed = true)
        val vm = AddScreenViewModel(
            repository = repo,
            clock = Clock.systemDefaultZone(),
            idGenerator = IdGenerator { "test-id" },
            locationProvider = locationProvider,
        )

        rule.setContent {
            AddScreen(onSaved = {}, onNavigateBack = {}, viewModel = vm)
        }

        rule.onNodeWithTag(AddScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()

        rule.onNodeWithTag(AddScreenTestTags.NAME_FIELD).performTextInput("Run")
        rule.onNodeWithTag(AddScreenTestTags.LOCATION_FIELD).performTextInput("Park")

        rule.onNodeWithTag(AddScreenTestTags.SAVE_BUTTON).assertIsEnabled()
    }
}
