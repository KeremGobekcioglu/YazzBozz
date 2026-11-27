package com.kg.yazzbozz.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "games", indices = [Index("finishedAt")])
data class GameEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    /** Null while the game is still being played. */
    val finishedAt: Long?,
    val team1Name: String,
    val team2Name: String,
    /** Final totals, written when the game is finished. */
    val totalTeam1: Int?,
    val totalTeam2: Int?,
)

@Entity(
    tableName = "game_players",
    primaryKeys = ["gameId", "seat"],
    indices = [Index("personKey")],
)
data class GamePlayerEntity(
    val gameId: String,
    val seat: Int,
    val firstName: String,
    val lastName: String,
    val personKey: String,
)

@Entity(tableName = "hands", primaryKeys = ["gameId", "position"])
data class HandEntity(
    val gameId: String,
    val position: Int,
    val scoreTeam1: Int,
    val scoreTeam2: Int,
    val finisherSeat: Int?,
    /** Comma-separated seats, e.g. "0,3". */
    val openerSeats: String,
    /** Comma-separated FinishKind names, e.g. "Elden,Okey"; empty = normal finish (and every v1 hand). */
    @ColumnInfo(defaultValue = "")
    val finishKinds: String = "",
)

@Entity(tableName = "penalties", primaryKeys = ["gameId", "position"])
data class PenaltyEntity(
    val gameId: String,
    val position: Int,
    val team: Int,
    val value: Int,
    val label: String?,
)
