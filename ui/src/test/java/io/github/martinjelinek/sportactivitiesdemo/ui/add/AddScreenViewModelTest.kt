package io.github.martinjelinek.sportactivitiesdemo.ui.add

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.martinjelinek.sportactivitiesdemo.domain.IdGenerator
import io.github.martinjelinek.sportactivitiesdemo.domain.location.LocationProvider
import io.github.martinjelinek.sportactivitiesdemo.domain.model.Coordinates
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportType
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
import kotlinx.coroutines.test.advanceUntilIdle
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
    private val locationProvider: LocationProvider = mockk(relaxed = true)

    private fun newVm() = AddScreenViewModel(repo, clock, idGenerator, locationProvider)

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `init seeds startedAt to now and endedAt to now plus default duration`() = runTest {
        val vm = newVm()
        assertThat(vm.state.value.startedAt).isEqualTo(FIXED_NOW)
        assertThat(vm.state.value.endedAt).isEqualTo(FIXED_NOW + DEFAULT_DURATION_MS)
    }

    @Test
    fun `isSavable requires a sport and non-blank location`() = runTest {
        val vm = newVm()
        // Times are already valid after init.
        assertThat(vm.state.value.isSavable).isFalse()
        vm.onEvent(AddScreenEvent.SportSelected(SportType.RUN))
        vm.onEvent(AddScreenEvent.LocationChanged("Park"))
        assertThat(vm.state.value.isSavable).isTrue()
    }

    @Test
    fun `Save calls repo with current form and emits Saved effect on success`() = runTest {
        val captured = slot<SportActivity>()
        coEvery { repo.save(capture(captured)) } returns Result.success(Unit)

        val vm = newVm()
        vm.effects.test {
            vm.onEvent(AddScreenEvent.SportSelected(SportType.RUN))
            vm.onEvent(AddScreenEvent.LocationChanged("Park"))
            vm.onEvent(AddScreenEvent.StorageChanged(StorageType.REMOTE))
            vm.onEvent(AddScreenEvent.Save)

            assertThat(awaitItem()).isEqualTo(AddScreenEffect.Saved(StorageType.REMOTE))
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { repo.save(any()) }
        assertThat(captured.captured.name).isEqualTo(SportType.RUN.displayName)
        assertThat(captured.captured.storage).isEqualTo(StorageType.REMOTE)
        assertThat(captured.captured.id).isEqualTo(FIXED_ID)
        assertThat(captured.captured.createdAt).isEqualTo(FIXED_NOW)
    }

    @Test
    fun `Save sets errorMessage on failure and emits no effect`() = runTest {
        coEvery { repo.save(any()) } returns Result.failure(RuntimeException("boom"))
        val vm = newVm()
        vm.effects.test {
            vm.onEvent(AddScreenEvent.SportSelected(SportType.RUN))
            vm.onEvent(AddScreenEvent.LocationChanged("Park"))
            vm.onEvent(AddScreenEvent.Save)
            advanceUntilIdle()

            expectNoEvents()
        }
        assertThat(vm.state.value.errorMessage).isEqualTo("boom")
    }

    @Test
    fun `RefreshLocation success populates coordinates and reverse-geocoded location`() = runTest {
        val coords = Coordinates(50.0871, 14.4213)
        coEvery { locationProvider.currentCoordinates() } returns coords
        coEvery { locationProvider.getLocationDescription(coords) } returns "Prague, Czechia"

        val vm = newVm()
        vm.onEvent(AddScreenEvent.RefreshLocation)
        advanceUntilIdle()

        val s = vm.state.value
        assertThat(s.coordinates).isEqualTo(coords)
        assertThat(s.location).isEqualTo("Prague, Czechia")
        assertThat(s.isResolvingLocation).isFalse()
        assertThat(s.hasLocationError).isFalse()
    }

    @Test
    fun `RefreshLocation when provider returns null sets hasLocationError`() = runTest {
        coEvery { locationProvider.currentCoordinates() } returns null

        val vm = newVm()
        vm.onEvent(AddScreenEvent.RefreshLocation)
        advanceUntilIdle()

        val s = vm.state.value
        assertThat(s.coordinates).isNull()
        assertThat(s.isResolvingLocation).isFalse()
        assertThat(s.hasLocationError).isTrue()
    }

    @Test
    fun `RefreshLocation does not overwrite user-edited location`() = runTest {
        val coords = Coordinates(50.0871, 14.4213)
        coEvery { locationProvider.currentCoordinates() } returns coords
        coEvery { locationProvider.getLocationDescription(coords) } returns "Prague, Czechia"

        val vm = newVm()
        vm.onEvent(AddScreenEvent.LocationChanged("My garden"))
        vm.onEvent(AddScreenEvent.RefreshLocation)
        advanceUntilIdle()

        // User-typed text wins; coordinates still update so the map can show the pin.
        assertThat(vm.state.value.location).isEqualTo("My garden")
        assertThat(vm.state.value.coordinates).isEqualTo(coords)
    }

    private companion object {
        const val FIXED_ID = "fixed-id"
        const val FIXED_NOW = 1_700_000_000_000L
        const val DEFAULT_DURATION_MS = 30L * 60L * 1000L
    }
}
