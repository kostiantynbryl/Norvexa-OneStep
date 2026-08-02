package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
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
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        SettingsSection(stringResource(R.string.appearance)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = settings.themeMode == mode,
                    onClick = { onThemeChanged(mode) },
                    label = { Text(stringResource(mode.labelResource())) },
                )
            }
        }
        SettingsSection(stringResource(R.string.language)) {
            listOf(
                "" to stringResource(R.string.language_system),
                "ru" to "Русский",
                "uk" to "Українська",
                "en" to "English",
                "pl" to "Polski",
            ).forEach { (tag, label) ->
                FilterChip(
                    selected = settings.languageTag == tag,
                    onClick = { onLanguageChanged(tag) },
                    label = { Text(label) },
                )
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.reminders), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.daily_reminder), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.remindersEnabled, onCheckedChange = onReminderChanged)
            }
        }
        Text(stringResource(R.string.backup), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Download, contentDescription = null)
                Text(stringResource(R.string.export_json))
            }
            OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Text(stringResource(R.string.import_json))
            }
        }
        Text(stringResource(R.string.about), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Norvexa OneStep\n${stringResource(R.string.version_format, BuildConfig.VERSION_NAME)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

private fun ThemeMode.labelResource(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
    ThemeMode.AMOLED -> R.string.theme_amoled
}
