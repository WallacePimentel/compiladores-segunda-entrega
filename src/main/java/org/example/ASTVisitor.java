package org.example;

public interface ASTVisitor {
    // Nós Base e Primitivos
    Object visit(ProgramNode node);
    Object visit(NumberNode node);
    Object visit(StringNode node);
    Object visit(BooleanNode node);
    Object visit(CharacterNode node);
    Object visit(IdentifierNode node);

    // Quotes
    Object visit(ListNode node);
    Object visit(QuoteNode node);
    Object visit(QuasiquoteNode node);
    Object visit(UnquoteNode node);
    Object visit(UnquoteSplicingNode node);

    // Funções e Parâmetros
    Object visit(ProcedureCallNode node);
    Object visit(FormalsNode node);
    Object visit(LambdaNode node);

    // Definições, Variáveis e Blocos
    Object visit(DefineNode node);
    Object visit(SetNode node);
    Object visit(BeginNode node);

    // Controle de Fluxo
    Object visit(IfNode node);

    Object visit(CondNode node);
    Object visit(CondClauseNode node);
    Object visit(CondArrowNode node);
    Object visit(CondElseNode node);

    Object visit(CaseNode node);
    Object visit(CaseClauseNode node);

    // Bindings (Let)
    Object visit(BindingNode node);
    Object visit(LetNode node);
    Object visit(NamedLetNode node);
    Object visit(LetStarNode node);
    Object visit(LetRecNode node);

    // Iteração e Lógica Ociosa (Lazy)
    Object visit(DoNode node);
    Object visit(DoBindingNode node);
    Object visit(DelayNode node);

    // Operadores Lógicos Short-Circuit
    Object visit(AndNode node);
    Object visit(OrNode node);
}