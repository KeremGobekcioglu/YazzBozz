package com.kg.yazzbozz.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun YazBozScreen(
    uiState: YazBozUiState,
    onAddResultClick: () -> Unit,
    onPenaltyClick: (team: Int) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(
                    uiState.team1Name,
                    Modifier.weight(1f),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    uiState.team2Name,
                    Modifier.weight(1f),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
            }

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(uiState.hands) { hand ->
                    HandRow(
                        scoreOfTeam1 = hand.scoreTeam1,
                        scoreOfTeam2 = hand.scoreTeam2,
                        playerFinished = hand.whoFinished,
                        playersWhoOpenedTheirHand = hand.whoOpened,
                    )
                }
            }

            Row(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    "Ceza: ${uiState.penaltiesTeam1}",
                    Modifier.weight(1f),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Ceza: ${uiState.penaltiesTeam2}",
                    Modifier.weight(1f),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
            }
        }

        VerticalDivider(Modifier.fillMaxHeight().align(Alignment.Center))

        ExtendedFloatingActionButton(
            onClick = onAddResultClick,
            containerColor = Color(0xFFFFD700),
            contentColor = Color.Black,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        ) {
            Text("El ekle", color = Color.Black)
        }
    }
}

@Composable
fun HandRow(
    scoreOfTeam1: Int,
    scoreOfTeam2: Int,
    playerFinished: Int = 0, // from left to right : 1 , 2 , 3 , 4. It should be matched with name.
    playersWhoOpenedTheirHand: ImmutableList<Int>
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = scoreOfTeam1.toString(),
            modifier = Modifier.weight(1f),
            color = Color.Black,
            textAlign = TextAlign.Center,
        )
        Text(
            text = scoreOfTeam2.toString(),
            modifier = Modifier.weight(1f),
            color = Color.Black,
            textAlign = TextAlign.Center,
        )
    }
}
