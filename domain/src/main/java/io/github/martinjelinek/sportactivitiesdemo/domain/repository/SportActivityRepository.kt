package io.github.martinjelinek.sportactivitiesdemo.domain.repository

import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import kotlinx.coroutines.flow.Flow

interface SportActivityRepository {

    /**
     * Observe all sport activities.
     * Filters by [filter] if not null.
     */
    fun observe(filter: StorageType? = null): Flow<List<SportActivity>>

    suspend fun save(sportActivity: SportActivity): Result<Unit>
}
