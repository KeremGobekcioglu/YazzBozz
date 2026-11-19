package com.kg.yazzbozz.domain

// Groupings over finished games. People are matched by [PlayerName.key] (first name + surname,
// case/whitespace-insensitive, Turkish-aware); a team is the pair of people, whatever it was
// called that night. All functions expect history newest first, so the first occurrence of a
// person or lineup is its most recent spelling.

data class KnownPlayer(
    val name: PlayerName,
    val games: Int,
)

/** Two pairs facing each other, as they sat the last time they played. */
data class Lineup(
    val key: String,
    val team1Name: String,
    val team2Name: String,
    val players: List<PlayerName>,
    val games: Int,
)

fun teamKey(a: PlayerName, b: PlayerName): String = listOf(a.key, b.key).sorted().joinToString(" + ")

fun GameSummary.teamKey(team: Int): String =
    if (team == 1) teamKey(players[0], players[1]) else teamKey(players[2], players[3])

fun GameSummary.lineupKey(): String = listOf(teamKey(1), teamKey(2)).sorted().joinToString(" vs ")

private val GameSummary.isComplete: Boolean get() = players.size == 4

/** Everyone who has played, most games first. */
fun knownPlayers(history: List<GameSummary>): List<KnownPlayer> =
    history.flatMap { it.players }
        .groupBy { it.key }
        .map { (_, appearances) -> KnownPlayer(name = appearances.first(), games = appearances.size) }
        .sortedByDescending { it.games }

/** Team names used before, most used first. */
fun knownTeamNames(history: List<GameSummary>): List<String> =
    history.flatMap { listOf(it.team1Name, it.team2Name) }
        .filter { it.isNotBlank() }
        .groupBy { nameKey(it) }
        .map { (_, uses) -> uses.first() to uses.size }
        .sortedByDescending { it.second }
        .map { it.first }

/** Lineups played before, most recently played first. */
fun lineups(history: List<GameSummary>): List<Lineup> =
    history.filter { it.isComplete }
        .groupBy { it.lineupKey() }
        .map { (key, games) ->
            val latest = games.first()
            Lineup(
                key = key,
                team1Name = latest.team1Name,
                team2Name = latest.team2Name,
                players = latest.players,
                games = games.size,
            )
        }

// ---------------------------------------------------------------- filtering & records

/** Lower total wins (scores on a yazboz are points against); a tie has no winner. */
val GameSummary.winningTeam: Int?
    get() = when {
        totalTeam1 < totalTeam2 -> 1
        totalTeam2 < totalTeam1 -> 2
        else -> null
    }

sealed interface HistoryFilter {
    val key: String

    data class Player(override val key: String) : HistoryFilter
    data class Team(override val key: String) : HistoryFilter
}

/** A person or pair in the history, with how many games they played and won. */
data class HistoryRecord(
    val filter: HistoryFilter,
    val label: String,
    val games: Int,
    val wins: Int,
)

/** The team the [filter]ed person or pair played on in this game, or null if they weren't in it. */
fun GameSummary.sideOf(filter: HistoryFilter): Int? = when (filter) {
    is HistoryFilter.Player -> players.indexOfFirst { it.key == filter.key }.takeIf { it >= 0 }?.let(::teamOfSeat)
    is HistoryFilter.Team -> when {
        !isComplete -> null
        teamKey(1) == filter.key -> 1
        teamKey(2) == filter.key -> 2
        else -> null
    }
}

fun List<GameSummary>.filteredBy(selection: HistoryFilter?): List<GameSummary> =
    if (selection == null) this else filter { it.sideOf(selection) != null }

private fun List<GameSummary>.recordFor(selection: HistoryFilter, label: String): HistoryRecord {
    val played = filteredBy(selection)
    return HistoryRecord(
        filter = selection,
        label = label,
        games = played.size,
        wins = played.count { it.winningTeam != null && it.winningTeam == it.sideOf(selection) },
    )
}

/** Every person in the history, most games first. */
fun playerRecords(history: List<GameSummary>): List<HistoryRecord> =
    knownPlayers(history).map { history.recordFor(HistoryFilter.Player(it.name.key), it.name.displayName) }

/** Every pair that played together, most games first. */
fun teamRecords(history: List<GameSummary>): List<HistoryRecord> =
    history.filter { it.isComplete }
        .flatMap { game ->
            listOf(
                game.teamKey(1) to (game.players[0] to game.players[1]),
                game.teamKey(2) to (game.players[2] to game.players[3]),
            )
        }
        .distinctBy { it.first }
        .map { (key, pair) ->
            history.recordFor(HistoryFilter.Team(key), "${pair.first.displayName} & ${pair.second.displayName}")
        }
        .sortedByDescending { it.games }
