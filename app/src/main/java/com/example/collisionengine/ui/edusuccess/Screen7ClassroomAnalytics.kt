package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
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
fun Screen7ClassroomAnalytics(
    onNavigateBack: () -> Unit,
    onNavigateToRiskAnalysis: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Overview") }
    val tabs = listOf("Overview", "Students")

    val isCoreLoaded = EduDataRepository.isCoreLoaded
    val isLogsLoaded = EduDataRepository.isLogsLoaded
    val selectedClass = EduDataRepository.selectedClassId

    val classOptions = remember(isCoreLoaded) {
        listOf("All") + EduDataRepository.classes.map { it.classId }
    }
    val classQuery = if (selectedClass == "All") null else selectedClass

    val studentCount = remember(selectedClass, isCoreLoaded) {
        EduDataRepository.classStudentCount(classQuery)
    }
    val avgScorePct = remember(selectedClass, isCoreLoaded) {
        EduDataRepository.avgScore(classQuery).roundToInt()
    }
    val trendData = remember(selectedClass, isLogsLoaded) {
        val realTrend = EduDataRepository.weeklyPerformanceTrend(classQuery)
        if (realTrend.isEmpty()) EduMockData.performanceTrend
        else realTrend.map { (wk, allAvg, clsAvg) -> WeekPerformance(wk, allAvg, clsAvg) }
    }
    val weakConcepts = remember(selectedClass, isLogsLoaded, isCoreLoaded) {
        val realWeak = EduDataRepository.weakConceptsForClass(classQuery, limit = 4)
        if (realWeak.isEmpty()) EduMockData.weakConcepts
        else realWeak.map { (concept, failPct) -> WeakConcept(concept.topic, failPct) }
    }
    val classStudents = remember(selectedClass, isCoreLoaded) {
        EduDataRepository.getStudentsInClass(classQuery)
            .sortedByDescending { EduDataRepository.getRisk(it.studentId)?.riskScore ?: 0f }
    }
    val classInfo = remember(selectedClass, isCoreLoaded) {
        if (classQuery != null) EduDataRepository.getClassInfo(classQuery) else null
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            EduTopBar(
                title = "Classroom Analytics",
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
            Spacer(modifier = Modifier.height(6.dp))

            // Class Filter Chips (All, 11A..12C from classes.csv)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classOptions.forEach { cls ->
                    val isSel = selectedClass == cls
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) PrimaryBlue else Color(0xFFE2E8F0))
                            .clickable { EduDataRepository.selectedClassId = cls }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (cls == "All") "All Classes" else "Class $cls",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSel) Color.White else TextPrimaryLight
                        )
                    }
                }
            }

            if (classInfo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Teacher: ${classInfo.classTeacher} • Counsellor: ${classInfo.counsellor}",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs: Overview | Students
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedTab = tab }
                            .padding(end = 24.dp, top = 6.dp, bottom = 6.dp)
                    ) {
                        Text(
                            text = if (tab == "Students") "Students ($studentCount)" else tab,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryBlue else TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(PrimaryBlue)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == "Overview") {
                // Metric Cards: Total Students | Avg Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = "Students" },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Group,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$studentCount",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                                Text(
                                    text = "Total Students",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFECFDF5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    tint = VerifiedGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$avgScorePct%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                                Text(
                                    text = "Avg Score",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Performance Trend Line Chart Card
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Performance Trend",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryBlue))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("School Avg", fontSize = 11.sp, color = TextSecondaryLight)

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VerifiedGreen))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Selected", fontSize = 11.sp, color = TextSecondaryLight)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ClassPerformanceLineChart(data = trendData)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 36.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            trendData.forEach { item ->
                                Text(item.week, fontSize = 12.sp, color = TextSecondaryLight, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Weak Concepts Alert Section from quiz_attempts.csv
                Text(
                    text = "Weak Concepts (Error Rate)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                Spacer(modifier = Modifier.height(12.dp))

                weakConcepts.forEach { concept ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onNavigateToRiskAnalysis() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = SyntheticOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = concept.conceptName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimaryLight
                                )
                            }

                            Text(
                                text = "${concept.failurePercent}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        }
                    }
                }
            } else {
                // Students Roster Tab (from students.csv + risk_scores_demo.csv + student_risk_features.csv)
                classStudents.take(40).forEach { s ->
                    val r = EduDataRepository.getRisk(s.studentId)
                    val f = EduDataRepository.getRiskFeatures(s.studentId)
                    val badgeBg = when (r?.riskLevel) {
                        "High" -> SoftRed
                        "Medium" -> SoftOrange
                        else -> SoftGreen
                    }
                    val badgeFg = when (r?.riskLevel) {
                        "High" -> AlertRed
                        "Medium" -> SyntheticOrange
                        else -> VerifiedGreen
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                EduDataRepository.selectedStudentId = s.studentId
                                onNavigateToRiskAnalysis()
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = s.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                    Text(
                                        text = "${s.rollNo} • Class ${s.classId} • Att ${f?.attendancePct?.roundToInt() ?: 0}%",
                                        fontSize = 11.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${r?.riskLevel ?: "Low"} (${((r?.riskScore ?: 0f) * 100).roundToInt()}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeFg
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
