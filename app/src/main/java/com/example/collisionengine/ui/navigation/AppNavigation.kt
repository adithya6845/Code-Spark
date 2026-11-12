package com.example.collisionengine.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.collisionengine.ui.edusuccess.*

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showScreenSwitcher by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.EduOnboarding.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(250)) + slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) + slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) + slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(250)) + slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(250)) }
        ) {
            // Screen 1: Splash / Onboarding
            composable(route = Screen.EduOnboarding.route) {
                Screen1Onboarding(
                    onGetStarted = {
                        navController.navigate(Screen.EduSignIn.route)
                    }
                )
            }

            // Screen 2: Sign In
            composable(route = Screen.EduSignIn.route) {
                Screen2SignIn(
                    onSignInSuccess = { role ->
                        when (role) {
                            "Student" -> navController.navigate(Screen.EduStudentHome.route)
                            "Teacher" -> navController.navigate(Screen.EduClassroomAnalytics.route)
                            "Counsellor" -> navController.navigate(Screen.EduStudentRisk.route)
                            else -> navController.navigate(Screen.EduStudentHome.route)
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.EduStudentHome.route)
                    }
                )
            }

            // Screen 3: Student Home Dashboard
            composable(route = Screen.EduStudentHome.route) {
                Screen3StudentHome(
                    onNavigateToLesson = { navController.navigate(Screen.EduLesson.route) },
                    onNavigateToEduVoice = { navController.navigate(Screen.EduVoice.route) },
                    onNavigateToQuizzes = { navController.navigate(Screen.EduAiGrading.route) },
                    onNavigateToProgress = { navController.navigate(Screen.EduMyProgress.route) },
                    onNavigateToCelebration = { navController.navigate(Screen.EduCelebration.route) },
                    onOpenDrawer = { navController.navigate(Screen.EduDrawerMenu.route) },
                    onOpenSwitcher = { showScreenSwitcher = true },
                    onBottomTabSelected = { tab ->
                        when (tab) {
                            "home" -> { /* Already here */ }
                            "learning" -> navController.navigate(Screen.EduLesson.route)
                            "analytics" -> navController.navigate(Screen.EduClassroomAnalytics.route)
                            "profile" -> navController.navigate(Screen.EduMyProgress.route)
                        }
                    }
                )
            }

            // Screen 4: Personalized Lesson
            composable(route = Screen.EduLesson.route) {
                Screen4PersonalizedLesson(
                    onNavigateBack = { navController.popBackStack() },
                    onStartQuiz = { navController.navigate(Screen.EduAiGrading.route) }
                )
            }

            // Screen 5: EduVoice AI
            composable(route = Screen.EduVoice.route) {
                Screen5EduVoiceAI(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen 6: AI Grading Results
            composable(route = Screen.EduAiGrading.route) {
                Screen6AiGrading(
                    onNavigateBack = { navController.popBackStack() },
                    onReviewClick = { navController.navigate(Screen.EduLesson.route) },
                    onApproveMarks = { navController.navigate(Screen.EduCelebration.route) }
                )
            }

            // Screen 7: Classroom Analytics
            composable(route = Screen.EduClassroomAnalytics.route) {
                Screen7ClassroomAnalytics(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRiskAnalysis = { navController.navigate(Screen.EduStudentRisk.route) }
                )
            }

            // Screen 8: Student Risk Analysis
            composable(route = Screen.EduStudentRisk.route) {
                Screen8StudentRisk(
                    onNavigateBack = { navController.popBackStack() },
                    onViewRecommendations = { navController.navigate(Screen.EduInterventionCopilot.route) }
                )
            }

            // Screen 9: Intervention Copilot
            composable(route = Screen.EduInterventionCopilot.route) {
                Screen9InterventionCopilot(
                    onNavigateBack = { navController.popBackStack() },
                    onViewDetails = { navController.navigate(Screen.EduInstitutionManagement.route) }
                )
            }

            // Screen 10: Institution Management
            composable(route = Screen.EduInstitutionManagement.route) {
                Screen10InstitutionManagement(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen 11: My Progress
            composable(route = Screen.EduMyProgress.route) {
                Screen11MyProgress(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Screen 12: Offline Mode
            composable(route = Screen.EduOfflineMode.route) {
                Screen12OfflineMode(
                    onNavigateBack = { navController.popBackStack() },
                    onBrowseOfflineContent = { navController.navigate(Screen.EduLesson.route) }
                )
            }

            // Screen 13: Prediction Report
            composable(route = Screen.EduPredictionReport.route) {
                Screen13PredictionReport(
                    onNavigateBack = { navController.popBackStack() },
                    onViewFullAnalysis = { navController.navigate(Screen.EduStudentRisk.route) }
                )
            }

            // Screen 14: Navigation Drawer
            composable(route = Screen.EduDrawerMenu.route) {
                Screen14DrawerMenu(
                    selectedRoute = "Dashboard",
                    onNavigate = { route ->
                        navController.navigate(route)
                    },
                    onCloseDrawer = { navController.popBackStack() }
                )
            }

            // Screen 15: Celebration
            composable(route = Screen.EduCelebration.route) {
                Screen15Celebration(
                    onContinueLearning = {
                        navController.navigate(Screen.EduStudentHome.route) {
                            popUpTo(Screen.EduStudentHome.route) { inclusive = true }
                        }
                    }
                )
            }

            // Legacy fallback alias
            composable(route = Screen.Splash.route) {
                Screen1Onboarding(
                    onGetStarted = { navController.navigate(Screen.EduSignIn.route) }
                )
            }
        }

        // Screen Switcher Gallery Bottom Sheet
        if (showScreenSwitcher) {
            ScreenSwitcherModal(
                onDismiss = { showScreenSwitcher = false },
                onNavigate = { targetRoute ->
                    showScreenSwitcher = false
                    navController.navigate(targetRoute)
                }
            )
        }
    }
}
