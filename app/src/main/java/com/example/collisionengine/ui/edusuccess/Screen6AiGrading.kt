package com.example.collisionengine.ui.edusuccess

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.AiSubmissionAnalysis
import com.example.collisionengine.data.edusuccess.AssignmentAiGrader
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Screen6AiGrading(
    onNavigateBack: () -> Unit,
    onReviewClick: () -> Unit,
    onApproveMarks: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val studentId = EduDataRepository.selectedStudentId
    val isLogsLoaded = EduDataRepository.isLogsLoaded
    val isCoreLoaded = EduDataRepository.isCoreLoaded

    var activeMode by remember { mutableStateOf("Submit") } // "Submit" | "Results"

    // Available assignments from assignments.csv (one representative question per assignment or all)
    val assignments = remember(isCoreLoaded) {
        EduDataRepository.assignments.distinctBy { it.assignmentId }
    }
    var selectedAssignmentId by remember(assignments) {
        mutableStateOf(assignments.firstOrNull()?.assignmentId ?: "A01")
    }
    val selectedAssignment = remember(selectedAssignmentId, assignments) {
        assignments.find { it.assignmentId == selectedAssignmentId }
    }

    var uploadedBitmap by remember(selectedAssignmentId) {
        mutableStateOf<Bitmap?>(
            AssignmentAiGrader.createSampleHandwrittenSheetBitmap(selectedAssignment)
        )
    }
    var imageSourceLabel by remember(selectedAssignmentId) {
        mutableStateOf("Sample Handwritten Sheet (Tap Gallery/Camera to replace)")
    }
    var studentNotes by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var stageMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var liveAnalysis by remember { mutableStateOf<AiSubmissionAnalysis?>(null) }
    var approvedLocal by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bmp = AssignmentAiGrader.loadBitmapFromUri(context, uri)
            if (bmp != null) {
                uploadedBitmap = bmp
                imageSourceLabel = "Uploaded from Gallery"
                liveAnalysis = null
                errorMessage = null
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            uploadedBitmap = bmp
            imageSourceLabel = "Captured from Camera"
            liveAnalysis = null
            errorMessage = null
        }
    }

    // Saved historical submissions & results
    val submissions = EduDataRepository.getSubmissions(studentId)
    var selectedSubId by remember(studentId, submissions.size, isLogsLoaded) {
        mutableStateOf(
            submissions.firstOrNull()?.submissionId ?: ""
        )
    }
    val activeSubmission = submissions.find { it.submissionId == selectedSubId }
    val gradingRows = remember(selectedSubId, isLogsLoaded, submissions.size) {
        if (selectedSubId.isBlank()) emptyList() else EduDataRepository.getGradingForSubmission(selectedSubId)
    }
    val assignmentQuestions = remember(activeSubmission, isCoreLoaded) {
        activeSubmission?.let { EduDataRepository.getAssignmentItems(it.assignmentId) } ?: emptyList()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            EduTopBar(
                title = "AI Assignment OCR & Grading",
                onBackClick = onNavigateBack
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onReviewClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue)
                        )
                    ) {
                        Text(
                            text = "Practice Weak Topic",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Button(
                        onClick = {
                            approvedLocal = true
                            onApproveMarks()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = if (approvedLocal) "Marks Approved" else "Approve Marks",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
            Spacer(modifier = Modifier.height(6.dp))

            // Mode Tabs: Submit & AI Grade | Saved Results
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Submit" to "Submit & AI Grade",
                    "Results" to "Saved Results (${submissions.size})"
                ).forEach { (key, label) ->
                    val isSel = activeMode == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) PrimaryBlue else Color.Transparent)
                            .clickable { activeMode = key }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else TextPrimaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeMode == "Submit") {
                // Step 1: Select Assignment
                if (assignments.isNotEmpty()) {
                    Text(
                        text = "1. Select Assignment",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        assignments.forEach { asg ->
                            val isSel = asg.assignmentId == selectedAssignmentId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSel) PrimaryBlue else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSel) PrimaryBlue else Color(0xFFE2E8F0),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        selectedAssignmentId = asg.assignmentId
                                        liveAnalysis = null
                                        errorMessage = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "${asg.assignmentId}: ${asg.assignmentTitle}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) Color.White else TextPrimaryLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Assignment Question Card
                if (selectedAssignment != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "${selectedAssignment.subject} • Max ${selectedAssignment.maxMarks.toInt()} Marks • Due ${selectedAssignment.dueDate}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedAssignment.question,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step 2: Upload / Capture Student Handwritten Sheet
                Text(
                    text = "2. Upload Handwritten Answer Sheet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            uploadedBitmap = AssignmentAiGrader.createSampleHandwrittenSheetBitmap(selectedAssignment)
                            imageSourceLabel = "Sample Handwritten Sheet"
                            liveAnalysis = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sample", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Image Preview
                uploadedBitmap?.let { bmp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Uploaded Assignment Sheet",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = imageSourceLabel,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = studentNotes,
                    onValueChange = { studentNotes = it },
                    label = { Text("Optional student answer / working notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Step 3: Submit for Llama 3.2 Vision Instruct OCR + GPT Grading
                Button(
                    onClick = {
                        val bmp = uploadedBitmap
                        val asg = selectedAssignment
                        if (bmp != null && asg != null && !isProcessing) {
                            scope.launch {
                                isProcessing = true
                                errorMessage = null
                                stageMessage = "1/2 Scanning handwriting with Llama 3.2 Vision Instruct OCR..."

                                val ocrResult = AssignmentAiGrader.runLlamaVisionOcr(
                                    bitmap = bmp,
                                    assignment = asg,
                                    studentNotes = studentNotes
                                )

                                ocrResult.onSuccess { (transcription, observations) ->
                                    stageMessage = "2/2 Grading answers & finding strong/weak points with GPT..."
                                    val rubrics = EduDataRepository.getRubrics(asg.rubricId)
                                    val gradeResult = AssignmentAiGrader.gradeWithOpenAi(
                                        assignment = asg,
                                        rubrics = rubrics,
                                        ocrTranscription = transcription,
                                        ocrObservations = observations
                                    )
                                    gradeResult.onSuccess { report ->
                                        liveAnalysis = report
                                        val newSubId = EduDataRepository.recordLiveAiSubmission(
                                            studentId = studentId,
                                            assignment = asg,
                                            score = report.score,
                                            feedback = report.summaryFeedback
                                        )
                                        selectedSubId = newSubId
                                    }.onFailure { err ->
                                        errorMessage = "GPT Grading Error: ${err.message}"
                                    }
                                }.onFailure { err ->
                                    errorMessage = "Llama Vision OCR Error: ${err.message}"
                                }

                                isProcessing = false
                                stageMessage = ""
                            }
                        }
                    },
                    enabled = !isProcessing && uploadedBitmap != null && selectedAssignment != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Analyzing...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit & Grade with Llama 3.2 + GPT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (stageMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stageMessage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlue
                    )
                }

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftRed)
                    ) {
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = AlertRed,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Live AI OCR + Grading Report
                liveAnalysis?.let { report ->
                    Spacer(modifier = Modifier.height(18.dp))

                    val pct = if (report.maxMarks > 0f) {
                        ((report.score / report.maxMarks) * 100f).roundToInt()
                    } else 0
                    val gaugeColor = when {
                        pct >= 75 -> VerifiedGreen
                        pct >= 45 -> SyntheticOrange
                        else -> AlertRed
                    }

                    // Score Donut + Verdict
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DonutProgress(
                                progressPercent = pct,
                                size = 96.dp,
                                strokeWidth = 10.dp,
                                color = gaugeColor,
                                centerValueText = "%.1f".format(report.score),
                                centerLabel = "/ ${report.maxMarks.toInt()}"
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(gaugeColor.copy(alpha = 0.14f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = report.verdict,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = gaugeColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = report.summaryFeedback,
                                    fontSize = 12.sp,
                                    color = TextPrimaryLight,
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "OCR: ${report.ocrModelUsed} • Grader: ${report.gradingModelUsed}",
                                    fontSize = 10.sp,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Llama 3.2 Vision OCR Transcription Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Llama 3.2 Vision OCR Transcription",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = report.ocrTranscription,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimaryLight
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "OCR Correction & Notes: ${report.ocrObservations}",
                                fontSize = 12.sp,
                                color = TextSecondaryLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Strong Points Card
                    if (report.strongPoints.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Strong Points",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                report.strongPoints.forEach { sp ->
                                    FeedbackCheckItem(title = sp, isWarning = false)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Weak Points Card
                    if (report.weakPoints.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Weak Points & Mistakes",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                report.weakPoints.forEach { wp ->
                                    FeedbackCheckItem(title = wp, isWarning = true)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Step-by-Step Answer Verification (Correct vs Wrong)
                    if (report.stepVerdicts.isNotEmpty()) {
                        Text(
                            text = "Answer Verification (Correct vs Wrong)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        report.stepVerdicts.forEach { sv ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = if (sv.isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = if (sv.isCorrect) VerifiedGreen else AlertRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${if (sv.isCorrect) "Correct" else "Wrong"}: ${sv.step}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sv.isCorrect) Color(0xFF065F46) else AlertRed
                                        )
                                        if (sv.explanation.isNotBlank()) {
                                            Text(
                                                text = sv.explanation,
                                                fontSize = 12.sp,
                                                color = TextSecondaryLight
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Saved Results Tab (existing CSV + live graded submissions)
                if (submissions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        submissions.forEach { sub ->
                            val isSel = sub.submissionId == selectedSubId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSel) PrimaryBlue else Color(0xFFE2E8F0))
                                    .clickable { selectedSubId = sub.submissionId }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${sub.assignmentId} (${sub.submissionType})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) Color.White else TextPrimaryLight
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Paper preview card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            for (i in 1..5) {
                                val y = i * h / 6
                                drawLine(
                                    color = Color(0xFFE2E8F0),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = activeSubmission?.answerFile?.substringAfterLast("/") ?: "Math_Assignment.jpg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            val sampleQ = assignmentQuestions.firstOrNull()
                            Text(
                                text = sampleQ?.question ?: "x² + 5x + 6 = 0",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Cursive,
                                color = Color(0xFF1E293B),
                                maxLines = 2
                            )
                            Text(
                                text = if (sampleQ != null) "Ref: ${sampleQ.referenceAnswer}" else "(x + 2)(x + 3) = 0 -> x = -2, -3",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Cursive,
                                color = TextSecondaryLight,
                                maxLines = 2
                            )
                        }

                        val allAutoApproved = gradingRows.none { it.reviewStatus.contains("Review", ignoreCase = true) }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(14.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (allAutoApproved) VerifiedGreen else SyntheticOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (allAutoApproved) Icons.Filled.Check else Icons.Filled.Warning,
                                contentDescription = "Status",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                val totalMax = gradingRows.sumOf { it.maxMarks.toDouble() }.toFloat()
                val totalScored = gradingRows.sumOf { it.finalMarks.toDouble() }.toFloat()
                val scorePercent = if (totalMax > 0f) ((totalScored / totalMax) * 100f).roundToInt() else 92
                val avgConfidence = if (gradingRows.isNotEmpty()) {
                    (gradingRows.map { it.confidence }.average() * 100).roundToInt()
                } else 92

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val gaugeColor = when {
                        scorePercent >= 70 -> VerifiedGreen
                        scorePercent >= 40 -> SyntheticOrange
                        else -> AlertRed
                    }
                    DonutProgress(
                        progressPercent = scorePercent,
                        size = 114.dp,
                        strokeWidth = 11.dp,
                        color = gaugeColor,
                        centerValueText = if (totalMax > 0f) "%.1f".format(totalScored) else "92",
                        centerLabel = if (totalMax > 0f) "/ ${totalMax.toInt()} • $avgConfidence% conf" else "/ 100"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Detailed AI Feedback (${gradingRows.size} Questions)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (gradingRows.isEmpty()) {
                    FeedbackCheckItem(title = "Correct factorization", subtitle = null, isWarning = false)
                    FeedbackCheckItem(title = "Minor arithmetic error", subtitle = null, isWarning = true)
                    FeedbackCheckItem(title = "Partial marks awarded", subtitle = null, isWarning = false)
                } else {
                    gradingRows.forEach { row ->
                        val needsReview = row.reviewStatus.contains("Review", ignoreCase = true)
                        FeedbackCheckItem(
                            title = "Q${row.questionNo}: ${row.finalMarks}/${row.maxMarks.toInt()} • ${row.gradedBy} (${(row.confidence * 100).roundToInt()}%)",
                            subtitle = row.feedback,
                            isWarning = needsReview
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FeedbackCheckItem(
    title: String,
    subtitle: String? = null,
    isWarning: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isWarning) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (isWarning) SyntheticOrange else VerifiedGreen,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondaryLight,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
