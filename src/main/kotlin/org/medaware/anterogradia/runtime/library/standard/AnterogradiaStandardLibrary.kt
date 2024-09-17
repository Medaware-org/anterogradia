package org.medaware.anterogradia.runtime.library.standard

import org.medaware.anterogradia.padIfNotEmpty
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.Function
import org.medaware.anterogradia.runtime.library.VariadicFunction

@AnterogradiaLibrary
class AnterogradiaStandardLibrary {

    @Function(identifier = "div", params = ["attributes", "html"])
    fun div(attrs: String, content: String): String =
        """
            <div${attrs.padIfNotEmpty()}>$content</div>
        """.trimIndent()

    @VariadicFunction(identifier = "combine")
    fun combine(params: Array<String>) = params.joinToString(separator = "")

}