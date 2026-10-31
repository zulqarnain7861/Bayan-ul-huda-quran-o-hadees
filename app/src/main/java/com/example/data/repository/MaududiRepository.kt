package com.example.data.repository

import android.content.Context
import com.example.data.local.AuthorEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.CommentaryEntryEntity
import com.example.data.local.EditionEntity
import com.example.data.local.KnowledgeDatabase
import com.example.data.local.SourceLinkEntity
import com.example.data.local.VerseRefEntity
import com.example.data.local.WorkEntity
import com.example.data.model.EditionCompleteness
import com.example.data.model.MaududiLibraryCategory
import com.example.data.model.MaududiLicenceState
import com.example.data.model.MaududiResearchBlock
import com.example.data.model.MaududiWorkRecord
import com.example.data.model.ResearchBlockType
import com.example.data.model.SourceTier
import com.example.data.model.WorkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bibliographic & Licence-State Enforced Repository for Section B (Fikr-e-Maududi Library)
 * per Bayan al-Huda Build Spec v4.0 (§7, V6, T9, T10).
 *
 * Strictly enforces:
 * - Zero unverified third-party corpus text bundled in APK assets.
 * - All `LINK_ONLY` works and `CommentaryEntryEntity` rows have `text = null`.
 * - Outbound links point to verified HTTPS destinations (`https://www.readmaududi.com`,
 *   `https://openlibrary.org/authors/OL103542A`).
 */
class MaududiRepository(
  private val context: Context,
  private val database: KnowledgeDatabase = KnowledgeDatabase.getInstance(context)
) {

  companion object {
    const val AUTHOR_ID_MAUDUDI = "author_maududi"
    const val TAFHEEM_DISCLOSURE_NOTICE =
      "Tafheem-ul-Quran text is not bundled. This section provides bibliographic records and links to authorised reading sources until a lawful edition is obtained."

    fun buildSeedWorks(): List<MaududiWorkRecord> = listOf(
      // Category 1: Tafheem-ul-Quran (Bibliographic & Outbound Index) — LINK_ONLY, bodyText = null
      MaududiWorkRecord(
        workId = "tafheem_vol_1",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Tafheem-ul-Quran — Volume I (Surah Al-Fatiha to Surah Al-An'am)",
        titleUr = "تفہیم القرآن — جلد اول (الفاتحہ تا الانعام)",
        workType = WorkType.TAFSEER,
        category = MaududiLibraryCategory.TAFHEEM_UL_QURAN,
        yearPublished = 1949,
        publisher = "Idara Tarjuman-ul-Quran, Lahore",
        languageTag = "ur",
        translator = "Zafar Ishaq Ansari (English Edition Reference)",
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Volume I of the 6-volume Urdu exegesis authored between 1942 and 1972 covering Surahs 1–6 (Al-Fatiha, Al-Baqarah, Aal-e-Imran, An-Nisa, Al-Ma'idah, Al-An'am) with historical and socio-legal introductions.",
        bodyText = null,
        primarySourceUrl = "https://www.readmaududi.com",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE,
        surahNumber = 1,
        ayahRange = "1:1-6:165"
      ),
      MaududiWorkRecord(
        workId = "tafheem_vol_2",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Tafheem-ul-Quran — Volume II (Surah Al-A'raf to Surah Bani Isra'il)",
        titleUr = "تفہیم القرآن — جلد دوم (الاعراف تا بنی اسرائیل)",
        workType = WorkType.TAFSEER,
        category = MaududiLibraryCategory.TAFHEEM_UL_QURAN,
        yearPublished = 1954,
        publisher = "Idara Tarjuman-ul-Quran, Lahore",
        languageTag = "ur",
        translator = "Zafar Ishaq Ansari",
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Volume II covering Surahs 7–17 (Al-A'raf through Al-Isra / Bani Isra'il), focusing on Prophetic missions, covenant history, and ethical foundations.",
        bodyText = null,
        primarySourceUrl = "https://www.readmaududi.com",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE,
        surahNumber = 7,
        ayahRange = "7:1-17:111"
      ),
      MaududiWorkRecord(
        workId = "tafheem_vol_3",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Tafheem-ul-Quran — Volume III (Surah Al-Kahf to Surah Ar-Rum)",
        titleUr = "تفہیم القرآن — جلد سوم (الکہف تا الروم)",
        workType = WorkType.TAFSEER,
        category = MaududiLibraryCategory.TAFHEEM_UL_QURAN,
        yearPublished = 1962,
        publisher = "Idara Tarjuman-ul-Quran, Lahore",
        languageTag = "ur",
        translator = "Abdul Aziz Kamal",
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Volume III covering Surahs 18–30 (Al-Kahf through Ar-Rum), including comparative historical notes on ancient civilisations and Makkan/Madinan socio-moral guidance.",
        bodyText = null,
        primarySourceUrl = "https://www.readmaududi.com",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE,
        surahNumber = 18,
        ayahRange = "18:1-30:60"
      ),
      MaududiWorkRecord(
        workId = "tafheem_vol_4_6",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Tafheem-ul-Quran — Volumes IV–VI (Surah Luqman to Surah An-Nas)",
        titleUr = "تفہیم القرآن — جلد چہارم تا ششم (لقمان تا الناس)",
        workType = WorkType.TAFSEER,
        category = MaududiLibraryCategory.TAFHEEM_UL_QURAN,
        yearPublished = 1972,
        publisher = "Idara Tarjuman-ul-Quran, Lahore",
        languageTag = "ur",
        translator = "Abdul Aziz Kamal",
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Volumes IV–VI completing Surahs 31–114 (Luqman through An-Nas), completed in 1972 after 30 years of authorship.",
        bodyText = null,
        primarySourceUrl = "https://www.readmaududi.com",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE,
        surahNumber = 31,
        ayahRange = "31:1-114:6"
      ),

      // Category 2: Books & Writings Catalog — LINK_ONLY, bodyText = null
      MaududiWorkRecord(
        workId = "book_khutbat",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Khutbat (Let Us Be Muslims — Fundamentals of Islam)",
        titleUr = "خطبات (حقیقتِ ایمان، اسلام، نماز، روزہ، زکوٰۃ، حج)",
        workType = WorkType.BOOK,
        category = MaududiLibraryCategory.BOOKS_AND_WRITINGS,
        yearPublished = 1940,
        publisher = "Islamic Publications, Lahore",
        languageTag = "ur",
        translator = "Khurram Murad",
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Collection of Friday addresses explaining the spiritual and practical purpose of Iman, Salah, Fasting, Zakat, and Hajj in accessible Urdu.",
        bodyText = null,
        primarySourceUrl = "https://www.readmaududi.com",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE
      ),
      MaududiWorkRecord(
        workId = "book_deeniyat",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Risalah-e-Deeniyat (Towards Understanding Islam)",
        titleUr = "رسالہ دینیات",
        workType = WorkType.BOOK,
        category = MaududiLibraryCategory.BOOKS_AND_WRITINGS,
        yearPublished = 1932,
        publisher = "Idara Tarjuman-ul-Quran",
        languageTag = "ur",
        translator = "Khurshid Ahmad",
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Introductory treatise on Tawheed, Prophethood (Risalah), Life after Death (Akhirah), and the distinction between Iman and Islam.",
        bodyText = null,
        primarySourceUrl = "https://openlibrary.org/authors/OL103542A",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE
      ),
      MaududiWorkRecord(
        workId = "book_khilafat_o_malookiat",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Khilafat-o-Malookiat (Caliphate and Monarchy — Historical Study)",
        titleUr = "خلافت و ملوکیت",
        workType = WorkType.BOOK,
        category = MaududiLibraryCategory.BOOKS_AND_WRITINGS,
        yearPublished = 1966,
        publisher = "Idara Tarjuman-ul-Quran, Lahore",
        languageTag = "ur",
        translator = null,
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Historical study published in 1966 examining the constitutional principles of the Rightly Guided Caliphate (Khilafat-e-Rashidah) and the transition to hereditary monarchy in early Islamic history.",
        bodyText = null,
        primarySourceUrl = "https://openlibrary.org/authors/OL103542A",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE
      ),
      MaududiWorkRecord(
        workId = "book_sunnat_ki_aaini_haisiyat",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Sunnat ki Aaini Haisiyat (The Constitutional Status of the Sunnah)",
        titleUr = "سنت کی آئینی حیثیت",
        workType = WorkType.BOOK,
        category = MaududiLibraryCategory.BOOKS_AND_WRITINGS,
        yearPublished = 1961,
        publisher = "Islamic Publications, Lahore",
        languageTag = "ur",
        translator = "Ahmad Hasan",
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Scholarly defense of the authority and preservation of the Prophetic Sunnah and Hadith in response to Hadith-rejectionist (Inkar-e-Hadith) arguments.",
        bodyText = null,
        primarySourceUrl = "https://www.readmaududi.com",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE
      ),

      // Category 3: Ideas & Core Themes — LINK_ONLY bibliographic + theme overview
      MaududiWorkRecord(
        workId = "theme_four_terms",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Quran ki Chaar Bunyadi Istilahein (Ilah, Rabb, Ibadah, Deen)",
        titleUr = "قرآن کی چار بنیادی اصطلاحیں (الٰہ، رب، عبادت، دین)",
        workType = WorkType.ESSAY,
        category = MaududiLibraryCategory.IDEAS_AND_THEMES,
        yearPublished = 1941,
        publisher = "Idara Tarjuman-ul-Quran",
        languageTag = "ur",
        translator = "Abu Asad",
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Lexical and Quranic study of four foundational terms — Ilah (Deity), Rabb (Sustainer/Sovereign), Ibadah (Worship/Servitude), and Deen (Way of Life/Accountability) — alongside classical comparative reviews.",
        bodyText = null,
        primarySourceUrl = "https://openlibrary.org/authors/OL103542A",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE
      ),
      MaududiWorkRecord(
        workId = "theme_shura_justice",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Shura, Rule of Law & Accountability in Early Islamic Governance",
        titleUr = "شوریٰ، عدلِ اجتماعی اور احتساب",
        workType = WorkType.ESSAY,
        category = MaududiLibraryCategory.IDEAS_AND_THEMES,
        yearPublished = 1955,
        publisher = "Islamic Publications, Lahore",
        languageTag = "ur",
        translator = "Khurshid Ahmad",
        completeness = EditionCompleteness.UNKNOWN,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Thematic synthesis of mutual consultation (Ash-Shura 42:38), judicial independence, and public treasury trust in classical Islamic jurisprudence.",
        bodyText = null,
        primarySourceUrl = "https://www.readmaududi.com",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE
      ),

      // Category 4: Research & Primary References — every block carries its tag (quotation | summary | editorial note)
      MaududiWorkRecord(
        workId = "research_comparative_notes",
        authorId = AUTHOR_ID_MAUDUDI,
        authorName = "Sayyid Abul A'la Maududi (1903–1979)",
        titleEn = "Research & Reference Dossier: Tafheem Methodology, Hadith Authority & Historical Sources",
        titleUr = "تحقیقی اشاریہ: منہجِ تفہیم، حجیتِ سنت اور تاریخی مآخذ",
        workType = WorkType.COLLECTION,
        category = MaududiLibraryCategory.RESEARCH_AND_REFERENCES,
        yearPublished = 1972,
        publisher = "Bayan al-Huda Bibliographic Index",
        languageTag = "en",
        translator = null,
        completeness = EditionCompleteness.PARTIAL,
        licenceState = MaududiLicenceState.LINK_ONLY,
        bibliographicSummary = "Structured reference index distinguishing direct citations, thematic summaries, and editorial provenance notes with outbound links to primary sources.",
        bodyText = null,
        primarySourceUrl = "https://www.readmaududi.com",
        sourceTier = SourceTier.THIRD_PARTY_ARCHIVE,
        researchBlocks = listOf(
          MaududiResearchBlock(
            id = "rb_01",
            blockType = ResearchBlockType.SUMMARY,
            heading = "Explanatory Approach in Tafheem-ul-Quran",
            content = "Tafheem-ul-Quran introduces each Surah with its historical period of revelation (Makkan or Madinan phase), socio-historical context, and thematic coherence before verse-by-verse notes.",
            citationReference = "Tafheem-ul-Quran, Vol. I, Muqaddimah (1949)",
            outboundUrl = "https://www.readmaududi.com"
          ),
          MaududiResearchBlock(
            id = "rb_02",
            blockType = ResearchBlockType.QUOTATION,
            heading = "Quranic Principle of Consultation (Surah Ash-Shura 42:38)",
            content = "And those who respond to their Lord, establish prayer, and conduct their affairs by mutual consultation (wa amruhum shura baynahum).",
            citationReference = "Al-Quran 42:38 (Cited in Khilafat-o-Malookiat, Ch. 1)",
            outboundUrl = "https://www.readmaududi.com"
          ),
          MaududiResearchBlock(
            id = "rb_03",
            blockType = ResearchBlockType.EDITORIAL_NOTE,
            heading = "Licence & Provenance Disclosure",
            content = "Third-party scraped repositories on GitHub/HuggingFace lack verified redistribution permissions and are intentionally excluded from this APK. Tap any source link below to consult authorised editions.",
            citationReference = "Bayan al-Huda Spec v4.0 §7.1 (V6 Compliance)",
            outboundUrl = "https://openlibrary.org/authors/OL103542A"
          )
        )
      )
    )
  }

  suspend fun ensureDatabaseSeeded(): List<MaududiWorkRecord> = withContext(Dispatchers.IO) {
    val works = buildSeedWorks()
    val existing = database.maududiDao().getAllWorks()
    if (existing.size < works.size) {
      val author = AuthorEntity(
        id = AUTHOR_ID_MAUDUDI,
        nameEn = "Sayyid Abul A'la Maududi",
        nameUr = "سید ابوالاعلیٰ مودودی",
        birthYear = 1903,
        deathYear = 1979,
        biographySummary = "20th-century Islamic scholar, exegete of Tafheem-ul-Quran (1942–1972), and author on Islamic theology, history, and constitutional thought.",
        officialPortalUrl = "https://www.readmaududi.com"
      )
      val workEntities = works.map { w ->
        WorkEntity(
          id = w.workId,
          authorId = w.authorId,
          titleEn = w.titleEn,
          titleUr = w.titleUr,
          type = w.workType.name,
          categoryId = w.category.id,
          summary = w.bibliographicSummary,
          primarySourceUrl = w.primarySourceUrl
        )
      }
      val editionEntities = works.map { w ->
        EditionEntity(
          id = "ed_${w.workId}",
          workId = w.workId,
          languageTag = w.languageTag,
          translator = w.translator,
          publisher = w.publisher,
          year = w.yearPublished,
          licenceId = w.licenceState.name,
          completeness = w.completeness.name
        )
      }
      val chapterEntities = works.mapIndexed { idx, w ->
        ChapterEntity(
          id = "ch_${w.workId}_1",
          workId = w.workId,
          ordinal = idx + 1,
          title = w.titleEn,
          sourceUrl = w.primarySourceUrl
        )
      }
      val verseRefs = listOf(
        VerseRefEntity(id = 1, surah = 1, ayah = 1),
        VerseRefEntity(id = 2, surah = 7, ayah = 1),
        VerseRefEntity(id = 3, surah = 18, ayah = 1),
        VerseRefEntity(id = 4, surah = 31, ayah = 1)
      )
      val commentaryEntries = works.mapIndexed { index, w ->
        CommentaryEntryEntity(
          rowid = index + 1,
          editionId = "ed_${w.workId}",
          verseRefId = ((index % 4) + 1).toLong(),
          page = null,
          title = w.titleEn,
          bibliographicNote = w.bibliographicSummary,
          text = if (w.licenceState == MaududiLicenceState.LINK_ONLY) null else w.bodyText
        )
      }
      val sourceLinks = works.map { w ->
        SourceLinkEntity(
          ownerType = "WORK",
          ownerId = w.workId,
          url = w.primarySourceUrl,
          tier = w.sourceTier.name,
          countryStatus = "PLAYABLE_GLOBAL",
          checkedOn = "2026-10-31T01:15:00Z"
        )
      }

      database.maududiDao().upsertAuthors(listOf(author))
      database.maududiDao().upsertWorks(workEntities)
      database.maududiDao().upsertEditions(editionEntities)
      database.maududiDao().upsertChapters(chapterEntities)
      database.maududiDao().upsertVerseRefs(verseRefs)
      database.maududiDao().upsertCommentaryEntries(commentaryEntries)
      database.lectureDao().upsertSourceLinks(sourceLinks)
      database.maududiDao().rebuildCommentaryFtsIndex()
    }
    works
  }

  fun getAllWorksMemory(): List<MaududiWorkRecord> = buildSeedWorks()
}
