package org.medaware.anterogradia.syntax.tokenizer

enum class TokenType(val value: String? = null) {

    UNDEFINED,

    LPAREN("("),
    RPAREN(")"),
    LBRACKET("["),
    RBRACKET("]"),
    LCURLY("{"),
    RCURLY("}"),

    IDENTIFIER,

    NUMBER_LITERAL,
    STRING_LITERAL,

    COMMA(","),
    MINUS("-"),
    PLUS("+"),
    ASTERISK("*"),
    SLASH("/"),
    LGREATER(">"),
    RGREATER("<"),
    SEMICOLON(";"),
    COLON(":"),
    DOLLARSIGN("$"),
    AMPERSAND("&"),
    EQUALS("="),
    VBAR("|"),
    HAT("^"),
    AT("@"),
    HASH("#"),
    DOT("."),
    EXCLAMATION("!"),

    // Complex tokens
    ASSIGN_RIGHT(":=")
}

fun findMatchingType(str: String): TokenType? {
    var match: TokenType? = null

    for (type in TokenType.entries) {
        if (type.value == null)
            continue

        if (str.startsWith(type.value) && ((match != null && type.value.length > match.value!!.length) || match == null))
            match = type
    }

    return match
}