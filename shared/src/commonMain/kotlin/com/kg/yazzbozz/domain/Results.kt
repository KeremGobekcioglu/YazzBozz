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

/** One player's line in the day's ranking. [score] = openings + 2 × finishes. */
data class PlayerRank(
    val seat: Int,
    val name: String,
    val openings: Int,
    val finishes: Int,
    val title: DayTitle?,
) {
    val score: Int get() = openings + FinishWeight * finishes
}

private const val FinishWeight = 2

/**
 * Players ordered by score, highest first. Whoever has the top score is "günün taşıyanı",
 * the bottom score "günün sıçanı" — shared on a tie, and nobody gets a title when
 * everyone scored the same.
 */
fun dayRanking(names: List<String>, openings: List<Int>, finishes: List<Int>): List<PlayerRank> {
    val ranks = names.indices.map { seat ->
        PlayerRank(
            seat = seat,
            name = names[seat],
            openings = openings.getOrElse(seat) { 0 },
            finishes = finishes.getOrElse(seat) { 0 },
            title = null,
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
