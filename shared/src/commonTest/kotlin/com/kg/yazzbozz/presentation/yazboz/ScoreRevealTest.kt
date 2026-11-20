package com.kg.yazzbozz.presentation.yazboz

import com.kg.yazzbozz.domain.GameRecord
import com.kg.yazzbozz.domain.Hand
import com.kg.yazzbozz.domain.Penalty
import com.kg.yazzbozz.domain.PlayerName
import com.kg.yazzbozz.domain.countSteps
import kotlinx.collections.immutable.persistentListOf
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScoreRevealTest {

    private fun game(hands: List<Hand>, penalties: List<Penalty> = emptyList()) = GameRecord(
        id = "g",
        startedAt = 0,
        finishedAt = 1,
        team1Name = "Biz",
        team2Name = "Onlar",
        players = listOf(PlayerName("K"), PlayerName("A"), PlayerName("Y"), PlayerName("F")),
        hands = hands,
        penalties = penalties,
    )

    /** [playReveal] never really suspends here (waits are counted, not slept), so it completes inline. */
    private fun runSync(block: suspend () -> Unit) {
        var outcome: Result<Unit>? = null
        block.startCoroutine(Continuation(EmptyCoroutineContext) { outcome = it })
        checkNotNull(outcome) { "suspended unexpectedly" }.getOrThrow()
    }

    private suspend fun play(record: GameRecord): Pair<List<ScoreRevealState>, Long> {
        val frames = mutableListOf<ScoreRevealState>()
        var waited = 0L
        playReveal(
            initial = record.startReveal(persistentListOf()),
            steps = record.countSteps(),
            emit = { frames += it },
            wait = { waited += it },
        )
        return frames to waited
    }

    @Test
    fun team1IsCountedCompletelyBeforeTeam2Starts() = runSync {
        val record = game(
            hands = listOf(Hand(scoreTeam1 = 40, scoreTeam2 = 87), Hand(scoreTeam1 = 132, scoreTeam2 = 0)),
            penalties = listOf(Penalty(2, 100)),
        )
        val (frames, _) = play(record)

        val counting = frames.filter { it.phase == RevealPhase.Counting && it.lastStep != null }
        assertEquals(listOf(40, 172, 172, 172), counting.map { it.shownTotal1 })
        assertEquals(listOf(0, 0, 87, 187), counting.map { it.shownTotal2 })
        assertTrue(counting.zipWithNext().all { (a, b) -> b.stepNumber == a.stepNumber + 1 })
    }

    @Test
    fun endsOnFinalTotalsWithWinnerThenRanking() = runSync {
        val record = game(hands = listOf(Hand(scoreTeam1 = 300, scoreTeam2 = 120)))
        val (frames, _) = play(record)

        val result = frames.first { it.phase == RevealPhase.Result }
        assertEquals(300 to 120, result.shownTotal1 to result.shownTotal2)
        assertEquals(2, result.winningTeam)
        assertEquals(RevealPhase.Ranking, frames.last().phase)
    }

    @Test
    fun aTwentyHandGameTakesAboutFifteenSecondsBeforeTheVerdict() = runSync {
        val hands = List(22) { Hand(scoreTeam1 = 10 + it, scoreTeam2 = 20 + it) }
        val penalties = listOf(Penalty(1, 100), Penalty(2, 100), Penalty(2, 55))
        val (_, waited) = play(game(hands, penalties))
        val untilVerdict = waited - 2_800 // the result hold comes after the verdict
        assertTrue(untilVerdict in 14_000..16_000, "took $untilVerdict ms")
    }

    @Test
    fun skippingMidCountLandsOnFinalTotalsAndRanking() = runSync {
        val record = game(hands = listOf(Hand(scoreTeam1 = 300, scoreTeam2 = 120), Hand(scoreTeam1 = 5, scoreTeam2 = 7)))
        val (frames, _) = play(record)
        val midCount = frames.first { it.phase == RevealPhase.Counting && it.lastStep != null }

        val skipped = midCount.skippedToEnd()
        assertEquals(305 to 127, skipped.shownTotal1 to skipped.shownTotal2)
        assertEquals(RevealPhase.Ranking, skipped.phase)
        assertEquals(null, skipped.countingTeam)
        assertEquals(null, skipped.lastStep)
        assertEquals(2, skipped.winningTeam)
    }

    @Test
    fun aVeryLongGameStillStaysUnderHalfAMinute() = runSync {
        val hands = List(40) { Hand(scoreTeam1 = 10 + it, scoreTeam2 = 20 + it) }
        val (_, waited) = play(game(hands))
        assertTrue(waited - 2_800 < 25_000, "took ${waited - 2_800} ms")
    }
}
