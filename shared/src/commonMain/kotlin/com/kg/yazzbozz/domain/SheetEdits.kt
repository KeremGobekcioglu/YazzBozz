package com.kg.yazzbozz.domain

import kotlinx.collections.immutable.toImmutableList

/**
 * A hand as it goes on the sheet: the finishing team wrote 0, and the finisher is among the
 * openers — nobody finishes without having opened, whether or not it was ticked.
 */
fun Hand.normalized(): Hand {
    val zeroed = when (finishingTeam) {
        1 -> copy(scoreTeam1 = 0)
        2 -> copy(scoreTeam2 = 0)
        else -> this
    }
    val finisher = finisherSeat ?: return zeroed
    if (finisher in openerSeats) return zeroed
    return zeroed.copy(openerSeats = (openerSeats + finisher).sorted().toImmutableList())
}

/** Whether [seat] opened in this hand. The finisher always did, so older sheets count right too. */
fun Hand.opened(seat: Int): Boolean = seat in openerSeats || seat == finisherSeat

/** The sheet with hand [index] replaced by [hand], or crossed out when [hand] is null. */
fun GameRecord.withHand(index: Int, hand: Hand?): GameRecord {
    if (index !in hands.indices) return this
    val updated = if (hand == null) {
        hands.filterIndexed { position, _ -> position != index }
    } else {
        hands.mapIndexed { position, existing -> if (position == index) hand else existing }
    }
    return copy(hands = updated)
}

/**
 * The sheet with the [indexInTeam]-th penalty written for [team] replaced by [penalty], or
 * crossed out when [penalty] is null. Penalties are listed per team on screen, hence the index.
 */
fun GameRecord.withPenalty(team: Int, indexInTeam: Int, penalty: Penalty?): GameRecord {
    val index = penalties.withIndex().filter { it.value.team == team }.getOrNull(indexInTeam)?.index ?: return this
    val updated = if (penalty == null) {
        penalties.filterIndexed { position, _ -> position != index }
    } else {
        penalties.mapIndexed { position, existing -> if (position == index) penalty.copy(team = team) else existing }
    }
    return copy(penalties = updated)
}
