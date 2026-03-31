package com.retro.arcade.feature.downshaft.engine

import com.retro.arcade.feature.downshaft.model.DownshaftBall
import com.retro.arcade.feature.downshaft.model.DownshaftDefaults
import com.retro.arcade.feature.downshaft.model.DownshaftDirection
import com.retro.arcade.feature.downshaft.model.DownshaftGameState
import com.retro.arcade.feature.downshaft.model.DownshaftPlatform
import com.retro.arcade.feature.downshaft.model.DownshaftPlatformKind
import kotlin.random.Random

class DownshaftGameEngine(
    private val random: Random = Random.Default
) {
    fun newGame(bestScore: Int = 0): DownshaftGameState {
        val platforms = buildInitialPlatforms()
        return DownshaftGameState(
            ball = DownshaftBall(
                x = DownshaftDefaults.initialBallX,
                y = DownshaftDefaults.initialBallY,
                vy = 0f
            ),
            platforms = platforms,
            score = 0,
            nextPlatformId = platforms.maxOf { it.id } + 1
        )
    }

    fun start(state: DownshaftGameState): DownshaftGameState {
        if (state.isStarted || state.isGameOver) return state
        return state.copy(
            isStarted = true,
            ball = state.ball.copy(vy = 0f)
        )
    }

    fun tick(
        state: DownshaftGameState,
        deltaMs: Float,
        direction: DownshaftDirection
    ): DownshaftGameState {
        val stepMs = deltaMs.coerceIn(0f, 40f)
        if (stepMs <= 0f) return state

        val frames = stepMs / DownshaftDefaults.msPerFrame
        var next = state.copy(
            elapsedMs = state.elapsedMs + stepMs,
            ball = state.ball.copy(
                invulnerabilityMs = (state.ball.invulnerabilityMs - stepMs).coerceAtLeast(0f)
            )
        )

        if (!next.isStarted || next.isGameOver) {
            return next
        }

        val scrollSpeed = (next.scrollSpeed + DownshaftDefaults.scrollAcceleration * frames)
            .coerceAtMost(DownshaftDefaults.maxScrollSpeed)
        val distance = next.distance + (scrollSpeed * frames)
        val score = (distance / DownshaftDefaults.scoreStepDistance).toInt()

        val movedPlatforms = next.platforms.map { platform ->
            updatePlatform(platform, scrollSpeed, frames)
        }
        val movedBall = updateBall(
            ball = next.ball,
            direction = direction,
            frames = frames,
            scrollSpeed = scrollSpeed
        )

        val collisionResult = resolvePlatformCollision(
            ball = movedBall,
            previousBall = next.ball,
            platforms = movedPlatforms,
            lives = next.lives
        )

        val boundaryResult = resolveBoundaryPenalty(
            ball = collisionResult.ball,
            lives = collisionResult.lives,
            platforms = collisionResult.platforms
        )

        val heartResult = collectHearts(
            platforms = boundaryResult.platforms,
            ball = boundaryResult.ball,
            currentLives = boundaryResult.lives,
            maxLives = next.maxLives
        )

        val cleanedPlatforms = heartResult.platforms.filter { it.y + it.height > 0f }
        val spawnResult = spawnPlatformsIfNeeded(
            platforms = cleanedPlatforms,
            nextPlatformId = next.nextPlatformId,
            distance = distance
        )

        val gameOver = heartResult.lives <= 0

        return next.copy(
            ball = heartResult.ball,
            platforms = spawnResult.platforms,
            lives = heartResult.lives,
            distance = distance,
            score = score,
            scrollSpeed = scrollSpeed,
            isGameOver = gameOver,
            nextPlatformId = spawnResult.nextPlatformId
        )
    }

    private fun collectHearts(
        platforms: List<DownshaftPlatform>,
        ball: DownshaftBall,
        currentLives: Int,
        maxLives: Int
    ): HeartCollectionResult {
        var lives = currentLives
        val updatedPlatforms = platforms.map { platform ->
            val result = maybeCollectHeart(
                ball = ball,
                platform = platform,
                currentLives = lives,
                maxLives = maxLives
            )
            if (result.healed) {
                lives = (lives + 1).coerceAtMost(maxLives)
            }
            result.platform
        }
        return HeartCollectionResult(
            ball = ball,
            platforms = updatedPlatforms,
            lives = lives
        )
    }

    private fun buildInitialPlatforms(): List<DownshaftPlatform> {
        val platforms = mutableListOf<DownshaftPlatform>()
        var nextId = 0
        var y = DownshaftDefaults.initialPlatformY

        platforms += DownshaftPlatform(
            id = nextId++,
            x = DownshaftDefaults.logicalWidth / 2f - 38f,
            y = y,
            width = DownshaftDefaults.platformWidth,
            horizontalSpeed = 0f,
            kind = DownshaftPlatformKind.NORMAL,
            hasHeart = false
        )

        while (y < DownshaftDefaults.logicalHeight - DownshaftDefaults.platformHeight) {
            y += randomGap()
            platforms += createPlatform(
                id = nextId++,
                y = y,
                distance = 0f
            )
        }

        return platforms
    }

    private fun updatePlatform(
        platform: DownshaftPlatform,
        scrollSpeed: Float,
        frames: Float
    ): DownshaftPlatform {
        return platform.copy(
            y = platform.y - (scrollSpeed * frames),
            horizontalSpeed = 0f
        )
    }

    private fun updateBall(
        ball: DownshaftBall,
        direction: DownshaftDirection,
        frames: Float,
        scrollSpeed: Float
    ): DownshaftBall {
        val speedRatio = (
            (scrollSpeed - DownshaftDefaults.startScrollSpeed) /
                (DownshaftDefaults.maxScrollSpeed - DownshaftDefaults.startScrollSpeed)
            ).coerceIn(0f, 1f)
        val moveSpeed = lerp(
            start = DownshaftDefaults.startMoveSpeed,
            end = DownshaftDefaults.maxMoveSpeed,
            fraction = speedRatio
        )
        val gravity = lerp(
            start = DownshaftDefaults.startGravity,
            end = DownshaftDefaults.maxGravity,
            fraction = speedRatio
        )
        val maxFallSpeed = lerp(
            start = DownshaftDefaults.startFallSpeed,
            end = DownshaftDefaults.maxFallSpeed,
            fraction = speedRatio
        )

        val nextX = (ball.x + (direction.axis * moveSpeed * frames))
            .coerceIn(
                DownshaftDefaults.ballRadius,
                DownshaftDefaults.logicalWidth - DownshaftDefaults.ballRadius
            )
        val nextVy = (ball.vy + (gravity * frames))
            .coerceAtMost(maxFallSpeed)
        return ball.copy(
            x = nextX,
            y = ball.y + (nextVy * frames),
            vy = nextVy
        )
    }

    private fun resolvePlatformCollision(
        ball: DownshaftBall,
        previousBall: DownshaftBall,
        platforms: List<DownshaftPlatform>,
        lives: Int
    ): CollisionResult {
        if (ball.vy < 0f) {
            return CollisionResult(
                ball = ball,
                platforms = platforms,
                lives = lives
            )
        }

        val previousBottom = previousBall.y + previousBall.radius
        val nextBottom = ball.y + ball.radius

        val supportingPlatform = platforms
            .filter { platform ->
                val withinX = ball.x + ball.radius > platform.x &&
                    ball.x - ball.radius < platform.x + platform.width
                val nearTop = nextBottom >= platform.y - DownshaftDefaults.platformSnapTolerance &&
                    nextBottom <= platform.y + platform.height + DownshaftDefaults.platformSnapTolerance
                val wasAbovePlatform = previousBottom <= platform.y + platform.height
                withinX && nearTop && wasAbovePlatform
            }
            .minByOrNull { platform ->
                kotlin.math.abs((ball.y + ball.radius) - platform.y)
            }

        if (supportingPlatform != null) {
            val settledBall = ball.copy(
                y = supportingPlatform.y - ball.radius,
                vy = 0f
            )

            if (supportingPlatform.kind == DownshaftPlatformKind.SPIKE &&
                ball.invulnerabilityMs <= 0f
            ) {
                val respawnBall = respawnBall(
                    currentBall = settledBall,
                    platforms = platforms
                ).copy(
                    invulnerabilityMs = DownshaftDefaults.invulnerabilityMs
                )
                return CollisionResult(
                    ball = respawnBall,
                    platforms = platforms,
                    lives = lives - 1
                )
            }

            return CollisionResult(
                ball = settledBall,
                platforms = platforms,
                lives = lives
            )
        }

        return CollisionResult(
            ball = ball,
            platforms = platforms,
            lives = lives
        )
    }

    private fun maybeCollectHeart(
        ball: DownshaftBall,
        platform: DownshaftPlatform,
        currentLives: Int,
        maxLives: Int
    ): HeartPickupCheck {
        if (!platform.hasHeart || platform.heartCollected || currentLives >= maxLives) {
            return HeartPickupCheck(
                platform = platform,
                healed = false
            )
        }

        val heartX = platform.x + (platform.width / 2f)
        val heartY = platform.y - 10f
        val dx = ball.x - heartX
        val dy = ball.y - heartY
        val touchDistance = ball.radius + DownshaftDefaults.heartRadius
        return if ((dx * dx) + (dy * dy) <= touchDistance * touchDistance) {
            HeartPickupCheck(
                platform = platform.copy(heartCollected = true),
                healed = true
            )
        } else {
            HeartPickupCheck(
                platform = platform,
                healed = false
            )
        }
    }

    private fun resolveBoundaryPenalty(
        ball: DownshaftBall,
        lives: Int,
        platforms: List<DownshaftPlatform>
    ): BoundaryResult {
        val hitTop = ball.y - ball.radius <= 0f
        val fellBottom = ball.y - ball.radius > DownshaftDefaults.logicalHeight
        if (!hitTop && !fellBottom) {
            return BoundaryResult(
                ball = ball,
                platforms = platforms,
                lives = lives
            )
        }

        val respawnBall = respawnBall(
            currentBall = ball,
            platforms = platforms
        ).copy(
            invulnerabilityMs = DownshaftDefaults.invulnerabilityMs
        )

        return BoundaryResult(
            ball = respawnBall,
            platforms = platforms,
            lives = lives - 1
        )
    }

    private fun spawnPlatformsIfNeeded(
        platforms: List<DownshaftPlatform>,
        nextPlatformId: Int,
        distance: Float
    ): SpawnResult {
        val result = platforms.toMutableList()
        var nextId = nextPlatformId
        var lastY = result.maxOfOrNull { it.y } ?: DownshaftDefaults.initialPlatformY

        while (lastY < DownshaftDefaults.logicalHeight - DownshaftDefaults.platformHeight) {
            lastY += randomGap()
            result += createPlatform(
                id = nextId++,
                y = lastY,
                distance = distance
            )
        }

        return SpawnResult(
            platforms = result,
            nextPlatformId = nextId
        )
    }

    private fun createPlatform(
        id: Int,
        y: Float,
        distance: Float
    ): DownshaftPlatform {
        val width = DownshaftDefaults.platformWidth
        val x = random.nextFloat() * (
            DownshaftDefaults.logicalWidth -
                DownshaftDefaults.sidePadding * 2f -
                width
            ) + DownshaftDefaults.sidePadding
        val spikeChance = (0.09f + (distance / 18000f)).coerceIn(0.09f, 0.24f)
        val kind = if (random.nextFloat() < spikeChance) {
            DownshaftPlatformKind.SPIKE
        } else {
            DownshaftPlatformKind.NORMAL
        }
        val heartChance = (0.24f - (distance / 22000f)).coerceIn(0.08f, 0.24f)
        val hasHeart = kind == DownshaftPlatformKind.NORMAL && random.nextFloat() < heartChance

        return DownshaftPlatform(
            id = id,
            x = x,
            y = y,
            width = width,
            horizontalSpeed = 0f,
            kind = kind,
            hasHeart = hasHeart
        )
    }

    private fun randomGap(): Float {
        return DownshaftDefaults.minGap +
            random.nextFloat() * (DownshaftDefaults.maxGap - DownshaftDefaults.minGap)
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + ((end - start) * fraction)
    }

    private fun respawnBall(
        currentBall: DownshaftBall,
        platforms: List<DownshaftPlatform>
    ): DownshaftBall {
        val visibleSafePlatforms = platforms.filter { platform ->
            platform.kind == DownshaftPlatformKind.NORMAL &&
                (!platform.hasHeart || platform.heartCollected) &&
                platform.y >= DownshaftDefaults.respawnTopPadding &&
                platform.y <= DownshaftDefaults.logicalHeight - DownshaftDefaults.respawnBottomPadding
        }

        val chosenPlatform = visibleSafePlatforms
            .minByOrNull { platform ->
                kotlin.math.abs(
                    (DownshaftDefaults.logicalHeight * 0.62f) - platform.y
                )
            }
            ?: platforms
                .filter {
                    it.kind == DownshaftPlatformKind.NORMAL &&
                        (!it.hasHeart || it.heartCollected)
                }
                .maxByOrNull { it.y }
            ?: platforms
                .filter { it.kind == DownshaftPlatformKind.NORMAL }
                .maxByOrNull { it.y }

        return if (chosenPlatform != null) {
            currentBall.copy(
                x = chosenPlatform.x + (chosenPlatform.width / 2f),
                y = chosenPlatform.y - currentBall.radius,
                vy = 0f
            )
        } else {
            currentBall.copy(
                x = DownshaftDefaults.initialBallX,
                y = DownshaftDefaults.initialBallY,
                vy = 0f
            )
        }
    }

    private data class CollisionResult(
        val ball: DownshaftBall,
        val platforms: List<DownshaftPlatform>,
        val lives: Int
    )

    private data class SpawnResult(
        val platforms: List<DownshaftPlatform>,
        val nextPlatformId: Int
    )

    private data class HeartPickupCheck(
        val platform: DownshaftPlatform,
        val healed: Boolean
    )

    private data class HeartCollectionResult(
        val ball: DownshaftBall,
        val platforms: List<DownshaftPlatform>,
        val lives: Int
    )

    private data class BoundaryResult(
        val ball: DownshaftBall,
        val platforms: List<DownshaftPlatform>,
        val lives: Int
    )
}
