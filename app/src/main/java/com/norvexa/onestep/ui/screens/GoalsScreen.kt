package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    val createDescription = stringResource(R.string.accessibility_create_goal)

    if (goals.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            PageHeader(
                title = stringResource(R.string.goals),
                trailing = { AddGoalButton(onCreateGoal, createDescription) },
            )
            EmptyToday(onCreateGoal)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PageHeader(
                title = stringResource(R.string.goals),
                trailing = { AddGoalButton(onCreateGoal, createDescription) },
            )
        }
        items(goals, key = { it.id }) { goal ->
            GoalSummaryCard(
                goal = goal,
                onClick = { onOpenGoal(goal.id) },
                trailing = if (goal.status != GoalStatus.ACTIVE && goal.status != GoalStatus.COMPLETED) {
                    {
                        Surface(
                            onClick = { onActivate(goal.id) },
                            modifier = Modifier
                                .size(40.dp)
                                .semantics { contentDescription = "${goal.title}: activate" },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                } else null,
            )
        }
    }
}

@Composable
private fun AddGoalButton(onCreateGoal: () -> Unit, description: String) {
    Surface(
        onClick = onCreateGoal,
        modifier = Modifier.size(44.dp).semantics { contentDescription = description },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
