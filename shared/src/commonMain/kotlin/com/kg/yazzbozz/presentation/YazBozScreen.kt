package com.kg.yazzbozz.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

private val ScreenTextSize = 21.sp

@Composable
fun YazBozScreen(
    uiState: YazBozUiState,
    onAddResultClick: () -> Unit,
    onFinishClick: () -> Unit,
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
                    fontSize = ScreenTextSize,
                    textAlign = TextAlign.Center,
                )
                Text(
                    uiState.team2Name,
                    Modifier.weight(1f),
                    color = Color.Black,
                    fontSize = ScreenTextSize,
                    textAlign = TextAlign.Center,
                )
            }

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(uiState.hands) { hand ->
                    HandRow(
                        scoreOfTeam1 = hand.scoreTeam1,
                        scoreOfTeam2 = hand.scoreTeam2,
                        whichTeamFinished = hand.whichTeamFinished ?: 0,
                        whoFinished = hand.whoFinished,
                        team1Openers = hand.whoOpenedTeam1,
                        team2Openers = hand.whoOpenedTeam2
                    )
                    HorizontalDivider()
                }
            }

            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 16.dp)) {
                Text(
                    "X".repeat(uiState.penaltiesTeam1),
                    Modifier.weight(1f),
                    color = Color.Black,
                    fontSize = ScreenTextSize,
                    textAlign = TextAlign.Start,
                )
                Text(
                    "X".repeat(uiState.penaltiesTeam2),
                    Modifier.weight(1f),
                    color = Color.Black,
                    fontSize = ScreenTextSize,
                    textAlign = TextAlign.Start,
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Button(
                    onClick = { onPenaltyClick(1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                ) {
                    Text("Ceza Ekle", fontSize = ScreenTextSize)
                }
                Button(
                    onClick = { onPenaltyClick(2) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                ) {
                    Text("Ceza Ekle", fontSize = ScreenTextSize)
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = onAddResultClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black,
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                ) {
                    Text("El Ekle", fontSize = ScreenTextSize)
                }
                Button(
                    onClick = onFinishClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                ) {
                    Text("Bitir", fontSize = ScreenTextSize)
                }
            }
        }

        VerticalDivider(Modifier.fillMaxHeight().align(Alignment.Center))
    }
}

@Composable
fun HandRow(
    scoreOfTeam1: Int,
    scoreOfTeam2: Int,
    whoFinished: Char? = null,
    whichTeamFinished: Int = 0, // 1 or 2, matches team1Openers/team2Openers
    team1Openers: ImmutableList<Char>,
    team2Openers: ImmutableList<Char>,
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        HandCell(
            score = scoreOfTeam1,
            openers = team1Openers,
            finishedInitial = whoFinished.takeIf { whichTeamFinished == 1 },
            modifier = Modifier.weight(1f),
        )
        VerticalDivider()
        HandCell(
            score = scoreOfTeam2,
            openers = team2Openers,
            finishedInitial = whoFinished.takeIf { whichTeamFinished == 2 },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HandCell(
    score: Int,
    openers: ImmutableList<Char>,
    finishedInitial: Char?,
    modifier: Modifier = Modifier,
) {
    // The finisher is also in `openers` (they had to open before finishing) — drop
    // their letter here so it's drawn once, circled, not once plain + once circled.
    val plainOpeners = if (finishedInitial != null) {
        openers.filterNot { it == finishedInitial }
    } else {
        openers
    }

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading spacer is smaller than the trailing one so the score sits just
        // left of dead-center, leaving more room for initials on the outer side.
        Spacer(Modifier.weight(0.8f))
        Text(
            text = score.toString(),
            color = Color.Black,
            fontSize = ScreenTextSize,
        )
        Spacer(Modifier.weight(1.2f))
        plainOpeners.forEach { initial ->
            Text(text = initial.toString(), color = Color.Black, fontSize = ScreenTextSize)
            Spacer(Modifier.width(4.dp))
        }
        finishedInitial?.let { initial ->
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(1.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = initial.toString(), color = Color.Black, fontSize = ScreenTextSize)
            }
        }
    }
}