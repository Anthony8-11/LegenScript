package com.legenscript;

// --- Asegúrate de tener TODAS estas importaciones ---
import com.legenscript.antlr.*; // Para Lexer, Parser, Listener base
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker; // <--- IMPORTANTE: El caminador de árboles

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList; // <--- Para crear lista de errores si no es global
import java.util.List;

public class Main {

    // --- Variables para guardar resultados ---
    // Puedes usar una lista única o separadas. Usaremos una combinada.
    private static final List<ErrorEntry> analysisErrors = new ArrayList<>();
    // Instancia de la tabla de símbolos
    private static final SymbolTable symbolTable = new SymbolTable();


    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java com.legenscript.Main <archivo_legenscript.ls>");
            System.exit(1);
        }

        String filePathString = args[0];
        Path sourceFile = null; // Inicializar a null
        try {
            sourceFile = Paths.get(filePathString);
            if (!Files.exists(sourceFile) || !Files.isReadable(sourceFile)) {
                System.err.println("Error: El archivo '" + filePathString + "' no existe o no se puede leer.");
                System.exit(1);
            }
        } catch (InvalidPathException e) {
            System.err.println("Error: Ruta de archivo inválida: " + filePathString);
            System.exit(1);
        }

        // Limpiar estado de análisis anteriores
        analysisErrors.clear();
        symbolTable.clear();

        System.out.println("Iniciando análisis de '" + (sourceFile != null ? sourceFile.getFileName() : filePathString) + "'...");
        System.out.println("----------------------------------------");

        ParseTree tree = null; // Variable para guardar el árbol de parseo

        try {
            CharStream input = CharStreams.fromPath(sourceFile);
            LegenScriptLexer lexer = new LegenScriptLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            LegenScriptParser parser = new LegenScriptParser(tokens);

            // Configurar Listener de Errores Léxicos/Sintácticos
            // Este listener llenará nuestra lista 'analysisErrors' si ANTLR detecta problemas
            LegenScriptErrorListener errorListener = new LegenScriptErrorListener(sourceFile.getFileName().toString(), analysisErrors);

            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // --- Ejecutar el Parseo ---
            System.out.println("Ejecutando análisis sintáctico...");
            tree = parser.program(); // Intenta construir el árbol

        } catch (IOException e) {
            System.err.println("Error al leer o procesar el archivo: " + e.getMessage());
            analysisErrors.add(new ErrorEntry("Fatal", 0, 0, "Error I/O: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("¡Intervention Mayor! Ocurrió un error inesperado durante el parseo: " + e.getMessage());
            analysisErrors.add(new ErrorEntry("Fatal", 0, 0, "Excepción durante parseo: " + e.getMessage()));
            e.printStackTrace();
        }

        // --- Análisis Semántico (Tabla de Símbolos) SÓLO si no hubo errores antes y se creó el árbol ---
        if (analysisErrors.isEmpty() && tree != null) {
            System.out.println("Sintaxis OK. Recorriendo árbol para tabla de símbolos y análisis semántico...");
            try {
                // 1. Crear el ParseTreeWalker (el "caminador" estándar de ANTLR)
                ParseTreeWalker walker = new ParseTreeWalker();

                // 2. Crear tu Listener (el que llena la tabla de símbolos)
                //    Le pasamos la tabla y la lista de errores (para reportar errores semánticos)
                LegenScriptCompilerListener compilerListener = new LegenScriptCompilerListener(symbolTable, analysisErrors);

                // 3. Ejecutar el recorrido (walk)
                //    Esto hará que el 'walker' visite cada nodo del 'tree'
                //    y llame a los métodos 'enterRuleName' / 'exitRuleName' en tu 'compilerListener'
                walker.walk(compilerListener, tree);

                System.out.println("Recorrido del árbol completado.");

            } catch (Exception e) {
                System.err.println("¡Intervention Mayor! Ocurrió un error inesperado durante el análisis semántico: " + e.getMessage());
                analysisErrors.add(new ErrorEntry("Fatal", 0, 0, "Excepción durante recorrido: " + e.getMessage()));
                e.printStackTrace();
            }
        } else if (tree == null && analysisErrors.isEmpty()) {
            // Caso raro donde no hubo errores reportados pero el árbol es nulo
            analysisErrors.add(new ErrorEntry("Fatal", 0, 0, "Fallo en el parseo, árbol nulo."));
            System.out.println("Fallo en el parseo: Árbol nulo.");
        } else {
            System.out.println("Se encontraron errores léxicos/sintácticos. Análisis semántico cancelado.");
        }


        // --- Reporte Final ---
        System.out.println("----------------------------------------");
        // 4. Imprimir la Tabla de Símbolos (SIEMPRE, para ver qué se llenó)
        System.out.println("Contenido Final de la Tabla de Símbolos:");
        System.out.println(symbolTable); // Llama al toString() que definiste en SymbolTable
        System.out.println("----------------------------------------");

        // Imprimir Errores Totales (Léxicos, Sintácticos, Semánticos)
        if (analysisErrors.isEmpty()) {
            System.out.println("Análisis completado sin errores detectados. ¡True Story!");
        } else {
            System.out.println("Análisis fallido. Se encontraron " + analysisErrors.size() + " errores en total:");
            // Ordenar errores para mostrarlos consistentemente (opcional pero útil)
            try {
                analysisErrors.sort((e1, e2) -> {
                    int lineCompare = Integer.compare(e1.getLine(), e2.getLine());
                    if (lineCompare == 0) {
                        return Integer.compare(e1.getColumn(), e2.getColumn());
                    }
                    return lineCompare;
                });
            } catch (Exception e) { /* Ignorar si falla la ordenación */ }

            for (ErrorEntry error : analysisErrors) {
                System.out.printf(" -> Error %-10s en línea %-3d col %-3d: %s%n",
                        error.getType(), error.getLine(), error.getColumn(), error.getMessage());
            }
            System.out.println("----------------------------------------");
            System.out.println("Se necesita una 'intervention' en este código.");
            System.exit(1); // Salir con código de error
        }
    }
}