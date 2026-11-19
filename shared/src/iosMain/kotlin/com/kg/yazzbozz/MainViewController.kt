package com.kg.yazzbozz

import androidx.compose.ui.window.ComposeUIViewController
import androidx.room.Room
import com.kg.yazzbozz.data.local.DatabaseFileName
import com.kg.yazzbozz.data.local.YazbozDatabase
import com.kg.yazzbozz.data.local.buildYazbozDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private val appGraph: AppGraph by lazy { AppGraph(createDatabase()) }

fun MainViewController() = ComposeUIViewController { App(appGraph) }

@OptIn(ExperimentalForeignApi::class)
private fun createDatabase(): YazbozDatabase {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val path = requireNotNull(documents?.path) { "Documents directory unavailable" }
    return Room.databaseBuilder<YazbozDatabase>(name = "$path/$DatabaseFileName").buildYazbozDatabase()
}
