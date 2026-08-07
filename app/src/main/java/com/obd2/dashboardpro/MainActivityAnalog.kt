// MainActivityAnalog.kt — Reemplaza MainActivity.kt si quieres modo analógico por defecto
// Copia este contenido a MainActivity.kt para activar los relojes analógicos en el móvil
package com.obd2.dashboardpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.obd2.dashboardpro.data.*
import com.obd2.dashboardpro.ui.components.AnalogGauge
import kotlinx.coroutines.flow.collectLatest

class MainActivityAnalog : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var profile by remember { mutableStateOf(PRESET_PROFILES[1]) }
                var values by remember { mutableStateOf<Map<ObdPid, Float>>(emptyMap()) }
                val obd = remember { Obd2Manager.getInstance() }
                LaunchedEffect(Unit) {
                    launch { obd.values.collectLatest { values = it } }
                    obd.connect(null)
                }
                val bg = Brush.verticalGradient(listOf(Color(0xFF0F1115), Color(0xFF1B1F27)))
                Column(Modifier.fillMaxSize().background(bg).padding(12.dp)) {
                    Text("DASHBOARD PRO • ANALOG", color = Color.White, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    // 2 grandes arriba
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        profile.slots.take(2).forEach { slot ->
                            val v = values[slot.pid] ?: 0f
                            val alert = (slot.alertMax != null && v > slot.alertMax!!)
                            Column(Modifier.weight(1f)) {
                                AnalogGauge(pid = slot.pid, value = v, isAlert = alert)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                        itemsIndexed(profile.slots.drop(2)) { idx, slot ->
                            val v = values[slot.pid] ?: 0f
                            AnalogGauge(pid = slot.pid, value = v, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}
