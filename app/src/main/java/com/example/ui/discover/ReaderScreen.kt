package com.example.ui.discover

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MaududiLicenceState
import com.example.data.model.MaududiResearchBlock
import com.example.data.model.MaududiWorkRecord
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

/**
 * Licence-State Enforced Reader Screen (`ui/discover/ReaderScreen.kt`) per Spec §7.1 & Test T10.
 *
 * Guarantees:
 * - When `work.licenceState == MaududiLicenceState.LINK_ONLY`, NEVER renders any body text
 *   (and `work.bodyText` is strictly `null`). Instead renders the bibliographic card,
 *   disclosure notice, and verified outbound link button (`readmaududi.com` / `openlibrary.org`).
 * - When `work.researchBlocks` are present (Category 4), renders each block with its explicit
 *   tag (`quotation` | `summary` | `editorial note`).
 */
@Composable
fun ReaderScreen(
  work: MaududiWorkRecord,
  disclosureNotice: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }
  val context = LocalContext.current

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag("maududi_reader_screen"),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("maududi_reader_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Fikr-e-Maududi Library"
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = work.category.titleEn,
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = work.titleEn,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.testTag("maududi_reader_title")
          )
        }
        SourceTierBadge(tier = work.sourceTier)
      }
    }

    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("maududi_reader_bibliographic_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = work.titleUr,
            style = MaterialTheme.typography.titleMedium.copy(
              color = GoldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Author: ${work.authorName}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
          )
          Text(
            text = "Publication: ${work.publisher ?: "Idara Tarjuman-ul-Quran"} (${work.yearPublished ?: "Classical Edition"})",
            style = MaterialTheme.typography.labelSmall
          )
          if (work.ayahRange != null) {
            Text(
              text = "Quranic Scope: ${work.ayahRange}",
              style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = work.bibliographicSummary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("maududi_reader_summary")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Strict Licence State Machine Check (Test T10)
          if (work.licenceState == MaududiLicenceState.LINK_ONLY || !work.licenceState.allowsInAppFullText) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("maududi_reader_link_only_gate")
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = disclosureNotice,
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag("maududi_reader_disclosure_text")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                  onClick = { launchSafeHttpsLink(context, work.primarySourceUrl) },
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                  modifier = Modifier.testTag("maududi_reader_outbound_source_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open authorised external source"
                  )
                  Text("  Open Official Source", fontSize = 12.sp)
                }
              }
            }
          } else if (!work.bodyText.isNullOrBlank()) {
            Text(
              text = work.bodyText,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.testTag("maududi_reader_licensed_body_text")
            )
          }
        }
      }
    }

    if (work.researchBlocks.isNotEmpty()) {
      item {
        Text(
          text = "Research & Primary Reference Blocks",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
      }
      items(work.researchBlocks, key = { it.id }) { block ->
        ResearchBlockCard(
          block = block,
          onOpenLink = { launchSafeHttpsLink(context, block.outboundUrl) }
        )
      }
    }
  }
}

@Composable
private fun ResearchBlockCard(
  block: MaududiResearchBlock,
  onOpenLink: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("research_block_${block.id}"),
    shape = RoundedCornerShape(12.dp),
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
          color = EmeraldPrimary.copy(alpha = 0.15f),
          modifier = Modifier.testTag("research_block_tag_${block.id}")
        ) {
          Text(
            text = block.blockType.tagLabel,
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
        Text(
          text = block.citationReference,
          style = MaterialTheme.typography.labelSmall
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = block.heading,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = block.content,
        style = MaterialTheme.typography.bodySmall
      )
    }
  }
}
