package ar.edu.itba.ss

import com.sun.source.tree.Tree
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.TreeSet
import kotlin.math.floor

private val logger = KotlinLogging.logger {}

private fun generateGrid(settings: Settings): Grid {
    val gridRows = mutableListOf<Row>()
    (0 until settings.matrixSize).forEach { rowIndex ->
        val row = mutableListOf<Cell>()
        (0 until settings.matrixSize).forEach { columnIndex ->
            row.add(Cell(index = rowIndex * settings.matrixSize + columnIndex, content = TreeSet()))
        }

        gridRows.add(row)
    }

    return Grid(gridRows)
}

private fun addParticlesToGrid(grid: Grid, settings: Settings) {
    settings.particles.forEach { particle ->
        val cellIndex =
            (floor(settings.matrixSize * particle.y / settings.boardSizeLength) * settings.matrixSize
                    + floor(settings.matrixSize * particle.x / settings.boardSizeLength)).toInt()

        grid.apply {
            getCell(cellIndex)?.let { cell -> particle.cell = cell } // Mutate Particle
            addParticle(particle, cellIndex)
        }
    }
}

fun cellIndexMethod(settings: Settings) {
    logger.debug { "Processing CIM" }

    val grid = generateGrid(settings)
    addParticlesToGrid(grid, settings)

    grid.forEachCell { cell ->
        val adjacentCells = grid.getAdjacentCells(cell, settings)

        cell.content.forEach { particle ->
            adjacentCells.forEach { aCell ->
                for (maybeNeighbour in aCell.content) {
                    if (particle.neighbours.contains(maybeNeighbour)) {
                        continue
                    }

                    val distance = particle.distance(maybeNeighbour, settings)
                    if (distance <= settings.rc) {
                        particle.neighbours.add(maybeNeighbour)
                        maybeNeighbour.neighbours.add(particle)
                    }
                }
            }
        }
    }
}

fun bruteForce(settings: Settings) {
    logger.debug { "Processing Brute Force" }
}

