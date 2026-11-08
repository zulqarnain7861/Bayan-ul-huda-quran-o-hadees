package com.example.player

import android.content.Context
import com.example.data.model.LectureRecord
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class SavedVideoResumeState(
  val youtubeId: String,
  val stableVideoId: String,
  val positionMs: Long,
  val durationMs: Long,
  val updatedAtEpochMs: Long
) {
  val positionSec: Int
    get() = (positionMs / 1000L).toInt().coerceAtLeast(0)

  val formattedTime: String
    get() {
      val totalSec = positionSec
      val hrs = totalSec / 3600
      val mins = (totalSec % 3600) / 60
      val secs = totalSec % 60
      return if (hrs > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hrs, mins, secs)
      } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
      }
    }
}

data class VideoIdentityValidationResult(
  val isValid: Boolean,
  val expectedYoutubeId: String,
  val expectedStableVideoId: String,
  val resolvedYoutubeId: String,
  val failureReason: String? = null
)

data class QualityApplicationConfirmation(
  val requestedQuality: VideoQualityOption,
  val activeQuality: VideoQualityOption,
  val fallbackToAutoApplied: Boolean,
  val maxWidth: Int,
  val maxHeight: Int,
  val maxBitrateBps: Int,
  val isVideoTrackDisabled: Boolean
)

/**
 * Manages Video Library playback correctness (Section 4 — Workstream C):
 * 1. Strict thumbnail-to-video identity validation (`validatePlaybackBinding`), refusing any
 *    mismatched video ID or unrelated fallback stream URL.
 * 2. Real quality enumeration (`getAvailableQualitiesForBundle`) and confirmed quality track
 *    application (`confirmQualitySelection`) with sticky user preference persistence.
 * 3. Keyed resume position persistence (`saveResumePosition`, `getSavedResumeState`,
 *    `clearResumePosition`, `onVideoCompleted`) keyed by stable video identity (`youtubeId` /
 *    `stableVideoId`), never by list index.
 */
object VideoPlaybackResumeStore {
  private const val PREFS_NAME = "bayan_video_playback_resume_prefs"
  private const val KEY_POS_MS_PREFIX = "resume_pos_ms_"
  private const val KEY_DUR_MS_PREFIX = "resume_dur_ms_"
  private const val KEY_STABLE_ID_PREFIX = "resume_stable_id_"
  private const val KEY_UPDATED_AT_PREFIX = "resume_updated_at_"
  private const val KEY_STICKY_QUALITY = "sticky_video_quality_option"

  private val memoryResumeMap = ConcurrentHashMap<String, SavedVideoResumeState>()
  @Volatile private var memoryStickyQuality: VideoQualityOption? = null

  /**
   * Validates that a resolved stream bundle / player target matches the tapped `LectureRecord`
   * before playback begins. Refuses playback on any mismatch.
   */
  fun validatePlaybackBinding(
    expectedLecture: LectureRecord,
    bundle: ResolvedVideoStreamBundle,
    candidateTrackUrl: String? = null
  ): VideoIdentityValidationResult {
    if (!LectureRecord.isValidYoutubeId(expectedLecture.youtubeId)) {
      return VideoIdentityValidationResult(
        isValid = false,
        expectedYoutubeId = expectedLecture.youtubeId,
        expectedStableVideoId = expectedLecture.stableVideoId,
        resolvedYoutubeId = bundle.youtubeId,
        failureReason = "Invalid expected YouTube ID: ${expectedLecture.youtubeId}"
      )
    }
    if (!expectedLecture.matchesVideoIdentity(bundle.youtubeId, bundle.stableVideoId)) {
      return VideoIdentityValidationResult(
        isValid = false,
        expectedYoutubeId = expectedLecture.youtubeId,
        expectedStableVideoId = expectedLecture.stableVideoId,
        resolvedYoutubeId = bundle.youtubeId,
        failureReason = "Resolved video identity (${bundle.stableVideoId}) does not match tapped item (${expectedLecture.stableVideoId})"
      )
    }
    // Guard against playing an unverified Archive.org fallback MP4 for a different lecture
    val url = candidateTrackUrl?.trim().orEmpty()
    if (url.contains("archive.org/download/", ignoreCase = true)) {
      val isExactMatch = YtEmbedUrlBuilder.isExactVerifiedArchiveMatch(expectedLecture)
      val expectedArchiveUrl = YtEmbedUrlBuilder.resolveVerifiedArchiveMp4OrNull(expectedLecture)
      if (!isExactMatch || expectedArchiveUrl == null || url != expectedArchiveUrl) {
        return VideoIdentityValidationResult(
          isValid = false,
          expectedYoutubeId = expectedLecture.youtubeId,
          expectedStableVideoId = expectedLecture.stableVideoId,
          resolvedYoutubeId = bundle.youtubeId,
          failureReason = "Refused unverified or mismatched Archive.org fallback stream for lecture ${expectedLecture.youtubeId}"
        )
      }
    }
    return VideoIdentityValidationResult(
      isValid = true,
      expectedYoutubeId = expectedLecture.youtubeId,
      expectedStableVideoId = expectedLecture.stableVideoId,
      resolvedYoutubeId = bundle.youtubeId,
      failureReason = null
    )
  }

  /**
   * Enumerates only the video qualities actually available for the given `ResolvedVideoStreamBundle`.
   * - If multi-resolution direct streams or adaptive HLS are present, returns the distinct available
   *   resolutions plus `Q_360P` (Auto / Default Saver) and available audio-only tracks.
   * - If a single progressive stream or WebView bridge stream is active, returns only the genuinely
   *   distinct supported modes rather than duplicating 8 identical dummy entries.
   */
  fun getAvailableQualitiesForBundle(bundle: ResolvedVideoStreamBundle): List<VideoQualityOption> {
    if (bundle.tracksByQuality.isEmpty()) {
      return listOf(VideoQualityOption.Q_360P, VideoQualityOption.AUDIO_128K)
    }
    val hasHls = bundle.tracksByQuality.values.any { it.isHls && it.videoUrl.isNotBlank() }
    if (hasHls) {
      // Adaptive HLS master playlist supports resolution cap selection across standard rungs
      return VideoQualityOption.entries.toList()
    }
    // Group non-blank tracks by distinct underlying stream URL + audio/video mode
    val distinctVideoUrls = LinkedHashSet<String>()
    val result = mutableListOf<VideoQualityOption>()
    for (option in VideoQualityOption.entries) {
      val track = bundle.tracksByQuality[option] ?: continue
      if (track.videoUrl.isBlank()) continue
      if (option.isAudioOnly) {
        result.add(option)
      } else if (distinctVideoUrls.add(track.videoUrl) || option == VideoQualityOption.Q_360P) {
        result.add(option)
      }
    }
    if (VideoQualityOption.Q_360P !in result) {
      result.add(0, VideoQualityOption.Q_360P)
    }
    if (result.none { it.isAudioOnly }) {
      result.add(VideoQualityOption.AUDIO_128K)
    }
    return result.distinct()
  }

  /**
   * Resolves and confirms the active quality selection for `bundle`. If `requestedQuality` is not
   * among the available qualities for `bundle`, falls back cleanly to `Q_360P` (Auto / Default Saver).
   */
  fun confirmQualitySelection(
    bundle: ResolvedVideoStreamBundle,
    requestedQuality: VideoQualityOption
  ): QualityApplicationConfirmation {
    val available = getAvailableQualitiesForBundle(bundle)
    val active = if (requestedQuality in available) {
      requestedQuality
    } else {
      VideoQualityOption.Q_360P
    }
    val fallbackApplied = active != requestedQuality
    return QualityApplicationConfirmation(
      requestedQuality = requestedQuality,
      activeQuality = active,
      fallbackToAutoApplied = fallbackApplied,
      maxWidth = active.maxWidth,
      maxHeight = active.maxHeight,
      maxBitrateBps = active.maxBitrateBps,
      isVideoTrackDisabled = active.isAudioOnly
    )
  }

  fun getStickyQuality(context: Context?): VideoQualityOption? {
    memoryStickyQuality?.let { return it }
    if (context == null) return null
    val raw = runCatching {
      context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_STICKY_QUALITY, null)
    }.getOrNull() ?: return null
    return VideoQualityOption.entries.firstOrNull { it.name == raw }?.also {
      memoryStickyQuality = it
    }
  }

  fun setStickyQuality(context: Context?, quality: VideoQualityOption) {
    memoryStickyQuality = quality
    if (context != null) {
      runCatching {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putString(KEY_STICKY_QUALITY, quality.name)
          .apply()
      }
    }
  }

  fun saveResumePosition(
    context: Context?,
    lecture: LectureRecord,
    positionMs: Long,
    durationMs: Long = (lecture.durationSec ?: 0) * 1000L
  ) {
    if (!LectureRecord.isValidYoutubeId(lecture.youtubeId)) return
    val safePosMs = positionMs.coerceAtLeast(0L)
    val safeDurMs = durationMs.coerceAtLeast(0L)

    // If playback has reached >= 95% of duration (or within last 4 seconds of a known duration),
    // treat the video as finished and clear the saved resume position.
    if (safeDurMs >= 10_000L && (safePosMs >= (safeDurMs * 0.95).toLong() || (safeDurMs - safePosMs) <= 4_000L)) {
      clearResumePosition(context, lecture.youtubeId)
      return
    }

    if (safePosMs < 1_000L) return

    val state = SavedVideoResumeState(
      youtubeId = lecture.youtubeId,
      stableVideoId = lecture.stableVideoId,
      positionMs = safePosMs,
      durationMs = safeDurMs,
      updatedAtEpochMs = System.currentTimeMillis()
    )
    memoryResumeMap[lecture.youtubeId] = state
    if (context != null) {
      runCatching {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putLong(KEY_POS_MS_PREFIX + lecture.youtubeId, state.positionMs)
          .putLong(KEY_DUR_MS_PREFIX + lecture.youtubeId, state.durationMs)
          .putString(KEY_STABLE_ID_PREFIX + lecture.youtubeId, state.stableVideoId)
          .putLong(KEY_UPDATED_AT_PREFIX + lecture.youtubeId, state.updatedAtEpochMs)
          .apply()
      }
    }
  }

  fun getSavedResumeState(context: Context?, lecture: LectureRecord): SavedVideoResumeState? {
    if (!LectureRecord.isValidYoutubeId(lecture.youtubeId)) return null
    memoryResumeMap[lecture.youtubeId]?.let { cached ->
      if (cached.youtubeId == lecture.youtubeId && cached.positionMs >= 1_000L) {
        return cached
      }
    }
    if (context == null) return null
    return runCatching {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val posMs = prefs.getLong(KEY_POS_MS_PREFIX + lecture.youtubeId, 0L)
      if (posMs < 1_000L) return@runCatching null
      val durMs = prefs.getLong(KEY_DUR_MS_PREFIX + lecture.youtubeId, (lecture.durationSec ?: 0) * 1000L)
      val stableId = prefs.getString(KEY_STABLE_ID_PREFIX + lecture.youtubeId, lecture.stableVideoId)
        ?: lecture.stableVideoId
      val updatedAt = prefs.getLong(KEY_UPDATED_AT_PREFIX + lecture.youtubeId, 0L)
      SavedVideoResumeState(
        youtubeId = lecture.youtubeId,
        stableVideoId = stableId,
        positionMs = posMs,
        durationMs = durMs,
        updatedAtEpochMs = updatedAt
      ).also {
        memoryResumeMap[lecture.youtubeId] = it
      }
    }.getOrNull()
  }

  fun clearResumePosition(context: Context?, youtubeId: String) {
    memoryResumeMap.remove(youtubeId)
    if (context != null) {
      runCatching {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .remove(KEY_POS_MS_PREFIX + youtubeId)
          .remove(KEY_DUR_MS_PREFIX + youtubeId)
          .remove(KEY_STABLE_ID_PREFIX + youtubeId)
          .remove(KEY_UPDATED_AT_PREFIX + youtubeId)
          .apply()
      }
    }
  }

  fun onVideoCompleted(context: Context?, youtubeId: String) {
    clearResumePosition(context, youtubeId)
  }

  fun clearAllForTesting(context: Context? = null) {
    memoryResumeMap.clear()
    memoryStickyQuality = null
    if (context != null) {
      runCatching {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
      }
    }
  }
}
