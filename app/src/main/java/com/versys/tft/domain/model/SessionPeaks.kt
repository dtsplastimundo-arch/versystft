package com.versys.tft.domain.model

import kotlin.math.max

/**
 * Picos de la sesión. null = todavía no hubo dato (no se sustituye por cero).
 * Inclinación izquierda/derecha y G de aceleración/frenado se guardan como magnitudes positivas.
 */
data class SessionPeaks(
    val maxSpeedKmh: Double? = null,
    val maxRpm: Int? = null,
    val maxLeanLeftDeg: Double? = null,
    val maxLeanRightDeg: Double? = null,
    val maxAccelG: Double? = null,
    val maxBrakeG: Double? = null,
) {
    fun updatedWith(s: TelemetryState): SessionPeaks = copy(
        maxSpeedKmh = peakOf(maxSpeedKmh, s.speedKmh),
        maxRpm = s.rpm?.let { new -> maxRpm?.let { max(it, new) } ?: new } ?: maxRpm,
        maxLeanLeftDeg = peakOf(maxLeanLeftDeg, s.leanAngleDeg?.let { max(0.0, -it) }),
        maxLeanRightDeg = peakOf(maxLeanRightDeg, s.leanAngleDeg?.let { max(0.0, it) }),
        maxAccelG = peakOf(maxAccelG, s.longitudinalG?.let { max(0.0, it) }),
        maxBrakeG = peakOf(maxBrakeG, s.longitudinalG?.let { max(0.0, -it) }),
    )

    private fun peakOf(current: Double?, new: Double?): Double? = when {
        new == null -> current
        current == null -> new
        else -> max(current, new)
    }
}
