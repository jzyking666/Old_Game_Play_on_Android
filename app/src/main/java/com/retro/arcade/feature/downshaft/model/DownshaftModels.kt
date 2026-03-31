package com.retro.arcade.feature.downshaft.model

enum class DownshaftDirection(val axis: Float) {
    LEFT(axis = -1f),
    NONE(axis = 0f),
    RIGHT(axis = 1f)
}

enum class DownshaftPlatformKind {
    NORMAL,
    SPIKE
}

data class DownshaftBall(
    val x: Float,
    val y: Float,
    val vy: Float,
    val radius: Float = DownshaftDefaults.ballRadius,
    val invulnerabilityMs: Float = 0f
)

data class DownshaftPlatform(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float = DownshaftDefaults.platformHeight,
    val horizontalSpeed: Float,
    val kind: DownshaftPlatformKind,
    val hasHeart: Boolean,
    val heartCollected: Boolean = false
)

data class DownshaftGameState(
    val ball: DownshaftBall,
    val platforms: List<DownshaftPlatform>,
    val lives: Int = DownshaftDefaults.maxLives,
    val maxLives: Int = DownshaftDefaults.maxLives,
    val distance: Float = 0f,
    val score: Int = 0,
    val scrollSpeed: Float = DownshaftDefaults.startScrollSpeed,
    val elapsedMs: Float = 0f,
    val isStarted: Boolean = false,
    val isGameOver: Boolean = false,
    val nextPlatformId: Int = 0
)

object DownshaftDefaults {
    const val logicalWidth = 240f
    const val logicalHeight = 360f
    const val msPerFrame = 1000f / 60f

    const val maxLives = 3
    const val ballRadius = 8f
    const val platformHeight = 10f
    const val platformWidth = 72f
    const val minGap = 38f
    const val maxGap = 60f
    const val sidePadding = 10f
    const val heartRadius = 7f
    const val platformSnapTolerance = 6f

    const val startGravity = 0.11f
    const val maxGravity = 0.34f
    const val startFallSpeed = 2.35f
    const val maxFallSpeed = 5.7f
    const val startMoveSpeed = 1.45f
    const val maxMoveSpeed = 3.25f
    const val startScrollSpeed = 0.42f
    const val maxScrollSpeed = 2.15f
    const val scrollAcceleration = 0.00062f

    const val invulnerabilityMs = 900f
    const val scoreStepDistance = 20f
    const val respawnTopPadding = 34f
    const val respawnBottomPadding = 48f

    const val initialPlatformY = 230f
    const val initialBallX = logicalWidth / 2f
    const val initialBallY = initialPlatformY - ballRadius
}
