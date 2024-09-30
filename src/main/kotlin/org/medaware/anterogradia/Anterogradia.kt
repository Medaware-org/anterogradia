package org.medaware.anterogradia

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.parser.Parser
import java.text.SimpleDateFormat
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

    private var _enableLogging = true

    var enableLogging: Boolean
        get() = _enableLogging
        set(v) {
            _enableLogging = v
            if (!v)
                logger.level = Level.OFF
            else
                logger.level = Level.ALL
        }

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
        _parameters: HashMap<String, String>? = null,
        antgRuntime: Runtime? = null
    ): AnterogradiaResult {
        val parameters = if (_parameters != null) _parameters else hashMapOf<String, String>()

        var result: String
        var except: Exception? = null
        var dump = ""

        val runtime = antgRuntime ?: Runtime(parameters)

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