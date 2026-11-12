package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.ui.theme.PrimaryBlue
import com.example.collisionengine.ui.theme.TextPrimaryLight
import com.example.collisionengine.ui.theme.TextSecondaryLight

data class ScreenItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val badge: String
)

val all15Screens = listOf(
    ScreenItem("onboarding", "1. Splash & Start", Icons.Filled.School, "Row 1"),
    ScreenItem("sign_in", "2. Welcome & Sign In", Icons.Filled.Lock, "Row 1"),
    ScreenItem("student_home", "3. Home Dashboard", Icons.Filled.Dashboard, "Row 1"),
    ScreenItem("lesson", "4. Personalized Lesson", Icons.Filled.PlayCircle, "Row 1"),
    ScreenItem("edu_voice", "5. EduVoice AI", Icons.Filled.Mic, "Row 1"),
    ScreenItem("ai_grading", "6. AI Grading Results", Icons.Filled.AssignmentTurnedIn, "Row 2"),
    ScreenItem("classroom_analytics", "7. Teacher Portal", Icons.Filled.Analytics, "Row 2"),
    ScreenItem("student_risk", "8. Student Risk Analysis", Icons.Filled.Warning, "Row 2"),
    ScreenItem("intervention_copilot", "9. Counsellor / Interventions", Icons.Filled.Lightbulb, "Row 2"),
    ScreenItem("institution_management", "10. Admin Dashboard", Icons.Filled.AccountBalance, "Row 2"),
    ScreenItem("my_progress", "11. My Progress", Icons.Filled.TrendingUp, "Row 3"),
    ScreenItem("offline_mode", "12. Offline Mode", Icons.Filled.Flight, "Row 3"),
    ScreenItem("prediction_report", "13. Prediction Report", Icons.Filled.CheckCircle, "Row 3"),
    ScreenItem("drawer_menu", "14. Navigation Drawer", Icons.Filled.Menu, "Row 3"),
    ScreenItem("celebration", "15. You're on Track!", Icons.Filled.EmojiEvents, "Row 3"),
    ScreenItem("research", "Campus Connect AI", Icons.Filled.SmartToy, "Genie")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSwitcherModal(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EduSuccess AI Screen Gallery",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = "Select any of the 15 screens to view & interact",
                        fontSize = 13.sp,
                        color = TextSecondaryLight
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                items(all15Screens) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigate(item.route)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryLight,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.badge,
                                fontSize = 10.sp,
                                color = TextSecondaryLight,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

