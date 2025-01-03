Feature: As a user, I want creare nuove stazioni, so that poter collegare tra di loro le stazioni (lato visivo anche)

  Scenario: Crea una stazione
    Given sono nella pagina dell'editing della rete
    When clicco bottone nuova stazione,
    Then si apre la finestra laterale di creazione stazione
    And inserisco nome stazione
    And seleziono luogo stazione
    And quanti binari ha la stazione

  Scenario: Selezione del luogo
    Given dato che sono nella creazione della stazione
    When clicco un punto sulla mappa o la inserisco a mano
    Then si riempiono i campi di latitudine e longitudine
    And anteprima sulla mappa della stazione

  Scenario: Riempiti tutti i campi per la crezione della stazione
    Given sono nella creazione della stazione
    When inserisco nome, luogo e binari
    Then la stazione viene creata
    And viene visualizzata sulla mappa