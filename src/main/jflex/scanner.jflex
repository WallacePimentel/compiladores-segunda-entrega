/* Scanner JFlex para Scheme */

package org.example;

import java.io.*;
import java_cup.runtime.*;

%%

%class Scanner
%function next_token
%type Symbol
%implements java_cup.runtime.Scanner
%line
%column
%unicode

%{
    private Symbol symbol(int type) {
        return new Token(type, yytext(), yyline + 1, yycolumn + 1);
    }

    private Symbol symbol(int type, Object value) {
        return new Token(type, value, yytext(), yyline + 1, yycolumn + 1);
    }
%}

/* Expressões regulares */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace = [ \t\f] | {LineTerminator}

/* Comentários */
Comment = ";" {InputCharacter}*

/* Identificadores gerais do Scheme */
Identifier = [a-zA-Z_+\-*/?!<>=][a-zA-Z0-9_+\-*/?!<>=]*

/* Números */
Sign = "+" | "-"
Digit = [0-9]
HexDigit = [0-9a-fA-F]
OctalDigit = [0-7]
BinaryDigit = [0-1]

Integer = {Sign}? {Digit}+
Hex = {Sign}? "#x" {HexDigit}+
Octal = {Sign}? "#o" {OctalDigit}+
Binary = {Sign}? "#b" {BinaryDigit}+
Float = {Sign}? {Digit}+ "." {Digit}+ ([eE] {Sign}? {Digit}+)?
Scientific = {Sign}? {Digit}+ [eE] {Sign}? {Digit}+

/* Strings */
StringCharacter = [^\"\\\r\n]
EscapeSequence = "\\\\" | "\\\"" | "\\n" | "\\t" | "\\r"
String = "\"" ({StringCharacter} | {EscapeSequence})* "\""

/* Caracteres */
Character = "#\\" ([a-zA-Z0-9] | "space" | "newline" | "tab" | "return")

%state NORMAL

%%

/* Ignorar espaços em branco e comentários */
{WhiteSpace}        { /* ignorar */ }
{Comment}           { /* ignorar comentário */ }

/* Palavras-chave e formas especiais */
"define"            { return symbol(sym.DEFINE); }
"lambda"            { return symbol(sym.LAMBDA); }
"if"                { return symbol(sym.IF); }
"begin"             { return symbol(sym.BEGIN); }
"set!"              { return symbol(sym.SET); }
"let"               { return symbol(sym.LET); }
"let*"              { return symbol(sym.LET_STAR); }
"letrec"            { return symbol(sym.LETREC); }
"cond"              { return symbol(sym.COND); }
"else"              { return symbol(sym.ELSE); }
"and"               { return symbol(sym.AND); }
"or"                { return symbol(sym.OR); }
"do"                { return symbol(sym.DO); }
"delay"             { return symbol(sym.DELAY); }

/* Literais booleanos */
"#t"                { return symbol(sym.TRUE, Boolean.TRUE); }
"#f"                { return symbol(sym.FALSE, Boolean.FALSE); }

/* Números */
{Binary}            { return symbol(sym.NUMBER, Integer.parseInt(yytext().substring(3), 2)); }
{Octal}             { return symbol(sym.NUMBER, Integer.parseInt(yytext().substring(3), 8)); }
{Hex}               { return symbol(sym.NUMBER, Integer.parseInt(yytext().substring(2), 16)); }
{Float}             { return symbol(sym.NUMBER, Double.parseDouble(yytext())); }
{Scientific}        { return symbol(sym.NUMBER, Double.parseDouble(yytext())); }
{Integer}           { return symbol(sym.NUMBER, Integer.parseInt(yytext())); }

/* Strings e caracteres */
{String}            {
    String str = yytext();
    String value = str.substring(1, str.length() - 1);
    return symbol(sym.STRING, value);
}

{Character}         {
    String str = yytext().substring(2);
    return symbol(sym.CHARACTER, str);
}

/* Símbolos específicos / Operadores */
"=>"                { return symbol(sym.ARROW); }
"."                 { return symbol(sym.DOT); }

/* Identificadores genéricos */
{Identifier}        { return symbol(sym.IDENTIFIER, yytext()); }

/* Delimitadores e Quoting */
"("                 { return symbol(sym.LPAREN); }
")"                 { return symbol(sym.RPAREN); }
"'"                 { return symbol(sym.QUOTE_CHAR); }
"`"                 { return symbol(sym.BACKQUOTE); }
","                 { return symbol(sym.COMMA); }
",@"                { return symbol(sym.COMMA_AT); }

/* Fim de arquivo */
<<EOF>>            { return symbol(sym.EOF); }

/* Tratamento de erros léxicos */
.                   {
    throw new RuntimeException("Erro Léxico: Caractere inesperado '" + yytext() +
        "' na linha " + (yyline + 1) + ", coluna " + (yycolumn + 1));
}