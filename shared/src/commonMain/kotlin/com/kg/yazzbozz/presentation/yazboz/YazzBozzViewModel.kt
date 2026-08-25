package com.kg.yazzbozz.presentation.yazboz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class YazzBozzViewModel : ViewModel() {
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
        uiState = uiState.copy(
            hands = (uiState.hands + hand).toImmutableList(),
        )
    }

    fun onPenaltyClick(team: Int) {
        uiState = when (team) {
            1 -> uiState.copy(penaltiesTeam1 = uiState.penaltiesTeam1 + 1)
            2 -> uiState.copy(penaltiesTeam2 = uiState.penaltiesTeam2 + 1)
            else -> uiState
        }
    }
}