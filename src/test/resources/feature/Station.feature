Feature: As a user, I want to create new station so then i can build a railway system

  Background:
    Given i am in the StationEditor page

  Scenario: Open the stationForm
    Given the stationEditorMenu is shown
    When i click on the create button
    Then the stationForm shows up

  #FORM V1
  Scenario: Insert station name
    Given the stationForm is shown
    When I insert the station name
    Then the station name is inserted

  ...

  Scenario: Fill the stationForm
    Given the stationForm is shown
    When the station name is inserted
    And the station latitude is inserted
    And the station longitude is inserted
    And the station tracks are inserted
    Then the stationForm is filled
  ###

  #FORM V2
  Scenario: Fill the stationForm
      Given the stationForm is shown
      When i insert the station
      And the latitude
      And the longitude
      And the station tracks
      Then the stationForm is filled
  ###

  Scenario: Make a station
    Given the stationForm is shown
    And the stationForm is filled
    When i click ok
    Then the station is created
    And (the station) is visualized in the WorldMap

  #TODO
  Scenario: Selezione del luogo
    Given sono nella creazione della stazione
    When clicco un punto sulla mappa o la inserisco a mano
    Then si riempiono i campi di latitudine e longitudine
    And anteprima sulla mappa della stazione

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
