package com.retro.arcade.feature.minesweeper.model

enum class MinesweeperMark {
    NONE,
    FLAG
}

enum class MinesweeperDifficulty(
    val label: String,
    val rows: Int,
    val cols: Int,
    val mines: Int
) {
    BEGINNER(label = "Beginner", rows = 9, cols = 9, mines = 10),
    INTERMEDIATE(label = "Intermediate", rows = 16, cols = 16, mines = 40),
    EXPERT(label = "Expert", rows = 16, cols = 30, mines = 99)
}

data class MinesweeperCell(
    val hasMine: Boolean = false,
    val adjacentMines: Int = 0,
    val isRevealed: Boolean = false,
    val mark: MinesweeperMark = MinesweeperMark.NONE
)

data class MinesweeperGameState(
    val difficulty: MinesweeperDifficulty = MinesweeperDifficulty.BEGINNER,
    val board: List<List<MinesweeperCell>> = emptyList(),
    val isStarted: Boolean = false,
    val isGameOver: Boolean = false,
    val isWin: Boolean = false,
    val explodedCell: Pair<Int, Int>? = null,
    val elapsedSeconds: Int = 0
) {
    val rows: Int
        get() = difficulty.rows

    val cols: Int
        get() = difficulty.cols

    val flaggedCount: Int
        get() = board.sumOf { row ->
            row.count { cell -> cell.mark == MinesweeperMark.FLAG }
        }

    val remainingMines: Int
        get() = difficulty.mines - flaggedCount
}

object MinesweeperDefaults {
    fun emptyBoard(rows: Int, cols: Int): List<List<MinesweeperCell>> {
        return List(rows) { List(cols) { MinesweeperCell() } }
    }
}
