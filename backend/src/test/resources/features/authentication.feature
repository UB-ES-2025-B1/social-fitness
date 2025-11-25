Feature: User Authentication API
  As a REST API client
  I want to authenticate users
  So that they can access the Social Fitness platform securely

  Scenario: Successful user registration
    Given the authentication API is available
    When I send a POST request to "/auth/register" with:
      | email    | newuser@example.com |
      | password | SecurePass123       |
      | username | newuser             |
    Then the response status code should be 201
    And the response should contain a success message

  Scenario: Registration with missing email
    Given the authentication API is available
    When I send a POST request to "/auth/register" with:
      | password | SecurePass123 |
      | username | testuser      |
    Then the response status code should be 400
    And the response should contain an error message

  Scenario: Successful login
    Given the authentication API is available
    And a user exists with email "existinguser@example.com" and password "ValidPass123"
    When I send a POST request to "/auth/login" with:
      | username | existinguser |
      | password | ValidPass123 |
    Then the response status code should be 200
    And the response should contain a success message

  Scenario: Login with incorrect password
    Given the authentication API is available
    And a user exists with email "existinguser@example.com" and password "ValidPass123"
    When I send a POST request to "/auth/login" with:
      | username | existinguser  |
      | password | WrongPassword |
    Then the response status code should be 400 or 401
    And the response should not contain an authentication token
