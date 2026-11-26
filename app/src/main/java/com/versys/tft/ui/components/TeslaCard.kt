package com.versys.tft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.versys.tft.ui.theme.LabelStyle
import com.versys.tft.ui.theme.NumberStyle
import com.versys.tft.ui.theme.VersysColors

@Composable
fun TeslaCard(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(VersysColors.Surface1)
            .border(1.dp, VersysColors.Outline, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

/** Etiqueta pequeña + valor grande + unidad opcional. */
@Composable
fun Metric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    valueSize: TextUnit = 28.sp,
    valueColor: Color = VersysColors.TextPrimary,
) {
    Column(modifier) {
        Text(label.uppercase(), style = LabelStyle)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = NumberStyle.copy(fontSize = valueSize, color = valueColor))
            if (unit != null) {
                Text(unit, style = LabelStyle, modifier = Modifier.padding(start = 3.dp, bottom = 4.dp))
            }
        }
    }
}
