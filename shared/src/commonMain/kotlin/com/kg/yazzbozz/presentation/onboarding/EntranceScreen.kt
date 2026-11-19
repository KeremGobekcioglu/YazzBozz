package com.kg.yazzbozz.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kg.yazzbozz.domain.KnownPlayer
import com.kg.yazzbozz.domain.Lineup
import com.kg.yazzbozz.domain.PlayerName
import com.kg.yazzbozz.domain.chipNames
import com.kg.yazzbozz.domain.nameKey

private val TitleTextSize = 22.sp
private val LabelTextSize = 14.sp
private const val MaxSuggestions = 6
private const val MaxLineups = 5

/** Seats 0,1 are team 1; 2,3 are team 2. */
data class EntranceOnboardingData(
    val team1Name: String,
    val team2Name: String,
    val players: List<PlayerName>,
)

private data class EntranceForm(
    val team1Name: String = "",
    val team2Name: String = "",
    val players: List<PlayerName> = List(4) { PlayerName("") },
) {
    fun withPlayer(seat: Int, player: PlayerName) =
        copy(players = players.toMutableList().also { it[seat] = player })
}

private val EntranceFormSaver = listSaver<EntranceForm, String>(
    save = { form -> listOf(form.team1Name, form.team2Name) + form.players.flatMap { listOf(it.firstName, it.lastName) } },
    restore = { saved ->
        EntranceForm(
            team1Name = saved[0],
            team2Name = saved[1],
            players = List(4) { seat -> PlayerName(saved[2 + seat * 2], saved[3 + seat * 2]) },
        )
    },
)

/** Which field has focus — suggestions are shown under it. */
private sealed interface FocusedField {
    data class TeamName(val team: Int) : FocusedField
    data class Player(val seat: Int) : FocusedField
}

@Composable
fun EntranceScreen(
    suggestions: EntranceSuggestions,
    onStartClick: (EntranceOnboardingData) -> Unit,
    onHistoryClick: () -> Unit,
) {
    var form by rememberSaveable(stateSaver = EntranceFormSaver) { mutableStateOf(EntranceForm()) }
    var focused: FocusedField? by remember { mutableStateOf(null) }

    val filledPlayers = form.players.filter { it.firstName.isNotBlank() }
    val hasDuplicatePerson = filledPlayers.groupBy { it.key }.any { it.value.size > 1 }
    val canStart = form.team1Name.isNotBlank() &&
        form.team2Name.isNotBlank() &&
        filledPlayers.size == 4 &&
        !hasDuplicatePerson

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Yazboz", fontSize = TitleTextSize, color = Color.Black, modifier = Modifier.weight(1f))
            TextButton(onClick = onHistoryClick) {
                Text("Geçmiş")
            }
        }
        Spacer(Modifier.height(16.dp))

        if (suggestions.lineups.isNotEmpty()) {
            Text("Son kadrolar", fontSize = LabelTextSize, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            suggestions.lineups.take(MaxLineups).forEach { lineup ->
                LineupRow(
                    lineup = lineup,
                    onClick = {
                        form = EntranceForm(
                            team1Name = lineup.team1Name,
                            team2Name = lineup.team2Name,
                            players = lineup.players,
                        )
                    },
                )
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(16.dp))
        }

        listOf(1, 2).forEach { team ->
            val seats = if (team == 1) listOf(0, 1) else listOf(2, 3)
            TeamSection(
                teamLabel = "$team. Takım",
                teamName = if (team == 1) form.team1Name else form.team2Name,
                onTeamNameChanged = { name ->
                    form = if (team == 1) form.copy(team1Name = name) else form.copy(team2Name = name)
                },
                teamNameSuggestions = if (focused == FocusedField.TeamName(team)) {
                    teamNameSuggestions(suggestions.teamNames, if (team == 1) form.team1Name else form.team2Name)
                } else {
                    emptyList()
                },
                onTeamNameFocused = { focused = FocusedField.TeamName(team) },
                players = seats.map { form.players[it] },
                onPlayerChanged = { index, player -> form = form.withPlayer(seats[index], player) },
                playerSuggestions = { index ->
                    val seat = seats[index]
                    if (focused == FocusedField.Player(seat)) {
                        playerSuggestions(suggestions.players, form.players, seat)
                    } else {
                        emptyList()
                    }
                },
                onPlayerFocused = { index -> focused = FocusedField.Player(seats[index]) },
                isLastSection = team == 2,
            )
            Spacer(Modifier.height(24.dp))
        }

        if (hasDuplicatePerson) {
            Text(
                "Aynı ad iki kez yazılmış. Farklı kişilerse soyadlarını da yaz.",
                color = Color.Red,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                onStartClick(
                    EntranceOnboardingData(
                        team1Name = form.team1Name,
                        team2Name = form.team2Name,
                        players = form.players,
                    ),
                )
            },
            enabled = canStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Başla")
        }
    }
}

private fun teamNameSuggestions(known: List<String>, typed: String): List<String> {
    val typedKey = nameKey(typed)
    return known
        .filter { nameKey(it).startsWith(typedKey) && nameKey(it) != typedKey }
        .take(MaxSuggestions)
}

/**
 * Known people whose name starts with what's typed in [seat], minus anyone already at the
 * table. With an empty field, the regulars are offered.
 */
private fun playerSuggestions(known: List<KnownPlayer>, table: List<PlayerName>, seat: Int): List<KnownPlayer> {
    val typed = table[seat]
    val typedKey = nameKey(typed.displayName)
    val takenKeys = table.filterIndexed { other, player -> other != seat && player.firstName.isNotBlank() }
        .map { it.key }
        .toSet()
    return known
        .filter { it.name.key !in takenKeys && it.name.key != typed.key }
        .filter { nameKey(it.name.displayName).startsWith(typedKey) }
        .take(MaxSuggestions)
}

@Composable
private fun LineupRow(lineup: Lineup, onClick: () -> Unit) {
    val names = chipNames(lineup.players)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            "${names[0]} & ${names[1]} — ${names[2]} & ${names[3]}",
            color = Color.Black,
            fontSize = 15.sp,
        )
        Text(
            "${lineup.team1Name} — ${lineup.team2Name} · ${lineup.games} oyun",
            color = Color.Gray,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun TeamSection(
    teamLabel: String,
    teamName: String,
    onTeamNameChanged: (String) -> Unit,
    teamNameSuggestions: List<String>,
    onTeamNameFocused: () -> Unit,
    players: List<PlayerName>,
    onPlayerChanged: (index: Int, PlayerName) -> Unit,
    playerSuggestions: (index: Int) -> List<KnownPlayer>,
    onPlayerFocused: (index: Int) -> Unit,
    isLastSection: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(teamLabel, fontSize = LabelTextSize, color = Color.Gray)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = teamName,
            onValueChange = onTeamNameChanged,
            label = { Text("Takım adı") },
            singleLine = true,
            keyboardOptions = nameKeyboardOptions(ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) onTeamNameFocused() },
        )
        SuggestionStrip(
            items = teamNameSuggestions,
            label = { it },
            onPick = onTeamNameChanged,
        )

        players.forEachIndexed { index, player ->
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = player.firstName,
                    onValueChange = { onPlayerChanged(index, player.copy(firstName = it)) },
                    label = { Text("${index + 1}. oyuncu") },
                    singleLine = true,
                    keyboardOptions = nameKeyboardOptions(ImeAction.Next),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { if (it.isFocused) onPlayerFocused(index) },
                )
                OutlinedTextField(
                    value = player.lastName,
                    onValueChange = { onPlayerChanged(index, player.copy(lastName = it)) },
                    label = { Text("Soyad") },
                    singleLine = true,
                    keyboardOptions = nameKeyboardOptions(
                        if (isLastSection && index == players.lastIndex) ImeAction.Done else ImeAction.Next,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { if (it.isFocused) onPlayerFocused(index) },
                )
            }
            SuggestionStrip(
                items = playerSuggestions(index),
                label = { "${it.name.displayName} · ${it.games}" },
                onPick = { onPlayerChanged(index, it.name) },
            )
        }
    }
}

@Composable
private fun <T> SuggestionStrip(
    items: List<T>,
    label: (T) -> String,
    onPick: (T) -> Unit,
) {
    if (items.isEmpty()) return
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEach { item ->
            Text(
                label(item),
                color = Color.Black,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                    .clickable { onPick(item) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }
}

private fun nameKeyboardOptions(imeAction: ImeAction) = KeyboardOptions(
    capitalization = KeyboardCapitalization.Words,
    imeAction = imeAction,
)
