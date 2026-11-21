package com.pipes.game.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pipes.game.core.puzzle.Grid
import com.pipes.game.core.puzzle.Tile
import com.pipes.game.ui.theme.BlueElectric
import com.pipes.game.ui.theme.BorderCyan
import com.pipes.game.ui.theme.CyanGlow
import com.pipes.game.ui.theme.PipeInactiveBase
import kotlin.math.min

@Composable
fun PipeBoardCanvas(
    grid: Grid,
    board: Array<IntArray>,
    energized: Array<BooleanArray>,
    onCellClick: (x: Int, y: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CorePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(grid.width, grid.height) {
                detectTapGestures { offset ->
                    val pad = 10.dp.toPx()
                    val availW = size.width - pad * 2f
                    val availH = size.height - pad * 2f
                    val cellSize = min(availW / grid.width, availH / grid.height)
                    val boardW = cellSize * grid.width
                    val boardH = cellSize * grid.height
                    val startX = (size.width - boardW) / 2f
                    val startY = (size.height - boardH) / 2f

                    val col = ((offset.x - startX) / cellSize).toInt()
                    val row = ((offset.y - startY) / cellSize).toInt()

                    if (col in 0 until grid.width && row in 0 until grid.height) {
                        onCellClick(col, row)
                    }
                }
            }
    ) {
        val pad = 10.dp.toPx()
        val availW = size.width - pad * 2f
        val availH = size.height - pad * 2f
        val cellSize = min(availW / grid.width, availH / grid.height)
        val boardW = cellSize * grid.width
        val boardH = cellSize * grid.height
        val startX = (size.width - boardW) / 2f
        val startY = (size.height - boardH) / 2f
        val pipeWidth = cellSize * 0.14f

        val boardPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset(startX, startY), Size(boardW, boardH)),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            )
        }

        // 1. Fondo de la cuadrícula
        drawPath(boardPath, color = Color(0xFF0F1420))
        drawPath(boardPath, color = BorderCyan, style = Stroke(width = 1.5.dp.toPx()))

        // 2. Limitar dibujo estrictamente al interior del tablero (sin desborde al exterior)
        clipPath(boardPath) {
            // Líneas de separación sutiles
            for (i in 1 until grid.width) {
                drawLine(
                    color = Color.White.copy(alpha = 0.035f),
                    start = Offset(startX + i * cellSize, startY),
                    end = Offset(startX + i * cellSize, startY + boardH),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (j in 1 until grid.height) {
                drawLine(
                    color = Color.White.copy(alpha = 0.035f),
                    start = Offset(startX, startY + j * cellSize),
                    end = Offset(startX + boardW, startY + j * cellSize),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Trazado de tuberías en cada celda
            for (y in 0 until grid.height) {
                for (x in 0 until grid.width) {
                    val cx = startX + x * cellSize + cellSize / 2f
                    val cy = startY + y * cellSize + cellSize / 2f
                    val mask = board[y][x]
                    val isEnergized = energized[y][x]
                    val isCore = (x == grid.corePosition.x && y == grid.corePosition.y)

                    drawPipePiece(
                        mask = mask,
                        center = Offset(cx, cy),
                        cellSize = cellSize,
                        pipeWidth = pipeWidth,
                        isEnergized = isEnergized
                    )

                    if (isCore) {
                        drawPowerCore(
                            center = Offset(cx, cy),
                            cellSize = cellSize,
                            pulseScale = pulseScale
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPipePiece(
    mask: Int,
    center: Offset,
    cellSize: Float,
    pipeWidth: Float,
    isEnergized: Boolean
) {
    if (mask == 0) return
    val half = cellSize / 2f

    val path = Path().apply {
        // Trazado continuo con extremos planos (StrokeCap.Butt) para uniones perfectas
        when (mask) {
            // Rectas
            Tile.NORTH or Tile.SOUTH -> {
                moveTo(center.x, center.y - half)
                lineTo(center.x, center.y + half)
            }
            Tile.EAST or Tile.WEST -> {
                moveTo(center.x - half, center.y)
                lineTo(center.x + half, center.y)
            }
            // Codos
            Tile.NORTH or Tile.EAST -> {
                moveTo(center.x, center.y - half)
                lineTo(center.x, center.y)
                lineTo(center.x + half, center.y)
            }
            Tile.EAST or Tile.SOUTH -> {
                moveTo(center.x + half, center.y)
                lineTo(center.x, center.y)
                lineTo(center.x, center.y + half)
            }
            Tile.SOUTH or Tile.WEST -> {
                moveTo(center.x, center.y + half)
                lineTo(center.x, center.y)
                lineTo(center.x - half, center.y)
            }
            Tile.WEST or Tile.NORTH -> {
                moveTo(center.x - half, center.y)
                lineTo(center.x, center.y)
                lineTo(center.x, center.y - half)
            }
            // Terminales
            Tile.NORTH -> { moveTo(center.x, center.y); lineTo(center.x, center.y - half) }
            Tile.EAST  -> { moveTo(center.x, center.y); lineTo(center.x + half, center.y) }
            Tile.SOUTH -> { moveTo(center.x, center.y); lineTo(center.x, center.y + half) }
            Tile.WEST  -> { moveTo(center.x, center.y); lineTo(center.x - half, center.y) }
            // T-pipes
            else -> {
                if ((mask and (Tile.NORTH or Tile.SOUTH)) == (Tile.NORTH or Tile.SOUTH)) {
                    moveTo(center.x, center.y - half)
                    lineTo(center.x, center.y + half)
                    if ((mask and Tile.EAST) != 0) { moveTo(center.x, center.y); lineTo(center.x + half, center.y) }
                    if ((mask and Tile.WEST) != 0) { moveTo(center.x, center.y); lineTo(center.x - half, center.y) }
                } else if ((mask and (Tile.EAST or Tile.WEST)) == (Tile.EAST or Tile.WEST)) {
                    moveTo(center.x - half, center.y)
                    lineTo(center.x + half, center.y)
                    if ((mask and Tile.NORTH) != 0) { moveTo(center.x, center.y); lineTo(center.x, center.y - half) }
                    if ((mask and Tile.SOUTH) != 0) { moveTo(center.x, center.y); lineTo(center.x, center.y + half) }
                } else {
                    // Cruz
                    moveTo(center.x, center.y - half); lineTo(center.x, center.y + half)
                    moveTo(center.x - half, center.y); lineTo(center.x + half, center.y)
                }
            }
        }
    }

    val bitCount = Integer.bitCount(mask and 0x0F)

    if (isEnergized) {
        // Pase 1: Resplandor Neón suave exterior
        drawPath(
            path = path,
            color = CyanGlow.copy(alpha = 0.35f),
            style = Stroke(
                width = pipeWidth * 2.2f,
                cap = StrokeCap.Butt,
                join = StrokeJoin.Round
            )
        )

        // Pase 2: Conductor eléctrico central blanco brillante
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(
                width = pipeWidth,
                cap = StrokeCap.Butt,
                join = StrokeJoin.Round
            )
        )

        // Nodo terminal sutil si es terminal
        if (bitCount == 1) {
            drawCircle(
                color = Color.White,
                radius = pipeWidth * 0.7f,
                center = center
            )
        }
    } else {
        // Tubería apagada Zen Slate
        drawPath(
            path = path,
            color = PipeInactiveBase,
            style = Stroke(
                width = pipeWidth * 1.35f,
                cap = StrokeCap.Butt,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFF64748B),
            style = Stroke(
                width = pipeWidth * 0.9f,
                cap = StrokeCap.Butt,
                join = StrokeJoin.Round
            )
        )

        if (bitCount == 1) {
            drawCircle(
                color = Color(0xFF64748B),
                radius = pipeWidth * 0.65f,
                center = center
            )
        }
    }
}

private fun DrawScope.drawPowerCore(
    center: Offset,
    cellSize: Float,
    pulseScale: Float
) {
    val radius = cellSize * 0.18f * pulseScale

    // Corona de resplandor degradada
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                CyanGlow.copy(alpha = 0.85f),
                BlueElectric.copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.8f
        ),
        radius = radius * 1.8f,
        center = center
    )

    // Núcleo central blanco brillante
    drawCircle(
        color = Color.White,
        radius = radius * 0.65f,
        center = center
    )
}
