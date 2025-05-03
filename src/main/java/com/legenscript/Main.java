package com.legenscript; // Ajusta el paquete


import com.legenscript.antlr.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java com.legenscript.Main <archivo_legenscript.ls>");
            System.exit(1);
        }

        String filePathString = args[0];
        Path sourceFile;
        try {
            sourceFile = Paths.get(filePathString);
            if (!Files.exists(sourceFile) || !Files.isReadable(sourceFile)) {
                System.err.println("Error: El archivo '" + filePathString + "' no existe o no se puede leer.");
                System.exit(1);
            }
        } catch (InvalidPathException e) {
            System.err.println("Error: Ruta de archivo inválida: " + filePathString);
            System.exit(1);
            return; // Necesario para el compilador
        }

        System.out.println("Iniciando análisis de '" + filePathString + "'...");
        System.out.println("----------------------------------------");

        try {
            // 1. Crear CharStream desde el archivo
            CharStream input = CharStreams.fromPath(sourceFile);

            // 2. Crear Lexer
            LegenScriptLexer lexer = new LegenScriptLexer(input);

            // 3. Crear Flujo de Tokens
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // 4. Crear Parser
            LegenScriptParser parser = new LegenScriptParser(tokens);

            // 5. Configurar Manejador de Errores Personalizado
            parser.removeErrorListeners(); // Quitar el listener por defecto que imprime a consola
            LegenScriptErrorListener errorListener = new LegenScriptErrorListener(sourceFile.getFileName().toString());
            parser.addErrorListener(errorListener);
            // También podríamos añadirlo al lexer si queremos capturar errores léxicos más específicamente
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            // 6. Iniciar el Parseo desde la regla inicial ('program')
            ParseTree tree = parser.program(); // Ejecuta el análisis

            // 7. Verificar Errores después del parseo
            List<ErrorEntry> errors = errorListener.getErrors();

            // 8. Reportar Resultados
            System.out.println("----------------------------------------");
            if (errors.isEmpty()) {
                System.out.println("Análisis completado. ¡True Story! El código es sintácticamente legendario.");

                // -- Opcional: Imprimir el árbol de parseo (para depuración/demostración) --
                // System.out.println("\nÁrbol de Parseo (Formato LISP):");
                // System.out.println(tree.toStringTree(parser));

                // -- Opcional: Imprimir Tokens (para depuración/demostración) --
                // System.out.println("\nTokens Reconocidos:");
                // tokens.fill(); // Asegurarse que todos los tokens están cargados
                // for (Token t : tokens.getTokens()) {
                //      String symbolicName = LegenScriptLexer.VOCABULARY.getSymbolicName(t.getType());
                //      System.out.printf("  %-15s '%s'\n", symbolicName, t.getText().replace("\n", "\\n"));
                // }

            } else {
                System.out.println("Análisis fallido. Se encontraron " + errors.size() + " errores:");
                System.out.print(errorListener.toString()); // Usar el toString del listener para formato
                System.out.println("----------------------------------------");
                System.out.println("Se necesita una 'intervention' en este código.");
                System.exit(1); // Salir con código de error
            }

        } catch (IOException e) {
            System.err.println("Error al leer o procesar el archivo: " + e.getMessage());
            e.printStackTrace(); // Imprimir stack trace para depuración
            System.exit(1);
        } catch (Exception e) {
            System.err.println("¡Intervention Mayor! Ocurrió un error inesperado: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}