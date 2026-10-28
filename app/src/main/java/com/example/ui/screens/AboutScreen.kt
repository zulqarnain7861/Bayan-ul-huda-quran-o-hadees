package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SourceRegistry
import com.example.data.local.VerificationStatus
import com.example.data.prayer.AppThemeMode
import com.example.data.repository.DiagnosticAuditReport
import com.example.ui.audio.Reciter
import com.example.ui.audio.availableReciters
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(
  currentReciter: Reciter,
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  auditReport: DiagnosticAuditReport? = null,
  sourceRegistryList: List<SourceRegistry> = emptyList(),
  onSelectThemeMode: (AppThemeMode) -> Unit = {},
  onRefreshAuditReport: () -> Unit = {},
  onSelectReciter: (Reciter) -> Unit,
  onOpenErrorReport: () -> Unit,
  userEmail: String? = null,
  userName: String? = null,
  isGuestMode: Boolean = false,
  remainingGuestDays: Int = 7,
  onSignOut: () -> Unit = {},
  onClearAudioCache: () -> Unit = {},
  currentAppLanguageCode: String = "en",
  onSelectAppLanguage: (String, Boolean) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val developerName = "Zulqarnain Ahmad Sheikh"
  val developerEmail = "sheikhzulqar@gmail.com"
  val instagramUsername = "@zulqarnainah"
  val instagramUrl = "https://www.instagram.com/zulqarnainah/"
  val facebookMessengerUrl = "https://www.facebook.com/share/1PiLFRT6jv/"
  val isVerifiedDeveloper = !isGuestMode &&
    com.example.data.remote.SecurityIntegrityGuard.isAuthorizedDeveloper(userEmail)
  var showDiagnosticAuditExpanded by remember { mutableStateOf(true) }
  var showLanguagePickerSheet by remember { mutableStateOf(false) }
  val activeLangMeta = remember(currentAppLanguageCode) {
    com.example.ui.localization.AppLocalization.findInterfaceLanguage(currentAppLanguageCode)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top "Support Play Store Publication" & Clickable "Assalamu Alaikum" WhatsApp Card (Single instance at top)
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("top_support_our_app_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Support Bayan al-Huda • Google Play Store Publication",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text(
                text = "We need US$25 for publication: One-time Google Play Console registration fee",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = GoldPrimary
                )
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = GoldPrimary.copy(alpha = 0.18f),
              modifier = Modifier
                .clickable { com.example.ui.components.openWhatsAppSupport(context) }
                .testTag("about_assalamu_alaikum_greeting_btn")
            ) {
              Text(
                text = "السَّلَامُ عَلَيْكُمْ • Assalamu Alaikum",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
          }

          Text(
            text = "Community support helps cover the one-time US$25 Google Play Console registration fee so we can publish Bayan al-Huda on the Google Play Store and enable more people worldwide to benefit from verified Quran, Hadith, and Salah tools.",
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
          )

          Text(
            text = "100% Free & Ad-Free Pledge: Bayan al-Huda is completely free of cost and contains zero advertisements. Supporting the Play Store registration is completely optional—tap below to connect on WhatsApp.",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPrimary,
              lineHeight = 16.sp
            )
          )

          Button(
            onClick = { com.example.ui.components.openWhatsAppSupport(context) },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
              containerColor = EmeraldPrimary,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("support_our_app_whatsapp_button")
          ) {
            Text(
              text = "Assalamu Alaikum — Support $25 Play Store Publication on WhatsApp",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }
    }

    // App Header Card
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MenuBook,
              contentDescription = null,
              tint = GoldPrimary,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Bayan al-Huda",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Text(
            text = "بَيَانُ الهُدَى • Quran, Hadith & Salah Library",
            style = MaterialTheme.typography.titleSmall.copy(
              fontFamily = ScheherazadeFontFamily,
              fontSize = 18.sp,
              color = GoldPrimary,
              fontWeight = FontWeight.Bold
            )
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Version 1.1.0 (Production Release)",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }
    }

    // All 8 Persistent Appearance Themes Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("appearance_theme_selector_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Palette, contentDescription = null, tint = EmeraldPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Appearance & Reading Themes (8 Modes)",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Choose System Default, Light, Dark, Pure Black (AMOLED), Warm Paper, Sepia, Green, or Blue. Your theme persists across restarts.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
          Spacer(modifier = Modifier.height(10.dp))

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            AppThemeMode.entries.forEach { mode ->
              FilterChip(
                selected = themeMode == mode,
                onClick = { onSelectThemeMode(mode) },
                label = { Text(mode.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("theme_chip_${mode.name.lowercase()}")
              )
            }
          }
        }
      }
    }

    // Priority 11 — Storage Cleaner & Audio Cache Management
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("storage_cleaner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Storage Cleaner",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Manage cached audio and temporary files",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Bayan al-Huda caches streamed Quran audio for smooth offline playback. You can clear this cache at any time to free up device space.",
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              onClearAudioCache()
              Toast.makeText(context, "Audio cache cleared successfully", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("clear_audio_cache_btn")
          ) {
            Text("Clear Cached Quran Audio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    // Source & Scholarly Integrity Audit Dashboard (Raw API URLs protected behind verified developer session)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("source_and_api_audit_dashboard_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.FactCheck, contentDescription = null, tint = EmeraldPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = if (isVerifiedDeveloper) {
                    "Developer Maintenance & API Audit"
                  } else {
                    "Scholarly Source & Coverage Audit"
                  },
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                )
                Text(
                  text = if (isVerifiedDeveloper) {
                    "Verified Developer View ($developerEmail) • Full Technical Diagnostics"
                  } else {
                    "Honest Coverage & Edition Attribution • Technical API Links Protected"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                )
              }
            }
            Row {
              IconButton(
                onClick = onRefreshAuditReport,
                modifier = Modifier.testTag("refresh_audit_dashboard_btn")
              ) {
                Icon(Icons.Default.Refresh, contentDescription = "Run Live Verification")
              }
              OutlinedButton(
                onClick = { showDiagnosticAuditExpanded = !showDiagnosticAuditExpanded },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(if (showDiagnosticAuditExpanded) "Hide" else "Inspect", fontSize = 11.sp)
              }
            }
          }

          if (showDiagnosticAuditExpanded) {
            Spacer(modifier = Modifier.height(10.dp))
            if (auditReport == null) {
              Text(
                text = "Loading verified scholarly source & Room SQLite metrics...",
                style = MaterialTheme.typography.bodySmall
              )
            } else {
              // Room SQLite Entity Counts Summary
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "Room SQLite Database Entities (Verified Local Counts)",
                    style = MaterialTheme.typography.labelLarge.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "• Surahs Table (surahs): ${auditReport.totalSurahsLocal} / 114 Surahs\n" +
                      "• Verses Table (ayahs): ${auditReport.totalAyahsLocal} / 6,236 Ayahs\n" +
                      "• Translations Table (translations): ${auditReport.totalTranslationsLocal} Verified Records (Sahih International & Urdu)\n" +
                      "• Hadith Collections Table (hadith_collections): ${auditReport.totalHadithCollectionsLocal} Collections\n" +
                      "• Hadiths Table (hadiths): ${auditReport.totalHadithsLocal} Locally Cached Narrations (+ On-Demand Verified Lookup)\n" +
                      "• Prayer Schedules Table (prayer_schedules): ${auditReport.totalPrayerSchedulesCached} Cached Schedules\n" +
                      "• Last Verified: ${auditReport.generatedAtFormatted}",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Per-Source Audit Entries
              auditReport.sourceEntries.forEach { entry ->
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.Top
                    ) {
                      Text(
                        text = entry.sourceName,
                        style = MaterialTheme.typography.labelLarge.copy(
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                      )
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldPrimary.copy(alpha = 0.14f)
                      ) {
                        Text(
                          text = entry.statusBadge,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                          )
                        )
                      }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Attribution: Verified Authentic Scholarly Publication • Links Protected for App Security",
                      style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                      text = "Books & Languages: ${entry.availableBooksAndLanguages}",
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                    Text(
                      text = "Records: ${entry.localCachedRecords} stored locally / ${entry.totalCanonicalRecords} canonical total",
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                      text = "Pagination: ${entry.paginationStatus}",
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                    Text(
                      text = "Translation Audit: ${entry.missingTranslationsReport}",
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                    Text(
                      text = "Edition Numbering: ${entry.editionNumberingReport}",
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                    Text(
                      text = "Coverage Notes: ${entry.coverageGapNotes}",
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              HorizontalDivider()
              Spacer(modifier = Modifier.height(8.dp))

              // Kutub al-Sittah & Hadith Collections Coverage Table
              Text(
                text = "Hadith Collections Coverage (Sunni & Shia Libraries):",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              auditReport.collectionAudits.forEach { col ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${col.nameEnglish} (${col.nameArabic})",
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                      text = "${col.darussalamEditionName} • ${col.inBookEditionName}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                  }
                  Text(
                    text = "${col.locallyCachedCount} cached / ${col.totalCanonicalHadiths} canonical",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary
                    )
                  )
                }
              }

              if (sourceRegistryList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = "Room Database-Backed Source Registry (source_registry Table):",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                )
                Spacer(modifier = Modifier.height(6.dp))

                sourceRegistryList.forEach { src ->
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp)
                  ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = src.title,
                          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                          modifier = Modifier.weight(1f)
                        )
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = if (src.verificationStatus == VerificationStatus.VERIFIED) EmeraldPrimary.copy(alpha = 0.16f) else GoldPrimary.copy(alpha = 0.16f)
                        ) {
                          Text(
                            text = src.verificationStatus.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                              fontSize = 9.sp,
                              fontWeight = FontWeight.Bold,
                              color = if (src.verificationStatus == VerificationStatus.VERIFIED) EmeraldPrimary else GoldPrimary
                            )
                          )
                        }
                      }
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = "Tradition: ${src.tradition} • Language: ${src.language}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      )
                      Text(
                        text = "License: ${src.license}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = GoldPrimary)
                      )
                      Text(
                        text = "Records: ${src.totalImportedRecords} imported / ${src.totalAvailableRecords} available (Status: ${src.importStatus.name})",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                      )
                      if (src.translationLanguages.isNotBlank()) {
                        Text(
                          text = "Translations: ${src.translationLanguages}",
                          style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                        )
                      }
                      Text(
                        text = "Endpoint: Verified Canonical Local Schema • Links Protected for App Security",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              OutlinedButton(
                onClick = { com.example.ui.components.openWhatsAppSupport(context) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("audit_whatsapp_support_button")
              ) {
                Text(
                  text = "Assalamu Alaikum — Audit & Source Inquiries on WhatsApp",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = EmeraldPrimary
                )
              }
            }
          }
        }
      }
    }

    // Cloud Sync / 7-Day Guest Access Status Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = if (isGuestMode && userEmail == null) {
              "Guest Access ($remainingGuestDays of 7 Days Remaining)"
            } else {
              "Cloud Sync Account (Firebase)"
            },
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isGuestMode && userEmail == null) {
              "Active 7-Day Guest Session (Offline Local SQLite Storage)"
            } else if (!userName.isNullOrBlank()) {
              "$userName (${userEmail ?: "Signed in"})"
            } else {
              userEmail ?: "Signed in with Google"
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
          )
          Text(
            text = if (isGuestMode && userEmail == null) {
              "Your bookmarks and study notes are stored locally on your device. Sign in with Google at any time to sync permanently to the cloud."
            } else {
              "Your Quran bookmarks and study notes are automatically synced with Firestore."
            },
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = onSignOut,
            modifier = Modifier.testTag("sign_out_button")
          ) {
            Text(if (isGuestMode && userEmail == null) "Sign in with Google / Switch Account" else "Sign Out")
          }
        }
      }
    }

    // Contact Us, Social Links & Play Store Publication Support
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("contact_and_support_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Contact Us & Support Bayan al-Huda",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "Lead Developer & Maintainer", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
              Text(text = developerName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Email Us action
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("contact_email_button")
              .clickable {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                  data = Uri.parse("mailto:$developerEmail")
                  putExtra(Intent.EXTRA_SUBJECT, "Bayan al-Huda Feedback / Inquiry")
                }
                try {
                  context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                } catch (e: Exception) {
                  Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                }
              },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(text = "Email Us", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                  Text(text = developerEmail, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                }
              }
              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText("Developer Email", developerEmail))
                  Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Email", modifier = Modifier.size(16.dp))
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Follow on Instagram action
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("contact_instagram_button")
              .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl))
                try {
                  context.startActivity(intent)
                } catch (e: Exception) {
                  Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                }
              },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "IG",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(text = "Follow on Instagram", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Text(text = instagramUsername, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Message on Facebook / Messenger action
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("contact_facebook_button")
              .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookMessengerUrl))
                try {
                  context.startActivity(intent)
                } catch (e: Exception) {
                  Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                }
              },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "FB",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(text = "Message on Facebook", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Text(text = "Facebook / Messenger Direct Link", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
              }
            }
          }
        }
      }
    }

    // App Interface Language Settings (Independent from Quran & Hadith Content Language)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("settings_change_language_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "App Interface Language (Choose Your Language)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Current Language: ${activeLangMeta.nameNative} (${activeLangMeta.nameEnglish}) • ${activeLangMeta.scriptInfo}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "App interface language is independent of your Quran & Hadith translation language. Reciter selection is available directly inside the Surah reader and above the Surah catalog.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = { showLanguagePickerSheet = true },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_change_language_button")
          ) {
            Text(
              text = "Change Language (${activeLangMeta.nameNative})",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Actions: Share App & Report Content Error
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Actions & Integrity",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Share App
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                val shareIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(
                    Intent.EXTRA_TEXT,
                    "Experience Bayan al-Huda — Complete Quran, Hadith & Five Daily Salah App with verified Uthmani script and Sahih International translations by Zulqarnain Ahmad Sheikh."
                  )
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Bayan al-Huda"))
              },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Share, contentDescription = null, tint = EmeraldPrimary)
              Spacer(modifier = Modifier.width(10.dp))
              Text(text = "Share Bayan al-Huda App", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Report Content Error
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(onClick = onOpenErrorReport),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.error)
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Report Content / Translation Error",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
              )
            }
          }
        }
      }
    }

    // Sacred Sources & Licencing Attribution
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Sacred Sources & Attribution",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "• Quran Text: Verified Uthmani Script (Tanzil / Quran Foundation standards)\n" +
              "• English Quran Translation: Sahih International (Explicitly attributed to Quran verses)\n" +
              "• Urdu Quran Translation: Maulana Fateh Muhammad Jalandhry\n" +
              "• Hadith Collections: Kutub al-Sittah (Sahih al-Bukhari, Sahih Muslim, Sunan Abu Dawud, Jami` at-Tirmidhi, Sunan an-Nasa'i, Sunan Ibn Majah) + Muwatta Malik, 40 Nawawi & Riyad as-Salihin with separate Darussalam and In-Book numbering.\n" +
              "• Prayer Times: AlAdhan Prayer-Time API + Offline Astronomical Calculation.\n" +
              "• Zero AI Text Alteration: Missing fields are explicitly reported as 'Not available in this source'.",
            style = MaterialTheme.typography.bodySmall.copy(
              lineHeight = 18.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }
    }
  }

  if (showLanguagePickerSheet) {
    AppLanguagePickerModalSheet(
      currentLanguageCode = currentAppLanguageCode,
      onSelectLanguage = { code, syncContent ->
        onSelectAppLanguage(code, syncContent)
        showLanguagePickerSheet = false
      },
      onDismiss = { showLanguagePickerSheet = false }
    )
  }
}

@Composable
fun ReportErrorDialog(
  initialContext: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var refText by remember { mutableStateOf(initialContext) }
  var errorDetails by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Report Content Error", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Help us maintain sacred text integrity. Please identify the Surah/Hadith reference and provide specific details.",
          style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        OutlinedTextField(
          value = refText,
          onValueChange = { refText = it },
          label = { Text("Reference (Surah / Hadith)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = errorDetails,
          onValueChange = { errorDetails = it },
          label = { Text("Description of suspected error") },
          minLines = 3,
          maxLines = 6,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (errorDetails.isNotBlank()) {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
              data = Uri.parse("mailto:sheikhzulqar@gmail.com")
              putExtra(Intent.EXTRA_SUBJECT, "Bayan al-Huda: Content Error Report [$refText]")
              putExtra(Intent.EXTRA_TEXT, "Reference: $refText\n\nIssue Details:\n$errorDetails\n\nApp Version: 1.1.0")
            }
            try {
              context.startActivity(Intent.createChooser(emailIntent, "Send Error Report"))
            } catch (e: Exception) {
              Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
            }
            onDismiss()
          }
        }
      ) {
        Text("Submit Report")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
