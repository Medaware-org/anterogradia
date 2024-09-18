package org.medaware.anterogradia.syntax.parser

import org.medaware.anterogradia.exception.ParseException
import org.medaware.anterogradia.syntax.FunctionCall
import org.medaware.anterogradia.syntax.Node
import org.medaware.anterogradia.syntax.Script
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
        if (currentToken.type == TokenType.UNDEFINED)
            return StringLiteral("")

        if (currentToken.compareToken("if"))
            return parseIfConstruct()

        return when (currentToken.type) {
            TokenType.IDENTIFIER -> parseFunctionCall()
            TokenType.STRING_LITERAL,
            TokenType.NUMBER_LITERAL -> parseStringLiteral()

            else -> throw ParseException("Could not parse expression: Unknown expression starting with token of type ${currentToken.type} on line ${currentToken.line}.")
        }
    }

    fun parseIfConstruct(): FunctionCall {
        if (!currentToken.compareToken("if"))
            throw ParseException("Expected identifier 'if' at the start of an if construct, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume() // Skip "if"

        if (!currentToken.compareToken(TokenType.LPAREN))
            throw ParseException("Expected '(' after the 'if' identifier, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume()

        val expr = parseExpression()

        if (!currentToken.compareToken(TokenType.RPAREN))
            throw ParseException("Expected ')' after the conditional expression, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume()

        if (!currentToken.compareToken(TokenType.LCURLY))
            throw ParseException("Expected '{', got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume()

        var paramNumber = 0

        val params = hashMapOf<String, Node>()

        var blockLine = currentToken.line

        while (!currentToken.compareToken(TokenType.RCURLY) && !currentToken.compareToken(TokenType.UNDEFINED)) {
            val blockExpr = parseExpression()
            params.put((paramNumber++).toString(), blockExpr)
        }

        if (currentToken.compareToken(TokenType.UNDEFINED))
            throw ParseException("Reached end of file while parsing if block on line ${blockLine}. This is likely due to an unclosed pair of parentheses or brackets.")

        consume() // Skip '}'

        val thenFunction = FunctionCall("", "progn", params, true)

        if (!currentToken.compareToken("else"))
            return FunctionCall(
                "",
                "_if",
                hashMapOf<String, Node>(
                    "cond" to expr,
                    "then" to thenFunction,
                    "else" to FunctionCall("", "nothing", hashMapOf())
                )
            )

        consume() // Skip 'else'

        if (!currentToken.compareToken(TokenType.LCURLY))
            throw ParseException("Expected '{' after 'else', got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume()

        blockLine = currentToken.line

        val elseParams = hashMapOf<String, Node>()

        paramNumber = 0

        while (!currentToken.compareToken(TokenType.RCURLY) && !currentToken.compareToken(TokenType.UNDEFINED)) {
            val blockExpr = parseExpression()
            elseParams.put((paramNumber++).toString(), blockExpr)
        }

        if (currentToken.compareToken(TokenType.UNDEFINED))
            throw ParseException("Reached end of file while parsing if block on line ${blockLine}. This is likely due to an unclosed pair of parentheses or brackets.")


        consume()

        val elseFunction = FunctionCall("", "progn", elseParams, true)

        return FunctionCall(
            "",
            "_if",
            hashMapOf<String, Node>(
                "cond" to expr,
                "then" to thenFunction,
                "else" to elseFunction
            )
        )
    }

    fun parseStringLiteral(): StringLiteral {
        val value: String = when (currentToken.type) {
            TokenType.STRING_LITERAL,
            TokenType.NUMBER_LITERAL -> currentToken.value

            else -> throw ParseException("Could not parse string literal from token of type ${currentToken.type} on line ${currentToken.line}.")
        }

        consume()

        return StringLiteral(value)
    }

    fun parseLoadInstruction(): String? {
        if (!currentToken.compareToken(TokenType.AT))
            return null // We don't just throw an exception here because we want this operation to be non-invasive

        consume()

        if (!currentToken.compareToken("library"))
            throw ParseException("Expected 'library' keyword after the '@' prefix, got ${currentToken.type} on line ${currentToken.line}.")

        consume()

        if (currentToken.type != TokenType.STRING_LITERAL)
            throw ParseException("Expected string literal (library path) after the 'library' keyword, got ${currentToken.type} on line ${currentToken.line}.")

        val path = currentToken.value

        consume()

        return path
    }

    fun parseFunctionCall(): FunctionCall {
        if (!currentToken.compareToken(TokenType.IDENTIFIER))
            throw ParseException("Could not parse function call: Unexpected token of type ${currentToken.type} found on line ${currentToken.line} in place of the function identifier or library prefix.")

        val libPrefix: String

        // The token could either be a lib prefix or the function identifier

        if (nextToken.compareToken(TokenType.COLON)) {
            libPrefix = currentToken.value

            consume() // Skip the lib prefix

            if (!nextToken.compareToken(TokenType.IDENTIFIER))
                throw ParseException("Expected function identifier after lib prefix colon, got ${currentToken.type} on line ${currentToken.line} instead.")

            consume() // Skip ':'

        } else libPrefix = ""

        val functionId = currentToken.value

        consume()

        if (!currentToken.compareToken(TokenType.LPAREN) && !currentToken.compareToken(TokenType.LCURLY))
            throw ParseException("Could not parse function call: Expected '(' or '{' after function identifier, got ${currentToken.type} on line ${currentToken.line}.")

        val variadic: Boolean = currentToken.type == TokenType.LCURLY
        val closingType = if (currentToken.type == TokenType.LPAREN) TokenType.RPAREN else TokenType.RCURLY
        val closingChar = if (closingType == TokenType.RPAREN) ')' else '}'

        consume()

        /**
         * The parameters are a comma-separated list of:
         * identifier '=' expr
         * The parameter list may also be empty.
         * For variadic functions, the parameters are unnamed - effectively
         * a list of comma-separated expressions.
         */

        val params = hashMapOf<String, Node>()
        var varargCount = 0

        if (currentToken.compareToken(closingType)) {
            consume() // Skip the closing token
            return FunctionCall(libPrefix, functionId, params, variadic = variadic)
        }

        while (true) {
            val paramId: String

            if (!variadic) {
                if (!currentToken.compareToken(TokenType.IDENTIFIER) || !nextToken.compareToken(TokenType.EQUALS))
                    throw ParseException("Could not parse function call: Expected parameter identifier, got ${currentToken.type} on line ${currentToken.line}.")

                paramId = currentToken.value

                consume()

                if (!currentToken.compareToken(TokenType.EQUALS))
                    throw ParseException("Could not parse function call: Expected '=' after parameter identifier '$paramId', got ${currentToken.type} on line ${currentToken.line}.")

                consume()
            } else {
                paramId = (varargCount++).toString()
            }

            val expr = parseExpression()

            if (params[paramId] != null)
                throw ParseException("Could not parse function call: Each parameter must only be assigned once. Violation on parameter '$paramId' when calling function '${functionId}' on line ${currentToken.line}.")

            params[paramId] = expr

            if (currentToken.compareToken(TokenType.COMMA)) {
                consume()
                continue
            }

            if (!currentToken.compareToken(closingType))
                throw ParseException("Could not parse function call: Expected '$closingChar' or ',' and more parameters, got ${currentToken.type} on line ${currentToken.line}.")

            consume() // Skip ')' or '}'
            break
        }

        return FunctionCall(libPrefix, functionId, params, variadic = variadic)
    }

    companion object {
        fun parseScript(str: String): Script {
            val parser = Parser(Tokenizer(str))
            val libs = mutableListOf<String>()
            while (true) {
                val lib = parser.parseLoadInstruction() ?: break
                libs.add(lib)
            }
            val expr = parser.parseExpression()
            return Script(libs, expr)
        }
    }

}