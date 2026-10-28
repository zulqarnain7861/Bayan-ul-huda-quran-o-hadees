package com.example

import android.content.Context
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.QulOfflineTranslationManager
import com.example.data.local.QulTranslationCatalog
import com.example.data.local.ShiaTranslationDataProvider
import com.example.data.prayer.PrayerPreferencesManager
import com.example.ui.localization.AppLocalization
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QulTranslationsVerificationTest {

  private val expected73Languages = listOf(
    "Persian (Farsi)", "Dari", "Pashto", "Kurdish", "Sindhi", "Kashmiri", "Uighur", "Dhivehi",
    "Hindi", "Bengali", "Punjabi", "Gujarati", "Marathi", "Nepali", "Assamese", "Tamil",
    "Telugu", "Kannada", "Malayalam", "Sinhala", "Indonesian", "Malay", "Tagalog", "Vietnamese",
    "Thai", "Khmer", "Chinese", "Japanese", "Korean", "Turkish", "Azerbaijani", "Uzbek",
    "Kazakh", "Tajik", "Tatar", "Chechen", "Russian", "Ukrainian", "Bulgarian", "Macedonian",
    "Bosnian", "Albanian", "Czech", "Polish", "Lithuanian", "Romanian", "Finnish", "Swedish",
    "Norwegian", "Dutch", "German", "French", "Spanish", "Portuguese", "Italian", "Hebrew",
    "Hausa", "Swahili", "Somali", "Amharic", "Oromo", "Yoruba", "Asante", "Bambara",
    "Fulah", "Ganda", "Lingala", "Maranao", "Chichewa", "Kinyarwanda", "Yao",
    "Tamazight (Amazigh)", "Afar"
  )

  private val expectedRtlLanguages = setOf(
    "Persian (Farsi)", "Dari", "Pashto", "Kurdish", "Sindhi", "Kashmiri", "Uighur", "Dhivehi", "Hebrew"
  )

  @Test
  fun verifyAll73LanguagesPresentAndDagbaniReportedMissing() {
    val availableGroups = QulTranslationCatalog.languageGroups.filter { !it.isMissingInSource }
    assertEquals(73, availableGroups.size)

    val actualLangNames = availableGroups.map { it.languageNameEn }.toSet()
    for (expected in expected73Languages) {
      assertTrue("Expected language '$expected' in QulTranslationCatalog", actualLangNames.contains(expected))
    }

    // Verify Afar uses fawazahmed0/quran-api edition aar-sheikhmahmoudab
    val afarEd = QulTranslationCatalog.findEditionByKey("fawaz.aar.sheikhmahmoudab")
    assertNotNull("Afar edition must be present", afarEd)
    assertEquals("fawazahmed0/quran-api", afarEd!!.source)
    assertEquals("aar-sheikhmahmoudab", afarEd.fawazEditionKey)

    // Verify Dagbani is explicitly reported as missing
    val dagbaniGroup = QulTranslationCatalog.languageGroups.find { it.languageNameEn == "Dagbani" }
    assertNotNull("Dagbani group must be present as a reported missing entry", dagbaniGroup)
    assertTrue("Dagbani must be marked isMissingInSource", dagbaniGroup!!.isMissingInSource)
    assertTrue(dagbaniGroup.editions.isEmpty())
  }

  @Test
  fun verifyRtlFlagsAndNativeScriptNames() {
    for (group in QulTranslationCatalog.languageGroups.filter { !it.isMissingInSource }) {
      assertTrue("Native name must not be blank for ${group.languageNameEn}", group.languageNameNative.isNotBlank())
      val shouldBeRtl = expectedRtlLanguages.contains(group.languageNameEn)
      assertEquals(
        "RTL mismatch for ${group.languageNameEn}",
        shouldBeRtl,
        group.isRtl
      )
      for (ed in group.editions) {
        assertEquals(shouldBeRtl, QulTranslationCatalog.isRtlEdition(ed.editionKey))
      }
    }
  }

  @Test
  fun verifyExistingEnglishUrduAndArabicPreserved() {
    assertEquals(8, ShiaTranslationDataProvider.sunniEditions.size)
    assertEquals(5, ShiaTranslationDataProvider.shiaEditions.size)
    assertEquals("en.sahih", ShiaTranslationDataProvider.sunniEditions.first().key)
    assertEquals("en.qarai", ShiaTranslationDataProvider.shiaEditions.first().key)

    // Verify locale fallback preserves existing English/Urdu defaults
    assertEquals("en.sahih", QulTranslationCatalog.resolveDefaultEditionForLocale(Locale.ENGLISH, "SUNNI"))
    assertEquals("en.qarai", QulTranslationCatalog.resolveDefaultEditionForLocale(Locale.ENGLISH, "SHIA"))
    assertEquals("qul.fa.135", QulTranslationCatalog.resolveDefaultEditionForLocale(Locale("fa"), "SUNNI"))
    assertEquals("qul.tr.77", QulTranslationCatalog.resolveDefaultEditionForLocale(Locale("tr"), "SUNNI"))
  }

  @Test
  fun verifyAll127EditionsLoadOfflineVersesFromBundledAsset() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals(127, QulTranslationCatalog.allEditions.size)

    for (edition in QulTranslationCatalog.allEditions) {
      val fatihah1 = QulOfflineTranslationManager.getOfflineVerseText(
        context = context,
        editionKey = edition.editionKey,
        surahNumber = 1,
        verseNumber = 1
      )
      assertNotNull("Offline Surah 1:1 must not be null for ${edition.editionKey} (${edition.languageNameEn})", fatihah1)
      assertFalse("Offline Surah 1:1 must not be blank for ${edition.editionKey}", fatihah1!!.isBlank())

      val ikhlas1 = QulOfflineTranslationManager.getOfflineVerseText(
        context = context,
        editionKey = edition.editionKey,
        surahNumber = 112,
        verseNumber = 1
      )
      assertNotNull("Offline Surah 112:1 must not be null for ${edition.editionKey}", ikhlas1)
      assertFalse("Offline Surah 112:1 must not be blank for ${edition.editionKey}", ikhlas1!!.isBlank())
    }
  }

  @Test
  fun verifyMultilingualOnboardingAndInterfaceLocalizationAndRtl() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 16 Full Interface Languages + 57 Content-Only QUL Languages = 73 total selectable languages
    assertEquals(16, AppLocalization.fullInterfaceLanguages.size)
    assertEquals(57, AppLocalization.contentOnlyLanguages.size)
    assertEquals(73, AppLocalization.allSelectableLanguages.size)

    // Verify Content-Only languages are never claimed as Full UI
    for (lang in AppLocalization.contentOnlyLanguages) {
      assertFalse("Content-only language ${lang.nameEnglish} must not claim full UI support", lang.hasFullInterfaceSupport)
      assertTrue(lang.coverageBadge.contains("Translation Only"))
    }

    // Verify RTL LayoutDirection for Arabic, Urdu, Persian, Pashto, and Perso-Arabic Kashmiri
    for (rtlCode in listOf("ar", "ur", "fa", "ps", "ks")) {
      assertTrue("Language $rtlCode must be RTL", AppLocalization.isRtlLanguage(rtlCode))
      assertEquals(LayoutDirection.Rtl, AppLocalization.getLayoutDirection(rtlCode))
    }
    for (ltrCode in listOf("en", "hi", "id", "ms", "tr", "fr", "es", "de", "ru", "zh", "bn")) {
      assertFalse("Language $ltrCode must be LTR", AppLocalization.isRtlLanguage(ltrCode))
      assertEquals(LayoutDirection.Ltr, AppLocalization.getLayoutDirection(ltrCode))
    }

    // Verify Hindi (हिन्दी) and Kashmiri (کٲشُر) localized strings & offline Quran translations
    val hiStrings = AppLocalization.getStrings("hi")
    assertEquals("मुखपृष्ठ", hiStrings.navHome)
    assertEquals("क़ुरआन", hiStrings.navQuran)
    val ksStrings = AppLocalization.getStrings("ks")
    assertEquals("قرآن", ksStrings.navQuran)

    val hindiFatihah = QulOfflineTranslationManager.getOfflineVerseText(context, "qul.hi.122", 1, 1)
    val kashmiriFatihah = QulOfflineTranslationManager.getOfflineVerseText(context, "qul.ks.273", 1, 1)
    assertNotNull("Hindi offline verse 1:1 must exist", hindiFatihah)
    assertNotNull("Kashmiri offline verse 1:1 must exist", kashmiriFatihah)
    assertTrue(hindiFatihah!!.isNotBlank())
    assertTrue(kashmiriFatihah!!.isNotBlank())
  }

  @Test
  fun verifyLanguageOnboardingAndReadingProgressPersistence() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val initial = PrayerPreferencesManager.loadConfig(context)
    assertFalse("Fresh install should not have completed language onboarding yet", initial.hasCompletedLanguageOnboarding)

    val updated = initial.copy(
      appInterfaceLanguageCode = "hi",
      hasCompletedLanguageOnboarding = true,
      lastReadSurahNumber = 36,
      lastReadVerseNumber = 12,
      lastReadSurahNameEn = "Ya-Sin",
      lastReadSurahNameAr = "يس",
      lastReadTotalVerses = 83,
      lastReadTradition = "SUNNI",
      lastReadTimestamp = 1730000000000L
    )
    PrayerPreferencesManager.saveConfig(context, updated)

    val reloaded = PrayerPreferencesManager.loadConfig(context)
    assertTrue("Onboarding completion must persist", reloaded.hasCompletedLanguageOnboarding)
    assertEquals("hi", reloaded.appInterfaceLanguageCode)
    assertEquals(36, reloaded.lastReadSurahNumber)
    assertEquals(12, reloaded.lastReadVerseNumber)
    assertEquals("Ya-Sin", reloaded.lastReadSurahNameEn)
    assertEquals("يس", reloaded.lastReadSurahNameAr)
    assertEquals(83, reloaded.lastReadTotalVerses)
    assertEquals("SUNNI", reloaded.lastReadTradition)
  }
}
