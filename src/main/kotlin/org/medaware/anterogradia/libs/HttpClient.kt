package org.medaware.anterogradia.libs

import okhttp3.OkHttpClient
import okhttp3.Request
import org.medaware.anterogradia.exception.FunctionCallException
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(prefix = "http")
class HttpClient(val runtime: Runtime) {

    companion object {
        val httpClient = OkHttpClient.Builder().build()
    }

    @DiscreteFunction(identifier = "about")
    fun about(): String = "HTTP Client Library\n{C} Medaware, 2024\n"

    @DiscreteFunction(identifier = "request", params = ["method", "url"])
    fun request(method: Node, url: Node): String {

        val evaluatedMethod = method.evaluate(runtime)
        val evaluatedUrl = url.evaluate(runtime)

        val request = Request.Builder()
            .method(evaluatedMethod, null)
            .url(evaluatedUrl)
            .build()

        httpClient.newCall(request).execute().use { response ->
            return response.body?.string() ?: throw FunctionCallException("Failed to parse body of http request $evaluatedMethod $evaluatedUrl because response body is empty.")
        }
    }

}