package io.github.martinjelinek.sportactivitiesdemo.ui.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class NavExtensionsTest {

    @Test
    fun `deliverResultAndGoBack writes to previous savedStateHandle and pops`() {
        val handle = SavedStateHandle()
        val previous: NavBackStackEntry = mockk { every { savedStateHandle } returns handle }
        val nav: NavController = mockk(relaxed = true) {
            every { previousBackStackEntry } returns previous
        }

        nav.deliverResultAndGoBack(ANY_KEY, ANY_VALUE)

        assertThat(handle.get<String>(ANY_KEY)).isEqualTo(ANY_VALUE)
        verify { nav.popBackStack() }
    }

    @Test
    fun `deliverResultAndGoBack still pops when there is no previous entry`() {
        val nav: NavController = mockk(relaxed = true) {
            every { previousBackStackEntry } returns null
        }

        nav.deliverResultAndGoBack(ANY_KEY, ANY_VALUE)

        verify { nav.popBackStack() }
    }

    private companion object {
        // Neutral fixtures — the helper is generic over (key, value), so the test
        // shouldn't pretend to know real application keys.
        const val ANY_KEY = "any_key"
        const val ANY_VALUE = "any_value"
    }
}
