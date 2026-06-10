package org.example;

import java.util.*;

public class SymbolTable {
    private Stack<Map<String, Type>> scopes;
    private int scopeLevel;

    public SymbolTable() {
        this.scopes = new Stack<>();
        this.scopeLevel = 0;
        // Escopo global
        this.scopes.push(new HashMap<>());
    }

    //Criar novo escopo
    public void pushScope() {
        scopes.push(new HashMap<>());
        scopeLevel++;
    }

    //Sair do escopo atual
    public void popScope() {
        if (scopes.size() > 1) {
            scopes.pop();
            scopeLevel--;
        }
    }

    //Definir variável no escopo atual
    public void define(String name, Type type) {
        scopes.peek().put(name, type);
    }

    //Procurar variável em todos os escopos
    public Type lookup(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                return scopes.get(i).get(name);
            }
        }
        return null;
    }

    // Verificar se a variável está definida em algum escopo
    public boolean isDefined(String name) {
        return lookup(name) != null;
    }

    // Para depuração: imprimir a tabela de símbolos
    public String getScopeInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Scope Level: ").append(scopeLevel).append("\n");
        for (int i = scopes.size() - 1; i >= 0; i--) {
            sb.append("  Scope ").append(i).append(": ");
            scopes.get(i).forEach((k, v) -> sb.append(k).append(": ").append(v).append(", "));
            sb.append("\n");
        }
        return sb.toString();
    }

    public int getScopeLevel() {
        return scopeLevel;
    }
}