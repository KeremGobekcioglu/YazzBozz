package com.kg.yazzbozz.data

import com.kg.yazzbozz.data.local.GameDao
import com.kg.yazzbozz.data.local.GameEntity
import com.kg.yazzbozz.data.local.GamePlayerEntity
import com.kg.yazzbozz.data.local.HandEntity
import com.kg.yazzbozz.data.local.PenaltyEntity
import com.kg.yazzbozz.domain.GameRecord
import com.kg.yazzbozz.domain.Hand
import com.kg.yazzbozz.domain.Penalty
import com.kg.yazzbozz.domain.PlayerName
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.launch

/**
 * Writes are queued and applied one at a time, in order, on [scope] — an app-wide scope, so a
 * hand typed just before the screen goes away still reaches the disk. Reads that must see
 * those writes (the ongoing game, a single game) wait behind the queue.
 */
class GameRepository(
    private val dao: GameDao,
    scope: CoroutineScope,
) {
    private val queue = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (operation in queue) {
                try {
                    operation()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    println("YazBoz: database write failed: $e")
                }
            }
        }
    }

    fun save(game: GameRecord) {
        queue.trySend {
            dao.replaceGame(
                game = game.toEntity(),
                players = game.players.mapIndexed { seat, player -> player.toEntity(game.id, seat) },
                hands = game.hands.mapIndexed { position, hand -> hand.toEntity(game.id, position) },
                penalties = game.penalties.mapIndexed { position, penalty -> penalty.toEntity(game.id, position) },
            )
        }
    }

    fun delete(gameId: String) {
        queue.trySend { dao.deleteGame(gameId) }
    }

    suspend fun loadOngoing(): GameRecord? = afterPendingWrites {
        dao.ongoingGame()?.let { loadRecord(it) }
    }

    suspend fun loadGame(gameId: String): GameRecord? = afterPendingWrites {
        dao.game(gameId)?.let { loadRecord(it) }
    }

    private suspend fun <T> afterPendingWrites(block: suspend () -> T): T {
        val result = CompletableDeferred<T>()
        queue.send { result.completeWith(runCatching { block() }) }
        return result.await()
    }

    private suspend fun loadRecord(game: GameEntity): GameRecord = GameRecord(
        id = game.id,
        startedAt = game.startedAt,
        finishedAt = game.finishedAt,
        team1Name = game.team1Name,
        team2Name = game.team2Name,
        players = dao.players(game.id).map { PlayerName(it.firstName, it.lastName) },
        hands = dao.hands(game.id).map { it.toHand() },
        penalties = dao.penalties(game.id).map { Penalty(team = it.team, value = it.value, label = it.label) },
    )
}

private fun GameRecord.toEntity() = GameEntity(
    id = id,
    startedAt = startedAt,
    finishedAt = finishedAt,
    team1Name = team1Name,
    team2Name = team2Name,
    totalTeam1 = if (isFinished) total(1) else null,
    totalTeam2 = if (isFinished) total(2) else null,
)

private fun PlayerName.toEntity(gameId: String, seat: Int) = GamePlayerEntity(
    gameId = gameId,
    seat = seat,
    firstName = firstName,
    lastName = lastName,
    personKey = key,
)

private fun Hand.toEntity(gameId: String, position: Int) = HandEntity(
    gameId = gameId,
    position = position,
    scoreTeam1 = scoreTeam1,
    scoreTeam2 = scoreTeam2,
    finisherSeat = finisherSeat,
    openerSeats = openerSeats.joinToString(","),
)

private fun HandEntity.toHand() = Hand(
    scoreTeam1 = scoreTeam1,
    scoreTeam2 = scoreTeam2,
    finisherSeat = finisherSeat,
    openerSeats = openerSeats.split(",").mapNotNull { it.toIntOrNull() }.toImmutableList(),
)

private fun Penalty.toEntity(gameId: String, position: Int) = PenaltyEntity(
    gameId = gameId,
    position = position,
    team = team,
    value = value,
    label = label,
)
