package org.medaware.anterogradia.runtime.library.standard

import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.Function
import org.medaware.anterogradia.runtime.library.VariadicFunction

@AnterogradiaLibrary
class AnterogradiaStandardLibrary {

    @VariadicFunction(identifier = "combine")
    fun combine(params: Array<String>) = params.joinToString(separator = "")

    @Function(identifier = "repeat", params = ["count", "str"])
    fun repeat(count: String, str: String) = try {
        str.repeat(count.toInt())
    } catch (e: NumberFormatException) {
        str
    }

    @Function(identifier = "repeat", params = ["count", "str", "separator"])
    fun repeat(count: String, str: String, separator: String) = try {
        List(count.toInt()) { str }.joinToString(separator = separator)
    } catch (e: NumberFormatException) {
        str
    }

}