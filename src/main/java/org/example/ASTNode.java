package org.example;

import java.util.ArrayList;
import java.util.List;


public abstract class ASTNode {
    abstract public Object accept(ASTVisitor visitor);

    @Override
    public abstract String toString();
}


// Nó para um programa (sequência de expressões)
class ProgramNode extends ASTNode {
    private List<ASTNode> expressions;

    public ProgramNode() {
        this.expressions = new ArrayList<>();
    }

    public void addExpression(ASTNode expr) {
        expressions.add(expr);
    }

    public List<ASTNode> getExpressions() {
        return expressions;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Program" + expressions;
    }
}

// Literais e Tipos Primitivos
class NumberNode extends ASTNode {
    private Number value;
    public NumberNode(Number value) { this.value = value; }
    public Number getValue() { return value; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return value.toString(); }
}

class StringNode extends ASTNode {
    private String value;
    public StringNode(String value) { this.value = value; }
    public String getValue() { return value; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "\"" + value + "\""; }
}

class BooleanNode extends ASTNode {
    private boolean value;
    public BooleanNode(boolean value) { this.value = value; }
    public boolean getValue() { return value; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return value ? "#t" : "#f"; }
}

class CharacterNode extends ASTNode {
    private String value;
    public CharacterNode(String value) { this.value = value; }
    public String getValue() { return value; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "#\\" + value; }
}

class IdentifierNode extends ASTNode {
    private String name;
    public IdentifierNode(String name) { this.name = name; }
    public String getName() { return name; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return name; }
}

// Quotes e Listas

class ListNode extends ASTNode {
    private List<ASTNode> elements;
    public ListNode() { this.elements = new ArrayList<>(); }
    public ListNode(List<ASTNode> elements) { this.elements = new ArrayList<>(elements); }
    public List<ASTNode> getElements() { return elements; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "'" + elements; }
}

class QuoteNode extends ASTNode {
    private ASTNode expr;
    public QuoteNode(ASTNode expr) { this.expr = expr; }
    public ASTNode getExpr() { return expr; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "'" + expr; }
}

class QuasiquoteNode extends ASTNode {
    private ASTNode expr;
    public QuasiquoteNode(ASTNode expr) { this.expr = expr; }
    public ASTNode getExpr() { return expr; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "`" + expr; }
}

class UnquoteNode extends ASTNode {
    private ASTNode expr;
    public UnquoteNode(ASTNode expr) { this.expr = expr; }
    public ASTNode getExpr() { return expr; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "," + expr; }
}

class UnquoteSplicingNode extends ASTNode {
    private ASTNode expr;
    public UnquoteSplicingNode(ASTNode expr) { this.expr = expr; }
    public ASTNode getExpr() { return expr; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return ",@" + expr; }
}

// Procedimentos e Parâmetros
class ProcedureCallNode extends ASTNode {
    private ASTNode procedure;
    private List<ASTNode> arguments;
    public ProcedureCallNode(ASTNode procedure, List<ASTNode> arguments) {
        this.procedure = procedure;
        this.arguments = arguments;
    }
    public ASTNode getProcedure() { return procedure; }
    public List<ASTNode> getArguments() { return arguments; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(" + procedure + " " + arguments + ")"; }
}

// Nó para os parâmetros (formals) de um lambda.
class FormalsNode extends ASTNode {
    private List<String> parameters = new ArrayList<>();
    private String restParameter = null;

    // lambda args
    public FormalsNode(String restParameter) { this.restParameter = restParameter; }

    // lambda (a b c)
    public FormalsNode(List<String> parameters) { this.parameters = parameters; }

    // lambda (a b . c)
    public FormalsNode(List<String> parameters, String restParameter) {
        this.parameters = parameters;
        this.restParameter = restParameter;
    }

    public List<String> getParameters() { return parameters; }
    public String getRestParameter() { return restParameter; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "Formals(" + parameters + (restParameter != null ? " . " + restParameter : "") + ")"; }
}

class LambdaNode extends ASTNode {
    private FormalsNode formals;
    private List<ASTNode> body; // Modificado para lista de nós para suportar begin implícito

    @SuppressWarnings("unchecked")
    public LambdaNode(FormalsNode formals, List<?> body) {
        this.formals = formals;
        this.body = (List<ASTNode>) body;
    }
    public FormalsNode getFormals() { return formals; }
    public List<ASTNode> getBody() { return body; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(lambda " + formals + " " + body + ")"; }
}

// Estruturas de Controle (Define, Set, If, Begin)

class DefineNode extends ASTNode {
    private String name;
    private ASTNode value;
    public DefineNode(String name, ASTNode value) { this.name = name; this.value = value; }
    public String getName() { return name; }
    public ASTNode getValue() { return value; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(define " + name + " " + value + ")"; }
}

class SetNode extends ASTNode {
    private String name;
    private ASTNode value;
    public SetNode(String name, ASTNode value) { this.name = name; this.value = value; }
    public String getName() { return name; }
    public ASTNode getValue() { return value; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(set! " + name + " " + value + ")"; }
}

class BeginNode extends ASTNode {
    private List<ASTNode> expressions;
    @SuppressWarnings("unchecked")
    public BeginNode(List<?> expressions) { this.expressions = (List<ASTNode>) expressions; }
    public List<ASTNode> getExpressions() { return expressions; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(begin " + expressions + ")"; }
}

class IfNode extends ASTNode {
    private ASTNode test;
    private ASTNode thenClause;
    private ASTNode elseClause;
    public IfNode(ASTNode test, ASTNode thenClause, ASTNode elseClause) {
        this.test = test; this.thenClause = thenClause; this.elseClause = elseClause;
    }
    public ASTNode getTest() { return test; }
    public ASTNode getThenClause() { return thenClause; }
    public ASTNode getElseClause() { return elseClause; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(if " + test + " " + thenClause + " " + elseClause + ")"; }
}

// Operadores Lógicos

class AndNode extends ASTNode {
    private List<ASTNode> expressions;
    @SuppressWarnings("unchecked")
    public AndNode(List<?> expressions) { this.expressions = (List<ASTNode>) expressions; }
    public List<ASTNode> getExpressions() { return expressions; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(and " + expressions + ")"; }
}

class OrNode extends ASTNode {
    private List<ASTNode> expressions;
    @SuppressWarnings("unchecked")
    public OrNode(List<?> expressions) { this.expressions = (List<ASTNode>) expressions; }
    public List<ASTNode> getExpressions() { return expressions; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(or " + expressions + ")"; }
}

class DelayNode extends ASTNode {
    private ASTNode expression;
    public DelayNode(ASTNode expression) { this.expression = expression; }
    public ASTNode getExpression() { return expression; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(delay " + expression + ")"; }
}

// Estruturas de Bindings (Let, Let*, Letrec)

class BindingNode extends ASTNode {
    private String name;
    private ASTNode value;
    public BindingNode(String name, ASTNode value) { this.name = name; this.value = value; }
    public String getName() { return name; }
    public ASTNode getValue() { return value; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(" + name + " " + value + ")"; }
}

class LetNode extends ASTNode {
    private List<ASTNode> bindings;
    private List<ASTNode> body;
    @SuppressWarnings("unchecked")
    public LetNode(List<?> bindings, List<?> body) {
        this.bindings = (List<ASTNode>) bindings; this.body = (List<ASTNode>) body;
    }
    public List<ASTNode> getBindings() { return bindings; }
    public List<ASTNode> getBody() { return body; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(let (" + bindings + ") " + body + ")"; }
}

class NamedLetNode extends ASTNode {
    private String name;
    private List<ASTNode> bindings;
    private List<ASTNode> body;
    @SuppressWarnings("unchecked")
    public NamedLetNode(String name, List<?> bindings, List<?> body) {
        this.name = name; this.bindings = (List<ASTNode>) bindings; this.body = (List<ASTNode>) body;
    }
    public String getName() { return name; }
    public List<ASTNode> getBindings() { return bindings; }
    public List<ASTNode> getBody() { return body; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(let " + name + " (" + bindings + ") " + body + ")"; }
}

class LetStarNode extends ASTNode {
    private List<ASTNode> bindings;
    private List<ASTNode> body;
    @SuppressWarnings("unchecked")
    public LetStarNode(List<?> bindings, List<?> body) {
        this.bindings = (List<ASTNode>) bindings; this.body = (List<ASTNode>) body;
    }
    public List<ASTNode> getBindings() { return bindings; }
    public List<ASTNode> getBody() { return body; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(let* (" + bindings + ") " + body + ")"; }
}

class LetRecNode extends ASTNode {
    private List<ASTNode> bindings;
    private List<ASTNode> body;
    @SuppressWarnings("unchecked")
    public LetRecNode(List<?> bindings, List<?> body) {
        this.bindings = (List<ASTNode>) bindings; this.body = (List<ASTNode>) body;
    }
    public List<ASTNode> getBindings() { return bindings; }
    public List<ASTNode> getBody() { return body; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(letrec (" + bindings + ") " + body + ")"; }
}

// Estruturas Condicionais (Cond)

class CondNode extends ASTNode {
    private List<ASTNode> clauses;
    @SuppressWarnings("unchecked")
    public CondNode(List<?> clauses) { this.clauses = (List<ASTNode>) clauses; }
    public List<ASTNode> getClauses() { return clauses; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(cond " + clauses + ")"; }
}

class CondClauseNode extends ASTNode {
    private ASTNode test;
    private List<ASTNode> body;
    @SuppressWarnings("unchecked")
    public CondClauseNode(ASTNode test, List<?> body) {
        this.test = test; this.body = (List<ASTNode>) body;
    }
    public ASTNode getTest() { return test; }
    public List<ASTNode> getBody() { return body; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(" + test + " " + body + ")"; }
}

class CondArrowNode extends ASTNode {
    private ASTNode test;
    private ASTNode func;
    public CondArrowNode(ASTNode test, ASTNode func) {
        this.test = test; this.func = func;
    }
    public ASTNode getTest() { return test; }
    public ASTNode getFunc() { return func; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(" + test + " => " + func + ")"; }
}

class CondElseNode extends ASTNode {
    private List<ASTNode> body;
    @SuppressWarnings("unchecked")
    public CondElseNode(List<?> body) { this.body = (List<ASTNode>) body; }
    public List<ASTNode> getBody() { return body; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(else " + body + ")"; }
}

class CaseNode extends ASTNode {
    private ASTNode key;
    private List<ASTNode> clauses;
    public CaseNode(ASTNode key, List<ASTNode> clauses) { this.key = key; this.clauses = clauses; }
    public ASTNode getKey() { return key; }
    public List<ASTNode> getClauses() { return clauses; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(case " + key + " " + clauses + ")"; }
}

class CaseClauseNode extends ASTNode {
    private List<ASTNode> keys;
    private ASTNode result;
    public CaseClauseNode(List<ASTNode> keys, ASTNode result) { this.keys = keys; this.result = result; }
    public List<ASTNode> getKeys() { return keys; }
    public ASTNode getResult() { return result; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(" + keys + " " + result + ")"; }
}

// Estruturas de Iteração (Do)

class DoBindingNode extends ASTNode {
    private String name;
    private ASTNode init;
    private ASTNode step; // Pode ser nulo
    public DoBindingNode(String name, ASTNode init, ASTNode step) {
        this.name = name; this.init = init; this.step = step;
    }
    public String getName() { return name; }
    public ASTNode getInit() { return init; }
    public ASTNode getStep() { return step; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(" + name + " " + init + (step != null ? " " + step : "") + ")"; }
}

class DoNode extends ASTNode {
    private List<ASTNode> bindings;
    private ASTNode test;
    private List<ASTNode> testBody;
    private List<ASTNode> body;

    @SuppressWarnings("unchecked")
    public DoNode(List<?> bindings, ASTNode test, List<?> testBody, List<?> body) {
        this.bindings = (List<ASTNode>) bindings;
        this.test = test;
        this.testBody = (List<ASTNode>) testBody;
        this.body = (List<ASTNode>) body;
    }
    public List<ASTNode> getBindings() { return bindings; }
    public ASTNode getTest() { return test; }
    public List<ASTNode> getTestBody() { return testBody; }
    public List<ASTNode> getBody() { return body; }
    @Override public Object accept(ASTVisitor visitor) { return visitor.visit(this); }
    @Override public String toString() { return "(do (" + bindings + ") (" + test + " " + testBody + ") " + body + ")"; }
}