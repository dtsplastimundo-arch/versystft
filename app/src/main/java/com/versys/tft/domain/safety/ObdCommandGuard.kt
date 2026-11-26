package com.versys.tft.domain.safety

import com.versys.tft.domain.model.TelemetryState

sealed interface ObdCommand {
    data class ReadPid(val mode: Int, val pid: Int) : ObdCommand
    data object ReadDtc : ObdCommand
    data object ClearDtc : ObdCommand
    data class Raw(val hex: String) : ObdCommand
}

sealed interface GuardResult {
    data object Allowed : GuardResult
    data class Denied(val reason: String) : GuardResult
}

/**
 * ÚNICO punto por el que debe pasar cualquier comando hacia la moto (capa bluetooth/).
 *  - Lectura de PIDs / DTC: siempre permitida (solo modos de lectura).
 *  - Borrado de DTC: solo con telemetría fresca, velocidad == 0, RPM == 0 (ambas presentes)
 *    y doble confirmación.
 *  - Comandos crudos: solo con la moto detenida y telemetría fresca.
 * Fail-safe: dato ausente (null) o viejo => se asume moto en movimiento.
 */
class ObdCommandGuard(
    private val nowMs: () -> Long = System::currentTimeMillis,
    private val maxAgeMs: Long = 1_500L,
) {

    fun check(command: ObdCommand, state: TelemetryState, confirmations: Int = 0): GuardResult =
        when (command) {
            is ObdCommand.ReadPid ->
                if (command.mode in READ_ONLY_MODES) GuardResult.Allowed
                else GuardResult.Denied("El modo OBD 0x%02X no es de solo lectura".format(command.mode))

            is ObdCommand.ReadDtc -> GuardResult.Allowed

            is ObdCommand.ClearDtc -> checkClear(state, confirmations)

            is ObdCommand.Raw ->
                if (isStoppedAndTrusted(state)) GuardResult.Allowed
                else GuardResult.Denied("Los comandos crudos solo se permiten con la moto detenida")
        }

    private fun checkClear(state: TelemetryState, confirmations: Int): GuardResult = when {
        !isFresh(state) ->
            GuardResult.Denied("Telemetría no confiable: se asume moto en movimiento")
        !state.isStationarySafeForDiagnostics ->
            GuardResult.Denied("Se requiere velocidad 0 y RPM 0 confirmadas (hay movimiento o falta el dato)")
        confirmations < REQUIRED_CONFIRMATIONS ->
            GuardResult.Denied("Requiere doble confirmación del usuario")
        else -> GuardResult.Allowed
    }

    private fun isFresh(s: TelemetryState): Boolean =
        s.timestampEpochMs > 0L && (nowMs() - s.timestampEpochMs) in 0L..maxAgeMs

    private fun isStoppedAndTrusted(s: TelemetryState) =
        isFresh(s) && s.isStationarySafeForDiagnostics

    private companion object {
        const val REQUIRED_CONFIRMATIONS = 2
        // 0x01 datos actuales, 0x02 freeze frame, 0x03 leer DTC, 0x07 DTC pendientes, 0x09 info del vehículo
        val READ_ONLY_MODES = setOf(0x01, 0x02, 0x03, 0x07, 0x09)
    }
}
