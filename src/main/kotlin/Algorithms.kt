package ar.edu.itba.ss

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*
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
                    if (particle == maybeNeighbour || particle.neighbours.contains(maybeNeighbour)) {
                        continue
                    }

                    mutuallyAddNeighbours(particle, maybeNeighbour, settings)
                }
            }
        }
    }
}

fun bruteForce(settings: Settings) {
    logger.debug { "Processing Brute Force" }

    for (particle1 in settings.particles) {
        for (particle2 in settings.particles) {
            if (particle1 == particle2 || particle1.neighbours.contains(particle2)) {
                continue
            }

            mutuallyAddNeighbours(particle1, particle2, settings)
        }
    }
}

private fun mutuallyAddNeighbours(particle: Particle, maybeNeighbour: Particle, settings: Settings) {
    val distance = particle.distance(maybeNeighbour, settings)
    if (distance <= settings.rc) {
        particle.neighbours.add(maybeNeighbour)
        maybeNeighbour.neighbours.add(particle)
    }
}