package com.example.data.prayer

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math. asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QiblaSensorState(
  val userLatitude: Double = 34.0837,
  val userLongitude: Double = 74.7973,
  val locationLabel: String = "Srinagar, Kashmir",
  val hasRotationVector: Boolean = true,
  val hasAccelerometer: Boolean = true,
  val hasMagnetometer: Boolean = true,
  val activeSensorModeLabel: String = "Rotation Vector (Primary)",
  val isListening: Boolean = false,
  val hasLiveFix: Boolean = false,
  val manualHeadingOverrideDeg: Float? = null,
  val magneticAzimuthDeg: Float = 0f,
  val trueAzimuthDeg: Float = 0f,
  val magneticDeclinationDeg: Float = 0f,
  val qiblaBearingFromTrueNorthDeg: Double = 256.8,
  val qiblaCardinalDirection: String = "WSW",
  val distanceToKaabaKm: Double = 3694.7,
  val relativeQiblaRotationDeg: Float = 256.8f,
  val signedAlignmentDeltaDeg: Float = -103.2f,
  val isAlignedWithQibla: Boolean = false,
  val pitchDeg: Float = 0f,
  val rollDeg: Float = 0f,
  val isDeviceHeldFlat: Boolean = true,
  val magneticFieldStrengthUt: Float = 45.0f,
  val isMagneticInterferenceDetected: Boolean = false,
  val sensorAccuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
  val accuracyLabel: String = "High Accuracy (Calibrated)"
) {
  val isSensorAvailable: Boolean
    get() = hasRotationVector || (hasAccelerometer && hasMagnetometer)

  val isReadingReliable: Boolean
    get() = !isMagneticInterferenceDetected && sensorAccuracy != SensorManager.SENSOR_STATUS_UNRELIABLE
}

object QiblaCompassCalculator {

  // Sacred coordinates of the Holy Kaaba in Makkah al-Mukarramah
  const val KAABA_LATITUDE = 21.4224779
  const val KAABA_LONGITUDE = 39.8251832
  private const val EARTH_MEAN_RADIUS_KM = 6371.0088
  const val ALIGNMENT_TOLERANCE_DEG = 5.0f

  /**
   * Normalizes a longitude in degrees to [-180.0, +180.0].
   */
  fun normalizeLongitudeDeg(lonDeg: Double): Double {
    var norm = (lonDeg + 180.0) % 360.0
    if (norm < 0.0) norm += 360.0
    return norm - 180.0
  }

  /**
   * Calculates the exact initial great-circle bearing (forward azimuth) in degrees [0.0, 360.0)
   * from the user's geographic coordinates to the Holy Kaaba using spherical trigonometry.
   */
  fun calculateQiblaBearing(userLatitude: Double, userLongitude: Double): Double {
    val normLon = normalizeLongitudeDeg(userLongitude)
    if (abs(userLatitude - KAABA_LATITUDE) < 1e-6 && abs(normLon - KAABA_LONGITUDE) < 1e-6) {
      return 0.0
    }
    val phi1 = Math.toRadians(userLatitude)
    val phi2 = Math.toRadians(KAABA_LATITUDE)
    val deltaLambda = Math.toRadians(normalizeLongitudeDeg(KAABA_LONGITUDE - normLon))

    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    val thetaRad = atan2(y, x)
    return (Math.toDegrees(thetaRad) + 360.0) % 360.0
  }

  /**
   * Calculates the great-circle distance in kilometers between the user's location and the Holy Kaaba
   * using the Haversine formula.
   */
  fun calculateDistanceToKaabaKm(userLatitude: Double, userLongitude: Double): Double {
    val dLat = Math.toRadians(KAABA_LATITUDE - userLatitude)
    val dLon = Math.toRadians(KAABA_LONGITUDE - userLongitude)
    val lat1 = Math.toRadians(userLatitude)
    val lat2 = Math.toRadians(KAABA_LATITUDE)

    val sinDLat = sin(dLat / 2.0)
    val sinDLon = sin(dLon / 2.0)
    val a = (sinDLat * sinDLat + cos(lat1) * cos(lat2) * sinDLon * sinDLon).coerceIn(0.0, 1.0)
    val c = 2.0 * asin(sqrt(a))
    return EARTH_MEAN_RADIUS_KM * c
  }

  /**
   * Computes local magnetic declination (difference between True North and Magnetic North) in degrees
   * using the World Magnetic Model provided by Android's [GeomagneticField].
   */
  fun computeMagneticDeclination(
    userLatitude: Double,
    userLongitude: Double,
    altitudeMeters: Float = 0f,
    timeMillis: Long = System.currentTimeMillis()
  ): Float {
    return try {
      val field = GeomagneticField(
        userLatitude.toFloat(),
        userLongitude.toFloat(),
        altitudeMeters,
        timeMillis
      )
      field.declination
    } catch (_: Exception) {
      0f
    }
  }

  /**
   * Computes the shortest signed angular delta in [-180f, +180f] from [currentHeadingDeg] to [targetBearingDeg].
   * Positive values indicate turning right (clockwise); negative values indicate turning left (counter-clockwise).
   */
  fun computeShortestSignedAngleDelta(currentHeadingDeg: Float, targetBearingDeg: Float): Float {
    var diff = (targetBearingDeg - currentHeadingDeg) % 360f
    if (diff > 180f) diff -= 360f
    if (diff < -180f) diff += 360f
    return diff
  }

  /**
   * Formats a bearing in degrees [0, 360) into a 16-wind compass cardinal label (e.g., "WSW").
   */
  fun formatCardinalDirection(degrees: Double): String {
    val normalized = ((degrees % 360.0) + 360.0) % 360.0
    val directions = arrayOf(
      "N", "NNE", "NE", "ENE",
      "E", "ESE", "SE", "SSE",
      "S", "SSW", "SW", "WSW",
      "W", "WNW", "NW", "NNW"
    )
    val index = ((normalized + 11.25) / 22.5).toInt() % 16
    return directions[index]
  }

  /**
   * Applies an exponential low-pass filter to raw 3-axis sensor readings to suppress high-frequency jitter.
   */
  fun lowPassFilter(input: FloatArray, output: FloatArray?, alpha: Float = 0.18f): FloatArray {
    if (output == null || output.size != input.size) {
      return input.copyOf()
    }
    for (i in input.indices) {
      output[i] = output[i] + alpha * (input[i] - output[i])
    }
    return output
  }

  /**
   * Circularly smooths an azimuth angle in [0f, 360f) using unit-circle sin/cos interpolation
   * so transitions across the 359° <-> 0° boundary never jump or wrap backward.
   */
  fun smoothCircularAzimuth(previousDeg: Float, currentDeg: Float, alpha: Float = 0.22f): Float {
    val prevRad = Math.toRadians(previousDeg.toDouble())
    val currRad = Math.toRadians(currentDeg.toDouble())
    val sinAvg = (1.0 - alpha) * sin(prevRad) + alpha * sin(currRad)
    val cosAvg = (1.0 - alpha) * cos(prevRad) + alpha * cos(currRad)
    val smoothedDeg = Math.toDegrees(atan2(sinAvg, cosAvg)).toFloat()
    return (smoothedDeg + 360f) % 360f
  }

  /**
   * Computes magnetic azimuth [0f, 360f), pitch, and roll from accelerometer and magnetometer vectors
   * using [SensorManager.getRotationMatrix] and [SensorManager.getOrientation].
   */
  fun computeOrientationFromSensors(
    accelerometerReading: FloatArray,
    magnetometerReading: FloatArray,
    displayRotation: Int = Surface.ROTATION_0
  ): Triple<Float, Float, Float>? {
    if (accelerometerReading.size < 3 || magnetometerReading.size < 3) return null
    val rotationMatrix = FloatArray(9)
    val inclinationMatrix = FloatArray(9)
    val success = SensorManager.getRotationMatrix(
      rotationMatrix,
      inclinationMatrix,
      accelerometerReading,
      magnetometerReading
    )
    if (!success) return null

    val remappedMatrix = FloatArray(9)
    when (displayRotation) {
      Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
        rotationMatrix,
        SensorManager.AXIS_Y,
        SensorManager.AXIS_MINUS_X,
        remappedMatrix
      )
      Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
        rotationMatrix,
        SensorManager.AXIS_MINUS_X,
        SensorManager.AXIS_MINUS_Y,
        remappedMatrix
      )
      Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
        rotationMatrix,
        SensorManager.AXIS_MINUS_Y,
        SensorManager.AXIS_X,
        remappedMatrix
      )
      else -> System.arraycopy(rotationMatrix, 0, remappedMatrix, 0, 9)
    }

    val orientationAngles = FloatArray(3)
    SensorManager.getOrientation(remappedMatrix, orientationAngles)
    var azimuthDeg = ((Math.toDegrees(orientationAngles[0].toDouble()).toFloat()) + 360f) % 360f
    val pitchDeg = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
    val rollDeg = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

    // Tilt compensation when device is held steep in hand (>50° pitch) to avoid gimbal lock
    if (abs(pitchDeg) > 50f) {
      val tiltRemapped = FloatArray(9)
      if (SensorManager.remapCoordinateSystem(
          remappedMatrix,
          SensorManager.AXIS_X,
          SensorManager.AXIS_Z,
          tiltRemapped
        )
      ) {
        val tiltAngles = FloatArray(3)
        SensorManager.getOrientation(tiltRemapped, tiltAngles)
        azimuthDeg = ((Math.toDegrees(tiltAngles[0].toDouble()).toFloat()) + 360f) % 360f
      }
    }
    return Triple(azimuthDeg, pitchDeg, rollDeg)
  }

  /**
   * Computes magnetic azimuth [0f, 360f), pitch, and roll from a [Sensor.TYPE_ROTATION_VECTOR] reading
   * using [SensorManager.getRotationMatrixFromVector] and [SensorManager.getOrientation].
   */
  fun computeOrientationFromRotationVector(
    rotationVectorReading: FloatArray,
    displayRotation: Int = Surface.ROTATION_0
  ): Triple<Float, Float, Float>? {
    if (rotationVectorReading.size < 3) return null
    val rotationMatrix = FloatArray(9)
    try {
      val trimmed = if (rotationVectorReading.size > 4) {
        rotationVectorReading.copyOfRange(0, 4)
      } else {
        rotationVectorReading
      }
      SensorManager.getRotationMatrixFromVector(rotationMatrix, trimmed)
    } catch (_: Exception) {
      return null
    }

    val remappedMatrix = FloatArray(9)
    when (displayRotation) {
      Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
        rotationMatrix,
        SensorManager.AXIS_Y,
        SensorManager.AXIS_MINUS_X,
        remappedMatrix
      )
      Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
        rotationMatrix,
        SensorManager.AXIS_MINUS_X,
        SensorManager.AXIS_MINUS_Y,
        remappedMatrix
      )
      Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
        rotationMatrix,
        SensorManager.AXIS_MINUS_Y,
        SensorManager.AXIS_X,
        remappedMatrix
      )
      else -> System.arraycopy(rotationMatrix, 0, remappedMatrix, 0, 9)
    }

    val orientationAngles = FloatArray(3)
    SensorManager.getOrientation(remappedMatrix, orientationAngles)
    var azimuthDeg = ((Math.toDegrees(orientationAngles[0].toDouble()).toFloat()) + 360f) % 360f
    val pitchDeg = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
    val rollDeg = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

    // Tilt compensation when device is held steep in hand (>50° pitch) to avoid gimbal lock
    if (abs(pitchDeg) > 50f) {
      val tiltRemapped = FloatArray(9)
      if (SensorManager.remapCoordinateSystem(
          remappedMatrix,
          SensorManager.AXIS_X,
          SensorManager.AXIS_Z,
          tiltRemapped
        )
      ) {
        val tiltAngles = FloatArray(3)
        SensorManager.getOrientation(tiltRemapped, tiltAngles)
        azimuthDeg = ((Math.toDegrees(tiltAngles[0].toDouble()).toFloat()) + 360f) % 360f
      }
    }
    return Triple(azimuthDeg, pitchDeg, rollDeg)
  }

  fun accuracyToLabel(accuracy: Int): String {
    return when (accuracy) {
      SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High Accuracy (Calibrated)"
      SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium Accuracy"
      SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low Accuracy — Wave in Figure-8"
      SensorManager.SENSOR_STATUS_UNRELIABLE -> "Calibration Needed — Move Away from Metal"
      else -> "Calibrating Compass..."
    }
  }
}

class QiblaSensorController(
  private val context: Context,
  initialLatitude: Double = 34.0837,
  initialLongitude: Double = 74.7973,
  initialLocationLabel: String = "Srinagar, Kashmir"
) : SensorEventListener {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val rotationVectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
  private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  private val magnetometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

  private var filteredGravity: FloatArray? = null
  private var filteredGeomagnetic: FloatArray? = null
  private var hasGravityReading = false
  private var hasMagnetometerReading = false
  private var hasRotationVectorReading = false

  private val _state = MutableStateFlow(
    buildInitialState(
      lat = initialLatitude,
      lon = initialLongitude,
      label = initialLocationLabel,
      hasRotVec = rotationVectorSensor != null,
      hasAcc = accelerometer != null,
      hasMag = magnetometer != null
    )
  )
  val state: StateFlow<QiblaSensorState> = _state.asStateFlow()

  private fun buildInitialState(
    lat: Double,
    lon: Double,
    label: String,
    hasRotVec: Boolean,
    hasAcc: Boolean,
    hasMag: Boolean
  ): QiblaSensorState {
    val bearing = QiblaCompassCalculator.calculateQiblaBearing(lat, lon)
    val distKm = QiblaCompassCalculator.calculateDistanceToKaabaKm(lat, lon)
    val declination = QiblaCompassCalculator.computeMagneticDeclination(lat, lon)
    val cardinal = QiblaCompassCompassCardinal(bearing)
    val signedDelta = QiblaCompassCalculator.computeShortestSignedAngleDelta(0f, bearing.toFloat())
    val modeLabel = when {
      hasRotVec -> "TYPE_ROTATION_VECTOR (Primary)"
      hasAcc && hasMag -> "Accelerometer + Magnetometer (Fallback)"
      else -> "Manual Compass Mode (No Hardware Sensor)"
    }
    return QiblaSensorState(
      userLatitude = lat,
      userLongitude = lon,
      locationLabel = label,
      hasRotationVector = hasRotVec,
      hasAccelerometer = hasAcc,
      hasMagnetometer = hasMag,
      activeSensorModeLabel = modeLabel,
      magneticDeclinationDeg = declination,
      qiblaBearingFromTrueNorthDeg = bearing,
      qiblaCardinalDirection = cardinal,
      distanceToKaabaKm = distKm,
      relativeQiblaRotationDeg = bearing.toFloat(),
      signedAlignmentDeltaDeg = signedDelta,
      isAlignedWithQibla = abs(signedDelta) <= QiblaCompassCalculator.ALIGNMENT_TOLERANCE_DEG
    )
  }

  private fun QiblaCompassCompassCardinal(bearing: Double): String =
    QiblaCompassCalculator.formatCardinalDirection(bearing)

  fun updateLocation(latitude: Double, longitude: Double, locationLabel: String) {
    val bearing = QiblaCompassCalculator.calculateQiblaBearing(latitude, longitude)
    val distKm = QiblaCompassCalculator.calculateDistanceToKaabaKm(latitude, longitude)
    val declination = QiblaCompassCalculator.computeMagneticDeclination(latitude, longitude)
    val cardinal = QiblaCompassCalculator.formatCardinalDirection(bearing)

    val current = _state.value
    val activeTrueAzimuth = current.manualHeadingOverrideDeg
      ?: ((current.magneticAzimuthDeg + declination + 360f) % 360f)
    val relRotation = ((bearing.toFloat() - activeTrueAzimuth) + 360f) % 360f
    val signedDelta = QiblaCompassCalculator.computeShortestSignedAngleDelta(
      activeTrueAzimuth,
      bearing.toFloat()
    )

    _state.value = current.copy(
      userLatitude = latitude,
      userLongitude = longitude,
      locationLabel = locationLabel,
      trueAzimuthDeg = activeTrueAzimuth,
      magneticDeclinationDeg = declination,
      qiblaBearingFromTrueNorthDeg = bearing,
      qiblaCardinalDirection = cardinal,
      distanceToKaabaKm = distKm,
      relativeQiblaRotationDeg = relRotation,
      signedAlignmentDeltaDeg = signedDelta,
      isAlignedWithQibla = abs(signedDelta) <= QiblaCompassCalculator.ALIGNMENT_TOLERANCE_DEG
    )
  }

  fun startListening() {
    if (sensorManager == null || (rotationVectorSensor == null && accelerometer == null && magnetometer == null)) {
      _state.value = _state.value.copy(isListening = false)
      return
    }
    // Primary: Sensor.TYPE_ROTATION_VECTOR with responsive SENSOR_DELAY_GAME rate
    rotationVectorSensor?.let {
      sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
    }
    // Fallback + Geomagnetic anchor & field strength telemetry: Accelerometer + Magnetometer
    accelerometer?.let {
      sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
    }
    magnetometer?.let {
      sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
    }
    _state.value = _state.value.copy(isListening = true)
  }

  fun stopListening() {
    sensorManager?.unregisterListener(this)
    _state.value = _state.value.copy(isListening = false)
  }

  fun setManualHeadingOverride(headingDeg: Float?) {
    val current = _state.value
    val normalizedOverride = headingDeg?.let { ((it % 360f) + 360f) % 360f }
    val effectiveTrueHeading = normalizedOverride ?: current.trueAzimuthDeg
    val bearing = current.qiblaBearingFromTrueNorthDeg.toFloat()
    val relRotation = ((bearing - effectiveTrueHeading) + 360f) % 360f
    val signedDelta = QiblaCompassCalculator.computeShortestSignedAngleDelta(
      effectiveTrueHeading,
      bearing
    )

    _state.value = current.copy(
      manualHeadingOverrideDeg = normalizedOverride,
      trueAzimuthDeg = effectiveTrueHeading,
      relativeQiblaRotationDeg = relRotation,
      signedAlignmentDeltaDeg = signedDelta,
      isAlignedWithQibla = abs(signedDelta) <= QiblaCompassCalculator.ALIGNMENT_TOLERANCE_DEG &&
        (normalizedOverride != null || current.isReadingReliable)
    )
  }

  /**
   * Processes a [Sensor.TYPE_ROTATION_VECTOR] reading as the primary orientation sensor.
   */
  fun processRotationVector(
    rotationVectorValues: FloatArray,
    displayRotation: Int = getDisplayRotation(),
    smooth: Boolean = true
  ) {
    val orientation = QiblaCompassCalculator.computeOrientationFromRotationVector(
      rotationVectorReading = rotationVectorValues,
      displayRotation = displayRotation
    ) ?: return

    val rawMagAzimuth = orientation.first
    val pitchDeg = orientation.second
    val rollDeg = orientation.third

    val current = _state.value
    val smoothedMagAzimuth = if (smooth && current.hasLiveFix) {
      QiblaCompassCalculator.smoothCircularAzimuth(current.magneticAzimuthDeg, rawMagAzimuth, alpha = 0.22f)
    } else {
      rawMagAzimuth
    }

    val trueAzimuth = current.manualHeadingOverrideDeg
      ?: ((smoothedMagAzimuth + current.magneticDeclinationDeg + 360f) % 360f)
    val qiblaBearing = current.qiblaBearingFromTrueNorthDeg.toFloat()
    val relRotation = ((qiblaBearing - trueAzimuth) + 360f) % 360f
    val signedDelta = QiblaCompassCalculator.computeShortestSignedAngleDelta(trueAzimuth, qiblaBearing)
    val isFlat = abs(pitchDeg) <= 25f && abs(rollDeg) <= 25f
    val reliable = !current.isMagneticInterferenceDetected &&
      current.sensorAccuracy != SensorManager.SENSOR_STATUS_UNRELIABLE

    _state.value = current.copy(
      hasLiveFix = true,
      activeSensorModeLabel = "TYPE_ROTATION_VECTOR (Primary)",
      magneticAzimuthDeg = smoothedMagAzimuth,
      trueAzimuthDeg = trueAzimuth,
      relativeQiblaRotationDeg = relRotation,
      signedAlignmentDeltaDeg = signedDelta,
      isAlignedWithQibla = abs(signedDelta) <= QiblaCompassCalculator.ALIGNMENT_TOLERANCE_DEG && reliable,
      pitchDeg = pitchDeg,
      rollDeg = rollDeg,
      isDeviceHeldFlat = isFlat
    )
  }

  /**
   * Allows deterministic unit/Robolectric tests or fallback sensor callbacks to process a paired
   * accelerometer + magnetometer vector through the full rotation matrix & declination pipeline.
   */
  fun processSensorVectors(
    accelerometerValues: FloatArray,
    magnetometerValues: FloatArray,
    displayRotation: Int = getDisplayRotation(),
    smooth: Boolean = true
  ) {
    filteredGravity = if (smooth) {
      QiblaCompassCalculator.lowPassFilter(accelerometerValues, filteredGravity, alpha = 0.20f)
    } else {
      accelerometerValues.copyOf()
    }
    filteredGeomagnetic = if (smooth) {
      QiblaCompassCalculator.lowPassFilter(magnetometerValues, filteredGeomagnetic, alpha = 0.20f)
    } else {
      magnetometerValues.copyOf()
    }

    val acc = filteredGravity ?: return
    val mag = filteredGeomagnetic ?: return

    val bx = mag[0]
    val by = mag[1]
    val bz = mag[2]
    val fieldStrengthUt = sqrt(bx * bx + by * by + bz * bz)
    val interference = fieldStrengthUt < 20f || fieldStrengthUt > 75f

    val orientation = QiblaCompassCalculator.computeOrientationFromSensors(
      accelerometerReading = acc,
      magnetometerReading = mag,
      displayRotation = displayRotation
    ) ?: return

    val rawMagAzimuth = orientation.first
    val pitchDeg = orientation.second
    val rollDeg = orientation.third

    val current = _state.value
    val smoothedMagAzimuth = if (smooth && current.hasLiveFix) {
      QiblaCompassCalculator.smoothCircularAzimuth(current.magneticAzimuthDeg, rawMagAzimuth, alpha = 0.22f)
    } else {
      rawMagAzimuth
    }

    val trueAzimuth = current.manualHeadingOverrideDeg
      ?: ((smoothedMagAzimuth + current.magneticDeclinationDeg + 360f) % 360f)
    val qiblaBearing = current.qiblaBearingFromTrueNorthDeg.toFloat()
    val relRotation = ((qiblaBearing - trueAzimuth) + 360f) % 360f
    val signedDelta = QiblaCompassCalculator.computeShortestSignedAngleDelta(trueAzimuth, qiblaBearing)
    val isFlat = abs(pitchDeg) <= 25f && abs(rollDeg) <= 25f
    val reliable = !interference && current.sensorAccuracy != SensorManager.SENSOR_STATUS_UNRELIABLE

    _state.value = current.copy(
      hasLiveFix = true,
      magneticAzimuthDeg = smoothedMagAzimuth,
      trueAzimuthDeg = trueAzimuth,
      relativeQiblaRotationDeg = relRotation,
      signedAlignmentDeltaDeg = signedDelta,
      isAlignedWithQibla = abs(signedDelta) <= QiblaCompassCalculator.ALIGNMENT_TOLERANCE_DEG && reliable,
      pitchDeg = pitchDeg,
      rollDeg = rollDeg,
      isDeviceHeldFlat = isFlat,
      magneticFieldStrengthUt = (fieldStrengthUt * 10f).roundToInt() / 10f,
      isMagneticInterferenceDetected = interference
    )
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null) return
    val displayRot = getDisplayRotation()
    when (event.sensor.type) {
      Sensor.TYPE_ROTATION_VECTOR -> {
        hasRotationVectorReading = true
        // Cross-check against geomagnetic heading when both accelerometer and magnetometer are healthy
        // to eliminate gyroscope yaw drift on Xiaomi/HyperOS & Samsung devices.
        val acc = filteredGravity
        val mag = filteredGeomagnetic
        val current = _state.value
        if (hasGravityReading && hasMagnetometerReading && acc != null && mag != null && !current.isMagneticInterferenceDetected) {
          val rotOrientation = QiblaCompassCalculator.computeOrientationFromRotationVector(event.values, displayRot)
          val magOrientation = QiblaCompassCalculator.computeOrientationFromSensors(acc, mag, displayRot)
          if (rotOrientation != null && magOrientation != null) {
            val driftDeg = abs(QiblaCompassCalculator.computeShortestSignedAngleDelta(rotOrientation.first, magOrientation.first))
            if (driftDeg > 18f) {
              // Anchor to geomagnetic sensor fusion when rotation vector has drifted
              processSensorVectors(acc, mag, displayRot, smooth = true)
              return
            }
          }
        }
        processRotationVector(event.values, displayRot, smooth = true)
      }
      Sensor.TYPE_ACCELEROMETER -> {
        filteredGravity = QiblaCompassCalculator.lowPassFilter(event.values, filteredGravity, alpha = 0.22f)
        hasGravityReading = true
      }
      Sensor.TYPE_MAGNETIC_FIELD -> {
        filteredGeomagnetic = QiblaCompassCalculator.lowPassFilter(event.values, filteredGeomagnetic, alpha = 0.22f)
        hasMagnetometerReading = true
        // Update magnetic field strength even when rotation vector is primary
        val mag = filteredGeomagnetic
        if (mag != null && mag.size >= 3) {
          val bx = mag[0]
          val by = mag[1]
          val bz = mag[2]
          val fieldStrengthUt = sqrt(bx * bx + by * by + bz * bz)
          val interference = fieldStrengthUt < 20f || fieldStrengthUt > 75f
          _state.value = _state.value.copy(
            magneticFieldStrengthUt = (fieldStrengthUt * 10f).roundToInt() / 10f,
            isMagneticInterferenceDetected = interference
          )
        }
      }
    }

    // Fallback to Accelerometer + Magnetometer when TYPE_ROTATION_VECTOR is unavailable or has not fired
    if (!hasRotationVectorReading && hasGravityReading && hasMagnetometerReading) {
      val acc = filteredGravity ?: return
      val mag = filteredGeomagnetic ?: return
      processSensorVectors(acc, mag, displayRot, smooth = true)
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    if (sensor?.type == Sensor.TYPE_ROTATION_VECTOR || sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
      _state.value = _state.value.copy(
        sensorAccuracy = accuracy,
        accuracyLabel = QiblaCompassCalculator.accuracyToLabel(accuracy)
      )
    }
  }

  @Suppress("DEPRECATION")
  private fun getDisplayRotation(): Int {
    return try {
      val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
      wm?.defaultDisplay?.rotation ?: Surface.ROTATION_0
    } catch (_: Exception) {
      Surface.ROTATION_0
    }
  }
}
