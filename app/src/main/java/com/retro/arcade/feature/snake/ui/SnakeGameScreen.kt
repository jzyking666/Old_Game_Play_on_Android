package com.retro.arcade.feature.snake.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retro.arcade.feature.snake.SnakeGameUiState
import com.retro.arcade.feature.snake.SnakeGameViewModel
import com.retro.arcade.feature.snake.model.Cell
import com.retro.arcade.feature.snake.model.Direction
import com.retro.arcade.feature.snake.model.SnakeControlMode
import com.retro.arcade.feature.snake.model.SnakeDifficulty
import com.retro.arcade.feature.snake.model.SnakePalette
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeGameScreen(
    difficulty: SnakeDifficulty,
    controlMode: SnakeControlMode,
    palette: SnakePalette,
    bestScore: Int,
    onBack: () -> Unit,
    onPersistBestScore: suspend (Int) -> Unit,
    viewModel: SnakeGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var savedSessionId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(difficulty, controlMode, palette) {
        viewModel.initialize(
            difficulty = difficulty,
            controlMode = controlMode,
            palette = palette,
            bestScore = bestScore
        )
    }

    LaunchedEffect(uiState.sessionId) {
        savedSessionId = null
    }

    LaunchedEffect(uiState.sessionId, uiState.gameState.isGameOver) {
        if (uiState.gameState.isGameOver && savedSessionId != uiState.sessionId) {
            savedSessionId = uiState.sessionId
            onPersistBestScore(uiState.gameState.score)
        }
    }

    val paletteColors = remember(uiState.palette) {
        uiState.palette.toPaletteColors()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Classic Snake") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = !uiState.gameState.isGameOver,
                        onClick = { viewModel.togglePause() }
                    ) {
                        Icon(
                            imageVector = if (uiState.gameState.isPaused) {
                                Icons.Default.PlayArrow
                            } else {
                                Icons.Default.Pause
                            },
                            contentDescription = if (uiState.gameState.isPaused) {
                                "Resume"
                            } else {
                                "Pause"
                            }
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScoreRow(uiState = uiState)
            SnakeBoard(
                uiState = uiState,
                paletteColors = paletteColors,
                onDirectionInput = viewModel::onDirectionInput,
                onResume = { viewModel.togglePause() },
                onRestart = { viewModel.restart() },
                onBack = onBack
            )
            ControlSection(
                uiState = uiState,
                paletteColors = paletteColors,
                onDirectionInput = viewModel::onDirectionInput
            )
            Text(
                text = when (uiState.controlMode) {
                    SnakeControlMode.DPAD -> "The bottom D-pad keeps the old keypad-phone feeling."
                    SnakeControlMode.SWIPE -> "Swipe on the board to change direction."
                },
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
private fun ScoreRow(uiState: SnakeGameUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScoreChip(
            modifier = Modifier.weight(1f),
            title = "Score",
            value = uiState.gameState.score.toString()
        )
        ScoreChip(
            modifier = Modifier.weight(1f),
            title = "Best",
            value = uiState.bestScore.toString()
        )
        ScoreChip(
            modifier = Modifier.weight(1f),
            title = "Speed",
            value = uiState.config.difficulty.label
        )
    }
}

@Composable
private fun ScoreChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun SnakeBoard(
    uiState: SnakeGameUiState,
    paletteColors: SnakePaletteColors,
    onDirectionInput: (Direction) -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    val boardModifier = if (uiState.controlMode == SnakeControlMode.SWIPE) {
        Modifier.pointerInput(uiState.sessionId, uiState.controlMode) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                val direction = if (abs(dragAmount.x) > abs(dragAmount.y)) {
                    if (dragAmount.x > 0f) Direction.RIGHT else Direction.LEFT
                } else {
                    if (dragAmount.y > 0f) Direction.DOWN else Direction.UP
                }
                onDirectionInput(direction)
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(
                uiState.config.board.cols.toFloat() / uiState.config.board.rows.toFloat()
            )
            .clip(RoundedCornerShape(28.dp))
            .background(paletteColors.frame)
            .border(
                width = 1.dp,
                color = paletteColors.grid.copy(alpha = 0.6f),
                shape = RoundedCornerShape(28.dp)
            )
            .then(boardModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            val rows = uiState.config.board.rows
            val cols = uiState.config.board.cols
            val cellWidth = size.width / cols
            val cellHeight = size.height / rows

            drawRoundRect(
                color = paletteColors.boardBackground,
                cornerRadius = CornerRadius(24f, 24f)
            )

            for (row in 0..rows) {
                val y = row * cellHeight
                drawLine(
                    color = paletteColors.grid.copy(alpha = 0.45f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            for (col in 0..cols) {
                val x = col * cellWidth
                drawLine(
                    color = paletteColors.grid.copy(alpha = 0.45f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            uiState.gameState.snake.forEachIndexed { index, cell ->
                drawCell(
                    cell = cell,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    color = if (index == 0) {
                        paletteColors.snakeHead
                    } else {
                        paletteColors.snakeBody
                    }
                )
            }

            drawFood(
                cell = uiState.gameState.food,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                color = paletteColors.food
            )

            drawRoundRect(
                color = paletteColors.grid.copy(alpha = 0.25f),
                style = Stroke(width = 2f),
                cornerRadius = CornerRadius(24f, 24f)
            )
        }

        when {
            uiState.sessionId == 0L -> Unit
            uiState.gameState.isGameOver -> {
                OverlayCard(
                    title = if (uiState.gameState.isVictory) "Board Cleared" else "Game Over",
                    message = "Score ${uiState.gameState.score}, best ${uiState.bestScore}",
                    primaryLabel = "Retry",
                    secondaryLabel = "Back",
                    onPrimary = onRestart,
                    onSecondary = onBack
                )
            }
            uiState.gameState.isPaused -> {
                OverlayCard(
                    title = "Paused",
                    message = "Resume to continue the current run.",
                    primaryLabel = "Resume",
                    secondaryLabel = "Restart",
                    onPrimary = onResume,
                    onSecondary = onRestart
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCell(
    cell: Cell,
    cellWidth: Float,
    cellHeight: Float,
    color: Color
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(
            x = cell.col * cellWidth + cellWidth * 0.08f,
            y = cell.row * cellHeight + cellHeight * 0.08f
        ),
        size = Size(
            width = cellWidth * 0.84f,
            height = cellHeight * 0.84f
        ),
        cornerRadius = CornerRadius(cellWidth * 0.2f, cellHeight * 0.2f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFood(
    cell: Cell,
    cellWidth: Float,
    cellHeight: Float,
    color: Color
) {
    val center = Offset(
        x = cell.col * cellWidth + cellWidth / 2,
        y = cell.row * cellHeight + cellHeight / 2
    )
    drawCircle(
        color = color,
        radius = minOf(cellWidth, cellHeight) * 0.26f,
        center = center
    )
}

@Composable
private fun OverlayCard(
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
            .padding(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
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
private fun ControlSection(
    uiState: SnakeGameUiState,
    paletteColors: SnakePaletteColors,
    onDirectionInput: (Direction) -> Unit
) {
    if (uiState.controlMode == SnakeControlMode.SWIPE) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "Swipe mode is on. Drag across the board in any of the four directions.",
                modifier = Modifier.padding(18.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DirectionButton(
            direction = Direction.UP,
            icon = Icons.Default.KeyboardArrowUp,
            paletteColors = paletteColors,
            onDirectionInput = onDirectionInput
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DirectionButton(
                direction = Direction.LEFT,
                icon = Icons.Default.KeyboardArrowLeft,
                paletteColors = paletteColors,
                onDirectionInput = onDirectionInput
            )
            DirectionButton(
                direction = Direction.DOWN,
                icon = Icons.Default.KeyboardArrowDown,
                paletteColors = paletteColors,
                onDirectionInput = onDirectionInput
            )
            DirectionButton(
                direction = Direction.RIGHT,
                icon = Icons.Default.KeyboardArrowRight,
                paletteColors = paletteColors,
                onDirectionInput = onDirectionInput
            )
        }
    }
}

@Composable
private fun DirectionButton(
    direction: Direction,
    icon: ImageVector,
    paletteColors: SnakePaletteColors,
    onDirectionInput: (Direction) -> Unit
) {
    FilledIconButton(
        modifier = Modifier.size(64.dp),
        onClick = { onDirectionInput(direction) }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = direction.name,
            tint = paletteColors.controlIcon
        )
    }
}

private data class SnakePaletteColors(
    val boardBackground: Color,
    val frame: Color,
    val grid: Color,
    val snakeHead: Color,
    val snakeBody: Color,
    val food: Color,
    val controlIcon: Color
)

private fun SnakePalette.toPaletteColors(): SnakePaletteColors {
    return when (this) {
        SnakePalette.CLASSIC_GREEN -> SnakePaletteColors(
            boardBackground = Color(0xFF102618),
            frame = Color(0xFF06110A),
            grid = Color(0xFF3A6F45),
            snakeHead = Color(0xFFB6FF78),
            snakeBody = Color(0xFF7FFF90),
            food = Color(0xFFFFD95A),
            controlIcon = Color(0xFF08110A)
        )
        SnakePalette.NIGHT_GLOW -> SnakePaletteColors(
            boardBackground = Color(0xFF04080F),
            frame = Color(0xFF02050A),
            grid = Color(0xFF274861),
            snakeHead = Color(0xFF74F4FF),
            snakeBody = Color(0xFF2FD8E8),
            food = Color(0xFFFF8E5E),
            controlIcon = Color(0xFF08110A)
        )
    }
}
