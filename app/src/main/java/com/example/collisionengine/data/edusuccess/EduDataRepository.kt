package com.example.collisionengine.data.edusuccess

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Repository that loads all 15 EduSuccess AI synthetic CSV datasets from app assets
 * and provides reactive Compose state + domain queries across screens.
 * ALL DATA IS SYNTHETIC (seed=42).
 */
object EduDataRepository {
    private var _students: List<StudentRow> = emptyList()
    private var _riskScores: List<RiskScoreRow> = emptyList()
    private var _riskFeatures: List<RiskFeatureRow> = emptyList()
    private var _concepts: List<ConceptRow> = emptyList()
    private var _classes: List<ClassRow> = emptyList()
    private val _interventions = mutableStateListOf<InterventionRow>()
    private var _assignments: List<AssignmentRow> = emptyList()
    private var _rubrics: List<RubricRow> = emptyList()
    private var _questions: List<QuestionRow> = emptyList()
    private var _voiceSessions: List<VoiceSessionRow> = emptyList()

    // Larger transactional/log datasets loaded asynchronously right after startup
    private var _submissions: List<SubmissionRow> = emptyList()
    private var _gradingResults: List<GradingResultRow> = emptyList()
    private var _engagementLogs: List<EngagementRow> = emptyList()
    private var _quizAttempts: List<QuizAttemptRow> = emptyList()
    private var _attendance: List<AttendanceRow> = emptyList()

    private var isInit = false

    var isCoreLoaded by mutableStateOf(false)
        private set

    var isLogsLoaded by mutableStateOf(false)
        private set

    /** Active student/class selection shared across screens (defaults to demo student S0001). */
    var selectedStudentId by mutableStateOf("S0001")
    var selectedClassId by mutableStateOf("12A")

    val students get() = _students
    val riskScores get() = _riskScores
    val riskFeatures get() = _riskFeatures
    val concepts get() = _concepts
    val classes get() = _classes
    val interventions: List<InterventionRow> get() = _interventions
    val assignments get() = _assignments
    val rubrics get() = _rubrics
    val questions get() = _questions
    val voiceSessions get() = _voiceSessions
    val submissions get() = _submissions
    val gradingResults get() = _gradingResults
    val engagementLogs get() = _engagementLogs
    val quizAttempts get() = _quizAttempts
    val attendance get() = _attendance

    fun init(context: Context) {
        if (isInit) return
        isInit = true
        val appContext = context.applicationContext

        // Load lightweight core datasets immediately so first screens never wait
        try {
            _students = parseCsv(appContext, "students.csv") { cols ->
                StudentRow(
                    studentId = cols[0],
                    name = cols[1],
                    rollNo = cols[2],
                    classId = cols[3],
                    grade = cols[4],
                    gender = cols.getOrNull(5) ?: "",
                    age = cols.getOrNull(6)?.toIntOrNull() ?: 0,
                    preferredLanguage = cols.getOrNull(7) ?: "English",
                    areaType = cols.getOrNull(8) ?: "",
                    internetAccess = cols.getOrNull(9) ?: "",
                    firstGenLearner = cols.getOrNull(10)?.equals("True", ignoreCase = true) == true,
                    scholarship = cols.getOrNull(11)?.equals("True", ignoreCase = true) == true
                )
            }
            _classes = parseCsv(appContext, "classes.csv") { cols ->
                ClassRow(
                    classId = cols[0],
                    grade = cols.getOrNull(1)?.toIntOrNull() ?: 0,
                    classTeacher = cols.getOrNull(2) ?: "",
                    counsellor = cols.getOrNull(3) ?: ""
                )
            }
            _concepts = parseCsv(appContext, "concepts.csv") { cols ->
                ConceptRow(
                    conceptId = cols[0],
                    topic = cols[1],
                    subject = cols[2],
                    unit = cols[3],
                    prerequisiteId = cols.getOrNull(4) ?: "",
                    gradeLevel = cols.getOrNull(5)?.toIntOrNull() ?: 0
                )
            }
            _riskScores = parseCsv(appContext, "risk_scores_demo.csv") { cols ->
                RiskScoreRow(
                    studentId = cols[0],
                    riskScore = cols[1].toFloatOrNull() ?: 0f,
                    riskLevel = cols[2],
                    shapAttendance = cols.getOrNull(3)?.toFloatOrNull() ?: 0f,
                    shapPerformance = cols.getOrNull(4)?.toFloatOrNull() ?: 0f,
                    shapAssignments = cols.getOrNull(5)?.toFloatOrNull() ?: 0f,
                    shapEngagement = cols.getOrNull(6)?.toFloatOrNull() ?: 0f,
                    scoredOn = cols.getOrNull(7) ?: ""
                )
            }
            _riskFeatures = parseCsv(appContext, "student_risk_features.csv") { cols ->
                RiskFeatureRow(
                    studentId = cols[0],
                    attendancePct = cols.getOrNull(1)?.toFloatOrNull() ?: 0f,
                    avgAssignmentScore = cols.getOrNull(2)?.toFloatOrNull() ?: 0f,
                    assignmentsSubmitted = cols.getOrNull(3)?.toIntOrNull() ?: 0,
                    assignmentsMissedPct = cols.getOrNull(4)?.toFloatOrNull() ?: 0f,
                    lateSubmissionPct = cols.getOrNull(5)?.toFloatOrNull() ?: 0f,
                    avgWeeklyLogins = cols.getOrNull(6)?.toFloatOrNull() ?: 0f,
                    lessonCompletionPct = cols.getOrNull(7)?.toFloatOrNull() ?: 0f,
                    avgQuizScore = cols.getOrNull(8)?.toFloatOrNull() ?: 0f,
                    voiceSessions = cols.getOrNull(9)?.toIntOrNull() ?: 0,
                    dropoutLabel = cols.getOrNull(10)?.toIntOrNull() ?: 0
                )
            }
            _interventions.clear()
            _interventions.addAll(parseCsv(appContext, "interventions.csv") { cols ->
                InterventionRow(
                    interventionId = cols[0],
                    studentId = cols[1],
                    riskLevel = cols[2],
                    riskScoreAtStart = cols.getOrNull(3)?.toFloatOrNull() ?: 0f,
                    recommendation = cols.getOrNull(4) ?: "",
                    createdOn = cols.getOrNull(5) ?: "",
                    assignedTo = cols.getOrNull(6) ?: "",
                    status = cols.getOrNull(7) ?: "",
                    followUpDate = cols.getOrNull(8) ?: "",
                    outcome = cols.getOrNull(9) ?: "",
                    riskScoreAfter = cols.getOrNull(10)?.toFloatOrNull()
                )
            })
            _assignments = parseCsv(appContext, "assignments.csv") { cols ->
                AssignmentRow(
                    assignmentId = cols[0],
                    assignmentTitle = cols[1],
                    questionNo = cols.getOrNull(2)?.toIntOrNull() ?: 1,
                    subject = cols.getOrNull(3) ?: "",
                    question = cols.getOrNull(4) ?: "",
                    referenceAnswer = cols.getOrNull(5) ?: "",
                    rubricId = cols.getOrNull(6) ?: "",
                    conceptId = cols.getOrNull(7) ?: "",
                    maxMarks = cols.getOrNull(8)?.toFloatOrNull() ?: 5f,
                    dueDate = cols.getOrNull(9) ?: ""
                )
            }
            _rubrics = parseCsv(appContext, "rubrics.csv") { cols ->
                RubricRow(
                    rubricId = cols[0],
                    subject = cols.getOrNull(1) ?: "",
                    criterion = cols.getOrNull(2) ?: "",
                    maxMarks = cols.getOrNull(3)?.toFloatOrNull() ?: 0f,
                    description = cols.getOrNull(4) ?: ""
                )
            }
            _questions = parseCsv(appContext, "question_bank.csv") { cols ->
                QuestionRow(
                    questionId = cols[0],
                    conceptId = cols.getOrNull(1) ?: "",
                    subject = cols.getOrNull(2) ?: "",
                    difficulty = cols.getOrNull(3) ?: "Easy",
                    difficultyLevel = cols.getOrNull(4)?.toIntOrNull() ?: 1,
                    questionType = cols.getOrNull(5) ?: "MCQ",
                    question = cols.getOrNull(6) ?: "",
                    optionA = cols.getOrNull(7) ?: "",
                    optionB = cols.getOrNull(8) ?: "",
                    optionC = cols.getOrNull(9) ?: "",
                    optionD = cols.getOrNull(10) ?: "",
                    correctOption = cols.getOrNull(11) ?: "A",
                    correctAnswer = cols.getOrNull(12) ?: "",
                    distractorMisconceptions = cols.getOrNull(13) ?: ""
                )
            }
            _voiceSessions = parseCsv(appContext, "voice_sessions.csv") { cols ->
                VoiceSessionRow(
                    sessionId = cols[0],
                    studentId = cols[1],
                    language = cols.getOrNull(2) ?: "English",
                    conceptId = cols.getOrNull(3) ?: "",
                    sessionResult = cols.getOrNull(4) ?: "",
                    startedAt = cols.getOrNull(5) ?: "",
                    durationMin = cols.getOrNull(6)?.toIntOrNull() ?: 0,
                    misconceptionsDetected = cols.getOrNull(7)?.toIntOrNull() ?: 0,
                    asrConfidence = cols.getOrNull(8)?.toFloatOrNull() ?: 0f
                )
            }
            isCoreLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Load heavier log CSVs on Dispatchers.IO
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val subs = parseCsv(appContext, "submissions.csv") { cols ->
                    SubmissionRow(
                        submissionId = cols[0],
                        studentId = cols[1],
                        assignmentId = cols[2],
                        answerFile = cols.getOrNull(3) ?: "",
                        submissionType = cols.getOrNull(4) ?: "",
                        submittedAt = cols.getOrNull(5) ?: "",
                        isLate = cols.getOrNull(6) == "1"
                    )
                }
                val grades = parseCsv(appContext, "grading_results.csv") { cols ->
                    GradingResultRow(
                        submissionId = cols[0],
                        questionNo = cols.getOrNull(1)?.toIntOrNull() ?: 1,
                        conceptId = cols.getOrNull(2) ?: "",
                        maxMarks = cols.getOrNull(3)?.toFloatOrNull() ?: 5f,
                        marks = cols.getOrNull(4)?.toFloatOrNull() ?: 0f,
                        feedback = cols.getOrNull(5) ?: "",
                        confidence = cols.getOrNull(6)?.toFloatOrNull() ?: 0f,
                        reviewStatus = cols.getOrNull(7) ?: "Auto-approved",
                        teacherOverrideMarks = cols.getOrNull(8)?.toFloatOrNull(),
                        finalMarks = cols.getOrNull(9)?.toFloatOrNull() ?: 0f,
                        gradedBy = cols.getOrNull(10) ?: "AI"
                    )
                }
                val eng = parseCsv(appContext, "engagement_logs.csv") { cols ->
                    EngagementRow(
                        studentId = cols[0],
                        weekStart = cols[1],
                        loginCount = cols.getOrNull(2)?.toIntOrNull() ?: 0,
                        lessonsCompleted = cols.getOrNull(3)?.toIntOrNull() ?: 0,
                        lessonCompletionPct = cols.getOrNull(4)?.toFloatOrNull() ?: 0f,
                        timeSpentMin = cols.getOrNull(5)?.toIntOrNull() ?: 0,
                        quizzesAttempted = cols.getOrNull(6)?.toIntOrNull() ?: 0,
                        voiceSessions = cols.getOrNull(7)?.toIntOrNull() ?: 0,
                        activity = cols.getOrNull(8) ?: "Low"
                    )
                }
                val quizzes = parseCsv(appContext, "quiz_attempts.csv") { cols ->
                    QuizAttemptRow(
                        attemptId = cols[0],
                        studentId = cols[1],
                        questionId = cols.getOrNull(2) ?: "",
                        conceptId = cols.getOrNull(3) ?: "",
                        answer = cols.getOrNull(4) ?: "",
                        score = cols.getOrNull(5)?.toIntOrNull() ?: 0,
                        misconceptionTag = cols.getOrNull(6) ?: "",
                        timeTakenSec = cols.getOrNull(7)?.toIntOrNull() ?: 0,
                        timestamp = cols.getOrNull(8) ?: ""
                    )
                }
                val att = parseCsv(appContext, "attendance.csv") { cols ->
                    AttendanceRow(
                        studentId = cols[0],
                        date = cols[1],
                        status = cols.getOrNull(2) ?: "Present"
                    )
                }

                _submissions = subs
                _gradingResults = grades
                _engagementLogs = eng
                _quizAttempts = quizzes
                _attendance = att
                isLogsLoaded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ── Convenience queries ──

    fun getStudent(studentId: String = selectedStudentId): StudentRow? =
        _students.find { it.studentId == studentId }

    fun getClassInfo(classId: String): ClassRow? =
        _classes.find { it.classId == classId }

    fun getRisk(studentId: String = selectedStudentId): RiskScoreRow? =
        _riskScores.find { it.studentId == studentId }

    fun getRiskFeatures(studentId: String = selectedStudentId): RiskFeatureRow? =
        _riskFeatures.find { it.studentId == studentId }

    fun getInterventions(studentId: String = selectedStudentId): List<InterventionRow> =
        _interventions.filter { it.studentId == studentId }

    fun addInterventionTask(studentId: String, recommendation: String) {
        val risk = getRisk(studentId)
        val student = getStudent(studentId)
        val classInfo = student?.let { getClassInfo(it.classId) }
        val newId = "INT%04d".format(_interventions.size + 1)
        _interventions.add(
            0,
            InterventionRow(
                interventionId = newId,
                studentId = studentId,
                riskLevel = risk?.riskLevel ?: "High",
                riskScoreAtStart = risk?.riskScore ?: 0.68f,
                recommendation = recommendation,
                createdOn = "2026-11-12",
                assignedTo = classInfo?.counsellor ?: "Mr. Ramesh Rao",
                status = "Scheduled",
                followUpDate = "2026-11-19",
                outcome = "Pending",
                riskScoreAfter = null
            )
        )
    }

    fun getEngagement(studentId: String = selectedStudentId): List<EngagementRow> =
        _engagementLogs.filter { it.studentId == studentId }

    fun getVoiceSessions(studentId: String = selectedStudentId): List<VoiceSessionRow> =
        _voiceSessions.filter { it.studentId == studentId }

    fun getSubmissions(studentId: String = selectedStudentId): List<SubmissionRow> =
        _submissions.filter { it.studentId == studentId }

    fun getGradingForSubmission(submissionId: String): List<GradingResultRow> =
        _gradingResults.filter { it.submissionId == submissionId }

    fun getAssignmentItems(assignmentId: String): List<AssignmentRow> =
        _assignments.filter { it.assignmentId == assignmentId }

    fun getQuestionsForConcept(conceptId: String): List<QuestionRow> =
        _questions.filter { it.conceptId == conceptId }

    fun getRubrics(rubricId: String): List<RubricRow> =
        _rubrics.filter { it.rubricId == rubricId }

    fun getAttendanceSummary(studentId: String = selectedStudentId): Map<String, Int> {
        val rows = _attendance.filter { it.studentId == studentId }
        return rows.groupingBy { it.status }.eachCount()
    }

    fun getStudentsInClass(classId: String? = null): List<StudentRow> =
        if (classId.isNullOrBlank() || classId == "All") _students
        else _students.filter { it.classId == classId }

    fun highRiskStudents(classId: String? = null): List<Pair<StudentRow, RiskScoreRow>> {
        val riskMap = _riskScores.associateBy { it.studentId }
        return getStudentsInClass(classId).mapNotNull { s ->
            riskMap[s.studentId]?.let { r ->
                if (r.riskLevel == "High") s to r else null
            }
        }.sortedByDescending { it.second.riskScore }
    }

    fun classStudentCount(classId: String? = null): Int =
        getStudentsInClass(classId).size

    fun avgScore(classId: String? = null): Float {
        val ids = getStudentsInClass(classId).map { it.studentId }.toSet()
        val features = if (ids.isEmpty()) _riskFeatures else _riskFeatures.filter { it.studentId in ids }
        return if (features.isEmpty()) 0f
        else features.map { it.avgAssignmentScore }.average().toFloat()
    }

    /** Computes failure rate % per concept across quiz attempts for a class or whole school. */
    fun weakConceptsForClass(classId: String? = null, limit: Int = 4): List<Pair<ConceptRow, Int>> {
        if (_quizAttempts.isEmpty() || _concepts.isEmpty()) return emptyList()
        val ids = getStudentsInClass(classId).map { it.studentId }.toSet()
        val relevant = if (classId.isNullOrBlank() || classId == "All") _quizAttempts
        else _quizAttempts.filter { it.studentId in ids }
        val grouped = relevant.groupBy { it.conceptId }
        return _concepts.mapNotNull { concept ->
            val rows = grouped[concept.conceptId] ?: return@mapNotNull null
            if (rows.isEmpty()) return@mapNotNull null
            val failPct = (rows.count { it.score == 0 } * 100f / rows.size).toInt()
            concept to failPct
        }.sortedByDescending { it.second }.take(limit)
    }

    /** Weakest concepts for a specific student from quiz attempts & grading results. */
    fun weakConceptsForStudent(studentId: String = selectedStudentId, limit: Int = 5): List<Pair<ConceptRow, Int>> {
        val attempts = _quizAttempts.filter { it.studentId == studentId }
        if (attempts.isEmpty()) {
            // Immediate fallback to C04-C06 (demo student's weak Matrices unit) before logs finish
            return _concepts.filter { it.conceptId in setOf("C06", "C05", "C04", "C02") }
                .map { it to 70 }
                .take(limit)
        }
        val grouped = attempts.groupBy { it.conceptId }
        return _concepts.mapNotNull { c ->
            val rows = grouped[c.conceptId] ?: return@mapNotNull null
            val accuracyPct = (rows.count { it.score == 1 } * 100f / rows.size).toInt()
            c to accuracyPct
        }.sortedBy { it.second }.take(limit)
    }

    /** Subject mastery percentages for a student derived from quiz attempts + grading results. */
    fun subjectMasteryForStudent(studentId: String = selectedStudentId): Map<String, Int> {
        val attempts = _quizAttempts.filter { it.studentId == studentId }
        val conceptSubject = _concepts.associate { it.conceptId to it.subject }
        val mathRows = attempts.filter { conceptSubject[it.conceptId] == "Mathematics" }
        val phyRows = attempts.filter { conceptSubject[it.conceptId] == "Physics" }

        val features = getRiskFeatures(studentId)
        val mathPct = if (mathRows.isNotEmpty()) {
            (mathRows.count { it.score == 1 } * 100f / mathRows.size).toInt().coerceIn(5, 99)
        } else (features?.avgQuizScore?.toInt() ?: 40)

        val phyPct = if (phyRows.isNotEmpty()) {
            (phyRows.count { it.score == 1 } * 100f / phyRows.size).toInt().coerceIn(5, 99)
        } else ((features?.avgAssignmentScore?.toInt() ?: 35) + 8).coerceIn(5, 99)

        val engPct = (features?.lessonCompletionPct?.toInt() ?: 25).coerceIn(5, 99)

        return linkedMapOf(
            "Mathematics" to mathPct,
            "Physics" to phyPct,
            "Lesson Mastery" to engPct
        )
    }

    /** 4-bucket performance trend comparing overall school vs selected class across quiz timestamps/engagement weeks. */
    fun weeklyPerformanceTrend(classId: String? = null): List<Triple<String, Float, Float>> {
        if (_engagementLogs.isEmpty()) return emptyList()
        val classStudentIds = getStudentsInClass(classId).map { it.studentId }.toSet()
        val weeks = _engagementLogs.map { it.weekStart }.distinct().sorted()
        if (weeks.isEmpty()) return emptyList()

        // Group 15 weeks into 4 periods (W1..W4)
        val chunkSize = (weeks.size / 4).coerceAtLeast(1)
        val buckets = weeks.chunked(chunkSize).take(4)
        return buckets.mapIndexed { idx, bucketWeeks ->
            val weekSet = bucketWeeks.toSet()
            val allLogs = _engagementLogs.filter { it.weekStart in weekSet }
            val clsLogs = if (classStudentIds.isEmpty()) allLogs else allLogs.filter { it.studentId in classStudentIds }
            val allAvg = if (allLogs.isEmpty()) 50f else allLogs.map { it.lessonCompletionPct }.average().toFloat()
            val clsAvg = if (clsLogs.isEmpty()) allAvg else clsLogs.map { it.lessonCompletionPct }.average().toFloat()
            Triple("W${idx + 1}", allAvg, clsAvg)
        }
    }

    fun getConceptById(id: String): ConceptRow? =
        _concepts.find { it.conceptId == id }

    // ── CSV parser ──

    private fun <T> parseCsv(context: Context, filename: String, mapper: (List<String>) -> T): List<T> {
        val results = mutableListOf<T>()
        context.assets.open(filename).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLine() // skip header
                var line = reader.readLine()
                while (line != null) {
                    if (line.isNotBlank()) {
                        try {
                            val cols = parseCsvLine(line)
                            if (cols.isNotEmpty()) {
                                results.add(mapper(cols))
                            }
                        } catch (_: Exception) { }
                    }
                    line = reader.readLine()
                }
            }
        }
        return results
    }

    /** Simple CSV line parser that handles quoted fields with commas inside. */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(c)
            }
        }
        result.add(sb.toString().trim())
        return result
    }
}

// ── Data row classes ──

data class StudentRow(
    val studentId: String,
    val name: String,
    val rollNo: String,
    val classId: String,
    val grade: String,
    val gender: String,
    val age: Int,
    val preferredLanguage: String,
    val areaType: String,
    val internetAccess: String,
    val firstGenLearner: Boolean = false,
    val scholarship: Boolean = false
)

data class RiskScoreRow(
    val studentId: String,
    val riskScore: Float,
    val riskLevel: String,
    val shapAttendance: Float,
    val shapPerformance: Float,
    val shapAssignments: Float,
    val shapEngagement: Float,
    val scoredOn: String = ""
)

data class RiskFeatureRow(
    val studentId: String,
    val attendancePct: Float,
    val avgAssignmentScore: Float,
    val assignmentsSubmitted: Int,
    val assignmentsMissedPct: Float,
    val lateSubmissionPct: Float,
    val avgWeeklyLogins: Float,
    val lessonCompletionPct: Float,
    val avgQuizScore: Float,
    val voiceSessions: Int,
    val dropoutLabel: Int
)

data class ConceptRow(
    val conceptId: String,
    val topic: String,
    val subject: String,
    val unit: String,
    val prerequisiteId: String,
    val gradeLevel: Int
)

data class ClassRow(
    val classId: String,
    val grade: Int,
    val classTeacher: String,
    val counsellor: String
)

data class InterventionRow(
    val interventionId: String,
    val studentId: String,
    val riskLevel: String,
    val riskScoreAtStart: Float,
    val recommendation: String,
    val createdOn: String,
    val assignedTo: String,
    val status: String,
    val followUpDate: String,
    val outcome: String,
    val riskScoreAfter: Float?
)

data class EngagementRow(
    val studentId: String,
    val weekStart: String,
    val loginCount: Int,
    val lessonsCompleted: Int,
    val lessonCompletionPct: Float,
    val timeSpentMin: Int,
    val quizzesAttempted: Int,
    val voiceSessions: Int,
    val activity: String
)

data class VoiceSessionRow(
    val sessionId: String,
    val studentId: String,
    val language: String,
    val conceptId: String,
    val sessionResult: String,
    val startedAt: String,
    val durationMin: Int,
    val misconceptionsDetected: Int,
    val asrConfidence: Float
)

data class AssignmentRow(
    val assignmentId: String,
    val assignmentTitle: String,
    val questionNo: Int,
    val subject: String,
    val question: String,
    val referenceAnswer: String,
    val rubricId: String,
    val conceptId: String,
    val maxMarks: Float,
    val dueDate: String
)

data class SubmissionRow(
    val submissionId: String,
    val studentId: String,
    val assignmentId: String,
    val answerFile: String,
    val submissionType: String,
    val submittedAt: String,
    val isLate: Boolean
)

data class GradingResultRow(
    val submissionId: String,
    val questionNo: Int,
    val conceptId: String,
    val maxMarks: Float,
    val marks: Float,
    val feedback: String,
    val confidence: Float,
    val reviewStatus: String,
    val teacherOverrideMarks: Float?,
    val finalMarks: Float,
    val gradedBy: String
)

data class QuestionRow(
    val questionId: String,
    val conceptId: String,
    val subject: String,
    val difficulty: String,
    val difficultyLevel: Int,
    val questionType: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String,
    val correctAnswer: String,
    val distractorMisconceptions: String
)

data class RubricRow(
    val rubricId: String,
    val subject: String,
    val criterion: String,
    val maxMarks: Float,
    val description: String
)

data class QuizAttemptRow(
    val attemptId: String,
    val studentId: String,
    val questionId: String,
    val conceptId: String,
    val answer: String,
    val score: Int,
    val misconceptionTag: String,
    val timeTakenSec: Int,
    val timestamp: String
)

data class AttendanceRow(
    val studentId: String,
    val date: String,
    val status: String
)
