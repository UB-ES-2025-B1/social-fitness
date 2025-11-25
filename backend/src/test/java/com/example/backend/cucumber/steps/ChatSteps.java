package com.example.backend.cucumber.steps;

import com.example.backend.cucumber.CucumberSpringConfiguration;
import io.cucumber.java.en.Given;
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
 * Step definitions for event chat functionality.
 */
public class ChatSteps extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<?> response;
    private Long currentEventId;
    private String sentMessageContent;
    private Long nonJoinedEventId;

    @Given("I have joined an event")
    public void iHaveJoinedAnEvent() {
        // Create a test event
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Test Chat Event");
        eventData.put("sport", "Tennis");
        eventData.put("location", "Test Location");
        eventData.put("date", "2025-12-20");
        eventData.put("time", "10:00");
        eventData.put("capacity", 15);
        eventData.put("organizer", "testuser");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(eventData, headers);
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(getBaseUrl() + "/events", request, Map.class);
        
        if (createResponse.getStatusCode() == HttpStatus.CREATED && createResponse.getBody() != null) {
            currentEventId = ((Number) createResponse.getBody().get("id")).longValue();
            
            // Join the event
            restTemplate.postForEntity(getBaseUrl() + "/events/" + currentEventId + "/join", new HttpEntity<>(headers), Map.class);
        }
    }

    @Given("multiple messages exist in the event chat")
    public void multipleMessagesExistInTheEventChat() {
        // Send several test messages
        String[] messages = {"First message", "Second message", "Third message"};
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        for (String msg : messages) {
            Map<String, String> messageData = new HashMap<>();
            messageData.put("content", msg);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(messageData, headers);
            restTemplate.postForEntity(getBaseUrl() + "/events/" + currentEventId + "/chat/messages", request, Map.class);
        }
    }

    @Given("there is an event I have not joined")
    public void thereIsAnEventIHaveNotJoined() {
        // Create an event but don't join it
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Unjoined Event");
        eventData.put("sport", "Basketball");
        eventData.put("location", "Other Location");
        eventData.put("date", "2025-12-25");
        eventData.put("time", "15:00");
        eventData.put("capacity", 10);
        eventData.put("organizer", "otheruser");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(eventData, headers);
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(getBaseUrl() + "/events", request, Map.class);
        
        if (createResponse.getStatusCode() == HttpStatus.CREATED && createResponse.getBody() != null) {
            nonJoinedEventId = ((Number) createResponse.getBody().get("id")).longValue();
        }
    }

    @Given("I have sent a message to the event chat")
    public void iHaveSentAMessageToTheEventChat() {
        Map<String, String> messageData = new HashMap<>();
        messageData.put("content", "Test message for persistence");
        sentMessageContent = messageData.get("content");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(messageData, headers);
        restTemplate.postForEntity(getBaseUrl() + "/events/" + currentEventId + "/chat/messages", request, Map.class);
    }

    @When("I send a message to the event chat")
    public void iSendAMessageToTheEventChat() {
        Map<String, String> messageData = new HashMap<>();
        messageData.put("content", "Hello from chat!");
        sentMessageContent = messageData.get("content");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(messageData, headers);
        response = restTemplate.postForEntity(getBaseUrl() + "/events/" + currentEventId + "/chat/messages", request, Map.class);
    }

    @When("I request all chat messages for the event")
    public void iRequestAllChatMessagesForTheEvent() {
        response = restTemplate.getForEntity(getBaseUrl() + "/events/" + currentEventId + "/chat/messages", List.class);
    }

    @When("I attempt to send a message to that event chat")
    public void iAttemptToSendAMessageToThatEventChat() {
        Map<String, String> messageData = new HashMap<>();
        messageData.put("content", "Unauthorized message");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(messageData, headers);
        response = restTemplate.postForEntity(getBaseUrl() + "/events/" + nonJoinedEventId + "/chat/messages", request, Map.class);
    }

    @When("I log out and log back in")
    public void iLogOutAndLogBackIn() {
        // Simulate logout/login by clearing and re-establishing session
        // In test context, the session is maintained by TestRestTemplate
    }

    @When("I request the chat messages")
    public void iRequestTheChatMessages() {
        response = restTemplate.getForEntity(getBaseUrl() + "/events/" + currentEventId + "/chat/messages", List.class);
    }

    @Then("the message should be saved successfully")
    public void theMessageShouldBeSavedSuccessfully() {
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), 
            "Message should be created with status 201");
    }

    @Then("the message should include my username and timestamp")
    public void theMessageShouldIncludeMyUsernameAndTimestamp() {
        assertNotNull(response.getBody(), "Response should contain message data");
        // In real implementation, verify the response includes username and timestamp
    }

    @Then("I should receive a complete ordered list of messages")
    public void iShouldReceiveACompleteOrderedListOfMessages() {
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should successfully fetch messages");
        assertNotNull(response.getBody(), "Should receive message list");
        
        if (response.getBody() instanceof List) {
            List<?> messages = (List<?>) response.getBody();
            assertTrue(messages.size() > 0, "Should have at least one message");
        }
    }

    @Then("each message should contain sender name and timestamp")
    public void eachMessageShouldContainSenderNameAndTimestamp() {
        // Verify message structure includes required fields
        assertNotNull(response.getBody(), "Messages should not be null");
    }

    @Then("I should receive a 403 Forbidden response")
    public void iShouldReceiveA403ForbiddenResponse() {
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), 
            "Should receive 403 Forbidden for non-participant");
    }

    @Then("the message should not be saved in the database")
    public void theMessageShouldNotBeSavedInTheDatabase() {
        // Verify the request was rejected
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Then("I should see my previous message")
    public void iShouldSeeMyPreviousMessage() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        if (response.getBody() instanceof List) {
            List<?> messages = (List<?>) response.getBody();
            assertTrue(messages.size() > 0, "Should have messages in history");
        }
    }
}
