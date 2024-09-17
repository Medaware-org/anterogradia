package org.medaware.anterogradia.runtime

import org.medaware.anterogradia.Anterogradia
import org.medaware.anterogradia.libs.Standard
import org.medaware.anterogradia.runtime.library.LibraryManager
import org.medaware.anterogradia.syntax.FunctionCall

class Runtime(val parameters: HashMap<String, String> = hashMapOf()) {

    private val libManager = LibraryManager()

    init {
        libManager.register(Standard::class.java)
    }

    fun loadLibrary(path: String) {
        val libClass = Class.forName(path)
        if (libManager.isLibLoaded(libClass)) {
            Anterogradia.logger.info("Library class '${libClass.canonicalName}' is already loaded. Skipping.")
            return
        }
        libManager.register(libClass)
    }

    fun callFunction(call: FunctionCall): String =
        libManager.invokeLibMethod(call.prefix, call.identifier.value, call.arguments, this, variadic = call.variadic)

}