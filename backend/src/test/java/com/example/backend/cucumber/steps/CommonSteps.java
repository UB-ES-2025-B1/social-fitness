package com.example.backend.cucumber.steps;

import com.example.backend.cucumber.CucumberSpringConfiguration;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CommonSteps extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private ApplicationContext applicationContext; //  Sin el contexto de Spring me empieza a petar seriamente 

    private ResponseEntity<Map> response;
    private String authToken;
    private Map<String, Object> lastRequestBody;

    @Override
    public String getBaseUrl() {
        return "http://localhost:" + port;
    }

    public ResponseEntity<Map> getResponse() {
        return response;
    }

    public void setResponse(ResponseEntity<Map> response) {
        this.response = response;
    }

    protected ResponseEntity<Map> getLastResponse() {
        return response;
    }

    protected String getLastResponseBody() {
        if (response != null && response.getBody() != null) {
            try {
                return new ObjectMapper().writeValueAsString(response.getBody());
            } catch (Exception e) {
                return response.getBody().toString();
            }
        }
        return "";
    }

    @Given("I am authenticated")
    public void iAmAuthenticated() {
        if (!userRepository.existsByUsername("testuser")) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("testuser@example.com");
            user.setPassword(passwordEncoder.encode("password"));
            userRepository.save(user);
        }
        this.authToken = "mock-jwt-token";
    }

    @Given("I am not authenticated")
    public void iAmNotAuthenticated() {
        this.authToken = null;
    }
    @When("I send a POST request to {string} with:")
    public void i_send_a_post_request_to_with(String endpoint, DataTable dataTable) {
        Map<String, String> body = dataTable.asMap(String.class, String.class);
        this.lastRequestBody = new HashMap<>(body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null) {
            headers.set("Authorization", "Bearer " + authToken);
        }

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            response = restTemplate.postForEntity(getBaseUrl() + endpoint, request, Map.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            response = ResponseEntity.status(e.getStatusCode()).body(parseErrorBody(e.getResponseBodyAsString()));
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @When("I send a POST request to {string} with body:")
    public void iSendAPOSTRequestToWithBody(String endpoint, DataTable dataTable) {
        Map<String, String> body = dataTable.asMap(String.class, String.class);
        this.lastRequestBody = new HashMap<>(body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null) {
            headers.set("Authorization", "Bearer " + authToken);
        }

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            response = restTemplate.postForEntity(getBaseUrl() + endpoint, request, Map.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            response = ResponseEntity.status(e.getStatusCode()).body(parseErrorBody(e.getResponseBodyAsString()));
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
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
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            response = ResponseEntity.status(e.getStatusCode()).body(parseErrorBody(e.getResponseBodyAsString()));
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @When("I send a GET request to {string}")
    public void iSendAGETRequestTo(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        
        if (authToken != null && !endpoint.startsWith("/events")) {
            headers.set("Authorization", "Bearer " + authToken);
        }
        
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            if (endpoint.startsWith("/events") || 
                endpoint.equals("/messages/chats") ||
                endpoint.startsWith("/messages/users/")) {
                
                ResponseEntity<List> listResponse = restTemplate.exchange(
                    getBaseUrl() + endpoint, HttpMethod.GET, request, List.class
                );
                response = ResponseEntity.status(listResponse.getStatusCode())
                    .body(Map.of("data", listResponse.getBody()));
            } else {
                response = restTemplate.exchange(getBaseUrl() + endpoint, HttpMethod.GET, request, Map.class);
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("❌ GET " + endpoint + " → " + e.getStatusCode());
            System.err.println("Response: " + e.getResponseBodyAsString());
            response = ResponseEntity.status(e.getStatusCode()).body(parseErrorBody(e.getResponseBodyAsString()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error on GET " + endpoint + ": " + e.getMessage());
            e.printStackTrace();
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @When("I send a PUT request to {string}")
    public void iSendAPUTRequestTo(String endpoint) {
        //   DirectMessagesSteps ha de quedarnos dinámicamente , nos petaba bastante debido aun circular dependency que me faltan detalles
        if (endpoint.contains("msg-1")) {
            try {
                DirectMessagesSteps directMessagesSteps = applicationContext.getBean(DirectMessagesSteps.class);
                String realId = directMessagesSteps.getLastMessageId();
                if (realId != null) {
                    endpoint = endpoint.replace("msg-1", realId);
                }
            } catch (Exception e) {
                System.err.println("  Could not get DirectMessagesSteps: " + e.getMessage());
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null) {
            headers.set("Authorization", "Bearer " + authToken);
        }
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(new HashMap<>(), headers);

        try {
            response = restTemplate.exchange(getBaseUrl() + endpoint, HttpMethod.PUT, request, Map.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            response = ResponseEntity.status(e.getStatusCode()).body(parseErrorBody(e.getResponseBodyAsString()));
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @When("I send a DELETE request to {string}")
    public void iSendADELETERequestTo(String endpoint) {
        //   Resolver DirectMessagesSteps dinámicamente
        if (endpoint.contains("msg-1")) {
            try {
                DirectMessagesSteps directMessagesSteps = applicationContext.getBean(DirectMessagesSteps.class);
                String realId = directMessagesSteps.getLastMessageId();
                if (realId != null) {
                    endpoint = endpoint.replace("msg-1", realId);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Could not get DirectMessagesSteps: " + e.getMessage());
            }
        }

        HttpHeaders headers = new HttpHeaders();
        if (authToken != null) {
            headers.set("Authorization", "Bearer " + authToken);
        }
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            response = restTemplate.exchange(getBaseUrl() + endpoint, HttpMethod.DELETE, request, Map.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            response = ResponseEntity.status(e.getStatusCode()).body(parseErrorBody(e.getResponseBodyAsString()));
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
        }
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatus) {
        assertNotNull(response, "Response should not be null");
        assertEquals(expectedStatus, response.getStatusCode().value(),
            "Expected status code " + expectedStatus + " but got " + response.getStatusCode().value());
    }
    @Then("the response status code should be {int} or {int}")
    public void theResponseStatusCodeShouldBeOr(int status1, int status2) {
        assertNotNull(response, "Response should not be null");
        int actualStatus = response.getStatusCode().value();
        assertTrue(actualStatus == status1 || actualStatus == status2,
            "Expected status code " + status1 + " or " + status2 + " but got " + actualStatus);
    }

    @Then("the response should contain {string}")
    public void theResponseShouldContain(String key) {
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().containsKey(key) || 
                   getLastResponseBody().contains(key),
            "Response should contain key: " + key);
    }

    private Map<String, Object> parseErrorBody(String body) {
        try {
            return new ObjectMapper().readValue(body, Map.class);
        } catch (Exception e) {
            return Map.of("message", body);
        }
    }
}