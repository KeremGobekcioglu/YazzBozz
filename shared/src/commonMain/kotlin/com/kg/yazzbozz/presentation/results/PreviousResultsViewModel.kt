package com.kg.yazzbozz.presentation.results

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kg.yazzbozz.data.GameRepository
import com.kg.yazzbozz.domain.GameSummary
import com.kg.yazzbozz.platform.formatGameDate
import com.kg.yazzbozz.presentation.yazboz.YazBozUiState
import com.kg.yazzbozz.presentation.yazboz.toUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreviousResultsViewModel(repository: GameRepository) : ViewModel() {

    /** Null until the first read lands. */
    val games: StateFlow<ImmutableList<GameSummary>?> = repository.observeHistory()
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
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
