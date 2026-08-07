package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.norvexa.onestep.R
import com.norvexa.onestep.model.Goal
import com.norvexa.onestep.model.GoalStatus
import com.norvexa.onestep.model.Step
import com.norvexa.onestep.model.StepStatus
import com.norvexa.onestep.model.completedStepsCount
import com.norvexa.onestep.model.progressFraction
import com.norvexa.onestep.model.totalStepsCount

@Composable
fun GoalSummaryCard(goal: Goal, onClick: () -> Unit, trailing: @Composable (() -> Unit)? = null) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (goal.description.isNotBlank()) {
                        Text(goal.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                trailing?.invoke()
            }
            LinearProgressIndicator(progress = { goal.progressFraction() }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = onClick, label = { Text(stringResource(goal.status.labelResource())) })
                Text(
                    "${goal.completedStepsCount()} / ${goal.totalStepsCount()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun StepRow(step: Step, onClick: (() -> Unit)? = null) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when (step.status) {
                    StepStatus.COMPLETED -> Icons.Default.CheckCircle
                    StepStatus.IN_PROGRESS -> Icons.Default.Schedule
                    else -> Icons.Default.RadioButtonUnchecked
                },
                contentDescription = null,
                tint = when (step.status) {
                    StepStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    StepStatus.SKIPPED -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Column(Modifier.weight(1f)) {
                Text(step.title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (step.status == StepStatus.IN_PROGRESS) FontWeight.Bold else FontWeight.Normal)
                Text(stringResource(R.string.minutes_format, step.estimatedMinutes), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (onClick != null) Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) { content() } else Card(Modifier.fillMaxWidth()) { content() }
}

fun GoalStatus.labelResource(): Int = when (this) {
    GoalStatus.DRAFT -> R.string.draft
    GoalStatus.ACTIVE -> R.string.active
    GoalStatus.PAUSED -> R.string.paused
    GoalStatus.COMPLETED -> R.string.completed
    GoalStatus.ARCHIVED -> R.string.archived
}
