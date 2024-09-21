package org.medaware.anterogradia.libs

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.StateRetention.STATELESS

@AnterogradiaLibrary(STATELESS)
class ASCII(val runtime: Runtime) {

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Anterogradia ASCII Extension\n{C} Medaware, 2024\n"

    @DiscreteFunction(identifier = "endl")
    fun endl(): String = "\n"

    @DiscreteFunction(identifier = "tab")
    fun tab(): String = "\t"

    @DiscreteFunction(identifier = "quo")
    fun quo(): String = "\""

}