package com.retro.arcade.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.retro.arcade.core.datastore.AppPreferencesRepository
import com.retro.arcade.core.datastore.UiPreferences
import com.retro.arcade.feature.downshaft.ui.DownshaftGameScreen
import com.retro.arcade.feature.dino.ui.DinoGameScreen
import com.retro.arcade.feature.home.HomeScreen
import com.retro.arcade.feature.minesweeper.ui.MinesweeperGameScreen
import com.retro.arcade.feature.settings.SettingsScreen
import com.retro.arcade.feature.snake.model.SnakeControlMode
import com.retro.arcade.feature.snake.model.SnakeDifficulty
import com.retro.arcade.feature.snake.model.SnakePalette
import com.retro.arcade.feature.snake.ui.SnakeGameScreen
import com.retro.arcade.feature.snake.ui.SnakeSetupScreen
import com.retro.arcade.feature.tetris.ui.TetrisGameScreen

object AppRoute {
    const val Home = "home"
    const val Settings = "settings"
    const val DinoGame = "dino/game"
    const val DownshaftGame = "downshaft/game"
    const val TetrisGame = "tetris/game"
    const val MinesweeperGame = "minesweeper/game"
    const val SnakeSetup = "snake/setup"
    const val SnakeGamePattern = "snake/game/{difficulty}/{controlMode}/{palette}"

    fun snakeGame(
        difficulty: SnakeDifficulty,
        controlMode: SnakeControlMode,
        palette: SnakePalette
    ): String {
        return "snake/game/${difficulty.name}/${controlMode.name}/${palette.name}"
    }
}

@Composable
fun RetroArcadeNavHost(
    preferences: UiPreferences,
    preferencesRepository: AppPreferencesRepository
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Home,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(AppRoute.Home) {
            HomeScreen(
                bestSnakeScore = preferences.bestSnakeScore,
                bestDinoScore = preferences.bestDinoScore,
                bestDownshaftScore = preferences.bestDownshaftScore,
                bestTetrisScore = preferences.bestTetrisScore,
                bestMinesweeperTime = preferences.bestMinesweeperTime,
                onOpenSnake = { navController.navigate(AppRoute.SnakeSetup) },
                onOpenDino = { navController.navigate(AppRoute.DinoGame) },
                onOpenDownshaft = { navController.navigate(AppRoute.DownshaftGame) },
                onOpenTetris = { navController.navigate(AppRoute.TetrisGame) },
                onOpenMinesweeper = { navController.navigate(AppRoute.MinesweeperGame) },
                onOpenSettings = { navController.navigate(AppRoute.Settings) }
            )
        }

        composable(AppRoute.Settings) {
            SettingsScreen(
                preferences = preferences,
                preferencesRepository = preferencesRepository,
                onBack = { navController.navigateUp() }
            )
        }

        composable(AppRoute.SnakeSetup) {
            SnakeSetupScreen(
                preferences = preferences,
                preferencesRepository = preferencesRepository,
                onBack = { navController.navigateUp() },
                onStartGame = { difficulty, controlMode, palette ->
                    navController.navigate(
                        AppRoute.snakeGame(difficulty, controlMode, palette)
                    )
                }
            )
        }

        composable(AppRoute.DinoGame) {
            DinoGameScreen(
                bestScore = preferences.bestDinoScore,
                onBack = { navController.navigateUp() },
                onPersistBestScore = { score ->
                    preferencesRepository.updateBestDinoScore(score)
                }
            )
        }

        composable(AppRoute.DownshaftGame) {
            DownshaftGameScreen(
                bestScore = preferences.bestDownshaftScore,
                onBack = { navController.navigateUp() },
                onPersistBestScore = { score ->
                    preferencesRepository.updateBestDownshaftScore(score)
                }
            )
        }

        composable(AppRoute.TetrisGame) {
            TetrisGameScreen(
                bestScore = preferences.bestTetrisScore,
                onBack = { navController.navigateUp() },
                onPersistBestScore = { score ->
                    preferencesRepository.updateBestTetrisScore(score)
                }
            )
        }

        composable(AppRoute.MinesweeperGame) {
            MinesweeperGameScreen(
                bestTimeSeconds = preferences.bestMinesweeperTime,
                onBack = { navController.navigateUp() },
                onPersistBestTime = { seconds ->
                    preferencesRepository.updateBestMinesweeperTime(seconds)
                }
            )
        }

        composable(
            route = AppRoute.SnakeGamePattern,
            arguments = listOf(
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("controlMode") { type = NavType.StringType },
                navArgument("palette") { type = NavType.StringType }
            )
        ) { entry ->
            val difficulty = SnakeDifficulty.fromName(
                entry.arguments?.getString("difficulty")
            )
            val controlMode = SnakeControlMode.fromName(
                entry.arguments?.getString("controlMode")
            )
            val palette = SnakePalette.fromName(
                entry.arguments?.getString("palette")
            )

            SnakeGameScreen(
                difficulty = difficulty,
                controlMode = controlMode,
                palette = palette,
                bestScore = preferences.bestSnakeScore,
                onBack = { navController.navigateUp() },
                onPersistBestScore = { score ->
                    preferencesRepository.updateBestSnakeScore(score)
                }
            )
        }
    }
}
