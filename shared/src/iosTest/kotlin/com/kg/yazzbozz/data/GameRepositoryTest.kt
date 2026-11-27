package com.kg.yazzbozz.data

import androidx.room.Room
import com.kg.yazzbozz.data.local.YazbozDatabase
import com.kg.yazzbozz.data.local.buildYazbozDatabase
import com.kg.yazzbozz.domain.FinishKind
import com.kg.yazzbozz.domain.GameRecord
import com.kg.yazzbozz.domain.Hand
import com.kg.yazzbozz.domain.Penalty
import com.kg.yazzbozz.domain.PlayerName
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Runs against a real on-disk database, closed and reopened like a killed app. */
@OptIn(ExperimentalForeignApi::class)
class GameRepositoryTest {

    private val path = NSTemporaryDirectory() + "yazboz-test-${Random.nextLong()}.db"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun openDatabase(): YazbozDatabase =
        Room.databaseBuilder<YazbozDatabase>(name = path).buildYazbozDatabase()

    @AfterTest
    fun tearDown() {
        scope.cancel()
        listOf("", "-wal", "-shm").forEach { NSFileManager.defaultManager.removeItemAtPath(path + it, null) }
    }

    private val started = GameRecord(
        id = "game-1",
        startedAt = 1_000,
        team1Name = "Biz",
        team2Name = "Onlar",
        players = listOf(PlayerName("İlker"), PlayerName("Ali", "Kaya"), PlayerName("Irmak"), PlayerName("Ayşe")),
    )

    private val played = started.copy(
        hands = listOf(
            Hand(
                scoreTeam1 = 0,
                scoreTeam2 = 87,
                finisherSeat = 1,
                openerSeats = persistentListOf(1, 2),
                finishKinds = persistentSetOf(FinishKind.Elden, FinishKind.Okey),
            ),
            Hand(scoreTeam1 = 132, scoreTeam2 = 0, finisherSeat = null, openerSeats = persistentListOf()),
        ),
        penalties = listOf(Penalty(team = 2, value = 100), Penalty(team = 1, value = 55, label = "Okey atma")),
    )

    @Test
    fun ongoingGameSurvivesReopeningTheDatabase() = runBlocking {
        val first = openDatabase()
        GameRepository(first.gameDao(), scope).apply {
            save(started)
            save(played)
            assertEquals(played, loadOngoing())
        }
        first.close()

        val second = openDatabase()
        val repository = GameRepository(second.gameDao(), scope)
        assertEquals(played, repository.loadOngoing())

        val finished = played.copy(finishedAt = 2_000)
        repository.save(finished)
        assertNull(repository.loadOngoing())
        assertEquals(finished, repository.loadGame(finished.id))

        val summary = repository.observeHistory().first().single()
        assertEquals(finished.players, summary.players)
        assertEquals(132 + 55, summary.totalTeam1)
        assertEquals(87 + 100, summary.totalTeam2)

        repository.delete(finished.id)
        assertNull(repository.loadGame(finished.id))
        second.close()
    }
}
