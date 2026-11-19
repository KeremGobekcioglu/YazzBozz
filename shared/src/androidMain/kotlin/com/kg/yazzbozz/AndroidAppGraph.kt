package com.kg.yazzbozz

import android.content.Context
import androidx.room.Room
import com.kg.yazzbozz.data.local.DatabaseFileName
import com.kg.yazzbozz.data.local.YazbozDatabase
import com.kg.yazzbozz.data.local.buildYazbozDatabase

/** Process-wide [AppGraph]; survives Activity recreation. */
object AndroidAppGraph {
    @Volatile
    private var instance: AppGraph? = null

    fun get(context: Context): AppGraph =
        instance ?: synchronized(this) {
            instance ?: AppGraph(createDatabase(context.applicationContext)).also { instance = it }
        }

    private fun createDatabase(context: Context): YazbozDatabase =
        Room.databaseBuilder<YazbozDatabase>(
            context = context,
            name = context.getDatabasePath(DatabaseFileName).absolutePath,
        ).buildYazbozDatabase()
}
