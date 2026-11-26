package com.versys.tft.simulator

/** Escenarios del simulador para probar la interfaz sin la moto. */
enum class SimulationScenario(val label: String) {
    CRUISING("Crucero"),
    PARKED_IGNITION_ON("Estacionada"),
    OVERHEAT_ALERT("Sobrecalentamiento"),
}
