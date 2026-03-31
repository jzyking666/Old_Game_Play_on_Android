package com.retro.arcade.feature.dino.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retro.arcade.R
import com.retro.arcade.feature.dino.DinoGameUiState
import com.retro.arcade.feature.dino.DinoGameViewModel
import com.retro.arcade.feature.dino.model.DinoCloud
import com.retro.arcade.feature.dino.model.DinoDefaults
import com.retro.arcade.feature.dino.model.DinoGameState
import com.retro.arcade.feature.dino.model.DinoObstacle
import com.retro.arcade.feature.dino.model.DinoObstacleKind
import com.retro.arcade.feature.dino.model.DinoRunnerState
import com.retro.arcade.feature.dino.model.DinoStar
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinoGameScreen(
    bestScore: Int,
    onBack: () -> Unit,
    onPersistBestScore: suspend (Int) -> Unit,
    viewModel: DinoGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalContext.current.resources
    val spriteSheet = remember {
        ImageBitmap.imageResource(resources, R.drawable.chrome_dino_sprite_1x)
    }
    var savedSessionId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(bestScore) {
        viewModel.initialize(bestScore)
    }

    LaunchedEffect(uiState.sessionId) {
        savedSessionId = null
    }

    LaunchedEffect(uiState.sessionId, uiState.gameState.isGameOver) {
        if (uiState.gameState.isGameOver && savedSessionId != uiState.sessionId) {
            savedSessionId = uiState.sessionId
            onPersistBestScore(uiState.gameState.displayScore)
        }
    }

    val palette = remember(uiState.gameState.isNightMode) {
        if (uiState.gameState.isNightMode) {
            DinoPalette(
                sky = Color(0xFF111111),
                foreground = Color(0xFFF7F7F7),
                border = Color(0xFF3B3B3B),
                surface = Color(0xFF171717)
            )
        } else {
            DinoPalette(
                sky = Color(0xFFF7F7F7),
                foreground = Color(0xFF535353),
                border = Color(0xFFD7D7D7),
                surface = Color(0xFFFFFFFF)
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Offline Dino") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DinoBoard(
                uiState = uiState,
                spriteSheet = spriteSheet,
                palette = palette,
                onJumpPress = viewModel::onPress,
                onJumpRelease = viewModel::onRelease,
                onRestart = viewModel::restart
            )
            DinoControlPad(
                gameState = uiState.gameState,
                palette = palette,
                onJumpPress = viewModel::onPress,
                onJumpRelease = viewModel::onRelease,
                onDuckPress = viewModel::onDuckPress,
                onDuckRelease = viewModel::onDuckRelease,
                onRestart = viewModel::restart
            )
            Text(
                text = "Desktop-style controls are mapped below: jump on the left, duck on the right.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun DinoBoard(
    uiState: DinoGameUiState,
    spriteSheet: ImageBitmap,
    palette: DinoPalette,
    onJumpPress: () -> Unit,
    onJumpRelease: () -> Unit,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f)
            .background(palette.sky, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = palette.border,
                shape = RoundedCornerShape(12.dp)
            )
            .dinoActionInput(
                enabled = !uiState.gameState.isGameOver,
                onPress = onJumpPress,
                onRelease = onJumpRelease
            )
            .dinoRestartInput(
                enabled = uiState.gameState.isGameOver,
                onRestart = onRestart
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val scaleX = size.width / DinoDefaults.logicalWidth
            val scaleY = size.height / DinoDefaults.logicalHeight
            val unit = min(scaleX, scaleY)

            fun dx(value: Float) = value * scaleX
            fun dy(value: Float) = value * scaleY
            fun dw(value: Float) = value * scaleX
            fun dh(value: Float) = value * scaleY

            drawRoundRect(
                color = palette.sky,
                cornerRadius = CornerRadius(4f * unit, 4f * unit)
            )

            if (uiState.gameState.nightState.opacity > 0f) {
                drawNightLayer(
                    gameState = uiState.gameState,
                    spriteSheet = spriteSheet,
                    dx = ::dx,
                    dy = ::dy,
                    dw = ::dw,
                    dh = ::dh,
                    palette = palette
                )
            }

            uiState.gameState.clouds.forEach { cloud ->
                drawCloud(
                    spriteSheet = spriteSheet,
                    cloud = cloud,
                    dx = ::dx,
                    dy = ::dy,
                    dw = ::dw,
                    dh = ::dh,
                    tint = palette.foreground
                )
            }

            drawHorizon(
                gameState = uiState.gameState,
                spriteSheet = spriteSheet,
                dx = ::dx,
                dy = ::dy,
                dw = ::dw,
                dh = ::dh,
                tint = palette.foreground
            )

            uiState.gameState.obstacles.forEach { obstacle ->
                drawObstacle(
                    spriteSheet = spriteSheet,
                    obstacle = obstacle,
                    dx = ::dx,
                    dy = ::dy,
                    dw = ::dw,
                    dh = ::dh,
                    tint = palette.foreground
                )
            }

            drawRunner(
                spriteSheet = spriteSheet,
                state = uiState.gameState,
                runner = uiState.gameState.runner,
                dx = ::dx,
                dy = ::dy,
                dw = ::dw,
                dh = ::dh,
                tint = palette.foreground
            )

            drawScore(
                spriteSheet = spriteSheet,
                state = uiState.gameState,
                dx = ::dx,
                dy = ::dy,
                dw = ::dw,
                dh = ::dh,
                tint = palette.foreground
            )

            if (uiState.gameState.isGameOver) {
                drawGameOver(
                    spriteSheet = spriteSheet,
                    dx = ::dx,
                    dy = ::dy,
                    dw = ::dw,
                    dh = ::dh,
                    tint = palette.foreground
                )
            }
        }
    }
}

@Composable
private fun DinoControlPad(
    gameState: DinoGameState,
    palette: DinoPalette,
    onJumpPress: () -> Unit,
    onJumpRelease: () -> Unit,
    onDuckPress: () -> Unit,
    onDuckRelease: () -> Unit,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DinoActionPad(
            modifier = Modifier.weight(1.6f),
            title = if (gameState.isGameOver) "RETRY" else "JUMP",
            subtitle = if (gameState.isGameOver) {
                "Tap to restart"
            } else {
                "Tap or hold"
            },
            icon = Icons.Default.ArrowUpward,
            palette = palette,
            onPress = if (gameState.isGameOver) onRestart else onJumpPress,
            onRelease = if (gameState.isGameOver) ({}) else onJumpRelease
        )
        DinoActionPad(
            modifier = Modifier.weight(1f),
            title = "DUCK",
            subtitle = if (gameState.isStarted && !gameState.isGameOver) {
                "Hold low"
            } else {
                "After start"
            },
            icon = Icons.Default.ArrowDownward,
            palette = palette,
            enabled = gameState.isStarted && !gameState.isGameOver,
            onPress = onDuckPress,
            onRelease = onDuckRelease
        )
    }
}

@Composable
private fun DinoActionPad(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    palette: DinoPalette,
    enabled: Boolean = true,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(108.dp)
            .dinoActionInput(
                enabled = enabled,
                onPress = onPress,
                onRelease = onRelease
            ),
        color = palette.surface,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun DrawScope.drawNightLayer(
    gameState: DinoGameState,
    spriteSheet: ImageBitmap,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    palette: DinoPalette
) {
    val alpha = gameState.nightState.opacity
    val moon = OriginalDinoSpriteSheet.moon(gameState.nightState.phaseIndex)
    drawSprite(
        image = spriteSheet,
        sprite = moon,
        dstTopLeft = Offset(dx(gameState.nightState.moonX), dy(gameState.nightState.moonY)),
        dstWidth = dw(moon.width.toFloat()),
        dstHeight = dh(moon.height.toFloat()),
        tint = palette.foreground,
        alpha = alpha
    )

    gameState.nightState.stars.forEach { star ->
        drawStar(
            spriteSheet = spriteSheet,
            star = star,
            dx = dx,
            dy = dy,
            dw = dw,
            dh = dh,
            tint = palette.foreground,
            alpha = alpha
        )
    }
}

private fun DrawScope.drawStar(
    spriteSheet: ImageBitmap,
    star: DinoStar,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    tint: Color,
    alpha: Float
) {
    val sprite = OriginalDinoSpriteSheet.star(star.spriteIndex)
    drawSprite(
        image = spriteSheet,
        sprite = sprite,
        dstTopLeft = Offset(dx(star.x), dy(star.y)),
        dstWidth = dw(sprite.width.toFloat()),
        dstHeight = dh(sprite.height.toFloat()),
        tint = tint,
        alpha = alpha
    )
}

private fun DrawScope.drawCloud(
    spriteSheet: ImageBitmap,
    cloud: DinoCloud,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    tint: Color
) {
    val sprite = OriginalDinoSpriteSheet.cloud
    drawSprite(
        image = spriteSheet,
        sprite = sprite,
        dstTopLeft = Offset(dx(cloud.x), dy(cloud.y)),
        dstWidth = dw(sprite.width.toFloat()),
        dstHeight = dh(sprite.height.toFloat()),
        tint = tint,
        alpha = 1f
    )
}

private fun DrawScope.drawHorizon(
    gameState: DinoGameState,
    spriteSheet: ImageBitmap,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    tint: Color
) {
    val sprites = listOf(
        OriginalDinoSpriteSheet.horizonA,
        OriginalDinoSpriteSheet.horizonB,
        OriginalDinoSpriteSheet.horizonA
    )
    sprites.forEachIndexed { index, sprite ->
        drawSprite(
            image = spriteSheet,
            sprite = sprite,
            dstTopLeft = Offset(
                x = dx((index * DinoDefaults.logicalWidth) - gameState.groundOffset),
                y = dy(DinoDefaults.horizonLineY)
            ),
            dstWidth = dw(sprite.width.toFloat()),
            dstHeight = dh(sprite.height.toFloat()),
            tint = tint
        )
    }
}

private fun DrawScope.drawObstacle(
    spriteSheet: ImageBitmap,
    obstacle: DinoObstacle,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    tint: Color
) {
    val sprite = when (obstacle.kind) {
        DinoObstacleKind.SMALL_CACTUS -> OriginalDinoSpriteSheet.smallCactus(obstacle.size)
        DinoObstacleKind.LARGE_CACTUS -> OriginalDinoSpriteSheet.largeCactus(obstacle.size)
        DinoObstacleKind.PTERODACTYL -> {
            if (obstacle.animationFrame == 0) {
                OriginalDinoSpriteSheet.pterodactylA
            } else {
                OriginalDinoSpriteSheet.pterodactylB
            }
        }
    }

    drawSprite(
        image = spriteSheet,
        sprite = sprite,
        dstTopLeft = Offset(dx(obstacle.x), dy(obstacle.y)),
        dstWidth = dw(sprite.width.toFloat()),
        dstHeight = dh(sprite.height.toFloat()),
        tint = tint
    )
}

private fun DrawScope.drawRunner(
    spriteSheet: ImageBitmap,
    state: DinoGameState,
    runner: DinoRunnerState,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    tint: Color
) {
    val sprite = when {
        state.isGameOver -> OriginalDinoSpriteSheet.trexCrash
        runner.isJumping -> OriginalDinoSpriteSheet.trexJump
        runner.isDucking -> {
            val duckFrame = ((state.elapsedTimeMs / 90f).toInt()) % 2
            if (duckFrame == 0) {
                OriginalDinoSpriteSheet.trexDuckA
            } else {
                OriginalDinoSpriteSheet.trexDuckB
            }
        }
        state.isStarted -> {
            val runFrame = ((state.elapsedTimeMs / 90f).toInt()) % 2
            if (runFrame == 0) {
                OriginalDinoSpriteSheet.trexRunA
            } else {
                OriginalDinoSpriteSheet.trexRunB
            }
        }
        else -> {
            val blinkFrame = if ((state.elapsedTimeMs % 2500f) > 2225f) 0 else 1
            if (blinkFrame == 0) {
                OriginalDinoSpriteSheet.trexIdleA
            } else {
                OriginalDinoSpriteSheet.trexIdleB
            }
        }
    }

    drawSprite(
        image = spriteSheet,
        sprite = sprite,
        dstTopLeft = Offset(dx(runner.x), dy(runner.y)),
        dstWidth = dw(sprite.width.toFloat()),
        dstHeight = dh(sprite.height.toFloat()),
        tint = tint
    )
}

private fun DrawScope.drawScore(
    spriteSheet: ImageBitmap,
    state: DinoGameState,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    tint: Color
) {
    val showCurrent = state.achievementFlashRemainingMs <= 0f ||
        ((state.achievementFlashRemainingMs / 100f).toInt() % 2 == 0)

    if (state.bestScore > 0) {
        drawScoreString(
            spriteSheet = spriteSheet,
            text = "HI ${paddedScore(state.bestScore)}",
            x = 438f,
            y = 10f,
            dx = dx,
            dy = dy,
            dw = dw,
            dh = dh,
            tint = tint,
            spacing = 2f
        )
    }

    if (showCurrent) {
        drawScoreString(
            spriteSheet = spriteSheet,
            text = paddedScore(state.displayScore),
            x = DinoDefaults.logicalWidth - 56f,
            y = 10f,
            dx = dx,
            dy = dy,
            dw = dw,
            dh = dh,
            tint = tint,
            spacing = 0f
        )
    }
}

private fun DrawScope.drawScoreString(
    spriteSheet: ImageBitmap,
    text: String,
    x: Float,
    y: Float,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    tint: Color,
    spacing: Float
) {
    var cursor = x
    text.forEach { char ->
        if (char == ' ') {
            cursor += 10f + spacing
        } else {
            val digit = OriginalDinoSpriteSheet.digit(char)
            drawSprite(
                image = spriteSheet,
                sprite = digit,
                dstTopLeft = Offset(dx(cursor), dy(y)),
                dstWidth = dw(digit.width.toFloat()),
                dstHeight = dh(digit.height.toFloat()),
                tint = tint
            )
            cursor += digit.width + spacing
        }
    }
}

private fun DrawScope.drawGameOver(
    spriteSheet: ImageBitmap,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    tint: Color
) {
    val gameOver = OriginalDinoSpriteSheet.gameOver
    drawSprite(
        image = spriteSheet,
        sprite = gameOver,
        dstTopLeft = Offset(
            x = dx((DinoDefaults.logicalWidth - gameOver.width) / 2f),
            y = dy(40f)
        ),
        dstWidth = dw(gameOver.width.toFloat()),
        dstHeight = dh(gameOver.height.toFloat()),
        tint = tint
    )

    val restart = OriginalDinoSpriteSheet.restartFrames.last()
    drawSprite(
        image = spriteSheet,
        sprite = restart,
        dstTopLeft = Offset(
            x = dx((DinoDefaults.logicalWidth - restart.width) / 2f),
            y = dy(64f)
        ),
        dstWidth = dw(restart.width.toFloat()),
        dstHeight = dh(restart.height.toFloat()),
        tint = tint
    )
}

private fun Modifier.dinoActionInput(
    enabled: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit
): Modifier {
    return pointerInput(enabled) {
        detectTapGestures(
            onPress = {
                if (enabled) {
                    onPress()
                    tryAwaitRelease()
                    onRelease()
                }
            }
        )
    }
}

private fun Modifier.dinoRestartInput(
    enabled: Boolean,
    onRestart: () -> Unit
): Modifier {
    return pointerInput(enabled) {
        detectTapGestures(
            onPress = {
                if (enabled) {
                    onRestart()
                }
            }
        )
    }
}

private fun paddedScore(score: Int): String {
    return score.coerceAtLeast(0).toString().padStart(5, '0')
}

private data class DinoPalette(
    val sky: Color,
    val foreground: Color,
    val border: Color,
    val surface: Color
)
