package com.obd2.dashboardpro

import android.bluetooth.BluetoothManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obd2.dashboardpro.data.*
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                DashboardProApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardProApp() {
    var profile by remember { mutableStateOf(PRESET_PROFILES[1]) }
    var values by remember { mutableStateOf<Map<ObdPid, Float>>(emptyMap()) }
    var connected by remember { mutableStateOf(false) }
    val obd = remember { Obd2Manager.getInstance() }

    LaunchedEffect(Unit) {
        launch { obd.values.collectLatest { values = it } }
        launch { obd.connected.collectLatest { connected = it } }
        obd.connect(null) // demo
    }

    val bg = when(profile.theme){
        ThemeId.DARK_PRO -> Brush.verticalGradient(listOf(Color(0xFF0F1115), Color(0xFF1B1F27)))
        ThemeId.SPORT_RED -> Brush.verticalGradient(listOf(Color(0xFF1A0A0A), Color(0xFF3A0F0F)))
        ThemeId.ALPINE -> Brush.verticalGradient(listOf(Color(0xFF0A1A1F), Color(0xFF143741)))
        ThemeId.CARBON -> Brush.verticalGradient(listOf(Color(0xFF121212), Color(0xFF2A2A2A)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OBD2 Dashboard PRO", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F1115), titleContentColor = Color.White),
                actions = {
                    Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.padding(12.dp))
                }
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Column(
            Modifier.fillMaxSize().background(bg).padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Estado + conectar
            Card(colors = CardDefaults.cardColors(containerColor = if(connected) Color(0xFF0F3A1F) else Color(0xFF3A1A0F)), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(if(connected) Color(0xFF1ED760) else Color(0xFFFF3B30)))
                        Column {
                            Text(if(connected) "Conectado • DEMO" else "Desconectado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("ELM327 • Android Auto listo", color = Color.White.copy(0.7f), fontSize = 11.sp)
                        }
                    }
                    FilledTonalButton(onClick = { if(connected) obd.disconnect() else obd.connect(null) }) {
                        Text(if(connected) "Desconectar" else "Conectar")
                    }
                }
            }

            // Selector perfiles
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRESET_PROFILES.forEach { p ->
                    val sel = p.name == profile.name
                    FilterChip(
                        selected = sel,
                        onClick = { profile = p },
                        label = { Text(p.name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFE11D48), labelColor = Color.White, selectedLabelColor = Color.White)
                    )
                }
            }

            // GRID 6 gauges configurables
            LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(profile.slots) { slot ->
                    val v = values[slot.pid] ?: 0f
                    GaugeCard(slot, v, onChangePid = { newPid ->
                        profile = profile.copy(slots = profile.slots.map { if(it.id==slot.id) it.copy(pid=newPid) else it })
                    })
                }
            }

            // Selector tema + export
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = {
                    val next = ThemeId.values()[(profile.theme.ordinal+1)%4]
                    profile = profile.copy(theme = next)
                }, modifier = Modifier.weight(1f)) { Text("Tema: ${profile.theme.name}") }
                Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))) {
                    Text("Exportar a Car Scanner")
                }
            }
            Text("Compatible con Android Auto: conecta USB y abre 'Dashboard PRO' en el launcher del coche.", color = Color.White.copy(0.6f), fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun GaugeCard(slot: GaugeSlot, value: Float, onChangePid: (ObdPid)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    val isAlert = (slot.alertMax != null && value > slot.alertMax!!) || (slot.alertMin != null && value < slot.alertMin!!)
    val progress = ((value - slot.pid.min) / (slot.pid.max - slot.pid.min)).coerceIn(0f,1f)

    Card(
        modifier = Modifier.height(148.dp).clip(RoundedCornerShape(18.dp)).border(1.dp, if(isAlert) Color(0xFFFF3B30) else Color.White.copy(0.08f), RoundedCornerShape(18.dp)).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.06f))
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(slot.pid.label.uppercase(), color = Color.White.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Icon(
                    when(slot.pid){
                        ObdPid.RPM -> Icons.Default.Speed
                        ObdPid.SPEED -> Icons.Default.DirectionsCar
                        ObdPid.COOLANT_TEMP -> Icons.Default.Thermostat
                        ObdPid.VOLTAGE -> Icons.Default.BatteryChargingFull
                        ObdPid.BOOST -> Icons.Default.Air
                        else -> Icons.Default.Analytics
                    }, null, tint = if(isAlert) Color(0xFFFF3B30) else Color(0xFF1ED760), modifier = Modifier.size(18.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    when(slot.pid){
                        ObdPid.RPM -> value.toInt().toString()
                        ObdPid.SPEED -> value.toInt().toString()
                        ObdPid.VOLTAGE -> String.format("%.1f", value)
                        ObdPid.BOOST -> String.format("%.2f", value)
                        else -> value.toInt().toString()
                    },
                    color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black
                )
                Text(slot.pid.unit, color = Color.White.copy(0.6f), fontSize = 11.sp)
            }
            // Barra
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(0.12f))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(if(isAlert) Color(0xFFFF3B30) else Color(0xFF1ED760)))
            }
            if(expanded) {
                DropdownMenu(expanded = true, onDismissRequest = { expanded = false }) {
                    ObdPid.values().forEach { pid ->
                        DropdownMenuItem(text = { Text("${pid.label} (${pid.unit})") }, onClick = { onChangePid(pid); expanded = false })
                    }
                }
            } else {
                Text("Toca para cambiar PID", color = Color.White.copy(0.35f), fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
