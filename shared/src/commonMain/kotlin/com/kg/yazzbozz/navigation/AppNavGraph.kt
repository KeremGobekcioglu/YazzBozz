package com.kg.yazzbozz.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kg.yazzbozz.AppGraph
import com.kg.yazzbozz.domain.PlayerName
import com.kg.yazzbozz.presentation.onboarding.EntranceOnboardingData
import com.kg.yazzbozz.presentation.onboarding.EntranceScreen
import com.kg.yazzbozz.presentation.yazboz.StartState
import com.kg.yazzbozz.presentation.yazboz.YazBozScreen
import com.kg.yazzbozz.presentation.yazboz.YazzBozzViewModel

@Composable
fun AppNavGraph(
    appGraph: AppGraph,
    navController: NavHostController = rememberNavController(),
) {
    val yazBozViewModel = viewModel { YazzBozzViewModel(appGraph.gameRepository) }

    if (yazBozViewModel.startState == StartState.Loading) {
        // A single small read; keep the screen blank rather than flash the entrance.
        Box(Modifier.fillMaxSize().background(Color.White))
        return
    }

    // Picked once: an unfinished game opens straight into its sheet.
    val startDestination: AppRoute = remember {
        if (yazBozViewModel.startState == StartState.Ongoing) YazBozRoute else EntranceRoute
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<EntranceRoute> {
            EntranceScreen(
                onStartClick = { onboarding: EntranceOnboardingData ->
                    yazBozViewModel.startNewGame(
                        team1Name = onboarding.team1Name,
                        team2Name = onboarding.team2Name,
                        players = listOf(
                            PlayerName(onboarding.player1Name),
                            PlayerName(onboarding.player2Name),
                            PlayerName(onboarding.player3Name),
                            PlayerName(onboarding.player4Name),
                        ),
                    )
                    navController.navigate(YazBozRoute) {
                        popUpTo<EntranceRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<YazBozRoute> {
            YazBozScreen(
                uiState = yazBozViewModel.uiState,
                onConfirmHand = yazBozViewModel::onConfirmHand,
                onFinishClick = yazBozViewModel::onFinishClicked,
                onAddPenalty = { team, value -> yazBozViewModel.onAddPenalty(team, value) },
                onNewGameClick = yazBozViewModel::onNewGameClicked,
                onLeaveClick = {
                    yazBozViewModel.onAbandonGame()
                    navController.navigate(EntranceRoute) {
                        popUpTo<YazBozRoute> { inclusive = true }
                    }
                },
            )
        }
    }
}
