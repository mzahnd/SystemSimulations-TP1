package ar.edu.itba.ss

import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class Particle(
    val id: Int,
    val radius: Double,
    val properties: Double,
    val x: Double,
    val y: Double,
    val vx: Double,
    val vy: Double
) : Comparable<Particle> {
    val neighbours: MutableSet<Particle> = TreeSet<Particle>()
    var cell: Cell? = null

    fun distance(particle: Particle, settings: Settings): Double =
        if (settings.periodicContour) {
            val straightDx = abs(x - particle.x)
            val straightDy = abs(y - particle.y)

            val dx =
                if (straightDx > settings.boardSizeLength / 2) settings.boardSizeLength - straightDx else straightDx
            val dy =
                if (straightDy > settings.boardSizeLength / 2) settings.boardSizeLength - straightDy else straightDy

            sqrt(dx.pow(2) + dy.pow(2))
        } else {
            sqrt((this.x - particle.x).pow(2) + (this.y - particle.y).pow(2)) - this.radius - particle.radius
        }

    override fun compareTo(other: Particle): Int = compareValuesBy(this, other) { it.id }

    override fun toString(): String {
        return "Particle{id=$id, radius=$radius, properties=$properties, x=$x, y=$y, vx=$vx, vy=$vy}"
    }
}
