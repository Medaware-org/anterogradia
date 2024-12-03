package org.medaware.anterogradia.runtime.type

import org.medaware.anterogradia.antgNumberOrNull

typealias ValidatorFunction = (input: String) -> Boolean

class TypeValidator {

    private val validators: HashMap<String, ValidatorFunction> = hashMapOf()

    init {
        validators["any"] = { true }

        validators["integer"] = { it ->
            it.antgNumberOrNull<Int>() != null
        }

        validators["real"] = { it ->
            it.antgNumberOrNull<Double>() != null
        }
    }

    fun register(id: String, validatorFunction: ValidatorFunction) {
        if (validators[id] != null)
            throw IllegalStateException("A validator for type '$id' is already present.")

        validators[id] = validatorFunction
    }

    fun validate(type: String, input: String): Boolean {
        val validator = validators[type] ?: throw IllegalArgumentException("No validator for type '$type' found.")
        return validator(input)
    }

}