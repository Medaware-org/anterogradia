package org.medaware.anterogradia.syntax.parser

import org.medaware.anterogradia.exception.ParseException
import org.medaware.anterogradia.syntax.FunctionCall
import org.medaware.anterogradia.syntax.Node
import org.medaware.anterogradia.syntax.StringLiteral
import org.medaware.anterogradia.syntax.tokenizer.Token
import org.medaware.anterogradia.syntax.tokenizer.TokenType
import org.medaware.anterogradia.syntax.tokenizer.Tokenizer

class Parser(private val tokenizer: Tokenizer) {

    private var currentToken: Token
    private var nextToken: Token

    init {
        currentToken = tokenizer.nextToken()
        nextToken = tokenizer.nextToken()
    }

    fun consume() {
        currentToken = nextToken
        nextToken = tokenizer.nextToken()
    }

    fun hasNext(): Boolean = currentToken.orNull() != null && nextToken.orNull() != null

    fun parseExpression(): Node {
        return when (currentToken.type) {
            TokenType.IDENTIFIER -> parseFunctionCall()
            TokenType.STRING_LITERAL,
            TokenType.NUMBER_LITERAL -> parseStringLiteral()

            else -> throw ParseException("Could not parse expression: Unknown expression starting with token of type ${currentToken.type} on line ${currentToken.line}.")
        }
    }

    fun parseStringLiteral(): StringLiteral {
        var value: String = when (currentToken.type) {
            TokenType.STRING_LITERAL,
            TokenType.NUMBER_LITERAL -> currentToken.value

            else -> throw ParseException("Could not parse string literal from token of type ${currentToken.type} on line ${currentToken.line}.")
        }

        consume()

        return StringLiteral(value)
    }

    fun parseFunctionCall(): FunctionCall {
        // Function identifier
        if (!currentToken.compareToken(TokenType.IDENTIFIER))
            throw ParseException("Could not parse function call: Unexpected token of type ${currentToken.type} found on line ${currentToken.line} in place of the function identifier.")

        val functionId = currentToken

        consume()

        if (!currentToken.compareToken(TokenType.LPAREN))
            throw ParseException("Could not parse function call: Expected '(' after function identifier, got ${currentToken.type} on line ${currentToken.line}.")

        consume()

        /**
         * The parameters are a comma-separated list of:
         * identifier '=' expr
         * The parameter list may also be empty
         */

        val params = hashMapOf<String, Node>()

        if (currentToken.compareToken(TokenType.RPAREN))
            return FunctionCall(functionId, params)

        while (true) {
            if (!currentToken.compareToken(TokenType.IDENTIFIER) || !nextToken.compareToken(TokenType.EQUALS))
                throw ParseException("Could not parse function call: Expected parameter identifier, got ${currentToken.type} on line ${currentToken.line}.")

            val paramId = currentToken.value

            consume()

            if (!currentToken.compareToken(TokenType.EQUALS))
                throw ParseException("Could not parse function call: Expected '=' after parameter identifier '$paramId', got ${currentToken.type} on line ${currentToken.line}.")

            consume()

            val expr = parseExpression()

            if (params[paramId] != null)
                throw ParseException("Could not parse function call: Each parameter must only be assigned once. Violation on parameter '$paramId' when calling function '${functionId.value}' on line ${currentToken.line}.")

            params[paramId] = expr

            if (currentToken.compareToken(TokenType.COMMA)) {
                consume()
                continue
            }

            if (!currentToken.compareToken(TokenType.RPAREN))
                throw ParseException("Could not parse function call: Expected ')' or ',' and more parameters, got ${currentToken.type} on line ${currentToken.line}.")

            consume() // Skip ')'
            break
        }

        return FunctionCall(functionId, params)
    }

}