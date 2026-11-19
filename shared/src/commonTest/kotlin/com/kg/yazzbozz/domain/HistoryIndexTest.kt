package com.kg.yazzbozz.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryIndexTest {

    private fun game(
        id: String,
        players: List<PlayerName>,
        team1: String = "Biz",
        team2: String = "Onlar",
        totals: Pair<Int, Int> = 0 to 0,
    ) = GameSummary(
        id = id,
        finishedAt = 0,
        team1Name = team1,
        team2Name = team2,
        players = players,
        totalTeam1 = totals.first,
        totalTeam2 = totals.second,
    )

    private val kerem = PlayerName("Kerem")
    private val ali = PlayerName("Ali")
    private val ayse = PlayerName("Ayşe")
    private val fatma = PlayerName("Fatma")
    private val ilker = PlayerName("İlker")

    // Newest first, like the repository returns it.
    private val history = listOf(
        // Same people, seats and teams swapped around, different spelling.
        game("3", listOf(PlayerName("ayşe "), fatma, PlayerName("ALİ"), kerem), team1 = "Kızlar", team2 = "Erkekler"),
        game("2", listOf(kerem, ali, ayse, fatma)),
        game("1", listOf(kerem, ilker, ayse, fatma)),
    )

    @Test
    fun playersAreMatchedAcrossSpellings() {
        val players = knownPlayers(history)
        assertEquals(listOf(3, 3, 3, 2, 1), players.map { it.games })
        assertEquals("ayşe ", players.first { it.name.key == ayse.key }.name.firstName, "most recent spelling")
        assertEquals(1, players.count { it.name.key == ali.key })
    }

    @Test
    fun lineupIgnoresSeatOrderAndTeamOrder() {
        val lineups = lineups(history)
        assertEquals(2, lineups.size)
        val latest = lineups.first()
        assertEquals(2, latest.games)
        assertEquals("Kızlar", latest.team1Name, "names come from the most recent game")
        assertEquals(1, lineups[1].games)
    }

    @Test
    fun teamNamesAreDistinctIgnoringCase() {
        val withVariants = history + game("0", listOf(kerem, ali, ayse, fatma), team1 = "BİZ", team2 = "onlar")
        assertEquals(listOf("Biz", "Onlar", "Kızlar", "Erkekler"), knownTeamNames(withVariants))
    }

    // Kerem & Ali vs Ayşe & Fatma; lower total wins.
    private val results = listOf(
        game("c", listOf(ayse, fatma, PlayerName("ali"), kerem), totals = 300 to 120), // Kerem & Ali win
        game("b", listOf(kerem, ali, ayse, fatma), totals = 90 to 400), // Kerem & Ali win
        game("a", listOf(kerem, ilker, ayse, fatma), totals = 250 to 250), // tie
    )

    @Test
    fun lowerTotalWinsAndTieHasNoWinner() {
        assertEquals(listOf(2, 1, null), results.map { it.winningTeam })
    }

    @Test
    fun playerRecordCountsGamesAndWinsOnWhicheverSideTheySat() {
        val records = playerRecords(results).associateBy { it.label }
        assertEquals(3, records.getValue("Kerem").games)
        assertEquals(2, records.getValue("Kerem").wins)
        assertEquals(3, records.getValue("Ayşe").games)
        assertEquals(0, records.getValue("Ayşe").wins)
        assertEquals(1, records.getValue("İlker").games)
        assertEquals(0, records.getValue("İlker").wins)
    }

    @Test
    fun teamRecordIgnoresSeatOrder() {
        val keremAli = teamRecords(results).first { it.filter.key == teamKey(kerem, ali) }
        assertEquals(2, keremAli.games)
        assertEquals(2, keremAli.wins)
        assertEquals(listOf("c", "b"), results.filteredBy(keremAli.filter).map { it.id })
    }

    @Test
    fun noFilterKeepsEverything() {
        assertEquals(results, results.filteredBy(null))
    }
}
