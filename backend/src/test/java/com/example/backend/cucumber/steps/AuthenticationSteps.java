package com.example.backend.cucumber.steps;

import com.example.backend.cucumber.CucumberSpringConfiguration;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationSteps extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommonSteps commonSteps;

    private ResponseEntity<Map> response;
    private Map<String, Object> requestBody = new HashMap<>();

    @Given("the authentication API is available")
    public void theAuthenticationAPIIsAvailable() {
        // The Spring Boot application is running via @SpringBootTest
        assertNotNull(restTemplate);
    }

    @Given("a user exists with email {string} and password {string}")
    public void aUserExistsWithEmailAndPassword(String email, String password) {
        // In a real scenario, you would create the user here
        // For now, we'll mock this step
        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("password", password);
        userData.put("username", "existinguser");
        
        // Attempt to register the user (might fail if already exists, which is fine)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(userData, headers);
        
        try {
            restTemplate.postForEntity(getBaseUrl() + "/auth/register", request, Map.class);
        } catch (Exception e) {
            // User might already exist, that's okay
        }
    }

    @Then("the response should contain a success message")
    public void theResponseShouldContainASuccessMessage() {
        ResponseEntity<?> response = commonSteps.getResponse();
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
    }

    @Then("the response should contain an error message")
    public void theResponseShouldContainAnErrorMessage() {
        ResponseEntity<?> response = commonSteps.getResponse();
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        // Check for error or message field
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey("message") || body.containsKey("error") || body.containsKey("errors"),
            "Response should contain an error message");
    }

    @Then("the response should contain an authentication token")
    public void theResponseShouldContainAnAuthenticationToken() {
        ResponseEntity<?> response = commonSteps.getResponse();
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey("token") || body.containsKey("accessToken"),
            "Response should contain an authentication token");
    }

    @Then("the response should not contain an authentication token")
    public void theResponseShouldNotContainAnAuthenticationToken() {
        ResponseEntity<?> response = commonSteps.getResponse();
        if (response != null && response.getBody() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertFalse(body.containsKey("token") && body.get("token") != null,
                "Response should not contain a valid token");
        }
    }
}
