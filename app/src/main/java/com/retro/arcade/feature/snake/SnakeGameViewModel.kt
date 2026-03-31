package com.retro.arcade.feature.snake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retro.arcade.feature.snake.engine.SnakeGameEngine
import com.retro.arcade.feature.snake.model.Direction
import com.retro.arcade.feature.snake.model.SnakeControlMode
import com.retro.arcade.feature.snake.model.SnakeDifficulty
import com.retro.arcade.feature.snake.model.SnakeGameConfig
import com.retro.arcade.feature.snake.model.SnakeGameState
import com.retro.arcade.feature.snake.model.SnakePalette
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SnakeGameUiState(
    val sessionId: Long = 0L,
    val config: SnakeGameConfig = SnakeGameConfig(),
    val controlMode: SnakeControlMode = SnakeControlMode.DPAD,
    val palette: SnakePalette = SnakePalette.CLASSIC_GREEN,
    val bestScore: Int = 0,
    val gameState: SnakeGameState = SnakeGameState.idle()
)

class SnakeGameViewModel : ViewModel() {
    private val engine = SnakeGameEngine()
    private val _uiState = MutableStateFlow(SnakeGameUiState())
    private var tickerJob: Job? = null
    private var initialized = false

    val uiState: StateFlow<SnakeGameUiState> = _uiState.asStateFlow()

    fun initialize(
        difficulty: SnakeDifficulty,
        controlMode: SnakeControlMode,
        palette: SnakePalette,
        bestScore: Int
    ) {
        if (initialized) return
        initialized = true

        val config = SnakeGameConfig(difficulty = difficulty)
        _uiState.value = SnakeGameUiState(
            sessionId = 1L,
            config = config,
            controlMode = controlMode,
            palette = palette,
            bestScore = bestScore,
            gameState = engine.newGame(config)
        )
        startTicker()
    }

    fun onDirectionInput(direction: Direction) {
        _uiState.update { state ->
            state.copy(
                gameState = engine.enqueueDirection(state.gameState, direction)
            )
        }
    }

    fun togglePause() {
        _uiState.update { state ->
            state.copy(
                gameState = engine.togglePause(state.gameState)
            )
        }
    }

    fun restart() {
        val current = _uiState.value
        val newState = engine.newGame(current.config)
        _uiState.value = current.copy(
            sessionId = current.sessionId + 1,
            bestScore = maxOf(current.bestScore, current.gameState.score),
            gameState = newState
        )
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(_uiState.value.config.difficulty.tickMillis)
                advance()
            }
        }
    }

    private fun advance() {
        val current = _uiState.value
        val nextState = engine.tick(current.gameState, current.config)
        if (nextState == current.gameState) {
            return
        }

        _uiState.value = current.copy(
            bestScore = maxOf(current.bestScore, nextState.score),
            gameState = nextState
        )

        if (nextState.isGameOver) {
            tickerJob?.cancel()
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
