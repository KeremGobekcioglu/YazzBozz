package com.kg.yazzbozz.presentation.yazboz

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

data class YazBozUiState(
    val team1Name: String = "",
    val team2Name: String = "",
    val playerNames: ImmutableList<String> = persistentListOf(),
    val penaltiesTeam1: Int = 0,
    val penaltiesTeam2: Int = 0,
    val totalScoreTeam1: Int = 0,
    val totalScoreTeam2: Int = 0,
    val previousScore: Int? = null,
    val finishCounts: ImmutableMap<Int, Int> = persistentMapOf(), // player number (1-4) to finish count
    val openingCounts: ImmutableMap<Int, Int> = persistentMapOf(), // player number (1-4) to opening count
    val hands: ImmutableList<Hand> = persistentListOf()
)

data class Hand(
    val scoreTeam1: Int = 0,
    val scoreTeam2: Int = 0,
    val whoFinished: Char? = null, // team 1 is 1,2 ; team 2 is 3,4
    val whichTeamFinished: Int? = null,
    val whoOpenedTeam1: ImmutableList<Char> = persistentListOf(),
    val whoOpenedTeam2: ImmutableList<Char> = persistentListOf()
)
