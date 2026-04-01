package com.retro.arcade.feature.tetris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retro.arcade.feature.tetris.engine.TetrisGameEngine
import com.retro.arcade.feature.tetris.model.TetrisDefaults
import com.retro.arcade.feature.tetris.model.TetrisGameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TetrisUiState(
    val sessionId: Long = 0L,
    val bestScore: Int = 0,
    val gameState: TetrisGameState? = null
)

class TetrisGameViewModel : ViewModel() {
    private val engine = TetrisGameEngine()
    private val _uiState = MutableStateFlow(TetrisUiState())
    private var tickerJob: Job? = null
    private var initialized = false
    private var gravityAccumulatorMs = 0f
    private var softDropHeld = false

    val uiState: StateFlow<TetrisUiState> = _uiState.asStateFlow()

    fun initialize(bestScore: Int) {
        if (initialized) return
        initialized = true
        gravityAccumulatorMs = 0f
        softDropHeld = false
        _uiState.value = TetrisUiState(
            sessionId = 1L,
            bestScore = bestScore,
            gameState = engine.newGame()
        )
        startTicker()
    }

    fun startGame() {
        _uiState.update { state ->
            val gameState = state.gameState ?: return@update state
            state.copy(gameState = engine.start(gameState))
        }
    }

    fun togglePause() {
        _uiState.update { state ->
            val gameState = state.gameState ?: return@update state
            state.copy(gameState = engine.togglePause(gameState))
        }
    }

    fun restart() {
        val current = _uiState.value
        gravityAccumulatorMs = 0f
        softDropHeld = false
        _uiState.value = current.copy(
            sessionId = current.sessionId + 1,
            bestScore = maxOf(current.bestScore, current.gameState?.score ?: 0),
            gameState = engine.restart()
        )
        startTicker()
    }

    fun moveLeft() {
        _uiState.update { state ->
            val started = ensureStarted(state.gameState) ?: return@update state
            state.copy(gameState = engine.moveLeft(started))
        }
    }

    fun moveRight() {
        _uiState.update { state ->
            val started = ensureStarted(state.gameState) ?: return@update state
            state.copy(gameState = engine.moveRight(started))
        }
    }

    fun rotate() {
        _uiState.update { state ->
            val started = ensureStarted(state.gameState) ?: return@update state
            state.copy(gameState = engine.rotate(started))
        }
    }

    fun softDropPress() {
        softDropHeld = true
        _uiState.update { state ->
            val started = ensureStarted(state.gameState) ?: return@update state
            state.copy(gameState = engine.softDrop(started))
        }
    }

    fun softDropRelease() {
        softDropHeld = false
    }

    private fun ensureStarted(gameState: TetrisGameState?): TetrisGameState? {
        gameState ?: return null
        return if (gameState.isStarted) {
            gameState
        } else {
            engine.start(gameState)
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            var lastFrameTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val deltaMs = (now - lastFrameTime) / 1_000_000f
                lastFrameTime = now
                advance(deltaMs)
                delay(16L)
            }
        }
    }

    private fun advance(deltaMs: Float) {
        val current = _uiState.value
        val gameState = current.gameState ?: return
        if (!gameState.isStarted || gameState.isPaused || gameState.isGameOver) {
            return
        }

        gravityAccumulatorMs += deltaMs.coerceAtMost(40f)
        val interval = if (softDropHeld) {
            TetrisDefaults.softDropIntervalMs
        } else {
            TetrisDefaults.fallIntervalMs(gameState.level)
        }

        var nextState = gameState
        while (gravityAccumulatorMs >= interval && !nextState.isGameOver) {
            gravityAccumulatorMs -= interval
            nextState = engine.tick(nextState)
        }

        _uiState.value = current.copy(
            bestScore = maxOf(current.bestScore, nextState.score),
            gameState = nextState
        )

        if (nextState.isGameOver) {
            softDropHeld = false
            tickerJob?.cancel()
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
