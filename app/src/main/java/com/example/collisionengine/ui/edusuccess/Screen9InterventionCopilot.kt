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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*

@Composable
fun Screen9InterventionCopilot(
    onNavigateBack: () -> Unit,
    onViewDetails: () -> Unit
) {
    // Mode toggles between Screen 5 (Intervention Progress) and Screen 2 (Counsellor Dashboard)
    val userRole = EduDataRepository.activeUserRole
    var selectedView by remember(userRole) {
        mutableStateOf(if (userRole == "Counsellor") "counsellor" else "progress")
    }

    if (selectedView == "counsellor") {
        CounsellorDashboardView(
            onNavigateBack = onNavigateBack,
            onPlanIntervention = { selectedView = "progress" }
        )
    } else {
        InterventionProgressView(
            onNavigateBack = onNavigateBack,
            onViewFullReport = onViewDetails,
            onSwitchToCounsellor = { selectedView = "counsellor" }
        )
    }
}

/**
 * Screen 5 in design: Intervention Progress
 */
@Composable
private fun InterventionProgressView(
    onNavigateBack: () -> Unit,
    onViewFullReport: () -> Unit,
    onSwitchToCounsellor: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            EduTopBar(
                title = "Intervention Progress",
                onBackClick = onNavigateBack,
                trailingIcon = Icons.Filled.SupervisorAccount,
                onTrailingClick = onSwitchToCounsellor
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
                    onClick = onViewFullReport,
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
                            text = "View Full Report",
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

            // ── Top Scheduled Alert Card matching Image 5 ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Intervention Scheduled!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Follow-up in 1 week",
                            fontSize = 13.sp,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Progress Overview Card matching Image 5 (78% milestone chart) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
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
                            text = "Progress Overview",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        // 78% Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "78% Milestone",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerifiedGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Upward Curved Chart (Aug to Dec reaching 78%)
                    InterventionCurvedChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Impact Summary Card matching Image 5 ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Impact Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ImpactCheckItem(text = "Attendance Improved")
                    Spacer(modifier = Modifier.height(12.dp))
                    ImpactCheckItem(text = "Quiz Score Increased")
                    Spacer(modifier = Modifier.height(12.dp))
                    ImpactCheckItem(text = "Risk Reduced to Low")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Screen 2 in design: Counsellor Dashboard
 */
@Composable
private fun CounsellorDashboardView(
    onNavigateBack: () -> Unit,
    onPlanIntervention: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            EduTopBar(
                title = "Counsellor Dashboard",
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
            Spacer(modifier = Modifier.height(10.dp))

            // Header profile: Mr. Ramesh Rao
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Mr. Ramesh Rao",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = "Senior Academic Counsellor • Class 12A",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── At-Risk Students Card (12 students, +2 more this week) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = AlertRed,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "12",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AlertRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "At-Risk Students",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }
                        Text(
                            text = "↑ 2 more this week across Class 12A & 11A",
                            fontSize = 12.sp,
                            color = SyntheticOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Risk Heatmap Card matching Image 2 (Donut: 68% At Risk) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
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
                            text = "Risk Heatmap",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        Text(
                            text = "View Details →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Donut
                        DonutProgress(
                            progressPercent = 68,
                            size = 100.dp,
                            strokeWidth = 11.dp,
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFFF3F4F6),
                            centerValueText = "68%"
                        )

                        // Legend breakdown: Low 32%, Med 38%, High 30%
                        Column {
                            RiskLegendRow(color = VerifiedGreen, label = "Low Risk", value = "32%")
                            Spacer(modifier = Modifier.height(8.dp))
                            RiskLegendRow(color = SyntheticOrange, label = "Medium Risk", value = "38%")
                            Spacer(modifier = Modifier.height(8.dp))
                            RiskLegendRow(color = AlertRed, label = "High Risk", value = "30%")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Recommended Actions Card matching Image 2 ──
            Text(
                text = "Recommended Actions",
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
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Adithya Kumar",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "High Risk (68%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Schedule an academic check-in",
                                fontSize = 13.sp,
                                color = TextSecondaryLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onPlanIntervention,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = "Plan Intervention →",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RiskLegendRow(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondaryLight,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )
    }
}

@Composable
private fun ImpactCheckItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = VerifiedGreen,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimaryLight
        )
    }
}

@Composable
private fun InterventionCurvedChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val paddingLeft = 32.dp.toPx()
        val paddingBottom = 24.dp.toPx()
        val graphW = w - paddingLeft - 20.dp.toPx()
        val graphH = h - paddingBottom - 10.dp.toPx()

        // Y-axis guide lines: 100, 75, 50, 25, 0
        for (i in 0..4) {
            val y = graphH - (i * graphH / 4) + 10.dp.toPx()
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(paddingLeft, y),
                end = Offset(w - 20.dp.toPx(), y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Points across 5 months: Aug (25%), Sep (45%), Oct (60%), Nov (70%), Dec (78%)
        val ratios = listOf(0.25f, 0.45f, 0.60f, 0.70f, 0.78f)
        val stepX = graphW / (ratios.size - 1)

        val points = ratios.mapIndexed { idx, r ->
            val x = paddingLeft + (idx * stepX)
            val y = graphH - (r * graphH) + 10.dp.toPx()
            Offset(x, y)
        }

        // Draw curved path
        val path = Path()
        points.forEachIndexed { idx, pt ->
            if (idx == 0) {
                path.moveTo(pt.x, pt.y)
            } else {
                val prev = points[idx - 1]
                val midX = (prev.x + pt.x) / 2
                path.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
            }
        }

        // Draw Gradient Fill under curve
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(points.last().x, graphH + 10.dp.toPx())
        fillPath.lineTo(points.first().x, graphH + 10.dp.toPx())
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF14B8A6).copy(alpha = 0.25f), Color.Transparent)
            )
        )

        // Draw Line
        drawPath(
            path = path,
            color = Color(0xFF14B8A6), // Teal
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Highlight milestone point on Dec (78%)
        val last = points.last()
        drawCircle(color = Color(0xFF14B8A6), radius = 6.dp.toPx(), center = last)
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = last)
    }

    // Month labels under canvas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("Aug", "Sep", "Oct", "Nov", "Dec").forEach { month ->
            Text(
                text = month,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryLight
            )
        }
    }
}
