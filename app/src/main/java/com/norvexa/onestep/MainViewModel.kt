package com.norvexa.onestep

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.norvexa.onestep.model.AppData
import com.norvexa.onestep.model.GoalCategory
import com.norvexa.onestep.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as OneStepApplication).repository
    val data: StateFlow<AppData> = repository.data
    val isReady: StateFlow<Boolean> = repository.isReady

    fun completeOnboarding() = launch { repository.completeOnboarding() }

    fun createGoal(
        title: String,
        description: String,
        category: GoalCategory,
        dailyMinutes: Int,
        deadlineDays: Int,
        onCreated: (String) -> Unit,
    ) = viewModelScope.launch {
        val id = repository.createGoal(title, description, category, dailyMinutes, deadlineDays)
        onCreated(id)
    }

    fun updateGoal(goalId: String, title: String, description: String, category: GoalCategory) =
        launch { repository.updateGoal(goalId, title, description, category) }

    fun addStage(goalId: String, title: String) = launch { repository.addStage(goalId, title) }
    fun addStep(goalId: String, stageId: String, title: String, description: String, estimatedMinutes: Int) =
        launch { repository.addStep(goalId, stageId, title, description, estimatedMinutes) }
    fun updateStepDetails(goalId: String, stepId: String, title: String, description: String, estimatedMinutes: Int) =
        launch { repository.updateStepDetails(goalId, stepId, title, description, estimatedMinutes) }
    fun deleteStep(goalId: String, stepId: String) = launch { repository.deleteStep(goalId, stepId) }
    fun deleteStage(goalId: String, stageId: String) = launch { repository.deleteStage(goalId, stageId) }
    fun moveStep(goalId: String, stageId: String, stepId: String, direction: Int) =
        launch { repository.moveStep(goalId, stageId, stepId, direction) }

    fun activateGoal(goalId: String) = launch { repository.activateGoal(goalId) }
    fun togglePause(goalId: String) = launch { repository.togglePause(goalId) }
    fun deleteGoal(goalId: String) = launch { repository.deleteGoal(goalId) }
    fun completeStep(goalId: String, stepId: String, actualMinutes: Int = 0) =
        launch { repository.completeStep(goalId, stepId, actualMinutes) }
    fun skipStep(goalId: String, stepId: String) = launch { repository.skipStep(goalId, stepId) }
    fun postponeStep(goalId: String, stepId: String) = launch { repository.postponeStep(goalId, stepId) }
    fun startStep(goalId: String, stepId: String) = launch { repository.startStep(goalId, stepId) }
    fun splitStep(goalId: String, stepId: String) = launch { repository.splitStep(goalId, stepId) }
    fun updateTheme(mode: ThemeMode) = launch { repository.updateTheme(mode) }
    fun updateLanguage(tag: String) = launch { repository.updateLanguage(tag) }
    fun updateReminders(enabled: Boolean) = launch { repository.updateReminders(enabled) }

    fun exportJson(): String = repository.exportJson()

    fun importJson(raw: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        onResult(repository.importJson(raw))
    }

    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
}
