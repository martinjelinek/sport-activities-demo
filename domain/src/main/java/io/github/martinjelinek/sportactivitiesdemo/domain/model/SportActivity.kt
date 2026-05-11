package io.github.martinjelinek.sportactivitiesdemo.domain.model

import java.util.UUID

enum class StorageType { LOCAL, REMOTE }

data class SportActivity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val location: String,
    val startedAt: Long,
    val endedAt: Long,
    val storage: StorageType,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val durationMillis: Long get() = endedAt - startedAt
}
