package com.kg.yazzbozz.domain

/**
 * Short marks written next to a hand's score, one per seat. Normally the initial ("A"),
 * lengthened only where two seats would otherwise get the same mark
 * (Ali & Ayşe → "Al", "Ay"; Ali Kaya & Ali Yılmaz → "AK", "AY").
 */
fun seatLabels(players: List<PlayerName>): List<String> {
    val candidates = players.map(::labelCandidates)
    val level = IntArray(players.size)

    repeat(MaxLabelLevels) {
        val labels = candidates.mapIndexed { seat, options -> options[minOf(level[seat], options.lastIndex)] }
        val clashing = labels.indices.filter { seat -> labels.count { it == labels[seat] } > 1 }
            .filter { seat -> level[seat] < candidates[seat].lastIndex }
        if (clashing.isEmpty()) return labels
        clashing.forEach { level[it]++ }
    }
    return candidates.mapIndexed { seat, options -> options[minOf(level[seat], options.lastIndex)] }
}

private const val MaxLabelLevels = 8

private fun labelCandidates(player: PlayerName): List<String> {
    val first = player.firstName.trim()
    val initial = first.turkishInitial()?.toString() ?: "?"
    val lastInitial = player.lastName.turkishInitial()
    return buildList {
        add(initial)
        if (lastInitial != null) add("$initial$lastInitial")
        if (first.length >= 2) add(initial + first.substring(1, 2).turkishLowercase())
        if (first.length >= 3) add(initial + first.substring(1, 3).turkishLowercase())
        add(player.displayName.ifBlank { "?" })
    }.distinct()
}

/**
 * Names for the selection chips: first names, plus a surname initial for any first name
 * that appears twice at the table.
 */
fun chipNames(players: List<PlayerName>): List<String> = players.map { player ->
    val sameFirstName = players.count { nameKey(it.firstName) == nameKey(player.firstName) } > 1
    val lastInitial = player.lastName.turkishInitial()
    if (sameFirstName && lastInitial != null) "${player.firstName} $lastInitial." else player.firstName
}
