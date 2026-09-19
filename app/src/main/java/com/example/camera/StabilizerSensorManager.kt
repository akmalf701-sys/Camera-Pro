package com.example.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.sqrt

class StabilizerSensorManager(
    context: Context,
    private val onOrientationChanged: (roll: Float, pitch: Float, isSteady: Boolean) -> Unit,
    private val onLightChanged: (lux: Float, isLowLight: Boolean) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var lastRoll = 0f
    private var lastPitch = 0f
    private val smoothingFactor = 0.2f

    fun start() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        lightSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate roll (tilt left/right) and pitch (tilt forward/backward)
                val rawRoll = (atan2(-x.toDouble(), sqrt((y * y + z * z).toDouble())) * (180.0 / Math.PI)).toFloat()
                val rawPitch = (atan2(y.toDouble(), sqrt((x * x + z * z).toDouble())) * (180.0 / Math.PI)).toFloat()

                // Low-pass filter for smooth horizon line display
                lastRoll = lastRoll + smoothingFactor * (rawRoll - lastRoll)
                lastPitch = lastPitch + smoothingFactor * (rawPitch - lastPitch)

                // Steady if roll is close to 0 (landscape or portrait upright) within +/- 2.5 degrees
                val isSteady = kotlin.math.abs(lastRoll) < 2.5f

                onOrientationChanged(lastRoll, lastPitch, isSteady)
            }
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                val isLowLight = lux < 20f
                onLightChanged(lux, isLowLight)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
