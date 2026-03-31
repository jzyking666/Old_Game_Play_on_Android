package com.retro.arcade.feature.dino.engine

import com.retro.arcade.feature.dino.model.DinoCloud
import com.retro.arcade.feature.dino.model.DinoCollisionBox
import com.retro.arcade.feature.dino.model.DinoDefaults
import com.retro.arcade.feature.dino.model.DinoGameState
import com.retro.arcade.feature.dino.model.DinoNightState
import com.retro.arcade.feature.dino.model.DinoObstacle
import com.retro.arcade.feature.dino.model.DinoObstacleKind
import com.retro.arcade.feature.dino.model.DinoObstacleType
import com.retro.arcade.feature.dino.model.DinoRunnerState
import com.retro.arcade.feature.dino.model.DinoStar
import kotlin.random.Random

class DinoGameEngine(
    private val random: Random = Random.Default
) {
    fun newGame(bestScore: Int = 0): DinoGameState {
        return DinoGameState(
            bestScore = bestScore,
            cloudSpawnCooldownMs = randomCloudSpawnCooldown()
        )
    }

    fun press(state: DinoGameState): DinoGameState {
        if (state.isGameOver) return state
        if (!state.isStarted) {
            return startJump(state.copy(isStarted = true))
        }
        if (!state.runner.isJumping) {
            return startJump(state)
        }
        return state
    }

    fun duckPress(state: DinoGameState): DinoGameState {
        if (state.isGameOver || !state.isStarted) return state
        val runner = state.runner
        if (runner.isJumping) {
            return state.copy(
                runner = runner.copy(
                    duckPressed = true,
                    speedDrop = true,
                    jumpVelocity = maxOf(runner.jumpVelocity, DinoDefaults.dropVelocity)
                )
            )
        }

        return state.copy(
            runner = runner.copy(
                isDucking = true,
                duckPressed = true
            )
        )
    }

    fun release(state: DinoGameState): DinoGameState {
        if (!state.isStarted || state.isGameOver) return state
        val runner = state.runner
        if (!runner.isJumping || !runner.reachedMinHeight) return state
        if (runner.jumpVelocity >= DinoDefaults.dropVelocity) return state
        return state.copy(
            runner = runner.copy(
                jumpVelocity = DinoDefaults.dropVelocity,
                speedDrop = true
            )
        )
    }

    fun duckRelease(state: DinoGameState): DinoGameState {
        if (state.isGameOver) return state
        val runner = state.runner
        return state.copy(
            runner = runner.copy(
                duckPressed = false,
                isDucking = if (runner.isJumping) runner.isDucking else false
            )
        )
    }

    fun restart(state: DinoGameState): DinoGameState {
        return newGame(bestScore = maxOf(state.bestScore, state.displayScore))
    }

    fun tick(
        state: DinoGameState,
        deltaMs: Float
    ): DinoGameState {
        val stepMs = deltaMs.coerceIn(0f, 40f)
        var next = state.copy(
            elapsedTimeMs = state.elapsedTimeMs + stepMs,
            achievementFlashRemainingMs = (state.achievementFlashRemainingMs - stepMs)
                .coerceAtLeast(0f),
            invertRemainingMs = (state.invertRemainingMs - stepMs).coerceAtLeast(0f)
        )
        next = updateClouds(next, stepMs)
        next = updateNight(next, stepMs)

        if (!next.isStarted || next.isGameOver) {
            return next
        }

        val frames = stepMs / DinoDefaults.msPerFrame
        val runner = updateRunner(next.runner, frames)
        var obstacles = updateObstacles(next.obstacles, next.currentSpeed, frames, stepMs)
        val runningTimeMs = next.runningTimeMs + stepMs
        obstacles = maybeSpawnObstacle(obstacles, next.currentSpeed, runningTimeMs)

        val distanceRan = next.distanceRan + next.currentSpeed * frames
        val displayScore = (distanceRan * DinoDefaults.distanceCoefficient).toInt()
        val currentSpeed = (next.currentSpeed + DinoDefaults.acceleration * frames)
            .coerceAtMost(DinoDefaults.maxSpeed)
        val groundOffset = (next.groundOffset + next.currentSpeed * frames)
            .rem(DinoDefaults.groundSegmentWidth)

        val collided = obstacles.any { obstacle ->
            collides(runner, obstacle)
        }

        val scoreState = updateScoreState(
            previous = next,
            displayScore = displayScore
        )

        return next.copy(
            runner = runner,
            obstacles = obstacles,
            isGameOver = collided,
            currentSpeed = currentSpeed,
            distanceRan = distanceRan,
            displayScore = displayScore,
            bestScore = maxOf(next.bestScore, displayScore),
            runningTimeMs = runningTimeMs,
            groundOffset = groundOffset,
            achievementFlashRemainingMs = scoreState.achievementFlashRemainingMs,
            lastAchievementScore = scoreState.lastAchievementScore,
            invertRemainingMs = scoreState.invertRemainingMs,
            nextInvertScore = scoreState.nextInvertScore
        )
    }

    private fun startJump(state: DinoGameState): DinoGameState {
        val jumpVelocity = DinoDefaults.initialJumpVelocity - (state.currentSpeed / 10f)
        return state.copy(
            runner = state.runner.copy(
                y = DinoDefaults.runnerGroundY,
                jumpVelocity = jumpVelocity,
                isJumping = true,
                isDucking = false,
                duckPressed = false,
                reachedMinHeight = false,
                speedDrop = false
            )
        )
    }

    private fun updateRunner(
        runner: DinoRunnerState,
        frames: Float
    ): DinoRunnerState {
        if (!runner.isJumping) {
            return runner
        }

        var nextY = runner.y + (runner.jumpVelocity * frames)
        var nextVelocity = runner.jumpVelocity + (DinoDefaults.gravity * frames)
        val reachedMinHeight = runner.reachedMinHeight ||
            nextY < DinoDefaults.minJumpTop ||
            runner.speedDrop

        if ((nextY < DinoDefaults.maxJumpTop || runner.speedDrop) &&
            reachedMinHeight &&
            nextVelocity < DinoDefaults.dropVelocity
        ) {
            nextVelocity = DinoDefaults.dropVelocity
        }

        if (nextY > DinoDefaults.runnerGroundY) {
            nextY = DinoDefaults.runnerGroundY
            return runner.copy(
                y = nextY,
                jumpVelocity = 0f,
                isJumping = false,
                isDucking = runner.duckPressed,
                reachedMinHeight = false,
                speedDrop = false,
                jumpCount = runner.jumpCount + 1
            )
        }

        return runner.copy(
            y = nextY,
            jumpVelocity = nextVelocity,
            reachedMinHeight = reachedMinHeight
        )
    }

    private fun updateClouds(
        state: DinoGameState,
        deltaMs: Float
    ): DinoGameState {
        val frames = deltaMs / DinoDefaults.msPerFrame
        val movedClouds = state.clouds
            .map { cloud ->
                cloud.copy(
                    x = cloud.x - (DinoDefaults.bgCloudSpeed * frames)
                )
            }
            .filter { cloud ->
                cloud.x + DinoDefaults.cloudWidth > -8f
            }

        var cooldown = state.cloudSpawnCooldownMs - deltaMs
        var clouds = movedClouds
        if (clouds.isEmpty() && cooldown <= 0f) {
            clouds = listOf(
                DinoCloud(
                    x = DinoDefaults.logicalWidth + random.nextFloat() * 60f,
                    y = 20f + random.nextFloat() * 36f
                )
            )
            cooldown = randomCloudSpawnCooldown()
        }

        return state.copy(
            clouds = clouds,
            cloudSpawnCooldownMs = cooldown
        )
    }

    private fun updateNight(
        state: DinoGameState,
        deltaMs: Float
    ): DinoGameState {
        val frames = deltaMs / DinoDefaults.msPerFrame
        val currentlyNight = state.isNightMode
        val targetOpacity = if (currentlyNight) 1f else 0f
        val opacity = approach(
            current = state.nightState.opacity,
            target = targetOpacity,
            step = DinoDefaults.nightFadeSpeed * frames
        )

        val justActivated = currentlyNight && state.nightState.opacity == 0f
        val stars = when {
            justActivated -> buildStars()
            opacity == 0f -> emptyList()
            else -> state.nightState.stars.map { star ->
                star.copy(x = star.x - (DinoDefaults.starSpeed * frames))
            }.filter { star ->
                star.x + DinoDefaults.starSize > -10f
            }
        }

        val moonX = if (opacity > 0f) {
            if (justActivated) {
                DinoDefaults.logicalWidth
            } else {
                state.nightState.moonX - (DinoDefaults.moonSpeed * frames)
            }
        } else {
            DinoDefaults.logicalWidth
        }

        return state.copy(
            nightState = state.nightState.copy(
                moonX = moonX,
                phaseIndex = if (justActivated) {
                    (state.nightState.phaseIndex + 1) % DinoDefaults.moonPhaseSourceX.size
                } else {
                    state.nightState.phaseIndex
                },
                opacity = opacity,
                stars = stars
            )
        )
    }

    private fun updateObstacles(
        obstacles: List<DinoObstacle>,
        currentSpeed: Float,
        frames: Float,
        deltaMs: Float
    ): List<DinoObstacle> {
        return obstacles.map { obstacle ->
            val movedX = obstacle.x - ((currentSpeed + obstacle.speedOffset) * frames)
            if (obstacle.kind == DinoObstacleKind.PTERODACTYL) {
                val timer = obstacle.animationTimerMs + deltaMs
                val advanceFrame = timer >= 100f
                obstacle.copy(
                    x = movedX,
                    animationTimerMs = if (advanceFrame) timer - 100f else timer,
                    animationFrame = if (advanceFrame) (obstacle.animationFrame + 1) % 2
                    else obstacle.animationFrame
                )
            } else {
                obstacle.copy(x = movedX)
            }
        }.filter { obstacle ->
            obstacle.x + obstacle.width > -8f
        }
    }

    private fun maybeSpawnObstacle(
        obstacles: List<DinoObstacle>,
        currentSpeed: Float,
        runningTimeMs: Float
    ): List<DinoObstacle> {
        if (runningTimeMs < DinoDefaults.clearTimeMs) {
            return obstacles
        }

        val lastObstacle = obstacles.lastOrNull()
        if (lastObstacle != null &&
            lastObstacle.x + lastObstacle.width + lastObstacle.gap >= DinoDefaults.logicalWidth
        ) {
            return obstacles
        }

        return obstacles + createObstacle(currentSpeed)
    }

    private fun createObstacle(currentSpeed: Float): DinoObstacle {
        val availableTypes = DinoDefaults.obstacleTypes.filter { type ->
            currentSpeed >= type.minSpeed
        }
        val type = availableTypes[random.nextInt(availableTypes.size)]
        val size = if (type.maxSize > 1 && currentSpeed >= type.multipleSpeed) {
            random.nextInt(1, type.maxSize + 1)
        } else {
            1
        }
        val width = type.width * size
        val height = type.height
        val y = type.yPositions[random.nextInt(type.yPositions.size)]
        val speedOffset = type.speedOffsets[random.nextInt(type.speedOffsets.size)]
        val gap = calculateGap(type, width, currentSpeed)

        return DinoObstacle(
            kind = type.kind,
            x = DinoDefaults.logicalWidth + 8f,
            y = y,
            width = width,
            height = height,
            size = size,
            gap = gap,
            speedOffset = speedOffset,
            collisionBoxes = buildCollisionBoxes(type, size)
        )
    }

    private fun buildCollisionBoxes(
        type: DinoObstacleType,
        size: Int
    ): List<DinoCollisionBox> {
        if (size == 1) {
            return type.collisionBoxes
        }

        return (0 until size).flatMap { index ->
            type.collisionBoxes.map { box ->
                box.translated(index * type.width, 0f)
            }
        }
    }

    private fun calculateGap(
        type: DinoObstacleType,
        width: Float,
        currentSpeed: Float
    ): Float {
        val minGap = (type.minGap * DinoDefaults.gapCoefficient) + (width * currentSpeed)
        val multiplier = 1f + random.nextFloat() * (DinoDefaults.maxGapCoefficient - 1f)
        return minGap * multiplier
    }

    private fun collides(
        runner: DinoRunnerState,
        obstacle: DinoObstacle
    ): Boolean {
        val baseBoxes = if (runner.isDucking) {
            listOf(DinoCollisionBox(1f, 18f, 55f, 25f))
        } else {
            DinoDefaults.runnerCollisionBoxes
        }
        val runnerBoxes = baseBoxes.map { box ->
            box.translated(runner.x, runner.y)
        }
        val obstacleBoxes = obstacle.collisionBoxes.map { box ->
            box.translated(obstacle.x, obstacle.y)
        }

        return runnerBoxes.any { runnerBox ->
            obstacleBoxes.any { obstacleBox ->
                runnerBox.overlaps(obstacleBox)
            }
        }
    }

    private fun updateScoreState(
        previous: DinoGameState,
        displayScore: Int
    ): ScoreState {
        var flashRemaining = previous.achievementFlashRemainingMs
        var lastAchievementScore = previous.lastAchievementScore
        var invertRemaining = previous.invertRemainingMs
        var nextInvertScore = previous.nextInvertScore

        if (displayScore > 0 &&
            displayScore % DinoDefaults.achievementDistance == 0 &&
            displayScore != lastAchievementScore
        ) {
            flashRemaining = DinoDefaults.achievementFlashDurationMs
            lastAchievementScore = displayScore
        }

        while (displayScore >= nextInvertScore) {
            invertRemaining = DinoDefaults.invertDurationMs
            nextInvertScore += DinoDefaults.invertDistance
        }

        return ScoreState(
            achievementFlashRemainingMs = flashRemaining,
            lastAchievementScore = lastAchievementScore,
            invertRemainingMs = invertRemaining,
            nextInvertScore = nextInvertScore
        )
    }

    private fun randomCloudSpawnCooldown(): Float {
        return 1800f + random.nextFloat() * 2200f
    }

    private fun buildStars(): List<DinoStar> {
        val starCount = random.nextInt(2, 4)
        return List(starCount) { index ->
            DinoStar(
                x = DinoDefaults.logicalWidth + (index * 80f) + random.nextFloat() * 60f,
                y = 18f + random.nextFloat() * (DinoDefaults.starMaxY - 18f),
                spriteIndex = index % 2
            )
        }
    }

    private fun approach(current: Float, target: Float, step: Float): Float {
        return when {
            current < target -> (current + step).coerceAtMost(target)
            current > target -> (current - step).coerceAtLeast(target)
            else -> current
        }
    }

    private data class ScoreState(
        val achievementFlashRemainingMs: Float,
        val lastAchievementScore: Int,
        val invertRemainingMs: Float,
        val nextInvertScore: Int
    )
}
