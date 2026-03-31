package com.retro.arcade.feature.dino

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retro.arcade.feature.dino.engine.DinoGameEngine
import com.retro.arcade.feature.dino.model.DinoGameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DinoGameUiState(
    val sessionId: Long = 0L,
    val gameState: DinoGameState = DinoGameState()
)

class DinoGameViewModel : ViewModel() {
    private val engine = DinoGameEngine()
    private val _uiState = MutableStateFlow(DinoGameUiState())
    private var tickerJob: Job? = null
    private var initialized = false

    val uiState: StateFlow<DinoGameUiState> = _uiState.asStateFlow()

    fun initialize(bestScore: Int) {
        if (initialized) return
        initialized = true

        _uiState.value = DinoGameUiState(
            sessionId = 1L,
            gameState = engine.newGame(bestScore)
        )
        startTicker()
    }

    fun onPress() {
        _uiState.update { state ->
            state.copy(gameState = engine.press(state.gameState))
        }
    }

    fun onRelease() {
        _uiState.update { state ->
            state.copy(gameState = engine.release(state.gameState))
        }
    }

    fun onDuckPress() {
        _uiState.update { state ->
            state.copy(gameState = engine.duckPress(state.gameState))
        }
    }

    fun onDuckRelease() {
        _uiState.update { state ->
            state.copy(gameState = engine.duckRelease(state.gameState))
        }
    }

    fun restart() {
        val current = _uiState.value
        _uiState.value = current.copy(
            sessionId = current.sessionId + 1,
            gameState = engine.restart(current.gameState)
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
                kotlinx.coroutines.delay(16L)
            }
        }
    }

    private fun advance(deltaMs: Float) {
        val current = _uiState.value
        val nextGameState = engine.tick(current.gameState, deltaMs)
        _uiState.value = current.copy(gameState = nextGameState)
        if (nextGameState.isGameOver) {
            tickerJob?.cancel()
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
