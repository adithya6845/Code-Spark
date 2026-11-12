package com.example.collisionengine.ui.edusuccess

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*
import kotlin.math.roundToInt

data class MonthPerfEngage(
    val month: String,
    val performancePct: Float,
    val engagementPct: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen7ClassroomAnalytics(
    onNavigateBack: () -> Unit,
    onNavigateToRiskAnalysis: () -> Unit
) {
    val isCoreLoaded = EduDataRepository.isCoreLoaded
    val isLogsLoaded = EduDataRepository.isLogsLoaded
    val selectedClass = EduDataRepository.selectedClassId

    var selectedTab by remember { mutableStateOf("home") } // "home" | "classes" | "analytics" | "profile"
    var riskFilter by remember { mutableStateOf("All") } // "All" | "High Risk" | "Medium Risk" | "Low Risk"
    var searchQuery by remember { mutableStateOf("") }
    var showCreateAssignmentDialog by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var lastCreatedAssignmentTitle by remember { mutableStateOf("") }

    val classOptions = remember(isCoreLoaded) {
        listOf("12A", "11A", "11B", "12B", "All")
    }
    val classQuery = if (selectedClass == "All") null else selectedClass

    val classStudents = remember(selectedClass, isCoreLoaded) {
        EduDataRepository.getStudentsInClass(classQuery)
            .sortedByDescending { EduDataRepository.getRisk(it.studentId)?.riskScore ?: 0f }
    }

    val totalStudentsCount = if (classStudents.isEmpty()) 60 else classStudents.size
    val atRiskStudentsCount = remember(classStudents, isCoreLoaded) {
        val count = classStudents.count {
            val r = EduDataRepository.getRisk(it.studentId)
            r?.riskLevel == "High"
        }
        if (count == 0) 5 else count
    }
    val avgScorePct = remember(classStudents, isCoreLoaded) {
        val score = EduDataRepository.avgScore(classQuery).roundToInt()
        if (score == 0) 85 else score
    }

    val monthlyData = remember {
        listOf(
            MonthPerfEngage("Aug", 40f, 55f),
            MonthPerfEngage("Sep", 65f, 70f),
            MonthPerfEngage("Oct", 72f, 85f),
            MonthPerfEngage("Nov", 60f, 75f),
            MonthPerfEngage("Dec", 68f, 82f)
        )
    }

    val filteredStudents = remember(classStudents, riskFilter, searchQuery, isCoreLoaded) {
        classStudents.filter { s ->
            val risk = EduDataRepository.getRisk(s.studentId)
            val levelMatches = when (riskFilter) {
                "High Risk" -> risk?.riskLevel == "High"
                "Medium Risk" -> risk?.riskLevel == "Medium"
                "Low Risk" -> risk?.riskLevel == "Low" || risk == null
                else -> true
            }
            val queryMatches = searchQuery.isBlank() ||
                s.name.contains(searchQuery, ignoreCase = true) ||
                s.rollNo.contains(searchQuery, ignoreCase = true)
            levelMatches && queryMatches
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            TeacherBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    if (tab == "analytics") {
                        // Switch to detailed view
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // ── Top Header matching Image 1 (Teacher Portal) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Teacher Avatar Photo / Badge
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE))
                                )
                            )
                            .border(2.dp, PrimaryBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Face,
                            contentDescription = "Ms. Sharma",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Teacher Portal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "Good Morning, Ms. Sharma!",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "Here's your class overview",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                // Notification Bell with unread dot
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AlertRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Class Selector Chips ──
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
                            .background(if (isSel) PrimaryBlue else Color.White)
                            .border(1.dp, if (isSel) PrimaryBlue else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .clickable { EduDataRepository.selectedClassId = cls }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (cls == "All") "All Classes" else "Class $cls (Ms. Sharma)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSel) Color.White else TextPrimaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 3 Metric KPI Cards (matching Image 1) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Students Count (60)
                TeacherMetricCard(
                    modifier = Modifier.weight(1f),
                    value = "$totalStudentsCount",
                    label = "Students",
                    valueColor = TextPrimaryLight,
                    onClick = { riskFilter = "All" }
                )

                // Card 2: Avg Score (85%)
                TeacherMetricCard(
                    modifier = Modifier.weight(1f),
                    value = "$avgScorePct%",
                    label = "Avg Score",
                    valueColor = VerifiedGreen,
                    onClick = {}
                )

                // Card 3: At Risk (5)
                TeacherMetricCard(
                    modifier = Modifier.weight(1f),
                    value = "$atRiskStudentsCount",
                    label = "At Risk",
                    valueColor = AlertRed,
                    onClick = { riskFilter = "High Risk" }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Classroom Analytics Dual-Bar Chart Card (matching Image 1) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Classroom Analytics",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        Text(
                            text = "View All →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier.clickable { riskFilter = "All" }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Legend: Performance (Blue) and Engagement (Teal)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Performance",
                            fontSize = 11.sp,
                            color = TextSecondaryLight,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF14B8A6)) // Teal
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Engagement",
                            fontSize = 11.sp,
                            color = TextSecondaryLight,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dual Bar Chart
                    DualClassroomBarChart(
                        data = monthlyData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Quick Actions Row (matching Image 1: 4 Cards) ──
            Text(
                text = "Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action 1: Create Assignment
                TeacherQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Create\nAssignment",
                    icon = Icons.Filled.NoteAdd,
                    iconBg = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF7C3AED),
                    onClick = { showCreateAssignmentDialog = true }
                )

                // Action 2: Review Grading
                TeacherQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Review\nGrading",
                    icon = Icons.Filled.RateReview,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = PrimaryBlue,
                    onClick = {
                        EduDataRepository.targetAssignmentId = "A01"
                        onNavigateToRiskAnalysis()
                    }
                )

                // Action 3: View Analytics
                TeacherQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "View\nAnalytics",
                    icon = Icons.Filled.InsertChart,
                    iconBg = Color(0xFFECFDF5),
                    iconTint = VerifiedGreen,
                    onClick = { riskFilter = "All" }
                )

                // Action 4: Suggest Remediation
                TeacherQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Suggest\nRemediation",
                    icon = Icons.Filled.AutoAwesome,
                    iconBg = Color(0xFFFFFBEB),
                    iconTint = SyntheticOrange,
                    onClick = { riskFilter = "High Risk" }
                )
            }

            // Success feedback banner when teacher assigns new assignment
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Assignment Assigned to Class 12A!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14532D)
                            )
                            Text(
                                text = "\"$lastCreatedAssignmentTitle\" is now live on all student dashboards.",
                                fontSize = 12.sp,
                                color = Color(0xFF15803D)
                            )
                        }
                        IconButton(onClick = { showSuccessBanner = false }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Student Reports & Dropout Chances Section (Teacher's Roster) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Student Reports & Dropout Chances",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = "Real-time ML risk predictions & academic indicators",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${filteredStudents.size} Shown",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Risk Filter Chips (All | High Risk | Medium Risk | Low Risk)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "High Risk", "Medium Risk", "Low Risk").forEach { chip ->
                    val isSel = riskFilter == chip
                    val chipColor = when (chip) {
                        "High Risk" -> AlertRed
                        "Medium Risk" -> SyntheticOrange
                        "Low Risk" -> VerifiedGreen
                        else -> PrimaryBlue
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSel) chipColor else Color.White)
                            .border(1.dp, if (isSel) chipColor else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            .clickable { riskFilter = chip }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else TextSecondaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar for Students
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search student name or roll number...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondaryLight)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Students List with Dropout Chances & Academic Report
            filteredStudents.take(25).forEach { student ->
                val risk = EduDataRepository.getRisk(student.studentId)
                val feat = EduDataRepository.getRiskFeatures(student.studentId)
                val riskScorePct = ((risk?.riskScore ?: 0.15f) * 100).roundToInt()
                val riskLevel = risk?.riskLevel ?: if (riskScorePct >= 60) "High" else if (riskScorePct >= 30) "Medium" else "Low"

                val badgeBg = when (riskLevel) {
                    "High" -> Color(0xFFFEE2E2)
                    "Medium" -> Color(0xFFFEF3C7)
                    else -> Color(0xFFDCFCE7)
                }
                val badgeFg = when (riskLevel) {
                    "High" -> Color(0xFFDC2626)
                    "Medium" -> Color(0xFFD97706)
                    else -> Color(0xFF15803D)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable {
                            EduDataRepository.selectedStudentId = student.studentId
                            onNavigateToRiskAnalysis()
                        },
                    shape = RoundedCornerShape(16.dp),
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.name.take(1).uppercase(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = student.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${student.rollNo} • Class ${student.classId}",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Att: ${feat?.attendancePct?.roundToInt() ?: 75}% • Score: ${feat?.avgAssignmentScore?.roundToInt() ?: 68}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryBlue
                                )
                            }
                        }

                        // Dropout Chance Pill
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$riskLevel Risk ($riskScorePct%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeFg
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Report",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = TextSecondaryLight,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // ── Create Assignment Modal Dialog ──
    if (showCreateAssignmentDialog) {
        CreateAssignmentDialog(
            onDismiss = { showCreateAssignmentDialog = false },
            onAssign = { title, subject, conceptId, maxMarks, dueDate, question, refAnswer ->
                EduDataRepository.createAssignment(
                    title = title,
                    subject = subject,
                    conceptId = conceptId,
                    question = question,
                    referenceAnswer = refAnswer,
                    maxMarks = maxMarks,
                    dueDate = dueDate
                )
                lastCreatedAssignmentTitle = title
                showSuccessBanner = true
                showCreateAssignmentDialog = false
            }
        )
    }
}

/**
 * 3 Top Metric Cards in Image 1: 60 Students | 85% Avg Score | 5 At Risk
 */
@Composable
private fun TeacherMetricCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondaryLight,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Quick Action Card matching Image 1: Create Assignment | Review Grading | View Analytics | Suggest Remediation
 */
@Composable
private fun TeacherQuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * Dual Bar Chart matching Image 1 (Classroom Analytics: Performance vs Engagement across Aug-Dec)
 */
@Composable
fun DualClassroomBarChart(
    data: List<MonthPerfEngage>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val paddingLeft = 32.dp.toPx()
        val paddingBottom = 24.dp.toPx()
        val paddingTop = 12.dp.toPx()
        val graphW = w - paddingLeft - 10.dp.toPx()
        val graphH = h - paddingBottom - paddingTop

        // Horizontal Guide Lines at 90, 65, 25, 0
        val gridPcts = listOf(90f, 65f, 25f, 0f)
        gridPcts.forEach { pct ->
            val y = paddingTop + graphH - ((pct / 100f) * graphH)
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(paddingLeft, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (data.isEmpty()) return@Canvas

        val columnWidth = graphW / data.size
        val barWidth = 10.dp.toPx()
        val barSpacing = 3.dp.toPx()
        val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

        data.forEachIndexed { idx, item ->
            val centerX = paddingLeft + (idx * columnWidth) + (columnWidth / 2)

            // Performance Bar (Blue)
            val perfHeight = (item.performancePct / 100f) * graphH
            val perfX = centerX - barWidth - (barSpacing / 2)
            val perfY = paddingTop + graphH - perfHeight
            drawRoundRect(
                color = Color(0xFF2563EB), // Primary Blue
                topLeft = Offset(perfX, perfY),
                size = Size(barWidth, perfHeight),
                cornerRadius = cornerRadius
            )

            // Engagement Bar (Teal)
            val engageHeight = (item.engagementPct / 100f) * graphH
            val engageX = centerX + (barSpacing / 2)
            val engageY = paddingTop + graphH - engageHeight
            drawRoundRect(
                color = Color(0xFF14B8A6), // Teal
                topLeft = Offset(engageX, engageY),
                size = Size(barWidth, engageHeight),
                cornerRadius = cornerRadius
            )
        }
    }

    // Month Labels row under canvas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        data.forEach { item ->
            Text(
                text = item.month,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondaryLight
            )
        }
    }
}

/**
 * Teacher Portal Bottom Navigation Bar matching Image 1: Home | Classes | Analytics | Profile
 */
@Composable
private fun TeacherBottomNavBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeacherNavItem(
                title = "Home",
                icon = Icons.Filled.Home,
                isSelected = selectedTab == "home",
                onClick = { onTabSelected("home") }
            )
            TeacherNavItem(
                title = "Classes",
                icon = Icons.Filled.Class,
                isSelected = selectedTab == "classes",
                onClick = { onTabSelected("classes") }
            )
            TeacherNavItem(
                title = "Analytics",
                icon = Icons.Filled.Leaderboard,
                isSelected = selectedTab == "analytics",
                onClick = { onTabSelected("analytics") }
            )
            TeacherNavItem(
                title = "Profile",
                icon = Icons.Filled.Person,
                isSelected = selectedTab == "profile",
                onClick = { onTabSelected("profile") }
            )
        }
    }
}

@Composable
private fun TeacherNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) PrimaryBlue else Color(0xFF94A3B8),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PrimaryBlue else Color(0xFF94A3B8)
        )
    }
}

/**
 * Dialog for Teacher to assign a new assignment to Class
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentDialog(
    onDismiss: () -> Unit,
    onAssign: (title: String, subject: String, conceptId: String, maxMarks: Float, dueDate: String, question: String, refAnswer: String) -> Unit
) {
    var title by remember { mutableStateOf("Quadratic Equations Practice Set") }
    var subject by remember { mutableStateOf("Mathematics") }
    var conceptId by remember { mutableStateOf("C07") }
    var maxMarks by remember { mutableStateOf("5.0") }
    var dueDate by remember { mutableStateOf("Tomorrow, 5:00 PM") }
    var question by remember { mutableStateOf("Solve 2x^2 + 5x - 3 = 0 and verify your solutions by substitution.") }
    var refAnswer by remember { mutableStateOf("Step 1: (2x - 1)(x + 3) = 0. Roots are x = 1/2 and x = -3. Verification shows LHS = RHS.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NoteAdd, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Assignment", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Assign to Class 12A (Ms. Sharma)",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = maxMarks,
                        onValueChange = { maxMarks = it },
                        label = { Text("Max Marks") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question / Problem Statement") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = refAnswer,
                    onValueChange = { refAnswer = it },
                    label = { Text("Reference Working / Steps (for AI OCR)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marksFloat = maxMarks.toFloatOrNull() ?: 5.0f
                    onAssign(title, subject, conceptId, marksFloat, dueDate, question, refAnswer)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Assign to Class", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryLight)
            }
        }
    )
}
