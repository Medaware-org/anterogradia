package org.medaware.anterogradia

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.Node

fun String.padIfNotEmpty(): String {
    if (isEmpty())
        return ""
    return " $this"
}

fun Array<Node>.evalToString(runtime: Runtime) = map { it.evaluate(runtime) }.joinToString(separator = "")
