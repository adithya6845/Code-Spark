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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.AiSubmissionAnalysis
import com.example.collisionengine.data.edusuccess.AssignmentAiGrader
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.data.edusuccess.UploadedFileMeta
import com.example.collisionengine.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
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

    var activeTab by remember { mutableStateOf("OCR Result") } // "OCR Result" | "Grading Report"
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val assignments = remember(isCoreLoaded) {
        EduDataRepository.assignments.distinctBy { it.assignmentId }
    }
    var selectedAssignmentId by remember(assignments) {
        mutableStateOf(assignments.firstOrNull()?.assignmentId ?: "A01")
    }
    val selectedAssignment = remember(selectedAssignmentId, assignments) {
        assignments.find { it.assignmentId == selectedAssignmentId }
    }

    var uploadedBitmap by remember {
        mutableStateOf<Bitmap?>(AssignmentAiGrader.loadSampleAssignmentBitmap(context))
    }
    var uploadedMeta by remember {
        mutableStateOf(UploadedFileMeta("A01_Algebra_Basics.jpg", "407 KB"))
    }
    var studentNotes by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var stageMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var liveAnalysis by remember { mutableStateOf<AiSubmissionAnalysis?>(null) }

    // Upload from Gallery / Documents
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bmp = AssignmentAiGrader.loadBitmapFromUri(context, uri)
            if (bmp != null) {
                uploadedBitmap = bmp
                uploadedMeta = AssignmentAiGrader.readFileMeta(context, uri)
                liveAnalysis = null
                errorMessage = null
            } else {
                errorMessage = "Could not decode selected file as an image. Please upload JPG or PNG."
            }
        }
    }

    // Capture with Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            uploadedBitmap = bmp
            uploadedMeta = UploadedFileMeta("Camera_Assignment.jpg", "Captured")
            liveAnalysis = null
            errorMessage = null
        }
    }

    fun runAiGradingPipeline() {
        val bmp = uploadedBitmap ?: return
        if (isProcessing) return
        scope.launch {
            isProcessing = true
            errorMessage = null
            stageMessage = "Running Llama 3.2 Vision Instruct OCR..."

            val ocrResult = AssignmentAiGrader.runLlamaVisionOcr(
                context = context,
                bitmap = bmp,
                assignment = selectedAssignment,
                studentNotes = studentNotes
            )

            ocrResult.onSuccess { (transcription, observations) ->
                stageMessage = "Checking Correct/Wrong & Strong/Weak points with GPT..."
                val rubrics = selectedAssignment?.let { EduDataRepository.getRubrics(it.rubricId) } ?: emptyList()
                val gradeResult = AssignmentAiGrader.gradeWithOpenAi(
                    context = context,
                    assignment = selectedAssignment,
                    rubrics = rubrics,
                    ocrTranscription = transcription,
                    ocrObservations = observations
                )
                gradeResult.onSuccess { report ->
                    liveAnalysis = report
                    selectedAssignment?.let { asg ->
                        EduDataRepository.recordLiveAiSubmission(
                            studentId = studentId,
                            assignment = asg,
                            score = report.score,
                            feedback = report.summaryFeedback
                        )
                    }
                }.onFailure { err ->
                    errorMessage = "GPT Evaluation Error: ${err.message}"
                }
            }.onFailure { err ->
                errorMessage = "Llama 3.2 Vision OCR Error: ${err.message}"
            }

            isProcessing = false
            stageMessage = ""
        }
    }

    // Historical saved submissions for Grading Report fallback
    val submissions = EduDataRepository.getSubmissions(studentId)
    val defaultSub = submissions.find { it.submissionType == "handwritten" } ?: submissions.firstOrNull()
    val savedGradingRows = remember(defaultSub, isLogsLoaded, submissions.size) {
        defaultSub?.let { EduDataRepository.getGradingForSubmission(it.submissionId) } ?: emptyList()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            EduTopBar(
                title = "Assignment Review",
                onBackClick = onNavigateBack,
                trailingIcon = Icons.Filled.Key,
                onTrailingClick = { showApiKeyDialog = true }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                color = Color.Transparent
            ) {
                Button(
                    onClick = {
                        if (liveAnalysis == null && !isProcessing) {
                            runAiGradingPipeline()
                        } else {
                            onReviewClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = if (liveAnalysis == null) "Run OCR & AI Grading" else "Practice Weak Concepts ->",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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

            // Uploaded File Card matching Image 1 ("Math_Assignment.jpg  2.4 MB  ✓")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { galleryLauncher.launch("image/*") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = uploadedMeta.fileName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight,
                                maxLines = 1
                            )
                            Text(
                                text = "${uploadedMeta.sizeText} • Tap to upload assignment",
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Ready",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Upload / Camera / Sample Row + Assignment Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = {
                        uploadedBitmap = AssignmentAiGrader.loadSampleAssignmentBitmap(context)
                        uploadedMeta = UploadedFileMeta("A01_Algebra_Basics.jpg", "407 KB")
                        selectedAssignmentId = "A01"
                        liveAnalysis = null
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sample", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compact Assignment selector chips
            if (assignments.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    assignments.forEach { asg ->
                        val isSel = asg.assignmentId == selectedAssignmentId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) PrimaryBlue.copy(alpha = 0.14f) else Color.White)
                                .border(
                                    1.dp,
                                    if (isSel) PrimaryBlue else Color(0xFFE2E8F0),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedAssignmentId = asg.assignmentId }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${asg.assignmentId}: ${asg.assignmentTitle}",
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) PrimaryBlue else TextSecondaryLight
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Underlined Tabs matching Image 1: OCR Result | Grading Report
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("OCR Result", "Grading Report").forEach { tab ->
                    val isSel = activeTab == tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTab = tab }
                            .padding(top = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tab,
                            fontSize = 15.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) PrimaryBlue else TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isSel) 2.5.dp else 1.dp)
                                .background(if (isSel) PrimaryBlue else Color(0xFFE2E8F0))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeTab == "OCR Result") {
                // Annotated Handwritten Sheet Preview with Green / Amber / Red Bounding Boxes
                uploadedBitmap?.let { bmp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(215.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Assignment Sheet",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Overlay colored step bounding boxes once analyzed (like Image 1)
                            liveAnalysis?.let { report ->
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    report.stepVerdicts.forEach { step ->
                                        val boxColor = when {
                                            step.status.equals("Correct", ignoreCase = true) -> VerifiedGreen
                                            step.status.equals("Partial", ignoreCase = true) -> SyntheticOrange
                                            else -> AlertRed
                                        }
                                        val top = (step.yStartFraction * h).coerceIn(4f, h - 18f)
                                        val boxH = ((step.yEndFraction - step.yStartFraction) * h).coerceAtLeast(18f)
                                        drawRoundRect(
                                            color = boxColor.copy(alpha = 0.08f),
                                            topLeft = Offset(w * 0.10f, top),
                                            size = Size(w * 0.80f, boxH),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                        )
                                        drawRoundRect(
                                            color = boxColor,
                                            topLeft = Offset(w * 0.10f, top),
                                            size = Size(w * 0.80f, boxH),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status / Progress Pill matching Image 1 ("✓ OCR Completed Successfully!")
                if (isProcessing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stageMessage,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                } else if (liveAnalysis != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFE8F7F0))
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(VerifiedGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "OCR Completed Successfully!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF065F46)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFEFF6FF))
                            .clickable { runAiGradingPipeline() }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap to Analyze Assignment with Llama 3.2 OCR + GPT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
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

                Spacer(modifier = Modifier.height(16.dp))

                // AI Grading Results Section (matching Image 1: Donut + Correct/Partial/Wrong card)
                val report = liveAnalysis
                val displayPct = report?.scorePercent ?: run {
                    val maxSum = savedGradingRows.sumOf { it.maxMarks.toDouble() }.toFloat()
                    val finSum = savedGradingRows.sumOf { it.finalMarks.toDouble() }.toFloat()
                    if (maxSum > 0f) ((finSum / maxSum) * 100f).roundToInt() else 92
                }
                val maxMarks = report?.maxMarks ?: 5f
                val correctScoreText = if (report != null) "%.2f / %.0f".format(report.correctMarks, maxMarks) else "2.75 / 5"
                val partialScoreText = if (report != null) "%.2f / %.0f".format(report.partialMarks, maxMarks) else "0.75 / 5"
                val wrongScoreText = if (report != null) "%.2f / %.0f".format(report.wrongMarks, maxMarks) else "0 / 5"

                Text(
                    text = "AI Grading Results",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val donutColor = when {
                        displayPct >= 75 -> VerifiedGreen
                        displayPct >= 45 -> SyntheticOrange
                        else -> AlertRed
                    }
                    DonutProgress(
                        progressPercent = displayPct,
                        size = 102.dp,
                        strokeWidth = 10.dp,
                        color = donutColor,
                        centerValueText = "$displayPct",
                        centerLabel = "/100"
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // Correct / Partial / Wrong Breakdown Card (Image 1)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BreakdownScoreRow(
                                dotColor = VerifiedGreen,
                                label = "Correct",
                                value = correctScoreText,
                                showCheck = true
                            )
                            BreakdownScoreRow(
                                dotColor = SyntheticOrange,
                                label = "Partial",
                                value = partialScoreText
                            )
                            BreakdownScoreRow(
                                dotColor = AlertRed,
                                label = "Wrong",
                                value = wrongScoreText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Identified Weak Concepts pills (matching Image 1)
                val weakConcepts = report?.weakConcepts?.takeIf { it.isNotEmpty() }
                    ?: listOf(
                        selectedAssignment?.conceptId?.let { EduDataRepository.getConceptById(it)?.topic }
                            ?: "Matrices & Determinants"
                    )

                Text(
                    text = "Identified Weak Concepts:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    weakConcepts.forEach { concept ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = concept,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }

                // Inline Strong & Weak Points preview right below (when analyzed)
                if (report != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    StrongWeakPointsSection(report = report)
                }
            } else {
                // Grading Report Tab: Detailed Strong Points, Weak Points, Correct vs Wrong Steps & OCR Text
                val report = liveAnalysis
                if (report != null) {
                    StrongWeakPointsSection(report = report)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Correct & Wrong Points (Step-by-Step)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    report.stepVerdicts.forEach { sv ->
                        val color = when {
                            sv.status.equals("Correct", ignoreCase = true) -> VerifiedGreen
                            sv.status.equals("Partial", ignoreCase = true) -> SyntheticOrange
                            else -> AlertRed
                        }
                        val icon = when {
                            sv.status.equals("Correct", ignoreCase = true) -> Icons.Filled.CheckCircle
                            sv.status.equals("Partial", ignoreCase = true) -> Icons.Filled.Warning
                            else -> Icons.Filled.Cancel
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${sv.status}: ${sv.step}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = color
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Llama 3.2 Vision Instruct OCR Text",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = report.ocrTranscription,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimaryLight
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "OCR Notes: ${report.ocrObservations}",
                                fontSize = 12.sp,
                                color = TextSecondaryLight
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Dataset Grading History (${defaultSub?.assignmentId ?: "A02"})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    savedGradingRows.forEach { row ->
                        val isWarn = row.finalMarks < row.maxMarks * 0.5f
                        FeedbackCheckItem(
                            title = "Q${row.questionNo}: ${row.finalMarks}/${row.maxMarks.toInt()} • ${row.gradedBy}",
                            subtitle = row.feedback,
                            isWarning = isWarn
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showApiKeyDialog) {
        var nvInput by remember { mutableStateOf(AssignmentAiGrader.getNvidiaKey(context)) }
        var oaInput by remember { mutableStateOf(AssignmentAiGrader.getOpenAiKey(context)) }
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("OCR & AI Grading API Keys", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nvInput,
                        onValueChange = { nvInput = it },
                        label = { Text("NVIDIA Llama 3.2 Vision Key (nvapi-...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = oaInput,
                        onValueChange = { oaInput = it },
                        label = { Text("OpenAI Key (sk-...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    AssignmentAiGrader.saveApiKeys(context, nvInput, oaInput)
                    showApiKeyDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BreakdownScoreRow(
    dotColor: Color,
    label: String,
    value: String,
    showCheck: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(dotColor),
                contentAlignment = Alignment.Center
            ) {
                if (showCheck) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimaryLight
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )
    }
}

@Composable
private fun StrongWeakPointsSection(report: AiSubmissionAnalysis) {
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

    if (report.weakPoints.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Weak Points",
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
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
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
