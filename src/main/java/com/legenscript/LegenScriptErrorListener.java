package com.legenscript; // Ajusta el paquete si es necesario

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LegenScriptErrorListener extends BaseErrorListener {

    private final List<ErrorEntry> errors = new ArrayList<>();
    private final String filename; // Para reportar en qué archivo ocurrió el error

    public LegenScriptErrorListener(String filename) {
        this.filename = filename != null ? filename : "<entrada>";
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg, RecognitionException e)
    {
        String sourceName = recognizer.getInputStream().getSourceName();
        if (!sourceName.isEmpty()) {
            sourceName = String.format("%s:%d:%d: ", sourceName, line, charPositionInLine);
        } else {
            sourceName = String.format("%s:%d:%d: ", this.filename, line, charPositionInLine + 1); // +1 para columnas base 1
        }

        String errorType = "Sintáctico";
        // Intentar determinar si es error léxico (a veces ANTLR los reporta aquí)
        if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            if (token.getType() == Token.INVALID_TYPE) {
                errorType = "Léxico";
                msg = "Token inválido encontrado: " + token.getText();
            }
        }
        // Simplificar mensajes comunes de ANTLR
        if (msg.startsWith("mismatched input")) {
            msg = "Entrada inesperada " + msg.substring("mismatched input".length());
        } else if (msg.startsWith("token recognition error at:")) {
            errorType = "Léxico";
            msg = "Carácter no reconocido: " + msg.substring("token recognition error at:".length());
        } else if (msg.startsWith("extraneous input")) {
            msg = "Entrada extraña " + msg.substring("extraneous input".length());
        } else if (msg.startsWith("missing ")) {
            msg = "Falta " + msg.substring("missing ".length());
        }


        // Guardar el error en nuestra lista
        errors.add(new ErrorEntry(errorType, line, charPositionInLine + 1, msg)); // Columna base 1
    }

    public List<ErrorEntry> getErrors() {
        return Collections.unmodifiableList(errors); // Devuelve una vista no modificable
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void clear() {
        errors.clear();
    }

    @Override
    public String toString() {
        if (!hasErrors()) {
            return "No hay errores.";
        }
        StringBuilder builder = new StringBuilder();
        for (ErrorEntry error : errors) {
            builder.append(String.format(" -> Error %s en línea %-3d col %-3d: %s%n",
                    error.getType(), error.getLine(), error.getColumn(), error.getMessage()));
        }
        return builder.toString();
    }
}