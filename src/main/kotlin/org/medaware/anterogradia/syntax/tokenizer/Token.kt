package org.medaware.anterogradia.syntax.tokenizer

class Token(val type: TokenType, val value: String, val line: Int) {
    override fun toString(): String {
        return "Token(type=$type, value='$value', line=$line)"
    }

    fun orNull(): Token? {
        if (type == TokenType.UNDEFINED)
            return null

        return this
    }

    fun compareToken(identifier: String): Boolean {
        return this.value == identifier && this.type == TokenType.IDENTIFIER
    }

    fun compareToken(type: TokenType): Boolean {
        return this.type == type
    }

    fun asType(type: TokenType) = Token(type, this.value, this.line)

    companion object {
        fun undefinedToken(line: Int): Token {
            return Token(TokenType.UNDEFINED, "", line)
        }
    }

}