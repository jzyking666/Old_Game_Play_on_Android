package com.retro.arcade.feature.snake.model

data class Cell(val row: Int, val col: Int)

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    fun isOpposite(other: Direction): Boolean {
        return (this == UP && other == DOWN) ||
            (this == DOWN && other == UP) ||
            (this == LEFT && other == RIGHT) ||
            (this == RIGHT && other == LEFT)
    }

    fun step(from: Cell): Cell {
        return when (this) {
            UP -> from.copy(row = from.row - 1)
            DOWN -> from.copy(row = from.row + 1)
            LEFT -> from.copy(col = from.col - 1)
            RIGHT -> from.copy(col = from.col + 1)
        }
    }
}

enum class SnakeDifficulty(val label: String, val tickMillis: Long) {
    SLOW(label = "Slow", tickMillis = 240L),
    NORMAL(label = "Normal", tickMillis = 170L),
    FAST(label = "Fast", tickMillis = 110L);

    companion object {
        fun fromName(value: String?): SnakeDifficulty {
            return values().firstOrNull { it.name == value } ?: NORMAL
        }
    }
}

enum class SnakeControlMode(val label: String) {
    DPAD(label = "D-pad"),
    SWIPE(label = "Swipe");

    companion object {
        fun fromName(value: String?): SnakeControlMode {
            return values().firstOrNull { it.name == value } ?: DPAD
        }
    }
}

enum class SnakePalette(val label: String) {
    CLASSIC_GREEN(label = "Classic Green"),
    NIGHT_GLOW(label = "Night Glow");

    companion object {
        fun fromName(value: String?): SnakePalette {
            return values().firstOrNull { it.name == value } ?: CLASSIC_GREEN
        }
    }
}

data class SnakeBoardConfig(
    val rows: Int = 20,
    val cols: Int = 16
)

data class SnakeGameConfig(
    val board: SnakeBoardConfig = SnakeBoardConfig(),
    val difficulty: SnakeDifficulty = SnakeDifficulty.NORMAL
)

data class SnakeGameState(
    val snake: List<Cell>,
    val direction: Direction,
    val pendingDirection: Direction? = null,
    val food: Cell,
    val score: Int = 0,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false
) {
    companion object {
        fun idle(): SnakeGameState {
            return SnakeGameState(
                snake = emptyList(),
                direction = Direction.RIGHT,
                food = Cell(0, 0),
                isPaused = true
            )
        }
    }
}
