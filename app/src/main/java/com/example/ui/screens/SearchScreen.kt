package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.SurahEntity
import com.example.ui.components.SahihGradeBadge
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodels.SearchCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
  searchQuery: String,
  searchCategory: SearchCategory,
  ayahResults: List<AyahEntity>,
  hadithResults: List<HadithEntity>,
  surahs: List<SurahEntity>,
  tafsirResults: List<com.example.data.repository.AyahTafsirResult> = emptyList(),
  isSearchingRemote: Boolean = false,
  onSearchQueryChanged: (String) -> Unit,
  onSearchCategoryChanged: (SearchCategory) -> Unit,
  onOpenSurah: (SurahEntity) -> Unit,
  onOpenHadith: (HadithEntity) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var hadithTraditionScope by remember { mutableStateOf("BOTH") }

  val scopedHadithResults = remember(hadithResults, hadithTraditionScope) {
    if (hadithTraditionScope == "BOTH") {
      hadithResults
    } else {
      val allowedKeys = ExtendedHadithDataProvider.primaryCollections
        .filter { it.tradition == hadithTraditionScope }
        .map { it.key }
        .toSet()
      hadithResults.filter { it.collectionKey in allowedKeys }
    }
  }

  val scopedTafsirResults = remember(tafsirResults, hadithTraditionScope) {
    when (hadithTraditionScope) {
      "SUNNI" -> tafsirResults.filter { !it.edition.eraBadge.contains("Shia", ignoreCase = true) }
      "SHIA" -> tafsirResults.filter { it.edition.eraBadge.contains("Shia", ignoreCase = true) }
      else -> tafsirResults
    }
  }

  val suggestions = listOf(
    "2:255",
    "36:1",
    "Al-Kafi 1",
    "Nahj al-Balagha 1",
    "Sermon 193",
    "Sermon 3",
    "Letter 53",
    "Khisal 1",
    "Imam Sadiq",
    "Bukhari 5027",
    "Muslim 223",
    "Mercy",
    "Knowledge"
  )

  Column(modifier = modifier.fillMaxSize()) {
    // Search input field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChanged,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("unified_search_input"),
      placeholder = {
        Text(
          text = "Search Quran (2:255), search any hadees (Al-Kafi, Bukhari), or text...",
          fontSize = 12.sp,
          maxLines = 1
        )
      },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      trailingIcon = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isSearchingRemote) {
            CircularProgressIndicator(
              modifier = Modifier
                .size(18.dp)
                .padding(end = 4.dp),
              strokeWidth = 2.dp
            )
          }
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChanged("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear search")
            }
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp)
    )

    // Category filter chips (Scrollable LazyRow so chips never wrap vertically)
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      item {
        FilterChip(
          selected = searchCategory == SearchCategory.ALL,
          onClick = { onSearchCategoryChanged(SearchCategory.ALL) },
          label = { Text("All Records", fontSize = 11.sp, maxLines = 1, softWrap = false) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldPrimary,
            selectedLabelColor = Color.White
          )
        )
      }
      item {
        FilterChip(
          selected = searchCategory == SearchCategory.QURAN,
          onClick = { onSearchCategoryChanged(SearchCategory.QURAN) },
          label = { Text("Quran (6,236 Verses)", fontSize = 11.sp, maxLines = 1, softWrap = false) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldPrimary,
            selectedLabelColor = Color.White
          )
        )
      }
      item {
        FilterChip(
          selected = searchCategory == SearchCategory.HADITH,
          onClick = { onSearchCategoryChanged(SearchCategory.HADITH) },
          label = {
            Text(
              text = if (hadithTraditionScope == "SHIA") {
                "Hadith (Shia Canonical & In-Book)"
              } else {
                "Hadith (Sunni & Shia Libraries)"
              },
              fontSize = 11.sp,
              maxLines = 1,
              softWrap = false
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldPrimary,
            selectedLabelColor = Color.White
          )
        )
      }
      item {
        FilterChip(
          selected = searchCategory == SearchCategory.TAFSIR,
          onClick = { onSearchCategoryChanged(SearchCategory.TAFSIR) },
          label = { Text("Tafsir (14 Exegeses)", fontSize = 11.sp, maxLines = 1, softWrap = false) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldPrimary,
            selectedLabelColor = Color.White
          )
        )
      }
    }

    // Tradition Scope Filter (Search Sunni, Shia, or Both Libraries across Hadith & Tafsir)
    if (searchCategory == SearchCategory.ALL || searchCategory == SearchCategory.HADITH || searchCategory == SearchCategory.TAFSIR) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = hadithTraditionScope == "BOTH",
          onClick = { hadithTraditionScope = "BOTH" },
          label = { Text("Both Traditions", fontSize = 11.sp, maxLines = 1, softWrap = false) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
          ),
          modifier = Modifier.weight(1f).testTag("search_hadith_scope_both")
        )
        FilterChip(
          selected = hadithTraditionScope == "SUNNI",
          onClick = { hadithTraditionScope = "SUNNI" },
          label = { Text("Sunni Only", fontSize = 11.sp, maxLines = 1, softWrap = false) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldPrimary,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.weight(1f).testTag("search_hadith_scope_sunni")
        )
        FilterChip(
          selected = hadithTraditionScope == "SHIA",
          onClick = { hadithTraditionScope = "SHIA" },
          label = { Text("Shia Only", fontSize = 11.sp, maxLines = 1, softWrap = false) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GoldPrimary,
            selectedLabelColor = Color.Black
          ),
          modifier = Modifier.weight(1f).testTag("search_hadith_scope_shia")
        )
      }
    }

    if (searchQuery.isBlank()) {
      // Prompt with suggested topics & direct reference examples
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          modifier = Modifier.size(56.dp),
          tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Unified Quran (6,236 Verses), Sunni/Shia Hadith, Sermons & Tafsir",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Search any verse by Surah:Ayah (e.g. 2:255, 36:1), any Hadith or Sermon by collection & number (e.g. Al-Kafi 1, Sermon 193, Letter 53, Bukhari 5027), or keywords in Arabic, English, and Urdu.",
          style = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "Try Quick Reference & Topic Searches:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(suggestions) { topic ->
            Surface(
              modifier = Modifier
                .clickable { onSearchQueryChanged(topic) }
                .testTag("search_suggestion_${topic.replace(" ", "_")}"),
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = topic,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }
          }
        }
      }
    } else {
      val showAyahs = searchCategory == SearchCategory.ALL || searchCategory == SearchCategory.QURAN
      val showHadiths = searchCategory == SearchCategory.ALL || searchCategory == SearchCategory.HADITH
      val showTafsirs = searchCategory == SearchCategory.ALL || searchCategory == SearchCategory.TAFSIR

      val totalCount = (if (showAyahs) ayahResults.size else 0) +
        (if (showHadiths) scopedHadithResults.size else 0) +
        (if (showTafsirs) scopedTafsirResults.size else 0)

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Found $totalCount verified results for \"$searchQuery\"",
              style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }

        if (showAyahs && ayahResults.isNotEmpty()) {
          item {
            Text(
              text = "Quran Verses — Sahih International & Uthmani (${ayahResults.size})",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.padding(top = 8.dp)
            )
          }

          items(ayahResults, key = { "search_ayah_${it.surahNumber}_${it.verseNumber}" }) { ayah ->
            val surah = surahs.find { it.number == ayah.surahNumber }
            val surahName = surah?.nameEnglish ?: "Surah ${ayah.surahNumber}"

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { surah?.let { onOpenSurah(it) } },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "$surahName ${ayah.surahNumber}:${ayah.verseNumber}",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary
                    )
                  )
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = "Sahih International • Open Surah",
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = ayah.textArabic,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = 20.sp,
                    lineHeight = 34.sp,
                    textAlign = TextAlign.Right
                  ),
                  modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = ayah.translationEnglish,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = ayah.translationUrdu,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = com.example.ui.theme.UrduNastaliqFontFamily,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }
        }

        if (showHadiths && scopedHadithResults.isNotEmpty()) {
          item {
            Text(
              text = if (hadithTraditionScope == "SHIA") {
                "Shia Hadith Narrations (${scopedHadithResults.size})"
              } else {
                "Hadith Narrations — Canonical & In-Book Editions (${scopedHadithResults.size})"
              },
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.padding(top = 12.dp)
            )
          }

          items(scopedHadithResults, key = { "search_hadith_${it.collectionKey}_${it.hadithNumber}" }) { hadith ->
            val isShia = ExtendedHadithDataProvider.isShiaCollection(hadith.collectionKey)
            val primaryLabel = if (isShia) {
              ExtendedHadithDataProvider.formatPrimaryEditionBadge(hadith)
            } else {
              hadith.darussalamReference.ifBlank { "Darussalam: #${hadith.hadithNumber}" }
            }
            val inBookLabel = hadith.inBookReference.ifBlank { "In-Book: Not available in this source" }
            val collectionMeta = ExtendedHadithDataProvider.primaryCollections.find { it.key == hadith.collectionKey }
            val traditionLabel = if (collectionMeta?.tradition == "SHIA") {
              "SHIA • ${collectionMeta.subTradition}"
            } else {
              "SUNNI • ${collectionMeta?.subTradition ?: "Ahl al-Sunnah"}"
            }

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenHadith(hadith) },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${hadith.collectionName} • ${hadith.reference}",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                  SahihGradeBadge(grade = hadith.grade.ifBlank { "Not graded in this source" })
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Separate Clickable Edition Numbering + Tradition Badges
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (collectionMeta?.tradition == "SHIA") GoldPrimary.copy(alpha = 0.22f) else EmeraldPrimary.copy(alpha = 0.18f)
                  ) {
                    Text(
                      text = traditionLabel,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (collectionMeta?.tradition == "SHIA") GoldPrimary else EmeraldPrimary
                      )
                    )
                  }

                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.14f),
                    modifier = Modifier.clickable {
                      onSearchQueryChanged("${hadith.collectionKey} ${hadith.hadithNumber}")
                    }
                  ) {
                    Text(
                      text = primaryLabel,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                  }

                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldPrimary.copy(alpha = 0.16f)
                  ) {
                    Text(
                      text = inBookLabel,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = "Narrator: ${hadith.narrator} • Grader: ${hadith.gradingScholar}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                  )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = hadith.arabicText,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = 19.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Right
                  ),
                  modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = hadith.englishText,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                )

                Text(
                  text = hadith.urduText,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = com.example.ui.theme.UrduNastaliqFontFamily,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }
        }

        if (showTafsirs && scopedTafsirResults.isNotEmpty()) {
          item {
            Text(
              text = if (hadithTraditionScope == "SHIA") {
                "Shia Exegesis & Commentary Matches (${scopedTafsirResults.size})"
              } else if (hadithTraditionScope == "SUNNI") {
                "Sunni Exegesis & Commentary Matches (${scopedTafsirResults.size})"
              } else {
                "Classical & Contemporary Tafsir Commentary Matches (${scopedTafsirResults.size})"
              },
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.padding(top = 10.dp)
            )
          }

          items(scopedTafsirResults, key = { "search_tafsir_${it.edition.key}_${it.verseRef}" }) { tafsir ->
            val parts = tafsir.verseRef.split(":")
            val sNum = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val surah = surahs.find { it.number == sNum }

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { surah?.let { onOpenSurah(it) } },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "${tafsir.edition.titleEnglish} • Surah ${tafsir.verseRef}",
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (tafsir.edition.eraBadge.contains("Shia", ignoreCase = true)) GoldPrimary else EmeraldPrimary
                      )
                    )
                    Text(
                      text = "${tafsir.edition.scholarName} (${tafsir.edition.eraBadge})",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = "Open in Reader",
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = tafsir.commentaryEnglish,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                  maxLines = 4
                )

                if (tafsir.commentaryUrdu.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = tafsir.commentaryUrdu,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = com.example.ui.theme.UrduNastaliqFontFamily,
                      fontSize = 13.sp,
                      textAlign = TextAlign.Right,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                  )
                }
              }
            }
          }
        }

        if (totalCount == 0) {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "No matching records found in the installed and indexed sources for \"$searchQuery\"",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Search across 6,236 Quran verses, 14 Tafsir exegeses, and Kutub al-Sittah / Shia Four Books by verse reference (e.g. 2:255), Hadith reference (e.g. Bukhari 5027, Al-Kafi 1), or broader English, Urdu, or Arabic terms.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}
