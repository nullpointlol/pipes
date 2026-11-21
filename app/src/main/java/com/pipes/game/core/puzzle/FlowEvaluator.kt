package com.pipes.game.core.puzzle

import java.util.ArrayDeque

/**
 * Resultado de la evaluación del flujo de energía en el tablero.
 */
data class FlowStatus(
    val isWon: Boolean,
    val energyPercent: Int,
    val energizedCount: Int,
    val totalCells: Int,
    val leaksCount: Int,
    val activeConnections: Int,
    val energizedMatrix: Array<BooleanArray>
)

/**
 * Evaluador en tiempo real de conectividad, propagación de corriente eléctrica y victoria.
 */
object FlowEvaluator {

    fun evaluate(grid: Grid, board: Array<IntArray>): FlowStatus {
        val width = grid.width
        val height = grid.height
        val totalCells = grid.totalCells
        val core = grid.corePosition

        val energized = Array(height) { BooleanArray(width) { false } }
        val visited = Array(height) { BooleanArray(width) { false } }
        val queue = ArrayDeque<Position>()

        // 1. Iniciar BFS desde el Núcleo Central (Power Core)
        energized[core.y][core.x] = true
        visited[core.y][core.x] = true
        queue.add(core)

        var energizedCount = 1

        while (queue.isNotEmpty()) {
            val curr = queue.poll() ?: continue
            val currMask = board[curr.y][curr.x]

            for (dir in Tile.ALL_DIRS) {
                if ((currMask and dir) != 0) {
                    val neighbor = grid.getNeighbor(curr, dir) ?: continue
                    val neighborMask = board[neighbor.y][neighbor.x]
                    val oppDir = Tile.opposite(dir)

                    // Verificación de reciprocidad: ambos puertos deben estar abiertos
                    if ((neighborMask and oppDir) != 0) {
                        if (!visited[neighbor.y][neighbor.x]) {
                            visited[neighbor.y][neighbor.x] = true
                            energized[neighbor.y][neighbor.x] = true
                            energizedCount++
                            queue.add(neighbor)
                        }
                    }
                }
            }
        }

        // 2. Conteo de fugas y conexiones activas
        var leaks = 0
        var totalDirectedConnections = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = Position(x, y)
                val mask = board[y][x]

                for (dir in Tile.ALL_DIRS) {
                    if ((mask and dir) != 0) {
                        val neighbor = grid.getNeighbor(p, dir)
                        if (neighbor == null) {
                            leaks++
                        } else {
                            val neighborMask = board[neighbor.y][neighbor.x]
                            val oppDir = Tile.opposite(dir)
                            if ((neighborMask and oppDir) != 0) {
                                totalDirectedConnections++
                            } else {
                                leaks++
                            }
                        }
                    }
                }
            }
        }

        val activeConnections = totalDirectedConnections / 2
        val targetConnections = totalCells - 1

        // Reglas de victoria:
        // 1. Todas las celdas energizadas (conectadas a la fuente central).
        // 2. Cero fugas hacia paredes o bordes abiertos sin correspondencia.
        // 3. Cero ciclos cerrados (las conexiones activas deben ser exactamente V - 1).
        val isWon = (energizedCount == totalCells) && (leaks == 0) && (activeConnections == targetConnections)
        val percent = (energizedCount * 100) / totalCells

        return FlowStatus(
            isWon = isWon,
            energyPercent = percent,
            energizedCount = energizedCount,
            totalCells = totalCells,
            leaksCount = leaks,
            activeConnections = activeConnections,
            energizedMatrix = energized
        )
    }
}
