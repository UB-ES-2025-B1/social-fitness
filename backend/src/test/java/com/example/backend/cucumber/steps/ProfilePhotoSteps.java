package com.example.backend.cucumber.steps;

import com.example.backend.cucumber.CucumberSpringConfiguration;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for profile photo management.
 */
public class ProfilePhotoSteps extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommonSteps commonSteps;

    private ResponseEntity<?> response;
    private String uploadedPhotoUrl;
    private String authToken;
    private Long currentUserId;

    @Given("I have uploaded a profile photo")
    public void iHaveUploadedAProfilePhoto() {
        // Get auth token from common steps
        this.authToken = getAuthTokenFromCommonSteps();
        
        Map<String, String> photoData = new HashMap<>();
        photoData.put("profileImage", "mock-base64-photo-data");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authToken);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(photoData, headers);
        ResponseEntity<Map> uploadResponse = restTemplate.exchange(
            getBaseUrl() + "/profile",
            HttpMethod.PUT,
            request,
            Map.class
        );
        
        if (uploadResponse.getStatusCode().is2xxSuccessful()) {
            uploadedPhotoUrl = "mock-base64-photo-data";
        }
    }

    @When("I upload a profile photo")
    public void iUploadAProfilePhoto() {
        this.authToken = getAuthTokenFromCommonSteps();
        
        Map<String, String> photoData = new HashMap<>();
        photoData.put("profileImage", "mock-base64-photo-data");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authToken);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(photoData, headers);
        response = restTemplate.exchange(
            getBaseUrl() + "/profile",
            HttpMethod.PUT,
            request,
            Map.class
        );
    }

    @When("I request my profile information")
    public void iRequestMyProfileInformation() {
        this.authToken = getAuthTokenFromCommonSteps();
        this.currentUserId = getCurrentUserIdFromCommonSteps();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        response = restTemplate.exchange(
            getBaseUrl() + "/users/" + currentUserId,
            HttpMethod.GET,
            request,
            Map.class
        );
    }

    @Then("the photo should be saved successfully")
    public void thePhotoShouldBeSavedSuccessfully() {
        assertTrue(response.getStatusCode().is2xxSuccessful(), 
            "Photo upload should succeed with 2xx status code");
    }

    @Then("my profile should reflect the new photo")
    public void myProfileShouldReflectTheNewPhoto() {
        assertNotNull(response.getBody(), "Response should contain profile data");
        if (response.getBody() instanceof Map) {
            Map body = (Map) response.getBody();
            assertNotNull(body.get("profileImage"), "Profile should have profileImage field");
        }
    }
    
    private String getAuthTokenFromCommonSteps() {
        try {
            java.lang.reflect.Field field = CommonSteps.class.getDeclaredField("authToken");
            field.setAccessible(true);
            return (String) field.get(commonSteps);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get auth token from CommonSteps", e);
        }
    }
    
    private Long getCurrentUserIdFromCommonSteps() {
        try {
            java.lang.reflect.Field field = CommonSteps.class.getDeclaredField("currentUserId");
            field.setAccessible(true);
            return (Long) field.get(commonSteps);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current user ID from CommonSteps", e);
        }
    }
}
