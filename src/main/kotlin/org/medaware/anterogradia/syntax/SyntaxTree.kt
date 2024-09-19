package org.medaware.anterogradia.syntax

import org.medaware.anterogradia.runtime.Runtime

data class Script(val libs: List<String>, val expression: Node)

sealed class Node {
    abstract fun evaluate(runtime: Runtime): String
    abstract fun dump(): String
}

data class FunctionCall(
    val prefix: String,
    val identifier: String,
    val arguments: HashMap<String, Node>,
    val variadic: Boolean = false
) : Node() {
    override fun evaluate(runtime: Runtime): String = runtime.callFunction(this)
    override fun dump(): String {
        val brackets = if (variadic) "{" to "}" else "(" to ")"
        val strPrefix = if (prefix.isEmpty()) "" else "$prefix:"
        val builder = StringBuilder("$strPrefix$identifier${brackets.first}")

        arguments.forEach { arg ->
            if (!variadic)
                builder.append("${arg.key}=${arg.value.dump()},")
            else
                builder.append("${arg.value.dump()},")
        }

        builder.deleteCharAt(builder.length - 1) // Remove trailing ','
        builder.append(brackets.second)

        return builder.toString()
    }
}

data class StringLiteral(val value: String) : Node() {
    override fun evaluate(runtime: Runtime): String = value
    override fun dump(): String = "\"$value\""
}
