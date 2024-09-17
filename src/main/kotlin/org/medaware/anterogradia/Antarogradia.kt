package org.medaware.anterogradia

import org.medaware.anterogradia.syntax.Tokenizer

fun main() {
    val input = "@antg instance section { }"
    val tok = Tokenizer(input)
    while (tok.hasNext()) {
        println(tok.nextToken())
    }
}