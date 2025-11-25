Feature: Event Filtering
  As a user
  I want to filter events by different criteria
  So that I can find events that match my interests

  Background:
    Given I am an authenticated user
    And multiple events exist with various sports, locations, and times

  Scenario: Filter events by sport
    When I request events filtered by sport "Tennis"
    Then I should receive only Tennis events
    And other sports should not be included

  Scenario: Filter events by location
    When I request events filtered by location "Central Park"
    Then I should receive only events at Central Park

  Scenario: Filter events by time range
    When I request events between "09:00" and "12:00"
    Then I should receive only events within that time range

  Scenario: Filter with multiple criteria
    When I request events with:
      | sport    | Yoga         |
      | location | Downtown     |
      | timeStart| 10:00        |
      | timeEnd  | 14:00        |
    Then I should receive events matching all criteria
    And events not matching should be excluded
