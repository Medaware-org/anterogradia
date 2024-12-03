package org.medaware.anterogradia.math

import org.medaware.anterogradia.antgNumber
import org.medaware.anterogradia.exception.AntgRuntimeException
import kotlin.math.sqrt

enum class VectorOperation {
    SUM,
    SUBTRACT
}

class Vector(val dimensions: Array<Double>) {

    companion object {
        fun parse(str: String): Vector {
            val components = str.split("|").map { it -> it.antgNumber<Double>() }
            return Vector(components.toTypedArray())
        }

        fun operation(vectors: Array<Vector>, operation: VectorOperation): Vector {
            if (vectors.isEmpty())
                throw AntgRuntimeException("Unable to sum 0 vectors.")

            if (vectors.size == 1)
                return vectors[0]

            var lastSize = vectors[0].dimensions.size
            var sum = vectors[0].dimensions
            for (i in 1 until vectors.size) {
                val vec = vectors[i]
                if (vec.dimensions.size != lastSize)
                    throw AntgRuntimeException("Unable to sum vectors of differing dimensions.")

                lastSize = vec.dimensions.size
                vec.dimensions.forEachIndexed { i, dim ->
                    if (operation == VectorOperation.SUM)
                        sum[i] += dim
                    else
                        sum[i] -= dim
                }
            }

            return Vector(sum)
        }

    }

    fun magnitude(): Double {
        var magnitudeSquared = 0.0
        dimensions.forEachIndexed { i, dimension ->
            magnitudeSquared += dimension * dimension
        }
        return sqrt(magnitudeSquared)
    }

    fun mul(d: Double): Vector {
        for (i in 0 until dimensions.size)
            dimensions[i] *= d;

        return this
    }

    fun div(d: Double): Vector {
        if (d == 0.0)
            return this

        for (i in 0 until dimensions.size)
            dimensions[i] /= d;

        return this
    }

    fun string(): String = dimensions.joinToString("|")

}