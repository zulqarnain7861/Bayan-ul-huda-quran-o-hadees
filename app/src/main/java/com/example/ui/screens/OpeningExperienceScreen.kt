package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import kotlinx.coroutines.delay

@Composable
fun OpeningExperienceScreen(
  onEnterApp: () -> Unit,
  modifier: Modifier = Modifier
) {
  var contentVisible by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(200)
    contentVisible = true
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            EmeraldDark,
            EmeraldPrimary,
            Color(0xFF072118)
          )
        )
      )
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onEnterApp
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    AnimatedVisibility(
      visible = contentVisible,
      enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Subtle Islamic Star Ornament
        Box(
          modifier = Modifier
            .size(96.dp)
            .border(2.dp, GoldPrimary, CircleShape)
            .background(Color(0xFF08281E), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = "Quran Emblem",
            tint = GoldLight,
            modifier = Modifier.size(48.dp)
          )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Sacred Bismillah Calligraphy in bundled Scheherazade New Arabic script
        Text(
          text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
          style = MaterialTheme.typography.displayMedium.copy(
            fontFamily = ScheherazadeFontFamily,
            color = GoldLight,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 52.sp
          ),
          modifier = Modifier.testTag("opening_bismillah_text")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Arabic Title Emblem in bundled Scheherazade New & Amiri calligraphy
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0x33D4AF37),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp)
          ) {
            Text(
              text = "بَيَانُ الهُدَى",
              style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = ScheherazadeFontFamily,
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 48.sp
              )
            )
            Text(
              text = "القرآن الكريم والحديث النبوي والمكتبة الإسلامية",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = AmiriFontFamily,
                color = GoldLight,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // English Title & Subtitle
        Text(
          text = "Bayan al-Huda",
          style = MaterialTheme.typography.titleLarge.copy(
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Complete Quran, Hadith & Islamic Library",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = Color(0xFFD0DDD6),
            textAlign = TextAlign.Center,
            fontSize = 14.sp
          )
        )

        Spacer(modifier = Modifier.height(44.dp))

        // Accessible, direct entry button
        Button(
          onClick = onEnterApp,
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color(0xFF1B261F)
          ),
          shape = RoundedCornerShape(28.dp),
          modifier = Modifier
            .fillMaxWidth(0.75f)
            .height(52.dp)
            .testTag("enter_bayan_app_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = "Enter Library",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Proceed",
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Tap anywhere to begin",
          style = MaterialTheme.typography.labelSmall.copy(
            color = Color(0x99FFFFFF),
            fontSize = 12.sp
          )
        )
      }
    }
  }
}
