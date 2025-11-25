Feature: User Profile Configuration API
  As a REST API client
  I want to configure user sports profiles
  So that users can specify their interests and skill levels

  Background:
    Given the profile API is available
    And I am authenticated as a user with id 1

  Scenario: Configure profile with sports preferences
    When I send a POST request to "/profile/1" with sports:
      | sport    | level        |
      | Tennis   | Intermediate |
      | Swimming | Advanced     |
      | Cycling  | Beginner     |
    Then the response status code should be 200
    And the response should confirm profile update

  Scenario: Attempt to save profile without sports
    When I send a POST request to "/profile/1" with empty sports list
    Then the response status code should be 400
    And the response should indicate that sports are required

  Scenario: Update existing profile with bio
    When I send a POST request to "/profile/1" with:
      | bio | Love running and outdoor activities |
    Then the response status code should be 200
    And the response should confirm profile update
