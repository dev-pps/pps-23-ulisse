# Created by buizo at 05/01/2025
# Selezione di due stazioni, una di partenza e una di arrivo
Feature: As a user, I want selezionare la stazione di partenza e arrivo della route, so that visualizzare l'anteprima della tratta

  Background:
    Given visualizzo la sezione di creazione

  Scenario: Seleziono una stazione
    Given nessuna stazione è selezionata
    When clicco su una stazione
    Then riempo il campo di partenza

  Scenario: Seleziono due stazioni
    Given ho selezionato la stazione di partenza
    When clicco su un'altra stazione
    Then riempo il campo di arrivo
    And visualizzo l'anteprima della tratta sulla mappa

  Scenario: Seleziono più di due stazioni
    Given ho selezionato la stazione di partenza e arrivo
    When clicco su un'altra stazione
    Then non succede nulla

  Scenario: Selezionate gia due stazioni, seleziono una stazione gia selezionata
    Given è selezionata la stazione di partenza e arrivo
    When clicco su una stazione gia selezionata
    Then si rimuove la selezione della stazione
    And rimuovo l'anteprima della tratta sulla mappa

  Scenario: Riseleziono la stazione di partenza, seleziono una nuova stazione di partenza
    Given non è selezionata la stazione di partenza
    And è selezionata la stazione di arrivo
    When clicco su una stazione
    Then riempo il campo di partenza
    And visualizzo l'anteprima della tratta sulla mappa