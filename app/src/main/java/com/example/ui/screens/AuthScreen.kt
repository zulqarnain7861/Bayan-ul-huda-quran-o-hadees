package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.auth.AuthManager
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
  remainingGuestDays: Int = 7,
  onContinueAsGuest: () -> Unit = {},
  onSignInSuccess: (FirebaseUser) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showAddAccountAction by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(EmeraldDark, EmeraldPrimary, Color(0xFF072A20))
        )
      )
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.5.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = EmeraldDark.copy(alpha = 0.94f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
          style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = ScheherazadeFontFamily,
            color = GoldLight,
            fontSize = 28.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(GoldPrimary.copy(alpha = 0.15f))
            .border(1.5.dp, GoldPrimary, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(34.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Bayan al-Huda",
          style = MaterialTheme.typography.headlineMedium.copy(
            color = CreamBackground,
            fontWeight = FontWeight.Bold
          )
        )

        Text(
          text = "بَيَانُ الهُدَى • Quran & Hadith Cloud Library",
          style = MaterialTheme.typography.labelLarge.copy(
            fontFamily = ScheherazadeFontFamily,
            fontSize = 18.sp,
            color = GoldPrimary,
            fontWeight = FontWeight.SemiBold
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Sign in with Google for permanent cloud backup across devices, or continue with Guest Access using offline local storage.",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = CreamBackground.copy(alpha = 0.88f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
          )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldPrimary.copy(alpha = 0.45f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CloudDone,
              contentDescription = null,
              tint = GoldPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "114 Surahs • 8 Tafsirs • 34 Hadith Books • Multi-Source Verified",
              style = MaterialTheme.typography.labelMedium.copy(
                color = CreamBackground
              )
            )
          }
        }

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = errorMessage!!,
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color(0xFFFFB4AB),
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.testTag("auth_error_text")
          )
          if (showAddAccountAction) {
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(
              onClick = {
                runCatching {
                  val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                    putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  }
                  context.startActivity(intent)
                }.onFailure {
                  runCatching {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                  }
                }
              },
              modifier = Modifier.testTag("add_google_account_settings_button")
            ) {
              Icon(
                imageVector = Icons.Default.PersonOutline,
                contentDescription = null,
                tint = GoldLight,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Add Google Account in Device Settings",
                color = GoldLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary: Sign in with Google
        Button(
          onClick = {
            if (!isLoading) {
              isLoading = true
              errorMessage = null
              showAddAccountAction = false
              scope.launch {
                val result = AuthManager.signInWithGoogle(context)
                isLoading = false
                result.onSuccess { user ->
                  onSignInSuccess(user)
                }.onFailure { err ->
                  when (err) {
                    is GetCredentialCancellationException -> {
                      errorMessage = "Sign-in was cancelled. You can try again or continue in Guest Mode below."
                    }
                    is NoCredentialException -> {
                      showAddAccountAction = true
                      errorMessage = "No Google account found on this device. Add a Google account in Device Settings or continue in Guest Mode below."
                    }
                    else -> {
                      showAddAccountAction = true
                      errorMessage = err.localizedMessage
                        ?: "Google Sign-In unavailable on this device. You can add a Google account or continue in Guest Mode below."
                    }
                  }
                }
              }
            }
          },
          enabled = !isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_sign_in_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = EmeraldDark
          )
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = EmeraldDark,
              strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Connecting to Google...", fontWeight = FontWeight.Bold)
          } else {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Sign in with Google",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary: Guest Mode button (always accessible so user is never locked out)
        val effectiveDays = remainingGuestDays.coerceAtLeast(7)
        OutlinedButton(
          onClick = {
            AuthManager.resetOrRenewGuestSession(context)
            onContinueAsGuest()
          },
          enabled = !isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("guest_access_7_days_button"),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.2.dp, GoldPrimary.copy(alpha = 0.8f)),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = GoldLight
          )
        ) {
          Icon(
            imageVector = Icons.Default.PersonOutline,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = GoldPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Continue as Guest ($effectiveDays Days Offline Access)",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = GoldLight
            )
          )
        }
      }
    }
  }
}
