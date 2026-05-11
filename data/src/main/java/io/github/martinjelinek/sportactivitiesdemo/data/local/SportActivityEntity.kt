package io.github.martinjelinek.sportactivitiesdemo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import io.github.martinjelinek.sportactivitiesdemo.domain.model.StorageType

@Entity(tableName = "sport_activity")
data class SportActivityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val startedAt: Long,
    val endedAt: Long,
    val createdAt: Long,
)

fun SportActivityEntity.toDomain() = SportActivity(
    id = id,
    name = name,
    startedAt = startedAt,
    endedAt = endedAt,
    storage = StorageType.LOCAL,
    createdAt = createdAt,
)

fun SportActivity.toEntity() = SportActivityEntity(
    id = id,
    name = name,
    startedAt = startedAt,
    endedAt = endedAt,
    createdAt = createdAt,
)
