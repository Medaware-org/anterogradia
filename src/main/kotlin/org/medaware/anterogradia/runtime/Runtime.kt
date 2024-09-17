package org.medaware.anterogradia.runtime

import org.medaware.anterogradia.runtime.library.LibraryManager
import org.medaware.anterogradia.runtime.library.standard.AnterogradiaStandardLibrary
import org.medaware.anterogradia.syntax.FunctionCall

class Runtime {

    private val libManager = LibraryManager()

    init {
        libManager.register(AnterogradiaStandardLibrary::class.java)
    }

    fun callFunction(call: FunctionCall): String =
        libManager.invokeLibMethod("", call.identifier.value, call.arguments, this, variadic = call.variadic)

}