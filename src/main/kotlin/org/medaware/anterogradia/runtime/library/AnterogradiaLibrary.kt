package org.medaware.anterogradia.runtime.library

/**
 * `StateRetention.STATEFUL` - A library that retains its state. Exists as a singleton and is thus only instantiated
 *  once throughout the entire runtime of the program.
 *
 *  `StateRetention.STATELESS` - Every function call from that library requires a new instance to be produced
 */
enum class StateRetention {
    STATEFUL,
    STATELESS
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AnterogradiaLibrary(val stateRetention: StateRetention = StateRetention.STATELESS)