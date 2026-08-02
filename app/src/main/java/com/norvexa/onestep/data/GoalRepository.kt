package com.norvexa.onestep.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.norvexa.onestep.model.AppData
import com.norvexa.onestep.model.AppSettings
import com.norvexa.onestep.model.DAY_MILLIS
import com.norvexa.onestep.model.Goal
import com.norvexa.onestep.model.GoalCategory
import com.norvexa.onestep.model.GoalStatus
import com.norvexa.onestep.model.GoalTemplates
import com.norvexa.onestep.model.Step
import com.norvexa.onestep.model.StepStatus
import com.norvexa.onestep.model.ThemeMode
import com.norvexa.onestep.model.isDone
import com.norvexa.onestep.model.startOfDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import java.util.UUID

private val Context.oneStepDataStore by preferencesDataStore(name = "onestep_data")

class GoalRepository(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataKey = stringPreferencesKey("app_data_json")
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    val data: StateFlow<AppData> = appContext.oneStepDataStore.data
        .map { AppDataCodec.decode(it[dataKey]) }
        .onEach { _isReady.value = true }
        .stateIn(scope, SharingStarted.Eagerly, AppData())

    suspend fun completeOnboarding() = update { it.copy(onboardingComplete = true) }

    suspend fun createGoal(
        title: String,
        description: String,
        category: GoalCategory,
        dailyMinutes: Int,
        deadlineDays: Int,
    ): String {
        val id = UUID.randomUUID().toString()
        update { current ->
            val hasActive = current.goals.any { it.status == GoalStatus.ACTIVE }
            val goal = Goal(
                id = id,
                title = title.trim(),
                description = description.trim(),
                category = category,
                status = if (hasActive) GoalStatus.DRAFT else GoalStatus.ACTIVE,
                deadline = System.currentTimeMillis() + deadlineDays.coerceAtLeast(1) * DAY_MILLIS,
                availableMinutesPerDay = dailyMinutes.coerceIn(5, 240),
                stages = GoalTemplates.stagesFor(
                    category = category,
                    title = title,
                    languageTag = current.settings.languageTag.ifBlank { Locale.getDefault().language },
                ),
            )
            current.copy(goals = current.goals + goal)
        }
        return id
    }

    suspend fun updateGoal(goalId: String, title: String, description: String, category: GoalCategory) =
        updateGoalById(goalId) { it.copy(title = title.trim(), description = description.trim(), category = category) }

    suspend fun addStage(goalId: String, title: String) = updateGoalById(goalId) { goal ->
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) goal else goal.copy(
            stages = goal.stages + com.norvexa.onestep.model.Stage(title = cleanTitle, steps = emptyList()),
        )
    }

    suspend fun addStep(
        goalId: String,
        stageId: String,
        title: String,
        description: String,
        estimatedMinutes: Int,
    ) = updateGoalById(goalId) { goal ->
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return@updateGoalById goal
        goal.copy(stages = goal.stages.map { stage ->
            if (stage.id == stageId) stage.copy(
                steps = stage.steps + Step(
                    title = cleanTitle,
                    description = description.trim(),
                    estimatedMinutes = estimatedMinutes.coerceIn(5, 240),
                ),
            ) else stage
        })
    }

    suspend fun updateStepDetails(
        goalId: String,
        stepId: String,
        title: String,
        description: String,
        estimatedMinutes: Int,
    ) = updateStep(goalId, stepId) { step ->
        step.copy(
            title = title.trim().ifBlank { step.title },
            description = description.trim(),
            estimatedMinutes = estimatedMinutes.coerceIn(5, 240),
        )
    }

    suspend fun deleteStep(goalId: String, stepId: String) = updateGoalById(goalId) { goal ->
        goal.copy(stages = goal.stages.map { stage ->
            stage.copy(steps = stage.steps.filterNot { it.id == stepId })
        })
    }

    suspend fun deleteStage(goalId: String, stageId: String) = updateGoalById(goalId) { goal ->
        goal.copy(stages = goal.stages.filterNot { it.id == stageId })
    }

    suspend fun moveStep(goalId: String, stageId: String, stepId: String, direction: Int) =
        updateGoalById(goalId) { goal ->
            goal.copy(stages = goal.stages.map stageMap@{ stage ->
                if (stage.id != stageId) return@stageMap stage
                val from = stage.steps.indexOfFirst { it.id == stepId }
                if (from < 0) return@stageMap stage
                val to = (from + direction).coerceIn(0, stage.steps.lastIndex)
                if (from == to) return@stageMap stage
                val reordered = stage.steps.toMutableList()
                val item = reordered.removeAt(from)
                reordered.add(to, item)
                stage.copy(steps = reordered)
            })
        }

    suspend fun activateGoal(goalId: String) = update { current ->
        current.copy(goals = current.goals.map { goal ->
            when {
                goal.id == goalId -> goal.copy(status = GoalStatus.ACTIVE)
                goal.status == GoalStatus.ACTIVE -> goal.copy(status = GoalStatus.PAUSED)
                else -> goal
            }
        })
    }

    suspend fun togglePause(goalId: String) = update { current ->
        val target = current.goals.firstOrNull { it.id == goalId } ?: return@update current
        if (target.status == GoalStatus.PAUSED || target.status == GoalStatus.DRAFT) {
            current.copy(goals = current.goals.map { goal ->
                when {
                    goal.id == goalId -> goal.copy(status = GoalStatus.ACTIVE)
                    goal.status == GoalStatus.ACTIVE -> goal.copy(status = GoalStatus.PAUSED)
                    else -> goal
                }
            })
        } else {
            current.copy(goals = current.goals.map { if (it.id == goalId) it.copy(status = GoalStatus.PAUSED) else it })
        }
    }

    suspend fun deleteGoal(goalId: String) = update { current ->
        val remaining = current.goals.filterNot { it.id == goalId }
        if (remaining.none { it.status == GoalStatus.ACTIVE }) {
            val firstAvailable = remaining.firstOrNull { it.status == GoalStatus.DRAFT || it.status == GoalStatus.PAUSED }
            current.copy(goals = remaining.map { if (it.id == firstAvailable?.id) it.copy(status = GoalStatus.ACTIVE) else it })
        } else current.copy(goals = remaining)
    }

    suspend fun completeStep(goalId: String, stepId: String, actualMinutes: Int = 0) = update { current ->
        val now = System.currentTimeMillis()
        val updatedGoals = current.goals.map goalMap@{ goal ->
            if (goal.id != goalId) return@goalMap goal
            val updated = goal.copy(stages = goal.stages.map { stage ->
                stage.copy(steps = stage.steps.map { step ->
                    if (step.id == stepId) step.copy(
                        status = StepStatus.COMPLETED,
                        completedAt = now,
                        actualMinutes = maxOf(step.actualMinutes, actualMinutes),
                    ) else step
                })
            })
            if (updated.isDone()) updated.copy(status = GoalStatus.COMPLETED, completedAt = now) else updated
        }
        current.copy(
            goals = updatedGoals,
            activityDates = current.activityDates + startOfDay(now),
        )
    }

    suspend fun skipStep(goalId: String, stepId: String) = update { current ->
        val now = System.currentTimeMillis()
        val updatedGoals = current.goals.map goalMap@{ goal ->
            if (goal.id != goalId) return@goalMap goal
            val updated = goal.copy(stages = goal.stages.map { stage ->
                stage.copy(steps = stage.steps.map { step ->
                    if (step.id == stepId) step.copy(status = StepStatus.SKIPPED, completedAt = now) else step
                })
            })
            if (updated.isDone()) updated.copy(status = GoalStatus.COMPLETED, completedAt = now) else updated
        }
        current.copy(goals = updatedGoals, activityDates = current.activityDates + startOfDay(now))
    }

    suspend fun postponeStep(goalId: String, stepId: String) = updateStep(goalId, stepId) {
        it.copy(
            status = StepStatus.TODO,
            scheduledAt = startOfDay() + DAY_MILLIS,
            postponeCount = it.postponeCount + 1,
        )
    }

    suspend fun startStep(goalId: String, stepId: String) = updateStep(goalId, stepId) {
        it.copy(status = StepStatus.IN_PROGRESS)
    }

    suspend fun splitStep(goalId: String, stepId: String) = update { current ->
        current.copy(goals = current.goals.map goalMap@{ goal ->
            if (goal.id != goalId) return@goalMap goal
            goal.copy(stages = goal.stages.map stageMap@{ stage ->
                val index = stage.steps.indexOfFirst { it.id == stepId }
                if (index < 0) return@stageMap stage
                val source = stage.steps[index]
                val chunk = maxOf(5, source.estimatedMinutes / 3)
                val language = current.settings.languageTag.ifBlank { Locale.getDefault().language }
                val replacements = GoalTemplates.splitTitles(source.title, language).map { title ->
                    Step(title = title, estimatedMinutes = chunk, parentStepId = source.id)
                }
                stage.copy(steps = stage.steps.take(index) + replacements + stage.steps.drop(index + 1))
            })
        })
    }

    suspend fun updateTheme(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }
    suspend fun updateLanguage(tag: String) = updateSettings { it.copy(languageTag = tag) }
    suspend fun updateReminders(enabled: Boolean) = updateSettings { it.copy(remindersEnabled = enabled) }

    fun exportJson(): String = AppDataCodec.encode(data.value)

    suspend fun getSnapshot(): AppData = appContext.oneStepDataStore.data
        .map { AppDataCodec.decode(it[dataKey]) }
        .first()

    suspend fun importJson(raw: String): Boolean {
        val decoded = AppDataCodec.decode(raw)
        if (raw.isBlank() || (!raw.contains("schemaVersion") && decoded == AppData())) return false
        appContext.oneStepDataStore.edit { it[dataKey] = AppDataCodec.encode(decoded) }
        return true
    }

    private suspend fun updateStep(goalId: String, stepId: String, transform: (Step) -> Step) = update { current ->
        current.copy(goals = current.goals.map goalMap@{ goal ->
            if (goal.id != goalId) return@goalMap goal
            goal.copy(stages = goal.stages.map { stage ->
                stage.copy(steps = stage.steps.map { step -> if (step.id == stepId) transform(step) else step })
            })
        })
    }

    private suspend fun updateGoalById(goalId: String, transform: (Goal) -> Goal) = update { current ->
        current.copy(goals = current.goals.map { if (it.id == goalId) transform(it) else it })
    }

    private suspend fun updateSettings(transform: (AppSettings) -> AppSettings) = update {
        it.copy(settings = transform(it.settings))
    }

    private suspend fun update(transform: (AppData) -> AppData) {
        appContext.oneStepDataStore.edit { preferences ->
            val current = AppDataCodec.decode(preferences[dataKey])
            preferences[dataKey] = AppDataCodec.encode(transform(current))
        }
    }
}
