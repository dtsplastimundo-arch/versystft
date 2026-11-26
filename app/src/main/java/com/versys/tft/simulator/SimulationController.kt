package com.versys.tft.simulator

import kotlinx.coroutines.flow.StateFlow

/**
 * Control opcional de escenarios. Solo lo implementan los repositorios simulados;
 * el repositorio KDS real no lo implementa y la UI oculta el selector.
 */
interface SimulationController {
    val scenario: StateFlow<SimulationScenario>
    fun setSimulationMode(mode: SimulationScenario)
}
