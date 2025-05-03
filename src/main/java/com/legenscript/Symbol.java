package com.legenscript;
// Representa una entrada en la tabla de símbolos
public class Symbol {
    enum Kind { VARIABLE, CONSTANT, FUNCTION, TYPE }

    String name;
    String type; // Tipo como string (e.g., "count", "memory", "function_return_type")
    Kind kind;
    int declarationLine;
    // Podrías añadir más: scope, parameter types (for functions), etc.

    public Symbol(String name, String type, Kind kind, int declarationLine) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.declarationLine = declarationLine;
    }

    @Override
    public String toString() {
        return String.format("[%s %s (%s) at L%d]", kind, name, type, declarationLine);
    }
}