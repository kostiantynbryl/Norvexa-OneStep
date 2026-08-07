package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.norvexa.onestep.BuildConfig
import com.norvexa.onestep.R
import com.norvexa.onestep.model.AppSettings
import com.norvexa.onestep.model.ThemeMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onReminderChanged: (Boolean) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    val languages = listOf(
        "" to stringResource(R.string.language_system),
        "ru" to "Русский",
        "uk" to "Українська",
        "en" to "English",
        "pl" to "Polski",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PageHeader(title = stringResource(R.string.settings))

        SettingsGroup(stringResource(R.string.appearance)) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SelectionRow(
                    label = stringResource(mode.labelResource()),
                    selected = settings.themeMode == mode,
                    onClick = { onThemeChanged(mode) },
                )
                if (index != ThemeMode.entries.lastIndex) GroupDivider()
            }
        }

        SettingsGroup(stringResource(R.string.language)) {
            languages.forEachIndexed { index, (tag, label) ->
                SelectionRow(
                    label = label,
                    selected = settings.languageTag == tag,
                    onClick = { onLanguageChanged(tag) },
                )
                if (index != languages.lastIndex) GroupDivider()
            }
        }

        SettingsGroup(stringResource(R.string.reminders)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReminderChanged(!settings.remindersEnabled) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(R.string.reminders), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.daily_reminder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = settings.remindersEnabled, onCheckedChange = onReminderChanged)
            }
        }

        SettingsGroup(stringResource(R.string.backup)) {
            ActionRow(
                label = stringResource(R.string.export_json),
                icon = { Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                onClick = onExport,
            )
            GroupDivider()
            ActionRow(
                label = stringResource(R.string.import_json),
                icon = { Icon(Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                onClick = onImport,
            )
        }

        SettingsGroup(stringResource(R.string.about)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text("Norvexa OneStep", style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(title)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SelectionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ActionRow(label: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

private fun ThemeMode.labelResource(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
    ThemeMode.AMOLED -> R.string.theme_amoled
}
