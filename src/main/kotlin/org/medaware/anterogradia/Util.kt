package org.medaware.anterogradia

import org.medaware.anterogradia.exception.AntgRuntimeException
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

inline fun <reified T> String.antgNumber(): T {
    if (T::class != Double::class && T::class != Int::class)
        throw AntgRuntimeException("The type '${T::class.java.simpleName}' is not a valid conversion type for a number")

    val double = this.toDouble()

    if (T::class == Int::class && (double % 1 != 0.0))
        throw AntgRuntimeException("Expected number to be of an effective integer type, got double ($double)")

    return when (T::class) {
        Double::class -> double
        Int::class -> double.toInt()
        else -> throw AntgRuntimeException("") // Won't happen
    } as T
}

inline fun <reified T> String.antgNumberOrNull(): T? {
    return try {
        antgNumber<T>()
    } catch (e: Exception) {
        null
    }
}

fun Throwable.rootCause(): Throwable {
    var cause = this
    while (cause.cause != null) {
        cause = cause.cause!!
    }
    return cause
}

fun randomString(): String {
    val chars = ('A'..'Z') + ('a'..'z')
    return CharArray(128) { chars.random() }.concatToString()
}
