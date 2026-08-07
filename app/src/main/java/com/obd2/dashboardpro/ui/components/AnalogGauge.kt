package com.obd2.dashboardpro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obd2.dashboardpro.data.ObdPid
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogGauge(
    pid: ObdPid,
    value: Float,
    modifier: Modifier = Modifier,
    isAlert: Boolean = false
) {
    val progress = ((value - pid.min) / (pid.max - pid.min)).coerceIn(0f, 1f)
    val sweep = 270f // 270 grados de barrido
    val startAngle = 135f
    val needleAngle = startAngle + progress * sweep

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val r = size.minDimension / 2
            val c = Offset(size.width/2, size.height/2)
            val stroke = 10f

            // Fondo
            drawCircle(color = Color(0xFF1A1E26), radius = r, center = c)
            drawCircle(color = Color(0xFF0A0C10), radius = r - 4, center = c, style = Stroke(1.5f))

            // Arco base
            drawArc(
                color = Color.White.copy(0.10f),
                startAngle = startAngle, sweepAngle = sweep,
                useCenter = false, topLeft = Offset(c.x - r + 12, c.y - r + 12),
                size = androidx.compose.ui.geometry.Size((r-12)*2, (r-12)*2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // Arco progreso
            val progColor = when {
                isAlert -> Color(0xFFFF3B30)
                pid == ObdPid.RPM && progress > 0.85f -> Color(0xFFFF3B30)
                pid == ObdPid.COOLANT_TEMP && value > 100 -> Color(0xFFFF9500)
                else -> Color(0xFF1ED760)
            }
            drawArc(
                color = progColor, startAngle = startAngle, sweepAngle = sweep * progress,
                useCenter = false, topLeft = Offset(c.x - r + 12, c.y - r + 12),
                size = androidx.compose.ui.geometry.Size((r-12)*2, (r-12)*2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // Ticks
            for (i in 0..10) {
                val a = Math.toRadians((startAngle + i * sweep / 10).toDouble())
                val inner = r - 22
                val outer = r - 14
                val isMajor = i % 2 == 0
                val len = if (isMajor) 9f else 5f
                val x1 = c.x + cos(a).toFloat() * (inner)
                val y1 = c.y + sin(a).toFloat() * (inner)
                val x2 = c.x + cos(a).toFloat() * (inner + len)
                val y2 = c.y + sin(a).toFloat() * (inner + len)
                drawLine(
                    color = Color.White.copy(if (isMajor) 0.55f else 0.25f),
                    start = Offset(x1, y1), end = Offset(x2, y2),
                    strokeWidth = if (isMajor) 2.2f else 1.2f, cap = StrokeCap.Round
                )
            }
            // Aguja
            rotate(degrees = needleAngle - 90, pivot = c) {
                val needleLen = r - 28
                drawLine(
                    color = Color.White,
                    start = c, end = Offset(c.x, c.y - needleLen),
                    strokeWidth = 3.2f, cap = StrokeCap.Round
                )
                // Sombra aguja
                drawLine(
                    color = progColor.copy(0.45f),
                    start = c, end = Offset(c.x, c.y - needleLen),
                    strokeWidth = 7f, cap = StrokeCap.Round, alpha = 0.25f
                )
            }
            // Centro
            drawCircle(color = Color(0xFF2A2F3A), radius = 14f, center = c)
            drawCircle(color = Color.White, radius = 9f, center = c)
            drawCircle(color = Color(0xFF0A0C10), radius = 3.5f, center = c)
        }
        // Textos centrales
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center).padding(top = 34.dp)) {
            Text(
                text = when(pid){
                    ObdPid.RPM -> value.toInt().toString()
                    ObdPid.VOLTAGE -> String.format("%.1f", value)
                    ObdPid.BOOST -> String.format("%.2f", value)
                    else -> value.toInt().toString()
                },
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, lineHeight = 20.sp
            )
            Text(pid.unit, color = Color.White.copy(0.55f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        // Label arriba
        Text(pid.label.uppercase(), color = Color.White.copy(0.65f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp))
    }
}
