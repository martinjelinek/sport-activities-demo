package io.github.martinjelinek.sportactivitiesdemo.data.repository

import io.github.martinjelinek.sportactivitiesdemo.data.local.LocalDataSource
import io.github.martinjelinek.sportactivitiesdemo.data.remote.RemoteDataSource
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType
import io.github.martinjelinek.sportactivitiesdemo.domain.repository.SportActivityRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SportActivityRepositoryImpl @Inject constructor(
    private val local: LocalDataSource,
    private val remote: RemoteDataSource,
) : SportActivityRepository {

    override fun observe(filter: StorageType?): Flow<List<SportActivity>> =
        when (filter) {
            StorageType.LOCAL -> local.observe()
            StorageType.REMOTE -> remote.observe()
            null -> local.observe().combine(remote.observe()) { l, r ->
                (l + r).sortedByDescending { it.createdAt }
            }
        }

    override suspend fun save(sportActivity: SportActivity): Result<Unit> = runCatching {
        when (sportActivity.storage) {
            StorageType.LOCAL -> local.save(sportActivity)
            StorageType.REMOTE -> remote.save(sportActivity)
        }
    }.onFailure { if (it is CancellationException) throw it }
}
