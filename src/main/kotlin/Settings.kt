package ar.edu.itba.ss

data class Settings(
    val algorithm: Algorithm,
    val particles: List<Particle>,
    val matrixSize: Int,
    val rc: Double,
    val periodicContour: Boolean,
    val boardSizeLength: Int = 0,
    val time: Double = 0.0,
)
