package org.medaware.anterogradia.libs

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.StateRetention
import org.medaware.anterogradia.runtime.library.VariadicFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(stateRetention = StateRetention.STATEFUL)
class Standard(val runtime: Runtime) {

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
        str.evaluate(runtime).repeat(count.evaluate(runtime).toInt())
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

    @DiscreteFunction(identifier = "_if", params = ["cond", "then", "else"])
    fun evalIf(str: Node, then: Node, _else: Node): String {
        val condStr = str.evaluate(runtime)

        if (condStr == "true" || condStr == "yes") {
            return then.evaluate(runtime)
        }

        return _else.evaluate(runtime)
    }

    @DiscreteFunction(identifier = "equal", params = ["a", "b"])
    fun equal(a: Node, b: Node): String = if (a.evaluate(runtime) == b.evaluate(runtime)) "true" else "false"

    @DiscreteFunction(identifier = "param", params = ["key"])
    fun parameter(id: Node): String = runtime.parameters[id.evaluate(runtime)] ?: ""

    @DiscreteFunction(identifier = "set", params = ["key", "value"])
    fun set(key: Node, value: Node): String {
        variableStore[key.evaluate(runtime)] = value.evaluate(runtime)
        return ""
    }

    @DiscreteFunction(identifier = "get", params = ["key"])
    fun set(key: Node): String = variableStore[key.evaluate(runtime)] ?: ""

    @DiscreteFunction(identifier = "astd", params = ["expr"])
    fun astd(n: Node): String = n.dump()

}