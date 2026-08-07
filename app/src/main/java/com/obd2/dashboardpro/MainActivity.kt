package com.obd2.dashboardpro
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
class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent { MaterialTheme(colorScheme = darkColorScheme()) { DashboardProApp() } }
 }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardProApp() {
 var profile by remember { mutableStateOf(PRESET_PROFILES[0]) }
 val obd = remember { Obd2Manager.getInstance() }
 val values by obd.values.collectAsState(initial = emptyMap())
 val connected by obd.connected.collectAsState(initial = false)
 LaunchedEffect(Unit) { obd.connect(null) }
 val bg = Brush.verticalGradient(listOf(Color(0xFF0F1115), Color(0xFF1B1F27)))
 Scaffold(topBar = { TopAppBar(title = { Text("OBD2 Dashboard PRO", fontWeight = FontWeight.Black) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F1115), titleContentColor = Color.White)) }, containerColor = Color.Transparent) { pad ->
  Column(Modifier.fillMaxSize().background(bg).padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
   Card(colors = CardDefaults.cardColors(containerColor = if(connected) Color(0xFF0F3A1F) else Color(0xFF3A1A0F)), shape = RoundedCornerShape(14.dp)) {
    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
     Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(if(connected) Color(0xFF1ED760) else Color(0xFFFF3B30)))
      Column { Text(if(connected) "Conectado • DEMO" else "Desconectado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("ELM327 • Android Auto listo", color = Color.White.copy(0.7f), fontSize = 11.sp) }
     }
     FilledTonalButton(onClick = { if(connected) obd.disconnect() else obd.connect(null) }) { Text(if(connected) "Desconectar" else "Conectar") }
    }
   }
   Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { PRESET_PROFILES.forEach { p -> FilterChip(selected = p.name == profile.name, onClick = { profile = p }, label = { Text(p.name, fontSize = 11.sp) }) } }
   LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
    items(profile.slots) { slot -> val v = values[slot.pid] ?: 0f; Card(Modifier.height(148.dp).clip(RoundedCornerShape(18.dp)).border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(18.dp)), colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.06f))) {
      Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
       Text(slot.pid.label.uppercase(), color = Color.White.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
       Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { Text(when(slot.pid){ObdPid.VOLTAGE -> String.format("%.1f", v); ObdPid.BOOST -> String.format("%.2f", v); else -> v.toInt().toString()}, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black); Text(slot.pid.unit, color = Color.White.copy(0.6f), fontSize = 11.sp) }
       Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(0.12f))) { Box(Modifier.fillMaxHeight().fillMaxWidth(((v - slot.pid.min)/(slot.pid.max - slot.pid.min)).coerceIn(0f,1f)).background(Color(0xFF1ED760))) }
      }
     } }
   }
   Text("Compatible con Android Auto: conecta USB y abre 'Dashboard PRO'", color = Color.White.copy(0.6f), fontSize = 11.sp)
  }
 }
}
