package com.retro.arcade.feature.tetris.ui

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retro.arcade.feature.tetris.TetrisGameViewModel
import com.retro.arcade.feature.tetris.TetrisUiState
import com.retro.arcade.feature.tetris.model.TetrisDefaults
import com.retro.arcade.feature.tetris.model.TetrisGameState
import com.retro.arcade.feature.tetris.model.TetrisPieceType
import com.retro.arcade.feature.tetris.model.TetrisShapeLibrary
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TetrisGameScreen(
    bestScore: Int,
    onBack: () -> Unit,
    onPersistBestScore: suspend (Int) -> Unit,
    viewModel: TetrisGameViewModel = viewModel()
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
                title = { Text("Classic Tetris") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePause() }) {
                        Icon(
                            imageVector = if (gameState.isPaused) {
                                Icons.Default.PlayArrow
                            } else {
                                Icons.Default.Pause
                            },
                            contentDescription = if (gameState.isPaused) {
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TetrisStatsRow(uiState = uiState, gameState = gameState)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TetrisBoard(
                    modifier = Modifier.weight(1.2f),
                    gameState = gameState,
                    onStart = viewModel::startGame,
                    onResume = viewModel::togglePause,
                    onRestart = viewModel::restart,
                    onBack = onBack
                )
                NextPanel(
                    modifier = Modifier.weight(0.72f),
                    nextPiece = gameState.nextPiece
                )
            }
            TetrisControls(
                onMoveLeft = viewModel::moveLeft,
                onMoveRight = viewModel::moveRight,
                onRotate = viewModel::rotate,
                onSoftDropPress = viewModel::softDropPress,
                onSoftDropRelease = viewModel::softDropRelease
            )
            Text(
                text = "Classic rules only: no hold, no ghost piece, no hard drop.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TetrisStatsRow(
    uiState: TetrisUiState,
    gameState: TetrisGameState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(
            modifier = Modifier.weight(1f),
            title = "Score",
            value = gameState.score.toString()
        )
        StatChip(
            modifier = Modifier.weight(1f),
            title = "Best",
            value = uiState.bestScore.toString()
        )
        StatChip(
            modifier = Modifier.weight(1f),
            title = "Lines",
            value = gameState.linesCleared.toString()
        )
        StatChip(
            modifier = Modifier.weight(1f),
            title = "Level",
            value = (gameState.level + 1).toString()
        )
    }
}

@Composable
private fun StatChip(
    modifier: Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

@Composable
private fun TetrisBoard(
    modifier: Modifier,
    gameState: TetrisGameState,
    onStart: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(
                TetrisDefaults.cols.toFloat() / TetrisDefaults.rows.toFloat()
            )
            .background(BoardBackground, MaterialTheme.shapes.large)
            .border(
                width = 1.dp,
                color = BoardBorder,
                shape = MaterialTheme.shapes.large
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            val cellWidth = size.width / TetrisDefaults.cols
            val cellHeight = size.height / TetrisDefaults.rows

            drawRoundRect(
                color = BoardSurface,
                cornerRadius = CornerRadius(10f, 10f)
            )

            for (row in 0..TetrisDefaults.rows) {
                val y = row * cellHeight
                drawLine(
                    color = GridLine,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            for (col in 0..TetrisDefaults.cols) {
                val x = col * cellWidth
                drawLine(
                    color = GridLine,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            gameState.board.forEachIndexed { row, line ->
                line.forEachIndexed { col, cell ->
                    if (cell != null) {
                        drawBoardCell(
                            row = row,
                            col = col,
                            type = cell,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight
                        )
                    }
                }
            }

            TetrisShapeLibrary.cellsFor(gameState.activePiece).forEach { cell ->
                drawBoardCell(
                    row = cell.row,
                    col = cell.col,
                    type = gameState.activePiece.type,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight
                )
            }

            drawRoundRect(
                color = BoardBorder.copy(alpha = 0.8f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 2f)
            )
        }

        when {
            !gameState.isStarted -> {
                TetrisOverlay(
                    title = "CLASSIC TETRIS",
                    message = "The original essentials only.\nStack, rotate, clear lines, survive.",
                    primaryLabel = "Start",
                    secondaryLabel = "Back",
                    onPrimary = onStart,
                    onSecondary = onBack
                )
            }

            gameState.isGameOver -> {
                TetrisOverlay(
                    title = "GAME OVER",
                    message = "Score ${gameState.score}\nLines ${gameState.linesCleared}\nLevel ${gameState.level + 1}",
                    primaryLabel = "Retry",
                    secondaryLabel = "Back",
                    onPrimary = onRestart,
                    onSecondary = onBack
                )
            }

            gameState.isPaused -> {
                TetrisOverlay(
                    title = "PAUSED",
                    message = "Resume to continue the current stack.",
                    primaryLabel = "Resume",
                    secondaryLabel = "Restart",
                    onPrimary = onResume,
                    onSecondary = onRestart
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBoardCell(
    row: Int,
    col: Int,
    type: TetrisPieceType,
    cellWidth: Float,
    cellHeight: Float
) {
    val fill = tetrisColor(type)
    drawRoundRect(
        color = fill,
        topLeft = Offset(
            x = (col * cellWidth) + 1.5f,
            y = (row * cellHeight) + 1.5f
        ),
        size = Size(
            width = cellWidth - 3f,
            height = cellHeight - 3f
        ),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = fill.copy(alpha = 0.65f),
        topLeft = Offset(
            x = (col * cellWidth) + 3f,
            y = (row * cellHeight) + 3f
        ),
        size = Size(
            width = cellWidth - 9f,
            height = (cellHeight - 9f) / 2f
        ),
        cornerRadius = CornerRadius(3f, 3f)
    )
}

@Composable
private fun NextPanel(
    modifier: Modifier,
    nextPiece: TetrisPieceType
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Next",
                style = MaterialTheme.typography.titleLarge
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(NextBackground, MaterialTheme.shapes.medium)
                    .border(
                        width = 1.dp,
                        color = BoardBorder.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(12.dp)
            ) {
                val cells = TetrisShapeLibrary.cellsFor(nextPiece, 0)
                val minRow = cells.minOf { it.row }
                val maxRow = cells.maxOf { it.row }
                val minCol = cells.minOf { it.col }
                val maxCol = cells.maxOf { it.col }
                val spanRows = maxRow - minRow + 1
                val spanCols = maxCol - minCol + 1

                val cellWidth = size.width / 4f
                val cellHeight = size.height / 4f
                val offsetCol = (4 - spanCols) / 2f
                val offsetRow = (4 - spanRows) / 2f

                cells.forEach { cell ->
                    val col = offsetCol + (cell.col - minCol)
                    val row = offsetRow + (cell.row - minRow)
                    drawRoundRect(
                        color = tetrisColor(nextPiece),
                        topLeft = Offset(
                            x = col * cellWidth,
                            y = row * cellHeight
                        ),
                        size = Size(
                            width = cellWidth - 4f,
                            height = cellHeight - 4f
                        ),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
            Text(
                text = "No hold\nNo ghost\nNo hard drop",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TetrisControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDropPress: () -> Unit,
    onSoftDropRelease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ControlButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.ArrowBack,
            label = "Left",
            onClick = onMoveLeft
        )
        ControlButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.RotateRight,
            label = "Rotate",
            onClick = onRotate
        )
        ControlButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.ArrowForward,
            label = "Right",
            onClick = onMoveRight
        )
        DropButton(
            modifier = Modifier.weight(1f),
            onPress = onSoftDropPress,
            onRelease = onSoftDropRelease
        )
    }
}

@Composable
private fun ControlButton(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledIconButton(
                onClick = onClick
            ) {
                Icon(imageVector = icon, contentDescription = label)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DropButton(
    modifier: Modifier,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    Surface(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Soft Drop",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Text(
                text = "Down",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TetrisOverlay(
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
            .padding(horizontal = 20.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
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
                    fontFamily = FontFamily.Monospace
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

private fun tetrisColor(type: TetrisPieceType): Color {
    return when (type) {
        TetrisPieceType.I -> Color(0xFF67B8C9)
        TetrisPieceType.O -> Color(0xFFD0B35A)
        TetrisPieceType.T -> Color(0xFF9376C6)
        TetrisPieceType.S -> Color(0xFF68A874)
        TetrisPieceType.Z -> Color(0xFFC56C6C)
        TetrisPieceType.J -> Color(0xFF6180C2)
        TetrisPieceType.L -> Color(0xFFC99057)
    }
}

private val BoardBackground = Color(0xFF101827)
private val BoardSurface = Color(0xFF161F31)
private val BoardBorder = Color(0xFF4B5F88)
private val GridLine = Color(0xFF24304A)
private val NextBackground = Color(0xFF11192A)
