package com.kg.yazzbozz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kg.yazzbozz.presentation.onboarding.EntranceScreen
import com.kg.yazzbozz.presentation.onboarding.EntranceOnboardingData
import com.kg.yazzbozz.presentation.yazboz.YazBozScreen
import com.kg.yazzbozz.presentation.yazboz.YazzBozzViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val yazBozViewModel = remember { YazzBozzViewModel() }

    NavHost(
        navController = navController,
        startDestination = EntranceRoute,
    ) {
        composable<EntranceRoute> {
            EntranceScreen(
                onStartClick = { onboarding: EntranceOnboardingData ->
                    navController.navigate(
                        YazBozRoute(
                            team1Name = onboarding.team1Name,
                            team2Name = onboarding.team2Name,
                            player1Name = onboarding.player1Name,
                            player2Name = onboarding.player2Name,
                            player3Name = onboarding.player3Name,
                            player4Name = onboarding.player4Name,
                        ),
                    )
                },
            )
        }

        composable<YazBozRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<YazBozRoute>()
            LaunchedEffect(route) {
                yazBozViewModel.initializeFromOnboarding(
                    team1Name = route.team1Name,
                    team2Name = route.team2Name,
                    player1Name = route.player1Name,
                    player2Name = route.player2Name,
                    player3Name = route.player3Name,
                    player4Name = route.player4Name,
                )
            }

            YazBozScreen(
                uiState = yazBozViewModel.uiState,
                onConfirmHand = yazBozViewModel::onConfirmHand,
                onFinishClick = yazBozViewModel::onFinishClicked,
                onPenaltyClick = yazBozViewModel::onPenaltyClick,
                onNewGameClick = yazBozViewModel::onNewGameClicked,
            )
        }
    }
}
