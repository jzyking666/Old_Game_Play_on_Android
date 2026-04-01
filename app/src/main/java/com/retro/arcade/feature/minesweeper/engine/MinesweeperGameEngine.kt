package com.retro.arcade.feature.minesweeper.engine

import com.retro.arcade.feature.minesweeper.model.MinesweeperCell
import com.retro.arcade.feature.minesweeper.model.MinesweeperDefaults
import com.retro.arcade.feature.minesweeper.model.MinesweeperDifficulty
import com.retro.arcade.feature.minesweeper.model.MinesweeperGameState
import com.retro.arcade.feature.minesweeper.model.MinesweeperMark
import kotlin.random.Random

class MinesweeperGameEngine(
    private val random: Random = Random.Default
) {
    fun newGame(difficulty: MinesweeperDifficulty): MinesweeperGameState {
        return MinesweeperGameState(
            difficulty = difficulty,
            board = MinesweeperDefaults.emptyBoard(difficulty.rows, difficulty.cols)
        )
    }

    fun tick(state: MinesweeperGameState): MinesweeperGameState {
        if (!state.isStarted || state.isGameOver || state.isWin) return state
        return state.copy(
            elapsedSeconds = (state.elapsedSeconds + 1).coerceAtMost(999)
        )
    }

    fun restart(state: MinesweeperGameState): MinesweeperGameState {
        return newGame(state.difficulty)
    }

    fun changeDifficulty(
        difficulty: MinesweeperDifficulty
    ): MinesweeperGameState {
        return newGame(difficulty)
    }

    fun revealCell(
        state: MinesweeperGameState,
        row: Int,
        col: Int
    ): MinesweeperGameState {
        if (state.isGameOver || state.isWin) return state
        val current = state.board[row][col]
        if (current.isRevealed || current.mark == MinesweeperMark.FLAG) return state

        val board = if (!state.isStarted) {
            generateBoard(state.difficulty, safeRow = row, safeCol = col)
        } else {
            state.board
        }

        val target = board[row][col]
        if (target.hasMine) {
            return state.copy(
                board = board,
                isStarted = true,
                isGameOver = true,
                isWin = false,
                explodedCell = row to col
            )
        }

        val revealedBoard = revealConnected(board, row, col)
        val isWin = countHiddenNonMines(revealedBoard) == 0
        return state.copy(
            board = if (isWin) autoFlagMines(revealedBoard) else revealedBoard,
            isStarted = true,
            isGameOver = isWin,
            isWin = isWin,
            explodedCell = null
        )
    }

    fun toggleFlag(
        state: MinesweeperGameState,
        row: Int,
        col: Int
    ): MinesweeperGameState {
        if (state.isGameOver || state.isWin) return state
        val cell = state.board[row][col]
        if (cell.isRevealed) return state

        val updated = updateCell(state.board, row, col) { current ->
            current.copy(
                mark = if (current.mark == MinesweeperMark.FLAG) {
                    MinesweeperMark.NONE
                } else {
                    MinesweeperMark.FLAG
                }
            )
        }
        return state.copy(board = updated)
    }

    fun chordReveal(
        state: MinesweeperGameState,
        row: Int,
        col: Int
    ): MinesweeperGameState {
        if (state.isGameOver || state.isWin) return state
        val cell = state.board[row][col]
        if (!cell.isRevealed || cell.adjacentMines == 0) return state

        val neighbors = neighbors(row, col, state.rows, state.cols)
        val flags = neighbors.count { (r, c) ->
            state.board[r][c].mark == MinesweeperMark.FLAG
        }
        if (flags != cell.adjacentMines) {
            return state
        }

        var nextState = state
        neighbors.forEach { (r, c) ->
            val neighborCell = nextState.board[r][c]
            if (!neighborCell.isRevealed && neighborCell.mark != MinesweeperMark.FLAG) {
                nextState = revealCell(nextState, r, c)
            }
        }
        return nextState
    }

    private fun generateBoard(
        difficulty: MinesweeperDifficulty,
        safeRow: Int,
        safeCol: Int
    ): List<List<MinesweeperCell>> {
        val rows = difficulty.rows
        val cols = difficulty.cols
        val totalCells = rows * cols
        val safeIndex = safeRow * cols + safeCol
        val mineIndexes = mutableSetOf<Int>()

        while (mineIndexes.size < difficulty.mines) {
            val candidate = random.nextInt(totalCells)
            if (candidate != safeIndex) {
                mineIndexes += candidate
            }
        }

        val base = List(rows) { row ->
            List(cols) { col ->
                val index = row * cols + col
                MinesweeperCell(hasMine = index in mineIndexes)
            }
        }

        return List(rows) { row ->
            List(cols) { col ->
                val adjacent = neighbors(row, col, rows, cols).count { (r, c) ->
                    base[r][c].hasMine
                }
                base[row][col].copy(adjacentMines = adjacent)
            }
        }
    }

    private fun revealConnected(
        board: List<List<MinesweeperCell>>,
        startRow: Int,
        startCol: Int
    ): List<List<MinesweeperCell>> {
        val rows = board.size
        val cols = board.first().size
        val mutable = board.map { row -> row.toMutableList() }
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue += startRow to startCol

        while (queue.isNotEmpty()) {
            val (row, col) = queue.removeFirst()
            val cell = mutable[row][col]
            if (cell.isRevealed || cell.mark == MinesweeperMark.FLAG) continue
            mutable[row][col] = cell.copy(isRevealed = true)

            if (cell.adjacentMines == 0 && !cell.hasMine) {
                neighbors(row, col, rows, cols).forEach { neighbor ->
                    val nextCell = mutable[neighbor.first][neighbor.second]
                    if (!nextCell.isRevealed && nextCell.mark != MinesweeperMark.FLAG) {
                        queue += neighbor
                    }
                }
            }
        }

        return mutable.map { row -> row.toList() }
    }

    private fun autoFlagMines(
        board: List<List<MinesweeperCell>>
    ): List<List<MinesweeperCell>> {
        return board.map { row ->
            row.map { cell ->
                if (cell.hasMine && cell.mark == MinesweeperMark.NONE) {
                    cell.copy(mark = MinesweeperMark.FLAG)
                } else {
                    cell
                }
            }
        }
    }

    private fun countHiddenNonMines(
        board: List<List<MinesweeperCell>>
    ): Int {
        return board.sumOf { row ->
            row.count { cell ->
                !cell.hasMine && !cell.isRevealed
            }
        }
    }

    private fun updateCell(
        board: List<List<MinesweeperCell>>,
        row: Int,
        col: Int,
        transform: (MinesweeperCell) -> MinesweeperCell
    ): List<List<MinesweeperCell>> {
        val mutable = board.map { it.toMutableList() }
        mutable[row][col] = transform(mutable[row][col])
        return mutable.map { it.toList() }
    }

    private fun neighbors(
        row: Int,
        col: Int,
        rows: Int,
        cols: Int
    ): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = row + dr
                val c = col + dc
                if (r in 0 until rows && c in 0 until cols) {
                    result += r to c
                }
            }
        }
        return result
    }
}
