package org.medaware.anterogradia.libs

import com.google.gson.JsonParser
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(prefix = "json")
class Json(val runtime: Runtime) {


    @DiscreteFunction(identifier = "about")
    fun about(): String = "JSON Library\n{C} Medaware, 2024\n"


    @DiscreteFunction(identifier = "extract", params = ["src", "key"])
    fun extractSingleValue(src: Node, key: Node): String {
        val jsonStructure = JsonParser.parseString(src.evaluate(runtime)).asJsonObject

        return jsonStructure[key.evaluate(runtime)].asString
    }
}