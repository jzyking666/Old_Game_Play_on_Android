package com.retro.arcade.feature.snake.engine

import com.retro.arcade.feature.snake.model.Cell
import com.retro.arcade.feature.snake.model.Direction
import com.retro.arcade.feature.snake.model.SnakeBoardConfig
import com.retro.arcade.feature.snake.model.SnakeGameConfig
import com.retro.arcade.feature.snake.model.SnakeGameState
import kotlin.random.Random

class SnakeGameEngine(
    private val random: Random = Random.Default
) {
    fun newGame(config: SnakeGameConfig): SnakeGameState {
        val centerRow = config.board.rows / 2
        val centerCol = config.board.cols / 2
        val snake = listOf(
            Cell(centerRow, centerCol + 1),
            Cell(centerRow, centerCol),
            Cell(centerRow, centerCol - 1)
        )

        return SnakeGameState(
            snake = snake,
            direction = Direction.RIGHT,
            food = generateFood(config.board, snake)
        )
    }

    fun enqueueDirection(
        state: SnakeGameState,
        direction: Direction
    ): SnakeGameState {
        if (state.isGameOver) return state

        val baseDirection = state.pendingDirection ?: state.direction
        if (direction == baseDirection || direction.isOpposite(baseDirection)) {
            return state
        }

        return state.copy(pendingDirection = direction)
    }

    fun togglePause(state: SnakeGameState): SnakeGameState {
        if (state.isGameOver) return state
        return state.copy(isPaused = !state.isPaused)
    }

    fun tick(
        state: SnakeGameState,
        config: SnakeGameConfig
    ): SnakeGameState {
        if (state.isPaused || state.isGameOver || state.snake.isEmpty()) {
            return state
        }

        val nextDirection = state.pendingDirection ?: state.direction
        val nextHead = nextDirection.step(state.snake.first())

        if (nextHead.isOutOfBounds(config.board)) {
            return state.copy(
                direction = nextDirection,
                pendingDirection = null,
                isGameOver = true
            )
        }

        val willEat = nextHead == state.food
        val collisionBody = if (willEat) state.snake else state.snake.dropLast(1)
        if (nextHead in collisionBody) {
            return state.copy(
                direction = nextDirection,
                pendingDirection = null,
                isGameOver = true
            )
        }

        val nextSnake = buildList {
            add(nextHead)
            addAll(if (willEat) state.snake else state.snake.dropLast(1))
        }

        val nextScore = state.score + if (willEat) 10 else 0
        val nextFood = if (willEat) {
            generateFoodOrNull(config.board, nextSnake)
        } else {
            state.food
        }

        return state.copy(
            snake = nextSnake,
            direction = nextDirection,
            pendingDirection = null,
            food = nextFood ?: nextHead,
            score = nextScore,
            isGameOver = nextFood == null && willEat,
            isVictory = nextFood == null && willEat
        )
    }

    private fun generateFood(board: SnakeBoardConfig, occupied: List<Cell>): Cell {
        return generateFoodOrNull(board, occupied)
            ?: error("Board is full, no available food positions.")
    }

    private fun generateFoodOrNull(board: SnakeBoardConfig, occupied: List<Cell>): Cell? {
        val occupiedSet = occupied.toHashSet()
        val available = mutableListOf<Cell>()
        for (row in 0 until board.rows) {
            for (col in 0 until board.cols) {
                val cell = Cell(row, col)
                if (cell !in occupiedSet) {
                    available += cell
                }
            }
        }
        if (available.isEmpty()) return null
        return available[random.nextInt(available.size)]
    }

    private fun Cell.isOutOfBounds(board: SnakeBoardConfig): Boolean {
        return row !in 0 until board.rows || col !in 0 until board.cols
    }
}
