package org.medaware.anterogradia.runtime.library

import org.medaware.anterogradia.Anterogradia
import org.medaware.anterogradia.exception.FunctionCallException
import org.medaware.anterogradia.exception.LibraryException
import org.medaware.anterogradia.exception.SanityException
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.Node

class LibraryManager {

    private val libraries = hashMapOf<String, Class<*>>()

    private val logger = Anterogradia.logger

    private fun libByPrefix(prefix: String): Class<*>? {
        return libraries[prefix]
    }

    private fun libSanityCheck(lib: Class<*>) {
        val registeredFunctions = mutableSetOf<Pair<String, Int?>>()

        try {
            lib.getConstructor(Runtime::class.java)
        } catch (e: Exception) {
            throw SanityException("Sanity check failed for '${lib.simpleName}': The class is expected to have a constructor with only a runtime parameter.")
        }

        // Require all libraries to have an "about" function
        lib.declaredMethods.find {
            it.isAnnotationPresent(DiscreteFunction::class.java) && it.getAnnotation(
                DiscreteFunction::class.java
            ).identifier == "about"
        }
            ?: throw SanityException("Sanity check failed for '${lib.simpleName}': Each library is expected to have a parameter-less discrete 'about' function.")

        lib.declaredMethods.forEach {

            if (it.isAnnotationPresent(DiscreteFunction::class.java) && it.isAnnotationPresent(VariadicFunction::class.java))
                throw SanityException("Sanity check failed for '${lib.simpleName}': A function must not be annotated with both @VariadicFunction and @DiscreteFunction at the same time.")

            /**
             * Handle variadic functions
             */

            it.getAnnotation(VariadicFunction::class.java)?.let { variadic ->
                if (registeredFunctions.any { it.first == variadic.identifier })
                    throw SanityException("Sanity check failed for '${lib.simpleName}': A variadic function must never co-exist with another function of the same name. Failed on variadic function '${variadic.identifier}'")

                if (it.parameters.size != 1)
                    throw SanityException("Sanity check failed for '${lib.simpleName}': A variadic function is expected to have a single string array parameter.")

                if (it.parameters.first().type != Array<Node>::class.java)
                    throw SanityException("Sanity check failed for '${lib.simpleName}': A variadic function is expected to have a parameter of type Array<Node>, got ${it.parameters.first().type.simpleName}")

                if (it.returnType != String::class.java)
                    throw SanityException("Sanity check failed for '${lib.simpleName}': Variadic function '${variadic.identifier}' is expected to have the return type String, got ${it.returnType.simpleName}.")

                registeredFunctions.add(variadic.identifier to null)

                return@forEach
            }

            /**
             * Regular (discrete) functions
             */

            val function = it.getAnnotation(DiscreteFunction::class.java) ?: return@forEach

            if (it.returnType != String::class.java)
                throw SanityException("All Anterogradia @DiscreteFunction-s must return a String value. Function '${function.identifier}' (${it.name}) violates this rule by returning '${it.returnType.simpleName}'.")

            // Parameter names must not overlap
            val params = mutableListOf<String>()

            function.params.forEach { paramId ->
                if (params.contains(paramId))
                    throw SanityException("Sanity check failed for '${lib.simpleName}': Parameter names of function '${function.identifier}' (${it.name}) overlap on parameter '${it}'.")
                params.add(paramId)
            }

            if (registeredFunctions.any { it.first == function.identifier && it.second == null })
                throw SanityException("Sanity check failed for '${lib.simpleName}': A discrete function may not co-exist with a variadic function. Failed on discrete function '${function.identifier}'.")

            if (!registeredFunctions.add(function.identifier to params.size))
                throw SanityException("Sanity check failed for '${lib.simpleName}': Function name and parameter count overlaps for function '${function.identifier}' (${it.name}).")

            // All parameters must be String-s and their count must match the array's length

            if (it.parameters.any { it.type != Node::class.java })
                throw SanityException("Sanity check failed for '${lib.simpleName}': All function parameters must be of type Node. This rule is violated in function '${function.identifier}' (${it.name}).")

            if (it.parameters.size != params.size)
                throw SanityException("Sanity check failed for '${lib.simpleName}': The length of the parameter list does not match the method's parameters of function '${function.identifier}' (${it.name}): ${it.parameters.size} parameters in the definition, ${function.params.size} parameters declared in the annotation.")
        }
    }

    fun register(libClass: Class<*>, prefix: String) {
        libClass.getAnnotation(AnterogradiaLibrary::class.java)
            ?: throw LibraryException("Failed to register library class '${libClass.canonicalName}'. Class is not annotated with '@AnterogradiaLibrary'.")

        val conflict = libByPrefix(prefix)

        if (conflict != null)
            throw LibraryException("Failed to register library class '${libClass.simpleName}' due to a namespace conflict with '${conflict.simpleName}': Both libraries were imported as '$prefix'")

        libSanityCheck(libClass)

        if (libByPrefix(prefix) != null)
            throw LibraryException("Attempted to register two libraries under the same prefix: '$prefix'.")

        libraries[prefix] = libClass

        logger.info("Successfully imported library '${libClass.simpleName}' as " + (if (prefix.isEmpty()) "the standard library" else "'$prefix'"))
    }

    fun invokeLibMethod(
        prefix: String,
        name: String,
        args: HashMap<String, Node>,
        runtime: Runtime,
        variadic: Boolean
    ): String {
        val lib =
            libByPrefix(prefix) ?: throw FunctionCallException("No library was imported with prefix '$prefix'.")

        val rawMethods =
            lib.declaredMethods.filter { it.getAnnotation(DiscreteFunction::class.java)?.identifier == name }

        val methods = rawMethods.map { (it to it.getAnnotation(DiscreteFunction::class.java)) }

        if (methods.isEmpty()) {
            /**
             * Variadic functions
             */

            val variadicFunctions =
                lib.declaredMethods.filter { it.getAnnotation(VariadicFunction::class.java)?.identifier == name }
                    .mapNotNull { (it to it.getAnnotation(VariadicFunction::class.java)) }

            val s = if (variadic) " variadic" else ""

            if (variadicFunctions.isEmpty())
                throw FunctionCallException("Could not find$s function $prefix:$name in library '${lib.simpleName}'.")

            if (variadicFunctions.size != 1)
                throw FunctionCallException("There is more than one variadic function of the same name ('${name}'). This should never happen!")

            val paramsList = mutableListOf<Node>()

            args.keys.map { it.toInt() }.sorted().forEach {
                paramsList.add(args[it.toString()]!!)
            }

            val instance = runtime.libInstance(lib)

            return variadicFunctions.first().first.invoke(instance, paramsList.toTypedArray()) as String
        }

        if (variadic)
            throw FunctionCallException("Attempted to call discrete function '$name' as a variadic function.")

        // At this point, 'methods' contains multiple potential matches for the desired function.
        // We need to narrow it down further with the help of the parameters.

        val method = methods.filter { it.second.params.size == args.size }

        val matchesString = methods.map {
            it.second.identifier + "(${
                it.second.params.map { it }.toString().replace("[\\[\\]]".toRegex(), "")
            })"
        }.toString()
            .replace("[", "").replace("]", "\n")

        if (method.size == 1) {
            val groupedParams = groupParameters(args, method.first().second).toTypedArray()
            val instance = runtime.libInstance(lib)
            if (groupedParams.isNotEmpty())
                return method.first().first.invoke(instance, *groupedParams) as String
            return method.first().first.invoke(instance) as String
        }

        if (method.size > 1)
            throw FunctionCallException("There is more than one potential match for function $prefix:$name:\n$matchesString")

        throw FunctionCallException("Could not find discrete function $prefix:$name with the desired number of parameters (${args.size}). Possible matches:\n$matchesString")
    }

    private fun groupParameters(params: HashMap<String, Node>, function: DiscreteFunction): List<Node> {
        val result = mutableListOf<Node>()

        if (!function.params.sortedArray().contentEquals(params.keys.toTypedArray().sortedArray()))
            throw FunctionCallException("The parameter names do not match the definition of function '${function.identifier}'. Expected parameters are ${function.params.map { it }}")

        function.params.forEach {
            result.add(
                params.get(it)
                    ?: throw FunctionCallException("Ordering function call parameters failed. This should not happen.")
            )
        }

        return result
    }

    fun isLibLoaded(libClass: Class<*>): Boolean {
        return this.libraries.values.contains(libClass)
    }

}