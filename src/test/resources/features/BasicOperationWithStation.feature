Feature: As a user, I want to create new station so then i can build a railway system

  Background:
    Given i am in the StationEditor page

  Scenario: Open the StationForm
    Given the StationEditorMenu is shown
    When i click on the create button
    Then the StationEditorMenu is closed
    And the StationForm is shown

  Scenario: Fill the StationForm
    Given the StationForm is shown
    When i insert the station name
    And i insert the station latitude
    And i insert the station longitude
    And i insert the station tracks
    Then the StationForm is filled

  Scenario: Make a station
    Given the StationForm is shown
    When i click ok
    And the StationForm is filled
    Then the station is created
    And is displayed in the StationMap

  Scenario: Select a station
    Given the StationMap is shown
    When i click on a station
    Then the StationForm is shown
    And is filled with station information

  Scenario: Remove a station
    Given the StationForm is shown
    And is filled with station information
    When i click on the remove button
    Then the station is removed
    And isn't displayed in the StationMap

  Scenario: Edit a station
    Given the StationForm is shown
    And is filled with station information
    When i click ok
    And the StationForm is filled
    Then the station is updated

  Scenario: Close the StationForm
    Given the StationForm is shown
    When i click on the back button
    Then the StationForm is closed
    And the stationEditorMenu is shown

  Scenario: Fill location fields
    Given the StationForm is shown
    And the StationMap is shown
    When i click on empty place in the map
    Then latitude and longitude fields are filled