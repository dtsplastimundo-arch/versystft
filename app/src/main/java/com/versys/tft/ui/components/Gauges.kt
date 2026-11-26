package com.versys.tft.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.versys.tft.ui.theme.LabelStyle
import com.versys.tft.ui.theme.NumberStyle
import com.versys.tft.ui.theme.VersysColors
import kotlin.math.abs
import kotlin.math.min

/** Tacómetro minimalista: arco de 240° + lectura en miles de RPM. */
@Composable
fun MiniTachometer(
    rpm: Int?,
    modifier: Modifier = Modifier,
    maxRpm: Int = 12_000,
    redlineRpm: Int = 10_500,
) {
    val fraction by animateFloatAsState(
        targetValue = (rpm ?: 0).coerceIn(0, maxRpm).toFloat() / maxRpm,
        animationSpec = tween(80),
        label = "rpm",
    )
    val arcColor = when {
        (rpm ?: 0) >= redlineRpm -> VersysColors.Red
        (rpm ?: 0) >= redlineRpm - 1_500 -> VersysColors.Amber
        else -> VersysColors.KawasakiGreen
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            val stroke = 10.dp.toPx()
            val d = min(size.width, size.height) - stroke
            val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val arcSize = Size(d, d)
            drawArc(
                color = VersysColors.Surface2, startAngle = 150f, sweepAngle = 240f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = arcColor, startAngle = 150f, sweepAngle = 240f * fraction, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(rpm?.let { "%.1f".format(it / 1000f) } ?: "—", style = NumberStyle.copy(fontSize = 26.sp))
            Text("×1000 RPM", style = LabelStyle)
        }
    }
}

/** Barra de inclinación lateral centrada en 0°. Negativo = izquierda. */
@Composable
fun LeanIndicator(leanDeg: Float?, modifier: Modifier = Modifier, maxDeg: Float = 50f) {
    val lean = leanDeg ?: 0f
    val side = when {
        lean < -0.5f -> "L "
        lean > 0.5f -> "R "
        else -> ""
    }
    val color = if (abs(lean) > 35f) VersysColors.Amber else VersysColors.KawasakiGreen
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("INCLINACIÓN", style = LabelStyle)
            Text(if (leanDeg == null) "—" else "$side${abs(lean).toInt()}°", style = NumberStyle.copy(fontSize = 16.sp))
        }
        Spacer(Modifier.height(6.dp))
        Canvas(Modifier.fillMaxWidth().height(10.dp)) {
            val cx = size.width / 2f
            val x = cx + (lean / maxDeg).coerceIn(-1f, 1f) * cx
            val radius = CornerRadius(size.height / 2f)
            drawRoundRect(VersysColors.Surface2, size = size, cornerRadius = radius)
            drawRoundRect(
                color = color, topLeft = Offset(min(cx, x), 0f),
                size = Size(abs(x - cx), size.height), cornerRadius = radius,
            )
            drawLine(VersysColors.TextPrimary, Offset(cx, 0f), Offset(cx, size.height), strokeWidth = 2.dp.toPx())
        }
    }
}
