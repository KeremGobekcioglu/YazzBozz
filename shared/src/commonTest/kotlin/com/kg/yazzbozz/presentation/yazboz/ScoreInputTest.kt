package com.kg.yazzbozz.presentation.yazboz

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScoreInputTest {

    @Test
    fun aTypedMinusIsDroppedSoScoresGoOnThePositive() {
        assertEquals("", sanitizeScore("-"))
        assertEquals("25", sanitizeScore("-25"))
        assertEquals(101, parseScore(sanitizeScore("-101")))
    }

    @Test
    fun anEmptyFieldReadsAsNothingWritten() {
        assertNull(parseScore(""))
    }

    @Test
    fun strayCharactersAreDropped() {
        assertEquals("87", sanitizeScore("8 7"))
        assertEquals("87", sanitizeScore("87-"))
        assertEquals("1000", sanitizeScore("1.000"))
    }

    @Test
    fun lengthIsCappedSoTheNumberAlwaysFitsAnInt() {
        assertEquals("123456", sanitizeScore("1234567890"))
    }

    @Test
    fun storedScoresShowBlankForZeroWhenCorrecting() {
        assertEquals("", scoreText(0))
        assertEquals("87", scoreText(87))
    }
}
