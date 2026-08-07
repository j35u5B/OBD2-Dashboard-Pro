package com.obd2.dashboardpro.car

import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class DashboardCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return DashboardSession()
    }
}

class DashboardSession : Session() {
    override fun onCreateScreen(intent: android.content.Intent): Screen {
        return DashboardScreen(carContext)
    }
}
