package com.versys.tft.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.versys.tft.data.repository.MockTelemetryRepository
import com.versys.tft.domain.model.SessionPeaks
import com.versys.tft.domain.model.TelemetryState
import com.versys.tft.domain.repository.TelemetryRepository
import com.versys.tft.simulator.SimulationController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Compartido por ROAD / SPORT / GARAGE. Depende solo de [TelemetryRepository]; el control de
 * escenarios existe únicamente si el repositorio activo implementa [SimulationController].
 */
class TelemetryViewModel(
    repositoryFactory: (CoroutineScope) -> TelemetryRepository,
) : ViewModel() {

    private val repository = repositoryFactory(viewModelScope)

    val telemetry: StateFlow<TelemetryState> = repository.telemetry

    /** No nulo solo cuando el repositorio activo es un simulador. */
    val simulation: SimulationController? = repository as? SimulationController

    private val mutablePeaks = MutableStateFlow(SessionPeaks())
    val peaks: StateFlow<SessionPeaks> = mutablePeaks.asStateFlow()

    // En memoria: se pierde si el sistema mata el proceso (la persistencia irá con Room/DataStore).
    private val mutableShiftRpm = MutableStateFlow(DEFAULT_SHIFT_RPM)
    val shiftRpm: StateFlow<Int> = mutableShiftRpm.asStateFlow()

    init {
        repository.start()
        viewModelScope.launch {
            telemetry.collect { state -> mutablePeaks.update { it.updatedWith(state) } }
        }
    }

    fun resetPeaks() {
        mutablePeaks.value = SessionPeaks()
    }

    fun setShiftRpm(rpm: Int) {
        mutableShiftRpm.value = rpm.coerceIn(MIN_SHIFT_RPM, MAX_SHIFT_RPM)
    }

    override fun onCleared() {
        repository.close()
    }

    companion object {
        // Valores provisionales: ajustar con la zona de potencia / limitador real de la moto.
        const val DEFAULT_SHIFT_RPM = 8_500
        const val MIN_SHIFT_RPM = 3_000
        const val MAX_SHIFT_RPM = 11_500

        /** Para pasar a la moto real, devolver aquí el repositorio KDS en lugar del simulador. */
        val Factory = viewModelFactory {
            initializer {
                TelemetryViewModel { scope -> MockTelemetryRepository(externalScope = scope) }
            }
        }
    }
}
