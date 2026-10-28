package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.prayer.PrayerAndAlarmConfig
import com.example.data.prayer.PrayerTimeCalculator
import com.example.data.prayer.QiblaCompassCalculator
import com.example.data.prayer.QiblaSensorController
import com.example.data.prayer.QiblaSensorState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QiblaCompassScreen(
  prayerConfig: PrayerAndAlarmConfig,
  onUpdatePrayerConfig: (PrayerAndAlarmConfig) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  val sensorController = remember(context) {
    QiblaSensorController(
      context = context,
      initialLatitude = prayerConfig.latitude,
      initialLongitude = prayerConfig.longitude,
      initialLocationLabel = "${prayerConfig.city}, ${prayerConfig.districtOrRegion.ifBlank { prayerConfig.country }}"
    )
  }

  val sensorState by sensorController.state.collectAsStateWithLifecycle()
  var showCityPresetSheet by remember { mutableStateOf(false) }
  var showCalibrationGuide by remember { mutableStateOf(false) }
  var manualModeEnabled by remember { mutableStateOf(false) }
  var manualHeadingSliderValue by remember { mutableFloatStateOf(0f) }

  // Keep sensor controller synced with user's configured prayer coordinates
  LaunchedEffect(prayerConfig.latitude, prayerConfig.longitude, prayerConfig.city, prayerConfig.districtOrRegion) {
    sensorController.updateLocation(
      latitude = prayerConfig.latitude,
      longitude = prayerConfig.longitude,
      locationLabel = "${prayerConfig.city}, ${prayerConfig.districtOrRegion.ifBlank { prayerConfig.country }}"
    )
  }

  // Register magnetometer + accelerometer listeners while screen is active
  DisposableEffect(sensorController) {
    sensorController.startListening()
    onDispose {
      sensorController.stopListening()
    }
  }

  // Haptic confirmation pulse when user aligns with the Qibla within tolerance
  var wasAlignedPreviously by remember { mutableStateOf(false) }
  LaunchedEffect(sensorState.isAlignedWithQibla) {
    if (sensorState.isAlignedWithQibla && !wasAlignedPreviously) {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    wasAlignedPreviously = sensorState.isAlignedWithQibla
  }

  // Continuous unwrapped angle so compass rose never spins 359° backward when crossing North
  var continuousDialRotation by remember { mutableFloatStateOf(-sensorState.trueAzimuthDeg) }
  LaunchedEffect(sensorState.trueAzimuthDeg) {
    val targetRotation = -sensorState.trueAzimuthDeg
    var delta = (targetRotation - continuousDialRotation) % 360f
    if (delta > 180f) delta -= 360f
    if (delta < -180f) delta += 360f
    continuousDialRotation += delta
  }

  val animatedDialRotation by animateFloatAsState(
    targetValue = continuousDialRotation,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "qibla_dial_rotation"
  )

  // Automatically refresh GPS coordinates when Qibla screen opens if location permission is already granted
  LaunchedEffect(Unit) {
    val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    if (hasFine || hasCoarse) {
      detectBestDeviceLocation(context)?.let { (lat, lon) ->
        if (kotlin.math.abs(lat - prayerConfig.latitude) > 0.005 || kotlin.math.abs(lon - prayerConfig.longitude) > 0.005) {
          onUpdatePrayerConfig(
            prayerConfig.copy(
              city = "Live GPS Location",
              districtOrRegion = String.format(Locale.US, "%.4f° N, %.4f° E", lat, lon),
              latitude = lat,
              longitude = lon,
              useAutoLocation = true
            )
          )
        }
      }
      requestFreshDeviceLocation(context) { lat, lon ->
        onUpdatePrayerConfig(
          prayerConfig.copy(
            city = "Live GPS Location",
            districtOrRegion = String.format(Locale.US, "%.4f° N, %.4f° E", lat, lon),
            latitude = lat,
            longitude = lon,
            useAutoLocation = true
          )
        )
      }
    }
  }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      val cachedLoc = detectBestDeviceLocation(context)
      if (cachedLoc != null) {
        val (lat, lon) = cachedLoc
        onUpdatePrayerConfig(
          prayerConfig.copy(
            city = "Live GPS Location",
            districtOrRegion = String.format(Locale.US, "%.4f° N, %.4f° E", lat, lon),
            country = TimeZone.getDefault().id,
            latitude = lat,
            longitude = lon,
            timeZoneId = TimeZone.getDefault().id,
            useAutoLocation = true
          )
        )
        Toast.makeText(context, "Qibla bearing updated from GPS (${String.format(Locale.US, "%.4f, %.4f", lat, lon)})", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(
          context,
          "Acquiring live GPS fix... Using ${prayerConfig.city} until fix arrives.",
          Toast.LENGTH_SHORT
        ).show()
      }
      requestFreshDeviceLocation(context) { lat, lon ->
        onUpdatePrayerConfig(
          prayerConfig.copy(
            city = "Live GPS Location",
            districtOrRegion = String.format(Locale.US, "%.4f° N, %.4f° E", lat, lon),
            country = TimeZone.getDefault().id,
            latitude = lat,
            longitude = lon,
            timeZoneId = TimeZone.getDefault().id,
            useAutoLocation = true
          )
        )
      }
    } else {
      Toast.makeText(context, "Location permission declined. Using configured city.", Toast.LENGTH_SHORT).show()
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("qibla_compass_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
    contentPadding = PaddingValues(vertical = 12.dp)
  ) {
    // 1. Sacred Quranic Verse Header (Al-Baqarah 2:144)
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0E2920)
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "فَوَلِّ وَجْهَكَ شَطْرَ الْمَسْجِدِ الْحَرَامِ",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontFamily = ScheherazadeFontFamily,
              fontSize = 26.sp,
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              textAlign = TextAlign.Center
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "\"So turn your face toward al-Masjid al-Haram\" — Surah Al-Baqarah 2:144",
            style = MaterialTheme.typography.bodySmall.copy(
              fontStyle = FontStyle.Italic,
              color = Color(0xFFE6DEC8),
              textAlign = TextAlign.Center
            )
          )
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 2. Active Location & Quick GPS / Preset City Controls
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp)
          .testTag("qibla_location_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(EmeraldPrimary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = sensorState.locationLabel,
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                )
                Text(
                  text = String.format(
                    Locale.US,
                    "Lat %.4f°, Lon %.4f° • %.0f km to Makkah",
                    sensorState.userLatitude,
                    sensorState.userLongitude,
                    sensorState.distanceToKaabaKm
                  ),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilledTonalButton(
              onClick = {
                locationPermissionLauncher.launch(
                  arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                  )
                )
              },
              modifier = Modifier
                .weight(1f)
                .testTag("qibla_gps_location_btn"),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
              Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Use GPS Location", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { showCityPresetSheet = true },
              modifier = Modifier
                .weight(1f)
                .testTag("qibla_select_city_btn"),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
              Icon(Icons.Default.LocationCity, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Select City", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 3. Real-Time Alignment Banner
    item {
      val aligned = sensorState.isAlignedWithQibla
      val deltaDeg = sensorState.signedAlignmentDeltaDeg.roundToInt()
      val turnText = when {
        aligned -> "Aligned with Qibla (Facing Al-Kaaba)"
        deltaDeg > 0 -> "Turn ${abs(deltaDeg)}° Right (Clockwise) for Qibla"
        else -> "Turn ${abs(deltaDeg)}° Left (Counter-Clockwise) for Qibla"
      }

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp)
          .testTag("qibla_alignment_status_banner"),
        shape = RoundedCornerShape(16.dp),
        color = if (aligned) EmeraldPrimary else MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 4.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = if (aligned) Icons.Default.CheckCircle else Icons.Default.Explore,
              contentDescription = null,
              tint = if (aligned) GoldPrimary else EmeraldPrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = turnText,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (aligned) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                )
              )
              Text(
                text = String.format(
                  Locale.US,
                  "Qibla Bearing: %.1f° (%s) • Device Heading: %.0f°",
                  sensorState.qiblaBearingFromTrueNorthDeg,
                  sensorState.qiblaCardinalDirection,
                  sensorState.trueAzimuthDeg
                ),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (aligned) Color(0xFFE6DEC8) else MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (aligned) GoldPrimary else EmeraldPrimary.copy(alpha = 0.15f)
          ) {
            Text(
              text = String.format(Locale.US, "%.1f°", sensorState.qiblaBearingFromTrueNorthDeg),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (aligned) Color(0xFF0F261D) else EmeraldPrimary
              )
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 4. Interactive Magnetometer + Accelerometer Qibla Compass Dial
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 360.dp)
          .aspectRatio(1f)
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        QiblaCompassDialCanvas(
          dialRotationDeg = animatedDialRotation,
          qiblaBearingDeg = sensorState.qiblaBearingFromTrueNorthDeg.toFloat(),
          isAligned = sensorState.isAlignedWithQibla,
          pitchDeg = sensorState.pitchDeg,
          rollDeg = sensorState.rollDeg,
          modifier = Modifier
            .fillMaxSize()
            .testTag("qibla_compass_canvas")
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // 5. Key Telemetry Cards (Qibla Bearing, True Heading, Distance, Declination)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QiblaMetricTile(
          title = "Qibla Bearing",
          primaryValue = String.format(Locale.US, "%.1f° %s", sensorState.qiblaBearingFromTrueNorthDeg, sensorState.qiblaCardinalDirection),
          subtitle = "From True North",
          highlightGold = true,
          modifier = Modifier
            .weight(1f)
            .testTag("qibla_metric_bearing")
        )
        QiblaMetricTile(
          title = "Device Heading",
          primaryValue = String.format(
            Locale.US,
            "%.0f° %s",
            sensorState.trueAzimuthDeg,
            QiblaCompassCalculator.formatCardinalDirection(sensorState.trueAzimuthDeg.toDouble())
          ),
          subtitle = String.format(Locale.US, "Mag Declination %+.1f°", sensorState.magneticDeclinationDeg),
          highlightGold = false,
          modifier = Modifier
            .weight(1f)
            .testTag("qibla_metric_heading")
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QiblaMetricTile(
          title = "Distance to Kaaba",
          primaryValue = String.format(Locale.US, "%,.0f km", sensorState.distanceToKaabaKm),
          subtitle = "Makkah al-Mukarramah",
          highlightGold = false,
          modifier = Modifier
            .weight(1f)
            .testTag("qibla_metric_distance")
        )
        QiblaMetricTile(
          title = "Magnetic Field",
          primaryValue = String.format(Locale.US, "%.1f µT", sensorState.magneticFieldStrengthUt),
          subtitle = if (sensorState.isDeviceHeldFlat) "Phone Held Flat" else "Hold Phone Flat",
          highlightGold = false,
          modifier = Modifier
            .weight(1f)
            .testTag("qibla_metric_field")
        )
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 6. Sensor Status, Calibration & Manual Heading Simulator Card
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp)
          .testTag("qibla_calibration_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (sensorState.isMagneticInterferenceDetected || !sensorState.isSensorAvailable) {
                  Icons.Default.WarningAmber
                } else {
                  Icons.Default.Tune
                },
                contentDescription = null,
                tint = if (sensorState.isMagneticInterferenceDetected) Color(0xFFD97706) else EmeraldPrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = if (sensorState.isSensorAvailable) {
                    "Magnetometer & Accelerometer: ${sensorState.accuracyLabel}"
                  } else {
                    "Hardware Compass Sensor Not Detected — Manual Dial Active"
                  },
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                )
                Text(
                  text = if (sensorState.isMagneticInterferenceDetected) {
                    "Magnetic interference detected (${sensorState.magneticFieldStrengthUt} µT). Move away from metal/magnets and wave phone in a figure-8."
                  } else {
                    "Hold your device flat (parallel to the ground) away from magnetic cases for peak precision."
                  },
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(10.dp))

          // Interactive Manual Heading Override (useful on emulators, sensorless tablets, or manual bearing verification)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Interactive Heading Dial (Manual Test / Override)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Rotate compass manually to test Qibla alignment or orient using landmarks",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
            Switch(
              checked = manualModeEnabled || !sensorState.isSensorAvailable,
              onCheckedChange = { enabled ->
                manualModeEnabled = enabled
                if (enabled) {
                  manualHeadingSliderValue = sensorState.trueAzimuthDeg
                  sensorController.setManualHeadingOverride(manualHeadingSliderValue)
                } else {
                  sensorController.setManualHeadingOverride(null)
                }
              },
              modifier = Modifier.testTag("qibla_manual_override_toggle")
            )
          }

          AnimatedVisibility(visible = manualModeEnabled || !sensorState.isSensorAvailable) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = String.format(Locale.US, "Simulated Heading: %.0f°", manualHeadingSliderValue),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                FilledTonalButton(
                  onClick = {
                    val exactQibla = sensorState.qiblaBearingFromTrueNorthDeg.toFloat()
                    manualHeadingSliderValue = exactQibla
                    sensorController.setManualHeadingOverride(exactQibla)
                  },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.testTag("qibla_snap_to_kaaba_btn")
                ) {
                  Text("Align to Qibla (${sensorState.qiblaBearingFromTrueNorthDeg.roundToInt()}°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
              Slider(
                value = manualHeadingSliderValue,
                onValueChange = { newDeg ->
                  manualHeadingSliderValue = newDeg
                  sensorController.setManualHeadingOverride(newDeg)
                },
                valueRange = 0f..359f,
                colors = SliderDefaults.colors(
                  thumbColor = GoldPrimary,
                  activeTrackColor = EmeraldPrimary
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("qibla_manual_heading_slider")
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // City Preset Bottom Sheet
  if (showCityPresetSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showCityPresetSheet = false },
      sheetState = sheetState
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        Text(
          text = "Select Location for Qibla Bearing",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )
        )
        Text(
          text = "Calculates exact great-circle Qibla azimuth and magnetic declination for your city",
          style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          PrayerTimeCalculator.locationPresets.forEach { preset ->
            val selected = preset.city.equals(prayerConfig.city, ignoreCase = true)
            val presetBearing = QiblaCompassCalculator.calculateQiblaBearing(preset.latitude, preset.longitude)
            FilterChip(
              selected = selected,
              onClick = {
                onUpdatePrayerConfig(
                  prayerConfig.copy(
                    city = preset.city,
                    districtOrRegion = preset.districtOrRegion,
                    country = preset.country,
                    latitude = preset.latitude,
                    longitude = preset.longitude,
                    timeZoneId = preset.timeZoneId,
                    useAutoLocation = false
                  )
                )
                showCityPresetSheet = false
              },
              label = {
                Text(
                  text = "${preset.city} (${String.format(Locale.US, "%.1f°", presetBearing)})",
                  fontSize = 12.sp,
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("qibla_city_chip_${preset.city.lowercase(Locale.US)}")
            )
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun QiblaMetricTile(
  title: String,
  primaryValue: String,
  subtitle: String,
  highlightGold: Boolean,
  modifier: Modifier = Modifier
) {
  ElevatedCard(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.SemiBold
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = primaryValue,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = if (highlightGold) GoldPrimary else EmeraldPrimary
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp
        )
      )
    }
  }
}

@Composable
private fun QiblaCompassDialCanvas(
  dialRotationDeg: Float,
  qiblaBearingDeg: Float,
  isAligned: Boolean,
  pitchDeg: Float,
  rollDeg: Float,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val diameter = size.minDimension
    val radius = diameter / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    // 1. Outer Sanctuary Glow & Bezel Ring
    val outerGlowColor = if (isAligned) GoldPrimary.copy(alpha = 0.28f) else EmeraldPrimary.copy(alpha = 0.12f)
    drawCircle(
      color = outerGlowColor,
      radius = radius,
      center = center
    )

    // Deep Sanctuary Emerald Dial Face
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0xFF163D2F),
          Color(0xFF0B2219),
          Color(0xFF071711)
        ),
        center = center,
        radius = radius * 0.94f
      ),
      radius = radius * 0.94f,
      center = center
    )

    // Double Burnished Gold Bezel Border
    drawCircle(
      color = if (isAligned) GoldPrimary else GoldPrimary.copy(alpha = 0.65f),
      radius = radius * 0.94f,
      center = center,
      style = Stroke(width = diameter * 0.015f)
    )
    drawCircle(
      color = GoldPrimary.copy(alpha = 0.28f),
      radius = radius * 0.86f,
      center = center,
      style = Stroke(width = diameter * 0.004f)
    )

    // 2. Fixed Top Lubber Triangle (Device Forward Heading Indicator at 12 o'clock)
    val lubberTopY = center.y - radius * 0.98f
    val lubberPath = Path().apply {
      moveTo(center.x, lubberTopY + radius * 0.09f)
      lineTo(center.x - radius * 0.045f, lubberTopY)
      lineTo(center.x + radius * 0.045f, lubberTopY)
      close()
    }
    drawPath(
      path = lubberPath,
      color = if (isAligned) GoldPrimary else Color(0xFFE6DEC8)
    )

    // 3. Rotating Compass Rose (rotates by -trueAzimuthDeg)
    rotate(degrees = dialRotationDeg, pivot = center) {
      val tickOuterRadius = radius * 0.84f
      val degreeTextPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      for (deg in 0 until 360 step 5) {
        val angleRad = Math.toRadians((deg - 90).toDouble())
        val isCardinal = deg % 90 == 0
        val isMajor30 = deg % 30 == 0
        val tickLength = when {
          isCardinal -> radius * 0.10f
          isMajor30 -> radius * 0.07f
          else -> radius * 0.035f
        }
        val tickStart = Offset(
          x = center.x + (tickOuterRadius * cos(angleRad)).toFloat(),
          y = center.y + (tickOuterRadius * sin(angleRad)).toFloat()
        )
        val tickEnd = Offset(
          x = center.x + ((tickOuterRadius - tickLength) * cos(angleRad)).toFloat(),
          y = center.y + ((tickOuterRadius - tickLength) * sin(angleRad)).toFloat()
        )
        drawLine(
          color = when {
            deg == 0 -> Color(0xFFEF4444) // North crimson highlight
            isCardinal -> GoldPrimary
            isMajor30 -> Color(0xFFD8CCB2)
            else -> Color(0xFF6E8B7E).copy(alpha = 0.6f)
          },
          start = tickStart,
          end = tickEnd,
          strokeWidth = if (isCardinal) diameter * 0.01f else if (isMajor30) diameter * 0.006f else diameter * 0.003f,
          cap = StrokeCap.Round
        )

        // Draw Cardinal Labels (N, E, S, W) and Arabic equivalents
        if (isCardinal) {
          val labelRadius = radius * 0.64f
          val lx = center.x + (labelRadius * cos(angleRad)).toFloat()
          val ly = center.y + (labelRadius * sin(angleRad)).toFloat()
          val cardinalText = when (deg) {
            0 -> "N"
            90 -> "E"
            180 -> "S"
            else -> "W"
          }
          degreeTextPaint.color = if (deg == 0) 0xFFEF4444.toInt() else 0xFFD4AF37.toInt()
          degreeTextPaint.textSize = diameter * 0.062f
          drawContext.canvas.nativeCanvas.drawText(
            cardinalText,
            lx,
            ly + degreeTextPaint.textSize * 0.35f,
            degreeTextPaint
          )
        } else if (isMajor30) {
          val degRadius = radius * 0.68f
          val dx = center.x + (degRadius * cos(angleRad)).toFloat()
          val dy = center.y + (degRadius * sin(angleRad)).toFloat()
          degreeTextPaint.color = 0xFFB5C4BC.toInt()
          degreeTextPaint.textSize = diameter * 0.034f
          drawContext.canvas.nativeCanvas.drawText(
            deg.toString(),
            dx,
            dy + degreeTextPaint.textSize * 0.35f,
            degreeTextPaint
          )
        }
      }

      // 4. Subtle Islamic 8-Pointed Geometric Star (Rub el Hizb) inside the dial
      val innerStarRadius = radius * 0.38f
      for (rot in listOf(0f, 45f)) {
        rotate(degrees = rot, pivot = center) {
          drawRect(
            color = GoldPrimary.copy(alpha = 0.16f),
            topLeft = Offset(center.x - innerStarRadius * 0.7f, center.y - innerStarRadius * 0.7f),
            size = Size(innerStarRadius * 1.4f, innerStarRadius * 1.4f),
            style = Stroke(width = diameter * 0.004f)
          )
        }
      }

      // 5. Golden Qibla Pointer Needle & Sacred Kaaba Emblem (at qiblaBearingDeg relative to North)
      rotate(degrees = qiblaBearingDeg, pivot = center) {
        val pointerTipY = center.y - radius * 0.78f
        val kaabaCenterY = center.y - radius * 0.50f

        // Glowing Qibla ray from center to Kaaba bearing
        val qiblaNeedlePath = Path().apply {
          moveTo(center.x, pointerTipY)
          lineTo(center.x - radius * 0.055f, center.y - radius * 0.12f)
          lineTo(center.x, center.y - radius * 0.04f)
          lineTo(center.x + radius * 0.055f, center.y - radius * 0.12f)
          close()
        }
        drawPath(
          path = qiblaNeedlePath,
          brush = Brush.verticalGradient(
            colors = listOf(
              if (isAligned) Color(0xFFFFDF73) else GoldPrimary,
              Color(0xFFB68D2C)
            ),
            startY = pointerTipY,
            endY = center.y
          )
        )

        // Counter-weight tail of Qibla needle
        val tailPath = Path().apply {
          moveTo(center.x - radius * 0.03f, center.y + radius * 0.04f)
          lineTo(center.x, center.y + radius * 0.24f)
          lineTo(center.x + radius * 0.03f, center.y + radius * 0.04f)
          close()
        }
        drawPath(
          path = tailPath,
          color = GoldPrimary.copy(alpha = 0.45f)
        )

        // Kaaba Icon Medallion on the Qibla Ray
        val medallionRadius = radius * 0.105f
        drawCircle(
          color = Color(0xFF0A1F17),
          radius = medallionRadius,
          center = Offset(center.x, kaabaCenterY)
        )
        drawCircle(
          color = if (isAligned) Color(0xFFFFDF73) else GoldPrimary,
          radius = medallionRadius,
          center = Offset(center.x, kaabaCenterY),
          style = Stroke(width = diameter * 0.008f)
        )

        // Stylized Black & Gold Kaaba Cube with Kiswah Hizam band
        val cubeSize = medallionRadius * 1.12f
        val cubeTopLeft = Offset(center.x - cubeSize / 2f, kaabaCenterY - cubeSize / 2f)
        drawRoundRect(
          color = Color(0xFF141414),
          topLeft = cubeTopLeft,
          size = Size(cubeSize, cubeSize),
          cornerRadius = CornerRadius(cubeSize * 0.15f, cubeSize * 0.15f)
        )
        drawRoundRect(
          color = GoldPrimary,
          topLeft = cubeTopLeft,
          size = Size(cubeSize, cubeSize),
          cornerRadius = CornerRadius(cubeSize * 0.15f, cubeSize * 0.15f),
          style = Stroke(width = diameter * 0.004f)
        )
        // Golden Kiswah belt across top third of Kaaba cube
        drawLine(
          color = GoldPrimary,
          start = Offset(cubeTopLeft.x + cubeSize * 0.1f, cubeTopLeft.y + cubeSize * 0.32f),
          end = Offset(cubeTopLeft.x + cubeSize * 0.9f, cubeTopLeft.y + cubeSize * 0.32f),
          strokeWidth = diameter * 0.009f,
          cap = StrokeCap.Butt
        )
      }
    }

    // 6. Central Spirit-Level Bubble (shows device tilt from pitch/roll)
    val levelRingRadius = radius * 0.11f
    drawCircle(
      color = Color(0xFF091B14),
      radius = levelRingRadius,
      center = center
    )
    drawCircle(
      color = GoldPrimary.copy(alpha = 0.6f),
      radius = levelRingRadius,
      center = center,
      style = Stroke(width = diameter * 0.005f)
    )

    val clampedRoll = (rollDeg / 45f).coerceIn(-1f, 1f)
    val clampedPitch = (pitchDeg / 45f).coerceIn(-1f, 1f)
    val bubbleOffset = Offset(
      x = center.x + clampedRoll * (levelRingRadius * 0.55f),
      y = center.y + clampedPitch * (levelRingRadius * 0.55f)
    )
    drawCircle(
      color = if (isAligned) GoldPrimary else EmeraldPrimary,
      radius = radius * 0.035f,
      center = bubbleOffset
    )
  }
}

@SuppressLint("MissingPermission")
private fun detectBestDeviceLocation(context: Context): Pair<Double, Double>? {
  return try {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = listOf(
      LocationManager.GPS_PROVIDER,
      LocationManager.NETWORK_PROVIDER,
      LocationManager.PASSIVE_PROVIDER
    )
    var bestLocation: android.location.Location? = null
    for (provider in providers) {
      if (runCatching { lm.isProviderEnabled(provider) }.getOrDefault(false)) {
        val loc = runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
        if (loc != null) {
          if (bestLocation == null || loc.time > bestLocation.time || loc.accuracy < bestLocation.accuracy) {
            bestLocation = loc
          }
        }
      }
    }
    bestLocation?.let { it.latitude to it.longitude }
  } catch (_: Exception) {
    null
  }
}

@SuppressLint("MissingPermission")
private fun requestFreshDeviceLocation(
  context: Context,
  onLocationUpdated: (Double, Double) -> Unit
) {
  try {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
    val provider = when {
      runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ->
        LocationManager.GPS_PROVIDER
      runCatching { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false) ->
        LocationManager.NETWORK_PROVIDER
      else -> return
    }
    val listener = object : android.location.LocationListener {
      override fun onLocationChanged(location: android.location.Location) {
        onLocationUpdated(location.latitude, location.longitude)
        runCatching { lm.removeUpdates(this) }
      }
      @Deprecated("Deprecated in Java")
      override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
      override fun onProviderEnabled(provider: String) {}
      override fun onProviderDisabled(provider: String) {}
    }
    lm.requestLocationUpdates(provider, 0L, 0f, listener, android.os.Looper.getMainLooper())
  } catch (_: Exception) {
  }
}
