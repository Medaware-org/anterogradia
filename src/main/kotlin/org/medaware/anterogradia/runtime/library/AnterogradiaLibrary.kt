package org.medaware.anterogradia.runtime.library

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AnterogradiaLibrary(val prefix: String = "")
