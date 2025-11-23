package com.example.backend.cucumber.steps;

import com.example.backend.cucumber.CucumberSpringConfiguration;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Common step definitions shared across multiple features.
 * This class contains reusable steps that apply to authentication, events, and profile features.
 */
public class CommonSteps extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<?> response;
    private String authToken;

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public void setResponse(ResponseEntity<?> response) {
        this.response = response;
    }

    public ResponseEntity<?> getResponse() {
        return response;
    }

    @When("I send a POST request to {string} with:")
    public void iSendAPOSTRequestToWith(String endpoint, DataTable dataTable) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        requestBody.putAll(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null) {
            headers.set("Authorization", "Bearer " + authToken);
        }
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            response = restTemplate.postForEntity(getBaseUrl() + endpoint, request, Map.class);
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>());
        }
    }

    @When("I send a POST request to {string}")
    public void iSendAPOSTRequestTo(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null) {
            headers.set("Authorization", "Bearer " + authToken);
        }
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(new HashMap<>(), headers);

        try {
            response = restTemplate.postForEntity(getBaseUrl() + endpoint, request, Map.class);
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>());
        }
    }

    @When("I send a GET request to {string}")
    public void iSendAGETRequestTo(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        if (authToken != null) {
            headers.set("Authorization", "Bearer " + authToken);
        }
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            // Try to get a list response first, fall back to map
            try {
                ResponseEntity<List> listResponse = restTemplate.exchange(getBaseUrl() + endpoint, HttpMethod.GET, request, List.class);
                response = listResponse;
            } catch (Exception listEx) {
                ResponseEntity<Map> mapResponse = restTemplate.exchange(getBaseUrl() + endpoint, HttpMethod.GET, request, Map.class);
                response = mapResponse;
            }
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>());
        }
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertNotNull(response, "Response should not be null");
        assertEquals(expectedStatusCode, response.getStatusCode().value(),
            "Expected status code " + expectedStatusCode + " but got " + response.getStatusCode().value());
    }

    @Then("the response status code should be {int} or {int}")
    public void theResponseStatusCodeShouldBeOr(int statusCode1, int statusCode2) {
        assertNotNull(response, "Response should not be null");
        int actualStatus = response.getStatusCode().value();
        assertTrue(actualStatus == statusCode1 || actualStatus == statusCode2,
            "Expected status code " + statusCode1 + " or " + statusCode2 + " but got " + actualStatus);
    }
}
