package com.kg.yazzbozz.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kg.yazzbozz.AppGraph
import com.kg.yazzbozz.presentation.onboarding.EntranceOnboardingData
import com.kg.yazzbozz.presentation.onboarding.EntranceScreen
import com.kg.yazzbozz.presentation.onboarding.EntranceViewModel
import com.kg.yazzbozz.presentation.results.PreviousGameViewModel
import com.kg.yazzbozz.presentation.results.PreviousResultsScreen
import com.kg.yazzbozz.presentation.results.PreviousResultsViewModel
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
            val entranceViewModel = viewModel { EntranceViewModel(appGraph.gameRepository) }
            val suggestions by entranceViewModel.suggestions.collectAsStateWithLifecycle()
            EntranceScreen(
                suggestions = suggestions,
                onHistoryClick = { navController.navigate(PreviousResultsRoute) },
                onStartClick = { onboarding: EntranceOnboardingData ->
                    yazBozViewModel.startNewGame(
                        team1Name = onboarding.team1Name,
                        team2Name = onboarding.team2Name,
                        players = onboarding.players,
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
                onAddPenalty = yazBozViewModel::onAddPenalty,
                onNewGameClick = yazBozViewModel::onNewGameClicked,
                onHistoryClick = { navController.navigate(PreviousResultsRoute) },
                onLeaveClick = {
                    yazBozViewModel.onAbandonGame()
                    navController.navigate(EntranceRoute) {
                        popUpTo<YazBozRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<PreviousResultsRoute> {
            val viewModel = viewModel { PreviousResultsViewModel(appGraph.gameRepository) }
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            PreviousResultsScreen(
                uiState = uiState,
                onFilterClick = viewModel::onFilterClick,
                onGameClick = { gameId -> navController.navigate(PreviousGameRoute(gameId)) },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable<PreviousGameRoute> { backStackEntry ->
            val gameId = backStackEntry.toRoute<PreviousGameRoute>().gameId
            val viewModel = viewModel(key = gameId) { PreviousGameViewModel(appGraph.gameRepository, gameId) }
            val uiState = viewModel.uiState
            if (uiState == null) {
                Box(Modifier.fillMaxSize().background(Color.White))
            } else {
                YazBozScreen(
                    uiState = uiState,
                    readOnly = true,
                    subtitle = viewModel.date,
                    onBackClick = { navController.popBackStack() },
                )
            }
        }
    }
}
