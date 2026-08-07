package com.obd2.dashboardpro.data

enum class ObdPid(val label: String, val unit: String, val obdCommand: String, val min: Float, val max: Float) {
    RPM("RPM", "rpm", "010C", 0f, 8000f),
    SPEED("Velocidad", "km/h", "010D", 0f, 250f),
    COOLANT_TEMP("Refrigerante", "°C", "0105", -40f, 215f),
    INTAKE_TEMP("Admisión", "°C", "010F", -40f, 215f),
    ENGINE_LOAD("Carga motor", "%", "0104", 0f, 100f),
    THROTTLE("Acelerador", "%", "0111", 0f, 100f),
    VOLTAGE("Batería", "V", "0142", 0f, 20f),
    BOOST("Presión Turbo", "bar", "010B", 0f, 3f),
    MAF("MAF", "g/s", "0110", 0f, 655f),
    FUEL_LEVEL("Combustible", "%", "012F", 0f, 100f),
    FUEL_CONSUMPTION("Consumo", "L/100", "015E", 0f, 30f),
    TIMING("Avance", "°", "010E", -64f, 64f)
}

enum class ThemeId { DARK_PRO, SPORT_RED, ALPINE, CARBON }

data class GaugeSlot(
    val id: Int,
    var pid: ObdPid,
    var enabled: Boolean = true,
    var alertMin: Float? = null,
    var alertMax: Float? = null
)

data class DashboardProfile(
    val name: String = "Sport",
    val theme: ThemeId = ThemeId.DARK_PRO,
    val slots: List<GaugeSlot> = listOf(
        GaugeSlot(0, ObdPid.RPM, true, null, 6500f),
        GaugeSlot(1, ObdPid.SPEED),
        GaugeSlot(2, ObdPid.COOLANT_TEMP, true, null, 105f),
        GaugeSlot(3, ObdPid.BOOST),
        GaugeSlot(4, ObdPid.VOLTAGE, true, 11.8f, null),
        GaugeSlot(5, ObdPid.ENGINE_LOAD)
    )
)

// Perfiles predefinidos compatibles con Car Scanner
val PRESET_PROFILES = listOf(
    // PERFIL ESTRELLA PARA TU COCHE
    DashboardProfile("ix35 1.7 CRDi ANALOG", ThemeId.DARK_PRO, listOf(
        GaugeSlot(0, ObdPid.RPM, true, null, 4500f), // corte 4800, roja 4500
        GaugeSlot(1, ObdPid.SPEED),
        GaugeSlot(2, ObdPid.BOOST, true, null, 1.70f), // pico 1.6 bar
        GaugeSlot(3, ObdPid.COOLANT_TEMP, true, null, 102f),
        GaugeSlot(4, ObdPid.INTAKE_TEMP, true, null, 65f),
        GaugeSlot(5, ObdPid.ENGINE_LOAD) // si tienes extendido, cambia por DPF_TEMP en Car Scanner
    )),
    DashboardProfile("Diario", ThemeId.DARK_PRO, listOf(
        GaugeSlot(0, ObdPid.SPEED), GaugeSlot(1, ObdPid.RPM),
        GaugeSlot(2, ObdPid.COOLANT_TEMP), GaugeSlot(3, ObdPid.FUEL_LEVEL),
        GaugeSlot(4, ObdPid.VOLTAGE), GaugeSlot(5, ObdPid.FUEL_CONSUMPTION)
    )),
    DashboardProfile("Sport", ThemeId.SPORT_RED, listOf(
        GaugeSlot(0, ObdPid.RPM, true, null, 6800f), GaugeSlot(1, ObdPid.SPEED),
        GaugeSlot(2, ObdPid.BOOST), GaugeSlot(3, ObdPid.INTAKE_TEMP),
        GaugeSlot(4, ObdPid.THROTTLE), GaugeSlot(5, ObdPid.ENGINE_LOAD)
    )),
    DashboardProfile("4x4 / Diesel", ThemeId.CARBON, listOf(
        GaugeSlot(0, ObdPid.COOLANT_TEMP), GaugeSlot(1, ObdPid.BOOST),
        GaugeSlot(2, ObdPid.MAF), GaugeSlot(3, ObdPid.VOLTAGE),
        GaugeSlot(4, ObdPid.FUEL_LEVEL), GaugeSlot(5, ObdPid.ENGINE_LOAD)
    )),
    DashboardProfile("Eléctrico/Híbrido", ThemeId.ALPINE, listOf(
        GaugeSlot(0, ObdPid.SPEED), GaugeSlot(1, ObdPid.VOLTAGE),
        GaugeSlot(2, ObdPid.FUEL_LEVEL), GaugeSlot(3, ObdPid.COOLANT_TEMP),
        GaugeSlot(4, ObdPid.ENGINE_LOAD), GaugeSlot(5, ObdPid.FUEL_CONSUMPTION)
    ))
)
