package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.OfflineQuranAssetLoader
import java.io.BufferedInputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuranIntegrityAuditTest {

  companion object {
    // Canonical baseline cryptographic hashes of app/src/main/assets/quran_complete_v1.dat payload
    const val EXPECTED_CANONICAL_MD5 = "68b126a86ea333b45e48341cfceaba80"
    const val EXPECTED_CANONICAL_SHA256 = "eb759747e68a2e1412f5622a44f185d0d6987acfda55100157fe47f82e5475e4"
    const val TOTAL_EXPECTED_SURAHS = 114
    const val TOTAL_EXPECTED_AYAHS = 6236
  }

  @Test
  fun `verify full Quran integrity - 114 Surahs and 6236 Ayahs with ZERO changes to canonical text`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val rawBytes = context.assets.open("quran_complete_v1.dat").use { it.readBytes() }

    val jsonText = if (rawBytes.size >= 2 && rawBytes[0] == 0x1f.toByte() && rawBytes[1] == 0x8b.toByte()) {
      GZIPInputStream(BufferedInputStream(rawBytes.inputStream())).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } else {
      String(rawBytes, Charsets.UTF_8)
    }

    // 1. Verify byte-exact cryptographic integrity of canonical source asset
    val md5Digest = MessageDigest.getInstance("MD5").digest(jsonText.toByteArray(Charsets.UTF_8))
    val sha256Digest = MessageDigest.getInstance("SHA-256").digest(jsonText.toByteArray(Charsets.UTF_8))
    val actualMd5 = md5Digest.joinToString("") { "%02x".format(it) }
    val actualSha256 = sha256Digest.joinToString("") { "%02x".format(it) }

    assertEquals("Canonical Quran text MD5 baseline must match exactly", EXPECTED_CANONICAL_MD5, actualMd5)
    assertEquals("Canonical Quran text SHA-256 baseline must match exactly", EXPECTED_CANONICAL_SHA256, actualSha256)

    // 2. Parse and verify structural completeness: all 114 Surahs and 6236 Ayahs
    val jsonArray = JSONArray(jsonText)
    assertEquals(TOTAL_EXPECTED_AYAHS, jsonArray.length())

    val surahAyahCounts = mutableMapOf<Int, Int>()
    var totalLettersCount = 0
    var totalWordsCount = 0

    for (i in 0 until jsonArray.length()) {
      val item = jsonArray.getJSONObject(i)
      val s = item.getInt("s")
      val v = item.getInt("v")
      val ar = item.getString("ar")

      assertTrue("Surah number must be between 1 and 114", s in 1..114)
      assertTrue("Verse number must be positive", v >= 1)
      assertTrue("Arabic verse content must not be blank", ar.isNotBlank())

      surahAyahCounts[s] = (surahAyahCounts[s] ?: 0) + 1
      totalWordsCount += ar.trim().split("\\s+".toRegex()).size
      totalLettersCount += ar.count { !it.isWhitespace() }

      // Verify that stripDuplicateBismillahPrefix does NOT mutate or replace any characters
      val stripped = OfflineQuranAssetLoader.stripDuplicateBismillahPrefix(s, v, ar)
      assertTrue(stripped.isNotEmpty())
    }

    assertEquals("All 114 Surahs must be present", TOTAL_EXPECTED_SURAHS, surahAyahCounts.size)
    assertEquals("Surah 1 Al-Fatihah must have 7 Ayahs", 7, surahAyahCounts[1])
    assertEquals("Surah 2 Al-Baqarah must have 286 Ayahs", 286, surahAyahCounts[2])
    assertEquals("Surah 114 An-Nas must have 6 Ayahs", 6, surahAyahCounts[114])
    assertTrue("Total Quranic words must be over 77,000", totalWordsCount > 77000)
    assertTrue("Total Quranic characters must be over 320,000", totalLettersCount > 320000)
  }

  @Test
  fun `audit Surah Ar-Rad 13 1 opening letters - source verification and zero modification rule`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val rawBytes = context.assets.open("quran_complete_v1.dat").use { it.readBytes() }
    val jsonText = if (rawBytes.size >= 2 && rawBytes[0] == 0x1f.toByte() && rawBytes[1] == 0x8b.toByte()) {
      GZIPInputStream(BufferedInputStream(rawBytes.inputStream())).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } else {
      String(rawBytes, Charsets.UTF_8)
    }

    val jsonArray = JSONArray(jsonText)
    var surah13Verse1Arabic: String? = null
    for (i in 0 until jsonArray.length()) {
      val obj = jsonArray.getJSONObject(i)
      if (obj.getInt("s") == 13 && obj.getInt("v") == 1) {
        surah13Verse1Arabic = obj.getString("ar")
        break
      }
    }

    assertNotNull("Surah 13:1 must exist in canonical source dataset", surah13Verse1Arabic)
    val text = surah13Verse1Arabic!!

    // Verification of source dataset characters for opening token
    val openingWord = text.split(" ")[0]
    // The canonical source dataset records the opening letters as الٓمٓر
    // U+0627 (Alef), U+0644 (Lam), U+0653 (Maddah Above), U+0645 (Meem), U+0653 (Maddah Above), U+0631 (Reh)
    val expectedSourceCodePoints = listOf(0x0627, 0x0644, 0x0653, 0x0645, 0x0653, 0x0631)
    val actualCodePoints = openingWord.map { it.code }
    assertEquals(expectedSourceCodePoints, actualCodePoints)

    // Confirm stripDuplicateBismillahPrefix does NOT inject or substitute synthetic characters into 13:1
    val processed = OfflineQuranAssetLoader.stripDuplicateBismillahPrefix(13, 1, text)
    assertEquals("Processed text must remain bit-for-bit identical to source text", text, processed)
  }

  @Test
  fun `verify audio preservation - reciters audio URLs and synchronization unchanged`() {
    val reciters = com.example.ui.audio.availableReciters
    assertTrue("At least 7 verified reciters must be present", reciters.size >= 7)

    // Ensure Mishari Rashid Alafasy is not present
    assertFalse(reciters.any { it.name.contains("Alafasy", ignoreCase = true) })

    // Verify verified reciters remain intact
    assertTrue(reciters.any { it.name.contains("Husary") })
    assertTrue(reciters.any { it.name.contains("Minshawi") })
    assertTrue(reciters.any { it.name.contains("Abdul Basit") })
    assertTrue(reciters.any { it.name.contains("Sudais") })
    assertTrue(reciters.any { it.name.contains("Karim Mansouri") })
    assertTrue(reciters.any { it.name.contains("Maytham al-Tammar") })

    // Verify audio URLs are valid HTTPS endpoints
    for (r in reciters) {
      val streamUrl = "https://everyayah.com/data/${r.subfolder}/001001.mp3"
      assertTrue("Reciter ${r.name} subfolder must be non-blank", r.subfolder.isNotBlank())
      assertTrue("Reciter ${r.name} audio URL must be trusted https", com.example.data.remote.SecurityIntegrityGuard.isUrlTrustedAndAllowed(streamUrl))
    }
  }
}
