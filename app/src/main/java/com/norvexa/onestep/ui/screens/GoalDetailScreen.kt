package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.norvexa.onestep.R
import com.norvexa.onestep.model.Goal
import com.norvexa.onestep.model.GoalStatus
import com.norvexa.onestep.model.Step
import com.norvexa.onestep.model.StepStatus
import com.norvexa.onestep.model.completedStepsCount
import com.norvexa.onestep.model.progressFraction
import com.norvexa.onestep.model.totalStepsCount

private data class StepEditorTarget(val stageId: String, val step: Step?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goal: Goal?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onFocus: (String) -> Unit,
    onActivate: () -> Unit,
    onTogglePause: () -> Unit,
    onComplete: (String) -> Unit,
    onAddStage: (String) -> Unit,
    onDeleteStage: (String) -> Unit,
    onAddStep: (String, String, String, Int) -> Unit,
    onEditStep: (String, String, String, Int) -> Unit,
    onDeleteStep: (String) -> Unit,
    onMoveStep: (String, String, Int) -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showStageEditor by remember { mutableStateOf(false) }
    var stepEditorTarget by remember { mutableStateOf<StepEditorTarget?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.goal_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showStageEditor = true }, enabled = goal != null) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_stage))
                    }
                    IconButton(onClick = onEdit, enabled = goal != null) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_goal))
                    }
                    IconButton(onClick = { confirmDelete = true }, enabled = goal != null) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                },
            )
        },
    ) { padding ->
        if (goal == null) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text(stringResource(R.string.goal_not_found))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(goal.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    if (goal.description.isNotBlank()) {
                        Text(goal.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LinearProgressIndicator(progress = { goal.progressFraction() }, modifier = Modifier.fillMaxWidth())
                    Text(stringResource(R.string.completed_format, goal.completedStepsCount(), goal.totalStepsCount()))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (goal.status != GoalStatus.ACTIVE && goal.status != GoalStatus.COMPLETED) {
                            Button(onClick = onActivate, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Text(stringResource(R.string.activate))
                            }
                        }
                        if (goal.status != GoalStatus.COMPLETED) {
                            OutlinedButton(onClick = onTogglePause, modifier = Modifier.weight(1f)) {
                                Icon(
                                    if (goal.status == GoalStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                )
                                Text(stringResource(if (goal.status == GoalStatus.PAUSED) R.string.resume else R.string.pause))
                            }
                        }
                    }
                }
            }

            goal.stages.forEach { stage ->
                item(key = stage.id) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stage.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { stepEditorTarget = StepEditorTarget(stage.id, null) }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_step))
                        }
                        IconButton(onClick = { onDeleteStage(stage.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_stage))
                        }
                    }
                }

                items(stage.steps, key = { it.id }) { step ->
                    StepRow(
                        step = step,
                        onClick = if (step.status == StepStatus.TODO || step.status == StepStatus.IN_PROGRESS) {
                            { onFocus(step.id) }
                        } else null,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { onMoveStep(stage.id, step.id, -1) }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.move_up))
                        }
                        IconButton(onClick = { onMoveStep(stage.id, step.id, 1) }) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.move_down))
                        }
                        IconButton(onClick = { stepEditorTarget = StepEditorTarget(stage.id, step) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_step))
                        }
                        IconButton(onClick = { onDeleteStep(step.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_step))
                        }
                        if (step.status == StepStatus.TODO || step.status == StepStatus.IN_PROGRESS) {
                            FilledTonalButton(onClick = { onComplete(step.id) }) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Text(stringResource(R.string.done))
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(onClick = { showStageEditor = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(R.string.add_stage))
                }
            }
        }
    }

    if (showStageEditor) {
        StageEditorDialog(
            onDismiss = { showStageEditor = false },
            onSave = { title ->
                onAddStage(title)
                showStageEditor = false
            },
        )
    }

    stepEditorTarget?.let { target ->
        StepEditorDialog(
            step = target.step,
            onDismiss = { stepEditorTarget = null },
            onSave = { title, description, minutes ->
                if (target.step == null) {
                    onAddStep(target.stageId, title, description, minutes)
                } else {
                    onEditStep(target.step.id, title, description, minutes)
                }
                stepEditorTarget = null
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.delete_confirmation)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun StageEditorDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_stage)) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.stage_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onSave(title) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun StepEditorDialog(step: Step?, onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    var title by rememberSaveable(step?.id) { mutableStateOf(step?.title.orEmpty()) }
    var description by rememberSaveable(step?.id) { mutableStateOf(step?.description.orEmpty()) }
    var minutes by rememberSaveable(step?.id) { mutableStateOf((step?.estimatedMinutes ?: 15).toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (step == null) R.string.add_step else R.string.edit_step)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.step_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.step_description)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit).take(3) },
                    label = { Text(stringResource(R.string.estimated_minutes)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, description, minutes.toIntOrNull() ?: 15) },
                enabled = title.isNotBlank(),
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
