package com.example.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.updater.AndroidPackageInstallerCoordinator
import com.example.data.updater.ApkUpdateDownloadManager
import com.example.data.updater.AppUpdaterConstants
import com.example.data.updater.DistributionChannel
import com.example.data.updater.DistributionChannelDetector
import com.example.data.updater.UpdateUrgency
import com.example.data.updater.UpdaterStatus
import com.example.data.updater.UpdaterUiState
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatEpochTimestamp(epochMs: Long): String {
  if (epochMs <= 0L) return "Not checked yet"
  return runCatching {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    sdf.format(Date(epochMs))
  }.getOrDefault("Recently")
}

/**
 * In-App Update Notification Banner shown when a newer GitHub Release is detected on startup/resume
 * (Requirement 3).
 *
 * Displays:
 * - "BAYAN AL-HUDA UPDATE AVAILABLE" (with Critical / Optional badge)
 * - Installed version & Latest version
 * - Release notes preview
 * - Buttons: "Download Update", "Remind Me Later", "Release Details"
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InAppUpdateAvailableBanner(
  updaterState: UpdaterUiState,
  onDownloadUpdate: () -> Unit,
  onInstallVerifiedUpdate: () -> Unit,
  onRemindMeLater: () -> Unit,
  onShowReleaseDetails: () -> Unit,
  onNavigateToUpdaterSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val release = updaterState.latestRelease ?: return
  if (!updaterState.showUpdateNotificationBanner) return

  val isCritical = updaterState.urgency == UpdateUrgency.CRITICAL
  val isPlayStore = updaterState.distributionChannel == DistributionChannel.GOOGLE_PLAY_STORE

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .testTag("in_app_update_available_banner"),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF10221B),
    border = BorderStroke(
      width = if (isCritical) 1.5.dp else 1.dp,
      color = if (isCritical) Color(0xFFFFB74D) else GoldPrimary.copy(alpha = 0.75f)
    ),
    tonalElevation = 6.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "BAYAN AL-HUDA UPDATE AVAILABLE",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = GoldPrimary
            ),
            modifier = Modifier.testTag("update_banner_title")
          )
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isCritical) Color(0xFFD32F2F) else EmeraldPrimary
        ) {
          Text(
            text = updaterState.urgency.badgeLabel,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
        }
      }

      Text(
        text = "Installed version: ${updaterState.installedVersionName} (${updaterState.installedVersionCode})  •  Latest version: ${release.versionName} (${release.versionCode})",
        style = MaterialTheme.typography.labelMedium.copy(
          color = Color.White,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("update_banner_versions_text")
      )

      if (release.releaseNotes.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color.White.copy(alpha = 0.07f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Release notes:",
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
              )
            )
            Text(
              text = release.releaseNotes,
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFFEAE2D3),
                lineHeight = 16.sp
              ),
              maxLines = 4
            )
          }
        }
      }

      if (updaterState.status == UpdaterStatus.DOWNLOADING) {
        Column(modifier = Modifier.fillMaxWidth()) {
          LinearProgressIndicator(
            progress = { (updaterState.downloadProgressPercent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = GoldPrimary,
            trackColor = Color.White.copy(alpha = 0.15f)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Downloading: ${updaterState.downloadProgressPercent}% (${ApkUpdateDownloadManager.formatFileSize(updaterState.downloadedBytes)} / ${ApkUpdateDownloadManager.formatFileSize(updaterState.totalBytes)})",
            style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (isPlayStore) {
          Button(
            onClick = {
              DistributionChannelDetector.openGooglePlayStoreUpdateListing(context)
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("update_banner_play_store_btn")
          ) {
            Text("Update on Google Play", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        } else if (updaterState.status == UpdaterStatus.READY_TO_INSTALL) {
          Button(
            onClick = onInstallVerifiedUpdate,
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0A1914)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("update_banner_install_btn")
          ) {
            Text("Install Verified Update", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = onDownloadUpdate,
            enabled = updaterState.status != UpdaterStatus.DOWNLOADING && updaterState.status != UpdaterStatus.VERIFYING,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("update_banner_download_btn")
          ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Download Update", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        OutlinedButton(
          onClick = onRemindMeLater,
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f)),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("update_banner_remind_later_btn")
        ) {
          Text("Remind Me Later", fontSize = 11.sp, color = GoldPrimary, fontWeight = FontWeight.SemiBold)
        }

        TextButton(
          onClick = {
            onShowReleaseDetails()
            onNavigateToUpdaterSettings()
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("update_banner_release_details_btn")
        ) {
          Text("Release Details", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

/**
 * Full Settings → About → Check for Updates Card (Requirement 9).
 *
 * Displays:
 * - Current installed version & distribution channel (Direct APK vs Google Play Store)
 * - Latest available version & Last update check date
 * - Check for Updates button
 * - Release notes & file size
 * - Real-time download progress bar, percentage, Cancel & Retry controls
 * - 7-Step Security Verification badge (SHA-256 + X.509 signing certificate + signed update.json)
 * - Android PackageInstaller launch button & "Install unknown apps" permission helper
 * - Official GitHub Releases link
 * - Expandable Update History
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsGitHubUpdaterCard(
  updaterState: UpdaterUiState,
  onCheckForUpdates: () -> Unit,
  onDownloadAndVerifyUpdate: () -> Unit,
  onCancelDownload: () -> Unit,
  onInstallVerifiedUpdate: () -> Unit,
  onRemindMeLater: () -> Unit,
  onTriggerFreshUpdateTest: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showHistoryExpanded by remember { mutableStateOf(false) }
  var showReleaseDetailsModal by remember { mutableStateOf(false) }
  val release = updaterState.latestRelease
  val isPlayStore = updaterState.distributionChannel == DistributionChannel.GOOGLE_PLAY_STORE

  ElevatedCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("settings_github_updater_card"),
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
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Symmetrical Multilingual Header
      Text(
        text = "تَحْدِيثُ التَّطْبِيقِ وَالتَّحَقُّقُ الأَمْنِيُّ",
        style = MaterialTheme.typography.titleMedium.copy(
          fontFamily = ScheherazadeFontFamily,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = GoldPrimary,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Text(
        text = "ایپ اپڈیٹ، گٹ ہب ریلیزز اور سیکیورٹی تصدیق",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = 14.sp,
          lineHeight = 23.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Text(
        text = "In-App Updater & Verified GitHub Releases",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )

      // Distribution Channel & Status Pill
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = EmeraldPrimary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Text(
            text = "Distribution Channel: ${updaterState.distributionChannel.label}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.testTag("updater_distribution_channel_text")
          )
          Text(
            text = "Status: ${updaterState.status.displayLabel}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = GoldPrimary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.testTag("updater_status_text")
          )
        }
      }

      // Version & Check Metadata Summary Grid
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Current Installed Version:",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
              text = "${updaterState.installedVersionName} (Code ${updaterState.installedVersionCode})",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.testTag("updater_installed_version_value")
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Latest Available Version:",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
              text = if (release != null) {
                "${release.versionName} (Code ${release.versionCode})"
              } else {
                "${updaterState.installedVersionName} (Up to date)"
              },
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (release != null && release.versionCode > updaterState.installedVersionCode) {
                  EmeraldPrimary
                } else {
                  MaterialTheme.colorScheme.onSurface
                }
              ),
              modifier = Modifier.testTag("updater_latest_version_value")
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Last Update Check:",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
              text = formatEpochTimestamp(updaterState.lastCheckedTimestampMs),
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              modifier = Modifier.testTag("updater_last_checked_value")
            )
          }

          if (release != null && release.apkAsset.sizeBytes > 0L) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Release APK Size:",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
              Text(
                text = ApkUpdateDownloadManager.formatFileSize(release.apkAsset.sizeBytes),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
              )
            }
          }
        }
      }

      // Release Notes Section when a release is loaded
      if (release != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("updater_release_notes_box")
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Release Notes (${release.tagName})",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
              TextButton(
                onClick = { showReleaseDetailsModal = true },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.testTag("updater_open_release_details_modal_btn")
              ) {
                Text("Release Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
            Text(
              text = release.releaseNotes,
              style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
              maxLines = 6
            )
          }
        }
      }

      // Download Progress Bar + Cancel / Retry Controls
      if (updaterState.status == UpdaterStatus.DOWNLOADING || updaterState.status == UpdaterStatus.VERIFYING) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldPrimary.copy(alpha = 0.08f),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("updater_download_progress_section")
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            LinearProgressIndicator(
              progress = { (updaterState.downloadProgressPercent / 100f).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .testTag("updater_download_progress_bar"),
              color = EmeraldPrimary,
              trackColor = EmeraldPrimary.copy(alpha = 0.2f)
            )
            Text(
              text = "${updaterState.downloadProgressPercent}% • ${ApkUpdateDownloadManager.formatFileSize(updaterState.downloadedBytes)} / ${ApkUpdateDownloadManager.formatFileSize(updaterState.totalBytes)}",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.testTag("updater_download_progress_label")
            )
            if (updaterState.status == UpdaterStatus.DOWNLOADING) {
              OutlinedButton(
                onClick = onCancelDownload,
                modifier = Modifier.testTag("updater_cancel_download_button")
              ) {
                Text("Cancel Download", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      // Security Verification Result Summary
      updaterState.verificationResult?.let { verification ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (verification.isValid) {
            EmeraldPrimary.copy(alpha = 0.12f)
          } else {
            MaterialTheme.colorScheme.errorContainer
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("updater_verification_result_card")
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (verification.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = if (verification.isValid) EmeraldPrimary else MaterialTheme.colorScheme.error,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (verification.isValid) {
                  "APK Integrity, Signed Metadata & X.509 Certificate Verified"
                } else {
                  "APK Security Verification Rejected Package"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (verification.isValid) EmeraldPrimary else MaterialTheme.colorScheme.onErrorContainer
                )
              )
              Text(
                text = if (verification.isValid) {
                  "SHA-256: ${verification.actualSha256?.take(16)}... • Cert SHA-256 matched installed app"
                } else {
                  verification.errorMessage ?: "Verification failed"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  lineHeight = 15.sp
                )
              )
            }
          }
        }
      }

      // Error / Rate Limit / Permission Guidance Banner
      if (!updaterState.errorMessage.isNullOrBlank()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("updater_error_message_box")
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = updaterState.errorMessage!!,
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium
              )
            )
            if (updaterState.status == UpdaterStatus.AWAITING_INSTALL_PERMISSION || !updaterState.canRequestPackageInstalls) {
              Button(
                onClick = {
                  AndroidPackageInstallerCoordinator.openUnknownAppSourcesSettings(context)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("updater_grant_install_permission_button")
              ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Android Settings → Allow 'Install Unknown Apps'", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Primary Action Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onCheckForUpdates,
          enabled = updaterState.status != UpdaterStatus.CHECKING && updaterState.status != UpdaterStatus.DOWNLOADING,
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldPrimary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("about_check_for_updates_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (updaterState.status == UpdaterStatus.CHECKING) "Checking..." else "Check for Updates",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }

        if (release != null && release.versionCode > updaterState.installedVersionCode) {
          if (isPlayStore) {
            Button(
              onClick = {
                DistributionChannelDetector.openGooglePlayStoreUpdateListing(context)
              },
              colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0A1914)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("about_update_on_play_store_button")
            ) {
              Text("Google Play Update", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          } else if (updaterState.status == UpdaterStatus.READY_TO_INSTALL || updaterState.status == UpdaterStatus.AWAITING_INSTALL_PERMISSION) {
            Button(
              onClick = onInstallVerifiedUpdate,
              colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0A1914)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("about_install_verified_update_button")
            ) {
              Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Install Update", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          } else {
            Button(
              onClick = onDownloadAndVerifyUpdate,
              enabled = updaterState.status != UpdaterStatus.DOWNLOADING && updaterState.status != UpdaterStatus.VERIFYING,
              colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0A1914)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("about_download_update_button")
            ) {
              Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (updaterState.status == UpdaterStatus.DOWNLOAD_CANCELLED || updaterState.status == UpdaterStatus.ERROR) {
                  "Retry Download"
                } else {
                  "Download Update"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
        }
      }

      // Fresh Update Test Trigger Button (stages a cryptographically signed v1.1.0 / Code 9 APK & opens Update Dialog)
      OutlinedButton(
        onClick = onTriggerFreshUpdateTest,
        border = BorderStroke(1.dp, GoldPrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("about_test_fresh_update_button")
      ) {
        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Test Fresh Update Dialog & Verification (v1.1.0 • Code ${updaterState.currentBuildConfigVersionCode + 1})",
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = EmeraldPrimary
        )
      }

      // Official GitHub Releases Web Link
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("about_open_github_releases_button")
          .clickable {
            val url = release?.htmlUrl ?: AppUpdaterConstants.GITHUB_RELEASES_WEB_URL
            try {
              context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              })
            } catch (_: Exception) {
              Toast.makeText(context, "Could not open browser for GitHub Releases", Toast.LENGTH_SHORT).show()
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
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Official GitHub Releases Page",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
            Text(
              text = AppUpdaterConstants.GITHUB_RELEASES_WEB_URL,
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
          Icon(
            imageVector = Icons.Default.FactCheck,
            contentDescription = "Open GitHub Releases",
            tint = EmeraldPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Update History Section
      HorizontalDivider()
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showHistoryExpanded = !showHistoryExpanded }
          .testTag("about_toggle_update_history_button")
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Refresh, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Update History (${updaterState.updateHistory.size})",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
          )
        }
        Text(
          text = if (showHistoryExpanded) "Hide" else "Show",
          style = MaterialTheme.typography.labelSmall.copy(
            color = EmeraldPrimary,
            fontWeight = FontWeight.Bold
          )
        )
      }

      if (showHistoryExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("about_update_history_list"),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (updaterState.updateHistory.isEmpty()) {
            Text(
              text = "No update events recorded yet.",
              style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
          } else {
            updaterState.updateHistory.take(10).forEachIndexed { idx, entry ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("update_history_item_$idx")
              ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "${entry.status} • v${entry.targetVersionName} (${entry.targetVersionCode})",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                    Text(
                      text = formatEpochTimestamp(entry.timestampMs),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                  }
                  Text(
                    text = entry.details,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  if (showReleaseDetailsModal && release != null) {
    AlertDialog(
      onDismissRequest = { showReleaseDetailsModal = false },
      title = {
        Text(
          text = "Bayan al-Huda ${release.tagName} Release Details",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Installed: v${updaterState.installedVersionName} (Code ${updaterState.installedVersionCode})\n" +
              "Latest: v${release.versionName} (Code ${release.versionCode})\n" +
              "Minimum Supported Code: ${release.minimumVersionCode}\n" +
              "Urgency: ${updaterState.urgency.badgeLabel}\n" +
              "Asset: ${release.apkAsset.name} (${ApkUpdateDownloadManager.formatFileSize(release.apkAsset.sizeBytes)})",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
          )
          if (release.signedManifest != null) {
            Text(
              text = "Expected SHA-256:\n${release.signedManifest.sha256}",
              style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary)
            )
          }
          HorizontalDivider()
          Text(
            text = release.releaseNotes,
            style = MaterialTheme.typography.bodySmall
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showReleaseDetailsModal = false
            onDownloadAndVerifyUpdate()
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text("Download Update")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showReleaseDetailsModal = false
            onRemindMeLater()
          }
        ) {
          Text("Remind Me Later")
        }
      }
    )
  }
}

/**
 * Modal In-App Update Dialog triggered automatically by `InAppUpdateService` when the GitHub Releases API
 * (`https://api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest`)
 * reports a `versionCode > BuildConfig.VERSION_CODE`.
 */
@Composable
fun InAppUpdateAlertDialog(
  updaterState: UpdaterUiState,
  onDownloadAndVerifyUpdate: () -> Unit,
  onInstallVerifiedUpdate: () -> Unit,
  onRemindMeLater: () -> Unit,
  onDismissDialog: () -> Unit,
  onOpenUpdaterSettings: () -> Unit
) {
  val context = LocalContext.current
  val release = updaterState.latestRelease ?: return
  if (!updaterState.showUpdateDialog) return

  val isPlayStore = updaterState.distributionChannel == DistributionChannel.GOOGLE_PLAY_STORE
  val isCritical = updaterState.urgency == UpdateUrgency.CRITICAL

  AlertDialog(
    onDismissRequest = {
      if (!isCritical) {
        onDismissDialog()
      }
    },
    modifier = Modifier.testTag("in_app_update_dialog"),
    title = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "تَحْدِيثٌ جَدِيدٌ لِلتَّطْبِيقِ",
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = ScheherazadeFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            textAlign = TextAlign.Center
          )
        )
        Text(
          text = "BAYAN AL-HUDA UPDATE AVAILABLE",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldPrimary,
            textAlign = TextAlign.Center
          ),
          modifier = Modifier.testTag("in_app_update_dialog_title")
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isCritical) Color(0xFFD32F2F) else EmeraldPrimary
        ) {
          Text(
            text = updaterState.urgency.badgeLabel,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.09f),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "Installed (BuildConfig.VERSION_CODE = ${updaterState.currentBuildConfigVersionCode}): v${updaterState.installedVersionName} (Code ${updaterState.installedVersionCode})",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              modifier = Modifier.testTag("in_app_update_dialog_installed_version")
            )
            Text(
              text = "Latest GitHub Release: v${release.versionName} (Code ${release.versionCode})",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPrimary
              ),
              modifier = Modifier.testTag("in_app_update_dialog_latest_version")
            )
            Text(
              text = "APK Size: ${ApkUpdateDownloadManager.formatFileSize(release.apkAsset.sizeBytes)} • Source: GitHub Releases API",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }

        if (release.releaseNotes.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "What's New in ${release.tagName}:",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
              Text(
                text = release.releaseNotes,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 12.sp,
                  lineHeight = 16.sp
                ),
                maxLines = 6
              )
            }
          }
        }

        if (updaterState.status == UpdaterStatus.DOWNLOADING || updaterState.status == UpdaterStatus.VERIFYING) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("in_app_update_dialog_progress"),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            LinearProgressIndicator(
              progress = { (updaterState.downloadProgressPercent / 100f).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = EmeraldPrimary,
              trackColor = EmeraldPrimary.copy(alpha = 0.2f)
            )
            Text(
              text = if (updaterState.status == UpdaterStatus.VERIFYING) {
                "Verifying SHA-256 checksum & X.509 certificate..."
              } else {
                "Downloading APK: ${updaterState.downloadProgressPercent}% (${ApkUpdateDownloadManager.formatFileSize(updaterState.downloadedBytes)} / ${ApkUpdateDownloadManager.formatFileSize(updaterState.totalBytes)})"
              },
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        updaterState.verificationResult?.let { verification ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (verification.isValid) {
              EmeraldPrimary.copy(alpha = 0.14f)
            } else {
              MaterialTheme.colorScheme.errorContainer
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("in_app_update_dialog_verification_badge")
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (verification.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (verification.isValid) EmeraldPrimary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (verification.isValid) {
                  "Verified SHA-256 (${verification.actualSha256?.take(12)}...) & X.509 Signing Certificate"
                } else {
                  verification.errorMessage ?: "Verification failed"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (verification.isValid) EmeraldPrimary else MaterialTheme.colorScheme.onErrorContainer
                )
              )
            }
          }
        }
      }
    },
    confirmButton = {
      if (isPlayStore) {
        Button(
          onClick = {
            DistributionChannelDetector.openGooglePlayStoreUpdateListing(context)
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
          modifier = Modifier.testTag("in_app_update_dialog_play_store_btn")
        ) {
          Text("Update on Google Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      } else if (updaterState.status == UpdaterStatus.READY_TO_INSTALL || updaterState.status == UpdaterStatus.AWAITING_INSTALL_PERMISSION) {
        Button(
          onClick = onInstallVerifiedUpdate,
          colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0A1914)),
          modifier = Modifier.testTag("in_app_update_dialog_install_btn")
        ) {
          Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Install Verified APK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      } else {
        Button(
          onClick = onDownloadAndVerifyUpdate,
          enabled = updaterState.status != UpdaterStatus.DOWNLOADING && updaterState.status != UpdaterStatus.VERIFYING,
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
          modifier = Modifier.testTag("in_app_update_dialog_download_btn")
        ) {
          Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Download & Verify Update", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(
          onClick = {
            onDismissDialog()
            onOpenUpdaterSettings()
          },
          modifier = Modifier.testTag("in_app_update_dialog_details_btn")
        ) {
          Text("Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        TextButton(
          onClick = onRemindMeLater,
          modifier = Modifier.testTag("in_app_update_dialog_remind_later_btn")
        ) {
          Text("Remind Me Later", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  )
}

