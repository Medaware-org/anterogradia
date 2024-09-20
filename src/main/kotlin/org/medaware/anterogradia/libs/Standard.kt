package org.medaware.anterogradia.libs

import org.medaware.anterogradia.hasNonNullEntry
import org.medaware.anterogradia.hasNullEntry
import org.medaware.anterogradia.map
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.StateRetention.STATEFUL
import org.medaware.anterogradia.runtime.library.VariadicFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(STATEFUL)
class Standard(val runtime: Runtime) {

    companion object {
        const val TRUE = "true"
        const val FALSE = "false"
        const val CMP_LEFT = "left"
        const val CMP_RIGHT = "right"
    }

    private val variableStore = hashMapOf<String, String>()

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Anterogradia Standard Library\n{C} Medaware, 2024\n"

    @VariadicFunction(identifier = "sequence")
    fun sequence(params: Array<Node>) = params.joinToString(separator = "") { it.evaluate(runtime) }

    @VariadicFunction(identifier = "progn")
    fun progn(params: Array<Node>): String {
        var last: String = ""
        params.forEach { last = it.evaluate(runtime) }
        return last
    }

    @DiscreteFunction(identifier = "nothing")
    fun nothing(): String = ""

    @DiscreteFunction(identifier = "repeat", params = ["count", "str"])
    fun repeat(count: Node, str: Node) = try {
        List(count.evaluate(runtime).toInt()) { str.evaluate(runtime) }.joinToString(separator = "")
    } catch (e: NumberFormatException) {
        str.evaluate(runtime)
    }

    @DiscreteFunction(identifier = "repeat", params = ["count", "str", "separator"])
    fun repeat(count: Node, str: Node, separator: Node) = try {
        List(count.evaluate(runtime).toInt()) { str.evaluate(runtime) }.joinToString(
            separator = separator.evaluate(
                runtime
            )
        )
    } catch (e: NumberFormatException) {
        str.evaluate(runtime)
    }

    @VariadicFunction(identifier = "random")
    fun random(strings: Array<Node>): String = strings.random().evaluate(runtime)

    @DiscreteFunction(identifier = "param", params = ["key"])
    fun parameter(id: Node): String = runtime.parameters[id.evaluate(runtime)] ?: ""

    @DiscreteFunction(identifier = "set", params = ["key", "value"])
    fun set(key: Node, value: Node): String {
        variableStore[key.evaluate(runtime)] = value.evaluate(runtime)
        return ""
    }

    @DiscreteFunction(identifier = "get", params = ["key"])
    fun get(key: Node): String = variableStore[key.evaluate(runtime)] ?: ""

    //
    // Boolean operations
    //

    @DiscreteFunction(identifier = "_if", params = ["cond", "then", "else"])
    fun evalIf(str: Node, then: Node, _else: Node): String {
        val condStr = str.evaluate(runtime)

        if (condStr == "true" || condStr == "yes") {
            return then.evaluate(runtime)
        }

        return _else.evaluate(runtime)
    }

    @DiscreteFunction(identifier = "equal", params = [CMP_LEFT, CMP_RIGHT])
    fun equal(a: Node, b: Node): String = if (a.evaluate(runtime) == b.evaluate(runtime)) "true" else "false"

    @DiscreteFunction(identifier = "lgt", params = [CMP_LEFT, CMP_RIGHT])
    fun leftGreater(a: Node, b: Node): String {
        val leftStr = a.evaluate(runtime)
        val rightStr = b.evaluate(runtime)

        val integers = (leftStr.toIntOrNull() to rightStr.toIntOrNull())

        // If both operands are integers, perform a numerical comparison
        if (!integers.hasNullEntry())
            return (integers.first!! > integers.second!!).map(TRUE, FALSE)

        // If only one type is an integer, compare to the remaining string's length
        if (integers.hasNullEntry() && integers.hasNonNullEntry()) {
            val left = integers.first ?: leftStr.length
            val right = integers.second ?: rightStr.length
            return (left > right).map(TRUE, FALSE)
        }

        // Otherwise, compare lexically
        return (leftStr > rightStr).map(TRUE, FALSE)
    }

    @DiscreteFunction(identifier = "rgt", params = [CMP_LEFT, CMP_RIGHT])
    fun rightGreater(a: Node, b: Node): String {
        val leftStr = a.evaluate(runtime)
        val rightStr = b.evaluate(runtime)

        val integers = (leftStr.toIntOrNull() to rightStr.toIntOrNull())

        // If both operands are integers, perform a numerical comparison
        if (!integers.hasNullEntry())
            return (integers.first!! < integers.second!!).map(TRUE, FALSE)

        // If only one type is an integer, compare to the remaining string's length
        if (integers.hasNullEntry() && integers.hasNonNullEntry()) {
            val left = integers.first ?: leftStr.length
            val right = integers.second ?: rightStr.length
            return (left < right).map(TRUE, FALSE)
        }

        // Otherwise, compare lexically
        return (leftStr < rightStr).map(TRUE, FALSE)
    }

    @DiscreteFunction(identifier = "len", params = ["expr"])
    fun length(expr: Node): String = expr.evaluate(runtime).length.toString()

    @DiscreteFunction(identifier = "astd", params = ["expr"])
    fun astd(n: Node): String = n.dump()

}