package org.medaware.anterogradia.syntax.tokenizer

import org.medaware.anterogradia.exception.LexerException
import org.medaware.anterogradia.exception.ParseException

class Tokenizer(private var inputString: String) {

    private var tokenParsers: Array<() -> Token>

    private var lineNumber: Int = 1
    private var lastToken: Token

    private var input: StringBuilder = StringBuilder(inputString)

    // Indicates whether to automatically convert the next token to a string literal
    private var stringLiteralConversion = false

    init {
        lastToken = Token.undefinedToken(lineNumber)

        tokenParsers =
            arrayOf(::parseIdentifier, ::parseNumberLiteral, ::parseStringMatchedToken, ::parseStringLiteralToken)
    }

    fun nextToken(): Token {
        skipSpaces() // Skip all spaces before analysing the upcoming token

        val currentChar: Char = this.input.firstOrNull() ?: return Token.undefinedToken(lineNumber)

        lastToken =
            runTokenParsers()

        if (stringLiteralConversion) {
            lastToken = lastToken.asType(TokenType.STRING_LITERAL)
            stringLiteralConversion = false
        }

        return lastToken
    }

    fun hasNext(): Boolean {
        skipSpaces()
        return input.isNotEmpty()
    }

    fun runTokenParsers(): Token {
        for (tokenParser in tokenParsers)
            return tokenParser().orNull() ?: continue

        throw ParseException("Could not parse token starting with '${input.firstOrNull() ?: "N/A"}' on line $lineNumber.")
    }

    fun skipSpaces() {
        var spaceCount: Int = 0

        var isComment = false

        for (char in input) {
            if (char == '#')
                isComment = true

            if (char == '`' && !isComment) {
                stringLiteralConversion = true
                spaceCount++
                continue
            }

            if (!isSpace(char) && !isComment)
                break

            if (stringLiteralConversion) {
                stringLiteralConversion = false
                throw LexerException("A token must immediately follow after a string conversion operator. Error on line $lineNumber.")
            }

            val trimmedChar: Char = char

            if (trimmedChar == '\n') {
                lineNumber++
                isComment = false
            }

            spaceCount++
        }

        input.advance(spaceCount)
    }

    fun parseIdentifier(): Token {
        if (!isLetter(input.firstOrNull() ?: return Token.undefinedToken(lineNumber)))
            return Token.undefinedToken(lineNumber)

        val buffer = StringBuilder()

        // The first character
        buffer.append(input.firstOrNull())
        input.advance()

        input.firstOrNull() ?: return Token(TokenType.IDENTIFIER, buffer.toString(), lineNumber)

        var remainingTokenLength: Int = 0

        // All remaining characters
        for (char in input) {
            if (!isLetter(char) && !isDigit(char))
                break

            buffer.append(char)
            remainingTokenLength++
        }

        input.advance(remainingTokenLength) // We cannot advance the input buffer while looping over its contents
        // because weird things will happen

        return Token(TokenType.IDENTIFIER, buffer.toString(), lineNumber)
    }

    fun parseNumberLiteral(): Token {
        if (!isDigit(input.firstOrNull() ?: return Token.undefinedToken(lineNumber)))
            return Token.undefinedToken(lineNumber)

        val buffer = StringBuilder()

        var tokenLength: Int = 0

        var decimalPresent = false
        var isCharAfterDecimal = false

        // All remaining characters
        for (char in input) {
            if (!isDigit(char)) {
                if (char == '.') {
                    if (decimalPresent)
                        throw LexerException("A floating point literal may not have more than a single decimal comma. Error on line ${this.lineNumber}.")
                    decimalPresent = true
                    isCharAfterDecimal = true
                } else break
            }

            buffer.append(char)
            tokenLength++

            if (char != '.')
                isCharAfterDecimal = false
        }

        if (isCharAfterDecimal)
            throw LexerException("Expected further digits after floating point comma on line ${this.lineNumber}.")

        input.advance(tokenLength)

        return Token(TokenType.NUMBER_LITERAL, buffer.toString(), lineNumber)
    }

    fun parseStringMatchedToken(): Token {
        input.firstOrNull() ?: return Token.undefinedToken(lineNumber)
        val type: TokenType = findMatchingType(input.toString()) ?: return Token.undefinedToken(lineNumber)

        input.advance(type.value!!.length)

        return Token(type, type.value, lineNumber)
    }

    fun parseStringLiteralToken(): Token {
        val first = input.firstOrNull() ?: return Token.undefinedToken(lineNumber)

        if (first != '"')
            return Token.undefinedToken(lineNumber)

        val buffer = StringBuilder("")

        input.advance() // Skip the ' " '

        var stringLength: Int = 0

        for (char in input) {
            if (char == '"')
                break

            buffer.append(char)
            stringLength++
        }

        input.advance(stringLength + 1) // +1 -> Also skip the trailing quotation mark

        return Token(TokenType.STRING_LITERAL, buffer.toString(), lineNumber)
    }

}

fun StringBuilder.advance() {
    if (this.isEmpty())
        return

    this.deleteCharAt(0)
}

// Efficiency lol
fun StringBuilder.advance(count: Int) {
    repeat(count) {
        advance()
    }
}

fun isLetter(c: Char?): Boolean {
    c ?: return false
    return (c in 'a'..'z') || (c in 'A'..'Z') || (c == '_')
}

fun isDigit(c: Char?): Boolean {
    c ?: return false
    return c in '0'..'9'
}

fun isSpace(c: Char?): Boolean {
    c ?: return false
    return c == ' ' || c == '\t' || c == '\n'
}