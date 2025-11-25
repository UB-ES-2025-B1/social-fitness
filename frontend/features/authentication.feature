Feature: User Authentication
  As a user
  I want to be able to register and login
  So that I can access the Social Fitness platform

  Scenario: Successful user registration
    Given I am a new user
    When I register with email "test@example.com" and password "SecurePass123"
    Then I should receive a successful registration response
    And my account should be created

  Scenario: Registration with missing fields
    Given I am a new user
    When I try to register without providing an email
    Then I should receive a validation error
    And the error should indicate that email is required

  Scenario: Successful login
    Given I am a registered user with email "test@example.com"
    When I login with correct credentials
    Then I should receive a successful login response
    And I should receive an authentication token

  Scenario: Login with incorrect credentials
    Given I am a registered user
    When I login with incorrect password
    Then I should receive an authentication error
    And I should not receive a token
