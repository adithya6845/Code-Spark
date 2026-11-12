package com.example.collisionengine.ui.edusuccess

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collisionengine.data.edusuccess.EduDataRepository
import com.example.collisionengine.ui.theme.*
import java.net.URLEncoder

data class LessonVideo(
    val conceptId: String,
    val topic: String,
    val subject: String,
    val unit: String,
    val formula: String,
    val duration: String,
    val youtubeUrl: String,
    val khanAcademyUrl: String,
    val description: String
)

object LessonVideoCatalog {
    private val catalog = mapOf(
        "C01" to LessonVideo(
            conceptId = "C01",
            topic = "Linear Equations",
            subject = "Mathematics",
            unit = "Algebra",
            formula = "ax + b = c  ⟹  x = (c - b) / a",
            duration = "08:45",
            youtubeUrl = "https://www.youtube.com/watch?v=bAerID24QJ8",
            khanAcademyUrl = "https://www.khanacademy.org/math/algebra/x2f8bb11595b61c86:solve-equations-inequalities",
            description = "Solving multi-step linear equations, isolating variables, and balancing both sides."
        ),
        "C02" to LessonVideo(
            conceptId = "C02",
            topic = "Quadratic Equations",
            subject = "Mathematics",
            unit = "Algebra",
            formula = "ax² + bx + c = 0,  x = (-b ± √(b² - 4ac)) / 2a",
            duration = "11:20",
            youtubeUrl = "https://www.youtube.com/watch?v=ZBalWWHY4Go",
            khanAcademyUrl = "https://www.khanacademy.org/math/algebra/x2f8bb11595b61c86:quadratic-functions-equations",
            description = "Factoring trinomials, completing the square, and applying the quadratic formula."
        ),
        "C03" to LessonVideo(
            conceptId = "C03",
            topic = "Functions & Graphs",
            subject = "Mathematics",
            unit = "Algebra",
            formula = "f(x) = y,  Domain: x ∈ ℝ,  Range: y ∈ ℝ",
            duration = "09:15",
            youtubeUrl = "https://www.youtube.com/watch?v=kvGsIo1TmsM",
            khanAcademyUrl = "https://www.khanacademy.org/math/algebra/x2f8bb11595b61c86:functions",
            description = "Understanding inputs, outputs, mapping, vertical line test, and graph transformations."
        ),
        "C04" to LessonVideo(
            conceptId = "C04",
            topic = "Matrices Basics",
            subject = "Mathematics",
            unit = "Matrices & Determinants",
            formula = "A = [a_ij]_{m × n},  Dimensions & Order",
            duration = "07:30",
            youtubeUrl = "https://www.youtube.com/watch?v=0oGJTQCy4cQ",
            khanAcademyUrl = "https://www.khanacademy.org/math/precalculus/x9e81a4f98381618:matrices",
            description = "Introduction to matrix elements, row and column vectors, and square matrices."
        ),
        "C05" to LessonVideo(
            conceptId = "C05",
            topic = "Matrix Operations",
            subject = "Mathematics",
            unit = "Matrices & Determinants",
            formula = "A + B,  c·A,  (AB)_ij = ∑ a_ik · b_kj",
            duration = "10:05",
            youtubeUrl = "https://www.youtube.com/watch?v=kYB8IZa5AuE",
            khanAcademyUrl = "https://www.khanacademy.org/math/precalculus/x9e81a4f98381618:matrices/x9e81a4f98381618:matrix-multiplication",
            description = "Matrix addition, scalar multiplication, and row-by-column dot product multiplication."
        ),
        "C06" to LessonVideo(
            conceptId = "C06",
            topic = "Determinants",
            subject = "Mathematics",
            unit = "Matrices & Determinants",
            formula = "|A| = ad - bc,  Expansion along Row 1",
            duration = "12:40",
            youtubeUrl = "https://www.youtube.com/watch?v=2PAm50Upm3Q",
            khanAcademyUrl = "https://www.khanacademy.org/math/algebra-home/alg-matrices/alg-determinants-and-inverses-of-matrices",
            description = "Calculating determinants of 2x2 and 3x3 matrices using cofactors and minors."
        ),
        "C07" to LessonVideo(
            conceptId = "C07",
            topic = "Matrix Inverse",
            subject = "Mathematics",
            unit = "Matrices & Determinants",
            formula = "A⁻¹ = (1/|A|) · adj(A),  A · A⁻¹ = I",
            duration = "13:10",
            youtubeUrl = "https://www.youtube.com/watch?v=AMfnd_U5e9Y",
            khanAcademyUrl = "https://www.khanacademy.org/math/precalculus/x9e81a4f98381618:matrices/x9e81a4f98381618:matrix-inverses",
            description = "Finding the inverse of invertible matrices to solve systems of linear equations."
        ),
        "C08" to LessonVideo(
            conceptId = "C08",
            topic = "Probability Basics",
            subject = "Mathematics",
            unit = "Probability",
            formula = "P(E) = n(E) / n(S),  0 ≤ P(E) ≤ 1",
            duration = "08:50",
            youtubeUrl = "https://www.youtube.com/watch?v=uzkc-qNVoOk",
            khanAcademyUrl = "https://www.khanacademy.org/math/statistics-probability/probability-library",
            description = "Sample space, independent events, mutually exclusive events, and basic probability laws."
        ),
        "C09" to LessonVideo(
            conceptId = "C09",
            topic = "Conditional Probability",
            subject = "Mathematics",
            unit = "Probability",
            formula = "P(A|B) = P(A ∩ B) / P(B),  Bayes' Theorem",
            duration = "11:45",
            youtubeUrl = "https://www.youtube.com/watch?v=ibINrxJLvlM",
            khanAcademyUrl = "https://www.khanacademy.org/math/statistics-probability/probability-library/conditional-probability-independence",
            description = "Conditional probability calculations, tree diagrams, and Bayes' formula."
        ),
        "C10" to LessonVideo(
            conceptId = "C10",
            topic = "Limits",
            subject = "Mathematics",
            unit = "Calculus",
            formula = "lim_{x→a} f(x) = L,  L'Hôpital's Rule: 0/0",
            duration = "09:30",
            youtubeUrl = "https://www.youtube.com/watch?v=riXcZT2ICjA",
            khanAcademyUrl = "https://www.khanacademy.org/math/calculus-1/cs1-limits-and-continuity",
            description = "Understanding limits, one-sided limits, continuity, and handling indeterminate forms."
        ),
        "C11" to LessonVideo(
            conceptId = "C11",
            topic = "Differentiation",
            subject = "Mathematics",
            unit = "Calculus",
            formula = "d/dx(xⁿ) = n·xⁿ⁻¹,  Chain & Product Rules",
            duration = "14:20",
            youtubeUrl = "https://www.youtube.com/watch?v=rAof9Ld5sOg",
            khanAcademyUrl = "https://www.khanacademy.org/math/calculus-1/cs1-derivatives-definition-and-basic-rules",
            description = "Derivatives as instantaneous rates of change, power rule, quotient rule, and chain rule."
        ),
        "C12" to LessonVideo(
            conceptId = "C12",
            topic = "Applications of Derivatives",
            subject = "Mathematics",
            unit = "Calculus",
            formula = "f'(x) = 0 => Extrema,  f''(x) > 0 => Min",
            duration = "15:10",
            youtubeUrl = "https://www.youtube.com/watch?v=4C5qA5d_1i8",
            khanAcademyUrl = "https://www.khanacademy.org/math/calculus-1/cs1-applying-derivatives-to-analyze-functions",
            description = "Finding local maxima and minima, optimization problems, and tangent slope analysis."
        ),
        "C13" to LessonVideo(
            conceptId = "C13",
            topic = "Integration Basics",
            subject = "Mathematics",
            unit = "Calculus",
            formula = "∫ xⁿ dx = (xⁿ⁺¹)/(n+1) + C,  Definite Integrals",
            duration = "16:00",
            youtubeUrl = "https://www.youtube.com/watch?v=rfG8ce4nNh0",
            khanAcademyUrl = "https://www.khanacademy.org/math/calculus-1/cs1-integrals",
            description = "Anti-derivatives, Riemann sums, integration constants, and calculating area under curves."
        ),
        "C14" to LessonVideo(
            conceptId = "C14",
            topic = "Kinematics",
            subject = "Physics",
            unit = "Mechanics",
            formula = "v = u + at,  s = ut + ½at²,  v² = u² + 2as",
            duration = "12:15",
            youtubeUrl = "https://www.youtube.com/watch?v=1F_45m0oG9w",
            khanAcademyUrl = "https://www.khanacademy.org/science/physics/one-dimensional-motion",
            description = "1D motion with constant acceleration, displacement-time graphs, and velocity vectors."
        ),
        "C15" to LessonVideo(
            conceptId = "C15",
            topic = "Newton's Laws of Motion",
            subject = "Physics",
            unit = "Mechanics",
            formula = "F_net = m·a,  F_action = -F_reaction",
            duration = "10:40",
            youtubeUrl = "https://www.youtube.com/watch?v=kKKM8Y-u7ds",
            khanAcademyUrl = "https://www.khanacademy.org/science/physics/forces-newtons-laws",
            description = "Inertia, force equals mass times acceleration, friction forces, and action-reaction pairs."
        ),
        "C16" to LessonVideo(
            conceptId = "C16",
            topic = "Work, Energy & Power",
            subject = "Physics",
            unit = "Mechanics",
            formula = "W = F·d·cos(θ),  KE = ½mv²,  P = W / t",
            duration = "11:55",
            youtubeUrl = "https://www.youtube.com/watch?v=2WS1dQxy7ps",
            khanAcademyUrl = "https://www.khanacademy.org/science/physics/work-and-energy",
            description = "Mechanical work, kinetic and potential energy conservation, and power rate."
        ),
        "C17" to LessonVideo(
            conceptId = "C17",
            topic = "Current Electricity",
            subject = "Physics",
            unit = "Electricity",
            formula = "I = q / t,  R = ρ·L / A,  I = n·A·e·v_d",
            duration = "13:25",
            youtubeUrl = "https://www.youtube.com/watch?v=mc979OhitAg",
            khanAcademyUrl = "https://www.khanacademy.org/science/physics/circuits-topic",
            description = "Flow of electric charges, resistance factors, drift velocity, and electric potential."
        ),
        "C18" to LessonVideo(
            conceptId = "C18",
            topic = "Ohm's Law & Circuits",
            subject = "Physics",
            unit = "Electricity",
            formula = "V = I · R,  R_eq = R₁ + R₂,  P = V · I",
            duration = "14:05",
            youtubeUrl = "https://www.youtube.com/watch?v=HsLLq61_QdQ",
            khanAcademyUrl = "https://www.khanacademy.org/science/physics/circuits-topic/circuits-library",
            description = "Direct proportionality of current and voltage, equivalent circuit resistance, and power."
        )
    )

    fun getVideoForConcept(conceptId: String, fallbackTopic: String): LessonVideo {
        return catalog[conceptId] ?: LessonVideo(
            conceptId = conceptId,
            topic = fallbackTopic,
            subject = "Mathematics & Science",
            unit = "Core Concepts",
            formula = "Core Method & Step-by-Step Proof",
            duration = "10:00",
            youtubeUrl = "https://www.youtube.com/results?search_query=${URLEncoder.encode("$fallbackTopic lesson Khan Academy", "UTF-8")}",
            khanAcademyUrl = "https://www.khanacademy.org/search?page_search_query=${URLEncoder.encode(fallbackTopic, "UTF-8")}",
            description = "Comprehensive educational lesson on $fallbackTopic."
        )
    }

    fun buildSearchUrl(query: String): String {
        return "https://www.youtube.com/results?search_query=${URLEncoder.encode("$query educational lesson Khan Academy", "UTF-8")}"
    }

    fun buildKhanSearchUrl(query: String): String {
        return "https://www.khanacademy.org/search?page_search_query=${URLEncoder.encode(query, "UTF-8")}"
    }
}

fun launchVideoIntent(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(url)), "Watch Lesson Video")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e2: Exception) {
            Toast.makeText(context, "Could not open video: ${e2.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun Screen4PersonalizedLesson(
    onNavigateBack: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val context = LocalContext.current
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

    // Current topic's distinct video
    val currentVideo = remember(conceptId, activeConcept?.topic) {
        LessonVideoCatalog.getVideoForConcept(conceptId, activeConcept?.topic ?: "Quadratic Equations")
    }

    var isBookmarked by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Summary") }
    val tabs = listOf("Summary", "Examples", "Practice")

    // Global Search Query state
    var searchQuery by remember { mutableStateOf("") }

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

            // Search Video From Anywhere Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search any topic or video (e.g. Limits, Optics)...",
                        fontSize = 13.sp,
                        color = TextSecondaryLight
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            launchVideoIntent(context, LessonVideoCatalog.buildSearchUrl(searchQuery))
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Search on YouTube",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            launchVideoIntent(context, LessonVideoCatalog.buildSearchUrl(searchQuery))
                        }
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal selector for student's weak concepts
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
                text = currentVideo.topic,
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
                Text(
                    text = "${currentVideo.subject} • ${currentVideo.unit}",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================================
            // VIDEO PLAYER CARD: Opens YouTube / EdTech on Click
            // ========================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        launchVideoIntent(context, currentVideo.youtubeUrl)
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Stylized Chalkboard / Educational Canvas Background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        drawRect(Color(0xFF1E293B))

                        // Grid lines
                        for (i in 0..6) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, i * h / 6),
                                end = Offset(w, i * h / 6),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Background instructor figure
                        drawCircle(Color(0xFF334155), radius = 22.dp.toPx(), center = Offset(w * 0.82f, h * 0.42f))
                        drawRoundRect(
                            Color(0xFF2563EB),
                            topLeft = Offset(w * 0.74f, h * 0.54f),
                            size = androidx.compose.ui.geometry.Size(48.dp.toPx(), 60.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )
                    }

                    // Formula & Unit Description
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp, end = 100.dp)
                    ) {
                        Text(
                            text = currentVideo.formula,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${currentVideo.unit} • ${currentVideo.topic}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF0000))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Khan Academy on YouTube",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    // Big Center Play Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF0000).copy(alpha = 0.95f))
                            .clickable {
                                launchVideoIntent(context, currentVideo.youtubeUrl)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play Video on YouTube",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Duration Badge (Bottom Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentVideo.duration,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Tap to Play Pill (Top Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Open Video",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Direct YouTube / Khan Academy / Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play on YouTube Button
                Button(
                    onClick = { launchVideoIntent(context, currentVideo.youtubeUrl) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "YouTube",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Khan Academy Button
                Button(
                    onClick = { launchVideoIntent(context, currentVideo.khanAcademyUrl) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14BF96)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Khan Acad.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Search EdTech Button
                OutlinedButton(
                    onClick = { launchVideoIntent(context, LessonVideoCatalog.buildSearchUrl(currentVideo.topic)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Search",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                    InteractiveLessonItem(
                        title = "What is ${currentVideo.topic}?",
                        subtitle = currentVideo.description,
                        onWatchClick = { launchVideoIntent(context, currentVideo.youtubeUrl) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InteractiveLessonItem(
                        title = if (prereq != null) "Prerequisite: $prereq" else "Core Formulas & Derivations",
                        subtitle = "Key Formula: ${currentVideo.formula}",
                        onWatchClick = { launchVideoIntent(context, currentVideo.youtubeUrl) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InteractiveLessonItem(
                        title = "Common Misconceptions & Step-by-Step Fixes",
                        subtitle = "Learn typical sign errors, algebraic pitfalls, and how to verify answers.",
                        onWatchClick = { launchVideoIntent(context, currentVideo.youtubeUrl) }
                    )
                }

                "Examples" -> {
                    if (matchingAssignments.isEmpty()) {
                        InteractiveLessonItem(
                            title = "Worked Example for ${currentVideo.topic}",
                            subtitle = "Watch step-by-step video demonstration of standard exam problems.",
                            onWatchClick = { launchVideoIntent(context, currentVideo.youtubeUrl) }
                        )
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${ex.assignmentTitle} • Q${ex.questionNo}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                        TextButton(
                                            onClick = { launchVideoIntent(context, currentVideo.youtubeUrl) },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.PlayCircle,
                                                contentDescription = null,
                                                tint = Color(0xFFCC0000),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Watch Video", fontSize = 11.sp, color = Color(0xFFCC0000), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
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
                                        Spacer(modifier = Modifier.height(4.dp))
                                        TextButton(
                                            onClick = { launchVideoIntent(context, currentVideo.youtubeUrl) },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Watch Video Explanation", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                        }
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
fun InteractiveLessonItem(
    title: String,
    subtitle: String,
    onWatchClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWatchClick() },
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondaryLight,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF2F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color(0xFFCC0000),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
