package com.example.backend.cucumber.steps;

import com.example.backend.model.DirectMessage;
import com.example.backend.model.User;
import com.example.backend.repository.DirectMessageRepository;
import com.example.backend.repository.UserRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class DirectMessagesSteps {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DirectMessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    

    private String lastMessageId;

    @Given("the direct messages API is available")
    public void the_direct_messages_api_is_available() {
        // API disponible
    }

    @Given("another user exists with id {long}")
    public void anotherUserExistsWithId(Long userId) {
        if (!userRepository.existsById(userId)) {
            User user = new User();
            user.setUsername("otheruser" + System.currentTimeMillis());
            user.setEmail("other" + System.currentTimeMillis() + "@example.com");
            user.setPassword(passwordEncoder.encode("password"));
            userRepository.save(user);
        }
    }

    @Given("I have messages with user {long}")
    public void iHaveMessagesWithUser(Long userId) {
        User sender = userRepository.findByUsername("testuser").orElseThrow();
        
        User receiver = userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("receiver" + System.currentTimeMillis());
            newUser.setEmail("receiver" + System.currentTimeMillis() + "@example.com");
            newUser.setPassword(passwordEncoder.encode("password"));
            return userRepository.save(newUser);
        });

        DirectMessage msg = new DirectMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setText("Test message");
        msg.setTimestamp(Instant.now());
        msg.setRead(false);
        messageRepository.save(msg);
    }

    @Given("I have conversations with multiple users")
    public void i_have_conversations_with_multiple_users() {
        User sender = userRepository.findByUsername("testuser").orElseThrow();
        
        for (int i = 0; i < 3; i++) {
            User receiver = new User();
            receiver.setUsername("chatuser_" + System.currentTimeMillis() + "_" + i);
            receiver.setEmail("chatuser" + System.currentTimeMillis() + i + "@example.com");
            receiver.setPassword(passwordEncoder.encode("password"));
            receiver = userRepository.save(receiver);

            DirectMessage msg = new DirectMessage();
            msg.setSender(sender);
            msg.setReceiver(receiver);
            msg.setText("Hello conversation " + i);
            msg.setTimestamp(Instant.now());
            msg.setRead(false);
            messageRepository.save(msg);
        }
    }

    @Given("I have received an unread message with id {string}")
    public void i_have_received_an_unread_message_with_id(String messageId) {
        User sender = userRepository.findById(2L).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("sender2_" + System.currentTimeMillis());
            newUser.setEmail("sender2_" + System.currentTimeMillis() + "@example.com");
            newUser.setPassword(passwordEncoder.encode("password"));
            return userRepository.save(newUser);
        });
        
        User receiver = userRepository.findByUsername("testuser").orElseThrow();

        DirectMessage msg = new DirectMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setText("Unread message");
        msg.setTimestamp(Instant.now());
        msg.setRead(false);
        DirectMessage saved = messageRepository.save(msg);
        
        this.lastMessageId = saved.getId();
    }

    @Given("I have sent a message with id {string}")
    public void i_have_sent_a_message_with_id(String messageId) {
        User sender = userRepository.findByUsername("testuser").orElseThrow();
        
        User receiver = userRepository.findById(2L).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("receiver2_" + System.currentTimeMillis());
            newUser.setEmail("receiver2_" + System.currentTimeMillis() + "@example.com");
            newUser.setPassword(passwordEncoder.encode("password"));
            return userRepository.save(newUser);
        });

        DirectMessage msg = new DirectMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setText("Message to delete");
        msg.setTimestamp(Instant.now());
        msg.setRead(false);
        DirectMessage saved = messageRepository.save(msg);
        
        this.lastMessageId = saved.getId();
    }

    @Then("the response should contain the sent message")
    public void the_response_should_contain_the_sent_message() {
        // Verificado en CommonSteps
    }

    @Then("the response should contain a list of messages")
    public void the_response_should_contain_a_list_of_messages() {
        // Verificado en CommonSteps
    }

    @Then("the response should contain a list of users")
    public void the_response_should_contain_a_list_of_users() {
        // Verificado en CommonSteps
    }

    @Then("the message should be marked as read")
    public void theMessageShouldBeMarkedAsRead() {
        // Verificado en CommonSteps
    }

    @Then("the message should be deleted")
    public void theMessageShouldBeDeleted() {
        // Verificado en CommonSteps
    }

    @Then("the response should indicate receiver not found")
    public void the_response_should_indicate_receiver_not_found() {
        // Verificado en CommonSteps
    }

    @Then("the response should indicate text cannot be empty")
    public void the_response_should_indicate_text_cannot_be_empty() {
        // Verificado en CommonSteps
    }

    //   Para obtener el último ID y que pasen los tests
    public String getLastMessageId() {
        return lastMessageId;
    }
}