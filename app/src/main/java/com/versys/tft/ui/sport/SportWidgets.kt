package com.versys.tft.ui.sport

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.versys.tft.domain.model.ShiftLight
import com.versys.tft.ui.theme.LabelStyle
import com.versys.tft.ui.theme.NumberStyle
import com.versys.tft.ui.theme.VersysColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** Luz de cambio: 10 segmentos verde→ámbar→rojo; al llegar al RPM de cambio parpadea toda en rojo. */
@Composable
fun ShiftLightBar(rpm: Int?, shiftRpm: Int, modifier: Modifier = Modifier) {
    val lit = ShiftLight.litSegments(rpm, shiftRpm)
    val shifting = ShiftLight.isShifting(rpm, shiftRpm)
    val flash by rememberInfiniteTransition(label = "shift").animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(90), RepeatMode.Reverse),
        label = "flash",
    )
    val alpha = if (shifting) flash else 1f   // solo lee la animación mientras se cambia
    Row(
        modifier.fillMaxWidth().height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(ShiftLight.SEGMENTS) { i ->
            val base = when {
                shifting -> VersysColors.Red
                i < 4 -> VersysColors.KawasakiGreen
                i < 7 -> VersysColors.Amber
                else -> VersysColors.Red
            }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (i < lit) base.copy(alpha = alpha) else VersysColors.Surface2)
            )
        }
    }
}

/** Tacómetro grande: arco de 240°, marcas cada 1000 RPM y marca blanca en el RPM de cambio. */
@Composable
fun SportTachometer(
    rpm: Int?,
    shiftRpm: Int,
    speedKmh: Double?,
    modifier: Modifier = Modifier,
    maxRpm: Int = 12_000,
) {
    val value = rpm ?: 0
    val fraction by animateFloatAsState(
        targetValue = value.coerceIn(0, maxRpm).toFloat() / maxRpm,
        animationSpec = tween(80),
        label = "sportRpm",
    )
    val arcColor = when {
        value >= shiftRpm -> VersysColors.Red
        value >= shiftRpm - 800 -> VersysColors.Amber
        else -> VersysColors.KawasakiGreen
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            val stroke = 16.dp.toPx()
            val d = min(size.width, size.height) - stroke
            val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val arcSize = Size(d, d)
            val center = Offset(size.width / 2f, size.height / 2f)
            val rMid = d / 2f
            drawArc(
                color = VersysColors.Surface2, startAngle = 150f, sweepAngle = 240f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Butt),
            )
            drawArc(
                color = arcColor, startAngle = 150f, sweepAngle = 240f * fraction, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Butt),
            )
            for (k in 0..(maxRpm / 1_000)) {
                val angle = 150f + 240f * (k * 1_000f) / maxRpm
                radialLine(center, rMid - stroke / 2f - 7.dp.toPx(), rMid - stroke / 2f - 1.dp.toPx(),
                    angle, VersysColors.TextSecondary, 2.dp.toPx())
            }
            val shiftAngle = 150f + 240f * shiftRpm.coerceIn(0, maxRpm).toFloat() / maxRpm
            radialLine(center, rMid - stroke / 2f - 11.dp.toPx(), rMid + stroke / 2f + 2.dp.toPx(),
                shiftAngle, Color.White, 3.dp.toPx())
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(rpm?.let { "%,d".format(it) } ?: "—", style = NumberStyle.copy(fontSize = 38.sp))
            Text("RPM", style = LabelStyle)
            Text(
                speedKmh?.let { "${it.roundToInt()} km/h" } ?: "— km/h",
                style = NumberStyle.copy(fontSize = 16.sp, color = VersysColors.TextSecondary),
            )
        }
    }
}

private fun DrawScope.radialLine(
    center: Offset, rInner: Float, rOuter: Float, angleDeg: Float, color: Color, width: Float,
) {
    val a = Math.toRadians(angleDeg.toDouble())
    val c = cos(a).toFloat()
    val s = sin(a).toFloat()
    drawLine(
        color,
        Offset(center.x + rInner * c, center.y + rInner * s),
        Offset(center.x + rOuter * c, center.y + rOuter * s),
        strokeWidth = width,
    )
}

/** Barra horizontal de apertura de acelerador (0–100 %). */
@Composable
fun TpsBar(percent: Double?, modifier: Modifier = Modifier) {
    val fraction by animateFloatAsState(
        targetValue = ((percent ?: 0.0) / 100.0).coerceIn(0.0, 1.0).toFloat(),
        animationSpec = tween(150),
        label = "tps",
    )
    Box(
        modifier.fillMaxWidth().height(14.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(VersysColors.Surface2)
    ) {
        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(VersysColors.KawasakiGreen))
    }
}

/** Botón táctil grande (44 dp) para ajustar el RPM de cambio. */
@Composable
fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(VersysColors.Surface2)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = VersysColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}
