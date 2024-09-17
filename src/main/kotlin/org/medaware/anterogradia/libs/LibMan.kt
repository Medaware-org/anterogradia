package org.medaware.anterogradia.libs

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(prefix = "libman")
class LibMan(val runtime: Runtime) {

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Anterogradia Dynamic Library Manager\n{C} Medaware, 2024\n"

    @DiscreteFunction(identifier = "load", params = ["path"])
    fun load(lib: Node): String {
        runtime.loadLibrary(lib.evaluate(runtime))
        return ""
    }

}