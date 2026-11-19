package com.kg.yazzbozz.presentation.results

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kg.yazzbozz.data.GameRepository
import com.kg.yazzbozz.domain.GameSummary
import com.kg.yazzbozz.domain.HistoryFilter
import com.kg.yazzbozz.domain.HistoryRecord
import com.kg.yazzbozz.domain.filteredBy
import com.kg.yazzbozz.domain.playerRecords
import com.kg.yazzbozz.domain.teamRecords
import com.kg.yazzbozz.platform.formatGameDate
import com.kg.yazzbozz.presentation.yazboz.YazBozUiState
import com.kg.yazzbozz.presentation.yazboz.toUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreviousResultsUiState(
    val games: ImmutableList<GameSummary>,
    val players: ImmutableList<HistoryRecord>,
    val teams: ImmutableList<HistoryRecord>,
    val selected: HistoryRecord?,
)

class PreviousResultsViewModel(repository: GameRepository) : ViewModel() {

    private val selectedFilter = MutableStateFlow<HistoryFilter?>(null)

    /** Null until the first read lands. */
    val uiState: StateFlow<PreviousResultsUiState?> =
        combine(repository.observeHistory(), selectedFilter) { history, filter ->
            val players = playerRecords(history)
            val teams = teamRecords(history)
            PreviousResultsUiState(
                games = history.filteredBy(filter).toImmutableList(),
                players = players.toImmutableList(),
                teams = teams.toImmutableList(),
                selected = (players + teams).firstOrNull { it.filter == filter },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Selecting the active filter again clears it. */
    fun onFilterClick(filter: HistoryFilter) {
        selectedFilter.update { if (it == filter) null else filter }
    }
}

class PreviousGameViewModel(
    repository: GameRepository,
    gameId: String,
) : ViewModel() {

    var uiState: YazBozUiState? by mutableStateOf(null)
        private set

    var date: String? by mutableStateOf(null)
        private set

    init {
        viewModelScope.launch {
            val game = repository.loadGame(gameId) ?: return@launch
            date = game.finishedAt?.let(::formatGameDate)
            uiState = game.toUiState()
        }
    }
}
