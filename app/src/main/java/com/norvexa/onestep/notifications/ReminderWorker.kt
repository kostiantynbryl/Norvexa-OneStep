package com.norvexa.onestep.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.norvexa.onestep.OneStepApplication
import com.norvexa.onestep.model.GoalStatus
import com.norvexa.onestep.model.currentStep

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as OneStepApplication
        val data = app.repository.getSnapshot()
        if (!data.settings.remindersEnabled) return Result.success()
        val goal = data.goals.firstOrNull { it.status == GoalStatus.ACTIVE } ?: return Result.success()
        val step = goal.currentStep() ?: return Result.success()
        NotificationHelper.showNextStep(applicationContext, goal.title, step.title)
        return Result.success()
    }
}
