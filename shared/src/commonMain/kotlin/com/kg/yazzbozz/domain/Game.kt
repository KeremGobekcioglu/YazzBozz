package com.kg.yazzbozz.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.absoluteValue

/**
 * A yazboz sheet. Everything in it is what the players wrote down — the app only stores,
 * sums and displays these numbers, it never derives a score.
 *
 * Seats: 0,1 are team 1; 2,3 are team 2.
 */
data class GameRecord(
    val id: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val team1Name: String,
    val team2Name: String,
    val players: List<PlayerName>,
    val hands: List<Hand> = emptyList(),
    val penalties: List<Penalty> = emptyList(),
) {
    val isFinished: Boolean get() = finishedAt != null

    /**
     * Every number on a yazboz is points against, so a minus written in front of one doesn't
     * lower the total — it's counted as positive like the rest (the higher total loses).
     */
    fun total(team: Int): Int =
        hands.sumOf { (if (team == 1) it.scoreTeam1 else it.scoreTeam2).absoluteValue } +
            penalties.filter { it.team == team }.sumOf { it.value.absoluteValue }
}

/** A finished game as listed in the history; totals are the ones stored at "Bitir". */
data class GameSummary(
    val id: String,
    val finishedAt: Long,
    val team1Name: String,
    val team2Name: String,
    val players: List<PlayerName>,
    val totalTeam1: Int,
    val totalTeam2: Int,
)

data class PlayerName(
    val firstName: String,
    val lastName: String = "",
) {
    val displayName: String get() = if (lastName.isBlank()) firstName else "$firstName $lastName"
    val key: String get() = personKey(firstName, lastName)
}

data class Hand(
    val scoreTeam1: Int = 0,
    val scoreTeam2: Int = 0,
    val finisherSeat: Int? = null,
    val openerSeats: ImmutableList<Int> = persistentListOf(),
) {
    val finishingTeam: Int? get() = finisherSeat?.let(::teamOfSeat)
}

/** [label] is only a note of why, as on paper; it never affects [value]. */
data class Penalty(
    val team: Int,
    val value: Int,
    val label: String? = null,
)

/** Ready-made reasons for a penalty. Picking one is optional. */
val PenaltyReasons: List<String> = listOf(
    "Yandan taş alma",
    "Okey atma",
    "Elde okey kalması",
    "Yanlış açma",
    "Diğer",
)

fun teamOfSeat(seat: Int): Int = if (seat < 2) 1 else 2
