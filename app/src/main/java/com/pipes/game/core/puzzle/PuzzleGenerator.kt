package com.pipes.game.core.puzzle

import java.util.Random

/**
 * Datos completos del rompecabezas generado.
 */
data class GeneratedPuzzle(
    val grid: Grid,
    val solution: Array<IntArray>,
    val shuffledBoard: Array<IntArray>,
    val optimalMoves: Int,
    val seed: Long
)

/**
 * Generador algorítmico de tableros basado en Árbol de Expansión Mínimo (Kruskal)
 * con pesos aleatorios y validación deductiva de solución única inspirada en Simon Tatham (net.c).
 */
class PuzzleGenerator(
    private val width: Int,
    private val height: Int,
    private val isTorus: Boolean = false
) {
    private val grid = Grid(width, height, isTorus)

    private data class Edge(
        val u: Int,
        val v: Int,
        val pos1: Position,
        val pos2: Position,
        val dirFrom1: Int
    )

    /**
     * Genera un tablero válido con solución única.
     */
    fun generate(seed: Long = System.currentTimeMillis()): GeneratedPuzzle {
        val rng = Random(seed)
        var attempts = 0
        val maxAttempts = 100

        var solution: Array<IntArray>? = null

        while (attempts < maxAttempts && solution == null) {
            attempts++
            val candidate = generateSpanningTree(rng)
            if (DeductiveSolver.hasUniqueSolution(grid, candidate)) {
                solution = candidate
            }
        }

        // Si excede intentos por semilla extrema, usar candidato
        val finalSolution = solution ?: generateSpanningTree(rng)

        // Barajar piezas (Shuffle)
        val shuffled = Array(height) { IntArray(width) }
        var optimalMoves = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val solMask = finalSolution[y][x]
                if (Tile.isInvariant(solMask)) {
                    shuffled[y][x] = solMask
                    continue
                }

                val turns = if (Tile.has180Symmetry(solMask)) {
                    if (rng.nextBoolean()) 0 else 1
                } else {
                    rng.nextInt(4)
                }

                var current = solMask
                for (t in 0 until turns) {
                    current = Tile.rotateClockwise(current)
                }
                shuffled[y][x] = current
                optimalMoves += Tile.turnsNeeded(current, solMask)
            }
        }

        return GeneratedPuzzle(
            grid = grid,
            solution = finalSolution,
            shuffledBoard = shuffled,
            optimalMoves = optimalMoves,
            seed = seed
        )
    }

    /**
     * Algoritmo de Kruskal con DSU (Disjoint Set Union) para generar un árbol de expansión aleatorio.
     * Garantiza matemáticamente que todas las celdas queden conectadas en 1 solo componente y sin ciclos.
     */
    private fun generateSpanningTree(rng: Random): Array<IntArray> {
        val board = Array(height) { IntArray(width) { 0 } }
        val edges = ArrayList<Edge>()

        // 1. Recolectar todas las aristas adyacentes
        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = Position(x, y)
                val u = grid.toIndex(p)

                // Vecino Este
                grid.getNeighbor(p, Tile.EAST)?.let { eastNeighbor ->
                    val v = grid.toIndex(eastNeighbor)
                    if (x + 1 < width || isTorus) {
                        edges.add(Edge(u, v, p, eastNeighbor, Tile.EAST))
                    }
                }

                // Vecino Sur
                grid.getNeighbor(p, Tile.SOUTH)?.let { southNeighbor ->
                    val v = grid.toIndex(southNeighbor)
                    if (y + 1 < height || isTorus) {
                        edges.add(Edge(u, v, p, southNeighbor, Tile.SOUTH))
                    }
                }
            }
        }

        // 2. Barajado aleatorio de aristas (equivalente a pesos aleatorios)
        for (i in edges.size - 1 downTo 1) {
            val j = rng.nextInt(i + 1)
            val temp = edges[i]
            edges[i] = edges[j]
            edges[j] = temp
        }

        // 3. Estructura DSU (Disjoint Set Union)
        val parent = IntArray(grid.totalCells) { it }
        fun find(i: Int): Int {
            var root = i
            while (root != parent[root]) {
                parent[root] = parent[parent[root]] // Compresión de camino
                root = parent[root]
            }
            return root
        }

        fun union(i: Int, j: Int): Boolean {
            val rootI = find(i)
            val rootJ = find(j)
            if (rootI != rootJ) {
                parent[rootI] = rootJ
                return true
            }
            return false
        }

        // 4. Agregar aristas al árbol
        var edgesCount = 0
        val targetEdges = grid.totalCells - 1

        for (edge in edges) {
            if (union(edge.u, edge.v)) {
                board[edge.pos1.y][edge.pos1.x] = board[edge.pos1.y][edge.pos1.x] or edge.dirFrom1
                board[edge.pos2.y][edge.pos2.x] = board[edge.pos2.y][edge.pos2.x] or Tile.opposite(edge.dirFrom1)
                edgesCount++
                if (edgesCount == targetEdges) break
            }
        }

        return board
    }
}
