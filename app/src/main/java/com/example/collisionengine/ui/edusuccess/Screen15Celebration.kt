package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.ui.theme.*

@Composable
fun Screen15Celebration(
    onContinueLearning: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy & Confetti Animation
                CelebrationTrophyIllustration(modifier = Modifier.size(200.dp))

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "You're on Track!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Your hard work is building\na brighter future.",
                    fontSize = 16.sp,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onContinueLearning,
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
                            text = "Continue Learning",
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

            // Neoclassical University campus building illustration at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .navigationBarsPadding()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Sky gradient
                    drawRect(Color(0xFFE2E8F0).copy(alpha = 0.2f))

                    // Green lawn
                    drawRect(
                        Color(0xFF86EFAC).copy(alpha = 0.3f),
                        topLeft = Offset(0f, h * 0.85f),
                        size = androidx.compose.ui.geometry.Size(w, h * 0.15f)
                    )

                    // Neoclassical campus building facade
                    val buildingLeft = w * 0.15f
                    val buildingRight = w * 0.85f
                    val buildingWidth = buildingRight - buildingLeft
                    val buildingTop = h * 0.35f
                    val buildingHeight = h * 0.5f

                    // Base building block
                    drawRect(
                        Color.White,
                        topLeft = Offset(buildingLeft, buildingTop),
                        size = androidx.compose.ui.geometry.Size(buildingWidth, buildingHeight)
                    )

                    // Pillars
                    val pillarCount = 6
                    val pillarSpacing = buildingWidth / (pillarCount + 1)
                    for (i in 1..pillarCount) {
                        drawRect(
                            Color(0xFFCBD5E1),
                            topLeft = Offset(buildingLeft + i * pillarSpacing - 4.dp.toPx(), buildingTop + 15.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(8.dp.toPx(), buildingHeight - 15.dp.toPx())
                        )
                    }

                    // Triangular pediment
                    val pedimentPath = Path().apply {
                        moveTo(w * 0.35f, buildingTop)
                        lineTo(w * 0.5f, h * 0.15f) // Peak
                        lineTo(w * 0.65f, buildingTop)
                        close()
                    }
                    drawPath(path = pedimentPath, color = Color(0xFFF1F5F9))

                    // Central dome
                    drawArc(
                        Color(0xFFCBD5E1),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.45f, h * 0.05f),
                        size = androidx.compose.ui.geometry.Size(w * 0.1f, h * 0.2f)
                    )

                    // Small flagpole
                    drawLine(
                        Color(0xFF64748B),
                        start = Offset(w * 0.5f, h * 0.05f),
                        end = Offset(w * 0.5f, 0f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}

