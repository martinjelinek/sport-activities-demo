package io.github.martinjelinek.sportactivitiesdemo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SportActivityDao {
    @Query("SELECT * FROM sport_activity ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SportActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SportActivityEntity)
}
