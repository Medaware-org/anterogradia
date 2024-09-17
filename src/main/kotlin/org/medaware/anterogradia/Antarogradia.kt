package org.medaware.anterogradia

import org.medaware.anterogradia.runtime.library.LibraryManager
import org.medaware.anterogradia.runtime.library.standard.AnterogradiaStandardLibrary

fun main() {
    val manager = LibraryManager()
    manager.register(AnterogradiaStandardLibrary::class.java)
}