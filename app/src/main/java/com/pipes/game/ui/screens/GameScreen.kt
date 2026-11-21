package com.pipes.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pipes.game.ui.CurrentScreen
import com.pipes.game.ui.GameViewModel
import com.pipes.game.ui.components.PipeBoardCanvas
import com.pipes.game.ui.theme.AmberStars
import com.pipes.game.ui.theme.BgDark
import com.pipes.game.ui.theme.BorderCyan
import com.pipes.game.ui.theme.CardDark
import com.pipes.game.ui.theme.CyanGlow
import com.pipes.game.ui.theme.TextDim
import com.pipes.game.ui.theme.TextLight

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BgDark
    ) {
        when (state.currentScreen) {
            CurrentScreen.HOME -> {
                HomeScreen(
                    isTorus = state.isTorus,
                    isSound = viewModel.soundController.isEnabled,
                    onSelectDensity = { cols -> viewModel.selectDensityAndPlay(cols) },
                    onToggleTorus = { torus -> viewModel.toggleTorus(torus) },
                    onToggleSound = { sound -> viewModel.toggleSound(sound) }
                )
            }
            CurrentScreen.GAME -> {
                GameplayScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * 1. PANTALLA INICIAL (ESTILO I LOVE HUE: DENSIDAD DE COLUMNAS ADAPTATIVAS)
 */
@Composable
private fun HomeScreen(
    isTorus: Boolean,
    isSound: Boolean,
    onSelectDensity: (Int) -> Unit,
    onToggleTorus: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit
) {
    val levels = listOf(
        Pair(4, "Inicio"),
        Pair(5, "Calma"),
        Pair(6, "Flujo"),
        Pair(7, "Desafío"),
        Pair(8, "Experto"),
        Pair(9, "Maestro")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PIPES",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextLight,
            letterSpacing = 6.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "LÓGICA • ELECTRO-ZEN",
            fontSize = 11.sp,
            color = TextDim,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Cuadrícula 3x2 de densidades
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(levels) { (cols, label) ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSelectDensity(cols) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$cols cols",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextLight
                        )
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = TextDim,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Opciones de configuración inicial
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x14FFFFFF))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Modo Toroide (Wrapping)", fontSize = 13.sp, color = TextDim)
                    Checkbox(
                        checked = isTorus,
                        onCheckedChange = onToggleTorus,
                        colors = CheckboxDefaults.colors(checkedColor = CyanGlow)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Audio Reactivo", fontSize = 13.sp, color = TextDim)
                    Checkbox(
                        checked = isSound,
                        onCheckedChange = onToggleSound,
                        colors = CheckboxDefaults.colors(checkedColor = CyanGlow)
                    )
                }
            }
        }
    }
}

/**
 * 2. PANTALLA DE JUEGO (SOLO LA GRILLA ALARGADA + 4 BOTONES ICONO MINIMALISTAS)
 */
@Composable
private fun GameplayScreen(
    viewModel: GameViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Espacio superior limpio
            Spacer(modifier = Modifier.height(4.dp))

            // Grilla que llena el área vertical disponible
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PipeBoardCanvas(
                    grid = state.grid,
                    board = state.board,
                    energized = state.flowStatus.energizedMatrix,
                    onCellClick = { x, y -> viewModel.onCellClicked(x, y) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Barra inferior flotante de 4 botones minimalistas (Solo iconos)
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Deshacer
                MinimalistIconButton(
                    icon = Icons.Default.Undo,
                    contentDescription = "Deshacer",
                    onClick = { viewModel.undo() }
                )

                // 2. Pista (con badge de cantidad)
                Box {
                    MinimalistIconButton(
                        icon = Icons.Default.Lightbulb,
                        contentDescription = "Pista",
                        iconTint = if (state.hintsLeft > 0) AmberStars else TextDim,
                        onClick = { viewModel.useHint() }
                    )
                    if (state.hintsLeft > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(AmberStars),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.hintsLeft.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BgDark
                            )
                        }
                    }
                }

                // 3. Nuevo / Reiniciar
                MinimalistIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Nuevo",
                    onClick = { viewModel.startNewLevel() }
                )

                // 4. Ajustes
                MinimalistIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    onClick = { viewModel.openSettings() }
                )
            }
        }

        // Modal de Ajustes (Settings)
        if (state.showSettingsDialog) {
            SettingsDialog(
                currentCols = state.grid.width,
                isTorus = state.isTorus,
                isSound = viewModel.soundController.isEnabled,
                onSelectDensity = { cols -> viewModel.selectDensityAndPlay(cols) },
                onToggleTorus = { torus -> viewModel.toggleTorus(torus) },
                onToggleSound = { sound -> viewModel.toggleSound(sound) },
                onOpenHome = { viewModel.openHome() },
                onClose = { viewModel.closeSettings() }
            )
        }

        // Modal de Victoria ("I Love Hue" style)
        if (state.isWon) {
            VictoryDialog(
                moves = state.moves,
                optimal = state.optimalMoves,
                stars = state.stars,
                onNextLevel = { viewModel.startNewLevel() },
                onOpenHome = { viewModel.openHome() }
            )
        }
    }
}

@Composable
private fun MinimalistIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    iconTint: Color = TextDim,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(CardDark)
            .border(1.dp, Color(0x22FFFFFF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * MODAL DE AJUSTES MINIMALISTA
 */
@Composable
private fun SettingsDialog(
    currentCols: Int,
    isTorus: Boolean,
    isSound: Boolean,
    onSelectDensity: (Int) -> Unit,
    onToggleTorus: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onOpenHome: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark.copy(alpha = 0.85f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CONFIGURACIÓN", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = TextLight)
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDim)
                    }
                }

                Text("Densidad de columnas:", fontSize = 12.sp, color = TextDim)

                // Selector de columnas (4 a 9 cols)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(4, 5, 6, 7, 8, 9).forEach { cols ->
                        val isSelected = cols == currentCols
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanGlow else Color(0x1AFFFFFF))
                                .clickable { onSelectDensity(cols) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${cols}c",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BgDark else TextDim
                            )
                        }
                    }
                }

                // Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Modo Toroide", fontSize = 13.sp, color = TextDim)
                    Checkbox(checked = isTorus, onCheckedChange = onToggleTorus, colors = CheckboxDefaults.colors(checkedColor = CyanGlow))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Audio Reactivo", fontSize = 13.sp, color = TextDim)
                    Checkbox(checked = isSound, onCheckedChange = onToggleSound, colors = CheckboxDefaults.colors(checkedColor = CyanGlow))
                }

                Button(
                    onClick = onOpenHome,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Inicio", modifier = Modifier.size(16.dp), tint = TextDim)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volver a Inicio", color = TextLight, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * MODAL DE VICTORIA (ESTILO I LOVE HUE: ESTRELLAS + MOVIMIENTOS VS ÓPTIMO)
 */
@Composable
private fun VictoryDialog(
    moves: Int,
    optimal: Int,
    stars: Int,
    onNextLevel: () -> Unit,
    onOpenHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Estrellas
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (index < stars) AmberStars else Color(0xFF334155),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Estadísticas estilo "I Love Hue"
                Text(
                    text = "$moves movimientos • Óptimo: $optimal",
                    fontSize = 14.sp,
                    color = TextDim,
                    textAlign = TextAlign.Center
                )

                // Acciones (Inicio y Siguiente)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onOpenHome,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Menú", tint = TextLight)
                    }

                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("➔", color = BgDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
