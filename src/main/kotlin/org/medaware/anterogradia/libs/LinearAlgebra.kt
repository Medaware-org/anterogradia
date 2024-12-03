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

    @DiscreteFunction("validate", params = ["str"])
    fun validate(str: Node): String {
        try {
            Vector.parse(str.evaluate(runtime))
        } catch (e: Exception) {
            return "false"
        }
        return "true"
    }

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

    @DiscreteFunction("div", params = ["v", "div"])
    fun div(vector: Node, div: Node): String =
        Vector.parse(vector.evaluate(runtime)).div(div.evaluate(runtime).antgNumber<Double>()).string()

    @DiscreteFunction("normalize", params = ["v"])
    fun normalize(vector: Node): String =
        Vector.parse(vector.evaluate(runtime)).normalize().string()

    @DiscreteFunction("len", params = ["v"])
    fun len(vector: Node): String = Vector.parse(vector.evaluate(runtime)).magnitude().toString()

    @DiscreteFunction("x", params = ["v"])
    fun x(vec: Node): String = Vector.parse(vec.evaluate(runtime)).x().toString()

    @DiscreteFunction("y", params = ["v"])
    fun y(vec: Node): String = Vector.parse(vec.evaluate(runtime)).y().toString()

    @DiscreteFunction("z", params = ["v"])
    fun z(vec: Node): String = Vector.parse(vec.evaluate(runtime)).z().toString()

    @DiscreteFunction("n", params = ["v", "n"])
    fun n(vec: Node, n: Node): String =
        Vector.parse(vec.evaluate(runtime)).n(n.evaluate(runtime).antgNumber<Int>()).toString()

    @DiscreteFunction("dot", params = ["a", "b"])
    fun dot(a: Node, b: Node): String {
        val v1 = Vector.parse(a.evaluate(runtime))
        val v2 = Vector.parse(b.evaluate(runtime))
        return v1.dot(v2).toString()
    }

}