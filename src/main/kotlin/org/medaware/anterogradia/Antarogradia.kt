package org.medaware.anterogradia

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.parser.Parser
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Logger

fun main() {
    val runtime = Runtime()
    val result: String
    try {
        result = Parser
            .parseScript(Files.readString(Path.of("concept.antg")))
            .evaluate(runtime)
    } catch (e: Exception) {
        Logger.getLogger("Master").info("${e::class.java.simpleName}: ${e.message}")
        return
    }
    println(result)
}