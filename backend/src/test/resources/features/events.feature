Feature: Event Management API
  As a REST API client
  I want to manage sports events
  So that users can organize and participate in fitness activities

  Background:
    Given the events API is available
    And I am authenticated as a user

  Scenario: Create a new event
    When I send a POST request to "/events" with:
      | title     | Morning Run  |
      | sport     | Running      |
      | location  | Central Park |
      | date      | 2025-12-01   |
      | time      | 08:00        |
      | organizer | testuser     |
      | capacity  | 10           |
    Then the response status code should be 201
    And the response should contain the created event details

  Scenario: Join an existing event
    Given an event exists with id 1
    When I send a POST request to "/events/1/join"
    Then the response status code should be 200
    And the response should confirm I joined the event

  Scenario: Leave an event
    Given an event exists with id 2
    And I have joined the event with id 2
    When I send a POST request to "/events/2/leave"
    Then the response status code should be 200
    And the response should confirm I left the event

  Scenario: Get all events
    Given multiple events exist in the system
    When I send a GET request to "/events"
    Then the response status code should be 200
    And the response should contain a list of events

  Scenario: Filter events by sport
    Given multiple events exist for different sports
    When I send a GET request to "/events?sport=Tennis"
    Then the response status code should be 200
    And the response should contain only Tennis events
