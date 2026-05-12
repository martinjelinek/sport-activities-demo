package io.github.martinjelinek.sportactivitiesdemo.data.remote

import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType

internal data class SportActivityDto(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val createdAt: Long = 0L,
)

internal fun SportActivityDto.toDomain() = SportActivity(
    id = id,
    name = name,
    location = location,
    startedAt = startedAt,
    endedAt = endedAt,
    storage = StorageType.REMOTE,
    createdAt = createdAt,
)

internal fun SportActivity.toDto() = SportActivityDto(
    id = id,
    name = name,
    location = location,
    startedAt = startedAt,
    endedAt = endedAt,
    createdAt = createdAt,
)
