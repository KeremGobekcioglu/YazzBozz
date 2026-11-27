package com.kg.yazzbozz.domain

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResultsTest {

    @Test
    fun lowerTotalWins() {
        assertEquals(1, winningTeam(100, 200))
        assertEquals(2, winningTeam(300, 50))
        assertNull(winningTeam(150, 150))
    }

    @Test
    fun rankingWeighsAFinishAsTwoOpenings() {
        val ranking = dayRanking(
            names = listOf("Kerem", "Ali", "Ayşe", "Fatma"),
            openings = listOf(3, 1, 2, 0),
            finishes = listOf(0, 2, 1, 0),
        )
        assertEquals(listOf("Ali", "Ayşe", "Kerem", "Fatma"), ranking.map { it.name })
        assertEquals(listOf(5, 4, 3, 0), ranking.map { it.score })
        assertEquals(listOf(DayTitle.Carrier, null, null, DayTitle.Rat), ranking.map { it.title })
    }

    @Test
    fun eachFinishKindDoublesTheFinish() {
        fun points(vararg kinds: FinishKind) = Hand(finisherSeat = 0, finishKinds = persistentSetOf(*kinds)).finishPoints
        assertEquals(2, points())
        assertEquals(4, points(FinishKind.Elden))
        assertEquals(4, points(FinishKind.Cifte))
        assertEquals(8, points(FinishKind.Elden, FinishKind.Okey))
        assertEquals(0, Hand().finishPoints)
    }

    @Test
    fun rankingUsesFinishPointsWhenGiven() {
        val ranking = dayRanking(
            names = listOf("A", "B", "C", "D"),
            openings = listOf(1, 1, 0, 0),
            finishes = listOf(1, 1, 0, 0),
            finishPoints = listOf(2, 8, 0, 0),
        )
        assertEquals(listOf("B", "A", "C", "D"), ranking.map { it.name })
        assertEquals(listOf(9, 3, 0, 0), ranking.map { it.score })
        assertEquals(listOf(1, 1, 0, 0), ranking.map { it.finishes })
    }

    @Test
    fun handGapsShowEachHandAndTheRunningGap() {
        val game = GameRecord(
            id = "g",
            startedAt = 0,
            team1Name = "Biz",
            team2Name = "Onlar",
            players = listOf(PlayerName("K"), PlayerName("A"), PlayerName("Y"), PlayerName("F")),
            hands = listOf(
                Hand(scoreTeam1 = 0, scoreTeam2 = 87),
                Hand(scoreTeam1 = 132, scoreTeam2 = -20), // -20 counts as 20
                Hand(scoreTeam1 = 25, scoreTeam2 = 25),
            ),
            penalties = listOf(Penalty(1, 100)), // not part of any hand
        )
        assertEquals(
            listOf(
                HandGap(gap = 87, aheadTeam = 1, runningGap = 87, runningAheadTeam = 1),
                HandGap(gap = 112, aheadTeam = 2, runningGap = 25, runningAheadTeam = 2),
                HandGap(gap = 0, aheadTeam = null, runningGap = 25, runningAheadTeam = 2),
            ),
            game.handGaps(),
        )
    }

    @Test
    fun sharedTopAndBottomScoresShareTheTitle() {
        val ranking = dayRanking(listOf("A", "B", "C", "D"), listOf(2, 2, 0, 0), listOf(0, 0, 0, 0))
        assertEquals(listOf(DayTitle.Carrier, DayTitle.Carrier, DayTitle.Rat, DayTitle.Rat), ranking.map { it.title })
    }

    @Test
    fun nobodyGetsATitleWhenEveryoneIsEqual() {
        val ranking = dayRanking(listOf("A", "B", "C", "D"), listOf(1, 1, 1, 1), listOf(0, 0, 0, 0))
        assertEquals(listOf(null, null, null, null), ranking.map { it.title })
    }

    @Test
    fun countStepsGoTeamByTeamHandsThenPenaltiesSkippingZerosAllAdding() {
        val game = GameRecord(
            id = "g",
            startedAt = 0,
            team1Name = "Biz",
            team2Name = "Onlar",
            players = listOf(PlayerName("K"), PlayerName("A"), PlayerName("Y"), PlayerName("F")),
            hands = listOf(
                Hand(scoreTeam1 = 0, scoreTeam2 = 87, finisherSeat = 1, openerSeats = persistentListOf(1)),
                Hand(scoreTeam1 = 132, scoreTeam2 = -101),
            ),
            penalties = listOf(Penalty(2, 100, "Okey atma"), Penalty(1, 55)),
        )
        assertEquals(
            listOf(
                CountStep(team = 1, delta = 132, isPenalty = false),
                CountStep(team = 1, delta = 55, isPenalty = true),
                CountStep(team = 2, delta = 87, isPenalty = false),
                CountStep(team = 2, delta = 101, isPenalty = false), // written as -101, counted as 101
                CountStep(team = 2, delta = 100, isPenalty = true, label = "Okey atma"),
            ),
            game.countSteps(),
        )
        assertEquals(game.total(1), game.countSteps().filter { it.team == 1 }.sumOf { it.delta })
        assertEquals(game.total(2), game.countSteps().filter { it.team == 2 }.sumOf { it.delta })
    }
}
