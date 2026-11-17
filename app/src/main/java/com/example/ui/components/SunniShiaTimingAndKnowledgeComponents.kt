package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PrayerScheduleEntity
import com.example.data.model.JurisprudentialRulingStatus
import com.example.data.model.ShiaDevotionalOccasionItem
import com.example.data.model.ShiaEvidencePracticeItem
import com.example.data.model.ShiaIslamicKnowledgeRepository
import com.example.data.model.ShiaLearningModuleItem
import com.example.data.model.ShiaSupplicationItem
import com.example.data.model.ShiaSupplicationSourceClassification
import com.example.data.prayer.NextPrayerStatus
import com.example.data.prayer.PrayerAndAlarmConfig
import com.example.data.prayer.PrayerTimeCalculator
import com.example.data.prayer.SehriIftarTimetableEngine
import com.example.data.prayer.ShiaJafariCalculationMethod
import com.example.data.prayer.ShiaJafariDailySchedule
import com.example.data.prayer.ShiaJafariPrayerAndFastingEngine
import com.example.data.prayer.ShiaPrayerAndFastingConfig
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.UrduNastaliqFontFamily
import java.util.Calendar

private data class CombinedTimingComparisonRow(
  val key: String,
  val labelEnglish: String,
  val labelUrdu: String,
  val sunniTime24: String,
  val shiaTime24: String,
  val sunniSubNote: String,
  val shiaSubNote: String,
  val isFastingRow: Boolean = false
)

/**
 * Requirement 1: ONE Combined Sunni–Shia Timing Comparison Table on the Home Screen.
 *
 * Displays Sunni and Shia Ja'fari timings simultaneously side-by-side for the exact same
 * location, date, and timezone, while keeping both calculation engines and settings independent.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CombinedSunniShiaTimingComparisonTable(
  sunniSchedule: PrayerScheduleEntity,
  sunniConfig: PrayerAndAlarmConfig,
  sunniNextPrayer: NextPrayerStatus,
  nowMillis: Long,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var shiaConfig by remember {
    mutableStateOf(ShiaJafariPrayerAndFastingEngine.loadShiaConfig(context))
  }
  var showShiaSettingsPanel by remember { mutableStateOf(false) }

  val baseShiaSchedule = remember(
    sunniSchedule.dateIso,
    sunniSchedule.latitude,
    sunniSchedule.longitude,
    sunniSchedule.timeZoneId,
    shiaConfig
  ) {
    ShiaJafariPrayerAndFastingEngine.calculateShiaSchedule(
      calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone(sunniSchedule.timeZoneId)),
      locationName = sunniSchedule.locationName,
      country = sunniSchedule.country,
      latitude = sunniSchedule.latitude,
      longitude = sunniSchedule.longitude,
      timeZoneId = sunniSchedule.timeZoneId,
      shiaConfig = shiaConfig
    )
  }

  var activeShiaSchedule by remember(baseShiaSchedule) { mutableStateOf(baseShiaSchedule) }

  LaunchedEffect(
    sunniSchedule.dateIso,
    sunniSchedule.latitude,
    sunniSchedule.longitude,
    sunniSchedule.timeZoneId,
    shiaConfig
  ) {
    activeShiaSchedule = ShiaJafariPrayerAndFastingEngine.fetchOrCalculateShiaSchedule(
      context = context,
      calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone(sunniSchedule.timeZoneId)),
      locationName = sunniSchedule.locationName,
      country = sunniSchedule.country,
      latitude = sunniSchedule.latitude,
      longitude = sunniSchedule.longitude,
      timeZoneId = sunniSchedule.timeZoneId,
      shiaConfig = shiaConfig
    )
  }

  val shiaNextPrayer = remember(activeShiaSchedule, nowMillis) {
    ShiaJafariPrayerAndFastingEngine.computeNextShiaPrayerStatus(activeShiaSchedule, nowMillis)
  }

  val sunniSehriEnds24 = remember(sunniSchedule.fajrTime, sunniConfig.sehriAdjustmentMinutes) {
    SehriIftarTimetableEngine.applyMinuteAdjustment(
      sunniSchedule.fajrTime,
      sunniConfig.sehriAdjustmentMinutes
    )
  }
  val sunniIftari24 = remember(sunniSchedule.maghribTime, sunniConfig.iftarAdjustmentMinutes) {
    SehriIftarTimetableEngine.applyMinuteAdjustment(
      sunniSchedule.maghribTime,
      sunniConfig.iftarAdjustmentMinutes
    )
  }

  val comparisonRows = remember(
    sunniSchedule,
    activeShiaSchedule,
    sunniSehriEnds24,
    sunniIftari24,
    sunniConfig.asrMadhhab,
    shiaConfig
  ) {
    listOf(
      CombinedTimingComparisonRow(
        key = "fajr",
        labelEnglish = "Fajr",
        labelUrdu = "فجر",
        sunniTime24 = sunniSchedule.fajrTime,
        shiaTime24 = activeShiaSchedule.fajrTime,
        sunniSubNote = "Sunni Engine",
        shiaSubNote = "Ja'fari (${shiaConfig.method.fajrAngle}°)"
      ),
      CombinedTimingComparisonRow(
        key = "sunrise",
        labelEnglish = "Sunrise",
        labelUrdu = "طلوعِ آفتاب",
        sunniTime24 = sunniSchedule.sunriseTime,
        shiaTime24 = activeShiaSchedule.sunriseTime,
        sunniSubNote = "Horizon (-0.833°)",
        shiaSubNote = "Horizon (-0.833°)"
      ),
      CombinedTimingComparisonRow(
        key = "dhuhr",
        labelEnglish = "Dhuhr",
        labelUrdu = "ظہر",
        sunniTime24 = sunniSchedule.dhuhrTime,
        shiaTime24 = activeShiaSchedule.dhuhrTime,
        sunniSubNote = "Post-Zawal",
        shiaSubNote = "Shar'i Zawal"
      ),
      CombinedTimingComparisonRow(
        key = "asr",
        labelEnglish = "Asr",
        labelUrdu = "عصر",
        sunniTime24 = sunniSchedule.asrTime,
        shiaTime24 = activeShiaSchedule.asrTime,
        sunniSubNote = if (sunniConfig.asrMadhhab == 1) "Hanafi (2x)" else "Standard (1x)",
        shiaSubNote = "Fadilah (1x / Jam')"
      ),
      CombinedTimingComparisonRow(
        key = "maghrib",
        labelEnglish = "Maghrib",
        labelUrdu = "مغرب",
        sunniTime24 = sunniSchedule.maghribTime,
        shiaTime24 = activeShiaSchedule.maghribTime,
        sunniSubNote = "Disk Sunset",
        shiaSubNote = "Redness (${shiaConfig.method.maghribRednessAngle}°)"
      ),
      CombinedTimingComparisonRow(
        key = "isha",
        labelEnglish = "Isha",
        labelUrdu = "عشاء",
        sunniTime24 = sunniSchedule.ishaTime,
        shiaTime24 = activeShiaSchedule.ishaTime,
        sunniSubNote = "Sunni Isha",
        shiaSubNote = "Fadilah (${shiaConfig.method.ishaAngle}°)"
      ),
      CombinedTimingComparisonRow(
        key = "sehri_ends",
        labelEnglish = "Sehri ends",
        labelUrdu = "ختمِ سحری",
        sunniTime24 = sunniSehriEnds24,
        shiaTime24 = activeShiaSchedule.sehriEndsTime,
        sunniSubNote = "Sunni Fajr-based",
        shiaSubNote = if (shiaConfig.sehriImsakPrecautionMinutes > 0) {
          "Shia Fajr -${shiaConfig.sehriImsakPrecautionMinutes}m"
        } else {
          "Shia Fajr-based"
        },
        isFastingRow = true
      ),
      CombinedTimingComparisonRow(
        key = "iftari",
        labelEnglish = "Iftari",
        labelUrdu = "افطاری",
        sunniTime24 = sunniIftari24,
        shiaTime24 = activeShiaSchedule.iftarTime,
        sunniSubNote = "Sunni Sunset",
        shiaSubNote = "Ja'fari Maghrib (Sunset ${PrayerTimeCalculator.format24hTo12h(activeShiaSchedule.astronomicalSunsetTime)})",
        isFastingRow = true
      )
    )
  }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
    border = BorderStroke(1.2.dp, GoldPrimary.copy(alpha = 0.65f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("combined_sunni_shia_timing_table")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Title & Independent Shia Settings Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Unified Sunni & Shia (Ja'fari) Timing Comparison Table",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldPrimary
            )
          )
          Text(
            text = "سنی اور شیعہ (جعفری) اوقاتِ نماز و سحری و افطاری کا تقابلی جدول",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              color = GoldPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          )
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = EmeraldPrimary.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
          modifier = Modifier
            .clickable { showShiaSettingsPanel = !showShiaSettingsPanel }
            .testTag("shia_calculation_settings_toggle_btn")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Shia Ja'fari Settings",
              tint = EmeraldPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Ja'fari Settings",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                fontSize = 10.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Dual Non-Confusing Next Prayer Status Pills (Sunni vs. Shia)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.14f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
          modifier = Modifier
            .weight(1f)
            .testTag("combined_table_next_sunni_badge")
        ) {
          Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
              text = "NEXT SUNNI: ${sunniNextPrayer.nextPrayerName.uppercase()}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPrimary,
                fontSize = 10.sp
              )
            )
            Text(
              text = "${PrayerTimeCalculator.format24hTo12h(sunniNextPrayer.nextPrayerTime24)} (-${sunniNextPrayer.countdownFormatted})",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp
              )
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0A4D3C).copy(alpha = 0.16f),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.75f)),
          modifier = Modifier
            .weight(1f)
            .testTag("combined_table_next_shia_badge")
        ) {
          Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
              text = "NEXT SHIA (JA'FARI): ${shiaNextPrayer.nextPrayerName.uppercase()}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = GoldPrimary,
                fontSize = 10.sp
              )
            )
            Text(
              text = "${PrayerTimeCalculator.format24hTo12h(shiaNextPrayer.nextPrayerTime24)} (-${shiaNextPrayer.countdownFormatted})",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp
              )
            )
          }
        }
      }

      // Expandable Independent Shia Calculation Settings
      if (showShiaSettingsPanel) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("shia_independent_settings_panel")
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Independent Shia Ja'fari Calculation Engine Settings",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPrimary
              )
            )
            Text(
              text = "Sunni settings remain unchanged. Select the Ja'fari calculation convention & precautionary Imsak offset:",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              ShiaJafariCalculationMethod.entries.forEach { methodOption ->
                FilterChip(
                  selected = shiaConfig.method == methodOption,
                  onClick = {
                    val updated = shiaConfig.copy(
                      method = methodOption,
                      sehriImsakPrecautionMinutes = methodOption.defaultSehriImsakPrecautionMinutes
                    )
                    shiaConfig = updated
                    ShiaJafariPrayerAndFastingEngine.saveShiaConfig(context, updated)
                  },
                  label = { Text(methodOption.shortLabel, fontSize = 10.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("shia_method_chip_${methodOption.id}")
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = shiaConfig.method.jurisprudentialSummary,
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Shia Sehri Imsak Precaution Before Fajr:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
              )
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0, 2, 5, 10).forEach { mins ->
                  FilterChip(
                    selected = shiaConfig.sehriImsakPrecautionMinutes == mins,
                    onClick = {
                      val updated = shiaConfig.copy(sehriImsakPrecautionMinutes = mins)
                      shiaConfig = updated
                      ShiaJafariPrayerAndFastingEngine.saveShiaConfig(context, updated)
                    },
                    label = { Text(if (mins == 0) "0m (Exact Fajr)" else "-${mins}m", fontSize = 10.sp) },
                    modifier = Modifier.testTag("shia_imsak_offset_chip_$mins")
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Table Column Headers: Prayer / Timing | Sunni | Shia
      Surface(
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        color = Color(0xFF0A4D3C),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Prayer / Timing",
            style = MaterialTheme.typography.labelMedium.copy(
              color = GoldPrimary,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 11.sp
            ),
            modifier = Modifier.weight(1.1f)
          )
          Text(
            text = "Sunni (${if (sunniConfig.asrMadhhab == 1) "Hanafi" else "Standard"})",
            style = MaterialTheme.typography.labelMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 11.sp,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.weight(1.15f)
          )
          Text(
            text = "Shia (Ja'fari)",
            style = MaterialTheme.typography.labelMedium.copy(
              color = GoldPrimary,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 11.sp,
              textAlign = TextAlign.End
            ),
            modifier = Modifier.weight(1.15f)
          )
        }
      }

      // 8 Comparison Rows (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, Sehri ends, Iftari)
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        comparisonRows.forEachIndexed { idx, row ->
          val isSunniNext = !row.isFastingRow &&
            sunniNextPrayer.nextPrayerName.equals(row.labelEnglish, ignoreCase = true)
          val isShiaNext = !row.isFastingRow &&
            shiaNextPrayer.nextPrayerName.equals(row.labelEnglish, ignoreCase = true)

          val rowBg = when {
            row.isFastingRow -> GoldPrimary.copy(alpha = 0.13f)
            idx % 2 == 0 -> MaterialTheme.colorScheme.surface
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
          }

          Surface(
            color = rowBg,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("combined_timing_row_${row.key}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Column 1: Prayer / Timing Name (English + RTL Urdu)
              Column(modifier = Modifier.weight(1.1f)) {
                Text(
                  text = row.labelEnglish,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (row.isFastingRow) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                  )
                )
                Text(
                  text = row.labelUrdu,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                )
              }

              // Column 2: Sunni Timing
              Column(
                modifier = Modifier.weight(1.15f),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isSunniNext) EmeraldPrimary.copy(alpha = 0.18f) else Color.Transparent,
                  border = if (isSunniNext) BorderStroke(1.dp, EmeraldPrimary) else null
                ) {
                  Text(
                    text = PrayerTimeCalculator.format24hTo12h(row.sunniTime24),
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.ExtraBold,
                      color = if (isSunniNext) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
                Text(
                  text = "${row.sunniTime24} • ${row.sunniSubNote}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                  )
                )
              }

              // Column 3: Shia (Ja'fari) Timing
              Column(
                modifier = Modifier.weight(1.15f),
                horizontalAlignment = Alignment.End
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isShiaNext) GoldPrimary.copy(alpha = 0.22f) else Color.Transparent,
                  border = if (isShiaNext) BorderStroke(1.dp, GoldPrimary) else null
                ) {
                  Text(
                    text = PrayerTimeCalculator.format24hTo12h(row.shiaTime24),
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.ExtraBold,
                      color = if (isShiaNext) Color(0xFF8D6E00) else EmeraldPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
                Text(
                  text = "${row.shiaTime24} • ${row.shiaSubNote}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                  )
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Method & Source Verification Footer
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("combined_table_sources_footer")
      ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
          Text(
            text = "• Sunni Engine: ${sunniSchedule.calculationMethodName} (${if (sunniConfig.asrMadhhab == 1) "Hanafi Asr" else "Shafi'i/Standard Asr"}) • ${sunniSchedule.sourceName}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 9.5.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold
            )
          )
          Text(
            text = "• Shia Engine: ${activeShiaSchedule.sourceDescription} • Shia Midnight (Muntasaf al-Layl): ${PrayerTimeCalculator.format24hTo12h(activeShiaSchedule.shiaMidnightTime)}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 9.5.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold
            )
          )
          Text(
            text = "• Jurisprudential Note: In Ja'fari Fiqh (Al-Kafi Vol. 3 & Sistani Ruling 719), Maghrib & Iftari enter after the eastern redness passes overhead (${shiaConfig.method.maghribRednessAngle}° solar depression), ~13–18 min after disk sunset (${PrayerTimeCalculator.format24hTo12h(activeShiaSchedule.astronomicalSunsetTime)}).",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 9.sp,
              color = GoldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }
  }
}

/**
 * Requirement 2, 3, 4, 5, 6, 7: Dedicated Shia Home Section Card on the Home Screen.
 *
 * Preserves all existing Sunni Home cards and provides an organized, evidence-based hub
 * for Shia Daily Duas, Shia Fasting & Ramadan Engine, Evidence-Based Practices & Reminders,
 * Shia Learning Modules, Devotional Calendar, and Verified Primary Sources.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiaHomeKnowledgeAndPracticesSectionCard(
  sunniConfig: PrayerAndAlarmConfig,
  onOpenSehriIftarScreen: () -> Unit,
  onOpenDailyDuasScreen: () -> Unit,
  onOpenLearnIslamScreen: () -> Unit,
  onOpenNooraniQaidaPdf: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var activeTabIndex by remember { mutableIntStateOf(0) }
  var shiaConfig by remember {
    mutableStateOf(ShiaJafariPrayerAndFastingEngine.loadShiaConfig(context))
  }
  var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

  val shiaSchedule = remember(
    sunniConfig.city,
    sunniConfig.latitude,
    sunniConfig.longitude,
    sunniConfig.timeZoneId,
    shiaConfig
  ) {
    ShiaJafariPrayerAndFastingEngine.calculateShiaSchedule(
      locationName = sunniConfig.city,
      country = sunniConfig.country,
      latitude = sunniConfig.latitude,
      longitude = sunniConfig.longitude,
      timeZoneId = sunniConfig.timeZoneId,
      shiaConfig = shiaConfig
    )
  }

  val fastingCountdown = remember(shiaSchedule, nowMillis) {
    ShiaJafariPrayerAndFastingEngine.computeShiaFastingCountdown(shiaSchedule, nowMillis)
  }

  val isDeveloperOptionsEnabled = remember {
    context.getSharedPreferences("bayan_developer_options_prefs", Context.MODE_PRIVATE)
      .getBoolean("developer_settings_enabled", false)
  }

  val sectionTabs = remember(isDeveloperOptionsEnabled) {
    buildList {
      add("🤲 Shia Duas (8)" to "shia_home_tab_duas")
      add("🌙 Shia Fasting Engine" to "shia_home_tab_fasting")
      add("⚖️ Practices & Rulings" to "shia_home_tab_practices")
      add("🎓 Learning & Calendar" to "shia_home_tab_learning")
      if (isDeveloperOptionsEnabled) {
        add("🔗 13 Primary Sources" to "shia_home_tab_sources")
      }
    }
  }

  ElevatedCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("home_shia_section_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Illuminated Header Banner
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0A4D3C),
        border = BorderStroke(1.5.dp, GoldPrimary),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = GoldPrimary.copy(alpha = 0.22f)
          ) {
            Text(
              text = "JA'FARI (SHIA ITHNA-ASHARI) EVIDENCE-BASED KNOWLEDGE SECTION",
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.5.sp,
                textAlign = TextAlign.Center
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "فقہِ جعفری: مستند اوقاتِ سحری و افطاری، ادعیہ، احکام، یاددہانی اور نصابِ تعلیم",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              color = GoldPrimary,
              fontSize = 15.sp,
              lineHeight = 26.sp,
              fontWeight = FontWeight.Bold,
              textDirection = TextDirection.Rtl,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Shia Timings, Duas, Practices & Jurisprudential Learning",
            style = MaterialTheme.typography.titleMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              textAlign = TextAlign.Center
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Grounded strictly in Al-Kafi, Man La Yahduruhu al-Faqih, Tahdhib al-Ahkam, Al-Istibsar, Wasa'il al-Shia, Al-Sahifa al-Sajjadiyya, Al-Misbah al-Mutahajjid, Iqbal al-A'mal, Mafatih al-Jinan & Official Marja'iyyah Rulings.",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFFD8F3DC),
              textAlign = TextAlign.Center,
              lineHeight = 15.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Section Navigation Chips
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        sectionTabs.forEachIndexed { idx, (label, tag) ->
          FilterChip(
            selected = activeTabIndex == idx,
            onClick = { activeTabIndex = idx },
            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag(tag)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      when (activeTabIndex) {
        0 -> {
          // Tab 0: Independent Shia Dua Library Preview & Full Viewer
          ShiaDuaLibraryInteractiveSection(
            onOpenFullDuasScreen = onOpenDailyDuasScreen
          )
        }
        1 -> {
          // Tab 1: Separate Shia Sehri & Iftari Engine + 30-Day Ramadan Calendar
          ShiaSehriIftarEnginePanel(
            sunniConfig = sunniConfig,
            shiaConfig = shiaConfig,
            shiaSchedule = shiaSchedule,
            fastingCountdown = fastingCountdown,
            onUpdateShiaConfig = { updated ->
              shiaConfig = updated
              ShiaJafariPrayerAndFastingEngine.saveShiaConfig(context, updated)
            },
            onOpenFullTimetableScreen = onOpenSehriIftarScreen
          )
        }
        2 -> {
          // Tab 2: Evidence-Based Shia Practices & Reminders
          ShiaEvidencePracticesSection()
        }
        3 -> {
          // Tab 3: Shia Learning Modules & Devotional Calendar
          ShiaLearningAndCalendarSection(
            onOpenNooraniQaidaPdf = onOpenNooraniQaidaPdf,
            onOpenLearnIslamScreen = onOpenLearnIslamScreen
          )
        }
        4 -> {
          // Tab 4: 13 Verified APIs & Primary Sources Registry
          ShiaVerifiedSourcesRegistrySection()
        }
      }
    }
  }
}

/**
 * Requirement 2: Separate Shia Sehri and Iftari Engine Panel.
 * Can be embedded in both the Home Screen Shia Section and the SehriIftarTimetableScreen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiaSehriIftarEnginePanel(
  sunniConfig: PrayerAndAlarmConfig,
  shiaConfig: ShiaPrayerAndFastingConfig,
  shiaSchedule: ShiaJafariDailySchedule,
  fastingCountdown: com.example.data.prayer.ShiaFastingCountdownStatus,
  onUpdateShiaConfig: (ShiaPrayerAndFastingConfig) -> Unit,
  onOpenFullTimetableScreen: () -> Unit = {},
  showOpenFullScreenButton: Boolean = true,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var show30DayShiaCalendar by remember { mutableStateOf(false) }

  val calendar30Days = remember(
    sunniConfig.city,
    sunniConfig.latitude,
    sunniConfig.longitude,
    sunniConfig.timeZoneId,
    shiaConfig
  ) {
    ShiaJafariPrayerAndFastingEngine.generateShia30DayFastingCalendar(
      daysCount = 30,
      locationName = sunniConfig.city,
      country = sunniConfig.country,
      latitude = sunniConfig.latitude,
      longitude = sunniConfig.longitude,
      timeZoneId = sunniConfig.timeZoneId,
      shiaConfig = shiaConfig
    )
  }

  val shiaFastingDuas = remember {
    ShiaIslamicKnowledgeRepository.shiaDuasCatalog.filter { it.categoryTag == "FASTING_RAMADAN" }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("sehri_iftar_shia_engine_card"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Live Shia Fasting Countdown Banner
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = Color(0xFF0A4D3C),
      border = BorderStroke(1.2.dp, GoldPrimary),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("shia_fasting_countdown_banner")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = fastingCountdown.currentPhaseTitle,
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold
              )
            )
            Text(
              text = "${fastingCountdown.targetEventLabel}: ${fastingCountdown.targetTime12h} (${fastingCountdown.targetTime24})",
              style = MaterialTheme.typography.titleSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            )
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = GoldPrimary.copy(alpha = 0.22f)
          ) {
            Text(
              text = "-${fastingCountdown.countdownHhMmSs}",
              style = MaterialTheme.typography.titleMedium.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = fastingCountdown.astronomicalSunsetNote,
          style = MaterialTheme.typography.labelSmall.copy(
            color = Color(0xFFD8F3DC),
            fontSize = 10.sp
          )
        )
      }
    }

    // 4-Box Shia Fasting Timings Grid: Shia Sehri Ends | Shia True Dawn (Fajr) | Astronomical Sunset | Shia Maghrib & Iftari
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ShiaTimingMetricTile(
        title = "Shia Sehri Ends",
        urduTitle = "ختمِ سحری (فقہِ جعفری)",
        time24 = shiaSchedule.sehriEndsTime,
        subNote = if (shiaConfig.sehriImsakPrecautionMinutes > 0) {
          "Imsak (-${shiaConfig.sehriImsakPrecautionMinutes}m before Fajr)"
        } else {
          "At True Dawn (Subh Sadiq)"
        },
        modifier = Modifier.weight(1f)
      )
      ShiaTimingMetricTile(
        title = "Shia Fajr (True Dawn)",
        urduTitle = "طلوعِ فجرِ صادق",
        time24 = shiaSchedule.fajrTime,
        subNote = "${shiaConfig.method.fajrAngle}° Solar Depression",
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ShiaTimingMetricTile(
        title = "Astronomical Sunset",
        urduTitle = "غروبِ قرصِ آفتاب",
        time24 = shiaSchedule.astronomicalSunsetTime,
        subNote = "Disk Sunset (-0.833°) • Wait for Redness",
        modifier = Modifier.weight(1f)
      )
      ShiaTimingMetricTile(
        title = "Shia Maghrib & Iftari",
        urduTitle = "شرعی مغرب و افطار",
        time24 = shiaSchedule.iftarTime,
        subNote = "Eastern Redness (${shiaConfig.method.maghribRednessAngle}°)",
        isHighlighted = true,
        modifier = Modifier.weight(1f)
      )
    }

    // Independent Shia Sehri & Iftari Notifications + Calculation Method Selector
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          text = "Shia Fasting Calculation Method & Notifications (${sunniConfig.city}, ${sunniConfig.country})",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldPrimary
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          ShiaJafariCalculationMethod.entries.forEach { m ->
            FilterChip(
              selected = shiaConfig.method == m,
              onClick = {
                onUpdateShiaConfig(
                  shiaConfig.copy(
                    method = m,
                    sehriImsakPrecautionMinutes = m.defaultSehriImsakPrecautionMinutes
                  )
                )
              },
              label = { Text(m.shortLabel, fontSize = 10.sp) }
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Shia Sehri & Iftari Notifications",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Alerts at Shia Sehri (${PrayerTimeCalculator.format24hTo12h(shiaSchedule.sehriEndsTime)}) & Ja'fari Iftari (${PrayerTimeCalculator.format24hTo12h(shiaSchedule.iftarTime)})",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
          Switch(
            checked = shiaConfig.shiaSehriNotificationEnabled && shiaConfig.shiaIftariNotificationEnabled,
            onCheckedChange = { enabled ->
              onUpdateShiaConfig(
                shiaConfig.copy(
                  shiaSehriNotificationEnabled = enabled,
                  shiaIftariNotificationEnabled = enabled
                )
              )
              val msg = if (enabled) {
                "Shia Sehri & Ja'fari Iftari notifications enabled"
              } else {
                "Shia Sehri & Ja'fari Iftari notifications paused"
              }
              Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("shia_sehri_iftar_notif_switch")
          )
        }
      }
    }

    // 30-Day Shia Ramadan / Monthly Fasting Calendar Toggle & Table
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { show30DayShiaCalendar = !show30DayShiaCalendar },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = EmeraldPrimary,
          contentColor = Color.White
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("toggle_shia_30_day_calendar_btn")
      ) {
        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (show30DayShiaCalendar) "Hide 30-Day Ja'fari Calendar" else "30-Day Shia Fasting Calendar",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
      if (showOpenFullScreenButton) {
        OutlinedButton(
          onClick = onOpenFullTimetableScreen,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text("Full Timetable Screen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    if (show30DayShiaCalendar) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("shia_30_day_calendar_table")
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(
            text = "30-Day Shia (Ja'fari) Sehri, Fajr, Sunset & Iftari Calendar (${sunniConfig.city})",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldPrimary
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF0A4D3C), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Text("Day / Date", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
            Text("Sehri / Fajr", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Sunset", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.85f))
            Text("Iftari", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.95f))
          }
          Spacer(modifier = Modifier.height(4.dp))
          calendar30Days.forEach { day ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1.3f)) {
                Text(
                  text = "Day ${day.dayNumber} • ${day.gregorianReadable.substringBeforeLast(" ")}",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                )
                Text(
                  text = day.hijriReadable,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = GoldPrimary)
                )
              }
              Text(
                text = "${PrayerTimeCalculator.format24hTo12h(day.sehriEnds24)}\n(Fajr ${day.fajrDawn24})",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                modifier = Modifier.weight(1f)
              )
              Text(
                text = PrayerTimeCalculator.format24hTo12h(day.astronomicalSunset24),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.5.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(0.85f)
              )
              Text(
                text = PrayerTimeCalculator.format24hTo12h(day.maghribIftari24),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldPrimary,
                  textAlign = TextAlign.End
                ),
                modifier = Modifier.weight(0.95f)
              )
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
          }
        }
      }
    }

    // Authentic Shia Fasting & Ramadan Duas with References
    Text(
      text = "Verified Shia Fasting & Ramadan Duas (Al-Kafi, Tahdhib & Iqbal al-A'mal)",
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.ExtraBold,
        color = EmeraldPrimary
      )
    )
    shiaFastingDuas.forEach { dua ->
      ShiaScholarlySupplicationCard(supplication = dua)
    }
  }
}

@Composable
private fun ShiaTimingMetricTile(
  title: String,
  urduTitle: String,
  time24: String,
  subNote: String,
  isHighlighted: Boolean = false,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isHighlighted) GoldPrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = BorderStroke(
      1.dp,
      if (isHighlighted) GoldPrimary else EmeraldPrimary.copy(alpha = 0.35f)
    ),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = if (isHighlighted) Color(0xFF8D6E00) else EmeraldPrimary
        )
      )
      Text(
        text = urduTitle,
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${PrayerTimeCalculator.format24hTo12h(time24)} ($time24)",
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      )
      Text(
        text = subNote,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 9.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
    }
  }
}

/**
 * Requirement 4: Independent Shia Dua Library Section with all 8 required scholarly fields
 * and explicit source-classification filter.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiaDuaLibraryInteractiveSection(
  onOpenFullDuasScreen: () -> Unit = {},
  showOpenFullDuasButton: Boolean = true
) {
  var selectedClassification by remember { mutableStateOf<ShiaSupplicationSourceClassification?>(null) }
  var searchQuery by remember { mutableStateOf("") }

  val filteredDuas = remember(selectedClassification, searchQuery) {
    val q = searchQuery.trim()
    ShiaIslamicKnowledgeRepository.shiaDuasCatalog.filter { item ->
      val classMatch = selectedClassification == null || item.sourceClassification == selectedClassification
      val queryMatch = q.isEmpty() ||
        item.titleEnglish.contains(q, ignoreCase = true) ||
        item.titleUrdu.contains(q, ignoreCase = true) ||
        item.arabicText.contains(q, ignoreCase = true) ||
        item.englishTranslation.contains(q, ignoreCase = true) ||
        item.originalBookAndAuthor.contains(q, ignoreCase = true) ||
        item.scholarlyGradingAndEvaluator.contains(q, ignoreCase = true)
      classMatch && queryMatch
    }
  }

  val context = LocalContext.current
  val isDeveloperOptionsEnabled = remember {
    context.getSharedPreferences("bayan_developer_options_prefs", Context.MODE_PRIVATE)
      .getBoolean("developer_settings_enabled", false)
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("shia_dua_library_interactive_section"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    if (isDeveloperOptionsEnabled) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = EmeraldPrimary.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(
            text = "Scholarly Verification Standard (Distinguishing Hadith from Devotional Texts)",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldPrimary
            )
          )
          Text(
            text = "Every supplication below displays: (1) Vocalized Arabic, (2) Transliteration, (3) Urdu Translation, (4) English Translation, (5) Original Book & Author, (6) Volume/Chapter/Reference, (7) Scholarly Grading & Evaluator (e.g. Allamah al-Majlisi in Mir'at al-Uqul, Shaykh al-Bahbudi), and (8) Source Classification & Context.",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 14.sp
            )
          )
        }
      }
    }

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      singleLine = true,
      placeholder = {
        Text("Search Al-Kafi, Sahifa Sajjadiyya, Misbah, Mafatih, Kumayl, Iftitah...", fontSize = 11.sp)
      },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("shia_duas_search_input")
    )

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      FilterChip(
        selected = selectedClassification == null,
        onClick = { selectedClassification = null },
        label = { Text("All (${ShiaIslamicKnowledgeRepository.shiaDuasCatalog.size})", fontSize = 10.sp) }
      )
      ShiaSupplicationSourceClassification.entries.forEach { tier ->
        FilterChip(
          selected = selectedClassification == tier,
          onClick = { selectedClassification = if (selectedClassification == tier) null else tier },
          label = { Text(tier.badgeLabelEnglish.substringBefore(" ("), fontSize = 10.sp) }
        )
      }
    }

    filteredDuas.forEach { dua ->
      ShiaScholarlySupplicationCard(supplication = dua)
    }

    if (showOpenFullDuasButton) {
      OutlinedButton(
        onClick = onOpenFullDuasScreen,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Open Full Dual Sunni & Shia Duas Screen", fontWeight = FontWeight.Bold, fontSize = 11.sp)
      }
    }
  }
}

@Composable
fun ShiaScholarlySupplicationCard(
  supplication: ShiaSupplicationItem,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("shia_supplication_card_${supplication.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Source Classification Badge
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = when (supplication.sourceClassification) {
          ShiaSupplicationSourceClassification.HADITH_PROPHET_OR_AHL_AL_BAYT -> EmeraldPrimary.copy(alpha = 0.16f)
          ShiaSupplicationSourceClassification.DEVOTIONAL_TRANSMITTED_LITERATURE -> GoldPrimary.copy(alpha = 0.2f)
          else -> MaterialTheme.colorScheme.secondaryContainer
        }
      ) {
        Text(
          text = "${supplication.sourceClassification.badgeLabelEnglish} • ${supplication.sourceClassification.badgeLabelUrdu}",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldPrimary,
            fontSize = 10.sp
          ),
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = supplication.titleEnglish,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      )
      Text(
        text = supplication.titleUrdu,
        style = MaterialTheme.typography.bodySmall.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = 14.sp,
          lineHeight = 24.sp,
          fontWeight = FontWeight.Bold,
          color = GoldPrimary,
          textDirection = TextDirection.Rtl,
          textAlign = TextAlign.Right
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 1. Original Arabic Text (Full RTL)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = supplication.arabicText,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = AmiriFontFamily,
            fontSize = 22.sp,
            lineHeight = 40.sp,
            textDirection = TextDirection.Rtl,
            textAlign = TextAlign.Right,
            color = MaterialTheme.colorScheme.onSurface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 2. Transliteration
      Text(
        text = supplication.transliteration,
        style = MaterialTheme.typography.bodySmall.copy(
          fontStyle = FontStyle.Italic,
          color = MaterialTheme.colorScheme.secondary,
          lineHeight = 18.sp
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      // 3. Urdu Translation (Full RTL)
      Text(
        text = supplication.urduTranslation,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = 15.sp,
          lineHeight = 27.sp,
          textDirection = TextDirection.Rtl,
          textAlign = TextAlign.Right,
          color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(4.dp))

      // 4. English Translation
      Text(
        text = supplication.englishTranslation,
        style = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 18.sp
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 5, 6, 7, 8: Book/Author, Volume/Chapter/Ref, Scholarly Grading & Evaluator, and Context
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Text(
            text = "📚 Book & Author: ${supplication.originalBookAndAuthor}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              fontSize = 10.sp
            )
          )
          Text(
            text = "📖 Reference: ${supplication.volumeChapterAndReference}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              fontSize = 10.sp
            )
          )
          Text(
            text = "⚖️ Scholarly Grading & Evaluator: ${supplication.scholarlyGradingAndEvaluator}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              fontSize = 10.sp
            )
          )
          Text(
            text = "🕰 Occasion & Context: ${supplication.contextAndRecommendedOccasion}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 9.5.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        OutlinedButton(
          onClick = {
            val fullShare = "${supplication.titleEnglish}\n${supplication.arabicText}\n\n" +
              "Transliteration: ${supplication.transliteration}\n\n" +
              "Urdu: ${supplication.urduTranslation}\n\n" +
              "English: ${supplication.englishTranslation}\n\n" +
              "Source: ${supplication.originalBookAndAuthor} (${supplication.volumeChapterAndReference})\n" +
              "Grading: ${supplication.scholarlyGradingAndEvaluator}"
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText(supplication.titleEnglish, fullShare))
            Toast.makeText(context, "Copied with full source & grading reference", Toast.LENGTH_SHORT).show()
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copy with Reference", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Requirement 5: Evidence-Based Shia Practices & Reminders Section.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiaEvidencePracticesSection() {
  var selectedRulingFilter by remember { mutableStateOf<JurisprudentialRulingStatus?>(null) }

  val filteredPractices = remember(selectedRulingFilter) {
    if (selectedRulingFilter == null) {
      ShiaIslamicKnowledgeRepository.shiaPracticesAndReminders
    } else {
      ShiaIslamicKnowledgeRepository.shiaPracticesAndReminders.filter { it.rulingStatus == selectedRulingFilter }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("shia_evidence_practices_section"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "Filter by Jurisprudential Status (Wajib, Mustahabb, Mubah, Disputed):",
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
    )
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      FilterChip(
        selected = selectedRulingFilter == null,
        onClick = { selectedRulingFilter = null },
        label = { Text("All (${ShiaIslamicKnowledgeRepository.shiaPracticesAndReminders.size})", fontSize = 10.sp) }
      )
      JurisprudentialRulingStatus.entries.forEach { status ->
        FilterChip(
          selected = selectedRulingFilter == status,
          onClick = { selectedRulingFilter = if (selectedRulingFilter == status) null else status },
          label = { Text(status.labelEnglish.substringBefore(" ("), fontSize = 10.sp) }
        )
      }
    }

    filteredPractices.forEach { practice ->
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("shia_practice_card_${practice.id}")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = when (practice.rulingStatus) {
                JurisprudentialRulingStatus.WAJIB -> EmeraldPrimary
                JurisprudentialRulingStatus.MUSTAHABB -> GoldPrimary.copy(alpha = 0.25f)
                JurisprudentialRulingStatus.MUBAH -> MaterialTheme.colorScheme.primaryContainer
                JurisprudentialRulingStatus.DISPUTED_OR_CUSTOM -> Color(0xFFB33A2B).copy(alpha = 0.18f)
              }
            ) {
              Text(
                text = "${practice.rulingStatus.labelEnglish} • ${practice.rulingStatus.labelUrdu}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = if (practice.rulingStatus == JurisprudentialRulingStatus.WAJIB) Color.White else EmeraldPrimary,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = practice.titleEnglish,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
          )
          Text(
            text = practice.titleUrdu,
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 14.sp,
              lineHeight = 24.sp,
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              textDirection = TextDirection.Rtl,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = practice.primaryArabicEvidenceSnippet,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = AmiriFontFamily,
                fontSize = 19.sp,
                lineHeight = 34.sp,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Right,
                color = EmeraldPrimary
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = practice.clearExplanationEnglish,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = practice.clearExplanationUrdu,
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 14.sp,
              lineHeight = 25.sp,
              textDirection = TextDirection.Rtl,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(8.dp),
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = "📖 Hadith & Primary Text: ${practice.primaryHadithBookAndReference}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = EmeraldPrimary)
              )
              Text(
                text = "⚖️ Jurisprudential Source: ${practice.jurisprudentialSourceAndRulingNumber}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp)
              )
              Text(
                text = "✔ Grading & Evaluation: ${practice.scholarlyGradingAndNotes}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = GoldPrimary)
              )
              Text(
                text = "🤝 Comparative & Shared Note: ${practice.comparativeAndSharedContextNote}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Requirement 6: Shia Learning Section & Devotional Calendar.
 */
@Composable
fun ShiaLearningAndCalendarSection(
  onOpenNooraniQaidaPdf: () -> Unit = {},
  onOpenLearnIslamScreen: () -> Unit = {}
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("shia_learning_and_calendar_section"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Shared Quranic Arabic & Noorani Qaida Banner
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = Color(0xFF0A4D3C),
      border = BorderStroke(1.dp, GoldPrimary),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          text = "Shared Foundation: Qur'anic Arabic, Noorani Qaida & Tajweed",
          style = MaterialTheme.typography.titleSmall.copy(
            color = GoldPrimary,
            fontWeight = FontWeight.ExtraBold
          )
        )
        Text(
          text = "Qur'anic Arabic, letter articulation (Makharij), Harakat, Madd, and Tajweed are 100% identical for all Muslims. We do not create artificial differences where Islamic teaching is shared.",
          style = MaterialTheme.typography.labelSmall.copy(
            color = Color.White,
            lineHeight = 16.sp
          )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = onOpenNooraniQaidaPdf,
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF073B2E)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Open 53-Page HD Qaida", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
          }
          OutlinedButton(
            onClick = onOpenLearnIslamScreen,
            border = BorderStroke(1.dp, GoldPrimary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Open 17 Audio Lessons", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Shia Learning Modules
    ShiaIslamicKnowledgeRepository.shiaLearningModules.forEach { mod ->
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Module ${mod.moduleNumber}: ${mod.titleEnglish}",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldPrimary
            )
          )
          Text(
            text = mod.titleUrdu,
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 14.sp,
              lineHeight = 24.sp,
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              textDirection = TextDirection.Rtl,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldPrimary.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "✔ ${mod.sharedWithAllMuslimsBanner}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                fontSize = 10.sp
              ),
              modifier = Modifier.padding(8.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = mod.jafariSpecificJurisprudenceExplanation,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = mod.urduExplanation,
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 14.sp,
              lineHeight = 25.sp,
              textDirection = TextDirection.Rtl,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "📚 Primary Sources: ${mod.primarySourcesCited}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              fontSize = 9.5.sp
            )
          )
        }
      }
    }

    // Shia Devotional Calendar
    Text(
      text = "Shia Devotional Calendar & Documented Occasions",
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.ExtraBold,
        color = EmeraldPrimary
      )
    )
    ShiaIslamicKnowledgeRepository.shiaDevotionalCalendar.forEach { occ ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "${occ.hijriMonthAndDay} • ${occ.rulingStatus.labelEnglish}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = GoldPrimary
            )
          )
          Text(
            text = occ.titleEnglish,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = occ.titleUrdu,
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 13.sp,
              textDirection = TextDirection.Rtl,
              textAlign = TextAlign.Right,
              color = EmeraldPrimary
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = occ.historicalAndDevotionalSummary,
            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.sp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = occ.recommendedVerifiedPractices,
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 15.sp
            )
          )
          Text(
            text = "Source: ${occ.primarySourceReference}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              fontSize = 9.5.sp
            )
          )
        }
      }
    }
  }
}

/**
 * Requirement 7: Verified APIs, Hadith Databases & Official Jurisprudential References Registry.
 */
@Composable
fun ShiaVerifiedSourcesRegistrySection() {
  val context = LocalContext.current

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("shia_verified_sources_registry_section"),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = "13 Evaluated Primary APIs, Hadith Databases & Official Jurisprudential References",
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.ExtraBold,
        color = EmeraldPrimary
      )
    )
    ShiaIslamicKnowledgeRepository.shiaVerifiedSourceLinks.forEach { src ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            runCatching {
              context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(src.officialUrl)))
            }
          }
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = src.category,
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.5.sp
              )
            )
            Text(
              text = src.title,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = src.usageAndVerificationRole,
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
              )
            )
            Text(
              text = src.officialUrl,
              style = MaterialTheme.typography.labelSmall.copy(
                color = EmeraldPrimary,
                fontSize = 9.5.sp
              )
            )
          }
          Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open Official Source",
            tint = EmeraldPrimary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

/**
 * Controls how the separated Sunni Homescreen Section and Shia Homescreen Section are displayed on HomeScreen:
 * - BOTH_SEPARATED: Displays the dedicated Sunni Homescreen Section followed by the dedicated Shia Homescreen Section, clearly separated.
 * - SUNNI_SECTION_ONLY: Focuses exclusively on the Sunni Homescreen Section.
 * - SHIA_SECTION_ONLY: Focuses exclusively on the Shia Homescreen Section.
 */
enum class HomeTraditionSectionMode(val titleEnglish: String, val titleUrdu: String) {
  BOTH_SEPARATED("Both Separated Sections", "سنی اور شیعہ دونوں الگ الگ سیکشن"),
  SUNNI_SECTION_ONLY("Sunni Home Section", "صرف اہلِ سنت ہوم سیکشن"),
  SHIA_SECTION_ONLY("Shia Home Section", "صرف فقہِ جعفری (شیعہ) ہوم سیکشن")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeTraditionSectionSelectorBar(
  selectedMode: HomeTraditionSectionMode,
  onSelectMode: (HomeTraditionSectionMode) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("home_tradition_section_selector_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ),
    border = BorderStroke(1.2.dp, GoldPrimary.copy(alpha = 0.85f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "ہوم اسکرین سیکشن کا انتخاب: اہلِ سنت اور فقہِ جعفری کے الگ الگ شعبے",
        style = MaterialTheme.typography.bodySmall.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = 14.sp,
          lineHeight = 24.sp,
          fontWeight = FontWeight.Bold,
          color = GoldPrimary,
          textDirection = TextDirection.Rtl,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Text(
        text = "Separated Sunni Homescreen Section & Shia Homescreen Section",
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          color = EmeraldPrimary,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        HomeTraditionSectionMode.entries.forEach { mode ->
          val isSelected = selectedMode == mode
          FilterChip(
            selected = isSelected,
            onClick = { onSelectMode(mode) },
            label = {
              Text(
                text = mode.titleEnglish,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = if (mode == HomeTraditionSectionMode.SHIA_SECTION_ONLY) {
                GoldPrimary
              } else {
                EmeraldPrimary
              },
              selectedLabelColor = if (mode == HomeTraditionSectionMode.SHIA_SECTION_ONLY) {
                Color.Black
              } else {
                Color.White
              }
            ),
            modifier = Modifier.testTag("home_section_mode_${mode.name.lowercase()}")
          )
        }
      }
    }
  }
}

/**
 * Prominent Section Header & Quick Navigation Banner for the Dedicated SUNNI HOMESCREEN SECTION.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SunniHomescreenSectionHeaderBanner(
  onNavigateToQuran: () -> Unit,
  onNavigateToHadith: () -> Unit,
  onNavigateToDailyDuas: () -> Unit,
  onNavigateToSehriIftar: () -> Unit,
  onNavigateToLearnIslam: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("sunni_homescreen_section_header"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF073B2E)
    ),
    border = BorderStroke(1.8.dp, GoldPrimary)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = GoldPrimary.copy(alpha = 0.22f)
      ) {
        Text(
          text = "SECTION 1 • SUNNI HOMESCREEN SECTION (AHLUS SUNNAH WAL-JAMA'AH)",
          style = MaterialTheme.typography.labelSmall.copy(
            color = GoldPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
          ),
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "اہلِ سنت و الجماعت ہوم سیکشن: اوقاتِ نماز و افطار، مسنون دعائیں، جمعہ سنتیں اور صحاحِ ستہ احادیث",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = UrduNastaliqFontFamily,
          color = GoldPrimary,
          fontSize = 15.sp,
          lineHeight = 26.sp,
          fontWeight = FontWeight.Bold,
          textDirection = TextDirection.Rtl,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Text(
        text = "Sunni Prayer Timings, Masnoon Duas, Jumu'ah Sunnah & Sahih Hadith",
        style = MaterialTheme.typography.titleMedium.copy(
          color = Color.White,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Dedicated Sunni Homescreen section powered by Sunni astronomical & API prayer calculations (Hanafi/Shafi'i), Masnoon Ramadan & daily supplications, calculated Jumu'ah reminders, and Sihah Sittah Hadith collections.",
        style = MaterialTheme.typography.labelSmall.copy(
          color = Color(0xFFD8F3DC),
          textAlign = TextAlign.Center,
          lineHeight = 15.sp
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))
      val sunniQuickLinks = listOf(
        Triple("📖 Sunni Quran & 8 Tafsirs", "sunni_home_quick_quran", onNavigateToQuran),
        Triple("📚 Sunni Hadith (Sihah Sittah)", "sunni_home_quick_hadith", onNavigateToHadith),
        Triple("🤲 Masnoon Daily Duas", "sunni_home_quick_duas", onNavigateToDailyDuas),
        Triple("🌙 Sunni Sehri & Iftar", "sunni_home_quick_sehri_iftar", onNavigateToSehriIftar),
        Triple("🎓 Sunni Namaz & Wudu", "sunni_home_quick_learn", onNavigateToLearnIslam)
      )
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        sunniQuickLinks.forEach { (label, tag, action) ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.65f)),
            modifier = Modifier
              .clickable { action() }
              .testTag(tag)
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
              ),
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Prominent Section Header & Quick Navigation Banner for the Dedicated SHIA HOMESCREEN SECTION.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiaHomescreenSectionHeaderBanner(
  onNavigateToQuran: () -> Unit,
  onNavigateToHadith: () -> Unit,
  onNavigateToDailyDuas: () -> Unit,
  onNavigateToSehriIftar: () -> Unit,
  onNavigateToLearnIslam: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("shia_homescreen_section_header"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF103742)
    ),
    border = BorderStroke(1.8.dp, GoldPrimary)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = GoldPrimary.copy(alpha = 0.24f)
      ) {
        Text(
          text = "SECTION 2 • SHIA HOMESCREEN SECTION (FIQH AL-JA'FARI & AHL AL-BAYT AS)",
          style = MaterialTheme.typography.labelSmall.copy(
            color = GoldPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
          ),
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "فقہِ جعفری (شیعہ اثنا عشری) ہوم سیکشن: اوقاتِ نماز و افطار، صحیفہ سجادیہ و مفاتیح الجنان، احکام اور کتبِ اربعہ",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = UrduNastaliqFontFamily,
          color = GoldPrimary,
          fontSize = 15.sp,
          lineHeight = 26.sp,
          fontWeight = FontWeight.Bold,
          textDirection = TextDirection.Rtl,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Text(
        text = "Shia (Ja'fari) Prayer Timings, Sehri/Iftari, Duas, Practices & Ahl al-Bayt Hadith",
        style = MaterialTheme.typography.titleMedium.copy(
          color = Color.White,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Dedicated Shia Homescreen section powered by the independent Ja'fari astronomical & AlAdhan Method 0 engine (eastern redness Maghrib/Iftari), Shia Dua library, evidence-based rulings, and Al-Kutub al-Arba'ah narrations.",
        style = MaterialTheme.typography.labelSmall.copy(
          color = Color(0xFFE0F2F1),
          textAlign = TextAlign.Center,
          lineHeight = 15.sp
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))
      val shiaQuickLinks = listOf(
        Triple("📖 Shia Translated Quran", "shia_home_quick_quran", onNavigateToQuran),
        Triple("📚 Al-Kafi & Four Books", "shia_home_quick_hadith", onNavigateToHadith),
        Triple("🤲 Shia Duas (Kumayl, Nudba)", "shia_home_quick_duas", onNavigateToDailyDuas),
        Triple("🌙 Shia Sehri & Iftari", "shia_home_quick_sehri_iftar", onNavigateToSehriIftar),
        Triple("🎓 Ja'fari Wudu & Namaz", "shia_home_quick_learn", onNavigateToLearnIslam)
      )
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        shiaQuickLinks.forEach { (label, tag, action) ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.65f)),
            modifier = Modifier
              .clickable { action() }
              .testTag(tag)
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
              ),
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Standalone Shia (Ja'fari) Daily Prayer & Fasting Timings Card inside the Shia Homescreen Section,
 * giving the Shia Homescreen Section its own full daily prayer schedule card parallel to the Sunni Salah card.
 */
@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ShiaStandalonePrayerTimingsCard(
  sunniConfig: PrayerAndAlarmConfig,
  onOpenSehriIftarScreen: () -> Unit,
  onOpenQiblaCompass: () -> Unit = {},
  alarmPlaybackState: com.example.ui.prayer.FajrAlarmPlaybackState = com.example.ui.prayer.FajrAlarmPlaybackState(),
  onUpdateConfig: (PrayerAndAlarmConfig) -> Unit = {},
  onStopActiveAlarm: () -> Unit = {},
  onSnoozeActiveAlarm: (Int) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val shiaMuezzins = remember { com.example.data.prayer.MuezzinAudioOption.shiaEntries }
  var showShiaAzansGallery by remember { mutableStateOf(false) }
  var showShiaNamazReferences by remember { mutableStateOf(true) }
  var showShiaSetupSheet by remember { mutableStateOf(false) }
  val activeShiaMuezzin = remember(sunniConfig.selectedShiaMuezzinId, sunniConfig.selectedMuezzinId) {
    shiaMuezzins.find { it.id == sunniConfig.selectedShiaMuezzinId }
      ?: shiaMuezzins.find { it.id == sunniConfig.selectedMuezzinId }
      ?: com.example.data.prayer.MuezzinAudioOption.SHIA_KARBALA_NAJAF
  }
  var shiaConfig by remember {
    mutableStateOf(ShiaJafariPrayerAndFastingEngine.loadShiaConfig(context))
  }
  var nowMillis by remember {
    androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis())
  }
  LaunchedEffect(Unit) {
    while (true) {
      kotlinx.coroutines.delay(1000L)
      nowMillis = System.currentTimeMillis()
    }
  }
  val shiaSchedule = remember(
    sunniConfig.city,
    sunniConfig.latitude,
    sunniConfig.longitude,
    sunniConfig.timeZoneId,
    shiaConfig
  ) {
    ShiaJafariPrayerAndFastingEngine.calculateShiaSchedule(
      locationName = sunniConfig.city,
      country = sunniConfig.country,
      latitude = sunniConfig.latitude,
      longitude = sunniConfig.longitude,
      timeZoneId = sunniConfig.timeZoneId,
      shiaConfig = shiaConfig
    )
  }
  val shiaNextPrayer = remember(shiaSchedule, nowMillis) {
    ShiaJafariPrayerAndFastingEngine.computeNextShiaPrayerStatus(shiaSchedule, nowMillis)
  }
  val shiaFajrWakeUpTime24 = remember(shiaSchedule.fajrTime, sunniConfig.fajrWakeUpOffsetMinutes) {
    PrayerTimeCalculator.calculateWakeUpTimeBeforeFajr(shiaSchedule.fajrTime, sunniConfig.fajrWakeUpOffsetMinutes)
  }
  val hwAlarmStatus = remember(sunniConfig.alarmVolumePercent, nowMillis / 1000L) {
    com.example.ui.prayer.AzanAudioController.getHardwareAlarmVolumeStatus(context)
  }
  val openAlarmVolumeSettings: () -> Unit = {
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
      context.startActivity(Intent(android.provider.Settings.ACTION_SOUND_SETTINGS))
    } catch (_: Exception) {
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    // Active Low-Glare Dark Adhan / Fajr Alarm Playback & Continuous Vibration Banner
    if (alarmPlaybackState.isActive || alarmPlaybackState.isContinuousVibrationActive) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 14.dp)
          .testTag("shia_active_fajr_alarm_banner"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF101915)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = alarmPlaybackState.prayerOrAlarmTitle.ifBlank { "Offline Pure Vocal Shia Adhan Alarm" },
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF3E9D2)
              ),
              modifier = Modifier.weight(1f)
            )
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
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                  )
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "حَيَّ عَلَىٰ خَيْرِ الْعَمَلِ • قُمْ إِلَى صَلَاةِ الْفَجْرِ",
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = AmiriFontFamily,
              fontSize = 20.sp,
              color = GoldPrimary,
              textDirection = TextDirection.Rtl,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onStopActiveAlarm,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC94A38), contentColor = Color.White),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Stop Alarm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
              onClick = { onSnoozeActiveAlarm(5) },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Snooze 5m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
            }
          }
        }
      }
    }

    ElevatedCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("shia_standalone_prayer_timings_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Shia (Ja'fari) Daily Salah, Azan & Fajr Alarm",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPrimary
              )
            )
            Text(
              text = "${shiaSchedule.locationName}, ${shiaSchedule.country} • ${shiaSchedule.gregorianReadable} • ${shiaSchedule.hijriReadable}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GoldPrimary.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, GoldPrimary),
              modifier = Modifier.clickable { showShiaSetupSheet = true }
            ) {
              Text(
                text = shiaConfig.method.shortLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            IconButton(
              onClick = { showShiaSetupSheet = true },
              modifier = Modifier
                .size(32.dp)
                .testTag("shia_open_setup_sheet_icon_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Shia Salah & Azan Setup",
                tint = EmeraldPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Next Shia Prayer Live Banner
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF103742),
          border = BorderStroke(1.2.dp, GoldPrimary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Next Ja'fari Prayer: ${shiaNextPrayer.nextPrayerName}",
                style = MaterialTheme.typography.titleSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.ExtraBold
                )
              )
              Text(
                text = "Adhan at ${PrayerTimeCalculator.format24hTo12h(shiaNextPrayer.nextPrayerTime24)} (${shiaNextPrayer.nextPrayerTime24}) • Active: ${shiaNextPrayer.currentActivePrayer}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFFE0F2F1)
                )
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "-${shiaNextPrayer.countdownFormatted}",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = GoldPrimary,
                  fontWeight = FontWeight.ExtraBold
                )
              )
              Text(
                text = "Live Countdown",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFFE0F2F1),
                  fontSize = 10.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Method Switcher Chips
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          ShiaJafariCalculationMethod.entries.forEach { method ->
            FilterChip(
              selected = shiaConfig.method == method,
              onClick = {
                val updated = shiaConfig.copy(method = method)
                shiaConfig = updated
                ShiaJafariPrayerAndFastingEngine.saveShiaConfig(context, updated)
              },
              label = { Text(method.shortLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Five Obligatory Ja'fari Prayers with Per-Prayer Azan Notification Mode Toggle + Fasting/Midnight rows
        val shiaFivePrayersWithModes = listOf(
          Triple("Fajr", shiaSchedule.fajrTime, sunniConfig.fajrAzanMode),
          Triple("Dhuhr", shiaSchedule.dhuhrTime, sunniConfig.dhuhrAzanMode),
          Triple("Asr", shiaSchedule.asrTime, sunniConfig.asrAzanMode),
          Triple("Maghrib", shiaSchedule.maghribTime, sunniConfig.maghribAzanMode),
          Triple("Isha", shiaSchedule.ishaTime, sunniConfig.ishaAzanMode)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          shiaFivePrayersWithModes.forEach { (prayerName, time24, mode) ->
            val isNext = shiaNextPrayer.nextPrayerName.contains(prayerName, ignoreCase = true)
            val noteText = when (prayerName) {
              "Fajr" -> "2 Rak'at FARZ/WAJIB (+ 2 Sunnat/Nafilah Before) • Dawn ${shiaConfig.method.fajrAngle}°"
              "Dhuhr" -> "4 Rak'at FARZ/WAJIB (+ 8 Sunnat/Nafilah Before) • Shar'i Zawal"
              "Asr" -> "4 Rak'at FARZ/WAJIB (+ 8 Sunnat/Nafilah Before) • Valid after Dhuhr; Fadilah"
              "Maghrib" -> "3 Rak'at FARZ/WAJIB (+ 4 Sunnat/Nafilah After) • Eastern Redness (${shiaConfig.method.maghribRednessAngle}°)"
              else -> "4 Rak'at FARZ/WAJIB (+ 2 Sitting Nafilah Al-Watirah) • Fadilah (${shiaConfig.method.ishaAngle}°)"
            }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isNext) {
                GoldPrimary.copy(alpha = 0.14f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("shia_prayer_row_${prayerName.lowercase()}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = prayerName,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isNext) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                      ),
                      modifier = Modifier.width(68.dp)
                    )
                    Text(
                      text = "${PrayerTimeCalculator.format24hTo12h(time24)} ($time24)",
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    )
                  }
                  Text(
                    text = noteText,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 9.5.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  )
                }

                // Per-Prayer Azan Mode Toggle (Full Vocal Azan -> Short Vocal Takbir -> Silent -> Disabled)
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (mode != com.example.data.prayer.AzanNotificationMode.OFF) {
                    EmeraldPrimary.copy(alpha = 0.14f)
                  } else {
                    MaterialTheme.colorScheme.surface
                  },
                  modifier = Modifier
                    .clickable {
                      val nextMode = when (mode) {
                        com.example.data.prayer.AzanNotificationMode.FULL_AZAN -> com.example.data.prayer.AzanNotificationMode.SHORT_TONE
                        com.example.data.prayer.AzanNotificationMode.SHORT_TONE -> com.example.data.prayer.AzanNotificationMode.SILENT
                        com.example.data.prayer.AzanNotificationMode.SILENT -> com.example.data.prayer.AzanNotificationMode.OFF
                        com.example.data.prayer.AzanNotificationMode.OFF -> com.example.data.prayer.AzanNotificationMode.FULL_AZAN
                      }
                      val updated = when (prayerName) {
                        "Fajr" -> sunniConfig.copy(fajrAzanMode = nextMode)
                        "Dhuhr" -> sunniConfig.copy(dhuhrAzanMode = nextMode)
                        "Asr" -> sunniConfig.copy(asrAzanMode = nextMode)
                        "Maghrib" -> sunniConfig.copy(maghribAzanMode = nextMode)
                        else -> sunniConfig.copy(ishaAzanMode = nextMode)
                      }
                      onUpdateConfig(updated)
                    }
                    .testTag("shia_azan_toggle_${prayerName.lowercase()}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.NotificationsActive,
                      contentDescription = null,
                      tint = if (mode == com.example.data.prayer.AzanNotificationMode.OFF) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      } else {
                        EmeraldPrimary
                      },
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = mode.label,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.5.sp,
                        color = if (mode == com.example.data.prayer.AzanNotificationMode.OFF) {
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

        Spacer(modifier = Modifier.height(6.dp))

        // Additional Astronomical & Fasting Horizons (Sehri Imsak, Sunrise, Disk Sunset, Shia Midnight)
        val extraHorizonPills = listOf(
          "Sehri Ends (Imsak): ${PrayerTimeCalculator.format24hTo12h(shiaSchedule.sehriEndsTime)}",
          "Sunrise: ${PrayerTimeCalculator.format24hTo12h(shiaSchedule.sunriseTime)}",
          "Disk Sunset: ${PrayerTimeCalculator.format24hTo12h(shiaSchedule.astronomicalSunsetTime)}",
          "Shia Iftari: ${PrayerTimeCalculator.format24hTo12h(shiaSchedule.iftarTime)}",
          "Shia Midnight: ${PrayerTimeCalculator.format24hTo12h(shiaSchedule.shiaMidnightTime)}"
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          extraHorizonPills.forEach { pill ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GoldPrimary.copy(alpha = 0.14f)
            ) {
              Text(
                text = pill,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Shia Daily Farz/Wajib (17 Rak'ats), Sunnat/Nafilah Rawatib (34 Rak'ats = 51 Total) & Additional Namaz with Authentic Hadith References
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldPrimary.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("shia_farz_sunnat_additional_namaz_section")
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Daily Farz (Wajib), Sunnat (Nafilah) & Additional Namaz (With Hadith References)",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = "17 Rak'at Farz/Wajib + 34 Rak'at Daily Sunnat/Nafilah (51 Rak'ats Total) + Salat al-Layl & Ghufaylah",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  )
                )
              }
              OutlinedButton(
                onClick = { showShiaNamazReferences = !showShiaNamazReferences },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = if (showShiaNamazReferences) "Hide" else "Show",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            if (showShiaNamazReferences) {
              Spacer(modifier = Modifier.height(8.dp))
              val shiaNamazReferenceRows = listOf(
                Triple(
                  "Fajr (Salat al-Subh): 4 Rak'ats Total",
                  "2 SUNNAT / NAFILAH (Before Fajr) + 2 FARZ / WAJIB (Loud Qira'ah)",
                  "📖 Al-Kafi Vol. 3, Kitab al-Salah, p. 442, Hadith 1 (Sahih from Imam al-Sadiq AS) • Tahdhib al-Ahkam Vol. 2, p. 3 & 133 • Wasa'il al-Shi'a Vol. 4, Bab 1"
                ),
                Triple(
                  "Dhuhr (Salat al-Zuhr): 12 Rak'ats Total",
                  "8 SUNNAT / NAFILAH (Before Dhuhr in 4x2 sets after Zawal) + 4 FARZ / WAJIB",
                  "📖 Al-Kafi Vol. 3, p. 442, Hadith 1 & 2 • Man La Yahduruhu al-Faqih Vol. 1, p. 196 • Wasa'il al-Shi'a Vol. 4, Abwab A'dad al-Fara'id wa Nawafiluha"
                ),
                Triple(
                  "Asr (Salat al-Asr): 12 Rak'ats Total",
                  "8 SUNNAT / NAFILAH (Before Asr in 4x2 sets) + 4 FARZ / WAJIB",
                  "📖 Al-Kafi Vol. 3, p. 442 • Tahdhib al-Ahkam Vol. 2, p. 4 • Wasa'il al-Shi'a Vol. 4, Bab 13 (51 daily Wajib & Nafilah Rak'ats established by the Prophet ﷺ)"
                ),
                Triple(
                  "Maghrib (Salat al-Maghrib): 7 Rak'ats Total",
                  "3 FARZ / WAJIB (After Eastern Redness) + 4 SUNNAT / NAFILAH (After Maghrib in 2x2 sets)",
                  "📖 Al-Kafi Vol. 3, p. 279 & 442 • Tahdhib al-Ahkam Vol. 2, p. 25 & 116 • Man La Yahduruhu al-Faqih Vol. 1, p. 219"
                ),
                Triple(
                  "Isha (Salat al-Isha): 5 (or 6 Sitting) Rak'ats Total",
                  "4 FARZ / WAJIB + 2 Sitting SUNNAT / NAFILAH (Al-Watirah, counted as 1 standing Rak'at)",
                  "📖 Al-Kafi Vol. 3, p. 443, Hadith 4 (Imam al-Baqir & Imam al-Sadiq AS on Al-Watirah after Isha) • Tahdhib al-Ahkam Vol. 2, p. 6"
                ),
                Triple(
                  "Additional Namaz: Salat al-Layl (Namaz-e-Shab / Tahajjud), 11 Rak'ats",
                  "SUNNAT MU'AKKADAH / MUSTAHABB • 8 Nawafil al-Layl (4x2) + 2 Salat al-Shaf' + 1 Salat al-Witr (with 40 Muminin Dua & 70x Istighfar)",
                  "📖 Qur'an 17:79 • Al-Kafi Vol. 3, p. 444–449, Hadith 12 (Imam al-Sadiq AS: 'The honour of a believer lies in Salat al-Layl') • Tahdhib al-Ahkam Vol. 2, p. 120"
                ),
                Triple(
                  "Additional Namaz: Salat al-Ghufaylah, 2 Rak'ats (Between Maghrib & Isha)",
                  "SUNNAT / MUSTAHABB • 1st Rak'ah after Fatiha: Qur'an 21:87 (Dhun-Nun verse); 2nd Rak'ah: Qur'an 6:59 (Wa 'indahu mafatihu al-ghayb)",
                  "📖 Misbah al-Mutahajjid of Shaykh al-Tusi, p. 95 • Wasa'il al-Shi'a Vol. 8, Abwab Baqiyyat al-Salawat al-Mandubah, p. 120 (Narrated from Imam al-Sadiq AS)"
                ),
                Triple(
                  "Additional Namaz: Salat Ja'far al-Tayyar (Salat al-Tasbih), 4 Rak'ats (2x2)",
                  "SUNNAT / MUSTAHABB (Especial Merit on Friday) • Contains 300x Tasbihat al-Arba'ah across 4 Rak'ats for forgiveness of sins",
                  "📖 Al-Kafi Vol. 3, Kitab al-Salah, p. 465, Hadith 1 (Prophet Muhammad ﷺ's gift to Ja'far ibn Abi Talib RA upon his return from Abyssinia) • Man La Yahduruhu al-Faqih Vol. 1, p. 348"
                )
              )

              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                shiaNamazReferenceRows.forEach { (title, statusLine, refLine) ->
                  val isAdditional = title.startsWith("Additional")
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAdditional) GoldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = title,
                          style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                          ),
                          modifier = Modifier.weight(1f)
                        )
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = if (isAdditional) GoldPrimary.copy(alpha = 0.25f) else EmeraldPrimary.copy(alpha = 0.15f)
                        ) {
                          Text(
                            text = if (isAdditional) "SUNNAT / NAFILAH" else "FARZ + SUNNAT",
                            style = MaterialTheme.typography.labelSmall.copy(
                              fontWeight = FontWeight.ExtraBold,
                              fontSize = 9.sp,
                              color = if (isAdditional) Color(0xFF102018) else EmeraldPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                          )
                        }
                      }
                      Text(
                        text = statusLine,
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                      )
                      Text(
                        text = refLine,
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontSize = 9.5.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          lineHeight = 13.5.sp
                        )
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Special 15-Minute Fajr Wake-Up, Salat al-Layl Alarm, Volume/Vibration & 6 Azans Section (Equal to Sunni Screen)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("shia_special_fajr_alarm_section")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Special Fajr & Salat al-Layl Wake-Up Alarm (15-Min Cap)",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = "Scheduled for ${PrayerTimeCalculator.format24hTo12h(shiaFajrWakeUpTime24)} (${sunniConfig.fajrWakeUpOffsetMinutes} min before Ja'fari Fajr)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Switch(
                checked = sunniConfig.fajrWakeUpAlarmEnabled,
                onCheckedChange = { onUpdateConfig(sunniConfig.copy(fajrWakeUpAlarmEnabled = it)) },
                modifier = Modifier.testTag("shia_fajr_wakeup_alarm_switch")
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Authentic Ja'fari Adhan Phrase & Hadith Reference
            Text(
              text = "حَيَّ عَلَىٰ خَيْرِ الْعَمَلِ • أَشْهَدُ أَنَّ عَلِيًّا وَلِيُّ اللَّهِ",
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = AmiriFontFamily,
                fontSize = 20.sp,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Right,
                color = EmeraldPrimary
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldPrimary.copy(alpha = 0.1f),
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
            ) {
              Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(
                  text = "📖 Authentic Ja'fari Adhan & Fajr Reference (Al-Kutub al-Arba'ah):",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    fontSize = 10.sp
                  )
                )
                Text(
                  text = "Al-Kafi Vol. 3, Kitab al-Salah, Bab al-Adhan wa al-Iqamah, p. 302, Hadith 1 & 2 (Sahih) • Man La Yahduruhu al-Faqih Vol. 1, p. 283 • Tahdhib al-Ahkam Vol. 2, p. 60: 'Hayya ala khayril-amal' (Hasten to the best of deeds) is an obligatory (Wajib) part of Adhan & Iqamah; the third testimony ('Ashhadu anna Aliyyan Waliyyullah') is recited as a Mustahabb (recommended) compliment.",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.5.sp,
                    lineHeight = 13.5.sp
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Selectable Minutes Before Fajr (60, 45, 30, 20, 15, 10)
            Text(
              text = "Wake-Up Time Before Ja'fari Fajr (For Salat al-Layl & Fajr):",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              listOf(60, 45, 30, 20, 15, 10).forEach { mins ->
                FilterChip(
                  selected = sunniConfig.fajrWakeUpOffsetMinutes == mins,
                  onClick = { onUpdateConfig(sunniConfig.copy(fajrWakeUpOffsetMinutes = mins)) },
                  label = { Text("${mins}m", fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("shia_fajr_offset_chip_$mins")
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Salat al-Layl (Namaz-e-Shab) Wake-Up Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Salat al-Layl (Namaz-e-Shab) Window: ${PrayerTimeCalculator.format24hTo12h(shiaSchedule.shiaMidnightTime)} → ${PrayerTimeCalculator.format24hTo12h(shiaSchedule.fajrTime)}",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                  text = "11 Rak'ats Mustahabb Night Prayer (8 Nawafil + 2 Shaf' + 1 Witr) before Subh-e-Sadiq.",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
              Switch(
                checked = sunniConfig.tahajjudWakeUpEnabled,
                onCheckedChange = { onUpdateConfig(sunniConfig.copy(tahajjudWakeUpEnabled = it)) },
                modifier = Modifier.testTag("shia_salat_al_layl_wakeup_switch")
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-Time Hardware Alarm Volume Sync, Continuous Vibration & 6 Azans (Ka'bah, Madinah & 4 Shia Shrines)
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = EmeraldPrimary.copy(alpha = 0.09f),
              border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("shia_official_azans_panel")
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Adhan: ${activeShiaMuezzin.title}",
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary
                      )
                    )
                    Text(
                      text = "Offline Vocal • Alarm Audio: ${hwAlarmStatus.currentPercent}% • Vib: ${if (sunniConfig.vibrationEnabled && sunniConfig.continuousVibration) "ON" else "OFF"}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                      )
                    )
                  }
                  Button(
                    onClick = openAlarmVolumeSettings,
                    colors = ButtonDefaults.buttonColors(
                      containerColor = GoldPrimary,
                      contentColor = Color(0xFF102820)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("Increase Vol", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))

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
                      selected = sunniConfig.alarmVolumePercent == volPct,
                      onClick = {
                        onUpdateConfig(
                          sunniConfig.copy(
                            alarmVolumePercent = volPct,
                            boostSystemAlarmStream = true
                          )
                        )
                        if (volPct > hwAlarmStatus.currentPercent) {
                          openAlarmVolumeSettings()
                        } else {
                          Toast.makeText(context, "Saved Shia Adhan Alarm Volume: $label", Toast.LENGTH_SHORT).show()
                        }
                      },
                      label = { Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                      )
                    )
                  }

                  FilterChip(
                    selected = sunniConfig.vibrationEnabled && sunniConfig.continuousVibration,
                    onClick = {
                      val next = !(sunniConfig.vibrationEnabled && sunniConfig.continuousVibration)
                      onUpdateConfig(
                        sunniConfig.copy(
                          vibrationEnabled = next,
                          continuousVibration = next
                        )
                      )
                    },
                    label = {
                      Text(
                        text = if (sunniConfig.vibrationEnabled && sunniConfig.continuousVibration) "Continuous Vib: ON" else "Continuous Vib: OFF",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                      )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = GoldPrimary,
                      selectedLabelColor = Color(0xFF101915)
                    )
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Expandable 6 Azans Gallery (Holy Ka'bah Makkah, Masjid an-Nabawi Madinah, Karbala & Najaf, Mashhad, Qom, Najaf al-Ashraf)
                OutlinedButton(
                  onClick = { showShiaAzansGallery = !showShiaAzansGallery },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_shia_official_azans_btn")
                ) {
                  Text(
                    text = if (showShiaAzansGallery) {
                      "Hide ${shiaMuezzins.size} Adhans (Ka'bah, Madinah & Official Shia Shrines)"
                    } else {
                      "Preview, Test & Keep ${shiaMuezzins.size} Adhans (Ka'bah, Madinah, Karbala, Najaf, Mashhad & Qom)"
                    },
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                if (showShiaAzansGallery) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("shia_official_azans_list")
                  ) {
                    shiaMuezzins.forEach { muezzin ->
                      val isSelected = activeShiaMuezzin.id == muezzin.id
                      val isPlayingThis = alarmPlaybackState.isActive && alarmPlaybackState.activeMuezzinId == muezzin.id
                      val isHaramainMandatory = muezzin.id == com.example.data.prayer.MuezzinAudioOption.MAKKAH_HARAM.id ||
                        muezzin.id == com.example.data.prayer.MuezzinAudioOption.MADINAH_NABAWI.id
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) {
                          EmeraldPrimary.copy(alpha = 0.16f)
                        } else {
                          MaterialTheme.colorScheme.surface
                        },
                        border = if (isSelected) BorderStroke(1.dp, GoldPrimary) else null,
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable {
                            onUpdateConfig(
                              sunniConfig.copy(
                                selectedShiaMuezzinId = muezzin.id,
                                selectedMuezzinId = muezzin.id
                              )
                            )
                            Toast.makeText(context, "Saved '${muezzin.title}' as Active Adhan", Toast.LENGTH_SHORT).show()
                          }
                          .testTag("shia_muezzin_card_${muezzin.id}")
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
                            Text(
                              text = muezzin.title,
                              style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                              ),
                              modifier = Modifier.weight(1f)
                            )
                            Surface(
                              shape = RoundedCornerShape(6.dp),
                              color = if (isHaramainMandatory) GoldPrimary.copy(alpha = 0.22f) else EmeraldPrimary.copy(alpha = 0.14f)
                            ) {
                              Text(
                                text = if (isHaramainMandatory) "KA'BAH & MADINAH (ALL MUSLIMS)" else "OFFICIAL JA'FARI ADHAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                  fontWeight = FontWeight.ExtraBold,
                                  fontSize = 8.5.sp,
                                  color = if (isHaramainMandatory) Color(0xFF102018) else EmeraldPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                              )
                            }
                          }
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
                              fontSize = 9.5.sp,
                              color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                          )
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
                                  com.example.ui.prayer.AzanAudioController.previewMuezzinAzan(context, muezzin)
                                }
                              },
                              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                              shape = RoundedCornerShape(8.dp),
                              modifier = Modifier
                                .weight(1f)
                                .testTag("shia_preview_muezzin_btn_${muezzin.id}")
                            ) {
                              Text(
                                text = if (isPlayingThis && alarmPlaybackState.isPreviewMode) "Stop Preview" else "Preview",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                              )
                            }
                            OutlinedButton(
                              onClick = {
                                if (isPlayingThis && !alarmPlaybackState.isPreviewMode) {
                                  onStopActiveAlarm()
                                } else {
                                  onUpdateConfig(
                                    sunniConfig.copy(
                                      selectedShiaMuezzinId = muezzin.id,
                                      selectedMuezzinId = muezzin.id
                                    )
                                  )
                                  com.example.ui.prayer.AzanAudioController.testMuezzinAzan(context, muezzin)
                                }
                              },
                              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                              shape = RoundedCornerShape(8.dp),
                              modifier = Modifier
                                .weight(1f)
                                .testTag("shia_test_muezzin_btn_${muezzin.id}")
                            ) {
                              Text(
                                text = if (isPlayingThis && !alarmPlaybackState.isPreviewMode) "Stop Test" else "Test Alarm",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                              )
                            }
                            Button(
                              onClick = {
                                onUpdateConfig(
                                  sunniConfig.copy(
                                    selectedShiaMuezzinId = muezzin.id,
                                    selectedMuezzinId = muezzin.id
                                  )
                                )
                                Toast.makeText(context, "Saved '${muezzin.title}' as Active Adhan", Toast.LENGTH_SHORT).show()
                              },
                              colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) EmeraldPrimary else GoldPrimary,
                                contentColor = if (isSelected) Color.White else Color(0xFF101915)
                              ),
                              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                              shape = RoundedCornerShape(8.dp),
                              modifier = Modifier
                                .weight(1f)
                                .testTag("shia_keep_muezzin_btn_${muezzin.id}")
                            ) {
                              Text(
                                text = if (isSelected) "Saved ★" else "Keep ★",
                                fontSize = 10.5.sp,
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

            // Action Buttons (Matching Sunni Screen: Test Offline Adhan, Test Continuous Vibration, Location & 6 Azans Setup)
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  com.example.ui.prayer.AzanAudioController.testMuezzinAzan(context, activeShiaMuezzin)
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("shia_test_fajr_wakeup_alarm_btn")
              ) {
                Text("Test Offline Adhan (${sunniConfig.alarmVolumePercent}%)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
              }

              OutlinedButton(
                onClick = {
                  if (alarmPlaybackState.isContinuousVibrationActive) {
                    com.example.ui.prayer.AzanAudioController.stopContinuousVibration(context)
                    Toast.makeText(context, "Continuous vibration stopped", Toast.LENGTH_SHORT).show()
                  } else {
                    com.example.ui.prayer.AzanAudioController.startContinuousVibrationTest(context)
                    Toast.makeText(context, "Continuous alarm vibration started (tap again to stop)", Toast.LENGTH_SHORT).show()
                  }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("shia_test_continuous_vibration_btn")
              ) {
                Text(
                  text = if (alarmPlaybackState.isContinuousVibrationActive) "Stop Vibration" else "Test Continuous Vibration",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              OutlinedButton(
                onClick = { showShiaSetupSheet = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("shia_configure_azan_location_btn")
              ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Location, ${shiaMuezzins.size} Azans & Setup", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onOpenSehriIftarScreen,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Shia 30-Day Sehri & Iftari", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = onOpenQiblaCompass,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = EmeraldPrimary,
              contentColor = Color.White
            ),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Qibla Compass", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // Full Shia Salah, Ja'fari Method, Location & 6 Azans Setup Bottom Sheet (Equal to Sunni Setup Sheet)
  if (showShiaSetupSheet) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customCityInput by remember(sunniConfig.city) { mutableStateOf(sunniConfig.city) }
    var customDistrictInput by remember(sunniConfig.districtOrRegion) { mutableStateOf(sunniConfig.districtOrRegion) }
    var customLatInput by remember(sunniConfig.latitude) { mutableStateOf(sunniConfig.latitude.toString()) }
    var customLonInput by remember(sunniConfig.longitude) { mutableStateOf(sunniConfig.longitude.toString()) }
    var customOffsetInput by remember(sunniConfig.fajrWakeUpOffsetMinutes) {
      mutableStateOf(sunniConfig.fajrWakeUpOffsetMinutes.toString())
    }

    androidx.compose.material3.ModalBottomSheet(
      onDismissRequest = { showShiaSetupSheet = false },
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp)
      ) {
        Text(
          text = "Shia (Ja'fari) Salah, Azan & Fajr Alarm Setup",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )
        )
        Text(
          text = "Configure City/Coordinates, Ja'fari Calculation Convention (Qom, Tehran, Najaf), 6 Official Adhans (Ka'bah, Madinah & Holy Shrines), and Fajr/Salat al-Layl Alarms.",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "1. Select City / District Preset (Kashmir, India & Worldwide):",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PrayerTimeCalculator.locationPresets.forEach { preset ->
            FilterChip(
              selected = sunniConfig.city.equals(preset.city, ignoreCase = true),
              onClick = {
                customCityInput = preset.city
                customDistrictInput = preset.districtOrRegion
                customLatInput = preset.latitude.toString()
                customLonInput = preset.longitude.toString()
                onUpdateConfig(
                  sunniConfig.copy(
                    city = preset.city,
                    districtOrRegion = preset.districtOrRegion,
                    country = preset.country,
                    latitude = preset.latitude,
                    longitude = preset.longitude,
                    timeZoneId = preset.timeZoneId,
                    useAutoLocation = false
                  )
                )
              },
              label = { Text("${preset.city} (${preset.country})", fontSize = 10.5.sp) },
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
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = customLonInput,
            onValueChange = { customLonInput = it },
            label = { Text("Longitude") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          Button(
            onClick = {
              val lat = customLatInput.toDoubleOrNull()
              val lon = customLonInput.toDoubleOrNull()
              if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
                onUpdateConfig(
                  sunniConfig.copy(
                    city = customCityInput.ifBlank { "Custom Location" },
                    districtOrRegion = customDistrictInput,
                    latitude = lat,
                    longitude = lon
                  )
                )
                Toast.makeText(context, "Custom coordinates saved", Toast.LENGTH_SHORT).show()
              }
            }
          ) {
            Text("Apply")
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        Text(
          text = "2. Ja'fari Calculation Convention & Maghrib Eastern Redness:",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ShiaJafariCalculationMethod.entries.forEach { method ->
            FilterChip(
              selected = shiaConfig.method == method,
              onClick = {
                val updated = shiaConfig.copy(method = method)
                shiaConfig = updated
                ShiaJafariPrayerAndFastingEngine.saveShiaConfig(context, updated)
              },
              label = { Text(method.shortLabel, fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        Text(
          text = "3. Select Active Adhan (Holy Ka'bah, Madinah & Official Shia Shrines):",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        shiaMuezzins.forEach { muezzin ->
          val isSelected = activeShiaMuezzin.id == muezzin.id
          val isPlayingThis = alarmPlaybackState.isActive && alarmPlaybackState.activeMuezzinId == muezzin.id
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onUpdateConfig(
                  sunniConfig.copy(
                    selectedShiaMuezzinId = muezzin.id,
                    selectedMuezzinId = muezzin.id
                  )
                )
              }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isSelected) "★ ${muezzin.title}" else muezzin.title,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                )
              )
              Text(
                text = "${muezzin.originMasjid} • ${muezzin.maqamOrStyle}",
                style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontSize = 9.5.sp)
              )
            }
            OutlinedButton(
              onClick = {
                if (isPlayingThis) {
                  onStopActiveAlarm()
                } else {
                  com.example.ui.prayer.AzanAudioController.previewMuezzinAzan(context, muezzin)
                }
              },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(if (isPlayingThis) "Stop" else "Preview", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(
              onClick = {
                onUpdateConfig(
                  sunniConfig.copy(
                    selectedShiaMuezzinId = muezzin.id,
                    selectedMuezzinId = muezzin.id
                  )
                )
                Toast.makeText(context, "Saved '${muezzin.title}'", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) EmeraldPrimary else GoldPrimary,
                contentColor = if (isSelected) Color.White else Color.Black
              ),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(if (isSelected) "Saved ★" else "Keep", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
            modifier = Modifier.weight(1f)
          )
          Button(
            onClick = {
              val mins = customOffsetInput.toIntOrNull()
              if (mins != null && mins in 1..180) {
                onUpdateConfig(sunniConfig.copy(fajrWakeUpOffsetMinutes = mins))
                Toast.makeText(context, "Ja'fari Fajr wake-up set to $mins minutes before Fajr", Toast.LENGTH_SHORT).show()
              }
            }
          ) {
            Text("Set")
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

/**
 * Dedicated Ahl al-Bayt (AS) Daily Hadith Card inside the Shia Homescreen Section,
 * parallel to the Sunni Prophetic Hadith card in the Sunni Homescreen Section.
 */
@Composable
fun ShiaDailyAhlulBaytHadithCard(
  onNavigateToHadith: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val ahlulBaytHadiths = remember {
    com.example.data.model.ReflectionDataProvider.filterItems(
      category = com.example.data.model.ReflectionCategory.AHLUL_BAYT_WISDOM,
      attributionFilter = com.example.data.model.WisdomAttributionStatus.VERIFIED_ATTRIBUTION
    )
  }
  var index by remember { mutableIntStateOf(0) }
  val safeIndex = if (ahlulBaytHadiths.isNotEmpty()) index.coerceIn(0, ahlulBaytHadiths.lastIndex) else 0
  val currentItem = ahlulBaytHadiths.getOrNull(safeIndex) ?: return

  ElevatedCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("shia_daily_hadith_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Ahl al-Bayt (AS) Hadith & Tradition (Al-Kutub al-Arba'ah)",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldPrimary
          ),
          modifier = Modifier.weight(1f)
        )
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = EmeraldPrimary.copy(alpha = 0.14f)
        ) {
          Text(
            text = currentItem.authenticityGrading,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              fontSize = 9.5.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = currentItem.sourceReference,
        style = MaterialTheme.typography.labelMedium.copy(
          color = GoldPrimary,
          fontWeight = FontWeight.Bold
        )
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Imam / Transmitter: ${currentItem.speaker}",
        style = MaterialTheme.typography.labelSmall.copy(
          color = MaterialTheme.colorScheme.secondary,
          fontWeight = FontWeight.SemiBold
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = currentItem.arabicOrOriginalText,
        style = MaterialTheme.typography.bodyLarge.copy(
          fontFamily = AmiriFontFamily,
          fontSize = 20.sp,
          lineHeight = 36.sp,
          textDirection = TextDirection.Rtl,
          textAlign = TextAlign.Right
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = currentItem.translationEnglish,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 13.5.sp,
          lineHeight = 20.sp
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = currentItem.translationUrdu,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = 14.5.sp,
          lineHeight = 28.sp,
          textDirection = TextDirection.Rtl,
          textAlign = TextAlign.Right,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onNavigateToHadith,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Explore Al-Kafi & Shia Hadith", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedButton(
            onClick = { index = (safeIndex + 1) % ahlulBaytHadiths.size },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Next (${safeIndex + 1}/${ahlulBaytHadiths.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText(
                "Ahl al-Bayt Hadith",
                "${currentItem.arabicOrOriginalText}\n\n${currentItem.translationEnglish}\n\n${currentItem.translationUrdu}\n\nNarrator: ${currentItem.speaker} (${currentItem.sourceReference})"
              )
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "Hadith copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Hadith", modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}

