package com.kg.yazzbozz.presentation.yazboz

import com.kg.yazzbozz.domain.GameRecord
import com.kg.yazzbozz.domain.Hand
import com.kg.yazzbozz.domain.Penalty
import com.kg.yazzbozz.domain.PlayerRank
import com.kg.yazzbozz.domain.chipNames
import com.kg.yazzbozz.domain.dayRanking
import com.kg.yazzbozz.domain.opened
import com.kg.yazzbozz.domain.seatLabels
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Per-seat lists are indexed by seat: 0,1 → team 1; 2,3 → team 2. */
data class YazBozUiState(
    val team1Name: String = "",
    val team2Name: String = "",
    val playerNames: ImmutableList<String> = persistentListOf(),
    val playerChipNames: ImmutableList<String> = persistentListOf(),
    val seatLabels: ImmutableList<String> = persistentListOf(),
    val penaltiesTeam1: ImmutableList<Penalty> = persistentListOf(),
    val penaltiesTeam2: ImmutableList<Penalty> = persistentListOf(),
    val totalScoreTeam1: Int = 0,
    val totalScoreTeam2: Int = 0,
    val isFinished: Boolean = false,
    val finishCounts: ImmutableList<Int> = persistentListOf(),
    val openingCounts: ImmutableList<Int> = persistentListOf(),
    val hands: ImmutableList<Hand> = persistentListOf(),
    /** Players by openings + 2 × finishes, with the day's titles. */
    val ranking: ImmutableList<PlayerRank> = persistentListOf(),
) {
    val hasEntries: Boolean get() = hands.isNotEmpty() || penaltiesTeam1.isNotEmpty() || penaltiesTeam2.isNotEmpty()
}

fun GameRecord.toUiState(): YazBozUiState {
    val seats = players.indices
    val names = players.map { it.displayName }
    val finishCounts = seats.map { seat -> hands.count { it.finisherSeat == seat } }
    val openingCounts = seats.map { seat -> hands.count { it.opened(seat) } }
    return YazBozUiState(
        team1Name = team1Name,
        team2Name = team2Name,
        playerNames = names.toImmutableList(),
        playerChipNames = chipNames(players).toImmutableList(),
        seatLabels = seatLabels(players).toImmutableList(),
        penaltiesTeam1 = penalties.filter { it.team == 1 }.toImmutableList(),
        penaltiesTeam2 = penalties.filter { it.team == 2 }.toImmutableList(),
        totalScoreTeam1 = total(1),
        totalScoreTeam2 = total(2),
        isFinished = isFinished,
        finishCounts = finishCounts.toImmutableList(),
        openingCounts = openingCounts.toImmutableList(),
        hands = hands.toImmutableList(),
        ranking = dayRanking(names, openingCounts, finishCounts).toImmutableList(),
    )
}
