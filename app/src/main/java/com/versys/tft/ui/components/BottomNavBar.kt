package com.versys.tft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.versys.tft.ui.navigation.Screen
import com.versys.tft.ui.theme.VersysColors

data class ExternalApp(val label: String, val packages: List<String>, val icon: ImageVector)

val DefaultExternalApps = listOf(
    ExternalApp("Waze", listOf("com.waze"), Icons.Filled.Navigation),
    ExternalApp("OsmAnd", listOf("net.osmand.plus", "net.osmand"), Icons.Filled.Map),
    ExternalApp("Música", listOf("com.google.android.apps.youtube.music"), Icons.Filled.MusicNote),
)

/** Barra fija inferior: objetivos táctiles >= 56 dp para usar con guantes. */
@Composable
fun BottomNavBar(
    current: Screen,
    onSelect: (Screen) -> Unit,
    onLaunchApp: (ExternalApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(VersysColors.Surface1)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DefaultExternalApps.forEach { app -> AppShortcut(app) { onLaunchApp(app) } }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Screen.entries.forEach { screen ->
                ScreenTab(screen.label, selected = screen == current) { onSelect(screen) }
            }
        }
    }
}

@Composable
private fun AppShortcut(app: ExternalApp, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(width = 68.dp, height = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(VersysColors.Surface2)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(app.icon, contentDescription = app.label, tint = VersysColors.TextPrimary, modifier = Modifier.size(26.dp))
        Text(app.label, color = VersysColors.TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun ScreenTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 132.dp, height = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) VersysColors.KawasakiGreen else VersysColors.Surface2)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else VersysColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
        )
    }
}
