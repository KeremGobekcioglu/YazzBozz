package com.kg.yazzbozz.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerLabelsTest {

    @Test
    fun distinctInitialsStaySingleLetters() {
        val players = listOf(PlayerName("Kerem"), PlayerName("İlker"), PlayerName("Irmak"), PlayerName("Fatma"))
        assertEquals(listOf("K", "İ", "I", "F"), seatLabels(players))
    }

    @Test
    fun clashingInitialsAreLengthened() {
        val players = listOf(PlayerName("Kerem"), PlayerName("Ali"), PlayerName("Ayşe"), PlayerName("Fatma"))
        assertEquals(listOf("K", "Al", "Ay", "F"), seatLabels(players))
    }

    @Test
    fun sameFirstNameUsesSurnameInitial() {
        val players = listOf(PlayerName("Ali", "Kaya"), PlayerName("Kerem"), PlayerName("Ali", "Yılmaz"), PlayerName("Fatma"))
        assertEquals(listOf("AK", "K", "AY", "F"), seatLabels(players))
        assertEquals(listOf("Ali K.", "Kerem", "Ali Y.", "Fatma"), chipNames(players))
    }

    @Test
    fun lowercaseTypedNamesStillGetTurkishCapitals() {
        val players = listOf(PlayerName("ilker"), PlayerName("ırmak"), PlayerName("işıl"), PlayerName("ıhlamur"))
        assertEquals(listOf("İl", "Ir", "İş", "Ih"), seatLabels(players))
    }
}
