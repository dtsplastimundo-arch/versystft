package com.versys.tft.data.repository

import com.versys.tft.domain.model.KtrcMode
import com.versys.tft.domain.model.PowerMode
import com.versys.tft.domain.model.TelemetrySource
import com.versys.tft.domain.model.TelemetryState
import com.versys.tft.domain.repository.TelemetryRepository
import com.versys.tft.simulator.SimulationController
import com.versys.tft.simulator.SimulationScenario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

/**
 * Simulador local de telemetría.
 * No abre Bluetooth, no envía comandos y no controla la motocicleta.
 *
 * Se puede inyectar un CoroutineScope (por ejemplo viewModelScope o TestScope).
 * Si no se proporciona, el repositorio crea y administra su propio scope.
 */
class MockTelemetryRepository(
    externalScope: CoroutineScope? = null
) : TelemetryRepository, SimulationController {
    private val ownsScope = externalScope == null
    private val repositoryScope = externalScope
        ?: CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val mutableScenario = MutableStateFlow(SimulationScenario.CRUISING)
    override val scenario = mutableScenario.asStateFlow()

    private val mutableTelemetry = MutableStateFlow(
        TelemetryState(
            speedKmh = 82.0,
            rpm = 4_623,
            gear = 5,
            powerMode = PowerMode.FULL,
            ktrcMode = KtrcMode.LEVEL_1,
            coolantTempC = 88.0,
            batteryVoltage = 14.1,
            leanAngleDeg = 0.0,
            tripAKm = 142.8,
            nearestFuelStationName = "Gasolinera de prueba",
            nearestFuelStationDistanceKm = 4.2,
            throttlePositionPct = 28.0,
            longitudinalG = 0.0,
            bluetoothConnected = false,
            kdsConnected = false,
            gpsAvailable = true,
            source = TelemetrySource.SIMULATED,
            timestampEpochMs = System.currentTimeMillis()
        )
    )

    override val telemetry = mutableTelemetry.asStateFlow()
    private var simulationJob: Job? = null

    /** Cambia el escenario que se publicará en el siguiente ciclo (máximo 200 ms). */
    override fun setSimulationMode(mode: SimulationScenario) {
        mutableScenario.value = mode
    }

    @Synchronized
    override fun start() {
        if (simulationJob?.isActive == true) return

        simulationJob = repositoryScope.launch {
            val startNanos = System.nanoTime()
            var lastNanos = startNanos
            var tripKm = mutableTelemetry.value.tripAKm

            try {
                while (isActive) {
                    val now = System.nanoTime()
                    val elapsedSeconds = (now - startNanos) / 1_000_000_000.0
                    val deltaHours = (now - lastNanos) / 3_600_000_000_000.0
                    lastNanos = now

                    val next = when (mutableScenario.value) {
                        SimulationScenario.CRUISING -> cruisingState(
                            elapsedSeconds = elapsedSeconds,
                            deltaHours = deltaHours,
                            currentTripKm = tripKm
                        ).also { tripKm = it.tripAKm }

                        SimulationScenario.PARKED_IGNITION_ON -> parkedState(tripKm)
                        SimulationScenario.OVERHEAT_ALERT -> overheatState(tripKm)
                    }

                    mutableTelemetry.value = next.copy(timestampEpochMs = System.currentTimeMillis())
                    delay(200L)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            }
        }
    }

    private fun cruisingState(
        elapsedSeconds: Double,
        deltaHours: Double,
        currentTripKm: Double
    ): TelemetryState {
        val speed = (82.0 + 27.0 * sin(elapsedSeconds / 8.0)).coerceIn(0.0, 135.0)
        val gear = when {
            speed < 12.0 -> 1
            speed < 27.0 -> 2
            speed < 45.0 -> 3
            speed < 68.0 -> 4
            speed < 94.0 -> 5
            else -> 6
        }
        val rpm = (1_050.0 + (speed * 218.0 / gear)).toInt().coerceIn(1_000, 10_500)
        val lean = 17.0 * sin(elapsedSeconds / 4.5)
        val coolant = 88.0 + 2.2 * sin(elapsedSeconds / 29.0)
        val voltage = 14.05 + 0.16 * sin(elapsedSeconds / 11.0)
        val stationDistance = 4.2 + 0.8 * sin(elapsedSeconds / 37.0)
        val throttle = (28.0 + 18.0 * sin(elapsedSeconds / 5.7)).coerceIn(0.0, 100.0)
        val longitudinalG = (0.22 * sin(elapsedSeconds / 3.8)).coerceIn(-0.4, 0.55)
        val updatedTrip = currentTripKm + speed * deltaHours

        return baseState(
            speedKmh = speed,
            rpm = rpm,
            gear = gear,
            coolantTempC = coolant,
            batteryVoltage = voltage,
            leanAngleDeg = lean,
            tripAKm = updatedTrip,
            throttlePositionPct = throttle,
            longitudinalG = longitudinalG,
            fuelStationDistanceKm = stationDistance
        )
    }

    private fun parkedState(tripKm: Double): TelemetryState = baseState(
        speedKmh = 0.0,
        rpm = 0,
        gear = 0,
        coolantTempC = 88.0,
        batteryVoltage = 12.5,
        leanAngleDeg = 0.0,
        tripAKm = tripKm,
        throttlePositionPct = 0.0,
        longitudinalG = 0.0,
        fuelStationDistanceKm = 4.2
    )

    private fun overheatState(tripKm: Double): TelemetryState = baseState(
        speedKmh = 0.0,
        rpm = 1_100,
        gear = 0,
        coolantTempC = 106.0,
        batteryVoltage = 14.0,
        leanAngleDeg = 0.0,
        tripAKm = tripKm,
        throttlePositionPct = 0.0,
        longitudinalG = 0.0,
        fuelStationDistanceKm = 4.2
    )

    private fun baseState(
        speedKmh: Double,
        rpm: Int,
        gear: Int,
        coolantTempC: Double,
        batteryVoltage: Double,
        leanAngleDeg: Double,
        tripAKm: Double,
        throttlePositionPct: Double,
        longitudinalG: Double,
        fuelStationDistanceKm: Double
    ) = TelemetryState(
        speedKmh = speedKmh,
        rpm = rpm,
        gear = gear,
        powerMode = PowerMode.FULL,
        ktrcMode = KtrcMode.LEVEL_1,
        coolantTempC = coolantTempC,
        batteryVoltage = batteryVoltage,
        leanAngleDeg = leanAngleDeg,
        tripAKm = tripAKm,
        nearestFuelStationName = "Gasolinera de prueba",
        nearestFuelStationDistanceKm = fuelStationDistanceKm,
        throttlePositionPct = throttlePositionPct,
        longitudinalG = longitudinalG,
        activeDtcs = emptyList(),
        bluetoothConnected = false,
        kdsConnected = false,
        gpsAvailable = true,
        source = TelemetrySource.SIMULATED,
        timestampEpochMs = System.currentTimeMillis()
    )

    @Synchronized
    override fun close() {
        simulationJob?.cancel()
        simulationJob = null
        if (ownsScope) repositoryScope.cancel()
    }
}
