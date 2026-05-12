package io.github.martinjelinek.sportactivitiesdemo.data.remote

import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
internal class FakeRemoteDataSource @Inject constructor() : RemoteDataSource {
    private val items = MutableStateFlow<List<SportActivity>>(emptyList())

    override fun observe() = items.asStateFlow()

    override suspend fun save(sportActivity: SportActivity) {
        val tagged = sportActivity.copy(storage = StorageType.REMOTE)
        items.value = listOf(tagged) + items.value
    }
}
