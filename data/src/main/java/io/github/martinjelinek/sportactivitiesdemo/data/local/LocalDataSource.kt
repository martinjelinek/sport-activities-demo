package io.github.martinjelinek.sportactivitiesdemo.data.local

import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LocalDataSource @Inject constructor(
    private val dao: SportActivityDao,
) {
    fun observe(): Flow<List<SportActivity>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun save(sportActivity: SportActivity) {
        dao.insert(sportActivity.toEntity())
    }
}
