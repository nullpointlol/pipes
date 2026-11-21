package com.pipes.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pipes.game.core.audio.SoundController
import com.pipes.game.core.haptics.HapticController
import com.pipes.game.core.puzzle.FlowEvaluator
import com.pipes.game.core.puzzle.FlowStatus
import com.pipes.game.core.puzzle.GeneratedPuzzle
import com.pipes.game.core.puzzle.Grid
import com.pipes.game.core.puzzle.MoveRecord
import com.pipes.game.core.puzzle.PuzzleGenerator
import com.pipes.game.core.puzzle.Tile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Stack

enum class CurrentScreen {
    HOME,
    GAME
}

data class GameUiState(
    val currentScreen: CurrentScreen = CurrentScreen.HOME,
    val showSettingsDialog: Boolean = false,
    val grid: Grid,
    val board: Array<IntArray>,
    val flowStatus: FlowStatus,
    val moves: Int = 0,
    val optimalMoves: Int = 0,
    val hintsLeft: Int = 3,
    val stars: Int = 3,
    val isWon: Boolean = false,
    val isTorus: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val soundController = SoundController(viewModelScope)
    val hapticController = HapticController(application.applicationContext)

    private val undoStack = Stack<MoveRecord>()
    private var currentPuzzle: GeneratedPuzzle? = null

    private val _uiState: MutableStateFlow<GameUiState>
    val uiState: StateFlow<GameUiState>

    init {
        val initialGrid = Grid(5, 5, isTorus = false)
        val initialBoard = Array(5) { IntArray(5) { 0 } }
        val initialFlow = FlowEvaluator.evaluate(initialGrid, initialBoard)

        _uiState = MutableStateFlow(
            GameUiState(
                currentScreen = CurrentScreen.HOME,
                grid = initialGrid,
                board = initialBoard,
                flowStatus = initialFlow
            )
        )
        uiState = _uiState.asStateFlow()
    }

    fun selectDensityAndPlay(cols: Int, rows: Int? = null) {
        val torus = _uiState.value.isTorus
        val targetRows = rows ?: ((cols * 19.5f) / 9f).toInt().coerceAtLeast(cols)
        startNewLevel(cols, targetRows, torus)
        _uiState.update { it.copy(currentScreen = CurrentScreen.GAME) }
    }

    fun openHome() {
        _uiState.update { it.copy(currentScreen = CurrentScreen.HOME, showSettingsDialog = false) }
    }

    fun openSettings() {
        _uiState.update { it.copy(showSettingsDialog = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(showSettingsDialog = false) }
    }

    fun toggleTorus(enabled: Boolean) {
        _uiState.update { it.copy(isTorus = enabled) }
        if (_uiState.value.currentScreen == CurrentScreen.GAME) {
            startNewLevel(_uiState.value.grid.width, _uiState.value.grid.height, enabled)
        }
    }

    fun toggleSound(enabled: Boolean) {
        soundController.isEnabled = enabled
    }

    fun startNewLevel(
        cols: Int = _uiState.value.grid.width,
        rows: Int = _uiState.value.grid.height,
        isTorus: Boolean = _uiState.value.isTorus
    ) {
        val generator = PuzzleGenerator(cols, rows, isTorus)
        val puzzle = generator.generate()
        currentPuzzle = puzzle
        undoStack.clear()

        val initialFlow = FlowEvaluator.evaluate(puzzle.grid, puzzle.shuffledBoard)

        _uiState.update { current ->
            current.copy(
                grid = puzzle.grid,
                board = puzzle.shuffledBoard.map { it.clone() }.toTypedArray(),
                flowStatus = initialFlow,
                moves = 0,
                optimalMoves = puzzle.optimalMoves,
                stars = 3,
                isWon = false,
                isTorus = isTorus,
                showSettingsDialog = false
            )
        }
    }

    fun onCellClicked(x: Int, y: Int) {
        val state = _uiState.value
        if (state.isWon) return

        val prevMask = state.board[y][x]
        val newMask = Tile.rotateClockwise(prevMask)

        undoStack.push(MoveRecord(x, y, prevMask, newMask))

        val newBoard = state.board.map { it.clone() }.toTypedArray()
        newBoard[y][x] = newMask

        val flow = FlowEvaluator.evaluate(state.grid, newBoard)
        val newMoves = state.moves + 1

        var stars = 3
        if (newMoves > state.optimalMoves + 2) stars = 2
        if (newMoves > state.optimalMoves + 7) stars = 1

        soundController.playRotateClick()
        hapticController.tickRotate()

        if (flow.energyPercent > state.flowStatus.energyPercent) {
            soundController.playEnergyStep(flow.energyPercent)
            hapticController.clickConnect()
        }

        if (flow.isWon) {
            soundController.playVictory()
            hapticController.victoryHaptic()
        }

        _uiState.update {
            it.copy(
                board = newBoard,
                flowStatus = flow,
                moves = newMoves,
                stars = stars,
                isWon = flow.isWon
            )
        }
    }

    fun undo() {
        val state = _uiState.value
        if (undoStack.isEmpty() || state.isWon) return

        val last = undoStack.pop()
        val newBoard = state.board.map { it.clone() }.toTypedArray()
        newBoard[last.y][last.x] = last.previousMask

        val flow = FlowEvaluator.evaluate(state.grid, newBoard)
        soundController.playRotateClick()
        hapticController.tickRotate()

        _uiState.update {
            it.copy(
                board = newBoard,
                flowStatus = flow,
                moves = (it.moves - 1).coerceAtLeast(0)
            )
        }
    }

    fun useHint() {
        val state = _uiState.value
        val puzzle = currentPuzzle ?: return
        if (state.isWon || state.hintsLeft <= 0) return

        for (y in 0 until state.grid.height) {
            for (x in 0 until state.grid.width) {
                val current = state.board[y][x]
                val sol = puzzle.solution[y][x]
                if (current != sol) {
                    val newBoard = state.board.map { it.clone() }.toTypedArray()
                    newBoard[y][x] = sol
                    val flow = FlowEvaluator.evaluate(state.grid, newBoard)

                    soundController.playRotateClick()
                    soundController.playEnergyStep(flow.energyPercent)
                    hapticController.clickConnect()

                    if (flow.isWon) {
                        soundController.playVictory()
                        hapticController.victoryHaptic()
                    }

                    _uiState.update {
                        it.copy(
                            board = newBoard,
                            flowStatus = flow,
                            hintsLeft = it.hintsLeft - 1,
                            moves = it.moves + 1,
                            isWon = flow.isWon
                        )
                    }
                    return
                }
            }
        }
    }
}
