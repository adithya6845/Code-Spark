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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*
import kotlin.math.roundToInt

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
    val student = remember(studentId, isCoreLoaded) { EduDataRepository.getStudent(studentId) }
    val features = remember(studentId, isCoreLoaded) { EduDataRepository.getRiskFeatures(studentId) }
    val risk = remember(studentId, isCoreLoaded) { EduDataRepository.getRisk(studentId) }

    val firstName = student?.name?.substringBefore(" ") ?: "Adithya"
    val overallProgress = features?.let {
        ((it.attendancePct + it.avgQuizScore + it.lessonCompletionPct) / 3f).roundToInt().coerceIn(5, 99)
    } ?: 78

    val latestAssignment = remember(isCoreLoaded) {
        EduDataRepository.assignments.distinctBy { it.assignmentId }.lastOrNull()
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
            Spacer(modifier = Modifier.height(16.dp))

            // Header: Hello, Adithya! + Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello, $firstName! \uD83D\uDC4B",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (student != null) {
                            "Class ${student.classId} • ${student.rollNo} • Synthetic Demo"
                        } else {
                            "Keep going! You're on track."
                        },
                        fontSize = 13.sp,
                        color = TextSecondaryLight
                    )
                }

                // Student Avatar Button (Clicking opens Drawer)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, PrimaryBlue, CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.15f))
                        .clickable { onOpenDrawer() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card: Overall Progress (from student_risk_features.csv)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProgress() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DonutProgress(
                        progressPercent = overallProgress,
                        size = 80.dp,
                        strokeWidth = 9.dp,
                        color = PrimaryBlue
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overall Progress",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val subText = if (features != null) {
                                "Att ${features.attendancePct.roundToInt()}% • Quiz ${features.avgQuizScore.roundToInt()}%"
                            } else {
                                "+ 12% this month"
                            }
                            Text(
                                text = subText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (risk?.riskLevel == "High") SyntheticOrange else VerifiedGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (risk?.riskLevel == "High") SyntheticOrange else VerifiedGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    MiniTrendSparkline()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Access Section
            Text(
                text = "Quick Access",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    bgColor = Color(0xFFFAF5FF),
                    iconColor = AccentPurple,
                    onClick = onNavigateToEduVoice
                )
                QuickAccessItem(
                    title = "Quizzes",
                    icon = Icons.Filled.SportsEsports,
                    bgColor = Color(0xFFF0FDF4),
                    iconColor = VerifiedGreen,
                    onClick = onNavigateToQuizzes
                )
                QuickAccessItem(
                    title = "Progress",
                    icon = Icons.Filled.BarChart,
                    bgColor = Color(0xFFFFFBEB),
                    iconColor = SyntheticOrange,
                    onClick = onNavigateToProgress
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToLesson() }
                ) {
                    Text(
                        text = "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Upcoming Assignment Card from assignments.csv
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToQuizzes() },
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoftPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = latestAssignment?.assignmentTitle ?: "Mathematics Assignment",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (latestAssignment != null) {
                                "${latestAssignment.subject} • Due ${latestAssignment.dueDate}"
                            } else {
                                "Due Tomorrow • 10 AM"
                            },
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Encouragement Card: You're doing great! Keep learning!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCelebration() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (risk?.riskLevel == "High") {
                                "${features?.voiceSessions ?: 5} EduVoice Sessions Completed"
                            } else {
                                "You're doing great!"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Keep learning!",
                            fontSize = 13.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = TrophyGold,
                        modifier = Modifier.size(38.dp)
                    )
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
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
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
