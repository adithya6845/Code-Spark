package com.example.collisionengine.ui.edusuccess

data class StudentProfile(
    val name: String = "Adithya Kumar",
    val rollNo: String = "23CS001",
    val role: String = "Student",
    val overallProgress: Int = 78,
    val progressDelta: Int = 12,
    val riskLevel: String = "High Risk",
    val dropoutRiskPercent: Int = 68,
    val lowRiskPredictionPercent: Int = 18
)

data class ShapFactor(
    val factorName: String,
    val impactValue: Float, // Negative indicates risk contribution, e.g. -0.35f
    val displayValue: String
)

data class Institution(
    val name: String,
    val location: String,
    val studentCount: String
)

data class SubjectMastery(
    val subject: String,
    val scorePercent: Int,
    val colorHex: Long
)

data class WeekPerformance(
    val week: String,
    val classAvg: Float,
    val yourClass: Float
)

data class WeakConcept(
    val conceptName: String,
    val failurePercent: Int
)

data class InterventionAction(
    val title: String,
    var isSelected: Boolean = true
)

object EduMockData {
    val student = StudentProfile()

    val shapFactors = listOf(
        ShapFactor("Attendance", -0.35f, "-0.35"),
        ShapFactor("Low Assignment", -0.28f, "-0.28"),
        ShapFactor("Performance", -0.22f, "-0.22"),
        ShapFactor("Engagement", -0.15f, "-0.15")
    )

    val institutions = mutableListOf(
        Institution("AMC Engineering College", "Bengaluru", "2,500 Students"),
        Institution("BNMIT", "Bengaluru", "1,800 Students"),
        Institution("NMIT", "Bengaluru", "1,200 Students")
    )

    val subjectMasteries = listOf(
        SubjectMastery("Mathematics", 92, 0xFF1976D2),
        SubjectMastery("Physics", 78, 0xFFF59E0B),
        SubjectMastery("Chemistry", 85, 0xFF10B981)
    )

    val performanceTrend = listOf(
        WeekPerformance("W1", 35f, 48f),
        WeekPerformance("W2", 52f, 65f),
        WeekPerformance("W3", 58f, 72f),
        WeekPerformance("W4", 68f, 87f)
    )

    val weakConcepts = listOf(
        WeakConcept("Factorization", 65),
        WeakConcept("Quadratic Equations", 42)
    )

    val languages = listOf("English", "ಕನ್ನಡ", "हिंदी", "தமிழ்")
}

