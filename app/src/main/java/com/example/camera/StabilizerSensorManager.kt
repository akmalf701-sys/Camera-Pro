package com.example.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class StabilizerSensorManager(
    context: Context,
    private val onOrientationChanged: (
        roll: Float,
        pitch: Float,
        isSteady: Boolean,
        shakeOffsetX: Float,
        shakeOffsetY: Float,
        stabilityScore: Int
    ) -> Unit,
    private val onLightChanged: (lux: Float, isLowLight: Boolean) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val lightSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var lastRoll = 0f
    private var lastPitch = 0f
    private var filteredAccelX = 0f
    private var filteredAccelY = 0f
    private var smoothShakeX = 0f
    private var smoothShakeY = 0f

    private val smoothingFactor = 0.25f
    private val antiShakeDampening = 0.35f

    fun start() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
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

                // High-pass filter for micro hand tremors / shake
                val deltaX = x - filteredAccelX
                val deltaY = y - filteredAccelY
                filteredAccelX = filteredAccelX + 0.1f * deltaX
                filteredAccelY = filteredAccelY + 0.1f * deltaY

                // Convert acceleration jitter to pixel dampening offset (max 30px buffer)
                val rawShakeX = (deltaX * 18f).coerceIn(-32f, 32f)
                val rawShakeY = (deltaY * 18f).coerceIn(-32f, 32f)

                smoothShakeX = smoothShakeX + antiShakeDampening * (rawShakeX - smoothShakeX)
                smoothShakeY = smoothShakeY + antiShakeDampening * (rawShakeY - smoothShakeY)

                val tremorMagnitude = sqrt(smoothShakeX * smoothShakeX + smoothShakeY * smoothShakeY)
                val stabilityScore = (100 - (tremorMagnitude * 2.5f).toInt()).coerceIn(60, 100)

                val isSteady = abs(lastRoll) < 2.5f && abs(smoothShakeX) < 4f && abs(smoothShakeY) < 4f

                onOrientationChanged(
                    lastRoll,
                    lastPitch,
                    isSteady,
                    smoothShakeX,
                    smoothShakeY,
                    stabilityScore
                )
            }
            Sensor.TYPE_GYROSCOPE -> {
                val rotX = event.values[0]
                val rotY = event.values[1]
                val rotZ = event.values[2]

                // Add gyro angular velocity compensation to shake offsets
                val gyroOffsetMultiplier = 8f
                smoothShakeX = (smoothShakeX + rotY * gyroOffsetMultiplier).coerceIn(-35f, 35f)
                smoothShakeY = (smoothShakeY + rotX * gyroOffsetMultiplier).coerceIn(-35f, 35f)
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
