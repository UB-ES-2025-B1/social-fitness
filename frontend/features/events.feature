Feature: Event Management
  As a user
  I want to manage sports events
  So that I can organize and participate in fitness activities

  Background:
    Given I am logged in as a user

  Scenario: Create a new event
    When I create an event with the following details:
      | title       | Morning Run         |
      | sport       | Running             |
      | location    | Central Park        |
      | date        | 2025-12-01          |
      | capacity    | 10                  |
    Then the event should be created successfully
    And I should see the event in my events list

  Scenario: Join an existing event
    Given there is an available event "Yoga Session"
    When I join the event
    Then I should be added to the event participants
    And the event should appear in my joined events

  Scenario: Leave an event
    Given I have joined an event "Basketball Game"
    When I leave the event
    Then I should be removed from the event participants
    And the event should not appear in my joined events

  Scenario: Search for events by sport
    Given there are multiple events for different sports
    When I search for "Tennis" events
    Then I should see only tennis-related events
    And other sports events should be filtered out
