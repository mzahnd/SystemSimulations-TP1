package ar.edu.itba.ss

data class Cell(val index: Int, val content: MutableSet<Particle>)
typealias Row = List<Cell>

class Grid(private val rows: List<Row>) {
    private val cellByIndex: Map<Int, Cell> = rows.flatten().associateBy { it.index }
    private val rowIndexByCellIndex: Map<Int, Int> = rows.flatMapIndexed { i, row ->
        row.map { cell -> cell.index to i }
    }.toMap()
    private val rowByCellIndex: Map<Int, Row> = rows.flatMap { row ->
        row.map { cell -> cell.index to row }
    }.toMap()

    fun getCell(index: Int): Cell? = cellByIndex[index]
    fun getRowByCellIndex(cellIndex: Int): Row? = rowByCellIndex[cellIndex]
    fun getRowIndexByCellIndex(cellIndex: Int): Int? = rowIndexByCellIndex[cellIndex]

    fun addParticle(particle: Particle, cellIndex: Int) {
        for (row in rows) {
            for (cell in row) {
                if (cell.index == cellIndex) {
                    cell.content += particle
                }
            }
        }
    }

    fun forEachCell(action: (Cell) -> Unit) {
        rows.forEach { row -> row.forEach { action(it) } }
    }

    private fun adjacentCellsInRow(cellIndex: Int, row: Row, settings: Settings): List<Cell> =
        if (row.isEmpty()) {
            emptyList()
        } else {
            if (settings.periodicContour) {
                listOf(
                    if (cellIndex - 1 < 0) row.last() else row[cellIndex - 1],
                    if (cellIndex + 1 > settings.matrixSize - 1) row.first() else row[cellIndex + 1],
                    row[cellIndex]
                )
            } else {
                mutableListOf<Cell>().apply {
                    if (cellIndex - 1 >= 0) add(row[cellIndex - 1])
                    if (cellIndex + 1 <= settings.matrixSize - 1) add(row[cellIndex + 1])
                    row[cellIndex]
                }
            }
        }

    fun getAdjacentCells(cell: Cell, settings: Settings): List<Cell> {
        return getRowIndexByCellIndex(cell.index)?.let { rowIndex ->
            val previousRow =
                if (settings.periodicContour && rowIndex - 1 < 0) rows.last() else if (rowIndex - 1 >= 0) rows[rowIndex - 1] else emptyList()
            val thisRow = rows[rowIndex]
            val nextRow =
                if (settings.periodicContour && rowIndex + 1 > settings.matrixSize - 1) rows.first() else if (rowIndex + 1 <= settings.matrixSize - 1) rows[rowIndex + 1] else emptyList()

            adjacentCellsInRow(cell.index % settings.matrixSize, previousRow, settings) +
                    adjacentCellsInRow(cell.index % settings.matrixSize, thisRow, settings) +
                    adjacentCellsInRow(cell.index % settings.matrixSize, nextRow, settings)
        } ?: emptyList()
    }
}