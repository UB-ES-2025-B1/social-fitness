Feature: Profile Photo Management
  As a user
  I want to edit my profile photo
  So that I can personalize my account

  Scenario: User views their current avatar
    Given I am logged in as a user
    When I navigate to "My Profile"
    Then I should see my current avatar

  Scenario: User changes their profile photo
    Given I am logged in as a user
    And I am on the profile page
    When I click the "Change Photo" button
    And I select a local image file
    And I save the new photo
    Then the image should be uploaded to the backend
    And my avatar should be updated in the app

  Scenario: User previews photo before saving
    Given I am logged in as a user
    And I am on the profile page
    When I click the "Change Photo" button
    And I select a local image file
    Then I should see a preview of the selected image
    When I save the new photo
    Then my avatar should display the new image
