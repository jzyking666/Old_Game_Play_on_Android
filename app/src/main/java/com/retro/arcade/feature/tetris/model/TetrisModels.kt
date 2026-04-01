package com.retro.arcade.feature.tetris.model

enum class TetrisPieceType {
    I,
    O,
    T,
    S,
    Z,
    J,
    L
}

data class TetrisCell(
    val row: Int,
    val col: Int
)

data class TetrisActivePiece(
    val type: TetrisPieceType,
    val rotation: Int,
    val row: Int,
    val col: Int
)

data class TetrisGameState(
    val board: List<List<TetrisPieceType?>>,
    val activePiece: TetrisActivePiece,
    val nextPiece: TetrisPieceType,
    val score: Int = 0,
    val linesCleared: Int = 0,
    val level: Int = 0,
    val isStarted: Boolean = false,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false
)

object TetrisDefaults {
    const val rows = 20
    const val cols = 10
    const val spawnRow = 0
    const val spawnCol = 3
    const val softDropIntervalMs = 45f

    private val levelSpeedsMs = listOf(
        800f,
        720f,
        640f,
        560f,
        480f,
        400f,
        320f,
        250f,
        180f,
        130f,
        100f,
        80f,
        65f,
        50f,
        40f
    )

    fun emptyBoard(): List<List<TetrisPieceType?>> {
        return List(rows) { List<TetrisPieceType?>(cols) { null } }
    }

    fun fallIntervalMs(level: Int): Float {
        return levelSpeedsMs.getOrElse(level) { levelSpeedsMs.last() }
    }
}

object TetrisShapeLibrary {
    fun cellsFor(piece: TetrisActivePiece): List<TetrisCell> {
        return cellsFor(piece.type, piece.rotation).map { cell ->
            TetrisCell(
                row = piece.row + cell.row,
                col = piece.col + cell.col
            )
        }
    }

    fun cellsFor(type: TetrisPieceType, rotation: Int): List<TetrisCell> {
        return when (type) {
            TetrisPieceType.I -> when (rotation.mod(2)) {
                0 -> listOf(
                    TetrisCell(1, 0),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2),
                    TetrisCell(1, 3)
                )
                else -> listOf(
                    TetrisCell(0, 2),
                    TetrisCell(1, 2),
                    TetrisCell(2, 2),
                    TetrisCell(3, 2)
                )
            }

            TetrisPieceType.O -> listOf(
                TetrisCell(0, 1),
                TetrisCell(0, 2),
                TetrisCell(1, 1),
                TetrisCell(1, 2)
            )

            TetrisPieceType.T -> when (rotation.mod(4)) {
                0 -> listOf(
                    TetrisCell(0, 1),
                    TetrisCell(1, 0),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2)
                )
                1 -> listOf(
                    TetrisCell(0, 1),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2),
                    TetrisCell(2, 1)
                )
                2 -> listOf(
                    TetrisCell(1, 0),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2),
                    TetrisCell(2, 1)
                )
                else -> listOf(
                    TetrisCell(0, 1),
                    TetrisCell(1, 0),
                    TetrisCell(1, 1),
                    TetrisCell(2, 1)
                )
            }

            TetrisPieceType.S -> when (rotation.mod(2)) {
                0 -> listOf(
                    TetrisCell(0, 1),
                    TetrisCell(0, 2),
                    TetrisCell(1, 0),
                    TetrisCell(1, 1)
                )
                else -> listOf(
                    TetrisCell(0, 1),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2),
                    TetrisCell(2, 2)
                )
            }

            TetrisPieceType.Z -> when (rotation.mod(2)) {
                0 -> listOf(
                    TetrisCell(0, 0),
                    TetrisCell(0, 1),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2)
                )
                else -> listOf(
                    TetrisCell(0, 2),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2),
                    TetrisCell(2, 1)
                )
            }

            TetrisPieceType.J -> when (rotation.mod(4)) {
                0 -> listOf(
                    TetrisCell(0, 0),
                    TetrisCell(1, 0),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2)
                )
                1 -> listOf(
                    TetrisCell(0, 1),
                    TetrisCell(0, 2),
                    TetrisCell(1, 1),
                    TetrisCell(2, 1)
                )
                2 -> listOf(
                    TetrisCell(1, 0),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2),
                    TetrisCell(2, 2)
                )
                else -> listOf(
                    TetrisCell(0, 1),
                    TetrisCell(1, 1),
                    TetrisCell(2, 0),
                    TetrisCell(2, 1)
                )
            }

            TetrisPieceType.L -> when (rotation.mod(4)) {
                0 -> listOf(
                    TetrisCell(0, 2),
                    TetrisCell(1, 0),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2)
                )
                1 -> listOf(
                    TetrisCell(0, 1),
                    TetrisCell(1, 1),
                    TetrisCell(2, 1),
                    TetrisCell(2, 2)
                )
                2 -> listOf(
                    TetrisCell(1, 0),
                    TetrisCell(1, 1),
                    TetrisCell(1, 2),
                    TetrisCell(2, 0)
                )
                else -> listOf(
                    TetrisCell(0, 0),
                    TetrisCell(0, 1),
                    TetrisCell(1, 1),
                    TetrisCell(2, 1)
                )
            }
        }
    }
}
