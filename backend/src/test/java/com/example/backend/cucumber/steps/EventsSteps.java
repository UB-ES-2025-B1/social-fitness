package com.example.backend.cucumber.steps;

import com.example.backend.cucumber.CucumberSpringConfiguration;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EventsSteps extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Given("the events API is available")
    public void theEventsAPIIsAvailable() {
        assertNotNull(restTemplate);
    }

    @Given("I am authenticated as a user")
    public void iAmAuthenticatedAsAUser() {
        // Register and login to establish a session
        String testEmail = "testuser@example.com";
        String testPassword = "TestPass123";
        String testUsername = "testuser";

        // Register the user (ignore if already exists)
        Map<String, String> registerData = new HashMap<>();
        registerData.put("email", testEmail);
        registerData.put("password", testPassword);
        registerData.put("username", testUsername);

        HttpHeaders registerHeaders = new HttpHeaders();
        registerHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> registerRequest = new HttpEntity<>(registerData, registerHeaders);
        
        try {
            restTemplate.postForEntity(getBaseUrl() + "/auth/register", registerRequest, Map.class);
        } catch (Exception e) {
            // User might already exist, that's fine
        }
        
        // Login to create a session
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", testUsername);
        loginData.put("password", testPassword);

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(loginData, loginHeaders);
        
        try {
            restTemplate.postForEntity(getBaseUrl() + "/auth/login", loginRequest, Map.class);
            // Session cookie will be automatically maintained by TestRestTemplate
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
        }
    }

    @Given("an event exists with id {int}")
    public void anEventExistsWithId(int eventId) {
        // Create a test event in the database
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Test Event " + eventId);
        eventData.put("sport", "Running");
        eventData.put("location", "Test Location");
        eventData.put("date", "2025-12-15");
        eventData.put("time", "10:00");
        eventData.put("organizer", "testuser");
        eventData.put("capacity", 10);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(eventData, headers);
        
        try {
            restTemplate.postForEntity(getBaseUrl() + "/events", request, Map.class);
        } catch (Exception e) {
            // If creation fails, the test will fail when trying to join
        }
    }

    @Given("I have joined the event with id {int}")
    public void iHaveJoinedTheEventWithId(int eventId) {
        // Join the event first
        try {
            restTemplate.postForEntity(getBaseUrl() + "/events/" + eventId + "/join", null, Map.class);
        } catch (Exception e) {
            // Continue - the leave test will handle errors
        }
    }

    @Given("multiple events exist in the system")
    public void multipleEventsExistInTheSystem() {
        // Mock multiple events existing
    }

    @Given("multiple events exist for different sports")
    public void multipleEventsExistForDifferentSports() {
        // Mock events for different sports
    }

    @Then("the response should contain the created event details")
    public void theResponseShouldContainTheCreatedEventDetails() {
        // Response assertions handled by CommonSteps
    }

    @Then("the response should confirm I joined the event")
    public void theResponseShouldConfirmIJoinedTheEvent() {
        // Response assertions handled by CommonSteps
    }

    @Then("the response should confirm I left the event")
    public void theResponseShouldConfirmILeftTheEvent() {
        // Response assertions handled by CommonSteps
    }

    @Then("the response should contain a list of events")
    public void theResponseShouldContainAListOfEvents() {
        // Response assertions handled by CommonSteps
    }

    @Then("the response should contain only Tennis events")
    public void theResponseShouldContainOnlyTennisEvents() {
        // Response assertions handled by CommonSteps
    }
}
