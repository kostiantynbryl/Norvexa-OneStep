package com.norvexa.onestep.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norvexa.onestep.R
import com.norvexa.onestep.model.Goal
import com.norvexa.onestep.model.Step
import kotlinx.coroutines.delay
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    goal: Goal?,
    step: Step?,
    onBack: () -> Unit,
    onStarted: () -> Unit,
    onComplete: (Int) -> Unit,
) {
    val totalSeconds = max(60, (step?.estimatedMinutes ?: 15) * 60)
    var remaining by rememberSaveable(step?.id) { mutableIntStateOf(totalSeconds) }
    var running by rememberSaveable(step?.id) { mutableStateOf(true) }

    LaunchedEffect(step?.id) { if (step != null) onStarted() }
    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.focus_session)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back)) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(goal?.title.orEmpty(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(step?.title ?: "—", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(42.dp))
            Text(formatTime(remaining), fontSize = 68.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(30.dp))
            FilledTonalButton(onClick = { running = !running }, modifier = Modifier.fillMaxWidth()) {
                Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Text(stringResource(if (running) R.string.pause else R.string.resume))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val elapsedMinutes = max(1, (totalSeconds - remaining + 59) / 60)
                    onComplete(elapsedMinutes)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Text(stringResource(R.string.finish_session))
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
