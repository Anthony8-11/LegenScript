package com.legenscript; // Ajusta el paquete

import java.util.Objects;

public class ErrorEntry {
    private final String type;
    private final int line;
    private final int column;
    private final String message;

    public ErrorEntry(String type, int line, int column, String message) {
        this.type = type;
        this.line = line;
        this.column = column;
        this.message = message;
    }

    public String getType() { return type; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return String.format("[%s L%d:%d] %s", type, line, column, message);
    }

    // Opcional: equals y hashCode si necesitas comparar errores o meterlos en Sets
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorEntry that = (ErrorEntry) o;
        return line == that.line && column == that.column && Objects.equals(type, that.type) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, line, column, message);
    }
}