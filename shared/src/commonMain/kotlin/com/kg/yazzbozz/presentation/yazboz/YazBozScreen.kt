package com.kg.yazzbozz.presentation.yazboz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kg.yazzbozz.domain.DayTitle
import com.kg.yazzbozz.domain.Hand
import com.kg.yazzbozz.domain.Penalty
import com.kg.yazzbozz.domain.PenaltyReasons
import com.kg.yazzbozz.domain.turkishLowercase
import com.kg.yazzbozz.domain.teamOfSeat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private val ScreenTextSize = 21.sp

/**
 * The sheet. With [readOnly] it shows a game from the history exactly as it was played,
 * minus the buttons that change it.
 */
@Composable
fun YazBozScreen(
    uiState: YazBozUiState,
    onConfirmHand: (Hand) -> Unit = {},
    onFinishClick: () -> Unit = {},
    onAddPenalty: (team: Int, value: Int, label: String?) -> Unit = { _, _, _ -> },
    onNewGameClick: () -> Unit = {},
    onLeaveClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    readOnly: Boolean = false,
    onBackClick: () -> Unit = {},
    subtitle: String? = null,
) {
    // Local, throwaway UI state — nothing here reaches the ViewModel until "Ekle"
    // is tapped inside the panel. Closing it any other way just discards the draft.
    var draftHand: Hand? by remember { mutableStateOf(null) }
    var showFinishConfirmation by remember { mutableStateOf(false) }
    var showLeaveConfirmation by remember { mutableStateOf(false) }
    var penaltyTeam: Int? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().navigationBarsPadding()
    ) {
        // Drawn first so opaque parts on top (results, buttons) cover it; it shows through
        // everywhere else, as the line down the middle of the paper.
        VerticalDivider(Modifier.fillMaxHeight().align(Alignment.Center))

        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (readOnly) {
                    TextButton(onClick = onBackClick) {
                        Text("‹ Geçmiş")
                    }
                    Spacer(Modifier.weight(1f))
                    subtitle?.let {
                        Text(it, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp))
                    }
                } else {
                    TextButton(onClick = onHistoryClick) {
                        Text("Geçmiş")
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            // A finished sheet is already saved; an empty one has nothing to lose.
                            if (uiState.isFinished || !uiState.hasEntries) onLeaveClick() else showLeaveConfirmation = true
                        },
                    ) {
                        Text("Yeni oyun")
                    }
                }
            }

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
                    HandRow(hand = hand, seatLabels = uiState.seatLabels)
                    HorizontalDivider()
                }
            }

            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 16.dp)) {
                PenaltyCell(uiState.penaltiesTeam1, Modifier.weight(1f))
                PenaltyCell(uiState.penaltiesTeam2, Modifier.weight(1f))
            }

            if (!readOnly) {
                // A finished game can't take more hands or penalties — only a fresh sheet.
                if (!uiState.isFinished) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Button(
                            onClick = { penaltyTeam = 1 },
                                                        colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Blue,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                        ) {
                            Text("Ceza Ekle", fontSize = ScreenTextSize)
                        }
                        Button(
                            onClick = { penaltyTeam = 2 },
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
                                                        colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                        ) {
                            Text("Eli Bitir", fontSize = ScreenTextSize)
                        }
                    }
                }

                if (uiState.isFinished) {
                    Button(
                        onClick = onNewGameClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text("Aynı kadroyla yeni oyun", fontSize = ScreenTextSize)
                    }
                }
            }
        }

        // Scrim + centered panels. They sit above everything else in this Box because
        // they're declared last — later children in a Box draw on top.
        val currentDraft = draftHand
        if (currentDraft != null && !uiState.isFinished && !readOnly) {
            CenteredPanel(onDismiss = { draftHand = null }) {
                AddHandPanel(
                    draftHand = currentDraft,
                    playerNames = uiState.playerChipNames,
                    onDraftChanged = { draftHand = it },
                    onConfirm = {
                        onConfirmHand(currentDraft)
                        draftHand = null
                    },
                    onDismiss = { draftHand = null },
                )
            }
        }

        val currentPenaltyTeam = penaltyTeam
        if (currentPenaltyTeam != null && !uiState.isFinished && !readOnly) {
            CenteredPanel(onDismiss = { penaltyTeam = null }) {
                AddPenaltyPanel(
                    teamName = if (currentPenaltyTeam == 1) uiState.team1Name else uiState.team2Name,
                    onConfirm = { value, label ->
                        onAddPenalty(currentPenaltyTeam, value, label)
                        penaltyTeam = null
                    },
                    onDismiss = { penaltyTeam = null },
                )
            }
        }
    }

    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmation = false },
            title = { Text("Yeni oyun?") },
            text = { Text("Bu oyun bitirilmedi ve silinecek. Geçmişe kaydetmek için önce \"Eli Bitir\"e bas.") },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmation = false }) {
                    Text("Vazgeç")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirmation = false
                        onLeaveClick()
                    },
                ) {
                    Text("Sil ve başla")
                }
            },
        )
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
            .background(Color.White)
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
            PenaltySummary(uiState.penaltiesTeam1)
            Spacer(Modifier.height(8.dp))
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(uiState.team2Name, color = Color.Black, fontSize = 18.sp)
                Text(uiState.totalScoreTeam2.toString(), color = Color.Black, fontSize = 18.sp)
            }
            PenaltySummary(uiState.penaltiesTeam2)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
        }

        item {
            Row(Modifier.fillMaxWidth()) {
                Text("Oyuncu", modifier = Modifier.weight(2f), color = Color.Black)
                Text("Açış", modifier = Modifier.weight(1f), color = Color.Black, textAlign = TextAlign.Center)
                Text("Bitiş", modifier = Modifier.weight(1f), color = Color.Black, textAlign = TextAlign.Center)
                Text("Puan", modifier = Modifier.weight(1f), color = Color.Black, textAlign = TextAlign.Center)
            }
            Text("Puan = açış + 2 × bitiş", color = Color.Gray, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
        }

        items(uiState.ranking, key = { it.seat }) { rank ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(2f)) {
                    Text(rank.name, color = Color.Black)
                    rank.title?.let { title ->
                        Text(title.text, color = title.lightColor, fontSize = 12.sp)
                    }
                }
                listOf(rank.openings, rank.finishes, rank.score).forEach { number ->
                    Text(
                        number.toString(),
                        modifier = Modifier.weight(1f),
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun PenaltySummary(penalties: ImmutableList<Penalty>) {
    if (penalties.isEmpty()) return
    val terms = penalties.joinToString(" + ") { penalty ->
        penalty.label?.let { "${penalty.value} (${it.turkishLowercase()})" } ?: penalty.value.toString()
    }
    Text(
        "Ceza: $terms = ${penalties.sumOf { it.value }}",
        color = Color.Gray,
        fontSize = 13.sp,
    )
}

/** The standard 100 is written as "X", like on paper; any other value as the number. */
private fun penaltyMark(value: Int): String = if (value == StandardPenalty) "X" else value.toString()

private const val StandardPenalty = 100

/** Penalties for one team, as written — no multiplier, no rule — each with its reason, if given. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PenaltyCell(penalties: ImmutableList<Penalty>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        penalties.forEach { penalty ->
            Row {
                Text(
                    penaltyMark(penalty.value),
                    color = Color.Black,
                    fontSize = ScreenTextSize,
                    modifier = Modifier.alignByBaseline(),
                )
                penalty.label?.let { label ->
                    Text(
                        label.turkishLowercase(),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.alignByBaseline().padding(start = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun HandRow(
    hand: Hand,
    seatLabels: ImmutableList<String>,
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        HandCell(hand = hand, team = 1, seatLabels = seatLabels, modifier = Modifier.weight(1f))
        VerticalDivider()
        HandCell(hand = hand, team = 2, seatLabels = seatLabels, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HandCell(
    hand: Hand,
    team: Int,
    seatLabels: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    val score = if (team == 1) hand.scoreTeam1 else hand.scoreTeam2
    val finisherSeat = hand.finisherSeat?.takeIf { teamOfSeat(it) == team }
    // The finisher also opened (they had to, to finish) — draw their mark once, circled.
    val plainOpeners = hand.openerSeats.filter { teamOfSeat(it) == team && it != finisherSeat }

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
        plainOpeners.forEach { seat ->
            Text(text = seatLabels.getOrElse(seat) { "?" }, color = Color.Black, fontSize = ScreenTextSize)
            Spacer(Modifier.width(4.dp))
        }
        finisherSeat?.let { seat ->
            val label = seatLabels.getOrElse(seat) { "?" }
            Box(
                modifier = Modifier
                    .border(1.dp, Color.Black, RoundedCornerShape(percent = 50))
                    .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                    .padding(horizontal = if (label.length > 1) 4.dp else 0.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = label, color = Color.Black, fontSize = ScreenTextSize)
            }
        }
    }
}

@Composable
private fun CenteredPanel(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() }, // tap outside the card to dismiss
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
            content()
        }
    }
}

// ---------------------------------------------------------------- penalty panel

private val DefaultPenaltyText = StandardPenalty.toString()

/**
 * Value is pre-filled so the common case is one tap; any number can be typed instead.
 * The reason is optional and has no effect on the value.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddPenaltyPanel(
    teamName: String,
    onConfirm: (value: Int, label: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var valueText by remember { mutableStateOf(DefaultPenaltyText) }
    var label: String? by remember { mutableStateOf(null) }
    val value = valueText.trim().toIntOrNull()
    val confirm = { value?.let { onConfirm(it, label) } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text("$teamName — ceza", fontSize = 16.sp, color = Color.Black)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = valueText,
            onValueChange = { valueText = it },
            label = { Text("Ceza puanı") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { confirm() }),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Text("Neden? (isteğe bağlı)", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(6.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PenaltyReasons.forEach { reason ->
                NameChip(
                    label = reason,
                    selected = label == reason,
                    // Tap the selected reason again to clear it.
                    onClick = { label = reason.takeUnless { label == reason } },
                    horizontalPadding = 10.dp,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Vazgeç")
            }
            Button(
                onClick = { confirm() },
                enabled = value != null,
                modifier = Modifier.weight(1f),
            ) {
                Text("Ekle")
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
 * [playerNames] is indexed by seat: 0,1 = team 1; 2,3 = team 2.
 */
@Composable
private fun AddHandPanel(
    draftHand: Hand,
    playerNames: ImmutableList<String>,
    onDraftChanged: (Hand) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val team1Locked = draftHand.finishingTeam == 1
    val team2Locked = draftHand.finishingTeam == 2

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
            playerNames.forEachIndexed { seat, name ->
                NameChip(
                    label = name,
                    selected = draftHand.finisherSeat == seat,
                    onClick = {
                        // Tap the same finisher again to clear it.
                        onDraftChanged(
                            draftHand.copy(finisherSeat = seat.takeUnless { draftHand.finisherSeat == seat }),
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
            playerNames.forEachIndexed { seat, name ->
                val opened = seat in draftHand.openerSeats
                NameChip(
                    label = name,
                    selected = opened,
                    onClick = {
                        val updated = if (opened) draftHand.openerSeats - seat else draftHand.openerSeats + seat
                        onDraftChanged(draftHand.copy(openerSeats = updated.sorted().toImmutableList()))
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
    horizontalPadding: Dp = 0.dp,
) {
    val borderColor = if (selected) Color.Black else Color.LightGray
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = Color.Black, fontSize = 13.sp)
    }
}

internal val DayTitle.text: String
    get() = when (this) {
        DayTitle.Carrier -> "Günün taşıyanı"
        DayTitle.Rat -> "Günün sıçanı"
    }

/** Title colour on a white background. */
internal val DayTitle.lightColor: Color
    get() = when (this) {
        DayTitle.Carrier -> Color(0xFFB8860B)
        DayTitle.Rat -> Color(0xFF8D6E63)
    }
