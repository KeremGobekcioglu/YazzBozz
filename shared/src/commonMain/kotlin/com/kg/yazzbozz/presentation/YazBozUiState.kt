package com.kg.yazzbozz.presentation

data class YazBozUiState(
    val team1Name: String = "",
    val team2Name: String = "",
    val penaltiesTeam1: Int = 0,
    val penaltiesTeam2: Int = 0,
    val totalScoreTeam1: Int = 0,
    val totalScoreTeam2: Int = 0,
    val previousScore: Int? = null,
    val player1FinishCount: Int = 0,
    val player2FinishCount: Int = 0,
    val player3FinishCount: Int = 0,
    val player4FinishCount: Int = 0,
    val player1OpeningCount: Int = 0,
    val player2OpeningCount: Int = 0,
    val player3OpeningCount: Int = 0,
    val player4OpeningCount: Int = 0,
)
