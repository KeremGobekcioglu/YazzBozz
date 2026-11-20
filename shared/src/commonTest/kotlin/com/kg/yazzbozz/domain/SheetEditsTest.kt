package com.kg.yazzbozz.domain

import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SheetEditsTest {

    private val game = GameRecord(
        id = "g",
        startedAt = 0,
        team1Name = "Biz",
        team2Name = "Onlar",
        players = listOf(PlayerName("Kerem"), PlayerName("Ali"), PlayerName("Ayşe"), PlayerName("Fatma")),
        hands = listOf(
            Hand(scoreTeam1 = 40, scoreTeam2 = 87),
            Hand(scoreTeam1 = 132, scoreTeam2 = 0, finisherSeat = 3),
            Hand(scoreTeam1 = -101, scoreTeam2 = 55),
        ),
        penalties = listOf(
            Penalty(team = 1, value = 100),
            Penalty(team = 2, value = 37, label = "Okey atma"),
            Penalty(team = 1, value = 101),
            Penalty(team = 2, value = 100),
        ),
    )

    // ------------------------------------------------------------ normalized

    @Test
    fun finisherIsAddedToOpenersWhenNotTicked() {
        val hand = Hand(finisherSeat = 2, openerSeats = persistentListOf(0, 3)).normalized()
        assertEquals(listOf(0, 2, 3), hand.openerSeats)
    }

    @Test
    fun finisherAlreadyTickedIsNotListedTwice() {
        val hand = Hand(finisherSeat = 1, openerSeats = persistentListOf(1)).normalized()
        assertEquals(listOf(1), hand.openerSeats)
    }

    @Test
    fun finishingTeamsScoreIsWrittenAsZero() {
        assertEquals(0, Hand(scoreTeam1 = 50, scoreTeam2 = 80, finisherSeat = 0).normalized().scoreTeam1)
        assertEquals(80, Hand(scoreTeam1 = 50, scoreTeam2 = 80, finisherSeat = 0).normalized().scoreTeam2)
        assertEquals(0, Hand(scoreTeam1 = 50, scoreTeam2 = 80, finisherSeat = 3).normalized().scoreTeam2)
    }

    @Test
    fun handWithoutFinisherIsKeptAsWritten() {
        val hand = Hand(scoreTeam1 = -20, scoreTeam2 = 80, openerSeats = persistentListOf(1))
        assertEquals(hand, hand.normalized())
    }

    @Test
    fun finisherCountsAsOpenerEvenOnOldSheets() {
        val oldHand = Hand(finisherSeat = 2, openerSeats = persistentListOf(0))
        assertEquals(listOf(true, false, true, false), (0..3).map { oldHand.opened(it) })
    }

    // ------------------------------------------------------------ hands

    @Test
    fun handIsCorrectedInPlace() {
        val corrected = game.withHand(1, Hand(scoreTeam1 = 13, scoreTeam2 = 0, finisherSeat = 2))
        assertEquals(listOf(40, 13, -101), corrected.hands.map { it.scoreTeam1 })
        assertEquals(2, corrected.hands[1].finisherSeat)
        assertEquals(40 + 13 + 101 + 100 + 101, corrected.total(1)) // -101 counts as 101
    }

    @Test
    fun handIsCrossedOut() {
        val crossed = game.withHand(0, null)
        assertEquals(listOf(132, -101), crossed.hands.map { it.scoreTeam1 })
        assertEquals(0 + 55 + 37 + 100, crossed.total(2))
        assertEquals(132 + 101 + 100 + 101, crossed.total(1))
    }

    @Test
    fun unknownHandIndexChangesNothing() {
        assertSame(game, game.withHand(3, null))
        assertSame(game, game.withHand(-1, Hand()))
    }

    // ------------------------------------------------------------ penalties

    @Test
    fun penaltyIndexIsCountedWithinItsTeam() {
        // Team 2's second penalty is the fourth on the sheet.
        val corrected = game.withPenalty(team = 2, indexInTeam = 1, penalty = Penalty(team = 2, value = 200))
        assertEquals(listOf(100, 37, 101, 200), corrected.penalties.map { it.value })
    }

    @Test
    fun penaltyIsCrossedOutWithoutTouchingTheOtherTeam() {
        val crossed = game.withPenalty(team = 1, indexInTeam = 0, penalty = null)
        assertEquals(listOf(37, 101, 100), crossed.penalties.map { it.value })
        assertEquals(listOf(2, 1, 2), crossed.penalties.map { it.team })
    }

    @Test
    fun correctedPenaltyStaysWithItsTeam() {
        val corrected = game.withPenalty(team = 1, indexInTeam = 1, penalty = Penalty(team = 2, value = 50, label = "Diğer"))
        assertEquals(Penalty(team = 1, value = 50, label = "Diğer"), corrected.penalties[2])
    }

    @Test
    fun unknownPenaltyIndexChangesNothing() {
        assertSame(game, game.withPenalty(team = 2, indexInTeam = 2, penalty = null))
        assertTrue(game.withPenalty(team = 1, indexInTeam = 5, penalty = null).penalties.size == 4)
    }
}
