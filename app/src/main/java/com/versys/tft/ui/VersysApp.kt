package com.versys.tft.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.versys.tft.domain.model.TelemetrySource
import com.versys.tft.ui.components.BottomNavBar
import com.versys.tft.ui.components.TopStatusBar
import com.versys.tft.ui.navigation.Screen
import com.versys.tft.ui.road.RoadScreen
import com.versys.tft.ui.sport.SportScreen
import com.versys.tft.ui.theme.LabelStyle
import com.versys.tft.ui.theme.VersysColors
import com.versys.tft.ui.util.AppLauncher

@Composable
fun VersysApp(vm: TelemetryViewModel = viewModel(factory = TelemetryViewModel.Factory)) {
    val context = LocalContext.current
    val telemetry by vm.telemetry.collectAsStateWithLifecycle()
    val peaks by vm.peaks.collectAsStateWithLifecycle()
    val shiftRpm by vm.shiftRpm.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf(Screen.ROAD) }

    Surface(
        modifier = Modifier.fillMaxSize().displayCutoutPadding(),
        color = VersysColors.Background,
        contentColor = VersysColors.TextPrimary,
    ) {
        Column(Modifier.fillMaxSize()) {
            TopStatusBar(telemetry)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (screen) {
                    Screen.ROAD -> RoadScreen(
                        state = telemetry,
                        simulation = vm.simulation,
                        onNavigateToFuelStation = {
                            // Con datos simulados el nombre es ficticio: se busca "gasolinera" genérico.
                            val name = telemetry.nearestFuelStationName
                                .takeIf { telemetry.source != TelemetrySource.SIMULATED }
                            AppLauncher.navigateToFuelStation(context, name)
                        },
                    )
                    Screen.SPORT -> SportScreen(
                        state = telemetry,
                        peaks = peaks,
                        shiftRpm = shiftRpm,
                        onShiftRpmChange = vm::setShiftRpm,
                        onResetPeaks = vm::resetPeaks,
                    )
                    Screen.GARAGE -> PlaceholderScreen("GARAGE")
                }
            }
            BottomNavBar(
                current = screen,
                onSelect = { screen = it },
                onLaunchApp = { AppLauncher.launchFirstInstalled(context, it.packages, it.label) },
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name · siguiente entregable", style = LabelStyle.copy(fontSize = 16.sp))
    }
}
