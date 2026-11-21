package com.pipes.game.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.pipes.game.ui.screens.GameScreen
import com.pipes.game.ui.theme.PipesTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PipesTheme {
                GameScreen(viewModel = viewModel)
            }
        }
    }
}
