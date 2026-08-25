package com.kg.yazzbozz.presentation.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TitleTextSize = 22.sp
private val LabelTextSize = 14.sp

/**
 * Entrance screen. Collects team1Name, team2Name, and two player names per team.
 * Stateless — every field is a value + onChange lambda, same pattern as YazBozScreen.
 * [canStart] is computed by the caller (ViewModel) and just gates the button here.
 */
@Composable
fun EntranceScreen(
    team1Name: String,
    team2Name: String,
    player1Name: String,
    player2Name: String,
    player3Name: String,
    player4Name: String,
    canStart: Boolean,
    onTeam1NameChanged: (String) -> Unit,
    onTeam2NameChanged: (String) -> Unit,
    onPlayer1NameChanged: (String) -> Unit,
    onPlayer2NameChanged: (String) -> Unit,
    onPlayer3NameChanged: (String) -> Unit,
    onPlayer4NameChanged: (String) -> Unit,
    onStartClick: () -> Unit,
) {
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
        Text("Yazboz", fontSize = TitleTextSize, color = Color.Black)
        Spacer(Modifier.height(24.dp))

        TeamSection(
            teamLabel = "1. Takım",
            teamName = team1Name,
            onTeamNameChanged = onTeam1NameChanged,
            player1Name = player1Name,
            onPlayer1NameChanged = onPlayer1NameChanged,
            player2Name = player2Name,
            onPlayer2NameChanged = onPlayer2NameChanged,
        )

        Spacer(Modifier.height(24.dp))

        TeamSection(
            teamLabel = "2. Takım",
            teamName = team2Name,
            onTeamNameChanged = onTeam2NameChanged,
            player1Name = player3Name,
            onPlayer1NameChanged = onPlayer3NameChanged,
            player2Name = player4Name,
            onPlayer2NameChanged = onPlayer4NameChanged,
            lastFieldImeAction = ImeAction.Done,
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onStartClick,
            enabled = canStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Başla")
        }
    }
}

@Composable
private fun TeamSection(
    teamLabel: String,
    teamName: String,
    onTeamNameChanged: (String) -> Unit,
    player1Name: String,
    onPlayer1NameChanged: (String) -> Unit,
    player2Name: String,
    onPlayer2NameChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    lastFieldImeAction: ImeAction = ImeAction.Next,
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
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = player1Name,
                onValueChange = onPlayer1NameChanged,
                label = { Text("1. oyuncu") },
                singleLine = true,
                keyboardOptions = nameKeyboardOptions(ImeAction.Next),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = player2Name,
                onValueChange = onPlayer2NameChanged,
                label = { Text("2. oyuncu") },
                singleLine = true,
                keyboardOptions = nameKeyboardOptions(lastFieldImeAction),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun nameKeyboardOptions(imeAction: ImeAction) = KeyboardOptions(
    capitalization = KeyboardCapitalization.Words,
    imeAction = imeAction,
)