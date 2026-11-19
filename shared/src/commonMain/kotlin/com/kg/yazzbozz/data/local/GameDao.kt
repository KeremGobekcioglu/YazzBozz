package com.kg.yazzbozz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface GameDao {

    @Upsert
    suspend fun upsertGame(game: GameEntity)

    @Insert
    suspend fun insertPlayers(players: List<GamePlayerEntity>)

    @Insert
    suspend fun insertHands(hands: List<HandEntity>)

    @Insert
    suspend fun insertPenalties(penalties: List<PenaltyEntity>)

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGameRow(gameId: String)

    @Query("DELETE FROM game_players WHERE gameId = :gameId")
    suspend fun deletePlayers(gameId: String)

    @Query("DELETE FROM hands WHERE gameId = :gameId")
    suspend fun deleteHands(gameId: String)

    @Query("DELETE FROM penalties WHERE gameId = :gameId")
    suspend fun deletePenalties(gameId: String)

    /** Writes the whole sheet; a game is small enough that replacing beats diffing. */
    @Transaction
    suspend fun replaceGame(
        game: GameEntity,
        players: List<GamePlayerEntity>,
        hands: List<HandEntity>,
        penalties: List<PenaltyEntity>,
    ) {
        upsertGame(game)
        deletePlayers(game.id)
        deleteHands(game.id)
        deletePenalties(game.id)
        insertPlayers(players)
        insertHands(hands)
        insertPenalties(penalties)
    }

    @Transaction
    suspend fun deleteGame(gameId: String) {
        deletePlayers(gameId)
        deleteHands(gameId)
        deletePenalties(gameId)
        deleteGameRow(gameId)
    }

    @Query("SELECT * FROM games WHERE finishedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun ongoingGame(): GameEntity?

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun game(gameId: String): GameEntity?

    @Query("SELECT * FROM game_players WHERE gameId = :gameId ORDER BY seat")
    suspend fun players(gameId: String): List<GamePlayerEntity>

    @Query("SELECT * FROM hands WHERE gameId = :gameId ORDER BY position")
    suspend fun hands(gameId: String): List<HandEntity>

    @Query("SELECT * FROM penalties WHERE gameId = :gameId ORDER BY position")
    suspend fun penalties(gameId: String): List<PenaltyEntity>
}
