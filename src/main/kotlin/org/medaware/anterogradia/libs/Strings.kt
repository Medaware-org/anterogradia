package org.medaware.anterogradia.libs

import org.medaware.anterogradia.antgNumber
import org.medaware.anterogradia.map
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.StateRetention.STATEFUL
import org.medaware.anterogradia.syntax.Node
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

    @DiscreteFunction(identifier = "at", params = ["str", "index", "new"])
    fun at(str: Node, index: Node, new: Node): String {
        val ix = index.evaluate(runtime).antgNumber<Int>()
        val strStr = str.evaluate(runtime)

        if (ix < 0 || ix >= strStr.length)
            return ""

        val substr = new.evaluate(runtime)

        if (substr.isEmpty())
            return strStr

        return strStr.substring(0, ix) + substr + strStr.substring(ix)
    }

    @DiscreteFunction(identifier = "upper", params = ["str"])
    fun upper(str: Node): String = str.evaluate(runtime).uppercase()

    @DiscreteFunction(identifier = "lower", params = ["str"])
    fun lower(str: Node): String = str.evaluate(runtime).lowercase()

}