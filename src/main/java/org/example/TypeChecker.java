package org.example;

import java.util.*;


public class TypeChecker implements ASTVisitor {
    private SymbolTable symbols;
    private List<String> errors;
    private List<String> warnings;

    public TypeChecker() {
        this.symbols = new SymbolTable();
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();

        // Registrar built-in procedures globais
        String[] builtins = {"+", "-", "*", "/", "=", "<", ">", "<=", ">=",
                "eq?", "eqv?", "equal?", "list", "cons", "car", "cdr",
                "null?", "number?", "string?", "boolean?", "pair?",
                "print", "display", "newline", "force"};
        for (String builtin : builtins) {
            symbols.define(builtin, Type.PROCEDURE);
        }
    }

    public SymbolTable getSymbolTable() { return symbols; }
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }

    public void addError(String message) { errors.add("Error: " + message); }
    public void addWarning(String message) { warnings.add("Warning: " + message); }
    public boolean hasErrors() { return !errors.isEmpty(); }

    // ==================== Visitantes Primitivos ====================

    @Override
    public Object visit(ProgramNode node) {
        for (ASTNode expr : node.getExpressions()) expr.accept(this);
        return Type.ANY;
    }

    @Override public Object visit(NumberNode node) { return Type.NUMBER; }
    @Override public Object visit(StringNode node) { return Type.STRING; }
    @Override public Object visit(CharacterNode node) { return Type.CHARACTER; }
    @Override public Object visit(BooleanNode node) { return Type.BOOLEAN; }

    @Override
    public Object visit(IdentifierNode node) {
        String name = node.getName();
        Type type = symbols.lookup(name);
        if (type == null) {
            addError("Variável não definida: '" + name + "'");
            return Type.UNDEFINED;
        }
        return type;
    }

    // ==================== Quotes e Listas ====================

    @Override public Object visit(ListNode node) { return Type.LIST; }
    @Override public Object visit(QuoteNode node) { return Type.ANY; }
    @Override public Object visit(QuasiquoteNode node) { return Type.ANY; }
    @Override public Object visit(UnquoteNode node) { return Type.ANY; }
    @Override public Object visit(UnquoteSplicingNode node) { return Type.ANY; }

    // ==================== Funções e Definições ====================

    @Override
    public Object visit(DefineNode node) {
        String name = node.getName();
        Type valueType = (Type) node.getValue().accept(this);
        symbols.define(name, valueType);
        return Type.ANY;
    }

    @Override
    public Object visit(FormalsNode node) {
        // Registra os parâmetros do Lambda no escopo atual
        for (String param : node.getParameters()) {
            symbols.define(param, Type.ANY);
        }
        if (node.getRestParameter() != null) {
            symbols.define(node.getRestParameter(), Type.LIST);
        }
        return Type.ANY;
    }

    @Override
    public Object visit(LambdaNode node) {
        symbols.pushScope();
        node.getFormals().accept(this);

        Type lastType = Type.ANY;
        for (ASTNode expr : node.getBody()) {
            lastType = (Type) expr.accept(this);
        }
        symbols.popScope();
        return Type.PROCEDURE;
    }

    @Override
    public Object visit(ProcedureCallNode node) {
        ASTNode func = node.getProcedure();
        Type funcType = (Type) func.accept(this);

        for (ASTNode arg : node.getArguments()) {
            arg.accept(this);
        }

        if (func instanceof IdentifierNode) {
            String funcName = ((IdentifierNode) func).getName();
            Type varType = symbols.lookup(funcName);
            if (varType != null && varType != Type.PROCEDURE && varType != Type.ANY && varType != Type.UNDEFINED) {
                addError("'" + funcName + "' não é um procedimento (tipo: " + varType + ")");
            }
        }
        return Type.ANY;
    }

    // ==================== Controle de Fluxo ====================

    @Override
    public Object visit(IfNode node) {
        Type testType = (Type) node.getTest().accept(this);
        if (testType != Type.BOOLEAN && testType != Type.ANY && testType != Type.UNDEFINED) {
            addWarning("Condição IF costuma ser booleana, mas avaliada como " + testType);
        }

        Type thenType = (Type) node.getThenClause().accept(this);
        Type elseType = (Type) node.getElseClause().accept(this);

        return (thenType == elseType) ? thenType : Type.ANY;
    }

    @Override
    public Object visit(CondNode node) {
        for (ASTNode clause : node.getClauses()) clause.accept(this);
        return Type.ANY;
    }

    @Override
    public Object visit(CondClauseNode node) {
        node.getTest().accept(this);
        for (ASTNode expr : node.getBody()) expr.accept(this);
        return Type.ANY;
    }

    @Override
    public Object visit(CondArrowNode node) {
        node.getTest().accept(this);
        Type funcType = (Type) node.getFunc().accept(this);
        if (funcType != Type.PROCEDURE && funcType != Type.ANY) {
            addWarning("O lado direito de '=>' em um cond deve ser um procedimento.");
        }
        return Type.ANY;
    }

    @Override
    public Object visit(CondElseNode node) {
        for (ASTNode expr : node.getBody()) expr.accept(this);
        return Type.ANY;
    }

    @Override
    public Object visit(CaseNode node) {
        node.getKey().accept(this);
        for (ASTNode clause : node.getClauses()) clause.accept(this);
        return Type.ANY;
    }

    @Override
    public Object visit(CaseClauseNode node) {
        node.getResult().accept(this);
        return Type.ANY;
    }

    @Override
    public Object visit(AndNode node) {
        for (ASTNode expr : node.getExpressions()) expr.accept(this);
        return Type.ANY;
    }

    @Override
    public Object visit(OrNode node) {
        for (ASTNode expr : node.getExpressions()) expr.accept(this);
        return Type.ANY;
    }

    // ==================== Blocos, Set e Bindings ====================

    @Override
    public Object visit(BeginNode node) {
        Type lastType = Type.ANY;
        for (ASTNode expr : node.getExpressions()) {
            lastType = (Type) expr.accept(this);
        }
        return lastType;
    }

    @Override
    public Object visit(SetNode node) {
        if (!symbols.isDefined(node.getName())) {
            addError("Não é possível fazer set! de variável não definida: '" + node.getName() + "'");
            return Type.UNDEFINED;
        }
        node.getValue().accept(this);
        return Type.ANY;
    }

    @Override
    public Object visit(BindingNode node) {
        return node.getValue().accept(this);
    }

    @Override
    public Object visit(LetNode node) {
        symbols.pushScope();
        for (ASTNode b : node.getBindings()) {
            BindingNode binding = (BindingNode) b;
            Type valueType = (Type) binding.accept(this);
            symbols.define(binding.getName(), valueType);
        }
        Type lastType = Type.ANY;
        for (ASTNode expr : node.getBody()) lastType = (Type) expr.accept(this);
        symbols.popScope();
        return lastType;
    }

    @Override
    public Object visit(NamedLetNode node) {
        symbols.pushScope();
        symbols.define(node.getName(), Type.PROCEDURE); // Permite recursão

        for (ASTNode b : node.getBindings()) {
            BindingNode binding = (BindingNode) b;
            Type valueType = (Type) binding.accept(this);
            symbols.define(binding.getName(), valueType);
        }

        Type lastType = Type.ANY;
        for (ASTNode expr : node.getBody()) lastType = (Type) expr.accept(this);
        symbols.popScope();
        return lastType;
    }

    @Override
    public Object visit(LetStarNode node) {
        symbols.pushScope();
        for (ASTNode b : node.getBindings()) {
            BindingNode binding = (BindingNode) b;
            Type valueType = (Type) binding.accept(this);
            symbols.define(binding.getName(), valueType); // Cada binding já vê o anterior no mesmo escopo
        }
        Type lastType = Type.ANY;
        for (ASTNode expr : node.getBody()) lastType = (Type) expr.accept(this);
        symbols.popScope();
        return lastType;
    }

    @Override
    public Object visit(LetRecNode node) {
        symbols.pushScope();
        for (ASTNode b : node.getBindings()) {
            symbols.define(((BindingNode)b).getName(), Type.ANY);
        }
        for (ASTNode b : node.getBindings()) {
            BindingNode binding = (BindingNode) b;
            symbols.define(binding.getName(), (Type) binding.accept(this));
        }
        Type lastType = Type.ANY;
        for (ASTNode expr : node.getBody()) lastType = (Type) expr.accept(this);
        symbols.popScope();
        return lastType;
    }

    // ==================== Iteração e Lógica Ociosa ====================

    @Override
    public Object visit(DoBindingNode node) {
        return node.getInit().accept(this);
    }

    @Override
    public Object visit(DoNode node) {
        //Avalia as inicializações no escopo ANTERIOR
        List<Type> initTypes = new ArrayList<>();
        for (ASTNode b : node.getBindings()) {
            DoBindingNode binding = (DoBindingNode) b;
            initTypes.add((Type) binding.accept(this));
        }

        //Cria o novo escopo para o loop DO
        symbols.pushScope();

        //Define as variáveis no novo escopo
        int index = 0;
        for (ASTNode b : node.getBindings()) {
            DoBindingNode binding = (DoBindingNode) b;
            symbols.define(binding.getName(), initTypes.get(index++));
        }

        //Avalia os incrementos, agora que as variáveis já existem
        for (ASTNode b : node.getBindings()) {
            DoBindingNode binding = (DoBindingNode) b;
            if (binding.getStep() != null) {
                binding.getStep().accept(this);
            }
        }

        //Avalia a condição e os corpos
        if (node.getTest() != null) {
            node.getTest().accept(this);
        }

        if (node.getTestBody() != null) {
            for (ASTNode expr : node.getTestBody()) expr.accept(this);
        }

        if (node.getBody() != null) {
            for (ASTNode expr : node.getBody()) expr.accept(this);
        }

        symbols.popScope();
        return Type.ANY;
    }

    @Override
    public Object visit(DelayNode node) {
        node.getExpression().accept(this);
        return Type.PROMISE;
    }

    // ==================== Relatório ====================

    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TYPE CHECKING REPORT ===\n\n");
        if (errors.isEmpty() && warnings.isEmpty()) {
            sb.append("Sem erros ou avisos de tipo\n");
        } else {
            if (!errors.isEmpty()) {
                sb.append("ERRORS (").append(errors.size()).append("):\n");
                for (String error : errors) sb.append("  ").append(error).append("\n");
            }
            if (!warnings.isEmpty()) {
                sb.append("\nWARNINGS (").append(warnings.size()).append("):\n");
                for (String warning : warnings) sb.append("  ").append(warning).append("\n");
            }
        }
        return sb.toString();
    }
}