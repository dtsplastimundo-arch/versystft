package com.versys.tft.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionPeaksTest {

    @Test
    fun nullReadings_doNotCreateOrChangePeaks() {
        assertEquals(SessionPeaks(), SessionPeaks().updatedWith(TelemetryState()))
    }

    @Test
    fun leanLeftAndRight_areTrackedAsPositiveMagnitudes() {
        var p = SessionPeaks()
        p = p.updatedWith(TelemetryState(leanAngleDeg = -20.0))
        p = p.updatedWith(TelemetryState(leanAngleDeg = 35.0))
        p = p.updatedWith(TelemetryState(leanAngleDeg = -10.0))
        assertEquals(20.0, p.maxLeanLeftDeg!!, 0.0)
        assertEquals(35.0, p.maxLeanRightDeg!!, 0.0)
    }

    @Test
    fun accelerationAndBraking_areSeparatedBySign() {
        var p = SessionPeaks()
        p = p.updatedWith(TelemetryState(longitudinalG = 0.40))
        p = p.updatedWith(TelemetryState(longitudinalG = -0.85))
        p = p.updatedWith(TelemetryState(longitudinalG = 0.10))
        assertEquals(0.40, p.maxAccelG!!, 0.0)
        assertEquals(0.85, p.maxBrakeG!!, 0.0)
    }

    @Test
    fun peaksNeverDecrease_andMissingReadingKeepsPreviousPeak() {
        var p = SessionPeaks()
        p = p.updatedWith(TelemetryState(speedKmh = 120.0, rpm = 7_000))
        p = p.updatedWith(TelemetryState(speedKmh = 40.0, rpm = 3_000))
        p = p.updatedWith(TelemetryState(speedKmh = null, rpm = null))
        assertEquals(120.0, p.maxSpeedKmh!!, 0.0)
        assertEquals(7_000, p.maxRpm)
    }
}
