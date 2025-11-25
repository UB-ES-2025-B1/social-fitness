Feature: Event Filtering
  As a user
  I want to filter events by various criteria
  So that I can find events that match my preferences

  Background:
    Given I am logged in as a user
    And there are multiple events with different sports, locations, days, and times

  Scenario: User opens filter window
    Given I am viewing the list of all events
    When I click the filter button
    Then a filter window should open
    And I should see filter options for sport, location, day, and time

  Scenario: Filter events by sport
    Given the filter window is open
    When I toggle the "Tennis" sport button
    And I apply the filters
    Then the window should close
    And I should see only Tennis events
    And other sports should be filtered out

  Scenario: Filter events by location
    Given the filter window is open
    When I enter "Central Park" in the location search
    And I apply the filters
    Then the window should close
    And I should see only events at Central Park

  Scenario: Filter events by day of week
    Given the filter window is open
    When I toggle the "Monday" day button
    And I toggle the "Wednesday" day button
    And I apply the filters
    Then the window should close
    And I should see only events on Monday or Wednesday

  Scenario: Filter events by time range
    Given the filter window is open
    When I set the start time to "09:00"
    And I set the end time to "12:00"
    And I apply the filters
    Then the window should close
    And I should see only events between 09:00 and 12:00

  Scenario: Filter with multiple criteria
    Given the filter window is open
    When I toggle the "Yoga" sport button
    And I enter "Downtown" in the location search
    And I toggle the "Saturday" day button
    And I set the time range from "10:00" to "14:00"
    And I apply the filters
    Then I should see only Yoga events in Downtown on Saturday between 10:00 and 14:00

  Scenario: Reset all filters
    Given the filter window is open
    And I have selected multiple filters
    When I click the reset button
    Then all filter selections should be cleared
    And the filter interface should be clean

  Scenario: Apply filters shows immediate results
    Given the filter window is open
    And I have selected some filters
    When I click the apply button
    Then the window should close immediately
    And the event list should show matching events
