package com.jeeprep.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jeeprep.app.ui.screens.home.HomeScreen
import com.jeeprep.app.ui.screens.practice.PracticeScreen
import com.jeeprep.app.ui.screens.practice.TopicListScreen
import com.jeeprep.app.ui.screens.practice.QuestionSessionScreen
import com.jeeprep.app.ui.screens.notes.NotesScreen
import com.jeeprep.app.ui.screens.notes.SubjectNotesScreen
import com.jeeprep.app.ui.screens.notes.TopicNotesScreen
import com.jeeprep.app.ui.screens.progress.ProgressScreen
import com.jeeprep.app.ui.screens.mocktest.MockTestScreen
import com.jeeprep.app.ui.screens.mocktest.MockResultScreen
import com.jeeprep.app.ui.screens.download.ModelDownloadScreen

@Composable
fun JeePrepNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Practice.route) {
            PracticeScreen(navController = navController)
        }
        composable(Screen.Notes.route) {
            NotesScreen(navController = navController)
        }
        composable(Screen.Progress.route) {
            ProgressScreen(
                onNavigatePremium = { navController.navigate(SubScreen.PREMIUM) }
            )
        }
        composable(
            route = SubScreen.TOPIC_LIST,
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 1
            TopicListScreen(subjectId = subjectId, navController = navController)
        }
        composable(
            route = SubScreen.QUESTION_SESSION,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.IntType },
                navArgument("topicId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 1
            val topicId = backStackEntry.arguments?.getInt("topicId") ?: 1
            QuestionSessionScreen(subjectId = subjectId, topicId = topicId, navController = navController)
        }
        composable(
            route = SubScreen.TOPIC_NOTES,
            arguments = listOf(navArgument("topicId") { type = NavType.IntType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getInt("topicId") ?: 1
            TopicNotesScreen(topicId = topicId, navController = navController)
        }
        composable(
            route = SubScreen.SUBJECT_NOTES,
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 1
            SubjectNotesScreen(subjectId = subjectId, navController = navController)
        }
        composable(
            route = SubScreen.MOCK_TEST,
            arguments = listOf(navArgument("testId") { type = NavType.LongType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            MockTestScreen(testId = testId, navController = navController)
        }
        composable(
            route = SubScreen.MOCK_RESULT,
            arguments = listOf(navArgument("testId") { type = NavType.LongType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            MockResultScreen(testId = testId, navController = navController)
        }
        composable(SubScreen.MODEL_DOWNLOAD) {
            ModelDownloadScreen(navController = navController)
        }
    }
}