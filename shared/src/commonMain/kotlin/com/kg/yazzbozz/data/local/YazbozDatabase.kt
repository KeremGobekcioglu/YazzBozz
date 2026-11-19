package com.kg.yazzbozz.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [GameEntity::class, GamePlayerEntity::class, HandEntity::class, PenaltyEntity::class],
    version = 1,
)
@ConstructedBy(YazbozDatabaseConstructor::class)
abstract class YazbozDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}

// Room's KSP processor generates the actuals.
@Suppress("KotlinNoActualForExpect")
expect object YazbozDatabaseConstructor : RoomDatabaseConstructor<YazbozDatabase> {
    override fun initialize(): YazbozDatabase
}

internal const val DatabaseFileName = "yazboz.db"

fun RoomDatabase.Builder<YazbozDatabase>.buildYazbozDatabase(): YazbozDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
