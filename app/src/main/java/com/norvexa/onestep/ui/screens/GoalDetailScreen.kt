package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
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
    var expandedStepMenuId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(goal.title, style = MaterialTheme.typography.headlineMedium)
                            if (goal.description.isNotBlank()) {
                                Text(
                                    goal.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { goal.progressFraction() },
                            modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (goal.status == GoalStatus.ACTIVE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Text(
                                    stringResource(goal.status.labelResource()),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (goal.status == GoalStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                stringResource(R.string.completed_format, goal.completedStepsCount(), goal.totalStepsCount()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (goal.status != GoalStatus.COMPLETED) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (goal.status != GoalStatus.ACTIVE) {
                                    Button(
                                        onClick = onActivate,
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = MaterialTheme.shapes.medium,
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Text(stringResource(R.string.activate))
                                    }
                                }
                                OutlinedButton(
                                    onClick = onTogglePause,
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = MaterialTheme.shapes.medium,
                                ) {
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
            }

            goal.stages.forEach { stage ->
                item(key = "header-${stage.id}") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stage.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { stepEditorTarget = StepEditorTarget(stage.id, null) }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_step))
                        }
                        IconButton(onClick = { onDeleteStage(stage.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_stage),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                items(stage.steps, key = { it.id }) { step ->
                    StepRow(
                        step = step,
                        onClick = if (step.status == StepStatus.TODO || step.status == StepStatus.IN_PROGRESS) {
                            { onFocus(step.id) }
                        } else null,
                        trailing = {
                            Box {
                                IconButton(onClick = { expandedStepMenuId = step.id }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = stringResource(R.string.edit_step),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                DropdownMenu(
                                    expanded = expandedStepMenuId == step.id,
                                    onDismissRequest = { expandedStepMenuId = null },
                                ) {
                                    if (step.status == StepStatus.TODO || step.status == StepStatus.IN_PROGRESS) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.done)) },
                                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                                            onClick = {
                                                expandedStepMenuId = null
                                                onComplete(step.id)
                                            },
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.move_up)) },
                                        leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) },
                                        onClick = {
                                            expandedStepMenuId = null
                                            onMoveStep(stage.id, step.id, -1)
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.move_down)) },
                                        leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                                        onClick = {
                                            expandedStepMenuId = null
                                            onMoveStep(stage.id, step.id, 1)
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.edit_step)) },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                        onClick = {
                                            expandedStepMenuId = null
                                            stepEditorTarget = StepEditorTarget(stage.id, step)
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.delete_step), color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            expandedStepMenuId = null
                                            onDeleteStep(step.id)
                                        },
                                    )
                                }
                            }
                        },
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { showStageEditor = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(R.string.add_stage))
                }
            }
            item {
                TextButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text(stringResource(R.string.delete))
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
                TextButton(
                    onClick = { confirmDelete = false; onDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
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
                shape = MaterialTheme.shapes.medium,
            )
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onSave(title.trim()) }, enabled = title.isNotBlank()) {
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
                    shape = MaterialTheme.shapes.medium,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.step_description)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit).take(3) },
                    label = { Text(stringResource(R.string.estimated_minutes)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), description.trim(), minutes.toIntOrNull() ?: 15) },
                enabled = title.isNotBlank(),
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
