package org.example;

//Tipos suportados pelo compilador
public enum Type {
    NUMBER("Number"),
    STRING("String"),
    BOOLEAN("Boolean"),
    CHARACTER("Character"),
    LIST("List"),
    PROCEDURE("Procedure"),
    PROMISE("Promise"), // Novo tipo para delay
    UNDEFINED("Undefined"),
    ANY("Any");

    private String name;

    Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isCompatibleWith(Type other) {
        if (this == ANY || other == ANY) return true;
        if (this == UNDEFINED || other == UNDEFINED) return true;
        return this == other;
    }
}