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
