package com.kg.yazzbozz.presentation.yazboz

// Every number on a yazboz is points against, so a score is never negative: a "-" typed in
// front of one is dropped and "-25" goes on the sheet as 25 (see GameRecord.total).

private const val MaxScoreDigits = 6

/** Keeps up to [MaxScoreDigits] digits and drops everything else, a minus sign included. */
internal fun sanitizeScore(raw: String): String = raw.filter { it.isDigit() }.take(MaxScoreDigits)

/** The number in a score field; an empty field reads as nothing written yet. */
internal fun parseScore(text: String): Int? = text.toIntOrNull()

/** How a stored score is shown when a hand is opened for editing; 0 is left blank, as when adding. */
internal fun scoreText(score: Int): String = if (score == 0) "" else score.toString()
