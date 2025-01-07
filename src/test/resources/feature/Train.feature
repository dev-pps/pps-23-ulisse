# Created by buizo at 03/01/2025
Feature: As a user, I want create new trains, so they will be assignable to a path

  Scenario: Create a train
    Given i'm in train editing page
    When i click on "new train" button
    Then appare la finestra di creazione treno
    And insert train name
    And insert amount of carriage and calculate train lenght
    And insert train type (nomral or high-speed), it is showed max speed reachable by train
    And insert max train capacity

  Scenario: Save train
    Given that all field are filled
    When click on save button
    Then train is saved/created

  Scenario: Select existing train
    Given i'm in train editing page
    When click on train
    Then shows train info
    And starts edit mode of train

  Scenario: Edit train info
    Given that I edited some train info
    When i click on save button
    Then train infos are updated/saved

  Scenario: Delete a train
    Given i'm in train editing page
    When I click on delete button
    Then train is deleted

  Scenario: Close edit train page
    Given i'm in train editing page
    When I click back button
    Then I back to homepage


