Feature: Event Group Chat
  As a participant in an event
  I want to chat with other participants
  So that we can coordinate and communicate

  Scenario: Participant opens event chat
    Given I am logged in as a user
    And I have joined an event "Basketball Game"
    When I click on the event in "Your Events"
    Then the chat should open
    And I should see the number of participants

  Scenario: Messages persist after closing chat
    Given I am logged in as a user
    And I have joined an event "Soccer Match"
    And I have sent a message "See you there!" in the event chat
    When I close the chat
    And I reopen the event chat
    Then I should see my previous message "See you there!"

  Scenario: Messages show sender name and time
    Given I am logged in as a user
    And I have joined an event "Yoga Session"
    When I send a message "Hello everyone" in the event chat
    Then the message should display my username
    And the message should display the current time

  Scenario: Backend returns complete ordered message list
    Given I am logged in as a user
    And I have joined an event "Tennis Match"
    And multiple messages exist in the event chat
    When I request the chat messages from the backend
    Then I should receive a complete list of messages
    And the messages should be ordered by time

  Scenario: Non-participant cannot send messages
    Given I am logged in as a user
    And there is an event "Running Club" I have not joined
    When I attempt to send a message to the event chat
    Then I should receive a 403 Forbidden response
    And the message should not be saved
