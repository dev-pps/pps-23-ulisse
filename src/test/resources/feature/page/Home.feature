Feature: As a user, I want to open the app, so tha i can see the home page

  Scenario: Selezione della "simulazione"
    Given sono nella home page
    When seleziono simulazione
    Then visualizzo i bottoni new e open

  Scenario: Selezione della "parco treni"
    Given sono nella home page
    When seleziono parco treni
    Then visualizzo i bottoni new e open

  Scenario: Selezione della "rete ferroviaria"
    Given sono nella home page
    When seleziono rete ferroviaria
    Then visualizzo i bottoni new e open