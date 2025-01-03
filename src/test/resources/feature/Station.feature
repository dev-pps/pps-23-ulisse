Feature: As a user, I want creare nuove stazioni, so that poter collegare tra di loro le stazioni (lato visivo anche)

  Scenario: Crea una stazione
    Given sono nella pagina dell'editing della rete
    When clicco bottone nuova stazione
    Then appare la finestra di creazione stazione
    And inserisco nome stazione
    And seleziono luogo stazione
    And quanti binari ha la stazione

  Scenario: Selezione del luogo
    Given sono nella creazione della stazione
    When clicco un punto sulla mappa o la inserisco a mano
    Then si riempiono i campi di latitudine e longitudine
    And anteprima sulla mappa della stazione

  Scenario: Salvataggio della stazione
    Given tutti i campi sono riempiti
    When inserisco nome, luogo e binari
    Then la stazione viene creata
    And viene visualizzata sulla mappa

  Scenario: Seleziono una stazione gia esistente
    Given sono nella pagina dell'editing della stazione
    When clicco sulla stazione
    Then viene visualizzato le informazioni della stazione
    And entro in modalità di modifica

  Scenario: modifico informazioni della stazione
    Given ho modificato le informazioni della stazione
    When clicco sul bottone salva
    Then la stazione viene aggiornato

  Scenario: Elimino una stazione
    Given sono nella modalità di modifica della stazione
    When clicco sul bottone elimina
    Then la stazione viene eliminata

  Scenario: Esco dall'editing delle stazione
    Given sono nella pagina dell'editing dei treni
    When clicco sul bottone back
    Then torno alla pagina home
