package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
fun Screen8StudentRisk(
    onNavigateBack: () -> Unit,
    onViewRecommendations: () -> Unit
) {
    val studentId = EduDataRepository.selectedStudentId
    val isCoreLoaded = EduDataRepository.isCoreLoaded
    val isLogsLoaded = EduDataRepository.isLogsLoaded

    val student = remember(studentId, isCoreLoaded) { EduDataRepository.getStudent(studentId) }
    val risk = remember(studentId, isCoreLoaded) { EduDataRepository.getRisk(studentId) }
    val features = remember(studentId, isCoreLoaded) { EduDataRepository.getRiskFeatures(studentId) }
    val attSummary = remember(studentId, isLogsLoaded) { EduDataRepository.getAttendanceSummary(studentId) }

    val riskPercent = ((risk?.riskScore ?: 0.68f) * 100).roundToInt()
    val riskLevelLabel = "${risk?.riskLevel ?: "High"} Risk"
    val riskColor = when (risk?.riskLevel) {
        "High" -> AlertRed
        "Medium" -> SyntheticOrange
        else -> VerifiedGreen
    }
    val riskBg = when (risk?.riskLevel) {
        "High" -> SoftRed
        "Medium" -> SoftOrange
        else -> SoftGreen
    }

    val shapFactors = remember(risk) {
        if (risk == null) {
            EduMockData.shapFactors
        } else {
            listOf(
                ShapFactor("Attendance", -risk.shapAttendance, "-%.2f".format(risk.shapAttendance)),
                ShapFactor("Poor Performance", -risk.shapPerformance, "-%.2f".format(risk.shapPerformance)),
                ShapFactor("Missed Assign.", -risk.shapAssignments, "-%.2f".format(risk.shapAssignments)),
                ShapFactor("Low Engagement", -risk.shapEngagement, "-%.2f".format(risk.shapEngagement))
            )
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            EduTopBar(
                title = "Student Risk Analysis",
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
                Button(
                    onClick = onViewRecommendations,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "View Recommendations",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Student Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(2.dp, PrimaryBlue, CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = student?.name ?: EduMockData.student.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Roll No: ${student?.rollNo ?: EduMockData.student.rollNo} • Class ${student?.classId ?: "12A"}",
                                fontSize = 12.sp,
                                color = TextSecondaryLight
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(riskBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = riskLevelLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gauge: Dropout Risk (Next 6 Months)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    DonutProgress(
                        progressPercent = riskPercent,
                        size = 110.dp,
                        strokeWidth = 11.dp,
                        color = riskColor,
                        trackColor = Color(0xFFF1F5F9)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = "Dropout Risk",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "(Next 6 Months)",
                            fontSize = 13.sp,
                            color = TextSecondaryLight
                        )
                        if (features != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Attendance: ${features.attendancePct}% • Quiz: ${features.avgQuizScore}%",
                                fontSize = 11.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Key Factors (SHAP / Synthetic Demo Shares)
            Text(
                text = "Key Factors (SHAP)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    shapFactors.forEach { factor ->
                        ShapHorizontalBar(
                            name = factor.factorName,
                            value = factor.impactValue,
                            displayValue = factor.displayValue
                        )
                    }
                }
            }

            if (attSummary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val present = attSummary["Present"] ?: 0
                        val late = attSummary["Late"] ?: 0
                        val absent = attSummary["Absent"] ?: 0
                        AttendanceStat("Present", present, VerifiedGreen)
                        AttendanceStat("Late", late, SyntheticOrange)
                        AttendanceStat("Absent", absent, AlertRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AttendanceStat(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondaryLight
        )
    }
}
