package com.retro.arcade.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.retro.arcade.feature.snake.model.SnakeControlMode
import com.retro.arcade.feature.snake.model.SnakeDifficulty
import com.retro.arcade.feature.snake.model.SnakePalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "retro_arcade_settings"
)

data class UiPreferences(
    val defaultDifficulty: SnakeDifficulty = SnakeDifficulty.NORMAL,
    val defaultControlMode: SnakeControlMode = SnakeControlMode.DPAD,
    val defaultPalette: SnakePalette = SnakePalette.CLASSIC_GREEN,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val bestSnakeScore: Int = 0,
    val bestDinoScore: Int = 0,
    val bestDownshaftScore: Int = 0,
    val bestTetrisScore: Int = 0,
    val bestMinesweeperTime: Int = 0
)

class AppPreferencesRepository(private val context: Context) {
    private object Keys {
        val DefaultDifficulty = stringPreferencesKey("default_difficulty")
        val DefaultControlMode = stringPreferencesKey("default_control_mode")
        val DefaultPalette = stringPreferencesKey("default_palette")
        val SoundEnabled = booleanPreferencesKey("sound_enabled")
        val VibrationEnabled = booleanPreferencesKey("vibration_enabled")
        val BestSnakeScore = intPreferencesKey("best_snake_score")
        val BestDinoScore = intPreferencesKey("best_dino_score")
        val BestDownshaftScore = intPreferencesKey("best_downshaft_score")
        val BestTetrisScore = intPreferencesKey("best_tetris_score")
        val BestMinesweeperTime = intPreferencesKey("best_minesweeper_time")
    }

    val preferences: Flow<UiPreferences> = context.dataStore.data.map { prefs ->
        UiPreferences(
            defaultDifficulty = SnakeDifficulty.fromName(prefs[Keys.DefaultDifficulty]),
            defaultControlMode = SnakeControlMode.fromName(prefs[Keys.DefaultControlMode]),
            defaultPalette = SnakePalette.fromName(prefs[Keys.DefaultPalette]),
            soundEnabled = prefs[Keys.SoundEnabled] ?: true,
            vibrationEnabled = prefs[Keys.VibrationEnabled] ?: true,
            bestSnakeScore = prefs[Keys.BestSnakeScore] ?: 0,
            bestDinoScore = prefs[Keys.BestDinoScore] ?: 0,
            bestDownshaftScore = prefs[Keys.BestDownshaftScore] ?: 0,
            bestTetrisScore = prefs[Keys.BestTetrisScore] ?: 0,
            bestMinesweeperTime = prefs[Keys.BestMinesweeperTime] ?: 0
        )
    }

    suspend fun updateDefaultDifficulty(value: SnakeDifficulty) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DefaultDifficulty] = value.name
        }
    }

    suspend fun updateDefaultControlMode(value: SnakeControlMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DefaultControlMode] = value.name
        }
    }

    suspend fun updateDefaultPalette(value: SnakePalette) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DefaultPalette] = value.name
        }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SoundEnabled] = enabled
        }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VibrationEnabled] = enabled
        }
    }

    suspend fun updateBestSnakeScore(score: Int) {
        context.dataStore.edit { prefs ->
            val currentBest = prefs[Keys.BestSnakeScore] ?: 0
            if (score > currentBest) {
                prefs[Keys.BestSnakeScore] = score
            }
        }
    }

    suspend fun updateBestDinoScore(score: Int) {
        context.dataStore.edit { prefs ->
            val currentBest = prefs[Keys.BestDinoScore] ?: 0
            if (score > currentBest) {
                prefs[Keys.BestDinoScore] = score
            }
        }
    }

    suspend fun updateBestDownshaftScore(score: Int) {
        context.dataStore.edit { prefs ->
            val currentBest = prefs[Keys.BestDownshaftScore] ?: 0
            if (score > currentBest) {
                prefs[Keys.BestDownshaftScore] = score
            }
        }
    }

    suspend fun updateBestTetrisScore(score: Int) {
        context.dataStore.edit { prefs ->
            val currentBest = prefs[Keys.BestTetrisScore] ?: 0
            if (score > currentBest) {
                prefs[Keys.BestTetrisScore] = score
            }
        }
    }

    suspend fun updateBestMinesweeperTime(seconds: Int) {
        context.dataStore.edit { prefs ->
            val currentBest = prefs[Keys.BestMinesweeperTime] ?: 0
            if (seconds > 0 && (currentBest == 0 || seconds < currentBest)) {
                prefs[Keys.BestMinesweeperTime] = seconds
            }
        }
    }
}
