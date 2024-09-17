package org.medaware.anterogradia.libs

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.Function

@AnterogradiaLibrary(prefix = "libman")
class LibMan(val runtime: Runtime) {

    @Function(identifier = "load", params = ["path"])
    fun load(lib: String): String {
        runtime.loadLibrary(lib)
        return ""
    }

}