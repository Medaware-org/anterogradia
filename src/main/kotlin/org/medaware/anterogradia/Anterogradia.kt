package org.medaware.anterogradia

import org.medaware.anterogradia.exception.AntgRuntimeException
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.parser.Parser
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

data class AnterogradiaResult(
    val input: String,
    val output: String,
    val exception: Exception? = null,
    val dump: String
) {
    fun <T> use(lambda: (input: String, output: String, except: Exception?, dump: String) -> T) =
        lambda(input, output, exception, dump)
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

    fun invokeCompiler(
        input: String,
        parameters: HashMap<String, String>? = null,
        antgRuntime: Runtime? = null
    ): AnterogradiaResult {
        if (parameters != null && antgRuntime != null || parameters == null && antgRuntime == null)
            throw AntgRuntimeException("Either the runtime parameter or the runtime itself has to be provided.")

        var result: String
        var except: Exception? = null
        var dump = ""

        val runtime = antgRuntime ?: Runtime(parameters!!)

        try {
            val script = Parser.parseScript(input)
            dump = script.expression.dump()
            script.libs.forEach { lib ->
                runtime.loadLibrary(lib.first, lib.second)
            }
            result = script.expression.evaluate(runtime)
        } catch (e: Exception) {
            except = e
            result = ""
        }

        return AnterogradiaResult(input, result, except, dump)
    }
}

// Just for testing purposes
fun main() {
    val input = Files.readString(Path.of("concept.antg"))

    Anterogradia.invokeCompiler(input, parameters = hashMapOf("time" to Instant.now().toString()))
        .use { input, output, except, dump ->
            if (except != null) {
                val cause = except.rootCause()
                Anterogradia.logger.log(Level.SEVERE, "${cause.javaClass.simpleName}: ${cause.message}")
            } else {
                println(output)
            }
        }

}