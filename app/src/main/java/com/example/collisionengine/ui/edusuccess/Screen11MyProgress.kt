package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun Screen11MyProgress(
    onNavigateBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Academic") }
    val filters = listOf("Academic", "Learning", "Engagement")

    val studentId = EduDataRepository.selectedStudentId
    val isCoreLoaded = EduDataRepository.isCoreLoaded
    val isLogsLoaded = EduDataRepository.isLogsLoaded

    val features = remember(studentId, isCoreLoaded) { EduDataRepository.getRiskFeatures(studentId) }
    val engagementLogs = remember(studentId, isLogsLoaded) { EduDataRepository.getEngagement(studentId) }
    val subjectMap = remember(studentId, isLogsLoaded, isCoreLoaded) {
        EduDataRepository.subjectMasteryForStudent(studentId)
    }

    // Build 5 periodic bars from engagement_logs.csv depending on selected filter
    val barsData = remember(engagementLogs, selectedFilter) {
        if (engagementLogs.isEmpty()) {
            listOf("W1" to 0.25f, "W2" to 0.45f, "W3" to 0.65f, "W4" to 0.82f, "W5" to 0.95f)
        } else {
            val chunkSize = (engagementLogs.size / 5).coerceAtLeast(1)
            engagementLogs.chunked(chunkSize).take(5).mapIndexed { idx, chunk ->
                val label = "P${idx + 1}"
                val ratio = when (selectedFilter) {
                    "Learning" -> (chunk.map { it.lessonCompletionPct }.average() / 100.0).toFloat()
                    "Engagement" -> (chunk.map { it.timeSpentMin }.average() / 180.0).toFloat()
                    else -> (chunk.map { it.quizzesAttempted * 10f + it.lessonCompletionPct }.average() / 100.0).toFloat()
                }.coerceIn(0.12f, 1f)
                label to ratio
            }
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            EduTopBar(
                title = "My Progress",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs: Academic | Learning | Engagement
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PrimaryBlue else Color(0xFFF1F5F9))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Growth Bar Chart Card from engagement_logs.csv
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "$selectedFilter Growth (${engagementLogs.size} Weeks)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MonthlyProgressBars(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        barColor = when (selectedFilter) {
                            "Learning" -> AccentPurple
                            "Engagement" -> VerifiedGreen
                            else -> PrimaryBlue
                        },
                        bars = barsData
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Subject Mastery Section from quiz_attempts.csv & student_risk_features.csv
            Text(
                text = "Subject Mastery & Metrics",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    val palette = listOf(PrimaryBlue, SyntheticOrange, VerifiedGreen)
                    subjectMap.entries.forEachIndexed { idx, (subj, pct) ->
                        SubjectMasteryRow(
                            subject = subj,
                            score = pct,
                            barColor = palette[idx % palette.size]
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (features != null) {
                        SubjectMasteryRow(
                            subject = "Attendance Rate",
                            score = features.attendancePct.roundToInt().coerceIn(0, 100),
                            barColor = AccentPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SubjectMasteryRow(
    subject: String,
    score: Int,
    barColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(barColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = subject,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryLight
                )
            }

            Text(
                text = "$score%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (score / 100f).coerceIn(0.02f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}
