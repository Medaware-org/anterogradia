package org.medaware.anterogradia.syntax

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.tokenizer.Token

data class Script(val libs: List<String>, val expression: Node)

sealed class Node {
    abstract fun evaluate(runtime: Runtime): String
}

data class FunctionCall(
    val prefix: String,
    val identifier: Token,
    val arguments: HashMap<String, Node>,
    val variadic: Boolean = false
) : Node() {
    override fun evaluate(runtime: Runtime): String = runtime.callFunction(this)
}

data class StringLiteral(val value: String) : Node() {
    override fun evaluate(runtime: Runtime): String = value
}
