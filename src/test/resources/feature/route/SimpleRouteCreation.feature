# Created by buizo at 04/01/2025
# Creazione di una route tra due stazioni
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


