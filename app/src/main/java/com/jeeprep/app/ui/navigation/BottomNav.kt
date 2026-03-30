package com.jeeprep.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Practice : Screen("practice", "Practice", Icons.Filled.Quiz, Icons.Outlined.Quiz)
    data object Notes : Screen("notes", "Notes", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    data object Progress : Screen("progress", "Progress", Icons.Filled.BarChart, Icons.Outlined.BarChart)
}

// Sub-screens (not in bottom bar)
object SubScreen {
    const val TOPIC_LIST = "practice/{subjectId}"
    const val QUESTION_SESSION = "practice/{subjectId}/{topicId}"
    const val AI_PRACTICE = "ai_practice/{subjectId}/{topicId}"
    const val MOCK_TEST = "mock_test/{testId}"
    const val MOCK_RESULT = "mock_result/{testId}"
    const val SUBJECT_NOTES = "notes/subject/{subjectId}"
    const val TOPIC_NOTES = "notes/{topicId}"
    const val MEMORY_TRICKS = "tricks/{topicId}"
    const val PYQ_YEARS = "pyq_years"
    const val PYQ_PAPER = "pyq_paper/{year}/{examType}"
    const val MODEL_DOWNLOAD = "model_download"
    const val CHAT = "chat/{questionId}"
    const val MISTAKE_JOURNAL = "mistake_journal"
}

val bottomBarScreens = listOf(Screen.Home, Screen.Practice, Screen.Notes, Screen.Progress)

@Composable
fun JeePrepBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom bar on main screens
    val showBottomBar = bottomBarScreens.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    if (showBottomBar) {
        NavigationBar {
            bottomBarScreens.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title
                        )
                    },
                    label = { Text(screen.title) }
                )
            }
        }
    }
}
