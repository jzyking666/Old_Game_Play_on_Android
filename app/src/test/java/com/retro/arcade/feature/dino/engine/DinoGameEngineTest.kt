package com.retro.arcade.feature.dino.engine

import com.retro.arcade.feature.dino.model.DinoDefaults
import com.retro.arcade.feature.dino.model.DinoObstacle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DinoGameEngineTest {
    private val engine = DinoGameEngine(Random(0))

    @Test
    fun press_starts_run_and_jump() {
        val state = engine.newGame()

        val next = engine.press(state)

        assertTrue(next.isStarted)
        assertTrue(next.runner.isJumping)
        assertTrue(next.runner.jumpVelocity < 0f)
    }

    @Test
    fun release_after_min_height_triggers_faster_drop() {
        var state = engine.press(engine.newGame())

        repeat(12) {
            state = engine.tick(state, 16.67f)
        }
        state = engine.release(state)

        assertTrue(state.runner.reachedMinHeight)
        assertTrue(state.runner.speedDrop)
        assertEquals(DinoDefaults.dropVelocity, state.runner.jumpVelocity, 0.001f)
    }

    @Test
    fun collision_with_obstacle_ends_game() {
        val obstacleType = DinoDefaults.obstacleTypes.first()
        val obstacle = DinoObstacle(
            kind = obstacleType.kind,
            x = DinoDefaults.runnerStartX + 6f,
            y = obstacleType.yPositions.first(),
            width = obstacleType.width,
            height = obstacleType.height,
            size = 1,
            gap = 0f,
            speedOffset = 0f,
            collisionBoxes = obstacleType.collisionBoxes
        )
        val state = engine.newGame().copy(
            isStarted = true,
            obstacles = listOf(obstacle)
        )

        val next = engine.tick(state, 16.67f)

        assertTrue(next.isGameOver)
    }

    @Test
    fun score_grows_while_running() {
        var state = engine.press(engine.newGame())

        repeat(120) {
            state = engine.tick(state, 16.67f)
        }

        assertTrue(state.displayScore > 0)
        assertEquals(state.displayScore, state.bestScore)
    }
}
