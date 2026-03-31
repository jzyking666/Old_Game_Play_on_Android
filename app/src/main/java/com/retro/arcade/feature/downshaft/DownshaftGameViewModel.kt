package com.retro.arcade.feature.downshaft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retro.arcade.feature.downshaft.engine.DownshaftGameEngine
import com.retro.arcade.feature.downshaft.model.DownshaftDirection
import com.retro.arcade.feature.downshaft.model.DownshaftGameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DownshaftUiState(
    val sessionId: Long = 0L,
    val bestScore: Int = 0,
    val inputDirection: DownshaftDirection = DownshaftDirection.NONE,
    val gameState: DownshaftGameState? = null
)

class DownshaftGameViewModel : ViewModel() {
    private val engine = DownshaftGameEngine()
    private val _uiState = MutableStateFlow(DownshaftUiState())
    private var tickerJob: Job? = null
    private var initialized = false

    val uiState: StateFlow<DownshaftUiState> = _uiState.asStateFlow()

    fun initialize(bestScore: Int) {
        if (initialized) return
        initialized = true

        _uiState.value = DownshaftUiState(
            sessionId = 1L,
            bestScore = bestScore,
            gameState = engine.newGame(bestScore)
        )
        startTicker()
    }

    fun onDirectionPressed(direction: DownshaftDirection) {
        _uiState.update { state ->
            val gameState = state.gameState ?: return@update state
            state.copy(
                inputDirection = direction,
                gameState = engine.start(gameState)
            )
        }
    }

    fun startGame() {
        _uiState.update { state ->
            val gameState = state.gameState ?: return@update state
            state.copy(gameState = engine.start(gameState))
        }
    }

    fun onDirectionReleased(direction: DownshaftDirection) {
        _uiState.update { state ->
            if (state.inputDirection != direction) return@update state
            state.copy(inputDirection = DownshaftDirection.NONE)
        }
    }

    fun restart() {
        val current = _uiState.value
        val bestScore = maxOf(current.bestScore, current.gameState?.score ?: 0)
        _uiState.value = current.copy(
            sessionId = current.sessionId + 1,
            bestScore = bestScore,
            inputDirection = DownshaftDirection.NONE,
            gameState = engine.newGame(bestScore)
        )
        startTicker()
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
        val nextGameState = engine.tick(
            state = gameState,
            deltaMs = deltaMs,
            direction = current.inputDirection
        )
        _uiState.value = current.copy(
            bestScore = maxOf(current.bestScore, nextGameState.score),
            gameState = nextGameState
        )
        if (nextGameState.isGameOver) {
            tickerJob?.cancel()
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
