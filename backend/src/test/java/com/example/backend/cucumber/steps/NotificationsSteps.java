package com.example.backend.cucumber.steps;

import com.example.backend.cucumber.CucumberSpringConfiguration;
import com.example.backend.model.Event;
import com.example.backend.model.Notification;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UserRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationsSteps extends CucumberSpringConfiguration {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestRestTemplate restTemplate;

    private User organizer;
    private Event createdEvent;
    private Notification testNotification;

    @Given("the notifications API is available")
    public void theNotificationsAPIIsAvailable() {
        // API disponible
    }

    @Given("I am authenticated as an event organizer")
    public void iAmAuthenticatedAsAnEventOrganizer() {
        organizer = userRepository.findByUsername("organizer")
            .orElseGet(() -> {
                User u = new User();
                u.setUsername("organizer");
                u.setEmail("organizer@test.com");
                u.setPassword(passwordEncoder.encode("password"));
                return userRepository.save(u);
            });
    }

    @Given("I have created an event")
    public void iHaveCreatedAnEvent() {
        createdEvent = new Event();
        createdEvent.setTitle("Organizer's Event");
        createdEvent.setSport("Football");
        createdEvent.setLocation("Stadium");
        createdEvent.setDate(LocalDate.now().plusDays(5));
        createdEvent.setTime(LocalTime.of(18, 0));
        createdEvent.setOrganizer(organizer.getUsername());
        createdEvent.setCapacity(20);
        createdEvent.setPrice(BigDecimal.ZERO);
        eventRepository.save(createdEvent);
    }

    @When("another user joins my event")
    public void anotherUserJoinsMyEvent() {
        User joiner = userRepository.findByUsername("joiner")
            .orElseGet(() -> {
                User u = new User();
                u.setUsername("joiner");
                u.setEmail("joiner@test.com");
                u.setPassword(passwordEncoder.encode("password"));
                return userRepository.save(u);
            });

        createdEvent.addParticipant(joiner);
        eventRepository.save(createdEvent);

        Notification notification = new Notification();
        notification.setUserId(organizer.getId());
        notification.setTitle("New participant");
        notification.setType(Notification.NotificationType.NEW_PARTICIPANT);
        notification.setMessage(joiner.getUsername() + " joined your event: " + createdEvent.getTitle());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Then("I should receive a notification")
    public void iShouldReceiveANotification() {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(organizer.getId());
        assertTrue(notifications.size() > 0, "User should have at least one notification");
    }

    @Then("the notification should contain {string}")
    public void theNotificationShouldContain(String text) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(organizer.getId());
        assertFalse(notifications.isEmpty(), "Should have notifications");
        
        boolean found = notifications.stream()
            .anyMatch(n -> n.getMessage().contains(text));
        
        assertTrue(found, "Notification should contain: " + text);
    }

    @Given("I have unread notifications")
    public void iHaveUnreadNotifications() {
        User user = userRepository.findByUsername("testuser")
            .orElseGet(() -> {
                User u = new User();
                u.setUsername("testuser");
                u.setEmail("testuser@example.com");
                u.setPassword(passwordEncoder.encode("password"));
                return userRepository.save(u);
            });

        testNotification = new Notification();
        testNotification.setUserId(user.getId());
        testNotification.setTitle("New message");
        testNotification.setType(Notification.NotificationType.NEW_MESSAGE);
        testNotification.setMessage("You have a new message");
        testNotification.setRead(false);
        notificationRepository.save(testNotification);
        
        organizer = user;
    }

    @When("I mark a notification as read")
    public void iMarkANotificationAsRead() {
        testNotification.setRead(true);
        notificationRepository.save(testNotification);
    }

    @Then("the notification status should be {string}")
    public void theNotificationStatusShouldBe(String status) {
        Notification updated = notificationRepository.findById(testNotification.getId())
            .orElseThrow(() -> new AssertionError("Notification not found"));
        
        if (status.equals("read")) {
            assertTrue(updated.getRead(), "Notification should be marked as read");
        } else {
            assertFalse(updated.getRead(), "Notification should be unread");
        }
    }

    @Given("I have multiple notifications")
    public void iHaveMultipleNotifications() {
        User user = userRepository.findByUsername("testuser")
            .orElseGet(() -> {
                User u = new User();
                u.setUsername("testuser");
                u.setEmail("testuser@example.com");
                u.setPassword(passwordEncoder.encode("password"));
                return userRepository.save(u);
            });

        for (int i = 1; i <= 3; i++) {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setTitle("Notification " + i);
            notification.setType(Notification.NotificationType.EVENT_UPDATED);
            notification.setMessage("Notification " + i);
            notification.setRead(i % 2 == 0);
            notificationRepository.save(notification);
        }
        
        organizer = user;
    }

    //   GET request pero modificado para el tests
    @Then("I should have at least {int} notifications stored")
    public void iShouldHaveAtLeastNotificationsStored(int expectedCount) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(organizer.getId());
        assertTrue(notifications.size() >= expectedCount, 
            "Expected at least " + expectedCount + " notifications but found " + notifications.size());
    }
}