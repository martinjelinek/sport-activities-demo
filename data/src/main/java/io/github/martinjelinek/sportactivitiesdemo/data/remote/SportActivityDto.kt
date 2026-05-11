package io.github.martinjelinek.sportactivitiesdemo.data.remote

import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType

data class SportActivityDto(
    val id: String = "",
    val name: String = "",
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val createdAt: Long = 0L,
)

fun SportActivityDto.toDomain() = SportActivity(
    id = id,
    name = name,
    startedAt = startedAt,
    endedAt = endedAt,
    storage = StorageType.REMOTE,
    createdAt = createdAt,
)

fun SportActivity.toDto() = SportActivityDto(
    id = id,
    name = name,
    startedAt = startedAt,
    endedAt = endedAt,
    createdAt = createdAt,
)
