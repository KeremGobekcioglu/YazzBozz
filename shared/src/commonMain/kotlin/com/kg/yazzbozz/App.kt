package com.kg.yazzbozz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kg.yazzbozz.presentation.Hand
import com.kg.yazzbozz.presentation.YazBozScreen
import com.kg.yazzbozz.presentation.YazBozUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.compose.resources.painterResource

import yazzbozz.shared.generated.resources.Res
import yazzbozz.shared.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        val sampleYazBozUiState = YazBozUiState(
            team1Name = "Kerem & Ali",
            team2Name = "Ayşe & Fatma",
            penaltiesTeam1 = 1,
            penaltiesTeam2 = 0,
            totalScoreTeam1 = 145,
            totalScoreTeam2 = 132,
            previousScore = 40,
            finishCounts = persistentMapOf(1 to 2, 2 to 1, 3 to 1, 4 to 0),
            openingCounts = persistentMapOf(1 to 3, 2 to 2, 3 to 1, 4 to 2),
            hands = persistentListOf(
                Hand(scoreTeam1 = 40, scoreTeam2 = 0, whoFinished = 0, whoOpened = persistentListOf(1, 3)),
                Hand(scoreTeam1 = 0, scoreTeam2 = 55, whoFinished = 1, whoOpened = persistentListOf(2)),
                Hand(scoreTeam1 = 30, scoreTeam2 = 0, whoFinished = 0, whoOpened = persistentListOf(1, 2, 4)),
            )
        )
        YazBozScreen(
            onAddResultClick = {},
            onPenaltyClick =  {},
            uiState = sampleYazBozUiState
        )
    }
}