package com.kg.yazzbozz.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute

@Serializable
data object EntranceRoute : AppRoute

@Serializable
data class YazBozRoute(
    val team1Name: String,
    val team2Name: String,
    val player1Name: String,
    val player2Name: String,
    val player3Name: String,
    val player4Name: String,
) : AppRoute
