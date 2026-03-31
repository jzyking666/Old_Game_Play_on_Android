package com.retro.arcade.feature.dino.model

data class DinoCollisionBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
) {
    fun translated(originX: Float, originY: Float): DinoCollisionBox {
        return copy(x = x + originX, y = y + originY)
    }

    fun overlaps(other: DinoCollisionBox): Boolean {
        return x < other.x + other.width &&
            x + width > other.x &&
            y < other.y + other.height &&
            y + height > other.y
    }
}

enum class DinoObstacleKind {
    SMALL_CACTUS,
    LARGE_CACTUS,
    PTERODACTYL
}

data class DinoObstacleType(
    val kind: DinoObstacleKind,
    val width: Float,
    val height: Float,
    val yPositions: List<Float>,
    val minSpeed: Float,
    val minGap: Float,
    val maxSize: Int,
    val multipleSpeed: Float,
    val collisionBoxes: List<DinoCollisionBox>,
    val animationFrameMs: Float = 0f,
    val speedOffsets: List<Float> = listOf(0f)
)

data class DinoObstacle(
    val kind: DinoObstacleKind,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val size: Int,
    val gap: Float,
    val speedOffset: Float,
    val collisionBoxes: List<DinoCollisionBox>,
    val animationFrame: Int = 0,
    val animationTimerMs: Float = 0f
)

data class DinoCloud(
    val x: Float,
    val y: Float
)

data class DinoStar(
    val x: Float,
    val y: Float,
    val spriteIndex: Int
)

data class DinoNightState(
    val moonX: Float = DinoDefaults.logicalWidth,
    val moonY: Float = 30f,
    val phaseIndex: Int = 0,
    val opacity: Float = 0f,
    val stars: List<DinoStar> = emptyList()
)

data class DinoRunnerState(
    val x: Float = DinoDefaults.runnerStartX,
    val y: Float = DinoDefaults.runnerGroundY,
    val jumpVelocity: Float = 0f,
    val isJumping: Boolean = false,
    val isDucking: Boolean = false,
    val duckPressed: Boolean = false,
    val reachedMinHeight: Boolean = false,
    val speedDrop: Boolean = false,
    val jumpCount: Int = 0
)

data class DinoGameState(
    val runner: DinoRunnerState = DinoRunnerState(),
    val obstacles: List<DinoObstacle> = emptyList(),
    val clouds: List<DinoCloud> = emptyList(),
    val isStarted: Boolean = false,
    val isGameOver: Boolean = false,
    val currentSpeed: Float = DinoDefaults.startSpeed,
    val distanceRan: Float = 0f,
    val displayScore: Int = 0,
    val bestScore: Int = 0,
    val runningTimeMs: Float = 0f,
    val elapsedTimeMs: Float = 0f,
    val groundOffset: Float = 0f,
    val achievementFlashRemainingMs: Float = 0f,
    val lastAchievementScore: Int = 0,
    val invertRemainingMs: Float = 0f,
    val nextInvertScore: Int = DinoDefaults.invertDistance,
    val cloudSpawnCooldownMs: Float = 1600f,
    val nightState: DinoNightState = DinoNightState()
) {
    val isNightMode: Boolean
        get() = invertRemainingMs > 0f
}

object DinoDefaults {
    const val logicalWidth = 600f
    const val logicalHeight = 150f
    const val msPerFrame = 1000f / 60f

    const val startSpeed = 6f
    const val maxSpeed = 13f
    const val acceleration = 0.001f
    const val clearTimeMs = 3000f
    const val distanceCoefficient = 0.025f

    const val invertDistance = 700
    const val invertDurationMs = 12000f
    const val achievementDistance = 100
    const val achievementFlashDurationMs = 1000f
    const val nightFadeSpeed = 0.035f
    const val moonSpeed = 0.25f
    const val starSpeed = 0.3f
    const val moonWidth = 20f
    const val moonHeight = 40f
    const val starSize = 9f
    const val starMaxY = 70f
    val moonPhaseSourceX = listOf(140, 120, 100, 60, 40, 20, 0)

    const val horizonLineY = 127f
    const val groundBaseline = 140f
    const val groundSegmentWidth = 24f

    const val runnerStartX = 50f
    const val runnerWidth = 44f
    const val runnerHeight = 47f
    const val runnerGroundY = logicalHeight - runnerHeight - 10f

    const val gravity = 0.6f
    const val initialJumpVelocity = -10f
    const val minJumpHeight = 30f
    const val minJumpTop = runnerGroundY - minJumpHeight
    const val maxJumpTop = 30f
    const val dropVelocity = -5f
    const val speedDropCoefficient = 3f

    const val bgCloudSpeed = 0.2f
    const val gapCoefficient = 0.6f
    const val maxGapCoefficient = 1.5f
    const val cloudWidth = 46f
    const val cloudHeight = 14f

    val runnerCollisionBoxes = listOf(
        DinoCollisionBox(1f, 7f, 30f, 27f),
        DinoCollisionBox(5f, 13f, 14f, 7f),
        DinoCollisionBox(4f, 21f, 16f, 6f),
        DinoCollisionBox(10f, 28f, 18f, 9f),
        DinoCollisionBox(21f, 29f, 15f, 5f),
        DinoCollisionBox(1f, 37f, 30f, 10f)
    )

    val obstacleTypes = listOf(
        DinoObstacleType(
            kind = DinoObstacleKind.SMALL_CACTUS,
            width = 17f,
            height = 35f,
            yPositions = listOf(105f),
            minSpeed = 0f,
            minGap = 120f,
            maxSize = 3,
            multipleSpeed = 4f,
            collisionBoxes = listOf(
                DinoCollisionBox(0f, 7f, 5f, 27f),
                DinoCollisionBox(4f, 0f, 6f, 34f),
                DinoCollisionBox(10f, 4f, 7f, 14f)
            )
        ),
        DinoObstacleType(
            kind = DinoObstacleKind.LARGE_CACTUS,
            width = 25f,
            height = 50f,
            yPositions = listOf(90f),
            minSpeed = 0f,
            minGap = 120f,
            maxSize = 3,
            multipleSpeed = 7f,
            collisionBoxes = listOf(
                DinoCollisionBox(0f, 12f, 7f, 38f),
                DinoCollisionBox(8f, 0f, 7f, 49f),
                DinoCollisionBox(13f, 10f, 10f, 38f)
            )
        ),
        DinoObstacleType(
            kind = DinoObstacleKind.PTERODACTYL,
            width = 46f,
            height = 40f,
            yPositions = listOf(100f, 75f, 50f),
            minSpeed = 8.5f,
            minGap = 150f,
            maxSize = 1,
            multipleSpeed = Float.MAX_VALUE,
            collisionBoxes = listOf(
                DinoCollisionBox(15f, 15f, 16f, 5f),
                DinoCollisionBox(18f, 21f, 24f, 6f),
                DinoCollisionBox(2f, 14f, 4f, 3f),
                DinoCollisionBox(6f, 10f, 4f, 7f),
                DinoCollisionBox(10f, 8f, 6f, 9f)
            ),
            animationFrameMs = 100f,
            speedOffsets = listOf(-0.8f, 0f, 0.8f)
        )
    )
}
