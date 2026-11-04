package com.example.ui.discover

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LectureRecord
import com.example.security.SecureNetworkClient
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

/**
 * Memory-cached YouTube Lecture Thumbnail Loader + Rich Islamic Scholarly Fallback Banner
 * so every lecture in Search Results, Recommendations, Related Lectures, and All Lectures
 * displays a clear visual thumbnail with play badge, Mas'alah/Lecture number, and duration.
 */
object LectureThumbnailCache {
  private val memoryCache = object : LruCache<String, ImageBitmap>(180) {}

  fun getCached(youtubeId: String): ImageBitmap? = synchronized(memoryCache) {
    memoryCache.get(youtubeId)
  }

  suspend fun fetchThumbnail(youtubeId: String): ImageBitmap? = withContext(Dispatchers.IO) {
    synchronized(memoryCache) {
      memoryCache.get(youtubeId)?.let { return@withContext it }
    }

    val urls = listOf(
      "https://i.ytimg.com/vi/$youtubeId/mqdefault.jpg",
      "https://i.ytimg.com/vi/$youtubeId/hqdefault.jpg",
      "https://img.youtube.com/vi/$youtubeId/mqdefault.jpg"
    )

    for (url in urls) {
      val bmp: ImageBitmap? = runCatching {
        val req = Request.Builder()
          .url(url)
          .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) Chrome/124.0.0.0 Mobile")
          .get()
          .build()
        SecureNetworkClient.httpClient.newCall(req).execute().use { resp ->
          if (!resp.isSuccessful) return@use null
          val bytes = resp.body?.bytes() ?: return@use null
          if (bytes.size < 1400) return@use null // Skip tiny 120x90 grey placeholder
          val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@use null
          if (decoded.width <= 120 && decoded.height <= 90) return@use null
          decoded.asImageBitmap()
        }
      }.getOrNull()

      if (bmp != null) {
        synchronized(memoryCache) {
          memoryCache.put(youtubeId, bmp)
        }
        return@withContext bmp
      }
    }

    null
  }
}

@Composable
fun LectureThumbnailBox(
  lecture: LectureRecord,
  modifier: Modifier = Modifier,
  compact: Boolean = false
) {
  var bitmap by remember(lecture.youtubeId) {
    mutableStateOf(LectureThumbnailCache.getCached(lecture.youtubeId))
  }

  LaunchedEffect(lecture.youtubeId) {
    if (bitmap == null) {
      bitmap = LectureThumbnailCache.fetchThumbnail(lecture.youtubeId)
    }
  }

  val shape = RoundedCornerShape(if (compact) 10.dp else 12.dp)
  val topicBadgeText = remember(lecture.topics, lecture.lectureNumber) {
    when {
      lecture.lectureNumber != null -> "Mas'alah #${lecture.lectureNumber}"
      "karbala_hussain" in lecture.topics -> "KARBALA & HUSSAIN A.S."
      "reality_of_yazeed" in lecture.topics -> "REALITY OF YAZEED"
      "sahaba_unity" in lecture.topics -> "SAHABA & KHILAFAT"
      "ahl_al_bayt" in lecture.topics -> "AHL AL-BAYT"
      "salah_azan" in lecture.topics -> "SALAH & SUNNAH"
      "tafseer_quran" in lecture.topics -> "QURAN TAFSEER"
      "aqeedah_tawheed" in lecture.topics -> "TAWHEED & AQEEDAH"
      else -> "EMAM LECTURE"
    }
  }

  val durationBadgeText = remember(lecture.durationSec, lecture.recordFormat) {
    val sec = lecture.durationSec
    if (sec != null && sec > 0) {
      val hrs = sec / 3600
      val mins = (sec % 3600) / 60
      val remSec = sec % 60
      if (hrs > 0) {
        String.format("%d:%02d:%02d", hrs, mins, remSec)
      } else {
        String.format("%d:%02d", mins, remSec)
      }
    } else {
      lecture.recordFormat.label.uppercase()
    }
  }

  Box(
    modifier = modifier
      .clip(shape)
      .background(
        brush = Brush.linearGradient(
          colors = listOf(
            Color(0xFF0A2E23),
            Color(0xFF124E3B),
            Color(0xFF071F17)
          )
        )
      )
      .border(1.dp, GoldPrimary.copy(alpha = 0.35f), shape)
      .testTag("lecture_thumbnail_${lecture.youtubeId}")
  ) {
    if (bitmap != null) {
      Image(
        bitmap = bitmap!!,
        contentDescription = "Thumbnail for ${lecture.displayTitle}",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
      // Subtle dark scrim for clear badge & play icon contrast
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Black.copy(alpha = 0.35f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.65f)
              )
            )
          )
      )
    } else {
      // Rich Styled Scholarly Artwork when thumbnail is loading or offline
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(if (compact) 6.dp else 10.dp)
      ) {
        Spacer(modifier = Modifier.weight(1f))
        if (!compact) {
          Text(
            text = lecture.displayTitle,
            color = Color.White.copy(alpha = 0.90f),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 10.sp,
              lineHeight = 12.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 36.dp)
          )
        }
      }
    }

    // Top-left Mas'alah / Topic pill
    Surface(
      shape = RoundedCornerShape(5.dp),
      color = Color.Black.copy(alpha = 0.72f),
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(if (compact) 4.dp else 6.dp)
    ) {
      Text(
        text = topicBadgeText,
        color = GoldPrimary,
        fontSize = if (compact) 8.sp else 9.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
      )
    }

    // Center Play button overlay
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .size(if (compact) 28.dp else 36.dp)
        .clip(CircleShape)
        .background(EmeraldPrimary.copy(alpha = 0.88f))
        .border(1.dp, GoldPrimary.copy(alpha = 0.8f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.PlayArrow,
        contentDescription = "Play ${lecture.displayTitle}",
        tint = Color.White,
        modifier = Modifier.size(if (compact) 18.dp else 22.dp)
      )
    }

    // Bottom-right duration / format badge
    Surface(
      shape = RoundedCornerShape(4.dp),
      color = Color.Black.copy(alpha = 0.82f),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(if (compact) 4.dp else 6.dp)
    ) {
      Text(
        text = durationBadgeText,
        color = Color.White,
        fontSize = if (compact) 8.sp else 9.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
      )
    }
  }
}
