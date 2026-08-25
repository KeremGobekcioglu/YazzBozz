package com.kg.yazzbozz.presentation.yazboz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableList

class YazzBozzViewModel : ViewModel() {
    private companion object {
        const val PenaltyPoint = 100
    }

    var uiState by mutableStateOf(YazBozUiState())
        private set

    fun initializeFromOnboarding(
        team1Name: String,
        team2Name: String,
        player1Name: String,
        player2Name: String,
        player3Name: String,
        player4Name: String,
    ) {
        uiState = YazBozUiState(
            team1Name = team1Name,
            team2Name = team2Name,
            playerNames = persistentListOf(
                player1Name,
                player2Name,
                player3Name,
                player4Name,
            ),
        )
    }

    fun onConfirmHand(hand: Hand) {
        val updatedHands = (uiState.hands + hand).toImmutableList()
        recalculateFromHands(updatedHands, isFinished = false)
    }

    fun onPenaltyClick(team: Int) {
        uiState = when (team) {
            1 -> uiState.copy(penaltiesTeam1 = uiState.penaltiesTeam1 + 1)
            2 -> uiState.copy(penaltiesTeam2 = uiState.penaltiesTeam2 + 1)
            else -> uiState
        }
    }

    fun onFinishClicked() {
        recalculateFromHands(uiState.hands, isFinished = true)
    }

    private fun recalculateFromHands(hands: List<Hand>, isFinished: Boolean) {
        val totalScoreTeam1 = hands.sumOf { it.scoreTeam1 } + (uiState.penaltiesTeam1 * PenaltyPoint)
        val totalScoreTeam2 = hands.sumOf { it.scoreTeam2 } + (uiState.penaltiesTeam2 * PenaltyPoint)

        val finishCounts = mutableMapOf<String, Int>()
        val openingCounts = mutableMapOf<String, Int>()
        val initialToPlayerName = uiState.playerNames
            .mapNotNull { name -> name.firstOrNull()?.let { it to name } }
            .toMap()

        hands.forEach { hand ->
            hand.whoFinished
                ?.let(initialToPlayerName::get)
                ?.let { playerName -> finishCounts[playerName] = (finishCounts[playerName] ?: 0) + 1 }

            hand.whoOpenedTeam1.forEach { initial ->
                initialToPlayerName[initial]?.let { playerName ->
                    openingCounts[playerName] = (openingCounts[playerName] ?: 0) + 1
                }
            }
            hand.whoOpenedTeam2.forEach { initial ->
                initialToPlayerName[initial]?.let { playerName ->
                    openingCounts[playerName] = (openingCounts[playerName] ?: 0) + 1
                }
            }
        }

        val previousScore = hands.lastOrNull()?.let { lastHand ->
            if (lastHand.scoreTeam1 >= lastHand.scoreTeam2) lastHand.scoreTeam1 else lastHand.scoreTeam2
        }

        uiState = uiState.copy(
            hands = hands.toImmutableList(),
            totalScoreTeam1 = totalScoreTeam1,
            totalScoreTeam2 = totalScoreTeam2,
            isFinished = isFinished,
            previousScore = previousScore,
            finishCounts = finishCounts.ifEmpty { persistentMapOf() }.toImmutableMap(),
            openingCounts = openingCounts.ifEmpty { persistentMapOf() }.toImmutableMap(),
        )
    }
}