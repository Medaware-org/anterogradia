package org.medaware.anterogradia.runtime.library.standard

import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.Function

@AnterogradiaLibrary
class AnterogradiaStandardLibrary {

    @Function(identifier = "title", params = ["title"])
    fun title(line: String): String {
        return ""
    }

}