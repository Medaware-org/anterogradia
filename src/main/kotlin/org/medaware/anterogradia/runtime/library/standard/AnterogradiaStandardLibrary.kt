package org.medaware.anterogradia.runtime.library.standard

import org.medaware.anterogradia.padIfNotEmpty
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.Function

@AnterogradiaLibrary
class AnterogradiaStandardLibrary {

    @Function(identifier = "div", params = ["attributes", "html"])
    fun div(attrs: String, content: String): String {
        return """
            <div${attrs.padIfNotEmpty()}>$content</div>
        """.trimIndent()
    }

}