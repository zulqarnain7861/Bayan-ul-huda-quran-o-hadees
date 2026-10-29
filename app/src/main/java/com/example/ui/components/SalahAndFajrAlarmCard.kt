package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PrayerScheduleEntity
import com.example.data.prayer.AzanNotificationMode
import com.example.data.prayer.MuezzinAudioOption
import com.example.data.prayer.PrayerAndAlarmConfig
import com.example.data.prayer.PrayerTimeCalculator
import com.example.ui.prayer.AzanAudioController
import com.example.ui.prayer.FajrAlarmPlaybackState
import com.example.ui.prayer.PrayerAlarmScheduler
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SalahAndFajrAlarmCard(
  schedule: PrayerScheduleEntity?,
  config: PrayerAndAlarmConfig,
  alarmPlaybackState: FajrAlarmPlaybackState,
  onUpdateConfig: (PrayerAndAlarmConfig) -> Unit,
  onRefreshSchedule: () -> Unit,
  onTestAzanOrFajrAlarm: (String, Boolean) -> Unit,
  onStopActiveAlarm: () -> Unit,
  onSnoozeActiveAlarm: (Int) -> Unit,
  onOpenQiblaCompass: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
  var showSetupSheet by remember { mutableStateOf(false) }
  var showAuthenticAzansGallery by remember { mutableStateOf(false) }
  var confirmStopArmed by remember { mutableStateOf(false) }

  // Live 1-second ticker for prayer countdown
  LaunchedEffect(Unit) {
    while (true) {
      delay(1000L)
      nowMillis = System.currentTimeMillis()
    }
  }

  val effectiveSchedule = remember(schedule, config) {
    schedule ?: PrayerTimeCalculator.calculateOfflineSchedule(
      locationName = config.city,
      districtOrRegion = config.districtOrRegion,
      country = config.country,
      latitude = config.latitude,
      longitude = config.longitude,
      timeZoneId = config.timeZoneId,
      methodId = config.calculationMethodId,
      asrMadhhab = config.asrMadhhab,
      highLatitudeRule = config.highLatitudeRule
    )
  }

  val nextPrayerStatus = remember(effectiveSchedule, nowMillis) {
    PrayerTimeCalculator.computeNextPrayerAndCountdown(effectiveSchedule, nowMillis)
  }

  val fajrWakeUpTime24 = remember(effectiveSchedule.fajrTime, config.fajrWakeUpOffsetMinutes) {
    PrayerTimeCalculator.calculateWakeUpTimeBeforeFajr(
      effectiveSchedule.fajrTime,
      config.fajrWakeUpOffsetMinutes
    )
  }

  val diagnostics = remember(config, showSetupSheet) {
    PrayerAlarmScheduler.getSetupDiagnostics(context, effectiveSchedule)
  }

  // Runtime permission launchers for Location and Notifications
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      detectDeviceGpsLocation(context)?.let { (lat, lon) ->
        onUpdateConfig(
          config.copy(
            city = "GPS Location",
            districtOrRegion = "Auto Coordinates (${"%.4f".format(lat)}, ${"%.4f".format(lon)})",
            country = TimeZone.getDefault().id,
            latitude = lat,
            longitude = lon,
            timeZoneId = TimeZone.getDefault().id,
            useAutoLocation = true
          )
        )
        Toast.makeText(context, "GPS coordinates updated for prayer times", Toast.LENGTH_SHORT).show()
      } ?: run {
        Toast.makeText(context, "Location permission granted. Using configured city.", Toast.LENGTH_SHORT).show()
      }
    }
  }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    val msg = if (granted) "Azan & Fajr alarm notifications enabled" else "Notification permission declined"
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
  }

  Column(modifier = modifier.fillMaxWidth()) {
    // Active Low-Glare Dark Fajr Alarm / Azan Playback / Continuous Vibration Screen
    if (alarmPlaybackState.isActive || alarmPlaybackState.isContinuousVibrationActive) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 14.dp)
          .testTag("active_fajr_alarm_banner"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF101915) // Comfortable dark low-glare surface
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = alarmPlaybackState.prayerOrAlarmTitle.ifBlank { "Offline Pure Vocal Adhan Alarm" },
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFF3E9D2)
                )
              )
            }
            if (alarmPlaybackState.isActive) {
              val remMin = alarmPlaybackState.remainingSeconds / 60
              val remSec = alarmPlaybackState.remainingSeconds % 60
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldPrimary.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "%02d:%02d / 15:00".format(remMin, remSec),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = alarmPlaybackState.currentStageLabel,
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA8B4AC))
          )

          Spacer(modifier = Modifier.height(8.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (alarmPlaybackState.isActive) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldPrimary.copy(alpha = 0.28f)
              ) {
                Text(
                  text = "🔊 Volume: ${alarmPlaybackState.currentVolumePercent}% • Offline Local Adhan (No Music)",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF3E9D2)
                  )
                )
              }
            }
            if (alarmPlaybackState.isContinuousVibrationActive) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldPrimary.copy(alpha = 0.25f)
              ) {
                Text(
                  text = "📳 CONTINUOUS VIBRATION ACTIVE (Pulse #${alarmPlaybackState.vibrationPulseCount.coerceAtLeast(1)})",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                  )
                )
              }
            }
          }

          if (alarmPlaybackState.includeFajrTathwibPhrase) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = PrayerTimeCalculator.FAJR_TATHWIB_ARABIC,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = AmiriFontFamily,
                fontSize = 24.sp,
                textAlign = TextAlign.Right,
                color = GoldPrimary
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "${PrayerTimeCalculator.FAJR_TATHWIB_TRANSLITERATION} — ${PrayerTimeCalculator.FAJR_TATHWIB_ENGLISH}",
              style = MaterialTheme.typography.bodySmall.copy(
                fontStyle = FontStyle.Italic,
                color = Color(0xFFD8CCB2)
              )
            )
            if (config.wakeUpLanguage == "ALL" || config.wakeUpLanguage == "UR") {
              Text(
                text = alarmPlaybackState.urduWakeMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                  textAlign = TextAlign.Right,
                  color = Color(0xFFD8CCB2)
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Snooze (5m / 10m), Stop Vibration, & Safeguarded Stop Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (alarmPlaybackState.isActive) {
              OutlinedButton(
                onClick = {
                  confirmStopArmed = false
                  onSnoozeActiveAlarm(5)
                  Toast.makeText(context, "Snoozed for 5 minutes", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("fajr_alarm_snooze_5m_btn")
              ) {
                Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Snooze 5m", fontSize = 12.sp, color = Color(0xFFF3E9D2))
              }

              OutlinedButton(
                onClick = {
                  confirmStopArmed = false
                  onSnoozeActiveAlarm(10)
                  Toast.makeText(context, "Snoozed for 10 minutes", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("fajr_alarm_snooze_10m_btn")
              ) {
                Text("Snooze 10m", fontSize = 12.sp, color = Color(0xFFF3E9D2))
              }
            } else if (alarmPlaybackState.isContinuousVibrationActive) {
              OutlinedButton(
                onClick = {
                  AzanAudioController.stopContinuousVibration(context)
                  Toast.makeText(context, "Continuous vibration stopped", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("stop_standalone_vibration_btn")
              ) {
                Text("Stop Continuous Vibration", fontSize = 12.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
              }
            }

            Button(
              onClick = {
                if (alarmPlaybackState.isStandaloneVibrationTest && !alarmPlaybackState.isActive) {
                  AzanAudioController.stopContinuousVibration(context)
                } else if (!confirmStopArmed && alarmPlaybackState.isFajrSpecialAlarm) {
                  confirmStopArmed = true
                  Toast.makeText(context, "Tap 'Confirm Stop' again to dismiss Fajr alarm", Toast.LENGTH_SHORT).show()
                } else {
                  confirmStopArmed = false
                  onStopActiveAlarm()
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (confirmStopArmed) Color(0xFFB33A2B) else EmeraldPrimary
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1.2f)
                .testTag("fajr_alarm_stop_btn")
            ) {
              Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (confirmStopArmed) "Confirm Stop" else "Stop Alarm",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Main Five Daily Salah Times & 15-Minute Fajr Wake-Up Card
    ElevatedCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("salah_times_and_fajr_alarm_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        // Header: Location, Hijri & Gregorian Dates, and Setup button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(EmeraldPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AccessTime,
                  contentDescription = null,
                  tint = GoldPrimary,
                  modifier = Modifier.size(16.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Five Daily Salah & Azan",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${effectiveSchedule.locationName} (${effectiveSchedule.districtOrRegion.ifBlank { effectiveSchedule.country }})",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              )
            }
            Text(
              text = "${effectiveSchedule.hijriReadable} • ${effectiveSchedule.gregorianReadable}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
              )
            )
          }

          Row {
            IconButton(
              onClick = onOpenQiblaCompass,
              modifier = Modifier
                .size(36.dp)
                .testTag("salah_card_qibla_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "Open Qibla Direction Compass",
                tint = GoldPrimary
              )
            }
            IconButton(
              onClick = onRefreshSchedule,
              modifier = Modifier
                .size(36.dp)
                .testTag("refresh_prayer_schedule_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Prayer Times",
                tint = MaterialTheme.colorScheme.primary
              )
            }
            IconButton(
              onClick = { showSetupSheet = true },
              modifier = Modifier
                .size(36.dp)
                .testTag("open_salah_setup_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configure Salah, Azan & Fajr Alarm",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Next Prayer & Live Countdown Banner
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = EmeraldPrimary,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("next_prayer_countdown_banner")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Next Prayer: ${nextPrayerStatus.nextPrayerName}",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Text(
                text = "Adhan at ${PrayerTimeCalculator.format24hTo12h(nextPrayerStatus.nextPrayerTime24)} (${nextPrayerStatus.nextPrayerTime24})",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFD1EADF))
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "-${nextPrayerStatus.countdownFormatted}",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary
                )
              )
              Text(
                text = "Live Countdown",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFD1EADF))
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Five Daily Prayers List with Per-Prayer Azan Toggle
        val fivePrayersRows = listOf(
          Triple("Fajr", effectiveSchedule.fajrTime, config.fajrAzanMode),
          Triple("Dhuhr", effectiveSchedule.dhuhrTime, config.dhuhrAzanMode),
          Triple("Asr", effectiveSchedule.asrTime, config.asrAzanMode),
          Triple("Maghrib", effectiveSchedule.maghribTime, config.maghribAzanMode),
          Triple("Isha", effectiveSchedule.ishaTime, config.ishaAzanMode)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          fivePrayersRows.forEach { (prayerName, time24, mode) ->
            val isNext = nextPrayerStatus.nextPrayerName == prayerName
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isNext) {
                GoldPrimary.copy(alpha = 0.14f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("prayer_row_${prayerName.lowercase()}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = prayerName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                      color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.width(72.dp)
                  )
                  Text(
                    text = "${PrayerTimeCalculator.format24hTo12h(time24)} ($time24)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  )
                }

                // Clickable Azan Mode Pill (Cycles Full Azan -> Short Tone -> Silent -> Off)
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (mode != AzanNotificationMode.OFF) {
                    EmeraldPrimary.copy(alpha = 0.14f)
                  } else {
                    MaterialTheme.colorScheme.surface
                  },
                  modifier = Modifier
                    .clickable {
                      val nextMode = when (mode) {
                        AzanNotificationMode.FULL_AZAN -> AzanNotificationMode.SHORT_TONE
                        AzanNotificationMode.SHORT_TONE -> AzanNotificationMode.SILENT
                        AzanNotificationMode.SILENT -> AzanNotificationMode.OFF
                        AzanNotificationMode.OFF -> AzanNotificationMode.FULL_AZAN
                      }
                      val updated = when (prayerName) {
                        "Fajr" -> config.copy(fajrAzanMode = nextMode)
                        "Dhuhr" -> config.copy(dhuhrAzanMode = nextMode)
                        "Asr" -> config.copy(asrAzanMode = nextMode)
                        "Maghrib" -> config.copy(maghribAzanMode = nextMode)
                        else -> config.copy(ishaAzanMode = nextMode)
                      }
                      onUpdateConfig(updated)
                    }
                    .testTag("azan_toggle_${prayerName.lowercase()}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (mode == AzanNotificationMode.OFF) {
                        Icons.Default.NotificationsOff
                      } else {
                        Icons.Default.NotificationsActive
                      },
                      contentDescription = null,
                      tint = if (mode == AzanNotificationMode.OFF) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      } else {
                        EmeraldPrimary
                      },
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = mode.label,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (mode == AzanNotificationMode.OFF) {
                          MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                          EmeraldPrimary
                        }
                      )
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Special 15-Minute Fajr Wake-Up Alarm & Tahajjud Card Section
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("special_fajr_alarm_section")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Special Fajr Wake-Up Alarm (15-Min Cap)",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                )
                Text(
                  text = "Scheduled for ${PrayerTimeCalculator.format24hTo12h(fajrWakeUpTime24)} (${config.fajrWakeUpOffsetMinutes} min before Fajr)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Switch(
                checked = config.fajrWakeUpAlarmEnabled,
                onCheckedChange = { onUpdateConfig(config.copy(fajrWakeUpAlarmEnabled = it)) },
                modifier = Modifier.testTag("fajr_wakeup_alarm_switch")
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Authentic Fajr Phrase (الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ)
            Text(
              text = PrayerTimeCalculator.FAJR_TATHWIB_ARABIC,
              style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = AmiriFontFamily,
                fontSize = 21.sp,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "${PrayerTimeCalculator.FAJR_TATHWIB_TRANSLITERATION} — ${PrayerTimeCalculator.FAJR_TATHWIB_ENGLISH}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.secondary
              )
            )
            Text(
              text = PrayerTimeCalculator.FAJR_TATHWIB_URDU,
              style = MaterialTheme.typography.labelSmall.copy(
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Selectable Minutes Before Fajr (60, 45, 30, 20, 15, 10)
            Text(
              text = "Wake-Up Time Before Fajr:",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              listOf(60, 45, 30, 20, 15, 10).forEach { mins ->
                FilterChip(
                  selected = config.fajrWakeUpOffsetMinutes == mins,
                  onClick = { onUpdateConfig(config.copy(fajrWakeUpOffsetMinutes = mins)) },
                  label = { Text("${mins}m", fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("fajr_offset_chip_$mins")
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tahajjud Final-Third-of-Night Wake-Up Option with Explicit Approximation Label
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Tahajjud Final Third: ${PrayerTimeCalculator.format24hTo12h(effectiveSchedule.tahajjudLastThirdStart)} • Sunrise: ${PrayerTimeCalculator.format24hTo12h(effectiveSchedule.sunriseTime)}",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                  text = "Note: Tahajjud final third is an approximate calculated interval from Maghrib to Fajr rather than an independently guaranteed religious ruling.",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
              Switch(
                checked = config.tahajjudWakeUpEnabled,
                onCheckedChange = { onUpdateConfig(config.copy(tahajjudWakeUpEnabled = it)) },
                modifier = Modifier.testTag("tahajjud_wakeup_switch")
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Offline Adhan Favourite, Real-Time Hardware Alarm Volume Sync & Continuous Vibration Panel
            val activeMuezzin = remember(config.selectedMuezzinId) {
              MuezzinAudioOption.entries.find { it.id == config.selectedMuezzinId }
                ?: MuezzinAudioOption.MADINAH_NABAWI
            }
            val hwAlarmStatus = remember(config.alarmVolumePercent, nowMillis / 1000L) {
              AzanAudioController.getHardwareAlarmVolumeStatus(context)
            }
            var showAlarmVolumeSettingGuide by remember { mutableStateOf(false) }

            val openAlarmVolumeSettingsAndGuide: () -> Unit = {
              showAlarmVolumeSettingGuide = true
              try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                audioManager?.adjustStreamVolume(
                  android.media.AudioManager.STREAM_ALARM,
                  android.media.AudioManager.ADJUST_RAISE,
                  android.media.AudioManager.FLAG_SHOW_UI
                )
              } catch (_: Exception) {
              }
              try {
                context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
              } catch (_: Exception) {
                Toast.makeText(
                  context,
                  "Setting Guide: Open Android Settings → Sound & Vibration → Alarm Volume and slide to 100%.",
                  Toast.LENGTH_LONG
                ).show()
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = EmeraldPrimary.copy(alpha = 0.10f),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("azan_200_boost_and_vibration_panel")
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Adhan: ${activeMuezzin.title}",
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    )
                    Text(
                      text = "Offline Vocal • Real-Time Alarm Audio: ${hwAlarmStatus.currentPercent}% • Vib: ${if (config.vibrationEnabled && config.continuousVibration) "ON" else "OFF"}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real-Time Hardware Alarm Volume Sync + Increase Redirect to Setting Guide
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Audio Level (Equal to Alarm Volume): ${hwAlarmStatus.currentPercent}%",
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    )
                    Text(
                      text = "Live System Alarm Stream: ${hwAlarmStatus.currentSteps}/${hwAlarmStatus.maxSteps} steps • App Target: ${config.alarmVolumePercent}%",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Button(
                    onClick = openAlarmVolumeSettingsAndGuide,
                    colors = ButtonDefaults.buttonColors(
                      containerColor = GoldPrimary,
                      contentColor = Color(0xFF102820)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("redirect_system_alarm_volume_btn")
                  ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Increase Vol",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      softWrap = false
                    )
                  }
                }

                Slider(
                  value = hwAlarmStatus.currentPercent.coerceIn(0, 100).toFloat(),
                  onValueChange = { newVal ->
                    val targetPct = newVal.toInt().coerceIn(0, 100)
                    onUpdateConfig(
                      config.copy(
                        alarmVolumePercent = targetPct,
                        boostSystemAlarmStream = true
                      )
                    )
                    if (targetPct > hwAlarmStatus.currentPercent) {
                      showAlarmVolumeSettingGuide = true
                    }
                  },
                  valueRange = 0f..100f,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("adhan_volume_slider_0_100")
                )

                // Concise Setting Guide & Hardware Alarm Redirect Banner (shown when below max or when user increases volume)
                if (showAlarmVolumeSettingGuide || hwAlarmStatus.isBelowHardwareMax || hwAlarmStatus.isMuted) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GoldPrimary.copy(alpha = 0.16f),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(bottom = 8.dp)
                      .testTag("hardware_alarm_volume_warning_banner")
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = "Setting Guide to Increase Alarm Volume (${hwAlarmStatus.currentPercent}%):",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                        )
                        Text(
                          text = "1. Tap 'Sound Settings' → 2. Raise 'Alarm Volume' slider to 100%. Audio level syncs automatically in real time.",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        )
                      }
                      Spacer(modifier = Modifier.width(8.dp))
                      OutlinedButton(
                        onClick = openAlarmVolumeSettingsAndGuide,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("open_sound_settings_btn")
                      ) {
                        Text(
                          text = "Sound Settings",
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          maxLines = 1,
                          softWrap = false
                        )
                      }
                    }
                  }
                }

                // Quick Volume Presets (25%, 50%, 75%, 100% Default) + Continuous Vibration Chip
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  listOf(
                    25 to "25%",
                    50 to "50%",
                    75 to "75%",
                    100 to "100% (Default)"
                  ).forEach { (volPct, label) ->
                    FilterChip(
                      selected = config.alarmVolumePercent == volPct,
                      onClick = {
                        onUpdateConfig(
                          config.copy(
                            alarmVolumePercent = volPct,
                            boostSystemAlarmStream = true
                          )
                        )
                        if (volPct > hwAlarmStatus.currentPercent) {
                          openAlarmVolumeSettingsAndGuide()
                        } else {
                          Toast.makeText(context, "Saved Adhan Alarm Volume: $label", Toast.LENGTH_SHORT).show()
                        }
                      },
                      label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                      ),
                      modifier = Modifier.testTag("azan_boost_chip_$volPct")
                    )
                  }

                  FilterChip(
                    selected = config.vibrationEnabled && config.continuousVibration,
                    onClick = {
                      val next = !(config.vibrationEnabled && config.continuousVibration)
                      onUpdateConfig(
                        config.copy(
                          vibrationEnabled = next,
                          continuousVibration = next
                        )
                      )
                    },
                    label = {
                      Text(
                        text = if (config.vibrationEnabled && config.continuousVibration) "Continuous Vib: ON" else "Continuous Vib: OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = GoldPrimary,
                      selectedLabelColor = Color(0xFF101915)
                    ),
                    modifier = Modifier.testTag("toggle_continuous_vibration_chip")
                  )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Expandable 14 Authentic Offline Adhans Gallery (Preview, Test, and Keep Favourite)
                OutlinedButton(
                  onClick = { showAuthenticAzansGallery = !showAuthenticAzansGallery },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_authentic_azans_gallery_btn")
                ) {
                  Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (showAuthenticAzansGallery) {
                      "Hide ${MuezzinAudioOption.entries.size} Offline Authentic Adhans"
                    } else {
                      "Preview, Test & Keep ${MuezzinAudioOption.entries.size} Offline Adhans (Masjid an-Nabawi, Makkah & More)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                if (showAuthenticAzansGallery) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("authentic_azans_gallery_list")
                  ) {
                    MuezzinAudioOption.entries.forEach { muezzin ->
                      val isSelected = config.selectedMuezzinId == muezzin.id
                      val isPlayingThis = alarmPlaybackState.isActive && alarmPlaybackState.activeMuezzinId == muezzin.id
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) {
                          EmeraldPrimary.copy(alpha = 0.16f)
                        } else {
                          MaterialTheme.colorScheme.surface
                        },
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable {
                            onUpdateConfig(config.copy(selectedMuezzinId = muezzin.id))
                            Toast.makeText(context, "Saved '${muezzin.title}' as permanent Adhan choice", Toast.LENGTH_SHORT).show()
                          }
                          .testTag("muezzin_card_${muezzin.id}")
                      ) {
                        Column(
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Column(modifier = Modifier.weight(1f)) {
                              Text(
                                text = muezzin.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                  fontWeight = FontWeight.Bold,
                                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                              )
                              Text(
                                text = "${muezzin.originMasjid} • ${muezzin.maqamOrStyle}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                  fontSize = 10.sp,
                                  color = GoldPrimary,
                                  fontWeight = FontWeight.SemiBold
                                )
                              )
                              Text(
                                text = muezzin.subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                  fontSize = 10.sp,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                              )
                            }
                          }
                          Spacer(modifier = Modifier.height(6.dp))
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            OutlinedButton(
                              onClick = {
                                if (isPlayingThis && alarmPlaybackState.isPreviewMode) {
                                  onStopActiveAlarm()
                                } else {
                                  AzanAudioController.previewMuezzinAzan(context, muezzin)
                                }
                              },
                              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                              shape = RoundedCornerShape(8.dp),
                              modifier = Modifier
                                .weight(1f)
                                .testTag("preview_muezzin_btn_${muezzin.id}")
                            ) {
                              Text(
                                text = if (isPlayingThis && alarmPlaybackState.isPreviewMode) "Stop Preview" else "Preview",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                              )
                            }
                            FilledTonalButton(
                              onClick = {
                                if (isPlayingThis && !alarmPlaybackState.isPreviewMode) {
                                  onStopActiveAlarm()
                                } else {
                                  onUpdateConfig(config.copy(selectedMuezzinId = muezzin.id))
                                  AzanAudioController.testMuezzinAzan(context, muezzin)
                                }
                              },
                              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                              shape = RoundedCornerShape(8.dp),
                              modifier = Modifier
                                .weight(1f)
                                .testTag("test_muezzin_btn_${muezzin.id}")
                            ) {
                              Text(
                                text = if (isPlayingThis && !alarmPlaybackState.isPreviewMode) "Stop Test" else "Test Alarm",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                              )
                            }
                            Button(
                              onClick = {
                                onUpdateConfig(config.copy(selectedMuezzinId = muezzin.id))
                                Toast.makeText(context, "Saved '${muezzin.title}' as permanent Adhan choice", Toast.LENGTH_SHORT).show()
                              },
                              colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) EmeraldPrimary else GoldPrimary,
                                contentColor = if (isSelected) Color.White else Color(0xFF101915)
                              ),
                              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                              shape = RoundedCornerShape(8.dp),
                              modifier = Modifier
                                .weight(1f)
                                .testTag("keep_muezzin_btn_${muezzin.id}")
                            ) {
                              Text(
                                text = if (isSelected) "Saved ★" else "Keep ★",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Test Offline Fajr Adhan, Test Continuous Vibration & Open Setup/Permissions
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilledTonalButton(
                onClick = { onTestAzanOrFajrAlarm("Fajr", true) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("test_fajr_wakeup_alarm_btn")
              ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Offline Adhan (${config.alarmVolumePercent}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }

              FilledTonalButton(
                onClick = {
                  if (alarmPlaybackState.isContinuousVibrationActive) {
                    AzanAudioController.stopContinuousVibration(context)
                    Toast.makeText(context, "Continuous vibration stopped", Toast.LENGTH_SHORT).show()
                  } else {
                    AzanAudioController.startContinuousVibrationTest(context)
                    Toast.makeText(context, "Continuous alarm vibration started — tap again to stop", Toast.LENGTH_SHORT).show()
                  }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("test_continuous_vibration_btn")
              ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (alarmPlaybackState.isContinuousVibrationActive) "Stop Vibration" else "Test Continuous Vibration",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              OutlinedButton(
                onClick = { showSetupSheet = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("configure_azan_location_btn")
              ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Location, 14 Azans & Setup", fontSize = 12.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cache & Sync Timestamp Footer
        val updatedFmt = remember(effectiveSchedule.lastUpdatedEpochMillis) {
          SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH).format(Date(effectiveSchedule.lastUpdatedEpochMillis))
        }
        Text(
          text = "Source: ${effectiveSchedule.sourceName} • Last updated: $updatedFmt",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }
    }
  }

  // Full Salah, Azan, Fajr Alarm, Location & Permissions Setup Bottom Sheet
  if (showSetupSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customCityInput by remember(config.city) { mutableStateOf(config.city) }
    var customDistrictInput by remember(config.districtOrRegion) { mutableStateOf(config.districtOrRegion) }
    var customLatInput by remember(config.latitude) { mutableStateOf(config.latitude.toString()) }
    var customLonInput by remember(config.longitude) { mutableStateOf(config.longitude.toString()) }
    var customOffsetInput by remember(config.fajrWakeUpOffsetMinutes) {
      mutableStateOf(config.fajrWakeUpOffsetMinutes.toString())
    }

    ModalBottomSheet(
      onDismissRequest = { showSetupSheet = false },
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Salah, Azan & Fajr Alarm Setup",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          IconButton(onClick = { showSetupSheet = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Permissions & System Readiness Check Card
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Permissions & Background Alarm Readiness",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
            Spacer(modifier = Modifier.height(8.dp))

            PermissionStatusRow(
              label = "Location (GPS / City Selection)",
              isReady = diagnostics.hasLocationPermission || config.city.isNotBlank(),
              statusText = if (diagnostics.hasLocationPermission) "GPS Granted" else "Manual City Active (${config.city})",
              actionLabel = "Use GPS",
              onAction = {
                locationPermissionLauncher.launch(
                  arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                  )
                )
              }
            )

            PermissionStatusRow(
              label = "Notifications (Azan & Alarm Alerts)",
              isReady = diagnostics.hasNotificationPermission,
              statusText = if (diagnostics.hasNotificationPermission) "Granted" else "Permission Needed",
              actionLabel = if (!diagnostics.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) "Grant" else null,
              onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                  notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
              }
            )

            PermissionStatusRow(
              label = "Exact Alarm Clock (AlarmManager)",
              isReady = diagnostics.canScheduleExactAlarms,
              statusText = if (diagnostics.canScheduleExactAlarms) "Exact Alarms Active" else "Fallback Mode (Grant Exact Alarm)",
              actionLabel = if (!diagnostics.canScheduleExactAlarms && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Enable" else null,
              onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                  try {
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                  } catch (_: Exception) {
                  }
                }
              }
            )

            PermissionStatusRow(
              label = "Device Alarm Volume (${diagnostics.alarmStreamVolumePercent}%)",
              isReady = !diagnostics.isAlarmStreamMuted && diagnostics.alarmStreamVolumePercent >= 100,
              statusText = when {
                diagnostics.isAlarmStreamMuted -> "Warning: Device Alarm Stream Muted! Raise Alarm slider in Android Sound Settings."
                diagnostics.alarmStreamVolumePercent < 100 -> "Device Alarm Stream is at ${diagnostics.alarmStreamVolumePercent}%. Raise to 100% in Android Sound Settings for full hardware volume."
                else -> "Ready (100% Maximum Permitted Hardware Alarm Volume)"
              },
              actionLabel = if (diagnostics.alarmStreamVolumePercent < 100) "Raise" else null,
              onAction = {
                try {
                  context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
                } catch (_: Exception) {
                }
              }
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Transparency Note: Android apps use the system Alarm audio stream and foreground service. Software volume operates from 0% to 100% of the device's hardware alarm stream and cannot exceed hardware limits or bypass hardware mute switches.",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. City / District Presets (Kashmir, India & Global) + Custom Coordinates
        Text(
          text = "Select Location (Kashmir, India & Worldwide):",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PrayerTimeCalculator.locationPresets.forEach { preset ->
            FilterChip(
              selected = config.city.equals(preset.city, ignoreCase = true),
              onClick = {
                customCityInput = preset.city
                customDistrictInput = preset.districtOrRegion
                customLatInput = preset.latitude.toString()
                customLonInput = preset.longitude.toString()
                onUpdateConfig(
                  config.copy(
                    city = preset.city,
                    districtOrRegion = preset.districtOrRegion,
                    country = preset.country,
                    latitude = preset.latitude,
                    longitude = preset.longitude,
                    timeZoneId = preset.timeZoneId,
                    calculationMethodId = preset.defaultMethodId,
                    asrMadhhab = preset.defaultAsrMadhhab,
                    useAutoLocation = false
                  )
                )
              },
              label = { Text("${preset.city} (${preset.country})", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = customCityInput,
            onValueChange = { customCityInput = it },
            label = { Text("City / Town") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = customDistrictInput,
            onValueChange = { customDistrictInput = it },
            label = { Text("District / Region") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customLatInput,
            onValueChange = { customLatInput = it },
            label = { Text("Latitude") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = customLonInput,
            onValueChange = { customLonInput = it },
            label = { Text("Longitude") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          Button(
            onClick = {
              val lat = customLatInput.toDoubleOrNull()
              val lon = customLonInput.toDoubleOrNull()
              if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
                onUpdateConfig(
                  config.copy(
                    city = customCityInput.ifBlank { "Custom Location" },
                    districtOrRegion = customDistrictInput,
                    latitude = lat,
                    longitude = lon
                  )
                )
                Toast.makeText(context, "Custom coordinates saved", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "Enter valid Latitude (-90..90) and Longitude (-180..180)", Toast.LENGTH_SHORT).show()
              }
            }
          ) {
            Text("Apply")
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // 3. Calculation Method & Asr Juristic Option (Compact Chips)
        Text(
          text = "Calculation Method & Asr Timing:",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = config.asrMadhhab == 1,
            onClick = { onUpdateConfig(config.copy(asrMadhhab = 1)) },
            label = { Text("Standard 2x Shadow", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            )
          )
          FilterChip(
            selected = config.asrMadhhab == 0,
            onClick = { onUpdateConfig(config.copy(asrMadhhab = 0)) },
            label = { Text("Standard 1x Shadow", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            )
          )
        }

        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PrayerTimeCalculator.calculationMethods.forEach { method ->
            FilterChip(
              selected = config.calculationMethodId == method.id,
              onClick = { onUpdateConfig(config.copy(calculationMethodId = method.id)) },
              label = { Text(method.name, fontSize = 11.sp, maxLines = 1) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // 4. Concise Adhan Voice, Real-Time Alarm Volume & Vibration Controls
        var expandMuezzinsInSheet by remember { mutableStateOf(false) }
        val currentSheetMuezzin = remember(config.selectedMuezzinId) {
          MuezzinAudioOption.entries.find { it.id == config.selectedMuezzinId }
            ?: MuezzinAudioOption.MADINAH_NABAWI
        }
        val sheetHwAlarm = remember(config.alarmVolumePercent, nowMillis / 1000L) {
          AzanAudioController.getHardwareAlarmVolumeStatus(context)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Active Adhan: ${currentSheetMuezzin.title}",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "${currentSheetMuezzin.originMasjid} • ${currentSheetMuezzin.maqamOrStyle}",
              style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.SemiBold)
            )
          }
          OutlinedButton(
            onClick = { expandMuezzinsInSheet = !expandMuezzinsInSheet },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = if (expandMuezzinsInSheet) "Hide Voices" else "Change (${MuezzinAudioOption.entries.size})",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        if (expandMuezzinsInSheet) {
          Spacer(modifier = Modifier.height(6.dp))
          MuezzinAudioOption.entries.forEach { muezzin ->
            val isPlayingThis = alarmPlaybackState.isActive && alarmPlaybackState.activeMuezzinId == muezzin.id
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onUpdateConfig(config.copy(selectedMuezzinId = muezzin.id)) }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = config.selectedMuezzinId == muezzin.id,
                onClick = { onUpdateConfig(config.copy(selectedMuezzinId = muezzin.id)) }
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(text = muezzin.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                Text(
                  text = "${muezzin.originMasjid} • ${muezzin.maqamOrStyle}",
                  style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontSize = 10.sp)
                )
              }
              OutlinedButton(
                onClick = {
                  if (isPlayingThis) {
                    onStopActiveAlarm()
                  } else {
                    AzanAudioController.previewMuezzinAzan(context, muezzin)
                  }
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(if (isPlayingThis) "Stop" else "Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(4.dp))
              OutlinedButton(
                onClick = {
                  if (isPlayingThis) {
                    onStopActiveAlarm()
                  } else {
                    onUpdateConfig(config.copy(selectedMuezzinId = muezzin.id))
                    AzanAudioController.testMuezzinAzan(context, muezzin)
                  }
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(if (isPlayingThis) "Stop" else "Keep", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Audio Level (Equal to Alarm Volume): ${sheetHwAlarm.currentPercent}%",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
          OutlinedButton(
            onClick = {
              try {
                context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
              } catch (_: Exception) {
              }
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Increase in Sound Settings", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
        Slider(
          value = sheetHwAlarm.currentPercent.coerceIn(0, 100).toFloat(),
          onValueChange = { onUpdateConfig(config.copy(alarmVolumePercent = it.toInt().coerceIn(0, 100))) },
          valueRange = 0f..100f
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Use Android Alarm Stream (Max Permitted Volume)",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f)
          )
          Switch(
            checked = config.boostSystemAlarmStream,
            onCheckedChange = { onUpdateConfig(config.copy(boostSystemAlarmStream = it)) }
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Continuous Vibration During Alarm",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f)
          )
          Switch(
            checked = config.vibrationEnabled && config.continuousVibration,
            onCheckedChange = {
              onUpdateConfig(
                config.copy(
                  vibrationEnabled = it,
                  continuousVibration = it
                )
              )
            }
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom Fajr Wake-Up Offset Input
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customOffsetInput,
            onValueChange = { customOffsetInput = it.filter { ch -> ch.isDigit() } },
            label = { Text("Custom Minutes Before Fajr (1-180)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )
          Button(
            onClick = {
              val mins = customOffsetInput.toIntOrNull()
              if (mins != null && mins in 1..180) {
                onUpdateConfig(config.copy(fajrWakeUpOffsetMinutes = mins))
                Toast.makeText(context, "Fajr wake-up set to $mins minutes before Fajr", Toast.LENGTH_SHORT).show()
              }
            }
          ) {
            Text("Set")
          }
        }

        Spacer(modifier = Modifier.height(28.dp))
      }
    }
  }
}

@Composable
private fun PermissionStatusRow(
  label: String,
  isReady: Boolean,
  statusText: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
        contentDescription = null,
        tint = if (isReady) EmeraldPrimary else GoldPrimary,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        Text(
          text = statusText,
          style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
      }
    }
    if (actionLabel != null && onAction != null) {
      OutlinedButton(
        onClick = onAction,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(actionLabel, fontSize = 11.sp)
      }
    }
  }
}

@SuppressLint("MissingPermission")
private fun detectDeviceGpsLocation(context: Context): Pair<Double, Double>? {
  return try {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = lm.getProviders(true)
    for (p in providers) {
      val loc = lm.getLastKnownLocation(p)
      if (loc != null) {
        return loc.latitude to loc.longitude
      }
    }
    null
  } catch (_: Exception) {
    null
  }
}
