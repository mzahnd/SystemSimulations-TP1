package ar.edu.itba.ss

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

class Cli : CliktCommand() {
    private val logger = KotlinLogging.logger {}

    private val staticConfigurationFile: File? by option().file().help("Path to the static configuration file.")
    private val dynamicConfigurationFile: File? by option().file().help("Path to the dynamic configuration file.")
    private val matrixSize: Int? by option().int().help("Size of one side of the squared matrix used by the Cell Index Method algorithm. Ignored by Brute Force algorithm")
    private val algorithm: Algorithm by option().enum<Algorithm>().default(Algorithm.CELL_INDEX_METHOD).help("[Optional] Algorithm implementation to run. Defaults to CELL_INDEX_METHOD.")
    private val periodicContour: Boolean by option().boolean().default(false).help("[Optional] Should use periodic contour. Defaults to false.")
    private val interactionRadius: Double by option().double().default(1.0).help("[Optional] Interaction radius between particles. Defaults to 1.0")

    override fun run() {
        val boardSizeLength: Int
        val time: Double
        var particles: List<Particle>

        requireNotNull(matrixSize) { IllegalArgumentException("matrix-size must be defined") }
        requireNotNull(staticConfigurationFile) { IllegalArgumentException("static-configuration-file can not be null") }
        requireNotNull(dynamicConfigurationFile) { IllegalArgumentException("dynamic-configuration-file can not be null") }

        val staticParsed = parseStaticConfigurationFile(staticConfigurationFile!!)
        boardSizeLength = staticParsed.first
        particles = staticParsed.second

        val dynamicParsed = parseDynamicConfigurationFile(dynamicConfigurationFile!!, particles)
        time = dynamicParsed.first
        particles = dynamicParsed.second


        val settings = Settings(
            algorithm = algorithm,
            particles = particles,
            matrixSize = matrixSize!!,
            rc = interactionRadius,
            periodicContour = periodicContour,
            boardSizeLength = boardSizeLength,
            time = time
        )

        logger.debug { settings.toString() }

        val startTime = System.currentTimeMillis()
        when (settings.algorithm) {
            Algorithm.CELL_INDEX_METHOD -> cellIndexMethod(settings)
            Algorithm.BRUTE_FORCE -> bruteForce(settings)
        }
        val endTime = System.currentTimeMillis()

        val elapsedTime = endTime - startTime;

        logger.info { "${settings.algorithm} | ${settings.particles.size} | $elapsedTime ms" }
        logger.info { settings.particles }
    }

    private fun parseStaticConfigurationFile(configurationFile: File): Pair<Int, List<Particle>> {
        val nParticles = configurationFile.useLines { it.elementAtOrNull(0)?.trim()?.toInt() ?: 0 }
        val boardSizeLength = configurationFile.useLines { it.elementAtOrNull(0)?.trim()?.toInt() ?: 0 }

        val particles = mutableListOf<Particle>()
        for (idx in 0 until nParticles) {
            val p = configurationFile.useLines { it.elementAtOrNull(idx + 2) }
            p?.let {
                val (r, props) = p.trim().split("\\s+".toRegex())
                particles.add(
                    Particle(
                        id = idx,
                        radius = r.toDouble(),
                        properties = props.toDouble(),
                        0.0,
                        0.0,
                        0.0,
                        0.0
                    )
                )
            }
        }

        if (nParticles != particles.size) {
            logger.warn { "Configuration file states that there are $nParticles particles, but ${particles.size} where found." }
        }

        return Pair<Int, List<Particle>>(boardSizeLength, particles)
    }

    private fun parseDynamicConfigurationFile(
        configurationFile: File,
        particles: List<Particle>
    ): Pair<Double, List<Particle>> {
        val time = configurationFile.useLines { it.elementAtOrNull(0)?.toDouble() ?: 0.0 }

        val updatedParticles = mutableListOf<Particle>()
        configurationFile.useLines {
            it.drop(1).forEachIndexed { idx, lineRead ->
                val (x, y, vx, vy) = lineRead.trim()
                    .split("\\s+".toRegex())
                    // Read files with and without velocity
                    .let { line -> if (line.size > 2) line else line + listOf("0.0", "0.0") }

                updatedParticles.add(
                    Particle(
                        particles[idx].id, particles[idx].radius, particles[idx].properties,
                        x = x.toDouble(),
                        y = y.toDouble(),
                        vx = vx.toDouble(),
                        vy = vy.toDouble()
                    )
                )
            }
        }

        return Pair(time, updatedParticles)
    }
}