package org.example;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Compilador Scheme -> Python");
            System.out.println("Uso: java -cp target/compilador-scheme.jar org.example.Main <arquivo.scm>");
            System.out.println("\nExemplos:");
            System.out.println("  java -cp ... org.example.Main test.scm");
            System.exit(1);
        }

        String filename = args[0];
        File file = new File(filename);

        if (!file.exists()) {
            System.err.println("Erro: Arquivo não encontrado: " + filename);
            System.exit(1);
        }

        try {
            System.out.println("=== Compilador Scheme -> Python ===");
            System.out.println("Arquivo: " + filename);
            System.out.println();

            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader);

            System.out.println("--- FASE 1: SCANNER (Análise Léxica) ---");
            System.out.println("Tokens encontrados:");

            java_cup.runtime.Symbol symbol;
            int tokenCount = 0;

            // Coletar e imprimir os tokens
            java.util.List<java_cup.runtime.Symbol> tokens = new java.util.ArrayList<>();
            while ((symbol = scanner.next_token()).sym != sym.EOF) {
                tokens.add(symbol);
                tokenCount++;
                System.out.println("  [" + tokenCount + "] " + formatToken(symbol));
            }
            tokens.add(symbol); // Adiciona o EOF

            System.out.println("\nTotal de tokens: " + tokenCount);
            System.out.println();
            fileReader.close();

            // Recriar o scanner para reiniciar a leitura do arquivo para o Parser
            fileReader = new FileReader(file);
            scanner = new Scanner(fileReader);

            System.out.println("--- FASE 2: PARSER (Análise Sintática) ---");
            parser p = new parser(scanner);

            java_cup.runtime.Symbol parseResult;
            ProgramNode ast = null;

            try {
                System.out.println("Parser iniciado...");
                parseResult = p.parse();
                System.out.println("Parsing concluído com sucesso!");
                System.out.println();

                if (parseResult != null && parseResult.value != null) {
                    System.out.println("--- AST (Árvore de Sintaxe Abstrata) ---");
                    System.out.println(parseResult.value);

                    // Converter para ProgramNode
                    if (parseResult.value instanceof ProgramNode) {
                        ast = (ProgramNode) parseResult.value;
                    }
                } else {
                    System.out.println("Aviso: AST vazio ou nulo");
                }
            } catch (Exception e) {
                System.err.println("Erro no parsing:");
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.exit(1);
            } finally {
                fileReader.close();
            }

            if (ast != null) {
                System.out.println("\n--- FASE 3: Verificação de Tipos ---");

                TypeChecker typeChecker = new TypeChecker();
                ast.accept(typeChecker);

                System.out.println(typeChecker.getReport());

                if (typeChecker.hasErrors()) {
                    System.err.println("Erros de tipo encontrados. A compilação será abortada.");
                    System.exit(1);
                } else {
                    System.out.println("Verificação de tipos concluída com sucesso!");
                }
            }

            if (ast != null) {
                System.out.println("\n--- FASE 4: Geração de Código Python ---");

                CodeGenerator codegen = new CodeGenerator();
                ast.accept(codegen);

                String pythonCode = codegen.getCode();

                System.out.println("\n=== PYTHON GERADO ===");
                System.out.println(pythonCode);
                System.out.println("Geração de código concluída com sucesso!");

                // Salvar código Python no mesmo diretório com extensão .py
                String pythonFilename = filename.replaceAll("\\.scm$", ".py");
                try (FileWriter fw = new FileWriter(pythonFilename)) {
                    fw.write(pythonCode);
                    System.out.println("\nCódigo Python salvo em: " + pythonFilename);
                } catch (IOException e) {
                    System.err.println("Erro ao salvar arquivo Python: " + e.getMessage());
                }
            }

            System.out.println("\n=== Compilação concluída ===");

        } catch (FileNotFoundException e) {
            System.err.println("Erro: Arquivo não encontrado: " + filename);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String formatToken(java_cup.runtime.Symbol symbol) {
        String typeName = getTokenTypeName(symbol.sym);

        if (symbol instanceof Token) {
            Token t = (Token) symbol;
            String valStr = t.getValue() != null ? ", valor=" + t.getValue() : "";
            return String.format("%-15s [lexema='%s'%s] (linha %d, col %d)",
                    typeName, t.getLexeme(), valStr, t.getLine(), t.getColumn());
        }

        String value = symbol.value != null ? " = " + symbol.value : "";
        return typeName + value + " (linha " + symbol.left + ", col " + symbol.right + ")";
    }

    private static String getTokenTypeName(int sym) {
        switch (sym) {
            case org.example.sym.NUMBER: return "NUMBER";
            case org.example.sym.IDENTIFIER: return "IDENTIFIER";
            case org.example.sym.STRING: return "STRING";
            case org.example.sym.CHARACTER: return "CHARACTER";
            case org.example.sym.TRUE: return "TRUE";
            case org.example.sym.FALSE: return "FALSE";
            case org.example.sym.DEFINE: return "DEFINE";
            case org.example.sym.LAMBDA: return "LAMBDA";
            case org.example.sym.IF: return "IF";
            case org.example.sym.BEGIN: return "BEGIN";
            case org.example.sym.SET: return "SET";

            // Novas palavras-chave adicionadas
            case org.example.sym.LET: return "LET";
            case org.example.sym.LET_STAR: return "LET_STAR";
            case org.example.sym.LETREC: return "LETREC";
            case org.example.sym.COND: return "COND";
            case org.example.sym.ELSE: return "ELSE";
            case org.example.sym.AND: return "AND";
            case org.example.sym.OR: return "OR";
            case org.example.sym.DO: return "DO";
            case org.example.sym.DELAY: return "DELAY";

            // Delimitadores e operadores
            case org.example.sym.LPAREN: return "LPAREN";
            case org.example.sym.RPAREN: return "RPAREN";
            case org.example.sym.QUOTE_CHAR: return "QUOTE";
            case org.example.sym.BACKQUOTE: return "BACKQUOTE";
            case org.example.sym.COMMA: return "COMMA";
            case org.example.sym.COMMA_AT: return "COMMA_AT";
            case org.example.sym.ARROW: return "ARROW";
            case org.example.sym.DOT: return "DOT";
            case org.example.sym.EOF: return "EOF";

            default: return "UNKNOWN(" + sym + ")";
        }
    }
}