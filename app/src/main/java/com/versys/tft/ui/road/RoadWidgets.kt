package com.versys.tft.ui.road

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.versys.tft.simulator.SimulationController
import com.versys.tft.simulator.SimulationScenario
import com.versys.tft.ui.theme.LabelStyle
import com.versys.tft.ui.theme.NumberStyle
import com.versys.tft.ui.theme.VersysColors
import kotlin.math.roundToInt

// Umbrales provisionales de temperatura de refrigerante: ajustar con valores reales de la moto.
internal const val COOLANT_WARN_C = 105.0
internal const val COOLANT_CRITICAL_C = 112.0

/**
 * Placeholder del mapa: cuadrícula + ruta + marcador de posición.
 * Sustituir por MapLibre / osmdroid con teselas offline manteniendo la misma firma.
 */
@Composable
fun MapPlaceholder(modifier: Modifier = Modifier) {
    Canvas(modifier.background(VersysColors.MapBackground)) {
        val grid = 48.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(VersysColors.MapGrid, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            x += grid
        }
        var y = 0f
        while (y < size.height) {
            drawLine(VersysColors.MapGrid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            y += grid
        }
        val w = size.width
        val h = size.height
        val route = Path().apply {
            moveTo(w * 0.50f, h * 0.85f)
            cubicTo(w * 0.50f, h * 0.60f, w * 0.25f, h * 0.50f, w * 0.30f, h * 0.25f)
            cubicTo(w * 0.33f, h * 0.10f, w * 0.70f, h * 0.10f, w * 0.80f, h * 0.05f)
        }
        drawPath(route, VersysColors.KawasakiGreen.copy(alpha = 0.25f), style = Stroke(14.dp.toPx(), cap = StrokeCap.Round))
        drawPath(route, VersysColors.KawasakiGreen, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
        val me = Offset(w * 0.50f, h * 0.85f)
        drawCircle(Color.White, 12.dp.toPx(), me)
        drawCircle(VersysColors.KawasakiGreen, 8.dp.toPx(), me)
    }
}

/** Tarjeta flotante: gasolinera más cercana + botón de navegación directa. */
@Composable
fun FuelStationCard(
    name: String,
    distanceKm: Double?,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .background(VersysColors.Surface1.copy(alpha = 0.95f), shape)
            .border(1.dp, VersysColors.Outline, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.LocalGasStation, contentDescription = null, tint = VersysColors.KawasakiGreen, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("GASOLINERA MÁS CERCANA", style = LabelStyle)
            Text(name, color = VersysColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                distanceKm?.let { "%.1f km".format(it) } ?: "—",
                style = NumberStyle.copy(fontSize = 22.sp, color = VersysColors.KawasakiGreen),
            )
        }
        Button(
            onClick = onNavigate,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VersysColors.KawasakiGreen, contentColor = Color.Black),
        ) {
            Icon(Icons.Filled.Navigation, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("IR", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

/** Aviso de temperatura alta: ámbar desde COOLANT_WARN_C, rojo desde COOLANT_CRITICAL_C. */
@Composable
fun TempAlertBanner(tempC: Double, modifier: Modifier = Modifier) {
    val color = if (tempC >= COOLANT_CRITICAL_C) VersysColors.Red else VersysColors.Amber
    Row(
        modifier = modifier
            .background(color, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "TEMPERATURA ALTA · ${tempC.roundToInt()} °C",
            color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold,
        )
    }
}

/** Selector de escenario: solo se muestra cuando el repositorio activo es un simulador. */
@Composable
fun ScenarioSelector(controller: SimulationController, modifier: Modifier = Modifier) {
    val current by controller.scenario.collectAsStateWithLifecycle()
    val shape = RoundedCornerShape(12.dp)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SimulationScenario.entries.forEach { scenario ->
            val selected = scenario == current
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(shape)
                    .background(if (selected) VersysColors.KawasakiGreen else VersysColors.Surface1.copy(alpha = 0.95f))
                    .border(1.dp, VersysColors.Outline, shape)
                    .clickable { controller.setSimulationMode(scenario) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    scenario.label.uppercase(),
                    color = if (selected) Color.Black else VersysColors.TextPrimary,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp,
                )
            }
        }
    }
}
