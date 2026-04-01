package com.retro.arcade.feature.minesweeper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retro.arcade.feature.minesweeper.engine.MinesweeperGameEngine
import com.retro.arcade.feature.minesweeper.model.MinesweeperDifficulty
import com.retro.arcade.feature.minesweeper.model.MinesweeperGameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MinesweeperUiState(
    val sessionId: Long = 0L,
    val bestTimeSeconds: Int = 0,
    val gameState: MinesweeperGameState? = null,
    val isPressingBoard: Boolean = false
)

class MinesweeperGameViewModel : ViewModel() {
    private val engine = MinesweeperGameEngine()
    private val _uiState = MutableStateFlow(MinesweeperUiState())
    private var timerJob: Job? = null
    private var initialized = false

    val uiState: StateFlow<MinesweeperUiState> = _uiState.asStateFlow()

    fun initialize(bestTimeSeconds: Int) {
        if (initialized) return
        initialized = true
        _uiState.value = MinesweeperUiState(
            sessionId = 1L,
            bestTimeSeconds = bestTimeSeconds,
            gameState = engine.newGame(MinesweeperDifficulty.BEGINNER)
        )
        startTimer()
    }

    fun changeDifficulty(difficulty: MinesweeperDifficulty) {
        _uiState.update { state ->
            state.copy(
                sessionId = state.sessionId + 1,
                gameState = engine.changeDifficulty(difficulty),
                isPressingBoard = false
            )
        }
    }

    fun restart() {
        _uiState.update { state ->
            val gameState = state.gameState ?: return@update state
            state.copy(
                sessionId = state.sessionId + 1,
                gameState = engine.restart(gameState),
                isPressingBoard = false
            )
        }
    }

    fun revealCell(row: Int, col: Int) {
        _uiState.update { state ->
            val gameState = state.gameState ?: return@update state
            val next = engine.revealCell(gameState, row, col)
            state.copy(
                gameState = next,
                bestTimeSeconds = bestTime(state.bestTimeSeconds, next)
            )
        }
    }

    fun toggleFlag(row: Int, col: Int) {
        _uiState.update { state ->
            val gameState = state.gameState ?: return@update state
            state.copy(gameState = engine.toggleFlag(gameState, row, col))
        }
    }

    fun chordReveal(row: Int, col: Int) {
        _uiState.update { state ->
            val gameState = state.gameState ?: return@update state
            val next = engine.chordReveal(gameState, row, col)
            state.copy(
                gameState = next,
                bestTimeSeconds = bestTime(state.bestTimeSeconds, next)
            )
        }
    }

    fun setBoardPressed(isPressed: Boolean) {
        _uiState.update { state ->
            state.copy(isPressingBoard = isPressed)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _uiState.update { state ->
                    val gameState = state.gameState ?: return@update state
                    val next = engine.tick(gameState)
                    state.copy(
                        gameState = next,
                        bestTimeSeconds = bestTime(state.bestTimeSeconds, next)
                    )
                }
            }
        }
    }

    private fun bestTime(
        currentBest: Int,
        state: MinesweeperGameState
    ): Int {
        return if (state.isWin) {
            if (currentBest == 0 || state.elapsedSeconds < currentBest) {
                state.elapsedSeconds
            } else {
                currentBest
            }
        } else {
            currentBest
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
