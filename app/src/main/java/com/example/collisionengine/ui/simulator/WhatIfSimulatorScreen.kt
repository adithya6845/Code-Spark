package com.example.collisionengine.ui.simulator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.models.Student
import com.example.collisionengine.ui.components.bouncyClickable
import com.example.collisionengine.ui.theme.*

private val ScreenBackground = BackgroundLight
private val SurfaceWhite = Color.White
private val BorderStrokeColor = Color(0xFFE2E8F0)
private val SubCardBackground = Color(0xFFF8FAFC)
private val SubCardBorder = Color(0xFFE8EEF5)

private val TextMain = TextPrimaryLight
private val TextSub = TextSecondaryLight
private val BluePrimary = PrimaryBlue
private val BlueSecondary = SecondaryBlue
private val VioletAccent = Color(0xFF8B5CF6)
private val EmeraldGreen = Color(0xFF10B981)
private val WarningAmber = Color(0xFFFF9500)
private val PinkAccent = Color(0xFFF43F5E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatIfSimulatorScreen(
    viewModel: WhatIfSimulatorViewModel,
    onNavigateBack: () -> Unit
) {
    val promptQuery by viewModel.promptQuery.collectAsState()
    val selectedCollaborators by viewModel.selectedCollaborators.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val showAddPersonSheet by viewModel.showAddPersonSheet.collectAsState()
    val showFullSimulationDialog by viewModel.showFullSimulationDialog.collectAsState()
    val availableStudents by viewModel.availableStudents.collectAsState()
    val searchStudentQuery by viewModel.searchStudentQuery.collectAsState()

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨ ",
                                fontSize = 18.sp
                            )
                            Text(
                                text = "What If Simulator",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        }
                        Text(
                            text = "Decision engine of Campus Connect AI",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSub,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(38.dp)
                            .shadow(2.dp, CircleShape)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextMain,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScreenBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ScreenBackground)
        ) {
            // Top Ambient Gradient matching Home Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientTop, ScreenBackground)
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 36.dp)
            ) {
                // 1. Natural Language Prompt Bar
                item {
                    SimulatorPromptBar(
                        prompt = promptQuery,
                        onPromptChange = { viewModel.onPromptChange(it) },
                        onRunSimulation = { viewModel.runSimulation() },
                        isAnalyzing = isAnalyzing
                    )
                }

                // 2. Selected Collaborators Section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Selected Collaborators",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextMain,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(selectedCollaborators, key = { it.id }) { collaborator ->
                                CollaboratorCard(
                                    collaborator = collaborator,
                                    onRemove = { viewModel.removeCollaborator(collaborator.id) }
                                )
                            }
                            item {
                                AddPersonCard(
                                    onClick = { viewModel.setShowAddPersonSheet(true) }
                                )
                            }
                        }
                    }
                }

                // 3. Genie Analysis Result Card
                item {
                    analysisResult?.let { result ->
                        GenieAnalysisResultSection(result = result)
                    }
                }

                // 4. Potential Outcomes Section
                item {
                    analysisResult?.let { result ->
                        PotentialOutcomesSection(outcomes = result.potentialOutcomes)
                    }
                }

                // 5. Suggested Project Idea Section
                item {
                    analysisResult?.let { result ->
                        SuggestedProjectIdeaCard(project = result.suggestedProject)
                    }
                }

                // 6. Explore Full Simulation Button
                item {
                    ExploreFullSimulationButton(
                        onClick = { viewModel.setShowFullSimulationDialog(true) }
                    )
                }
            }

            // Collaborator Picker Modal Sheet
            if (showAddPersonSheet) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.setShowAddPersonSheet(false) },
                    containerColor = SurfaceWhite,
                    scrimColor = Color.Black.copy(alpha = 0.5f),
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .width(40.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCBD5E1))
                        )
                    }
                ) {
                    CollaboratorPickerSheet(
                        searchQuery = searchStudentQuery,
                        onSearchQueryChange = { viewModel.onSearchStudentChange(it) },
                        students = availableStudents,
                        onStudentSelected = { viewModel.addCollaboratorFromStudent(it) }
                    )
                }
            }

            // Full Simulation Deep Dive Dialog
            if (showFullSimulationDialog) {
                analysisResult?.let { result ->
                    FullSimulationDetailDialog(
                        result = result,
                        onDismiss = { viewModel.setShowFullSimulationDialog(false) }
                    )
                }
            }
        }
    }
}

@Composable
fun SimulatorPromptBar(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onRunSimulation: () -> Unit,
    isAnalyzing: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = BluePrimary.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderStrokeColor, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Sparkle icon
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Editable Text Field
            BasicTextField(
                value = prompt,
                onValueChange = onPromptChange,
                textStyle = TextStyle(
                    color = TextMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 19.sp
                ),
                cursorBrush = SolidColor(BluePrimary),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                decorationBox = { innerTextField ->
                    if (prompt.isEmpty()) {
                        Text(
                            text = "Ask any 'What if' scenario...",
                            color = TextSub.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            // Circular Gradient Run Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape, spotColor = BluePrimary.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BluePrimary, BlueSecondary)
                        )
                    )
                    .bouncyClickable { onRunSimulation() },
                contentAlignment = Alignment.Center
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Run Simulation",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CollaboratorCard(
    collaborator: SimulatorCollaborator,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(172.dp)
            .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = BluePrimary.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderStrokeColor, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar with Online dot
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BluePrimary.copy(alpha = 0.15f), GradientTop)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = collaborator.avatarInitials,
                        color = BluePrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (collaborator.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                            .border(1.5.dp, SurfaceWhite, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collaborator.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = collaborator.roleTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSub,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Tag pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BluePrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = collaborator.primarySkillTag,
                        color = BluePrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Close button
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = TextSub,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun AddPersonCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(84.dp)
            .height(78.dp)
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = BluePrimary.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .border(
                width = 1.dp,
                brush = SolidColor(Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(18.dp)
            )
            .bouncyClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Person",
                tint = BluePrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Add\nPerson",
                color = TextSub,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun GenieAnalysisResultSection(result: SimulationAnalysisResult) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = BluePrimary.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderStrokeColor, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BluePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Genie Analysis Result",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Based on campus data & past collaborations",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSub,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6 Grid Cards (2 x 3)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: Skill Coverage & Project Feasibility
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.TrackChanges,
                        iconTint = VioletAccent,
                        title = "Skill Coverage",
                        value = "${result.skillCoveragePercent}%",
                        valueColor = EmeraldGreen,
                        subtitle = "${result.coveredSkillsCount} / ${result.totalSkillsCount} Skills Covered"
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Outlined.TrendingUp,
                        iconTint = BluePrimary,
                        title = "Project Feasibility",
                        value = "${result.projectFeasibilityPercent}%",
                        valueColor = BluePrimary,
                        subtitle = result.feasibilityLabel
                    )
                }

                // Row 2: Collaboration Strength & Missing Skill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Groups,
                        iconTint = VioletAccent,
                        title = "Collaboration Strength",
                        value = "${result.collaborationStrengthPercent}%",
                        valueColor = VioletAccent,
                        subtitle = result.collaborationLabel
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.WarningAmber,
                        iconTint = WarningAmber,
                        title = "Missing Skill",
                        value = result.missingSkill,
                        valueColor = TextMain,
                        isLargeValue = false,
                        subtitle = result.missingSkillRecommendation
                    )
                }

                // Row 3: Relevant Faculty & Related Research
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.PersonOutline,
                        iconTint = BluePrimary,
                        title = "Relevant Faculty",
                        value = result.relevantFaculty,
                        valueColor = TextMain,
                        isLargeValue = false,
                        subtitle = result.facultyDepartment
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        iconTint = PinkAccent,
                        title = "Related Research",
                        value = "${result.relatedResearchCount}",
                        valueColor = PinkAccent,
                        subtitle = result.researchLabel
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    valueColor: Color,
    subtitle: String,
    isLargeValue: Boolean = true
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 115.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SubCardBackground)
            .border(1.dp, SubCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSub,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = valueColor,
                fontSize = if (isLargeValue) 26.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSub,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PotentialOutcomesSection(outcomes: List<OutcomeItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = EmeraldGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Potential Outcomes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            outcomes.forEach { outcome ->
                val iconVector = when (outcome.iconType) {
                    OutcomeIconType.HACKATHON -> Icons.Filled.EmojiEvents
                    OutcomeIconType.RESEARCH_PROJECT -> Icons.AutoMirrored.Filled.MenuBook
                    OutcomeIconType.STARTUP_IDEA -> Icons.Filled.RocketLaunch
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(82.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = BluePrimary.copy(alpha = 0.06f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite)
                        .border(1.dp, BorderStrokeColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = Color(outcome.colorHex),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = outcome.title,
                                color = TextMain,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = outcome.potentialText,
                            color = Color(outcome.colorHex),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestedProjectIdeaCard(project: ProjectIdea) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = BluePrimary.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderStrokeColor, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Suggested Project Idea",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        fontSize = 14.5.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSub,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Custom Vector Graphic
                CampusTrafficGraphic(
                    modifier = Modifier
                        .size(width = 100.dp, height = 80.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }
        }
    }
}

@Composable
fun CampusTrafficGraphic(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Isometric grid road lines
            val roadPath = Path().apply {
                moveTo(w * 0.1f, h * 0.7f)
                lineTo(w * 0.9f, h * 0.2f)
                moveTo(w * 0.2f, h * 0.2f)
                lineTo(w * 0.8f, h * 0.8f)
            }
            drawPath(
                path = roadPath,
                color = Color(0xFF334155),
                style = Stroke(width = 6f)
            )

            // Miniature 3D campus building blocks
            drawRect(
                color = Color(0xFF475569),
                topLeft = Offset(w * 0.65f, h * 0.15f),
                size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.35f)
            )
            drawRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(w * 0.15f, h * 0.45f),
                size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.35f)
            )

            // Glowing IoT Nodes / Sensor beacons
            drawCircle(
                color = EmeraldGreen,
                radius = 4f,
                center = Offset(w * 0.35f, h * 0.35f)
            )
            drawCircle(
                color = BluePrimary,
                radius = 4f,
                center = Offset(w * 0.7f, h * 0.6f)
            )

            // Glowing Purple Location Pin
            val pinCenter = Offset(w * 0.5f, h * 0.45f)
            drawCircle(
                color = VioletAccent.copy(alpha = 0.35f),
                radius = 12f,
                center = pinCenter
            )
            drawCircle(
                color = VioletAccent,
                radius = 6f,
                center = pinCenter
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = pinCenter
            )
        }
    }
}

@Composable
fun ExploreFullSimulationButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = BluePrimary.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(BluePrimary, VioletAccent)
                )
            )
            .bouncyClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✨",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Explore Full Simulation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaboratorPickerSheet(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    students: List<Student>,
    onStudentSelected: (Student) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .heightIn(max = 520.dp)
    ) {
        Text(
            text = "Add Collaborator",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )
        Text(
            text = "Select campus peers to simulate team chemistry",
            style = MaterialTheme.typography.bodySmall,
            color = TextSub
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Box
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search by name, skill, department...", color = TextSub) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSub) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SubCardBackground,
                unfocusedContainerColor = SubCardBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(students) { student ->
                val name = student.name ?: "Peer"
                val skills = student.skills ?: ""
                val dept = student.department ?: "Engineering"
                val year = if (!student.year.isNullOrBlank()) "Year ${student.year}" else ""

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SubCardBackground)
                        .border(1.dp, BorderStrokeColor, RoundedCornerShape(14.dp))
                        .clickable { onStudentSelected(student) }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BluePrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(2).uppercase(),
                                color = BluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                            Text(
                                text = "$dept • $year",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSub,
                                fontSize = 11.5.sp
                            )
                            if (skills.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = skills,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BluePrimary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Filled.AddCircleOutline,
                            contentDescription = "Add",
                            tint = BluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullSimulationDetailDialog(
    result: SimulationAnalysisResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        titleContentColor = TextMain,
        textContentColor = TextSub,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "✨ ", fontSize = 20.sp)
                Text(
                    text = "Full Simulation Report",
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Skill Coverage Matrix
                item {
                    Text(
                        text = "Skill Coverage Breakdown",
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        fontSize = 14.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    result.skillDetails.forEach { skill ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (skill.isCovered) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = if (skill.isCovered) EmeraldGreen else WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = skill.skillName,
                                    color = TextMain,
                                    fontSize = 12.5.sp
                                )
                            }
                            Text(
                                text = skill.coveredBy ?: "Missing (Need Recruit)",
                                color = if (skill.isCovered) BluePrimary else WarningAmber,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Section 2: 4-Phase Roadmap
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Execution Roadmap",
                        fontWeight = FontWeight.Bold,
                        color = VioletAccent,
                        fontSize = 14.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    result.roadmap.forEach { phase ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SubCardBackground)
                                .border(1.dp, BorderStrokeColor, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Phase ${phase.phaseNumber}: ${phase.title}",
                                        fontWeight = FontWeight.Bold,
                                        color = TextMain,
                                        fontSize = 12.5.sp
                                    )
                                    Text(
                                        text = phase.duration,
                                        color = BluePrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = phase.milestone,
                                    color = TextSub,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }

                // Section 3: Matched Research Papers
                if (result.matchedPapers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Matching Campus Research Papers (${result.matchedPapers.size})",
                            fontWeight = FontWeight.Bold,
                            color = PinkAccent,
                            fontSize = 14.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        result.matchedPapers.forEach { paper ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SubCardBackground)
                                    .border(1.dp, BorderStrokeColor, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = paper.title ?: "Research Paper",
                                        fontWeight = FontWeight.Bold,
                                        color = TextMain,
                                        fontSize = 12.5.sp
                                    )
                                    Text(
                                        text = "Area: ${paper.researchArea ?: "AI"} • ${paper.publication ?: "IEEE"}",
                                        color = TextSub,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = paper.abstract ?: "",
                                        color = TextSub,
                                        fontSize = 11.5.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Databricks Genie Verification
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Databricks Genie AI Insights",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        fontSize = 14.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result.genieExplanation,
                        color = TextMain,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", color = Color.White)
            }
        }
    )
}
