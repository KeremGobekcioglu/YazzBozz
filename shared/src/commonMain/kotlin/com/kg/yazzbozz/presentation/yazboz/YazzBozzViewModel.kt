package com.kg.yazzbozz.presentation.yazboz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kg.yazzbozz.data.GameRepository
import com.kg.yazzbozz.domain.GameRecord
import com.kg.yazzbozz.domain.Hand
import com.kg.yazzbozz.domain.Penalty
import com.kg.yazzbozz.domain.PlayerName
import com.kg.yazzbozz.domain.cleanName
import com.kg.yazzbozz.domain.countSteps
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class StartState { Loading, Entrance, Ongoing }

/**
 * Holds the sheet being played. Every change is written through to [repository] so the
 * game survives the process being killed.
 */
class YazzBozzViewModel(
    private val repository: GameRepository,
) : ViewModel() {

    var startState by mutableStateOf(StartState.Loading)
        private set

    var uiState by mutableStateOf(YazBozUiState())
        private set

    /** Non-null while the end-of-game count/celebration is on screen. */
    var reveal: ScoreRevealState? by mutableStateOf(null)
        private set

    private var game: GameRecord? = null
    private var revealJob: Job? = null

    init {
        viewModelScope.launch {
            val ongoing = repository.loadOngoing()
            if (ongoing != null) show(ongoing)
            startState = if (ongoing != null) StartState.Ongoing else StartState.Entrance
        }
    }

    fun startNewGame(team1Name: String, team2Name: String, players: List<PlayerName>) {
        clearReveal()
        game?.takeUnless { it.isFinished }?.let { repository.delete(it.id) }
        update(
            GameRecord(
                id = newGameId(),
                startedAt = nowMillis(),
                team1Name = cleanName(team1Name),
                team2Name = cleanName(team2Name),
                players = players.map { PlayerName(cleanName(it.firstName), cleanName(it.lastName)) },
            ),
        )
    }

    fun onConfirmHand(hand: Hand) {
        val current = game?.takeUnless { it.isFinished } ?: return
        // The finishing team's field is locked to 0 in the panel; store what it shows.
        val written = when (hand.finishingTeam) {
            1 -> hand.copy(scoreTeam1 = 0)
            2 -> hand.copy(scoreTeam2 = 0)
            else -> hand
        }
        update(current.copy(hands = current.hands + written))
    }

    fun onAddPenalty(team: Int, value: Int, label: String? = null) {
        val current = game?.takeUnless { it.isFinished } ?: return
        update(current.copy(penalties = current.penalties + Penalty(team = team, value = value, label = label)))
    }

    fun onFinishClicked() {
        val current = game?.takeUnless { it.isFinished } ?: return
        val finished = current.copy(finishedAt = nowMillis())
        update(finished)
        // Saved first; the scene is only a show on top of an already-finished game.
        revealJob?.cancel()
        revealJob = viewModelScope.launch {
            playReveal(
                initial = finished.startReveal(uiState.ranking),
                steps = finished.countSteps(),
                emit = { reveal = it },
            )
        }
    }

    /** Only offered once the ranking is up; the count itself always plays to the end. */
    fun onRevealClosed() {
        clearReveal()
    }

    private fun clearReveal() {
        revealJob?.cancel()
        revealJob = null
        reveal = null
    }

    /** "Yeni Oyun" after finishing: same table, fresh sheet. */
    fun onNewGameClicked() {
        val current = game ?: return
        startNewGame(current.team1Name, current.team2Name, current.players)
    }

    /** Leaves the current sheet to go back to the entrance; an unfinished sheet is discarded. */
    fun onAbandonGame() {
        clearReveal()
        game?.takeUnless { it.isFinished }?.let { repository.delete(it.id) }
        game = null
        uiState = YazBozUiState()
    }

    private fun update(record: GameRecord) {
        show(record)
        repository.save(record)
    }

    private fun show(record: GameRecord) {
        game = record
        uiState = record.toUiState()
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun newGameId(): String = Uuid.random().toString()

@OptIn(ExperimentalTime::class)
internal fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
