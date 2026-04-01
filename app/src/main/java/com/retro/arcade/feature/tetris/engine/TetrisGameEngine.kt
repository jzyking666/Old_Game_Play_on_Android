package com.retro.arcade.feature.tetris.engine

import com.retro.arcade.feature.tetris.model.TetrisActivePiece
import com.retro.arcade.feature.tetris.model.TetrisDefaults
import com.retro.arcade.feature.tetris.model.TetrisGameState
import com.retro.arcade.feature.tetris.model.TetrisPieceType
import com.retro.arcade.feature.tetris.model.TetrisShapeLibrary
import kotlin.random.Random

class TetrisGameEngine(
    private val random: Random = Random.Default
) {
    fun newGame(): TetrisGameState {
        val active = spawnPiece(randomPiece())
        return TetrisGameState(
            board = TetrisDefaults.emptyBoard(),
            activePiece = active,
            nextPiece = randomPiece()
        )
    }

    fun start(state: TetrisGameState): TetrisGameState {
        if (state.isStarted || state.isGameOver) return state
        return state.copy(isStarted = true)
    }

    fun togglePause(state: TetrisGameState): TetrisGameState {
        if (!state.isStarted || state.isGameOver) return state
        return state.copy(isPaused = !state.isPaused)
    }

    fun restart(): TetrisGameState {
        return newGame()
    }

    fun tick(state: TetrisGameState): TetrisGameState {
        if (!state.isStarted || state.isPaused || state.isGameOver) return state
        return stepDown(state)
    }

    fun moveLeft(state: TetrisGameState): TetrisGameState {
        return moveHorizontally(state, delta = -1)
    }

    fun moveRight(state: TetrisGameState): TetrisGameState {
        return moveHorizontally(state, delta = 1)
    }

    fun rotate(state: TetrisGameState): TetrisGameState {
        if (!state.isStarted || state.isPaused || state.isGameOver) return state
        val rotated = state.activePiece.copy(
            rotation = state.activePiece.rotation + 1
        )
        return if (isValidPosition(state.board, rotated)) {
            state.copy(activePiece = rotated)
        } else {
            state
        }
    }

    fun softDrop(state: TetrisGameState): TetrisGameState {
        if (!state.isStarted || state.isPaused || state.isGameOver) return state
        return stepDown(state)
    }

    private fun moveHorizontally(
        state: TetrisGameState,
        delta: Int
    ): TetrisGameState {
        if (!state.isStarted || state.isPaused || state.isGameOver) return state
        val shifted = state.activePiece.copy(
            col = state.activePiece.col + delta
        )
        return if (isValidPosition(state.board, shifted)) {
            state.copy(activePiece = shifted)
        } else {
            state
        }
    }

    private fun stepDown(state: TetrisGameState): TetrisGameState {
        val moved = state.activePiece.copy(
            row = state.activePiece.row + 1
        )
        if (isValidPosition(state.board, moved)) {
            return state.copy(activePiece = moved)
        }

        val lockedBoard = lockPiece(state.board, state.activePiece)
        val clearResult = clearLines(lockedBoard)
        val updatedLines = state.linesCleared + clearResult.linesCleared
        val updatedLevel = updatedLines / 10
        val updatedScore = state.score + scoreForLines(
            lines = clearResult.linesCleared,
            level = state.level
        )

        val nextActive = spawnPiece(state.nextPiece)
        val nextPiece = randomPiece()
        if (!isValidPosition(clearResult.board, nextActive)) {
            return state.copy(
                board = clearResult.board,
                score = updatedScore,
                linesCleared = updatedLines,
                level = updatedLevel,
                isGameOver = true
            )
        }

        return state.copy(
            board = clearResult.board,
            activePiece = nextActive,
            nextPiece = nextPiece,
            score = updatedScore,
            linesCleared = updatedLines,
            level = updatedLevel
        )
    }

    private fun spawnPiece(type: TetrisPieceType): TetrisActivePiece {
        return TetrisActivePiece(
            type = type,
            rotation = 0,
            row = TetrisDefaults.spawnRow,
            col = TetrisDefaults.spawnCol
        )
    }

    private fun randomPiece(): TetrisPieceType {
        val values = TetrisPieceType.values()
        return values[random.nextInt(values.size)]
    }

    private fun isValidPosition(
        board: List<List<TetrisPieceType?>>,
        piece: TetrisActivePiece
    ): Boolean {
        return TetrisShapeLibrary.cellsFor(piece).all { cell ->
            cell.row in 0 until TetrisDefaults.rows &&
                cell.col in 0 until TetrisDefaults.cols &&
                board[cell.row][cell.col] == null
        }
    }

    private fun lockPiece(
        board: List<List<TetrisPieceType?>>,
        piece: TetrisActivePiece
    ): List<List<TetrisPieceType?>> {
        val mutable = board.map { row -> row.toMutableList() }
        TetrisShapeLibrary.cellsFor(piece).forEach { cell ->
            mutable[cell.row][cell.col] = piece.type
        }
        return mutable.map { row -> row.toList() }
    }

    private fun clearLines(
        board: List<List<TetrisPieceType?>>
    ): ClearResult {
        val remainingRows = board.filter { row ->
            row.any { it == null }
        }.toMutableList()
        val cleared = TetrisDefaults.rows - remainingRows.size
        repeat(cleared) {
            remainingRows.add(
                index = 0,
                element = List(TetrisDefaults.cols) { null }
            )
        }
        return ClearResult(
            board = remainingRows.toList(),
            linesCleared = cleared
        )
    }

    private fun scoreForLines(lines: Int, level: Int): Int {
        val base = when (lines) {
            1 -> 40
            2 -> 100
            3 -> 300
            4 -> 1200
            else -> 0
        }
        return base * (level + 1)
    }

    private data class ClearResult(
        val board: List<List<TetrisPieceType?>>,
        val linesCleared: Int
    )
}
