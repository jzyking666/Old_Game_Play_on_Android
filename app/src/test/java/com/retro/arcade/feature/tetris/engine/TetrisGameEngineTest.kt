package com.retro.arcade.feature.tetris.engine

import com.retro.arcade.feature.tetris.model.TetrisActivePiece
import com.retro.arcade.feature.tetris.model.TetrisDefaults
import com.retro.arcade.feature.tetris.model.TetrisGameState
import com.retro.arcade.feature.tetris.model.TetrisPieceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TetrisGameEngineTest {
    private val engine = TetrisGameEngine(Random(0))

    @Test
    fun new_game_has_active_and_next_piece() {
        val state = engine.newGame()

        assertEquals(0, state.score)
        assertEquals(0, state.linesCleared)
        assertTrue(state.activePiece.type in TetrisPieceType.values())
        assertTrue(state.nextPiece in TetrisPieceType.values())
    }

    @Test
    fun start_marks_game_as_started() {
        val state = engine.newGame()

        val next = engine.start(state)

        assertTrue(next.isStarted)
    }

    @Test
    fun clearing_one_line_awards_classic_score() {
        val filledRow = List(TetrisDefaults.cols) { index ->
            if (index == 4) {
                null
            } else {
                TetrisPieceType.O
            }
        }
        val board = TetrisDefaults.emptyBoard().toMutableList()
        board[TetrisDefaults.rows - 1] = filledRow
        val state = TetrisGameState(
            board = board,
            activePiece = TetrisActivePiece(
                type = TetrisPieceType.I,
                rotation = 1,
                row = TetrisDefaults.rows - 4,
                col = 2
            ),
            nextPiece = TetrisPieceType.O,
            isStarted = true
        )

        val next = engine.softDrop(
            engine.softDrop(
                engine.softDrop(state)
            )
        )

        assertEquals(1, next.linesCleared)
        assertEquals(40, next.score)
    }

    @Test
    fun rotation_blocked_by_wall_without_kick() {
        val state = TetrisGameState(
            board = TetrisDefaults.emptyBoard(),
            activePiece = TetrisActivePiece(
                type = TetrisPieceType.I,
                rotation = 0,
                row = 0,
                col = 7
            ),
            nextPiece = TetrisPieceType.O,
            isStarted = true
        )

        val next = engine.rotate(state)

        assertEquals(state.activePiece.rotation, next.activePiece.rotation)
        assertEquals(state.activePiece.col, next.activePiece.col)
    }

    @Test
    fun top_out_marks_game_over() {
        val board = List(TetrisDefaults.rows) { row ->
            List(TetrisDefaults.cols) { col ->
                if (row < 2 && col in 3..6) TetrisPieceType.Z else null
            }
        }
        val state = TetrisGameState(
            board = board,
            activePiece = TetrisActivePiece(
                type = TetrisPieceType.I,
                rotation = 1,
                row = TetrisDefaults.rows - 4,
                col = 2
            ),
            nextPiece = TetrisPieceType.T,
            isStarted = true
        )

        val dropped = generateSequence(state) { current ->
            if (current.isGameOver) null else engine.softDrop(current)
        }.last()

        assertTrue(dropped.isGameOver)
    }
}
