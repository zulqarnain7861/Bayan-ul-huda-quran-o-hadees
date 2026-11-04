package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommandmentOrAttributeItem
import com.example.data.model.FinalAdviceBlock
import com.example.data.model.NabiAakhriWasiyatPaper
import com.example.data.model.Paper7Section
import com.example.data.model.QuranCommandmentsAndAwliyaPaper
import com.example.data.model.WasiyatItem
import com.example.data.model.WasiyatSection
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.UrduNastaliqFontFamily
import kotlinx.coroutines.launch

private fun copyExtraResearchText(context: Context, label: String, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
  clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
  Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

// =========================================================================================
// 1. RESEARCH PAPER 1 READER: رسول اللہ ﷺ کی آخری وصیّتیں (12 Sections)
// =========================================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Paper1WasiyatInteractiveReaderView(
  paper: NabiAakhriWasiyatPaper,
  initialSectionIndex: Int = 0,
  onClose: () -> Unit,
  onAddNote: (String) -> Unit
) {
  val context = LocalContext.current
  var selectedSectionFilter by remember(initialSectionIndex) { mutableIntStateOf(-1) }
  var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, HADITH, QURAN_DUA, SCHOLAR, NOTE
  var inPaperSearchQuery by remember { mutableStateOf("") }
  var fontScaleStep by remember { mutableIntStateOf(0) } // -1, 0, +1, +2
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(initialSectionIndex) {
    if (initialSectionIndex > 0) {
      val targetLazyIndex = (initialSectionIndex + 1).coerceAtLeast(0)
      listState.scrollToItem(targetLazyIndex)
    }
  }

  val matchesTypeFilter: (WasiyatItem) -> Boolean = { item ->
    when (selectedTypeFilter) {
      "HADITH" -> item.type == "hadith"
      "QURAN_DUA" -> item.type == "ayah" || item.type == "dua"
      "SCHOLAR" -> item.type == "scholar_quote" || item.type == "narrative"
      "NOTE" -> item.type == "note" || item.inlineNoteUr.isNotBlank()
      else -> true
    }
  }

  val filteredSections = remember(paper.sections, selectedSectionFilter, selectedTypeFilter, inPaperSearchQuery) {
    val base = if (selectedSectionFilter in paper.sections.indices) {
      listOf(paper.sections[selectedSectionFilter])
    } else {
      paper.sections
    }
    val q = inPaperSearchQuery.trim()
    base.mapNotNull { sec ->
      val matchingItems = sec.items.filter { item ->
        val typeOk = matchesTypeFilter(item)
        val searchOk = if (q.isBlank()) {
          true
        } else {
          sec.titleUr.contains(q, ignoreCase = true) ||
            sec.introUr.contains(q, ignoreCase = true) ||
            item.primaryArabicText.contains(q, ignoreCase = true) ||
            item.textUr.contains(q, ignoreCase = true) ||
            item.narratorUr.contains(q, ignoreCase = true) ||
            item.introUr.contains(q, ignoreCase = true) ||
            item.inlineNoteUr.contains(q, ignoreCase = true) ||
            item.references.any { ref ->
              ref.formatted.contains(q, ignoreCase = true) ||
                ref.workUr.contains(q, ignoreCase = true) ||
                ref.number.contains(q, ignoreCase = true)
            }
        }
        typeOk && searchOk
      }
      if (matchingItems.isNotEmpty() || (q.isBlank() && selectedTypeFilter == "ALL")) {
        sec.copy(items = if (q.isBlank() && selectedTypeFilter == "ALL") sec.items else matchingItems)
      } else {
        null
      }
    }
  }

  val arabicBoost = (fontScaleStep * 2)
  val urduBoost = (fontScaleStep * 2)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("paper_1_wasiyat_reader_view")
  ) {
    // Sticky Reader Top Bar
    Surface(
      color = EmeraldPrimary.copy(alpha = 0.12f),
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            IconButton(
              onClick = onClose,
              modifier = Modifier
                .size(36.dp)
                .testTag("paper_1_back_button")
            ) {
              Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Islamic Research",
                tint = EmeraldPrimary
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = paper.meta.titleUr,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 18.sp,
                  lineHeight = 28.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                ),
                maxLines = 1
              )
              Text(
                text = "${paper.meta.titleRoman} • ${paper.sections.size} Sections (${paper.meta.pageCount} Pages)",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              )
            }
          }

          // Font Scale Controls (A- / A+)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
              modifier = Modifier.clickable {
                fontScaleStep = (fontScaleStep - 1).coerceAtLeast(-1)
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
                fontScaleStep = (fontScaleStep + 1).coerceAtMost(3)
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

        // In-Paper Search Input
        OutlinedTextField(
          value = inPaperSearchQuery,
          onValueChange = { inPaperSearchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("paper_1_search_input"),
          placeholder = {
            Text(
              "Search within آخری وصیّتیں (e.g. قرطاس, ثقلین, 4437, 6288, غدیرِ خُم, ابوبکر)...",
              fontSize = 11.sp
            )
          },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            if (inPaperSearchQuery.isNotEmpty()) {
              IconButton(onClick = { inPaperSearchQuery = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )

        // Evidence Type Filter Chips (Distinguishing Hadiths, Qur'an/Dua, Scholar Quotes, and Author's Notes)
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val typeFilters = listOf(
            "ALL" to "All Content",
            "HADITH" to "صحیح احادیث (Hadiths)",
            "QURAN_DUA" to "قرآنی آیات و دُعا (Qur'an & Dua)",
            "SCHOLAR" to "اقوالِ علماء و تاریخ (Scholars)",
            "NOTE" to "مصنف کے نوٹس (Author's Notes)"
          )
          itemsIndexed(typeFilters) { _, (filterKey, filterLabel) ->
            FilterChip(
              selected = selectedTypeFilter == filterKey,
              onClick = { selectedTypeFilter = filterKey },
              label = {
                Text(
                  text = filterLabel,
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldPrimary.copy(alpha = 0.24f),
                selectedLabelColor = EmeraldPrimary
              ),
              modifier = Modifier.testTag("paper_1_type_filter_$filterKey")
            )
          }
        }

        // Horizontal Section Jump Pills (12 Sections)
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          item {
            FilterChip(
              selected = selectedSectionFilter == -1,
              onClick = { selectedSectionFilter = -1 },
              label = {
                Text("All Sections (${paper.sections.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                selectedSectionFilter = if (selectedSectionFilter == idx) -1 else idx
                coroutineScope.launch { listState.scrollToItem(0) }
              },
              label = {
                Text(
                  text = "${idx + 1}. ${sec.titleUr.take(26)}${if (sec.titleUr.length > 26) "…" else ""}",
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("paper_1_filter_chip_${sec.id}")
            )
          }
        }
      }
    }

    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .testTag("paper_1_lazy_column"),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Top Attribution & Methodological Notice Card
      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.elevatedCardColors(
            containerColor = GoldPrimary.copy(alpha = 0.12f)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldPrimary
              ) {
                Text(
                  text = "${paper.meta.paperLabel} • ${paper.meta.website}",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Text(
                text = "${paper.meta.dateHijri} • ${paper.meta.dateGregorian}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.Bold
                )
              )
            }

            if (paper.meta.basmalaUr.isNotBlank()) {
              Text(
                text = "${paper.meta.basmalaUr}  ${paper.meta.openingUr}",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = AmiriFontFamily,
                  fontSize = (18 + arabicBoost).sp,
                  lineHeight = (30 + arabicBoost).sp,
                  color = EmeraldPrimary,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }

            Text(
              text = paper.meta.bannerUr.ifBlank { "﴿${paper.meta.publisherUr}﴾" },
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )

            // Editorial transparency banner (distinguishing Hadith vs Author's Note)
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "تحقیقی وضاحت: اِس مقالے میں صحیح احادیث، قرآنی آیات، مفسرین کے اقوال اور مصنف کے تحقیقی نوٹس (نوٹ) کو الگ الگ رنگوں اور عنوانات کے ساتھ واضح طور پر ممتاز کیا گیا ہے تاکہ قاری حدیث کے الفاظ اور مصنف کے تبصرے میں بآسانی فرق کر سکے۔",
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 13.sp,
                  lineHeight = 22.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  textAlign = TextAlign.Right
                )
              )
            }
          }
        }
      }

      // Sections
      itemsIndexed(
        items = filteredSections,
        key = { _, sec -> "paper1_sec_${sec.id}" }
      ) { _, section ->
        Paper1WasiyatSectionCard(
          section = section,
          arabicBoost = arabicBoost,
          urduBoost = urduBoost,
          onCopy = { label, text -> copyExtraResearchText(context, label, text) },
          onAddNote = { text ->
            onAddNote(text)
            Toast.makeText(context, "Saved to Study Notes", Toast.LENGTH_SHORT).show()
          }
        )
      }

      // Footer Attribution & Transcription Notes Card
      if (paper.meta.transcriptionNotesEn.isNotEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = EmeraldPrimary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "Publisher & Editorial Notes (${paper.meta.website})",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
              paper.meta.transcriptionNotesEn.forEach { note ->
                Text(
                  text = "• $note",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun Paper1WasiyatSectionCard(
  section: WasiyatSection,
  arabicBoost: Int,
  urduBoost: Int,
  onCopy: (String, String) -> Unit,
  onAddNote: (String) -> Unit
) {
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("paper1_section_card_${section.id}"),
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
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Section Header Banner
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = EmeraldPrimary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldPrimary
            ) {
              Text(
                text = "Page ${section.page} • ${section.id.uppercase()}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
            }
            Text(
              text = "${section.items.size} Evidence Items",
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
              )
            )
          }

          Text(
            text = section.titleUr,
            style = MaterialTheme.typography.titleLarge.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = (19 + urduBoost).sp,
              lineHeight = (32 + urduBoost).sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )

          if (section.introUr.isNotBlank()) {
            Text(
              text = section.introUr,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = (15 + urduBoost).sp,
                lineHeight = (26 + urduBoost).sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // Items inside this section
      section.items.forEachIndexed { idx, item ->
        if (idx > 0) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        }
        Paper1WasiyatItemCard(
          sectionHeadingUr = section.titleUr,
          item = item,
          arabicBoost = arabicBoost,
          urduBoost = urduBoost,
          onCopy = onCopy,
          onAddNote = onAddNote
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Paper1WasiyatItemCard(
  sectionHeadingUr: String,
  item: WasiyatItem,
  arabicBoost: Int,
  urduBoost: Int,
  onCopy: (String, String) -> Unit,
  onAddNote: (String) -> Unit
) {
  val (typeBadgeLabel, badgeBgColor, badgeTextColor) = when (item.type) {
    "hadith" -> Triple("صحیح حدیث • Prophetic Hadith", EmeraldPrimary.copy(alpha = 0.16f), EmeraldPrimary)
    "ayah" -> Triple("قرآنِ حکیم • Qur'anic Verse", GoldPrimary.copy(alpha = 0.22f), EmeraldPrimary)
    "dua" -> Triple("مسنون دُعا • Prophetic Supplication", EmeraldPrimary.copy(alpha = 0.18f), EmeraldPrimary)
    "scholar_quote" -> Triple("قولِ مفسر / مورخ • Scholar Quote", Color(0xFF0D47A1).copy(alpha = 0.14f), Color(0xFF0D47A1))
    "narrative" -> Triple("تاریخی پس منظر • Historical Narrative", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    "note" -> Triple("مصنف کا تحقیقی نوٹ • Author's Note (نوٹ)", GoldPrimary.copy(alpha = 0.26f), Color(0xFF8D5B00))
    else -> Triple(item.type, EmeraldPrimary.copy(alpha = 0.12f), EmeraldPrimary)
  }

  val cardBgColor = when (item.type) {
    "note" -> GoldPrimary.copy(alpha = 0.10f)
    "ayah", "dua" -> EmeraldPrimary.copy(alpha = 0.05f)
    else -> Color.Transparent
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = cardBgColor,
    border = if (item.type == "note") {
      BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f))
    } else if (item.type == "ayah" || item.type == "dua") {
      BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.30f))
    } else {
      null
    },
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(if (item.type == "note" || item.type == "ayah" || item.type == "dua") 10.dp else 4.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Item Type Badge + Narrator / Item Number
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = badgeBgColor
        ) {
          Text(
            text = if (item.number != null) "${item.number}. $typeBadgeLabel" else typeBadgeLabel,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = badgeTextColor
            )
          )
        }

        if (item.narratorUr.isNotBlank()) {
          Text(
            text = "راوی: ${item.narratorUr}",
            style = MaterialTheme.typography.labelMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Right
            )
          )
        }
      }

      // Context / Intro Urdu (if any)
      if (item.introUr.isNotBlank()) {
        Text(
          text = item.introUr,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = (15 + urduBoost).sp,
            lineHeight = (26 + urduBoost).sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Right
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Arabic Text Box
      val arabicText = item.primaryArabicText
      if (arabicText.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.28f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = arabicText,
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
              fontFamily = AmiriFontFamily,
              fontSize = (20 + arabicBoost).sp,
              lineHeight = (35 + arabicBoost).sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Right
            )
          )
        }
      }

      // Urdu Text
      if (item.textUr.isNotBlank()) {
        Text(
          text = item.textUr,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = (16 + urduBoost).sp,
            lineHeight = (28 + urduBoost).sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Right
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Attached Inline Note (if item.inlineNoteUr is present on a Hadith/Item)
      if (item.inlineNoteUr.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = GoldPrimary.copy(alpha = 0.14f),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Text(
              text = "مصنف کا تحقیقی نوٹ (Author's Note):",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D5B00),
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = item.inlineNoteUr,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = (14 + urduBoost).sp,
                lineHeight = (25 + urduBoost).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // References Chips
      if (item.references.isNotEmpty()) {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          item.references.forEach { ref ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldPrimary.copy(alpha = 0.12f),
              border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
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
                  text = ref.formatted,
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

      // Copy & Save Note Actions
      val fullTextForCopy = buildString {
        appendLine("【رسول اللہ ﷺ کی آخری وصیّتیں — $sectionHeadingUr】")
        if (item.narratorUr.isNotBlank()) appendLine("راوی: ${item.narratorUr}")
        if (arabicText.isNotBlank()) appendLine(arabicText)
        if (item.textUr.isNotBlank()) appendLine(item.textUr)
        if (item.inlineNoteUr.isNotBlank()) appendLine("نوٹ: ${item.inlineNoteUr}")
        if (item.references.isNotEmpty()) {
          appendLine("حوالہ: ${item.references.joinToString(" ، ") { it.formatted }}")
        }
      }.trim()

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = { onCopy(sectionHeadingUr, fullTextForCopy) },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.height(30.dp)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(6.dp))
        TextButton(
          onClick = { onAddNote(fullTextForCopy) },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.height(30.dp)
        ) {
          Icon(Icons.Default.NoteAdd, contentDescription = "Save to Notes", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

// =========================================================================================
// 2. RESEARCH PAPER 7 READER: قرآنِ حکیم کے 10 بنیادی احکام اور اولیاء کرام کی 12 صفات
// =========================================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Paper7CommandmentsInteractiveReaderView(
  paper: QuranCommandmentsAndAwliyaPaper,
  initialSectionIndex: Int = 0,
  onClose: () -> Unit,
  onAddNote: (String) -> Unit
) {
  val context = LocalContext.current
  var selectedSectionTab by remember(initialSectionIndex) {
    mutableIntStateOf(-1)
  } // -1 = Both Sections, 0 = 10 Commandments, 1 = 12 Attributes of Awliya
  var inPaperSearchQuery by remember { mutableStateOf("") }
  var fontScaleStep by remember { mutableIntStateOf(0) }
  val muhasabahCheckedMap = remember { mutableStateMapOf<String, Boolean>() }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(initialSectionIndex) {
    if (initialSectionIndex == 1) {
      selectedSectionTab = 1
    }
  }

  val totalPointsCount = remember(paper.sections) {
    paper.sections.sumOf { it.items.size }
  }
  val checkedPointsCount = muhasabahCheckedMap.values.count { it }

  val visibleSections = remember(paper.sections, selectedSectionTab, inPaperSearchQuery) {
    val base = if (selectedSectionTab in paper.sections.indices) {
      listOf(paper.sections[selectedSectionTab])
    } else {
      paper.sections
    }
    val q = inPaperSearchQuery.trim()
    if (q.isBlank()) {
      base
    } else {
      base.mapNotNull { sec ->
        val matching = sec.items.filter { item ->
          item.arabic.contains(q, ignoreCase = true) ||
            item.urdu.contains(q, ignoreCase = true) ||
            item.note.contains(q, ignoreCase = true) ||
            item.arabicNote.contains(q, ignoreCase = true) ||
            (item.supportingVerse?.arabic?.contains(q, ignoreCase = true) == true) ||
            (item.supportingVerse?.urdu?.contains(q, ignoreCase = true) == true) ||
            (item.supportingVerse?.conclusion?.contains(q, ignoreCase = true) == true) ||
            item.number.toString() == q
        }
        if (matching.isNotEmpty() || sec.title.contains(q, ignoreCase = true)) {
          sec.copy(items = if (matching.isNotEmpty()) matching else sec.items)
        } else {
          null
        }
      }
    }
  }

  val arabicBoost = (fontScaleStep * 2)
  val urduBoost = (fontScaleStep * 2)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("paper_7_commandments_reader_view")
  ) {
    // Sticky Top Bar
    Surface(
      color = EmeraldPrimary.copy(alpha = 0.12f),
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            IconButton(
              onClick = onClose,
              modifier = Modifier
                .size(36.dp)
                .testTag("paper_7_back_button")
            ) {
              Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Islamic Research",
                tint = EmeraldPrimary
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "قرآنِ حکیم کے 10 بنیادی احکام اور اولیاء کی 12 صفات",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 17.sp,
                  lineHeight = 27.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                ),
                maxLines = 1
              )
              Text(
                text = "Research Paper #${paper.publication.researchPaperNumber} • Muhasabah Checked: $checkedPointsCount / $totalPointsCount",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }

          // Font Scale + Reset Muhasabah
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
              modifier = Modifier.clickable {
                fontScaleStep = (fontScaleStep - 1).coerceAtLeast(-1)
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
                fontScaleStep = (fontScaleStep + 1).coerceAtMost(3)
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
            if (checkedPointsCount > 0) {
              IconButton(
                onClick = { muhasabahCheckedMap.clear() },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  Icons.Default.Refresh,
                  contentDescription = "Reset Checklist",
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        // In-Paper Search Bar
        OutlinedTextField(
          value = inPaperSearchQuery,
          onValueChange = { inPaperSearchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("paper_7_search_input"),
          placeholder = {
            Text(
              "Search 10 Commandments & 12 Attributes (e.g. والدین, اسراف, عباد الرحمن, النمل, 62)...",
              fontSize = 11.sp
            )
          },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            if (inPaperSearchQuery.isNotEmpty()) {
              IconButton(onClick = { inPaperSearchQuery = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )

        // Section Filter Tabs (All 22, 10 Commandments, 12 Attributes of Awliya)
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            FilterChip(
              selected = selectedSectionTab == -1,
              onClick = {
                selectedSectionTab = -1
                coroutineScope.launch { listState.scrollToItem(0) }
              },
              label = {
                Text("All ($totalPointsCount Points)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("paper_7_tab_all")
            )
          }
          paper.sections.forEachIndexed { idx, _ ->
            item {
              FilterChip(
                selected = selectedSectionTab == idx,
                onClick = {
                  selectedSectionTab = if (selectedSectionTab == idx) -1 else idx
                  coroutineScope.launch { listState.scrollToItem(0) }
                },
                label = {
                  Text(
                    text = if (idx == 0) {
                      "① 10 بنیادی احکام (سورۃ بنی اسرائیل)"
                    } else {
                      "② اولیاء کی 12 صفات (سورۃ الفرقان)"
                    },
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("paper_7_tab_section_$idx")
              )
            }
          }
        }
      }
    }

    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .testTag("paper_7_lazy_column"),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Publication & Interactive Muhasabah Header Card
      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.elevatedCardColors(
            containerColor = GoldPrimary.copy(alpha = 0.12f)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldPrimary
              ) {
                Text(
                  text = "Research Paper #${paper.publication.researchPaperNumber} • www.AhleSunnatPak.com",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Text(
                text = "${paper.publication.dateHijri} • ${paper.publication.dateGregorian}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.Bold
                )
              )
            }

            if (paper.basmalaAndSalawat.isNotBlank()) {
              Text(
                text = paper.basmalaAndSalawat,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = AmiriFontFamily,
                  fontSize = (18 + arabicBoost).sp,
                  lineHeight = (30 + arabicBoost).sp,
                  color = EmeraldPrimary,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }

            Text(
              text = paper.header,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    Icons.Default.FactCheck,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                  Text(
                    text = "Self-Reflection Checklist (عمل و محاسبہ): $checkedPointsCount / $totalPointsCount",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary
                    )
                  )
                }
                Text(
                  text = "Tap ✓ on any point to mark reflected",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }
          }
        }
      }

      // Render Each Section (Section 1: 10 Commandments, Section 2: 12 Attributes of Awliya)
      visibleSections.forEach { section ->
        item(key = "paper7_sec_header_${section.sectionNumber}") {
          Paper7SectionHeaderBanner(
            section = section,
             urduBoost = urduBoost
          )
        }

        itemsIndexed(
          items = section.items,
          key = { _, item -> "paper7_sec_${section.sectionNumber}_item_${item.number}" }
        ) { _, item ->
          val checkKey = "${section.sectionNumber}_${item.number}"
          val isChecked = muhasabahCheckedMap[checkKey] == true
          Paper7CommandmentOrAttributeCard(
            sectionNumber = section.sectionNumber,
            sectionTitleUrdu = section.title,
            item = item,
            isChecked = isChecked,
            onToggleChecked = { muhasabahCheckedMap[checkKey] = !isChecked },
            arabicBoost = arabicBoost,
            urduBoost = urduBoost,
            onCopy = { label, text -> copyExtraResearchText(context, label, text) },
            onAddNote = { text ->
              onAddNote(text)
              Toast.makeText(context, "Saved to Study Notes", Toast.LENGTH_SHORT).show()
            }
          )
        }

        val finalAdvice = section.finalAdvice
        if (finalAdvice != null) {
          item(key = "paper7_sec_final_advice_${section.sectionNumber}") {
            Paper7FinalAdviceCard(
              sectionTitleUrdu = section.title,
              finalAdvice = finalAdvice,
              arabicBoost = arabicBoost,
              urduBoost = urduBoost,
              onCopy = { label, text -> copyExtraResearchText(context, label, text) },
              onAddNote = { text ->
                onAddNote(text)
                Toast.makeText(context, "Saved to Study Notes", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun Paper7SectionHeaderBanner(
  section: Paper7Section,
  urduBoost: Int
) {
  val surahRefEnglish = if (section.sectionNumber == 1) {
    "Surah Bani Isra'il (17:23–39)"
  } else {
    "Surah Al-Furqan (25:63–77)"
  }

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("paper7_section_header_${section.sectionNumber}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = EmeraldPrimary.copy(alpha = 0.12f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = EmeraldPrimary
        ) {
          Text(
            text = "Section ${section.sectionNumber} • $surahRefEnglish",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
        }
        Text(
          text = "Pages ${section.pages.joinToString("-")}",
          style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary
          )
        )
      }

      Text(
        text = section.title,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = (20 + urduBoost).sp,
          lineHeight = (34 + urduBoost).sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary,
          textAlign = TextAlign.Right
        ),
        modifier = Modifier.fillMaxWidth()
      )

      if (section.intro.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = GoldPrimary.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "تمہیدی نوٹ: ${section.intro}",
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = (14 + urduBoost).sp,
              lineHeight = (25 + urduBoost).sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Right
            )
          )
        }
      }
    }
  }
}

@Composable
private fun Paper7CommandmentOrAttributeCard(
  sectionNumber: Int,
  sectionTitleUrdu: String,
  item: CommandmentOrAttributeItem,
  isChecked: Boolean,
  onToggleChecked: () -> Unit,
  arabicBoost: Int,
  urduBoost: Int,
  onCopy: (String, String) -> Unit,
  onAddNote: (String) -> Unit
) {
  val itemPrefixUrdu = if (sectionNumber == 1) "حکم نمبر ${item.number}" else "صفت نمبر ${item.number}"
  val itemPrefixEn = if (sectionNumber == 1) "Commandment #${item.number}" else "Attribute #${item.number}"

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("paper7_item_card_${sectionNumber}_${item.number}"),
    shape = RoundedCornerShape(15.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = if (isChecked) {
        EmeraldPrimary.copy(alpha = 0.07f)
      } else {
        MaterialTheme.colorScheme.surface
      }
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Top Number Badge & Muhasabah Check Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldPrimary
          ) {
            Text(
              text = "$itemPrefixUrdu • $itemPrefixEn",
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
          if (item.arabicNote.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = GoldPrimary.copy(alpha = 0.20f)
            ) {
              Text(
                text = item.arabicNote,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
          }
        }

        // Muhasabah Toggle Pill
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isChecked) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.10f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.40f)),
          modifier = Modifier
            .clickable { onToggleChecked() }
            .testTag("paper7_muhasabah_toggle_${sectionNumber}_${item.number}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              Icons.Default.CheckCircle,
              contentDescription = "Mark Reflected",
              tint = if (isChecked) Color.White else EmeraldPrimary,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = if (isChecked) "عمل و محاسبہ ✓" else "محاسبہ",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isChecked) Color.White else EmeraldPrimary
              )
            )
          }
        }
      }

      // Arabic Verse Box
      if (item.arabic.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldPrimary.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.28f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = item.arabic,
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
              fontFamily = AmiriFontFamily,
              fontSize = (20 + arabicBoost).sp,
              lineHeight = (36 + arabicBoost).sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Right
            )
          )
        }
      }

      // Urdu Translation
      if (item.urdu.isNotBlank()) {
        Text(
          text = "ترجمہ: ${item.urdu}",
          style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = (16 + urduBoost).sp,
            lineHeight = (28 + urduBoost).sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Right
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Explanatory Note (نوٹ)
      if (item.note.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = GoldPrimary.copy(alpha = 0.14f),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "تحقیقی و تفسیری وضاحت (نوٹ):",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D5B00),
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = item.note,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = (14 + urduBoost).sp,
                lineHeight = (25 + urduBoost).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // Supporting Verse Callout (e.g. Surah An-Naml 27:62 on Attribute #6)
      val sup = item.supportingVerse
      if (sup != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.10f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.40f)),
          modifier = Modifier.fillMaxWidth()
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
                color = EmeraldPrimary
              ) {
                Text(
                  text = "تائیدی آیتِ قرآنی • Supporting Qur'an Verse",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Text(
                text = sup.reference,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }

            Text(
              text = sup.arabic,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = AmiriFontFamily,
                fontSize = (19 + arabicBoost).sp,
                lineHeight = (34 + arabicBoost).sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Text(
              text = "ترجمہ: ${sup.urdu}",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = (15 + urduBoost).sp,
                lineHeight = (26 + urduBoost).sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )

            if (sup.conclusion.isNotBlank()) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldPrimary.copy(alpha = 0.18f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "${sup.conclusionTitle.ifBlank { "نتیجہ" }}: ${sup.conclusion}",
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = (14 + urduBoost).sp,
                    lineHeight = (24 + urduBoost).sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.Right
                  )
                )
              }
            }
          }
        }
      }

      // Copy & Save Note Row
      val copyPayload = buildString {
        appendLine("【$sectionTitleUrdu — $itemPrefixUrdu】")
        appendLine(item.arabic)
        appendLine("ترجمہ: ${item.urdu}")
        if (item.note.isNotBlank()) appendLine("نوٹ: ${item.note}")
        if (sup != null) {
          appendLine("${sup.reference}: ${sup.arabic}")
          appendLine("ترجمہ: ${sup.urdu}")
          if (sup.conclusion.isNotBlank()) appendLine("${sup.conclusionTitle}: ${sup.conclusion}")
        }
      }.trim()

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = { onCopy(itemPrefixUrdu, copyPayload) },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.height(30.dp)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(6.dp))
        TextButton(
          onClick = { onAddNote(copyPayload) },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.height(30.dp)
        ) {
          Icon(Icons.Default.NoteAdd, contentDescription = "Save Note", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun Paper7FinalAdviceCard(
  sectionTitleUrdu: String,
  finalAdvice: FinalAdviceBlock,
  arabicBoost: Int,
  urduBoost: Int,
  onCopy: (String, String) -> Unit,
  onAddNote: (String) -> Unit
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(15.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = GoldPrimary.copy(alpha = 0.14f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = EmeraldPrimary
      ) {
        Text(
          text = "${finalAdvice.title} • Concluding Qur'anic Advice",
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        )
      }

      Text(
        text = finalAdvice.arabic,
        style = MaterialTheme.typography.bodyLarge.copy(
          fontFamily = AmiriFontFamily,
          fontSize = (20 + arabicBoost).sp,
          lineHeight = (35 + arabicBoost).sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary,
          textAlign = TextAlign.Right
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Text(
        text = "ترجمہ: ${finalAdvice.urdu}",
        style = MaterialTheme.typography.bodyLarge.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = (16 + urduBoost).sp,
          lineHeight = (28 + urduBoost).sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Right
        ),
        modifier = Modifier.fillMaxWidth()
      )

      val copyText = buildString {
        appendLine("【$sectionTitleUrdu — ${finalAdvice.title}】")
        appendLine(finalAdvice.arabic)
        appendLine("ترجمہ: ${finalAdvice.urdu}")
      }.trim()

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        TextButton(onClick = { onCopy(finalAdvice.title, copyText) }) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = { onAddNote(copyText) }) {
          Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
