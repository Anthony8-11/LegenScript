package com.legenscript;
import java.util.HashMap;
import java.util.Map;

// Tabla de símbolos simple (podría mejorarse con manejo de ámbitos)
public class SymbolTable {
    private Map<String, Symbol> symbols = new HashMap<>();

    public boolean define(Symbol symbol) {
        if (symbols.containsKey(symbol.name)) {
            return false; // Ya definido
        }
        symbols.put(symbol.name, symbol);
        return true;
    }

    public Symbol resolve(String name) {
        return symbols.get(name); // Retorna null si no se encuentra
    }

    public void clear() {
        symbols.clear();
    }

    @Override
    public String toString() {
        if (symbols.isEmpty()) return "(Tabla de Símbolos Vacía)";
        StringBuilder sb = new StringBuilder("Tabla de Símbolos:\n");
        symbols.values().forEach(s -> sb.append(" - ").append(s).append("\n"));
        return sb.toString();
    }
}