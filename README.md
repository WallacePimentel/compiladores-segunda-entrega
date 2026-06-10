# Compilador Scheme → Python

Compilador que traduz código Scheme para Python. Implementado em Java com JFlex (scanner) e CUP (parser).

## Requisitos

- Java 11+
- Maven 3.6+

## Compilação

```bash
mvn clean compile package
```

O JAR executável será gerado em `target/compilador-scheme.jar`.

## Execução

```bash
java -cp target/compilador-scheme.jar org.example.Main <arquivo.scm>
```

**Exemplo:**
```bash
java -cp target/compilador-scheme.jar org.example.Main examples/test-completo.scm
```

A saída impressa no terminal contém os tokens encontrados, a AST gerada, o relatório de type checking e o código Python traduzido. A saída do scheme traduzido para arquivo python será no diretório examples.

## Estrutura

```
src/main/jflex/scanner.jflex   # Especificação léxica (JFlex)
src/main/cup/parser.cup        # Gramática (CUP)
src/main/java/org/example/
  ├── ASTNode.java             # Nós da AST
  ├── ASTVisitor.java          # Interface Visitor
  ├── TypeChecker.java         # Verificação de tipos
  ├── CodeGenerator.java       # Geração de código Python
  └── Main.java                # Ponto de entrada
examples/                      # Arquivos .scm de exemplo
```
