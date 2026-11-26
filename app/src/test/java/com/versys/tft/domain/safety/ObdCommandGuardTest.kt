package com.versys.tft.domain.safety

import com.versys.tft.domain.model.TelemetrySource
import com.versys.tft.domain.model.TelemetryState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObdCommandGuardTest {
    private val now = 100_000L
    private val guard = ObdCommandGuard(nowMs = { now })

    private fun state(speed: Double?, rpm: Int?, ageMs: Long = 200L) = TelemetryState(
        speedKmh = speed,
        rpm = rpm,
        source = TelemetrySource.SIMULATED,
        timestampEpochMs = now - ageMs,
    )

    private fun assertDenied(result: GuardResult) =
        assertTrue("Se esperaba Denied pero fue $result", result is GuardResult.Denied)

    @Test
    fun readPid_inReadMode_isAllowedEvenWhileMoving() {
        val r = guard.check(ObdCommand.ReadPid(mode = 0x01, pid = 0x0C), state(80.0, 4_000))
        assertEquals(GuardResult.Allowed, r)
    }

    @Test
    fun readPid_inNonReadMode_isDenied() {
        assertDenied(guard.check(ObdCommand.ReadPid(mode = 0x04, pid = 0x00), state(0.0, 0)))
    }

    @Test
    fun clearDtc_whileMoving_isDenied() {
        assertDenied(guard.check(ObdCommand.ClearDtc, state(30.0, 3_000), confirmations = 2))
    }

    @Test
    fun clearDtc_withEngineIdling_isDenied() {
        assertDenied(guard.check(ObdCommand.ClearDtc, state(0.0, 1_100), confirmations = 2))
    }

    @Test
    fun clearDtc_withStaleTelemetry_isDenied() {
        assertDenied(guard.check(ObdCommand.ClearDtc, state(0.0, 0, ageMs = 5_000L), confirmations = 2))
    }

    @Test
    fun clearDtc_withMissingSpeedOrRpm_isDenied() {
        assertDenied(guard.check(ObdCommand.ClearDtc, state(null, 0), confirmations = 2))
        assertDenied(guard.check(ObdCommand.ClearDtc, state(0.0, null), confirmations = 2))
    }

    @Test
    fun clearDtc_withoutDoubleConfirmation_isDenied() {
        assertDenied(guard.check(ObdCommand.ClearDtc, state(0.0, 0), confirmations = 1))
    }

    @Test
    fun clearDtc_stoppedFreshAndConfirmedTwice_isAllowed() {
        assertEquals(GuardResult.Allowed, guard.check(ObdCommand.ClearDtc, state(0.0, 0), confirmations = 2))
    }
}
