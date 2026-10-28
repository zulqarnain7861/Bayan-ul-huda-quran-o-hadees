package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppInterfaceLanguage
import com.example.ui.localization.AppLocalization
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily

@Composable
fun LanguageOnboardingScreen(
  initialLanguageCode: String = "en",
  onContinueWithLanguage: (languageCode: String, syncContentTranslation: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCode by remember(initialLanguageCode) {
    mutableStateOf(initialLanguageCode.ifBlank { "en" })
  }
  var searchQuery by remember { mutableStateOf("") }
  var syncContentLanguage by remember { mutableStateOf(false) }

  val filteredInterfaceLanguages = remember(searchQuery) {
    val q = searchQuery.trim()
    if (q.isEmpty()) {
      AppLocalization.supportedInterfaceLanguages
    } else {
      AppLocalization.supportedInterfaceLanguages.filter {
        it.nameNative.contains(q, ignoreCase = true) ||
          it.nameEnglish.contains(q, ignoreCase = true) ||
          it.code.contains(q, ignoreCase = true) ||
          it.quranEditionsSummary.contains(q, ignoreCase = true)
      }
    }
  }

  val filteredContentOnlyLanguages = remember(searchQuery) {
    val q = searchQuery.trim()
    if (q.isEmpty()) {
      AppLocalization.contentOnlyLanguages
    } else {
      AppLocalization.contentOnlyLanguages.filter {
        it.nameNative.contains(q, ignoreCase = true) ||
          it.nameEnglish.contains(q, ignoreCase = true) ||
          it.code.contains(q, ignoreCase = true) ||
          it.quranEditionsSummary.contains(q, ignoreCase = true)
      }
    }
  }

  val selectedLangMeta = remember(selectedCode) {
    AppLocalization.findInterfaceLanguage(selectedCode)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            EmeraldDark,
            Color(0xFF0A2E22),
            Color(0xFF061C14)
          )
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("language_onboarding_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Sacred Header Emblem & Bismillah
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .border(1.5.dp, GoldPrimary, CircleShape)
            .background(Color(0xFF08281E), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Language,
            contentDescription = "Language Selection",
            tint = GoldLight,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = ScheherazadeFontFamily,
              color = GoldLight,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.testTag("opening_bismillah_text")
          )
          Text(
            text = "Bayan al-Huda • بَيَانُ الهُدَى",
            style = MaterialTheme.typography.labelMedium.copy(
              color = Color(0xFFD0DDD6),
              fontWeight = FontWeight.SemiBold
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Required Title & Subtitle
      Text(
        text = "Choose Your Language",
        style = MaterialTheme.typography.headlineSmall.copy(
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.testTag("choose_your_language_title")
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Select your preferred language to continue.",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = GoldLight,
          fontSize = 14.sp,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.testTag("choose_your_language_subtitle")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Search Input
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            text = "Search language (العربية, اردو, हिन्दी, کٲشُر, English, فارسی, Türkçe...)",
            fontSize = 12.sp,
            color = Color(0xFFB5C7BE)
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Languages",
            tint = GoldPrimary
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GoldPrimary,
          unfocusedBorderColor = GoldPrimary.copy(alpha = 0.45f),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedContainerColor = Color(0xFF0D3528),
          unfocusedContainerColor = Color(0xFF0D3528)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("language_onboarding_search_input")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Scrollable Language List
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .testTag("language_onboarding_list"),
        contentPadding = PaddingValues(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (filteredInterfaceLanguages.isNotEmpty()) {
          item {
            Text(
              text = "Full App Interface + Verified Quran Translations (${filteredInterfaceLanguages.size})",
              style = MaterialTheme.typography.labelLarge.copy(
                color = GoldLight,
                fontWeight = FontWeight.Bold
              ),
              modifier = Modifier.padding(vertical = 2.dp)
            )
          }

          items(filteredInterfaceLanguages, key = { "ui_${it.code}" }) { lang ->
            LanguageOptionCard(
              language = lang,
              isSelected = selectedCode.equals(lang.code, ignoreCase = true),
              onSelect = { selectedCode = lang.code }
            )
          }
        }

        if (filteredContentOnlyLanguages.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Additional Quran Translation Languages (Interface in English • ${filteredContentOnlyLanguages.size})",
              style = MaterialTheme.typography.labelLarge.copy(
                color = Color(0xFFB5C7BE),
                fontWeight = FontWeight.Bold
              ),
              modifier = Modifier.padding(vertical = 2.dp)
            )
          }

          items(filteredContentOnlyLanguages, key = { "content_${it.code}" }) { lang ->
            LanguageOptionCard(
              language = lang,
              isSelected = selectedCode.equals(lang.code, ignoreCase = true),
              onSelect = {
                selectedCode = lang.code
                syncContentLanguage = true
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Independent Content Language Control Note
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0D3528),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { syncContentLanguage = !syncContentLanguage }
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = syncContentLanguage,
            onCheckedChange = { syncContentLanguage = it },
            colors = CheckboxDefaults.colors(
              checkedColor = GoldPrimary,
              checkmarkColor = Color.Black,
              uncheckedColor = GoldLight
            ),
            modifier = Modifier.testTag("sync_content_language_checkbox")
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Independent Interface & Quran Translation Language",
              style = MaterialTheme.typography.labelMedium.copy(
                color = GoldLight,
                fontWeight = FontWeight.Bold
              )
            )
            Text(
              text = if (syncContentLanguage) {
                "Will also set initial Quran translation to ${selectedLangMeta.nameEnglish} (${selectedLangMeta.nameNative}). You can change Quran/Hadith translations independently anytime."
              } else {
                "Keep existing Arabic/English/Urdu Quran translation while using ${selectedLangMeta.nameNative} (${selectedLangMeta.nameEnglish}) for the app interface."
              },
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFFD0DDD6),
                fontSize = 11.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Prominent Continue Button
      Button(
        onClick = {
          onContinueWithLanguage(selectedCode, syncContentLanguage)
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldPrimary,
          contentColor = Color(0xFF13221B)
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("language_onboarding_continue_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = "Continue (${selectedLangMeta.nameNative})",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Continue",
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun LanguageOptionCard(
  language: AppInterfaceLanguage,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  val nativeFontFamily = when (language.code) {
    "ur", "ks", "fa", "ps" -> UrduNastaliqFontFamily
    "ar" -> ScheherazadeFontFamily
    else -> MaterialTheme.typography.titleMedium.fontFamily
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onSelect)
      .testTag("language_option_${language.code}"),
    shape = RoundedCornerShape(14.dp),
    color = if (isSelected) Color(0xFF134B38) else Color(0xFF0C2E22),
    border = BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) GoldPrimary else GoldPrimary.copy(alpha = 0.25f)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      RadioButton(
        selected = isSelected,
        onClick = onSelect,
        colors = RadioButtonDefaults.colors(
          selectedColor = GoldPrimary,
          unselectedColor = Color(0xFF9AB3A8)
        )
      )

      Spacer(modifier = Modifier.width(8.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = language.nameNative,
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = nativeFontFamily,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 18.sp,
              textDirection = if (language.isRtl) TextDirection.Rtl else TextDirection.Ltr
            )
          )
          Text(
            text = "• ${language.nameEnglish}",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = GoldLight,
              fontWeight = FontWeight.SemiBold
            )
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (language.isInterfaceComplete) {
              EmeraldPrimary.copy(alpha = 0.45f)
            } else {
              Color(0xFF37474F)
            }
          ) {
            Text(
              text = if (language.isInterfaceComplete) {
                "Full App Interface + Quran"
              } else {
                "Quran Translation Only (UI: EN)"
              },
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (language.isInterfaceComplete) GoldLight else Color(0xFFD0DDD6),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          if (language.isRtl) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = GoldPrimary.copy(alpha = 0.22f)
            ) {
              Text(
                text = "RTL",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GoldLight,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          if (language.regionOrCategory == "PRIORITY_INDIAN") {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = GoldPrimary.copy(alpha = 0.28f)
            ) {
              Text(
                text = "Priority Regional",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GoldLight,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = language.quranEditionsSummary,
          style = MaterialTheme.typography.bodySmall.copy(
            color = Color(0xFFC5D6CE),
            fontSize = 11.sp
          )
        )
      }

      if (isSelected) {
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Selected",
            tint = GoldPrimary,
            modifier = Modifier.size(22.dp)
          )
          Text(
            text = "Selected",
            style = MaterialTheme.typography.labelSmall.copy(
              color = GoldPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLanguagePickerModalSheet(
  currentLanguageCode: String,
  onSelectLanguage: (languageCode: String, syncContentTranslation: Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = EmeraldDark
  ) {
    Box(modifier = Modifier.height(640.dp)) {
      LanguageOnboardingScreen(
        initialLanguageCode = currentLanguageCode,
        onContinueWithLanguage = { code, syncContent ->
          onSelectLanguage(code, syncContent)
          onDismiss()
        }
      )
    }
  }
}
