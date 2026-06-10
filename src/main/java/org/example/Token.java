package org.example;

import java_cup.runtime.Symbol;

public class Token extends Symbol {
    private String lexeme;

    public Token(int type, Object value, String lexeme, int line, int column) {
        super(type, line, column, value);
        this.lexeme = lexeme;
    }

    public Token(int type, String lexeme, int line, int column) {
        this(type, lexeme, lexeme, line, column);
    }

    public int getType() {
        return sym;
    }

    public Object getValue() {
        return value;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return left;
    }

    public int getColumn() {
        return right;
    }

    @Override
    public String toString() {
        return String.format("Token{type=%d, value=%s, lexeme='%s', line=%d, column=%d}",
                sym, value, lexeme, left, right);
    }
}