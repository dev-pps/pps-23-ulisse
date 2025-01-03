Feature: As a user, I want to open the app, so tha i can see the home page

  Scenario: Creazione un nuovo progetto di simulazione
    Given sono nella home page
    When clicco il bottone nuovo progetto
    Then cambia schermata della creazione della simulazione
    And visualizzo i bottoni new o open

  Scenario: Creazione un nuovo parco treni
    Given sono nella home page
    When clicco il bottone nuovo parco treni
    Then cambia schermata della creazione del parco treni
    And visualizzo i bottoni new o open

  Scenario: Creazione di una nuova rete ferroviaria
    Given sono nella home page
    When clicco il bottone nuova rete ferroviaria
    Then cambia schermata della creazione della rete ferroviaria
    And visualizzo i bottoni new o open


