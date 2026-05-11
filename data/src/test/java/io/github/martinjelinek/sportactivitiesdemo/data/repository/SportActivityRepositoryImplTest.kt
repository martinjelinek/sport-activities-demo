package io.github.martinjelinek.sportactivitiesdemo.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.martinjelinek.sportactivitiesdemo.data.local.LocalDataSource
import io.github.martinjelinek.sportactivitiesdemo.data.remote.RemoteDataSource
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SportActivityRepositoryImplTest {

    private val local: LocalDataSource = mockk(relaxed = true)
    private val remote: RemoteDataSource = mockk(relaxed = true)
    private val sut = SportActivityRepositoryImpl(local, remote)

    private val localItem = SportActivity(
        id = "l1", name = "Run", location = "",
        startedAt = 0L, endedAt = 1000L,
        storage = StorageType.LOCAL, createdAt = 100L,
    )
    private val remoteItem = SportActivity(
        id = "r1", name = "Bike", location = "",
        startedAt = 0L, endedAt = 2000L,
        storage = StorageType.REMOTE, createdAt = 200L,
    )

    @Test
    fun `observe with null filter merges both sources sorted by createdAt desc`() = runTest {
        every { local.observe() } returns flowOf(listOf(localItem))
        every { remote.observe() } returns flowOf(listOf(remoteItem))

        sut.observe(null).test {
            val list = awaitItem()
            assertThat(list).containsExactly(remoteItem, localItem).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `observe LOCAL emits only local items`() = runTest {
        every { local.observe() } returns flowOf(listOf(localItem))
        every { remote.observe() } returns flowOf(listOf(remoteItem))

        sut.observe(StorageType.LOCAL).test {
            assertThat(awaitItem()).containsExactly(localItem)
            awaitComplete()
        }
    }

    @Test
    fun `observe REMOTE emits only remote items`() = runTest {
        every { local.observe() } returns flowOf(listOf(localItem))
        every { remote.observe() } returns flowOf(listOf(remoteItem))

        sut.observe(StorageType.REMOTE).test {
            assertThat(awaitItem()).containsExactly(remoteItem)
            awaitComplete()
        }
    }

    @Test
    fun `save LOCAL routes to local data source`() = runTest {
        coEvery { local.save(any()) } returns Unit
        val result = sut.save(localItem)
        assertThat(result.isSuccess).isTrue()
        coVerify { local.save(localItem) }
    }

    @Test
    fun `save REMOTE routes to remote data source`() = runTest {
        coEvery { remote.save(any()) } returns Unit
        val result = sut.save(remoteItem)
        assertThat(result.isSuccess).isTrue()
        coVerify { remote.save(remoteItem) }
    }

    @Test
    fun `save returns failure when data source throws`() = runTest {
        coEvery { local.save(any()) } throws RuntimeException("disk full")
        val result = sut.save(localItem)
        assertThat(result.isFailure).isTrue()
    }
}
