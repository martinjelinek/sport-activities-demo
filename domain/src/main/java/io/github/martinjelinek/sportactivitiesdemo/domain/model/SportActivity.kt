package io.github.martinjelinek.sportactivitiesdemo.domain.model

enum class StorageType { LOCAL, REMOTE }

data class SportActivity(
    val id: String,
    val name: String,
    val location: String,
    val startedAt: Long,
    val endedAt: Long,
    val storage: StorageType,
    val createdAt: Long,
) {
    val durationMillis: Long get() = endedAt - startedAt
}
