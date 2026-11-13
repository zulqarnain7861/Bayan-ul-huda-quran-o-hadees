package com.example.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import com.example.data.remote.SecurityIntegrityGuard
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class Reciter(
  val id: String,
  val name: String,
  val subfolder: String,
  val arabicName: String = "",
  val tradition: String = "SUNNI", // "SUNNI" or "SHIA"
  val style: String = "Murattal",
  val verificationStatus: String = "Verified 114-Surah Quran Recitation",
  val isSurahLevelReciter: Boolean = false,
  val moshafId: Int = Mp3QuranTimingClient.resolveReadIdForReciter(id) ?: 1
)

data class ResolvedPlaybackSource(
  val reciterId: String,
  val moshafId: Int,
  val surahNumber: Int,
  val ayahNumber: Int,
  val candidateUrls: List<String>,
  val cuePoints: List<Mp3QuranTimingClient.Mp3QuranAyatTimingEntry>,
  val isPerAyahSource: Boolean,
  val isAvailable: Boolean
)

data class ReciterAvailabilityNote(
  val name: String,
  val arabicName: String,
  val statusBadge: String,
  val explanation: String
)

data class AudioState(
  val isVisible: Boolean = false,
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
  val isAudioUnavailable: Boolean = false,
  val surahNumber: Int? = null,
  val ayahNumber: Int? = null,
  val surahName: String = "",
  val currentPositionMs: Int = 0,
  val durationMs: Int = 0,
  val statusMessage: String = "",
  val reciter: Reciter = availableReciters.first(),
  val playbackSpeed: Float = TajweedAudioSyncEngine.DEFAULT_PLAYBACK_SPEED,
  val isRecitingBasmalaPrefix: Boolean = false,
  val hasBasmalaInCurrentSurahStream: Boolean = true,
  val basmalaOffsetMs: Long = 0L,
  val timingStatus: TimingCalibrationStatus = TimingCalibrationStatus.DATASET,
  val calibrationSource: BasmalaCalibrationSource = BasmalaCalibrationSource.VERIFIED_DATASET,
  val isAudioAssistedRefinementEnabled: Boolean = true,
  val audioSource: String = "online_stream"
)

val availableReciters = listOf(
  // ── Verified Sunni Quran Reciters (EveryAyah Verse-by-Verse + MP3Quran Timing API Sync) ──
  Reciter(
    id = "husary",
    name = "Mahmoud Khalil Al-Husary",
    subfolder = "Husary_128kbps",
    arabicName = "محمود خليل الحصري",
    tradition = "SUNNI",
    style = "Murattal (Tajweed Standard)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #118)"
  ),
  Reciter(
    id = "husary_mujawwad",
    name = "Mahmoud Khalil Al-Husary (Mujawwad)",
    subfolder = "Husary_128kbps_Mujawwad",
    arabicName = "محمود خليل الحصري (مجود)",
    tradition = "SUNNI",
    style = "Mujawwad (Melodic Tajweed 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #119)"
  ),
  Reciter(
    id = "husary_muallim",
    name = "Mahmoud Khalil Al-Husary (Al-Muallim)",
    subfolder = "Husary_Muallim_128kbps",
    arabicName = "محمود خليل الحصري (المعلم)",
    tradition = "SUNNI",
    style = "Muallim (Teaching Cadence 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #118)"
  ),
  Reciter(
    id = "abdulbasit",
    name = "Abdul Basit Abdul Samad",
    subfolder = "Abdul_Basit_Murattal_192kbps",
    arabicName = "عبد الباسط عبد الصمد",
    tradition = "SUNNI",
    style = "Murattal (Classical 192 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #53)"
  ),
  Reciter(
    id = "abdulbasit_mujawwad",
    name = "Abdul Basit Abdul Samad (Mujawwad)",
    subfolder = "Abdul_Basit_Mujawwad_128kbps",
    arabicName = "عبد الباسط عبد الصمد (مجود)",
    tradition = "SUNNI",
    style = "Mujawwad (Golden Era 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #51)"
  ),
  Reciter(
    id = "minshawi",
    name = "Muhammad Siddiq Al-Minshawi",
    subfolder = "Minshawy_Murattal_128kbps",
    arabicName = "محمد صديق المنشاوي",
    tradition = "SUNNI",
    style = "Murattal (Classical 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #112)"
  ),
  Reciter(
    id = "minshawi_mujawwad",
    name = "Muhammad Siddiq Al-Minshawi (Mujawwad)",
    subfolder = "Minshawy_Mujawwad_192kbps",
    arabicName = "محمد صديق المنشاوي (مجود)",
    tradition = "SUNNI",
    style = "Mujawwad (Soulful 192 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #113)"
  ),
  Reciter(
    id = "sudais",
    name = "Abdul Rahman Al-Sudais",
    subfolder = "Abdurrahmaan_As-Sudais_192kbps",
    arabicName = "عبد الرحمن السديس",
    tradition = "SUNNI",
    style = "Murattal (Imam of Masjid al-Haram)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #54)"
  ),
  Reciter(
    id = "shuraim",
    name = "Saud Al-Shuraim",
    subfolder = "Saood_ash-Shuraym_128kbps",
    arabicName = "سعود الشريم",
    tradition = "SUNNI",
    style = "Murattal (Masjid al-Haram 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #31)"
  ),
  Reciter(
    id = "maher_muaiqly",
    name = "Maher Al-Muaiqly",
    subfolder = "MaherAlMuaiqly128kbps",
    arabicName = "ماهر المعيقلي",
    tradition = "SUNNI",
    style = "Murattal (Masjid al-Haram 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #102)"
  ),
  Reciter(
    id = "shatri",
    name = "Abu Bakr Al-Shatri",
    subfolder = "Abu_Bakr_Ash-Shaatree_128kbps",
    arabicName = "أبو بكر الشاطري",
    tradition = "SUNNI",
    style = "Murattal (128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #4)"
  ),
  Reciter(
    id = "ghamdi",
    name = "Saad Al-Ghamdi",
    subfolder = "Ghamadi_40kbps",
    arabicName = "سعد الغامدي",
    tradition = "SUNNI",
    style = "Murattal (Classic Haramain Style)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #30)"
  ),
  Reciter(
    id = "hudhaify",
    name = "Ali Al-Hudhaify",
    subfolder = "Hudhaify_128kbps",
    arabicName = "علي الحذيفي",
    tradition = "SUNNI",
    style = "Murattal (Imam of Masjid an-Nabawi)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #74)"
  ),
  Reciter(
    id = "dossari",
    name = "Yasser Al-Dossari",
    subfolder = "Yasser_Ad-Dussary_128kbps",
    arabicName = "ياسر الدوسري",
    tradition = "SUNNI",
    style = "Murattal (Masjid al-Haram 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #128)"
  ),
  Reciter(
    id = "qatami",
    name = "Nasser Al-Qatami",
    subfolder = "Nasser_Alqatami_128kbps",
    arabicName = "ناصر القطامي",
    tradition = "SUNNI",
    style = "Murattal (Melodious 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #122)"
  ),
  Reciter(
    id = "ayyub",
    name = "Muhammad Ayyub",
    subfolder = "Muhammad_Ayyoub_128kbps",
    arabicName = "محمد أيوب",
    tradition = "SUNNI",
    style = "Murattal (Masjid an-Nabawi 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #109)"
  ),
  Reciter(
    id = "jibreel",
    name = "Muhammad Jibreel",
    subfolder = "Muhammad_Jibreel_128kbps",
    arabicName = "محمد جبريل",
    tradition = "SUNNI",
    style = "Murattal (Egyptian Qira'at 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #111)"
  ),
  Reciter(
    id = "basfar",
    name = "Abdullah Basfar",
    subfolder = "Abdullah_Basfar_192kbps",
    arabicName = "عبد الله بصفر",
    tradition = "SUNNI",
    style = "Murattal (192 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #60)"
  ),
  Reciter(
    id = "rifai",
    name = "Hani Ar-Rifai",
    subfolder = "Hani_Rifai_192kbps",
    arabicName = "هاني الرفاعي",
    tradition = "SUNNI",
    style = "Murattal (Emotional 192 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #125)"
  ),
  Reciter(
    id = "ajamy",
    name = "Ahmed ibn Ali Al-Ajamy",
    subfolder = "ahmed_ibn_ali_al_ajamy_128kbps",
    arabicName = "أحمد بن علي العجمي",
    tradition = "SUNNI",
    style = "Murattal (128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #5)"
  ),
  Reciter(
    id = "akhdar",
    name = "Ibrahim Al-Akhdar",
    subfolder = "Ibrahim_Akhdar_32kbps",
    arabicName = "إبراهيم الأخضر",
    tradition = "SUNNI",
    style = "Murattal (Masjid an-Nabawi)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #1)"
  ),
  Reciter(
    id = "bukhatir",
    name = "Salah Bukhatir",
    subfolder = "Salaah_AbdulRahman_Bukhatir_128kbps",
    arabicName = "صلاح بو خاطر",
    tradition = "SUNNI",
    style = "Murattal (128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #46)"
  ),
  Reciter(
    id = "budair",
    name = "Salah Al-Budair",
    subfolder = "Salah_Al_Budair_128kbps",
    arabicName = "صلاح البدير",
    tradition = "SUNNI",
    style = "Murattal (Imam of Masjid an-Nabawi)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #47)"
  ),
  Reciter(
    id = "matroud",
    name = "Abdullah Al-Matroud",
    subfolder = "Abdullah_Matroud_128kbps",
    arabicName = "عبد الله المطرود",
    tradition = "SUNNI",
    style = "Murattal (128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #65)"
  ),
  Reciter(
    id = "juhany",
    name = "Abdullah Awad Al-Juhany",
    subfolder = "Abdullaah_3awwaad_Al-Juhaynee_128kbps",
    arabicName = "عبد الله عواد الجهني",
    tradition = "SUNNI",
    style = "Murattal (Masjid al-Haram 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #62)"
  ),
  Reciter(
    id = "ali_jaber",
    name = "Ali Jaber",
    subfolder = "Ali_Jaber_64kbps",
    arabicName = "علي جابر",
    tradition = "SUNNI",
    style = "Murattal (Former Imam of Masjid al-Haram)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #76)"
  ),
  Reciter(
    id = "fares_abbad",
    name = "Fares Abbad",
    subfolder = "Fares_Abbad_64kbps",
    arabicName = "فارس عباد",
    tradition = "SUNNI",
    style = "Murattal (Clear Articulation)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #81)"
  ),
  Reciter(
    id = "tunaiji",
    name = "Khalifa Al-Tunaiji",
    subfolder = "khalefa_al_tunaiji_64kbps",
    arabicName = "خليفة الطنيجي",
    tradition = "SUNNI",
    style = "Murattal (Tajweed Precision)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #26)"
  ),
  Reciter(
    id = "banna",
    name = "Mahmoud Ali Al-Banna",
    subfolder = "mahmoud_ali_al_banna_32kbps",
    arabicName = "محمود علي البنا",
    tradition = "SUNNI",
    style = "Murattal (Classical Egyptian)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #117)"
  ),
  Reciter(
    id = "suwayd",
    name = "Dr. Ayman Suwayd",
    subfolder = "Ayman_Sowaid_64kbps",
    arabicName = "أيمن سويد",
    tradition = "SUNNI",
    style = "Muallim (Master of Tajweed Articulation)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #12)"
  ),
  Reciter(
    id = "muhsin_qasim",
    name = "Abdul Muhsin Al-Qasim",
    subfolder = "Muhsin_Al_Qasim_192kbps",
    arabicName = "عبد المحسن القاسم",
    tradition = "SUNNI",
    style = "Murattal (Imam of Masjid an-Nabawi)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #58)"
  ),
  Reciter(
    id = "qahtani",
    name = "Khaled Al-Qahtani",
    subfolder = "Khaled_Al-Qahtani_192kbps",
    arabicName = "خالد القحطاني",
    tradition = "SUNNI",
    style = "Murattal (192 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #24)"
  ),
  Reciter(
    id = "sahl_yassin",
    name = "Sahl Yassin",
    subfolder = "Sahl_Yassin_128kbps",
    arabicName = "سهل ياسين",
    tradition = "SUNNI",
    style = "Murattal (128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #44)"
  ),
  Reciter(
    id = "tablawi",
    name = "Mohammad Al-Tablawi",
    subfolder = "Mohammad_al_Tablaway_128kbps",
    arabicName = "محمد محمود الطبلاوي",
    tradition = "SUNNI",
    style = "Murattal (Classical Egyptian 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #106)"
  ),
  Reciter(
    id = "warsh_yassin",
    name = "Yassin Al-Jazaery (Warsh)",
    subfolder = "warsh/warsh_yassin_al_jazaery_64kbps",
    arabicName = "ياسين الجزائري (ورش عن نافع)",
    tradition = "SUNNI",
    style = "Murattal (Riwayat Warsh 'an Nafi')",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah & MP3Quran Read #131)"
  ),

  // ── Verified MP3Quran.net v3 & Timing API Reciters (Surah Stream + Live Verse Sync) ──
  Reciter(
    id = "islam_sobhi",
    name = "Islam Sobhi",
    subfolder = "islam/Rewayat-Hafs-A-n-Assem",
    arabicName = "إسلام صبحي",
    tradition = "SUNNI",
    style = "Murattal (Melodious & Soulful • MP3Quran Read #253)",
    verificationStatus = "Verified MP3Quran v3 API & Timing Sync (server14.mp3quran.net + Archive)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "abdul_rehman_mossad",
    name = "Abdul Rehman Mossad",
    subfolder = "abdulrahman-mosad",
    arabicName = "عبد الرحمن مسعد",
    tradition = "SUNNI",
    style = "Murattal (Tranquil & Heart-Touching • Verified Partial Mushaf #2540)",
    verificationStatus = "Verified Archive Recitations with Acoustic Verse Timing Sync",
    isSurahLevelReciter = true,
    moshafId = 2540
  ),
  Reciter(
    id = "bandar_baleelah",
    name = "Bandar Baleelah",
    subfolder = "bandar_baleela",
    arabicName = "بندر بليلة",
    tradition = "SUNNI",
    style = "Murattal (Imam of Masjid al-Haram • MP3Quran Read #15)",
    verificationStatus = "Verified Complete 114 Surahs (MP3Quran v3 server6 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "idrees_abkar",
    name = "Idrees Abkar",
    subfolder = "idrees_abkar",
    arabicName = "إدريس أبكر",
    tradition = "SUNNI",
    style = "Murattal (Deep Emotional Cadence • MP3Quran Read #10)",
    verificationStatus = "Verified Complete 114 Surahs (MP3Quran v3 server6 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "hazza_albalushi",
    name = "Hazza Al-Balushi",
    subfolder = "hazza_al_balushi",
    arabicName = "هزاع البلوشي",
    tradition = "SUNNI",
    style = "Murattal (Tranquil Recitation • MP3Quran Read #231)",
    verificationStatus = "Verified MP3Quran v3 Official Stream (server11 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "raad_alkurdi",
    name = "Raad Mohammad Al-Kurdi",
    subfolder = "raad_al_kurdi",
    arabicName = "رعد محمد الكردي",
    tradition = "SUNNI",
    style = "Murattal (Powerful & Clear • MP3Quran Read #221)",
    verificationStatus = "Verified MP3Quran v3 Official Stream (server6 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "wadih_alyamani",
    name = "Wadih Al-Yamani",
    subfolder = "wadee_hammadi_al-yamani",
    arabicName = "وديع اليمني",
    tradition = "SUNNI",
    style = "Murattal (Complete 114 Surahs • MP3Quran Read #218)",
    verificationStatus = "Verified Complete 114 Surahs (MP3Quran v3 server6 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "muhammad_luhaidan",
    name = "Muhammad Al-Luhaidan",
    subfolder = "muhammad_al_luhaidan",
    arabicName = "محمد اللحيدان",
    tradition = "SUNNI",
    style = "Murattal (Reverent & Moving • MP3Quran Read #107)",
    verificationStatus = "Verified Complete 114 Surahs (MP3Quran v3 server8 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "mansour_alsalimi",
    name = "Mansour Al-Salimi",
    subfolder = "mansor",
    arabicName = "منصور السالمي",
    tradition = "SUNNI",
    style = "Murattal (Heart-Softening • MP3Quran Read #245)",
    verificationStatus = "Verified MP3Quran v3 Official Stream (server14 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "noreen_siddiq",
    name = "Noreen Mohammad Siddiq",
    subfolder = "nourin_siddig/Rewayat-Aldori-A-n-Abi-Amr",
    arabicName = "نورين محمد صديق",
    tradition = "SUNNI",
    style = "Murattal (Riwayat Ad-Duri 'an Abi 'Amr • MP3Quran Read #229)",
    verificationStatus = "Verified Complete 114 Surahs (MP3Quran v3 server16 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "sherzad_taher",
    name = "Sherzad Abdulrahman Taher",
    subfolder = "taher",
    arabicName = "شيرزاد عبد الرحمن طاهر",
    tradition = "SUNNI",
    style = "Murattal (Complete 114 Surahs • MP3Quran Read #49)",
    verificationStatus = "Verified Complete 114 Surahs (MP3Quran v3 server12 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "hatem_farid",
    name = "Hatem Farid Al-Waer",
    subfolder = "hatem_farid",
    arabicName = "حاتم فريد الواعر",
    tradition = "SUNNI",
    style = "Murattal (Complete 114 Surahs • MP3Quran Read #19)",
    verificationStatus = "Verified Complete 114 Surahs (MP3Quran v3 server11 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "peshawari",
    name = "Abdul Basit (MP3Quran Full Mushaf)",
    subfolder = "basit/Almusshaf-Al-Mojawwad",
    arabicName = "عبد الباسط عبد الصمد (المصحف المجود الكامل)",
    tradition = "SUNNI",
    style = "Mujawwad Full-Surah Stream (MP3Quran Read #52)",
    verificationStatus = "Verified Complete 114 Surahs (MP3Quran v3 server7 + Ayat Timing API)",
    isSurahLevelReciter = true
  ),

  // ── Verified Shia Quran Reciters (Quranic Recitation Only — Separate from Duas/Ziyarat) ──
  Reciter(
    id = "karim_mansouri",
    name = "Karim Mansouri",
    subfolder = "Karim_Mansoori_40kbps",
    arabicName = "كريم منصوري",
    tradition = "SHIA",
    style = "Tartil / Murattal (Verified Shia Quran Reciter)",
    verificationStatus = "Verified Quran Recitation Stream (EveryAyah Archive)"
  ),
  Reciter(
    id = "maytham_tammar",
    name = "Maytham al-Tammar",
    subfolder = "Maytham_Al_Tammar_128kbps",
    arabicName = "ميثم التمار",
    tradition = "SHIA",
    style = "Murattal (Verified Shia Quran Reciter)",
    verificationStatus = "Verified Shia Quran Reciter (Available Surahs Only)"
  ),
  Reciter(
    id = "shahriar_parhizgar",
    name = "Shahriar Parhizgar",
    subfolder = "Parhizgar_48kbps",
    arabicName = "شهريار پرهيزگار",
    tradition = "SHIA",
    style = "Tartil / Murattal (Complete 114 Surahs)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah Archive)"
  )
)

val reciterAvailabilityNotes = listOf(
  ReciterAvailabilityNote(
    name = "Islam Sobhi",
    arabicName = "إسلام صبحي",
    statusBadge = "MP3Quran v3 API Verified",
    explanation = "Streams verified Surah recordings from server14.mp3quran.net (/islam/Rewayat-Hafs-A-n-Assem/) and MP3Quran v3 API. Unrecorded Surahs report 'Audio not available' without silent fallback or AI voice."
  ),
  ReciterAvailabilityNote(
    name = "Abdul Rehman Mossad",
    arabicName = "عبد الرحمن مسعد",
    statusBadge = "Verified Partial Mushaf",
    explanation = "Streams verified Surah recordings from Archive.org (abdulrahman-mosad) & MP3Quran API. Because his public recordings cover selected Surahs rather than all 114 Surahs, unrecorded Surahs honestly display 'Audio not available'."
  ),
  ReciterAvailabilityNote(
    name = "Abdullah Shehab",
    arabicName = "عبد الله شهاب",
    statusBadge = "Unverified Open Stream",
    explanation = "Not indexed in official Quran.com, QuranicAudio, or EveryAyah public repositories; documented honestly rather than fabricating audio URLs."
  )
)

class AudioPlayerManager(private val context: Context) {
  private var mediaPlayer: MediaPlayer? = null
  private var nextMediaPlayer: MediaPlayer? = null
  private var nextPreparedSurah: Int = -1
  private var nextPreparedAyah: Int = -1
  private var nextPreparedReciterId: String = ""
  private var nextPlayerPrepJob: Job? = null
  private var offlineAudioTrack: AudioTrack? = null
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var progressJob: Job? = null
  private var fallbackJob: Job? = null
  private var autoDismissJob: Job? = null

  private val initialSavedReciter: Reciter = runCatching {
    val savedId = com.example.data.prayer.PrayerPreferencesManager.loadConfig(context).sunniQuranReciterId
    availableReciters.find { it.id == savedId } ?: availableReciters.first()
  }.getOrDefault(availableReciters.first())

  private val initialPlaybackSpeed: Float = TajweedAudioSyncEngine.loadGlobalPlaybackSpeed(context)
  private val initialRefinementEnabled: Boolean = TajweedAudioSyncEngine.isAudioAssistedRefinementEnabled(context)

  private val _audioState = MutableStateFlow(
    AudioState(
      reciter = initialSavedReciter,
      playbackSpeed = initialPlaybackSpeed,
      isAudioAssistedRefinementEnabled = initialRefinementEnabled,
      hasBasmalaInCurrentSurahStream = initialSavedReciter.isSurahLevelReciter
    )
  )
  val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

  private var preloadJob: Job? = null
  private var preloadTargetKey: String = ""
  var autoAdvanceFullSurah: Boolean = true
  @Volatile
  var isSingleVersePlaybackMode: Boolean = false
    private set

  private data class Mp3QuranMoshafInfo(
    val serverBaseUrl: String,
    val availableSurahs: Set<Int>
  )

  private val resolvedMp3QuranServers = mutableMapOf<String, Mp3QuranMoshafInfo>()
  private val resolvedArchiveSurahFiles = mutableMapOf<String, Map<Int, String>>()
  private val surahAyahWeights = mutableMapOf<Int, List<Int>>()
  @Volatile
  private var activeCuePoints: List<Mp3QuranTimingClient.Mp3QuranAyatTimingEntry> = emptyList()
  @Volatile
  var lastConfirmedAudioUrl: String = ""
    private set

  init {
    // Purge any legacy v1 cached files for surah-level reciters that may have stored playlist-offset tracks
    scope.launch(Dispatchers.IO) {
      runCatching {
        File(context.filesDir, "audio_cache/islam_sobhi").deleteRecursively()
        File(context.filesDir, "audio_cache/abdul_rehman_mossad").deleteRecursively()
      }
    }
  }

  companion object {
    const val KEY_PREFERRED_RECITER_ID = "preferred_reciter_id"
    private const val PREFS_NAME = "bayan_prayer_and_alarm_prefs"

    /**
     * Reads the user's stored preferred reciter ID from SharedPreferences (`preferred_reciter_id`).
     * Edge-case protection: If the stored reciter ID no longer exists in `catalog`, clears the stored
     * value and returns `null` so the picker opens with no preselection.
     */
    fun getPreferredReciterId(
      context: Context,
      catalog: List<Reciter> = availableReciters
    ): String? {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val storedId = prefs.getString(KEY_PREFERRED_RECITER_ID, null)?.trim()?.takeIf { it.isNotEmpty() }
        ?: return null
      val existsInCatalog = catalog.any { it.id == storedId }
      if (!existsInCatalog) {
        prefs.edit().remove(KEY_PREFERRED_RECITER_ID).apply()
        return null
      }
      return storedId
    }

    /**
     * Saves the user's chosen reciter ID (`preferred_reciter_id`) ONLY when the user confirms Play in the dialog.
     */
    fun savePreferredReciterId(context: Context, reciterId: String) {
      val cleanId = reciterId.trim()
      if (cleanId.isEmpty()) return
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit().putString(KEY_PREFERRED_RECITER_ID, cleanId).apply()
    }

    fun clearPreferredReciterId(context: Context) {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit().remove(KEY_PREFERRED_RECITER_ID).apply()
    }
    private val SURAH_VERSE_COUNTS = intArrayOf(
      7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
      123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
      112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
      34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
      54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
      60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
      14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
      28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
      29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
      15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
      11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
      5, 4, 5, 6
    )

    // Canonical 1-based Arabic & English Surah names for strict Archive.org track-to-Surah verification
    // (Prevents album playlist track numbers like "06 Al-Ma'idah" or "36 Al-Ghashiyah" from being misread as Surah 6 or Surah 36!)
    private val CANONICAL_SURAH_ARABIC_NAMES = arrayOf(
      "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
      "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
      "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
      "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
      "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق",
      "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة",
      "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج",
      "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
      "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد",
      "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات",
      "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر",
      "المسد", "الإخلاص", "الفلق", "الناس"
    )

    private val CANONICAL_SURAH_LATIN_KEYWORDS = arrayOf(
      "fatiha", "baqara", "imran", "nisa", "maidah", "anam", "araf", "anfal", "tawba", "yunus",
      "hud", "yusuf", "rad", "ibrahim", "hijr", "nahl", "isra", "kahf", "maryam", "ta ha",
      "anbiya", "hajj", "muminun", "nur", "furqan", "shuara", "naml", "qasas", "ankabut", "rum",
      "luqman", "sajda", "ahzab", "saba", "fatir", "yasin", "saffat", "sad", "zumar", "ghafir",
      "fussilat", "shura", "zukhruf", "dukhan", "jathiya", "ahqaf", "muhammad", "fath", "hujurat", "qaf",
      "dhariyat", "tur", "najm", "qamar", "rahman", "waqia", "hadid", "mujadila", "hashr", "mumtahina",
      "saff", "jumua", "munafiqun", "taghabun", "talaq", "tahrim", "mulk", "qalam", "haqqa", "maarij",
      "nuh", "jinn", "muzzammil", "muddaththir", "qiyama", "insan", "mursalat", "naba", "naziat", "abasa",
      "takwir", "infitar", "mutaffifin", "inshiqaq", "buruj", "tariq", "ala", "ghashiya", "fajr", "balad",
      "shams", "layl", "duha", "sharh", "tin", "alaq", "qadr", "bayyina", "zalzala", "adiyat",
      "qaria", "takathur", "asr", "humaza", "fil", "quraysh", "maun", "kawthar", "kafirun", "nasr",
      "masad", "ikhlas", "falaq", "nas"
    )

    // Canonical surahs recorded by Islam Sobhi in MP3Quran v3 Moshaf #253 (`server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem/`)
    val ISLAM_SOBHI_AVAILABLE_SURAHS = setOf(
      1, 2, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26, 28, 29, 30, 31, 32, 34, 36,
      39, 40, 41, 42, 44, 50, 51, 52, 53, 54, 55, 56, 57, 62, 63, 64, 66, 67, 68, 70, 71, 72,
      73, 74, 75, 76, 77, 78, 79, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 97,
      99, 100, 102, 103, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114
    )

    // Surahs available with canonical 3-digit filenames on https://archive.org/download/AbdulrahmanMassaad/{002..114}.mp3
    // (Excludes partial-ayah excerpts so only full-surah recordings are selected)
    val ABDUL_RAHMAN_MASSAAD_CLEAN_SURAHS = setOf(
      15, 25, 29, 32, 49, 67, 73, 78, 87, 88, 100, 107, 112, 113, 114
    )

    // Verified canonical Surah-number -> exact FULL-Surah Archive.org filename mapping for `abdulrahman-mosad`
    // (Strictly excludes partial-ayah clips such as `197-202`, `1-14`, `67-76`, `40`, etc.)
    val ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES = mapOf(
      1 to "001%20Al-Fatiha%20%D8%A7%D9%84%D9%81%D8%A7%D8%AA%D8%AD%D8%A9.mp3",
      29 to "029%20Al-Ankabut%20%D8%A7%D9%84%D8%B9%D9%86%D9%83%D8%A8%D9%88%D8%AA.mp3",
      32 to "032%20As-Sajdah%20%D8%A7%D9%84%D8%B3%D8%AC%D8%AF%D8%A9.mp3",
      49 to "049%20Al-Hujurat%20%D8%A7%D9%84%D8%AD%D8%AC%D8%B1%D8%A7%D8%AA.mp3",
      73 to "073%20Al-Muzzammil%20%D8%A7%D9%84%D9%85%D8%B2%D9%85%D9%84.mp3",
      78 to "078%20An-Naba%20%D8%A7%D9%84%D9%86%D8%A8%D8%A3.mp3",
      87 to "087%20Al-Ala%20%D8%A7%D9%84%D8%A3%D8%B9%D9%84%D9%89.mp3",
      88 to "088%20Al-Ghashiyah%20%D8%A7%D9%84%D8%BA%D8%A7%D8%B4%D9%8A%D8%A9.mp3",
      100 to "100%20Al-Adiyat%20%D8%A7%D9%84%D8%B9%D8%A7%D8%AF%D9%8A%D8%A7%D8%AA.mp3",
      107 to "107%20Al-Ma%27un%20%D8%A7%D9%84%D9%85%D8%A7%D8%B9%D9%88%D9%86.mp3"
    )

    /**
     * Checks whether `reciter` has a verified recording for `surahNumber` (1..114).
     * Used by the UI (`SelectReciterDialog` and `QuranRecitersBottomSheet`) to disable
     * unavailable reciter/surah combinations with a neutral note instead of substituting another reciter.
     */
    fun isSurahAvailableForReciter(reciter: Reciter, surahNumber: Int): Boolean {
      if (surahNumber !in 1..114) return false
      if (!reciter.isSurahLevelReciter) return true
      return when (reciter.id) {
        "islam_sobhi" -> surahNumber in ISLAM_SOBHI_AVAILABLE_SURAHS
        "abdul_rehman_mossad" ->
          surahNumber in ABDUL_RAHMAN_MASSAAD_CLEAN_SURAHS ||
            ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES.containsKey(surahNumber)
        else -> {
          val dynamicSet = Mp3QuranTimingClient.getDynamicReciterAvailableSurahs(reciter.id)
          dynamicSet?.contains(surahNumber) ?: true
        }
      }
    }

    fun getSurahTotalAyahs(surahNumber: Int): Int {
      return if (surahNumber in 1..114) SURAH_VERSE_COUNTS[surahNumber - 1] else 7
    }

    /**
     * Strictly maps an Archive.org MP3 filename to its canonical Quran Surah number (1..114).
     * - If the filename is purely a 3-digit surah number (e.g. "001.mp3", "005.mp3", "036.mp3"), maps directly to 1..114.
     * - If the filename contains a Surah name (Arabic or Latin), maps ONLY to the Surah whose canonical name matches,
     *   ignoring leading playlist track indices (such as "06 Al-Ma'idah.mp3" -> 5, "36 Al-Ghashiyah.mp3" -> 88).
     */
    fun resolveCanonicalSurahNumberFromFileName(rawFileName: String): Int? {
      val cleanName = rawFileName.substringAfterLast('/').trim()
      if (!cleanName.endsWith(".mp3", ignoreCase = true)) return null
      val baseName = cleanName.dropLast(4).trim()

      // Case 1: Pure 3-digit canonical filename e.g. "001.mp3", "005.mp3", "036.mp3"
      if (Regex("""^\d{3}$""").matches(baseName)) {
        val num = baseName.toIntOrNull()
        return if (num != null && num in 1..114) num else null
      }

      // Case 2: Match against canonical Arabic Surah names (longest first so "آل عمران" matches before "ق" or "ص")
      val normalizedArabic = baseName
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
      val arabicIdxByLength = CANONICAL_SURAH_ARABIC_NAMES.indices.sortedByDescending {
        CANONICAL_SURAH_ARABIC_NAMES[it].length
      }
      for (idx in arabicIdxByLength) {
        val arName = CANONICAL_SURAH_ARABIC_NAMES[idx]
          .replace('أ', 'ا')
          .replace('إ', 'ا')
          .replace('آ', 'ا')
        if (arName.length >= 2 && normalizedArabic.contains(arName)) {
          return idx + 1
        }
      }

      // Case 3: Match against canonical Latin Surah keywords
      val normalizedLatin = baseName.lowercase()
        .replace("'", "")
        .replace("’", "")
        .replace("`", "")
        .replace("-", " ")
        .replace("_", " ")
      val latinIdxByLength = CANONICAL_SURAH_LATIN_KEYWORDS.indices.sortedByDescending {
        CANONICAL_SURAH_LATIN_KEYWORDS[it].length
      }
      for (idx in latinIdxByLength) {
        val keyword = CANONICAL_SURAH_LATIN_KEYWORDS[idx]
        if (keyword.length >= 3 && normalizedLatin.contains(keyword)) {
          return idx + 1
        }
      }
      return null
    }
  }

  /**
   * Registers the Arabic character lengths of the verses in a Surah so surah-level reciters
   * (`islam_sobhi`, `abdul_rehman_mossad`) derive accurate per-ayah & word-by-word green highlight timing.
   */
  fun setSurahAyahWeights(surahNumber: Int, weights: List<Int>) {
    if (surahNumber in 1..114 && weights.isNotEmpty()) {
      surahAyahWeights[surahNumber] = weights.map { it.coerceAtLeast(10) }
    }
  }

  private fun getWeightsForSurah(surahNumber: Int): List<Int> {
    val totalAyahs = getSurahTotalAyahs(surahNumber)
    val existing = surahAyahWeights[surahNumber]
    if (existing != null && existing.size == totalAyahs) return existing
    return List(totalAyahs) { 60 }
  }

  private fun resolveSurahBasmalaOffsetMs(surah: Int, totalSurahDurationMs: Int): Int {
    if (surah == 1 || surah == 9) return 0
    val reciter = _audioState.value.reciter
    if (!reciter.isSurahLevelReciter) return 0
    val cal = TajweedAudioSyncEngine.resolveBasmalaCalibration(
      context = context,
      reciter = reciter,
      surahNumber = surah,
      audioSource = "surah_stream",
      fileDurationMs = totalSurahDurationMs.toLong(),
      fileChecksum = ""
    )
    return cal.basmalaOffsetMs.toInt().coerceIn(0, (totalSurahDurationMs / 3).coerceAtLeast(0))
  }

  /**
   * Derives `(activeAyahNumber, withinAyahPositionMs, ayahWindowDurationMs)` from a surah-level audio position.
   * Uses validated cue points (`Mp3QuranTimingClient.normalizeAndValidateCuePoints`) without linear duration
   * scaling or millisecond fudge offsets.
   */
  fun computeSurahAyahTimingForPosition(
    surah: Int,
    positionMs: Int,
    totalSurahDurationMs: Int
  ): Triple<Int, Int, Int> {
    val activeReciterId = _audioState.value.reciter.id
    val rawTimings = activeCuePoints.takeIf { it.isNotEmpty() }
      ?: Mp3QuranTimingClient.getCachedAyatTimingsForReciter(activeReciterId, surah)
    val validatedTimings = if (!rawTimings.isNullOrEmpty()) {
      Mp3QuranTimingClient.normalizeAndValidateCuePoints(rawTimings, totalSurahDurationMs)
    } else {
      null
    }
    if (!validatedTimings.isNullOrEmpty()) {
      val maxEndMs = validatedTimings.last().endTimeMs
      val clampedPosMs = positionMs.coerceIn(0, maxEndMs)
      val matchingEntry = validatedTimings.firstOrNull { clampedPosMs in it.startTimeMs until it.endTimeMs }
        ?: validatedTimings.lastOrNull { clampedPosMs >= it.startTimeMs }
        ?: validatedTimings.first()
      val withinAyahMs = (clampedPosMs - matchingEntry.startTimeMs).coerceAtLeast(0)
      val ayahDurationMs = matchingEntry.durationMs.coerceAtLeast(600)
      return Triple(matchingEntry.ayah, withinAyahMs, ayahDurationMs)
    }
    return Triple(1, positionMs.coerceAtLeast(0), totalSurahDurationMs.coerceAtLeast(1000))
  }

  fun computeStartOffsetMsForAyah(surah: Int, targetAyah: Int, totalSurahDurationMs: Int): Int {
    if (targetAyah <= 1) return 0
    val activeReciterId = _audioState.value.reciter.id
    val rawTimings = activeCuePoints.takeIf { it.isNotEmpty() }
      ?: Mp3QuranTimingClient.getCachedAyatTimingsForReciter(activeReciterId, surah)
    val validatedTimings = if (!rawTimings.isNullOrEmpty()) {
      Mp3QuranTimingClient.normalizeAndValidateCuePoints(rawTimings, totalSurahDurationMs)
    } else {
      null
    }
    val entry = validatedTimings?.firstOrNull { it.ayah == targetAyah }
    return entry?.startTimeMs?.coerceIn(0, (totalSurahDurationMs - 500).coerceAtLeast(0)) ?: 0
  }

  /**
   * Explicitly applies the global user playback speed (`1.0f` default, clamped `0.5f..2.0f`)
   * with `pitch = 1.0f` to the active MediaPlayer so no reciter or surah ever plays at a stale or fast rate.
   */
  private fun applyPlaybackSpeedToPlayer(mp: MediaPlayer?) {
    if (mp == null) return
    val speed = TajweedAudioSyncEngine.normalizePlaybackSpeed(_audioState.value.playbackSpeed)
    if (speed != _audioState.value.playbackSpeed) {
      _audioState.value = _audioState.value.copy(playbackSpeed = speed)
    }
    runCatching {
      val params = mp.playbackParams
      params.speed = speed
      params.pitch = 1.0f
      mp.playbackParams = params
    }
  }

  fun setPlaybackSpeed(requestedSpeed: Float) {
    val validSpeed = TajweedAudioSyncEngine.saveGlobalPlaybackSpeed(context, requestedSpeed)
    _audioState.value = _audioState.value.copy(playbackSpeed = validSpeed)
    val mp = mediaPlayer
    if (mp != null && _audioState.value.isPlaying) {
      applyPlaybackSpeedToPlayer(mp)
    }
  }

  fun setAudioAssistedRefinement(enabled: Boolean) {
    TajweedAudioSyncEngine.setAudioAssistedRefinementEnabled(context, enabled)
    _audioState.value = _audioState.value.copy(isAudioAssistedRefinementEnabled = enabled)
  }

  private fun buildEveryAyahUrl(reciter: Reciter, surah: Int, ayah: Int): String {
    val formattedSurah = surah.toString().padStart(3, '0')
    val formattedAyah = ayah.toString().padStart(3, '0')
    return if (reciter.isSurahLevelReciter) {
      when (reciter.id) {
        "islam_sobhi" -> "https://server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem/$formattedSurah.mp3"
        "abdul_rehman_mossad" -> {
          when {
            surah in ABDUL_RAHMAN_MASSAAD_CLEAN_SURAHS ->
              "https://archive.org/download/AbdulrahmanMassaad/$formattedSurah.mp3"
            ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES.containsKey(surah) ->
              "https://archive.org/download/abdulrahman-mosad/${ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES[surah]}"
            else ->
              "https://archive.org/download/AbdulrahmanMassaad/$formattedSurah.mp3"
          }
        }
        else -> {
          val registeredServers = Mp3QuranTimingClient.getRegisteredServerUrlsForReciter(reciter.id)
          if (registeredServers.isNotEmpty()) {
            "${registeredServers.first().trimEnd('/')}/$formattedSurah.mp3"
          } else {
            "https://download.quranicaudio.com/quran/${reciter.subfolder}/$formattedSurah.mp3"
          }
        }
      }
    } else {
      "https://everyayah.com/data/${reciter.subfolder}/$formattedSurah$formattedAyah.mp3"
    }
  }

  /**
   * Resolves verified, canonical 1-based Surah audio URLs for the given reciter, surah (1..114), and ayah.
   * Guarantees that pressing Play on Fatiha (1), Baqarah (2), Maidah (5), Yasin (36), or any Surah
   * plays ONLY that exact Surah and never a playlist-offset or adjacent Surah.
   */
  fun getCandidateAudioUrls(reciter: Reciter, surah: Int, ayah: Int = 1): List<String> {
    return resolveCandidateUrlsForReciter(reciter, surah, ayah)
  }

  fun resolvePrimaryAudioUrlForTest(reciter: Reciter, surah: Int, ayah: Int = 1): String {
    val url = resolveCandidateUrlsForReciter(reciter, surah, ayah).firstOrNull()
      ?: buildEveryAyahUrl(reciter, surah, ayah)
    confirmUrlMatchesRequestedSurah(reciter, surah, ayah, url)
    return url
  }

  fun confirmUrlMatchesRequestedSurah(reciter: Reciter, surah: Int, ayah: Int, url: String): Boolean {
    val formattedSurah = surah.toString().padStart(3, '0')
    val formattedAyah = ayah.toString().padStart(3, '0')
    val matches = if (!reciter.isSurahLevelReciter) {
      url.endsWith("/$formattedSurah$formattedAyah.mp3")
    } else {
      when {
        url.endsWith("/$formattedSurah.mp3") -> true
        reciter.id == "abdul_rehman_mossad" && ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES[surah] != null &&
          url.endsWith(ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES[surah]!!) -> true
        else -> {
          val decoded = runCatching { java.net.URLDecoder.decode(url.substringAfterLast('/'), "UTF-8") }
            .getOrDefault(url.substringAfterLast('/'))
          resolveCanonicalSurahNumberFromFileName(decoded) == surah
        }
      }
    }
    Log.i(
      "AudioPlayerManager",
      "FINAL_AUDIO_URL reciter=${reciter.id} surah=$surah ($formattedSurah) ayah=$ayah matchesSurah=$matches url=$url"
    )
    if (matches) {
      lastConfirmedAudioUrl = url
    }
    return matches
  }

  /**
   * Single canonical resolver used by every playback path.
   * Takes `reciter`, `moshafId`, `surahNumber`, and `ayahNumber`, selects audio strictly by
   * `surahNumber` inside that reciter's own list of available surahs (never by list position),
   * and returns the audio source URLs plus validated cue points from the same recording.
   */
  fun resolvePlaybackSourceAndCuePoints(
    reciter: Reciter,
    moshafId: Int = reciter.moshafId,
    surahNumber: Int,
    ayahNumber: Int = 1
  ): ResolvedPlaybackSource {
    if (surahNumber !in 1..114 || !isSurahAvailableForReciter(reciter, surahNumber)) {
      return ResolvedPlaybackSource(
        reciterId = reciter.id,
        moshafId = moshafId,
        surahNumber = surahNumber,
        ayahNumber = ayahNumber,
        candidateUrls = emptyList(),
        cuePoints = emptyList(),
        isPerAyahSource = !reciter.isSurahLevelReciter,
        isAvailable = false
      )
    }

    val formattedSurah = surahNumber.toString().padStart(3, '0')
    val formattedAyah = ayahNumber.toString().padStart(3, '0')

    // Per-ayah reciters (EveryAyah): each file is a single ayah (`{surah}{ayah}.mp3`), so the playing file is the current ayah
    if (!reciter.isSurahLevelReciter) {
      val ayahUrl = "https://everyayah.com/data/${reciter.subfolder}/$formattedSurah$formattedAyah.mp3"
      confirmUrlMatchesRequestedSurah(reciter, surahNumber, ayahNumber, ayahUrl)
      return ResolvedPlaybackSource(
        reciterId = reciter.id,
        moshafId = moshafId,
        surahNumber = surahNumber,
        ayahNumber = ayahNumber,
        candidateUrls = listOf(ayahUrl),
        cuePoints = emptyList(),
        isPerAyahSource = true,
        isAvailable = true
      )
    }

    val candidates = mutableListOf<String>()
    when (reciter.id) {
      "islam_sobhi" -> {
        if (surahNumber in ISLAM_SOBHI_AVAILABLE_SURAHS) {
          candidates.add("https://server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem/$formattedSurah.mp3")
          candidates.add("https://cdn.mp3quran.net/audio/islam-sobhi/r1/$formattedSurah.mp3")
          candidates.add("https://media.way2quran.com/mp3/islam-sobhi/$formattedSurah.mp3")
        }
      }
      "abdul_rehman_mossad" -> {
        if (surahNumber in ABDUL_RAHMAN_MASSAAD_CLEAN_SURAHS) {
          candidates.add("https://archive.org/download/AbdulrahmanMassaad/$formattedSurah.mp3")
        }
        ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES[surahNumber]?.let { exactFile ->
          candidates.add("https://archive.org/download/abdulrahman-mosad/$exactFile")
        }
        if (candidates.isNotEmpty()) {
          candidates.add("https://media.way2quran.com/mp3/abdul-rahman-musaad/$formattedSurah.mp3")
        }
      }
      else -> {
        val registeredServers = Mp3QuranTimingClient.getRegisteredServerUrlsForReciter(reciter.id)
        for (srv in registeredServers) {
          candidates.add("${srv.trimEnd('/')}/$formattedSurah.mp3")
        }
        candidates.add("https://download.quranicaudio.com/quran/${reciter.subfolder}/$formattedSurah.mp3")
      }
    }

    // Check MP3Quran v3 API cached/live metadata ONLY when the reciter's own moshaf `surahSet` contains `surahNumber`
    val mp3Info = fetchMp3QuranInfoIfNeeded(reciter.id)
    if (mp3Info != null && mp3Info.availableSurahs.contains(surahNumber)) {
      candidates.add("${mp3Info.serverBaseUrl.trimEnd('/')}/$formattedSurah.mp3")
    }

    val verifiedCandidates = candidates
      .distinct()
      .filter { SecurityIntegrityGuard.isUrlTrustedAndAllowed(it) }
      .filter { confirmUrlMatchesRequestedSurah(reciter, surahNumber, ayahNumber, it) }

    val rawCues = Mp3QuranTimingClient.getCachedAyatTimingsForReciter(reciter.id, surahNumber).orEmpty()
    val validatedCues = Mp3QuranTimingClient.normalizeAndValidateCuePoints(rawCues, realAudioDurationMs = 0).orEmpty()

    return ResolvedPlaybackSource(
      reciterId = reciter.id,
      moshafId = moshafId,
      surahNumber = surahNumber,
      ayahNumber = ayahNumber,
      candidateUrls = verifiedCandidates,
      cuePoints = validatedCues,
      isPerAyahSource = false,
      isAvailable = verifiedCandidates.isNotEmpty()
    )
  }

  private fun resolveCandidateUrlsForReciter(reciter: Reciter, surah: Int, ayah: Int): List<String> {
    return resolvePlaybackSourceAndCuePoints(
      reciter = reciter,
      moshafId = reciter.moshafId,
      surahNumber = surah,
      ayahNumber = ayah
    ).candidateUrls
  }

  private fun fetchMp3QuranInfoIfNeeded(reciterId: String): Mp3QuranMoshafInfo? {
    resolvedMp3QuranServers[reciterId]?.let { return it }
    val directReadId = Mp3QuranTimingClient.resolveReadIdForReciter(reciterId)
    val searchTerms = when (reciterId) {
      "islam_sobhi" -> listOf("islam sobhi", "إسلام صبحي", "اسلام صبحي")
      // Strictly match Abdul Rahman Mossad / Musaad (never "masoud" which is a different reciter)
      "abdul_rehman_mossad" -> listOf("abdulrahman mosad", "abdul rahman mossad", "abdulrahman musaad", "عبدالرحمن مسعد", "عبد الرحمن مسعد")
      else -> emptyList()
    }
    if (searchTerms.isEmpty() && directReadId == null) return null

    for (lang in listOf("eng", "ar")) {
      val reciters = Mp3QuranTimingClient.fetchReciters(language = lang)
      for (entry in reciters) {
        val lowerName = entry.name.lowercase()
        val idMatch = directReadId != null && entry.id == directReadId
        val nameMatch = searchTerms.isNotEmpty() && searchTerms.any { lowerName.contains(it) }
        if (idMatch || nameMatch) {
          val moshaf = entry.moshafList.maxByOrNull { it.surahTotal } ?: entry.moshafList.firstOrNull()
          if (moshaf != null && SecurityIntegrityGuard.isUrlTrustedAndAllowed(moshaf.serverUrl)) {
            val info = Mp3QuranMoshafInfo(moshaf.serverUrl, moshaf.surahSet)
            resolvedMp3QuranServers[reciterId] = info
            return info
          }
        }
      }
    }
    return null
  }

  private fun fetchArchiveSurahFileIfNeeded(archiveIdentifier: String, surah: Int): String? {
    val map = resolvedArchiveSurahFiles.getOrPut(archiveIdentifier) {
      val discovered = mutableMapOf<Int, String>()
      val metaUrl = "https://archive.org/metadata/$archiveIdentifier"
      val body = Mp3QuranTimingClient.executeSafeGetJson(metaUrl)
      if (!body.isNullOrBlank()) {
        try {
          val root = org.json.JSONObject(body)
          val filesArr = root.optJSONArray("files")
          if (filesArr != null) {
            for (i in 0 until filesArr.length()) {
              val fileObj = filesArr.getJSONObject(i)
              val fileName = fileObj.optString("name", "")
              if (fileName.endsWith(".mp3", ignoreCase = true)) {
                val canonicalSurah = resolveCanonicalSurahNumberFromFileName(fileName)
                if (canonicalSurah != null && canonicalSurah in 1..114 && !discovered.containsKey(canonicalSurah)) {
                  discovered[canonicalSurah] = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
                }
              }
            }
          }
        } catch (_: Exception) {
        }
      }
      discovered
    }
    return map[surah]
  }

  private fun verifyAudioUrlReachable(url: String): Boolean {
    if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(url)) return false
    return try {
      var currentUrl = url
      var redirects = 0
      while (redirects < 5) {
        if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(currentUrl) ||
          !com.example.security.SsrfRedirectValidator.validateUrlSafety(currentUrl, resolveDns = false)
        ) return false
        val conn = java.net.URL(currentUrl).openConnection() as java.net.HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.requestMethod = "GET"
        conn.setRequestProperty("Range", "bytes=0-1023")
        conn.connectTimeout = 3500
        conn.readTimeout = 4000
        val code = conn.responseCode
        if (code in 301..308) {
          val location = conn.getHeaderField("Location") ?: return false
          currentUrl = if (location.startsWith("http")) location.replace("http://", "https://") else return false
          conn.disconnect()
          redirects++
          continue
        }
        val contentType = conn.contentType?.lowercase().orEmpty()
        val isValidAudio = (code == 200 || code == 206) &&
          (contentType.contains("audio") || contentType.contains("mpeg") || contentType.contains("octet-stream"))
        conn.disconnect()
        return isValidAudio
      }
      false
    } catch (_: Exception) {
      false
    }
  }

  private fun preloadUpcomingAyahs(reciter: Reciter, surah: Int, currentAyah: Int, count: Int = 8) {
    if (reciter.isSurahLevelReciter) return
    val totalAyahs = getSurahTotalAyahs(surah)
    if (currentAyah >= totalAyahs) return

    val key = "${reciter.id}:$surah"
    if (preloadTargetKey != key) {
      preloadJob?.cancel()
      preloadTargetKey = key
    }
    preloadJob = scope.launch(Dispatchers.IO) {
      val endAyah = (currentAyah + count).coerceAtMost(totalAyahs)
      for (nextAyah in (currentAyah + 1)..endAyah) {
        if (!isActive) break
        val targetCache = getCachedAudioFile(reciter.id, surah, nextAyah)
        if (!targetCache.exists() || targetCache.length() <= 4096L) {
          val nextUrl = buildEveryAyahUrl(reciter, surah, nextAyah)
          if (SecurityIntegrityGuard.isUrlTrustedAndAllowed(nextUrl)) {
            catchAudioForOffline(nextUrl, targetCache)
          }
        }
      }
    }
  }

  private fun releaseNextMediaPlayerOnly() {
    nextPlayerPrepJob?.cancel()
    nextPlayerPrepJob = null
    try {
      mediaPlayer?.setNextMediaPlayer(null)
    } catch (_: Exception) {
    }
    try {
      nextMediaPlayer?.release()
    } catch (_: Exception) {
    }
    nextMediaPlayer = null
    nextPreparedSurah = -1
    nextPreparedAyah = -1
    nextPreparedReciterId = ""
  }

  /**
   * Pre-buffers and prepares the next Ayah's `MediaPlayer` in the background while the current Ayah
   * is playing, attaching it via `setNextMediaPlayer` for zero-gap continuous Surah recitation.
   */
  private fun prepareNextAyahPlayerInBackground(
    reciter: Reciter,
    surah: Int,
    currentAyah: Int,
    surahName: String
  ) {
    if (isSingleVersePlaybackMode || !autoAdvanceFullSurah || reciter.isSurahLevelReciter) return
    val totalAyahs = getSurahTotalAyahs(surah)
    val nextAyah = currentAyah + 1
    if (nextAyah > totalAyahs) return

    releaseNextMediaPlayerOnly()
    nextPlayerPrepJob = scope.launch(Dispatchers.IO) {
      val offlineFile = runCatching {
        com.example.data.download.DownloadManager.getInstance(context)
          .findVerifiedOfflineAudioFile(reciter.id, surah, nextAyah)
      }.getOrNull()

      val cacheFile = getCachedAudioFile(reciter.id, surah, nextAyah)
      if ((offlineFile == null || !offlineFile.exists() || offlineFile.length() <= 64L) &&
        (!cacheFile.exists() || cacheFile.length() <= 4096L)
      ) {
        val nextUrl = buildEveryAyahUrl(reciter, surah, nextAyah)
        if (SecurityIntegrityGuard.isUrlTrustedAndAllowed(nextUrl)) {
          catchAudioForOffline(nextUrl, cacheFile)
        }
      }
      if (!isActive) return@launch

      val readyFile = when {
        offlineFile != null && offlineFile.exists() && offlineFile.length() > 64L -> offlineFile
        cacheFile.exists() && cacheFile.length() > 4096L -> cacheFile
        else -> null
      }
      val nextUrl = buildEveryAyahUrl(reciter, surah, nextAyah)

      launch(Dispatchers.Main) {
        val activeState = _audioState.value
        if (!activeState.isPlaying || activeState.surahNumber != surah || activeState.ayahNumber != currentAyah) {
          return@launch
        }
        runCatching {
          val candidateMp = MediaPlayer().apply {
            setAudioAttributes(
              AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            )
            if (readyFile != null) {
              setDataSource(readyFile.absolutePath)
              prepare()
              nextMediaPlayer = this
              nextPreparedSurah = surah
              nextPreparedAyah = nextAyah
              nextPreparedReciterId = reciter.id
              runCatching { mediaPlayer?.setNextMediaPlayer(this) }
            } else {
              setDataSource(nextUrl)
              setOnPreparedListener { preparedMp ->
                val st = _audioState.value
                if (st.isPlaying && st.surahNumber == surah && st.ayahNumber == currentAyah) {
                  nextMediaPlayer = preparedMp
                  nextPreparedSurah = surah
                  nextPreparedAyah = nextAyah
                  nextPreparedReciterId = reciter.id
                  runCatching { mediaPlayer?.setNextMediaPlayer(preparedMp) }
                } else {
                  runCatching { preparedMp.release() }
                }
              }
              prepareAsync()
            }
          }
        }
      }
    }
  }

  private fun handleAyahOrSurahCompletion(surah: Int, ayah: Int, surahName: String) {
    stopProgressTracking()
    val currentReciter = _audioState.value.reciter
    val totalAyahs = getSurahTotalAyahs(surah)

    // Continuous playback is strictly restricted to the Quran/Surah section (!isSingleVersePlaybackMode)
    if (!isSingleVersePlaybackMode && autoAdvanceFullSurah && !currentReciter.isSurahLevelReciter && ayah < totalAyahs) {
      val nextAyah = ayah + 1
      val prepped = nextMediaPlayer
      if (prepped != null &&
        nextPreparedSurah == surah &&
        nextPreparedAyah == nextAyah &&
        nextPreparedReciterId == currentReciter.id
      ) {
        val oldMp = mediaPlayer
        mediaPlayer = prepped
        nextMediaPlayer = null
        nextPreparedSurah = -1
        nextPreparedAyah = -1
        nextPreparedReciterId = ""
        runCatching { oldMp?.setNextMediaPlayer(null) }
        runCatching { oldMp?.release() }

        val startedOk = runCatching {
          prepped.setOnCompletionListener {
            handleAyahOrSurahCompletion(surah, nextAyah, surahName)
          }
          if (!prepped.isPlaying) {
            prepped.start()
          }
          true
        }.getOrDefault(false)

        if (startedOk) {
          val dur = runCatching { prepped.duration.coerceAtLeast(1000) }.getOrDefault(6000)
          _audioState.value = _audioState.value.copy(
            isVisible = true,
            isBuffering = false,
            isAudioUnavailable = false,
            isPlaying = true,
            surahNumber = surah,
            ayahNumber = nextAyah,
            surahName = surahName,
            currentPositionMs = 0,
            durationMs = dur,
            statusMessage = "${currentReciter.name} • Ayah $nextAyah/$totalAyahs (Full Surah Auto-Play)"
          )
          startProgressTracking()
          preloadUpcomingAyahs(currentReciter, surah, nextAyah, count = 8)
          prepareNextAyahPlayerInBackground(currentReciter, surah, nextAyah, surahName)
          return
        }
      }

      playAyah(surah, nextAyah, surahName, singleVerseOnly = false, isAutoAdvance = true)
      return
    }

    val completionMessage = if (isSingleVersePlaybackMode) {
      "Exact Quranic verse finished ($surah:$ayah) • Tap Play to replay"
    } else {
      "Surah recitation finished • Tap Play to replay or X to close"
    }

    _audioState.value = _audioState.value.copy(
      isPlaying = false,
      isBuffering = false,
      isAudioUnavailable = false,
      currentPositionMs = _audioState.value.durationMs,
      statusMessage = completionMessage
    )
    scheduleAutoDismissAfterCompletion()
  }

  /**
   * Resets the green verse & word highlight state and clears stale cue points when the reciter or surah changes.
   */
  fun resetHighlightState() {
    activeCuePoints = emptyList()
    _audioState.value = _audioState.value.copy(
      ayahNumber = null,
      currentPositionMs = 0,
      durationMs = 0
    )
  }

  fun setReciter(reciter: Reciter, autoResumeCurrentAyah: Boolean = true) {
    val previousState = _audioState.value
    val previousReciterId = previousState.reciter.id
    val savedSpeed = TajweedAudioSyncEngine.loadGlobalPlaybackSpeed(context)
    val activeSurah = previousState.surahNumber
    val activeAyah = previousState.ayahNumber
    val activeSurahName = previousState.surahName
    val wasPlayingOrBuffering = previousState.isPlaying || previousState.isBuffering

    if (previousReciterId != reciter.id) {
      activeCuePoints = emptyList()
      releasePlayersOnly()
      val hasBasmala = TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(
        context = context,
        reciter = reciter,
        surahNumber = activeSurah ?: 1,
        activeAyahNumber = activeAyah ?: 1
      )
      _audioState.value = previousState.copy(
        reciter = reciter,
        playbackSpeed = savedSpeed,
        isAudioUnavailable = false,
        isPlaying = false,
        isBuffering = false,
        isRecitingBasmalaPrefix = false,
        hasBasmalaInCurrentSurahStream = hasBasmala,
        basmalaOffsetMs = 0L,
        ayahNumber = null,
        currentPositionMs = 0,
        durationMs = 0
      )
      TajweedAndAudioDiagnosticsLog.record(
        "RECITER",
        "Switched reciter $previousReciterId -> ${reciter.id} (speed=${savedSpeed}x, surah=${activeSurah ?: -1}, ayah=${activeAyah ?: -1})"
      )
      if (autoResumeCurrentAyah && wasPlayingOrBuffering && activeSurah != null && activeAyah != null) {
        if (activeAyah == 0) {
          playStartingBasmalaVerse(activeSurah, activeSurahName)
        } else {
          playAyah(activeSurah, activeAyah, activeSurahName, singleVerseOnly = isSingleVersePlaybackMode)
        }
      }
    } else {
      _audioState.value = previousState.copy(
        reciter = reciter,
        playbackSpeed = savedSpeed,
        isAudioUnavailable = false
      )
    }
  }

  /**
   * Plays the standalone Starting Bismillah Verse (`بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ`) for any Surah 2..114 (except 9).
   * - For surah-level reciters (`islam_sobhi`, `abdul_rehman_mossad`), starts at `0ms` of the Surah stream
   *   where the Basmala prefix is recited.
   * - For ayah-level EveryAyah reciters, streams the reciter's verified Bismillah (`001001.mp3`) as `ayahNumber = 0`
   *   (highlighting Bismillah in Green with Tajweed) and then automatically transitions to `surah:1`!
   */
  fun playStartingBasmalaVerse(surah: Int, surahName: String = "") {
    if (surah == 9) return
    if (surah == 1) {
      playAyah(1, 1, surahName)
      return
    }
    val currentReciter = _audioState.value.reciter
    if (currentReciter.isSurahLevelReciter) {
      playAyah(surah, 1, surahName)
      return
    }
    try {
      releasePlayersOnly()
      isSingleVersePlaybackMode = false
      val savedSpeed = TajweedAudioSyncEngine.loadGlobalPlaybackSpeed(context)
      val basmalaUrl = buildEveryAyahUrl(currentReciter, 1, 1)
      val cacheFile = getCachedAudioFile(currentReciter.id, 1, 1)
      _audioState.value = _audioState.value.copy(
        isVisible = true,
        isPlaying = false,
        isBuffering = true,
        isAudioUnavailable = false,
        surahNumber = surah,
        ayahNumber = 0,
        surahName = surahName,
        currentPositionMs = 0,
        durationMs = 4500,
        playbackSpeed = savedSpeed,
        isRecitingBasmalaPrefix = true,
        hasBasmalaInCurrentSurahStream = true,
        statusMessage = "${currentReciter.name} • Reciting Bismillah ($surahName)..."
      )
      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        if (cacheFile.exists() && cacheFile.length() > 4096L) {
          setDataSource(cacheFile.absolutePath)
        } else {
          setDataSource(basmalaUrl)
        }
        setOnPreparedListener { mp ->
          val dur = mp.duration.coerceAtLeast(1500)
          applyPlaybackSpeedToPlayer(mp)
          mp.start()
          _audioState.value = _audioState.value.copy(
            isVisible = true,
            isBuffering = false,
            isPlaying = true,
            surahNumber = surah,
            ayahNumber = 0,
            surahName = surahName,
            currentPositionMs = 0,
            durationMs = dur,
            isRecitingBasmalaPrefix = true,
            hasBasmalaInCurrentSurahStream = true,
            statusMessage = "${currentReciter.name} • Bismillahir Rahmanir Rahim ($surahName)"
          )
          startProgressTracking()
        }
        setOnCompletionListener {
          // After reciting the Starting Bismillah Verse, seamlessly continue into Ayah 1 of the Surah
          playAyah(surah, 1, surahName, singleVerseOnly = false, isAutoAdvance = true)
        }
        setOnErrorListener { _, _, _ ->
          playAyah(surah, 1, surahName)
          true
        }
        prepareAsync()
      }
    } catch (_: Exception) {
      playAyah(surah, 1, surahName)
    }
  }

  /**
   * Plays ONLY the exact single verse (Surah:Ayah) using an ayah-level human reciter and stops
   * immediately when that single verse finishes (never auto-advances to the next verse).
   * Used for Quranic Duas, Namaz Quranic verses, and reliable Noorani Qaida verse examples.
   */
  fun playSingleAyahOnly(surah: Int, ayah: Int, surahName: String = "") {
    playAyah(surah = surah, ayah = ayah, surahName = surahName, singleVerseOnly = true)
  }

  fun playAyah(
    surah: Int,
    ayah: Int,
    surahName: String = "",
    singleVerseOnly: Boolean = false,
    isAutoAdvance: Boolean = false
  ) {
    try {
      releasePlayersOnly()
      isSingleVersePlaybackMode = singleVerseOnly

      val configuredReciter = _audioState.value.reciter
      // When playing a single exact verse (e.g. Quranic Dua), ensure we use an ayah-level human reciter
      // rather than a full-surah stream so ONLY that exact verse is played.
      val currentReciter = if (singleVerseOnly && configuredReciter.isSurahLevelReciter) {
        availableReciters.firstOrNull { !it.isSurahLevelReciter } ?: configuredReciter
      } else {
        configuredReciter
      }
      val reciterId = currentReciter.id
      val totalAyahs = getSurahTotalAyahs(surah)
      val safeAyah = ayah.coerceIn(1, totalAyahs)

      // Update active surah & ayah without flickering isPlaying off during auto-advance
      _audioState.value = _audioState.value.copy(
        surahNumber = surah,
        ayahNumber = safeAyah,
        surahName = surahName,
        currentPositionMs = 0,
        durationMs = if (isAutoAdvance) _audioState.value.durationMs.coerceAtLeast(1000) else 0
      )

      // Preload upcoming ayahs in background ONLY when in continuous Surah playback mode
      if (!singleVerseOnly) {
        preloadUpcomingAyahs(currentReciter, surah, safeAyah, count = 8)
      }

      val offlineFile = runCatching {
        com.example.data.download.DownloadManager.getInstance(context)
          .findVerifiedOfflineAudioFile(reciterId, surah, safeAyah)
      }.getOrNull()

      if (offlineFile != null && offlineFile.exists() && offlineFile.length() > 64L) {
        val offlineStatusLabel =
          "${currentReciter.name} • Ayah $safeAyah/$totalAyahs • Offline Audio"
        Log.i("AudioPlayerManager", "FINAL_AUDIO_URL reciter=$reciterId surah=$surah ayah=$safeAyah url=file://${offlineFile.absolutePath}")
        lastConfirmedAudioUrl = "file://${offlineFile.absolutePath}"
        runCatching {
          mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
              AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            )
            setDataSource(offlineFile.absolutePath)
            setOnCompletionListener {
              handleAyahOrSurahCompletion(surah, safeAyah, surahName)
            }
            prepare()
            if (currentReciter.isSurahLevelReciter && safeAyah > 1) {
              val offsetMs = computeStartOffsetMsForAyah(surah, safeAyah, duration)
              if (offsetMs > 0) seekTo(offsetMs)
            }
            applyPlaybackSpeedToPlayer(this)
            start()
          }
          startProgressTracking()
        }
        val rawOfflineDur = mediaPlayer?.duration ?: (totalAyahs * 4000)
        scope.launch(Dispatchers.IO) {
          TajweedAudioSyncEngine.calibrateFromLocalAudioFile(
            context = context,
            reciter = currentReciter,
            surahNumber = surah,
            ayahNumber = safeAyah,
            audioFile = offlineFile,
            fileDurationMs = rawOfflineDur.toLong()
          )
        }
        val initialDur = if (currentReciter.isSurahLevelReciter) {
          computeSurahAyahTimingForPosition(
            surah,
            mediaPlayer?.currentPosition ?: 0,
            rawOfflineDur
          ).third
        } else {
          (mediaPlayer?.duration ?: 8000).coerceAtLeast(1000)
        }
        val hasBasmala = TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(
          context = context,
          reciter = currentReciter,
          surahNumber = surah,
          activeAyahNumber = safeAyah,
          fileDurationMs = rawOfflineDur.toLong()
        )
        _audioState.value = _audioState.value.copy(
          isVisible = true,
          isBuffering = false,
          isAudioUnavailable = false,
          isPlaying = true,
          surahNumber = surah,
          ayahNumber = safeAyah,
          surahName = surahName,
          currentPositionMs = 0,
          durationMs = initialDur,
          hasBasmalaInCurrentSurahStream = hasBasmala,
          timingStatus = TimingCalibrationStatus.CALIBRATED,
          statusMessage = offlineStatusLabel
        )
        if (!singleVerseOnly) {
          prepareNextAyahPlayerInBackground(currentReciter, surah, safeAyah, surahName)
        }
        return
      }

      val isSurahMode = currentReciter.isSurahLevelReciter
      val cacheKeyAyah = if (isSurahMode) 0 else safeAyah

      // Play immediately from preloaded/on-demand local cache when available
      val cacheFile = getCachedAudioFile(reciterId, surah, cacheKeyAyah)
      if (cacheFile.exists() && cacheFile.length() > 4096L) {
        Log.i("AudioPlayerManager", "FINAL_AUDIO_URL reciter=$reciterId surah=$surah ayah=$safeAyah url=cache://${cacheFile.absolutePath}")
        lastConfirmedAudioUrl = "cache://${cacheFile.absolutePath}"
        playFromLocalFile(
          file = cacheFile,
          surah = surah,
          ayah = safeAyah,
          surahName = surahName,
          status = if (isSurahMode) {
            "${currentReciter.name} • Surah $surah • Ayah $safeAyah/$totalAyahs (Cached Audio)"
          } else {
            "${currentReciter.name} • Ayah $safeAyah/$totalAyahs (Preloaded)"
          }
        )
        return
      }

      val connectingStatus = if (isSurahMode) {
        "Verifying ${currentReciter.name} audio for Surah $surah..."
      } else {
        "Loading ${currentReciter.name} (Ayah $safeAyah/$totalAyahs)..."
      }

      // Fast path for ayah-level EveryAyah reciters: start streaming directly on Main thread without
      // an extra thread-hop pause or stopping isPlaying during continuous Surah auto-advance
      if (!isSurahMode) {
        val directUrl = buildEveryAyahUrl(currentReciter, surah, safeAyah)
        confirmUrlMatchesRequestedSurah(currentReciter, surah, safeAyah, directUrl)
        _audioState.value = _audioState.value.copy(
          isVisible = true,
          isPlaying = isAutoAdvance,
          isBuffering = !isAutoAdvance,
          isAudioUnavailable = false,
          surahNumber = surah,
          ayahNumber = safeAyah,
          surahName = surahName,
          currentPositionMs = 0,
          durationMs = 3000,
          statusMessage = if (isAutoAdvance) {
            "${currentReciter.name} • Ayah $safeAyah/$totalAyahs (Full Surah Auto-Play)"
          } else {
            connectingStatus
          }
        )
        streamVerifiedAudioUrl(
          url = directUrl,
          fallbackUrls = emptyList(),
          surah = surah,
          ayah = safeAyah,
          surahName = surahName,
          isSurahMode = false,
          totalAyahs = totalAyahs,
          cacheFile = cacheFile
        )
        return
      }

      val primaryCandidate = resolveCandidateUrlsForReciter(currentReciter, surah, safeAyah).firstOrNull()
      if (primaryCandidate != null) {
        confirmUrlMatchesRequestedSurah(currentReciter, surah, safeAyah, primaryCandidate)
      }

      _audioState.value = _audioState.value.copy(
        isVisible = true,
        isPlaying = false,
        isBuffering = true,
        isAudioUnavailable = false,
        surahNumber = surah,
        ayahNumber = safeAyah,
        surahName = surahName,
        currentPositionMs = 0,
        durationMs = 3000,
        statusMessage = connectingStatus
      )

      // Warm MP3Quran Ayat Timing in parallel without delaying audio startup
      scope.launch(Dispatchers.IO) {
        Mp3QuranTimingClient.prefetchAyatTimingsForReciterAndSurah(currentReciter.id, surah)
      }

      fallbackJob = scope.launch(Dispatchers.IO) {
        val candidateUrls = resolveCandidateUrlsForReciter(currentReciter, surah, safeAyah)
        if (candidateUrls.isEmpty()) {
          launch(Dispatchers.Main) {
            reportAudioNotAvailable(surah, safeAyah, surahName, "Audio not available for ${currentReciter.name} (Surah $surah)")
          }
          return@launch
        }

        // Prefer the first reachable mirror, or fall back to candidateUrls.first() so MediaPlayer can cascade across fallbackUrls
        val verifiedUrl = if (isSurahMode) {
          candidateUrls.firstOrNull { verifyAudioUrlReachable(it) } ?: candidateUrls.firstOrNull()
        } else {
          candidateUrls.firstOrNull()
        }

        if (verifiedUrl == null) {
          launch(Dispatchers.Main) {
            reportAudioNotAvailable(
              surah = surah,
              ayah = safeAyah,
              surahName = surahName,
              reason = "Audio not available for Surah $surah by ${currentReciter.name} (No AI fallback used)"
            )
          }
          return@launch
        }

        confirmUrlMatchesRequestedSurah(currentReciter, surah, safeAyah, verifiedUrl)

        launch(Dispatchers.Main) {
          streamVerifiedAudioUrl(
            url = verifiedUrl,
            fallbackUrls = candidateUrls.filter { it != verifiedUrl },
            surah = surah,
            ayah = safeAyah,
            surahName = surahName,
            isSurahMode = isSurahMode,
            totalAyahs = totalAyahs,
            cacheFile = cacheFile
          )
        }
      }
    } catch (e: Exception) {
      Log.w("AudioPlayerManager", "Audio playback error", e)
      reportAudioNotAvailable(surah, ayah, surahName)
    }
  }

  private fun streamVerifiedAudioUrl(
    url: String,
    fallbackUrls: List<String>,
    surah: Int,
    ayah: Int,
    surahName: String,
    isSurahMode: Boolean,
    totalAyahs: Int,
    cacheFile: File
  ) {
    val currentReciter = _audioState.value.reciter
    confirmUrlMatchesRequestedSurah(currentReciter, surah, ayah, url)
    var preparedRef = false
    try {
      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(url)
        setOnPreparedListener { mp ->
          preparedRef = true
          val rawDur = mp.duration
          val totalDur = rawDur.coerceAtLeast(1000)
          if (isSurahMode && ayah > 1 && rawDur > 1500) {
            val startOffset = computeStartOffsetMsForAyah(surah, ayah, totalDur)
            if (startOffset > 0) {
              runCatching { mp.seekTo(startOffset) }
            }
          }
          val initialTiming = if (isSurahMode && rawDur > 1500) {
            val pos = runCatching { mp.currentPosition }.getOrDefault(0)
            computeSurahAyahTimingForPosition(surah, pos, totalDur)
          } else {
            Triple(ayah, 0, totalDur)
          }
          val playingStatus = if (isSurahMode) {
            "${currentReciter.name} • Surah $surah • Ayah ${initialTiming.first}/$totalAyahs ${if (surahName.isNotBlank()) "($surahName)" else ""}"
          } else if (isSingleVersePlaybackMode) {
            "${currentReciter.name} • Exact Verse $surah:$ayah ${if (surahName.isNotBlank()) "($surahName)" else ""}"
          } else {
            "${currentReciter.name} • Ayah $ayah/$totalAyahs (Full Surah Auto-Play)"
          }
          val hasBasmala = TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(
            context = context,
            reciter = currentReciter,
            surahNumber = surah,
            activeAyahNumber = initialTiming.first,
            fileDurationMs = totalDur.toLong()
          )
          val basmalaOffset = if (isSurahMode && surah != 1 && surah != 9) {
            resolveSurahBasmalaOffsetMs(surah, totalDur).toLong()
          } else {
            0L
          }
          _audioState.value = _audioState.value.copy(
            isVisible = true,
            isBuffering = false,
            isAudioUnavailable = false,
            isPlaying = true,
            surahNumber = surah,
            ayahNumber = initialTiming.first,
            currentPositionMs = initialTiming.second,
            durationMs = initialTiming.third,
            hasBasmalaInCurrentSurahStream = hasBasmala,
            basmalaOffsetMs = basmalaOffset,
            isRecitingBasmalaPrefix = basmalaOffset > 0L && initialTiming.first == 1 && initialTiming.second < basmalaOffset,
            statusMessage = playingStatus
          )
          applyPlaybackSpeedToPlayer(mp)
          mp.start()
          startProgressTracking()

          scope.launch(Dispatchers.IO) {
            catchAudioForOffline(url, cacheFile)
            if (cacheFile.exists() && cacheFile.length() > 4096L) {
              TajweedAudioSyncEngine.calibrateFromLocalAudioFile(
                context = context,
                reciter = currentReciter,
                surahNumber = surah,
                ayahNumber = ayah,
                audioFile = cacheFile,
                fileDurationMs = totalDur.toLong()
              )
            }
          }
          if (!isSingleVersePlaybackMode) {
            preloadUpcomingAyahs(currentReciter, surah, ayah, count = 8)
            prepareNextAyahPlayerInBackground(currentReciter, surah, ayah, surahName)
          }
        }
        setOnCompletionListener {
          handleAyahOrSurahCompletion(surah, ayah, surahName)
        }
        setOnErrorListener { _, what, extra ->
          Log.w("AudioPlayerManager", "Stream error (what=$what, extra=$extra) on $url")
          if (!preparedRef && fallbackUrls.isNotEmpty()) {
            streamVerifiedAudioUrl(
              url = fallbackUrls.first(),
              fallbackUrls = fallbackUrls.drop(1),
              surah = surah,
              ayah = ayah,
              surahName = surahName,
              isSurahMode = isSurahMode,
              totalAyahs = totalAyahs,
              cacheFile = cacheFile
            )
          } else if (!preparedRef) {
            reportAudioNotAvailable(surah, ayah, surahName)
          }
          true
        }
        prepareAsync()
      }
    } catch (_: Exception) {
      if (fallbackUrls.isNotEmpty()) {
        streamVerifiedAudioUrl(
          url = fallbackUrls.first(),
          fallbackUrls = fallbackUrls.drop(1),
          surah = surah,
          ayah = ayah,
          surahName = surahName,
          isSurahMode = isSurahMode,
          totalAyahs = totalAyahs,
          cacheFile = cacheFile
        )
      } else {
        reportAudioNotAvailable(surah, ayah, surahName)
      }
    }
  }

  /**
   * Honest error reporting when a reciter's audio is genuinely unavailable for a Surah/Ayah
   * or when the device is offline without cached audio.
   * Strictly NEVER uses AI-generated speech or silent fake playback for Quran recitation.
   */
  private fun reportAudioNotAvailable(
    surah: Int,
    ayah: Int,
    surahName: String,
    reason: String? = null
  ) {
    fallbackJob?.cancel()
    stopProgressTracking()
    activeCuePoints = emptyList()
    try {
      mediaPlayer?.setOnPreparedListener(null)
      mediaPlayer?.setOnCompletionListener(null)
      mediaPlayer?.setOnErrorListener(null)
      mediaPlayer?.reset()
    } catch (_: Exception) {
    }
    try {
      mediaPlayer?.release()
    } catch (_: Exception) {
    }
    mediaPlayer = null

    val reciterName = _audioState.value.reciter.name
    _audioState.value = _audioState.value.copy(
      isVisible = true,
      isBuffering = false,
      isPlaying = false,
      isAudioUnavailable = true,
      surahNumber = surah,
      ayahNumber = null,
      surahName = surahName,
      currentPositionMs = 0,
      durationMs = 0,
      statusMessage = reason ?: "Audio not available for Surah $surah ($reciterName) • Select another reciter or check connection"
    )
  }

  private fun scheduleAutoDismissAfterCompletion() {
    autoDismissJob?.cancel()
    autoDismissJob = scope.launch {
      delay(4000L)
      if (!_audioState.value.isPlaying && !_audioState.value.isBuffering) {
        stop()
      }
    }
  }

  fun pause() {
    autoDismissJob?.cancel()
    try {
      if (mediaPlayer?.isPlaying == true) {
        mediaPlayer?.pause()
      }
    } catch (_: Exception) {
    }
    try {
      offlineAudioTrack?.pause()
    } catch (_: Exception) {
    }
    _audioState.value = _audioState.value.copy(
      isPlaying = false,
      statusMessage = "Paused • Tap Play to resume"
    )
    stopProgressTracking()
  }

  fun resume() {
    autoDismissJob?.cancel()
    val current = _audioState.value
    val mp = mediaPlayer
    if (mp != null) {
      try {
        applyPlaybackSpeedToPlayer(mp)
        mp.start()
        _audioState.value = current.copy(
          isVisible = true,
          isPlaying = true,
          statusMessage = current.reciter.name
        )
        startProgressTracking()
        return
      } catch (_: Exception) {
      }
    }
    // If playback had already finished or used offline fallback, replay the current ayah cleanly
    val sNum = current.surahNumber
    val aNum = current.ayahNumber
    if (sNum != null && aNum != null) {
      playAyah(sNum, aNum, current.surahName)
    }
  }

  fun togglePlayPause() {
    if (_audioState.value.isPlaying) {
      pause()
    } else {
      resume()
    }
  }

  fun seekTo(positionMs: Int) {
    try {
      mediaPlayer?.seekTo(positionMs)
    } catch (_: Exception) {
    }
    _audioState.value = _audioState.value.copy(currentPositionMs = positionMs)
  }

  private fun releasePlayersOnly() {
    fallbackJob?.cancel()
    fallbackJob = null
    autoDismissJob?.cancel()
    autoDismissJob = null
    stopProgressTracking()
    releaseNextMediaPlayerOnly()
    val mp = mediaPlayer
    mediaPlayer = null
    if (mp != null) {
      try {
        mp.setOnPreparedListener(null)
        mp.setOnCompletionListener(null)
        mp.setOnErrorListener(null)
        mp.reset()
      } catch (_: Exception) {
      }
      try {
        mp.release()
      } catch (_: Exception) {
      }
    }

    try {
      offlineAudioTrack?.stop()
    } catch (_: Exception) {
    }
    try {
      offlineAudioTrack?.release()
    } catch (_: Exception) {
    }
    offlineAudioTrack = null
  }

  /**
   * Completely stops audio playback and immediately dismisses the AudioMiniPlayerBar popup.
   */
  fun stop() {
    preloadJob?.cancel()
    preloadJob = null
    preloadTargetKey = ""
    releasePlayersOnly()
    _audioState.value = AudioState(
      isVisible = false,
      isPlaying = false,
      isBuffering = false,
      surahNumber = null,
      ayahNumber = null,
      surahName = "",
      currentPositionMs = 0,
      durationMs = 0,
      statusMessage = "",
      reciter = _audioState.value.reciter,
      playbackSpeed = _audioState.value.playbackSpeed,
      isAudioAssistedRefinementEnabled = _audioState.value.isAudioAssistedRefinementEnabled,
      hasBasmalaInCurrentSurahStream = _audioState.value.reciter.isSurahLevelReciter
    )
  }

  /**
   * Updates the active ayah and within-ayah progress from the current audio position.
   * For surah-level reciters (`islam_sobhi`, `abdul_rehman_mossad`), derives the active `ayahNumber`
   * and within-ayah progress from `positionMs` so green text highlighting works across all verses.
   */
  fun applySurahProgressSnapshot(surah: Int, positionMs: Int, totalDurationMs: Int) {
    val state = _audioState.value
    val reciter = state.reciter
    if (reciter.isSurahLevelReciter) {
      if (totalDurationMs <= 1500) {
        return
      }
      val basmalaOffset = resolveSurahBasmalaOffsetMs(surah, totalDurationMs)
      val (derivedAyah, withinAyahPos, ayahDur) = computeSurahAyahTimingForPosition(
        surah = surah,
        positionMs = positionMs,
        totalSurahDurationMs = totalDurationMs
      )
      val isInBasmala = basmalaOffset > 0 && positionMs < basmalaOffset
      val totalAyahs = getSurahTotalAyahs(surah)
      _audioState.value = state.copy(
        isVisible = true,
        isPlaying = true,
        isBuffering = false,
        surahNumber = surah,
        ayahNumber = derivedAyah,
        currentPositionMs = withinAyahPos,
        durationMs = ayahDur,
        isRecitingBasmalaPrefix = isInBasmala,
        hasBasmalaInCurrentSurahStream = surah != 9,
        basmalaOffsetMs = basmalaOffset.toLong(),
        statusMessage = if (isInBasmala) {
          "${reciter.name} • Surah $surah • Bismillahir Rahmanir Rahim"
        } else {
          "${reciter.name} • Surah $surah • Ayah $derivedAyah/$totalAyahs"
        }
      )
    } else {
      _audioState.value = state.copy(
        currentPositionMs = positionMs.coerceAtLeast(0),
        durationMs = totalDurationMs.coerceAtLeast(1000)
      )
    }
  }

  private fun startProgressTracking() {
    stopProgressTracking()
    progressJob = scope.launch {
      while (isActive && mediaPlayer?.isPlaying == true) {
        val pos = try {
          mediaPlayer?.currentPosition ?: 0
        } catch (_: Exception) {
          0
        }
        val totalDur = try {
          mediaPlayer?.duration ?: 0
        } catch (_: Exception) {
          0
        }
        val current = _audioState.value
        val sNum = current.surahNumber
        if (current.reciter.isSurahLevelReciter && sNum != null && sNum in 1..114) {
          if (totalDur > 1500) {
            applySurahProgressSnapshot(sNum, pos, totalDur)
          }
        } else {
          _audioState.value = current.copy(currentPositionMs = pos)
        }
        delay(60)
      }
    }
  }

  private fun stopProgressTracking() {
    progressJob?.cancel()
    progressJob = null
  }

  private fun getCachedAudioFile(
    reciterId: String,
    surah: Int,
    ayah: Int,
    moshafId: Int = Mp3QuranTimingClient.resolveReadIdForReciter(reciterId) ?: 1
  ): File {
    val dir = File(context.filesDir, "audio_cache_v3/${reciterId}_m${moshafId}")
    if (!dir.exists()) dir.mkdirs()
    val name = if (surah > 0 && ayah > 0) "s${surah}_a${ayah}.mp3" else "s${surah}.mp3"
    return File(dir, name)
  }

  private fun playFromLocalFile(file: File, surah: Int, ayah: Int, surahName: String, status: String) {
    val currentReciter = _audioState.value.reciter
    val isSurahMode = currentReciter.isSurahLevelReciter
    val played = runCatching {
      var totalDur = 8000
      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(file.absolutePath)
        setOnCompletionListener {
          handleAyahOrSurahCompletion(surah, ayah, surahName)
        }
        prepare()
        totalDur = duration.coerceAtLeast(1000)
        if (isSurahMode && ayah > 1) {
          val offsetMs = computeStartOffsetMsForAyah(surah, ayah, totalDur)
          if (offsetMs > 0) seekTo(offsetMs)
        }
        applyPlaybackSpeedToPlayer(this)
        start()
      }
      scope.launch(Dispatchers.IO) {
        TajweedAudioSyncEngine.calibrateFromLocalAudioFile(
          context = context,
          reciter = currentReciter,
          surahNumber = surah,
          ayahNumber = ayah,
          audioFile = file,
          fileDurationMs = totalDur.toLong()
        )
      }
      val initialTiming = if (isSurahMode) {
        val pos = runCatching { mediaPlayer?.currentPosition ?: 0 }.getOrDefault(0)
        computeSurahAyahTimingForPosition(surah, pos, totalDur)
      } else {
        Triple(ayah, 0, totalDur)
      }
      val hasBasmala = TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(
        context = context,
        reciter = currentReciter,
        surahNumber = surah,
        activeAyahNumber = initialTiming.first,
        fileDurationMs = totalDur.toLong()
      )
      val basmalaOffset = if (isSurahMode && surah != 1 && surah != 9) {
        resolveSurahBasmalaOffsetMs(surah, totalDur).toLong()
      } else {
        0L
      }
      _audioState.value = _audioState.value.copy(
        isVisible = true,
        isBuffering = false,
        isPlaying = true,
        surahNumber = surah,
        ayahNumber = initialTiming.first,
        surahName = surahName,
        currentPositionMs = initialTiming.second,
        durationMs = initialTiming.third,
        hasBasmalaInCurrentSurahStream = hasBasmala,
        basmalaOffsetMs = basmalaOffset,
        isRecitingBasmalaPrefix = basmalaOffset > 0L && initialTiming.first == 1 && initialTiming.second < basmalaOffset,
        timingStatus = TimingCalibrationStatus.CALIBRATED,
        statusMessage = status
      )
      startProgressTracking()
      if (!isSingleVersePlaybackMode) {
        preloadUpcomingAyahs(_audioState.value.reciter, surah, ayah, count = 8)
        prepareNextAyahPlayerInBackground(_audioState.value.reciter, surah, ayah, surahName)
      }
      true
    }.getOrDefault(false)

    if (!played) {
      reportAudioNotAvailable(surah, ayah, surahName)
    }
  }

  private suspend fun catchAudioForOffline(url: String, targetFile: File) {
    if (targetFile.exists() && targetFile.length() > 4096L) return
    val maxAudioCacheBytes = 64L * 1024L * 1024L
    try {
      var currentUrl = url
      var redirects = 0
      while (redirects < 5) {
        if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(currentUrl) ||
          !com.example.security.SsrfRedirectValidator.validateUrlSafety(currentUrl, resolveDns = false)
        ) return
        val conn = java.net.URL(currentUrl).openConnection() as java.net.HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.connectTimeout = 5000
        conn.readTimeout = 10000
        val code = conn.responseCode
        if (code in 301..308) {
          val location = conn.getHeaderField("Location")
          conn.disconnect()
          if (location.isNullOrBlank()) return
          currentUrl = if (location.startsWith("http")) location.replace("http://", "https://") else return
          redirects++
          continue
        }
        if (code == 200 && conn.contentLengthLong <= maxAudioCacheBytes) {
          val tmp = File(targetFile.parentFile, "${targetFile.name}.tmp")
          var totalBytes = 0L
          conn.inputStream.use { input ->
            tmp.outputStream().use { output ->
              val buf = ByteArray(8192)
              var read: Int
              while (input.read(buf).also { read = it } != -1) {
                totalBytes += read
                if (totalBytes > maxAudioCacheBytes) {
                  tmp.delete()
                  conn.disconnect()
                  return
                }
                output.write(buf, 0, read)
              }
            }
          }
          if (tmp.exists() && tmp.length() > 4096L) {
            tmp.renameTo(targetFile)
          }
        }
        conn.disconnect()
        return
      }
    } catch (_: Exception) {}
  }

  /**
   * Priority 11 Addition: Clear all cached audio files to free up storage.
   */
  fun clearAudioCache() {
    scope.launch(Dispatchers.IO) {
      try {
        File(context.filesDir, "audio_cache").deleteRecursively()
        File(context.filesDir, "audio_cache_v2").deleteRecursively()
      } catch (_: Exception) {}
    }
  }
}
