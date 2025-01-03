Feature: As a user, I want creare nuove tratte, so that collego due stazioni

  Scenario: Crea una tratta
    Given sono nella pagina dell'editing della rete
    When clicco bottone nuova tratta,
    Then appare la finestra di creazione tratta
    And dove seleziono una coppia di stazioni
    And inserisco la lunghezza della tratta (derivata dalla distanza tra le stazioni)
    And inserisco il tipo di tratta normale o AV
    And scegliere quante rotaie ha la tratta

  Scenario: Riempita la coppia di stazione e inserite le rotaie
    Given sono nella creazione della tratta
    When clicco il bottone salva
    Then la tratta viene creata
    And viene visualizzata sulla mappa

  Scenario: Seleziono solo 2 stazioni
    Given dato che sono nella creazione della tratta
    When clicco sulla stazione di partenza e arrivo
    Then voglio vedere l'anteprima della tratta sulla mappa

  Scenario: Seleziono più di due stazioni
    Given dato che sono nella creazione della tratta
    When clicco sulla stazione di partenza e arrivo
    And clicco su altre stazioni
    Then non succede nulla

  Scenario: Selezione di una stazione gia selezionata
    Given dato che sono nella creazione della tratta
    When clicco sulla stazione di partenza o arrivo
    And clicco su una stazione gia selezionata
    Then si rimuove la selezione della stazione