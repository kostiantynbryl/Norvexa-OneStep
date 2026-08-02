package com.norvexa.onestep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.norvexa.onestep.notifications.ReminderScheduler
import com.norvexa.onestep.ui.OneStepRoot
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var pendingExport: String = ""
    private var pendingReminderEnable = false

    private val createBackup = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(pendingExport) }
            }.onFailure { Toast.makeText(this, R.string.import_error, Toast.LENGTH_SHORT).show() }
        }
    }

    private val openBackup = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val raw = runCatching {
                contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
            }.getOrDefault("")
            viewModel.importJson(raw) { success ->
                Toast.makeText(this, if (success) R.string.import_success else R.string.import_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingReminderEnable) {
            viewModel.updateReminders(true)
            ReminderScheduler.schedule(this)
        }
        pendingReminderEnable = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.data.map { it.settings.languageTag }.distinctUntilChanged().collect { tag ->
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                    }
                }
                launch {
                    viewModel.data.map { it.settings.remindersEnabled }.distinctUntilChanged().collect { enabled ->
                        if (enabled) ReminderScheduler.schedule(this@MainActivity) else ReminderScheduler.cancel(this@MainActivity)
                    }
                }
            }
        }

        setContent {
            OneStepRoot(
                viewModel = viewModel,
                onExport = {
                    pendingExport = viewModel.exportJson()
                    createBackup.launch("OneStep-backup.json")
                },
                onImport = { openBackup.launch(arrayOf("application/json", "text/plain")) },
                onReminderChanged = ::changeReminderState,
            )
        }
    }

    private fun changeReminderState(enabled: Boolean) {
        if (!enabled) {
            viewModel.updateReminders(false)
            ReminderScheduler.cancel(this)
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.updateReminders(true)
            ReminderScheduler.schedule(this)
        } else {
            pendingReminderEnable = true
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
