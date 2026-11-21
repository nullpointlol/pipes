package com.pipes.game.core.puzzle

/**
 * Representación de datos de tuberías mediante máscara de 4 bits.
 * Norte = 1 (0001), Este = 2 (0010), Sur = 4 (0100), Oeste = 8 (1000).
 * Basado en la especificación matemática de Simon Tatham (net.c).
 */
object Tile {
    const val NONE  = 0
    const val NORTH = 1  // 0001
    const val EAST  = 2  // 0010
    const val SOUTH = 4  // 0100
    const val WEST  = 8  // 1000

    val ALL_DIRS = intArrayOf(NORTH, EAST, SOUTH, WEST)

    /**
     * Retorna la dirección opuesta correspondiente.
     */
    fun opposite(dir: Int): Int = when (dir) {
        NORTH -> SOUTH
        EAST  -> WEST
        SOUTH -> NORTH
        WEST  -> EAST
        else  -> NONE
    }

    /**
     * Rotación horaria de 90 grados:
     * N(1) -> E(2) -> S(4) -> W(8) -> N(1)
     * Desplazamiento cíclico a la derecha de los 4 bits.
     */
    fun rotateClockwise(mask: Int): Int {
        val shifted = (mask shl 1) and 0x0F
        val overflow = (mask and 0x08) shr 3
        return shifted or overflow
    }

    /**
     * Calcula la cantidad de giros horarios (0..3) necesarios para transformar
     * la máscara actual en la máscara objetivo.
     */
    fun turnsNeeded(current: Int, target: Int): Int {
        var turns = 0
        var m = current
        while (turns < 4) {
            if (m == target) return turns
            m = rotateClockwise(m)
            turns++
        }
        return 0
    }

    /**
     * Determina si la pieza tiene simetría de 180° (por ejemplo las rectas 5 y 10).
     */
    fun has180Symmetry(mask: Int): Boolean {
        return mask == 5 || mask == 10 // 0101 (N+S) o 1010 (E+W)
    }

    /**
     * Determina si la pieza es invariante ante rotaciones (cruz 15 o vacía 0).
     */
    fun isInvariant(mask: Int): Boolean {
        return mask == 0 || mask == 15
    }
}
