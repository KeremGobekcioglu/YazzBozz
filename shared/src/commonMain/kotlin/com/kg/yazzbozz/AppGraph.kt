package com.kg.yazzbozz

import com.kg.yazzbozz.data.GameRepository
import com.kg.yazzbozz.data.local.YazbozDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** App-lifetime objects. Created once per process by the platform entry point. */
class AppGraph(database: YazbozDatabase) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val gameRepository = GameRepository(database.gameDao(), appScope)
}
