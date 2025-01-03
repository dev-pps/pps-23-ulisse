# Created by buizo at 03/01/2025
Feature: As a user, I want creare nuovi treni alla rete, so that saranno disponibili per l'assegnazione di un percorso

  Scenario: Creao un treno
    Given sono nella pagina dell'editing dei treni
    When clicco bottone nuovo treno
    Then si apre la finestra laterale di creazione treno
    And inserisco nome treno
    And inserisco le carrozze, con calcolo della lunghezza del treno
    And inserisco tipo normale o AV, con visualizzazione della velocità massima del treno
    And inserisco la capacità massima del treno

  Scenario: Riempiti tutti i campi per la crezione del treno
    Given sono nella creazione del treno
    When clicclo il bottone salva
    Then il treno viene creato

  Scenario: Seleziono un treno gia esistente
    Given sono nella pagina dell'editing dei treni
    When clicco sul treno
    Then viene visualizzato le informazioni del treno
    And entro in modalità di modifica

  Scenario: modifico informazioni del treno
    Given ho modificato le informazioni del treno
    When clicco sul bottone salva
    Then il treno viene aggiornato

  Scenario: Elimino un treno
    Given sono nella modalità di modifica del treno
    When clicco sul bottone elimina
    Then il treno viene eliminato

  Scenario: Esco dall'editing dei treni
    Given sono nella pagina dell'editing dei treni
    When clicco sul bottone back
    Then torno alla pagina home


