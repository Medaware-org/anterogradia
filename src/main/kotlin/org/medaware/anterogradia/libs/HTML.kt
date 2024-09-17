package org.medaware.anterogradia.libs

import org.medaware.anterogradia.evalToString
import org.medaware.anterogradia.padIfNotEmpty
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.VariadicFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(prefix = "HTML")
class HTML(val runtime: Runtime) {

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Medaware Antg. HTML Plugin\n{C} Medaware, 2024\n"

    private fun tag(id: String, attrs: String = "", content: String = "") =
        """
            <$id${attrs.padIfNotEmpty()}>$content</$id>
        """.trimIndent()

    @VariadicFunction(identifier = "html")
    fun html(body: Array<Node>): String = tag("html", content = body.evalToString(runtime))

    @VariadicFunction(identifier = "head")
    fun head(body: Array<Node>): String = tag("head", content = body.evalToString(runtime))

    @VariadicFunction(identifier = "body")
    fun body(body: Array<Node>): String = tag("body", content = body.evalToString(runtime))

    @DiscreteFunction(identifier = "div", params = ["attributes", "body"])
    fun div(attributes: Node, body: Node): String =
        tag("div", attrs = attributes.evaluate(runtime), content = body.evaluate(runtime))

    @DiscreteFunction(identifier = "h1", params = ["attributes", "body"])
    fun h1(attributes: Node, body: Node): String =
        tag("h1", attrs = attributes.evaluate(runtime), content = body.evaluate(runtime))

    @VariadicFunction(identifier = "classList")
    fun classes(classes: Array<Node>): String =
        "class=\"${classes.map { it.evaluate(runtime) }.joinToString(" ").trim()}\""

}