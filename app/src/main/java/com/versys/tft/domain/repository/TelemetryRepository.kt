package com.versys.tft.domain.repository

import com.versys.tft.domain.model.TelemetryState
import kotlinx.coroutines.flow.StateFlow

/** Contrato único de telemetría: lo implementan el simulador y (luego) el enlace OBD/KDS real. */
interface TelemetryRepository {
    val telemetry: StateFlow<TelemetryState>
    fun start()
    fun close()
}
