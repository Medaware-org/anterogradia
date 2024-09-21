package org.medaware.anterogradia

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.Node

fun String.padIfNotEmpty(): String {
    if (isEmpty())
        return ""
    return " $this"
}

fun Array<Node>.evalToString(runtime: Runtime) = map { it.evaluate(runtime) }.joinToString(separator = "")

fun <A, B> Pair<A, B>.hasNullEntry() = this.first == null || this.second == null

fun <A, B> Pair<A, B>.hasNonNullEntry() = this.first != null || this.second != null

fun <T> Boolean.map(`if`: T, `else`: T) = if (this) `if` else `else`

fun Pair<Double, Double>.min() = if (this.first < this.second) this.first else this.second

fun Pair<Double, Double>.max() = if (this.first > this.second) this.first else this.second

fun Throwable.rootCause(): Throwable {
    var cause = this
    while (cause.cause != null) {
        cause = cause.cause!!
    }
    return cause
}
