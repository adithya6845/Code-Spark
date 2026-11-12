package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*
import kotlin.math.roundToInt

private data class LearningPathStep(
    val title: String,
    val status: String, // "Completed" | "In Progress" | "Pending"
    val dotColor: Color,
    val badgeBg: Color,
    val badgeTextColor: Color,
    val isCheckedIcon: Boolean = false
)

@Composable
fun Screen3StudentHome(
    onNavigateToLesson: () -> Unit,
    onNavigateToEduVoice: () -> Unit,
    onNavigateToQuizzes: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToCelebration: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSwitcher: () -> Unit,
    onBottomTabSelected: (String) -> Unit
) {
    val studentId = EduDataRepository.selectedStudentId
    val isCoreLoaded = EduDataRepository.isCoreLoaded
    val isLogsLoaded = EduDataRepository.isLogsLoaded
    val lastOcr = EduDataRepository.lastOcrAnalysis
    val lastOcrTitle = EduDataRepository.lastOcrAssignmentTitle

    val student = remember(studentId, isCoreLoaded) { EduDataRepository.getStudent(studentId) }
    val features = remember(studentId, isCoreLoaded) { EduDataRepository.getRiskFeatures(studentId) }
    val weakFromCsv = remember(studentId, isCoreLoaded, isLogsLoaded) {
        EduDataRepository.weakConceptsForStudent(studentId, limit = 6)
    }

    val firstName = student?.name?.substringBefore(" ") ?: "Adithya"
    val baseProgress = features?.let {
        ((it.attendancePct + it.avgQuizScore + it.lessonCompletionPct) / 3f).roundToInt().coerceIn(25, 95)
    } ?: 78
    val overallProgress = if (lastOcr != null) {
        ((baseProgress * 0.65f) + (lastOcr.scorePercent * 0.35f)).roundToInt().coerceIn(30, 99)
    } else {
        baseProgress
    }

    // Build Personalized "Your Learning Path" timeline steps dynamically from OCR weak concepts + CSV curriculum
    val learningPathSteps = remember(lastOcr, weakFromCsv, isCoreLoaded) {
        val ocrWeak = lastOcr?.weakConcepts.orEmpty().distinct()
        val csvTopics = weakFromCsv.map { it.first.topic }.distinct()
        val allConcepts = EduDataRepository.concepts.map { it.topic }.distinct()

        val completedTopic = lastOcr?.strongPoints?.firstOrNull()?.let { sp ->
            if (sp.length <= 28) sp else csvTopics.lastOrNull() ?: "Matrices & Determinants"
        } ?: (csvTopics.getOrNull(0) ?: "Matrices & Determinants")

        val inProgressTopic = ocrWeak.firstOrNull()
            ?: csvTopics.firstOrNull { it != completedTopic }
            ?: "Functions & Graphs"

        val pendingTopic1 = ocrWeak.getOrNull(1)
            ?: csvTopics.firstOrNull { it != completedTopic && it != inProgressTopic }
            ?: allConcepts.firstOrNull { it != completedTopic && it != inProgressTopic }
            ?: "Probability"

        val pendingTopic2 = ocrWeak.getOrNull(2)
            ?: csvTopics.firstOrNull { it != completedTopic && it != inProgressTopic && it != pendingTopic1 }
            ?: allConcepts.firstOrNull { it != completedTopic && it != inProgressTopic && it != pendingTopic1 }
            ?: "Calculus Basics"

        listOf(
            LearningPathStep(
                title = completedTopic,
                status = "Completed",
                dotColor = VerifiedGreen,
                badgeBg = Color(0xFFDCFCE7),
                badgeTextColor = Color(0xFF15803D),
                isCheckedIcon = true
            ),
            LearningPathStep(
                title = inProgressTopic,
                status = "In Progress",
                dotColor = PrimaryBlue,
                badgeBg = Color(0xFFDBEAFE),
                badgeTextColor = Color(0xFF1D4ED8),
                isCheckedIcon = true
            ),
            LearningPathStep(
                title = pendingTopic1,
                status = "Pending",
                dotColor = SyntheticOrange,
                badgeBg = Color(0xFFFDF2F8),
                badgeTextColor = Color(0xFF6B7280),
                isCheckedIcon = false
            ),
            LearningPathStep(
                title = pendingTopic2,
                status = "Pending",
                dotColor = Color(0xFFF87171),
                badgeBg = Color(0xFFFDF2F8),
                badgeTextColor = Color(0xFF6B7280),
                isCheckedIcon = false
            )
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            EduBottomNavBar(
                selectedTab = "home",
                onTabSelected = onBottomTabSelected,
                onFabClick = onOpenSwitcher
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

            // Header matching Image 3: Avatar + "Hello, Adithya! 👋" + "Keep going! You're on track." + Bell icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenDrawer() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFDE68A), Color(0xFF93C5FD))
                                )
                            )
                            .border(2.dp, PrimaryBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = firstName.take(1).uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Hello, $firstName! \uD83D\uDC4B",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (lastOcr != null) {
                                "OCR updated: ${lastOcr.scorePercent}% on ${lastOcrTitle ?: "Assignment"}"
                            } else {
                                "Keep going! You're on track."
                            },
                            fontSize = 13.sp,
                            color = TextSecondaryLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Notification Bell Icon with red badge dot
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToCelebration() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(26.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(AlertRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Overall Progress Card matching uploaded Image 3 (Green Donut + Growth % + Area Sparkline)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProgress() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DonutProgress(
                        progressPercent = overallProgress,
                        size = 86.dp,
                        strokeWidth = 9.dp,
                        color = VerifiedGreen,
                        trackColor = Color(0xFFDCFCE7)
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overall Progress",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (lastOcr != null) {
                                    "OCR Score ${lastOcr.scorePercent}% • +12% this month"
                                } else {
                                    "+12% this month"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VerifiedGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                tint = VerifiedGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        MiniTrendSparkline(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            lineColor = VerifiedGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4 Quick Action Tiles: My Learning | EduVoice | Upload & OCR | Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickAccessItem(
                    title = "My Learning",
                    icon = Icons.Filled.MenuBook,
                    bgColor = Color(0xFFEFF6FF),
                    iconColor = PrimaryBlue,
                    onClick = onNavigateToLesson
                )
                QuickAccessItem(
                    title = "EduVoice",
                    icon = Icons.Filled.Mic,
                    bgColor = Color(0xFFF5F3FF),
                    iconColor = AccentPurple,
                    onClick = onNavigateToEduVoice
                )
                QuickAccessItem(
                    title = "Upload & OCR",
                    icon = Icons.Filled.AssignmentTurnedIn,
                    bgColor = Color(0xFFF5F3FF),
                    iconColor = AccentPurple,
                    onClick = onNavigateToQuizzes
                )
                QuickAccessItem(
                    title = "Messages",
                    icon = Icons.Filled.MarkEmailUnread,
                    bgColor = Color(0xFFF5F3FF),
                    iconColor = AccentPurple,
                    onClick = onNavigateToProgress
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // "Your Learning Path" + "View Plan ->" (matching Image 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Learning Path",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToLesson() }
                ) {
                    Text(
                        text = "View Plan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (lastOcr != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Updated from latest OCR grading check (${lastOcr.weakConcepts.joinToString(", ").ifBlank { "Concept review" }})",
                    fontSize = 11.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Vertical Connected Timeline Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    learningPathSteps.forEachIndexed { index, step ->
                        val isLast = index == learningPathSteps.lastIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToLesson() }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step circle + connector bar
                            Box(
                                modifier = Modifier.width(30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isLast) {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = 22.dp)
                                            .width(2.dp)
                                            .height(22.dp)
                                            .background(Color(0xFFE2E8F0))
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(step.dotColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (step.isCheckedIcon) Icons.Filled.Check else Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = step.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(step.badgeBg)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = step.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = step.badgeTextColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // EduVoice Tutor Banner Card matching Image 3 bottom section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFE0E7FF), Color(0xFFDDD6FE))
                        )
                    )
                    .clickable { onNavigateToEduVoice() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4F46E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "EduVoice Tutor",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "EduVoice Tutor",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Text(
                                text = "Learn through voice!",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    // Audio waveform bars matching screenshot
                    Canvas(
                        modifier = Modifier
                            .width(56.dp)
                            .height(28.dp)
                    ) {
                        val heights = listOf(0.25f, 0.55f, 0.35f, 0.9f, 0.6f, 0.95f, 0.45f, 0.7f, 0.3f)
                        val barW = 3.dp.toPx()
                        val gap = (size.width - heights.size * barW) / (heights.size - 1).coerceAtLeast(1)
                        heights.forEachIndexed { idx, frac ->
                            val bh = size.height * frac
                            val x = idx * (barW + gap)
                            val y = (size.height - bh) / 2f
                            drawRoundRect(
                                color = Color(0xFF3B82F6),
                                topLeft = Offset(x, y),
                                size = Size(barW, bh),
                                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Launch Voice Tutor",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickAccessItem(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryLight
        )
    }
}
