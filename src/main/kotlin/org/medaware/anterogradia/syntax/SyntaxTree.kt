package org.medaware.anterogradia.syntax

import org.medaware.anterogradia.runtime.Runtime

data class Script(val libs: List<Pair<String, String>>, val expression: Node)

sealed class Node(open val line: Int) {
    abstract fun evaluate(runtime: Runtime): String
    abstract fun dump(): String
}

data class FunctionCall(
    val prefix: String,
    val identifier: String,
    val arguments: HashMap<String, Node>,
    override val line: Int,
    val variadic: Boolean = false
) : Node(line) {
    override fun evaluate(runtime: Runtime): String = runtime.callFunction(this)
    override fun dump(): String {
        val brackets = if (variadic) "{" to "}" else "(" to ")"
        val strPrefix = if (prefix.isEmpty()) "" else "$prefix."
        val builder = StringBuilder("$strPrefix$identifier${brackets.first}")

        if (!variadic) {
            arguments.forEach { arg ->
                builder.append("${arg.key}=${arg.value.dump()},")
            }
        } else {
            arguments.map { it.key.toInt() }.sorted().forEach {
                builder.append("${arguments[it.toString()]!!.dump()},")
            }
        }

        if (arguments.isNotEmpty())
            builder.deleteCharAt(builder.length - 1) // Remove trailing ','

        builder.append(brackets.second)

        return builder.toString()
    }
}

data class StringLiteral(val value: String, override val line: Int) : Node(line) {
    override fun evaluate(runtime: Runtime): String = value
    override fun dump(): String = "\"$value\""
}
