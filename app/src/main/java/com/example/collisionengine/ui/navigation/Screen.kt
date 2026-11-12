package com.example.collisionengine.ui.navigation

sealed class Screen(val route: String) {
    // EduSuccess AI 15 Core Screens (matching design mockup)
    object EduOnboarding : Screen("onboarding")
    object EduSignIn : Screen("sign_in")
    object EduStudentHome : Screen("student_home")
    object EduLesson : Screen("lesson")
    object EduVoice : Screen("edu_voice")
    object EduAiGrading : Screen("ai_grading")
    object EduClassroomAnalytics : Screen("classroom_analytics")
    object EduStudentRisk : Screen("student_risk")
    object EduInterventionCopilot : Screen("intervention_copilot")
    object EduInstitutionManagement : Screen("institution_management")
    object EduMyProgress : Screen("my_progress")
    object EduOfflineMode : Screen("offline_mode")
    object EduPredictionReport : Screen("prediction_report")
    object EduDrawerMenu : Screen("drawer_menu")
    object EduCelebration : Screen("celebration")

    // Legacy routes preserved for compatibility
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Research : Screen("research")
    object Placement : Screen("placement")
    object Simulator : Screen("simulator")
    
    object Results : Screen("results/{type}/{query}") {
        fun createRoute(type: String, query: String) = "results/$type/${android.net.Uri.encode(query)}"
    }
    
    object Explanation : Screen("explanation/{name}/{role}/{reason}/{score}") {
        fun createRoute(name: String, role: String, reason: String, score: Int): String {
            return "explanation/${android.net.Uri.encode(name)}/${android.net.Uri.encode(role)}/${android.net.Uri.encode(reason)}/$score"
        }
    }
    
    object Conversation : Screen("conversation/{name}/{reason}") {
        fun createRoute(name: String, reason: String): String {
            return "conversation/${android.net.Uri.encode(name)}/${android.net.Uri.encode(reason)}"
        }
    }

    object Chat : Screen("chat/{name}") {
        fun createRoute(name: String) = "chat/${android.net.Uri.encode(name)}"
    }
    
    object Messages : Screen("messages")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object AddPaper : Screen("add_paper")
}
