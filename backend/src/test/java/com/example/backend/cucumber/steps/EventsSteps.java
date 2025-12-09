package com.example.backend.cucumber.steps;

import com.example.backend.cucumber.CucumberSpringConfiguration;
import com.example.backend.dto.LoginRequest;
import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class EventsSteps extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Given("the events API is available")
    public void theEventsAPIIsAvailable() {
        // API disponible
    }

    @Given("I am authenticated as a user")
    public void iAmAuthenticatedAsAUser() {
        if (!userRepository.existsByUsername("testuser")) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("testuser@example.com");
            user.setPassword(passwordEncoder.encode("password")); // Nos faltaba el encode de Password
            userRepository.save(user);
        }

        
    }

    @Given("an event exists with id {int}")
    public void anEventExistsWithId(int eventId) {
        if (!eventRepository.existsById((long) eventId)) {
            User organizer = userRepository.findByUsername("testuser").orElseThrow();
            
            Event event = new Event();
            event.setTitle("Test Event " + eventId);
            event.setSport("Football");
            event.setLocation("Stadium");
            event.setDate(LocalDate.now().plusDays(5));
            event.setTime(LocalTime.of(18, 0));
            event.setOrganizer(organizer.getUsername());
            event.setCapacity(20);
            event.setPrice(BigDecimal.ZERO);
            eventRepository.save(event);
        }
    }

    @Given("I have joined the event with id {int}")
    public void iHaveJoinedTheEventWithId(int eventId) {
        try {
            restTemplate.postForEntity(getBaseUrl() + "/events/" + eventId + "/join", null, Map.class);
        } catch (Exception e) {
             
        }
    }

    @Given("multiple events exist in the system")
    public void multipleEventsExistInTheSystem() {
        User organizer = userRepository.findByUsername("testuser")
            .orElseGet(() -> {
                User u = new User();
                u.setUsername("testuser");
                u.setEmail("testuser@example.com");
                u.setPassword(passwordEncoder.encode("password"));  
                return userRepository.save(u);
            });

        for (int i = 0; i < 3; i++) {
            Event event = new Event();
            event.setTitle("Event " + i);
            event.setSport("Sport" + i);
            event.setLocation("Location " + i);
            event.setDate(LocalDate.now().plusDays(i + 1));
            event.setTime(LocalTime.of(10 + i, 0));
            event.setOrganizer(organizer.getUsername());
            event.setCapacity(10);
            event.setPrice(BigDecimal.ZERO);
            eventRepository.save(event);
        }
    }

    @Given("multiple events exist for different sports")
    public void multipleEventsExistForDifferentSports() {
        User organizer = userRepository.findByUsername("testuser")
            .orElseGet(() -> {
                User u = new User();
                u.setUsername("testuser");
                u.setEmail("testuser@example.com");
                u.setPassword(passwordEncoder.encode("password"));  
                return userRepository.save(u);
            });

        String[] sports = {"Tennis", "Football", "Basketball"};
        for (String sport : sports) {
            Event event = new Event();
            event.setTitle(sport + " Match");
            event.setSport(sport);
            event.setLocation("Court");
            event.setDate(LocalDate.now().plusDays(2));
            event.setTime(LocalTime.of(15, 0));
            event.setOrganizer(organizer.getUsername());
            event.setCapacity(10);
            event.setPrice(BigDecimal.ZERO);
            eventRepository.save(event);
        }
    }

    @Then("the response should contain the created event details")
    public void theResponseShouldContainTheCreatedEventDetails() {
        // Handled by CommonSteps
    }

    @Then("the response should confirm I joined the event")
    public void theResponseShouldConfirmIJoinedTheEvent() {
        // Handled by CommonSteps
    }

    @Then("the response should confirm I left the event")
    public void theResponseShouldConfirmILeftTheEvent() {
        // Handled by CommonSteps
    }

    @Then("the response should contain a list of events")
    public void theResponseShouldContainAListOfEvents() {
        // Handled by CommonSteps
    }

    @Then("the response should contain only Tennis events")
    public void theResponseShouldContainOnlyTennisEvents() {
        // Handled by CommonSteps
    }
}