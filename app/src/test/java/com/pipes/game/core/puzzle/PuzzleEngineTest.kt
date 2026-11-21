package com.pipes.game.core.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleEngineTest {

    @Test
    fun testTileBitmaskRotation() {
        // N(1) -> E(2) -> S(4) -> W(8) -> N(1)
        assertEquals(Tile.EAST, Tile.rotateClockwise(Tile.NORTH))
        assertEquals(Tile.SOUTH, Tile.rotateClockwise(Tile.EAST))
        assertEquals(Tile.WEST, Tile.rotateClockwise(Tile.SOUTH))
        assertEquals(Tile.NORTH, Tile.rotateClockwise(Tile.WEST))

        // Codo N+E (3) -> E+S (6) -> S+W (12) -> W+N (9) -> N+E (3)
        var elbow = Tile.NORTH or Tile.EAST
        elbow = Tile.rotateClockwise(elbow)
        assertEquals(Tile.EAST or Tile.SOUTH, elbow)
        elbow = Tile.rotateClockwise(elbow)
        assertEquals(Tile.SOUTH or Tile.WEST, elbow)
        elbow = Tile.rotateClockwise(elbow)
        assertEquals(Tile.WEST or Tile.NORTH, elbow)
        elbow = Tile.rotateClockwise(elbow)
        assertEquals(Tile.NORTH or Tile.EAST, elbow)

        // Cruz invariante (15 -> 15)
        assertEquals(15, Tile.rotateClockwise(15))
    }

    @Test
    fun testTileTurnsNeeded() {
        val mask = Tile.NORTH
        assertEquals(0, Tile.turnsNeeded(mask, Tile.NORTH))
        assertEquals(1, Tile.turnsNeeded(mask, Tile.EAST))
        assertEquals(2, Tile.turnsNeeded(mask, Tile.SOUTH))
        assertEquals(3, Tile.turnsNeeded(mask, Tile.WEST))
    }

    @Test
    fun testKruskalSpanningTreeProperties() {
        val width = 5
        val height = 5
        val generator = PuzzleGenerator(width, height, isTorus = false)
        val puzzle = generator.generate(seed = 12345L)

        val flow = FlowEvaluator.evaluate(puzzle.grid, puzzle.solution)

        // La solución generada por Kruskal debe ser un árbol perfecto:
        // 1. Victoria garantizada
        assertTrue("La solución de Kruskal debe cumplir victoria", flow.isWon)
        // 2. 100% de celdas energizadas
        assertEquals(100, flow.energyPercent)
        // 3. Cero fugas
        assertEquals(0, flow.leaksCount)
        // 4. Conexiones activas exactamente V - 1 (24 para 5x5)
        assertEquals(width * height - 1, flow.activeConnections)
    }

    @Test
    fun testTorusModeGeneration() {
        val width = 4
        val height = 4
        val generator = PuzzleGenerator(width, height, isTorus = true)
        val puzzle = generator.generate(seed = 67890L)

        val flow = FlowEvaluator.evaluate(puzzle.grid, puzzle.solution)
        assertTrue("Modo toroide debe generar árbol conexo sin fugas", flow.isWon)
        assertEquals(width * height - 1, flow.activeConnections)
    }

    @Test
    fun testRectangularTallBoards() {
        // Formatos alargados para celulares (ratios 9:16 a 9:22)
        val tallConfigurations = listOf(
            Pair(4, 9),   // ~9:20.2
            Pair(5, 11),  // ~9:19.8
            Pair(6, 13),  // ~9:19.5
            Pair(7, 15),  // ~9:19.3
            Pair(8, 17)   // ~9:19.1
        )

        for ((cols, rows) in tallConfigurations) {
            val generator = PuzzleGenerator(cols, rows, isTorus = false)
            val puzzle = generator.generate(seed = (cols * 1000 + rows).toLong())

            // 1. Verificar propiedades del árbol generador (solución)
            val solFlow = FlowEvaluator.evaluate(puzzle.grid, puzzle.solution)
            assertTrue("Tablero alargado ${cols}x${rows} debe tener solución válida", solFlow.isWon)
            assertEquals("Todas las celdas deben estar energizadas", 100, solFlow.energyPercent)
            assertEquals("No debe haber fugas en la solución", 0, solFlow.leaksCount)
            assertEquals("Debe tener exactamente V - 1 aristas", cols * rows - 1, solFlow.activeConnections)

            // 2. Verificar que el centro de poder se ubica correctamente
            assertEquals(cols / 2, puzzle.grid.corePosition.x)
            assertEquals(rows / 2, puzzle.grid.corePosition.y)

            // 3. El tablero barajado no debe estar ganado inicialmente (en tableros grandes)
            val shuffledFlow = FlowEvaluator.evaluate(puzzle.grid, puzzle.shuffledBoard)
            assertFalse("Tablero mezclado no debería estar resuelto", shuffledFlow.isWon)
            assertTrue("Debe requerir movimientos óptimos > 0", puzzle.optimalMoves > 0)
        }
    }

    @Test
    fun testRectangularTorusMode() {
        val cols = 5
        val rows = 12
        val generator = PuzzleGenerator(cols, rows, isTorus = true)
        val puzzle = generator.generate(seed = 998877L)

        val solFlow = FlowEvaluator.evaluate(puzzle.grid, puzzle.solution)
        assertTrue("Toroide alargado 5x12 debe ser conexo y válido", solFlow.isWon)
        assertEquals(100, solFlow.energyPercent)
        assertEquals(0, solFlow.leaksCount)
        assertEquals(cols * rows - 1, solFlow.activeConnections)
    }
}

