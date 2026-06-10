package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class CodeGenerator implements ASTVisitor {
    private StringBuilder code = new StringBuilder();
    private int indentLevel = 0;
    private int lambdaCounter = 0;
    private int condCounter = 0;
    private int tempCounter = 0;

    public String getCode() {
        return code.toString();
    }

    private void appendLine(String text) {
        for (int i = 0; i < indentLevel; i++) {
            code.append("    ");
        }
        code.append(text).append("\n");
    }

    private void indent() { indentLevel++; }
    private void dedent() { if (indentLevel > 0) indentLevel--; }

    private String sanitizeName(String name) {
        return name.replace("-", "_").replace("?", "_p").replace("!", "_b");
    }

    private void emitStatement(String expr) {
        if (expr != null && !expr.isEmpty()) {
            appendLine(expr);
        }
    }

    // ==================== Program ====================

    @Override
    public Object visit(ProgramNode node) {
        appendLine("if __name__ == \"__main__\":");
        indent();

        for (ASTNode expr : node.getExpressions()) {
            String result = (String) expr.accept(this);
            emitStatement(result);
        }

        dedent();
        return null;
    }

    // ==================== Literais ====================

    @Override public Object visit(NumberNode node)    { return node.getValue().toString(); }
    @Override public Object visit(StringNode node)    { return "\"" + node.getValue().replace("\"", "\\\"") + "\""; }
    @Override public Object visit(BooleanNode node)   { return node.getValue() ? "True" : "False"; }
    @Override public Object visit(CharacterNode node) { return "\"" + node.getValue() + "\""; }

    @Override
    public Object visit(IdentifierNode node) {
        return sanitizeName(node.getName());
    }

    // ==================== Quotes e Listas ====================

    @Override
    public Object visit(ListNode node) {
        List<ASTNode> elements = node.getElements();
        String items = elements.stream()
                .map(e -> (String) e.accept(this))
                .collect(Collectors.joining(", "));
        return "[" + items + "]";
    }

    @Override
    public Object visit(QuoteNode node) {
        // 'expr → representação literal em Python
        return quoteExpr(node.getExpr());
    }

    @Override
    public Object visit(QuasiquoteNode node) {
        return quoteExpr(node.getExpr());
    }

    @Override
    public Object visit(UnquoteNode node) {
        return (String) node.getExpr().accept(this);
    }

    @Override
    public Object visit(UnquoteSplicingNode node) {
        return "*" + node.getExpr().accept(this);
    }

    private String quoteExpr(ASTNode expr) {
        if (expr instanceof NumberNode)     return (String) expr.accept(this);
        if (expr instanceof StringNode)     return (String) expr.accept(this);
        if (expr instanceof BooleanNode)    return (String) expr.accept(this);
        if (expr instanceof CharacterNode)  return (String) expr.accept(this);
        if (expr instanceof IdentifierNode) return "\"" + ((IdentifierNode) expr).getName() + "\"";
        if (expr instanceof ListNode) {
            List<ASTNode> elements = ((ListNode) expr).getElements();
            String items = elements.stream()
                    .map(this::quoteExpr)
                    .collect(Collectors.joining(", "));
            return "[" + items + "]";
        }
        // fallback
        return (String) expr.accept(this);
    }

    // ==================== Definições ====================

    @Override
    public Object visit(DefineNode node) {
        String name = sanitizeName(node.getName());
        ASTNode value = node.getValue();

        if (value instanceof LambdaNode) {
            // (define (f x) body) → def f(x): ...
            emitLambdaAsDef(name, (LambdaNode) value);
        } else {
            String val = (String) value.accept(this);
            appendLine(name + " = " + val);
        }
        return null;
    }

    @Override
    public Object visit(FormalsNode node) {
        List<String> params = node.getParameters().stream()
                .map(this::sanitizeName)
                .collect(Collectors.toList());
        String result = String.join(", ", params);
        if (node.getRestParameter() != null) {
            if (!result.isEmpty()) result += ", ";
            result += "*" + sanitizeName(node.getRestParameter());
        }
        return result;
    }

    private String emitLambdaAsDef(String name, LambdaNode node) {
        String params = (String) node.getFormals().accept(this);
        appendLine("def " + name + "(" + params + "):");
        indent();
        emitBodyWithReturn(node.getBody());
        dedent();
        return name;
    }

    @Override
    public Object visit(LambdaNode node) {
        String lambdaName = "_lambda_" + (lambdaCounter++);
        emitLambdaAsDef(lambdaName, node);
        return lambdaName;
    }

    private void emitBodyWithReturn(List<ASTNode> body) {
        for (int i = 0; i < body.size() - 1; i++) {
            String stmt = (String) body.get(i).accept(this);
            emitStatement(stmt);
        }
        String last = (String) body.get(body.size() - 1).accept(this);
        if (last == null || last.isEmpty()) last = "None";
        appendLine("return " + last);
    }

    // ==================== Chamada de procedimento ====================

    @Override
    public Object visit(ProcedureCallNode node) {
        ASTNode proc = node.getProcedure();
        List<ASTNode> args = node.getArguments();

        String procName = (proc instanceof IdentifierNode)
                ? ((IdentifierNode) proc).getName()
                : (String) proc.accept(this);

        // Operadores aritméticos → infix Python
        if (args.size() >= 2) {
            switch (procName) {
                case "+": return generateBinaryOp("+", args);
                case "-": return generateBinaryOp("-", args);
                case "*": return generateBinaryOp("*", args);
                case "/": return generateBinaryOp("/", args);
                case "=":
                case "eq?":
                case "eqv?":
                case "equal?": return generateComparison("==", args);
                case "<":  return generateComparison("<", args);
                case ">":  return generateComparison(">", args);
                case "<=": return generateComparison("<=", args);
                case ">=": return generateComparison(">=", args);
            }
        }

        // Operador unário negativo
        if (args.size() == 1 && "-".equals(procName)) {
            return "(-" + args.get(0).accept(this) + ")";
        }

        // (list a b c) → [a, b, c]
        if ("list".equals(procName)) {
            String items = args.stream()
                    .map(a -> (String) a.accept(this))
                    .collect(Collectors.joining(", "));
            return "[" + items + "]";
        }

        // Chamada genérica
        String callArgs = args.stream()
                .map(a -> (String) a.accept(this))
                .collect(Collectors.joining(", "));
        return sanitizeName(procName) + "(" + callArgs + ")";
    }

    private String generateBinaryOp(String op, List<ASTNode> args) {
        String joined = args.stream()
                .map(a -> (String) a.accept(this))
                .collect(Collectors.joining(" " + op + " "));
        return "(" + joined + ")";
    }

    private String generateComparison(String op, List<ASTNode> args) {
        if (args.size() == 2) {
            return "(" + args.get(0).accept(this) + " " + op + " " + args.get(1).accept(this) + ")";
        }
        // Encadeia comparações: (= a b c) → (a == b == c)
        String joined = args.stream()
                .map(a -> (String) a.accept(this))
                .collect(Collectors.joining(" " + op + " "));
        return "(" + joined + ")";
    }

    // ==================== Operadores lógicos ====================

    @Override
    public Object visit(AndNode node) {
        if (node.getExpressions().isEmpty()) return "True";
        String joined = node.getExpressions().stream()
                .map(e -> (String) e.accept(this))
                .collect(Collectors.joining(" and "));
        return "(" + joined + ")";
    }

    @Override
    public Object visit(OrNode node) {
        if (node.getExpressions().isEmpty()) return "False";
        String joined = node.getExpressions().stream()
                .map(e -> (String) e.accept(this))
                .collect(Collectors.joining(" or "));
        return "(" + joined + ")";
    }

    // ==================== Fluxo de controle ====================

    @Override
    public Object visit(IfNode node) {
        String test       = (String) node.getTest().accept(this);
        String thenClause = (String) node.getThenClause().accept(this);
        String elseClause = node.getElseClause() != null
                ? (String) node.getElseClause().accept(this)
                : "None";
        return "(" + thenClause + " if " + test + " else " + elseClause + ")";
    }

    @Override
    public Object visit(BeginNode node) {
        List<ASTNode> exprs = node.getExpressions();
        // Todos os statements intermediários são emitidos diretamente
        for (int i = 0; i < exprs.size() - 1; i++) {
            String stmt = (String) exprs.get(i).accept(this);
            emitStatement(stmt);
        }
        // A última expressão é retornada para o contexto externo
        String last = (String) exprs.get(exprs.size() - 1).accept(this);
        return (last == null || last.isEmpty()) ? "None" : last;
    }

    @Override
    public Object visit(SetNode node) {
        String name  = sanitizeName(node.getName());
        String value = (String) node.getValue().accept(this);
        appendLine(name + " = " + value);
        return null;
    }

    @Override
    public Object visit(DelayNode node) {
        return "(lambda: " + node.getExpression().accept(this) + ")";
    }

    // ==================== Let / Let* / Letrec ====================

    private String processLetBindings(List<ASTNode> bindings, List<ASTNode> body) {
        for (ASTNode b : bindings) {
            BindingNode binding = (BindingNode) b;
            ASTNode val = binding.getValue();
            if (val instanceof LambdaNode) {
                emitLambdaAsDef(sanitizeName(binding.getName()), (LambdaNode) val);
            } else {
                String value = (String) val.accept(this);
                appendLine(sanitizeName(binding.getName()) + " = " + value);
            }
        }
        // Statements intermediários do corpo
        for (int i = 0; i < body.size() - 1; i++) {
            String stmt = (String) body.get(i).accept(this);
            emitStatement(stmt);
        }
        // Última expressão retornada para o contexto externo
        String last = (String) body.get(body.size() - 1).accept(this);
        return (last == null || last.isEmpty()) ? "None" : last;
    }

    @Override public Object visit(BindingNode node) {
        return sanitizeName(node.getName()) + " = " + node.getValue().accept(this);
    }

    @Override public Object visit(LetNode node)     { return processLetBindings(node.getBindings(), node.getBody()); }
    @Override public Object visit(LetStarNode node) { return processLetBindings(node.getBindings(), node.getBody()); }
    @Override public Object visit(LetRecNode node)  { return processLetBindings(node.getBindings(), node.getBody()); }

    @Override
    public Object visit(NamedLetNode node) {
        String funcName = sanitizeName(node.getName());
        List<String> params = new ArrayList<>();
        List<String> initValues = new ArrayList<>();

        for (ASTNode b : node.getBindings()) {
            BindingNode binding = (BindingNode) b;
            params.add(sanitizeName(binding.getName()));
            initValues.add((String) binding.getValue().accept(this));
        }

        appendLine("def " + funcName + "(" + String.join(", ", params) + "):");
        indent();
        emitBodyWithReturn(node.getBody());
        dedent();

        return funcName + "(" + String.join(", ", initValues) + ")";
    }

    // ==================== Cond ====================

    @Override
    public Object visit(CondNode node) {
        int condId = condCounter++;
        String resultVar = "_cond_res_" + condId;

        appendLine(resultVar + " = None");
        boolean isFirst = true;

        for (ASTNode clauseNode : node.getClauses()) {
            if (clauseNode instanceof CondClauseNode) {
                CondClauseNode clause = (CondClauseNode) clauseNode;
                String test = (String) clause.getTest().accept(this);

                appendLine((isFirst ? "if " : "elif ") + test + ":");
                indent();
                List<ASTNode> body = clause.getBody();
                if (body.isEmpty()) {
                    appendLine(resultVar + " = " + test);
                } else {
                    for (int i = 0; i < body.size() - 1; i++) {
                        String stmt = (String) body.get(i).accept(this);
                        emitStatement(stmt);
                    }
                    String last = (String) body.get(body.size() - 1).accept(this);
                    appendLine(resultVar + " = " + (last == null || last.isEmpty() ? "None" : last));
                }
                dedent();
                isFirst = false;

            } else if (clauseNode instanceof CondArrowNode) {
                CondArrowNode arrow = (CondArrowNode) clauseNode;
                String test = (String) arrow.getTest().accept(this);
                String func = (String) arrow.getFunc().accept(this);

                String tempVar = "_test_tmp_" + (tempCounter++);
                appendLine((isFirst ? "if (" : "elif (") + tempVar + " := " + test + "):");
                indent();
                appendLine(resultVar + " = " + func + "(" + tempVar + ")");
                dedent();
                isFirst = false;

            } else if (clauseNode instanceof CondElseNode) {
                CondElseNode elseClause = (CondElseNode) clauseNode;
                appendLine("else:");
                indent();
                List<ASTNode> body = elseClause.getBody();
                for (int i = 0; i < body.size() - 1; i++) {
                    String stmt = (String) body.get(i).accept(this);
                    emitStatement(stmt);
                }
                String last = (String) body.get(body.size() - 1).accept(this);
                appendLine(resultVar + " = " + (last == null || last.isEmpty() ? "None" : last));
                dedent();
            }
        }
        return resultVar;
    }

    @Override public Object visit(CondClauseNode node)  { return null; }
    @Override public Object visit(CondArrowNode node)   { return null; }
    @Override public Object visit(CondElseNode node)    { return null; }
    @Override public Object visit(CaseNode node)        { return null; }
    @Override public Object visit(CaseClauseNode node)  { return null; }

    // ==================== Do ====================

    @Override
    public Object visit(DoBindingNode node) { return null; }

    @Override
    public Object visit(DoNode node) {
        // Inicialização das variáveis
        for (ASTNode bNode : node.getBindings()) {
            DoBindingNode b = (DoBindingNode) bNode;
            String init = (String) b.getInit().accept(this);
            appendLine(sanitizeName(b.getName()) + " = " + init);
        }

        String testCond = (String) node.getTest().accept(this);
        appendLine("while not (" + testCond + "):");
        indent();

        // Corpo do loop
        if (node.getBody() != null) {
            for (ASTNode expr : node.getBody()) {
                String stmt = (String) expr.accept(this);
                emitStatement(stmt);
            }
        }

        // Calcular próximos valores antes de atribuir (evita dependência de ordem)
        List<String> stepsToUpdate = new ArrayList<>();
        for (ASTNode bNode : node.getBindings()) {
            DoBindingNode b = (DoBindingNode) bNode;
            if (b.getStep() != null) {
                String step = (String) b.getStep().accept(this);
                String tmp  = "_next_" + sanitizeName(b.getName());
                appendLine(tmp + " = " + step);
                stepsToUpdate.add(sanitizeName(b.getName()) + " = " + tmp);
            }
        }
        for (String upd : stepsToUpdate) {
            appendLine(upd);
        }

        dedent();

        // Expressão de resultado após o loop
        if (node.getTestBody() != null && !node.getTestBody().isEmpty()) {
            List<ASTNode> testBody = node.getTestBody();
            for (int i = 0; i < testBody.size() - 1; i++) {
                String stmt = (String) testBody.get(i).accept(this);
                emitStatement(stmt);
            }
            String last = (String) testBody.get(testBody.size() - 1).accept(this);
            return (last == null || last.isEmpty()) ? "None" : last;
        }
        return null;
    }
}
