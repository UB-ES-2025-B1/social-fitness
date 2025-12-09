Feature: User Notifications
  As a user
  I want to receive notifications
  So that I stay updated about events and messages

  Scenario: User receives notification when someone joins their event
    Given the notifications API is available
    And I am authenticated as an event organizer
    And I have created an event
    When another user joins my event
    Then I should receive a notification
    And the notification should contain "joined your event"

  Scenario: Mark notification as read
    Given the notifications API is available
    And I am authenticated as a user
    And I have unread notifications
    When I mark a notification as read
    Then the notification status should be "read"

  Scenario: Verify multiple notifications are stored
    Given the notifications API is available
    And I am authenticated as a user
    And I have multiple notifications
    Then I should have at least 3 notifications stored