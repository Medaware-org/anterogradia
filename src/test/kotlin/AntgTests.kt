import org.medaware.anterogradia.Anterogradia
import kotlin.test.Test
import kotlin.test.assertEquals

class AntgTests {

    private fun compile(input: String) = Anterogradia.invokeCompiler(input).output

    @Test
    fun `Variables are modifiable and accessible`() {
        assertEquals(
            compile(
                """
                    `foo := 123
                    &`foo
                """.trimIndent()
            ),
            "123"
        )
    }

    @Test
    fun `Variables preserve their value throughout function calls`() {
        assertEquals(
            compile(
                """
                    fun foo <a> { `a := 321 }
                    `a := 123
                    eval foo(a = &`a)
                    &`a
                """.trimIndent()
            ),
            "123"
        )
    }

}