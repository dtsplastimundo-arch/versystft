package com.versys.tft.data.repository

import com.versys.tft.domain.model.TelemetryState
import com.versys.tft.simulator.SimulationScenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockTelemetryRepositoryTest {
    @Test
    fun parkedIgnitionOn_isStationaryForSimulationInterlock() = runTest {
        val repository = MockTelemetryRepository(externalScope = this)
        repository.setSimulationMode(SimulationScenario.PARKED_IGNITION_ON)
        repository.start()

        advanceTimeBy(250L)
        runCurrent()

        val state = repository.telemetry.value
        assertEquals(0.0, state.speedKmh!!, 0.0)
        assertEquals(0, state.rpm ?: -1)
        assertEquals(0, state.gear ?: -1)
        assertTrue(state.isStationarySafeForDiagnostics)

        repository.close()
        assertTrue("The repository must not cancel an injected scope", coroutineContext[kotlinx.coroutines.Job]!!.isActive)
    }

    @Test
    fun overheatScenario_isNotSafeForDiagnosticsWhileEngineRuns() = runTest {
        val repository = MockTelemetryRepository(externalScope = this)
        repository.setSimulationMode(SimulationScenario.OVERHEAT_ALERT)
        repository.start()
        runCurrent()

        val state = repository.telemetry.value
        assertEquals(106.0, state.coolantTempC!!, 0.0)
        assertEquals(1_100, state.rpm ?: -1)
        assertFalse(state.isStationarySafeForDiagnostics)

        repository.close()
    }

    @Test
    fun missingSpeedOrRpm_neverPassesStationaryInterlock() {
        assertFalse(TelemetryState(speedKmh = null, rpm = 0).isStationarySafeForDiagnostics)
        assertFalse(TelemetryState(speedKmh = 0.0, rpm = null).isStationarySafeForDiagnostics)
        assertFalse(TelemetryState(speedKmh = 0.1, rpm = 0).isStationarySafeForDiagnostics)
        assertFalse(TelemetryState(speedKmh = 0.0, rpm = 1).isStationarySafeForDiagnostics)
    }
}
