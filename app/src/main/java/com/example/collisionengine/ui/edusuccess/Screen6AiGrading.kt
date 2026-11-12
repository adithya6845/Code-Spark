package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun Screen6AiGrading(
    onNavigateBack: () -> Unit,
    onReviewClick: () -> Unit,
    onApproveMarks: () -> Unit
) {
    val studentId = EduDataRepository.selectedStudentId
    val isLogsLoaded = EduDataRepository.isLogsLoaded
    val isCoreLoaded = EduDataRepository.isCoreLoaded

    val submissions = remember(studentId, isLogsLoaded) {
        EduDataRepository.getSubmissions(studentId)
    }
    var selectedSubId by remember(studentId, submissions) {
        mutableStateOf(
            submissions.find { it.submissionType == "handwritten" }?.submissionId
                ?: submissions.firstOrNull()?.submissionId
                ?: ""
        )
    }
    val activeSubmission = submissions.find { it.submissionId == selectedSubId }
    val gradingRows = remember(selectedSubId, isLogsLoaded) {
        if (selectedSubId.isBlank()) emptyList() else EduDataRepository.getGradingForSubmission(selectedSubId)
    }
    val assignmentQuestions = remember(activeSubmission, isCoreLoaded) {
        activeSubmission?.let { EduDataRepository.getAssignmentItems(it.assignmentId) } ?: emptyList()
    }

    val totalMax = gradingRows.sumOf { it.maxMarks.toDouble() }.toFloat()
    val totalScored = gradingRows.sumOf { it.finalMarks.toDouble() }.toFloat()
    val scorePercent = if (totalMax > 0f) ((totalScored / totalMax) * 100f).roundToInt() else 92
    val avgConfidence = if (gradingRows.isNotEmpty()) {
        (gradingRows.map { it.confidence }.average() * 100).roundToInt()
    } else 92

    var approvedLocal by remember(selectedSubId) { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            EduTopBar(
                title = "AI Grading Results",
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onReviewClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue)
                        )
                    ) {
                        Text(
                            text = "Review Lesson",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Button(
                        onClick = {
                            approvedLocal = true
                            onApproveMarks()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = if (approvedLocal) "Marks Approved" else "Approve Marks",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Submission selector pills from submissions.csv
            if (submissions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    submissions.forEach { sub ->
                        val isSel = sub.submissionId == selectedSubId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSel) PrimaryBlue else Color(0xFFE2E8F0))
                                .clickable { selectedSubId = sub.submissionId }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${sub.assignmentId} (${sub.submissionType})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSel) Color.White else TextPrimaryLight
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Handwritten / Submitted Answer Sheet Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        for (i in 1..5) {
                            val y = i * h / 6
                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activeSubmission?.answerFile?.substringAfterLast("/") ?: "Math_Assignment.jpg",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        val sampleQ = assignmentQuestions.firstOrNull()
                        Text(
                            text = sampleQ?.question ?: "x² + 5x + 6 = 0",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Cursive,
                            color = Color(0xFF1E293B),
                            maxLines = 2
                        )
                        Text(
                            text = if (sampleQ != null) "Ref: ${sampleQ.referenceAnswer}" else "(x + 2)(x + 3) = 0  ->  x = -2, -3",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Cursive,
                            color = TextSecondaryLight,
                            maxLines = 2
                        )
                    }

                    val allAutoApproved = gradingRows.none { it.reviewStatus.contains("Review", ignoreCase = true) }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (allAutoApproved) VerifiedGreen else SyntheticOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (allAutoApproved) Icons.Filled.Check else Icons.Filled.Warning,
                            contentDescription = "Status",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Score Gauge Donut computed from grading_results.csv
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val gaugeColor = when {
                    scorePercent >= 70 -> VerifiedGreen
                    scorePercent >= 40 -> SyntheticOrange
                    else -> AlertRed
                }
                DonutProgress(
                    progressPercent = scorePercent,
                    size = 118.dp,
                    strokeWidth = 12.dp,
                    color = gaugeColor,
                    centerValueText = if (totalMax > 0f) "%.1f".format(totalScored) else "92",
                    centerLabel = if (totalMax > 0f) "/ ${totalMax.toInt()} • $avgConfidence% conf" else "/ 100"
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Detailed Feedback Section from grading_results.csv
            Text(
                text = "Detailed AI Feedback (${gradingRows.size} Questions)",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (gradingRows.isEmpty()) {
                FeedbackCheckItem(title = "Correct factorization", subtitle = null, isWarning = false)
                FeedbackCheckItem(title = "Minor arithmetic error", subtitle = null, isWarning = false)
                FeedbackCheckItem(title = "Partial marks awarded", subtitle = null, isWarning = false)
            } else {
                gradingRows.forEach { row ->
                    val needsReview = row.reviewStatus.contains("Review", ignoreCase = true)
                    FeedbackCheckItem(
                        title = "Q${row.questionNo}: ${row.finalMarks}/${row.maxMarks.toInt()} • ${row.reviewStatus} (${(row.confidence * 100).roundToInt()}%)",
                        subtitle = row.feedback,
                        isWarning = needsReview
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FeedbackCheckItem(
    title: String,
    subtitle: String? = null,
    isWarning: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isWarning) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (isWarning) SyntheticOrange else VerifiedGreen,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondaryLight,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
