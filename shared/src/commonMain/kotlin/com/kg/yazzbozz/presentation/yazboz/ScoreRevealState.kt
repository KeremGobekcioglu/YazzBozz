package com.kg.yazzbozz.presentation.yazboz

import com.kg.yazzbozz.domain.CountStep
import com.kg.yazzbozz.domain.GameRecord
import com.kg.yazzbozz.domain.PlayerRank
import com.kg.yazzbozz.domain.winningTeam
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay

enum class RevealPhase {
    /** Totals tick up one hand / penalty at a time, team 1 first. */
    Counting,

    /** Final totals, winner celebrated, loser mocked. */
    Result,

    /** Result stays up and the day's player ranking appears under it. */
    Ranking,
}

/**
 * The end-of-game scene. Lives in the ViewModel so a rotation mid-count picks up where it
 * was instead of starting over; nothing here is persisted — the game itself already is.
 */
data class ScoreRevealState(
    val team1Name: String,
    val team2Name: String,
    val finalTotal1: Int,
    val finalTotal2: Int,
    val winningTeam: Int?,
    val ranking: ImmutableList<PlayerRank>,
    val shownTotal1: Int = 0,
    val shownTotal2: Int = 0,
    val countingTeam: Int? = null,
    val lastStep: CountStep? = null,
    /** Bumped on every step so the "+87" pop replays even when two steps are equal. */
    val stepNumber: Int = 0,
    val phase: RevealPhase = RevealPhase.Counting,
) {
    fun shownTotal(team: Int): Int = if (team == 1) shownTotal1 else shownTotal2
}

internal fun GameRecord.startReveal(ranking: ImmutableList<PlayerRank>) = ScoreRevealState(
    team1Name = team1Name,
    team2Name = team2Name,
    finalTotal1 = total(1),
    finalTotal2 = total(2),
    winningTeam = winningTeam(),
    ranking = ranking,
)

/** Back during the count: jump straight to the final totals with the ranking up. */
internal fun ScoreRevealState.skippedToEnd() = copy(
    shownTotal1 = finalTotal1,
    shownTotal2 = finalTotal2,
    countingTeam = null,
    lastStep = null,
    phase = RevealPhase.Ranking,
)

private const val IntroMillis = 800L
private const val TeamBudgetMillis = 6_500L
private const val MinStepMillis = 260L
private const val MaxStepMillis = 1_200L
private const val EmptyTeamMillis = 900L
private const val BetweenTeamsMillis = 700L
private const val ResultHoldMillis = 2_800L

/**
 * Runs the ~15 s timeline (for a 20+ hand game), publishing each frame of state through [emit]. Each team gets
 * about [TeamBudgetMillis]; steps are spread evenly within it, clamped so a long game
 * doesn't flicker and a short one doesn't drag.
 */
internal suspend fun playReveal(
    initial: ScoreRevealState,
    steps: List<CountStep>,
    emit: (ScoreRevealState) -> Unit,
    wait: suspend (millis: Long) -> Unit = { delay(it) },
) {
    var state = initial
    fun publish(next: ScoreRevealState) {
        state = next
        emit(next)
    }

    publish(state)
    wait(IntroMillis)

    for (team in 1..2) {
        val teamSteps = steps.filter { it.team == team }
        publish(state.copy(countingTeam = team, lastStep = null))
        if (teamSteps.isEmpty()) {
            wait(EmptyTeamMillis)
        } else {
            val stepMillis = (TeamBudgetMillis / teamSteps.size).coerceIn(MinStepMillis, MaxStepMillis)
            teamSteps.forEach { step ->
                publish(
                    state.copy(
                        shownTotal1 = state.shownTotal1 + if (team == 1) step.delta else 0,
                        shownTotal2 = state.shownTotal2 + if (team == 2) step.delta else 0,
                        lastStep = step,
                        stepNumber = state.stepNumber + 1,
                    ),
                )
                wait(stepMillis)
            }
        }
        wait(BetweenTeamsMillis)
    }

    publish(
        state.copy(
            shownTotal1 = state.finalTotal1,
            shownTotal2 = state.finalTotal2,
            countingTeam = null,
            lastStep = null,
            phase = RevealPhase.Result,
        ),
    )
    wait(ResultHoldMillis)
    publish(state.copy(phase = RevealPhase.Ranking))
}
