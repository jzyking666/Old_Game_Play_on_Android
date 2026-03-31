package com.retro.arcade.feature.downshaft.ui

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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retro.arcade.feature.downshaft.DownshaftGameViewModel
import com.retro.arcade.feature.downshaft.DownshaftUiState
import com.retro.arcade.feature.downshaft.model.DownshaftBall
import com.retro.arcade.feature.downshaft.model.DownshaftDefaults
import com.retro.arcade.feature.downshaft.model.DownshaftDirection
import com.retro.arcade.feature.downshaft.model.DownshaftGameState
import com.retro.arcade.feature.downshaft.model.DownshaftPlatform
import com.retro.arcade.feature.downshaft.model.DownshaftPlatformKind
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownshaftGameScreen(
    bestScore: Int,
    onBack: () -> Unit,
    onPersistBestScore: suspend (Int) -> Unit,
    viewModel: DownshaftGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var savedSessionId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(bestScore) {
        viewModel.initialize(bestScore)
    }

    LaunchedEffect(uiState.sessionId) {
        savedSessionId = null
    }

    LaunchedEffect(uiState.sessionId, uiState.gameState?.isGameOver) {
        val gameState = uiState.gameState ?: return@LaunchedEffect
        if (gameState.isGameOver && savedSessionId != uiState.sessionId) {
            savedSessionId = uiState.sessionId
            onPersistBestScore(gameState.score)
        }
    }

    val gameState = uiState.gameState ?: return

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Ball Downshaft") },
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
            DownshaftBoard(
                uiState = uiState,
                gameState = gameState,
                onStart = viewModel::startGame,
                onRestart = viewModel::restart,
                onBack = onBack
            )
            DownshaftControlRow(
                enabled = !gameState.isGameOver,
                onLeftPress = { viewModel.onDirectionPressed(DownshaftDirection.LEFT) },
                onLeftRelease = { viewModel.onDirectionReleased(DownshaftDirection.LEFT) },
                onRightPress = { viewModel.onDirectionPressed(DownshaftDirection.RIGHT) },
                onRightRelease = { viewModel.onDirectionReleased(DownshaftDirection.RIGHT) }
            )
            Text(
                text = "Move left and right, stay inside the frame, avoid red spike platforms, and recover on safe platforms after taking damage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DownshaftBoard(
    uiState: DownshaftUiState,
    gameState: DownshaftGameState,
    onStart: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(
                DownshaftDefaults.logicalWidth / DownshaftDefaults.logicalHeight
            )
            .background(DownshaftBackground, RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = DownshaftBorder,
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            val scaleX = size.width / DownshaftDefaults.logicalWidth
            val scaleY = size.height / DownshaftDefaults.logicalHeight
            val unit = min(scaleX, scaleY)

            fun dx(value: Float) = value * scaleX
            fun dy(value: Float) = value * scaleY
            fun dw(value: Float) = value * scaleX
            fun dh(value: Float) = value * scaleY

            drawScanlineBackdrop(unit)
            drawFrameLines(unit)
            drawPlatforms(gameState.platforms, ::dx, ::dy, ::dw, ::dh, unit)
            drawBall(gameState.ball, ::dx, ::dy, ::dw, unit)
            drawHud(
                gameState = gameState,
                bestScore = uiState.bestScore,
                dx = ::dx,
                dy = ::dy,
                unit = unit
            )
        }

        when {
            !gameState.isStarted -> {
                DownshaftOverlay(
                    title = "BALL DOWNSHAFT",
                    message = "Old-school descent mode.\nTake spike damage and respawn on a safe platform.\nHearts get rarer the longer you survive.",
                    primaryLabel = "Start",
                    secondaryLabel = "Back",
                    onPrimary = onStart,
                    onSecondary = onBack
                )
            }

            gameState.isGameOver -> {
                DownshaftOverlay(
                    title = "GAME OVER",
                    message = "Depth ${gameState.score}\nBest ${uiState.bestScore.coerceAtLeast(gameState.score)}",
                    primaryLabel = "Retry",
                    secondaryLabel = "Close",
                    onPrimary = onRestart,
                    onSecondary = onBack
                )
            }
        }
    }
}

private fun DrawScope.drawScanlineBackdrop(unit: Float) {
    drawRoundRect(
        color = DownshaftBackground,
        cornerRadius = CornerRadius(10f * unit, 10f * unit)
    )
    val rowHeight = 7f * unit
    var y = 0f
    while (y < size.height) {
        drawRect(
            color = Scanline.copy(alpha = 0.18f),
            topLeft = Offset(0f, y),
            size = Size(size.width, rowHeight / 2f)
        )
        y += rowHeight
    }
}

private fun DrawScope.drawFrameLines(unit: Float) {
    drawRoundRect(
        color = DownshaftInnerFrame,
        cornerRadius = CornerRadius(10f * unit, 10f * unit),
        style = Stroke(width = 2f * unit)
    )
}

private fun DrawScope.drawPlatforms(
    platforms: List<DownshaftPlatform>,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    unit: Float
) {
    platforms.forEach { platform ->
        val topLeft = Offset(dx(platform.x), dy(platform.y))
        val size = Size(dw(platform.width), dh(platform.height))
        val fill = if (platform.kind == DownshaftPlatformKind.SPIKE) {
            SpikePlatformFill
        } else {
            PlatformFill
        }
        val border = if (platform.kind == DownshaftPlatformKind.SPIKE) {
            SpikePlatformBorder
        } else {
            PlatformBorder
        }

        drawRoundRect(
            color = fill,
            topLeft = topLeft,
            size = size,
            cornerRadius = CornerRadius(3f * unit, 3f * unit)
        )
        drawRoundRect(
            color = border,
            topLeft = topLeft,
            size = size,
            cornerRadius = CornerRadius(3f * unit, 3f * unit),
            style = Stroke(width = 1.3f * unit)
        )

        if (platform.kind == DownshaftPlatformKind.SPIKE) {
            drawSpikes(platform, dx, dy, dw, dh, unit)
        }

        if (platform.hasHeart && !platform.heartCollected) {
            drawHeart(
                centerX = dx(platform.x + (platform.width / 2f)),
                centerY = dy(platform.y - 10f),
                unit = unit,
                fill = HeartFill
            )
        }
    }
}

private fun DrawScope.drawSpikes(
    platform: DownshaftPlatform,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    dh: (Float) -> Float,
    unit: Float
) {
    val spikeCount = (platform.width / 14f).toInt().coerceAtLeast(3)
    val spikeWidth = platform.width / spikeCount
    repeat(spikeCount) { index ->
        val left = platform.x + (index * spikeWidth)
        val right = left + spikeWidth
        val path = Path().apply {
            moveTo(dx(left), dy(platform.y))
            lineTo(dx(left + spikeWidth / 2f), dy(platform.y - 7f))
            lineTo(dx(right), dy(platform.y))
            close()
        }
        drawPath(path = path, color = SpikeTip)
    }
}

private fun DrawScope.drawHeart(
    centerX: Float,
    centerY: Float,
    unit: Float,
    fill: Color
) {
    drawCircle(
        color = fill,
        radius = 4.2f * unit,
        center = Offset(centerX - 3.4f * unit, centerY - 1.8f * unit)
    )
    drawCircle(
        color = fill,
        radius = 4.2f * unit,
        center = Offset(centerX + 3.4f * unit, centerY - 1.8f * unit)
    )
    val path = Path().apply {
        moveTo(centerX - 8f * unit, centerY - 0.5f * unit)
        lineTo(centerX, centerY + 9f * unit)
        lineTo(centerX + 8f * unit, centerY - 0.5f * unit)
        close()
    }
    drawPath(path = path, color = fill)
}

private fun DrawScope.drawBall(
    ball: DownshaftBall,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    dw: (Float) -> Float,
    unit: Float
) {
    val flash = ball.invulnerabilityMs > 0f &&
        ((ball.invulnerabilityMs / 90f).toInt() % 2 == 0)
    if (flash) return

    drawCircle(
        color = BallFill,
        radius = dw(ball.radius),
        center = Offset(dx(ball.x), dy(ball.y))
    )
    drawCircle(
        color = BallBorder,
        radius = dw(ball.radius),
        center = Offset(dx(ball.x), dy(ball.y)),
        style = Stroke(width = 2f * unit)
    )
    drawCircle(
        color = BallHighlight,
        radius = 2.3f * unit,
        center = Offset(
            x = dx(ball.x - 2.2f),
            y = dy(ball.y - 2.5f)
        )
    )
}

private fun DrawScope.drawHud(
    gameState: DownshaftGameState,
    bestScore: Int,
    dx: (Float) -> Float,
    dy: (Float) -> Float,
    unit: Float
) {
    repeat(gameState.maxLives) { index ->
        val isFilled = index < gameState.lives
        drawHeart(
            centerX = dx(18f + (index * 18f)),
            centerY = dy(18f),
            unit = unit,
            fill = if (isFilled) HeartFill else HeartEmpty
        )
    }

    drawTextBlock(
        text = "BEST ${bestScore.toString().padStart(4, '0')}",
        x = dx(110f),
        y = dy(8f),
        color = HudText
    )
    drawTextBlock(
        text = "DEPTH ${gameState.score.toString().padStart(4, '0')}",
        x = dx(100f),
        y = dy(24f),
        color = HudAccent
    )
}

private fun DrawScope.drawTextBlock(
    text: String,
    x: Float,
    y: Float,
    color: Color
) {
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 18f
            typeface = android.graphics.Typeface.MONOSPACE
            this.color = color.toArgb()
            isFakeBoldText = true
        }
        drawText(text, x, y + 14f, paint)
    }
}

@Composable
private fun DownshaftOverlay(
    title: String,
    message: String,
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPrimary) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Text(primaryLabel)
                }
                Button(onClick = onSecondary) {
                    Text(secondaryLabel)
                }
            }
        }
    }
}

@Composable
private fun DownshaftControlRow(
    enabled: Boolean,
    onLeftPress: () -> Unit,
    onLeftRelease: () -> Unit,
    onRightPress: () -> Unit,
    onRightRelease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ControlPad(
            modifier = Modifier.weight(1f),
            title = "LEFT",
            subtitle = "Hold",
            icon = Icons.Default.ArrowBackIosNew,
            enabled = enabled,
            onPress = onLeftPress,
            onRelease = onLeftRelease
        )
        ControlPad(
            modifier = Modifier.weight(1f),
            title = "RIGHT",
            subtitle = "Hold",
            icon = Icons.Default.ArrowForwardIos,
            enabled = enabled,
            onPress = onRightPress,
            onRelease = onRightRelease
        )
    }
}

@Composable
private fun ControlPad(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(108.dp)
            .downshaftInput(
                enabled = enabled,
                onPress = onPress,
                onRelease = onRelease
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
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
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Modifier.downshaftInput(
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

private val DownshaftBackground = Color(0xFF101C1B)
private val DownshaftBorder = Color(0xFF305048)
private val DownshaftInnerFrame = Color(0xFF497466)
private val Scanline = Color(0xFF1D2E2A)
private val PlatformFill = Color(0xFFD7C688)
private val PlatformBorder = Color(0xFF77663B)
private val SpikePlatformFill = Color(0xFF874A3F)
private val SpikePlatformBorder = Color(0xFFD48A6D)
private val SpikeTip = Color(0xFFF0C2AB)
private val HeartFill = Color(0xFFE85E74)
private val HeartEmpty = Color(0xFF4E3A40)
private val BallFill = Color(0xFFF5E8C2)
private val BallBorder = Color(0xFF43351F)
private val BallHighlight = Color(0xFFFFFFFF)
private val HudText = Color(0xFFB9D3CC)
private val HudAccent = Color(0xFFF9D97B)
