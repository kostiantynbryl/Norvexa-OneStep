package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.norvexa.onestep.R
import com.norvexa.onestep.model.AppData
import com.norvexa.onestep.model.GoalStatus
import com.norvexa.onestep.model.completedStepsCount
import com.norvexa.onestep.model.currentStep
import com.norvexa.onestep.model.progressFraction
import com.norvexa.onestep.model.totalStepsCount

@Composable
fun TodayScreen(
    data: AppData,
    onCreateGoal: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onFocus: (String, String) -> Unit,
    onComplete: (String, String, Int) -> Unit,
    onPostpone: (String, String) -> Unit,
    onSplit: (String, String) -> Unit,
    onSkip: (String, String) -> Unit,
) {
    val goal = data.goals.firstOrNull { it.status == GoalStatus.ACTIVE }
    val createGoalDescription = stringResource(R.string.accessibility_create_goal)
    val stepDoneDescription = stringResource(R.string.accessibility_step_done)
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGoal,
                modifier = Modifier.semantics { contentDescription = createGoalDescription },
            ) { Icon(Icons.Default.Add, contentDescription = null) }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(stringResource(R.string.today), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (goal == null) {
                EmptyToday(onCreateGoal)
                return@Column
            }
            Text(goal.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(progress = { goal.progressFraction() }, modifier = Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.completed_format, goal.completedStepsCount(), goal.totalStepsCount()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val step = goal.currentStep()
            if (step == null) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.all_done_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.all_done_body), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(R.string.current_step), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(step.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.minutes_format, step.estimatedMinutes), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { onFocus(goal.id, step.id) }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text(stringResource(R.string.focus))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = { onComplete(goal.id, step.id, step.estimatedMinutes) },
                                modifier = Modifier.weight(1f).semantics { contentDescription = stepDoneDescription },
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Text(stringResource(R.string.done))
                            }
                            OutlinedButton(onClick = { onPostpone(goal.id, step.id) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                                Text(stringResource(R.string.postpone))
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { onSplit(goal.id, step.id) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.CallSplit, contentDescription = null)
                                Text(stringResource(R.string.split_smaller))
                            }
                            OutlinedButton(onClick = { onSkip(goal.id, step.id) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.SkipNext, contentDescription = null)
                                Text(stringResource(R.string.skip))
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = { onOpenGoal(goal.id) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.goal_details))
            }
        }
    }
}

@Composable
fun EmptyToday(onCreateGoal: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.no_goals_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(stringResource(R.string.no_goals_body), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onClick = onCreateGoal) { Text(stringResource(R.string.new_goal)) }
    }
}
