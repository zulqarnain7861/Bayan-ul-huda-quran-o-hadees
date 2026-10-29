package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.evidence.ClaimVerificationResponse
import com.example.data.evidence.ClaimVerificationStatus
import com.example.data.evidence.EvidenceQuickAction
import com.example.data.evidence.EvidenceVerificationEngine
import com.example.data.evidence.PrimarySourceCitation
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IslamWithEvidenceScreen(
  evidenceEngine: EvidenceVerificationEngine,
  onOpenQuranReference: (Int, Int) -> Unit = { _, _ -> },
  onOpenHadithReference: (String, Int) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var selectedTab by remember { mutableIntStateOf(0) }
  var queryInput by remember { mutableStateOf("Actions are judged by intentions") }
  var activeResponse by remember { mutableStateOf<ClaimVerificationResponse?>(null) }

  // Report Incorrect Reference dialog state
  var showReportDialog by remember { mutableStateOf(false) }
  var reportRefIdentifier by remember { mutableStateOf("") }
  var reportSourceCollection by remember { mutableStateOf("") }
  var reportCategory by remember { mutableStateOf("Incorrect Hadith Grading / Numbering") }
  var reportUserNotes by remember { mutableStateOf("") }

  // Learning module quiz answers map: moduleNumber -> selectedOptionIndex
  val quizSelections = remember { mutableStateMapOf<Int, Int>() }

  val gradingGuide = remember { evidenceEngine.getHadithGradingGuide() }
  val dossiers = remember { evidenceEngine.getControversialAndHistoricalDossiers() }
  val learningModules = remember { evidenceEngine.getAll10LearningModules() }

  LaunchedEffect(Unit) {
    activeResponse = evidenceEngine.verifyClaimOrQuestion(queryInput)
  }

  Column(modifier = modifier.fillMaxSize().testTag("islam_with_evidence_screen")) {
    // Header Banner with Scholarly Epistemic Humility Notice
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = EmeraldPrimary.copy(alpha = 0.09f)
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = Icons.Default.FactCheck,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Islam with Evidence • الإِسْلَامُ بِالدَّلِيل",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text(
                text = "Claim Verification, Hadith Takhrij & Respectful Evidence-Based Study",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
          }
          OutlinedButton(
            onClick = {
              reportRefIdentifier = activeResponse?.directQuotations?.firstOrNull()?.narrationOrVerseIdentifier ?: queryInput.take(60)
              reportSourceCollection = activeResponse?.directQuotations?.firstOrNull()?.collectionTitle ?: "General Verification"
              showReportDialog = true
            },
            modifier = Modifier.testTag("evidence_report_incorrect_reference_button")
          ) {
            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Report Ref", fontSize = 11.sp)
          }
        }
      }
    }

    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary,
      edgePadding = 12.dp
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        modifier = Modifier.testTag("evidence_tab_verify_claim"),
        text = { Text("A–C. Verify Claim", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        modifier = Modifier.testTag("evidence_tab_grading_guide"),
        text = { Text("D. Hadith Grading", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        modifier = Modifier.testTag("evidence_tab_historical_dossiers"),
        text = { Text("E–F. Historical & Traditions", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
      Tab(
        selected = selectedTab == 3,
        onClick = { selectedTab = 3 },
        modifier = Modifier.testTag("evidence_tab_learning_pathways"),
        text = { Text("G. 10 Learning Modules", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
    }

    when (selectedTab) {
      0 -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // 7 Quick Actions (Section A)
          item {
            Text(
              text = "Select an Investigation Action or Paste Any Claim / Quote:",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              EvidenceQuickAction.entries.forEach { action ->
                FilterChip(
                  selected = queryInput == action.sampleQuery,
                  onClick = {
                    if (action == EvidenceQuickAction.LEARN_HADITH_AUTHENTICATION) {
                      selectedTab = 3
                    } else {
                      queryInput = action.sampleQuery
                      scope.launch {
                        activeResponse = evidenceEngine.verifyClaimOrQuestion(action.sampleQuery)
                      }
                    }
                  },
                  label = { Text(action.title, fontSize = 11.sp) },
                  modifier = Modifier.testTag("evidence_quick_action_${action.id}")
                )
              }
            }
          }

          // Search / Quotation Input Box supporting Arabic, Urdu, English & Transliteration
          item {
            ElevatedCard(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                  value = queryInput,
                  onValueChange = { queryInput = it },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("evidence_claim_input"),
                  label = { Text("Enter question or paste quote (Arabic, Urdu, English, Transliteration)") },
                  leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                  trailingIcon = {
                    if (queryInput.isNotEmpty()) {
                      IconButton(onClick = { queryInput = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                      }
                    }
                  },
                  shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Arabic, Urdu, English & Transliteration",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                  )
                  Button(
                    onClick = {
                      scope.launch {
                        activeResponse = evidenceEngine.verifyClaimOrQuestion(queryInput)
                      }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("evidence_verify_submit_button")
                  ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify Evidence", fontSize = 12.sp)
                  }
                }
              }
            }
          }

          // Display Verification Results (Section B, C, D, H)
          activeResponse?.let { resp ->
            item {
              VerificationResultSummaryCard(
                response = resp,
                onOpenQuranReference = onOpenQuranReference,
                onOpenHadithReference = onOpenHadithReference,
                onReportReference = { refId, colTitle ->
                  reportRefIdentifier = refId
                  reportSourceCollection = colTitle
                  showReportDialog = true
                }
              )
            }
          }
        }
      }

      1 -> {
        // SECTION D: Hadith Grading Explanations
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              color = GoldPrimary.copy(alpha = 0.12f)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "Golden Principles of Hadith Grading (Mustalah al-Hadith)",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "• Never assume every report in a non-Sahih compilation is automatically authentic without checking its individual chain (isnad).\n• Never label an entire classical collection unreliable merely because it contains reports requiring individual assessment.\n• Always distinguish the original narration (matn) from a scholar's grading or commentary (sharh).",
                  style = MaterialTheme.typography.bodySmall
                )
              }
            }
          }

          items(gradingGuide, key = { it.name }) { grade ->
            ElevatedCard(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = grade.gradeTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary
                    )
                  )
                  Text(
                    text = grade.arabicTerm,
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontFamily = ScheherazadeFontFamily,
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary
                    )
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = grade.definition,
                  style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Criteria: ${grade.conditionsSummary}",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Normative Scope: ${grade.normativeUsageNote}",
                  style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary)
                )
              }
            }
          }
        }
      }

      2 -> {
        // SECTION E & F: Controversial Subjects & Historical Research + Scholar & Lecture Policy
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              color = EmeraldPrimary.copy(alpha = 0.08f)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Balance, contentDescription = null, tint = EmeraldPrimary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Respectful, Evidence-Based Historical & Inter-Tradition Study",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Presents verified primary evidence, Sunni & Twelver Shia scholarly perspectives, areas of consensus, and known disagreements without selective quoting, sectarian insults, or equating mere repetition with authenticity.",
                  style = MaterialTheme.typography.bodySmall
                )
              }
            }
          }

          items(dossiers, key = { it.topicId }) { dossier ->
            ElevatedCard(
              modifier = Modifier.fillMaxWidth().testTag("dossier_card_${dossier.topicId}"),
              shape = RoundedCornerShape(16.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = EmeraldPrimary.copy(alpha = 0.12f)
                ) {
                  Text(
                    text = dossier.categoryLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = EmeraldPrimary,
                      fontWeight = FontWeight.Bold
                    )
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = dossier.titleEnglish,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "${dossier.titleArabic} • ${dossier.titleUrdu}",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = ScheherazadeFontFamily,
                    color = GoldPrimary,
                    fontSize = 15.sp
                  )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Primary Evidence: ${dossier.primaryEvidenceSummary}",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Sunni Scholarly Perspective: ${dossier.sunniScholarlyPerspective}",
                  style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Twelver Shia Scholarly Perspective: ${dossier.shiaScholarlyPerspective}",
                  style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Shared Consensus: ${dossier.areasOfAgreement}",
                  style = MaterialTheme.typography.bodySmall.copy(color = EmeraldPrimary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Known Disagreements & Scholarly Adab: ${dossier.knownDisagreementsAndAdab}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }
          }
        }
      }

      3 -> {
        // SECTION G: 10 Structured Learning Pathways
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          item {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.School, contentDescription = null, tint = EmeraldPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "10 Structured Learning Modules in Source Verification",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
          }

          items(learningModules, key = { it.id }) { mod ->
            val selectedOpt = quizSelections[mod.moduleNumber]
            ElevatedCard(
              modifier = Modifier.fillMaxWidth().testTag("learning_module_${mod.moduleNumber}"),
              shape = RoundedCornerShape(16.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = mod.title,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = mod.arabicSubtitle,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = ScheherazadeFontFamily,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = mod.shortExplanation, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  color = EmeraldPrimary.copy(alpha = 0.07f)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = "${mod.workedExampleTitle} (${mod.workedExampleSourceRef})",
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                      text = mod.workedExampleArabic,
                      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                      textAlign = TextAlign.End,
                      style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AmiriFontFamily,
                        color = EmeraldPrimary
                      )
                    )
                    Text(
                      text = mod.workedExampleAnalysis,
                      style = MaterialTheme.typography.bodySmall
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Quiz
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Quiz, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Quick Check: ${mod.quiz.question}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                  )
                }

                mod.quiz.options.forEachIndexed { idx, optText ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { quizSelections[mod.moduleNumber] = idx }
                      .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = selectedOpt == idx,
                      onClick = { quizSelections[mod.moduleNumber] = idx }
                    )
                    Text(text = optText, style = MaterialTheme.typography.bodySmall)
                  }
                }

                if (selectedOpt != null) {
                  val isCorrect = selectedOpt == mod.quiz.correctOptionIndex
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCorrect) EmeraldPrimary.copy(alpha = 0.14f) else Color(0xFFB71C1C).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                  ) {
                    Text(
                      text = (if (isCorrect) "Correct! " else "Review: ") + mod.quiz.explanation,
                      modifier = Modifier.padding(8.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCorrect) EmeraldPrimary else Color(0xFFB71C1C)
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Further Reading: ${mod.furtherReading.joinToString(" • ")}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                  )
                )
              }
            }
          }
        }
      }
    }
  }

  // Report Incorrect Reference Dialog (Section H)
  if (showReportDialog) {
    AlertDialog(
      onDismissRequest = { showReportDialog = false },
      title = { Text("Report Incorrect Reference", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Help preserve scholarly precision. Reported references are saved in the local audit log for administrator review; AI never alters canonical gradings automatically.",
            style = MaterialTheme.typography.bodySmall
          )
          OutlinedTextField(
            value = reportRefIdentifier,
            onValueChange = { reportRefIdentifier = it },
            label = { Text("Reference / Citation ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = reportSourceCollection,
            onValueChange = { reportSourceCollection = it },
            label = { Text("Collection / Edition") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = reportUserNotes,
            onValueChange = { reportUserNotes = it },
            label = { Text("Describe the citation or grading issue") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val capturedRef = reportRefIdentifier.ifBlank { "Unspecified Reference" }
            val capturedCollection = reportSourceCollection.ifBlank { "General" }
            val capturedCategory = reportCategory
            val capturedNotes = reportUserNotes.ifBlank { "User flagged reference for verification review." }
            scope.launch {
              evidenceEngine.reportIncorrectReference(
                claimOrQuery = queryInput,
                referenceIdentifier = capturedRef,
                sourceCollection = capturedCollection,
                issueCategory = capturedCategory,
                userNotes = capturedNotes
              )
              showReportDialog = false
              reportUserNotes = ""
              val launchedEmail = com.example.data.remote.SecurityIntegrityGuard.launchDeveloperEmailReport(
                context = context,
                referenceTitle = capturedRef,
                issueCategory = capturedCategory,
                issueDetails = "Query/Claim: $queryInput\n\nNotes: $capturedNotes",
                sourceCollectionOrEdition = capturedCollection
              )
              Toast.makeText(
                context,
                if (launchedEmail) {
                  "Redirecting report to Gmail (sheikhzulqar@gmail.com)..."
                } else {
                  "Reference issue recorded for sheikhzulqar@gmail.com."
                },
                Toast.LENGTH_SHORT
              ).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.testTag("submit_incorrect_reference_report_button")
        ) {
          Text("Report & Send to Gmail")
        }
      },
      dismissButton = {
        TextButton(onClick = { showReportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun VerificationResultSummaryCard(
  response: ClaimVerificationResponse,
  onOpenQuranReference: (Int, Int) -> Unit,
  onOpenHadithReference: (String, Int) -> Unit,
  onReportReference: (String, String) -> Unit
) {
  val context = LocalContext.current
  val statusColor = when (response.status) {
    ClaimVerificationStatus.EXACT_SOURCE_FOUND -> EmeraldPrimary
    ClaimVerificationStatus.PARTIAL_MATCH_FOUND -> Color(0xFF00796B)
    ClaimVerificationStatus.SCHOLARLY_GRADING_DISAGREEMENT -> GoldPrimary
    ClaimVerificationStatus.HISTORICAL_CLAIM_NEEDS_MORE_EVIDENCE -> Color(0xFFE65100)
    ClaimVerificationStatus.ATTRIBUTION_DISPUTED -> Color(0xFFB71C1C)
    ClaimVerificationStatus.NO_VERIFIED_MATCH_IN_INDEXED_SOURCES -> Color(0xFF546E7A)
  }

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // Verdict Banner
    ElevatedCard(
      modifier = Modifier.fillMaxWidth().testTag("verification_verdict_card"),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusColor.copy(alpha = 0.15f)
          ) {
            Text(
              text = response.status.displayLabel,
              modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("verification_status_badge"),
              style = MaterialTheme.typography.labelMedium.copy(
                color = statusColor,
                fontWeight = FontWeight.Bold
              )
            )
          }

          Text(
            text = "Detected: ${response.detectedLanguage}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = response.headlineVerdict,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = response.status.badgeSummary,
          style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }
    }

    // 1. Direct Verbatim Primary Quotations (kept strictly separate from commentary!)
    if (response.directQuotations.isNotEmpty()) {
      Text(
        text = "1. Verbatim Primary Source Quotations (${response.directQuotations.size})",
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary
        )
      )
      response.directQuotations.forEach { citation ->
        PrimarySourceCitationCard(
          citation = citation,
          onOpenQuranReference = onOpenQuranReference,
          onOpenHadithReference = onOpenHadithReference,
          onReportReference = onReportReference
        )
      }
    }

    // 2. Thematic Corroborations (clearly distinguished from direct verbatim quotations)
    if (response.thematicCorroborations.isNotEmpty()) {
      Text(
        text = "2. Thematic Scriptural Corroborations (Shared Principle, Distinct Wording)",
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = GoldPrimary
        )
      )
      response.thematicCorroborations.forEach { citation ->
        PrimarySourceCitationCard(
          citation = citation,
          onOpenQuranReference = onOpenQuranReference,
          onOpenHadithReference = onOpenHadithReference,
          onReportReference = onReportReference
        )
      }
    }

    // 3. Scholarly Assessments & Gradings with Attribution
    if (response.scholarlyAssessments.isNotEmpty()) {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "3. Documented Scholarly Assessments & Gradings",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(8.dp))
          response.scholarlyAssessments.forEach { sa ->
            Surface(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = sa.scholarName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                  )
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.14f)
                  ) {
                    Text(
                      text = "${sa.assignedGrade.gradeTitle} (${sa.assignedGrade.arabicTerm})",
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold
                      )
                    )
                  }
                }
                Text(
                  text = "Tradition: ${sa.scholarlyTradition} • Work Cited: ${sa.citedWorkAndEdition}",
                  style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = sa.reasoningSummary, style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }
    }

    // 4. Scholarly Synthesis & Commentary (English + Urdu) + Separation Safeguard
    ElevatedCard(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "4. Contextual Explanation & Commentary (Separate from Primary Text)",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = response.evidenceVsCommentaryNote,
          style = MaterialTheme.typography.labelSmall.copy(
            color = EmeraldPrimary,
            fontWeight = FontWeight.SemiBold
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = response.commentaryAndSynthesisEnglish,
          style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = response.commentaryAndSynthesisUrdu,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.End,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 15.sp,
            lineHeight = 28.sp
          )
        )

        if (response.counterEvidenceOrNuance.isNotEmpty()) {
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
          Text(
            text = "Methodological Nuance & Counter-Evidence:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
          response.counterEvidenceOrNuance.forEach { note ->
            Text(
              text = "• $note",
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }
      }
    }

    // 5. Verified Further Study Links (Section F)
    if (response.furtherStudyLinks.isNotEmpty()) {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "5. Further Study (Verified Original Sources)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          response.furtherStudyLinks.forEach { link ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(link.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                Text(
                  text = "${link.speakerOrAuthor} • Publisher: ${link.originalPublisher} (${link.resourceType})",
                  style = MaterialTheme.typography.labelSmall
                )
              }
              OutlinedButton(
                onClick = {
                  runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.officialUrl)))
                  }
                }
              ) {
                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Source", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    // Epistemic Humility Disclaimer
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
      Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = response.epistemicDisclaimer,
          style = MaterialTheme.typography.labelSmall
        )
      }
    }
  }
}

@Composable
private fun PrimarySourceCitationCard(
  citation: PrimarySourceCitation,
  onOpenQuranReference: (Int, Int) -> Unit,
  onOpenHadithReference: (String, Int) -> Unit,
  onReportReference: (String, String) -> Unit
) {
  val context = LocalContext.current
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("citation_card_${citation.citationId}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
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
            text = citation.traditionBadge,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
        Text(
          text = citation.relationshipType.replace('_', ' '),
          style = MaterialTheme.typography.labelSmall.copy(
            color = GoldPrimary,
            fontWeight = FontWeight.Bold
          )
        )
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "${citation.narrationOrVerseIdentifier} — ${citation.collectionTitle}",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )
      Text(
        text = "${citation.editionTitle} • ${citation.chapterOrBookLabel}",
        style = MaterialTheme.typography.labelSmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
      if (citation.narratorOrIsnad.isNotBlank()) {
        Text(
          text = "Isnad / Transmission: ${citation.narratorOrIsnad}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = EmeraldPrimary,
            fontWeight = FontWeight.SemiBold
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Verbatim Arabic Quotation
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = EmeraldPrimary.copy(alpha = 0.06f)
      ) {
        Text(
          text = citation.originalArabicText,
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          textAlign = TextAlign.End,
          style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = AmiriFontFamily,
            lineHeight = 34.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = citation.englishTranslation,
        style = MaterialTheme.typography.bodySmall
      )
      if (citation.urduTranslation.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = citation.urduTranslation,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.End,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 15.sp,
            lineHeight = 28.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Clickable Citation Actions (Priority 12 Section B.8 & Section I Test 10)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (citation.navigableSurahNumber != null && citation.navigableVerseNumber != null) {
          FilledTonalButton(
            onClick = {
              onOpenQuranReference(citation.navigableSurahNumber, citation.navigableVerseNumber)
            },
            modifier = Modifier.testTag("open_citation_in_app_${citation.citationId}")
          ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Open ${citation.narrationOrVerseIdentifier}", fontSize = 11.sp)
          }
        } else if (citation.navigableCollectionKey != null && citation.navigableHadithNumber != null) {
          FilledTonalButton(
            onClick = {
              onOpenHadithReference(citation.navigableCollectionKey, citation.navigableHadithNumber)
            },
            modifier = Modifier.testTag("open_citation_in_app_${citation.citationId}")
          ) {
            Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Open ${citation.narrationOrVerseIdentifier}", fontSize = 11.sp)
          }
        }

        OutlinedButton(
          onClick = {
            runCatching {
              context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(citation.officialVerificationUrl)))
            }
          }
        ) {
          Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Verify Link", fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
          onClick = {
            onReportReference(citation.narrationOrVerseIdentifier, citation.collectionTitle)
          }
        ) {
          Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(2.dp))
          Text("Flag", fontSize = 11.sp)
        }
      }
    }
  }
}
