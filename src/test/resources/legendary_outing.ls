// legendary_outing.ls
// Un episodio de ejemplo en LegenScript para decidir si salir.
// CORREGIDO: Separada la declaración de la inicialización para 'define'.

episode {
    // wait... for... it... Reglas y constantes iniciales
    // bro_rule SÍ permite inicialización directa
    bro_rule truth GO_TO_MACLARENS gets true_story;
    bro_rule count MIN_AWESOMENESS gets 7;
    bro_rule count MAX_STORIES gets 3;

    // Estado inicial de la 'crew'
    // Con 'define', primero declaramos y luego asignamos
    define count ted_enthusiasm;      // Declaración OK
    ted_enthusiasm gets 6;            // Asignación OK (en sentencia separada)

    define truth barney_has_suit;     // Declaración OK
    barney_has_suit gets true_story;  // Asignación OK

    define memory lily_advice;        // Declaración OK
    lily_advice gets "Think about the consequences, Ted!"; // Asignación OK

    define count stories_told;        // Declaración OK
    stories_told gets 0;              // Asignación OK

    narrate "Evaluando la noche..."; // OK
    narrate "Consejo de Lily:", lily_advice; // OK

    // Decisión basada en reglas
    define truth proceed_to_bar;      // Declaración OK
    proceed_to_bar gets lie;          // Asignación OK

    if_this_happens (barney_has_suit is_same_as lie) { // OK
        narrate "Barney no lleva traje. ¡Intervención necesaria!"; // OK
        // slap "¡Emergencia de Traje!";
    } what_if (ted_enthusiasm < MIN_AWESOMENESS and GO_TO_MACLARENS is_same_as true_story) { // OK
        narrate "El entusiasmo de Ted es bajo, pero el código es el código."; // OK
        proceed_to_bar gets true_story; // OK
        ted_enthusiasm gets ted_enthusiasm + 1; // OK
    } what_if (ted_enthusiasm >= MIN_AWESOMENESS and GO_TO_MACLARENS is_same_as true_story) { // OK
        narrate "¡El entusiasmo de Ted es suficiente y hay que ir!"; // OK
        proceed_to_bar gets true_story; // OK
    } otherwise { // OK
        narrate "Parece que no se cumplen las condiciones... ¿noche tranquila?"; // OK
    }

    // Si vamos al bar, contamos historias
    if_this_happens (proceed_to_bar is_same_as true_story) { // OK
        narrate "¡Nos vemos en MacLaren's!"; // OK

        define count slaps_to_give;   // Declaración OK
        slaps_to_give gets 1;        // Asignación OK

        // Asumamos que marshall_slaps_left se definió en otro lugar o antes
        // Si no, habría que declararlo aquí también:
        // define count marshall_slaps_left;
        // marshall_slaps_left gets 3;

        // Para este ejemplo, si no existe, causaría un error SEMÁNTICO (no sintáctico)
        // pero para que el parser funcione, la sintaxis es correcta asumiendo que existe.
        // Si quieres que sea autocontenido, descomenta las 2 líneas anteriores.
        // Para probar solo el parser, no es necesario.

        challenge_accepted (slaps_to_give > 0 and marshall_slaps_left > 0) { // OK (asumiendo que marshall_slaps_left existe)
            narrate "Marshall prepares a slap..."; // OK
            marshall_slaps_left gets marshall_slaps_left - 1; // OK
            slaps_to_give gets slaps_to_give - 1; // OK
            narrate "SLAP! Slaps left:", marshall_slaps_left; // OK

            if_this_happens (marshall_slaps_left is_same_as 0) { // OK
                 narrate "Slap Bet settled for now!"; // OK
                 intervention; // OK
            }
        } // Fin del challenge_accepted

        narrate "Buenas historias contadas."; // OK

    } // Fin del if proceed_to_bar

    narrate "Fin de la evaluación de la noche."; // OK

    // Ejemplo comentado de definición y llamada a función (sintaxis parece correcta)
    // tell_story CheckSuitStatus(truth has_suit) returns truth {
    //    narrate "Revisando traje...";
    //    returns has_suit;
    // }
    // define truth suit_ok;
    // // suit_ok gets ...; // Necesitaría asignación para tener valor
    // suit_ok gets call_play CheckSuitStatus(barney_has_suit);
    // narrate "Estado del traje verificado:", suit_ok;

} the_end // El fin. ¡Legendario! // OK