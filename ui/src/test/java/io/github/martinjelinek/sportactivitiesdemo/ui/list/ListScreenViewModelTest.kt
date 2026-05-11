package io.github.martinjelinek.sportactivitiesdemo.ui.list

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.github.martinjelinek.sportactivitiesdemo.domain.repository.SportActivityRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListScreenViewModelTest {

    private val repo: SportActivityRepository = mockk()

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private val item = SportActivity(
        id = "id-1",
        name = "Run", location = "Park",
        startedAt = 0L, endedAt = 1000L,
        storage = StorageType.LOCAL,
        createdAt = 0L,
    )

    @Test
    fun `state starts with null filter (all) and loads items`() = runTest {
        every { repo.observe(null) } returns flowOf(listOf(item))
        val vm = ListScreenViewModel(repo)
        vm.state.test {
            val finalState = expectMostRecentItem()
            assertThat(finalState.filter).isNull()
            assertThat(finalState.items).containsExactly(item)
            assertThat(finalState.isLoading).isFalse()
        }
    }

    @Test
    fun `selecting filter switches to filtered stream`() = runTest {
        val allFlow = MutableStateFlow(listOf(item))
        val localFlow = MutableStateFlow(listOf(item))
        every { repo.observe(null) } returns allFlow
        every { repo.observe(StorageType.LOCAL) } returns localFlow

        val vm = ListScreenViewModel(repo)
        vm.onEvent(ListScreenEvent.FilterSelected(StorageType.LOCAL))

        vm.state.test {
            assertThat(expectMostRecentItem().filter).isEqualTo(StorageType.LOCAL)
        }
    }
}
