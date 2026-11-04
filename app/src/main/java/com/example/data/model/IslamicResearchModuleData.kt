package com.example.data.model

import android.content.Context
import com.example.security.SacredContentIntegrityGuard
import java.io.File
import java.nio.charset.StandardCharsets

data class ResearchTermDefinition(
  val term: String,
  val meaning: String
)

data class ResearchCitationRef(
  val work: String,
  val kind: String,
  val numbers: List<String>,
  val raw: String,
  val volume: String = "",
  val page: String = "",
  val grading: String = ""
)

data class ResearchPaperItem(
  val type: String, // paragraph, note, quran_ayah, ayah_translation, hadith, deed, quote
  val number: String = "",
  val label: String = "",
  val arabic: String = "",
  val urdu: String = "",
  val referencePrinted: String = "",
  val references: List<ResearchCitationRef> = emptyList(),
  val arabicReferenceRaw: String = "",
  val ayahTranslationLabel: String = "",
  val ayahTranslationUrdu: String = "",
  val page: String = "",
  val relatedPaper: String = ""
)

data class ResearchPaperSection(
  val id: String,
  val title: String,
  val intro: String = "",
  val definitions: List<ResearchTermDefinition> = emptyList(),
  val items: List<ResearchPaperItem> = emptyList()
)

data class ResearchPaperMeta(
  val title: String,
  val researchPaper: String,
  val dateGregorian: String,
  val dateHijri: String,
  val totalPages: Int,
  val pageHeader: String,
  val bismillah: String,
  val publisher: String,
  val website: String
)

data class RafziyatNasbiyatYazeediyatPaper(
  val meta: ResearchPaperMeta,
  val definitions: List<ResearchTermDefinition>,
  val callToReader: String,
  val sections: List<ResearchPaperSection>
)

// =========================================================================
// Research Paper 1: رسول اللہ ﷺ کی آخری وصیّتیں (NabiAakhriWasiyatPaper)
// =========================================================================

data class WasiyatReference(
  val workUr: String,
  val chapterUr: String = "",
  val number: String = ""
) {
  val formatted: String
    get() = buildString {
      append(workUr)
      if (chapterUr.isNotBlank()) {
        append(" ، ").append(chapterUr)
      }
      if (number.isNotBlank()) {
        append(" : ").append(number)
      }
    }
}

data class WasiyatItem(
  val id: String,
  val type: String, // hadith, ayah, note, narrative, scholar_quote, dua
  val page: Int = 1,
  val number: Int? = null,
  val narratorUr: String = "",
  val introUr: String = "",
  val textAr: String = "",
  val embeddedAr: String = "",
  val textUr: String = "",
  val inlineNoteUr: String = "",
  val checkEn: String = "",
  val references: List<WasiyatReference> = emptyList()
) {
  val primaryArabicText: String
    get() = textAr.ifBlank { embeddedAr }
}

data class WasiyatSection(
  val id: String,
  val page: Int,
  val titleUr: String,
  val introUr: String = "",
  val items: List<WasiyatItem> = emptyList()
)

data class WasiyatMeta(
  val titleUr: String,
  val titleRoman: String,
  val paperLabel: String,
  val dateHijri: String,
  val dateGregorian: String,
  val language: String,
  val bannerUr: String,
  val basmalaUr: String,
  val openingUr: String,
  val publisherUr: String,
  val website: String,
  val sourceFile: String,
  val pageCount: Int,
  val itemTypes: Map<String, String> = emptyMap(),
  val transcriptionNotesEn: List<String> = emptyList()
)

data class NabiAakhriWasiyatPaper(
  val meta: WasiyatMeta,
  val sections: List<WasiyatSection>
)

// =========================================================================
// Research Paper 7: قرآنِ حکیم کے 10 بنیادی احکام اور اولیاء کی 12 صفات
// =========================================================================

data class Paper7Publication(
  val researchPaperNumber: Int,
  val dateGregorian: String,
  val dateHijri: String,
  val publisherPage2: String,
  val publisherPage4: String
)

data class SupportingVerseItem(
  val reference: String,
  val arabic: String,
  val urdu: String,
  val conclusionTitle: String,
  val conclusion: String
)

data class CommandmentOrAttributeItem(
  val number: Int,
  val arabic: String,
  val arabicNote: String = "",
  val urdu: String,
  val note: String = "",
  val supportingVerse: SupportingVerseItem? = null
)

data class FinalAdviceBlock(
  val title: String,
  val arabic: String,
  val urdu: String
)

data class Paper7Section(
  val sectionNumber: Int,
  val pages: List<Int>,
  val title: String,
  val intro: String,
  val items: List<CommandmentOrAttributeItem>,
  val finalAdvice: FinalAdviceBlock? = null
)

data class QuranCommandmentsAndAwliyaPaper(
  val header: String,
  val basmalaAndSalawat: String,
  val noteOnSymbols: String,
  val publication: Paper7Publication,
  val sections: List<Paper7Section>
)

// =========================================================================
// Sunnah Azkar After Obligatory Prayers (FardNamazAzkarPamphlet)
// =========================================================================

data class AzkarReferenceBadge(
  val book: String,
  val number: String
) {
  val formatted: String
    get() = if (number.isNotBlank()) "$book : $number" else book
}

data class AzkarIntroItem(
  val type: String, // quran, sahih_hadith, sahih_hadith_qudsi
  val arabic: String = "",
  val urdu: String = "",
  val urduTranslation: String = "",
  val references: List<AzkarReferenceBadge> = emptyList()
)

data class AzkarPartItem(
  val repeat: String = "",
  val arabicLines: List<String> = emptyList(),
  val urduTranslation: String = "",
  val references: List<AzkarReferenceBadge> = emptyList()
)

data class FardNamazZikrItem(
  val number: Int,
  val type: String = "zikr",
  val narrator: String = "",
  val narration: String = "",
  val repeat: String = "",
  val arabicLines: List<String> = emptyList(),
  val urduTranslation: String = "",
  val urdu: String = "",
  val note: String = "",
  val arabicIsAbridged: Boolean = false,
  val parts: List<AzkarPartItem> = emptyList(),
  val references: List<AzkarReferenceBadge> = emptyList()
) {
  val targetTasbihCount: Int
    get() = when {
      repeat.contains("33") || parts.any { it.repeat.contains("33") } -> 100
      repeat.contains("10") -> 10
      repeat.contains("7") -> 7
      repeat.contains("3") || parts.any { it.repeat.contains("3") } -> 3
      else -> 1
    }
}

data class AzkarClosingNote(
  val label: String,
  val urdu: String,
  val references: List<AzkarReferenceBadge> = emptyList()
)

data class FardNamazAzkarPamphlet(
  val title: String,
  val subtitle: String,
  val noteOnNumbering: String,
  val openingLine: String,
  val publisher: String,
  val website: String,
  val email: String,
  val introduction: List<AzkarIntroItem>,
  val azkar: List<FardNamazZikrItem>,
  val closingNotes: List<AzkarClosingNote>
)

data class IslamicResearchSearchHit(
  val paperId: String, // "paper_1_wasiyat", "paper_5a", "paper_7_commandments", or "fard_namaz_azkar"
  val paperTitleUrdu: String,
  val paperTitleEnglish: String,
  val sectionOrZikrTitle: String,
  val sectionIndex: Int,
  val snippetUrdu: String,
  val snippetArabic: String,
  val referenceBadge: String
)

/**
 * Pure-Kotlin JSON AST & recursive descent parser so JSON assets parse identically
 * in both Android runtime and plain JVM unit tests without org.json stub exceptions.
 */
private sealed class KJsonVal {
  data class Obj(val map: Map<String, KJsonVal>) : KJsonVal()
  data class Arr(val list: List<KJsonVal>) : KJsonVal()
  data class Str(val value: String) : KJsonVal()
  data class Num(val raw: String) : KJsonVal()
  data class Bool(val value: Boolean) : KJsonVal()
  data object Null : KJsonVal()

  val asObj: Map<String, KJsonVal>?
    get() = (this as? Obj)?.map

  val asArr: List<KJsonVal>?
    get() = (this as? Arr)?.list

  val asString: String
    get() = when (this) {
      is Str -> value
      is Num -> raw
      is Bool -> value.toString()
      else -> ""
    }

  val asInt: Int?
    get() = when (this) {
      is Num -> raw.toIntOrNull()
      is Str -> value.toIntOrNull()
      else -> null
    }

  val asBoolean: Boolean?
    get() = when (this) {
      is Bool -> value
      is Str -> value.toBooleanStrictOrNull()
      else -> null
    }
}

private class PureKotlinJsonParser(private val input: String) {
  private var pos = 0
  private val len = input.length

  fun parseRoot(): KJsonVal {
    skipWhitespace()
    if (pos >= len) return KJsonVal.Obj(emptyMap())
    return parseValue()
  }

  private fun skipWhitespace() {
    while (pos < len) {
      val c = input[pos]
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\uFEFF') {
        pos++
      } else {
        break
      }
    }
  }

  private fun parseValue(): KJsonVal {
    skipWhitespace()
    if (pos >= len) return KJsonVal.Null
    return when (input[pos]) {
      '{' -> parseObject()
      '[' -> parseArray()
      '"' -> KJsonVal.Str(parseString())
      't', 'f' -> parseBoolean()
      'n' -> parseNull()
      else -> parseNumber()
    }
  }

  private fun parseObject(): KJsonVal.Obj {
    val map = LinkedHashMap<String, KJsonVal>()
    pos++ // skip '{'
    skipWhitespace()
    if (pos < len && input[pos] == '}') {
      pos++
      return KJsonVal.Obj(map)
    }
    while (pos < len) {
      skipWhitespace()
      if (pos < len && input[pos] == '}') {
        pos++
        break
      }
      val key = if (input[pos] == '"') parseString() else break
      skipWhitespace()
      if (pos < len && input[pos] == ':') pos++
      val value = parseValue()
      map[key] = value
      skipWhitespace()
      if (pos < len && input[pos] == ',') {
        pos++
      } else if (pos < len && input[pos] == '}') {
        pos++
        break
      } else {
        break
      }
    }
    return KJsonVal.Obj(map)
  }

  private fun parseArray(): KJsonVal.Arr {
    val list = ArrayList<KJsonVal>()
    pos++ // skip '['
    skipWhitespace()
    if (pos < len && input[pos] == ']') {
      pos++
      return KJsonVal.Arr(list)
    }
    while (pos < len) {
      skipWhitespace()
      if (pos < len && input[pos] == ']') {
        pos++
        break
      }
      list.add(parseValue())
      skipWhitespace()
      if (pos < len && input[pos] == ',') {
        pos++
      } else if (pos < len && input[pos] == ']') {
        pos++
        break
      } else {
        break
      }
    }
    return KJsonVal.Arr(list)
  }

  private fun parseString(): String {
    if (pos >= len || input[pos] != '"') return ""
    pos++ // skip opening '"'
    val sb = StringBuilder()
    while (pos < len) {
      val c = input[pos++]
      if (c == '"') {
        break
      } else if (c == '\\' && pos < len) {
        when (val esc = input[pos++]) {
          '"' -> sb.append('"')
          '\\' -> sb.append('\\')
          '/' -> sb.append('/')
          'b' -> sb.append('\b')
          'f' -> sb.append('\u000C')
          'n' -> sb.append('\n')
          'r' -> sb.append('\r')
          't' -> sb.append('\t')
          'u' -> {
            if (pos + 4 <= len) {
              val hex = input.substring(pos, pos + 4)
              val code = hex.toIntOrNull(16)
              if (code != null) sb.append(code.toChar())
              pos += 4
            }
          }
          else -> sb.append(esc)
        }
      } else {
        sb.append(c)
      }
    }
    return sb.toString()
  }

  private fun parseBoolean(): KJsonVal.Bool {
    return if (input.startsWith("true", pos)) {
      pos += 4
      KJsonVal.Bool(true)
    } else if (input.startsWith("false", pos)) {
      pos += 5
      KJsonVal.Bool(false)
    } else {
      pos++
      KJsonVal.Bool(false)
    }
  }

  private fun parseNull(): KJsonVal.Null {
    if (input.startsWith("null", pos)) {
      pos += 4
    } else {
      pos++
    }
    return KJsonVal.Null
  }

  private fun parseNumber(): KJsonVal.Num {
    val start = pos
    while (pos < len) {
      val c = input[pos]
      if (c in '0'..'9' || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
        pos++
      } else {
        break
      }
    }
    val token = if (pos > start) input.substring(start, pos) else "0"
    if (pos == start) pos++
    return KJsonVal.Num(token)
  }
}

object IslamicResearchRepository {
  const val ASSET_PAPER_1_WASIYAT = "nabi_aakhri_wasiyat_v2.json"
  const val ASSET_PAPER_5A = "rafziyat_nasbiyat_yazeediyat_research.json"
  const val ASSET_PAPER_7_COMMANDMENTS = "quran_commandments_and_awliya_attributes.json"
  const val ASSET_FARD_NAMAZ_AZKAR = "fard_namaz_ke_baad_azkar.json"

  @Volatile
  private var cachedPaper1Wasiyat: NabiAakhriWasiyatPaper? = null

  @Volatile
  private var cachedPaper5a: RafziyatNasbiyatYazeediyatPaper? = null

  @Volatile
  private var cachedPaper7Commandments: QuranCommandmentsAndAwliyaPaper? = null

  @Volatile
  private var cachedFardNamazAzkar: FardNamazAzkarPamphlet? = null

  fun loadPaper1Wasiyat(context: Context? = null): NabiAakhriWasiyatPaper {
    cachedPaper1Wasiyat?.let { return it }
    val rawJson = readJsonAssetOrFallback(context, ASSET_PAPER_1_WASIYAT)
    val parsed = parsePaper1WasiyatJson(rawJson)
    cachedPaper1Wasiyat = parsed
    return parsed
  }

  fun loadPaper5a(context: Context? = null): RafziyatNasbiyatYazeediyatPaper {
    cachedPaper5a?.let { return it }
    val rawJson = readJsonAssetOrFallback(context, ASSET_PAPER_5A)
    val parsed = parsePaper5aJson(rawJson)
    cachedPaper5a = parsed
    return parsed
  }

  fun loadPaper7Commandments(context: Context? = null): QuranCommandmentsAndAwliyaPaper {
    cachedPaper7Commandments?.let { return it }
    val rawJson = readJsonAssetOrFallback(context, ASSET_PAPER_7_COMMANDMENTS)
    val parsed = parsePaper7CommandmentsJson(rawJson)
    cachedPaper7Commandments = parsed
    return parsed
  }

  fun loadFardNamazAzkar(context: Context? = null): FardNamazAzkarPamphlet {
    cachedFardNamazAzkar?.let { return it }
    val rawJson = readJsonAssetOrFallback(context, ASSET_FARD_NAMAZ_AZKAR)
    val parsed = parseFardNamazAzkarJson(rawJson)
    cachedFardNamazAzkar = parsed
    return parsed
  }

  private fun readJsonAssetOrFallback(context: Context?, assetFileName: String): String {
    if (context != null) {
      val bytes = SacredContentIntegrityGuard.verifyOrRestoreSacredAsset(
        context = context,
        cachedFile = null,
        assetFileName = assetFileName
      )
      if (bytes.isNotEmpty()) {
        return String(bytes, StandardCharsets.UTF_8)
      }
    }
    val candidateFiles = listOf(
      File("src/main/assets/$assetFileName"),
      File("app/src/main/assets/$assetFileName"),
      File("../app/src/main/assets/$assetFileName")
    )
    for (file in candidateFiles) {
      if (file.exists() && file.isFile) {
        return file.readText(StandardCharsets.UTF_8)
      }
    }
    val stream = IslamicResearchRepository::class.java.classLoader?.getResourceAsStream("assets/$assetFileName")
      ?: IslamicResearchRepository::class.java.classLoader?.getResourceAsStream(assetFileName)
    if (stream != null) {
      return stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }
    return "{}"
  }

  fun parsePaper1WasiyatJson(rawJson: String): NabiAakhriWasiyatPaper {
    val root = runCatching { PureKotlinJsonParser(rawJson).parseRoot().asObj }
      .getOrNull() ?: emptyMap()

    val metaObj = root["meta"]?.asObj ?: emptyMap()
    val itemTypesMap = metaObj["item_types"]?.asObj?.mapValues { it.value.asString.trim() } ?: emptyMap()
    val notesEn = metaObj["transcription_notes_en"]?.asArr?.map { it.asString.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    val meta = WasiyatMeta(
      titleUr = metaObj.str("title_ur").ifBlank { "رسول اللہ ﷺ کی آخری وصیّتیں" },
      titleRoman = metaObj.str("title_roman").ifBlank { "Rasool Allah SAW ki Aakhri Wasiyat" },
      paperLabel = metaObj.str("paper_label").ifBlank { "Research Paper 1" },
      dateHijri = metaObj.str("date_hijri").ifBlank { "12 ربیع الاول 1438ھ" },
      dateGregorian = metaObj.str("date_gregorian").ifBlank { "2016-12-12" },
      language = metaObj.str("language").ifBlank { "ur" },
      bannerUr = metaObj.str("banner_ur"),
      basmalaUr = metaObj.str("basmala_ur"),
      openingUr = metaObj.str("opening_ur"),
      publisherUr = metaObj.str("publisher_ur"),
      website = metaObj.str("website"),
      sourceFile = metaObj.str("source_file"),
      pageCount = metaObj["page_count"]?.asInt ?: 4,
      itemTypes = itemTypesMap,
      transcriptionNotesEn = notesEn
    )

    val sections = root["sections"]?.asArr?.mapNotNull { secEl ->
      val secObj = secEl.asObj ?: return@mapNotNull null
      val items = secObj["items"]?.asArr?.mapNotNull { itemEl ->
        val itemObj = itemEl.asObj ?: return@mapNotNull null
        val refs = itemObj["references"]?.asArr?.mapNotNull { refEl ->
          val rObj = refEl.asObj ?: return@mapNotNull null
          WasiyatReference(
            workUr = rObj.str("work_ur"),
            chapterUr = rObj.str("chapter_ur"),
            number = rObj.str("number")
          )
        } ?: emptyList()

        WasiyatItem(
          id = itemObj.str("id"),
          type = itemObj.str("type").ifBlank { "hadith" },
          page = itemObj["page"]?.asInt ?: (secObj["page"]?.asInt ?: 1),
          number = itemObj["number"]?.asInt,
          narratorUr = itemObj.str("narrator_ur"),
          introUr = itemObj.str("intro_ur"),
          textAr = itemObj.str("text_ar"),
          embeddedAr = itemObj.str("embedded_ar"),
          textUr = itemObj.str("text_ur"),
          inlineNoteUr = itemObj.str("inline_note_ur"),
          checkEn = itemObj.str("check_en"),
          references = refs
        )
      } ?: emptyList()

      WasiyatSection(
        id = secObj.str("id"),
        page = secObj["page"]?.asInt ?: 1,
        titleUr = secObj.str("title_ur"),
        introUr = secObj.str("intro_ur"),
        items = items
      )
    } ?: emptyList()

    return NabiAakhriWasiyatPaper(
      meta = meta,
      sections = sections
    )
  }

  fun parsePaper7CommandmentsJson(rawJson: String): QuranCommandmentsAndAwliyaPaper {
    val root = runCatching { PureKotlinJsonParser(rawJson).parseRoot().asObj }
      .getOrNull() ?: emptyMap()

    val pubObj = root["publication"]?.asObj ?: emptyMap()
    val publication = Paper7Publication(
      researchPaperNumber = pubObj["research_paper_number"]?.asInt ?: 7,
      dateGregorian = pubObj.str("date_gregorian").ifBlank { "10 November 2019" },
      dateHijri = pubObj.str("date_hijri").ifBlank { "12 ربیع الاول 1441ھ" },
      publisherPage2 = pubObj.str("publisher_page_2"),
      publisherPage4 = pubObj.str("publisher_page_4")
    )

    val sections = root["sections"]?.asArr?.mapNotNull { secEl ->
      val secObj = secEl.asObj ?: return@mapNotNull null
      val pages = secObj["pages"]?.asArr?.mapNotNull { it.asInt } ?: emptyList()
      val items = secObj["items"]?.asArr?.mapNotNull { itemEl ->
        val itemObj = itemEl.asObj ?: return@mapNotNull null
        val svObj = itemObj["supporting_verse"]?.asObj
        val supportingVerse = if (svObj != null) {
          SupportingVerseItem(
            reference = svObj.str("reference"),
            arabic = svObj.str("arabic"),
            urdu = svObj.str("urdu"),
            conclusionTitle = svObj.str("conclusion_title"),
            conclusion = svObj.str("conclusion")
          )
        } else {
          null
        }
        CommandmentOrAttributeItem(
          number = itemObj["number"]?.asInt ?: 0,
          arabic = itemObj.str("arabic"),
          arabicNote = itemObj.str("arabic_note"),
          urdu = itemObj.str("urdu"),
          note = itemObj.str("note"),
          supportingVerse = supportingVerse
        )
      } ?: emptyList()

      val faObj = secObj["final_advice"]?.asObj
      val finalAdvice = if (faObj != null) {
        FinalAdviceBlock(
          title = faObj.str("title"),
          arabic = faObj.str("arabic"),
          urdu = faObj.str("urdu")
        )
      } else {
        null
      }

      Paper7Section(
        sectionNumber = secObj["section_number"]?.asInt ?: 1,
        pages = pages,
        title = secObj.str("title"),
        intro = secObj.str("intro"),
        items = items,
        finalAdvice = finalAdvice
      )
    } ?: emptyList()

    return QuranCommandmentsAndAwliyaPaper(
      header = root.str("header").ifBlank { "فرقہ واریت سے بچ کر “دینِ اسلام” پر چلنے والوں کیلئے" },
      basmalaAndSalawat = root.str("basmala_and_salawat"),
      noteOnSymbols = root.str("note_on_symbols"),
      publication = publication,
      sections = sections
    )
  }

  fun parsePaper5aJson(rawJson: String): RafziyatNasbiyatYazeediyatPaper {
    val root = runCatching { PureKotlinJsonParser(rawJson).parseRoot().asObj }
      .getOrNull() ?: emptyMap()

    val metaObj = root["meta"]?.asObj ?: emptyMap()
    val meta = ResearchPaperMeta(
      title = metaObj.str("title").ifBlank { "رافضیت ، ناصبیت اور یزیدیت کا تحقیقی جائزہ" },
      researchPaper = metaObj.str("research_paper").ifBlank { "5a" },
      dateGregorian = metaObj.str("date_gregorian").ifBlank { "24 October 2015" },
      dateHijri = metaObj.str("date_hijri").ifBlank { "10 محرم الحرام 1437ھ" },
      totalPages = metaObj["total_pages"]?.asInt ?: 4,
      pageHeader = metaObj.str("page_header"),
      bismillah = metaObj.str("bismillah"),
      publisher = metaObj.str("publisher"),
      website = metaObj.str("website")
    )

    val definitions = root["definitions"]?.asArr?.mapNotNull { el ->
      val obj = el.asObj ?: return@mapNotNull null
      ResearchTermDefinition(
        term = obj.str("term"),
        meaning = obj.str("meaning")
      )
    } ?: emptyList()

    val callToReader = root.str("call_to_reader")

    val sections = root["sections"]?.asArr?.mapNotNull { secEl ->
      val secObj = secEl.asObj ?: return@mapNotNull null
      val secDefs = secObj["definitions"]?.asArr?.mapNotNull { defEl ->
        val dObj = defEl.asObj ?: return@mapNotNull null
        ResearchTermDefinition(
          term = dObj.str("term"),
          meaning = dObj.str("meaning")
        )
      } ?: emptyList()

      val items = secObj["items"]?.asArr?.mapNotNull { itemEl ->
        val itemObj = itemEl.asObj ?: return@mapNotNull null
        val refs = itemObj["references"]?.asArr?.mapNotNull { refEl ->
          val rObj = refEl.asObj ?: return@mapNotNull null
          val nums = rObj["numbers"]?.asArr?.map { it.asString }?.filter { it.isNotBlank() } ?: emptyList()
          ResearchCitationRef(
            work = rObj.str("work"),
            kind = rObj.str("kind"),
            numbers = nums,
            raw = rObj.str("raw"),
            volume = rObj.str("volume"),
            page = rObj.str("page"),
            grading = rObj.str("grading")
          )
        } ?: emptyList()

        val arabicRefObj = itemObj["arabic_reference"]?.asObj
        val ayahTransObj = itemObj["ayah_translation"]?.asObj

        ResearchPaperItem(
          type = itemObj.str("type").ifBlank { "paragraph" },
          number = itemObj.str("number"),
          label = itemObj.str("label"),
          arabic = itemObj.str("arabic"),
          urdu = itemObj.str("urdu"),
          referencePrinted = itemObj.str("reference_printed"),
          references = refs,
          arabicReferenceRaw = arabicRefObj?.str("raw") ?: "",
          ayahTranslationLabel = ayahTransObj?.str("label") ?: "",
          ayahTranslationUrdu = ayahTransObj?.str("urdu") ?: "",
          page = itemObj.str("page"),
          relatedPaper = itemObj.str("related_paper")
        )
      } ?: emptyList()

      ResearchPaperSection(
        id = secObj.str("id"),
        title = secObj.str("title"),
        intro = secObj.str("intro"),
        definitions = secDefs,
        items = items
      )
    } ?: emptyList()

    return RafziyatNasbiyatYazeediyatPaper(
      meta = meta,
      definitions = definitions,
      callToReader = callToReader,
      sections = sections
    )
  }

  fun parseFardNamazAzkarJson(rawJson: String): FardNamazAzkarPamphlet {
    val root = runCatching { PureKotlinJsonParser(rawJson).parseRoot().asObj }
      .getOrNull() ?: emptyMap()

    val introList = root["introduction"]?.asArr?.mapNotNull { el ->
      val obj = el.asObj ?: return@mapNotNull null
      AzkarIntroItem(
        type = obj.str("type"),
        arabic = obj.str("arabic"),
        urdu = obj.str("urdu"),
        urduTranslation = obj.str("urdu_translation"),
        references = parseAzkarRefs(obj["references"])
      )
    } ?: emptyList()

    val azkarList = root["azkar"]?.asArr?.mapNotNull { el ->
      val obj = el.asObj ?: return@mapNotNull null
      val partsList = obj["parts"]?.asArr?.mapNotNull { partEl ->
        val pObj = partEl.asObj ?: return@mapNotNull null
        AzkarPartItem(
          repeat = pObj.str("repeat"),
          arabicLines = extractArabicLines(pObj["arabic"]),
          urduTranslation = pObj.str("urdu_translation"),
          references = parseAzkarRefs(pObj["references"])
        )
      } ?: emptyList()

      FardNamazZikrItem(
        number = obj["number"]?.asInt ?: 0,
        type = obj.str("type").ifBlank { "zikr" },
        narrator = obj.str("narrator"),
        narration = obj.str("narration"),
        repeat = obj.str("repeat"),
        arabicLines = extractArabicLines(obj["arabic"]),
        urduTranslation = obj.str("urdu_translation"),
        urdu = obj.str("urdu"),
        note = obj.str("note"),
        arabicIsAbridged = obj["arabic_is_abridged"]?.asBoolean ?: false,
        parts = partsList,
        references = parseAzkarRefs(obj["references"])
      )
    } ?: emptyList()

    val closingNotes = root["closing_notes"]?.asArr?.mapNotNull { el ->
      val obj = el.asObj ?: return@mapNotNull null
      AzkarClosingNote(
        label = obj.str("label"),
        urdu = obj.str("urdu"),
        references = parseAzkarRefs(obj["references"])
      )
    } ?: emptyList()

    return FardNamazAzkarPamphlet(
      title = root.str("title").ifBlank { "فرض نماز کے بعد سُنّت اَذکار" },
      subtitle = root.str("subtitle"),
      noteOnNumbering = root.str("note_on_numbering"),
      openingLine = root.str("opening_line"),
      publisher = root.str("publisher"),
      website = root.str("website"),
      email = root.str("email"),
      introduction = introList,
      azkar = azkarList,
      closingNotes = closingNotes
    )
  }

  private fun parseAzkarRefs(element: KJsonVal?): List<AzkarReferenceBadge> {
    val arr = element?.asArr ?: return emptyList()
    return arr.mapNotNull { el ->
      val obj = el.asObj ?: return@mapNotNull null
      AzkarReferenceBadge(
        book = obj.str("book"),
        number = obj.str("number")
      )
    }
  }

  private fun extractArabicLines(element: KJsonVal?): List<String> {
    return when (element) {
      is KJsonVal.Str -> {
        val text = element.value.trim()
        if (text.isNotEmpty()) listOf(text) else emptyList()
      }
      is KJsonVal.Arr -> element.list.map { it.asString.trim() }.filter { it.isNotEmpty() }
      else -> emptyList()
    }
  }

  private fun Map<String, KJsonVal>.str(key: String): String {
    return this[key]?.asString?.trim() ?: ""
  }

  private val HARAKAT_REGEX = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

  private fun normalizeForSearch(input: String): String {
    if (input.isEmpty()) return ""
    return HARAKAT_REGEX.replace(input, "")
      .replace('أ', 'ا')
      .replace('إ', 'ا')
      .replace('آ', 'ا')
      .replace('ٱ', 'ا')
      .replace('ة', 'ه')
      .replace('ہ', 'ه')
      .replace('ی', 'ي')
      .replace('ے', 'ي')
      .replace('ک', 'ك')
  }

  private fun matchesSearch(haystack: String, rawQuery: String, normalizedQuery: String): Boolean {
    if (haystack.contains(rawQuery, ignoreCase = true)) return true
    if (normalizedQuery.isNotEmpty() && normalizeForSearch(haystack).contains(normalizedQuery, ignoreCase = true)) {
      return true
    }
    return false
  }

  fun searchAcrossIslamicResearch(
    rawQuery: String,
    context: Context? = null
  ): List<IslamicResearchSearchHit> {
    val query = rawQuery.trim()
    if (query.isBlank()) return emptyList()
    val normQuery = normalizeForSearch(query)

    val paper1 = loadPaper1Wasiyat(context)
    val paper5a = loadPaper5a(context)
    val paper7 = loadPaper7Commandments(context)
    val azkarPamphlet = loadFardNamazAzkar(context)
    val hits = mutableListOf<IslamicResearchSearchHit>()

    // 1. Search Research Paper 1: رسول اللہ ﷺ کی آخری وصیّتیں
    paper1.sections.forEachIndexed { secIdx, section ->
      val secTitleMatches = matchesSearch(section.titleUr, query, normQuery) ||
        matchesSearch(section.introUr, query, normQuery)

      section.items.forEach { item ->
        val refText = item.references.joinToString(" • ") { it.formatted }
        val combined = buildString {
          append(section.titleUr).append(" ")
          append(item.narratorUr).append(" ")
          append(item.introUr).append(" ")
          append(item.primaryArabicText).append(" ")
          append(item.textUr).append(" ")
          append(item.inlineNoteUr).append(" ")
          append(refText)
        }
        if (secTitleMatches || matchesSearch(combined, query, normQuery)) {
          hits.add(
            IslamicResearchSearchHit(
              paperId = "paper_1_wasiyat",
              paperTitleUrdu = paper1.meta.titleUr,
              paperTitleEnglish = "Research Paper 1: The Final Will & Testaments of Prophet Muhammad ﷺ",
              sectionOrZikrTitle = section.titleUr,
              sectionIndex = secIdx,
              snippetUrdu = item.textUr,
              snippetArabic = item.primaryArabicText,
              referenceBadge = refText.ifBlank { "Page ${item.page}" }
            )
          )
        }
      }
    }

    // 2. Search definitions in Paper 5a
    paper5a.definitions.forEach { def ->
      if (matchesSearch(def.term, query, normQuery) || matchesSearch(def.meaning, query, normQuery)) {
        hits.add(
          IslamicResearchSearchHit(
            paperId = "paper_5a",
            paperTitleUrdu = paper5a.meta.title,
            paperTitleEnglish = "Research Paper 5a: Critical Review of Rafziyat, Nasbiyat & Yazeediyat",
            sectionOrZikrTitle = "اصطلاحی تعریف: ${def.term}",
            sectionIndex = 0,
            snippetUrdu = "${def.term}: ${def.meaning}",
            snippetArabic = "",
            referenceBadge = "Research Paper 5a • Page 1"
          )
        )
      }
    }

    // 3. Search sections and items in Paper 5a
    paper5a.sections.forEachIndexed { secIdx, section ->
      val secTitleMatches = matchesSearch(section.title, query, normQuery) ||
        matchesSearch(section.intro, query, normQuery)

      section.items.forEach { item ->
        val refText = item.referencePrinted.ifBlank {
          item.references.joinToString(" ، ") { it.raw }
        }
        val combined = buildString {
          append(section.title).append(" ")
          append(item.label).append(" ")
          append(item.arabic).append(" ")
          append(item.urdu).append(" ")
          append(item.ayahTranslationUrdu).append(" ")
          append(refText)
        }
        if (secTitleMatches || matchesSearch(combined, query, normQuery)) {
          hits.add(
            IslamicResearchSearchHit(
              paperId = "paper_5a",
              paperTitleUrdu = paper5a.meta.title,
              paperTitleEnglish = "Research Paper 5a: Critical Review of Rafziyat, Nasbiyat & Yazeediyat",
              sectionOrZikrTitle = section.title,
              sectionIndex = secIdx,
              snippetUrdu = item.urdu.ifBlank { item.ayahTranslationUrdu },
              snippetArabic = item.arabic,
              referenceBadge = refText.ifBlank { "Page ${item.page}" }
            )
          )
        }
      }
    }

    // 4. Search Research Paper 7: قرآن کے 10 بنیادی احکام اور اولیاء کی 12 صفات
    paper7.sections.forEachIndexed { secIdx, section ->
      val secTitleMatches = matchesSearch(section.title, query, normQuery) ||
        matchesSearch(section.intro, query, normQuery)

      section.items.forEach { item ->
        val sv = item.supportingVerse
        val combined = buildString {
          append(section.title).append(" ")
          append(section.intro).append(" ")
          append(item.arabic).append(" ")
          append(item.arabicNote).append(" ")
          append(item.urdu).append(" ")
          append(item.note).append(" ")
          if (sv != null) {
            append(sv.reference).append(" ")
            append(sv.arabic).append(" ")
            append(sv.urdu).append(" ")
            append(sv.conclusion)
          }
        }
        if (secTitleMatches || matchesSearch(combined, query, normQuery)) {
          val badge = if (section.sectionNumber == 1) {
            "سورۃ بنی اسرائیل : حکم #${item.number}"
          } else {
            "سورۃ الفرقان : صفت #${item.number}"
          }
          hits.add(
            IslamicResearchSearchHit(
              paperId = "paper_7_commandments",
              paperTitleUrdu = "قرآنِ حکیم کے 10 بنیادی احکام اور اولیاء کی 12 صفات",
              paperTitleEnglish = "Research Paper 7: 10 Qur'anic Commandments & 12 Attributes of True Awliya",
              sectionOrZikrTitle = section.title,
              sectionIndex = secIdx,
              snippetUrdu = item.urdu,
              snippetArabic = item.arabic,
              referenceBadge = badge
            )
          )
        }
      }
    }

    // 5. Search Fard Namaz Ke Baad Sunnat Azkar
    azkarPamphlet.introduction.forEachIndexed { idx, intro ->
      val refs = intro.references.joinToString(" ، ") { it.formatted }
      val combined = "${intro.arabic} ${intro.urdu} ${intro.urduTranslation} $refs"
      if (matchesSearch(combined, query, normQuery)) {
        hits.add(
          IslamicResearchSearchHit(
            paperId = "fard_namaz_azkar",
            paperTitleUrdu = azkarPamphlet.title,
            paperTitleEnglish = "Sunnah Azkar After Obligatory Prayers (Sahih Ahadith)",
            sectionOrZikrTitle = "مقدمہ و فضیلتِ ذکر (#${idx + 1})",
            sectionIndex = 0,
            snippetUrdu = intro.urdu.ifBlank { intro.urduTranslation },
            snippetArabic = intro.arabic,
            referenceBadge = refs
          )
        )
      }
    }

    azkarPamphlet.azkar.forEachIndexed { zikrIdx, zikr ->
      val refs = (zikr.references + zikr.parts.flatMap { it.references }).joinToString(" ، ") { it.formatted }
      val allArabic = (zikr.arabicLines + zikr.parts.flatMap { it.arabicLines }).joinToString(" ")
      val allUrdu = buildString {
        append(zikr.narrator).append(" ")
        append(zikr.narration).append(" ")
        append(zikr.urduTranslation).append(" ")
        append(zikr.urdu).append(" ")
        append(zikr.note).append(" ")
        zikr.parts.forEach { append(it.urduTranslation).append(" ") }
      }
      if (matchesSearch("$allArabic $allUrdu $refs #${zikr.number}", query, normQuery)) {
        hits.add(
          IslamicResearchSearchHit(
            paperId = "fard_namaz_azkar",
            paperTitleUrdu = azkarPamphlet.title,
            paperTitleEnglish = "Sunnah Azkar After Obligatory Prayers (Sahih Ahadith)",
            sectionOrZikrTitle = "ذکر نمبر ${zikr.number} — ${zikr.narrator.ifBlank { "مسنون ذکر و دعا" }}",
            sectionIndex = zikrIdx,
            snippetUrdu = zikr.urduTranslation.ifBlank { zikr.narration.ifBlank { zikr.urdu } },
            snippetArabic = allArabic,
            referenceBadge = refs
          )
        )
      }
    }

    azkarPamphlet.closingNotes.forEach { note ->
      val refs = note.references.joinToString(" ، ") { it.formatted }
      if (matchesSearch("${note.label} ${note.urdu} $refs", query, normQuery)) {
        hits.add(
          IslamicResearchSearchHit(
            paperId = "fard_namaz_azkar",
            paperTitleUrdu = azkarPamphlet.title,
            paperTitleEnglish = "Sunnah Azkar After Obligatory Prayers (Sahih Ahadith)",
            sectionOrZikrTitle = "${note.label} (اختتامی فوائد و نوٹس)",
            sectionIndex = (azkarPamphlet.azkar.size - 1).coerceAtLeast(0),
            snippetUrdu = note.urdu,
            snippetArabic = "",
            referenceBadge = refs
          )
        )
      }
    }

    return hits
  }
}
