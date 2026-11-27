package com.kg.yazzbozz.domain

import kotlin.math.absoluteValue

/** Lower total wins (scores on a yazboz are points against); a tie has no winner. */
fun winningTeam(totalTeam1: Int, totalTeam2: Int): Int? = when {
    totalTeam1 < totalTeam2 -> 1
    totalTeam2 < totalTeam1 -> 2
    else -> null
}

fun GameRecord.winningTeam(): Int? = winningTeam(total(1), total(2))

// ---------------------------------------------------------------- day ranking

enum class DayTitle { Carrier, Rat }

/**
 * One player's line in the day's ranking. [score] = openings + [finishPoints], where a normal
 * finish is worth 2 and each finish kind (elden, okey, çifte) doubles it.
 */
data class PlayerRank(
    val seat: Int,
    val name: String,
    val openings: Int,
    val finishes: Int,
    val title: DayTitle?,
    val finishPoints: Int = FinishWeight * finishes,
) {
    val score: Int get() = openings + finishPoints
}

private const val FinishWeight = 2

/** What this hand's finish adds to the finisher's ranking: 2, doubled per kind (elden+okey = 8). */
val Hand.finishPoints: Int
    get() = if (finisherSeat == null) 0 else FinishWeight shl finishKinds.size

/**
 * Players ordered by score, highest first. Whoever has the top score is "günün taşıyanı",
 * the bottom score "günün sıçanı" — shared on a tie, and nobody gets a title when
 * everyone scored the same. [finishPoints] defaults to every finish being a normal one.
 */
fun dayRanking(
    names: List<String>,
    openings: List<Int>,
    finishes: List<Int>,
    finishPoints: List<Int> = finishes.map { FinishWeight * it },
): List<PlayerRank> {
    val ranks = names.indices.map { seat ->
        PlayerRank(
            seat = seat,
            name = names[seat],
            openings = openings.getOrElse(seat) { 0 },
            finishes = finishes.getOrElse(seat) { 0 },
            title = null,
            finishPoints = finishPoints.getOrElse(seat) { 0 },
        )
    }
    val best = ranks.maxOfOrNull { it.score } ?: return ranks
    val worst = ranks.minOf { it.score }
    return ranks
        .map { rank ->
            rank.copy(
                title = when {
                    best == worst -> null
                    rank.score == best -> DayTitle.Carrier
                    rank.score == worst -> DayTitle.Rat
                    else -> null
                },
            )
        }
        .sortedWith(compareByDescending<PlayerRank> { it.score }.thenBy { it.seat })
}

// ---------------------------------------------------------------- hand gaps

/**
 * How far apart the teams were in one hand ([gap], [aheadTeam] = the lower score) and on the
 * sheet after it ([runningGap], [runningAheadTeam]). Hands only — penalties belong to no hand.
 * A null team means level.
 */
data class HandGap(
    val gap: Int,
    val aheadTeam: Int?,
    val runningGap: Int,
    val runningAheadTeam: Int?,
)

fun GameRecord.handGaps(): List<HandGap> {
    var sum1 = 0
    var sum2 = 0
    return hands.map { hand ->
        val s1 = hand.scoreTeam1.absoluteValue
        val s2 = hand.scoreTeam2.absoluteValue
        sum1 += s1
        sum2 += s2
        HandGap(
            gap = (s1 - s2).absoluteValue,
            aheadTeam = winningTeam(s1, s2),
            runningGap = (sum1 - sum2).absoluteValue,
            runningAheadTeam = winningTeam(sum1, sum2),
        )
    }
}

// ---------------------------------------------------------------- counting steps

/** One number being added to a team's total during the end-of-game count. */
data class CountStep(
    val team: Int,
    val delta: Int,
    val isPenalty: Boolean,
    val label: String? = null,
)

/**
 * What gets counted, in order: team 1's hands then its penalties, then team 2's. Hands where
 * the team wrote 0 are skipped — they don't move the total and would only slow the count.
 * Every step adds, as in [GameRecord.total]: a negative number counts as positive.
 */
fun GameRecord.countSteps(): List<CountStep> = listOf(1, 2).flatMap { team ->
    val handSteps = hands
        .map { (if (team == 1) it.scoreTeam1 else it.scoreTeam2).absoluteValue }
        .filter { it != 0 }
        .map { CountStep(team = team, delta = it, isPenalty = false) }
    val penaltySteps = penalties
        .filter { it.team == team }
        .map { CountStep(team = team, delta = it.value.absoluteValue, isPenalty = true, label = it.label) }
    handSteps + penaltySteps
}
