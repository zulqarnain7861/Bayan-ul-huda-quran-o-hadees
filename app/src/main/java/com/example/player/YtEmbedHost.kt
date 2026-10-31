package com.example.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.LectureRecord
import com.example.data.model.PlaybackDataProfile
import com.example.data.remote.SecurityIntegrityGuard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.delay

/**
 * Hardened YouTube Privacy-Enhanced IFrame Player Host (`youtube-nocookie.com`) per Spec §6.1, §9, V1, V2, T8.
 *
 * Security & Compliance:
 * - Loads `https://www.youtube-nocookie.com/embed/{id}?rel=0&enablejsapi=1&origin=https://www.youtube-nocookie.com`
 *   via `loadUrl(url, mapOf("Referer" to "https://www.youtube-nocookie.com"))` to prevent Error 153.
 * - `allowFileAccess = false`, `allowContentAccess = false`, `MIXED_CONTENT_NEVER_ALLOW`.
 * - Single `@JavascriptInterface` bridge (`YtPlayerBridge`) with `onError` and `onDuration`.
 * - Handles IFrame `onError` codes (2, 5, 100, 101, 150, 153) with the required fallback card.
 * - Tracks local watch-time seconds so the Watch-Time Topic Recommendation Engine promotes
 *   related lectures (e.g. Karbala, Reality of Yazeed, Ahl al-Bayt) to the top.
 */
object YtEmbedUrlBuilder {
  const val ORIGIN_URL = "https://www.youtube-nocookie.com"
  const val PLAYER_MENU_HELPER_NOTE = "Picture quality is set inside the YouTube player's ⋮ menu."
  private const val BRIDGE_NAME = "YtPlayerBridge"

  val HANDLED_IFRAME_ERROR_CODES = setOf(2, 5, 100, 101, 150, 153)

  const val ERROR_FALLBACK_MESSAGE =
    "This video is not playable in your region or has been removed."
  const val NO_ALTERNATE_COPY_MESSAGE =
    "No verified alternate copy indexed"

  fun buildEmbedUrl(youtubeId: String): String {
    require(LectureRecord.isValidYoutubeId(youtubeId)) {
      "Invalid 11-char YouTube ID: $youtubeId"
    }
    return "$ORIGIN_URL/embed/$youtubeId?rel=0&enablejsapi=1&origin=$ORIGIN_URL"
  }

  fun buildEmbedHeaders(): Map<String, String> = mapOf(
    "Referer" to ORIGIN_URL
  )

  fun isErrorCodeHandled(code: Int): Boolean = code in HANDLED_IFRAME_ERROR_CODES

  fun formatApproxDataEstimate(profile: PlaybackDataProfile, durationSec: Int?): String {
    val mbPerHour = profile.approxMbPerHour
    return if (durationSec != null && durationSec > 0) {
      val estMb = ((durationSec / 3600.0) * mbPerHour).toInt().coerceAtLeast(2)
      "~${estMb} MB approx for this lecture (~${mbPerHour} MB/hr · ${profile.targetResolutionHint})"
    } else {
      "~${mbPerHour} MB/hr approx (${profile.targetResolutionHint})"
    }
  }

  fun openOnYouTubeSafely(context: Context, youtubeId: String): Boolean {
    if (!LectureRecord.isValidYoutubeId(youtubeId)) return false
    val httpsUrl = "https://www.youtube.com/watch?v=$youtubeId"
    if (!SecurityIntegrityGuard.isExternalIntentSafe(httpsUrl)) return false

    val pm = context.packageManager
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$youtubeId")).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (appIntent.resolveActivity(pm) != null) {
      return try {
        context.startActivity(appIntent)
        true
      } catch (_: Exception) {
        false
      }
    }

    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(httpsUrl)).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (webIntent.resolveActivity(pm) != null) {
      return try {
        context.startActivity(webIntent)
        true
      } catch (_: Exception) {
        false
      }
    }
    return false
  }

  fun isActiveNetworkUnmeteredWifi(context: Context): Boolean {
    return try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
      val net = cm.activeNetwork ?: return false
      val caps = cm.getNetworkCapabilities(net) ?: return false
      caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    } catch (_: Exception) {
      false
    }
  }
}

class YtPlayerJsBridge(
  private val onErrorCallback: (Int) -> Unit,
  private val onDurationCallback: (Int) -> Unit
) {
  @JavascriptInterface
  fun onError(code: Int) {
    onErrorCallback(code)
  }

  @JavascriptInterface
  fun onDuration(seconds: Int) {
    if (seconds in 1..86_400) {
      onDurationCallback(seconds)
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YtEmbedHost(
  lecture: LectureRecord,
  allIndexedLectures: List<LectureRecord>,
  selectedProfile: PlaybackDataProfile,
  wifiOnlyEnabled: Boolean,
  simulatedErrorCode: Int? = null,
  onSelectProfile: (PlaybackDataProfile) -> Unit,
  onToggleWifiOnly: (Boolean) -> Unit,
  onWatchSecondsRecorded: (youtubeId: String, deltaSec: Int, currentSec: Int) -> Unit,
  onSelectAlternateLecture: (LectureRecord) -> Unit,
  onReportBroken: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var activeErrorCode by remember(lecture.youtubeId, simulatedErrorCode) {
    mutableStateOf(simulatedErrorCode)
  }
  var alternateStatusMessage by remember(lecture.youtubeId) {
    mutableStateOf<String?>(null)
  }
  var brokenReported by remember(lecture.youtubeId) {
    mutableStateOf(false)
  }
  var isPlayingSessionActive by remember(lecture.youtubeId) {
    mutableStateOf(true)
  }
  var sessionWatchedSeconds by remember(lecture.youtubeId) {
    mutableIntStateOf(0)
  }
  var dynamicDurationSec by remember(lecture.youtubeId) {
    mutableStateOf(lecture.durationSec)
  }

  val isWifiConnected = remember(wifiOnlyEnabled) {
    YtEmbedUrlBuilder.isActiveNetworkUnmeteredWifi(context)
  }
  val blockedByWifiOnly = wifiOnlyEnabled && !isWifiConnected

  // Local watch-time heartbeat tracker: increments watch time while player is open and error-free
  LaunchedEffect(lecture.youtubeId, activeErrorCode, blockedByWifiOnly, isPlayingSessionActive) {
    if (activeErrorCode == null && !blockedByWifiOnly && isPlayingSessionActive) {
      // Initial engagement signal (5 seconds) so opening and watching a topic immediately registers
      delay(1500L)
      sessionWatchedSeconds += 15
      onWatchSecondsRecorded(lecture.youtubeId, 15, sessionWatchedSeconds)
      while (true) {
        delay(10_000L)
        sessionWatchedSeconds += 10
        onWatchSecondsRecorded(lecture.youtubeId, 10, sessionWatchedSeconds)
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("yt_embed_host_container")
  ) {
    if (activeErrorCode != null && YtEmbedUrlBuilder.isErrorCodeHandled(activeErrorCode!!)) {
      YtErrorFallbackCard(
        errorCode = activeErrorCode!!,
        lecture = lecture,
        allIndexedLectures = allIndexedLectures,
        alternateStatusMessage = alternateStatusMessage,
        brokenReported = brokenReported,
        onFindAnotherVersion = {
          val verifiedAlt = lecture.alternateYoutubeIds
            .mapNotNull { altId -> allIndexedLectures.firstOrNull { it.youtubeId == altId } }
            .firstOrNull()
          if (verifiedAlt != null) {
            onSelectAlternateLecture(verifiedAlt)
          } else {
            alternateStatusMessage = YtEmbedUrlBuilder.NO_ALTERNATE_COPY_MESSAGE
          }
        },
        onOpenOnYouTube = {
          YtEmbedUrlBuilder.openOnYouTubeSafely(context, lecture.youtubeId)
        },
        onReportBroken = {
          brokenReported = true
          onReportBroken(lecture.youtubeId)
        }
      )
    } else if (blockedByWifiOnly) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("yt_wifi_only_blocked_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Wi-Fi Only Guard",
              tint = GoldPrimary
            )
            Text(
              text = "  Wi-Fi Only Mode Active",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Video stream paused to protect mobile data. Connect to Wi-Fi or turn off Wi-Fi Only below to stream.",
            style = MaterialTheme.typography.bodySmall
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = { onToggleWifiOnly(false) },
            modifier = Modifier.testTag("yt_disable_wifi_only_button")
          ) {
            Text("Stream on Mobile Data")
          }
        }
      }
    } else {
      var webViewInstance: WebView? by remember { mutableStateOf(null) }

      DisposableEffect(lecture.youtubeId) {
        onDispose {
          webViewInstance?.let { wv ->
            try {
              wv.onPause()
              wv.removeJavascriptInterface("YtPlayerBridge")
              wv.stopLoading()
              wv.destroy()
            } catch (_: Exception) {
            }
          }
          webViewInstance = null
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 9f)
          .background(Color.Black, RoundedCornerShape(14.dp))
          .testTag("yt_webview_frame")
      ) {
        AndroidView(
          modifier = Modifier.fillMaxSize(),
          factory = { ctx ->
            WebView(ctx).apply {
              settings.apply {
                javaScriptEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                mediaPlaybackRequiresUserGesture = false
                domStorageEnabled = true
                // Strip "; wv" token so YouTube's HTML5 embed player does not reject Android WebView
                userAgentString = userAgentString.replace("; wv", "")
              }
              webChromeClient = android.webkit.WebChromeClient()
              addJavascriptInterface(
                YtPlayerJsBridge(
                  onErrorCallback = { code ->
                    post {
                      if (YtEmbedUrlBuilder.isErrorCodeHandled(code)) {
                        activeErrorCode = code
                      }
                    }
                  },
                  onDurationCallback = { dur ->
                    post {
                      dynamicDurationSec = dur
                    }
                  }
                ),
                "YtPlayerBridge"
              )
              webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                  view: WebView?,
                  request: WebResourceRequest?
                ): Boolean {
                  if (request?.isForMainFrame != true) {
                    return false
                  }
                  val reqUrl = request.url?.toString() ?: return true
                  val host = request.url?.host?.lowercase() ?: return true
                  val path = request.url?.path?.lowercase() ?: ""
                  // Allow embed URLs on youtube-nocookie.com or youtube.com/embed; open watch links externally
                  return if (host.endsWith("youtube-nocookie.com") ||
                    (host.endsWith("youtube.com") && path.startsWith("/embed/"))
                  ) {
                    false
                  } else {
                    if (SecurityIntegrityGuard.isExternalIntentSafe(reqUrl)) {
                      YtEmbedUrlBuilder.openOnYouTubeSafely(ctx, lecture.youtubeId)
                    }
                    true
                  }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                  super.onPageFinished(view, url)
                  // Attach listener for YouTube IFrame postMessage events and DOM error overlay (e.g. Error 150 geo-restriction)
                  view?.evaluateJavascript(
                    """
                    (function() {
                      if (window.__bayanYtHooked) return;
                      window.__bayanYtHooked = true;
                      window.addEventListener('message', function(ev) {
                        try {
                          var d = typeof ev.data === 'string' ? JSON.parse(ev.data) : ev.data;
                          if (d && d.event === 'onError' && typeof d.info === 'number') {
                            window.YtPlayerBridge.onError(d.info);
                          } else if (d && d.event === 'infoDelivery' && d.info && d.info.duration > 0) {
                            window.YtPlayerBridge.onDuration(Math.round(d.info.duration));
                          }
                        } catch (e) {}
                      });
                      function checkDomError() {
                        try {
                          var errEl = document.querySelector('.ytp-error');
                          if (errEl && errEl.offsetParent !== null) {
                            window.YtPlayerBridge.onError(150);
                          }
                        } catch (e) {}
                      }
                      setTimeout(checkDomError, 2400);
                      setTimeout(checkDomError, 5000);
                    })();
                    """.trimIndent(),
                    null
                  )
                }

                override fun onReceivedError(
                  view: WebView?,
                  request: WebResourceRequest?,
                  error: WebResourceError?
                ) {
                  super.onReceivedError(view, request, error)
                  if (request?.isForMainFrame == true) {
                    activeErrorCode = 153
                  }
                }
              }
              val embedUrl = YtEmbedUrlBuilder.buildEmbedUrl(lecture.youtubeId)
              loadUrl(embedUrl, YtEmbedUrlBuilder.buildEmbedHeaders())
              webViewInstance = this
            }
          },
          update = { wv ->
            val targetUrl = YtEmbedUrlBuilder.buildEmbedUrl(lecture.youtubeId)
            if (wv.url != targetUrl) {
              wv.loadUrl(targetUrl, YtEmbedUrlBuilder.buildEmbedHeaders())
            }
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Multi-Channel & Fan-Channel Stream Switcher Bar (India & Global Playability)
    val fanAlternates = remember(lecture.youtubeId, allIndexedLectures) {
      val directAlts = lecture.alternateYoutubeIds
        .mapNotNull { altId -> allIndexedLectures.firstOrNull { it.youtubeId == altId } }
      val topicFanAlts = allIndexedLectures.filter { cand ->
        cand.youtubeId != lecture.youtubeId &&
          cand.sourceTier == com.example.data.model.SourceTier.FAN_CHANNEL &&
          cand.topics.any { it in lecture.topics }
      }
      (directAlts + topicFanAlts).distinctBy { it.youtubeId }.take(4)
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("yt_channel_stream_switcher_card"),
      shape = RoundedCornerShape(10.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
      )
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Channel: ${lecture.channelDisplayName}",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "${lecture.channelHandle ?: "@RAAH-e-HIDAYAT-EMAM"} · ${lecture.recordFormat.label}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
          OutlinedButton(
            onClick = { YtEmbedUrlBuilder.openOnYouTubeSafely(context, lecture.youtubeId) },
            modifier = Modifier.testTag("yt_quick_open_youtube_button")
          ) {
            Icon(
              imageVector = Icons.Default.OpenInNew,
              contentDescription = "Open in YouTube"
            )
            Text(" YouTube", fontSize = 11.sp)
          }
        }

        if (fanAlternates.isNotEmpty()) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Alternate Channels & Streams:",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPrimary
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            fanAlternates.forEachIndexed { idx, altLecture ->
              FilterChip(
                selected = false,
                onClick = { onSelectAlternateLecture(altLecture) },
                label = {
                  Text(
                    text = "Stream ${idx + 2}: ${altLecture.channelDisplayName}",
                    fontSize = 11.sp
                  )
                },
                modifier = Modifier.testTag("yt_fan_stream_chip_${altLecture.youtubeId}")
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Data Policy Selector (SAVER | BALANCED | QUALITY | AUTO) + WIFI_ONLY Toggle
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("yt_data_policy_card"),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
      )
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Playback Data Policy",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Wi-Fi Only  ",
              style = MaterialTheme.typography.labelSmall
            )
            Switch(
              checked = wifiOnlyEnabled,
              onCheckedChange = onToggleWifiOnly,
              modifier = Modifier
                .semantics { contentDescription = "Toggle Wi-Fi Only video streaming" }
                .testTag("yt_wifi_only_switch")
            )
          }
        }

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PlaybackDataProfile.entries.forEach { profile ->
            FilterChip(
              selected = selectedProfile == profile,
              onClick = { onSelectProfile(profile) },
              label = {
                Text(
                  text = "${profile.name} (${profile.targetResolutionHint})",
                  fontSize = 11.sp
                )
              },
              modifier = Modifier.testTag("yt_profile_chip_${profile.name}")
            )
          }
        }

        Text(
          text = YtEmbedUrlBuilder.formatApproxDataEstimate(selectedProfile, dynamicDurationSec),
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
          modifier = Modifier.testTag("yt_approx_data_estimate_label")
        )
        Text(
          text = YtEmbedUrlBuilder.PLAYER_MENU_HELPER_NOTE,
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          modifier = Modifier.testTag("yt_player_menu_helper_note")
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun YtErrorFallbackCard(
  errorCode: Int,
  lecture: LectureRecord,
  allIndexedLectures: List<LectureRecord>,
  alternateStatusMessage: String?,
  brokenReported: Boolean,
  onFindAnotherVersion: () -> Unit,
  onOpenOnYouTube: () -> Unit,
  onReportBroken: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("yt_error_fallback_card"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.errorContainer
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.ErrorOutline,
          contentDescription = "Playback Error Code $errorCode",
          tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
          text = "  Playback Unavailable (Code $errorCode)",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer
          )
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = YtEmbedUrlBuilder.ERROR_FALLBACK_MESSAGE,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier.testTag("yt_error_fallback_message")
      )

      if (alternateStatusMessage != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = alternateStatusMessage,
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer
          ),
          modifier = Modifier.testTag("yt_no_alternate_message")
        )
      }

      if (brokenReported) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Thank you. This video has been reported.",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onErrorContainer
          ),
          modifier = Modifier.testTag("yt_broken_reported_badge")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onFindAnotherVersion,
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.testTag("yt_find_another_version_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Find another version"
          )
          Text("  Find another version", fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = onOpenOnYouTube,
          modifier = Modifier.testTag("yt_open_on_youtube_button")
        ) {
          Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open on YouTube"
          )
          Text("  Open on YouTube", fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = onReportBroken,
          modifier = Modifier.testTag("yt_report_broken_button")
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Report broken"
          )
          Text("  Report broken", fontSize = 12.sp)
        }
      }
    }
  }
}
