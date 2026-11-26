package com.versys.tft.domain.model

/** Lógica pura de la luz de cambio (10 segmentos que suben hacia el RPM de cambio). */
object ShiftLight {
    const val SEGMENTS = 10
    const val STEP_RPM = 250

    /** true cuando se alcanzó o superó el RPM de cambio; con dato ausente nunca es true. */
    fun isShifting(rpm: Int?, shiftRpm: Int): Boolean = rpm != null && rpm >= shiftRpm

    /** Cantidad de segmentos encendidos: 0 sin dato o por debajo de la zona; SEGMENTS al cambiar. */
    fun litSegments(rpm: Int?, shiftRpm: Int): Int {
        if (rpm == null) return 0
        if (rpm >= shiftRpm) return SEGMENTS
        val firstOn = shiftRpm - SEGMENTS * STEP_RPM
        if (rpm < firstOn) return 0
        return ((rpm - firstOn) / STEP_RPM + 1).coerceAtMost(SEGMENTS)
    }
}
