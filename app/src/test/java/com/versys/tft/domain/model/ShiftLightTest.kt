package com.versys.tft.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShiftLightTest {
    private val shift = 8_500   // zona de encendido: desde 6_000

    @Test
    fun missingRpm_lightsNothingAndIsNotShifting() {
        assertEquals(0, ShiftLight.litSegments(null, shift))
        assertFalse(ShiftLight.isShifting(null, shift))
    }

    @Test
    fun belowZone_lightsNothing() {
        assertEquals(0, ShiftLight.litSegments(5_999, shift))
    }

    @Test
    fun segmentsRiseEvery250Rpm() {
        assertEquals(1, ShiftLight.litSegments(6_000, shift))
        assertEquals(2, ShiftLight.litSegments(6_250, shift))
        assertEquals(9, ShiftLight.litSegments(8_249, shift))
        assertEquals(10, ShiftLight.litSegments(8_250, shift))
        assertEquals(10, ShiftLight.litSegments(8_499, shift))
    }

    @Test
    fun atOrAboveShiftRpm_isShiftingWithAllSegments() {
        assertFalse(ShiftLight.isShifting(8_499, shift))
        assertTrue(ShiftLight.isShifting(8_500, shift))
        assertEquals(10, ShiftLight.litSegments(11_000, shift))
    }
}
