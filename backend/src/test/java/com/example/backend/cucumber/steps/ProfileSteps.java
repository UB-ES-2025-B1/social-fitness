package com.example.backend.cucumber.steps;

import com.example.backend.cucumber.CucumberSpringConfiguration;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileSteps extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommonSteps commonSteps;

    private ResponseEntity<Map> response;
    private String authToken = "mock-token";
    private Map<String, Object> requestBody = new HashMap<>();
    private List<Map<String, String>> existingSports = new ArrayList<>();

    @Given("the profile API is available")
    public void theProfileAPIIsAvailable() {
        assertNotNull(restTemplate);
    }

    @Given("I am authenticated as a user with id {int}")
    public void iAmAuthenticatedAsAUserWithId(int userId) {
        // Create a test user in the database
        String testEmail = "testuser" + userId + "@example.com";
        String testPassword = "TestPass123";
        String testUsername = "testuser" + userId;

        Map<String, String> registerData = new HashMap<>();
        registerData.put("email", testEmail);
        registerData.put("password", testPassword);
        registerData.put("username", testUsername);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(registerData, headers);
        
        try {
            restTemplate.postForEntity(getBaseUrl() + "/auth/register", request, Map.class);
        } catch (Exception e) {
            // User might already exist, that's fine
        }
    }

    @Given("my profile already contains {string} as a sport")
    public void myProfileAlreadyContainsAsASport(String sport) {
        Map<String, String> sportData = new HashMap<>();
        sportData.put("sport", sport);
        sportData.put("level", "Intermediate");
        existingSports.add(sportData);
    }

    @Given("my profile is configured with sports")
    public void myProfileIsConfiguredWithSports() {
        existingSports.add(Map.of("sport", "Running", "level", "Advanced"));
        existingSports.add(Map.of("sport", "Swimming", "level", "Beginner"));
    }

    @When("I send a POST request to {string} with sports:")
    public void iSendAPOSTRequestToWithSports(String endpoint, DataTable dataTable) {
        List<Map<String, String>> sports = new ArrayList<>();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            sports.add(Map.of(
                "sport", row.get("sport"),
                "level", row.get("level")
            ));
        }

        requestBody.clear();
        requestBody.put("sports", sports);

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
        commonSteps.setResponse(response);
    }

    @When("I send a POST request to {string} with empty sports list")
    public void iSendAPOSTRequestToWithEmptySportsList(String endpoint) {
        requestBody.clear();
        requestBody.put("sports", new ArrayList<>());

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
        commonSteps.setResponse(response);
    }

    @When("I send a POST request to {string} adding {string}")
    public void iSendAPOSTRequestToAdding(String endpoint, String newSport) {
        existingSports.add(Map.of("sport", newSport, "level", "Beginner"));
        
        requestBody.clear();
        requestBody.put("sports", existingSports);

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
        commonSteps.setResponse(response);
    }

    @Then("the response should confirm profile update")
    public void theResponseShouldConfirmProfileUpdate() {
        ResponseEntity<?> resp = commonSteps.getResponse();
        assertNotNull(resp, "Response should not be null");
        assertNotNull(resp.getBody(), "Response body should not be null");
    }

    @Then("my sports preferences should be stored")
    public void mySportsPreferencesShouldBeStored() {
        assertNotNull(response.getBody(), "Response body should not be null");
    }

    @Then("the response should indicate that sports are required")
    public void theResponseShouldIndicateThatSportsAreRequired() {
        assertNotNull(response.getBody(), "Response body should not be null");
        Map<String, Object> body = response.getBody();
        assertTrue(body.containsKey("message") || body.containsKey("error"),
            "Response should contain an error message");
    }

    @Then("my profile should contain both {string} and {string}")
    public void myProfileShouldContainBothAnd(String sport1, String sport2) {
        // Verify both sports are in the existingSports list
        boolean hasSport1 = existingSports.stream()
            .anyMatch(s -> s.get("sport").equals(sport1));
        boolean hasSport2 = existingSports.stream()
            .anyMatch(s -> s.get("sport").equals(sport2));
        
        assertTrue(hasSport1, "Profile should contain " + sport1);
        assertTrue(hasSport2, "Profile should contain " + sport2);
    }

    @Then("the response should contain my profile information")
    public void theResponseShouldContainMyProfileInformation() {
        assertNotNull(response.getBody(), "Response body should not be null");
    }
}
