package org.medaware.anterogradia.libs

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.Function
import org.medaware.anterogradia.runtime.library.VariadicFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary
class Standard(val runtime: Runtime) {

    @VariadicFunction(identifier = "sequence")
    fun sequence(params: Array<Node>) = params.joinToString(separator = "") { it.evaluate(runtime) }

    @VariadicFunction(identifier = "progn")
    fun progn(params: Array<Node>): String {
        var last: String = ""
        params.forEach { last = it.evaluate(runtime) }
        return last
    }

    @Function(identifier = "nothing")
    fun nothing(): String = ""

    @Function(identifier = "repeat", params = ["count", "str"])
    fun repeat(count: Node, str: Node) = try {
        str.evaluate(runtime).repeat(count.evaluate(runtime).toInt())
    } catch (e: NumberFormatException) {
        str.evaluate(runtime)
    }

    @Function(identifier = "repeat", params = ["count", "str", "separator"])
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

}