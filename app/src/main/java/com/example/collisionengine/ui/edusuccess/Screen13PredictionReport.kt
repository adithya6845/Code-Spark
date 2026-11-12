package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun Screen13PredictionReport(
    onNavigateBack: () -> Unit,
    onViewFullAnalysis: () -> Unit
) {
    val isCoreLoaded = EduDataRepository.isCoreLoaded
    // Default to S0003 (Low Risk example in dataset) or let user toggle to selected student
    var previewLowRiskSample by remember { mutableStateOf(true) }
    val targetId = if (previewLowRiskSample) "S0003" else EduDataRepository.selectedStudentId

    val student = remember(targetId, isCoreLoaded) { EduDataRepository.getStudent(targetId) }
    val risk = remember(targetId, isCoreLoaded) { EduDataRepository.getRisk(targetId) }
    val features = remember(targetId, isCoreLoaded) { EduDataRepository.getRiskFeatures(targetId) }

    val riskLevel = risk?.riskLevel ?: "Low"
    val riskPct = ((risk?.riskScore ?: 0.10f) * 100).roundToInt()
    val isLow = riskLevel == "Low"

    val pillBg = if (isLow) Color(0xFFDCFCE7) else SoftRed
    val pillBorder = if (isLow) Color(0xFF86EFAC) else AlertRed.copy(alpha = 0.4f)
    val accent = if (isLow) VerifiedGreen else AlertRed

    val reasons = remember(features, isLow) {
        if (features == null) {
            listOf("Good attendance", "Improving performance", "Regular engagement")
        } else {
            listOf(
                "Attendance: ${features.attendancePct}% (${if (features.attendancePct >= 80f) "Strong" else "Needs attention"})",
                "Avg Assignment Score: ${features.avgAssignmentScore}% (${features.assignmentsSubmitted} submitted)",
                "Lesson Completion: ${features.lessonCompletionPct}% • ${features.avgWeeklyLogins} logins/wk"
            )
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            EduTopBar(
                title = "Prediction Report",
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp),
                color = Color.Transparent
            ) {
                OutlinedButton(
                    onClick = {
                        EduDataRepository.selectedStudentId = targetId
                        onViewFullAnalysis()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "View Full Analysis",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Toggle between Low Risk Demo Student (S0003) and Active Student (S0001)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(true to "Low Risk (S0003)", false to "Active (${EduDataRepository.selectedStudentId})").forEach { (isLowMode, label) ->
                    val sel = previewLowRiskSample == isLowMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (sel) PrimaryBlue else Color.Transparent)
                            .clickable { previewLowRiskSample = isLowMode }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (sel) Color.White else TextSecondaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${student?.name ?: "Rahul Sharma"} • Class ${student?.classId ?: "11A"}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondaryLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Big Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(pillBg)
                    .border(1.dp, pillBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLow) Icons.Filled.Check else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$riskLevel Risk",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card: Risk Probability
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(if (isLow) Color(0xFFF0FDF4) else SoftRed, Color.White)
                            )
                        )
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Risk Probability",
                            fontSize = 16.sp,
                            color = TextSecondaryLight,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$riskPct%",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Reasoning Factors Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Reasoning Factors",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                Spacer(modifier = Modifier.height(14.dp))

                reasons.forEach { reason ->
                    ReasoningCheckItem(title = reason, isWarning = !isLow)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ReasoningCheckItem(title: String, isWarning: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isWarning) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (isWarning) SyntheticOrange else VerifiedGreen,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryLight
        )
    }
}
