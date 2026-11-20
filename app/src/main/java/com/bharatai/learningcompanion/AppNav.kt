package com.bharatai.learningcompanion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNav(
    viewModel: LearningViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState = viewModel.uiState.collectAsState().value

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(
                viewModel = viewModel,
                navigateToGaps = {
                    navController.navigate("gaps") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("gaps") {
            GapsScreen(
                skillGaps = uiState.skillGaps,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onNext = {
                    viewModel.createPlan(
                        gaps = uiState.skillGaps,
                        timeframe = "4 weeks"
                    )
                    navController.navigate("plan") {
                        popUpTo("gaps") { inclusive = true }
                    }
                }
            )
        }
        composable("plan") {
            PlanScreen(
                studyPlan = uiState.studyPlan,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                navigateToChat = {
                    navController.navigate("chat")
                }
            )
        }
        composable("chat") {
            ChatScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}