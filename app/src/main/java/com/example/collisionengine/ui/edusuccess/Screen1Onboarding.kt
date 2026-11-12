package com.example.collisionengine.ui.edusuccess

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.ui.theme.AccentCyan
import com.example.collisionengine.ui.theme.PrimaryBlue
import com.example.collisionengine.ui.theme.VibrantBlue

@Composable
fun Screen1Onboarding(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F3E8F),
                        Color(0xFF1976D2),
                        Color(0xFF0D47A1)
                    )
                )
            )
    ) {
        // Glowing celestial rays and academy background canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Golden / Cyan light rays radiating from upper center
            val rayCenter = Offset(w * 0.5f, h * 0.38f)
            for (angle in 0..12) {
                val rad = Math.toRadians((angle * 30).toDouble())
                val endX = rayCenter.x + (w * 0.8f * Math.cos(rad)).toFloat()
                val endY = rayCenter.y + (h * 0.8f * Math.sin(rad)).toFloat()
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = rayCenter,
                    end = Offset(endX, endY),
                    strokeWidth = 35.dp.toPx()
                )
            }

            // Glowing castle / academy silhouette in midground
            val castlePath = Path().apply {
                moveTo(w * 0.2f, h * 0.58f)
                lineTo(w * 0.2f, h * 0.44f)
                lineTo(w * 0.25f, h * 0.40f)
                lineTo(w * 0.30f, h * 0.44f)
                lineTo(w * 0.45f, h * 0.42f)
                lineTo(w * 0.50f, h * 0.36f) // Central high spire
                lineTo(w * 0.55f, h * 0.42f)
                lineTo(w * 0.70f, h * 0.44f)
                lineTo(w * 0.75f, h * 0.40f)
                lineTo(w * 0.80f, h * 0.44f)
                lineTo(w * 0.80f, h * 0.58f)
                close()
            }
            drawPath(path = castlePath, color = Color(0xFF1E3A8A).copy(alpha = 0.8f))

            // Winding glowing blue path leading to the academy
            val roadPath = Path().apply {
                moveTo(w * 0.1f, h)
                cubicTo(w * 0.25f, h * 0.85f, w * 0.4f, h * 0.75f, w * 0.48f, h * 0.58f)
                lineTo(w * 0.52f, h * 0.58f)
                cubicTo(w * 0.6f, h * 0.75f, w * 0.75f, h * 0.85f, w * 0.9f, h)
                close()
            }
            drawPath(
                path = roadPath,
                brush = Brush.verticalGradient(
                    colors = listOf(AccentCyan.copy(alpha = 0.9f), Color(0xFF38BDF8), Color(0xFF1E40AF)),
                    startY = h * 0.58f,
                    endY = h
                )
            )

            // Students with backpacks walking forward silhouette
            // Student 1 (Left - boy in blue backpack)
            drawCircle(Color(0xFF0F172A), radius = 12.dp.toPx(), center = Offset(w * 0.26f, h * 0.76f))
            drawRoundRect(
                Color(0xFF1E3A8A),
                topLeft = Offset(w * 0.23f, h * 0.78f),
                size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 40.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )

            // Student 2 (Center - girl with light blue bag)
            drawCircle(Color(0xFF0F172A), radius = 12.dp.toPx(), center = Offset(w * 0.48f, h * 0.73f))
            drawRoundRect(
                Color(0xFF38BDF8),
                topLeft = Offset(w * 0.45f, h * 0.75f),
                size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 44.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )

            // Student 3 (Right - student with backpack)
            drawCircle(Color(0xFF0F172A), radius = 12.dp.toPx(), center = Offset(w * 0.70f, h * 0.76f))
            drawRoundRect(
                Color(0xFF1E3A8A),
                topLeft = Offset(w * 0.67f, h * 0.78f),
                size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 40.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
        }

        // Top Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Top Brand Logo
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "EduSuccess Logo",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "EduSuccess AI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Understand Every Student.\nTeach Every Gap.\nKeep Every Student on Track.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Bottom CTA Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0F172A)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

