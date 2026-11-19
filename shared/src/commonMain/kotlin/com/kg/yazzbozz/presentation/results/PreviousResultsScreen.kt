package com.kg.yazzbozz.presentation.results

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kg.yazzbozz.domain.GameSummary
import com.kg.yazzbozz.domain.HistoryFilter
import com.kg.yazzbozz.domain.HistoryRecord
import com.kg.yazzbozz.platform.formatGameDate
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PreviousResultsScreen(
    uiState: PreviousResultsUiState?,
    onFilterClick: (HistoryFilter) -> Unit,
    onGameClick: (gameId: String) -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBackClick) { Text("‹ Geri") }
        }
        Text(
            "Geçmiş",
            color = Color.Black,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        when {
            uiState == null -> Unit
            uiState.players.isEmpty() -> Text(
                "Henüz biten oyun yok.",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp),
            )
            else -> {
                FilterRow("Oyuncu", uiState.players, uiState.selected, onFilterClick)
                FilterRow("Takım", uiState.teams, uiState.selected, onFilterClick)

                uiState.selected?.let { record ->
                    Text(
                        "${record.label}: ${record.games} oyun, ${record.wins} galibiyet",
                        color = Color.Black,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                HorizontalDivider()

                LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                    items(uiState.games, key = { it.id }) { game ->
                        GameSummaryRow(game, onClick = { onGameClick(game.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    title: String,
    records: ImmutableList<HistoryRecord>,
    selected: HistoryRecord?,
    onClick: (HistoryFilter) -> Unit,
) {
    if (records.isEmpty()) return
    Text(
        title,
        color = Color.Gray,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
    )
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        records.forEach { record ->
            val isSelected = record.filter == selected?.filter
            Text(
                "${record.label} · ${record.games}",
                color = Color.Black,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Color.Black else Color.LightGray,
                        shape = RoundedCornerShape(6.dp),
                    )
                    .clickable { onClick(record.filter) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
internal fun GameSummaryRow(game: GameSummary, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(formatGameDate(game.finishedAt), color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                game.team1Name,
                Modifier.weight(1f),
                color = Color.Black,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${game.totalTeam1}  —  ${game.totalTeam2}",
                Modifier.padding(horizontal = 8.dp),
                color = Color.Black,
                fontSize = 18.sp,
            )
            Text(
                game.team2Name,
                Modifier.weight(1f),
                color = Color.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(lineupText(game), color = Color.Gray, fontSize = 13.sp)
    }
}

internal fun lineupText(game: GameSummary): String {
    val names = game.players.map { it.displayName }
    if (names.size < 4) return names.joinToString(", ")
    return "${names[0]} & ${names[1]} — ${names[2]} & ${names[3]}"
}
