package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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

    val student = remember(studentId, isCoreLoaded) { EduDataRepository.getStudent(studentId) }
    val risk = remember(studentId, isCoreLoaded) { EduDataRepository.getRisk(studentId) }

    val riskScorePct = ((risk?.riskScore ?: 0.68f) * 100).roundToInt()
    val studentName = student?.name ?: "Adithya Kumar"
    val rollNo = student?.rollNo ?: "22CS001"

    // Key factors matching Image 4 (SHAP values)
    val shapItems = remember(risk) {
        listOf(
            ShapDisplayItem("Low Attendance", 0.32f, "0.32"),
            ShapDisplayItem("Poor Performance", 0.28f, "0.28"),
            ShapDisplayItem("Missed Assignments", 0.22f, "0.22"),
            ShapDisplayItem("Low Engagement", 0.18f, "0.18")
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            EduTopBar(
                title = "Student Risk Analysis",
                onBackClick = onNavigateBack,
                trailingIcon = Icons.Filled.Refresh,
                onTrailingClick = { /* Refresh predictions */ }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
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
                            text = "Track Intervention",
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
            Spacer(modifier = Modifier.height(10.dp))

            // ── Student Info Row matching Image 4 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = studentName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Roll No: $rollNo",
                        fontSize = 13.sp,
                        color = TextSecondaryLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Large Risk Level Card matching Image 4 (High Risk + 68% Donut) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Risk Level",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE11D48)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "High Risk",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFBE123C)
                        )
                    }

                    // Circular Progress Donut 68%
                    DonutProgress(
                        progressPercent = riskScorePct,
                        size = 80.dp,
                        strokeWidth = 9.dp,
                        color = Color(0xFFF43F5E), // Coral Red
                        trackColor = Color(0xFFFFE4E6),
                        centerValueText = "$riskScorePct%"
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Key Factors (SHAP) Card matching Image 4 ──
            Text(
                text = "Key Factors (SHAP)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                    shapItems.forEachIndexed { index, item ->
                        ShapRedBarRow(
                            label = item.label,
                            value = item.value,
                            displayValue = item.displayValue
                        )
                        if (index < shapItems.lastIndex) {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Recommended Actions Card matching Image 4 ──
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
                        text = "Recommended Actions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Schedule a mentoring session",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimaryLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Offer remedial lessons",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class ShapDisplayItem(
    val label: String,
    val value: Float,
    val displayValue: String
)

@Composable
private fun ShapRedBarRow(
    label: String,
    value: Float,
    displayValue: String
) {
    val fraction = (value / 0.40f).coerceIn(0.1f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red accent indicator line
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFF43F5E))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryLight,
            modifier = Modifier.width(140.dp)
        )

        // Horizontal red bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFF1F5F9))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = fraction)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFF43F5E))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = displayValue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondaryLight,
            modifier = Modifier.width(36.dp)
        )
    }
}
