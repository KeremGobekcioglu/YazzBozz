package com.kg.yazzbozz.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute

@Serializable
data object EntranceRoute : AppRoute

/** The sheet itself lives in YazzBozzViewModel (and the database), not in the route. */
@Serializable
data object YazBozRoute : AppRoute

@Serializable
data object PreviousResultsRoute : AppRoute

@Serializable
data class PreviousGameRoute(val gameId: String) : AppRoute
