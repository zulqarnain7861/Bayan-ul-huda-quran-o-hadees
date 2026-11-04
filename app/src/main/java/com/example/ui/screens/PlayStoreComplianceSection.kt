package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

/**
 * Optional external Google Sites URLs.
 * Left empty by default so the app never opens a fake or placeholder URL.
 * Once the developer publishes their Google Sites pages and pastes the public URLs here,
 * the in-app dialogs automatically display an "Open Published Webpage" button alongside the in-app text.
 */
object PlayStoreComplianceConfig {
  const val DEVELOPER_NAME = "Zulqarnain Ahmad Sheikh"
  const val DEVELOPER_SUPPORT_EMAIL = "sheikhzulqar@gmail.com"
  const val APP_PACKAGE_ID = "com.bayanulhuda.com"
  const val EFFECTIVE_DATE = "November 1, 2026"

  // Leave empty until the public Google Sites pages are published by the developer:
  const val PUBLISHED_PRIVACY_POLICY_URL: String = ""
  const val PUBLISHED_ACCOUNT_DELETION_URL: String = ""
  const val PUBLISHED_TERMS_URL: String = ""
}

@Composable
fun PlayStoreComplianceCard(
  userEmail: String?,
  userName: String?,
  isGuestMode: Boolean,
  onDeleteAccount: (Boolean, (Boolean, String) -> Unit) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
  var showTermsDialog by remember { mutableStateOf(false) }
  var showPermissionsDialog by remember { mutableStateOf(false) }
  var showDeleteAccountDialog by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("play_store_compliance_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(EmeraldPrimary.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Privacy, Terms, Permissions & Account Control",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Text(
            text = "Data safety, permission disclosures, legal terms & account deletion",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }

      Text(
        text = "Bayan al-Huda is 100% free and ad-free. Review how your data and device permissions are used, read our Privacy Policy and Terms of Use, or permanently delete your account and cloud-synced data at any time.",
        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
      )

      // 1. Privacy Policy Button
      OutlinedButton(
        onClick = { showPrivacyPolicyDialog = true },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("privacy_policy_button")
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Privacy Policy", fontWeight = FontWeight.SemiBold)
      }

      // 2. Terms & Conditions Button
      OutlinedButton(
        onClick = { showTermsDialog = true },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("terms_and_conditions_button")
      ) {
        Icon(
          imageVector = Icons.Default.Description,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Terms & Conditions", fontWeight = FontWeight.SemiBold)
      }

      // 3. Permissions & Data Safety Transparency Button
      OutlinedButton(
        onClick = { showPermissionsDialog = true },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("permissions_transparency_button")
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Permissions & Data Transparency", fontWeight = FontWeight.SemiBold)
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

      // 4. Account & Cloud Data Deletion Button
      Button(
        onClick = { showDeleteAccountDialog = true },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.errorContainer,
          contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("delete_account_button")
      ) {
        Icon(
          imageVector = Icons.Default.DeleteForever,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (!userEmail.isNullOrBlank()) {
            "Delete Account & Cloud Data ($userEmail)"
          } else {
            "Delete Account / Clear User Data"
          },
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }
    }
  }

  if (showPrivacyPolicyDialog) {
    PrivacyPolicyDialog(
      context = context,
      onDismiss = { showPrivacyPolicyDialog = false }
    )
  }

  if (showTermsDialog) {
    TermsAndConditionsDialog(
      context = context,
      onDismiss = { showTermsDialog = false }
    )
  }

  if (showPermissionsDialog) {
    PermissionsTransparencyDialog(
      onDismiss = { showPermissionsDialog = false }
    )
  }

  if (showDeleteAccountDialog) {
    DeleteAccountConfirmationDialog(
      context = context,
      userEmail = userEmail,
      userName = userName,
      isGuestMode = isGuestMode,
      onDeleteAccount = onDeleteAccount,
      onDismiss = { showDeleteAccountDialog = false }
    )
  }
}

@Composable
fun PrivacyPolicyDialog(
  context: Context,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "Privacy Policy — Bayan al-Huda",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "Effective Date: ${PlayStoreComplianceConfig.EFFECTIVE_DATE}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PolicySectionBlock(
          heading = "1. Overview & Zero-Advertising Pledge",
          body = "Bayan al-Huda (${PlayStoreComplianceConfig.APP_PACKAGE_ID}), developed by ${PlayStoreComplianceConfig.DEVELOPER_NAME}, is a free, ad-free Islamic educational and worship companion app providing the Holy Quran, Sunni & Shia Hadith collections, Tafsir, Prayer Times, Azan & Fajr Alarms, Qibla Compass, Masnoon Duas, and Islamic scholarly lectures. We do not sell, rent, or monetize your personal data, and we do not include any third-party advertising SDKs."
        )
        PolicySectionBlock(
          heading = "2. Account Information & Authentication",
          body = "• Google Sign-In (Optional): You may sign in using your Google account via Android Credential Manager and Firebase Authentication to synchronize your bookmarks and study notes across devices. When you sign in, Firebase Authentication receives your Google account email address, display name, profile photo URL (if any), and a unique Firebase User ID (UID).\n• Unlimited Offline Guest Mode: You may use the app in Guest Mode for unlimited time without signing into a Google account. In Guest Mode, all bookmarks, study notes, and preferences remain strictly on your device."
        )
        PolicySectionBlock(
          heading = "3. Cloud Sync Data (Firebase Firestore)",
          body = "When signed in with Google, only the Quran/Hadith bookmarks (`/users/{uid}/bookmarks`) and personal study notes (`/users/{uid}/notes`) that you explicitly create are stored in Google Cloud Firestore. Access is restricted by zero-trust security rules (`request.auth.uid == userId`) so only your authenticated account can read, write, or delete your bookmarks and notes."
        )
        PolicySectionBlock(
          heading = "4. Location Data (Prayer Times & Qibla Compass)",
          body = "• Permissions: `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`.\n• How it is used: Location is used only when you request GPS coordinate detection to calculate accurate local 5 Daily Prayer Times, Sehri/Iftar timetables (via the AlAdhan API), and the Qibla direction bearing toward the Kaaba in Makkah.\n• Storage & Sharing: Your last selected city and coordinates are saved locally in app preferences on your device. We do not track your background location or store location history on our servers. You may also select any city manually without granting location permission."
        )
        PolicySectionBlock(
          heading = "5. Microphone & Voice Search (`RECORD_AUDIO`)",
          body = "• Permission: `RECORD_AUDIO`.\n• How it is used: The microphone is accessed only when you actively tap the Voice Search microphone button to dictate a Quran, Hadith, or lecture search query using Android's SpeechRecognizer service.\n• No Audio Recording Storage: Bayan al-Huda does not record, store, or upload your voice recordings to any developer server."
        )
        PolicySectionBlock(
          heading = "6. Offline Downloads & App-Private Storage",
          body = "Downloaded Quran translations, Hadith databases, and MP3 Quran recitations are saved exclusively inside the application's private sandboxed internal directories (`filesDir` and `cacheDir`). The app does NOT request broad external storage permissions (`READ_EXTERNAL_STORAGE` or `WRITE_EXTERNAL_STORAGE`) and cannot read your personal photos, videos, or documents. You can delete cached audio or offline packages at any time using the in-app Storage Cleaner and Offline Library."
        )
        PolicySectionBlock(
          heading = "7. Alarms, Notifications & Media Playback",
          body = "Permissions such as `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `RECEIVE_BOOT_COMPLETED`, `VIBRATE`, `WAKE_LOCK`, and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` are used solely to deliver timely 5 Daily Salah Azan notifications, the Fajr wake-up alarm, daily Sunnah reminders, and continuous Quran/lecture audio playback."
        )
        PolicySectionBlock(
          heading = "8. Data Security, Retention & Account Deletion",
          body = "All network communication with Firebase, Quran/Hadith APIs, AlAdhan, and YouTube uses HTTPS/TLS encryption in transit. Cloud bookmarks and notes are retained only while your account remains active. You can permanently delete your Firebase account and all associated Firestore bookmarks and notes at any time directly inside the app (Info/About -> Delete Account & Cloud Data) or by emailing ${PlayStoreComplianceConfig.DEVELOPER_SUPPORT_EMAIL}."
        )
        PolicySectionBlock(
          heading = "9. Developer Contact",
          body = "Developer: ${PlayStoreComplianceConfig.DEVELOPER_NAME}\nSupport Email: ${PlayStoreComplianceConfig.DEVELOPER_SUPPORT_EMAIL}"
        )
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (PlayStoreComplianceConfig.PUBLISHED_PRIVACY_POLICY_URL.isNotBlank()) {
          OutlinedButton(
            onClick = {
              runCatching {
                context.startActivity(
                  Intent(Intent.ACTION_VIEW, Uri.parse(PlayStoreComplianceConfig.PUBLISHED_PRIVACY_POLICY_URL))
                )
              }
            }
          ) {
            Text("Open Webpage")
          }
        }
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldPrimary,
            contentColor = Color.White
          )
        ) {
          Text("Close")
        }
      }
    }
  )
}

@Composable
fun TermsAndConditionsDialog(
  context: Context,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "Terms & Conditions — Bayan al-Huda",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "Effective Date: ${PlayStoreComplianceConfig.EFFECTIVE_DATE}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PolicySectionBlock(
          heading = "1. Acceptance of Terms",
          body = "By installing or using Bayan al-Huda, you agree to these Terms and Conditions. Bayan al-Huda is provided free of charge for personal, educational, and religious study."
        )
        PolicySectionBlock(
          heading = "2. Religious & Scholarly Content Disclaimer",
          body = "• Sacred Texts: Arabic Quranic text, translations, Tafsir editions, and Sunni & Shia Hadith collections are sourced from verified scholarly repositories (including Tanzil/QuranEnc, QUL, fawazahmed0 Hadith API, Al-Quran Cloud, and bundled SQLite editions).\n• Multiple Traditions & Perspectives: The app presents primary scriptural sources and scholarly works (including Sunni collections, Shia collections, Understand Islam video lectures by Engineer Muhammad Ali Mirza, and Fikr-e-Maududi classical treatises) for comparative study and research. Inclusion of third-party lectures or external YouTube streams is for educational reference.\n• Verification: If you notice any typographical or citation issue in a translation or Hadith entry, please use the built-in 'Report Content Error' feature so it can be reviewed against primary manuscripts."
        )
        PolicySectionBlock(
          heading = "3. Prayer Times, Sehri/Iftar & Qibla Calculations",
          body = "Prayer times, Sehri/Iftar timetables, and Qibla compass angles depend on your device's GPS coordinates, compass magnetometer calibration, and selected calculation method (e.g., Karachi, ISNA, MWL, Umm al-Qura, Jafari). Always allow a prudent safety margin for Sehri/Iftar and verify with your local mosque timetable when needed."
        )
        PolicySectionBlock(
          heading = "4. User Accounts, Notes & Acceptable Use",
          body = "You are responsible for maintaining the security of the Google account you use to sign in. Personal study notes and bookmarks synced to Cloud Firestore must comply with applicable laws and must not be used to store unlawful or malicious content. You may sign out or permanently delete your account at any time."
        )
        PolicySectionBlock(
          heading = "5. Third-Party Services & Streaming Content",
          body = "Video lectures stream directly from official YouTube channels in compliance with YouTube policies; the app does not provide unauthorized video file downloading. External links (such as source repositories, YouTube channels, or support channels) are governed by their respective terms."
        )
        PolicySectionBlock(
          heading = "6. Limitation of Liability & Updates",
          body = "Bayan al-Huda is provided on an 'as-is' basis without warranties of uninterrupted availability. The developer (${PlayStoreComplianceConfig.DEVELOPER_NAME}) may update features, databases, or policies to maintain accuracy and Google Play policy compliance."
        )
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (PlayStoreComplianceConfig.PUBLISHED_TERMS_URL.isNotBlank()) {
          OutlinedButton(
            onClick = {
              runCatching {
                context.startActivity(
                  Intent(Intent.ACTION_VIEW, Uri.parse(PlayStoreComplianceConfig.PUBLISHED_TERMS_URL))
                )
              }
            }
          ) {
            Text("Open Webpage")
          }
        }
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldPrimary,
            contentColor = Color.White
          )
        ) {
          Text("Close")
        }
      }
    }
  )
}

@Composable
fun PermissionsTransparencyDialog(
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "Permissions & Data Safety Transparency",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "Why each permission is requested and how your privacy is protected",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PolicySectionBlock(
          heading = "1. Location (`ACCESS_COARSE_LOCATION` & `ACCESS_FINE_LOCATION`)",
          body = "• Purpose: Calculates accurate local 5 Daily Prayer Times, Ramadan Sehri/Iftar schedules, and the Qibla Compass bearing from your location to the Kaaba.\n• Optional & On-Demand: Requested only when you tap 'Detect GPS Location'. You can deny location permission and select any city or coordinates manually."
        )
        PolicySectionBlock(
          heading = "2. Microphone (`RECORD_AUDIO`)",
          body = "• Purpose: Enables hands-free Voice Search when you tap the microphone icon in the Search screen.\n• Optional & Ephemeral: Never accessed in the background. Audio is processed transiently by Android SpeechRecognizer to produce search text and is never stored or uploaded by Bayan al-Huda."
        )
        PolicySectionBlock(
          heading = "3. Notifications & Exact Alarms (`POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `RECEIVE_BOOT_COMPLETED`)",
          body = "• Purpose: Delivers on-time 5 Daily Salah Azan alerts, the pre-Fajr wake-up alarm, and daily Sunnah/Dua reminders even after a device reboot."
        )
        PolicySectionBlock(
          heading = "4. Foreground Media Playback (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `WAKE_LOCK`, `VIBRATE`)",
          body = "• Purpose: Keeps Quran tilawat audio, Azan recitation, and vibration alerts playing reliably while the screen is off or while you switch apps."
        )
        PolicySectionBlock(
          heading = "5. Why NO Storage Permission Is Needed for Offline Downloads",
          body = "• Zero External Storage Access: Bayan al-Huda does NOT request `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, or `READ_MEDIA_*`.\n• App-Private Sandboxed Storage: All offline Quran translations, Hadith packs, and MP3 recitations are stored inside Android's private app-specific directories (`context.filesDir` and `context.cacheDir`). This protects your personal photos and files while giving you full offline access."
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(
          containerColor = EmeraldPrimary,
          contentColor = Color.White
        )
      ) {
        Text("Got It")
      }
    }
  )
}

@Composable
fun DeleteAccountConfirmationDialog(
  context: Context,
  userEmail: String?,
  userName: String?,
  isGuestMode: Boolean,
  onDeleteAccount: (Boolean, (Boolean, String) -> Unit) -> Unit,
  onDismiss: () -> Unit
) {
  var deleteLocalDataToo by remember { mutableStateOf(true) }
  var isDeleting by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isDeletionSuccess by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = { if (!isDeleting) onDismiss() },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.DeleteForever,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Delete Account & Data",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
          )
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = if (!userEmail.isNullOrBlank()) {
            "You are signed in as ${userName?.takeIf { it.isNotBlank() } ?: userEmail} ($userEmail)."
          } else if (isGuestMode) {
            "You are currently using an offline Guest Session (no Google account is signed in)."
          } else {
            "Manage permanent deletion of your Bayan al-Huda account and synced cloud data."
          },
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        Text(
          text = "Confirming deletion will permanently and irreversibly remove:\n" +
            "• Your Firebase Authentication user account record (UID, email & profile link)\n" +
            "• All Cloud Firestore bookmarks (`/users/{uid}/bookmarks`)\n" +
            "• All Cloud Firestore personal study notes (`/users/{uid}/notes`)",
          style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
        )

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = deleteLocalDataToo,
              onCheckedChange = { deleteLocalDataToo = it },
              enabled = !isDeleting
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Also delete local bookmarks and study notes stored on this device",
              style = MaterialTheme.typography.bodySmall
            )
          }
        }

        if (statusMessage != null) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isDeletionSuccess) {
              EmeraldPrimary.copy(alpha = 0.14f)
            } else {
              MaterialTheme.colorScheme.errorContainer
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = statusMessage!!,
              modifier = Modifier.padding(10.dp),
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isDeletionSuccess) {
                  EmeraldPrimary
                } else {
                  MaterialTheme.colorScheme.onErrorContainer
                },
                fontWeight = FontWeight.SemiBold
              )
            )
          }
        }

        HorizontalDivider()

        Text(
          text = "External / Email Deletion Request Option:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "If you cannot sign in or prefer to request account deletion outside the app, tap below to send a pre-filled deletion request to ${PlayStoreComplianceConfig.DEVELOPER_SUPPORT_EMAIL}.",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
          )
        )

        OutlinedButton(
          onClick = {
            if (PlayStoreComplianceConfig.PUBLISHED_ACCOUNT_DELETION_URL.isNotBlank()) {
              runCatching {
                context.startActivity(
                  Intent(Intent.ACTION_VIEW, Uri.parse(PlayStoreComplianceConfig.PUBLISHED_ACCOUNT_DELETION_URL))
                )
              }
            } else {
              val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${PlayStoreComplianceConfig.DEVELOPER_SUPPORT_EMAIL}")
                putExtra(
                  Intent.EXTRA_SUBJECT,
                  "Account & Data Deletion Request — Bayan al-Huda"
                )
                putExtra(
                  Intent.EXTRA_TEXT,
                  "Please permanently delete my Bayan al-Huda Firebase account and all associated Cloud Firestore bookmarks and study notes.\n\nAccount Email: ${userEmail ?: "[Enter your Google Sign-In email]"}\nPackage: ${PlayStoreComplianceConfig.APP_PACKAGE_ID}"
                )
              }
              try {
                context.startActivity(Intent.createChooser(emailIntent, "Request Account Deletion"))
              } catch (_: Exception) {
                Toast.makeText(
                  context,
                  "Email: ${PlayStoreComplianceConfig.DEVELOPER_SUPPORT_EMAIL}",
                  Toast.LENGTH_LONG
                ).show()
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("request_deletion_email_button")
        ) {
          Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (PlayStoreComplianceConfig.PUBLISHED_ACCOUNT_DELETION_URL.isNotBlank()) {
              "Open External Account Deletion Portal"
            } else {
              "Request Deletion via Support Email"
            },
            fontSize = 12.sp
          )
        }
      }
    },
    confirmButton = {
      if (!isDeletionSuccess) {
        Button(
          onClick = {
            if (!isDeleting) {
              isDeleting = true
              statusMessage = null
              onDeleteAccount(deleteLocalDataToo) { success, message ->
                isDeleting = false
                isDeletionSuccess = success
                statusMessage = message
                if (success) {
                  Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
              }
            }
          },
          enabled = !isDeleting,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
          ),
          modifier = Modifier.testTag("confirm_delete_account_button")
        ) {
          Text(
            text = if (isDeleting) "Deleting..." else "Permanently Delete Account",
            fontWeight = FontWeight.Bold
          )
        }
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        enabled = !isDeleting
      ) {
        Text(if (isDeletionSuccess) "Done" else "Cancel")
      }
    }
  )
}

@Composable
private fun PolicySectionBlock(
  heading: String,
  body: String
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = heading,
      style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        color = EmeraldPrimary
      )
    )
    Text(
      text = body,
      style = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 18.sp
      )
    )
  }
}
