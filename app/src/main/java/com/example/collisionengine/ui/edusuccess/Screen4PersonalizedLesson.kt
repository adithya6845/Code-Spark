package com.example.collisionengine.ui.edusuccess

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*

@Composable
fun Screen4PersonalizedLesson(
    onNavigateBack: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val studentId = EduDataRepository.selectedStudentId
    val isCoreLoaded = EduDataRepository.isCoreLoaded
    val isLogsLoaded = EduDataRepository.isLogsLoaded

    val weakConcepts = remember(studentId, isCoreLoaded, isLogsLoaded) {
        EduDataRepository.weakConceptsForStudent(studentId, limit = 6)
    }

    var selectedConceptId by remember { mutableStateOf("") }
    val activeConcept = remember(selectedConceptId, weakConcepts, isCoreLoaded) {
        EduDataRepository.getConceptById(selectedConceptId)
            ?: weakConcepts.firstOrNull()?.first
            ?: EduDataRepository.concepts.firstOrNull()
    }
    val conceptId = activeConcept?.conceptId ?: "C02"

    var isBookmarked by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Summary") }
    val tabs = listOf("Summary", "Examples", "Practice")

    // Worked assignment example & MCQ practice questions from datasets
    val matchingAssignments = remember(conceptId, isCoreLoaded) {
        EduDataRepository.assignments.filter { it.conceptId == conceptId }
    }
    val practiceQuestions = remember(conceptId, isCoreLoaded) {
        EduDataRepository.getQuestionsForConcept(conceptId).take(3)
    }
    var selectedAnswers by remember(conceptId) { mutableStateOf(mapOf<String, String>()) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            EduTopBar(
                title = "Personalized Lesson",
                onBackClick = onNavigateBack,
                trailingIcon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                onTrailingClick = { isBookmarked = !isBookmarked }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                color = Color.Transparent
            ) {
                Button(
                    onClick = onStartQuiz,
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
                            text = "Start Quiz",
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
            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal selector for student's weak concepts from quiz_attempts.csv + concepts.csv
            if (weakConcepts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weakConcepts.forEach { (concept, accPct) ->
                        val isSel = concept.conceptId == conceptId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSel) PrimaryBlue else Color(0xFFE2E8F0))
                                .clickable { selectedConceptId = concept.conceptId }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${concept.topic} ($accPct%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSel) Color.White else TextPrimaryLight
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Topic Name
            Text(
                text = activeConcept?.topic ?: "Factorization",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(6.dp))

            // "Your weak concept" badge + Subject/Unit info
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEDE9FE))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Your weak concept",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentPurple
                    )
                }
                if (activeConcept != null) {
                    Text(
                        text = "${activeConcept.subject} • ${activeConcept.unit}",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Video Player Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        drawRect(Color(0xFFE8EEF5))
                        for (i in 0..6) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(0f, i * h / 6),
                                end = Offset(w, i * h / 6),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        drawCircle(Color(0xFF1E293B), radius = 22.dp.toPx(), center = Offset(w * 0.78f, h * 0.42f))
                        drawRoundRect(
                            Color(0xFF1976D2),
                            topLeft = Offset(w * 0.70f, h * 0.54f),
                            size = androidx.compose.ui.geometry.Size(48.dp.toPx(), 60.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )
                    }

                    val previewEquation = when (conceptId) {
                        "C04", "C05", "C06", "C07" -> "|A| = ad - bc"
                        "C14", "C15", "C16" -> "F = ma, KE = ½mv²"
                        "C17", "C18" -> "V = I × R"
                        else -> "a² - b² = (a+b)(a-b)"
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp, end = 90.dp)
                    ) {
                        Text(
                            text = previewEquation,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeConcept?.unit ?: "Algebra",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { selectedTab = "Examples" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "04:32",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Segmented Tabs: Summary | Examples | Practice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryBlue else TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(PrimaryBlue)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                "Summary" -> {
                    val prereq = activeConcept?.prerequisiteId
                        ?.takeIf { it.isNotBlank() }
                        ?.let { EduDataRepository.getConceptById(it)?.topic }
                    LessonCheckItem(title = "What is ${activeConcept?.topic ?: "Factorization"}?")
                    Spacer(modifier = Modifier.height(10.dp))
                    LessonCheckItem(
                        title = if (prereq != null) "Prerequisite: $prereq" else "Core Formulas & Methods"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LessonCheckItem(title = "Common Misconceptions & Step-by-Step Fixes")
                }

                "Examples" -> {
                    if (matchingAssignments.isEmpty()) {
                        LessonCheckItem(title = "Example: Solve x² - 7x + 12 = 0 -> x = 3, 4")
                    } else {
                        matchingAssignments.forEach { ex ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${ex.assignmentTitle} • Q${ex.questionNo}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = ex.question,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimaryLight
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SoftGreen)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "Reference Answer: ${ex.referenceAnswer}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "Practice" -> {
                    practiceQuestions.forEachIndexed { idx, q ->
                        val chosen = selectedAnswers[q.questionId]
                        val options = listOf("A" to q.optionA, "B" to q.optionB, "C" to q.optionC, "D" to q.optionD)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Q${idx + 1} (${q.difficulty}) • ${q.questionId}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = q.question,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimaryLight
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                options.forEach { (letter, optText) ->
                                    val isChosen = chosen == letter
                                    val isCorrect = letter == q.correctOption
                                    val bg = when {
                                        chosen == null -> Color(0xFFF8FAFC)
                                        isChosen && isCorrect -> SoftGreen
                                        isChosen && !isCorrect -> SoftRed
                                        isCorrect -> SoftGreen.copy(alpha = 0.6f)
                                        else -> Color(0xFFF8FAFC)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(bg)
                                            .border(
                                                1.dp,
                                                if (isChosen) PrimaryBlue else Color(0xFFE2E8F0),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                selectedAnswers = selectedAnswers + (q.questionId to letter)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "$letter. $optText",
                                            fontSize = 13.sp,
                                            color = TextPrimaryLight,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                                if (chosen != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (chosen == q.correctOption) {
                                        Text(
                                            text = "Correct! ${q.correctAnswer}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = VerifiedGreen
                                        )
                                    } else {
                                        val hint = q.distractorMisconceptions.split(";")
                                            .find { it.trim().startsWith("$chosen:") }
                                            ?.substringAfter(":")
                                            ?.replace("_", " ")
                                            ?: "check steps carefully"
                                        Text(
                                            text = "Misconception detected: $hint (Correct: ${q.correctOption})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AlertRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LessonCheckItem(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight
            )
        }
    }
}
