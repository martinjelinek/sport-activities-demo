package io.github.martinjelinek.sportactivitiesdemo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SportActivityEntity::class], version = 3, exportSchema = false)
abstract class SportActivityDatabase : RoomDatabase() {
    abstract fun sportActivityDao(): SportActivityDao
}
