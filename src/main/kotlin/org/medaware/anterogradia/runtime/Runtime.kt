package org.medaware.anterogradia.runtime

import org.medaware.anterogradia.exception.LibraryException
import org.medaware.anterogradia.libs.Standard
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.LibraryManager
import org.medaware.anterogradia.runtime.library.StateRetention
import org.medaware.anterogradia.syntax.FunctionCall

class Runtime(val parameters: HashMap<String, String> = hashMapOf()) {

    private val libManager = LibraryManager()

    private val libInstances = hashMapOf<Class<*>, Any>()

    init {
        libManager.register(Standard::class.java, "")
    }

    fun loadLibrary(path: String, prefix: String) {
        val libClass: Class<*> = try {
            Class.forName(path)
        } catch (e: Exception) {
            throw LibraryException("Could not load library '${path}'.")
        }

        if (libManager.isLibLoaded(libClass))
            throw LibraryException("Library class '${libClass.canonicalName}' is already loaded.")

        libManager.register(libClass, prefix)
    }

    fun callFunction(call: FunctionCall): String =
        libManager.invokeLibMethod(call.prefix, call.identifier, call.arguments, this, variadic = call.variadic)

    /**
     * Provides the caller with the instance of a given library class respectful of the state retention rules
     */
    fun libInstance(libClass: Class<*>): Any {
        if (libClass.getAnnotation(AnterogradiaLibrary::class.java).stateRetention == StateRetention.STATELESS)
            return libClass.getDeclaredConstructor(Runtime::class.java).newInstance(this)

        val singleton = libInstances[libClass]

        if (singleton == null)
            libInstances[libClass] = libClass.getDeclaredConstructor(Runtime::class.java).newInstance(this)

        return libInstances[libClass]!!
    }

}