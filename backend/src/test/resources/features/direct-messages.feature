Feature: Direct Messages Management

  Scenario: Send a direct message to another user
    Given the direct messages API is available
    And I am authenticated as a user
    Given another user exists with id 2
    When I send a POST request to "/messages/users/2" with:
      | text | Hello, how are you? |
    Then the response status code should be 201
    And the response should contain the sent message

  Scenario: View conversation with another user
    Given the direct messages API is available
    And I am authenticated as a user
    Given I have messages with user 2
    When I send a GET request to "/messages/users/2"
    Then the response status code should be 200
    And the response should contain a list of messages

  Scenario: Get all my conversations
    Given the direct messages API is available
    And I am authenticated as a user
    Given I have conversations with multiple users
    When I send a GET request to "/messages/chats"
    Then the response status code should be 200
    And the response should contain a list of users

  Scenario: Mark message as read
    Given the direct messages API is available
    And I am authenticated as a user
    Given I have received an unread message with id "msg-1"
    When I send a PUT request to "/messages/msg-1/read"
    Then the response status code should be 200
    And the message should be marked as read

  Scenario: Delete a message I sent
    Given the direct messages API is available
    And I am authenticated as a user
    Given I have sent a message with id "msg-1"
    When I send a DELETE request to "/messages/msg-1"
    Then the response status code should be 200
    And the message should be deleted

  Scenario: Try to send message to non-existent user
    Given the direct messages API is available
    And I am authenticated as a user
    When I send a POST request to "/messages/users/999" with:
      | text | Hello |
    Then the response status code should be 404
    And the response should indicate receiver not found

  Scenario: Try to send empty message
    Given the direct messages API is available
    And I am authenticated as a user
    When I send a POST request to "/messages/users/2" with:
      | text |  |
    Then the response status code should be 400
    And the response should indicate text cannot be empty