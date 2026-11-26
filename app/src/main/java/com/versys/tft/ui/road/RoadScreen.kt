package com.versys.tft.ui.road

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.versys.tft.domain.model.TelemetryState
import com.versys.tft.simulator.SimulationController
import com.versys.tft.ui.components.LeanIndicator
import com.versys.tft.ui.components.Metric
import com.versys.tft.ui.components.MiniTachometer
import com.versys.tft.ui.components.TeslaCard
import com.versys.tft.ui.theme.LabelStyle
import com.versys.tft.ui.theme.NumberStyle
import com.versys.tft.ui.theme.VersysColors
import kotlin.math.roundToInt

/** Pantalla ROAD: 20 % izquierda (vitales) · 60 % centro (mapa) · 20 % derecha (RPM/inclinación/trip). */
@Composable
fun RoadScreen(
    state: TelemetryState,
    simulation: SimulationController?,
    onNavigateToFuelStation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LeftPanel(state, Modifier.weight(0.2f).fillMaxHeight())
        CenterPanel(state, simulation, onNavigateToFuelStation, Modifier.weight(0.6f).fillMaxHeight())
        RightPanel(state, Modifier.weight(0.2f).fillMaxHeight())
    }
}

@Composable
private fun LeftPanel(state: TelemetryState, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TeslaCard(
            Modifier.fillMaxWidth().weight(1.25f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(state.speedKmh?.roundToInt()?.toString() ?: "—", style = NumberStyle.copy(fontSize = 64.sp))
            Text("KM/H", style = LabelStyle)
        }
        TeslaCard(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Metric("Marcha", state.gearLabel, valueSize = 44.sp, valueColor = VersysColors.KawasakiGreen)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Metric(
                        "Temp", state.coolantTempC?.roundToInt()?.toString() ?: "—",
                        unit = "°C", valueSize = 20.sp, valueColor = coolantColor(state.coolantTempC),
                    )
                    Metric(
                        "Batería", state.batteryVoltage?.let { "%.1f".format(it) } ?: "—",
                        unit = "V", valueSize = 20.sp, valueColor = batteryColor(state),
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterPanel(
    state: TelemetryState,
    simulation: SimulationController?,
    onNavigateToFuelStation: () -> Unit,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val coolant = state.coolantTempC
    val stationName = state.nearestFuelStationName
    Box(modifier.clip(shape).border(1.dp, VersysColors.Outline, shape)) {
        MapPlaceholder(Modifier.fillMaxSize())
        Text("MAPA · PLACEHOLDER", style = LabelStyle, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (simulation != null) {
                ScenarioSelector(simulation)
            }
            if (coolant != null && coolant >= COOLANT_WARN_C) {
                TempAlertBanner(coolant)
            }
        }
        if (stationName != null) {
            FuelStationCard(
                name = stationName,
                distanceKm = state.nearestFuelStationDistanceKm,
                onNavigate = onNavigateToFuelStation,
                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RightPanel(state: TelemetryState, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TeslaCard(Modifier.fillMaxWidth().weight(1.2f)) {
            MiniTachometer(state.rpm, Modifier.fillMaxSize())
        }
        TeslaCard(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.SpaceEvenly) {
            LeanIndicator(state.leanAngleDeg?.toFloat())
            Metric("Trip A", "%.1f".format(state.tripAKm), unit = "km", valueSize = 22.sp)
        }
    }
}

private fun coolantColor(c: Double?): Color = when {
    c == null -> VersysColors.TextPrimary
    c >= COOLANT_CRITICAL_C -> VersysColors.Red
    c >= COOLANT_WARN_C -> VersysColors.Amber
    else -> VersysColors.TextPrimary
}

private fun batteryColor(s: TelemetryState): Color {
    val volts = s.batteryVoltage ?: return VersysColors.TextPrimary
    val rpm = s.rpm ?: 0
    return when {
        rpm > 500 && volts < 12.0 -> VersysColors.Red
        rpm > 2_000 && volts < 13.2 -> VersysColors.Amber
        else -> VersysColors.TextPrimary
    }
}
