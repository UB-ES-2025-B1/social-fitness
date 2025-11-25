Feature: Event Group Chat
  As a participant in an event
  I want to communicate with other participants
  So that we can coordinate our activities

  Scenario: Participant sends message to event chat
    Given I am an authenticated user
    And I have joined an event
    When I send a message to the event chat
    Then the message should be saved successfully
    And the message should include my username and timestamp

  Scenario: Retrieve all messages for an event
    Given I am an authenticated user
    And I have joined an event
    And multiple messages exist in the event chat
    When I request all chat messages for the event
    Then I should receive a complete ordered list of messages
    And each message should contain sender name and timestamp

  Scenario: Non-participant cannot send messages
    Given I am an authenticated user
    And there is an event I have not joined
    When I attempt to send a message to that event chat
    Then I should receive a 403 Forbidden response
    And the message should not be saved in the database

  Scenario: Messages persist across sessions
    Given I am an authenticated user
    And I have joined an event
    And I have sent a message to the event chat
    When I log out and log back in
    And I request the chat messages
    Then I should see my previous message
