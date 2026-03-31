package com.retro.arcade.feature.snake.engine

import com.retro.arcade.feature.snake.model.Cell
import com.retro.arcade.feature.snake.model.Direction
import com.retro.arcade.feature.snake.model.SnakeBoardConfig
import com.retro.arcade.feature.snake.model.SnakeDifficulty
import com.retro.arcade.feature.snake.model.SnakeGameConfig
import com.retro.arcade.feature.snake.model.SnakeGameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SnakeGameEngineTest {
    private val config = SnakeGameConfig(
        board = SnakeBoardConfig(rows = 6, cols = 6),
        difficulty = SnakeDifficulty.NORMAL
    )
    private val engine = SnakeGameEngine(Random(0))

    @Test
    fun reverse_direction_input_is_ignored() {
        val state = SnakeGameState(
            snake = listOf(Cell(2, 3), Cell(2, 2), Cell(2, 1)),
            direction = Direction.RIGHT,
            food = Cell(0, 0)
        )

        val reverse = engine.enqueueDirection(state, Direction.LEFT)
        val validTurn = engine.enqueueDirection(state, Direction.DOWN)

        assertNull(reverse.pendingDirection)
        assertEquals(Direction.DOWN, validTurn.pendingDirection)
    }

    @Test
    fun eating_food_grows_snake_and_adds_score() {
        val state = SnakeGameState(
            snake = listOf(Cell(2, 3), Cell(2, 2), Cell(2, 1)),
            direction = Direction.RIGHT,
            food = Cell(2, 4)
        )

        val next = engine.tick(state, config)

        assertEquals(Cell(2, 4), next.snake.first())
        assertEquals(4, next.snake.size)
        assertEquals(10, next.score)
        assertFalse(next.isGameOver)
        assertFalse(next.food in next.snake)
    }

    @Test
    fun hitting_wall_ends_game() {
        val state = SnakeGameState(
            snake = listOf(Cell(0, 2), Cell(1, 2), Cell(2, 2)),
            direction = Direction.UP,
            food = Cell(5, 5)
        )

        val next = engine.tick(state, config)

        assertTrue(next.isGameOver)
        assertFalse(next.isVictory)
    }

    @Test
    fun running_into_body_ends_game() {
        val state = SnakeGameState(
            snake = listOf(
                Cell(2, 2),
                Cell(2, 1),
                Cell(1, 1),
                Cell(1, 2),
                Cell(1, 3),
                Cell(2, 3)
            ),
            direction = Direction.UP,
            food = Cell(5, 5)
        )

        val next = engine.tick(state, config)

        assertTrue(next.isGameOver)
    }
}
