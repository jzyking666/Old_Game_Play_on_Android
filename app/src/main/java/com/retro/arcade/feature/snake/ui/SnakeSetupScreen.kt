package com.retro.arcade.feature.snake.ui

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
fun SnakeSetupScreen(
    preferences: UiPreferences,
    preferencesRepository: AppPreferencesRepository,
    onBack: () -> Unit,
    onStartGame: (SnakeDifficulty, SnakeControlMode, SnakePalette) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedDifficulty by remember(preferences.defaultDifficulty) {
        mutableStateOf(preferences.defaultDifficulty)
    }
    var selectedControlMode by remember(preferences.defaultControlMode) {
        mutableStateOf(preferences.defaultControlMode)
    }
    var selectedPalette by remember(preferences.defaultPalette) {
        mutableStateOf(preferences.defaultPalette)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Classic Snake") },
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
                    text = "Just like before: open it and play right away.",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Your default setup is saved automatically, so the next run starts faster.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Best Score",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = preferences.bestSnakeScore.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            RetroPanel {
                ChoiceSection(
                    title = "Difficulty",
                    values = SnakeDifficulty.values().toList(),
                    selected = selectedDifficulty,
                    label = { it.label },
                    onSelected = { value ->
                        selectedDifficulty = value
                        scope.launch {
                            preferencesRepository.updateDefaultDifficulty(value)
                        }
                    }
                )
                HorizontalDivider()
                ChoiceSection(
                    title = "Controls",
                    values = SnakeControlMode.values().toList(),
                    selected = selectedControlMode,
                    label = { it.label },
                    onSelected = { value ->
                        selectedControlMode = value
                        scope.launch {
                            preferencesRepository.updateDefaultControlMode(value)
                        }
                    }
                )
                HorizontalDivider()
                ChoiceSection(
                    title = "Theme",
                    values = SnakePalette.values().toList(),
                    selected = selectedPalette,
                    label = { it.label },
                    onSelected = { value ->
                        selectedPalette = value
                        scope.launch {
                            preferencesRepository.updateDefaultPalette(value)
                        }
                    }
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onStartGame(
                        selectedDifficulty,
                        selectedControlMode,
                        selectedPalette
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Text("Start Game")
            }
        }
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
        Text(text = title, style = MaterialTheme.typography.titleLarge)
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
