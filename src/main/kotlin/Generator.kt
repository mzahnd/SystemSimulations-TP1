package ar.edu.itba.ss

import java.io.File
import kotlin.random.Random

fun generateRandomStaticConfig(file: File, numberOfParticles: Int?, boardSideLength: Int?, particleRadius: Double?, interactionDistance: Double?): Pair<Int, List<Particle>> {
    val nParticles = numberOfParticles ?: Random.nextInt(10, 101) // Límite superior es exclusivo
    val boardSizeLength = boardSideLength ?: Random.nextInt(10, 101)

    file.writeText("$nParticles\n$boardSizeLength\n")
    val particles = mutableListOf<Particle>()

    repeat(nParticles) { id ->
        val radius = particleRadius ?: Random.nextDouble(0.1, 2.0)
        val property = interactionDistance ?: Random.nextDouble(1.0, 10.0)
        file.appendText("$radius $property\n")
        particles.add(Particle(id, radius, property, 0.0, 0.0, 0.0, 0.0))
    }

    return Pair(boardSizeLength, particles)
}

fun generateRandomDynamicConfig(file: File, boardSize: Int, particles: List<Particle>) {
    file.writeText("0.0\n")

    particles.forEach { _ ->
        val x = Random.nextDouble(0.0, boardSize.toDouble())
        val y = Random.nextDouble(0.0, boardSize.toDouble())
        val vx = Random.nextDouble(-1.0, 1.0)
        val vy = Random.nextDouble(-1.0, 1.0)
        file.appendText("$x $y $vx $vy\n")
    }
}
