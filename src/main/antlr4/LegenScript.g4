// LegenScript.g4
// Define la gramática combinada (Lexer y Parser) para ANTLR 4

grammar LegenScript;

// Añade este bloque para especificar el paquete de las clases generadas
@header {
package com.legenscript.antlr; // << Asegúrate que este sea el paquete donde quieres el código generado
                               //    y que coincida con la estructura en <outputDirectory> del pom.xml
}



// Punto de entrada principal
program : EPISODE LBRACE statements RBRACE THE_END EOF; // EOF asegura que se parsea todo el archivo

// Lista de sentencias (puede ser vacía)
statements : statement* ; // '*' significa cero o más sentencias

// Una sentencia individual (alternativas)
statement
    : declaration_stmt    # StmtDeclaration
    | assignment_stmt     # StmtAssignment
    | narrate_stmt        # StmtNarrate
    | ask_kids_stmt       # StmtAskKids
    | if_stmt             # StmtIf
    | while_stmt          # StmtWhile
    | function_def_stmt   # StmtFuncDef
    | function_call_stmt  # StmtFuncCall
    | intervention_stmt   # StmtIntervention
    | slap_stmt           # StmtSlap
    | block_stmt          # StmtBlock
    | SEMI                # StmtEmpty // Sentencia vacía
    ;

// Bloque de código explícito
block_stmt : LBRACE statements RBRACE ;

// Declaraciones
declaration_stmt
    : DEFINE type ID SEMI                                # DeclVar
    | BRO_RULE type ID GETS expression SEMI              # DeclConst
    ;

// Tipos de dato
type
    : MEMORY                                             # TypeMemory
    | COUNT                                              # TypeCount
    | TRUTH                                              # TypeTruth
    | CREW LBRACKET RBRACKET                             # TypeCrew
    ;

// Asignación
assignment_stmt : ID GETS expression SEMI ;

// Salida
narrate_stmt : NARRATE expression_list SEMI ;

// Entrada
ask_kids_stmt : ID GETS ASK_KIDS LPAREN expression RPAREN SEMI ;

// Condicional If/Else If/Else
if_stmt : IF_THIS_HAPPENS LPAREN expression RPAREN statement (what_if_clause)* (else_clause)? ;
what_if_clause : WHAT_IF LPAREN expression RPAREN statement ;
else_clause : OTHERWISE statement ;

// Bucle While
while_stmt : CHALLENGE_ACCEPTED LPAREN expression RPAREN statement ;

// Salida de bucle
intervention_stmt : INTERVENTION SEMI ;

// Lanzar error
slap_stmt : SLAP expression SEMI ;

// Definición de Función
function_def_stmt : TELL_STORY ID LPAREN param_list? RPAREN RETURNS type block_stmt ; // param_list opcional
param_list : param (COMMA param)* ;
param : type ID ;

// Llamada a Función (como sentencia)
function_call_stmt : CALL_PLAY ID LPAREN arg_list? RPAREN SEMI ; // arg_list opcional

// Llamada a Función (como expresión)
// Se define abajo en las reglas de expresión (átomo)

// Lista de argumentos (para llamadas) o expresiones (para narrate)
expression_list : expression (COMMA expression)* ;
arg_list : expression_list ; // Reutiliza expression_list

// --- Expresiones (con precedencia y asociatividad) ---
// Los #labels ayudan a crear métodos específicos en el Listener/Visitor
expression
    : NOT expression                           # ExprNot
    | expression (TIMES | DIVIDE) expression   # ExprMulDiv
    | expression (PLUS | MINUS) expression    # ExprAddSub
    | expression (LT | GT | LTE | GTE | IS_SAME_AS | IS_NOT) expression # ExprCompare
    | expression AND expression                # ExprAnd
    | expression OR expression                 # ExprOr
    | atom                                     # ExprAtom
    ;

// Elementos básicos (átomos) de una expresión
atom
    : LPAREN expression RPAREN                 # ExprParen // Paréntesis
    | NUMBER                                   # AtomNumber
    | STRING                                   # AtomString
    | TRUE_STORY                               # AtomTrue
    | LIE                                      # AtomFalse
    | ID                                       # AtomId
    | function_call_expr                       # AtomFuncCall
    | array_access                             # AtomArrayAccess
    ;

function_call_expr : CALL_PLAY ID LPAREN arg_list? RPAREN ; // Llamada como expresión
array_access : ID LBRACKET expression RBRACKET ;     // Acceso a Array

// --- Reglas del Lexer (Tokens) ---

// -- Palabras Reservadas y Operadores con nombre --
// ANTLR procesa las reglas del lexer en orden, la más larga/específica primero.
// Por eso, las keywords van antes que la regla ID general.
EPISODE             : 'episode';
THE_END             : 'the_end';
TELL_STORY          : 'tell_story';
RETURNS             : 'returns';
CALL_PLAY           : 'call_play';
MEMORY              : 'memory';
COUNT               : 'count';
TRUTH               : 'truth';
CREW                : 'crew';
DEFINE              : 'define';
BRO_RULE            : 'bro_rule';
NARRATE             : 'narrate';
ASK_KIDS            : 'ask_kids';
IF_THIS_HAPPENS     : 'if_this_happens';
OTHERWISE           : 'otherwise';
WHAT_IF             : 'what_if';
CHALLENGE_ACCEPTED  : 'challenge_accepted';
INTERVENTION        : 'intervention';
TRUE_STORY          : 'true_story';
LIE                 : 'lie';
SLAP                : 'slap';
GETS                : 'gets';
IS_SAME_AS          : 'is_same_as';
IS_NOT              : 'is_not';
AND                 : 'and';
OR                  : 'or';
NOT                 : 'not';

// -- Símbolos y Operadores --
PLUS      : '+';
MINUS     : '-';
TIMES     : '*';
DIVIDE    : '/';
LT        : '<';
GT        : '>';
LTE       : '<=';
GTE       : '>=';
LPAREN    : '(';
RPAREN    : ')';
LBRACE    : '{';
RBRACE    : '}';
LBRACKET  : '[';
RBRACKET  : ']';
SEMI      : ';';
COMMA     : ',';
// DOT       : '.'; // Removido por no usarse

// -- Literales --
NUMBER : INT ('.' DIGIT*)? | '.' DIGIT+ ; // Entero o Flotante
fragment INT : '0' | [1-9] DIGIT* ; // Reutilizable para número
fragment DIGIT : [0-9] ;

// String con escapes básicos (\", \\, \n, \t, etc.)
STRING : '"' ( ESC | ~["\\] )*? '"' ;
fragment ESC : '\\' (["\\/bfnrt] | UNICODE) ; // Escapes simples + unicode
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;

// -- Identificadores --
ID : [a-zA-Z_] [a-zA-Z0-9_]* ; // Letras, números, underscore; no empieza con número

// -- Comentarios (a ignorar) --
LINE_COMMENT : '//' ~[\r\n]* -> skip ; // Comentarios de una línea
     // Punto y coma terminando la regla en su PROPIA línea

WS : [ \t\r\n]+ -> skip; // Ignora espacios, tabs, saltos de línea

// -- Manejo de Errores Léxicos (Implícito) --
// Cualquier carácter no reconocido por las reglas anteriores será un error léxico.
// ANTLR lo reportará a través del ErrorListener.