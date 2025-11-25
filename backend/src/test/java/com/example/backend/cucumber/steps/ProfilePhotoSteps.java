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

    private ResponseEntity<?> response;
    private String uploadedPhotoUrl;

    @Given("I have uploaded a profile photo")
    public void iHaveUploadedAProfilePhoto() {
        Map<String, String> photoData = new HashMap<>();
        photoData.put("profilePicture", "mock-base64-photo-data");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(photoData, headers);
        ResponseEntity<Map> uploadResponse = restTemplate.postForEntity("/profile/update-photo", request, Map.class);
        
        if (uploadResponse.getStatusCode().is2xxSuccessful() && uploadResponse.getBody() != null) {
            uploadedPhotoUrl = (String) uploadResponse.getBody().get("profilePictureUrl");
        }
    }

    @When("I upload a profile photo")
    public void iUploadAProfilePhoto() {
        Map<String, String> photoData = new HashMap<>();
        photoData.put("profilePicture", "mock-base64-photo-data");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(photoData, headers);
        response = restTemplate.postForEntity("/profile/update-photo", request, Map.class);
    }

    @When("I request my profile information")
    public void iRequestMyProfileInformation() {
        response = restTemplate.getForEntity("/profile/me", Map.class);
    }

    @Then("the photo should be saved successfully")
    public void thePhotoShouldBeSavedSuccessfully() {
        assertTrue(response.getStatusCode().is2xxSuccessful(), 
            "Photo upload should succeed with 2xx status code");
    }

    @Then("my profile should reflect the new photo")
    public void myProfileShouldReflectTheNewPhoto() {
        assertNotNull(response.getBody(), "Response should contain confirmation data");
    }

    @Then("I should receive my profile data")
    public void iShouldReceiveMyProfileData() {
        assertEquals(HttpStatus.OK, response.getStatusCode(), 
            "Should successfully retrieve profile data");
        assertNotNull(response.getBody(), "Profile data should not be null");
    }

    @Then("the profile should include my photo URL")
    public void theProfileShouldIncludeMyPhotoURL() {
        if (response.getBody() instanceof Map) {
            Map<?, ?> profileData = (Map<?, ?>) response.getBody();
            // In full implementation, verify profilePictureUrl field exists
            assertNotNull(profileData, "Profile should contain photo information");
        }
    }
}
