package org.medaware.anterogradia.syntax.parser

import org.medaware.anterogradia.exception.ParseException
import org.medaware.anterogradia.libs.Standard
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

    fun parserEntrypoint(): FunctionCall {
        val expressions = mutableListOf<Node>()
        while (currentToken.orNull() != null)
            expressions.add(parseExpression())
        val prognParams = hashMapOf<String, Node>()
        expressions.forEachIndexed { index, it ->
            prognParams.put(index.toString(), it)
        }
        return FunctionCall("", "progn", prognParams, true)
    }

    private fun parseSimpleExpression(): Node {
        val getBinding = (currentToken.type == TokenType.AMPERSAND)

        if (getBinding)
            consume() // '&'

        if (currentToken.type == TokenType.UNDEFINED)
            return StringLiteral("")

        if (currentToken.compareToken(TokenType.LPAREN)) {
            consume() // '('
            val expr = parseExpression()
            if (!currentToken.compareToken(TokenType.RPAREN))
                throw ParseException("Expected ')' after sub-expression, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")
            consume() // ')'
            return expr
        }

        // Magnitude binding
        if (currentToken.type == TokenType.VBAR) {
            consume() // '|'
            val expr = parseExpression()
            if (currentToken.type != TokenType.VBAR)
                throw ParseException("Expected '|' after magnitude expression, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")
            consume() // '|'
            return FunctionCall("", "len", hashMapOf("expr" to expr), false)
        }

        val expr = parseBindings() ?: when (currentToken.type) {
            TokenType.IDENTIFIER -> parseFunctionCall()
            TokenType.STRING_LITERAL,
            TokenType.NUMBER_LITERAL -> parseStringLiteral()
            else -> throw ParseException("Could not parse expression: Unknown expression starting with token of type ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")
        }

        return if (getBinding) FunctionCall("", "get", hashMapOf("key" to expr), false) else expr
    }

    fun parseAdditiveExpression(): Node {
        var left = parseSimpleExpression()

        if (currentToken.compareToken(TokenType.EQUALS)) {
            consume() // '='
            left = FunctionCall(
                "",
                "equal",
                hashMapOf(Standard.CMP_LEFT to left, Standard.CMP_RIGHT to parseSimpleExpression()),
                false
            )
        } else if (currentToken.compareToken(TokenType.LGREATER)) {
            consume() // '>'
            left = FunctionCall(
                "",
                "lgt",
                hashMapOf(Standard.CMP_LEFT to left, Standard.CMP_RIGHT to parseSimpleExpression()),
                false
            )
        } else if (currentToken.compareToken(TokenType.RGREATER)) {
            consume() // '<'
            left = FunctionCall(
                "",
                "rgt",
                hashMapOf(Standard.CMP_LEFT to left, Standard.CMP_RIGHT to parseSimpleExpression()),
                false
            )
        }

        return left
    }

    fun parseExpression(): Node {
        val negated = currentToken.compareToken(TokenType.EXCLAMATION)

        if (negated)
            consume() // '!'

        var left = parseAdditiveExpression()

        // Variable assignment
        if (currentToken.compareToken(TokenType.ASSIGN_RIGHT)) {
            consume() // ':='
            left = FunctionCall("", "set", hashMapOf("key" to left, "value" to parseSimpleExpression()), false)
        }

        if (negated)
            left = FunctionCall("", "not", hashMapOf("cond" to left), false)

        return left
    }

    /**
     * Parse the block as a `progn` call
     */
    fun parseBlock(): FunctionCall {
        if (!currentToken.compareToken(TokenType.LCURLY))
            throw ParseException("Expected '{' at the beginning of a block expression, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume() // '{'

        var paramNumber = 0
        val params = hashMapOf<String, Node>()
        val blockLine = currentToken.line

        while (!currentToken.compareToken(TokenType.RCURLY) && !currentToken.compareToken(TokenType.UNDEFINED)) {
            val blockExpr = parseExpression()
            params.put((paramNumber++).toString(), blockExpr)
        }

        if (currentToken.compareToken(TokenType.UNDEFINED))
            throw ParseException("Reached end of file while parsing block on line ${blockLine}. This is likely due to an unclosed pair of parentheses or brackets.")

        consume() // Skip '}'

        return FunctionCall("", "progn", params, true)
    }

    fun parseBindings(): Node? {
        if (currentToken.compareToken("if"))
            return parseIfConstruct()

        if (currentToken.compareToken("fun"))
            return parseFunctionDefinition()

        if (currentToken.compareToken("eval"))
            return parseFunctionEval()

        if (currentToken.compareToken("while"))
            return parseWhileLoop()

        var result: Node? = null

        if (currentToken.compareToken("true"))
            result = StringLiteral("true")
        else if (currentToken.compareToken("false"))
            result = StringLiteral("false")

        if (result != null) {
            consume()
            return result
        }

        return null
    }

    fun parseIfConstruct(): FunctionCall {
        if (!currentToken.compareToken("if"))
            throw ParseException("Expected identifier 'if' at the start of an if construct, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume() // "if"

        if (!currentToken.compareToken(TokenType.LPAREN))
            throw ParseException("Expected '(' after the 'if' identifier, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume() // '(

        val expr = parseExpression()

        if (!currentToken.compareToken(TokenType.RPAREN))
            throw ParseException("Expected ')' after the conditional expression, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume() // ')'

        val thenFunction = parseBlock()

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

        consume() // 'else'

        val elseFunction = parseBlock()

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

    fun parseFunctionEval(): FunctionCall {
        if (!currentToken.compareToken("eval"))
            throw ParseException("Expected identifier 'eval' at the start of an evaluation, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume() // 'eval'

        if (currentToken.type != TokenType.IDENTIFIER)
            throw ParseException("Expected identifier of function to be evaluated, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}")

        // Parse stored expression call
        if (nextToken.type == TokenType.LPAREN) {
            val line = currentToken.line
            val call = parseFunctionCall()

            if (call.variadic)
                throw ParseException("A function called via 'eval' must not be variadic. Error on line $line.")

            val prep = call.arguments.keys.map { arg ->
                FunctionCall("", "set", hashMapOf("key" to StringLiteral(arg), "value" to call.arguments[arg]!!))
            }.toMutableList()

            prep.add(FunctionCall("", "_eval", hashMapOf("id" to StringLiteral(call.identifier)), false))

            val map = hashMapOf<String, Node>()

            prep.forEachIndexed { index, it ->
                map.put(index.toString(), it)
            }

            return FunctionCall("", "progn", map, true)
        }

        val functionId = currentToken.value

        consume() // Identifier

        return FunctionCall("", "_eval", hashMapOf("id" to StringLiteral(functionId)), false)
    }

    fun parseWhileLoop(): FunctionCall {
        if (!currentToken.compareToken("while"))
            throw ParseException("Expected identifier 'while' at the start of a while expression, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume()

        if (!currentToken.compareToken(TokenType.LPAREN))
            throw ParseException("Expected '(' after 'while' identifier, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume()

        val expr = parseExpression()

        if (!currentToken.compareToken(TokenType.RPAREN))
            throw ParseException("Expected ')' after while condition, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume()

        val blk = parseBlock()

        return FunctionCall("", "_while", hashMapOf("cond" to expr, "expr" to blk))
    }

    fun parseFunctionDefinition(): FunctionCall {
        if (!currentToken.compareToken("fun"))
            throw ParseException("Expected identifier 'fun' at the start of a function definition, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume() // 'fun'

        if (currentToken.type != TokenType.IDENTIFIER)
            throw ParseException("Expected function identifier after 'fun' keyword, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        val functionId = currentToken.value

        consume() // Function id

        var requiredParams = mutableListOf<String>()

        // Required parameters list (optional)
        if (currentToken.compareToken(TokenType.RGREATER)) {
            consume() // '<'

            while (true) {
                if (!currentToken.compareToken(TokenType.IDENTIFIER))
                    throw ParseException("Expected required property identifier, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

                requiredParams.add(currentToken.value)

                consume() // Identifier

                if (currentToken.compareToken(TokenType.COMMA)) {
                    consume()
                    continue
                }

                if (currentToken.compareToken(TokenType.LGREATER)) {
                    consume() // '>'
                    break
                }

                throw ParseException("Could not parse required parameters list: Expected '>' or ',' and more identifiers, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")
            }
        }

        var blk = parseBlock()

        if (requiredParams.isNotEmpty()) {
            var wrapperParams = hashMapOf<String, Node>()

            // Generate the __require_prop calls
            requiredParams.forEachIndexed { index, it ->
                wrapperParams.put(
                    index.toString(),
                    FunctionCall(
                        "",
                        "__require_prop",
                        hashMapOf(
                            "id" to StringLiteral(it),
                            "err" to StringLiteral("The property '$it' required for function '$functionId' was not present at the time of evaluation.")
                        )
                    )
                )
            }

            // Finally, insert the original function progn
            wrapperParams.put(requiredParams.size.toString(), blk)

            blk = FunctionCall(
                "", "progn", wrapperParams, true
            )
        }

        return FunctionCall(
            "", "_fun", hashMapOf(
                "id" to StringLiteral(functionId),
                "expr" to blk
            ), false
        )
    }

    fun parseStringLiteral(): StringLiteral {
        val value: String = when (currentToken.type) {
            TokenType.STRING_LITERAL,
            TokenType.NUMBER_LITERAL -> currentToken.value

            else -> throw ParseException("Could not parse string literal from token of type ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")
        }

        consume()

        return StringLiteral(value)
    }

    fun parseLoadInstruction(): Pair<String, String>? {
        if (!currentToken.compareToken(TokenType.AT))
            return null // We don't just throw an exception here because we want this operation to be non-invasive

        consume()

        if (!currentToken.compareToken("library"))
            throw ParseException("Expected 'library' keyword after the '@' prefix, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        consume()

        if (currentToken.type != TokenType.STRING_LITERAL)
            throw ParseException("Expected string literal (library path) after the 'library' keyword, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

        val path = currentToken.value

        consume()

        if (!currentToken.compareToken("as"))
            throw ParseException("Expected 'as' after library path, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}")

        consume()

        if (!currentToken.compareToken(TokenType.IDENTIFIER))
            throw ParseException("Expected prefix identifier after 'as' keyword, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}")

        val prefix = currentToken.value

        consume()

        return path to prefix
    }

    fun parseFunctionCall(): FunctionCall {
        if (!currentToken.compareToken(TokenType.IDENTIFIER))
            throw ParseException("Could not parse function call: Unexpected token of type ${currentToken.type} \"${currentToken.value}\" found on line ${currentToken.line} in place of the function identifier or library prefix.")

        val libPrefix: String

        // The token could either be a lib prefix or the function identifier

        if (nextToken.compareToken(TokenType.DOT)) {
            libPrefix = currentToken.value

            consume() // Skip the lib prefix

            if (!nextToken.compareToken(TokenType.IDENTIFIER))
                throw ParseException("Expected function identifier after lib prefix dot, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line} instead.")

            consume() // Skip ':'

        } else libPrefix = ""

        val functionId = currentToken.value

        consume()

        if (!currentToken.compareToken(TokenType.LPAREN) && !currentToken.compareToken(TokenType.LCURLY))
            throw ParseException("Could not parse function call of \"$functionId\": Expected '(' or '{' after function identifier, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

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
                    throw ParseException("Could not parse function call: Expected parameter identifier, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

                paramId = currentToken.value

                consume()

                if (!currentToken.compareToken(TokenType.EQUALS))
                    throw ParseException("Could not parse function call: Expected '=' after parameter identifier '$paramId', got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")

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

            if (!currentToken.compareToken(closingType)) {
                if (variadic)
                    continue
                throw ParseException("Could not parse function call: Expected '$closingChar' or ',' and more parameters, got ${currentToken.type} \"${currentToken.value}\" on line ${currentToken.line}.")
            }

            consume() // Skip ')' or '}'
            break
        }

        return FunctionCall(libPrefix, functionId, params, variadic = variadic)
    }

    companion object {
        fun parseScript(str: String): Script {
            val parser = Parser(Tokenizer(str))
            val libs = mutableListOf<Pair<String, String>>()
            while (true) {
                val lib = parser.parseLoadInstruction() ?: break
                libs.add(lib)
            }
            val expr = parser.parserEntrypoint()
            return Script(libs, expr)
        }
    }

}