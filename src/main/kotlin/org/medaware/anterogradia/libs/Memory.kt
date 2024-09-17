package org.medaware.anterogradia.libs

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(prefix = "mem")
class Memory(val runtime: Runtime) {

    companion object {
        private val variables: HashMap<String, String> = hashMapOf()
    }

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Anterogradia Memory Library\n{C} Medaware, 2024\n"

    @DiscreteFunction(identifier = "set", params = ["key", "value"])
    fun setVariable(key: Node, value: Node): String {
        variables[key.evaluate(runtime)] = value.evaluate(runtime)
        return ""
    }

    @DiscreteFunction(identifier = "get", params = ["key"])
    fun getVariable(key: Node): String {
        return variables[key.evaluate(runtime)] ?: "nothing"
    }

}