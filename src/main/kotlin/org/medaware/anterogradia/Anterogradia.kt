package org.medaware.anterogradia

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.parser.Parser
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

data class AnterogradiaResult(val input: String, val output: String, val exception: Exception? = null) {
    fun <T> use(lambda: (input: String, output: String, except: Exception?) -> T) = lambda(input, output, exception)
}

object Anterogradia {
    val logger = Logger.getLogger("Anterogradia")

    init {
        val handler = ConsoleHandler()

        handler.formatter = object : SimpleFormatter() {
            override fun format(record: LogRecord): String {
                return "[${SimpleDateFormat("yyyy-hh-mm-ss").format(Date(record.millis))}] ${record.message}\n"
            }
        }

        logger.addHandler(handler)
        logger.useParentHandlers = false
    }

    fun invokeCompiler(input: String, runtime: Runtime = Runtime()): AnterogradiaResult {
        var result: String
        var except: Exception? = null

        try {
            result = Parser
                .parseScript(input)
                .evaluate(runtime)
        } catch (e: Exception) {
            except = e
            result = ""
        }

        return AnterogradiaResult(input, result, except)
    }
}

// Just for testing purposes
fun main() {
    val input = Files.readString(Path.of("concept.antg"))

    Anterogradia.invokeCompiler(input).use { input, output, except ->
        println(output)
    }

}