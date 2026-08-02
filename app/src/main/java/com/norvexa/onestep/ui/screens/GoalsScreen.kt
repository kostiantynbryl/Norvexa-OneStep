package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.norvexa.onestep.R
import com.norvexa.onestep.model.Goal
import com.norvexa.onestep.model.GoalStatus

@Composable
fun GoalsScreen(
    goals: List<Goal>,
    onCreateGoal: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onActivate: (String) -> Unit,
) {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onCreateGoal) { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_goal)) } },
    ) { padding ->
        if (goals.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text(stringResource(R.string.goals), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                EmptyToday(onCreateGoal)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { Text(stringResource(R.string.goals), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
                items(goals, key = { it.id }) { goal ->
                    GoalSummaryCard(
                        goal = goal,
                        onClick = { onOpenGoal(goal.id) },
                        trailing = if (goal.status != GoalStatus.ACTIVE && goal.status != GoalStatus.COMPLETED) {
                            { FilledTonalButton(onClick = { onActivate(goal.id) }) { Text(stringResource(R.string.activate)) } }
                        } else null,
                    )
                }
            }
        }
    }
}
