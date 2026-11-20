package com.kg.yazzbozz.presentation.yazboz

import com.kg.yazzbozz.domain.GameRecord
import com.kg.yazzbozz.domain.Hand
import com.kg.yazzbozz.domain.Penalty
import com.kg.yazzbozz.domain.PlayerName
import com.kg.yazzbozz.domain.winningTeam
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class YazBozUiStateTest {

    private val game = GameRecord(
        id = "g",
        startedAt = 0,
        team1Name = "Biz",
        team2Name = "Onlar",
        players = listOf(PlayerName("Kerem"), PlayerName("Ali"), PlayerName("Ayşe"), PlayerName("Fatma")),
        hands = listOf(
            Hand(scoreTeam1 = 0, scoreTeam2 = 87, finisherSeat = 1, openerSeats = persistentListOf(1, 2)),
            Hand(scoreTeam1 = 132, scoreTeam2 = -101, finisherSeat = 3, openerSeats = persistentListOf(0, 3)),
        ),
        penalties = listOf(Penalty(team = 1, value = 100), Penalty(team = 2, value = 37), Penalty(team = 1, value = 101)),
    )

    @Test
    fun totalsAreSumsOfWhatWasWrittenWithMinusSignsCountedAsPositive() {
        val state = game.toUiState()
        assertEquals(0 + 132 + 100 + 101, state.totalScoreTeam1)
        assertEquals(87 + 101 + 37, state.totalScoreTeam2) // the -101 hand still counts against them
    }

    @Test
    fun aMinusSignNeverHelpsATeamWin() {
        val withMinus = game.copy(hands = listOf(Hand(scoreTeam1 = 50, scoreTeam2 = -80)), penalties = emptyList())
        val state = withMinus.toUiState()
        assertEquals(50 to 80, state.totalScoreTeam1 to state.totalScoreTeam2)
        assertEquals(1, withMinus.winningTeam())
    }

    @Test
    fun countsAreKeptPerSeatEvenWhenInitialsClash() {
        val state = game.toUiState()
        assertEquals(listOf(0, 1, 0, 1), state.finishCounts)
        assertEquals(listOf(1, 1, 1, 1), state.openingCounts)
        assertEquals(listOf("K", "Al", "Ay", "F"), state.seatLabels)
    }

    @Test
    fun finisherCountsAsOpenerWhetherOrNotTheyWereTicked() {
        val ticked = game.copy(hands = listOf(Hand(finisherSeat = 2, openerSeats = persistentListOf(0, 2))))
        val notTicked = game.copy(hands = listOf(Hand(finisherSeat = 2, openerSeats = persistentListOf(0))))
        assertEquals(listOf(1, 0, 1, 0), notTicked.toUiState().openingCounts)
        assertEquals(ticked.toUiState().ranking, notTicked.toUiState().ranking)
    }

    @Test
    fun penaltiesAreSplitByTeamInOrder() {
        val state = game.toUiState()
        assertEquals(listOf(100, 101), state.penaltiesTeam1.map { it.value })
        assertEquals(listOf(37), state.penaltiesTeam2.map { it.value })
    }
}
