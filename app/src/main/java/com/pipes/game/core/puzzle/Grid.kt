package com.pipes.game.core.puzzle

/**
 * Coordenada bidimensional dentro de la cuadrícula.
 */
data class Position(val x: Int, val y: Int)

/**
 * Registro de un movimiento realizado por el jugador para soporte de deshacer (Undo).
 */
data class MoveRecord(val x: Int, val y: Int, val previousMask: Int, val newMask: Int)

/**
 * Representación y gestión espacial de la cuadrícula del juego.
 * Soporta modo acotado normal y modo toroide (wrapping cilíndrico/esférico en bordes).
 */
class Grid(
    val width: Int,
    val height: Int,
    val isTorus: Boolean = false
) {
    val totalCells: Int = width * height
    val corePosition: Position = Position(width / 2, height / 2)

    /**
     * Retorna la posición del vecino en la dirección indicada,
     * o null si sale de los límites en modo no-toroide.
     */
    fun getNeighbor(pos: Position, dir: Int): Position? {
        val dx = when (dir) {
            Tile.EAST -> 1
            Tile.WEST -> -1
            else -> 0
        }
        val dy = when (dir) {
            Tile.SOUTH -> 1
            Tile.NORTH -> -1
            else -> 0
        }

        var nx = pos.x + dx
        var ny = pos.y + dy

        if (isTorus) {
            nx = (nx + width) % width
            ny = (ny + height) % height
            return Position(nx, ny)
        } else {
            if (nx in 0 until width && ny in 0 until height) {
                return Position(nx, ny)
            }
            return null
        }
    }

    /**
     * Convierte posición (X, Y) a índice plano.
     */
    fun toIndex(pos: Position): Int {
        return pos.y * width + pos.x
    }
}
