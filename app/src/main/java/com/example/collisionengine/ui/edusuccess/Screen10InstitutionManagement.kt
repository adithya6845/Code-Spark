package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun Screen10InstitutionManagement(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("dashboard") } // "dashboard" | "institutions"
    var showAddDialog by remember { mutableStateOf(false) }
    var newCollegeName by remember { mutableStateOf("") }
    var newLocation by remember { mutableStateOf("Bengaluru") }

    val institutionList = remember { mutableStateListOf(*EduMockData.institutions.toTypedArray()) }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            AdminBottomNavBar(
                selectedTab = if (selectedTab == "dashboard") "home" else "manage",
                onTabSelected = { tab ->
                    selectedTab = if (tab == "home") "dashboard" else "institutions"
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

            // ── Top Header matching Image 3 (Crown + Admin Dashboard + Bell) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryLight
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Filled.MilitaryTech, // Crown / Gold medal
                        contentDescription = "Admin",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Admin Dashboard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { },
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

            // ── 3 Metric Cards matching Image 3 (5.2K Students | 320 Teachers | 18 Institutions) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    value = "5.2K",
                    label = "Total Students",
                    onClick = {}
                )
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    value = "320",
                    label = "Teachers",
                    onClick = {}
                )
                AdminMetricCard(
                    modifier = Modifier.weight(1f),
                    value = "18",
                    label = "Institutions",
                    onClick = { selectedTab = "institutions" }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Platform Analytics Card matching Image 3 ──
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
                            text = "Platform Analytics",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        Text(
                            text = "This Month ∨",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend: Active Users (Blue) and Course Completion (Teal)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active Users",
                            fontSize = 11.sp,
                            color = TextSecondaryLight,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF14B8A6))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Course Completion",
                            fontSize = 11.sp,
                            color = TextSecondaryLight,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Line Graph across 1st, 7th, 14th, 21st, 30th
                    PlatformAnalyticsChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Manage Section matching Image 3 (Users | Curriculum | Institutions | Settings) ──
            Text(
                text = "Manage",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminManageCard(
                    modifier = Modifier.weight(1f),
                    title = "Users",
                    icon = Icons.Filled.Group,
                    onClick = {}
                )
                AdminManageCard(
                    modifier = Modifier.weight(1f),
                    title = "Curriculum",
                    icon = Icons.Filled.MenuBook,
                    onClick = {}
                )
                AdminManageCard(
                    modifier = Modifier.weight(1f),
                    title = "Institutions",
                    icon = Icons.Filled.AccountBalance,
                    onClick = { showAddDialog = true }
                )
                AdminManageCard(
                    modifier = Modifier.weight(1f),
                    title = "Settings",
                    icon = Icons.Filled.Settings,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Institutions Roster preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connected Institutions (${institutionList.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "+ Add",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { showAddDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            institutionList.take(3).forEach { inst ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inst.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Text(
                                text = "${inst.location} • ${inst.studentCount}",
                                fontSize = 12.sp,
                                color = TextSecondaryLight
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Institution", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCollegeName,
                        onValueChange = { newCollegeName = it },
                        label = { Text("Institution Name") },
                        placeholder = { Text("e.g. BMS College of Engineering") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newLocation,
                        onValueChange = { newLocation = it },
                        label = { Text("Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCollegeName.isNotBlank()) {
                            institutionList.add(
                                0,
                                Institution(
                                    name = newCollegeName,
                                    location = newLocation,
                                    studentCount = "1,800 Students"
                                )
                            )
                            newCollegeName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Add Institution")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminMetricCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
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
                .padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimaryLight
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

@Composable
private fun AdminManageCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
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
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight
            )
        }
    }
}

@Composable
private fun PlatformAnalyticsChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val paddingLeft = 32.dp.toPx()
        val paddingBottom = 22.dp.toPx()
        val graphW = w - paddingLeft - 16.dp.toPx()
        val graphH = h - paddingBottom - 10.dp.toPx()

        // Horizontal guides
        for (i in 0..3) {
            val y = graphH - (i * graphH / 3) + 10.dp.toPx()
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(paddingLeft, y),
                end = Offset(w - 16.dp.toPx(), y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 5 Days: 1st, 7th, 14th, 21st, 30th
        val userRatios = listOf(0.35f, 0.50f, 0.65f, 0.75f, 0.88f)
        val courseRatios = listOf(0.20f, 0.38f, 0.52f, 0.62f, 0.72f)
        val stepX = graphW / (userRatios.size - 1)

        val userPoints = userRatios.mapIndexed { idx, r ->
            Offset(paddingLeft + (idx * stepX), graphH - (r * graphH) + 10.dp.toPx())
        }
        val coursePoints = courseRatios.mapIndexed { idx, r ->
            Offset(paddingLeft + (idx * stepX), graphH - (r * graphH) + 10.dp.toPx())
        }

        // Draw Active Users curve (Blue)
        val userPath = Path()
        userPoints.forEachIndexed { idx, p ->
            if (idx == 0) userPath.moveTo(p.x, p.y) else userPath.lineTo(p.x, p.y)
        }
        drawPath(
            path = userPath,
            color = Color(0xFF2563EB),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
        userPoints.forEach { drawCircle(Color(0xFF2563EB), radius = 3.5.dp.toPx(), center = it) }

        // Draw Course Completion curve (Teal)
        val coursePath = Path()
        coursePoints.forEachIndexed { idx, p ->
            if (idx == 0) coursePath.moveTo(p.x, p.y) else coursePath.lineTo(p.x, p.y)
        }
        drawPath(
            path = coursePath,
            color = Color(0xFF14B8A6),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
        coursePoints.forEach { drawCircle(Color(0xFF14B8A6), radius = 3.5.dp.toPx(), center = it) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("1st", "7th", "14th", "21st", "30th").forEach { day ->
            Text(text = day, fontSize = 11.sp, color = TextSecondaryLight)
        }
    }
}

@Composable
private fun AdminBottomNavBar(
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
            AdminNavItem(
                title = "Home",
                icon = Icons.Filled.Home,
                isSelected = selectedTab == "home",
                onClick = { onTabSelected("home") }
            )
            AdminNavItem(
                title = "Manage",
                icon = Icons.Filled.DashboardCustomize,
                isSelected = selectedTab == "manage",
                onClick = { onTabSelected("manage") }
            )
            AdminNavItem(
                title = "Analytics",
                icon = Icons.Filled.BarChart,
                isSelected = selectedTab == "analytics",
                onClick = { onTabSelected("analytics") }
            )
            AdminNavItem(
                title = "Profile",
                icon = Icons.Filled.Person,
                isSelected = selectedTab == "profile",
                onClick = { onTabSelected("profile") }
            )
        }
    }
}

@Composable
private fun AdminNavItem(
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
