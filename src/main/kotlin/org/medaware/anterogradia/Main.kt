package org.medaware.anterogradia

import java.nio.file.Files
import java.nio.file.Path

fun main() {

    val result = Anterogradia.invokeCompiler(Files.readString(Path.of("input.antg")))
    if (result.exception != null) {
        println("Error: " + (result.exception.rootCause().message ?: "Unknown error"))
        return
    }
    Files.writeString(Path.of("image.ppm"), result.output)
    print(result.output)
}