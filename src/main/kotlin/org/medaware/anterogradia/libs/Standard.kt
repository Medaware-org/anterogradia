package org.medaware.anterogradia.libs

import org.medaware.anterogradia.*
import org.medaware.anterogradia.exception.AntgRuntimeException
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.StateRetention.STATEFUL
import org.medaware.anterogradia.runtime.library.VariadicFunction
import org.medaware.anterogradia.syntax.Node
import org.medaware.anterogradia.syntax.StringLiteral
import kotlin.math.pow

@AnterogradiaLibrary(STATEFUL)
class Standard(val runtime: Runtime) {

    companion object {
        const val TRUE = "true"
        const val FALSE = "false"
        const val CMP_LEFT = "left"
        const val CMP_RIGHT = "right"
    }

    private val variableStore = hashMapOf<String, String>()
    private val functionStore = hashMapOf<String, Node>()

    private val validatorValueId = randomString()

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Anterogradia Standard Library\n{C} Medaware, 2024\n"

    @VariadicFunction(identifier = "sequence")
    fun sequence(params: Array<Node>) = params.joinToString(separator = "") { it.evaluate(runtime) }

    @VariadicFunction(identifier = "progn")
    fun progn(params: Array<Node>): String {
        var last: String = ""
        params.forEach { last = it.evaluate(runtime) }
        return last
    }

    @VariadicFunction(identifier = "omit")
    fun omit(params: Array<Node>): String {
        params.forEach { it.evaluate(runtime) }
        return ""
    }

    @DiscreteFunction(identifier = "nothing")
    fun nothing(): String = ""

    @DiscreteFunction(identifier = "repeat", params = ["count", "str"])
    fun repeat(count: Node, str: Node) = try {
        List(count.evaluate(runtime).antgNumber<Int>()) { str.evaluate(runtime) }.joinToString(separator = "")
    } catch (e: NumberFormatException) {
        str.evaluate(runtime)
    }

    @DiscreteFunction(identifier = "repeat", params = ["count", "str", "separator"])
    fun repeat(count: Node, str: Node, separator: Node) = try {
        List(count.evaluate(runtime).antgNumber<Int>()) { str.evaluate(runtime) }.joinToString(
            separator = separator.evaluate(
                runtime
            )
        )
    } catch (e: NumberFormatException) {
        str.evaluate(runtime)
    }

    @VariadicFunction(identifier = "random")
    fun random(strings: Array<Node>): String = strings.random().evaluate(runtime)

    @DiscreteFunction(identifier = "param", params = ["key"])
    fun param(id: Node): String = runtime.parameters[id.evaluate(runtime)] ?: ""

    @DiscreteFunction(identifier = "set", params = ["key", "value"])
    fun set(key: Node, value: Node): String {
        variableStore[key.evaluate(runtime)] = value.evaluate(runtime)
        return ""
    }

    @DiscreteFunction(identifier = "get", params = ["key"])
    fun get(key: Node): String = variableStore[key.evaluate(runtime)] ?: ""

    @DiscreteFunction(identifier = "_if", params = ["cond", "then", "else"])
    fun _if(str: Node, then: Node, _else: Node): String {
        val condStr = str.evaluate(runtime)

        if (condStr == "true" || condStr == "yes") {
            return then.evaluate(runtime)
        }

        return _else.evaluate(runtime)
    }

    @DiscreteFunction(identifier = "equal", params = [CMP_LEFT, CMP_RIGHT])
    fun equal(left: Node, right: Node): String = if (left.evaluate(runtime) == right.evaluate(runtime)) TRUE else FALSE

    @DiscreteFunction(identifier = "lgt", params = [CMP_LEFT, CMP_RIGHT])
    fun lgt(a: Node, b: Node): String {
        val leftStr = a.evaluate(runtime)
        val rightStr = b.evaluate(runtime)

        val integers = (leftStr.antgNumberOrNull<Int>() to rightStr.antgNumberOrNull<Int>())

        // If both operands are integers, perform a numerical comparison
        if (!integers.hasNullEntry())
            return (integers.first!! > integers.second!!).map(TRUE, FALSE)

        // If only one type is an integer, compare to the remaining string's length
        if (integers.hasNullEntry() && integers.hasNonNullEntry()) {
            val left = integers.first ?: leftStr.length
            val right = integers.second ?: rightStr.length
            return (left > right).map(TRUE, FALSE)
        }

        // Otherwise, compare lexically
        return (leftStr > rightStr).map(TRUE, FALSE)
    }

    @DiscreteFunction(identifier = "rgt", params = [CMP_LEFT, CMP_RIGHT])
    fun rgt(a: Node, b: Node): String {
        val leftStr = a.evaluate(runtime)
        val rightStr = b.evaluate(runtime)

        val integers = (leftStr.antgNumberOrNull<Int>() to rightStr.antgNumberOrNull<Int>())

        // If both operands are integers, perform a numerical comparison
        if (!integers.hasNullEntry())
            return (integers.first!! < integers.second!!).map(TRUE, FALSE)

        // If only one type is an integer, compare to the remaining string's length
        if (integers.hasNullEntry() && integers.hasNonNullEntry()) {
            val left = integers.first ?: leftStr.length
            val right = integers.second ?: rightStr.length
            return (left < right).map(TRUE, FALSE)
        }

        // Otherwise, compare lexically
        return (leftStr < rightStr).map(TRUE, FALSE)
    }

    @DiscreteFunction(identifier = "len", params = ["expr"])
    fun len(expr: Node): String = expr.evaluate(runtime).length.toString()

    @DiscreteFunction(identifier = "astd", params = ["expr"])
    fun astd(n: Node): String = n.dump()

    @DiscreteFunction(identifier = "_fun", params = ["id", "expr"])
    fun _fun(name: Node, expr: Node): String {
        val id = name.evaluate(runtime)
        functionStore[id] = expr
        return id
    }

    @DiscreteFunction(identifier = "_eval", params = ["id"])
    fun _eval(id: Node): String {
        val stored = functionStore[id.evaluate(runtime)]
        if (stored == null)
            throw AntgRuntimeException("Could not find stored function '${id.evaluate(runtime)}'.")
        return stored.evaluate(runtime)
    }

    @DiscreteFunction(identifier = "__require_prop", params = ["id", "err"])
    fun __require_prop(id: Node, err: Node): String {
        if (variableStore.containsKey(id.evaluate(runtime)))
            return ""

        throw AntgRuntimeException(err.evaluate(runtime))
    }

    @DiscreteFunction(identifier = "compile", params = ["source"])
    fun compile(source: Node): String = Anterogradia.invokeCompiler(source.evaluate(runtime), antgRuntime = runtime)
        .use { input, output, except, dump ->
            if (except != null)
                throw AntgRuntimeException("Error occurred while evaluating compile expression: ${except.rootCause().message}")

            return@use output
        }

    @DiscreteFunction(identifier = "add", params = ["left", "right"])
    fun add(left: Node, right: Node): String =
        (left.evaluate(runtime).antgNumber<Double>() + right.evaluate(runtime).antgNumber<Double>()).toString()

    @DiscreteFunction(identifier = "sub", params = ["left", "right"])
    fun sub(left: Node, right: Node): String =
        (left.evaluate(runtime).antgNumber<Double>() - right.evaluate(runtime).antgNumber<Double>()).toString()

    @DiscreteFunction(identifier = "mul", params = ["left", "right"])
    fun mul(left: Node, right: Node): String =
        (left.evaluate(runtime).antgNumber<Double>() * right.evaluate(runtime).antgNumber<Double>()).toString()

    @DiscreteFunction(identifier = "div", params = ["left", "right"])
    fun div(left: Node, right: Node): String =
        (left.evaluate(runtime).antgNumber<Double>() / right.evaluate(runtime).antgNumber<Double>()).toString()

    @DiscreteFunction(identifier = "mod", params = ["left", "right"])
    fun mod(left: Node, right: Node): String =
        (left.evaluate(runtime).antgNumber<Double>() % right.evaluate(runtime).antgNumber<Double>()).toString()

    @DiscreteFunction(identifier = "sqrt", params = ["expr"])
    fun sqrt(expr: Node): String = kotlin.math.sqrt(expr.evaluate(runtime).antgNumber<Double>()).toString()

    @DiscreteFunction(identifier = "pow", params = ["expr", "pow"])
    fun pow(expr: Node, pow: Node) =
        expr.evaluate(runtime).antgNumber<Double>().pow(pow.evaluate(runtime).antgNumber<Double>()).toString()

    @DiscreteFunction(identifier = "vsignflp", params = ["id"])
    fun vsignflp(id: Node): String {
        val key = id.evaluate(runtime)
        if (variableStore[key] == null)
            return "0.0"
        variableStore[key] = (variableStore[key]!!.antgNumber<Double>() * (-1.0)).toString()
        return variableStore[key]!!
    }

    @DiscreteFunction(identifier = "signflp", params = ["expr"])
    fun signflp(expr: Node): String = (expr.evaluate(runtime).antgNumber<Double>() * (-1.0)).toString()

    @DiscreteFunction(identifier = "increment", params = ["id"])
    fun increment(id: Node): String {
        val key = id.evaluate(runtime)
        if (variableStore[key] == null)
            return "0.0"
        variableStore[key] = (variableStore[key]!!.antgNumber<Double>() + 1.0).toString()
        return variableStore[key]!!
    }

    @DiscreteFunction(identifier = "decrement", params = ["id"])
    fun decrement(id: Node): String {
        val key = id.evaluate(runtime)
        if (variableStore[key] == null)
            return "0.0"
        variableStore[key] = (variableStore[key]!!.antgNumber<Double>() - 1.0).toString()
        return variableStore[key]!!
    }

    @DiscreteFunction(identifier = "_while", params = ["cond", "expr"])
    fun _while(cond: Node, expr: Node): String {
        var last = ""
        while (cond.evaluate(runtime).lowercase() == TRUE.lowercase()) {
            last = expr.evaluate(runtime)
        }
        return last
    }

    @DiscreteFunction(identifier = "not", params = ["cond"])
    fun not(cond: Node): String {
        if (cond.evaluate(runtime).lowercase() == TRUE.lowercase())
            return FALSE
        return TRUE
    }

    @DiscreteFunction(identifier = "_debug", params = ["str"])
    fun _debug(str: Node): String {
        Anterogradia.logger.info(str.evaluate(runtime))
        return ""
    }

    @DiscreteFunction(identifier = "trunc", params = ["expr"])
    fun trunc(expr: Node): String = expr.evaluate(runtime).antgNumberOrNull<Double>().let { it ->
        if (it != null)
            return@let it.toInt()
        return ""
    }.toString()

    @DiscreteFunction(identifier = "__validate", params = ["type", "value"])
    fun __validate(type: Node, value: Node): String {
        val typeStr = type.evaluate(runtime)
        val valueStr = value.evaluate(runtime)
        if (runtime.typeValidate(typeStr, valueStr))
            return "true"
        throw AntgRuntimeException("The value \"$valueStr\" does not conform to the norms of type \"$typeStr\".")
    }

    @DiscreteFunction(identifier = "__validator_value")
    fun __validator_value(): String {
        return get(StringLiteral(validatorValueId))
    }

    @DiscreteFunction(identifier = "__register_validator", params = ["type", "validator"])
    fun __register_validator(type: Node, validator: Node): String {
        val typeStr = type.evaluate(runtime)
        val validatorStr = validator.evaluate(runtime)
        if (functionStore[validatorStr] == null)
            throw AntgRuntimeException("The requested validator function '$validatorStr' does not exist.")
        runtime.registerValidator(typeStr) { input ->
            val previousValue = get(StringLiteral(validatorValueId))
            set(StringLiteral(validatorValueId), StringLiteral(input))
            val status = _eval(validator) == "true"
            set(StringLiteral(validatorValueId), StringLiteral(previousValue))
            status
        }
        return "true"
    }

}