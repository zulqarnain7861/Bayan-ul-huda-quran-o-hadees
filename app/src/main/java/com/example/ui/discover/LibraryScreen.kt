package com.example.ui.discover

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.MaududiLibraryCategory
import com.example.data.model.MaududiWorkRecord
import com.example.data.remote.SecurityIntegrityGuard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

/**
 * Section B — Fikr-e-Maududi 4-Category Library Screen (`ui/discover/LibraryScreen.kt`) per Spec §7.
 *
 * Categories:
 * 1. Tafheem-ul-Quran (Bibliographic & Outbound Index)
 * 2. Books & Writings Catalog
 * 3. Ideas & Core Themes
 * 4. Research & Primary References (with `quotation | summary | editorial note` tags)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaududiLibraryScreen(
  searchViewModel: SearchViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val state by searchViewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  state.selectedMaududiWork?.let { activeWork ->
    ReaderScreen(
      work = activeWork,
      disclosureNotice = state.tafheemDisclosureNotice,
      onBack = { searchViewModel.closeMaududiWorkReader() },
      modifier = modifier
    )
    return
  }

  val filteredWorks = state.maududiWorks.filter { it.category == state.selectedMaududiCategory }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag("maududi_library_screen"),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Data-Driven Licence & Bibliographic Notice (§7.2 Category 1)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("tafheem_disclosure_notice_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = GoldPrimary.copy(alpha = 0.14f)
        )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Licence and Bibliographic Disclosure",
              tint = EmeraldPrimary
            )
            Text(
              text = "  Fikr-e-Maududi · Bibliographic & Authorised Reference Index",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = state.tafheemDisclosureNotice,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.testTag("tafheem_disclosure_notice_text")
          )
        }
      }
    }

    // 4-Category Selector
    item {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.testTag("maududi_category_chips")
      ) {
        MaududiLibraryCategory.entries.forEach { category ->
          FilterChip(
            selected = state.selectedMaududiCategory == category,
            onClick = { searchViewModel.selectMaududiCategory(category) },
            label = { Text(category.titleEn, fontSize = 11.sp) },
            modifier = Modifier.testTag("maududi_cat_chip_${category.id}")
          )
        }
      }
    }

    items(filteredWorks, key = { it.workId }) { work ->
      MaududiWorkCard(
        work = work,
        onOpenReader = { searchViewModel.openMaududiWorkReader(work) },
        onOpenExternalLink = { url ->
          launchSafeHttpsLink(context, url)
        }
      )
    }
  }
}

@Composable
private fun MaududiWorkCard(
  work: MaududiWorkRecord,
  onOpenReader: () -> Unit,
  onOpenExternalLink: (String) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onOpenReader)
      .testTag("maududi_work_card_${work.workId}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
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
            text = work.licenceState.badgeLabel,
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
        SourceTierBadge(tier = work.sourceTier)
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.MenuBook,
          contentDescription = work.titleEn,
          tint = EmeraldPrimary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Column {
          Text(
            text = work.titleEn,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = work.titleUr,
            style = MaterialTheme.typography.bodySmall.copy(color = GoldPrimary)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = work.bibliographicSummary,
        style = MaterialTheme.typography.bodySmall
      )

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${work.authorName} · ${work.yearPublished ?: ""} · ${work.publisher ?: ""}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          modifier = Modifier.weight(1f)
        )
        OutlinedButton(
          onClick = { onOpenExternalLink(work.primarySourceUrl) },
          modifier = Modifier.testTag("maududi_outbound_link_${work.workId}")
        ) {
          Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open verified reading source"
          )
          Text("  Source", fontSize = 11.sp)
        }
      }
    }
  }
}

fun launchSafeHttpsLink(context: android.content.Context, rawUrl: String): Boolean {
  if (!SecurityIntegrityGuard.isExternalIntentSafe(rawUrl)) return false
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl)).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  return if (intent.resolveActivity(context.packageManager) != null) {
    try {
      context.startActivity(intent)
      true
    } catch (_: Exception) {
      false
    }
  } else {
    false
  }
}
