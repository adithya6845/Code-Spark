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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun Screen14DrawerMenu(
    selectedRoute: String = "Dashboard",
    onNavigate: (route: String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val activeStudent = EduDataRepository.getStudent()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Header: EduSuccess AI
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                EduLogo(modifier = Modifier.size(38.dp), tint = AccentCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "EduSuccess AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // User Profile Card (Adithya Kumar, Student ∨)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF1E293B))
                    .clickable { onNavigate("my_progress") }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(2.dp, AccentCyan, CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = activeStudent?.name ?: "Adithya Kumar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (activeStudent != null) "Student • ${activeStudent.classId} (${activeStudent.rollNo})" else "Student",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Menu Items List
            DrawerMenuItem(
                icon = Icons.Filled.Dashboard,
                title = "Dashboard",
                isSelected = selectedRoute == "Dashboard",
                onClick = { onNavigate("student_home") }
            )

            DrawerMenuItem(
                icon = Icons.Filled.MenuBook,
                title = "My Learning",
                isSelected = selectedRoute == "My Learning",
                onClick = { onNavigate("lesson") }
            )

            DrawerMenuItem(
                icon = Icons.Filled.Assignment,
                title = "Assignments",
                isSelected = selectedRoute == "Assignments",
                onClick = { onNavigate("ai_grading") }
            )

            DrawerMenuItem(
                icon = Icons.Filled.Mic,
                title = "EduVoice Tutor",
                isSelected = selectedRoute == "EduVoice Tutor",
                onClick = { onNavigate("edu_voice") }
            )

            DrawerMenuItem(
                icon = Icons.Filled.BarChart,
                title = "My Progress",
                isSelected = selectedRoute == "My Progress",
                onClick = { onNavigate("my_progress") }
            )

            DrawerMenuItem(
                icon = Icons.Filled.SmartToy,
                title = "Campus Connect AI (Genie)",
                isSelected = selectedRoute == "Campus Connect AI",
                onClick = { onNavigate("research") }
            )

            DrawerMenuItem(
                icon = Icons.Filled.Settings,
                title = "Settings",
                isSelected = selectedRoute == "Settings",
                onClick = { onNavigate("classroom_analytics") }
            )

            DrawerMenuItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Logout",
                isSelected = false,
                onClick = { onNavigate("sign_in") }
            )

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Curved wave decoration at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            CurvedWaveDecoration(
                waveColor = Color(0xFF1E3A8A).copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimaryBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFFE2E8F0)
            )
        }
    }
}

