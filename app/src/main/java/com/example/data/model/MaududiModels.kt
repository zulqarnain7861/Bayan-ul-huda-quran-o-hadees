package com.example.data.model

/**
 * Domain models and Licence State Machine for Section B (Fikr-e-Maududi Library)
 * per Bayan al-Huda Build Spec v4.0 (§7).
 *
 * Every work defaults to LINK_ONLY (no bundled copyrighted body text) unless backed
 * by a verified licence state.
 */

enum class WorkType {
  BOOK,
  TAFSEER,
  ESSAY,
  COLLECTION,
  CORRESPONDENCE;

  companion object {
    fun fromRaw(raw: String?): WorkType =
      entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: BOOK
  }
}

enum class EditionCompleteness {
  FULL,
  PARTIAL,
  UNKNOWN;

  companion object {
    fun fromRaw(raw: String?): EditionCompleteness =
      entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: UNKNOWN
  }
}

enum class MaududiLicenceState(
  val badgeLabel: String,
  val allowsInAppFullText: Boolean
) {
  LINK_ONLY(
    badgeLabel = "Bibliographic + Verified Link Only",
    allowsInAppFullText = false
  ),
  LICENSED_EXCERPT(
    badgeLabel = "Short Scholarly Excerpt",
    allowsInAppFullText = false
  ),
  FULL_TEXT_PUBLIC_DOMAIN(
    badgeLabel = "Public Domain Full Text",
    allowsInAppFullText = true
  ),
  OWN_PERMISSION(
    badgeLabel = "Written Permission on File",
    allowsInAppFullText = true
  );

  companion object {
    fun fromRaw(raw: String?): MaududiLicenceState =
      entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: LINK_ONLY
  }
}

enum class MaududiLibraryCategory(
  val id: String,
  val titleEn: String,
  val titleUr: String
) {
  TAFHEEM_UL_QURAN(
    id = "tafheem",
    titleEn = "1. Tafheem-ul-Quran (Bibliographic & Outbound Index)",
    titleUr = "تفہیم القرآن (اشاریہ اور مستند روابط)"
  ),
  BOOKS_AND_WRITINGS(
    id = "books",
    titleEn = "2. Books & Writings Catalog",
    titleUr = "کتب اور تصانیف کا کیٹلاگ"
  ),
  IDEAS_AND_THEMES(
    id = "themes",
    titleEn = "3. Ideas & Core Themes",
    titleUr = "بنیادی افکار و موضوعات"
  ),
  RESEARCH_AND_REFERENCES(
    id = "research",
    titleEn = "4. Research & Primary References",
    titleUr = "تحقیقی حوالہ جات اور اشاریہ"
  )
}

enum class ResearchBlockType(val tagLabel: String) {
  QUOTATION("quotation"),
  SUMMARY("summary"),
  EDITORIAL_NOTE("editorial note")
}

data class MaududiWorkRecord(
  val workId: String,
  val authorId: String,
  val authorName: String,
  val titleEn: String,
  val titleUr: String,
  val workType: WorkType,
  val category: MaududiLibraryCategory,
  val yearPublished: Int?,
  val publisher: String?,
  val languageTag: String,
  val translator: String?,
  val completeness: EditionCompleteness,
  val licenceState: MaududiLicenceState,
  val bibliographicSummary: String,
  val bodyText: String?, // Strictly null when licenceState == LINK_ONLY
  val primarySourceUrl: String,
  val sourceTier: SourceTier,
  val surahNumber: Int? = null,
  val ayahRange: String? = null,
  val researchBlocks: List<MaududiResearchBlock> = emptyList()
) {
  init {
    require(licenceState != MaududiLicenceState.LINK_ONLY || bodyText == null) {
      "LINK_ONLY works must never ship in-app bodyText (workId=$workId)"
    }
  }
}

data class MaududiResearchBlock(
  val id: String,
  val blockType: ResearchBlockType,
  val heading: String,
  val content: String,
  val citationReference: String,
  val outboundUrl: String
)
