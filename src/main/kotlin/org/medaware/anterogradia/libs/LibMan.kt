package org.medaware.anterogradia.libs

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.Function
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(prefix = "libman")
class LibMan(val runtime: Runtime) {

    @Function(identifier = "about")
    fun about(): String = "Anterogradia Dynamic Library Manager\n{C} 2024 Medaware\n"

    @Function(identifier = "load", params = ["path"])
    fun load(lib: Node): String {
        runtime.loadLibrary(lib.evaluate(runtime))
        return ""
    }

}