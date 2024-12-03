package org.medaware.anterogradia.libs

import org.medaware.anterogradia.antgNumber
import org.medaware.anterogradia.exception.AntgRuntimeException
import org.medaware.anterogradia.math.Vector
import org.medaware.anterogradia.math.VectorOperation
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.StateRetention
import org.medaware.anterogradia.runtime.library.VariadicFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(stateRetention = StateRetention.STATEFUL)
class LinearAlgebra(val runtime: Runtime) {

    @DiscreteFunction("about")
    fun about(): String = "Standard Linear Algebra Library\n{C} Medaware, 2024\n"

    @VariadicFunction("v")
    fun v(coords: Array<Node>): String {
        if (coords.isEmpty())
            throw AntgRuntimeException("A vector must have at least 1 component.")

        val arr = coords.map {
            it.evaluate(runtime).antgNumber<Double>()
        }

        return arr.joinToString(separator = "|")
    }

    @VariadicFunction("sum")
    fun sum(vectors: Array<Node>): String {
        val vectors = vectors.map { Vector.parse(it.evaluate(runtime)) }
        return Vector.operation(vectors.toTypedArray(), VectorOperation.SUM).string()
    }

    @VariadicFunction("sub")
    fun sub(vectors: Array<Node>): String {
        val vectors = vectors.map { Vector.parse(it.evaluate(runtime)) }
        return Vector.operation(vectors.toTypedArray(), VectorOperation.SUBTRACT).string()
    }

    @DiscreteFunction("mul", params = ["v", "fac"])
    fun mul(vector: Node, fac: Node): String =
        Vector.parse(vector.evaluate(runtime)).mul(fac.evaluate(runtime).antgNumber<Double>()).string()

    @DiscreteFunction("div", params = ["v", "fac"])
    fun div(vector: Node, fac: Node): String =
        Vector.parse(vector.evaluate(runtime)).div(fac.evaluate(runtime).antgNumber<Double>()).string()

    @DiscreteFunction("len", params = ["vec"])
    fun len(vector: Node): String = Vector.parse(vector.evaluate(runtime)).magnitude().toString()

}