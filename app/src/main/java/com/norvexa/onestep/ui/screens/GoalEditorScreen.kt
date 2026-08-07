package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.norvexa.onestep.R
import com.norvexa.onestep.model.Goal
import com.norvexa.onestep.model.GoalCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditorScreen(
    existing: Goal?,
    onBack: () -> Unit,
    onCreate: (String, String, GoalCategory, Int, Int) -> Unit,
    onUpdate: (String, String, GoalCategory) -> Unit,
) {
    var title by rememberSaveable(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var description by rememberSaveable(existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var category by remember(existing?.id) { mutableStateOf(existing?.category ?: GoalCategory.OTHER) }
    var dailyMinutes by rememberSaveable { mutableStateOf(existing?.availableMinutesPerDay?.toString() ?: "25") }
    var deadlineDays by rememberSaveable { mutableStateOf("30") }
    var showError by rememberSaveable { mutableStateOf(false) }

    val save: () -> Unit = {
        if (title.isBlank()) {
            showError = true
        } else if (existing == null) {
            onCreate(
                title.trim(),
                description.trim(),
                category,
                dailyMinutes.toIntOrNull() ?: 25,
                deadlineDays.toIntOrNull() ?: 30,
            )
        } else {
            onUpdate(title.trim(), description.trim(), category)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (existing == null) R.string.new_goal else R.string.edit_goal)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(onClick = save) {
                        Text(stringResource(R.string.save))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                EditorCard {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; showError = false },
                        label = { Text(stringResource(R.string.goal_title)) },
                        supportingText = if (showError) ({ Text(stringResource(R.string.required_field)) }) else null,
                        isError = showError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.goal_description)) },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(stringResource(R.string.category))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(GoalCategory.entries) { item ->
                            FilterChip(
                                selected = item == category,
                                onClick = { category = item },
                                label = { Text(stringResource(item.labelResource())) },
                            )
                        }
                    }
                }
            }

            if (existing == null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel(stringResource(R.string.daily_minutes))
                        EditorCard {
                            OutlinedTextField(
                                value = dailyMinutes,
                                onValueChange = { dailyMinutes = it.filter(Char::isDigit).take(3) },
                                label = { Text(stringResource(R.string.daily_minutes)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                            )
                            OutlinedTextField(
                                value = deadlineDays,
                                onValueChange = { deadlineDays = it.filter(Char::isDigit).take(4) },
                                label = { Text(stringResource(R.string.deadline_days)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                            )
                            Text(
                                stringResource(R.string.template_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

private fun GoalCategory.labelResource(): Int = when (this) {
    GoalCategory.WORK -> R.string.category_work
    GoalCategory.LEARNING -> R.string.category_learning
    GoalCategory.HEALTH -> R.string.category_health
    GoalCategory.BUSINESS -> R.string.category_business
    GoalCategory.FINANCE -> R.string.category_finance
    GoalCategory.DEVELOPMENT -> R.string.category_development
    GoalCategory.HOME -> R.string.category_home
    GoalCategory.CREATIVE -> R.string.category_creative
    GoalCategory.OTHER -> R.string.category_other
}
