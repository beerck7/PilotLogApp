package com.example.pilotlog.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pilotlog.R

class HorizonFragment : Fragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    
    private lateinit var horizonView: HorizonView
    private lateinit var debugText: TextView

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_horizon, container, false)
        horizonView = view.findViewById(R.id.horizon_view)
        debugText = view.findViewById(R.id.text_debug_attitude)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        }
        magnetometer?.also { mag ->
            sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private val alpha = 0.1f

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelerometerReading[0] = alpha * event.values[0] + (1 - alpha) * accelerometerReading[0]
            accelerometerReading[1] = alpha * event.values[1] + (1 - alpha) * accelerometerReading[1]
            accelerometerReading[2] = alpha * event.values[2] + (1 - alpha) * accelerometerReading[2]
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerReading[0] = alpha * event.values[0] + (1 - alpha) * magnetometerReading[0]
            magnetometerReading[1] = alpha * event.values[1] + (1 - alpha) * magnetometerReading[1]
            magnetometerReading[2] = alpha * event.values[2] + (1 - alpha) * magnetometerReading[2]
        }

        updateOrientationAngles()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private val remappedMatrix = FloatArray(9)

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            remappedMatrix
        )

        SensorManager.getOrientation(remappedMatrix, orientationAngles)

        
        var pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        var roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
        
        
        horizonView.updateAttitude(pitch, roll)
        debugText.text = "Pitch: %.1f° Roll: %.1f°".format(pitch, roll)
    }
}
