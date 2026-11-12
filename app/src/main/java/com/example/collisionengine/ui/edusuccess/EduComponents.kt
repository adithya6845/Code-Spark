package com.example.collisionengine.ui.edusuccess

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.ui.theme.*

/**
 * Standard Header Top Bar for EduSuccess AI Screens
 */
@Composable
fun EduTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    isDarkTheme: Boolean = false
) {
    val contentColor = if (isDarkTheme) Color.White else TextPrimaryLight
    val bgColor = if (isDarkTheme) BackgroundDark else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = contentColor
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (trailingIcon != null && onTrailingClick != null) {
            IconButton(onClick = onTrailingClick) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = "Action",
                    tint = contentColor
                )
            }
        }
    }
}

/**
 * Animated Circular Donut Progress
 */
@Composable
fun DonutProgress(
    progressPercent: Int,
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp,
    color: Color = PrimaryBlue,
    trackColor: Color = Color(0xFFE2E8F0),
    centerLabel: String? = null,
    centerValueText: String = "$progressPercent%"
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "donutProgress"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.toPx() - stroke
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress Arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerValueText,
                fontSize = (size.value * 0.24f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimaryLight
            )
            if (centerLabel != null) {
                Text(
                    text = centerLabel,
                    fontSize = (size.value * 0.12f).sp,
                    color = TextSecondaryLight,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Green Upward Trend Sparkline with area fill & endpoint dot (Screen 3)
 */
@Composable
fun MiniTrendSparkline(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(40.dp),
    lineColor: Color = VerifiedGreen
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pts = listOf(
            Offset(0f, h * 0.88f),
            Offset(w * 0.16f, h * 0.72f),
            Offset(w * 0.28f, h * 0.78f),
            Offset(w * 0.44f, h * 0.54f),
            Offset(w * 0.56f, h * 0.58f),
            Offset(w * 0.72f, h * 0.36f),
            Offset(w * 0.85f, h * 0.39f),
            Offset(w, h * 0.12f)
        )

        val linePath = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fillPath = Path().apply {
            moveTo(pts.first().x, h)
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(w, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.22f), Color.Transparent)
            )
        )
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(color = lineColor, radius = 4.dp.toPx(), center = pts.last())
    }
}

/**
 * Two-line Comparison Chart (Teacher Classroom Analytics - Screen 7)
 */
@Composable
fun ClassPerformanceLineChart(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(180.dp),
    data: List<WeekPerformance>
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val paddingLeft = 40.dp.toPx()
        val paddingBottom = 30.dp.toPx()
        val graphW = w - paddingLeft - 20.dp.toPx()
        val graphH = h - paddingBottom - 10.dp.toPx()

        // Horizontal Grid Lines (0, 25, 50, 75, 100)
        for (i in 0..4) {
            val y = graphH - (i * graphH / 4) + 10.dp.toPx()
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(paddingLeft, y),
                end = Offset(w - 20.dp.toPx(), y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
        }

        if (data.isEmpty()) return@Canvas

        val stepX = graphW / (data.size - 1).coerceAtLeast(1)

        val classAvgPoints = data.mapIndexed { idx, item ->
            val x = paddingLeft + (idx * stepX)
            val y = graphH - ((item.classAvg / 100f) * graphH) + 10.dp.toPx()
            Offset(x, y)
        }

        val yourClassPoints = data.mapIndexed { idx, item ->
            val x = paddingLeft + (idx * stepX)
            val y = graphH - ((item.yourClass / 100f) * graphH) + 10.dp.toPx()
            Offset(x, y)
        }

        // Draw Class Avg line (Blue)
        val bluePath = Path()
        classAvgPoints.forEachIndexed { idx, p ->
            if (idx == 0) bluePath.moveTo(p.x, p.y) else bluePath.lineTo(p.x, p.y)
        }
        drawPath(
            path = bluePath,
            color = PrimaryBlue,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        classAvgPoints.forEach { p ->
            drawCircle(PrimaryBlue, radius = 5.dp.toPx(), center = p)
            drawCircle(Color.White, radius = 2.5.dp.toPx(), center = p)
        }

        // Draw Your Class line (Green)
        val greenPath = Path()
        yourClassPoints.forEachIndexed { idx, p ->
            if (idx == 0) greenPath.moveTo(p.x, p.y) else greenPath.lineTo(p.x, p.y)
        }
        drawPath(
            path = greenPath,
            color = VerifiedGreen,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        yourClassPoints.forEach { p ->
            drawCircle(VerifiedGreen, radius = 5.dp.toPx(), center = p)
            drawCircle(Color.White, radius = 2.5.dp.toPx(), center = p)
        }
    }
}

/**
 * Monthly Growth Bar Chart (Screen 11 - Jun to Oct)
 */
@Composable
fun MonthlyProgressBars(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(160.dp),
    barColor: Color = PrimaryBlue,
    bars: List<Pair<String, Float>> = listOf(
        "Jun" to 0.25f,
        "Jul" to 0.45f,
        "Aug" to 0.65f,
        "Sep" to 0.82f,
        "Oct" to 0.95f
    )
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { (month, heightRatio) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(fraction = heightRatio)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SecondaryBlue, barColor)
                            )
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = month,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondaryLight
                )
            }
        }
    }
}

/**
 * SHAP Factor Horizontal Bar (Screen 8)
 */
@Composable
fun ShapHorizontalBar(
    name: String,
    value: Float,
    displayValue: String
) {
    val normalized = (kotlin.math.abs(value) / 0.4f).coerceIn(0.1f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryLight,
            modifier = Modifier.width(110.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = normalized)
                    .clip(RoundedCornerShape(7.dp))
                    .background(PrimaryBlue)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = displayValue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondaryLight,
            modifier = Modifier.width(45.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * EduSuccess AI Logo Icon (Graduation Cap + Sparkles)
 */
@Composable
fun EduLogo(
    modifier: Modifier = Modifier.size(56.dp),
    tint: Color = PrimaryBlue
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.School,
            contentDescription = "EduSuccess AI Logo",
            tint = tint,
            modifier = Modifier.fillMaxSize(0.85f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(VibrantBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

/**
 * Robot Face & Soundwaves for EduVoice AI (Screen 5)
 */
@Composable
fun EduVoiceRobotAvatar(
    modifier: Modifier = Modifier.size(180.dp),
    isTalking: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "robotPulse")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Equalizer sound waves flanking the robot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left waves
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(20, 45, 70, 30, 60).forEachIndexed { index, h ->
                    val dynHeight = (h * waveScale * (0.8f + index * 0.1f)).dp.coerceIn(10.dp, 80.dp)
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(dynHeight)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.7f))
                    )
                }
            }

            // Right waves
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(60, 30, 70, 45, 20).forEachIndexed { index, h ->
                    val dynHeight = (h * waveScale * (0.8f + index * 0.1f)).dp.coerceIn(10.dp, 80.dp)
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(dynHeight)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.7f))
                    )
                }
            }
        }

        // Robot Head Center Card
        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = AccentCyan)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Robot Eyes
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(AccentCyan)
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(AccentCyan)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Robot Smiling Visor / Mouth
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                )
            }
        }
    }
}

/**
 * Airplane & Clouds Illustration (Offline Mode - Screen 12)
 */
@Composable
fun AirplaneOfflineIllustration(modifier: Modifier = Modifier.size(180.dp)) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft blue cloud circles
            drawCircle(Color(0xFFE2E8F0).copy(alpha = 0.5f), radius = 45.dp.toPx(), center = Offset(w * 0.35f, h * 0.65f))
            drawCircle(Color(0xFFE2E8F0).copy(alpha = 0.7f), radius = 55.dp.toPx(), center = Offset(w * 0.55f, h * 0.6f))
            drawCircle(Color(0xFFE2E8F0).copy(alpha = 0.6f), radius = 40.dp.toPx(), center = Offset(w * 0.75f, h * 0.7f))

            // Airplane silhouette path soaring diagonally
            val planePath = Path().apply {
                moveTo(w * 0.2f, h * 0.5f)
                lineTo(w * 0.65f, h * 0.25f)
                lineTo(w * 0.85f, h * 0.2f)
                lineTo(w * 0.75f, h * 0.35f)
                lineTo(w * 0.55f, h * 0.45f)
                lineTo(w * 0.58f, h * 0.65f)
                lineTo(w * 0.50f, h * 0.55f)
                lineTo(w * 0.35f, h * 0.6f)
                close()
            }

            drawPath(
                path = planePath,
                color = PrimaryBlue
            )
        }

        // Green checkmark badge in bottom corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
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
    }
}

/**
 * Confetti & Golden Trophy Illustration (Celebration - Screen 15)
 */
@Composable
fun CelebrationTrophyIllustration(modifier: Modifier = Modifier.size(160.dp)) {
    val infiniteTransition = rememberInfiniteTransition(label = "trophyFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Confetti particles
            val confettiColors = listOf(AlertRed, PrimaryBlue, TrophyGold, VerifiedGreen, AccentPurple)
            val confettiPositions = listOf(
                Offset(w * 0.15f, h * 0.15f),
                Offset(w * 0.35f, h * 0.08f),
                Offset(w * 0.70f, h * 0.12f),
                Offset(w * 0.85f, h * 0.22f),
                Offset(w * 0.20f, h * 0.35f),
                Offset(w * 0.82f, h * 0.45f),
                Offset(w * 0.10f, h * 0.60f),
                Offset(w * 0.90f, h * 0.68f)
            )

            confettiPositions.forEachIndexed { i, pos ->
                drawCircle(
                    color = confettiColors[i % confettiColors.size],
                    radius = 4.dp.toPx(),
                    center = pos
                )
            }
        }

        // Golden Trophy
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = "Trophy",
            tint = TrophyGold,
            modifier = Modifier
                .size(90.dp)
                .offset(y = floatOffset.dp)
        )
    }
}

/**
 * Bottom Navigation Bar matching Screen 3 design
 */
@Composable
fun EduBottomNavBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onFabClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            EduNavItem(
                icon = Icons.Filled.Home,
                label = "Home",
                isSelected = selectedTab == "home",
                onClick = { onTabSelected("home") }
            )

            // Learning
            EduNavItem(
                icon = Icons.Filled.MenuBook,
                label = "Learning",
                isSelected = selectedTab == "learning",
                onClick = { onTabSelected("learning") }
            )

            // Center Blue (+) Button
            Box(
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .size(54.dp)
                    .shadow(10.dp, CircleShape, spotColor = PrimaryBlue)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
                    .clickable { onFabClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Action",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Progress
            EduNavItem(
                icon = Icons.Filled.BarChart,
                label = "Progress",
                isSelected = selectedTab == "analytics",
                onClick = { onTabSelected("analytics") }
            )

            // Profile
            EduNavItem(
                icon = Icons.Filled.Person,
                label = "Profile",
                isSelected = selectedTab == "profile",
                onClick = { onTabSelected("profile") }
            )
        }
    }
}

@Composable
fun EduNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) PrimaryBlue else TextSecondaryLight

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = tint
        )
    }
}

/**
 * Curved Wave decoration used at the bottom of login and drawer
 */
@Composable
fun CurvedWaveDecoration(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(80.dp),
    waveColor: Color = PrimaryBlue.copy(alpha = 0.25f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, h * 0.4f)
            cubicTo(
                w * 0.25f, h * 0.1f,
                w * 0.65f, h * 0.8f,
                w, h * 0.3f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(path = path, color = waveColor)
    }
}

