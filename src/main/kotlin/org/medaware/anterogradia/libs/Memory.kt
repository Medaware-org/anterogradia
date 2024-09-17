package org.medaware.anterogradia.libs

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.Function
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(prefix = "mem")
class Memory(val runtime: Runtime) {

    companion object {
        private val variables: HashMap<String, String> = hashMapOf()
    }

    @Function(identifier = "about")
    fun about(): String = "Anterogradia Memory Library\n{C} 2024 Medaware\n"

    @Function(identifier = "set", params = ["key", "value"])
    fun setVariable(key: Node, value: Node): String {
        variables[key.evaluate(runtime)] = value.evaluate(runtime)
        return ""
    }

    @Function(identifier = "get", params = ["key"])
    fun getVariable(key: Node): String {
        return variables[key.evaluate(runtime)] ?: "nothing"
    }

}