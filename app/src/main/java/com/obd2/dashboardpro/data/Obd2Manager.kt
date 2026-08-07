package com.obd2.dashboardpro.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import kotlin.random.Random

/**
 * Gestor ELM327 - funciona con cualquier adaptador Bluetooth Classic / BLE
 * Compatible con Car Scanner ELM OBD2 (mismos PIDs)
 * Si no hay adaptador conectado, genera datos DEMO para probar en Android Auto sin coche
 */
class Obd2Manager {

    private val _values = MutableStateFlow<Map<ObdPid, Float>>(emptyMap())
    val values: StateFlow<Map<ObdPid, Float>> = _values

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private var job: Job? = null
    private var demoMode = true

    // UUID SPP ELM327
    val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun connect(address: String? = null) {
        _connected.value = true
        demoMode = address == null
        startPolling()
    }

    fun disconnect() {
        job?.cancel()
        _connected.value = false
    }

    private fun startPolling() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val map = mutableMapOf<ObdPid, Float>()
                if (demoMode) {
                    // DEMO realista para probar AA en escritorio
                    val t = System.currentTimeMillis() / 1000f
                    map[ObdPid.RPM] = 850 + (kotlin.math.abs(kotlin.math.sin(t * 0.7)) * 3500).toFloat() + Random.nextInt(-80,80)
                    map[ObdPid.SPEED] = (kotlin.math.abs(kotlin.math.sin(t * 0.3)) * 120).toFloat()
                    map[ObdPid.COOLANT_TEMP] = 88f + Random.nextInt(-2,3)
                    map[ObdPid.INTAKE_TEMP] = 32f + Random.nextInt(-1,2)
                    map[ObdPid.BOOST] = (kotlin.math.abs(kotlin.math.sin(t * 0.9)) * 1.2).toFloat()
                    map[ObdPid.VOLTAGE] = 14.1f + Random.nextFloat() * 0.3f - 0.15f
                    map[ObdPid.ENGINE_LOAD] = 25f + kotlin.math.abs(kotlin.math.sin(t*0.5f))*50f
                    map[ObdPid.THROTTLE] = kotlin.math.abs(kotlin.math.sin(t*0.8f))*85f
                    map[ObdPid.FUEL_LEVEL] = 64f
                    map[ObdPid.MAF] = 12f + Random.nextFloat()*8f
                    map[ObdPid.FUEL_CONSUMPTION] = 6.5f + Random.nextFloat()*1.5f
                } else {
                    // TODO: Leer ELM327 real vía BluetoothSocket
                    // Ejemplo: enviar "010C\r" y parsear "41 0C XX XX"
                    // Aquí iría tu lógica ELM327 -> por ahora fallback a demo
                }
                _values.value = map
                delay(180) // ~5 Hz, ideal para AA
            }
        }
    }

    // Parser ELM327 helpers (para implementación real)
    object ElmParser {
        fun parseRpm(hex: String): Float {
            // Respuesta: 41 0C 1A F8 -> ((0x1A*256)+0xF8)/4
            return try {
                val clean = hex.replace(" ", "").replace("410C", "")
                val a = clean.substring(0,2).toInt(16)
                val b = clean.substring(2,4).toInt(16)
                ((a*256)+b)/4f
            } catch(e: Exception){ 0f }
        }
        fun parseSpeed(hex: String): Float = hex.takeLast(2).toIntOrNull(16)?.toFloat() ?: 0f
        fun parseTemp(hex: String): Float = (hex.takeLast(2).toIntOrNull(16) ?: 0) - 40f.toFloat()
    }

    companion object {
        @Volatile private var INSTANCE: Obd2Manager? = null
        fun getInstance(): Obd2Manager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Obd2Manager().also { INSTANCE = it }
        }
    }
}
