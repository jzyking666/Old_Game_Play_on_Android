package com.retro.arcade.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retro.arcade.core.datastore.AppPreferencesRepository
import com.retro.arcade.core.datastore.UiPreferences
import com.retro.arcade.core.ui.RetroPanel
import com.retro.arcade.feature.snake.model.SnakeControlMode
import com.retro.arcade.feature.snake.model.SnakeDifficulty
import com.retro.arcade.feature.snake.model.SnakePalette
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UiPreferences,
    preferencesRepository: AppPreferencesRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            RetroPanel {
                Text(
                    text = "Default Play Setup",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "These choices are used as the default values when you enter Snake.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ChoiceSection(
                    title = "Default Difficulty",
                    values = SnakeDifficulty.values().toList(),
                    selected = preferences.defaultDifficulty,
                    label = { it.label },
                    onSelected = { value ->
                        scope.launch {
                            preferencesRepository.updateDefaultDifficulty(value)
                        }
                    }
                )
                HorizontalDivider()
                ChoiceSection(
                    title = "Default Controls",
                    values = SnakeControlMode.values().toList(),
                    selected = preferences.defaultControlMode,
                    label = { it.label },
                    onSelected = { value ->
                        scope.launch {
                            preferencesRepository.updateDefaultControlMode(value)
                        }
                    }
                )
                HorizontalDivider()
                ChoiceSection(
                    title = "Default Theme",
                    values = SnakePalette.values().toList(),
                    selected = preferences.defaultPalette,
                    label = { it.label },
                    onSelected = { value ->
                        scope.launch {
                            preferencesRepository.updateDefaultPalette(value)
                        }
                    }
                )
            }

            RetroPanel {
                Text(
                    text = "Experience Toggles",
                    style = MaterialTheme.typography.titleLarge
                )
                SettingSwitchRow(
                    title = "Sound",
                    subtitle = "The setting is stored now, and the actual sound effects can be added next.",
                    checked = preferences.soundEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            preferencesRepository.updateSoundEnabled(enabled)
                        }
                    }
                )
                HorizontalDivider()
                SettingSwitchRow(
                    title = "Vibration",
                    subtitle = "Useful later for a small buzz when eating food or ending a run.",
                    checked = preferences.vibrationEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            preferencesRepository.updateVibrationEnabled(enabled)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun <T> ChoiceSection(
    title: String,
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            values.forEach { value ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelected(value) },
                    label = { Text(label(value)) }
                )
            }
        }
    }
}
