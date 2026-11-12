package com.example.collisionengine.ui.edusuccess

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        mutableStateOf<Bitmap?>(null)
    }
    var uploadedMeta by remember {
        mutableStateOf<UploadedFileMeta?>(null)
    }
    var studentNotes by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var stageMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var liveAnalysis by remember { mutableStateOf<AiSubmissionAnalysis?>(null) }

    fun runAiGradingPipeline(targetBitmap: Bitmap? = uploadedBitmap) {
        val bmp = targetBitmap ?: uploadedBitmap ?: return
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
                    EduDataRepository.lastOcrAnalysis = report
                    EduDataRepository.lastOcrAssignmentTitle =
                        selectedAssignment?.assignmentTitle ?: uploadedMeta?.fileName ?: "Assignment"
                    selectedAssignment?.let { asg ->
                        EduDataRepository.recordLiveAiSubmission(
                            studentId = studentId,
                            assignment = asg,
                            score = report.score,
                            feedback = report.summaryFeedback,
                            analysis = report
                        )
                    }
                }.onFailure { err ->
                    errorMessage = "AI Evaluation Error: ${err.message}"
                }
            }.onFailure { err ->
                errorMessage = "Vision OCR Error: ${err.message}"
            }

            isProcessing = false
            stageMessage = ""
        }
    }

    // Upload from Gallery / Documents
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bmp = AssignmentAiGrader.loadBitmapFromUri(context, uri)
            if (bmp != null) {
                val meta = AssignmentAiGrader.readFileMeta(context, uri)
                uploadedBitmap = bmp
                uploadedMeta = meta
                liveAnalysis = null
                errorMessage = null
                if (meta.fileName.contains("A01", true) || meta.fileName.contains("Algebra", true)) {
                    selectedAssignmentId = "A01"
                } else if (meta.fileName.contains("A02", true) || meta.fileName.contains("Matrices", true)) {
                    selectedAssignmentId = "A02"
                }
                runAiGradingPipeline(bmp)
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
            uploadedMeta = UploadedFileMeta("Camera_Assignment_${System.currentTimeMillis()}.jpg", "Captured")
            liveAnalysis = null
            errorMessage = null
            runAiGradingPipeline(bmp)
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
                if (uploadedBitmap == null) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.UploadFile,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upload Assignment",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else if (isProcessing) {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = PrimaryBlue.copy(alpha = 0.6f),
                            disabledContentColor = Color.White
                        )
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Analyzing Assignment...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else if (liveAnalysis == null) {
                    Button(
                        onClick = { runAiGradingPipeline(uploadedBitmap) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Run OCR & AI Grading",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReviewClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            border = BorderStroke(1.2.dp, PrimaryBlue)
                        ) {
                            Text(
                                text = "Practice Lesson",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                        Button(
                            onClick = onApproveMarks,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(
                                text = "View Learning Path ->",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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
            Spacer(modifier = Modifier.height(10.dp))

            if (uploadedBitmap == null) {
                // ==========================================
                // 1. EMPTY STATE: NO ASSIGNMENT UPLOADED YET
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { galleryLauncher.launch("image/*") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudUpload,
                                contentDescription = "Upload Assignment",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Upload Assignment",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Upload your handwritten math worksheet or test (PNG / JPG). The AI will transcribe your equations, verify each mathematical step, and calculate your score.",
                            fontSize = 13.sp,
                            color = TextSecondaryLight,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Choose from Gallery / Files",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.2.dp, PrimaryBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Take Photo with Camera",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                val bmp = AssignmentAiGrader.loadSampleAssignmentBitmap(context)
                                if (bmp != null) {
                                    uploadedBitmap = bmp
                                    uploadedMeta = UploadedFileMeta("A01_Algebra_Basics.jpg", "407 KB")
                                    selectedAssignmentId = "A01"
                                    liveAnalysis = null
                                    errorMessage = null
                                    runAiGradingPipeline(bmp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Or test with sample worksheet (A01: Algebra)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Assignment Subject / Topic Chips
                Text(
                    text = "Select Target Subject / Rubric:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (assignments.isNotEmpty()) {
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) PrimaryBlue.copy(alpha = 0.14f) else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSel) PrimaryBlue else Color(0xFFE2E8F0),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedAssignmentId = asg.assignmentId }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "${asg.assignmentId}: ${asg.assignmentTitle}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) PrimaryBlue else TextSecondaryLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // How AI Grading Works Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "How AI Assignment Grading Works",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        AiFeatureItem(
                            icon = Icons.Filled.DocumentScanner,
                            iconTint = PrimaryBlue,
                            title = "1. Llama 3.2 Vision Instruct OCR",
                            desc = "Transcribes handwritten steps, variables, and algebraic deductions from the uploaded image."
                        )
                        AiFeatureItem(
                            icon = Icons.Filled.CheckCircle,
                            iconTint = VerifiedGreen,
                            title = "2. GPT Mathematical Verifier",
                            desc = "Verifies each arithmetic operation, confirms LHS = RHS proof, and computes marks (up to 5/5 full marks)."
                        )
                        AiFeatureItem(
                            icon = Icons.Filled.Lightbulb,
                            iconTint = SyntheticOrange,
                            title = "3. Instant Diagnostic Feedback",
                            desc = "Draws colored bounding boxes for each step and pinpoints strong concepts and weak areas."
                        )
                    }
                }
            } else {
                // ==========================================
                // 2. UPLOADED STATE: ASSIGNMENT PRESENT
                // ==========================================
                // Uploaded File Bar (File Name, Size, Replace, Remove)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                    text = uploadedMeta?.fileName ?: "Uploaded_Assignment.jpg",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${uploadedMeta?.sizeText ?: "Uploaded"} • Ready for AI grading",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Replace Image",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = {
                                uploadedBitmap = null
                                uploadedMeta = null
                                liveAnalysis = null
                                errorMessage = null
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove Image",
                                    tint = AlertRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Assignment Selector Chips
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

                // Tabs: OCR Result | Grading Report
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
                    // Uploaded Assignment Image Preview with Bounding Boxes
                    val bmp = uploadedBitmap
                    if (bmp != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(14.dp),
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

                                // Overlay colored step bounding boxes once analyzed
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

                    // Status / Progress / Action Pill
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
                                text = "OCR & AI Grading Completed Successfully!",
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
                                .clickable { runAiGradingPipeline(uploadedBitmap) }
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

                    // Show AI Grading Results ONLY when analysis has been executed!
                    liveAnalysis?.let { report ->
                        Spacer(modifier = Modifier.height(16.dp))

                        val displayPct = report.scorePercent
                        val maxMarks = report.maxMarks
                        val correctScoreText = "%.2f / %.0f".format(report.correctMarks, maxMarks)
                        val partialScoreText = "%.2f / %.0f".format(report.partialMarks, maxMarks)
                        val wrongScoreText = "%.2f / %.0f".format(report.wrongMarks, maxMarks)

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

                            // Correct / Partial / Wrong Breakdown Card
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

                        val weakConcepts = report.weakConcepts
                        Text(
                            text = if (weakConcepts.isEmpty()) "Identified Weak Concepts: None (Full Marks! 🎉)" else "Identified Weak Concepts:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (weakConcepts.isEmpty()) VerifiedGreen else TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (weakConcepts.isNotEmpty()) {
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
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✓ Perfect Solution — Full marks awarded",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        StrongWeakPointsSection(report = report)
                    }
                } else {
                    // Grading Report Tab
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
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No Grading Report Yet",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap 'Run OCR & AI Grading' on the uploaded assignment to generate detailed step-by-step math analysis.",
                                    fontSize = 12.sp,
                                    color = TextSecondaryLight,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
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

@Composable
private fun AiFeatureItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = TextSecondaryLight,
                lineHeight = 16.sp
            )
        }
    }
}
