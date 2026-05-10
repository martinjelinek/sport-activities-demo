package io.github.martinjelinek.sportactivitiesdemo.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import io.github.martinjelinek.sportactivitiesdemo.domain.repository.SportActivityRepository
import io.github.martinjelinek.sportactivitiesdemo.ui.add.AddScreen
import io.github.martinjelinek.sportactivitiesdemo.ui.add.AddScreenEvent
import io.github.martinjelinek.sportactivitiesdemo.ui.add.AddScreenTestTags
import io.github.martinjelinek.sportactivitiesdemo.ui.add.AddScreenViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

// Driving the actual Material3 DatePickerDialog + TimePicker dialog from a
// Compose UI test is brittle — the dialogs are window-level and the picker
// nodes don't expose stable test tags. We exercise the form-validation
// contract by feeding the timestamps through the ViewModel directly; the
// picker dialog wiring itself is covered by the manual smoke test in the
// plan's Task 15 Step 4.
//
// Nodes are matched by testTag (not displayed text) so the test stays
// locale-safe and survives copy edits.
class AddScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun save_button_disabled_until_form_valid_then_enabled() {
        val repo: SportActivityRepository = mockk(relaxed = true)
        coEvery { repo.save(any()) } returns Result.success(Unit)
        val vm = AddScreenViewModel(repo)

        rule.setContent {
            AddScreen(onSaved = {}, onNavigateBack = {}, viewModel = vm)
        }

        rule.onNodeWithTag(AddScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()

        rule.onNodeWithTag(AddScreenTestTags.NAME_FIELD).performTextInput("Run")
        rule.onNodeWithTag(AddScreenTestTags.LOCATION_FIELD).performTextInput("Park")

        // Bypass the date+time picker dialogs (window-level, hard to drive
        // from a Compose test) by pushing the timestamps in via the VM.
        vm.onEvent(AddScreenEvent.StartedAtChanged(0L))
        vm.onEvent(AddScreenEvent.EndedAtChanged(60_000L))

        rule.onNodeWithTag(AddScreenTestTags.SAVE_BUTTON).assertIsEnabled()
    }
}
