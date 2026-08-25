package com.kg.yazzbozz.presentation.yazboz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private val ScreenTextSize = 21.sp

@Composable
fun YazBozScreen(
    uiState: YazBozUiState,
    onConfirmHand: (Hand) -> Unit,
    onFinishClick: () -> Unit,
    onPenaltyClick: (team: Int) -> Unit,
    onNewGameClick: () -> Unit,
) {
    // Local, throwaway UI state — nothing here reaches the ViewModel until "Ekle"
    // is tapped inside the panel. Closing it any other way just discards the draft.
    var draftHand: Hand? by remember { mutableStateOf(null) }
    var showFinishConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            if (uiState.isFinished) {
                ResultsContent(uiState = uiState)
                Spacer(Modifier.height(12.dp))
            }

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
                    enabled = !uiState.isFinished,
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
                    enabled = !uiState.isFinished,
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
                    onClick = { draftHand = Hand() },
                    enabled = !uiState.isFinished,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black,
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                ) {
                    Text("El Ekle", fontSize = ScreenTextSize)
                }
                Button(
                    onClick = { showFinishConfirmation = true },
                    enabled = !uiState.isFinished,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                ) {
                    Text("Eli Bitir", fontSize = ScreenTextSize)
                }
            }

            if (uiState.isFinished) {
                Button(
                    onClick = onNewGameClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("Yeni Oyun", fontSize = ScreenTextSize)
                }
            }
        }

        VerticalDivider(Modifier.fillMaxHeight().align(Alignment.Center))

        // Scrim + centered panel. Sits above everything else in this Box because
        // it's declared last — later children in a Box draw on top.
        val currentDraft = draftHand
        if (currentDraft != null && !uiState.isFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { draftHand = null }, // tap outside the card to dismiss
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { /* absorb clicks so tapping the card doesn't dismiss it */ },
                ) {
                    AddHandPanel(
                        draftHand = currentDraft,
                        playerNames = uiState.playerNames,
                        onDraftChanged = { draftHand = it },
                        onConfirm = {
                            onConfirmHand(currentDraft)
                            draftHand = null
                        },
                        onDismiss = { draftHand = null },
                    )
                }
            }
        }
    }

    if (showFinishConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmation = false },
            title = { Text("Eli bitir?") },
            text = { Text("Eli bitirmek istediğinize emin misiniz?") },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmation = false }) {
                    Text("Vazgeç")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishConfirmation = false
                        onFinishClick()
                    },
                ) {
                    Text("Evet")
                }
            },
        )
    }
}

@Composable
private fun ResultsContent(uiState: YazBozUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Text("Sonuçlar", color = Color.Black, fontSize = 24.sp)
            Spacer(Modifier.height(16.dp))
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(uiState.team1Name, color = Color.Black, fontSize = 18.sp)
                Text(uiState.totalScoreTeam1.toString(), color = Color.Black, fontSize = 18.sp)
            }
            Text(
                "Ceza: ${uiState.penaltiesTeam1} x 100 = ${uiState.penaltiesTeam1 * 100}",
                color = Color.Gray,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(uiState.team2Name, color = Color.Black, fontSize = 18.sp)
                Text(uiState.totalScoreTeam2.toString(), color = Color.Black, fontSize = 18.sp)
            }
            Text(
                "Ceza: ${uiState.penaltiesTeam2} x 100 = ${uiState.penaltiesTeam2 * 100}",
                color = Color.Gray,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
        }

        item {
            Row(Modifier.fillMaxWidth()) {
                Text("Oyuncu", modifier = Modifier.weight(2f), color = Color.Black)
                Text("Açış", modifier = Modifier.weight(1f), color = Color.Black, textAlign = TextAlign.Center)
                Text("Bitiş", modifier = Modifier.weight(1f), color = Color.Black, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
        }

        items(uiState.playerNames) { playerName ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(playerName, modifier = Modifier.weight(2f), color = Color.Black)
                Text(
                    (uiState.openingCounts[playerName] ?: 0).toString(),
                    modifier = Modifier.weight(1f),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    (uiState.finishCounts[playerName] ?: 0).toString(),
                    modifier = Modifier.weight(1f),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
            }
        }
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

// ---------------------------------------------------------------- add-hand panel

/**
 * Inline result-entry panel — same page as YazBozScreen, no sheet, no navigation.
 * [draftHand] is a plain [Hand] being filled in locally in YazBozScreen; every tap
 * here just produces an updated copy via [onDraftChanged]. Nothing reaches the
 * ViewModel until the screen's own onConfirm fires.
 *
 * Team split assumes playerNames[0],[1] = team1, playerNames[2],[3] = team2
 * (indices 0-3, so player id = index + 1).
 */
@Composable
private fun AddHandPanel(
    draftHand: Hand,
    playerNames: ImmutableList<String>,
    onDraftChanged: (Hand) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val team1Locked = draftHand.whichTeamFinished == 1
    val team2Locked = draftHand.whichTeamFinished == 2

    Column(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text("El sonucu", fontSize = 16.sp, color = Color.Black)
        Spacer(Modifier.height(12.dp))

        Text("Kim bitirdi?", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            playerNames.forEachIndexed { index, name ->
                val id = index + 1
                val team = if (id in 1..2) 1 else 2
                val initial = name.firstOrNull() ?: return@forEachIndexed
                NameChip(
                    label = name,
                    selected = draftHand.whoFinished == initial,
                    onClick = {
                        // Tap the same finisher again to clear it.
                        onDraftChanged(
                            if (draftHand.whoFinished == initial) {
                                draftHand.copy(whoFinished = null, whichTeamFinished = null)
                            } else {
                                draftHand.copy(whoFinished = initial, whichTeamFinished = team)
                            },
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Elini kim açtı?", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            playerNames.forEachIndexed { index, name ->
                val id = index + 1
                val team = if (id in 1..2) 1 else 2
                val initial = name.firstOrNull() ?: return@forEachIndexed
                val opened = initial in draftHand.whoOpenedTeam1 || initial in draftHand.whoOpenedTeam2
                NameChip(
                    label = name,
                    selected = opened,
                    onClick = {
                        onDraftChanged(
                            if (team == 1) {
                                val updated = if (opened) {
                                    draftHand.whoOpenedTeam1.filterNot { it == initial }
                                } else {
                                    draftHand.whoOpenedTeam1 + initial
                                }
                                draftHand.copy(whoOpenedTeam1 = updated.toImmutableList())
                            } else {
                                val updated = if (opened) {
                                    draftHand.whoOpenedTeam2.filterNot { it == initial }
                                } else {
                                    draftHand.whoOpenedTeam2 + initial
                                }
                                draftHand.copy(whoOpenedTeam2 = updated.toImmutableList())
                            },
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = if (team1Locked) "0" else draftHand.scoreTeam1.takeIf { it != 0 }?.toString().orEmpty(),
                onValueChange = { text ->
                    onDraftChanged(draftHand.copy(scoreTeam1 = text.toIntOrNull() ?: 0))
                },
                enabled = !team1Locked,
                label = { Text("1. takım") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = if (team2Locked) "0" else draftHand.scoreTeam2.takeIf { it != 0 }?.toString().orEmpty(),
                onValueChange = { text ->
                    onDraftChanged(draftHand.copy(scoreTeam2 = text.toIntOrNull() ?: 0))
                },
                enabled = !team2Locked,
                label = { Text("2. takım") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Vazgeç")
            }
            Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                Text("Ekle")
            }
        }
    }
}

@Composable
private fun NameChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) Color.Black else Color.LightGray
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = Color.Black, fontSize = 13.sp)
    }
}
