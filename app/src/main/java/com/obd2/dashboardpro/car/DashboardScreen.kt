package com.obd2.dashboardpro.car
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.obd2.dashboardpro.data.Obd2Manager
import com.obd2.dashboardpro.data.PRESET_PROFILES
class DashboardScreen(carContext: CarContext) : Screen(carContext) {
 private val obd = Obd2Manager.getInstance()
 private val profile = PRESET_PROFILES[0]
 override fun onGetTemplate(): Template {
  val vals = obd.values.value
  val list = ItemList.Builder()
  profile.slots.take(6).forEach { slot ->
   val v = vals[slot.pid] ?: 0f
   val txt = when(slot.pid){ com.obd2.dashboardpro.data.ObdPid.VOLTAGE -> String.format("%.1f %s", v, slot.pid.unit); com.obd2.dashboardpro.data.ObdPid.BOOST -> String.format("%.2f %s", v, slot.pid.unit); else -> "${v.toInt()} ${slot.pid.unit}"}
   list.addItem(Row.Builder().setTitle(slot.pid.label).addText(txt).build())
  }
  return ListTemplate.Builder().setTitle("Dashboard PRO • ${profile.name}").setSingleList(list.build()).setHeader(Header.Builder().setTitle("ix35 1.7 CRDi").build()).build()
 }
}
