package ar.edu.itba.ss

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class Cli : CliktCommand() {
    private val logger = KotlinLogging.logger {}

    private val staticConfigurationFile: File? by option().file()
        .help("Path to the static configuration file.")
    private val dynamicConfigurationFile: File? by option().file()
        .help("Path to the dynamic configuration file.")
    private val outputDirectory: Path? by option().path(
        canBeFile = false,
        canBeDir = true,
        mustExist = true,
        mustBeReadable = true
    ).help("Path to the output directory.")
    private val matrixSize: Int? by option().int()
        .help("Size of one side of the squared matrix used by the Cell Index Method algorithm. Ignored by Brute Force algorithm")
    private val numberOfParticles: Int? by option().int()
        .help("Total number of particles.")
    private val boardSideLength: Int? by option().int().default(20)
        .help("Square side length.")
    private val particleRadius: Double? by option().double().default(0.25)
        .help("Radius of particles.")
    private val algorithm: Algorithm by option().enum<Algorithm>().default(Algorithm.CELL_INDEX_METHOD)
        .help("[Optional] Algorithm implementation to run. Defaults to CELL_INDEX_METHOD.")
    private val periodicContour: Boolean by option().boolean().default(false)
        .help("[Optional] Should use periodic contour. Defaults to false.")
    private val interactionRadius: Double by option().double().default(1.0)
        .help("[Optional] Interaction radius between particles. Defaults to 1.0")
    private val generateRandom: Boolean by option("--generate-random").flag()
        .help("Generate random static and dynamic configuration files.")

    override fun run() {
        var boardSideLength: Int? = boardSideLength
        val numberOfParticles: Int? = numberOfParticles
        val interactionRadius: Double = interactionRadius
        val particleRadius: Double? = particleRadius
        val time: Double
        var particles: List<Particle>

        requireNotNull(matrixSize) { IllegalArgumentException("matrix-size must be defined") }
        requireNotNull(outputDirectory) { IllegalArgumentException("output-directory must be defined") }

        val staticFile = staticConfigurationFile ?: File("static_config.txt")
        val dynamicFile = dynamicConfigurationFile ?: File("dynamic_config.txt")

        val (boardSize, particlesList) = if (generateRandom) {
            val generatedStatic = generateRandomStaticConfig(
                staticFile,
                numberOfParticles,
                boardSideLength,
                particleRadius,
                interactionRadius
            )
            generateRandomDynamicConfig(dynamicFile, generatedStatic.first, generatedStatic.second)
            generatedStatic
        } else {
            requireNotNull(staticConfigurationFile) { IllegalArgumentException("static-configuration-file must be defined unless --generate-random is used") }
            requireNotNull(dynamicConfigurationFile) { IllegalArgumentException("dynamic-configuration-file must be defined unless --generate-random is used") }
            parseStaticConfigurationFile(staticFile)
        }

        boardSideLength = boardSize
        particles = particlesList

        val dynamicParsed = parseDynamicConfigurationFile(dynamicFile, particles)
        time = dynamicParsed.first
        particles = dynamicParsed.second

        val settings = Settings(
            algorithm = algorithm,
            particles = particles,
            matrixSize = matrixSize!!,
            rc = interactionRadius,
            periodicContour = periodicContour,
            boardSizeLength = boardSideLength,
            time = time
        )

        logger.debug { settings.toString() }

        val startTime = System.currentTimeMillis()
        when (settings.algorithm) {
            Algorithm.CELL_INDEX_METHOD -> cellIndexMethod(settings)
            Algorithm.BRUTE_FORCE -> bruteForce(settings)
        }
        val endTime = System.currentTimeMillis()

        val elapsedTime = endTime - startTime

        logger.debug { settings.particles }
        logger.info { "${settings.algorithm} | ${settings.particles.size} | $elapsedTime ms" }

        writeOutputFile(outputDirectory!!, settings)
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

    private fun writeOutputFile(outputDir: Path, settings: Settings) {
        val dirName =
            "${settings.algorithm}-particles=${settings.particles.size}-M=${settings.matrixSize}-rc=${settings.rc}-periodic=${settings.periodicContour}-L=${settings.boardSizeLength}"
        if (!File(outputDir.resolve(dirName).toAbsolutePath().toString()).exists()) {
            File(outputDir.resolve(dirName).toAbsolutePath().toString()).mkdir()
        }
        val fileName =
            //"${settings.algorithm}-ts=${System.currentTimeMillis()}-particles=${settings.particles.size}-M=${settings.matrixSize}-rc=${settings.rc}-periodic=${settings.periodicContour}-L=${settings.boardSizeLength}.txt"
            // Version without ts, easier to pipe with python
            "${settings.algorithm}-particles=${settings.particles.size}-M=${settings.matrixSize}-rc=${settings.rc}-periodic=${settings.periodicContour}-L=${settings.boardSizeLength}.txt"
        val file = outputDir.resolve(dirName).resolve(fileName).toFile()

        settings.particles.forEach { particle ->
            val neighbours = particle.neighbours.joinToString(" ") { "${it.id}" }
            file.appendText("${particle.id} $neighbours\n")
        }
    }
}