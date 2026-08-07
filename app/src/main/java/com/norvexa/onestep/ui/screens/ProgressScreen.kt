package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.norvexa.onestep.R
import com.norvexa.onestep.model.AppData
import com.norvexa.onestep.model.GoalStatus
import com.norvexa.onestep.model.StepStatus
import com.norvexa.onestep.model.allSteps
import com.norvexa.onestep.model.calculateStreak

@Composable
fun ProgressScreen(data: AppData) {
    val completedGoals = data.goals.count { it.status == GoalStatus.COMPLETED }
    val completedSteps = data.goals.sumOf { goal -> goal.allSteps().count { it.status == StepStatus.COMPLETED } }
    val streak = calculateStreak(data.activityDates)
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.progress), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(stringResource(R.string.total_goals), data.goals.size, Modifier.weight(1f))
            MetricCard(stringResource(R.string.completed_goals), completedGoals, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(stringResource(R.string.completed_steps), completedSteps, Modifier.weight(1f))
            MetricCard(stringResource(R.string.streak), streak, Modifier.weight(1f))
        }
        data.goals.sortedByDescending { it.createdAt }.forEach { goal ->
            GoalSummaryCard(goal = goal, onClick = {})
        }
    }
}

@Composable
private fun MetricCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(value.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
