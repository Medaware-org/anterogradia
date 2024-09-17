package org.medaware.anterogradia

fun String.padIfNotEmpty(): String {
    if (isEmpty())
        return ""
    return " $this"
}
