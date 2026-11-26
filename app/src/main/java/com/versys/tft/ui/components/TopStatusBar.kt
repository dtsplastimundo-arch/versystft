package com.versys.tft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.versys.tft.domain.model.TelemetrySource
import com.versys.tft.domain.model.TelemetryState
import com.versys.tft.ui.theme.NumberStyle
import com.versys.tft.ui.theme.VersysColors
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TopStatusBar(state: TelemetryState, modifier: Modifier = Modifier) {
    val (connLabel, connColor) = when {
        state.source == TelemetrySource.SIMULATED -> "SIMULADOR" to VersysColors.Amber
        state.kdsConnected -> "KDS" to VersysColors.KawasakiGreen
        state.bluetoothConnected -> "BT SIN KDS" to VersysColors.Amber
        else -> "SIN ENLACE" to VersysColors.Red
    }
    val (gpsIcon, gpsColor) =
        if (state.gpsAvailable) Icons.Filled.GpsFixed to VersysColors.KawasakiGreen
        else Icons.Filled.GpsOff to VersysColors.Red

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(VersysColors.Surface1)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = buildAnnotatedString {
                append("VERSYS 1000 ")
                withStyle(SpanStyle(color = VersysColors.KawasakiGreen)) { append("TFT") }
            },
            color = VersysColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bluetooth, contentDescription = "Bluetooth", tint = connColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(connLabel, color = connColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.width(20.dp))
            Icon(gpsIcon, contentDescription = "GPS", tint = gpsColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(20.dp))
            Text(rememberClock(), style = NumberStyle.copy(fontSize = 18.sp))
        }
    }
}

@Composable
private fun rememberClock(): String {
    val fmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val time by produceState(initialValue = LocalTime.now().format(fmt)) {
        while (true) {
            value = LocalTime.now().format(fmt)
            delay(1_000)
        }
    }
    return time
}
