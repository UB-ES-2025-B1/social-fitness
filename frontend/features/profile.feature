Feature: User Profile Configuration
  As a user
  I want to configure my sports profile
  So that I can specify my interests and skill levels

  Background:
    Given I am logged in as a user

  Scenario: Configure profile with sports preferences
    When I select the following sports:
      | sport      | level        |
      | Tennis     | Intermediate |
      | Swimming   | Advanced     |
      | Cycling    | Beginner     |
    And I save my profile
    Then my profile should be updated successfully
    And my sports preferences should be stored

  Scenario: Attempt to save profile without sports
    When I try to save my profile without selecting any sports
    Then I should receive a validation error
    And the error should indicate that at least one sport is required

  Scenario: Update existing profile
    Given I have already configured my profile with "Running"
    When I add "Yoga" to my sports list
    And I save my profile
    Then my profile should include both "Running" and "Yoga"
