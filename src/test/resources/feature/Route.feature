Feature: As a user, I want creare nuove tratte, so that collego due stazioni

  Background:
    Given sono nell'editing della rete

  Scenario: visualizzo la sezione per la creazione
    Given non viene visualizzata la sezione
    When clicco il bottone create
    Then visualizzo la sezione di creazione

  Scenario: creo una tratta
    Given visualizzo la sezione di creazione
    And tutti i campi sono vuoti
    When inserisco il tipo di tratta (normale o AV)
    And numero di rotaie
    And stazione di partenza e arrivo
    Then lunghezza della tratta viene derivata dalla distanza tra le stazioni
    And il bottone salva diventa cliccabile

  Scenario: visualizzo la tratta
    Given tutti i campi obbligatori sono riempiti
    When clicco il bottone save
    Then la tratta viene salvata
    And visualizzata sulla mappa

  Scenario: Seleziono una stazione
    Given visualizzo la sezione di creazione
    And nessuna stazione è selezionata
    When clicco su una stazione
    Then riempo il campo di partenza

  Scenario: Seleziono due stazioni
    Given visualizzo la sezione di creazione
    And ho selezionato la stazione di partenza
    When clicco su un'altra stazione
    Then riempo il campo di arrivo
    And visualizzo l'anteprima della tratta sulla mappa

  Scenario: Seleziono più di due stazioni
    Given visualizzo la sezione di creazione
    And ho selezionato la stazione di partenza e arrivo
    When clicco su un'altra stazione
    Then non succede nulla

  Scenario: Selezionate gia due stazioni, seleziono una stazione gia selezionata
    Given visualizzo la sezione di creazione
    And è selezionata la stazione di partenza e arrivo
    When clicco su una stazione gia selezionata
    Then si rimuove la selezione della stazione
    And rimuovo l'anteprima della tratta sulla mappa

  Scenario: Riseleziono la stazione di partenza, seleziono una nuova stazione di partenza
    Given visualizzo la sezione di creazione
    And è selezionata la stazione di arrivo
    When clicco su una stazione
    Then riempo il campo di partenza
    And visualizzo l'anteprima della tratta sulla mappa
