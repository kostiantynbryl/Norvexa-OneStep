package com.norvexa.onestep.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.norvexa.onestep.MainViewModel
import com.norvexa.onestep.R
import com.norvexa.onestep.model.allSteps
import com.norvexa.onestep.ui.screens.FocusScreen
import com.norvexa.onestep.ui.screens.GoalDetailScreen
import com.norvexa.onestep.ui.screens.GoalEditorScreen
import com.norvexa.onestep.ui.screens.GoalsScreen
import com.norvexa.onestep.ui.screens.OnboardingScreen
import com.norvexa.onestep.ui.screens.ProgressScreen
import com.norvexa.onestep.ui.screens.SettingsScreen
import com.norvexa.onestep.ui.screens.TodayScreen
import com.norvexa.onestep.ui.theme.OneStepTheme

private object Routes {
    const val Today = "today"
    const val Goals = "goals"
    const val Progress = "progress"
    const val Settings = "settings"
    const val NewGoal = "goal/new"
    const val Detail = "goal/{goalId}"
    const val Edit = "goal/{goalId}/edit"
    const val Focus = "focus/{goalId}/{stepId}"

    fun detail(goalId: String) = "goal/$goalId"
    fun edit(goalId: String) = "goal/$goalId/edit"
    fun focus(goalId: String, stepId: String) = "focus/$goalId/$stepId"
}

@Composable
fun OneStepRoot(
    viewModel: MainViewModel,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onReminderChanged: (Boolean) -> Unit,
) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val isReady by viewModel.isReady.collectAsStateWithLifecycle()
    OneStepTheme(data.settings.themeMode) {
        if (!isReady) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }
        } else if (!data.onboardingComplete) {
            OnboardingScreen(onStart = viewModel::completeOnboarding)
        } else {
            val navController = rememberNavController()
            val bottomRoutes = setOf(Routes.Today, Routes.Goals, Routes.Progress, Routes.Settings)
            val backStack by navController.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (currentRoute in bottomRoutes) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(68.dp)
                                    .clip(MaterialTheme.shapes.extraLarge),
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                tonalElevation = 0.dp,
                            ) {
                                listOf(
                                    Triple(Routes.Today, R.string.today, Icons.Default.Home),
                                    Triple(Routes.Goals, R.string.goals, Icons.Default.CheckCircle),
                                    Triple(Routes.Progress, R.string.progress, Icons.Default.BarChart),
                                    Triple(Routes.Settings, R.string.settings, Icons.Default.Settings),
                                ).forEach { (route, label, icon) ->
                                    val selected = currentRoute == route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(icon, contentDescription = stringResource(label)) },
                                        label = { Text(stringResource(label)) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                },
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = Routes.Today,
                    modifier = Modifier.padding(padding),
                ) {
                    composable(Routes.Today) {
                        TodayScreen(
                            data = data,
                            onCreateGoal = { navController.navigate(Routes.NewGoal) },
                            onOpenGoal = { navController.navigate(Routes.detail(it)) },
                            onFocus = { goalId, stepId -> navController.navigate(Routes.focus(goalId, stepId)) },
                            onComplete = viewModel::completeStep,
                            onPostpone = viewModel::postponeStep,
                            onSplit = viewModel::splitStep,
                            onSkip = viewModel::skipStep,
                        )
                    }
                    composable(Routes.Goals) {
                        GoalsScreen(
                            goals = data.goals,
                            onCreateGoal = { navController.navigate(Routes.NewGoal) },
                            onOpenGoal = { navController.navigate(Routes.detail(it)) },
                            onActivate = viewModel::activateGoal,
                        )
                    }
                    composable(Routes.Progress) { ProgressScreen(data) }
                    composable(Routes.Settings) {
                        SettingsScreen(
                            settings = data.settings,
                            onThemeChanged = viewModel::updateTheme,
                            onLanguageChanged = viewModel::updateLanguage,
                            onReminderChanged = onReminderChanged,
                            onExport = onExport,
                            onImport = onImport,
                        )
                    }
                    composable(Routes.NewGoal) {
                        GoalEditorScreen(
                            existing = null,
                            onBack = { navController.popBackStack() },
                            onCreate = { title, description, category, minutes, days ->
                                viewModel.createGoal(title, description, category, minutes, days) { id ->
                                    navController.navigate(Routes.detail(id)) {
                                        popUpTo(Routes.NewGoal) { inclusive = true }
                                    }
                                }
                            },
                            onUpdate = { _, _, _ -> },
                        )
                    }
                    composable(Routes.Detail) { entry ->
                        val goalId = entry.arguments?.getString("goalId").orEmpty()
                        val goal = data.goals.firstOrNull { it.id == goalId }
                        GoalDetailScreen(
                            goal = goal,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate(Routes.edit(goalId)) },
                            onFocus = { stepId -> navController.navigate(Routes.focus(goalId, stepId)) },
                            onActivate = { viewModel.activateGoal(goalId) },
                            onTogglePause = { viewModel.togglePause(goalId) },
                            onComplete = { viewModel.completeStep(goalId, it) },
                            onAddStage = { viewModel.addStage(goalId, it) },
                            onDeleteStage = { viewModel.deleteStage(goalId, it) },
                            onAddStep = { stageId, title, description, minutes ->
                                viewModel.addStep(goalId, stageId, title, description, minutes)
                            },
                            onEditStep = { stepId, title, description, minutes ->
                                viewModel.updateStepDetails(goalId, stepId, title, description, minutes)
                            },
                            onDeleteStep = { viewModel.deleteStep(goalId, it) },
                            onMoveStep = { stageId, stepId, direction ->
                                viewModel.moveStep(goalId, stageId, stepId, direction)
                            },
                            onDelete = {
                                viewModel.deleteGoal(goalId)
                                navController.popBackStack()
                            },
                        )
                    }
                    composable(Routes.Edit) { entry ->
                        val goalId = entry.arguments?.getString("goalId").orEmpty()
                        val goal = data.goals.firstOrNull { it.id == goalId }
                        GoalEditorScreen(
                            existing = goal,
                            onBack = { navController.popBackStack() },
                            onCreate = { _, _, _, _, _ -> },
                            onUpdate = { title, description, category ->
                                viewModel.updateGoal(goalId, title, description, category)
                                navController.popBackStack()
                            },
                        )
                    }
                    composable(Routes.Focus) { entry ->
                        val goalId = entry.arguments?.getString("goalId").orEmpty()
                        val stepId = entry.arguments?.getString("stepId").orEmpty()
                        val goal = data.goals.firstOrNull { it.id == goalId }
                        val step = goal?.allSteps()?.firstOrNull { it.id == stepId }
                        FocusScreen(
                            goal = goal,
                            step = step,
                            onBack = { navController.popBackStack() },
                            onStarted = { viewModel.startStep(goalId, stepId) },
                            onComplete = { actualMinutes ->
                                viewModel.completeStep(goalId, stepId, actualMinutes)
                                navController.popBackStack()
                            },
                        )
                    }
                }
            }
        }
    }
}
