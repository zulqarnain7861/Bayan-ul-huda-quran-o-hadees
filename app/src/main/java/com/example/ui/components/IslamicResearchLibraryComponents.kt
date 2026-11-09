package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AzkarClosingNote
import com.example.data.model.AzkarIntroItem
import com.example.data.model.FardNamazAzkarPamphlet
import com.example.data.model.FardNamazZikrItem
import com.example.data.model.IslamicResearchRepository
import com.example.data.model.RafziyatNasbiyatYazeediyatPaper
import com.example.data.model.ResearchPaperItem
import com.example.data.model.ResearchPaperSection
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IslamicResearchHubView(
  onAddStudyNote: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val paper1Wasiyat = remember(context) { IslamicResearchRepository.loadPaper1Wasiyat(context) }
  val paper5a = remember(context) { IslamicResearchRepository.loadPaper5a(context) }
  val paper7Commandments = remember(context) { IslamicResearchRepository.loadPaper7Commandments(context) }
  val fardNamazAzkar = remember(context) { IslamicResearchRepository.loadFardNamazAzkar(context) }

  var activeDocumentId by remember { mutableStateOf<String?>(null) } // "paper_1_wasiyat", "paper_5a", "paper_7_commandments", "fard_namaz_azkar"
  var selectedCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "PAPER_1", "PAPER_5A", "PAPER_7", "AZKAR"
  var initialSectionOrZikrIndex by remember { mutableIntStateOf(0) }
  var researchSearchQuery by remember { mutableStateOf("") }
  var hubFontScaleStep by remember { mutableIntStateOf(0) }
  val hubMuhasabahCheckedMap = remember { mutableStateMapOf<String, Boolean>() }
  val hubTasbihCounts = remember { mutableStateMapOf<Int, Int>() }
  val hubArabicBoost = hubFontScaleStep * 2
  val hubUrduBoost = hubFontScaleStep * 2

  val searchHits = remember(researchSearchQuery, context) {
    IslamicResearchRepository.searchAcrossIslamicResearch(researchSearchQuery, context)
  }

  if (activeDocumentId == "paper_1_wasiyat") {
    BackHandler { activeDocumentId = null }
    Paper1WasiyatInteractiveReaderView(
      paper = paper1Wasiyat,
      initialSectionIndex = initialSectionOrZikrIndex,
      onClose = { activeDocumentId = null },
      onAddNote = onAddStudyNote
    )
    return
  }

  if (activeDocumentId == "paper_5a") {
    BackHandler { activeDocumentId = null }
    Paper5aInteractiveReaderView(
      paper = paper5a,
      initialSectionIndex = initialSectionOrZikrIndex,
      onClose = { activeDocumentId = null },
      onAddNote = onAddStudyNote
    )
    return
  }

  if (activeDocumentId == "paper_7_commandments") {
    BackHandler { activeDocumentId = null }
    Paper7CommandmentsInteractiveReaderView(
      paper = paper7Commandments,
      initialSectionIndex = initialSectionOrZikrIndex,
      onClose = { activeDocumentId = null },
      onAddNote = onAddStudyNote
    )
    return
  }

  if (activeDocumentId == "fard_namaz_azkar") {
    BackHandler { activeDocumentId = null }
    FardNamazAzkarInteractiveReaderView(
      pamphlet = fardNamazAzkar,
      initialZikrIndex = initialSectionOrZikrIndex,
      onClose = { activeDocumentId = null },
      onAddNote = onAddStudyNote
    )
    return
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("islamic_research_hub_list"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // 1. Header Banner Card (Symmetrical with separate Urdu & English lines)
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("islamic_research_header_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = EmeraldPrimary.copy(alpha = 0.10f)
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "البُحُوثُ الإِسْلَامِيَّةُ وَالأَذْكَارُ المَسْنُونَة",
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = ScheherazadeFontFamily,
              fontSize = 19.sp,
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "تحقیقی مقالات و مسنون اذکار (4)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 17.sp,
              lineHeight = 28.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Islamic Research — 4 Verified Research Papers & Sunnah Compilations",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Qur'an & Sahih Hadiths (International Numbering) • Scroll up for full-screen reading",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "﴿فرقہ واریت سے بچ کر، صِرف قرآن اور صحیح الاسناد اَحادیث کو حجت و دلیل ماننے والوں کیلئے﴾",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 14.sp,
              lineHeight = 24.sp,
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // 2. Unified Search Bar across Islamic Research Papers
    item {
      OutlinedTextField(
        value = researchSearchQuery,
        onValueChange = { researchSearchQuery = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("islamic_research_search_input"),
        placeholder = {
          Text(
            text = "Search all 4 Research Papers & Azkar (وصیت، قرطاس، رافضیت، اولیاء، عباد الرحمن، 4437، 842)...",
            fontSize = 12.sp
          )
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (researchSearchQuery.isNotEmpty()) {
            IconButton(onClick = { researchSearchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )
    }

    // 2b. Intelligent Quick Filter / Switcher Row + Font Size (A- / A+) across all 4 Research Compilations
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        LazyRow(
          modifier = Modifier
            .weight(1f)
            .testTag("islamic_research_category_filter_row"),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val filterTabs = listOf(
            "ALL" to "All Research (Continuous Scroll • 4)",
            "PAPER_1" to "Paper #1: آخری وصیّتیں",
            "PAPER_5A" to "Paper #5a: رافضیت و ناصبیت",
            "PAPER_7" to "Paper #7: 10 احکام و 12 صفات",
            "AZKAR" to "Sunnah Azkar (19)"
          )
          itemsIndexed(filterTabs) { _, (filterKey, labelText) ->
            FilterChip(
              selected = selectedCategoryFilter == filterKey,
              onClick = { selectedCategoryFilter = filterKey },
              label = {
                Text(
                  text = labelText,
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("islamic_research_filter_chip_$filterKey")
            )
          }
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
          modifier = Modifier.clickable {
            hubFontScaleStep = (hubFontScaleStep - 1).coerceAtLeast(-1)
          }
        ) {
          Text(
            text = "A-",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
          modifier = Modifier.clickable {
            hubFontScaleStep = (hubFontScaleStep + 1).coerceAtMost(3)
          }
        ) {
          Text(
            text = "A+",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          )
        }
      }
    }

    // 3. Search Results (if query is active)
    if (researchSearchQuery.isNotBlank()) {
      item {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${searchHits.size} Matches in Islamic Research for \"${researchSearchQuery.trim()}\"",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              modifier = Modifier.weight(1f)
            )
            TextButton(
              onClick = { researchSearchQuery = "" },
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
              modifier = Modifier.height(26.dp)
            ) {
              Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      itemsIndexed(
        items = searchHits,
        key = { idx, hit -> "research_hit_${hit.paperId}_${hit.sectionIndex}_$idx" }
      ) { _, hit ->
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              initialSectionOrZikrIndex = hit.sectionIndex
              activeDocumentId = hit.paperId
            }
            .testTag("research_search_hit_${hit.paperId}_${hit.sectionIndex}"),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldPrimary.copy(alpha = 0.14f)
              ) {
                Text(
                  text = hit.paperTitleUrdu,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
              }
              if (hit.referenceBadge.isNotBlank()) {
                Text(
                  text = hit.referenceBadge,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Text(
              text = hit.sectionOrZikrTitle,
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 15.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )

            if (hit.snippetArabic.isNotBlank()) {
              Text(
                text = hit.snippetArabic,
                style = MaterialTheme.typography.bodyLarge.copy(
                  fontFamily = AmiriFontFamily,
                  fontSize = 18.sp,
                  lineHeight = 30.sp,
                  color = EmeraldPrimary,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }

            if (hit.snippetUrdu.isNotBlank()) {
              Text(
                text = hit.snippetUrdu,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 14.sp,
                  lineHeight = 24.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  textAlign = TextAlign.Right
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }
    }

    // 4a. Featured Card: Research Paper #1 — رسول اللہ ﷺ کی آخری وصیّتیں (Nabi SAW ki Aakhri Wasiyat)
    if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "PAPER_1") {
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              initialSectionOrZikrIndex = 0
              activeDocumentId = "paper_1_wasiyat"
            }
            .testTag("islamic_research_card_paper_1_wasiyat"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldPrimary
              ) {
                Text(
                  text = "${paper1Wasiyat.meta.paperLabel} • ${paper1Wasiyat.meta.pageCount} Pages (${paper1Wasiyat.sections.size} Sections)",
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Text(
                text = "${paper1Wasiyat.meta.dateHijri} • ${paper1Wasiyat.meta.dateGregorian}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }

            // Urdu Title
            Text(
              text = paper1Wasiyat.meta.titleUr,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 22.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Text(
              text = "${paper1Wasiyat.meta.titleRoman} — The Final Bequests & Testaments of Prophet Muhammad ﷺ (Hadith-e-Qirtas, Hadith-e-Thaqalayn, Ghadir Khumm, Fazail of Abu Bakr RA, Ahl al-Bayt AS, Salah & Slaves)",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.fillMaxWidth()
            )

            // Key Highlights Box
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = GoldPrimary.copy(alpha = 0.12f),
              border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.40f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "اہم مباحث: ❶ وصیّت کا قرآنی مفہوم • ❷ حدیثِ قرطاس کی حقیقت • ❸ سیدنا ابوبکر صدیق ؓ کی خلافت کا اشارہ • ❹ حدیثِ ثقلین (غدیرِ خُم) اور مودّتِ قربیٰ • ❺ قبر پرستی کی ممانعت • ❻ نماز اور غلاموں کے حقوق • ❼ دُعائے استعاذہ و درودِ ابراہیمی",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            // Quick Section Jump Chips (12 Sections)
            Text(
              text = "Jump to Section (${paper1Wasiyat.sections.size} Sections):",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              paper1Wasiyat.sections.forEachIndexed { idx, sec ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = EmeraldPrimary.copy(alpha = 0.10f),
                  border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.30f)),
                  modifier = Modifier
                    .clickable {
                      initialSectionOrZikrIndex = idx
                      activeDocumentId = "paper_1_wasiyat"
                    }
                    .testTag("paper_1_section_chip_${sec.id}")
                ) {
                  Text(
                    text = "${idx + 1}. ${sec.titleUr.take(34)}${if (sec.titleUr.length > 34) "…" else ""}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 12.sp,
                      color = EmeraldPrimary,
                      fontWeight = FontWeight.Bold
                    )
                  )
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = paper1Wasiyat.meta.publisherUr,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
                Text(
                  text = paper1Wasiyat.meta.website,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
              }
              Button(
                onClick = {
                  initialSectionOrZikrIndex = 0
                  activeDocumentId = "paper_1_wasiyat"
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("open_paper_1_wasiyat_button")
              ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Read Paper #1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Continuous Inline Stream of All 12 Sections of Research Paper #1 (Direct Top-to-Bottom Reading with Headings & References)
      itemsIndexed(
        items = paper1Wasiyat.sections,
        key = { _, sec -> "hub_paper1_sec_${sec.id}" }
      ) { _, section ->
        Paper1WasiyatSectionCard(
          section = section,
          arabicBoost = hubArabicBoost,
          urduBoost = hubUrduBoost,
          onCopy = { label, text -> copyExtraResearchText(context, label, text) },
          onAddNote = { text ->
            onAddStudyNote(text)
            Toast.makeText(context, "Saved to Study Notes", Toast.LENGTH_SHORT).show()
          }
        )
      }
    }

    // 4b. Featured Card: Research Paper 5a — Rafziyat, Nasbiyat aur Yazeediyat ka Tahqeeqi Jaiza
    if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "PAPER_5A") {
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              initialSectionOrZikrIndex = 0
              activeDocumentId = "paper_5a"
            }
            .testTag("islamic_research_card_paper_5a"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldPrimary
            ) {
              Text(
                text = "Research Paper #${paper5a.meta.researchPaper} • ${paper5a.meta.totalPages} Pages",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
            }
            Text(
              text = "${paper5a.meta.dateHijri} • ${paper5a.meta.dateGregorian}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
              )
            )
          }

          // Urdu Title
          Text(
            text = paper5a.meta.title,
            style = MaterialTheme.typography.headlineSmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 22.sp,
              lineHeight = 36.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Text(
            text = "A Critical Research Review of Rafziyat, Nasbiyat & Yazeediyat in the Light of the Qur'an & Sahih Hadiths",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
          )

          // 3 Core Definitions Box
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = GoldPrimary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              paper5a.definitions.forEach { def ->
                Text(
                  text = "• ${def.term} : ${def.meaning}",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }

          // Quick Section Jump Chips
          Text(
            text = "Jump to Section (${paper5a.sections.size} Sections):",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            paper5a.sections.forEachIndexed { idx, section ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldPrimary.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.30f)),
                modifier = Modifier
                  .clickable {
                    initialSectionOrZikrIndex = idx
                    activeDocumentId = "paper_5a"
                  }
                  .testTag("paper_5a_section_chip_${section.id}")
              ) {
                Text(
                  text = "${idx + 1}. ${section.title.take(36)}${if (section.title.length > 36) "…" else ""}",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 12.sp,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = paper5a.meta.publisher,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
              Text(
                text = paper5a.meta.website,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
            Button(
              onClick = {
                initialSectionOrZikrIndex = 0
                activeDocumentId = "paper_5a"
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              modifier = Modifier.testTag("open_paper_5a_button")
            ) {
              Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Read Paper 5a", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

      // Continuous Inline Stream of All 12 Sections of Research Paper #5a (Rafziyat, Nasbiyat & Yazeediyat)
      itemsIndexed(
        items = paper5a.sections,
        key = { _, sec -> "hub_paper_5a_sec_${sec.id}" }
      ) { secIdx, section ->
        Paper5aSectionCard(
          sectionNumber = secIdx + 1,
          section = section,
          onCopyItem = { textToCopy ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(section.title, textToCopy))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          onAddNote = onAddStudyNote
        )
      }
    }

    // 4c. Featured Card: Research Paper #7 — 10 Qur'anic Commandments & 12 Attributes of True Awliya
    if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "PAPER_7") {
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              initialSectionOrZikrIndex = 0
              activeDocumentId = "paper_7_commandments"
            }
            .testTag("islamic_research_card_paper_7_commandments"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldPrimary
              ) {
                Text(
                  text = "Research Paper #${paper7Commandments.publication.researchPaperNumber} • 10 Commandments + 12 Attributes",
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Text(
                text = "${paper7Commandments.publication.dateHijri} • ${paper7Commandments.publication.dateGregorian}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }

            Text(
              text = "قرآنِ حکیم کے 10 بنیادی احکام اور اللہ ﷻ کے حقیقی اولیاء کرام کی 12 صفات",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 21.sp,
                lineHeight = 35.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Text(
              text = "The 10 Core Commandments of the Noble Qur'an (Surah Bani Isra'il 17:23–39) & The 12 Attributes of the True Awliya of Allah ('Ibad-ur-Rahman, Surah Al-Furqan 25:63–77)",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.fillMaxWidth()
            )

            // Summary Box of the 2 Sections
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = EmeraldPrimary.copy(alpha = 0.08f),
              border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.30f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "حصہ اوّل: قرآنِ حکیم کے 10 بنیادی احکام (سورۃ بنی اسرائیل : 23 تا 39) — توحید، والدین کے ساتھ احسان، حقوق العباد، اسراف سے بچاؤ، قتلِ اولاد، بے حیائی، قتلِ ناحق، مالِ یتیم، ایفائے عہد، ناپ تول میں انصاف اور تکبر کی ممانعت۔",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                Text(
                  text = "حصہ دوم: اللہ ﷻ کے حقیقی اولیاء کرام (عِبَادُ الرَّحْمٰنِ) کی 12 صفات (سورۃ الفرقان : 63 تا 77 + سورۃ النمل : 62) — تواضع، تہجد و قیام اللیل، خوفِ جہنم، اعتدال، شرک و قتل و زنا سے اجتناب، توبہ، جھوٹی گواہی سے پرہیز، آیاتِ الٰہی پر تدبر اور صالح ذریت کی دعا۔",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            // Quick Jump Chips for Section 1 and Section 2
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldPrimary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier
                  .clickable {
                    initialSectionOrZikrIndex = 0
                    activeDocumentId = "paper_7_commandments"
                  }
                  .testTag("paper_7_jump_section_1")
              ) {
                Text(
                  text = "① 10 بنیادی احکام (بنی اسرائیل 23–39)",
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldPrimary.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier
                  .clickable {
                    initialSectionOrZikrIndex = 1
                    activeDocumentId = "paper_7_commandments"
                  }
                  .testTag("paper_7_jump_section_2")
              ) {
                Text(
                  text = "② اولیاء کرام کی 12 صفات (الفرقان 63–77)",
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = paper7Commandments.header,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
                Text(
                  text = "www.AhleSunnatPak.com • Research Paper #${paper7Commandments.publication.researchPaperNumber}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
              }
              Button(
                onClick = {
                  initialSectionOrZikrIndex = 0
                  activeDocumentId = "paper_7_commandments"
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("open_paper_7_commandments_button")
              ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Read Paper #7", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Continuous Inline Stream of Research Paper #7 (10 Qur'anic Commandments + 12 Attributes of True Awliya)
      paper7Commandments.sections.forEach { section ->
        item(key = "hub_paper7_sec_header_${section.sectionNumber}") {
          Paper7SectionHeaderBanner(
            section = section,
            urduBoost = hubUrduBoost
          )
        }
        itemsIndexed(
          items = section.items,
          key = { _, item -> "hub_paper7_sec_${section.sectionNumber}_item_${item.number}" }
        ) { _, item ->
          val checkKey = "${section.sectionNumber}_${item.number}"
          val isChecked = hubMuhasabahCheckedMap[checkKey] == true
          Paper7CommandmentOrAttributeCard(
            sectionNumber = section.sectionNumber,
            sectionTitleUrdu = section.title,
            item = item,
            isChecked = isChecked,
            onToggleChecked = { hubMuhasabahCheckedMap[checkKey] = !isChecked },
            arabicBoost = hubArabicBoost,
            urduBoost = hubUrduBoost,
            onCopy = { label, text -> copyExtraResearchText(context, label, text) },
            onAddNote = { text ->
              onAddStudyNote(text)
              Toast.makeText(context, "Saved to Study Notes", Toast.LENGTH_SHORT).show()
            }
          )
        }
        val finalAdvice = section.finalAdvice
        if (finalAdvice != null) {
          item(key = "hub_paper7_sec_final_advice_${section.sectionNumber}") {
            Paper7FinalAdviceCard(
              sectionTitleUrdu = section.title,
              finalAdvice = finalAdvice,
              arabicBoost = hubArabicBoost,
              urduBoost = hubUrduBoost,
              onCopy = { label, text -> copyExtraResearchText(context, label, text) },
              onAddNote = { text ->
                onAddStudyNote(text)
                Toast.makeText(context, "Saved to Study Notes", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }
      }
    }

    // 4d. Featured Card: Fard Namaz Ke Baad Sunnat Azkar
    if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "AZKAR") {
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
          .clickable {
            initialSectionOrZikrIndex = 0
            activeDocumentId = "fard_namaz_azkar"
          }
          .testTag("islamic_research_card_fard_namaz_azkar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GoldPrimary.copy(alpha = 0.22f)
            ) {
              Text(
                text = "Sunnah Adhkar Research • ${fardNamazAzkar.azkar.size} Authentic Azkar",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = EmeraldPrimary,
                  fontWeight = FontWeight.Bold
                )
              )
            }
            Text(
              text = "International Hadith #",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
              )
            )
          }

          Text(
            text = fardNamazAzkar.title,
            style = MaterialTheme.typography.headlineSmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 22.sp,
              lineHeight = 36.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Text(
            text = fardNamazAzkar.subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 15.sp,
              lineHeight = 25.sp,
              fontWeight = FontWeight.SemiBold,
              color = GoldPrimary,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Text(
            text = "Sunnah Azkar After Obligatory Prayers — 5 Introductory Qur'an/Hadith Proofs, 19 Authenticated Post-Salah Adhkar with Arabic, Urdu Translation, Repeat Counts & Interactive Tasbih, plus 4 Scholarly Notes",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 12.sp,
              lineHeight = 18.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
          )

          // Highlight Box with sample Azkar preview
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = EmeraldPrimary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.30f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "نمایاں مسنون اذکار : ❶ اَللّٰہُ اَکۡبَرُ (1 مرتبہ) • ❷ اَسۡتَغۡفِرُ اللّٰہَ (3 مرتبہ) + اَللّٰہُمَّ اَنۡتَ السَّلَامُ... • ❸ آیۃُ الکُرسی • ❺ رَبِّ اَعِنِّیۡ عَلٰی ذِکۡرِکَ... • ⓭ معوذات (3 سورتیں) • ⓮ تسبیحِ فاطمہ (33-33-33 + 1)",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 14.sp,
                  lineHeight = 24.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = fardNamazAzkar.publisher,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
              Text(
                text = "${fardNamazAzkar.website} • ${fardNamazAzkar.email}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
            Button(
              onClick = {
                initialSectionOrZikrIndex = 0
                activeDocumentId = "fard_namaz_azkar"
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              modifier = Modifier.testTag("open_fard_namaz_azkar_button")
            ) {
              Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Open Azkar (19)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

      // Continuous Inline Stream of All 5 Introductory Proofs, 19 Sunnah Azkar After Fard Namaz, and 4 Scholarly Notes
      item(key = "hub_azkar_intro_card") {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.elevatedCardColors(
            containerColor = EmeraldPrimary.copy(alpha = 0.08f)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "مقدمہ : ذکرِ الٰہی اور فرض نماز کے بعد اذکار کی فضیلت (${fardNamazAzkar.introduction.size} دلائل)",
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 16.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
            fardNamazAzkar.introduction.forEach { intro ->
              AzkarIntroProofRow(intro = intro)
            }
          }
        }
      }

      items(
        items = fardNamazAzkar.azkar,
        key = { "hub_fard_zikr_${it.number}" }
      ) { zikr ->
        val currentCount = hubTasbihCounts[zikr.number] ?: 0
        FardNamazZikrCard(
          zikr = zikr,
          currentCount = currentCount,
          onIncrementTasbih = { hubTasbihCounts[zikr.number] = currentCount + 1 },
          onResetTasbih = { hubTasbihCounts[zikr.number] = 0 },
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = buildString {
              appendLine("ذکر نمبر ${zikr.number} — ${zikr.narrator}")
              if (zikr.narration.isNotBlank()) appendLine(zikr.narration)
              zikr.arabicLines.forEach { appendLine(it) }
              if (zikr.urduTranslation.isNotBlank()) appendLine(zikr.urduTranslation)
              zikr.parts.forEach { p ->
                appendLine("[${p.repeat}]")
                p.arabicLines.forEach { appendLine(it) }
                if (p.urduTranslation.isNotBlank()) appendLine(p.urduTranslation)
              }
              val refs = (zikr.references + zikr.parts.flatMap { it.references }).joinToString(" ، ") { it.formatted }
              if (refs.isNotBlank()) appendLine("[ $refs ]")
            }
            clipboard.setPrimaryClip(ClipData.newPlainText("Zikr #${zikr.number}", text))
            Toast.makeText(context, "Zikr #${zikr.number} copied", Toast.LENGTH_SHORT).show()
          },
          onAddNote = {
            onAddStudyNote("${fardNamazAzkar.title} — ذکر نمبر ${zikr.number}")
          }
        )
      }

      if (fardNamazAzkar.closingNotes.isNotEmpty()) {
        item(key = "hub_azkar_closing_notes") {
          ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
              containerColor = GoldPrimary.copy(alpha = 0.10f)
            )
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = "علمی فوائد و اہم نوٹس (${fardNamazAzkar.closingNotes.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 18.sp,
                  lineHeight = 30.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
              fardNamazAzkar.closingNotes.forEach { note ->
                ClosingNoteRow(note = note)
              }
            }
          }
        }
      }
    }

    // Bottom Report Footer Card for the continuous Islamic Research Hub
    item(key = "hub_research_report_footer") {
      ResearchPaperReportFooterCard(
        paperTitle = "Islamic Research Library",
        tagPrefix = "hub_research",
        onReportSubmitted = { reportMsg ->
          onAddStudyNote(reportMsg)
        }
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Paper5aInteractiveReaderView(
  paper: RafziyatNasbiyatYazeediyatPaper,
  initialSectionIndex: Int = 0,
  onClose: () -> Unit,
  onAddNote: (String) -> Unit
) {
  val context = LocalContext.current
  var selectedSectionFilter by remember(initialSectionIndex) {
    mutableIntStateOf(-1) // -1 = Show All Sections (scroll to initialSectionIndex)
  }
  var inPaperSearchQuery by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(initialSectionIndex) {
    if (initialSectionIndex > 0) {
      selectedSectionFilter = initialSectionIndex
    }
  }

  val displayedSections = remember(paper.sections, selectedSectionFilter, inPaperSearchQuery) {
    val base = if (selectedSectionFilter in paper.sections.indices) {
      listOf(paper.sections[selectedSectionFilter])
    } else {
      paper.sections
    }
    val q = inPaperSearchQuery.trim()
    if (q.isBlank()) {
      base
    } else {
      base.mapNotNull { sec ->
        val matchingItems = sec.items.filter { item ->
          sec.title.contains(q, ignoreCase = true) ||
            item.urdu.contains(q, ignoreCase = true) ||
            item.arabic.contains(q, ignoreCase = true) ||
            item.referencePrinted.contains(q, ignoreCase = true) ||
            item.ayahTranslationUrdu.contains(q, ignoreCase = true)
        }
        if (matchingItems.isNotEmpty() || sec.title.contains(q, ignoreCase = true)) {
          sec.copy(items = if (matchingItems.isNotEmpty()) matchingItems else sec.items)
        } else {
          null
        }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("paper_5a_reader_view")
  ) {
    // Top Reader Bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 3.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_paper_5a_reader_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = paper.meta.title,
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Research Paper #${paper.meta.researchPaper} • ${paper.meta.dateGregorian} • ${paper.meta.dateHijri}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          Row {
            IconButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val fullText = buildString {
                  appendLine(paper.meta.title)
                  appendLine(paper.meta.pageHeader)
                  appendLine(paper.meta.bismillah)
                  paper.definitions.forEach { appendLine("${it.term}: ${it.meaning}") }
                  appendLine(paper.callToReader)
                  paper.sections.forEach { sec ->
                    appendLine("\n=== ${sec.title} ===")
                    sec.items.forEach { item ->
                      if (item.arabic.isNotBlank()) appendLine(item.arabic)
                      if (item.urdu.isNotBlank()) appendLine(item.urdu)
                      if (item.referencePrinted.isNotBlank()) appendLine(item.referencePrinted)
                    }
                  }
                }
                clipboard.setPrimaryClip(ClipData.newPlainText(paper.meta.title, fullText))
                Toast.makeText(context, "Research Paper 5a copied to clipboard", Toast.LENGTH_SHORT).show()
              }
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy All", tint = EmeraldPrimary)
            }
            IconButton(
              onClick = { onAddNote("Research Paper 5a: ${paper.meta.title}") }
            ) {
              Icon(Icons.Default.NoteAdd, contentDescription = "Add Note", tint = EmeraldPrimary)
            }
          }
        }
      }
    }

    LazyColumn(
      state = listState,
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Scrolling Search & Section Selector Controls (scrolls naturally like Quran reader instead of covering half screen)
      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          OutlinedTextField(
            value = inPaperSearchQuery,
            onValueChange = { inPaperSearchQuery = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("paper_5a_in_reader_search"),
            placeholder = {
              Text("Search inside Research Paper 5a (Hadith #, keyword, صحابہ، علی، حسین، یزید)...", fontSize = 11.sp)
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
              if (inPaperSearchQuery.isNotEmpty()) {
                IconButton(onClick = { inPaperSearchQuery = "" }) {
                  Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
          )

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
          ) {
            item {
              FilterChip(
                selected = selectedSectionFilter == -1,
                onClick = {
                  selectedSectionFilter = -1
                  coroutineScope.launch { listState.animateScrollToItem(0) }
                },
                label = {
                  Text(
                    text = "All Sections (${paper.sections.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
            itemsIndexed(paper.sections) { idx, sec ->
              FilterChip(
                selected = selectedSectionFilter == idx,
                onClick = {
                  selectedSectionFilter = idx
                  coroutineScope.launch { listState.animateScrollToItem(0) }
                },
                label = {
                  Text(
                    text = "${idx + 1}. ${sec.title.take(28)}${if (sec.title.length > 28) "…" else ""}",
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 12.sp
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      }

      // Top Metadata, Bismillah, Definitions & Call to Reader (when viewing All or First section)
      if (selectedSectionFilter == -1 || selectedSectionFilter == 0) {
        item {
          ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
              containerColor = EmeraldPrimary.copy(alpha = 0.08f)
            )
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (paper.meta.pageHeader.isNotBlank()) {
                Text(
                  text = paper.meta.pageHeader,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.Center
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }

              if (paper.meta.bismillah.isNotBlank()) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = GoldPrimary.copy(alpha = 0.14f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = paper.meta.bismillah,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontFamily = ScheherazadeFontFamily,
                      fontSize = 19.sp,
                      lineHeight = 32.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary,
                      textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(10.dp)
                  )
                }
              }

              // Core Definitions (Rafziyat, Nasbiyat, Yazeediyat)
              if (paper.definitions.isNotEmpty()) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  paper.definitions.forEach { def ->
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.surface,
                      border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = def.meaning,
                          style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = UrduNastaliqFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 25.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right
                          ),
                          modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = EmeraldPrimary
                        ) {
                          Text(
                            text = def.term,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                              fontFamily = UrduNastaliqFontFamily,
                              fontSize = 14.sp,
                              fontWeight = FontWeight.Bold,
                              color = Color.White
                            )
                          )
                        }
                      }
                    }
                  }
                }
              }

              // Call to Reader
              if (paper.callToReader.isNotBlank()) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color(0xFFFFF8E1),
                  border = BorderStroke(1.dp, GoldPrimary),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = paper.callToReader,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 15.sp,
                      lineHeight = 26.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF5D4037),
                      textAlign = TextAlign.Right
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Render Each Section & Its Items
      itemsIndexed(
        items = displayedSections,
        key = { _, sec -> "paper_5a_sec_${sec.id}" }
      ) { secIdx, section ->
        Paper5aSectionCard(
          sectionNumber = if (selectedSectionFilter >= 0) selectedSectionFilter + 1 else secIdx + 1,
          section = section,
          onCopyItem = { textToCopy ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(section.title, textToCopy))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          onAddNote = onAddNote
        )
      }

      item(key = "paper_5a_report_footer") {
        ResearchPaperReportFooterCard(
          paperTitle = "Research Paper #${paper.meta.researchPaper}: ${paper.meta.title}",
          tagPrefix = "paper_5a",
          onReportSubmitted = { reportMsg ->
            onAddNote(reportMsg)
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Paper5aSectionCard(
  sectionNumber: Int,
  section: ResearchPaperSection,
  onCopyItem: (String) -> Unit,
  onAddNote: (String) -> Unit
) {
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("paper_5a_section_card_${section.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Section Title Banner
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = EmeraldPrimary,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Surface(
            shape = CircleShape,
            color = GoldPrimary,
            modifier = Modifier.size(28.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "$sectionNumber",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.Black
                )
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 18.sp,
              lineHeight = 30.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.weight(1f)
          )
        }
      }

      if (section.intro.isNotBlank()) {
        Text(
          text = section.intro,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 15.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Right
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Section Sub-Definitions (e.g. Jang-e-Jamal & Jang-e-Siffin)
      if (section.definitions.isNotEmpty()) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          section.definitions.forEach { def ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GoldPrimary.copy(alpha = 0.14f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "${def.term} : ${def.meaning}",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 14.sp,
                  lineHeight = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // Section Items
      section.items.forEachIndexed { itemIdx, item ->
        Paper5aItemBlock(
          item = item,
          onCopy = {
            val txt = buildString {
              if (item.label.isNotBlank()) appendLine(item.label)
              if (item.arabic.isNotBlank()) appendLine(item.arabic)
              if (item.urdu.isNotBlank()) appendLine(item.urdu)
              if (item.ayahTranslationUrdu.isNotBlank()) appendLine(item.ayahTranslationUrdu)
              if (item.referencePrinted.isNotBlank()) appendLine(item.referencePrinted)
            }
            onCopyItem(txt)
          },
          onAddNote = {
            onAddNote("${section.title} — ${item.referencePrinted.ifBlank { "Item #${itemIdx + 1}" }}")
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Paper5aItemBlock(
  item: ResearchPaperItem,
  onCopy: () -> Unit,
  onAddNote: () -> Unit
) {
  val isNote = item.type == "note"
  val isQuran = item.type == "quran_ayah" || item.type == "ayah_translation"
  val isDeed = item.type == "deed"
  val isQuote = item.type == "quote"

  val containerColor = when {
    isQuran -> EmeraldPrimary.copy(alpha = 0.08f)
    isNote -> GoldPrimary.copy(alpha = 0.12f)
    isDeed -> Color(0xFFFFEBEE).copy(alpha = 0.65f)
    isQuote -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
  }

  val borderColor = when {
    isQuran -> EmeraldPrimary.copy(alpha = 0.40f)
    isNote -> GoldPrimary.copy(alpha = 0.55f)
    isDeed -> Color(0xFFC62828).copy(alpha = 0.40f)
    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f)
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Top Badge Row (Item Number, Label, Page, Copy & Note actions)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (item.number.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (isDeed) Color(0xFFC62828) else EmeraldPrimary
            ) {
              Text(
                text = "#${item.number}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }
          val badgeLabel = item.label.ifBlank {
            when (item.type) {
              "quran_ayah" -> "آیتِ مبارکہ"
              "hadith" -> "ترجمہ صحیح حدیث"
              "deed" -> "تاریخی و حدیثی حقیقت"
              "quote" -> "قولِ محدث و نصیحت"
              "note" -> "نوٹ"
              else -> ""
            }
          }
          if (badgeLabel.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = GoldPrimary.copy(alpha = 0.22f)
            ) {
              Text(
                text = badgeLabel,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
          }
          if (item.page.isNotBlank()) {
            Text(
              text = "Pg. ${item.page}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }

        Row {
          IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
            Icon(
              Icons.Default.ContentCopy,
              contentDescription = "Copy",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
          IconButton(onClick = onAddNote, modifier = Modifier.size(28.dp)) {
            Icon(
              Icons.Default.NoteAdd,
              contentDescription = "Add Note",
              tint = EmeraldPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Urdu Text (if present and before or after Arabic)
      if (item.urdu.isNotBlank()) {
        Text(
          text = item.urdu,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 16.sp,
            lineHeight = 29.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Right
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Arabic Text (Quran Ayah or Hadith Arabic)
      if (item.arabic.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.30f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = item.arabic,
              style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = ScheherazadeFontFamily,
                fontSize = 22.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
            if (item.arabicReferenceRaw.isNotBlank()) {
              Text(
                text = "[ ${item.arabicReferenceRaw} ]",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }

      // Embedded Ayah Translation (e.g. Surah al-Ahzab 33 inside Hadith #7)
      if (item.ayahTranslationUrdu.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = EmeraldPrimary.copy(alpha = 0.08f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            if (item.ayahTranslationLabel.isNotBlank()) {
              Text(
                text = item.ayahTranslationLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
            Text(
              text = item.ayahTranslationUrdu,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 15.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // References Badges
      if (item.references.isNotEmpty() || item.referencePrinted.isNotBlank()) {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (item.references.isNotEmpty()) {
            item.references.forEach { ref ->
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldPrimary.copy(alpha = 0.14f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    Icons.Default.Verified,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(13.dp)
                  )
                  Text(
                    text = ref.raw,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary
                    )
                  )
                }
              }
            }
          } else if (item.referencePrinted.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldPrimary.copy(alpha = 0.14f)
            ) {
              Text(
                text = item.referencePrinted,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FardNamazAzkarInteractiveReaderView(
  pamphlet: FardNamazAzkarPamphlet,
  initialZikrIndex: Int = 0,
  onClose: () -> Unit,
  onAddNote: (String) -> Unit
) {
  val context = LocalContext.current
  var azkarSearchQuery by remember { mutableStateOf("") }
  val tasbihCounts = remember { mutableStateMapOf<Int, Int>() }
  val listState = rememberLazyListState()

  val filteredAzkar = remember(pamphlet.azkar, azkarSearchQuery) {
    val q = azkarSearchQuery.trim()
    if (q.isBlank()) {
      pamphlet.azkar
    } else {
      pamphlet.azkar.filter { zikr ->
        zikr.number.toString() == q.removePrefix("#") ||
          zikr.narrator.contains(q, ignoreCase = true) ||
          zikr.narration.contains(q, ignoreCase = true) ||
          zikr.urduTranslation.contains(q, ignoreCase = true) ||
          zikr.urdu.contains(q, ignoreCase = true) ||
          zikr.note.contains(q, ignoreCase = true) ||
          zikr.arabicLines.any { it.contains(q, ignoreCase = true) } ||
          zikr.parts.any { p ->
            p.urduTranslation.contains(q, ignoreCase = true) ||
              p.arabicLines.any { it.contains(q, ignoreCase = true) }
          } ||
          zikr.references.any { it.formatted.contains(q, ignoreCase = true) }
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("fard_namaz_azkar_reader_view")
  ) {
    // Top Reader Bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 3.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_fard_namaz_azkar_reader_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = pamphlet.title,
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = pamphlet.subtitle,
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 12.sp,
                color = GoldPrimary
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          Row {
            IconButton(
              onClick = {
                tasbihCounts.clear()
                Toast.makeText(context, "All Tasbih counters reset", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.testTag("reset_all_azkar_tasbih_button")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = "Reset Counters", tint = EmeraldPrimary)
            }
            IconButton(
              onClick = { onAddNote(pamphlet.title) }
            ) {
              Icon(Icons.Default.NoteAdd, contentDescription = "Add Note", tint = EmeraldPrimary)
            }
          }
        }
      }
    }

    LazyColumn(
      state = listState,
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        OutlinedTextField(
          value = azkarSearchQuery,
          onValueChange = { azkarSearchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("fard_namaz_azkar_search_input"),
          placeholder = {
            Text("Search 19 Sunnah Azkar (Zikr #, narrator, Arabic, Urdu, Hadith #)...", fontSize = 11.sp)
          },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            if (azkarSearchQuery.isNotEmpty()) {
              IconButton(onClick = { azkarSearchQuery = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )
      }

      // 1. Opening Header & International Hadith Numbering Notice
      if (azkarSearchQuery.isBlank()) {
        item {
          ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
              containerColor = EmeraldPrimary.copy(alpha = 0.08f)
            )
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (pamphlet.openingLine.isNotBlank()) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = GoldPrimary.copy(alpha = 0.14f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = pamphlet.openingLine,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontFamily = ScheherazadeFontFamily,
                      fontSize = 18.sp,
                      lineHeight = 30.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary,
                      textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(10.dp)
                  )
                }
              }

              Text(
                text = pamphlet.noteOnNumbering,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 14.sp,
                  lineHeight = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary,
                  textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
              )

              // 5 Introductory Quran & Hadith Proofs
              HorizontalDivider(color = EmeraldPrimary.copy(alpha = 0.2f))
              Text(
                text = "مقدمہ : ذکرِ الٰہی اور فرض نماز کے بعد اذکار کی فضیلت (${pamphlet.introduction.size} دلائل)",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 16.sp,
                  lineHeight = 27.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )

              pamphlet.introduction.forEach { intro ->
                AzkarIntroProofRow(intro = intro)
              }
            }
          }
        }
      }

      // 2. The 19 Numbered Sunnah Azkar After Obligatory Prayers
      items(
        items = filteredAzkar,
        key = { "fard_zikr_${it.number}" }
      ) { zikr ->
        val currentCount = tasbihCounts[zikr.number] ?: 0
        FardNamazZikrCard(
          zikr = zikr,
          currentCount = currentCount,
          onIncrementTasbih = {
            val next = currentCount + 1
            tasbihCounts[zikr.number] = next
          },
          onResetTasbih = {
            tasbihCounts[zikr.number] = 0
          },
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = buildString {
              appendLine("ذکر نمبر ${zikr.number} — ${zikr.narrator}")
              if (zikr.narration.isNotBlank()) appendLine(zikr.narration)
              zikr.arabicLines.forEach { appendLine(it) }
              if (zikr.urduTranslation.isNotBlank()) appendLine(zikr.urduTranslation)
              zikr.parts.forEach { p ->
                appendLine("[${p.repeat}]")
                p.arabicLines.forEach { appendLine(it) }
                if (p.urduTranslation.isNotBlank()) appendLine(p.urduTranslation)
              }
              val refs = (zikr.references + zikr.parts.flatMap { it.references }).joinToString(" ، ") { it.formatted }
              if (refs.isNotBlank()) appendLine("[ $refs ]")
            }
            clipboard.setPrimaryClip(ClipData.newPlainText("Zikr #${zikr.number}", text))
            Toast.makeText(context, "Zikr #${zikr.number} copied", Toast.LENGTH_SHORT).show()
          },
          onAddNote = {
            onAddNote("${pamphlet.title} — ذکر نمبر ${zikr.number}")
          }
        )
      }

      // 3. Closing Scholarly Notes & Benefits (4 Notes)
      if (azkarSearchQuery.isBlank() && pamphlet.closingNotes.isNotEmpty()) {
        item {
          ElevatedCard(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("fard_namaz_azkar_closing_notes_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
              containerColor = GoldPrimary.copy(alpha = 0.10f)
            )
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = "علمی فوائد و اہم نوٹس (${pamphlet.closingNotes.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 18.sp,
                  lineHeight = 30.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )

              pamphlet.closingNotes.forEach { note ->
                ClosingNoteRow(note = note)
              }
            }
          }
        }
      }

      item(key = "fard_namaz_azkar_report_footer") {
        ResearchPaperReportFooterCard(
          paperTitle = pamphlet.title,
          tagPrefix = "fard_namaz_azkar",
          onReportSubmitted = { reportMsg ->
            onAddNote(reportMsg)
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AzkarIntroProofRow(intro: AzkarIntroItem) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (intro.arabic.isNotBlank()) {
        Text(
          text = intro.arabic,
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = ScheherazadeFontFamily,
            fontSize = 20.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary,
            textAlign = TextAlign.Right
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
      val urduText = intro.urduTranslation.ifBlank { intro.urdu }
      if (urduText.isNotBlank()) {
        Text(
          text = urduText,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 15.sp,
            lineHeight = 26.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Right
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
      if (intro.references.isNotEmpty()) {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          intro.references.forEach { ref ->
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldPrimary.copy(alpha = 0.14f)
            ) {
              Text(
                text = "[ ${ref.formatted} ]",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FardNamazZikrCard(
  zikr: FardNamazZikrItem,
  currentCount: Int,
  onIncrementTasbih: () -> Unit,
  onResetTasbih: () -> Unit,
  onCopy: () -> Unit,
  onAddNote: () -> Unit
) {
  val context = LocalContext.current
  val targetCount = zikr.targetTasbihCount
  val isCompleted = currentCount >= targetCount
  val verifiedAudio = remember(zikr.number) {
    com.example.data.repository.IslamicDigitalLibraryRepository.getVerifiedAzkarAudioForZikr(context, zikr.number)
  }
  var isAudioPlaying by remember { mutableStateOf(false) }
  var repeatMode by remember { mutableStateOf("1x") } // "1x", "3x", "Loop"
  var activeMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
  var activeTts by remember { mutableStateOf<TextToSpeech?>(null) }

  DisposableEffect(zikr.number) {
    onDispose {
      runCatching {
        activeMediaPlayer?.stop()
        activeMediaPlayer?.release()
      }
      activeMediaPlayer = null
      runCatching {
        activeTts?.stop()
        activeTts?.shutdown()
      }
      activeTts = null
    }
  }

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("fard_namaz_zikr_card_${zikr.number}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Header: Number Circle + Repeat Badge + Narrator + Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(if (isCompleted) Color(0xFF2E7D32) else EmeraldPrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${zikr.number}",
              style = MaterialTheme.typography.labelLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            )
          }

          if (zikr.repeat.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GoldPrimary.copy(alpha = 0.22f)
            ) {
              Text(
                text = zikr.repeat,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
          }
        }

        Row {
          IconButton(onClick = onCopy, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
          }
          IconButton(onClick = onAddNote, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.NoteAdd, contentDescription = "Add Note", tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
          }
        }
      }

      // 1b. Verified 1:1 Azkar Audio Recitation Bar (Matches exact Arabic Azkar text)
      if (verifiedAudio != null && verifiedAudio.verifiedMatch) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("zikr_verified_audio_bar_${zikr.number}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                Icons.Default.VolumeUp,
                contentDescription = "Verified Audio",
                tint = EmeraldPrimary,
                modifier = Modifier.size(16.dp)
              )
              Column {
                Text(
                  text = "${verifiedAudio.titleEn} • Verified Audio ✓",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${verifiedAudio.reciter} • ${verifiedAudio.hadithReference}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = GoldPrimary.copy(alpha = 0.22f),
                modifier = Modifier
                  .clickable {
                    repeatMode = when (repeatMode) {
                      "1x" -> "3x"
                      "3x" -> "Loop"
                      else -> "1x"
                    }
                  }
                  .testTag("zikr_audio_repeat_btn_${zikr.number}")
              ) {
                Text(
                  text = "Repeat: $repeatMode",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
              }

              OutlinedButton(
                onClick = {
                  if (isAudioPlaying) {
                    runCatching {
                      activeMediaPlayer?.stop()
                      activeMediaPlayer?.release()
                    }
                    activeMediaPlayer = null
                    runCatching { activeTts?.stop() }
                    isAudioPlaying = false
                  } else {
                    isAudioPlaying = true
                    val targetLoops = when (repeatMode) {
                      "3x" -> 3
                      "Loop" -> 33
                      else -> 1
                    }
                    try {
                      val mp = MediaPlayer()
                      activeMediaPlayer = mp
                      mp.setDataSource(verifiedAudio.primaryAudioUrl)
                      var remainingPlays = targetLoops
                      mp.setOnPreparedListener { player ->
                        player.start()
                      }
                      mp.setOnCompletionListener { player ->
                        remainingPlays -= 1
                        if (remainingPlays > 0 && isAudioPlaying) {
                          player.seekTo(0)
                          player.start()
                        } else {
                          isAudioPlaying = false
                          runCatching { player.release() }
                          activeMediaPlayer = null
                        }
                      }
                      mp.setOnErrorListener { player, _, _ ->
                        runCatching { player.release() }
                        activeMediaPlayer = null
                        // Offline fallback: speak exact verified Arabic Azkar text via Android TTS
                        var ttsInstance: TextToSpeech? = null
                        ttsInstance = TextToSpeech(context) { status ->
                          if (status == TextToSpeech.SUCCESS) {
                            ttsInstance?.language = Locale("ar")
                            val textToSpeak = verifiedAudio.exactArabicText.ifBlank {
                              zikr.arabicLines.joinToString(" ")
                            }
                            repeat(targetLoops.coerceAtMost(3)) { idx ->
                              val queueMode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                              ttsInstance?.speak(textToSpeak, queueMode, null, "zikr_${zikr.number}_$idx")
                            }
                          }
                          isAudioPlaying = false
                        }
                        activeTts = ttsInstance
                        true
                      }
                      mp.prepareAsync()
                    } catch (_: Throwable) {
                      isAudioPlaying = false
                    }
                  }
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                  .height(30.dp)
                  .testTag("zikr_audio_play_btn_${zikr.number}")
              ) {
                Icon(
                  if (isAudioPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                  contentDescription = if (isAudioPlaying) "Stop Azkar Audio" else "Play Azkar Audio",
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = if (isAudioPlaying) "Stop" else "Listen",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              }
            }
          }
        }
      }

      // 2. Narrator & Narration Context
      if (zikr.narrator.isNotBlank() || zikr.narration.isNotBlank()) {
        Text(
          text = listOf(zikr.narrator, zikr.narration).filter { it.isNotBlank() }.joinToString(" "),
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 15.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Right
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      // 3. Direct Arabic Lines (when not split into parts)
      if (zikr.arabicLines.isNotEmpty()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldPrimary.copy(alpha = 0.07f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.30f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            zikr.arabicLines.forEach { arLine ->
              Text(
                text = arLine,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontFamily = ScheherazadeFontFamily,
                  fontSize = 23.sp,
                  lineHeight = 40.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }

      // 4. Urdu Translation / Urdu Content
      val mainUrdu = zikr.urduTranslation.ifBlank { zikr.urdu }
      if (mainUrdu.isNotBlank()) {
        Text(
          text = "ترجمہ : $mainUrdu",
          style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 15.sp,
            lineHeight = 27.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Right
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      // 5. Multi-Part Adhkar (e.g. Zikr #2, #14 Tasbih-e-Fatima, #18 Witr Azkar)
      if (zikr.parts.isNotEmpty()) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          zikr.parts.forEach { part ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = EmeraldPrimary.copy(alpha = 0.06f),
              border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                if (part.repeat.isNotBlank()) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldPrimary.copy(alpha = 0.25f)
                  ) {
                    Text(
                      text = part.repeat,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                  }
                }
                part.arabicLines.forEach { ar ->
                  Text(
                    text = ar,
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontFamily = ScheherazadeFontFamily,
                      fontSize = 22.sp,
                      lineHeight = 38.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary,
                      textAlign = TextAlign.Right
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                }
                if (part.urduTranslation.isNotBlank()) {
                  Text(
                    text = "ترجمہ : ${part.urduTranslation}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 15.sp,
                      lineHeight = 26.sp,
                      color = MaterialTheme.colorScheme.onSurface,
                      textAlign = TextAlign.Right
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                }
                if (part.references.isNotEmpty()) {
                  FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    part.references.forEach { ref ->
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldPrimary.copy(alpha = 0.14f)
                      ) {
                        Text(
                          text = "[ ${ref.formatted} ]",
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = UrduNastaliqFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
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
      }

      // 6. Special Note (e.g. Ayat-ul-Kursi or 3 Quls complete recitation note)
      if (zikr.note.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = GoldPrimary.copy(alpha = 0.15f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "نوٹ : ${zikr.note}",
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 13.sp,
              lineHeight = 23.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      // 7. Footer: Authentic Hadith References + Interactive Tasbih Counter
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        FlowRow(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          zikr.references.forEach { ref ->
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldPrimary.copy(alpha = 0.14f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  Icons.Default.Verified,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(13.dp)
                )
                Text(
                  text = "[ ${ref.formatted} ]",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
              }
            }
          }
        }

        // Interactive Tasbih Button for this Zikr
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (currentCount > 0) {
            TextButton(
              onClick = onResetTasbih,
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
              modifier = Modifier.height(30.dp)
            ) {
              Text("Reset", fontSize = 10.sp)
            }
          }
          OutlinedButton(
            onClick = onIncrementTasbih,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            border = BorderStroke(1.dp, if (isCompleted) Color(0xFF2E7D32) else EmeraldPrimary),
            modifier = Modifier.testTag("zikr_tasbih_btn_${zikr.number}")
          ) {
            Icon(
              if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Add,
              contentDescription = null,
              tint = if (isCompleted) Color(0xFF2E7D32) else EmeraldPrimary,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$currentCount / $targetCount",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isCompleted) Color(0xFF2E7D32) else EmeraldPrimary
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClosingNoteRow(note: AzkarClosingNote) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          note.references.forEach { ref ->
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldPrimary.copy(alpha = 0.14f)
            ) {
              Text(
                text = "[ ${ref.formatted} ]",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
          }
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = EmeraldPrimary
        ) {
          Text(
            text = note.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
        }
      }

      Text(
        text = note.urdu,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = 15.sp,
          lineHeight = 26.sp,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Right
        ),
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}
