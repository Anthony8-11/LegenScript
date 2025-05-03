package com.legenscript; // Asegúrate que el paquete sea correcto

import com.legenscript.antlr.*; // Importa las clases generadas por ANTLR
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener para recorrer el árbol de ANTLR y realizar acciones,
 * como llenar la tabla de símbolos.
 */
public class LegenScriptCompilerListener extends LegenScriptBaseListener {

    private final SymbolTable symbolTable;
    private final List<ErrorEntry> semanticErrors; // Lista para errores semánticos (opcional)

    // Constructor: Recibe la tabla de símbolos (y errores) para llenarla
    public LegenScriptCompilerListener(SymbolTable symbolTable, List<ErrorEntry> errorList) {
        this.symbolTable = symbolTable;
        // Usamos la misma lista de errores global o una específica para semántica
        this.semanticErrors = errorList;
    }

    // Método llamado al entrar en la regla de declaración de variable (#DeclVar)
    @Override
    public void enterDeclVar(LegenScriptParser.DeclVarContext ctx) {
        String varName = ctx.ID().getText();
        String typeName = ctx.type().getText(); // Obtiene el texto del tipo (ej: "count")
        int line = ctx.ID().getSymbol().getLine();
        int col = ctx.ID().getSymbol().getCharPositionInLine() + 1;

        // Crear el símbolo
        Symbol symbol = new Symbol(varName, typeName, Symbol.Kind.VARIABLE, line);

        // Intentar definir en la tabla de símbolos
        if (!symbolTable.define(symbol)) {
            // Error: Variable ya definida (Análisis Semántico Básico)
            // Añadimos a la misma lista de errores por simplicidad
            String errorMsg = "Variable '" + varName + "' ya definida previamente.";
            semanticErrors.add(new ErrorEntry("Semántico", line, col, errorMsg));
        } else {
            System.out.println("DEBUG: Símbolo definido: " + symbol); // Mensaje de depuración
        }
    }

    // Método llamado al entrar en la regla de declaración de constante (#DeclConst)
    @Override
    public void enterDeclConst(LegenScriptParser.DeclConstContext ctx) {
        String constName = ctx.ID().getText();
        String typeName = ctx.type().getText();
        int line = ctx.ID().getSymbol().getLine();
        int col = ctx.ID().getSymbol().getCharPositionInLine() + 1;
        // Nota: No estamos evaluando la 'expression' aquí, solo registrando la constante

        Symbol symbol = new Symbol(constName, typeName, Symbol.Kind.CONSTANT, line);

        if (!symbolTable.define(symbol)) {
            String errorMsg = "Constante '" + constName + "' ya definida previamente.";
            semanticErrors.add(new ErrorEntry("Semántico", line, col, errorMsg));
        } else {
            System.out.println("DEBUG: Símbolo definido: " + symbol); // Mensaje de depuración
        }
    }

    // Método llamado al entrar en la definición de una función (#StmtFuncDef)
    // Nota: El nombre del método es enterNombreRegla (nombre de la regla en .g4)
    @Override
    public void enterFunction_def_stmt(LegenScriptParser.Function_def_stmtContext ctx) {
        String funcName = ctx.ID().getText();
        String returnType = ctx.type().getText(); // Tipo de retorno
        int line = ctx.ID().getSymbol().getLine();
        int col = ctx.ID().getSymbol().getCharPositionInLine() + 1;
        // Aquí podríamos procesar ctx.param_list() para obtener detalles de parámetros

        Symbol symbol = new Symbol(funcName, returnType, Symbol.Kind.FUNCTION, line);

        if (!symbolTable.define(symbol)) {
            String errorMsg = "Función '" + funcName + "' ya definida previamente.";
            semanticErrors.add(new ErrorEntry("Semántico", line, col, errorMsg));
        } else {
            System.out.println("DEBUG: Símbolo definido: " + symbol); // Mensaje de depuración
        }

        // Aquí podrías entrar a un nuevo ámbito (scope) si tu tabla de símbolos lo soporta
        // y procesar los parámetros (ctx.param_list()) para añadirlos a ese ámbito.
    }

    // Podrías añadir enterVariableReference o similar si necesitas chequear
    // si una variable usada en una expresión fue declarada (más análisis semántico).
    // Por ejemplo, dentro de enterAtomId:
    @Override
    public void enterAtomId(LegenScriptParser.AtomIdContext ctx) {
        String varName = ctx.ID().getText();
        int line = ctx.ID().getSymbol().getLine();
        int col = ctx.ID().getSymbol().getCharPositionInLine() + 1;

        // Chequeo semántico básico: ¿existe el símbolo?
        if (symbolTable.resolve(varName) == null) {
            String errorMsg = "Variable o función '" + varName + "' no definida.";
            // Evitar duplicar el error si es una llamada a función no definida
            boolean isPartOfFuncCall = ctx.parent instanceof LegenScriptParser.Function_call_exprContext ||
                    (ctx.parent != null && ctx.parent.parent instanceof LegenScriptParser.Function_call_exprContext); // Simple check

            if (!isPartOfFuncCall) { // Solo reporta si no es parte de una llamada (la llamada se validaría aparte)
                ErrorEntry errorEntry = new ErrorEntry("Semántico", line, col, errorMsg);
                // Evitar reportar el mismo error múltiples veces
                if (!semanticErrors.contains(errorEntry)) {
                    semanticErrors.add(errorEntry);
                }
            }
        }
    }

    // Opcional: Salir del ámbito al finalizar una función
    // @Override
    // public void exitFunction_def_stmt(LegenScriptParser.Function_def_stmtContext ctx) {
    //     // Lógica para salir del ámbito si la tabla de símbolos lo maneja
    // }

}