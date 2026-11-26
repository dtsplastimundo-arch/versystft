package com.versys.tft.ui.sport

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.versys.tft.domain.model.SessionPeaks
import com.versys.tft.domain.model.TelemetryState
import com.versys.tft.ui.components.LeanIndicator
import com.versys.tft.ui.components.Metric
import com.versys.tft.ui.components.TeslaCard
import com.versys.tft.ui.theme.LabelStyle
import com.versys.tft.ui.theme.NumberStyle
import com.versys.tft.ui.theme.VersysColors
import kotlin.math.roundToInt

private const val SHIFT_STEP_RPM = 250

/** Pantalla SPORT: marcha + acelerador · tacómetro con luz de cambio · inclinación/G + picos. */
@Composable
fun SportScreen(
    state: TelemetryState,
    peaks: SessionPeaks,
    shiftRpm: Int,
    onShiftRpmChange: (Int) -> Unit,
    onResetPeaks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LeftColumn(state, Modifier.weight(0.27f).fillMaxHeight())
        CenterColumn(state, shiftRpm, onShiftRpmChange, Modifier.weight(0.46f).fillMaxHeight())
        RightColumn(state, peaks, onResetPeaks, Modifier.weight(0.27f).fillMaxHeight())
    }
}

@Composable
private fun LeftColumn(state: TelemetryState, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TeslaCard(
            Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("MARCHA", style = LabelStyle)
            Text(state.gearLabel, style = NumberStyle.copy(fontSize = 64.sp, color = VersysColors.KawasakiGreen))
        }
        TeslaCard(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("ACELERADOR", style = LabelStyle)
                Text(
                    state.throttlePositionPct?.roundToInt()?.let { "$it%" } ?: "—",
                    style = NumberStyle.copy(fontSize = 28.sp),
                )
            }
            Spacer(Modifier.height(8.dp))
            TpsBar(state.throttlePositionPct)
        }
    }
}

@Composable
private fun CenterColumn(
    state: TelemetryState,
    shiftRpm: Int,
    onShiftRpmChange: (Int) -> Unit,
    modifier: Modifier,
) {
    TeslaCard(modifier) {
        ShiftLightBar(state.rpm, shiftRpm)
        Spacer(Modifier.height(6.dp))
        SportTachometer(state.rpm, shiftRpm, state.speedKmh, Modifier.fillMaxWidth().weight(1f))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepButton("−") { onShiftRpmChange(shiftRpm - SHIFT_STEP_RPM) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LUZ DE CAMBIO", style = LabelStyle)
                Text("%,d RPM".format(shiftRpm), style = NumberStyle.copy(fontSize = 16.sp))
            }
            StepButton("+") { onShiftRpmChange(shiftRpm + SHIFT_STEP_RPM) }
        }
    }
}

@Composable
private fun RightColumn(
    state: TelemetryState,
    peaks: SessionPeaks,
    onResetPeaks: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TeslaCard(Modifier.fillMaxWidth().weight(0.8f), verticalArrangement = Arrangement.Center) {
            val g = state.longitudinalG
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LeanIndicator(state.leanAngleDeg?.toFloat(), Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                Metric(
                    "G", g?.let { "%+.2f".format(it) } ?: "—",
                    valueSize = 22.sp, valueColor = gColor(g),
                )
            }
        }
        TeslaCard(Modifier.fillMaxWidth().weight(1.4f)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("PICOS DE SESIÓN", style = LabelStyle)
                Text(
                    "REINICIAR",
                    color = VersysColors.KawasakiGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onResetPeaks).padding(vertical = 8.dp, horizontal = 4.dp),
                )
            }
            PeakRow("VELOCIDAD", peaks.maxSpeedKmh?.roundToInt()?.let { "$it km/h" })
            PeakRow("RPM", peaks.maxRpm?.let { "%,d".format(it) })
            PeakRow(
                "INCLINACIÓN",
                if (peaks.maxLeanLeftDeg == null && peaks.maxLeanRightDeg == null) null
                else "L ${peaks.maxLeanLeftDeg.deg()} · R ${peaks.maxLeanRightDeg.deg()}",
            )
            PeakRow(
                "G ACEL / FRENO",
                if (peaks.maxAccelG == null && peaks.maxBrakeG == null) null
                else "${peaks.maxAccelG.g()} / ${peaks.maxBrakeG.g()}",
            )
        }
    }
}

@Composable
private fun PeakRow(label: String, value: String?) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = LabelStyle)
        Text(value ?: "—", style = NumberStyle.copy(fontSize = 13.sp))
    }
}

private fun Double?.deg(): String = this?.let { "${it.roundToInt()}°" } ?: "—"
private fun Double?.g(): String = this?.let { "%.2f".format(it) } ?: "—"

private fun gColor(g: Double?): Color = when {
    g == null -> VersysColors.TextPrimary
    g > 0.05 -> VersysColors.KawasakiGreen
    g < -0.05 -> VersysColors.Amber
    else -> VersysColors.TextPrimary
}
