package com.retro.arcade.feature.minesweeper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retro.arcade.feature.minesweeper.MinesweeperGameViewModel
import com.retro.arcade.feature.minesweeper.MinesweeperUiState
import com.retro.arcade.feature.minesweeper.model.MinesweeperCell
import com.retro.arcade.feature.minesweeper.model.MinesweeperDifficulty
import com.retro.arcade.feature.minesweeper.model.MinesweeperGameState
import com.retro.arcade.feature.minesweeper.model.MinesweeperMark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinesweeperGameScreen(
    bestTimeSeconds: Int,
    onBack: () -> Unit,
    onPersistBestTime: suspend (Int) -> Unit,
    viewModel: MinesweeperGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var savedSessionId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(bestTimeSeconds) {
        viewModel.initialize(bestTimeSeconds)
    }

    LaunchedEffect(uiState.sessionId) {
        savedSessionId = null
    }

    LaunchedEffect(uiState.sessionId, uiState.gameState?.isWin) {
        val gameState = uiState.gameState ?: return@LaunchedEffect
        if (gameState.isWin && savedSessionId != uiState.sessionId) {
            savedSessionId = uiState.sessionId
            onPersistBestTime(gameState.elapsedSeconds)
        }
    }

    val gameState = uiState.gameState ?: return

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Classic Minesweeper") },
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
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DifficultyRow(
                difficulty = gameState.difficulty,
                onDifficultyChange = viewModel::changeDifficulty
            )
            MinesweeperPanel(
                uiState = uiState,
                gameState = gameState,
                onRestart = viewModel::restart,
                onReveal = viewModel::revealCell,
                onFlag = viewModel::toggleFlag,
                onChord = viewModel::chordReveal,
                onPressChange = viewModel::setBoardPressed
            )
            Text(
                text = "Tap to reveal, long press to flag, double tap an opened number to chord.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DifficultyRow(
    difficulty: MinesweeperDifficulty,
    onDifficultyChange: (MinesweeperDifficulty) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MinesweeperDifficulty.values().forEach { item ->
            FilterChip(
                selected = item == difficulty,
                onClick = { onDifficultyChange(item) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun MinesweeperPanel(
    uiState: MinesweeperUiState,
    gameState: MinesweeperGameState,
    onRestart: () -> Unit,
    onReveal: (Int, Int) -> Unit,
    onFlag: (Int, Int) -> Unit,
    onChord: (Int, Int) -> Unit,
    onPressChange: (Boolean) -> Unit
) {
    Surface(
        shape = RectangleShape,
        color = XpFrame,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, XpDarkBorder)
            .drawXpRaised()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TopInfoBar(
                gameState = gameState,
                bestTimeSeconds = uiState.bestTimeSeconds,
                isPressingBoard = uiState.isPressingBoard,
                onRestart = onRestart
            )
            BoardFrame {
                MinesweeperBoard(
                    gameState = gameState,
                    onReveal = onReveal,
                    onFlag = onFlag,
                    onChord = onChord,
                    onPressChange = onPressChange
                )
            }
        }
    }
}

@Composable
private fun TopInfoBar(
    gameState: MinesweeperGameState,
    bestTimeSeconds: Int,
    isPressingBoard: Boolean,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, XpDarkBorder)
            .drawXpSunken()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DigitalCounter(gameState.remainingMines)
        FaceButton(
            face = when {
                gameState.isWin -> "B)"
                gameState.isGameOver -> "X("
                isPressingBoard -> ":O"
                else -> ":)"
            },
            onClick = onRestart
        )
        DigitalCounter(gameState.elapsedSeconds)
    }
    if (bestTimeSeconds > 0) {
        Text(
            text = "Best Time: ${bestTimeSeconds}s",
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DigitalCounter(value: Int) {
    Surface(
        color = CounterBackground,
        shape = RectangleShape,
        modifier = Modifier
            .size(width = 72.dp, height = 42.dp)
            .border(2.dp, XpDarkBorder)
            .drawXpSunken()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = value.coerceIn(-99, 999).toString().padStart(3, '0'),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                ),
                color = CounterRed
            )
        }
    }
}

@Composable
private fun FaceButton(
    face: String,
    onClick: () -> Unit
) {
    Surface(
        color = XpFrame,
        shape = RectangleShape,
        modifier = Modifier
            .size(42.dp)
            .border(2.dp, XpDarkBorder)
            .drawXpRaised()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = face)
        }
    }
}

@Composable
private fun BoardFrame(content: @Composable () -> Unit) {
    Surface(
        color = XpFrame,
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, XpDarkBorder)
            .drawXpSunken()
            .padding(4.dp)
    ) {
        content()
    }
}

@Composable
private fun MinesweeperBoard(
    gameState: MinesweeperGameState,
    onReveal: (Int, Int) -> Unit,
    onFlag: (Int, Int) -> Unit,
    onChord: (Int, Int) -> Unit,
    onPressChange: (Boolean) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellSize = maxWidth / gameState.cols
        val boardHeight = cellSize * gameState.rows

        Column(
            modifier = Modifier
                .width(maxWidth)
                .height(boardHeight)
        ) {
            gameState.board.forEachIndexed { rowIndex, row ->
                Row {
                    row.forEachIndexed { colIndex, cell ->
                        MinesweeperCellView(
                            cell = cell,
                            isExploded = gameState.explodedCell == rowIndex to colIndex,
                            showMines = gameState.isGameOver,
                            modifier = Modifier.size(cellSize),
                            onReveal = { onReveal(rowIndex, colIndex) },
                            onFlag = { onFlag(rowIndex, colIndex) },
                            onChord = { onChord(rowIndex, colIndex) },
                            onPressChange = onPressChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinesweeperCellView(
    cell: MinesweeperCell,
    isExploded: Boolean,
    showMines: Boolean,
    modifier: Modifier,
    onReveal: () -> Unit,
    onFlag: () -> Unit,
    onChord: () -> Unit,
    onPressChange: (Boolean) -> Unit
) {
    val background = when {
        cell.isRevealed -> XpCellOpen
        isExploded -> XpExploded
        else -> XpFrame
    }

    val revealMine = showMines && cell.hasMine
    val wrongFlag = showMines && cell.mark == MinesweeperMark.FLAG && !cell.hasMine

    Box(
        modifier = modifier
            .background(background)
            .then(
                if (cell.isRevealed) {
                    Modifier.border(1.dp, XpCellBorder)
                } else {
                    Modifier
                        .border(1.dp, XpDarkBorder)
                        .drawXpRaised()
                }
            )
            .pointerInput(cell.isRevealed, showMines) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    },
                    onTap = { onReveal() },
                    onDoubleTap = { onChord() },
                    onLongPress = { onFlag() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.mark == MinesweeperMark.FLAG && !showMines -> {
                Text(
                    text = "F",
                    color = FlagRed,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            wrongFlag -> {
                Text(
                    text = "X",
                    color = WrongFlagRed,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            revealMine -> {
                Text(
                    text = "*",
                    color = MineBlack,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            cell.isRevealed && cell.adjacentMines > 0 -> {
                Text(
                    text = cell.adjacentMines.toString(),
                    color = numberColor(cell.adjacentMines),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }
    }
}

private fun numberColor(number: Int): Color {
    return when (number) {
        1 -> Color(0xFF0000FF)
        2 -> Color(0xFF008000)
        3 -> Color(0xFFFF0000)
        4 -> Color(0xFF000080)
        5 -> Color(0xFF800000)
        6 -> Color(0xFF008080)
        7 -> Color(0xFF000000)
        8 -> Color(0xFF808080)
        else -> Color.Unspecified
    }
}

private fun Modifier.drawXpRaised(): Modifier = drawBehind {
    drawLine(
        color = XpLightBorder,
        start = androidx.compose.ui.geometry.Offset(0f, size.height),
        end = androidx.compose.ui.geometry.Offset(0f, 0f),
        strokeWidth = 2f
    )
    drawLine(
        color = XpLightBorder,
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
        strokeWidth = 2f
    )
    drawLine(
        color = XpShadowBorder,
        start = androidx.compose.ui.geometry.Offset(size.width, 0f),
        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
        strokeWidth = 2f
    )
    drawLine(
        color = XpShadowBorder,
        start = androidx.compose.ui.geometry.Offset(0f, size.height),
        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
        strokeWidth = 2f
    )
}

private fun Modifier.drawXpSunken(): Modifier = drawBehind {
    drawLine(
        color = XpShadowBorder,
        start = androidx.compose.ui.geometry.Offset(0f, size.height),
        end = androidx.compose.ui.geometry.Offset(0f, 0f),
        strokeWidth = 2f
    )
    drawLine(
        color = XpShadowBorder,
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
        strokeWidth = 2f
    )
    drawLine(
        color = XpLightBorder,
        start = androidx.compose.ui.geometry.Offset(size.width, 0f),
        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
        strokeWidth = 2f
    )
    drawLine(
        color = XpLightBorder,
        start = androidx.compose.ui.geometry.Offset(0f, size.height),
        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
        strokeWidth = 2f
    )
}

private val XpFrame = Color(0xFFC0C0C0)
private val XpLightBorder = Color(0xFFFFFFFF)
private val XpShadowBorder = Color(0xFF808080)
private val XpDarkBorder = Color(0xFF404040)
private val XpCellOpen = Color(0xFFC0C0C0)
private val XpCellBorder = Color(0xFFA0A0A0)
private val XpExploded = Color(0xFFFF0000)
private val CounterBackground = Color(0xFF1A1A1A)
private val CounterRed = Color(0xFFFF2A2A)
private val FlagRed = Color(0xFFD62626)
private val WrongFlagRed = Color(0xFF9B1C1C)
private val MineBlack = Color(0xFF111111)
