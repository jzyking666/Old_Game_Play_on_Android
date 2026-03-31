package com.retro.arcade.feature.downshaft.engine

import com.retro.arcade.feature.downshaft.model.DownshaftBall
import com.retro.arcade.feature.downshaft.model.DownshaftDefaults
import com.retro.arcade.feature.downshaft.model.DownshaftDirection
import com.retro.arcade.feature.downshaft.model.DownshaftGameState
import com.retro.arcade.feature.downshaft.model.DownshaftPlatform
import com.retro.arcade.feature.downshaft.model.DownshaftPlatformKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DownshaftGameEngineTest {
    private val engine = DownshaftGameEngine(Random(0))

    @Test
    fun start_sets_run_active_without_forcing_a_bounce() {
        val state = engine.newGame()

        val next = engine.start(state)

        assertTrue(next.isStarted)
        assertEquals(0f, next.ball.vy, 0.001f)
    }

    @Test
    fun landing_on_normal_platform_settles_ball_on_surface() {
        val platform = DownshaftPlatform(
            id = 1,
            x = 80f,
            y = 100f,
            width = DownshaftDefaults.platformWidth,
            horizontalSpeed = 0f,
            kind = DownshaftPlatformKind.NORMAL,
            hasHeart = false
        )
        val state = DownshaftGameState(
            ball = DownshaftBall(
                x = 100f,
                y = 90f,
                vy = 4.5f
            ),
            platforms = listOf(platform),
            isStarted = true,
            nextPlatformId = 2
        )

        val next = engine.tick(state, 16.67f, DownshaftDirection.NONE)

        assertEquals(0f, next.ball.vy, 0.001f)
        assertEquals(platform.y - next.ball.radius, next.ball.y, 0.5f)
        assertEquals(3, next.lives)
    }

    @Test
    fun landing_on_spike_platform_costs_one_life() {
        val safePlatform = DownshaftPlatform(
            id = 2,
            x = 120f,
            y = 180f,
            width = DownshaftDefaults.platformWidth,
            horizontalSpeed = 0f,
            kind = DownshaftPlatformKind.NORMAL,
            hasHeart = false
        )
        val platform = DownshaftPlatform(
            id = 1,
            x = 80f,
            y = 100f,
            width = DownshaftDefaults.platformWidth,
            horizontalSpeed = 0f,
            kind = DownshaftPlatformKind.SPIKE,
            hasHeart = false
        )
        val state = DownshaftGameState(
            ball = DownshaftBall(
                x = 100f,
                y = 90f,
                vy = 4.5f
            ),
            platforms = listOf(platform, safePlatform),
            isStarted = true,
            nextPlatformId = 3
        )

        val next = engine.tick(state, 16.67f, DownshaftDirection.NONE)

        assertEquals(2, next.lives)
        assertTrue(next.ball.invulnerabilityMs > 0f)
        assertEquals(
            safePlatform.x + (safePlatform.width / 2f),
            next.ball.x,
            0.5f
        )
        assertEquals(
            safePlatform.y - next.ball.radius,
            next.ball.y,
            0.5f
        )
    }

    @Test
    fun heart_restores_life_but_not_above_max() {
        val platform = DownshaftPlatform(
            id = 1,
            x = 80f,
            y = 100f,
            width = DownshaftDefaults.platformWidth,
            horizontalSpeed = 0f,
            kind = DownshaftPlatformKind.NORMAL,
            hasHeart = true
        )
        val state = DownshaftGameState(
            ball = DownshaftBall(
                x = 115f,
                y = 90f,
                vy = 4.5f
            ),
            platforms = listOf(platform),
            isStarted = true,
            lives = 2,
            nextPlatformId = 2
        )

        val next = engine.tick(state, 16.67f, DownshaftDirection.NONE)

        assertEquals(3, next.lives)
        assertTrue(next.platforms.any { it.id == 1 && it.heartCollected })
    }

    @Test
    fun speed_and_score_rise_over_time() {
        var state = engine.start(engine.newGame())

        repeat(180) {
            state = engine.tick(state, 16.67f, DownshaftDirection.RIGHT)
        }

        assertTrue(state.scrollSpeed > DownshaftDefaults.startScrollSpeed)
        assertTrue(state.score > 0)
    }

    @Test
    fun touching_top_costs_one_life_and_respawns_on_safe_platform() {
        val safePlatform = DownshaftPlatform(
            id = 5,
            x = 96f,
            y = 170f,
            width = DownshaftDefaults.platformWidth,
            horizontalSpeed = 0f,
            kind = DownshaftPlatformKind.NORMAL,
            hasHeart = false
        )
        val state = DownshaftGameState(
            ball = DownshaftBall(
                x = 110f,
                y = 6f,
                vy = -2f
            ),
            platforms = listOf(safePlatform),
            isStarted = true,
            nextPlatformId = 6
        )

        val next = engine.tick(state, 16.67f, DownshaftDirection.NONE)

        assertEquals(2, next.lives)
        assertEquals(
            safePlatform.x + (safePlatform.width / 2f),
            next.ball.x,
            0.5f
        )
        assertEquals(
            safePlatform.y - next.ball.radius,
            next.ball.y,
            0.5f
        )
    }

    @Test
    fun falling_out_bottom_costs_one_life_and_respawns_on_safe_platform() {
        val safePlatform = DownshaftPlatform(
            id = 7,
            x = 88f,
            y = 190f,
            width = DownshaftDefaults.platformWidth,
            horizontalSpeed = 0f,
            kind = DownshaftPlatformKind.NORMAL,
            hasHeart = false
        )
        val state = DownshaftGameState(
            ball = DownshaftBall(
                x = 120f,
                y = DownshaftDefaults.logicalHeight + 20f,
                vy = 3f
            ),
            platforms = listOf(safePlatform),
            isStarted = true,
            nextPlatformId = 8
        )

        val next = engine.tick(state, 16.67f, DownshaftDirection.NONE)

        assertEquals(2, next.lives)
        assertEquals(
            safePlatform.x + (safePlatform.width / 2f),
            next.ball.x,
            0.5f
        )
        assertEquals(
            safePlatform.y - next.ball.radius,
            next.ball.y,
            0.5f
        )
    }
}
