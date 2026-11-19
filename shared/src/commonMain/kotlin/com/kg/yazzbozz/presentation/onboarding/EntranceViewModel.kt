package com.kg.yazzbozz.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kg.yazzbozz.data.GameRepository
import com.kg.yazzbozz.domain.KnownPlayer
import com.kg.yazzbozz.domain.Lineup
import com.kg.yazzbozz.domain.knownPlayers
import com.kg.yazzbozz.domain.knownTeamNames
import com.kg.yazzbozz.domain.lineups
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class EntranceSuggestions(
    val players: ImmutableList<KnownPlayer> = persistentListOf(),
    val teamNames: ImmutableList<String> = persistentListOf(),
    val lineups: ImmutableList<Lineup> = persistentListOf(),
)

class EntranceViewModel(repository: GameRepository) : ViewModel() {

    val suggestions: StateFlow<EntranceSuggestions> = repository.observeHistory()
        .map { history ->
            EntranceSuggestions(
                players = knownPlayers(history).toImmutableList(),
                teamNames = knownTeamNames(history).toImmutableList(),
                lineups = lineups(history).toImmutableList(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntranceSuggestions())
}
