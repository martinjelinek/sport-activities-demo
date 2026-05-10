package io.github.martinjelinek.sportactivitiesdemo.data.remote

import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    fun observe(): Flow<List<SportActivity>>
    suspend fun save(sportActivity: SportActivity)
}
