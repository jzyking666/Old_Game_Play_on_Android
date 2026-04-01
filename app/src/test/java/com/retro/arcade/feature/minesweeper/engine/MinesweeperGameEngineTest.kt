package com.retro.arcade.feature.minesweeper.engine

import com.retro.arcade.feature.minesweeper.model.MinesweeperCell
import com.retro.arcade.feature.minesweeper.model.MinesweeperDifficulty
import com.retro.arcade.feature.minesweeper.model.MinesweeperGameState
import com.retro.arcade.feature.minesweeper.model.MinesweeperMark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MinesweeperGameEngineTest {
    private val engine = MinesweeperGameEngine(Random(0))

    @Test
    fun first_reveal_is_always_safe() {
        val state = engine.newGame(MinesweeperDifficulty.BEGINNER)

        val next = engine.revealCell(state, 0, 0)

        assertTrue(next.isStarted)
        assertFalse(next.board[0][0].hasMine)
        assertTrue(next.board[0][0].isRevealed)
    }

    @Test
    fun toggle_flag_marks_and_unmarks_hidden_cell() {
        val state = engine.newGame(MinesweeperDifficulty.BEGINNER)

        val flagged = engine.toggleFlag(state, 1, 1)
        val unflagged = engine.toggleFlag(flagged, 1, 1)

        assertEquals(MinesweeperMark.FLAG, flagged.board[1][1].mark)
        assertEquals(MinesweeperMark.NONE, unflagged.board[1][1].mark)
    }

    @Test
    fun zero_region_reveals_neighbors() {
        val board = listOf(
            listOf(
                MinesweeperCell(hasMine = false, adjacentMines = 0),
                MinesweeperCell(hasMine = false, adjacentMines = 0),
                MinesweeperCell(hasMine = false, adjacentMines = 1)
            ),
            listOf(
                MinesweeperCell(hasMine = false, adjacentMines = 0),
                MinesweeperCell(hasMine = false, adjacentMines = 1),
                MinesweeperCell(hasMine = true, adjacentMines = 0)
            ),
            listOf(
                MinesweeperCell(hasMine = false, adjacentMines = 0),
                MinesweeperCell(hasMine = false, adjacentMines = 1),
                MinesweeperCell(hasMine = false, adjacentMines = 1)
            )
        )
        val state = MinesweeperGameState(
            difficulty = MinesweeperDifficulty.BEGINNER,
            board = List(9) { row ->
                List(9) { col ->
                    if (row < 3 && col < 3) board[row][col] else MinesweeperCell()
                }
            }
        )

        val next = engine.revealCell(state.copy(isStarted = true), 0, 0)

        assertTrue(next.board[0][0].isRevealed)
        assertTrue(next.board[0][1].isRevealed)
        assertTrue(next.board[1][0].isRevealed)
    }

    @Test
    fun revealing_last_safe_cell_triggers_win() {
        val board = List(9) { row ->
            List(9) { col ->
                when {
                    row == 0 && col == 0 -> MinesweeperCell(hasMine = true)
                    row == 0 && col == 1 -> MinesweeperCell(
                        hasMine = false,
                        adjacentMines = 1,
                        isRevealed = false
                    )
                    else -> MinesweeperCell(
                        hasMine = false,
                        adjacentMines = 0,
                        isRevealed = true
                    )
                }
            }
        }
        val state = MinesweeperGameState(
            difficulty = MinesweeperDifficulty.BEGINNER,
            board = board,
            isStarted = true
        )

        val next = engine.revealCell(state, 0, 1)

        assertTrue(next.isWin)
        assertTrue(next.isGameOver)
    }
}
