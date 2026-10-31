package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.DailyDuaItem
import com.example.data.model.DailyDuasDataProvider
import com.example.data.prayer.CalculationMethodOption
import com.example.data.prayer.JuridicalSchool
import com.example.data.prayer.PrayerAndAlarmConfig
import com.example.data.prayer.PrayerLocationPreset
import com.example.data.prayer.SehriIftarTimetableEngine
import com.example.data.prayer.SehriIftarTimetableResult
import com.example.data.prayer.TimetableScopeMode
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GREGORIAN_MONTH_NAMES = listOf(
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SehriIftarTimetableScreen(
  prayerConfig: PrayerAndAlarmConfig,
  timetableResult: SehriIftarTimetableResult?,
  isLoading: Boolean,
  selectedScopeMode: TimetableScopeMode,
  selectedGregorianYear: Int,
  selectedHijriYear: Int,
  selectedGregorianMonth: Int,
  onSelectScopeMode: (TimetableScopeMode) -> Unit,
  onSelectGregorianYear: (Int) -> Unit,
  onSelectHijriYear: (Int) -> Unit,
  onSelectGregorianMonth: (Int) -> Unit,
  onUpdatePrayerConfig: (PrayerAndAlarmConfig) -> Unit,
  onRefreshTimetable: (Boolean) -> Unit,
  onApplyTrueMapLocationAndCacheMonth: (Double, Double, String, String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val todayIso = remember {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
  }

  val countries: List<String> = remember { SehriIftarTimetableEngine.getCountriesList() }
  var selectedCountry by remember(prayerConfig.country) {
    mutableStateOf(
      if (countries.contains(prayerConfig.country)) prayerConfig.country else countries.firstOrNull() ?: "India"
    )
  }
  val citiesInCountry: List<PrayerLocationPreset> = remember(selectedCountry) {
    SehriIftarTimetableEngine.getCitiesForCountry(selectedCountry)
  }

  var showTrueLocationMapCard by remember { mutableStateOf(false) }
  var showMethodAndAdjustmentPanel by remember { mutableStateOf(false) }
  var showRamadanDuasSection by remember { mutableStateOf(false) }
  var locationStatusMessage by remember { mutableStateOf<String?>(null) }

  // Map picker coordinates state
  var mapPinLatitude by remember(prayerConfig.latitude) { mutableDoubleStateOf(prayerConfig.latitude) }
  var mapPinLongitude by remember(prayerConfig.longitude) { mutableDoubleStateOf(prayerConfig.longitude) }
  var customLatText by remember(prayerConfig.latitude) {
    mutableStateOf(String.format(Locale.US, "%.4f", prayerConfig.latitude))
  }
  var customLonText by remember(prayerConfig.longitude) {
    mutableStateOf(String.format(Locale.US, "%.4f", prayerConfig.longitude))
  }

  val dualRamadanHijriYears = remember(selectedGregorianYear) {
    SehriIftarTimetableEngine.getHijriRamadanYearsForGregorianYear(selectedGregorianYear)
  }

  val activeMethodSpec: CalculationMethodOption = remember(prayerConfig.calculationMethodId) {
    SehriIftarTimetableEngine.findMethodById(prayerConfig.calculationMethodId)
  }
  val activeSchool: JuridicalSchool = remember(prayerConfig.asrMadhhab) {
    JuridicalSchool.fromId(prayerConfig.asrMadhhab)
  }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (fineGranted || coarseGranted) {
      val resolvedLoc = fetchLastKnownDeviceLocation(context)
      if (resolvedLoc != null) {
        val lat = resolvedLoc.latitude
        val lon = resolvedLoc.longitude
        mapPinLatitude = lat
        mapPinLongitude = lon
        customLatText = String.format(Locale.US, "%.4f", lat)
        customLonText = String.format(Locale.US, "%.4f", lon)
        val nearest = SehriIftarTimetableEngine.resolveNearestCityForCoordinates(lat, lon)
        locationStatusMessage =
          "True GPS Location acquired: ${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lon)} (Nearest: ${nearest.city}, ${nearest.country}). Fetching & caching monthly Sehri/Iftar calendar..."
        showTrueLocationMapCard = true
        onApplyTrueMapLocationAndCacheMonth(
          lat,
          lon,
          nearest.city,
          nearest.country,
          nearest.timeZoneId
        )
      } else {
        showTrueLocationMapCard = true
        locationStatusMessage =
          "Location permission granted. Device GPS signal is warming up — tap any point on the interactive map below to set your exact coordinates and cache the monthly Sehri/Iftar calendar."
      }
    } else {
      showTrueLocationMapCard = true
      locationStatusMessage =
        "Location permission was not granted. You can still select your exact coordinates on the interactive map or choose any city worldwide below."
    }
  }

  val entries = timetableResult?.days ?: emptyList()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp)
      .testTag("sehri_iftar_timetable_screen"),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // 1. Header & Mandatory Authority Verification Disclaimer
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sehri_iftar_header_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = Color(0xFF102820)
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "رمضان المبارك • Yearly & Ramadan Sehri / Iftar Timetable",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary
                )
              )
              Text(
                text = "${prayerConfig.city}, ${prayerConfig.country} • ${String.format(Locale.US, "%.4f", prayerConfig.latitude)}°, ${String.format(Locale.US, "%.4f", prayerConfig.longitude)}° (${prayerConfig.timeZoneId})",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFFE0D6C3)
                )
              )
            }
            IconButton(
              onClick = { onRefreshTimetable(true) },
              modifier = Modifier.testTag("sehri_iftar_refresh_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Timetable from AlAdhan API",
                tint = GoldPrimary
              )
            }
          }

          // Active Calculation Method, School & Adjustment Summary Chips
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldPrimary.copy(alpha = 0.35f)
            ) {
              Text(
                text = "Method: ${activeMethodSpec.name} (Fajr ${activeMethodSpec.fajrAngle}° / Isha ${activeMethodSpec.ishaAngle}°)",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GoldPrimary.copy(alpha = 0.22f)
            ) {
              Text(
                text = "School: ${activeSchool.label}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GoldPrimary,
                  fontWeight = FontWeight.Bold
                )
              )
            }
            if (prayerConfig.sehriAdjustmentMinutes != 0 || prayerConfig.iftarAdjustmentMinutes != 0) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2E4F42)
              ) {
                Text(
                  text = "Manual Adj: Sehri ${formatSignedMin(prayerConfig.sehriAdjustmentMinutes)}m • Iftar ${formatSignedMin(prayerConfig.iftarAdjustmentMinutes)}m",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                  )
                )
              }
            }
          }

          // Action Row: True GPS + Interactive Map Location, Method & Adjustments, Special Ramadan Duas
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                val hasFine = ContextCompat.checkSelfPermission(
                  context,
                  Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(
                  context,
                  Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasFine || hasCoarse) {
                  val loc = fetchLastKnownDeviceLocation(context)
                  showTrueLocationMapCard = true
                  if (loc != null) {
                    mapPinLatitude = loc.latitude
                    mapPinLongitude = loc.longitude
                    customLatText = String.format(Locale.US, "%.4f", loc.latitude)
                    customLonText = String.format(Locale.US, "%.4f", loc.longitude)
                    val nearest = SehriIftarTimetableEngine.resolveNearestCityForCoordinates(loc.latitude, loc.longitude)
                    locationStatusMessage =
                      "True GPS coordinates (${String.format(Locale.US, "%.4f", loc.latitude)}, ${String.format(Locale.US, "%.4f", loc.longitude)}) resolved to ${nearest.city}, ${nearest.country}. Caching monthly timetable..."
                    onApplyTrueMapLocationAndCacheMonth(
                      loc.latitude,
                      loc.longitude,
                      nearest.city,
                      nearest.country,
                      nearest.timeZoneId
                    )
                  } else {
                    locationStatusMessage =
                      "Interactive True Location Map opened. Tap any location on the map or enter exact coordinates to fetch and cache the monthly Sehri/Iftar calendar."
                  }
                } else {
                  locationPermissionLauncher.launch(
                    arrayOf(
                      Manifest.permission.ACCESS_FINE_LOCATION,
                      Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                  )
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color(0xFF102018)
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("true_location_map_access_btn")
            ) {
              Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "True Location & Map",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            OutlinedButton(
              onClick = { showMethodAndAdjustmentPanel = !showMethodAndAdjustmentPanel },
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f)),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("toggle_method_adjustment_panel_btn")
            ) {
              Icon(Icons.Default.Tune, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (showMethodAndAdjustmentPanel) "Hide Method & Minute Adj." else "Hanafi/Shafi & Minute Adj.",
                fontSize = 12.sp,
                color = GoldPrimary,
                fontWeight = FontWeight.SemiBold
              )
            }

            OutlinedButton(
              onClick = { showRamadanDuasSection = !showRamadanDuasSection },
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, Color(0xFFE0D6C3).copy(alpha = 0.6f)),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("toggle_ramadan_duas_in_timetable_btn")
            ) {
              Text(
                text = if (showRamadanDuasSection) "Hide Ramadan Duas" else "🌙 Special Ramadan Duas",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          // Mandatory Disclaimer
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF1B3A2E),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("sehri_iftar_authority_disclaimer_banner")
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Disclaimer",
                tint = GoldPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = timetableResult?.authorityVerificationDisclaimer
                  ?: SehriIftarTimetableEngine.AUTHORITY_DISCLAIMER,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFFEAE2D3),
                  lineHeight = 16.sp
                )
              )
            }
          }
        }
      }
    }

    // 2. Interactive True Location & Map Picker Card (when opened or after GPS request)
    if (showTrueLocationMapCard) {
      item {
        TrueLocationInteractiveMapCard(
          latitude = mapPinLatitude,
          longitude = mapPinLongitude,
          customLatText = customLatText,
          customLonText = customLonText,
          statusMessage = locationStatusMessage,
          onCustomLatChanged = { txt ->
            customLatText = txt
            txt.toDoubleOrNull()?.let { v ->
              if (v in -90.0..90.0) mapPinLatitude = v
            }
          },
          onCustomLonChanged = { txt ->
            customLonText = txt
            txt.toDoubleOrNull()?.let { v ->
              if (v in -180.0..180.0) mapPinLongitude = v
            }
          },
          onPinMoved = { newLat, newLon ->
            mapPinLatitude = newLat
            mapPinLongitude = newLon
            customLatText = String.format(Locale.US, "%.4f", newLat)
            customLonText = String.format(Locale.US, "%.4f", newLon)
          },
          onRequestDeviceGps = {
            locationPermissionLauncher.launch(
              arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
              )
            )
          },
          onApplyAndCacheMonth = {
            val nearest = SehriIftarTimetableEngine.resolveNearestCityForCoordinates(
              mapPinLatitude,
              mapPinLongitude
            )
            selectedCountry = nearest.country
            locationStatusMessage =
              "Applied exact map coordinates (${String.format(Locale.US, "%.4f", mapPinLatitude)}, ${String.format(Locale.US, "%.4f", mapPinLongitude)}) • ${nearest.city}, ${nearest.country} (${nearest.timeZoneId}) and cached monthly Sehri/Iftar calendar."
            onApplyTrueMapLocationAndCacheMonth(
              mapPinLatitude,
              mapPinLongitude,
              nearest.city,
              nearest.country,
              nearest.timeZoneId
            )
          },
          onClose = { showTrueLocationMapCard = false }
        )
      }
    }

    // 3. Special Ramadan Duas Section (collapsible inside timetable screen as well as on Home Screen)
    if (showRamadanDuasSection) {
      item {
        SpecialRamadanDuasShowcaseCard(
          duas = DailyDuasDataProvider.ramadanSpecialDuas,
          modifier = Modifier.testTag("timetable_ramadan_duas_card")
        )
      }
    }

    // 4. Hanafi / Shafi Toggle, Calculation Method & Manual Minute Adjustments Panel
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hanafi_shafi_and_adjustments_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Juridical School (Hanafi / Shafi'i) & Calculation Settings",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )

          // Hanafi vs Shafi'i Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            JuridicalSchool.entries.forEach { school ->
              val isSelected = prayerConfig.asrMadhhab == school.schoolId
              FilterChip(
                selected = isSelected,
                onClick = {
                  onUpdatePrayerConfig(
                    prayerConfig.copy(asrMadhhab = school.schoolId)
                  )
                },
                label = {
                  Text(
                    text = school.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("school_chip_${school.name.lowercase()}")
              )
            }
          }

          // Manual Minute Adjustments for Sehri (Fajr) & Iftar (Maghrib)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Sehri Ending (Fajr) Adjustment",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Precautionary / local offset (${formatSignedMin(prayerConfig.sehriAdjustmentMinutes)} min)",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              OutlinedButton(
                onClick = {
                  val next = (prayerConfig.sehriAdjustmentMinutes - 1).coerceIn(-30, 30)
                  onUpdatePrayerConfig(prayerConfig.copy(sehriAdjustmentMinutes = next))
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                  .size(width = 36.dp, height = 32.dp)
                  .testTag("sehri_adj_minus_btn")
              ) {
                Text("-", fontWeight = FontWeight.Bold)
              }
              Text(
                text = "${formatSignedMin(prayerConfig.sehriAdjustmentMinutes)}m",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                ),
                modifier = Modifier
                  .width(48.dp)
                  .testTag("sehri_adj_value_text"),
                textAlign = TextAlign.Center
              )
              OutlinedButton(
                onClick = {
                  val next = (prayerConfig.sehriAdjustmentMinutes + 1).coerceIn(-30, 30)
                  onUpdatePrayerConfig(prayerConfig.copy(sehriAdjustmentMinutes = next))
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                  .size(width = 36.dp, height = 32.dp)
                  .testTag("sehri_adj_plus_btn")
              ) {
                Icon(Icons.Default.Add, contentDescription = "Increase Sehri minutes", modifier = Modifier.size(14.dp))
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Iftar (Maghrib) Adjustment",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Local horizon / elevation offset (${formatSignedMin(prayerConfig.iftarAdjustmentMinutes)} min)",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              OutlinedButton(
                onClick = {
                  val next = (prayerConfig.iftarAdjustmentMinutes - 1).coerceIn(-30, 30)
                  onUpdatePrayerConfig(prayerConfig.copy(iftarAdjustmentMinutes = next))
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                  .size(width = 36.dp, height = 32.dp)
                  .testTag("iftar_adj_minus_btn")
              ) {
                Text("-", fontWeight = FontWeight.Bold)
              }
              Text(
                text = "${formatSignedMin(prayerConfig.iftarAdjustmentMinutes)}m",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary
                ),
                modifier = Modifier
                  .width(48.dp)
                  .testTag("iftar_adj_value_text"),
                textAlign = TextAlign.Center
              )
              OutlinedButton(
                onClick = {
                  val next = (prayerConfig.iftarAdjustmentMinutes + 1).coerceIn(-30, 30)
                  onUpdatePrayerConfig(prayerConfig.copy(iftarAdjustmentMinutes = next))
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                  .size(width = 36.dp, height = 32.dp)
                  .testTag("iftar_adj_plus_btn")
              ) {
                Icon(Icons.Default.Add, contentDescription = "Increase Iftar minutes", modifier = Modifier.size(14.dp))
              }
            }
          }

          if (prayerConfig.sehriAdjustmentMinutes != 0 || prayerConfig.iftarAdjustmentMinutes != 0) {
            OutlinedButton(
              onClick = {
                onUpdatePrayerConfig(
                  prayerConfig.copy(sehriAdjustmentMinutes = 0, iftarAdjustmentMinutes = 0)
                )
              },
              modifier = Modifier.testTag("reset_minute_adjustments_btn")
            ) {
              Text("Reset Manual Minute Adjustments to 0", fontSize = 11.sp)
            }
          }

          // Full Calculation Method Selector (Always clearly shown or expandable)
          Text(
            text = "Calculation Method: ${activeMethodSpec.name} (${activeMethodSpec.regionDescription})",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPrimary
            ),
            modifier = Modifier.testTag("active_calculation_method_label")
          )

          if (showMethodAndAdjustmentPanel) {
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              SehriIftarTimetableEngine.allCalculationMethods.forEach { method ->
                val isSelected = prayerConfig.calculationMethodId == method.id
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    onUpdatePrayerConfig(prayerConfig.copy(calculationMethodId = method.id))
                  },
                  label = {
                    Text(
                      text = "${method.name} (${method.fajrAngle}°/${method.ishaAngle}°)",
                      fontSize = 11.sp
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("calc_method_chip_${method.id}")
                )
              }
            }
          }
        }
      }
    }

    // 5. Country, City, Scope Mode, Gregorian Year & Hijri Ramadan Year Selectors
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("country_city_year_selector_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "1. Select Country & Worldwide City",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )

          // Country Horizontal Chip Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            countries.forEach { country ->
              val isSelected = selectedCountry.equals(country, ignoreCase = true)
              FilterChip(
                selected = isSelected,
                onClick = {
                  selectedCountry = country
                  val firstCity = SehriIftarTimetableEngine.getCitiesForCountry(country).firstOrNull()
                  if (firstCity != null) {
                    onUpdatePrayerConfig(
                      prayerConfig.copy(
                        city = firstCity.city,
                        districtOrRegion = firstCity.districtOrRegion,
                        country = firstCity.country,
                        latitude = firstCity.latitude,
                        longitude = firstCity.longitude,
                        timeZoneId = firstCity.timeZoneId,
                        calculationMethodId = firstCity.defaultMethodId,
                        asrMadhhab = firstCity.defaultAsrMadhhab
                      )
                    )
                  }
                },
                label = { Text(country, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("country_chip_${country.lowercase().replace(" ", "_")}")
              )
            }
          }

          // Cities in selected Country
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            citiesInCountry.forEach { cityPreset ->
              val isSelected = prayerConfig.city.equals(cityPreset.city, ignoreCase = true)
              FilterChip(
                selected = isSelected,
                onClick = {
                  mapPinLatitude = cityPreset.latitude
                  mapPinLongitude = cityPreset.longitude
                  customLatText = String.format(Locale.US, "%.4f", cityPreset.latitude)
                  customLonText = String.format(Locale.US, "%.4f", cityPreset.longitude)
                  onUpdatePrayerConfig(
                    prayerConfig.copy(
                      city = cityPreset.city,
                      districtOrRegion = cityPreset.districtOrRegion,
                      country = cityPreset.country,
                      latitude = cityPreset.latitude,
                      longitude = cityPreset.longitude,
                      timeZoneId = cityPreset.timeZoneId,
                      calculationMethodId = cityPreset.defaultMethodId,
                      asrMadhhab = cityPreset.defaultAsrMadhhab
                    )
                  )
                },
                label = {
                  Text(
                    text = "${cityPreset.city} (${cityPreset.districtOrRegion})",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = GoldPrimary,
                  selectedLabelColor = Color(0xFF102018)
                ),
                modifier = Modifier.testTag("city_chip_${cityPreset.city.lowercase().replace(" ", "_")}")
              )
            }
          }

          HorizontalDivider()

          // Timetable Mode Selector: Ramadan Month vs Monthly Calendar vs Full Gregorian Year
          Text(
            text = "2. Select Timetable Mode, Gregorian Year & Hijri Ramadan Year",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              TimetableScopeMode.RAMADAN_MONTH to "🌙 Ramadan Timetable (29/30 Days)",
              TimetableScopeMode.GREGORIAN_MONTH to "📅 Monthly Sehri/Iftar Calendar",
              TimetableScopeMode.FULL_GREGORIAN_YEAR to "🗓️ Complete Yearly Timetable (365 Days)"
            ).forEach { (mode, label) ->
              val isSelected = selectedScopeMode == mode
              FilterChip(
                selected = isSelected,
                onClick = { onSelectScopeMode(mode) },
                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("scope_mode_chip_${mode.name.lowercase()}")
              )
            }
          }

          // Gregorian Year Selector (2024..2032, including 2030 dual-Ramadan year)
          Text(
            text = "Gregorian Year (CE):",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            (2024..2032).forEach { gYear ->
              val isSelected = selectedGregorianYear == gYear
              val hasDualRamadan =
                SehriIftarTimetableEngine.getHijriRamadanYearsForGregorianYear(gYear).size > 1
              FilterChip(
                selected = isSelected,
                onClick = { onSelectGregorianYear(gYear) },
                label = {
                  Text(
                    text = if (hasDualRamadan) "$gYear CE (2 Ramadans)" else "$gYear CE",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("gregorian_year_chip_$gYear")
              )
            }
          }

          // Hijri Ramadan Year Selector (1445..1454 AH)
          Text(
            text = "Hijri Ramadan Year (AH):",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            (1445..1454).forEach { hYear ->
              val isSelected = selectedHijriYear == hYear
              FilterChip(
                selected = isSelected,
                onClick = { onSelectHijriYear(hYear) },
                label = {
                  Text(
                    text = "Ramadan $hYear AH",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = GoldPrimary,
                  selectedLabelColor = Color(0xFF102018)
                ),
                modifier = Modifier.testTag("hijri_year_chip_$hYear")
              )
            }
          }

          // Dual-Ramadan Year Alert Banner (e.g., Gregorian year 2030 CE has both 1451 AH and 1452 AH!)
          if (dualRamadanHijriYears.size > 1) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = GoldPrimary.copy(alpha = 0.16f),
              border = BorderStroke(1.dp, GoldPrimary),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("dual_ramadan_year_banner")
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "✨ Dual-Ramadan Gregorian Year ($selectedGregorianYear CE)",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Gregorian year $selectedGregorianYear CE contains TWO holy months of Ramadan: Ramadan ${dualRamadanHijriYears[0]} AH (beginning January $selectedGregorianYear) and Ramadan ${dualRamadanHijriYears[1]} AH (beginning December $selectedGregorianYear). Switch between both below:",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  dualRamadanHijriYears.forEachIndexed { idx, rYear ->
                    Button(
                      onClick = { onSelectHijriYear(rYear) },
                      colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedHijriYear == rYear) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedHijriYear == rYear) Color.White else MaterialTheme.colorScheme.onSurface
                      ),
                      shape = RoundedCornerShape(8.dp),
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                      modifier = Modifier.testTag("dual_ramadan_switch_btn_$rYear")
                    ) {
                      Text(
                        text = if (idx == 0) "1st: Ramadan $rYear AH (Jan)" else "2nd: Ramadan $rYear AH (Dec)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }
            }
          }

          // If Monthly Calendar mode is active, allow choosing Gregorian Month (1..12)
          if (selectedScopeMode == TimetableScopeMode.GREGORIAN_MONTH) {
            Text(
              text = "Gregorian Month ($selectedGregorianYear CE):",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              GREGORIAN_MONTH_NAMES.forEachIndexed { idx, mName ->
                val mNum = idx + 1
                val isSelected = selectedGregorianMonth == mNum
                FilterChip(
                  selected = isSelected,
                  onClick = { onSelectGregorianMonth(mNum) },
                  label = { Text("$mNum. $mName", fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("gregorian_month_chip_$mNum")
                )
              }
            }
          }
        }
      }
    }

    // 6. Status / Offline Cache / API Error Info Banner
    item {
      val result = timetableResult
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("timetable_status_summary_bar")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            val titleText = when (selectedScopeMode) {
              TimetableScopeMode.RAMADAN_MONTH ->
                "Ramadan ${result?.hijriRamadanYear ?: selectedHijriYear} AH (${result?.monthLengthDays ?: entries.size}-Day Month)"
              TimetableScopeMode.GREGORIAN_MONTH ->
                "${GREGORIAN_MONTH_NAMES.getOrElse(selectedGregorianMonth - 1) { "Month" }} $selectedGregorianYear CE (${entries.size} Days)"
              TimetableScopeMode.FULL_GREGORIAN_YEAR ->
                "Complete Year $selectedGregorianYear CE (${entries.size} Days • Ramadan Highlighted)"
            }
            Text(
              text = titleText,
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
            Text(
              text = if (result != null) {
                val srcTag = if (result.isFromOfflineCache) "Cached Offline" else "Live AlAdhan API Synced"
                "$srcTag • ${entries.size} verified days • Zero duplicates"
              } else {
                "Loading Sehri/Iftar timetable..."
              },
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
            if (!result?.apiErrorOrFallbackNotice.isNullOrBlank()) {
              Text(
                text = result?.apiErrorOrFallbackNotice ?: "",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GoldPrimary,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              strokeWidth = 2.5.dp,
              color = EmeraldPrimary
            )
          }
        }
      }
    }

    // 7. Table Column Headers
    item {
      Surface(
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        color = EmeraldPrimary,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sehri_iftar_table_header")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Day",
            modifier = Modifier.width(42.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = "Hijri Date",
            modifier = Modifier.weight(1.1f),
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = "Gregorian Date",
            modifier = Modifier.weight(1.15f),
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = "Sehri Ends\n(Fajr)",
            modifier = Modifier.weight(0.85f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
              color = GoldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = "Iftar\n(Maghrib)",
            modifier = Modifier.weight(0.85f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }

    // 8. All Timetable Rows (29 or 30 Ramadan days, or Monthly / Yearly rows)
    itemsIndexed(
      items = entries,
      key = { _, item -> item.uniqueDayKey }
    ) { index, day ->
      val isToday = day.gregorianDateIso == todayIso
      val rowBg = when {
        isToday -> GoldPrimary.copy(alpha = 0.22f)
        day.isRamadanDay -> if (index % 2 == 0) {
          EmeraldPrimary.copy(alpha = 0.08f)
        } else {
          MaterialTheme.colorScheme.surface
        }
        index % 2 == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = rowBg,
        border = if (isToday) BorderStroke(1.2.dp, GoldPrimary) else null,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sehri_iftar_row_${day.daySequenceNumber}")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Day #
          Box(
            modifier = Modifier
              .size(30.dp)
              .clip(CircleShape)
              .background(
                if (day.isRamadanDay) EmeraldPrimary.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.surfaceVariant
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${day.daySequenceNumber}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (day.isRamadanDay) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
              )
            )
          }
          Spacer(modifier = Modifier.width(12.dp))

          // Hijri Date
          Column(modifier = Modifier.weight(1.1f)) {
            Text(
              text = day.hijriDateFormatted,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (day.isRamadanDay) FontWeight.Bold else FontWeight.Medium,
                color = if (day.isRamadanDay) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
              )
            )
            if (day.hijriMonthNameAr.isNotBlank()) {
              Text(
                text = day.hijriMonthNameAr,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = ScheherazadeFontFamily,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
          }

          // Gregorian Date
          Column(modifier = Modifier.weight(1.15f)) {
            Text(
              text = day.gregorianReadable,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
              )
            )
            Text(
              text = if (isToday) "${day.gregorianDateIso} • TODAY" else day.gregorianDateIso,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = if (isToday) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
              )
            )
          }

          // Sehri Ending (Fajr)
          Column(
            modifier = Modifier.weight(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = day.adjustedSehriFajr24,
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
            Text(
              text = "Imsak ${day.rawImsak24}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }

          // Iftar (Maghrib)
          Column(
            modifier = Modifier.weight(0.85f),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = day.adjustedIftarMaghrib24,
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
              )
            )
            Text(
              text = "Sunrise ${day.rawSunrise24}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }
      }
    }
  }
}

/**
 * Interactive True Location & Map Picker Card.
 * Renders an interactive world/coordinate map canvas where users can tap or drag a pin,
 * inspect nearby global cities, request device GPS coordinates, and immediately fetch & cache
 * the monthly & Ramadan Sehri/Iftar timetable for that exact location.
 */
@Composable
fun TrueLocationInteractiveMapCard(
  latitude: Double,
  longitude: Double,
  customLatText: String,
  customLonText: String,
  statusMessage: String?,
  onCustomLatChanged: (String) -> Unit,
  onCustomLonChanged: (String) -> Unit,
  onPinMoved: (Double, Double) -> Unit,
  onRequestDeviceGps: () -> Unit,
  onApplyAndCacheMonth: () -> Unit,
  onClose: () -> Unit
) {
  val nearestCity = remember(latitude, longitude) {
    SehriIftarTimetableEngine.resolveNearestCityForCoordinates(latitude, longitude)
  }

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("true_location_interactive_map_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Explore, contentDescription = null, tint = EmeraldPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "True Location Interactive Map & Monthly Cache",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
            Text(
              text = "Tap or drag on the map to pinpoint your exact location worldwide",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }
        OutlinedButton(
          onClick = onClose,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text("Done", fontSize = 11.sp)
        }
      }

      if (!statusMessage.isNullOrBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = statusMessage,
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.SemiBold
            )
          )
        }
      }

      // Interactive Coordinate Map Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(210.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(
            Brush.verticalGradient(
              listOf(Color(0xFF0B2230), Color(0xFF12352A), Color(0xFF0E241E))
            )
          )
          .testTag("interactive_location_map_canvas")
          .pointerInput(Unit) {
            detectTapGestures { offset ->
              val w = size.width.toFloat().coerceAtLeast(1f)
              val h = size.height.toFloat().coerceAtLeast(1f)
              val newLon = ((offset.x / w) * 360.0) - 180.0
              val newLat = 90.0 - ((offset.y / h) * 180.0)
              onPinMoved(
                newLat.coerceIn(-85.0, 85.0),
                newLon.coerceIn(-180.0, 180.0)
              )
            }
          }
          .pointerInput(Unit) {
            detectDragGestures { change, _ ->
              change.consume()
              val w = size.width.toFloat().coerceAtLeast(1f)
              val h = size.height.toFloat().coerceAtLeast(1f)
              val newLon = ((change.position.x / w) * 360.0) - 180.0
              val newLat = 90.0 - ((change.position.y / h) * 180.0)
              onPinMoved(
                newLat.coerceIn(-85.0, 85.0),
                newLon.coerceIn(-180.0, 180.0)
              )
            }
          }
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height

          // Latitude & Longitude Grid Lines
          val gridColor = Color.White.copy(alpha = 0.14f)
          for (lonStep in -150..150 step 30) {
            val x = ((lonStep + 180f) / 360f) * w
            drawLine(
              color = gridColor,
              start = Offset(x, 0f),
              end = Offset(x, h),
              strokeWidth = 1f
            )
          }
          for (latStep in -60..60 step 30) {
            val y = ((90f - latStep) / 180f) * h
            drawLine(
              color = if (latStep == 0) Color.White.copy(alpha = 0.28f) else gridColor,
              start = Offset(0f, y),
              end = Offset(w, y),
              strokeWidth = if (latStep == 0) 1.5f else 1f
            )
          }

          // Plot Worldwide Cities as subtle reference dots
          SehriIftarTimetableEngine.worldwideCityCatalog.forEach { city ->
            val cx = (((city.longitude + 180.0) / 360.0) * w).toFloat()
            val cy = (((90.0 - city.latitude) / 180.0) * h).toFloat()
            drawCircle(
              color = Color(0xFFA5D6A7).copy(alpha = 0.55f),
              radius = 3.2f,
              center = Offset(cx, cy)
            )
          }

          // Plot Holy Kaaba (Makkah: 21.4225N, 39.8262E)
          val makkahX = (((39.8262 + 180.0) / 360.0) * w).toFloat()
          val makkahY = (((90.0 - 21.4225) / 180.0) * h).toFloat()
          drawCircle(
            color = GoldPrimary,
            radius = 6.5f,
            center = Offset(makkahX, makkahY)
          )

          // Plot Selected Pin
          val pinX = (((longitude.coerceIn(-180.0, 180.0) + 180.0) / 360.0) * w).toFloat()
          val geodesicY = (((90.0 - latitude.coerceIn(-85.0, 85.0)) / 180.0) * h).toFloat()

          // Great-circle direction line from Selected Pin to Makkah
          drawLine(
            color = GoldPrimary.copy(alpha = 0.65f),
            start = Offset(pinX, geodesicY),
            end = Offset(makkahX, makkahY),
            strokeWidth = 2.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
          )

          // Outer pulse & inner target for Selected Pin
          drawCircle(
            color = EmeraldPrimary.copy(alpha = 0.35f),
            radius = 16f,
            center = Offset(pinX, geodesicY)
          )
          drawCircle(
            color = Color.White,
            radius = 7.5f,
            center = Offset(pinX, geodesicY)
          )
          drawCircle(
            color = EmeraldPrimary,
            radius = 5.2f,
            center = Offset(pinX, geodesicY)
          )
        }

        // Overlay Legend
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color.Black.copy(alpha = 0.55f),
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
        ) {
          Text(
            text = "Pin: ${String.format(Locale.US, "%.4f", latitude)}°, ${String.format(Locale.US, "%.4f", longitude)}° • Nearest: ${nearestCity.city}, ${nearestCity.country} (${nearestCity.timeZoneId})",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontSize = 10.sp
            )
          )
        }
      }

      // Coordinate TextFields
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = customLatText,
          onValueChange = onCustomLatChanged,
          label = { Text("Latitude (-90 to 90)") },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("map_latitude_input")
        )
        OutlinedTextField(
          value = customLonText,
          onValueChange = onCustomLonChanged,
          label = { Text("Longitude (-180 to 180)") },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("map_longitude_input")
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onRequestDeviceGps,
          modifier = Modifier
            .weight(1f)
            .testTag("request_gps_from_map_btn")
        ) {
          Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Use Device GPS", fontSize = 12.sp)
        }
        Button(
          onClick = onApplyAndCacheMonth,
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldPrimary,
            contentColor = Color.White
          ),
          modifier = Modifier
            .weight(1.3f)
            .testTag("apply_map_location_and_cache_btn")
        ) {
          Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Apply & Cache Month", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Reusable card showcasing the authentic Special Duas for the Month of Ramadan
 * (Sehri Niyyah, Iftar Dua, 1st/2nd/3rd Ashra Duas, Laylatul Qadr Dua, and Ramadan Hilal Dua).
 * Used on both the Home Screen and the Sehri/Iftar Timetable Screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpecialRamadanDuasShowcaseCard(
  duas: List<DailyDuaItem> = DailyDuasDataProvider.ramadanSpecialDuas,
  onOpenSehriIftarTimetable: (() -> Unit)? = null,
  todaySehriEnds: String? = null,
  todayIftarTime: String? = null,
  cityName: String? = null,
  prayerConfig: PrayerAndAlarmConfig? = null,
  onUpdatePrayerConfig: ((PrayerAndAlarmConfig) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val duaAudioController = remember(context) { com.example.ui.audio.DuaAudioController.getInstance(context) }
  val playbackState by duaAudioController.playbackState.collectAsState()
  var selectedDuaIndex by remember { mutableIntStateOf(0) }
  val activeDua = duas.getOrElse(selectedDuaIndex) { duas.first() }
  val isThisRamadanDuaPlaying = playbackState.isPlaying && playbackState.activeDuaId == activeDua.id

  ElevatedCard(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "🌙 Sehri & Iftari Timings • Fasting Duas",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Prophetic Sunnah Duas & Verified Astronomical Timetable",
            style = MaterialTheme.typography.labelSmall.copy(
              color = GoldPrimary,
              fontWeight = FontWeight.SemiBold
            )
          )
        }
        if (onOpenSehriIftarTimetable != null) {
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = onOpenSehriIftarTimetable,
            colors = ButtonDefaults.buttonColors(
              containerColor = EmeraldPrimary,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("home_open_sehri_iftar_timetable_btn")
          ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Timetable",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }

      // Today's Quick Iftari (First) & Sehri (Second) Summary Strip + Well-Spaced Single-Line Alarm Buttons
      if (!todaySehriEnds.isNullOrBlank() && !todayIftarTime.isNullOrBlank()) {
        val isIftarAlarmOn = prayerConfig?.maghribAzanMode != com.example.data.prayer.AzanNotificationMode.OFF
        val isSehriAlarmOn = prayerConfig?.fajrWakeUpAlarmEnabled ?: true

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF102820),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSehriIftarTimetable?.invoke() }
            .testTag("home_today_sehri_iftar_summary_strip")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // 1. IFTARI FIRST — Responsive Vertical Layout inside Half-Width Box so "Alarm ON" never wraps
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1B3A2E),
                modifier = Modifier
                  .weight(1f)
                  .testTag("iftari_first_timing_box")
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "1. Iftari (Maghrib)",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = GoldPrimary,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = todayIftarTime,
                      style = MaterialTheme.typography.titleLarge.copy(
                        color = GoldPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                      ),
                      maxLines = 1,
                      softWrap = false
                    )
                    if (prayerConfig != null && onUpdatePrayerConfig != null) {
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isIftarAlarmOn) GoldPrimary else Color.White.copy(alpha = 0.14f),
                        modifier = Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .clickable {
                            val nextMode = if (isIftarAlarmOn) {
                              com.example.data.prayer.AzanNotificationMode.OFF
                            } else {
                              com.example.data.prayer.AzanNotificationMode.FULL_AZAN
                            }
                            onUpdatePrayerConfig(prayerConfig.copy(maghribAzanMode = nextMode))
                          }
                          .testTag("iftar_concise_alarm_btn")
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Icon(
                            imageVector = if (isIftarAlarmOn) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (isIftarAlarmOn) Color(0xFF102820) else Color.White,
                            modifier = Modifier.size(12.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = if (isIftarAlarmOn) "Alarm ON" else "Alarm OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIftarAlarmOn) Color(0xFF102820) else Color.White,
                            maxLines = 1,
                            softWrap = false
                          )
                        }
                      }
                    }
                  }
                }
              }

              // 2. SEHRI SECOND — Responsive Vertical Layout inside Half-Width Box so "Alarm ON" never wraps
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1B3A2E),
                modifier = Modifier
                  .weight(1f)
                  .testTag("sehri_second_timing_box")
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "2. Sehri (Fajr)",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = Color(0xFFD8CCB2),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = todaySehriEnds,
                      style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                      ),
                      maxLines = 1,
                      softWrap = false
                    )
                    if (prayerConfig != null && onUpdatePrayerConfig != null) {
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSehriAlarmOn) EmeraldPrimary else Color.White.copy(alpha = 0.14f),
                        modifier = Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .clickable {
                            onUpdatePrayerConfig(
                              prayerConfig.copy(fajrWakeUpAlarmEnabled = !isSehriAlarmOn)
                            )
                          }
                          .testTag("sehri_concise_alarm_btn")
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Icon(
                            imageVector = if (isSehriAlarmOn) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = if (isSehriAlarmOn) "Alarm ON" else "Alarm OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                          )
                        }
                      }
                    }
                  }
                }
              }
            }

            // Clean Location & Method Footer (Without Sectarian Labels)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "📍 ${cityName ?: "Current Location"} • Method #${prayerConfig?.calculationMethodId ?: 1}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFFD8CCB2),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              if (onOpenSehriIftarTimetable != null) {
                Text(
                  text = "Full Calendar →",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  ),
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }
      }

      // Selector Chips for each Ramadan / Fasting Dua (Sehri Dua & Iftari Dua first)
      var isDuaTranslationExpanded by remember { mutableStateOf(false) }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        duas.forEachIndexed { idx, dua ->
          val isSelected = idx == selectedDuaIndex
          FilterChip(
            selected = isSelected,
            onClick = {
              if (selectedDuaIndex == idx) {
                isDuaTranslationExpanded = !isDuaTranslationExpanded
              } else {
                selectedDuaIndex = idx
              }
            },
            label = {
              Text(
                text = dua.title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("ramadan_dua_chip_${dua.id}")
          )
        }
      }

      // Active Sehri / Iftari Dua Card — Compact by default; click to open Transliteration, Urdu & English Translation
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { isDuaTranslationExpanded = !isDuaTranslationExpanded }
          .testTag("active_ramadan_dua_display")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = activeDua.title,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
              Text(
                text = activeDua.occasionBadge,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GoldPrimary,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              val activeDuaHasAudio = activeDua.quranSurahAyah != null ||
                !activeDua.humanAudioUrl.isNullOrBlank() ||
                com.example.ui.audio.DuaAudioController.hasOfflineQaidaAudioAsset(null, activeDua.id)
              if (activeDuaHasAudio) {
                FilledIconButton(
                  onClick = {
                    if (isThisRamadanDuaPlaying) {
                      duaAudioController.stop()
                    } else {
                      duaAudioController.playDuaVocal(
                        dua = activeDua,
                        includeTranslations = false,
                        preferQuranReciterIfAvailable = true
                      )
                    }
                  },
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isThisRamadanDuaPlaying) GoldPrimary else EmeraldPrimary,
                    contentColor = if (isThisRamadanDuaPlaying) Color(0xFF073B2E) else Color.White
                  ),
                  modifier = Modifier
                    .size(32.dp)
                    .testTag("play_ramadan_dua_btn_${activeDua.id}")
                ) {
                  Icon(
                    imageVector = if (isThisRamadanDuaPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                    contentDescription = "Play ${activeDua.title}",
                    modifier = Modifier.size(16.dp)
                  )
                }
              } else {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.testTag("no_audio_ramadan_dua_badge_${activeDua.id}")
                ) {
                  Text(
                    text = "No audio available",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
              IconButton(
                onClick = {
                  val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val text =
                    "${activeDua.title}\n${activeDua.arabicText}\n${activeDua.transliteration}\n${activeDua.englishTranslation}\n${activeDua.urduTranslation}\n(${activeDua.authenticReference})"
                  clip.setPrimaryClip(ClipData.newPlainText(activeDua.title, text))
                  Toast.makeText(context, "Fasting Dua copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy Fasting Dua",
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          // Arabic Text with Scheherazade / Amiri Calligraphy (Compact line height & zero font ascent padding)
          Text(
            text = activeDua.arabicText,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.headlineSmall.copy(
              fontFamily = ScheherazadeFontFamily,
              fontSize = 22.sp,
              lineHeight = 33.sp,
              letterSpacing = 0.sp,
              fontWeight = FontWeight.Bold,
              platformStyle = com.example.ui.theme.CompactPlatformTextStyle,
              lineHeightStyle = com.example.ui.theme.CompactLineHeightStyle,
              textDirection = TextDirection.Rtl
            )
          )

          // Clickable Toggle Bar to Open/Close Transliteration, Urdu & English Translation
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldPrimary.copy(alpha = 0.12f),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("toggle_fasting_dua_translation_btn")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isDuaTranslationExpanded) {
                  "Hide Translation (Transliteration, Urdu & English) ▲"
                } else {
                  "Tap to Open Translation (Transliteration, Urdu & English) ▼"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                  color = EmeraldPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              )
            }
          }

          if (isDuaTranslationExpanded) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("expanded_fasting_dua_translation"),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // Transliteration
              Text(
                text = activeDua.transliteration,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontStyle = FontStyle.Italic,
                  color = EmeraldPrimary,
                  fontWeight = FontWeight.SemiBold,
                  lineHeight = 18.sp
                )
              )

              // Urdu Translation with Nastaliq Font (Compact line height & zero font ascent padding)
              Text(
                text = activeDua.urduTranslation,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 14.sp,
                  lineHeight = 22.sp,
                  platformStyle = com.example.ui.theme.CompactPlatformTextStyle,
                  lineHeightStyle = com.example.ui.theme.CompactLineHeightStyle,
                  textDirection = TextDirection.Rtl
                )
              )

              // English Translation
              Text(
                text = activeDua.englishTranslation,
                style = MaterialTheme.typography.bodySmall.copy(
                  lineHeight = 18.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              )

              // Authentic Reference & Timing
              Text(
                text = "Source: ${activeDua.authenticReference} • ${activeDua.virtueAndTiming}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp,
                  lineHeight = 15.sp
                )
              )
            }
          }
        }
      }
    }
  }
}

private fun formatSignedMin(min: Int): String = if (min >= 0) "+$min" else "$min"

@SuppressLint("MissingPermission")
private fun fetchLastKnownDeviceLocation(context: Context): Location? {
  return try {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return null
    val providers = listOf(
      LocationManager.GPS_PROVIDER,
      LocationManager.NETWORK_PROVIDER,
      LocationManager.PASSIVE_PROVIDER
    )
    var bestLocation: Location? = null
    for (provider in providers) {
      val loc = runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
      if (loc != null) {
        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
          bestLocation = loc
        }
      }
    }
    bestLocation
  } catch (_: Exception) {
    null
  }
}
