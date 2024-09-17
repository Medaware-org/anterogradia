package org.medaware.anterogradia.runtime.library

import org.medaware.anterogradia.Anterogradia
import org.medaware.anterogradia.exception.FunctionCallException
import org.medaware.anterogradia.exception.LibraryException
import org.medaware.anterogradia.exception.SanityException
import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.Node

class LibraryManager {

    private val libraries: MutableSet<Class<*>> = mutableSetOf()

    private val logger = Anterogradia.logger

    private fun libByPrefix(prefix: String): Class<*>? {
        return try {
            libraries.first { it.getAnnotation(AnterogradiaLibrary::class.java).prefix == prefix }
        } catch (e: NoSuchElementException) {
            null
        }
    }

    private fun libSanityCheck(lib: Class<*>) {
        val registeredFunctions = mutableSetOf<Pair<String, Int?>>()

        try {
            lib.getConstructor()
        } catch (e: Exception) {
            throw SanityException("Sanity check failed for '${lib.simpleName}': The class is expected to have a nullary constructor.")
        }

        lib.declaredMethods.forEach {

            /**
             * Handle variadic functions
             */

            it.getAnnotation(VariadicFunction::class.java)?.let { variadic ->
                if (registeredFunctions.any { it.first == variadic.identifier })
                    throw SanityException("Sanity check failed for '${lib.simpleName}': A variadic function must never co-exist with another function of the same name. Failed on variadic function '${variadic.identifier}'")

                if (it.parameters.size != 1)
                    throw SanityException("Sanity check failed for '${lib.simpleName}': A variadic function is expected to have a single string array parameter.")

                if (it.parameters.first().type != Array<String>::class.java)
                    throw SanityException("Sanity check failed for '${lib.simpleName}': A variadic function is expected to have a parameter of type Array<String>, got ${it.parameters.first().type.simpleName}")

                registeredFunctions.add(variadic.identifier to null)

                return@forEach
            }

            /**
             * Regular (discrete) functions
             */

            val function = it.getAnnotation(Function::class.java) ?: return@forEach

            if (it.returnType != String::class.java)
                throw SanityException("All Anterogradia @Function-s must return a String value. Function '${function.identifier}' (${it.name}) violates this rule by returning '${it.returnType.simpleName}'.")

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

            if (it.parameters.any { it.type != String::class.java })
                throw SanityException("Sanity check failed for '${lib.simpleName}': All function parameters must be of type String. This rule is violated in function '${function.identifier}' (${it.name}).")

            if (it.parameters.size != params.size)
                throw SanityException("Sanity check failed for '${lib.simpleName}': The length of the parameter list does not match the method's parameters of function '${function.identifier}' (${it.name}): ${it.parameters.size} parameters in the definition, ${function.params.size} parameters declared in the annotation.")
        }
    }

    fun register(libClass: Class<*>) {
        val lib = libClass.getAnnotation(AnterogradiaLibrary::class.java)
            ?: throw LibraryException("Failed to register library class '${libClass.canonicalName}'. Class is not annotated with '@AnterogradiaLibrary'.")

        val conflict = libByPrefix(lib.prefix)

        if (conflict != null)
            throw LibraryException("Failed to register library class '${libClass.simpleName}' due to a namespace conflict with '${conflict.simpleName}'.")

        libSanityCheck(libClass)

        if (!libraries.add(libClass))
            throw LibraryException("Attempted to register the same library class twice: '${libClass.simpleName}'.")

        logger.info("Successfully registered library '${libClass.simpleName}' with prefix '${lib.prefix}'.")
    }

    fun invokeLibMethod(
        prefix: String,
        name: String,
        args: HashMap<String, Node>,
        runtime: Runtime,
        variadic: Boolean
    ): String {
        val lib =
            libByPrefix(prefix) ?: throw FunctionCallException("Could not find any library with prefix '$prefix'.")

        val rawMethods = lib.declaredMethods.filter { it.getAnnotation(Function::class.java)?.identifier == name }

        val methods = rawMethods.map { (it to it.getAnnotation(Function::class.java)) }

        if (methods.isEmpty()) {
            /**
             * Variadic functions
             */

            val variadicFunctions =
                lib.declaredMethods.filter { it.name == name && it.isAnnotationPresent(VariadicFunction::class.java) }
                    .mapNotNull { (it to it.getAnnotation(VariadicFunction::class.java)) }

            val s = if (variadic) " variadic" else ""

            if (variadicFunctions.isEmpty())
                throw FunctionCallException("Could not find$s function $prefix:$name in library '${lib.simpleName}'.")

            if (variadicFunctions.size != 1)
                throw FunctionCallException("There is more than one variadic function of the same name ('${name}'). This should never happen!")

            val params = args.values.map { it.evaluate(runtime) }.toTypedArray()
            val instance = lib.getConstructor().newInstance()

            return variadicFunctions.first().first.invoke(instance, params) as String
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
            val groupedParams = groupParameters(args, method.first().second).map {
                it.evaluate(runtime)
            }.toTypedArray()
            val instance = lib.getConstructor().newInstance()
            if (groupedParams.isNotEmpty())
                return method.first().first.invoke(instance, *groupedParams) as String
            return method.first().first.invoke(instance) as String
        }

        if (method.size > 1)
            throw FunctionCallException("There is more than one potential match for function $prefix:$name:\n$matchesString")

        throw FunctionCallException("Could not find discrete function $prefix:$name with the desired number of parameters (${args.size}). Possible matches:\n$matchesString")
    }

    private fun groupParameters(params: HashMap<String, Node>, function: Function): List<Node> {
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
        return this.libraries.contains(libClass)
    }

}