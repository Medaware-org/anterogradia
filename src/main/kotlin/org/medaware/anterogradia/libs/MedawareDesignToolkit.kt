package org.medaware.anterogradia.libs

import org.medaware.anterogradia.evalToString
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.VariadicFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(prefix = "mdk")
class MedawareDesignToolkit(val runtime: Runtime) {

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Medaware Design Toolkit\n{C} Medaware, 2024\n"

    @DiscreteFunction(identifier = "title", params = ["str"])
    fun title(title: Node): String =
        """
            <h2>${title.evaluate(runtime)}</h2>
        """.trimIndent()

    @DiscreteFunction(identifier = "banner", params = ["src"])
    fun banner(src: Node): String =
        """
            <img src="${src.evaluate(runtime)}" style="display: block; width: 95%; height: auto;"></img>
        """.trimIndent()

    @DiscreteFunction(identifier = "lead", params = ["str"])
    fun lead(str: Node): String =
        """
            <h4>${str.evaluate(runtime)}</h4>
        """.trimIndent()

    @VariadicFunction(identifier = "article")
    fun article(contents: Array<Node>) =
        """
            <div class="mdk-article">
            ${contents.evalToString(runtime)}
            </div>
        """.trimIndent()

}