package org.medaware.anterogradia.syntax

class Token(val type: TokenType, val value: String, val line: Int) {
    override fun toString(): String {
        return "Token(type=$type, value='$value', line=$line)"
    }

    fun orNull(): Token? {
        if (type == TokenType.UNDEFINED)
            return null

        return this
    }

    companion object {
        fun compareToken(token: Token?, identifier: String): Boolean {
            token ?: return false

            return token.value == identifier && token.type == TokenType.IDENTIFIER
        }

        fun compareToken(token: Token?, type: TokenType): Boolean {
            token ?: return false

            return token.type == type
        }

        fun undefinedToken(line: Int): Token {
            return Token(TokenType.UNDEFINED, "", line)
        }
    }

}