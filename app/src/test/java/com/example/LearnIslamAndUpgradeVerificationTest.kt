package com.example

import com.example.data.local.QulTranslationCatalog
import com.example.data.model.DailyDuasDataProvider
import com.example.data.model.LearnIslamCurriculumData
import com.example.data.prayer.PrayerAndAlarmConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearnIslamAndUpgradeVerificationTest {

  @Test
  fun verifyNooraniQaidaCurriculumStructureAndLessons() {
    val lessons = LearnIslamCurriculumData.nooraniQaidaLessons
    assertEquals(17, lessons.size)

    // Lesson 1: Mufradat (29 single letters & Makharij)
    val lesson1 = lessons.first { it.lessonNumber == 1 }
    assertTrue(lesson1.titleEnglish.contains("Single Arabic Letters", ignoreCase = true))
    assertEquals(29, lesson1.practiceItems.size)
    assertTrue(lesson1.quizQuestions.isNotEmpty())

    // Verify key Tajweed & Darasgah progression lessons exist
    assertTrue(lessons.any { it.titleEnglish.contains("Murakkabat", ignoreCase = true) })
    assertTrue(lessons.any { it.titleEnglish.contains("Muqatta'at", ignoreCase = true) })
    assertTrue(lessons.any { it.titleEnglish.contains("Harakat", ignoreCase = true) })
    assertTrue(lessons.any { it.titleEnglish.contains("Tanween", ignoreCase = true) })
    assertTrue(lessons.any { it.titleEnglish.contains("Maddah", ignoreCase = true) })
    assertTrue(lessons.any { it.titleEnglish.contains("Sukoon", ignoreCase = true) })
    assertTrue(lessons.any { it.titleEnglish.contains("Shaddah", ignoreCase = true) })
    assertTrue(lessons.any { it.titleEnglish.contains("Noon Sakinah", ignoreCase = true) && it.ruleSummaryEnglish.contains("Ikhfa", ignoreCase = true) })
    assertTrue(lessons.any { it.titleEnglish.contains("Waqf", ignoreCase = true) })

    // Every practice item must have non-blank Arabic, transliteration, Urdu, and English explanations
    lessons.forEach { lesson ->
      assertTrue("Lesson ${lesson.lessonNumber} must have practice items", lesson.practiceItems.isNotEmpty())
      lesson.practiceItems.forEach { item ->
        assertTrue(item.arabicText.isNotBlank())
        assertTrue(item.transliteration.isNotBlank())
        assertTrue(item.makhrajOrRuleEnglish.isNotBlank())
        assertTrue(item.makhrajOrRuleUrdu.isNotBlank())
      }
    }
  }

  @Test
  fun verifySixKalimasAndImanEssentials() {
    val imanItems = LearnIslamCurriculumData.imanAndKalimaItems
    assertEquals(9, imanItems.size)

    // Verify all 6 Kalimas + Iman-e-Mujmal + Iman-e-Mufassal + 5 Pillars
    assertTrue(imanItems.any { it.id == "kalima_1_tayyibah" && it.titleEnglish.contains("Tayyibah", ignoreCase = true) })
    assertTrue(imanItems.any { it.id == "kalima_2_shahadah" && it.titleEnglish.contains("Shahadah", ignoreCase = true) })
    assertTrue(imanItems.any { it.id == "kalima_3_tamjeed" && it.titleEnglish.contains("Tamjeed", ignoreCase = true) })
    assertTrue(imanItems.any { it.id == "kalima_4_tawheed" && it.titleEnglish.contains("Tawheed", ignoreCase = true) })
    assertTrue(imanItems.any { it.id == "kalima_5_istighfar" && it.titleEnglish.contains("Istighfar", ignoreCase = true) })
    assertTrue(imanItems.any { it.id == "kalima_6_radde_kufr" && it.titleEnglish.contains("Radd-e-Kufr", ignoreCase = true) })
    assertTrue(imanItems.any { it.id == "iman_mujmal" })
    assertTrue(imanItems.any { it.id == "iman_mufassal" })
    assertTrue(imanItems.any { it.id == "five_pillars_of_islam" })

    imanItems.forEach { item ->
      assertTrue(item.arabicText.isNotBlank())
      assertTrue(item.transliteration.isNotBlank())
      assertTrue(item.urduTranslation.isNotBlank())
      assertTrue(item.englishTranslation.isNotBlank())
      assertTrue(item.authenticReference.isNotBlank())
    }
  }

  @Test
  fun verifyWuduAndNamazMutarjamAndScholarlyDifferences() {
    val salahSteps = LearnIslamCurriculumData.wuduAndSalahSteps
    assertEquals(14, salahSteps.size)

    // Verify Wudu, Sana, Ta'awwudh, Fatiha, Short Surahs, Ruku, Sajdah, Tashahhud, Durood Ibrahim, Dua after Durood, Salam, Salat al-Janaza
    assertTrue(salahSteps.any { it.section == "WUDU" })
    assertTrue(salahSteps.any { it.titleEnglish.contains("Sana", ignoreCase = true) })
    assertTrue(salahSteps.any { it.titleEnglish.contains("Ta'awwudh", ignoreCase = true) })
    assertTrue(salahSteps.any { it.titleEnglish.contains("Al-Fatiha", ignoreCase = true) })
    assertTrue(salahSteps.any { it.titleEnglish.contains("Ruku", ignoreCase = true) })
    assertTrue(salahSteps.any { it.titleEnglish.contains("Sajdah", ignoreCase = true) })
    assertTrue(salahSteps.any { it.titleEnglish.contains("Tashahhud", ignoreCase = true) })
    assertTrue(salahSteps.any { it.titleEnglish.contains("Durood-e-Ibrahimi", ignoreCase = true) })
    assertTrue(salahSteps.any { it.titleEnglish.contains("Salam", ignoreCase = true) })
    assertTrue(salahSteps.any { it.id == "namaz_step_10_salatul_janaza" && it.titleEnglish.contains("Janaza", ignoreCase = true) })

    // Verify scholarly differences are documented with school attribution and evidence
    val stepsWithScholarlyViews = salahSteps.filter { it.scholarlyDifferences.isNotEmpty() }
    assertTrue(stepsWithScholarlyViews.isNotEmpty())
    stepsWithScholarlyViews.flatMap { it.scholarlyDifferences }.forEach { diff ->
      assertTrue(diff.schoolOrTradition.isNotBlank())
      assertTrue(diff.positionSummaryEnglish.isNotBlank())
      assertTrue(diff.primarySourceReference.isNotBlank())
    }
  }

  @Test
  fun verifyDailyDuasSequenceAndIftariDuaSunanAbiDawud2357() {
    val dailyDuas = LearnIslamCurriculumData.dailyDuasInSequence
    assertEquals(14, dailyDuas.size)

    // Verify chronological sequence numbers are strictly ordered
    val orders = dailyDuas.map { it.sequenceOrder }
    assertEquals(orders.sorted(), orders)

    // Verify the Iftari / Breaking Fast dua specifically matches Sunan Abi Dawud 2357
    val iftariDua = dailyDuas.firstOrNull { it.duaId == "dua_ramadan_iftar" }
    assertNotNull(iftariDua)
    assertEquals(
      "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللّٰهُ",
      iftariDua!!.arabicText
    )
    assertTrue(
      iftariDua.transliteration.contains(
        "Zahaba al-zama'u wabtallatil-'urooqu wa thabatal-ajru in sha' Allah",
        ignoreCase = true
      )
    )
    assertEquals(
      "The thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
      iftariDua.englishTranslation
    )
    assertTrue(iftariDua.authenticReference.contains("Sunan Abi Dawud 2357"))

    // Also verify DailyDuasDataProvider Ramadan Iftar dua has the exact reference and wording
    val providerIftarDua = DailyDuasDataProvider.allDuas.firstOrNull { it.id == "dua_ramadan_iftar" }
    assertNotNull(providerIftarDua)
    assertTrue(providerIftarDua!!.authenticReference.contains("Sunan Abi Dawud 2357"))
    assertTrue(providerIftarDua.transliteration.contains("Zahaba al-zama'u wabtallatil-'urooqu"))
  }

  @Test
  fun verifySehriNiyatDistinguishesHeartIntentionFromCustomaryVerbalFormula() {
    val niyat = LearnIslamCurriculumData.sehriNiyatGuidance
    assertTrue(niyat.heartIntentionPrincipleEnglish.contains("heart", ignoreCase = true))
    assertTrue(niyat.propheticHadithReferences.any { it.contains("Sunan an-Nasa'i 2331") })
    assertTrue(niyat.propheticHadithReferences.any { it.contains("Sahih al-Bukhari 1") })
    assertTrue(niyat.propheticHadithReferences.any { it.contains("IslamQA Fatwa #37805") })

    // Must never label customary verbal formula as an authentic Hadith
    assertEquals(2, niyat.customaryFormulas.size)
    niyat.customaryFormulas.forEach { formula ->
      assertTrue(formula.classificationBadge.contains("NOT A HADITH", ignoreCase = true))
      assertTrue(formula.attributionAndStatusNote.contains("NOT a Prophetic", ignoreCase = true))
    }
  }

  @Test
  fun verifyDynamicTranslationCatalogAndMultiTranslatorSupport() {
    val languageCount = QulTranslationCatalog.computeAvailableLanguageCount()
    val editionCount = QulTranslationCatalog.computeTotalAvailableEditionsCount()
    assertTrue(languageCount >= 70)
    assertTrue(editionCount > languageCount)

    // Verify Urdu, English, Kashmiri, and Hindi attribution lookup
    val urAttr = QulTranslationCatalog.getAttributionDetailsForKey("ur.jalandhry")
    assertEquals("ur", urAttr.languageCode)
    assertTrue(urAttr.translatorName.contains("Jalandhry", ignoreCase = true))
    assertTrue(urAttr.sourceAttribution.isNotBlank())

    val enAttr = QulTranslationCatalog.getAttributionDetailsForKey("en.sahih")
    assertEquals("en", enAttr.languageCode)
    assertTrue(enAttr.translatorName.contains("Sahih International", ignoreCase = true))

    val ksAttr = QulTranslationCatalog.getAttributionDetailsForKey("qul.ks.273")
    assertEquals("ks", ksAttr.languageCode)
    assertEquals("Kashmiri", ksAttr.languageNameEn)

    val hiAttr = QulTranslationCatalog.getAttributionDetailsForKey("qul.hi.122")
    assertEquals("hi", hiAttr.languageCode)
    assertEquals("Hindi", hiAttr.languageNameEn)

    // Verify PrayerAndAlarmConfig supports multiple selectedTranslationKeys
    val defaultConfig = PrayerAndAlarmConfig()
    assertTrue(defaultConfig.selectedTranslationKeys.contains("en.sahih"))
    assertTrue(defaultConfig.selectedTranslationKeys.contains("ur.jalandhry"))
  }

  @Test
  fun verifyInvestigatedSourcesRegistryPermissions() {
    val sources = LearnIslamCurriculumData.verifiedSourceRegistry
    assertEquals(6, sources.size)

    // Verify Quran.com API v4, QUD timing, Sunnah.com, and Hisnul Muslim are documented
    assertTrue(sources.any { it.url.contains("api-docs.quran.com") })
    assertTrue(sources.any { it.url.contains("quranic-universal-audio") })
    assertTrue(sources.any { it.url.contains("hisnulmuslim.app") })
    assertTrue(sources.any { it.url.contains("sunnah.com") })
  }

  @Test
  fun verifyDeveloperOptionsApiSourcesAndLicencesAndZeroSecretExposure() {
    val devSources = com.example.data.repository.SourceRegistryRepository.getDeveloperApiSourcesAndLicences()
    assertEquals(38, devSources.size)

    // Verify all 6 categories are represented
    val categories = devSources.map { it.category }.toSet()
    assertTrue(categories.contains("Quran & Tafsir"))
    assertTrue(categories.contains("Hadith"))
    assertTrue(categories.contains("Prayer & Learning"))
    assertTrue(categories.contains("Audio"))
    assertTrue(categories.contains("Fonts & Icons"))
    assertTrue(categories.contains("Cloud & SDKs"))

    // Verify every item has complete fields, safe HTTPS URLs, masked config status, and zero raw secrets
    devSources.forEach { item ->
      assertTrue("Source name must be non-blank for ${item.id}", item.sourceName.isNotBlank())
      assertTrue("Purpose must be non-blank for ${item.id}", item.purpose.isNotBlank())
      assertTrue("Official doc URL must be HTTPS for ${item.id}", item.officialDocumentationUrl.startsWith("https://"))
      assertTrue(
        "Official doc URL must be allowed by SecurityIntegrityGuard: ${item.officialDocumentationUrl}",
        com.example.data.remote.SecurityIntegrityGuard.isExternalIntentSafe(item.officialDocumentationUrl)
      )
      assertTrue("Licence name must be non-blank for ${item.id}", item.applicableLicenceName.isNotBlank())
      assertTrue("Licence URL must be HTTPS for ${item.id}", item.applicableLicenceUrl.startsWith("https://"))
      assertTrue(
        "Licence URL must be allowed by SecurityIntegrityGuard: ${item.applicableLicenceUrl}",
        com.example.data.remote.SecurityIntegrityGuard.isExternalIntentSafe(item.applicableLicenceUrl)
      )
      assertTrue("Attribution requirements must be non-blank for ${item.id}", item.attributionRequirements.isNotBlank())
      assertTrue(
        "Configuration status must be strictly 'Configured' or 'Not configured' for ${item.id}, got '${item.configurationStatus}'",
        item.configurationStatus == "Configured" || item.configurationStatus == "Not configured"
      )

      // Never expose raw API keys, tokens, or query secrets anywhere in the record
      val combinedText = listOfNotNull(
        item.sourceName,
        item.purpose,
        item.officialDocumentationUrl,
        item.applicableLicenceName,
        item.applicableLicenceUrl,
        item.attributionRequirements,
        item.configurationStatus,
        item.missingDetailsNote
      ).joinToString(" ")
      assertTrue("Must never expose raw HadithAPI key prefix", !combinedText.contains("$2y$10$"))
      assertTrue("Must never expose raw UmmahAPI key prefix", !combinedText.contains("umh_"))
      assertTrue("Must never expose apiKey query parameter", !combinedText.contains("apiKey="))
    }

    // Verify bundled fonts & icons are documented
    assertTrue(devSources.any { it.id == "font_amiri_quran" && it.applicableLicenceName.contains("SIL Open Font License") })
    assertTrue(devSources.any { it.id == "font_scheherazade_new" && it.applicableLicenceName.contains("SIL Open Font License") })
    assertTrue(devSources.any { it.id == "font_noto_nastaliq_urdu" && it.applicableLicenceName.contains("SIL Open Font License") })
    assertTrue(devSources.any { it.id == "google_material_icons_compose" && it.applicableLicenceName.contains("Apache License 2.0") })
  }

  @Test
  fun verifyNooraniQaidaVoiceAndQuranMicrophoneRecordingEvaluation() {
    val lessons = LearnIslamCurriculumData.nooraniQaidaLessons
    val allItems = lessons.flatMap { it.practiceItems }
    assertEquals(149, allItems.size)

    // Verify all 149 Noorani Qaida items across all 17 lessons have offline human MP3 audio bundled in assets/qaida_audio/
    allItems.forEach { item ->
      assertTrue(
        "Bundled offline MP3 must exist in assets/qaida_audio/${item.id}.mp3",
        com.example.ui.audio.DuaAudioController.hasOfflineQaidaAudioAsset(null, item.id)
      )
      val vocalized = com.example.ui.audio.DuaAudioController.buildNooraniQaidaVocalizedArabicForMaleTts(
        id = item.id,
        arabicText = item.arabicText,
        transliteration = item.transliteration
      )
      assertTrue("Vocalized text must be non-blank for ${item.id}", vocalized.isNotBlank())
    }

    // Verify Quran microphone recording & cadence evaluation produces full word-by-word grading
    val fatihaVerse1 = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ"
    val cadenceEval = com.example.ui.quran.TajweedCorrectionEngine.evaluateRecordedAudioCadence(
      canonicalVerseText = fatihaVerse1,
      surah = 1,
      ayah = 1,
      recordingDurationMs = 2400L,
      peakVoiceLevel = 0.72f
    )
    assertEquals(4, cadenceEval.totalCount)
    assertEquals(4, cadenceEval.matchedCount)
    assertEquals(100, cadenceEval.accuracyPercentage)
    assertTrue(cadenceEval.feedbackSummary.contains("Microphone Recording Complete"))
  }

  @Test
  fun verifyIntelligentDuaContentAndAudioAuditCompleteness() {
    val allDuas = DailyDuasDataProvider.allDuas
    assertEquals(67, allDuas.size)

    // Verify every single Dua has Arabic, Roman Urdu transliteration, English translation, Urdu translation, and authentic reference
    allDuas.forEach { dua ->
      assertTrue("Arabic text must be non-blank for ${dua.id}", dua.arabicText.isNotBlank())
      assertTrue("Transliteration (Roman Urdu) must be non-blank for ${dua.id}", dua.transliteration.isNotBlank())
      assertTrue("English translation must be non-blank for ${dua.id}", dua.englishTranslation.isNotBlank())
      assertTrue("Urdu translation must be non-blank for ${dua.id}", dua.urduTranslation.isNotBlank())
      assertTrue("Authentic reference must be non-blank for ${dua.id}", dua.authenticReference.isNotBlank())
    }

    // Verify 100% of all 67 Duas have verified human audio (offline asset, Quranic Ayah audio, or verified human MP3 URL)
    val duasWithVerifiedAudio = allDuas.count {
      it.quranSurahAyah != null ||
        !it.humanAudioUrl.isNullOrBlank() ||
        com.example.ui.audio.DuaAudioController.hasOfflineQaidaAudioAsset(null, it.id)
    }
    assertEquals("Expected all 67 Duas to have verified human audio", allDuas.size, duasWithVerifiedAudio)

    // Verify 100% of all 9 Iman & 6 Kalima items have both humanAudioUrl and bundled offline MP3
    val imanItems = LearnIslamCurriculumData.imanAndKalimaItems
    assertEquals(9, imanItems.size)
    imanItems.forEach { item ->
      assertTrue("Iman/Kalima item ${item.id} must have humanAudioUrl", !item.humanAudioUrl.isNullOrBlank())
      assertTrue(
        "Iman/Kalima item ${item.id} must have bundled offline MP3",
        com.example.ui.audio.DuaAudioController.hasOfflineQaidaAudioAsset(null, item.id)
      )
    }

    // Verify 100% of all 14 Wudu & Namaz-e-Mutarjam steps have humanAudioUrl and bundled offline MP3
    val salahSteps = LearnIslamCurriculumData.wuduAndSalahSteps
    assertEquals(14, salahSteps.size)
    salahSteps.forEach { step ->
      assertTrue("Wudu/Salah step ${step.id} must have humanAudioUrl", !step.humanAudioUrl.isNullOrBlank())
      assertTrue(
        "Wudu/Salah step ${step.id} must have bundled offline MP3",
        com.example.ui.audio.DuaAudioController.hasOfflineQaidaAudioAsset(null, step.id)
      )
    }

    // Verify After-Salah Adhkar section (6 duas)
    val afterSalahDuas = allDuas.filter { it.category == com.example.data.model.DuaCategory.AFTER_SALAH }
    assertEquals(6, afterSalahDuas.size)
    assertTrue(afterSalahDuas.any { it.id == "dua_after_salah_istighfar_salam" })
    assertTrue(afterSalahDuas.any { it.id == "dua_after_salah_tahlil_la_mani" })
    assertTrue(afterSalahDuas.any { it.id == "dua_after_salah_ainni_dhikrika" })
    assertTrue(afterSalahDuas.any { it.id == "dua_after_salah_tasbih_fatimi" })
    assertTrue(afterSalahDuas.any { it.id == "dua_after_salah_fajr_ilman_nafian" })
    assertTrue(afterSalahDuas.any { it.id == "dua_after_adhan_wasilah" })

    // Verify Janaza & Funeral section (5 duas) including both masculine & feminine Arabic pronouns for minor boy & girl
    val janazaDuas = allDuas.filter { it.occasionType == com.example.data.model.SpecialOccasionType.JANAZA_FUNERAL }
    assertEquals(5, janazaDuas.size)
    val childDua = janazaDuas.firstOrNull { it.id == "dua_janaza_child_boy_girl" }
    assertNotNull(childDua)
    assertTrue(childDua!!.arabicText.contains("فَرَطًا"))
    assertTrue(childDua.arabicText.contains("اجْعَلْهُ"))
    assertTrue(childDua.arabicText.contains("اجْعَلْهَا"))

    // Verify candidate mirror URL builder produces Primary + Archive + OpenDua mirrors
    val mirrors = com.example.ui.audio.DuaAudioController.buildCandidateHumanAudioUrls(
      "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/65hm.mp3"
    )
    assertTrue(mirrors.any { it.contains("65hm.mp3") })
    assertTrue(mirrors.any { it.contains("HisnulMuslimAudio_201510/n65.mp3") })
    assertTrue(mirrors.any { it.contains("audio.opendua.org/hisn/v0.0.2/OD-065.mp3") })

    // Verify the Intelligent Dua Content & Audio Audit Report is populated
    val auditReport = com.example.data.repository.SourceRegistryRepository.getDuaContentAndAudioAuditReport()
    assertEquals(5, auditReport.size)
    assertTrue(auditReport.any { it.summary.contains("779") && it.summary.contains("222") })
  }
}

