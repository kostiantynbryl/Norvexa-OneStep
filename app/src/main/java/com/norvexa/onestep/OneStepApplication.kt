package com.norvexa.onestep

import android.app.Application
import com.norvexa.onestep.data.GoalRepository
import com.norvexa.onestep.notifications.NotificationHelper

class OneStepApplication : Application() {
    val repository: GoalRepository by lazy { GoalRepository(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
