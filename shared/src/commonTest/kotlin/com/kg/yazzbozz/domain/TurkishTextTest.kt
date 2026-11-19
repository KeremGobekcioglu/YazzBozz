package com.kg.yazzbozz.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TurkishTextTest {

    @Test
    fun initialsFollowTurkishCasing() {
        assertEquals('İ', "ilker".turkishInitial())
        assertEquals('İ', "İlker".turkishInitial())
        assertEquals('I', "ırmak".turkishInitial())
        assertEquals('I', "Irmak".turkishInitial())
        assertEquals('Ş', "  şule".turkishInitial())
    }

    @Test
    fun nameKeyIgnoresCaseAndSurroundingWhitespace() {
        assertEquals(nameKey("İlker"), nameKey("  ilker "))
        assertEquals(nameKey("İLKER"), nameKey("ilker"))
        assertEquals(nameKey("Irmak"), nameKey("ırmak"))
        assertEquals(nameKey("IRMAK"), nameKey("Irmak"))
        assertEquals(nameKey("Ayşe  Nur"), nameKey("ayşe nur"))
    }

    @Test
    fun dottedAndDotlessIAreDifferentLetters() {
        assertNotEquals(nameKey("Irmak"), nameKey("irmak"))
        assertNotEquals(nameKey("İlker"), nameKey("ılker"))
    }

    @Test
    fun cleanNameCapitalisesEachWordTheTurkishWay() {
        assertEquals("İlker", cleanName("  ilker "))
        assertEquals("Irmak", cleanName("ırmak"))
        assertEquals("Ayşe Nur", cleanName("ayşe   nur"))
        assertEquals("", cleanName("   "))
    }

    @Test
    fun personKeyIncludesSurname() {
        assertEquals(personKey("ali", "yılmaz"), PlayerName("Ali", "Yılmaz").key)
        assertNotEquals(PlayerName("Ali", "Yılmaz").key, PlayerName("Ali", "Kaya").key)
        assertNotEquals(PlayerName("Ali").key, PlayerName("Ali", "Kaya").key)
    }
}
