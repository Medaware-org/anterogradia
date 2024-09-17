package org.medaware.anterogradia.libs

import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.Function

@AnterogradiaLibrary(prefix = "mem")
class Memory {

    companion object {
        private val variables: HashMap<String, String> = hashMapOf()
    }

    @Function(identifier = "set", params = ["key", "value"])
    fun setVariable(key: String, value: String): String {
        variables[key] = value
        return ""
    }

    @Function(identifier = "get", params = ["key"])
    fun getVariable(key: String): String {
        return variables[key] ?: "nothing"
    }

}