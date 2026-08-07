# OBD2 Dashboard PRO - Configurable + Android Auto

Dashboard profesional configurable compatible con **Android Auto** nativo (androidx.car.app 1.7+).
Diseñado para funcionar con **cualquier adaptador ELM327 Bluetooth / BLE** y como complemento visual a **Car Scanner ELM OBD2**.

### Compatibilidad
- ✅ Android Auto nativo (Google Play compliant) - categoria `carAppTypes: navigation`
- ✅ Android 8.0 - 15
- ✅ Funciona con Torque / Car Scanner / ELM327 genérico (Vgate iCar Pro, Veepeak, OBDLink)
- ✅ Sin root, sin AAAD, sin plugins XDA

### Características
- 6 gauges 100% configurables (RPM, Velocidad, Temp Refrigerante, Voltaje, Presión Turbo, Carga Motor, etc)
- 4 temas PRO: Dark, Sport Red, Alpine, Carbon
- HUD y modo noche automático
- Conexión Bluetooth Classic + BLE
- Templates Android Auto: Grid + List con datos en tiempo real
- Exportación de perfiles compatible Car Scanner

### Compilar el APK

**Opción A - Android Studio (recomendado, 1 click):**
1. Abre `OBD2-Dashboard-Pro` en Android Studio Hedgehog+
2. Espera a que sincronice Gradle
3. `Build > Build APK(s)` o `Run` en tu móvil
4. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`

**Opción B - GitHub Actions (compila en la nube sin instalar nada):**
1. Sube este proyecto a GitHub
2. Haz push y Actions compilará automáticamente el APK
3. Descarga el artefacto `OBD2-Dashboard-PRO-APK`

**Opción C - Línea de comandos (si tienes SDK):**
```bash
./gradlew assembleDebug
```

### Instalación en coche
1. Instala el APK en el móvil
2. En Ajustes > Apps > Acceso especial > Android Auto > añade OBD2 Dashboard PRO
3. Conecta ELM327 al OBD2 del coche y empareja Bluetooth (PIN 1234 / 0000)
4. Abre la app en el móvil, elige tu perfil y gauges
5. Conecta por USB a Android Auto > verás "Dashboard PRO" en el launcher

### Estructura
```
app/src/main/java/com/obd2/dashboardpro/
├── MainActivity.kt (configurador móvil)
├── data/Obd2Manager.kt (gestor ELM327)
├── data/GaugeConfig.kt (perfiles)
├── ui/theme/ & ui/components/GaugeView.kt
└── car/DashboardCarAppService.kt (servicio Android Auto)
```

Creado para Castelló de la Plana - 07/08/2026
