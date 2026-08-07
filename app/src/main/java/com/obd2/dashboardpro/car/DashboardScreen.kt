package com.obd2.dashboardpro.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.obd2.dashboardpro.data.GaugeConfig
import com.obd2.dashboardpro.data.DashboardProfile
import com.obd2.dashboardpro.data.ObdPid
import com.obd2.dashboardpro.data.Obd2Manager
import com.obd2.dashboardpro.data.PRESET_PROFILES
import kotlinx.coroutines.*

class DashboardScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    private val obd = Obd2Manager.getInstance()
    private var profile: DashboardProfile = PRESET_PROFILES[1] // Sport por defecto
    private var scope = CoroutineScope(Dispatchers.Main)
    private var currentValues: Map<ObdPid, Float> = emptyMap()

    init {
        lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        obd.connect(null) // demo si no hay BT; en real pasa la MAC
        scope.launch {
            obd.values.collect { vals ->
                currentValues = vals
                invalidate() // refresca AA ~5fps
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        scope.cancel()
        scope = CoroutineScope(Dispatchers.Main)
        super.onPause(owner)
    }

    override fun onGetTemplate(): Template {
        val isConnected = currentValues.isNotEmpty()

        // Header con estado
        val header = Header.Builder()
            .setTitle("Dashboard PRO • ${profile.name}")
            .setStartHeaderAction(Action.BACK)
            .build()

        // Grid de 6 gauges (2x3) - lo más compatible en AA
        val gridBuilder = GridTemplate.Builder()
            .setHeader(header)

        if (!isConnected) {
            gridBuilder.setLoading(true)
        } else {
            profile.slots.take(6).forEach { slot ->
                val pid = slot.pid
                val value = currentValues[pid] ?: 0f
                val isAlert = (slot.alertMax != null && value > slot.alertMax!!) ||
                              (slot.alertMin != null && value < slot.alertMin!!)

                val formatted = when(pid) {
                    ObdPid.RPM -> "${value.toInt()}"
                    ObdPid.SPEED -> "${value.toInt()}"
                    ObdPid.COOLANT_TEMP, ObdPid.INTAKE_TEMP -> "${value.toInt()}°"
                    ObdPid.VOLTAGE -> String.format("%.1f", value)
                    ObdPid.BOOST -> String.format("%.2f", value)
                    else -> String.format("%.0f", value)
                }

                val item = GridItem.Builder()
                    .setTitle("${pid.label}")
                    .setText("$formatted ${pid.unit} ${if(isAlert) "⚠" else ""}")
                    .setImage(
                        CarIcon.APP_ICON // puedes poner iconos por PID
                    )
                    .build()
                gridBuilder.addGridItem(item)
            }
        }

        gridBuilder.setActionStrip(
            ActionStrip.Builder()
                .addAction(
                    Action.Builder()
                        .setTitle("Tema")
                        .setOnClickListener { showThemePicker() }
                        .build()
                )
                .addAction(
                    Action.Builder()
                        .setTitle("Perfil")
                        .setOnClickListener { showProfilePicker() }
                        .build()
                )
                .build()
        )

        return gridBuilder.build()
    }

    private fun showProfilePicker() {
        screenManager.push(ProfilePickerScreen(carContext) { selected ->
            profile = selected
            screenManager.pop()
            invalidate()
        })
    }

    private fun showThemePicker() {
        // simplificado: rotar tema
        val themes = com.obd2.dashboardpro.data.ThemeId.values()
        val next = themes[(profile.theme.ordinal + 1) % themes.size]
        profile = profile.copy(theme = next)
        CarToast.makeText(carContext, "Tema: $next", CarToast.LENGTH_SHORT).show()
        invalidate()
    }
}

class ProfilePickerScreen(
    carContext: CarContext,
    private val onPick: (DashboardProfile) -> Unit
) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val listBuilder = ListTemplate.Builder()
            .setTitle("Elige perfil")
            .setHeader(Header.Builder().setTitle("Perfiles Car Scanner").build())

        PRESET_PROFILES.forEach { p ->
            listBuilder.addSingleLineListItem(
                Row.Builder()
                    .setTitle(p.name)
                    .setOnClickListener { onPick(p) }
                    .addText("${p.slots.joinToString { it.pid.label }}")
                    .setBrowsable(false)
                    .build()
            )
        }
        return listBuilder.build()
    }
}
