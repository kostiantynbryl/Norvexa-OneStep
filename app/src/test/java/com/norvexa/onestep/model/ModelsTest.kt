package com.norvexa.onestep.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {
    @Test
    fun currentStepPrefersInProgressStep() {
        val todo = Step(title = "Todo")
        val active = Step(title = "Active", status = StepStatus.IN_PROGRESS)
        val goal = Goal(title = "Goal", stages = listOf(Stage(title = "Stage", steps = listOf(todo, active))))

        assertEquals(active.id, goal.currentStep()?.id)
    }

    @Test
    fun postponedStepDoesNotBlockNextAvailableStep() {
        val postponed = Step(title = "Later", scheduledAt = System.currentTimeMillis() + DAY_MILLIS)
        val available = Step(title = "Now")
        val goal = Goal(title = "Goal", stages = listOf(Stage(title = "Stage", steps = listOf(postponed, available))))

        assertEquals(available.id, goal.currentStep()?.id)
    }

    @Test
    fun completedAndSkippedStepsFinishGoalPlan() {
        val goal = Goal(
            title = "Goal",
            stages = listOf(
                Stage(
                    title = "Stage",
                    steps = listOf(
                        Step(title = "Done", status = StepStatus.COMPLETED),
                        Step(title = "Skipped", status = StepStatus.SKIPPED),
                    ),
                ),
            ),
        )

        assertTrue(goal.isDone())
        assertEquals(1f, goal.progressFraction())
    }

    @Test
    fun emptyGoalHasNoCurrentStep() {
        assertNull(Goal(title = "Empty").currentStep())
    }

    @Test
    fun templatesUseRequestedLanguage() {
        val stages = GoalTemplates.stagesFor(GoalCategory.DEVELOPMENT, "Build app", "en")

        assertEquals("Preparation: Build app", stages.first().title)
        assertTrue(stages.first().steps.first().title.startsWith("Define"))
    }
}
