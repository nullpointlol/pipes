package com.pipes.game.core.puzzle

/**
 * Solucionador deductivo basado en las reglas de lógica humana de Simon Tatham (net.c).
 * Determina si una configuración de piezas puede resolverse de forma unívoca sin adivinanzas.
 */
object DeductiveSolver {

    /**
     * Verifica si el tablero solución tiene solución única determinable por deducción lógica.
     */
    fun hasUniqueSolution(grid: Grid, solution: Array<IntArray>): Boolean {
        val width = grid.width
        val height = grid.height

        // Para cada celda, guardamos qué direcciones son posibles (posibles máscaras)
        // Cada celda tiene una lista de posibles orientaciones permitidas
        val possibleOrientations = Array(height) { y ->
            Array(width) { x ->
                val baseMask = solution[y][x]
                getValidRotations(baseMask).toMutableList()
            }
        }

        var progress = true
        var iterations = 0
        val maxIterations = width * height * 4

        while (progress && iterations < maxIterations) {
            progress = false
            iterations++

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pos = Position(x, y)
                    val orientations = possibleOrientations[y][x]

                    if (orientations.size <= 1) continue // Ya resuelta

                    val toRemove = ArrayList<Int>()

                    for (candidate in orientations) {
                        var invalid = false

                        // Verificar compatibilidad de cada puerto con vecinos
                        for (dir in Tile.ALL_DIRS) {
                            val neighborPos = grid.getNeighbor(pos, dir)
                            val hasPort = (candidate and dir) != 0

                            if (neighborPos == null) {
                                // Apunta hacia una pared exterior: INVÁLIDO
                                if (hasPort) {
                                    invalid = true
                                    break
                                }
                            } else {
                                val neighborOrientations = possibleOrientations[neighborPos.y][neighborPos.x]
                                val oppDir = Tile.opposite(dir)

                                if (hasPort) {
                                    // Si este candidato apunta hacia el vecino, el vecino DEBE tener al menos una orientación que pueda corresponder
                                    val neighborCanConnect = neighborOrientations.any { (it and oppDir) != 0 }
                                    if (!neighborCanConnect) {
                                        invalid = true
                                        break
                                    }
                                } else {
                                    // Si no tenemos puerto aquí, pero el vecino OBLIGATORIAMENTE tiene puerto hacia nosotros en TODAS sus opciones
                                    val neighborMustConnect = neighborOrientations.all { (it and oppDir) != 0 }
                                    if (neighborMustConnect) {
                                        invalid = true
                                        break
                                    }
                                }
                            }
                        }

                        if (invalid) {
                            toRemove.add(candidate)
                        }
                    }

                    if (toRemove.isNotEmpty()) {
                        orientations.removeAll(toRemove)
                        progress = true
                    }
                }
            }
        }

        // Si todas las celdas se redujeron a exactamente 1 orientación válida, la solución es 100% única y deducible
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (possibleOrientations[y][x].size != 1) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Retorna todas las orientaciones distintas que puede adoptar una pieza al girar 360°.
     */
    private fun getValidRotations(mask: Int): Set<Int> {
        val result = HashSet<Int>()
        var curr = mask
        for (i in 0 until 4) {
            result.add(curr)
            curr = Tile.rotateClockwise(curr)
        }
        return result
    }
}
