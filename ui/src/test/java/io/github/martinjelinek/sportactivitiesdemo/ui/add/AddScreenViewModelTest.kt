package io.github.martinjelinek.sportactivitiesdemo.ui.add

import com.google.common.truth.Truth.assertThat
import io.github.martinjelinek.sportactivitiesdemo.domain.IdGenerator
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.github.martinjelinek.sportactivitiesdemo.domain.repository.SportActivityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddScreenViewModelTest {

    private val repo: SportActivityRepository = mockk()
    private val clock: Clock = Clock.fixed(Instant.ofEpochMilli(FIXED_NOW), ZoneOffset.UTC)
    private val idGenerator = IdGenerator { FIXED_ID }

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `isSavable false until all fields valid`() = runTest {
        val vm = AddScreenViewModel(repo, clock, idGenerator)
        assertThat(vm.state.value.isSavable).isFalse()
        vm.onEvent(AddScreenEvent.NameChanged("Run"))
        vm.onEvent(AddScreenEvent.LocationChanged("Park"))
        vm.onEvent(AddScreenEvent.StartedAtChanged(0L))
        vm.onEvent(AddScreenEvent.EndedAtChanged(1000L))
        assertThat(vm.state.value.isSavable).isTrue()
    }

    @Test
    fun `Save calls repo with current form, emits savedTo on success`() = runTest {
        val captured = slot<SportActivity>()
        coEvery { repo.save(capture(captured)) } returns Result.success(Unit)

        val vm = AddScreenViewModel(repo, clock, idGenerator)
        vm.onEvent(AddScreenEvent.NameChanged("Run"))
        vm.onEvent(AddScreenEvent.LocationChanged("Park"))
        vm.onEvent(AddScreenEvent.StartedAtChanged(0L))
        vm.onEvent(AddScreenEvent.EndedAtChanged(1000L))
        vm.onEvent(AddScreenEvent.StorageChanged(StorageType.REMOTE))
        vm.onEvent(AddScreenEvent.Save)

        coVerify { repo.save(any()) }
        assertThat(captured.captured.name).isEqualTo("Run")
        assertThat(captured.captured.storage).isEqualTo(StorageType.REMOTE)
        assertThat(captured.captured.id).isEqualTo(FIXED_ID)
        assertThat(captured.captured.createdAt).isEqualTo(FIXED_NOW)
        assertThat(vm.state.value.savedTo).isEqualTo(StorageType.REMOTE)
    }

    @Test
    fun `Save sets errorMessage on failure`() = runTest {
        coEvery { repo.save(any()) } returns Result.failure(RuntimeException("boom"))
        val vm = AddScreenViewModel(repo, clock, idGenerator)
        vm.onEvent(AddScreenEvent.NameChanged("Run"))
        vm.onEvent(AddScreenEvent.LocationChanged("Park"))
        vm.onEvent(AddScreenEvent.StartedAtChanged(0L))
        vm.onEvent(AddScreenEvent.EndedAtChanged(1000L))
        vm.onEvent(AddScreenEvent.Save)

        assertThat(vm.state.value.errorMessage).isEqualTo("boom")
        assertThat(vm.state.value.savedTo).isNull()
    }

    private companion object {
        const val FIXED_ID = "fixed-id"
        const val FIXED_NOW = 1_700_000_000_000L
    }
}
