package org.medaware.anterogradia.libs

import org.medaware.anterogradia.antgNumber
import org.medaware.anterogradia.exception.AntgRuntimeException
import org.medaware.anterogradia.map
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.StateRetention.STATEFUL
import org.medaware.anterogradia.syntax.Node
import org.medaware.anterogradia.syntax.StringLiteral
import kotlin.String

@AnterogradiaLibrary(STATEFUL)
class Strings(val runtime: Runtime) {

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Anterogradia String Library\n{C} 2024 Medaware\n"

    @DiscreteFunction(identifier = "contains", params = ["str", "substr"])
    fun contains(str: Node, substr: Node): String =
        str.evaluate(runtime).contains(substr.evaluate(runtime)).map(Standard.TRUE, Standard.FALSE)

    @DiscreteFunction(identifier = "at", params = ["str", "index"])
    fun at(str: Node, index: Node): String {
        val ix = index.evaluate(runtime).antgNumber<Int>()
        val strStr = str.evaluate(runtime)
        if (ix < 0 || ix >= strStr.length)
            return ""
        return strStr[ix].toString()
    }

    @DiscreteFunction(identifier = "at", params = ["str", "index", "insert"])
    fun at(str: Node, index: Node, insert: Node): String {
        val ix = index.evaluate(runtime).antgNumber<Int>()
        val strStr = str.evaluate(runtime)

        if (ix < 0 || ix >= strStr.length)
            return ""

        val substr = insert.evaluate(runtime)

        if (substr.isEmpty())
            return strStr

        return strStr.substring(0, ix) + substr + strStr.substring(ix)
    }

    @DiscreteFunction(identifier = "upper", params = ["str"])
    fun upper(str: Node): String = str.evaluate(runtime).uppercase()

    @DiscreteFunction(identifier = "lower", params = ["str"])
    fun lower(str: Node): String = str.evaluate(runtime).lowercase()

    @DiscreteFunction(identifier = "matches", params = ["str", "regex"])
    fun matches(str: Node, regex: Node) =
        str.evaluate(runtime).matches(regex.evaluate(runtime).toRegex()).map(Standard.TRUE, Standard.FALSE)

    @DiscreteFunction(identifier = "replace", params = ["org", "regex", "str", "mode"])
    fun replace(org: Node, regex: Node, str: Node, mode: Node): String {
        val modeStr = mode.evaluate(runtime)
        val orgStr = org.evaluate(runtime)
        val regexRegex = regex.evaluate(runtime).toRegex()
        val strStr = str.evaluate(runtime)
        return if (modeStr == "all")
            orgStr.replace(regexRegex, strStr)
        else if (modeStr == "first")
            orgStr.replaceFirst(regexRegex, strStr)
        else throw AntgRuntimeException("Replacement mode must either be 'all' or 'first', got '${modeStr}'.")
    }

    @DiscreteFunction(identifier = "replace", params = ["org", "regex", "str"])
    fun replace(org: Node, regex: Node, str: Node) =
        replace(org, regex, str, StringLiteral("all"))

    @DiscreteFunction(identifier = "trim", params = ["str"])
    fun trim(str: Node) = str.evaluate(runtime).trim()

    @DiscreteFunction(identifier = "capture", params = ["str", "regex", "group"])
    fun capture(str: Node, regex: Node, group: Node): String {
        val results = regex.evaluate(runtime).toRegex().matchEntire(str.evaluate(runtime)) ?: return ""
        val groupNumber = group.evaluate(runtime).antgNumber<Int>()
        if (groupNumber < 0 || groupNumber >= results.groups.size)
            return ""
        return results.groups[groupNumber]!!.value
    }

    @DiscreteFunction(identifier = "substr", params = ["str", "start", "end"])
    fun substr(str: Node, start: Node, end: Node): String {
        val startIndex = start.evaluate(runtime).antgNumber<Int>()
        val endIndex = end.evaluate(runtime).antgNumber<Int>()
        val strStr = str.evaluate(runtime)

        if (startIndex !in 0 .. strStr.length || endIndex !in 0 .. strStr.length ||
            endIndex < startIndex)
            throw AntgRuntimeException("Start and End index are invalid.")

        return strStr.substring(startIndex, endIndex)
    }

    @DiscreteFunction(identifier = "substr", params = ["str", "start"])
    fun substr(str: Node, start: Node): String = substr(str, start, StringLiteral(str.evaluate(runtime).length.toString()))

}