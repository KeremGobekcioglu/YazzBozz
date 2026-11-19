package com.kg.yazzbozz.domain

// Kotlin's uppercase()/lowercase() are locale-invariant, so 'i' → 'I' and 'İ' → "i̇".
// Names here are Turkish: the dotted/dotless pairs are mapped explicitly.

fun Char.turkishUppercaseChar(): Char = when (this) {
    'i' -> 'İ'
    'ı' -> 'I'
    else -> uppercaseChar()
}

fun Char.turkishLowercaseChar(): Char = when (this) {
    'I' -> 'ı'
    'İ' -> 'i'
    else -> lowercaseChar()
}

fun String.turkishLowercase(): String = buildString(length) {
    this@turkishLowercase.forEach { append(it.turkishLowercaseChar()) }
}

private val Whitespace = Regex("\\s+")

private fun String.words(): List<String> = trim().split(Whitespace).filter { it.isNotEmpty() }

/**
 * Tidies a typed name for storage: trims, collapses inner whitespace and capitalises the
 * first letter of each word ("ilker  " → "İlker"). The rest of each word is kept as typed.
 */
fun cleanName(raw: String): String = raw.words().joinToString(" ") { word ->
    "${word.first().turkishUppercaseChar()}${word.substring(1)}"
}

/** Case- and whitespace-insensitive matching key ("  İLKER " and "ilker" → "ilker"). */
fun nameKey(raw: String): String = raw.words().joinToString(" ") { it.turkishLowercase() }

/** Identity of a person across games: first name + surname, both normalised. */
fun personKey(firstName: String, lastName: String): String = "${nameKey(firstName)}|${nameKey(lastName)}"

fun String.turkishInitial(): Char? = trim().firstOrNull()?.turkishUppercaseChar()
