package com.norvexa.onestep.model

import java.util.UUID

const val DAY_MILLIS = 24L * 60L * 60L * 1000L

enum class GoalStatus { DRAFT, ACTIVE, PAUSED, COMPLETED, ARCHIVED }
enum class StepStatus { TODO, IN_PROGRESS, COMPLETED, SKIPPED }
enum class GoalCategory { WORK, LEARNING, HEALTH, BUSINESS, FINANCE, DEVELOPMENT, HOME, CREATIVE, OTHER }
enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

data class Step(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val status: StepStatus = StepStatus.TODO,
    val estimatedMinutes: Int = 15,
    val actualMinutes: Int = 0,
    val scheduledAt: Long? = null,
    val completedAt: Long? = null,
    val postponeCount: Int = 0,
    val parentStepId: String? = null,
)

data class Stage(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val steps: List<Step>,
)

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: GoalCategory = GoalCategory.OTHER,
    val status: GoalStatus = GoalStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val deadline: Long? = null,
    val completedAt: Long? = null,
    val availableMinutesPerDay: Int = 25,
    val stages: List<Stage> = emptyList(),
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageTag: String = "",
    val remindersEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
)

data class AppData(
    val onboardingComplete: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val activityDates: Set<Long> = emptySet(),
)

fun Goal.allSteps(): List<Step> = stages.flatMap { it.steps }
fun Goal.completedStepsCount(): Int = allSteps().count { it.status == StepStatus.COMPLETED }
fun Goal.totalStepsCount(): Int = allSteps().size
fun Goal.progressFraction(): Float = if (totalStepsCount() == 0) 0f else allSteps().count { it.status == StepStatus.COMPLETED || it.status == StepStatus.SKIPPED }.toFloat() / totalStepsCount()
fun Goal.currentStep(now: Long = System.currentTimeMillis()): Step? {
    val steps = allSteps()
    return steps.firstOrNull { it.status == StepStatus.IN_PROGRESS }
        ?: steps.firstOrNull { it.status == StepStatus.TODO && (it.scheduledAt == null || it.scheduledAt <= now) }
}
fun Goal.isDone(): Boolean = allSteps().isNotEmpty() && allSteps().all { it.status == StepStatus.COMPLETED || it.status == StepStatus.SKIPPED }

fun startOfDay(timestamp: Long = System.currentTimeMillis()): Long {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

fun calculateStreak(activityDates: Set<Long>, today: Long = startOfDay()): Int {
    if (activityDates.isEmpty()) return 0
    var cursor = today
    var streak = 0
    while (activityDates.contains(cursor)) {
        streak++
        cursor -= DAY_MILLIS
    }
    return streak
}
