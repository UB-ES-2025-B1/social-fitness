@Pending
Feature: Profile Photo Management
  As a user
  I want to manage my profile photo
  So that I can personalize my account

  Scenario: Upload new profile photo
    Given I am an authenticated user
    When I upload a profile photo
    Then the photo should be saved successfully
    And my profile should reflect the new photo

  Scenario: Retrieve user profile with photo
    Given I am an authenticated user
    And I have uploaded a profile photo
    When I request my profile information
    Then I should receive my profile data
    And the profile should include my photo URL
