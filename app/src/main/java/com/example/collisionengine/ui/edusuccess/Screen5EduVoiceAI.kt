package com.example.collisionengine.ui.edusuccess

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*

@Composable
fun Screen5EduVoiceAI(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val studentId = EduDataRepository.selectedStudentId
    val student = EduDataRepository.getStudent(studentId)
    val sessions = remember(studentId, EduDataRepository.isCoreLoaded) {
        EduDataRepository.getVoiceSessions(studentId)
    }

    var activeMode by remember { mutableStateOf("Live Teacher") } // "Live Teacher" | "History"
    var isListening by remember { mutableStateOf(false) }
    var selectedLanguage by remember {
        mutableStateOf(
            when (student?.preferredLanguage) {
                "Kannada" -> "ಕನ್ನಡ"
                "Hindi" -> "हिंदी"
                "Tamil" -> "தமிழ்"
                else -> "English"
            }
        )
    }
    var robotSpeech by remember { mutableStateOf("Let's solve a problem together!") }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            reloadTrigger++
        }
    }

    // Request microphone access as soon as user opens EduVoice AI if not already granted
    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val languages = EduMockData.languages

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micPulseScale"
    )

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            EduTopBar(
                title = "EduVoice AI",
                onBackClick = onNavigateBack,
                trailingIcon = Icons.Filled.Refresh,
                onTrailingClick = { reloadTrigger++ },
                isDarkTheme = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact Robot Companion & Soundwaves header
            EduVoiceRobotAvatar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                isTalking = isListening || activeMode == "Live Teacher"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mode switcher: Live ElevenLabs Teacher | Session History (CSV)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1E293B))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Live Teacher" to "ElevenLabs Teacher",
                    "History" to "Sessions (${sessions.size})"
                ).forEach { (key, label) ->
                    val isSelected = activeMode == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) PrimaryBlue else Color.Transparent)
                            .clickable { activeMode = key }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeMode == "Live Teacher") {
                if (!hasAudioPermission) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Microphone permission is needed to speak with the ElevenLabs teacher.",
                                fontSize = 12.sp,
                                color = Color(0xFFFCA5A5),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Enable Mic", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Embedded ElevenLabs Conversational AI Teacher Widget WebView
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                        .background(BackgroundDark)
                ) {
                    ElevenLabsVoiceWebView(
                        modifier = Modifier.fillMaxSize(),
                        reloadTrigger = reloadTrigger
                    )
                }
            } else {
                // Interactive Practice & CSV Voice Sessions Log
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = AccentCyan.copy(alpha = 0.3f))
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = robotSpeech,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier.size(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening) {
                            Box(
                                modifier = Modifier
                                    .size((84 * micPulse).dp)
                                    .clip(CircleShape)
                                    .background(AccentCyan.copy(alpha = 0.2f))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .shadow(16.dp, CircleShape, spotColor = AccentCyan)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(AccentCyan, PrimaryBlue)
                                    )
                                )
                                .clickable {
                                    isListening = !isListening
                                    robotSpeech = if (isListening) {
                                        "Listening in $selectedLanguage... Ask about Determinants or Matrices!"
                                    } else {
                                        "Tip from your tutor: Swap signs on the off-diagonal when finding 2x2 adjoints."
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Filled.GraphicEq else Icons.Filled.Mic,
                                contentDescription = "Microphone",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions) { session ->
                            val conceptTopic = EduDataRepository.getConceptById(session.conceptId)?.topic
                                ?: session.conceptId
                            val resultColor = when (session.sessionResult) {
                                "Mastered" -> VerifiedGreen
                                "Improved" -> AccentCyan
                                "Needs Review" -> SyntheticOrange
                                else -> AlertRed
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF112240))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conceptTopic,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${session.language} • ${session.durationMin}m • ASR ${(session.asrConfidence * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(resultColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = session.sessionResult,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = resultColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Language Selection Pills at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                languages.forEach { lang ->
                    val isSelected = selectedLanguage == lang
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PrimaryBlue else Color(0xFF1E293B))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AccentCyan else Color(0xFF334155),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                selectedLanguage = lang
                                robotSpeech = "Switched tutor language to $lang. Tap to ask a question!"
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = lang,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ElevenLabsVoiceWebView(
    modifier: Modifier = Modifier,
    reloadTrigger: Int = 0
) {
    val context = LocalContext.current
    val htmlContent = remember {
        context.assets.open("elevenlabs_voice.html").bufferedReader().use { it.readText() }
    }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.apply {
                stopLoading()
                destroy()
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.parseColor("#081225"))
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                    allowContentAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest?) {
                        request?.let { req ->
                            post {
                                val audioResources = req.resources.filter {
                                    it == PermissionRequest.RESOURCE_AUDIO_CAPTURE
                                }.toTypedArray()
                                if (audioResources.isNotEmpty()) {
                                    req.grant(audioResources)
                                } else {
                                    req.grant(req.resources)
                                }
                            }
                        }
                    }
                }
                // Using an https:// baseUrl gives getUserMedia a secure browser context
                loadDataWithBaseURL(
                    "https://edusuccess.ai/",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
                webViewInstance = this
            }
        },
        update = { webView ->
            if (reloadTrigger > 0) {
                webView.loadDataWithBaseURL(
                    "https://edusuccess.ai/",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
    )
}
