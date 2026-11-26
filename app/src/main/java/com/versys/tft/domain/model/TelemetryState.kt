package com.versys.tft.domain.model

/**
 * Snapshot unificado de telemetría.
 * Los valores null significan "sin dato disponible"; no deben sustituirse por cero.
 * source permite distinguir explícitamente los datos simulados de los reales.
 */
data class TelemetryState(
    val speedKmh: Double? = null,
    val rpm: Int? = null,
    val gear: Int? = null, // 0 = neutral; 1..6 = marcha calculada/detectada
    val powerMode: PowerMode? = null, // F = Full; L = Low (~75%)
    val ktrcMode: KtrcMode? = null, // 1..3 u OFF
    val coolantTempC: Double? = null,
    val batteryVoltage: Double? = null,
    val leanAngleDeg: Double? = null,
    val tripAKm: Double = 0.0,
    val nearestFuelStationName: String? = null,
    val nearestFuelStationDistanceKm: Double? = null,
    val throttlePositionPct: Double? = null,
    val longitudinalG: Double? = null,
    val activeDtcs: List<String> = emptyList(),
    val bluetoothConnected: Boolean = false,
    val kdsConnected: Boolean = false,
    val gpsAvailable: Boolean = false,
    val source: TelemetrySource = TelemetrySource.NONE,
    val timestampEpochMs: Long = 0L
) {
    val gearLabel: String
        get() = when {
            gear == null -> "—"
            gear == 0 -> "N"
            gear in 1..6 -> gear.toString()
            else -> "—"
        }

    /**
     * Interlock básico para la simulación: solo true cuando ambas lecturas existen
     * y son exactamente cero. No sustituye una autorización segura para borrar DTC
     * en la moto real (que además exigirá datos frescos y validados).
     */
    val isStationarySafeForDiagnostics: Boolean
        get() = speedKmh == 0.0 && rpm == 0
}

/** Modos originales Kawasaki: la app solo los representa; no los modifica. */
enum class PowerMode(val label: String) {
    FULL("F"),
    LOW("L")
}

enum class KtrcMode(val label: String) {
    LEVEL_1("1"),
    LEVEL_2("2"),
    LEVEL_3("3"),
    OFF("OFF")
}

enum class TelemetrySource {
    NONE,
    SIMULATED,
    KDS_REAL,
    SENSOR_REAL
}
